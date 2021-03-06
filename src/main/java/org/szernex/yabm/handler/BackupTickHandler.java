package org.szernex.yabm.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import org.szernex.yabm.YABM;
import org.szernex.yabm.util.ScheduleHelper;
import org.szernex.yabm.util.Time;

public class BackupTickHandler
{
	Time nextSchedule;
	long lastRun = 0;

	public BackupTickHandler()
	{
		updateScheduleTime();
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event)
	{
		Time current_time = new Time(System.currentTimeMillis());
		long timestamp = System.currentTimeMillis();

		if (event.phase == TickEvent.Phase.START
				|| !ConfigHandler.backupEnabled
				|| current_time.compareTo(nextSchedule) != 0
				|| YABM.backupManager.isRunning()
				|| timestamp < (lastRun + 60000))
		{
			return;
		}

		YABM.backupManager.startBackup();
		updateScheduleTime();
		lastRun = System.currentTimeMillis();
	}

	public void updateScheduleTime()
	{
		nextSchedule = ScheduleHelper.getNextSchedule(ConfigHandler.backupSchedule.trim().split(" "));
	}
}
