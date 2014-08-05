package org.szernex.yabm.util;

import net.minecraft.server.MinecraftServer;
import org.szernex.yabm.handler.ConfigHandler;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupHelper
{
	public static final String TIMESTAMP_FORMAT = "y-MM-dd_HH-mm-ss";

	public static String getArchiveFileName(boolean includetimestamp)
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

	public static String getFormattedTime(long timestamp, String format)
	{
		if (format == null)
		{
			format = TIMESTAMP_FORMAT;
		}

		SimpleDateFormat dateformat = new SimpleDateFormat(format);

		return dateformat.format(new Date(timestamp));
	}

	public static String getNextIntervalTime()
	{
		return getFormattedTime(System.currentTimeMillis() + (ConfigHandler.backupInterval * 60 * 1000), "HH:mm");
	}
}
