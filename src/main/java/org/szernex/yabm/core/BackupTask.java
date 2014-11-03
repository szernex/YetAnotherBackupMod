package org.szernex.yabm.core;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.szernex.yabm.util.ChatHelper;
import org.szernex.yabm.util.FileHelper;
import org.szernex.yabm.util.LogHelper;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class BackupTask implements Runnable
{
	private static final ReentrantLock backupLock = new ReentrantLock();

	private String targetPath;
	private String filePrefix;
	private String[] sourceList;
	private int compressionLevel;
	private File lastBackupFile;
	private ServerConfigurationManager serverConfigurationManager = MinecraftServer.getServer().getConfigurationManager();

	public File getLastBackupFile()
	{
		return lastBackupFile;
	}

	public void init(String target_path, String file_prefix, String[] source_list, int compression_level)
	{
		targetPath = target_path;
		filePrefix = file_prefix;
		sourceList = source_list;
		compressionLevel = compression_level;
	}

	public void run()
	{
		if (!backupLock.tryLock())
		{
			LogHelper.warn("Could not acquire backup lock - Aborting.");
			ChatHelper.sendServerChatMsg(ChatHelper.getLocalizedMsg("yabm.backup.error.lock_failed"));
			return;
		}

		LogHelper.info("Backup lock acquired, starting backup...");
		lastBackupFile = null;

		try
		{
			File target_dir = new File(targetPath).getCanonicalFile();
			File world_dir = new File(new File(".").toURI().relativize(DimensionManager.getCurrentSaveRootDirectory().toURI()).getPath());
			File target_file = new File(target_dir, FileHelper.getArchiveFileName(filePrefix, true) + ".zip");

			if (target_file.exists())
			{
				LogHelper.error("Target file %s already exists - Aborting.", target_file);
				return;
			}

			if (!target_dir.exists())
			{
				if (!target_dir.mkdirs())
				{
					LogHelper.error("Could not create target directory %s - Aborting.", target_dir);
					return;
				}
			}

			LogHelper.info("Backup settings: Target file: %s; World directory: %s", target_file, world_dir);
			ChatHelper.sendServerChatMsg(ChatHelper.getLocalizedMsg("yabm.backup.general.backup_started"));


			serverConfigurationManager.saveAllPlayerData();
			LogHelper.info("Player data saved...");


			MinecraftServer server = MinecraftServer.getServer();
			boolean[] save_flags = new boolean[server.worldServers.length];

			// disable auto-save
			LogHelper.info("Turning auto-save off and saving worlds...");
			for (int i = 0; i < server.worldServers.length; i++)
			{
				WorldServer world_server = server.worldServers[i];
				save_flags[i] = world_server.levelSaving;
				world_server.levelSaving = false;

				try
				{
					world_server.saveAllChunks(true, null);
					world_server.saveChunkData();
					LogHelper.debug("Saved %s.", world_server);
				}
				catch (MinecraftException ex)
				{
					LogHelper.warn("Error saving %s: %s", world_server, ex.getMessage());
					ex.printStackTrace();
				}
			}
			LogHelper.info("Worlds saved.");
			ChatHelper.sendServerChatMsg(ChatHelper.getLocalizedMsg("yabm.backup.general.worlds_saved"));


			// create backup archive
			Set<File> source_files = FileHelper.getFileList(sourceList);

			source_files.addAll(FileHelper.getDirectoryContents(world_dir));
			LogHelper.info("Archiving %d files...", source_files.size());

			if (FileHelper.createZipArchive(target_file, source_files, compressionLevel))
			{
				LogHelper.info("Successfully created backup archive.");
			}
			else
			{
				LogHelper.warn("Failed to create backup archive.");
				ChatHelper.sendServerChatMsg(ChatHelper.getLocalizedMsg("yabm.backup.error.archive_failed"));
			}


			// re-enable auto-save
			LogHelper.info("Turning auto-save back on...");
			for (int i = 0; i < server.worldServers.length; i++)
			{
				server.worldServers[i].levelSaving = save_flags[i];
			}

			lastBackupFile = target_file;
			LogHelper.info("Backup finished.");
			ChatHelper.sendServerChatMsg(ChatHelper.getLocalizedMsg("yabm.backup.general.backup_finished"));
		}
		catch (IOException ex)
		{
			LogHelper.error("Error during backup: " + ex.getMessage());
			ex.printStackTrace();
			ChatHelper.sendServerChatMsg(ChatHelper.getLocalizedMsg("yabm.backup.error.backup_failed", ex.getMessage()));
		}
		finally
		{
			backupLock.unlock();
			LogHelper.info("Backup lock released.");
		}
	}
}
