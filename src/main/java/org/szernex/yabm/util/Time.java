package org.szernex.yabm.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Time implements Comparable<Time>
{
	public int hour = -1;
	public int minute = -1;

	public Time()
	{

	}

	public Time(String time)
	{
		if (isValidTime(time))
		{
			String[] parts = time.split(":");
			hour = Integer.valueOf(parts[0]);
			minute = Integer.valueOf(parts[1]);
		}
	}

	public Time(Time time)
	{
		hour = time.hour;
		minute = time.minute;
	}

	public Time(long timestamp)
	{
		this(new SimpleDateFormat("HH:mm").format(new Date(timestamp)));
	}

	public Time(int h, int m)
	{
		hour = h;
		minute = m;
	}

	@Override
	public String toString()
	{
		return String.format("%02d:%02d", hour, minute);
	}

	public static boolean isValidTime(String input)
	{
		if (input == null)
		{
			return false;
		}

		if (input.matches("^\\d?\\d:\\d?\\d$"))
		{
			String[] parts = input.split(":");
			int hours = Integer.valueOf(parts[0]);
			int minutes = Integer.valueOf(parts[1]);

			return ((hours >= 0 && hours <= 23) && (minutes >= 0 && minutes <= 59));
		}

		return false;
	}

	@Override
	public int compareTo(Time t)
	{
		if (t == null)
		{
			return 1;
		}

		if (hour < t.hour)
		{
			return -1;
		}
		else if (hour > t.hour)
		{
			return 1;
		}
		else
		{
			if (minute < t.minute)
			{
				return -1;
			}
			else if (minute > t.minute)
			{
				return 1;
			}
			else
			{
				return 0;
			}
		}
	}
}
