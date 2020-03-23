package com.l2jhellas.util.filters.file;

import java.io.File;
import java.io.FileFilter;

public class XMLFilter implements FileFilter
{
	@Override
	public boolean accept(File f)
	{
		if ((f == null) || !f.isFile())
		{
			return false;
		}
		return f.getName().toLowerCase().endsWith(".xml");
	}
}
