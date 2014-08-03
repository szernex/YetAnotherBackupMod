package org.szernex.yabm.handler;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;
import org.szernex.yabm.reference.Reference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigHandler
{
	public static Configuration configuration;

	public static Map<String, Object> properties = new HashMap<String, Object>();
	public static String targetPath = "../backups";
	public static String[] backupTimes = {"12:00"};
	public static boolean backupEnabled = true;
	public static String[] includeList = {"banned-ips.json", "banned-players.json", "ops.json", "options.json", "server.properties", "usercache.json", "whitelist.json", "config", "crash-reports", "logs", "mods", "resourcepacks"};
	public static String filePrefix = "backup";
	//public static String timestampFormat = "y-MM-dd_HH-mm-ss";
	public static int maxBackupCount = 5;
	public static int backupInterval = 180;
	public static boolean enableIntervalBackup = false;
	public static boolean enablePersistentBackup = false;
	public static String persistentPath = "../backups/persistent";

	public static void init(File configfile)
	{
		if (configuration == null)
		{
			configuration = new Configuration(configfile);
			loadConfig();
		}
	}

	public static void loadConfig()
	{
		properties.put("targetPath", targetPath = configuration.getString("targetPath", Configuration.CATEGORY_GENERAL, "../backups", "The path where to store the backups. Can be a relative path in relation to the Minecraft installation root."));
		properties.put("backupTimes", backupTimes = configuration.getStringList("backupTimes", Configuration.CATEGORY_GENERAL, backupTimes, "The times when to run a backup in 24h format."));
		properties.put("backupEnabled", backupEnabled = configuration.getBoolean("backupEnabled", Configuration.CATEGORY_GENERAL, true, "Turns automatic backups on/off."));
		properties.put("includeList", includeList = configuration.getStringList("includeList", Configuration.CATEGORY_GENERAL, includeList, "List of files and folders to backup additionally to the current world save."));
		properties.put("filePrefix", filePrefix = configuration.getString("filePrefix", Configuration.CATEGORY_GENERAL, "backup", "The prefix for the backup files. On SP it will be formatted 'prefix_worldname_timestamp', on dedicated servers 'prefix_timestamp'."));
		//properties.put("timestampFormat", timestampFormat = configuration.getString("timestampFormat", Configuration.CATEGORY_GENERAL, "y-MM-dd_HH-mm-ss", "The format for the backup timestamp in Javas DateFormat format"));
		properties.put("maxBackupCount", maxBackupCount = configuration.getInt("maxBackupCount", Configuration.CATEGORY_GENERAL, 5, 1, 100, "The maximum number of backups to keep. If the backup count exceeds this number the oldest backup(s) will be deleted."));
		properties.put("backupInterval", backupInterval = configuration.getInt("backupInterval", Configuration.CATEGORY_GENERAL, 180, 5, Integer.MAX_VALUE, "The interval in minutes between backups. enableIntervalBackup needs to be set to true to enable this. Especially useful in single player worlds."));
		properties.put("enableIntervalBackup", enableIntervalBackup = configuration.getBoolean("enableIntervalBackup", Configuration.CATEGORY_GENERAL, false, "When this is set to true backupTimes will be ignored and instead backups get created every X minutes specified via backupInterval."));
		properties.put("enablePersistentBackup", enablePersistentBackup = configuration.getBoolean("enablePersistentBackup", Configuration.CATEGORY_GENERAL, false, "Whether to enable persistent backups or not. The first backup of each day will be persistent and stored in persistentPath."));
		properties.put("persistentPath", persistentPath = configuration.getString("persistentPath", Configuration.CATEGORY_GENERAL, "../backups/persistent", "The path where to store persistent backups. Can be a relative path in relation to the Minecraft installation root."));

		if (configuration.hasChanged())
		{
			configuration.save();
		}
	}

	@SubscribeEvent
	public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if (event.modID.equalsIgnoreCase(Reference.MOD_ID))
		{
			loadConfig();
		}
	}
}