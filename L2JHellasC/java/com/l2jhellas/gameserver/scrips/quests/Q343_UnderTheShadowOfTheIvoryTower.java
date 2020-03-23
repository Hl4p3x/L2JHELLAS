package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q343_UnderTheShadowOfTheIvoryTower extends Quest
{	
	private static final String qn = "Q343_UnderTheShadowOfTheIvoryTower";

	public int CEMA = 30834;
	public int ICARUS = 30835;
	public int MARSHA = 30934;
	public int TRUMPIN = 30935;
	public int ORB = 4364;
	public int ECTOPLASM = 4365;
	public int CHANCE = 50;
	
	public int[] ALLOWED_CLASSES =
	{
		11,
		12,
		13,
		14,
		26,
		27,
		28,
		39,
		40,
		41
	};
	
	public int[] MOBS = new int[]
	{
		20563,
		20564,
		20565,
		20566
	};
		
	public Q343_UnderTheShadowOfTheIvoryTower()
	{
		super(343, qn, "Under the Shadow of the Ivory Tower");		
		setItemsIds(4364);
		
		addStartNpc(30834);
		addTalkId(30834, 30835, 30934, 30935);
		
		for (int i : MOBS)
		{
			addKillId(i);
		}		
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return htmltext;
		
		final int random1 = Rnd.get(3);
		final int random2 = Rnd.get(2);
		final int orbs = st.getQuestItemsCount(4364);
		
		if ("30834-03.htm".equalsIgnoreCase(event))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if ("30834-08.htm".equalsIgnoreCase(event))
		{
			if (orbs > 0)
			{
				st.giveItems(57, orbs * 120);
				st.takeItems(4364, -1);
			}
			else
				htmltext = "30834-08.htm";
		}
		else if ("30834-09.htm".equalsIgnoreCase(event))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if ("30934-02.htm".equalsIgnoreCase(event) || "30934-03.htm".equalsIgnoreCase(event))
		{
			if (orbs < 10)
				htmltext = "noorbs.htm";
			else if ("30934-03.htm".equalsIgnoreCase(event))
			{
				if (orbs >= 10)
				{
					st.takeItems(4364, 10);
					st.set("playing", "1");
				}
				else
					htmltext = "noorbs.htm";
			}
		}
		else if ("30934-04.htm".equalsIgnoreCase(event))
		{
			if (st.getInt("playing") > 0)
			{
				switch (random1)
				{
					case 0:
					{
						htmltext = "30934-05.htm";
						st.giveItems(4364, 10);
						break;
					}
					case 1:
					{
						htmltext = "30934-06.htm";
						break;
					}
					default:
					{
						htmltext = "30934-04.htm";
						st.giveItems(4364, 20);
						break;
					}
				}
				st.unset("playing");
			}
			else
			{
				htmltext = "Player is cheating";
				st.takeItems(4364, -1);
				st.exitQuest(true);
			}
		}
		else if ("30934-05.htm".equalsIgnoreCase(event))
		{
			if (st.getInt("playing") > 0)
			{
				switch (random1)
				{
					case 0:
						htmltext = "30934-04.htm";
						st.giveItems(4364, 20);
						break;
					case 1:
						htmltext = "30934-05.htm";
						st.giveItems(4364, 10);
						break;
					default:
						htmltext = "30934-06.htm";
						break;
				}
				st.unset("playing");
			}
			else
			{
				htmltext = "Player is cheating";
				st.takeItems(4364, -1);
				st.exitQuest(true);
			}
		}
		else if ("30934-06.htm".equalsIgnoreCase(event))
		{
			if (st.getInt("playing") > 0)
			{
				switch (random1)
				{
					case 0:
						htmltext = "30934-04.htm";
						st.giveItems(4364, 20);
						break;
					case 1:
						htmltext = "30934-06.htm";
						break;
					default:
						htmltext = "30934-05.htm";
						st.giveItems(4364, 10);
						break;
				}
				st.unset("playing");
			}
			else
			{
				htmltext = "Player is cheating";
				st.takeItems(4364, -1);
				st.exitQuest(true);
			}
		}
		else if ("30935-02.htm".equalsIgnoreCase(event) || "30935-03.htm".equalsIgnoreCase(event))
		{
			st.unset("toss");
			if (orbs < 10)
				htmltext = "noorbs.htm";
		}
		else if ("30935-05.htm".equalsIgnoreCase(event))
		{
			if (orbs >= 10)
			{
				if (random2 == 0)
				{
					final int toss = st.getInt("toss");
					if (toss == 4)
					{
						st.unset("toss");
						st.giveItems(4364, 150);
						htmltext = "30935-07.htm";
					}
					else
					{
						st.set("toss", String.valueOf(toss + 1));
						htmltext = "30935-04.htm";
					}
				}
				else
				{
					st.unset("toss");
					st.takeItems(4364, 10);
				}
			}
			else
			{
				htmltext = "noorbs.htm";
			}
		}
		else if ("30935-06.htm".equalsIgnoreCase(event))
		{
			if (orbs >= 10)
			{
				final int toss = st.getInt("toss");
				st.unset("toss");
				switch (toss)
				{
					case 1:
						st.giveItems(4364, 10);
						break;
					case 2:
						st.giveItems(4364, 30);
						break;
					case 3:
						st.giveItems(4364, 70);
						break;
					case 4:
						st.giveItems(4364, 150);
						break;
				}
			}
			else
			{
				htmltext = "noorbs.htm";
			}
		}
		else if ("30835-02.htm".equalsIgnoreCase(event))
		{
			if (st.getQuestItemsCount(4365) > 0)
			{
				st.takeItems(4365, 1);
				final int random3 = Rnd.get(1000);
				
				if (random3 <= 119)
					st.giveItems(955, 1);
				else if (random3 <= 169)
					st.giveItems(951, 1);
				else if (random3 <= 329)
					st.giveItems(2511, (Rnd.get(200) + 401));
				else if (random3 <= 559)
					st.giveItems(2510, (Rnd.get(200) + 401));
				else if (random3 <= 561)
					st.giveItems(316, 1);
				else if (random3 <= 578)
					st.giveItems(630, 1);
				else if (random3 <= 579)
					st.giveItems(188, 1);
				else if (random3 <= 581)
					st.giveItems(885, 1);
				else if (random3 <= 582)
					st.giveItems(103, 1);
				else if (random3 <= 584)
					st.giveItems(917, 1);
				else
					st.giveItems(736, 1);
			}
			else
				htmltext = "30835-03.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		
		if (st == null)
			return htmltext;
		
		final int npcId = npc.getNpcId();
		final int id = st.getState();
		switch (npcId)
		{
			case 30834:
			{
				if (id != 2)
				{
					for (int i : ALLOWED_CLASSES)
					{
						if ((st.getPlayer().getClassId().getId() == i) && (st.getPlayer().getLevel() >= 40))
							htmltext = "30834-01.htm";
					}
					if (!"30834-01.htm".equals(htmltext))
					{
						htmltext = "30834-07.htm";
						st.exitQuest(true);
					}
				}
				else if (st.getQuestItemsCount(4364) > 0)
					htmltext = "30834-06.htm";
				else
					htmltext = "30834-05.htm";
				break;
			}
			case 30835:
			{
				htmltext = "30835-01.htm";
				break;
			}
			case 30934:
			{
				htmltext = "30934-01.htm";
				break;
			}
			case 30935:
			{
				htmltext = "30935-01.htm";
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		
		if (st == null)
			return null;
		
		if (Rnd.get(100) < 50)
		{
			st.giveItems(4364, 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q343_UnderTheShadowOfTheIvoryTower();
	}
}
