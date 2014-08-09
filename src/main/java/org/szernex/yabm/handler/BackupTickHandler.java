package org.szernex.yabm.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import org.szernex.yabm.core.BackupTask;
import org.szernex.yabm.util.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class BackupTickHandler
{
	Time nextSchedule;
	BackupTask backupTask = new BackupTask();

	public BackupTickHandler()
	{
		updateScheduleTime();
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event)
	{
		Time current_time = new Time(System.currentTimeMillis());

		if (event.phase == TickEvent.Phase.START
				|| !ConfigHandler.backupEnabled
				|| current_time.compareTo(nextSchedule) != 0)
		{
			return;
		}

		startBackup();
		updateScheduleTime();
	}

	public void startBackup()
	{
		if (isPersistentBackup())
		{
			backupTask.init(ConfigHandler.persistentLocation, ConfigHandler.backupPrefix, ConfigHandler.backupList);
		}
		else
		{
			backupTask.init(ConfigHandler.backupLocation, ConfigHandler.backupPrefix, ConfigHandler.backupList);
		}

		new Thread(backupTask).run();
		consolidateBackups();
	}

	public void updateScheduleTime()
	{
		nextSchedule = ScheduleHelper.getNextSchedule(ConfigHandler.backupSchedule.trim().split(" "));
	}

	private boolean isPersistentBackup()
	{
		if (!ConfigHandler.persistentEnabled)
		{
			return false;
		}

		String archive_name = FileHelper.getArchiveFileName(ConfigHandler.backupPrefix, true);

		archive_name = archive_name.substring(0, archive_name.lastIndexOf("_"));

		try
		{
			File target_path = new File(ConfigHandler.persistentLocation).getCanonicalFile();

			if (!target_path.exists())
			{
				if (!target_path.mkdirs())
				{
					LogHelper.error("Could not create persistent backup directory %s", target_path.toString());
					return false;
				}

				return true;
			}

			File[] files = target_path.listFiles(new FileHelper.BackupFileFilter(archive_name));

			return (files.length == 0);
		}
		catch (IOException ex)
		{
			LogHelper.error("Error during persistent backup detection: %s" , ex.getMessage());
			ex.printStackTrace();
			return false;
		}
	}

	private void consolidateBackups()
	{
		if (ConfigHandler.maxBackupCount <= 0)
		{
			return;
		}

		String archive_name = FileHelper.getArchiveFileName(ConfigHandler.backupPrefix, false);

		try
		{
			File target_path = new File(ConfigHandler.backupLocation).getCanonicalFile();

			if (!target_path.exists())
			{
				return;
			}

			File[] files = target_path.listFiles(new FileHelper.BackupFileFilter(archive_name));

			if (files.length <= ConfigHandler.maxBackupCount)
			{
				return;
			}

			Arrays.sort(files);

			files = Arrays.copyOfRange(files, 0, files.length - ConfigHandler.maxBackupCount);
			ChatHelper.sendServerChatMsg(ChatHelper.getLocalizedMsg("yabm.backup.general.backup_consolidation", files.length));

			for (File f : files)
			{
				if (f.delete())
				{
					LogHelper.info("Deleted old backup %s", f);
				}
				else
				{
					LogHelper.warn("Could not delete old backup %s", f);
				}
			}
		}
		catch (IOException ex)
		{
			LogHelper.error("Error during consolidation: %s", ex.getMessage());
			ex.printStackTrace();
		}
	}
}
