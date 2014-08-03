package org.szernex.yabm.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.szernex.yabm.util.ChatHelper;
import org.szernex.yabm.util.FileHelper;
import org.szernex.yabm.util.LogHelper;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class BackupTickHandler
{
	public static final String TIMESTAMP_FORMAT = "y-MM-dd_HH-mm-ss";

	private String lastRun = "";
	private boolean backupActive = false;
	private String nextBackupTime = getNextIntervalTime();
	private ServerConfigurationManager serverConfigManager = MinecraftServer.getServer().getConfigurationManager();

	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent event)
	{
		String currenttime = getFormattedTime(System.currentTimeMillis(), "HH:mm");
		boolean runbackup = false;

		if (!ConfigHandler.backupEnabled
				|| event.phase == TickEvent.Phase.START
				|| currenttime.equalsIgnoreCase(lastRun)
				|| backupActive)
		{
			return;
		}

		if (ConfigHandler.enableIntervalBackup)
		{
			runbackup = currenttime.equalsIgnoreCase(nextBackupTime);
		}
		else
		{
			String[] times = ConfigHandler.backupTimes;

			for (String t : times)
			{
				if (currenttime.equalsIgnoreCase(t))
				{
					runbackup = true;
					break;
				}
			}
		}

		if (runbackup)
		{
			backupActive = true;
			startBackup();
			backupActive = false;
			lastRun = currenttime;
			nextBackupTime = getNextIntervalTime();
		}
	}

	public void startBackup()
	{
		if (ConfigHandler.enablePersistentBackup && isFirstBackup())
		{
			LogHelper.info("Starting persistent backup...");
			serverConfigManager.sendChatMsg(ChatHelper.getLocalizedChatComponent("yabm.backup.general.persistent_start"));

			backup(ConfigHandler.persistentPath);
		}
		else
		{
			backup(ConfigHandler.targetPath);
		}
	}

	private void backup(String path)
	{
		checkConsolidation();

		try
		{
			File targetpath = new File(path).getCanonicalFile();
			File targetfile = new File(targetpath, getArchiveFileName(true) + ".zip");
			File rootpath = Paths.get("").toAbsolutePath().toFile();
			File worldpath = DimensionManager.getCurrentSaveRootDirectory().getCanonicalFile();
			MinecraftServer server = MinecraftServer.getServer();

			if (targetfile.exists())
			{
				LogHelper.warn("File " + targetfile.toString() + " already exists - Aborting");
				return;
			}

			if (!targetpath.exists())
			{
				if (!targetpath.mkdirs())
				{
					LogHelper.warn("Could not create backup directory " + targetpath + " - Aborting backup");
					serverConfigManager.sendChatMsg(ChatHelper.getLocalizedChatComponent("yabm.backup.error.create_dir_failed"));
					return;
				}
			}

			LogHelper.info(String.format("Starting backup. Target file: %s; Root path: %s; World path: %s", targetfile, rootpath, worldpath));
			serverConfigManager.sendChatMsg(ChatHelper.getLocalizedChatComponent("yabm.backup.general.backup_start"));

			serverConfigManager.saveAllPlayerData();

			boolean[] saveflags = new boolean[server.worldServers.length];

			LogHelper.info("Turning auto-save off and saving worlds...");
			serverConfigManager.sendChatMsg(ChatHelper.getLocalizedChatComponent("yabm.backup.general.autosave_off"));

			for (int i = 0; i < server.worldServers.length; i++)
			{
				WorldServer worldserver = server.worldServers[i];
				saveflags[i] = worldserver.levelSaving;

				try
				{
					worldserver.saveAllChunks(true, null);
					worldserver.saveChunkData();
					LogHelper.debug("Saved " + worldserver);
				}
				catch (MinecraftException ex)
				{
					LogHelper.warn("Failed to save " + worldserver + ": " + ex.getMessage());
					ex.printStackTrace();
				}
			}

			LogHelper.info("Worlds saved...");
			serverConfigManager.sendChatMsg(ChatHelper.getLocalizedChatComponent("yabm.backup.general.worlds_saved"));

			Set<File> backuplist = new HashSet<File>();

			for (String entry : ConfigHandler.includeList)
			{
				File f = new File(rootpath, entry);

				if (f.isDirectory())
				{
					backuplist.addAll(FileHelper.getDirectoryContents(f));
				}
				else
				{
					backuplist.add(f);
				}
			}

			backuplist.addAll(FileHelper.getDirectoryContents(worldpath));

			LogHelper.debug("Backup list:\n" + backuplist);
			LogHelper.info("Saving backup to " + targetfile);

			FileHelper.createZipArchive(targetfile, backuplist);

			LogHelper.info("Turning auto-save on...");
			serverConfigManager.sendChatMsg(ChatHelper.getLocalizedChatComponent("yabm.backup.general.autosave_on"));

			for (int i = 0; i < server.worldServers.length; i++)
			{
				server.worldServers[i].levelSaving = saveflags[i];
			}

			LogHelper.info("Backup finished.");
			serverConfigManager.sendChatMsg(ChatHelper.getLocalizedChatComponent("yabm.backup.general.backup_finished"));
		}
		catch (IOException ex)
		{
			LogHelper.error("Error creating backup: " + ex.getMessage());
			ex.printStackTrace();
			serverConfigManager.sendChatMsg(ChatHelper.getLocalizedChatComponent("yabm.backup.error.create_backup_failed", ex.getMessage()));
		}
	}

	private boolean isFirstBackup()
	{
		File targetpath;
		File[] files;
		String timestamp = getFormattedTime(System.currentTimeMillis(), TIMESTAMP_FORMAT);
		final String filter = String.format("%s%s", getArchiveFileName(false), timestamp.substring(0, timestamp.indexOf("_")));

		LogHelper.info(filter);

		try
		{
			targetpath = new File(ConfigHandler.persistentPath).getCanonicalFile();

			if (!targetpath.exists())
			{
				return true;
			}

			files = targetpath.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File file)
				{
					return (file.isFile() && file.getName().startsWith(filter));
				}
			});

			LogHelper.info(files.length);

			return (files.length == 0);
		}
		catch (IOException ex)
		{
			LogHelper.error("Error reading persistent directory: " + ex.getMessage());
			ex.printStackTrace();
			serverConfigManager.sendChatMsg(ChatHelper.getLocalizedChatComponent("yabm.backup.error.persistent_failed", ex.getMessage()));
			return false;
		}
	}

	private void checkConsolidation()
	{
		File targetpath;
		File[] files;
		int backupcount = ConfigHandler.maxBackupCount - 1;

		try
		{
			targetpath = new File(ConfigHandler.targetPath).getCanonicalFile();
			files = targetpath.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File file)
				{
					return (file.isFile() && file.getName().startsWith(getArchiveFileName(false)));
				}
			});
		}
		catch (IOException ex)
		{
			LogHelper.error("Error during consolidation: " + ex.getMessage());
			ex.printStackTrace();
			serverConfigManager.sendChatMsg(ChatHelper.getLocalizedChatComponent("yabm.backup.error.consolidation_failed", ex.getMessage()));
			return;
		}

		if (files == null || files.length <= backupcount)
		{
			return;
		}

		Arrays.sort(files, new Comparator<File>()
		{
			@Override
			public int compare(File file, File file2)
			{
				return file.compareTo(file2);
			}
		});

		files = Arrays.copyOfRange(files, 0, files.length - backupcount);

		LogHelper.info("Deleting " + files.length + " old backups...");
		serverConfigManager.sendChatMsg(ChatHelper.getLocalizedChatComponent("yabm.backup.general.consolidate_backups", files.length));

		for (File f : files)
		{
			if (f.delete())
			{
				LogHelper.debug("Deleted old backup " + f.getName());
			}
			else
			{
				LogHelper.warn("Could not delete backup " + f.getName());
			}
		}
	}

	private String getNextIntervalTime()
	{
		return getFormattedTime(System.currentTimeMillis() + (ConfigHandler.backupInterval * 60 * 1000), "HH:mm");
	}

	private String getFormattedTime(long timestamp, String format)
	{
		SimpleDateFormat dateformat = new SimpleDateFormat(format);

		return dateformat.format(new Date(timestamp));
	}

	private String getArchiveFileName(boolean includetimestamp)
	{
		String output;

		if (MinecraftServer.getServer().isDedicatedServer())
		{
			output = String.format("%s_%s", ConfigHandler.filePrefix, (includetimestamp ? getFormattedTime(System.currentTimeMillis(), TIMESTAMP_FORMAT) : ""));
		}
		else
		{
			output = String.format("%s_%s_%s", ConfigHandler.filePrefix, MinecraftServer.getServer().getWorldName(), (includetimestamp ? getFormattedTime(System.currentTimeMillis(), TIMESTAMP_FORMAT) : ""));
		}

		return output;
	}
}
