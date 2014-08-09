package org.szernex.yabm.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import org.szernex.yabm.YABM;
import org.szernex.yabm.util.StringHelper;

import java.util.*;

public class CommandYABM extends CommandBase
{
	private Map<String, CommandBase> availableCommands = new HashMap<String, CommandBase>();

	public CommandYABM()
	{
		availableCommands.put("get", new CommandGet());
		availableCommands.put("set", new CommandSet());
		availableCommands.put("startbackup", new CommandStartBackup());
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	@Override
	public String getCommandName()
	{
		return "yabm";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "commands.yabm.general.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		String sub_command = args[0].toLowerCase();

		if (availableCommands.containsKey(sub_command))
		{
			availableCommands.get(sub_command).processCommand(sender, Arrays.copyOfRange(args, 1, args.length));
		}

		YABM.backupTickHandler.updateScheduleTime();
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args)
	{
		if (args.length == 1)
		{
			return new ArrayList<String>(StringHelper.getWordsStartingWith(args[0], availableCommands.keySet()));
		}

		String sub_command = args[0].toLowerCase();

		if (availableCommands.containsKey(sub_command))
		{
			return availableCommands.get(sub_command).addTabCompletionOptions(sender, Arrays.copyOfRange(args, 1, args.length));
		}

		return null;
	}
}
