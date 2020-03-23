package com.l2jhellas.logs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.l2jhellas.Config;

public class FileLogFormatter extends Formatter
{
	private static final String CRLF = "\r\n";
	private static final String tab = "\t";
	private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	
	@Override
	public String format(LogRecord record)
	{
		StringBuilder output = new StringBuilder();
		
		output.append("L2jHellas");
		output.append(tab);
		output.append(dateFmt.format(new Date(record.getMillis())));
		output.append(tab);
		output.append(record.getLevel().getName());
		if (Config.DEBUG_LOGGER)
		{
			output.append(tab);
			output.append(record.getThreadID());
			output.append(tab);
			output.append(record.getLoggerName());
		}
		output.append(tab);
		output.append(record.getMessage());
		
		output.append(CRLF);
		
		return output.toString();
	}
}