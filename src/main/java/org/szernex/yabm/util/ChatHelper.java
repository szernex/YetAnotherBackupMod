package org.szernex.yabm.util;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public class ChatHelper
{
	public static IChatComponent getLocalizedChatComponent(String key, Object... args)
	{
		return new ChatComponentTranslation(key, args);
	}

	public static IChatComponent getFormattedChatComponent(String format, Object... args)
	{
		return new ChatComponentText(String.format(format, args));
	}
}
