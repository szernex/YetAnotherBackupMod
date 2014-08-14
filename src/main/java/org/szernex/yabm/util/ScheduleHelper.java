package org.szernex.yabm.util;

import java.util.TreeSet;

public class ScheduleHelper
{
	public static Time getNextSchedule(String[] list)
	{
		if (list.length == 1)
		{
			String time = list[0];

			if (time == null)
			{
				return null;
			}
			else
			{
				if (time.matches("^\\d+$"))
				{
					long delay = Integer.valueOf(time) * 60 * 1000;

					return new Time(System.currentTimeMillis() + delay);
				}
				else if (Time.isValidTime(time))
				{
					return new Time(time);
				}
			}
		}
		else if (list.length > 1)
		{
			TreeSet<Time> times = new TreeSet<Time>();
			Time current_time = new Time(System.currentTimeMillis());

			for (String time : list)
			{
				if (Time.isValidTime(time))
				{
					times.add(new Time(time));
				}
			}

			if (times.size() > 0)
			{
				for (Time t : times)
				{
					if (t.compareTo(current_time) > 0)
					{
						return t;
					}
				}

				return times.first();
			}
		}

		return null;
	}
}
