package org.szernex.yabm.handler;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;
import org.szernex.yabm.reference.Reference;

import java.io.File;

public class ConfigHandler
{
	public static Configuration configuration;

	//public static boolean configValue = false;
	public static String    targetPath = "..";
	public static int       backupInterval = 10;

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
		//configValue = configuration.getBoolean("configValue", Configuration.CATEGORY_GENERAL, false, "This is an example config value");
		targetPath = configuration.getString("targetPath", Configuration.CATEGORY_GENERAL, "..", "The path where to store the backups.");
		backupInterval = configuration.getInt("backupInterval", Configuration.CATEGORY_GENERAL, 10, 5, 1440, "The interval for backups to be made, in minutes.");

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