package org.szernex.yabm.util;

public class ConvertHelper
{
	public static int msToTicks(int milliseconds)
	{
		return Math.round(milliseconds / 50);
	}

	public static int ticksToMS(int ticks)
	{
		return (ticks * 50);
	}

	public static int secToTicks(int seconds)
	{
		return (seconds * 20);
	}

	public static int ticksToSeconds(int ticks)
	{
		return Math.round(ticks / 20);
	}
}
