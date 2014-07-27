package org.szernex.yabm;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import org.szernex.yabm.command.YABMCommand;
import org.szernex.yabm.handler.BackupTickHandler;
import org.szernex.yabm.handler.ConfigHandler;
import org.szernex.yabm.proxy.IProxy;
import org.szernex.yabm.reference.Reference;
import org.szernex.yabm.util.LogHelper;

import java.nio.file.Paths;

@Mod(modid = Reference.MOD_ID, version = Reference.VERSION, name = Reference.MOD_NAME, guiFactory = Reference.GUI_FACTORY_CLASS)
public class YABM
{
	@Mod.Instance("YABM")
	public static YABM instance;

	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static IProxy proxy;

	public static BackupTickHandler backupTickHandler;


	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ConfigHandler.init(event.getSuggestedConfigurationFile());
		FMLCommonHandler.instance().bus().register(new ConfigHandler());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		LogHelper.info(Paths.get("").toAbsolutePath().toString());
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{

	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new YABMCommand());
	}

	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event)
	{
		backupTickHandler = new BackupTickHandler();
		FMLCommonHandler.instance().bus().register(backupTickHandler);
		LogHelper.info("BackupTickHandler registered");
	}

	@Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent event)
	{

	}
}
