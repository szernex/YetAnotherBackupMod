package org.szernex.yabm.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StringHelper
{
	public static List<String> getWordsStartingWith(String beginning, Collection<String> list)
	{
		List<String> output = new ArrayList<String>();

		for (String s : list)
		{
			if (s.toLowerCase().startsWith(beginning.toLowerCase()))
			{
				output.add(s);
			}
		}

		return output;
	}
}
