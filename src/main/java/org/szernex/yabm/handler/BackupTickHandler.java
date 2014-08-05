package org.szernex.yabm.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import org.szernex.yabm.backup.BackupThread;
import org.szernex.yabm.util.BackupHelper;
import org.szernex.yabm.util.ChatHelper;
import org.szernex.yabm.util.LogHelper;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class BackupTickHandler
{
	private String lastRun = "";
	private boolean backupActive = false;
	private String nextBackupTime = BackupHelper.getNextIntervalTime();
	private ServerConfigurationManager serverConfigManager = MinecraftServer.getServer().getConfigurationManager();
	private BackupThread backupThread = new BackupThread();

	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent event)
	{
		String currenttime = BackupHelper.getFormattedTime(System.currentTimeMillis(), "HH:mm");
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
			nextBackupTime = BackupHelper.getNextIntervalTime();
		}
	}

	public void startBackup()
	{
		if (backupThread.isRunning)
		{
			LogHelper.warn("Backup thread is already running - aborting.");
			serverConfigManager.sendChatMsg(ChatHelper.getLocalizedChatComponent("yabm.backup.error.backup_already_running"));
		}
		else
		{
			String targetpath;

			if (ConfigHandler.enablePersistentBackup && isFirstBackup())
			{
				LogHelper.info("Starting persistent backup...");
				serverConfigManager.sendChatMsg(ChatHelper.getLocalizedChatComponent("yabm.backup.general.persistent_start"));
				targetpath = ConfigHandler.persistentPath;
			}
			else
			{
				LogHelper.info("Starting regular backup...");
				serverConfigManager.sendChatMsg(ChatHelper.getLocalizedChatComponent("yabm.backup.general.backup_start"));
				targetpath = ConfigHandler.targetPath;
			}

			backupThread.setTargetPath(targetpath);
			new Thread(backupThread).run();
		}
	}

	private boolean isFirstBackup()
	{
		File targetpath;
		File[] files;
		String timestamp = BackupHelper.getFormattedTime(System.currentTimeMillis(), null);
		final String filter = String.format("%s%s", BackupHelper.getArchiveFileName(false), timestamp.substring(0, timestamp.indexOf("_")));

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








}
