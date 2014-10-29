package org.szernex.yabm.core;

import org.apache.commons.net.ftp.FTPClient;
import org.szernex.yabm.util.ChatHelper;
import org.szernex.yabm.util.LogHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FTPTask implements Runnable
{
	private File targetFile;
	private String ftpServer;
	private int ftpPort;
	private String ftpUsername;
	private String ftpPassword;
	private String ftpLocation;
	private boolean lastTaskSucceeded = false;

	public boolean didLastTaskSucceed()
	{
		return lastTaskSucceeded;
	}

	public void init(File target_file, String server, int port, String username, String password, String location)
	{
		targetFile = target_file;
		ftpServer = server;
		ftpPort = port;
		ftpUsername = username;
		ftpPassword = password;
		ftpLocation = location;
	}

	@Override
	public void run()
	{
		LogHelper.info("FTP upload starting...");
		ChatHelper.sendServerChatMsg(ChatHelper.getLocalizedMsg("yabm.backup.ftp.start"));

		FTPClient ftp_client = new FTPClient();

		lastTaskSucceeded = false;

		try
		{
			LogHelper.info("Connecting to server %s:%d", ftpServer, ftpPort);
			ftp_client.connect(ftpServer, ftpPort);

			LogHelper.info("Authenticating with credentials...");
			if (ftp_client.login(ftpUsername, ftpPassword))
			{
				LogHelper.info("Successfully logged in.");

				LogHelper.info("Entering passive mode...");
				ftp_client.enterLocalPassiveMode();
				ftp_client.setFileType(FTPClient.BINARY_FILE_TYPE);

				InputStream input_stream = new FileInputStream(targetFile);

				LogHelper.info("Starting upload...");

				boolean success = ftp_client.storeFile(new File(ftpLocation, targetFile.getName()).getPath(), input_stream);

				input_stream.close();

				if (success)
				{
					ChatHelper.sendServerChatMsg(ChatHelper.getLocalizedMsg("yabm.backup.ftp.upload_success"));
					LogHelper.info("Uploaded successfully.");
					lastTaskSucceeded = true;
				}
				else
				{
					ChatHelper.sendServerChatMsg(ChatHelper.getLocalizedMsg("yabm.backup.error.ftp.upload_failed"));
					LogHelper.warn("Upload failed.");

					String[] replies = ftp_client.getReplyStrings();

					for (int i = 0; i < replies.length; i++)
					{
						LogHelper.warn(replies[i]);
					}
				}
			}
			else
			{
				LogHelper.warn("Failed to login - Aborting upload.");
			}
		}
		catch (IOException ex)
		{
			LogHelper.error("FTP upload failed: %s", ex.getMessage());
			ex.printStackTrace();
			ChatHelper.sendServerChatMsg(ChatHelper.getLocalizedMsg("yabm.backup.error.ftp.upload_failed"));
		}
		finally
		{
			try
			{
				if (ftp_client.isConnected())
				{
					ftp_client.logout();
					ftp_client.disconnect();
				}
			}
			catch (IOException ex)
			{
				LogHelper.error("Error while closing FTP connection: %s", ex.getMessage());
				ex.printStackTrace();
			}
		}
	}
}
