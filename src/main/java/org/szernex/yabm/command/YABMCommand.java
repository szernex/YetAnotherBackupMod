package org.szernex.yabm.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.szernex.yabm.YABM;
import org.szernex.yabm.handler.ConfigHandler;
import org.szernex.yabm.util.ChatHelper;
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
			throw new WrongUsageException(getCommandUsage(sender));
		}

		String command = args[0];

		if (command.equalsIgnoreCase("set"))
		{
			commandSet(sender, args);
		}
		else if (command.equalsIgnoreCase("get"))
		{
			commandGet(sender, args);
		}
		else if (command.equalsIgnoreCase("startbackup"))
		{
			commandStartBackup(sender, args);
		}
		else
		{
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}

	private void commandSet(ICommandSender sender, String[] args)
	{
		String key = args[1];
		String[] values = Arrays.copyOfRange(args, 2, args.length);

		if (ConfigHandler.properties.containsKey(key))
		{
			Property prop = ConfigHandler.configuration.get(Configuration.CATEGORY_GENERAL, key, "");
			Object obj = ConfigHandler.properties.get(key);

			try
			{
				if (obj instanceof String)
				{
					prop.set(values[0]);
				}
				else if (obj instanceof Integer)
				{
					prop.set(Integer.valueOf(values[0]));
				}
				else if (obj instanceof Boolean)
				{
					prop.set(Boolean.valueOf(values[0]));
				}
				else if (obj instanceof String[])
				{
					prop.set(values);
				}
				else if (obj instanceof Double)
				{
					prop.set(Double.valueOf(values[0]));
				}

				ConfigHandler.configuration.save();
				ConfigHandler.loadConfig();
				sender.addChatMessage(ChatHelper.getLocalizedChatComponent("commands.yabm.set.set_success", key, Arrays.deepToString(values)));
			}
			catch (NumberFormatException ex)
			{
				sender.addChatMessage(ChatHelper.getLocalizedChatComponent("commands.yabm.set.set_invalid_value", key));
			}
		}
	}

	private void commandGet(ICommandSender sender, String[] args)
	{
		String key = args[1];

		if (ConfigHandler.properties.containsKey(key))
		{
			String result;
			Object value = ConfigHandler.properties.get(key);

			if (value instanceof String[])
			{
				result = Arrays.deepToString((String[]) value);
			}
			else
			{
				result = value.toString();
			}

			sender.addChatMessage(ChatHelper.getLocalizedChatComponent("commands.yabm.get.get_success", key, result));
		}
		else
		{
			sender.addChatMessage(ChatHelper.getLocalizedChatComponent("commands.yabm.get.get_invalid_key", key));
		}
	}

	private void commandStartBackup(ICommandSender sender, String[] args)
	{
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(ChatHelper.getLocalizedChatComponent("commands.yabm.startbackup.start", sender.getCommandSenderName()));
		YABM.backupTickHandler.startBackup();
	}
}
