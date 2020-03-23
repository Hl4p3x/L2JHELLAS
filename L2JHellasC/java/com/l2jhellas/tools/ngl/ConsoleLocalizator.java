package com.l2jhellas.tools.ngl;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Formatter;
import java.util.Locale;
import java.util.Scanner;

import com.l2jhellas.util.osnative.CodePage;
import com.l2jhellas.util.osnative.WinConsole;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class ConsoleLocalizator extends LocalizationParser
{
	public static final String EOL = System.getProperty("line.separator");
	
	private WinConsole _wcon;
	private Pointer _stdout;
	private static PrintStream _out;
	
	Scanner _scn = new Scanner(System.in);
	String _baseName = "NGLConsole";
	
	public ConsoleLocalizator(String dir, String baseName)
	{
		this(dir, baseName, Locale.getDefault());
	}
	
	public ConsoleLocalizator(String dir, String baseName, Locale locale)
	{
		super(dir, baseName, locale);
		loadConsole();
	}
	
	public ConsoleLocalizator(String dir, String baseName, String locale)
	{
		super(dir, baseName, locale);
		loadConsole();
	}
	
	private void loadConsole()
	{
		if (Platform.isWindows())
		{
			try
			{
				_wcon = WinConsole.INSTANCE;
				
				if (_wcon.GetConsoleOutputCP() != 0)
				{
					// Set Console Output to UTF8
					_wcon.SetConsoleOutputCP(CodePage.CP_UTF8);
					
					// Set Output to STDOUT
					_stdout = _wcon.GetStdHandle(-11);
				}
				else
				{
					// Not running from windows console
					_wcon = null;
				}
			}
			catch (Exception e)
			{
				// Missing function in Kernel32
				_wcon = null;
			}
		}
		
		if (_wcon == null) // Not running windows console
		{
			try
			{
				// UTF-8 Print Stream
				_out = new PrintStream(System.out, true, "UTF-8");
			}
			catch (UnsupportedEncodingException e)
			{
				// UTF-8 Not Supported
				_out = new PrintStream(System.out, true);
				directPrint("Your system doesn't support UTF-8 encoding" + EOL);
			}
		}
	}
	
	public void print(String id, Object... args)
	{
		String msg = getStringFromId(id);
		if (msg == null)
		{
			msg = formatText("Untranslated id: %s", id);
		}
		else
		{
			msg = formatText(msg, args);
		}
		directPrint(msg);
	}
	
	public void println()
	{
		directPrint(EOL);
	}
	
	public void println(String id, Object... args)
	{
		String msg = getStringFromId(id);
		if (msg == null)
		{
			msg = formatText("Untranslated id: %s" + EOL, id);
		}
		else
		{
			msg = formatText(msg + EOL, args);
		}
		directPrint(msg);
	}
	
	public String inputString(String id, Object... args)
	{
		print(id, args);
		directPrint(": ");
		String ret = _scn.next();
		return ret;
	}
	
	public String getString(String id, Object... args)
	{
		String msg = getStringFromId(id);
		if (msg == null)
		{
			return formatText("Untranslated id: %s", id);
		}
		return formatText(msg, args);
	}
	
	private static String formatText(String text, Object... args)
	{
		String formattedText = null;
		try (Formatter form = new Formatter())
		{
			formattedText = form.format(text, args).toString();
		}
		return formattedText;
	}
	
	private void directPrint(String message)
	{
		if (_wcon == null)
		{
			_out.print(message);
		}
		else
		{
			_wcon.WriteConsoleW(_stdout, message.toCharArray(), message.length(), new IntByReference(), null);
		}
	}
}