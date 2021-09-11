package com.l2jhellas.util;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class CLogger
{
	private final Logger _logger;
	
	public CLogger(String name)
	{
		_logger = Logger.getLogger(name);
	}
	
	private void log0(Level level, StackTraceElement caller, Object message, Throwable exception)
	{
		if (!_logger.isLoggable(level))
			return;
		
		if (caller == null)
			caller = new Throwable().getStackTrace()[2];
		
		_logger.logp(level, caller.getClassName(), caller.getMethodName(), String.valueOf(message), exception);
	}
	
	private void log0(Level level, StackTraceElement caller, Object message, Throwable exception, Object... args)
	{
		if (!_logger.isLoggable(level))
			return;
		
		if (caller == null)
			caller = new Throwable().getStackTrace()[2];
		
		_logger.logp(level, caller.getClassName(), caller.getMethodName(), format(String.valueOf(message), args), exception);
	}
	
	public void log(LogRecord record)
	{
		_logger.log(record);
	}

	public void debug(Object message)
	{
		log0(Level.FINE, null, message, null);
	}

	public void debug(Object message, Object... args)
	{
		log0(Level.FINE, null, message, null, args);
	}

	public void debug(Object message, Throwable exception)
	{
		log0(Level.FINE, null, message, exception);
	}

	public void debug(Object message, Throwable exception, Object... args)
	{
		log0(Level.FINE, null, message, exception, args);
	}

	public void info(Object message)
	{
		log0(Level.INFO, null, message, null);
	}

	public void info(Object message, Object... args)
	{
		log0(Level.INFO, null, message, null, args);
	}

	public void info(Object message, Throwable exception)
	{
		log0(Level.INFO, null, message, exception);
	}

	public void info(Object message, Throwable exception, Object... args)
	{
		log0(Level.INFO, null, message, exception, args);
	}

	public void warn(Object message)
	{
		log0(Level.WARNING, null, message, null);
	}

	public void warn(Object message, Object... args)
	{
		log0(Level.WARNING, null, message, null, args);
	}

	public void warn(Object message, Throwable exception)
	{
		log0(Level.WARNING, null, message, exception);
	}

	public void warn(Object message, Throwable exception, Object... args)
	{
		log0(Level.WARNING, null, message, exception, args);
	}

	public void error(Object message)
	{
		log0(Level.SEVERE, null, message, null);
	}

	public void error(Object message, Object... args)
	{
		log0(Level.SEVERE, null, message, null, args);
	}

	public void error(Object message, Throwable exception)
	{
		log0(Level.SEVERE, null, message, exception);
	}

	public void error(Object message, Throwable exception, Object... args)
	{
		log0(Level.SEVERE, null, message, exception, args);
	}

	private static final String format(String message, Object... args)
	{
		if (args == null || args.length == 0)
			return message;
		
		final StringReplacer sr = new StringReplacer(message);
		sr.replaceAll(args);
		return sr.toString();
	}
}