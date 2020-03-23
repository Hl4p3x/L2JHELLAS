package com.l2jhellas.util.filters.file;

import java.io.File;
import java.io.FileFilter;

public class HtmFilter implements FileFilter
{
	@Override
	public boolean accept(File file)
	{
		return file.isDirectory() || (file.getName().endsWith(".htm") || file.getName().endsWith(".html"));
	}
}