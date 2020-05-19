package com.l2jhellas.gameserver.scrips.quests;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q140_ShadowFoxPart2 extends Quest
{
	private static final String qn = "Q140_ShadowFoxPart2";

	private static final int KLUCK = 30895;
	private static final int XENOVIA = 30912;

	private static final int DARK_CRYSTAL = 10347;
	private static final int DARK_OXYDE = 10348;
	private static final int CRYPTOGRAM_OF_THE_GODDESS_SWORD = 10349;

	private static final Map<Integer, Integer> MOBS = new HashMap<>();
	static
	{
		MOBS.put(20789, 45); 
		MOBS.put(20790, 58);
		MOBS.put(20791, 100);
		MOBS.put(20792, 92);
	}
	
	private static final int MIN_LEVEL = 37;
	private static final int CHANCE = 8;
	private static final int CRYSTAL_COUNT = 5;
	private static final int OXYDE_COUNT = 2;
	
	public Q140_ShadowFoxPart2()
	{
		super(140,qn,"Shadow Fox Part2");
		addStartNpc(KLUCK);
		addTalkId(KLUCK, XENOVIA);
		addKillId(MOBS.keySet());
		setItemsIds(DARK_CRYSTAL, DARK_OXYDE, CRYPTOGRAM_OF_THE_GODDESS_SWORD);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(qn);

		if (qs == null)
			return null;
		
		String htmltext = event;
		switch (event)
		{
			case "30895-05.html":
			case "30895-06.html":
			case "30912-03.html":
			case "30912-04.html":
			case "30912-05.html":
			case "30912-08.html":
			case "30895-10.html":
			{
				break;
			}
			case "30895-03.htm":
			{
				qs.setState(STATE_STARTED);
				qs.set("cond", "1");
				qs.playSound(QuestState.SOUND_ACCEPT);
				break;
			}
			case "30895-07.html":
			{
				qs.set("cond", "2");
				break;
			}
			case "30912-06.html":
			{
				qs.set("talk", "1");
				break;
			}
			case "30912-09.html":
			{
				qs.unset("talk");
				qs.set("cond", "3");
				break;
			}
			case "30912-14.html":
			{
				if (Rnd.get(10) < CHANCE)
				{
					if (qs.getQuestItemsCount(DARK_OXYDE) < OXYDE_COUNT)
					{
						qs.giveItems(DARK_OXYDE, 1);
						qs.takeItems(DARK_CRYSTAL, 5);
						return "30912-12.html";
					}
					qs.giveItems(CRYPTOGRAM_OF_THE_GODDESS_SWORD, 1);
					qs.takeItems(DARK_CRYSTAL, -1);
					qs.takeItems(DARK_OXYDE, -1);
					qs.set("cond", "4");
					return "30912-13.html";
				}
				qs.takeItems(DARK_CRYSTAL, 5);
				break;
			}
			case "30895-11.html":
			{
				qs.exitQuest(false);
				break;
			}
			default:
			{
				htmltext = null;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final L2PcInstance member = getRandomPartyMember(player, npc);

		if (member == null)
			return super.onKill(npc, player, isSummon);

		final QuestState qs = member.getQuestState(qn);
 
	     if(qs == null)
	 		return super.onKill(npc, player, isSummon);

		if (Rnd.get(100) < MOBS.get(npc.getNpcId()))
		{
			qs.giveItems(DARK_CRYSTAL, 1);
			qs.playSound(QuestState.SOUND_ITEMGET);
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		
		switch (npc.getNpcId())
		{
			case KLUCK:
			{
				switch (qs.getState())
				{
					case STATE_CREATED:
					{
						final QuestState qst = player.getQuestState(Q139_ShadowFoxPart1.class.getSimpleName());
						htmltext = (player.getLevel() >= MIN_LEVEL) ? ((qst != null) && qst.isCompleted()) ? "30895-01.htm" : "30895-00.htm" : "30895-02.htm";
						break;
					}
					case STATE_STARTED:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								htmltext = "30895-04.html";
								break;
							}
							case 2:
							case 3:
							{
								htmltext = "30895-08.html";
								break;
							}
							case 4:
							{
								if (qs.isSet("talk"))
									htmltext = "30895-10.html";
								else
								{
									qs.takeItems(CRYPTOGRAM_OF_THE_GODDESS_SWORD, -1);
									qs.set("talk", "1");
									htmltext = "30895-09.html";
								}
								break;
							}
						}
						break;
					}
					case STATE_COMPLETED:
					{
						htmltext = getAlreadyCompletedMsg();
						break;
					}
				}
				break;
			}
			case XENOVIA:
			{
				if (qs.isStarted())
				{
					switch (qs.getCond())
					{
						case 1:
						{
							htmltext = "30912-01.html";
							break;
						}
						case 2:
						{
							htmltext = qs.isSet("talk") ? "30912-07.html" : "30912-02.html";
							break;
						}
						case 3:
						{
							htmltext = (qs.getQuestItemsCount(DARK_CRYSTAL) >= CRYSTAL_COUNT) ? "30912-11.html" : "30912-10.html";
							break;
						}
						case 4:
						{
							htmltext = "30912-15.html";
							break;
						}
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q140_ShadowFoxPart2();
	}
}