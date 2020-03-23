package com.l2jhellas.util;

import java.util.logging.Logger;

public final class StringUtil
{
	
	private static final Logger LOG = Logger.getLogger(StringUtil.class.getSimpleName());
	
	private StringUtil()
	{
	}
	
	public static String concat(final String... strings)
	{
		final StringBuilder sbString = new StringBuilder(getLength(strings));
		
		for (final String string : strings)
		{
			sbString.append(string);
		}
		
		return sbString.toString();
	}
	
	public static StringBuilder startAppend(final int sizeHint, final String... strings)
	{
		final int length = getLength(strings);
		final StringBuilder sbString = new StringBuilder(sizeHint > length ? sizeHint : length);
		
		for (final String string : strings)
		{
			sbString.append(string);
		}
		
		return sbString;
	}
	
	public static void append(StringBuilder sb, Object... content)
	{
		for (Object obj : content)
			sb.append((obj == null) ? null : obj.toString());
	}
	
	private static int getLength(final String[] strings)
	{
		int length = 0;
		
		for (final String string : strings)
		{
			length += string.length();
		}
		
		return length;
	}
	
	public static boolean isDigit(String text)
	{
		if (text == null)
			return false;
		
		return text.matches("[0-9]+");
	}
	
	public static void printSection(String text)
	{
		final StringBuilder sb = new StringBuilder(80);
		for (int i = 0; i < (73 - text.length()); i++)
			sb.append("-");
		
		StringUtil.append(sb, "=[ ", text, " ]");
		
		LOG.info(sb.toString());
	}
}