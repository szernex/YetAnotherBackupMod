package org.szernex.yabm.proxy;

import cpw.mods.fml.common.FMLCommonHandler;
import org.szernex.yabm.handler.BackupTickHandler;

public class ServerProxy extends CommonProxy
{

	@Override
	public void init()
	{
		super.init();

		FMLCommonHandler.instance().bus().register(new BackupTickHandler());
	}
}
