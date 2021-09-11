package com.l2jhellas.util;

public final class StringReplacer
{
	private static final String DELIM_STR = "{}";
	
	private final StringBuilder _sb;
	
	public StringReplacer(String source)
	{
		_sb = new StringBuilder(source);
	}

	public final void replaceAll(String pattern, String replacement)
	{
		int point;
		while ((point = _sb.indexOf(pattern)) != -1)
			_sb.replace(point, point + pattern.length(), replacement);
	}

	public final void replaceAll(Object... args)
	{
		int index;
		int newIndex = 0;
		
		for (Object obj : args)
		{
			index = _sb.indexOf(DELIM_STR, newIndex);
			if (index == -1)
				break;
			
			newIndex = index + 2;
			_sb.replace(index, newIndex, (obj == null) ? null : obj.toString());
		}
	}

	public final void replaceFirst(String pattern, String replacement)
	{
		final int point = _sb.indexOf(pattern);
		_sb.replace(point, point + pattern.length(), replacement);
	}

	public final void replaceLast(String pattern, String replacement)
	{
		final int point = _sb.lastIndexOf(pattern);
		_sb.replace(point, point + pattern.length(), replacement);
	}
	
	@Override
	public String toString()
	{
		return _sb.toString();
	}
}