package org.szernex.yabm.util;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;

public class ChatHelper
{
	public static IChatComponent getLocalizedMsg(String key, Object... args)
	{
		return new ChatComponentText(StatCollector.translateToLocalFormatted(key, args));
	}

	public static IChatComponent getFormattedMsg(String format, Object... args)
	{
		return new ChatComponentText(String.format(format, args));
	}

	public static boolean sendServerChatMsg(IChatComponent chat_component)
	{
		ServerConfigurationManager manager = MinecraftServer.getServer().getConfigurationManager();

		if (manager == null)
		{
			return false;
		}

		manager.sendChatMsg(chat_component);
		return true;
	}

	public static void sendUserChatMsg(ICommandSender sender, IChatComponent chat_component)
	{
		if (sender != null)
		{
			sender.addChatMessage(chat_component);
		}
	}
}
