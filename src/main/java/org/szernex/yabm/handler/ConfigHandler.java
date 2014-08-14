package org.szernex.yabm.handler;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.szernex.yabm.reference.Reference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigHandler
{
	public static Configuration configuration;

	public static Map<String, Object> properties = new HashMap<String, Object>();
	public static boolean backupEnabled = true;
	public static String backupLocation = "../backups";
	public static String[] backupList = new String[]{"banned-ips.json", "banned-players.json", "ops.json", "options.json", "options.txt", "server.properties", "usercache.json", "whitelist.json", "config", "crash-reports", "logs", "mods", "resourcepacks"};
	public static String backupPrefix = "backup";
	public static String backupSchedule = "12:00";
	public static boolean persistentEnabled = true;
	public static String persistentLocation = "../backups/persistent";
	public static int maxBackupCount = 10;
	public static int compressionLevel = 9;
	public static boolean ftpEnabled = false;
	public static String ftpLocation = "backups";
	public static String ftpServer = "";
	public static int ftpPort = 21;
	public static String ftpUsername = "";
	public static String ftpPassword = "";


	public static void init(File config_file)
	{
		if (configuration == null)
		{
			configuration = new Configuration(config_file);
			loadConfig();
		}
	}

	public static void loadConfig()
	{
		String cat_general = Configuration.CATEGORY_GENERAL;

		properties.put("backupEnabled",
		               backupEnabled = configuration.getBoolean(
				               "backupEnabled",
				               cat_general,
				               true,
				               "Turn automatic backups on/off."
		               ));
		properties.put("backupLocation",
		               backupLocation = configuration.getString(
				               "backupLocation",
				               cat_general,
				               "../backups",
				               "The path where to store backups. Can be a path relative to the Minecraft installation root."
		               ));
		properties.put("backupList",
		               backupList = configuration.getStringList(
				               "backupList",
				               cat_general,
				               backupList,
				               "The list of files and folders to include in the backup."
		               ));
		properties.put("backupPrefix",
		               backupPrefix = configuration.getString(
				               "backupPrefix",
				               cat_general,
				               "backup",
				               "The file prefix for the archive files. Final result will be 'prefix_TIMESTAMP.zip' on SMP or 'prefix_saveName_TIMESTAMP.zip' on SP."
		               ));
		properties.put("backupSchedule",
		               backupSchedule = configuration.getString(
				               "backupSchedule",
				               cat_general,
				               "12:00",
				               "The schedule for automatic backups. Can either be a single number indicating the interval in minutes in which backups should be made, or a list of times in 24h format seperated by spaces. (ex.: 180 - does a backup every 3 hours; 4:00 12:00 20:00 - does a backup at the specified times)\nInterval backups are recommended for single player while scheduled backups are recommended for dedicated servers."
		               ));
		properties.put("persistentEnabled",
		               persistentEnabled = configuration.getBoolean(
				               "persistentEnabled",
				               cat_general,
				               true,
				               "Turn persistent backups on/off. When enabled the first backup of each day will be stored in a separate location and is excluded from automatic consolidation."
		               ));
		properties.put("persistentLocation",
		               persistentLocation = configuration.getString(
				               "persistentLocation",
				               cat_general,
				               "../backups/persistent",
				               "The path where to store persistent backups. Can be a path relative to the Minecraft installation root."
		               ));
		properties.put("maxBackupCount",
		               maxBackupCount = configuration.getInt(
				               "maxBackupCount",
				               cat_general,
				               10,
				               0,
				               Integer.MAX_VALUE,
				               "The maximum number of backups to keep per world. -1 disables this functionality. Persistent backups are excluded from this."
		               ));
		properties.put("compressionLevel",
		               compressionLevel = configuration.getInt(
				               "compressionLevel",
				               cat_general,
				               9,
				               0,
				               9,
				               "The zip compression level to use, 0 being no compression and 9 maximum compression."
		               ));

		properties.put("ftpEnabled",
		               ftpEnabled = configuration.getBoolean(
				               "ftpEnabled",
				               cat_general,
				               false,
				               "Turns uploading backups via FTP on/off. If you want to not keep any local backups set maxBackupCount to 0 and persistentEnabled to false."
		               ));
		properties.put("ftpLocation",
		               ftpLocation = configuration.getString(
				               "ftpLocation",
				               cat_general,
				               "backups",
				               "The remote location on the FTP server to store backups."
		               ));
		properties.put("ftpServer",
		               ftpServer = configuration.getString(
				               "ftpServer",
				               cat_general,
				               "",
				               "The FTP server address."
		               ));
		properties.put("ftpPort",
		               ftpPort = configuration.getInt(
				               "ftpPort",
				               cat_general,
				               21,
				               1,
				               Integer.MAX_VALUE,
				               "The port to use."
		               ));
		properties.put("ftpUsername",
		               ftpUsername = configuration.getString(
				               "ftpUsername",
				               cat_general,
				               "",
				               "The username to use when logging into the FTP server."
		               ));
		properties.put("ftpPassword",
		               ftpPassword = configuration.getString(
				               "ftpPassword",
				               cat_general,
				               "",
				               "The password to use when logging into the FTP server."
		               ));

		if (configuration.hasChanged())
		{
			configuration.save();
		}
	}

	public static Object getValue(String key)
	{
		if (properties.containsKey(key))
		{
			return properties.get(key);
		}

		return null;
	}

	public static void setValue(String key, String... value)
	{
		if (!properties.containsKey(key))
		{
			return;
		}

		Property property = configuration.get(Configuration.CATEGORY_GENERAL, key, "");
		Object v = getValue(key);

		if (v instanceof String)
		{
			property.set(String.valueOf(value[0]));
		}
		else if (v instanceof String[])
		{
			property.set(value);
		}
		else if (v instanceof Integer)
		{
			property.set(Integer.valueOf(value[0]));
		}
		else if (v instanceof Double)
		{
			property.set(Double.valueOf(value[0]));
		}
		else if (v instanceof Boolean)
		{
			property.set(Boolean.valueOf(value[0]));
		}

		configuration.save();
		loadConfig();
	}

	@SubscribeEvent
	public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if (event.modID.equalsIgnoreCase(Reference.MOD_ID))
		{
			loadConfig();
		}
	}
}
