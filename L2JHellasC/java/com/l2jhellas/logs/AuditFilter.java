package com.l2jhellas.logs;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class AuditFilter implements Filter
{
	@Override
	public boolean isLoggable(LogRecord record)
	{
		return record.getLoggerName().equalsIgnoreCase("audit");
	}
}