package com.l2jhellas.logs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class AuditFormatter extends Formatter
{
	private static final String CRLF = "\r\n";
	private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM H:mm:ss");
	
	@Override
	public String format(LogRecord record)
	{
		StringBuilder output = new StringBuilder();
		output.append('[');
		output.append(dateFmt.format(new Date(record.getMillis())));
		output.append(']');
		output.append(' ');
		output.append(record.getMessage());
		for (Object p : record.getParameters())
		{
			if (p == null)
				continue;
			output.append(',');
			output.append(' ');
			output.append(p.toString());
		}
		output.append(CRLF);
		
		return output.toString();
	}
}