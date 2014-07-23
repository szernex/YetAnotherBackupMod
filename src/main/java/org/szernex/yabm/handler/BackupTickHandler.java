package org.szernex.yabm.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.szernex.yabm.util.LogHelper;

@SideOnly(Side.SERVER)
public class BackupTickHandler
{
	private int intervalCounter = ConfigHandler.backupInterval;

	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent event)
	{
		intervalCounter--;

		if (intervalCounter > 0)
		{
			return;
		}

		LogHelper.info("hello world");

		intervalCounter = ConfigHandler.backupInterval;
	}
}
