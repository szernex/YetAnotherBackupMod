package org.szernex.yabm.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.szernex.yabm.YABM;
import org.szernex.yabm.handler.ConfigHandler;
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
	}

	private void commandSet(ICommandSender sender, String[] args)
	{
		String key = args[1];
		String value = args[2];

		if (ConfigHandler.properties.containsKey(key))
		{
			Property prop = ConfigHandler.configuration.get(Configuration.CATEGORY_GENERAL, key, "");
			Object obj = ConfigHandler.properties.get(key);

			try
			{
				if (obj instanceof String)
				{
					prop.set(value);
				}
				else if (obj instanceof Integer)
				{
					prop.set(Integer.valueOf(value));
				}
				else if (obj instanceof Boolean)
				{
					prop.set(Boolean.valueOf(value));
				}
				else if (obj instanceof String[])
				{
					prop.set(Arrays.copyOfRange(args, 2, args.length));
				}
				else if (obj instanceof Double)
				{
					prop.set(Double.valueOf(value));
				}

				ConfigHandler.configuration.save();
				ConfigHandler.loadConfig();
				sender.addChatMessage(new ChatComponentText("Set " + key + " to " + Arrays.deepToString(Arrays.copyOfRange(args, 2, args.length))));
			}
			catch (NumberFormatException ex)
			{
				sender.addChatMessage(new ChatComponentText("Invalid value type for " + key));
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

			sender.addChatMessage(new ChatComponentText(key + ": " + result));
		}
	}

	private void commandStartBackup(ICommandSender sender, String[] args)
	{
		MinecraftServer.getServer().addChatMessage(new ChatComponentText(sender.getCommandSenderName() + " manually started a backup"));
		YABM.backupTickHandler.startBackup();
	}
}
