package com.l2jhellas.logs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class LogRecorder
{
	private static final Logger _log = Logger.getLogger(LogRecorder.class.getName());
	
	public static final void add(String text, String cat)
	{
		final String date = (new SimpleDateFormat("yy.MM.dd H:mm:ss")).format(new Date());
		
		new File("log/game").mkdirs();
		
		try (FileWriter save = new FileWriter(new File("log/game/" + (cat != null ? cat : "_all") + ".txt"), true))
		{
			String out = "[" + date + "] Character: " + text + "\r\n"; // "+char_name()+"
			save.write(out);
			save.flush();
		}
		catch (IOException e)
		{
			_log.warning(LogRecorder.class.getSimpleName() + ": saving chat log failed: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		if (cat != null)
		{
			add(text, null);
		}
	}
	
	@Deprecated
	public static final void addEvent(L2PcInstance pc, String text)
	{
		final String date = (new SimpleDateFormat("yy.MM.dd H:mm:ss")).format(new Date());
		final String filedate = (new SimpleDateFormat("yyMMdd_H")).format(new Date());
		
		new File("log/game").mkdirs();
		try (FileWriter save = new FileWriter(new File("log/game/actions_" + filedate + ".txt"), true))
		{
			String out = "[" + date + "] '<" + pc.getName() + ">': " + text + "\r\n";
			save.write(out);
		}
		catch (IOException e)
		{
			_log.warning(LogRecorder.class.getSimpleName() + ": saving actions log failed: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	@Deprecated
	public static final void Assert(boolean exp)
	{
		Assert(exp, "");
	}
	
	public static final void Assert(boolean exp, String cmt)
	{
		if (exp)
			return;
		
		System.out.println("Assertion error [" + cmt + "]");
		Thread.dumpStack();
	}
}