package org.szernex.yabm.core;

import org.szernex.yabm.handler.ConfigHandler;
import org.szernex.yabm.util.ChatHelper;
import org.szernex.yabm.util.FileHelper;
import org.szernex.yabm.util.LogHelper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class BackupManager implements Runnable
{
	private BackupTask backupTask = new BackupTask();
	private FTPTask ftpTask = new FTPTask();
	private boolean running = false;


	public boolean isRunning()
	{
		return running;
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
			LogHelper.error("Error during persistent backup detection: %s", ex.getMessage());
			ex.printStackTrace();
			return false;
		}
	}

	private void consolidateBackups()
	{
		if (ConfigHandler.maxBackupCount < 0)
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
			ChatHelper.sendServerChatMsg(ChatHelper.getLocalizedMsg("yabm.backup.general.backup_consolidation", files.length, (files.length > 0 ? "s" : "")));

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

	private void startAndWaitForThread(Runnable task)
	{
		Thread thread = new Thread(task);

		thread.start();

		try
		{
			thread.join();
		}
		catch (InterruptedException ex)
		{
			LogHelper.error("Thread got interrupted: %s", ex.getMessage());
			ex.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		running = true;

		boolean do_consolidation = true;

		backupTask.init((isPersistentBackup() ? ConfigHandler.persistentLocation : ConfigHandler.backupLocation),
		                ConfigHandler.backupPrefix,
		                ConfigHandler.backupList,
		                ConfigHandler.compressionLevel
		);

		startAndWaitForThread(backupTask);

		if (ConfigHandler.ftpEnabled)
		{
			if (backupTask.getLastBackupFile() != null)
			{
				ftpTask.init(backupTask.getLastBackupFile(),
				             ConfigHandler.ftpServer,
				             ConfigHandler.ftpPort,
				             ConfigHandler.ftpUsername,
				             ConfigHandler.ftpPassword,
				             ConfigHandler.ftpLocation
				);
			}

			startAndWaitForThread(ftpTask);
			do_consolidation = ftpTask.didLastTaskSucceed();
		}

		if (do_consolidation)
		{
			consolidateBackups();
		}

		running = false;
	}

	public void startBackup()
	{
		new Thread(this).start();
	}
}
