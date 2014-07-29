package org.szernex.yabm.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
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
	private String lastRun = "";
	private boolean backupActive = false;
	private String nextBackupTime = getNextIntervalTime();
	private ServerConfigurationManager serverConfigManager = MinecraftServer.getServer().getConfigurationManager();

	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent event)
	{
		String currenttime = getFormattedTime(System.currentTimeMillis());
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
		checkConsolidation();

		try
		{
			File targetpath = new File(ConfigHandler.targetPath).getCanonicalFile();
			File targetfile = new File(targetpath, getArchiveFileName(true));
			File rootpath = Paths.get("").toAbsolutePath().toFile();
			File worldpath = DimensionManager.getCurrentSaveRootDirectory().getCanonicalFile();
			MinecraftServer server = MinecraftServer.getServer();
			int counter = 0;
			String tempname = targetfile.getName();

			while (new File(tempname + ".zip").exists())
			{
				counter++;
				tempname = targetfile.getName() + "_" + counter;
			}

			targetfile = new File(targetfile.getParentFile(), tempname + ".zip");

			if (!targetpath.exists())
			{
				if (!targetpath.mkdirs())
				{
					LogHelper.warn("Could not create backup directory " + targetpath + " - Aborting backup");
					serverConfigManager.sendChatMsg(new ChatComponentText("Error creating backup: Could not create backup directory - Aborting."));
					return;
				}
			}

			LogHelper.info(String.format("Starting backup. Target file: %s; Root path: %s; World path: %s", targetfile, rootpath, worldpath));
			serverConfigManager.sendChatMsg(new ChatComponentText("Starting backup, prepare for possible lag..."));

			if (server.getConfigurationManager() != null)
			{
				server.getConfigurationManager().saveAllPlayerData();
			}

			WorldServer worldserver;
			boolean[] saveflags = new boolean[server.worldServers.length];

			LogHelper.info("Turning auto-save off and saving worlds...");
			serverConfigManager.sendChatMsg(new ChatComponentText("Turning auto-save off..."));

			for (int i = 0; i < server.worldServers.length; i++)
			{
				worldserver = server.worldServers[i];
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
			serverConfigManager.sendChatMsg(new ChatComponentText("Worlds saved."));

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
			serverConfigManager.sendChatMsg(new ChatComponentText("Turning auto-save back on..."));

			for (int i = 0; i < server.worldServers.length; i++)
			{
				server.worldServers[i].levelSaving = saveflags[i];
			}

			LogHelper.info("Backup finished.");
			serverConfigManager.sendChatMsg(new ChatComponentText("Backup finished."));
		}
		catch (IOException ex)
		{
			LogHelper.error("Error creating backup: " + ex.getMessage());
			serverConfigManager.sendChatMsg(new ChatComponentText("Error creating backup: " + ex.getMessage()));
			ex.printStackTrace();
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
			serverConfigManager.sendChatMsg(new ChatComponentText("Error during consolidation: " + ex.getMessage()));
			ex.printStackTrace();
			return;
		}

		if (files != null)
		{
			LogHelper.info(files.length);
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

		LogHelper.info("Maximum backup count exceeded; deleting " + files.length + " old backups...");
		serverConfigManager.sendChatMsg(new ChatComponentText("Maximum backup count exceeded; deleting " + files.length + " old backups..."));

		for (int i = 0; i < files.length; i++)
		{
			if (files[i].delete())
			{
				LogHelper.debug("Deleted old backup " + files[i].getName());
			}
			else
			{
				LogHelper.warn("Could not delete backup " + files[i].getName());
			}
		}
	}

	private String getNextIntervalTime()
	{
		LogHelper.info(System.currentTimeMillis() + (ConfigHandler.backupInterval * 60 * 1000));
		return getFormattedTime(System.currentTimeMillis() + (ConfigHandler.backupInterval * 60 * 1000));
	}

	private String getFileTimestamp()
	{
		SimpleDateFormat format = new SimpleDateFormat(ConfigHandler.timestampFormat);

		return format.format(new Date());
	}

	private String getFormattedTime(long timestamp)
	{
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");

		return format.format(new Date(timestamp));
	}

	private String getArchiveFileName(boolean includetimestamp)
	{
		String output;

		if (MinecraftServer.getServer().isDedicatedServer())
		{
			output = String.format("%s_%s", ConfigHandler.filePrefix, (includetimestamp ? getFileTimestamp() : ""));
		}
		else
		{
			output = String.format("%s_%s_%s", ConfigHandler.filePrefix, MinecraftServer.getServer().getWorldName(), (includetimestamp ? getFileTimestamp() : ""));
		}

		return output;
	}
}
