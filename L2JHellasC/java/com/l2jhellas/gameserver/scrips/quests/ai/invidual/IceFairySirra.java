package com.l2jhellas.gameserver.scrips.quests.ai.invidual;

import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.datatables.xml.DoorData;
import com.l2jhellas.gameserver.instancemanager.ZoneManager;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.model.spawn.L2Spawn;
import com.l2jhellas.gameserver.model.spawn.SpawnData;
import com.l2jhellas.gameserver.model.zone.L2ZoneType;
import com.l2jhellas.gameserver.model.zone.type.L2BossZone;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class IceFairySirra extends AbstractNpcAI
{
	private static final Logger _log = Logger.getLogger(IceFairySirra.class.getName());
	private static final int STEWARD = 32029;
	private static final int SILVER_HEMOCYTE = 8057;
	L2ZoneType _Zone = ZoneManager.getInstance().getZoneById(110016);
	private static L2PcInstance _player = null;
	protected ArrayList<L2Npc> _allMobs = new ArrayList<>();
	protected Future<?> _onDeadEventTask = null;
	
	public IceFairySirra()
	{
		super(IceFairySirra.class.getSimpleName(), "ai/individual");
		int[] mob =
		{
			STEWARD,
			22100,
			22102,
			22104
		};
		registerMobs(mob);
		addEventId(STEWARD, QuestEventType.QUEST_START);
		addEventId(STEWARD, QuestEventType.ON_TALK);
		addEventId(STEWARD, QuestEventType.ON_FIRST_TALK);
		init();
	}
	
	public void init()
	{				
		L2Npc steward = findTemplate(STEWARD);
		if (steward != null)
			steward.setBusy(false);
		openGates();
	}
	
	public void cleanUp()
	{
		init();
		cancelQuestTimer("30MinutesRemaining", null, _player);
		cancelQuestTimer("20MinutesRemaining", null, _player);
		cancelQuestTimer("10MinutesRemaining", null, _player);
		cancelQuestTimer("End", null, _player);
		for (L2Npc mob : _allMobs)
		{
			try
			{
				mob.getSpawn().stopRespawn();
				mob.deleteMe();
			}
			catch (Exception e)
			{
				_log.severe(IceFairySirra.class.getName() + ": Failed deleting mob.");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
		}
		_allMobs.clear();
	}
	
	public L2Npc findTemplate(int npcId)
	{
		L2Spawn spawn = SpawnData.getInstance().getSpawn(npcId);
		return spawn != null ? spawn.getLastSpawn() : null;
	}
	
	protected void openGates()
	{
		for (int i = 23140001; i < 23140003; i++)
		{
			try
			{
				L2DoorInstance door = DoorData.getInstance().getDoor(i);
				if (door != null)
				{
					door.openMe();
				}
				else
				{
					_log.warning(IceFairySirra.class.getName() + ": Attempted to open undefined door. doorId: " + i);
				}
			}
			catch (Exception e)
			{
				_log.warning(IceFairySirra.class.getName() + ": Failed closing door");
			}
		}
	}
	
	protected void closeGates()
	{
		for (int i = 23140001; i < 23140003; i++)
		{
			try
			{
				L2DoorInstance door = DoorData.getInstance().getDoor(i);
				if (door != null)
				{
					door.closeMe();
				}
				else
				{
					_log.warning(IceFairySirra.class.getName() + ": Attempted to close undefined door. doorId: " + i);
				}
			}
			catch (Exception e)
			{
				_log.warning(IceFairySirra.class.getName() + ": Failed closing door");
			}
		}
	}
	
	public boolean checkItems(L2PcInstance player)
	{
		if (player.getParty() != null)
		{
			for (L2PcInstance pc : player.getParty().getPartyMembers())
			{
				L2ItemInstance i = pc.getInventory().getItemByItemId(SILVER_HEMOCYTE);
				if (i == null || i.getCount() < 10)
				{
					return false;
				}
			}
		}
		else
			return false;
		return true;
	}
	
	public void destroyItems(L2PcInstance player)
	{
		if (player.getParty() != null)
		{
			for (L2PcInstance pc : player.getParty().getPartyMembers())
			{
				L2ItemInstance i = pc.getInventory().getItemByItemId(SILVER_HEMOCYTE);
				pc.destroyItem("Hemocytes", i.getObjectId(), 10, null, false);
			}
		}
		else
			cleanUp();
	}
	
	public void teleportInside(L2PcInstance player)
	{
		if (player.getParty() != null)
		{
			for (L2PcInstance pc : player.getParty().getPartyMembers())
			{
				pc.teleToLocation(113533, -126159, -3488, false);
				
				((L2BossZone) _Zone).allowPlayerEntry(pc, 2103);
			}
		}
		else
			cleanUp();
	}
	
	public void screenMessage(L2PcInstance player, String text, int time)
	{
		if (player.getParty() != null)
		{
			for (L2PcInstance pc : player.getParty().getPartyMembers())
			{
				pc.sendPacket(new ExShowScreenMessage(text, time));
			}
		}
		else
			cleanUp();
	}
	
	public void doSpawns()
	{
		int[][] mobs =
		{
			{
				29060,
				102722 - 50,
				-127892 - 50,
				-2768 + 30
			},
			{
				29056,
				102722,
				-127892,
				-2768
			},
			{
				22100,
				102722 + 50,
				-127892 + 50,
				-2768 + 30
			},
			{
				22102,
				102722 - 100,
				-127892 - 100,
				-2768 + 30
			},
			{
				22104,
				102722 + 100,
				-127892 + 100,
				-2768 + 30
			}
		};
		
		L2Spawn spawnDat;
		L2NpcTemplate template;
		try
		{
			for (int i = 0; i < 5; i++)
			{
				template = NpcData.getInstance().getTemplate(mobs[i][0]);
				if (template != null)
				{
					spawnDat = new L2Spawn(template);
					spawnDat.setAmount(1);
					spawnDat.setLocx(mobs[i][1]);
					spawnDat.setLocy(mobs[i][2]);
					spawnDat.setLocz(mobs[i][3]);
					spawnDat.setHeading(0);
					spawnDat.setRespawnDelay(60);
					SpawnData.getInstance().addNewSpawn(spawnDat, false);
					_allMobs.add(spawnDat.doSpawn());
					spawnDat.stopRespawn();
				}
				else
				{
					_log.warning(IceFairySirra.class.getName() + ": Data missing in NPC table for ID: " + mobs[i][0]);
				}
			}
		}
		catch (Exception e)
		{
			_log.warning(IceFairySirra.class.getName() + ": Spawns could not be initialized: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public String getHtmlPath(int val)
	{
		String pom = "";
		pom = "32029-" + val;
		if (val == 0)
			pom = "32029";
		String temp = "data/html/default/" + pom + ".htm";
		if (HtmCache.getInstance().isLoadable(temp))
			return temp;
		// If the file is not found, the standard message
		// "I have nothing to say to you" is returned
		return "data/html/npcdefault.htm";
	}
	
	public void sendHtml(L2Npc npc, L2PcInstance player, String filename)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(npc.getObjectId()));
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getQuestState("IceFairySirra") == null)
			newQuestState(player);
		player.setLastQuestNpcObject(npc.getObjectId());
		String filename = "";
		if (npc.isBusy())
			filename = getHtmlPath(10);
		else
			filename = getHtmlPath(0);
		sendHtml(npc, player, filename);
		return null;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("check_condition"))
		{
			if (npc.isBusy())// should never happen
				return super.onAdvEvent(event, npc, player);
			String filename = "";
			if (player.isInParty() && player.getParty().getPartyLeaderOID() == player.getObjectId())
			{
				if (checkItems(player) == true)
				{
					startQuestTimer("start", 100000, null, player, false);
					_player = player;
					destroyItems(player);
					player.getInventory().addItem("Scroll", 8379, 3, player, null);
					npc.setBusy(true);
					screenMessage(player, "Steward: Please wait a moment.", 100000);
					filename = getHtmlPath(3);
				}
				else
				{
					filename = getHtmlPath(2);
				}
			}
			else
			{
				filename = getHtmlPath(1);
			}
			sendHtml(npc, player, filename);
		}
		else if (event.equalsIgnoreCase("start"))
		{
			if (_Zone == null)
			{
				_log.warning(IceFairySirra.class.getName() + ": Failed to load zone");
				cleanUp();
				return super.onAdvEvent(event, npc, player);
			}
			closeGates();
			doSpawns();
			startQuestTimer("Party_Port", 2000, null, player, false);
			startQuestTimer("End", 1802000, null, player, false);
		}
		else if (event.equalsIgnoreCase("Party_Port"))
		{
			teleportInside(player);
			screenMessage(player, "Steward: Please restore the Queen's appearance!", 10000);
			startQuestTimer("30MinutesRemaining", 300000, null, player, false);
		}
		else if (event.equalsIgnoreCase("30MinutesRemaining"))
		{
			screenMessage(player, "30 minute(s) are remaining.", 10000);
			startQuestTimer("20minutesremaining", 600000, null, player, false);
		}
		else if (event.equalsIgnoreCase("20MinutesRemaining"))
		{
			screenMessage(player, "20 minute(s) are remaining.", 10000);
			startQuestTimer("10minutesremaining", 600000, null, player, false);
		}
		else if (event.equalsIgnoreCase("10MinutesRemaining"))
		{
			screenMessage(player, "Steward: Waste no time! Please hurry!", 10000);
		}
		else if (event.equalsIgnoreCase("End"))
		{
			screenMessage(player, "Steward: Was it indeed too much to ask.", 10000);
			cleanUp();
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	public static void main(String[] args)
	{
		new IceFairySirra();
	}
}
