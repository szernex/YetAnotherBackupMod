package org.szernex.yabm.util;

import cpw.mods.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import org.szernex.yabm.reference.Reference;

public class LogHelper
{
	public static void log(Level level, Object message, Object... args)
	{
		if (message != null)
		{
			if (message instanceof String)
			{
				FMLLog.log(Reference.MOD_NAME, level, String.format((String) message, args));
			}
			else
			{
				FMLLog.log(Reference.MOD_NAME, level, String.valueOf(message));
			}
		}
	}

	public static void all(Object message, Object... args)
	{
		log(Level.ALL, message, args);
	}

	public static void debug(Object message, Object... args)
	{
		log(Level.DEBUG, message, args);
	}

	public static void info(Object message, Object... args)
	{
		log(Level.INFO, message, args);
	}

	public static void error(Object message, Object... args)
	{
		log(Level.ERROR, message, args);
	}

	public static void fatal(Object message, Object... args)
	{
		log(Level.FATAL, message, args);
	}

	public static void off(Object message, Object... args)
	{
		log(Level.OFF, message, args);
	}

	public static void trace(Object message, Object... args)
	{
		log(Level.TRACE, message, args);
	}

	public static void warn(Object message, Object... args)
	{
		log(Level.WARN, message, args);
	}
}
