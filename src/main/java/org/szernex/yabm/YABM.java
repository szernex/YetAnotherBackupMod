package org.szernex.yabm;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import org.szernex.yabm.command.CommandYABM;
import org.szernex.yabm.core.BackupManager;
import org.szernex.yabm.handler.BackupTickHandler;
import org.szernex.yabm.handler.ConfigHandler;
import org.szernex.yabm.reference.Reference;

@Mod(modid = Reference.MOD_ID, version = Reference.VERSION, name = Reference.MOD_NAME, guiFactory = Reference.GUI_FACTORY_CLASS, acceptableRemoteVersions = "*")
public class YABM
{
	@Mod.Instance(Reference.MOD_ID)
	public static YABM instance;

	public static BackupTickHandler backupTickHandler;
	public static BackupManager backupManager;


	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ConfigHandler.init(event.getSuggestedConfigurationFile());
		FMLCommonHandler.instance().bus().register(new ConfigHandler());
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandYABM());
	}

	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event)
	{
		backupManager = new BackupManager();
		backupTickHandler = new BackupTickHandler();
		FMLCommonHandler.instance().bus().register(backupTickHandler);
	}
}
