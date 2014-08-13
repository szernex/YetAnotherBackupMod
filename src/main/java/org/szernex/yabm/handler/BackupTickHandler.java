package org.szernex.yabm.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import org.szernex.yabm.YABM;
import org.szernex.yabm.core.BackupTask;
import org.szernex.yabm.core.FTPTask;
import org.szernex.yabm.util.ScheduleHelper;
import org.szernex.yabm.util.Time;

public class BackupTickHandler
{
	Time nextSchedule;
	BackupTask backupTask = new BackupTask();
	FTPTask ftpTask = new FTPTask();

	public BackupTickHandler()
	{
		updateScheduleTime();
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event)
	{
		Time current_time = new Time(System.currentTimeMillis());

		if (event.phase == TickEvent.Phase.START
				|| !ConfigHandler.backupEnabled
				|| current_time.compareTo(nextSchedule) != 0)
		{
			return;
		}

		YABM.backupManager.startBackup();
		updateScheduleTime();
	}

	public void updateScheduleTime()
	{
		nextSchedule = ScheduleHelper.getNextSchedule(ConfigHandler.backupSchedule.trim().split(" "));
	}
}
