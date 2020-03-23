package com.l2jhellas.gameserver.model.entity.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.Announcements;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.instancemanager.GrandBossManager;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.events.engines.ZodiacMain;
import com.l2jhellas.gameserver.model.zone.type.L2BossZone;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.skills.SkillTable;

public class ChaosEvent
{
	public static List<L2PcInstance> _players = new ArrayList<>();
	public static L2PcInstance _topplayer, _topplayer2, _topplayer3, _topplayer4, _topplayer5;
	public static int _topkills = 0, _topkills2 = 0, _topkills3 = 0, _topkills4 = 0, _topkills5 = 0;
	public static boolean _isChaosActive = false;
	private final static int seconds = 120;
	
	public static void stopChaos()
	{
		ZodiacMain.ZodiacRegisterActive = false;
		_isChaosActive = false;
		Announcements.getInstance().announceToAll("Chaos Event has ended!");
		getTopKiller();
		calculateRewards();
		for (L2PcInstance player : _players)
		{
			removeSuperHaste(player);
		}
		cleanColors();
		cleanPlayers();
		_players.clear();
	}
	
	public static void cleanColors()
	{
		for (L2PcInstance player : _players)
		{
			player.getAppearance().setNameColor(0xFFFFFF);
			player.broadcastUserInfo();
		}
	}
	
	public static void cleanPlayers()
	{
		for (L2PcInstance player : _players)
		{
			player.isinZodiac = false;
			player.ZodiacPoints = 0;
			_topkills = 0;
			_topplayer = null;
		}
	}
	
	public static void registerToChaos(L2PcInstance player)
	{
		if (player == null)
			return;
		if (!registerToChaosOk(player))
		{
			return;
		}
		_players.add(player);
		player.OriginalColor = player.getAppearance().getNameColor();
		player.isinZodiac = true;
		player.ZodiacPoints = 0;
		player.getAppearance().setNameColor(0x000000);
		player.broadcastUserInfo();
		player.sendMessage("You have joined Chaos Event.");
		addSuperHaste(player);
	}
	
	public static void addSuperHaste(L2PcInstance player)
	{
		if (player == null)
			return;
		final L2Skill skill = SkillTable.getInstance().getInfo(7029, 4);
		if (skill != null)
		{
			skill.getEffects(player, player);
		}
	}
	
	public static boolean registerToChaosOk(L2PcInstance chaosplayer)
	{
		if (_players.contains(chaosplayer))
		{
			chaosplayer.sendMessage("You already are in Chaos Event.");
			return false;
		}
		return true;
	}
	
	public static void removeFromChaos(L2PcInstance player)
	{
		if (!removeFromChaosOk(player))
		{
			return;
		}
		_players.remove(player);
		player.ZodiacPoints = 0;
		player.isinZodiac = false;
		player.sendMessage("You have left Chaos Event.");
		player.getAppearance().setNameColor(player.OriginalColor);
		player.broadcastUserInfo();
		removeSuperHaste(player);
	}
	
	public static boolean removeFromChaosOk(L2PcInstance chaosplayer)
	{
		if (!chaosplayer.isinZodiac)
		{
			chaosplayer.sendMessage("You are not registered in Chaos Event.");
			return true;
		}
		return false;
	}
	
	public static void getTopKiller()
	{
		for (L2PcInstance player : _players)
		{
			if (player.ZodiacPoints > _topkills)
			{
				_topplayer = player;
				_topkills = player.ZodiacPoints;
			}
			if ((player.ZodiacPoints > _topkills2) && (player.ZodiacPoints < _topkills))
			{
				_topplayer2 = player;
				_topkills2 = player.ZodiacPoints;
			}
			if ((player.ZodiacPoints > _topkills3) && (player.ZodiacPoints < _topkills2))
			{
				_topplayer3 = player;
				_topkills3 = player.ZodiacPoints;
			}
			if ((player.ZodiacPoints > _topkills4) && (player.ZodiacPoints < _topkills3))
			{
				_topplayer4 = player;
				_topkills4 = player.ZodiacPoints;
			}
			if ((player.ZodiacPoints > _topkills5) && (player.ZodiacPoints < _topkills4))
			{
				_topplayer5 = player;
				_topkills5 = player.ZodiacPoints;
			}
		}
	}
	
	public static void calculateRewards()
	{
		Announcements.getInstance().announceToAll("Winners of Chaos Event:");
		if (_topplayer != null)
		{
			_topplayer.addItem("Chaos Event Reward", Config.ZODIAC_REWARD, Config.ZODIAC_REWARD_COUN, _topplayer, true);
			Announcements.getInstance().announceToAll("1) " + _topplayer.getName());
		}
		if (_topplayer2 != null)
		{
			_topplayer2.addItem("Chaos Event Reward 2", Config.ZODIAC_REWARD, Config.ZODIAC_REWARD_COUN, _topplayer2, true);
			Announcements.getInstance().announceToAll("2) " + _topplayer2.getName());
		}
		if (_topplayer3 != null)
		{
			_topplayer3.addItem("Chaos Event Reward 3", Config.ZODIAC_REWARD, Config.ZODIAC_REWARD_COUN, _topplayer3, true);
			Announcements.getInstance().announceToAll("3) " + _topplayer3.getName());
		}
		if (_topplayer4 != null)
		{
			_topplayer4.addItem("Chaos Event Reward 4", Config.ZODIAC_REWARD, Config.ZODIAC_REWARD_COUN, _topplayer4, true);
			Announcements.getInstance().announceToAll("4) " + _topplayer4.getName());
		}
		if (_topplayer5 != null)
		{
			_topplayer5.addItem("Chaos Event Reward 5", Config.ZODIAC_REWARD, Config.ZODIAC_REWARD_COUN, _topplayer5, true);
			Announcements.getInstance().announceToAll("5) " + _topplayer5.getName());
		}
	}
	
	public static void removeSuperHaste(L2PcInstance activeChar)
	{
		if (activeChar != null)
		{
			L2Effect[] effects = activeChar.getAllEffects();
			
			for (L2Effect e : effects)
			{
				if ((e != null) && (e.getSkill().getId() == 7029))
				{
					e.exit();
					break;
				}
			}
		}
	}
	
	public static void registration()
	{
		ZodiacMain.ZodiacRegisterActive = true;
		Announcements.getInstance().announceToAll("Chaos Event has started!");
		Announcements.getInstance().announceToAll("Type .join to join and .leave to leave!");
		Announcements.getInstance().announceToAll("You have 5 minutes until the event is finished!");
		_isChaosActive = true;
		StartcheckBoss();
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			String html = HtmCache.getInstance().getHtm("data/html/zodiac/ChaosTuto.htm");
			NpcHtmlMessage warning = new NpcHtmlMessage(1);
			warning.setHtml(html);
			
			player.sendPacket(warning);
		}
		_players.clear();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				stopChaos();
			}
		}, 60 * 1000 * 5);
		
	}
	
	public static void onRevive(L2PcInstance player)
	{
		addSuperHaste(player);
	}
	
	public static void onDeath(L2PcInstance player, L2PcInstance killer)
	{
		if (killer.isinZodiac)
			killer.ZodiacPoints++;
	}
	
	private static ScheduledFuture<?> _BossChecker;
	
	static class checkBossZone implements Runnable
	{
		
		@Override
		public void run()
		{
			
			if (_isChaosActive)
			{
				try
				{
					for (L2PcInstance player : _players)
					{
						
						if (player == null)
							continue;
						
						for (L2BossZone zone : GrandBossManager.getInstance().getZones())
						{
							if (player.isinZodiac && zone.isCharacterInZone(player))
							{
								zone.removePlayer(player);
								player.teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
							}
						}
					}
					
				}
				catch (Exception e)
				{
					if (Config.DEVELOPER)
					{
						e.printStackTrace();
					}
				}
				
				StartcheckBoss();
				
			}
			else
				stopcheckBoss();
			
		}
	}
	
	static void stopcheckBoss()
	{
		if (_BossChecker != null)
			_BossChecker.cancel(true);
		_BossChecker = null;
	}
	
	static void StartcheckBoss()
	{
		_BossChecker = ThreadPoolManager.getInstance().scheduleGeneral(new checkBossZone(), seconds * 1000);
	}
}