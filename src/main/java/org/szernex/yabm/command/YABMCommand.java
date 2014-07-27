package org.szernex.yabm.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;
import org.szernex.yabm.handler.ConfigHandler;
import org.szernex.yabm.util.LogHelper;
import org.szernex.yabm.util.StringHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YABMCommand extends CommandBase
{
	private List<String> availableCommands = new ArrayList<String>();

	public YABMCommand()
	{
		super();

		availableCommands.add("get");
		availableCommands.add("set");
		availableCommands.add("startbackup");
	}

	@Override
	public String getCommandName()
	{
		return "yabm";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args)
	{
		LogHelper.info(Arrays.deepToString(args));
		LogHelper.info(args.length);

		if (args.length == 1)
		{
			return StringHelper.getWordsStartingWith(args[0], availableCommands);
		}
		else if (args.length == 2)
		{
			if (args[0].equalsIgnoreCase("get")
					|| args[0].equalsIgnoreCase("set"))
			{
				return StringHelper.getWordsStartingWith(args[1], ConfigHandler.properties.keySet());
			}
		}

		return null;
	}


	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "commands.yabm.general.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		if (args.length == 0)
		{
			throw new WrongUsageException(this.getCommandUsage(sender), new Object[0]);
		}

		String command = args[0];

		if (command.equalsIgnoreCase("set"))
		{

		}
		else if (command.equalsIgnoreCase("get"))
		{
			String key = args[1].toLowerCase();

			if (ConfigHandler.properties.containsKey(key))
			{
				String result = "";
				Object value = ConfigHandler.properties.get(key);

				if (value instanceof String[])
				{
					result = Arrays.deepToString((String[]) value);
				}
				else
				{
					result = value.toString();
				}

				sender.addChatMessage(new ChatComponentText(key + ": " + result));
			}
		}


	}
}
