package com.l2jhellas.gameserver.instancemanager;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.network.serverpackets.PledgeCrest;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.StringUtil;

public class BotsPreventionManager
{
	protected static Map<Integer, Future<?>> _beginvalidation = new HashMap<>();
	protected static Map<Integer, PlayerData> _validation = new HashMap<>();
	protected static Map<Integer, byte[]> _images = new HashMap<>();
	
	protected int WINDOW_DELAY = 3;
	//1 minute
	protected int VALIDATION_TIME = 60 * 1000;

	BotsPreventionManager()
	{		
		if(_images.size() <= 0)
		   getimages();
	}

	public void StartCheck(L2PcInstance player)
	{
		if(player == null)
			return;
		
		if (_validation.get(player.getObjectId()) == null)
			validationtasks(player);
	}

	public void prevalidationwindow(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		StringBuilder tb = new StringBuilder();
		StringUtil.append(tb, "<html>");
		StringUtil.append(tb, "<title>Bots prevention</title>");
		StringUtil.append(tb,"<body><center><br><br><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\">");
		StringUtil.append(tb,"<br><br><font color=\"a2a0a2\">You have one(1) minute to match the color.<br1></font>");
		StringUtil.append(tb,"<br><br><font color=\"b09979\">if given answer results are incorrect or no action is made<br1>server is going to punish character instantly.</font>");
		StringUtil.append(tb,"<br><br><button value=\"CONTINUE\" action=\"bypass report_continue\" width=\"75\" height=\"21\" back=\"L2UI_CH3.Btn1_normal\" fore=\"L2UI_CH3.Btn1_normal\">");
		StringUtil.append(tb, "</center></body>");
		StringUtil.append(tb, "</html>");
		html.setHtml(tb.toString());
		player.sendPacket(html);
	}

	private static void validationwindow(L2PcInstance player)
	{
		PlayerData container = _validation.get(player.getObjectId());
		NpcHtmlMessage html = new NpcHtmlMessage(1);

		StringBuilder tb = new StringBuilder();
		StringUtil.append(tb, "<html>");
		StringUtil.append(tb, "<title>Bots prevention</title>");
		StringUtil.append(tb,"<body><center><br><br><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\">");
		StringUtil.append(tb,"<br><br><font color=\"a2a0a2\">in order to prove you are a human being<br1>you've to</font> <font color=\"b09979\">match colors within generated pattern:</font>");
		StringUtil.append(tb, "<br><br><img src=\"Crest.crest_" + Config.SERVER_ID + "_"+ (_validation.get(player.getObjectId()).patternid) + "\" width=\"32\" height=\"32\"></td></tr>");
		StringUtil.append(tb, "<br><br><font color=b09979>click-on pattern of your choice beneath:</font>");
		StringUtil.append(tb, "<table><tr>");
		
		for (int i = 0; i < container.options.size(); i++)
		{
			StringUtil.append(tb,"<td><button action=\"bypass -h report_" + i + "\" width=32 height=32 back=\"Crest.crest_"+ Config.SERVER_ID + "_" + (container.options.get(i) + 1500) + "\" fore=\"Crest.crest_"+ Config.SERVER_ID + "_" + (container.options.get(i) + 1500) + "\"></td>");
		}
		StringUtil.append(tb, "</tr></table>");
		StringUtil.append(tb, "</center></body>");
		StringUtil.append(tb, "</html>");

		html.setHtml(tb.toString());
		player.sendPacket(html);
	}

	public void punishmentnwindow(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		StringBuilder tb = new StringBuilder();
		StringUtil.append(tb, "<html>");
		StringUtil.append(tb, "<title>Bots prevention</title>");
		StringUtil.append(tb,"<body><center><br><br><img src=\"L2UI_CH3.herotower_deco\" width=\"256\" height=\"32\">");
		StringUtil.append(tb,"<br><br><font color=\"a2a0a2\">if such window appears, it means character haven't<br1>passed through prevention system.");
		StringUtil.append(tb,"<br><br><font color=\"b09979\">in such case character get moved to nearest town.</font>");
		StringUtil.append(tb, "</center></body>");
		StringUtil.append(tb, "</html>");
		html.setHtml(tb.toString());
		player.sendPacket(html);
	}

	public void validationtasks(L2PcInstance player)
	{
		PlayerData container = new PlayerData();
		randomizeimages(container, player);

		for (int i = 0; i < container.options.size(); i++)
		{
			PledgeCrest packet = new PledgeCrest((container.options.get(i) + 1500),_images.get(container.options.get(i)));
			player.sendPacket(packet);
		}

		PledgeCrest packet = new PledgeCrest(container.patternid,_images.get(container.options.get(container.mainpattern)));
		player.sendPacket(packet);

		_validation.put(player.getObjectId(), container);

		Future<?> newTask = ThreadPoolManager.getInstance().scheduleGeneral(new ReportCheckTask(player),VALIDATION_TIME);
		ThreadPoolManager.getInstance().scheduleGeneral(new countdown(player, VALIDATION_TIME / 1000), 0);
		_beginvalidation.put(player.getObjectId(), newTask);
	}

	protected void randomizeimages(PlayerData container, L2PcInstance player)
	{
		int buttonscount = 4;
		int imagescount = _images.size();

		for (int i = 0; i < buttonscount; i++)
		{
			int next = Rnd.nextInt(imagescount);
			while (container.options.indexOf(next) > -1)
				next = Rnd.nextInt(imagescount);
			container.options.add(next);
		}

		int mainIndex = Rnd.nextInt(buttonscount);
		container.mainpattern = mainIndex;

		Calendar token = Calendar.getInstance();
		String uniquetoken = Integer.toString(token.get(Calendar.DAY_OF_MONTH)) + Integer.toString(token.get(Calendar.HOUR_OF_DAY)) 
		+ Integer.toString(token.get(Calendar.MINUTE))
		+ Integer.toString(token.get(Calendar.SECOND))
		+ Integer.toString(token.get(Calendar.MILLISECOND) / 100);
		container.patternid = Integer.parseInt(uniquetoken);
	}

	protected void punishment(L2PcInstance player)
	{
		if (_validation.containsKey(player.getObjectId()))
			_validation.remove(player.getObjectId());
		
		if(_beginvalidation.containsKey(player.getObjectId()))
		{
		   _beginvalidation.get(player.getObjectId()).cancel(true);
		   _beginvalidation.remove(player.getObjectId());
		}
		
		if(player.isInCombat() || player.getPvpFlag() > 0 || player.getKarma() > 0)
		{
			player.store();
		    player.closeNetConnection(false);
		}
		else
		{
			player.stopMove(null);
			player.teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
			punishmentnwindow(player);
		}
	}

	public void CheckBypass(String command, L2PcInstance player)
	{
		if (!_validation.containsKey(player.getObjectId()))
			return;

		String params = command.substring(command.indexOf("_") + 1);

		if (params.startsWith("continue"))
		{
			validationwindow(player);
			_validation.get(player.getObjectId()).firstWindow = false;
		}
		else
		{
			int choosenoption = -1;

			try
			{
				choosenoption = Integer.parseInt(params);
			}
			catch (NumberFormatException e)
			{
				choosenoption = -1;
			}

			if (choosenoption > -1)
			{
				PlayerData playerData = _validation.get(player.getObjectId());
				if (choosenoption != playerData.mainpattern)
					punishment(player);
				else
				{
					if (_validation.containsKey(player.getObjectId()))
						_validation.remove(player.getObjectId());

					if (_beginvalidation.containsKey(player.getObjectId()))
					{
						_beginvalidation.get(player.getObjectId()).cancel(true);
						_beginvalidation.remove(player.getObjectId());
					}
				}
			}
		}
	}

	protected class countdown implements Runnable
	{
		private final L2PcInstance _player;
		private int _time;

		public countdown(L2PcInstance player, int time)
		{
			_time = time;
			_player = player;
		}

		@Override
		public void run()
		{
			if (_player.isbOnline())
			{
				if (_validation.containsKey(_player.getObjectId()) && _validation.get(_player.getObjectId()).firstWindow)
				{
					if (_time % WINDOW_DELAY == 0)
						prevalidationwindow(_player);
				}

				switch (_time)
				{
				case 61:
				case 60:
					_player.sendMessage(_time / 60 + " minute(s) to match the color.");
					break;
				case 30:
				case 10:
				case 5:
				case 4:
				case 3:
				case 2:
				case 1:
					_player.sendMessage(_time + " second(s) to match the color!");
					break;
				}
				if (_time > 1 && _validation.containsKey(_player.getObjectId()))
					ThreadPoolManager.getInstance().scheduleGeneral(new countdown(_player, _time - 1), 1000);
			}
		}
	}

	private class ReportCheckTask implements Runnable
	{
		private final L2PcInstance _player;

		public ReportCheckTask(L2PcInstance player)
		{
			_player = player;
		}

		@Override
		public void run()
		{
			if (_validation.get(_player.getObjectId()) != null)
				punishment(_player);
		}
	}
	
	private static void getimages()
	{
		String CRESTS_DIR = "data/images/prevention";

		final File directory = new File(CRESTS_DIR);
		directory.mkdirs();

		int i = 0;
		for (File file : directory.listFiles())
		{
			if (!file.getName().endsWith(".dds"))
				continue;

			byte[] data;

			try (RandomAccessFile f = new RandomAccessFile(file, "r"))
			{
				data = new byte[(int) f.length()];
				f.readFully(data);
			}
			catch (Exception e)
			{
				continue;
			}
			_images.put(i, data);
			i++;
		}
	}

	private class PlayerData
	{
		public PlayerData()
		{
			firstWindow = true;
		}

		public int mainpattern;
		public List<Integer> options = new ArrayList<>();
		public boolean firstWindow;
		public int patternid;
	}
	
	public static final BotsPreventionManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final BotsPreventionManager _instance = new BotsPreventionManager();
	}
}