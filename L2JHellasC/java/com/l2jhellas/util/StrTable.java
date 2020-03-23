package com.l2jhellas.util;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class StrTable
{
	private final HashMap<Integer, HashMap<String, String>> rows = new HashMap<>();
	private final LinkedHashMap<String, Integer> columns = new LinkedHashMap<>();
	private final GArray<String> titles = new GArray<>();
	
	public StrTable(String title)
	{
		if (title != null)
			titles.add(title);
	}
	
	public StrTable()
	{
		this(null);
	}
	
	public StrTable set(int rowIndex, String colName, Object value)
	{
		String val = value.toString();
		HashMap<String, String> row;
		
		synchronized (rows)
		{
			if (rows.containsKey(rowIndex))
				row = rows.get(rowIndex);
			else
			{
				row = new HashMap<>();
				rows.put(rowIndex, row);
			}
		}
		
		synchronized (row)
		{
			row.put(colName, val);
		}
		
		synchronized (columns)
		{
			int columnSize;
			if (!columns.containsKey(colName))
				columnSize = Math.max(colName.length(), val.length());
			else if (columns.get(colName) >= (columnSize = val.length()))
				return this;
			columns.put(colName, columnSize);
		}
		
		return this;
	}
	
	public StrTable addTitle(String s)
	{
		synchronized (rows)
		{
			titles.add(s);
		}
		return this;
	}
	
	public static String pad_right(String s, int sz)
	{
		String result = s;
		if ((sz -= s.length()) > 0)
			result += repeat(" ", sz);
		return result;
	}
	
	public static String pad_left(String s, int sz)
	{
		String result = s;
		if ((sz -= s.length()) > 0)
			result = repeat(" ", sz) + result;
		return result;
	}
	
	public static String pad_center(String s, int sz)
	{
		String result = s;
		int i;
		while ((i = sz - result.length()) > 0)
			if (i == 1)
				result += " ";
			else
				result = " " + result + " ";
		return result;
	}
	
	public static String repeat(String s, int sz)
	{
		String result = "";
		for (int i = 0; i < sz; i++)
			result += s;
		return result;
	}
	
	@Override
	public String toString()
	{
		String[] result;
		synchronized (rows)
		{
			if (columns.isEmpty())
				return "";
			
			String header = "|";
			String line = "|";
			for (String c : columns.keySet())
			{
				header += pad_center(c, columns.get(c) + 2) + "|";
				line += repeat("-", columns.get(c) + 2) + "|";
			}
			
			result = new String[rows.size() + 4 + (titles.isEmpty() ? 0 : titles.size() + 1)];
			int i = 0;
			if (!titles.isEmpty())
			{
				result[i++] = " " + repeat("-", header.length() - 2) + " ";
				for (String title : titles)
					result[i++] = "| " + pad_right(title, header.length() - 3) + "|";
			}
			
			result[i++] = result[result.length - 1] = " " + repeat("-", header.length() - 2) + " ";
			result[i++] = header;
			result[i++] = line;
			
			for (HashMap<String, String> row : rows.values())
			{
				line = "|";
				for (String c : columns.keySet())
					line += pad_center(row.containsKey(c) ? row.get(c) : "-", columns.get(c) + 2) + "|";
				result[i++] = line;
			}
		}
		
		return joinStrings("\r\n", result);
	}
	
	public static String joinStrings(String glueStr, String[] strings, int startIdx, int maxCount)
	{
		String result = "";
		if (startIdx < 0)
		{
			startIdx += strings.length;
			if (startIdx < 0)
				return result;
		}
		while (startIdx < strings.length && maxCount != 0)
		{
			if (!result.isEmpty() && glueStr != null && !glueStr.isEmpty())
				result += glueStr;
			result += strings[startIdx++];
			maxCount--;
		}
		return result;
	}
	
	public static String joinStrings(String glueStr, String[] strings, int startIdx)
	{
		return joinStrings(glueStr, strings, startIdx, -1);
	}
	
	public static String joinStrings(String glueStr, String[] strings)
	{
		return joinStrings(glueStr, strings, 0);
	}
	
	public String toL2Html()
	{
		return toString().replaceAll("\r\n", "<br1>");
	}
}