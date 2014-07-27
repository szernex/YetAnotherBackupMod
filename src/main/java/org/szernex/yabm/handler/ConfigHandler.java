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
	public static String    targetPath = "../backups";
	public static String[]  backupTimes = {"12:00"};
	public static boolean   backupEnabled = true;
	public static String[]  includeList = {"banned-ips.json", "banned-players.json", "ops.json", "options.json", "server.properties", "usercache.json", "whitelist.json", "config", "crash-reports", "logs", "mods", "resourcepacks"};
	public static String    filePrefix = "backup_";
	public static String    timestampFormat = "y-MM-dd_HH-mm-ss";

	public static void init(File configfile)
	{
		if (configuration == null)
		{
			configuration = new Configuration(configfile);
			loadConfig();
		}
	}

	private static void loadConfig()
	{
		properties.put("targetpath", targetPath = configuration.getString("targetPath", Configuration.CATEGORY_GENERAL, "../backups", "The path where to store the backups."));
		properties.put("backuptimes", backupTimes = configuration.getStringList("backupTimes", Configuration.CATEGORY_GENERAL, new String[]{"12:00"}, "The times when to run a backup in 24h format."));
		properties.put("backupenabled", backupEnabled = configuration.getBoolean("backupEnabled", Configuration.CATEGORY_GENERAL, true, "Turns automatic backups on/off."));
		properties.put("includelist", includeList = configuration.getStringList("includeList", Configuration.CATEGORY_GENERAL, includeList, "List of files and folders to backup additionally to the current world save."));
		properties.put("fileprefix", filePrefix = configuration.getString("filePrefix", Configuration.CATEGORY_GENERAL, "backup_", "The prefix for the backup files."));
		properties.put("timestampformat", timestampFormat = configuration.getString("timestampFormat", Configuration.CATEGORY_GENERAL, "y-MM-dd_HH-mm-ss", "The format for the backup timestamp in Javas DateFormat format"));

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