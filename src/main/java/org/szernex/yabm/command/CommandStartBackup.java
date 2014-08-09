package org.szernex.yabm.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import org.szernex.yabm.YABM;
import org.szernex.yabm.util.ChatHelper;

public class CommandStartBackup extends CommandBase
{
	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	@Override
	public String getCommandName()
	{
		return null;
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return null;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		ChatHelper.sendServerChatMsg(ChatHelper.getLocalizedMsg("commands.yabm.startbackup.start", sender.getCommandSenderName()));
		YABM.backupTickHandler.startBackup();
	}
}
