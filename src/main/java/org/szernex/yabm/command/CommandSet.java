package org.szernex.yabm.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import org.szernex.yabm.handler.ConfigHandler;
import org.szernex.yabm.util.ChatHelper;
import org.szernex.yabm.util.StringHelper;

import java.util.Arrays;
import java.util.List;

public class CommandSet extends CommandBase
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
		String key = args[0];
		String[] values = Arrays.copyOfRange(args, 1, args.length);

		if (ConfigHandler.properties.containsKey(key))
		{
			ConfigHandler.setValue(key, values);

			ChatHelper.sendUserChatMsg(sender, ChatHelper.getLocalizedMsg("commands.yabm.set.success", key, Arrays.deepToString(values)));
		}
		else
		{
			ChatHelper.sendUserChatMsg(sender, ChatHelper.getLocalizedMsg("commands.yabm.set.invalid_key", key));
		}
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args)
	{
		if (args.length > 0)
		{
			return StringHelper.getWordsStartingWith(args[0], ConfigHandler.properties.keySet());
		}

		return null;
	}
}
