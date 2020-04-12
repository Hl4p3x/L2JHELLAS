package com.l2jhellas.gameserver.scrips.quests;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q142_FallenAngelRequestOfDawn extends Quest
{
	private static final String qn = "Q142_FallenAngelRequestOfDawn";

	private static final int RAYMOND = 30289;
	private static final int CASIAN = 30612;
	private static final int NATOOLS = 30894;
	private static final int ROCK = 32368;

	private static final int FALLEN_ANGEL = 27338;
	
	private static final Map<Integer, Integer> MOBS = new HashMap<>();
	
	static
	{
		MOBS.put(20079, 338);
		MOBS.put(20080, 363);
		MOBS.put(20081, 611);
		MOBS.put(20082, 371);
		MOBS.put(20084, 421);
		MOBS.put(20086, 371);
		MOBS.put(20087, 900);
		MOBS.put(20088, 1000); 
		MOBS.put(20089, 431); 
		MOBS.put(20090, 917);
	}
	
	private static final int CRYPTOGRAM_OF_THE_ANGEL_SEARCH = 10351;
	private static final int PROPHECY_FRAGMENT = 10352;
	private static final int FALLEN_ANGEL_BLOOD = 10353;

	private static final int FRAGMENT_COUNT = 30;
	private boolean isAngelSpawned = false;
	
	public Q142_FallenAngelRequestOfDawn()
	{
		super(142,qn, "Fallen Angel Request Of Dawn");
		addTalkId(NATOOLS, RAYMOND, CASIAN, ROCK);
		addKillId(MOBS.keySet());
		addKillId(FALLEN_ANGEL);
		setItemsIds(CRYPTOGRAM_OF_THE_ANGEL_SEARCH, PROPHECY_FRAGMENT, FALLEN_ANGEL_BLOOD);
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
			case "30894-02.html":
			case "30289-03.html":
			case "30289-04.html":
			case "30612-03.html":
			case "30612-04.html":
			case "30612-06.html":
			case "30612-07.html":
			{
				break;
			}
			case "30894-01.html":
			{
				qs.setState(STATE_STARTED);
				qs.set("cond", "1");
				qs.playSound(QuestState.SOUND_ACCEPT);
				break;
			}
			case "30894-03.html":
			{
				qs.giveItems(CRYPTOGRAM_OF_THE_ANGEL_SEARCH, 1);
				qs.set("cond", "2");
				break;
			}
			case "30289-05.html":
			{
				qs.unset("talk");
				qs.set("cond", "3");
				break;
			}
			case "30612-05.html":
			{
				qs.set("talk", "2");
				break;
			}
			case "30612-08.html":
			{
				qs.unset("talk");
				qs.set("cond", "4");
				break;
			}
			case "32368-04.html":
			{
				if (isAngelSpawned)
					return "32368-03.html";

				addSpawn(FALLEN_ANGEL, npc.getX() + 100, npc.getY() + 100, npc.getZ(), 0, false, 120000,false);
				isAngelSpawned = true;
				startQuestTimer("despawn", 120000, null, player,false);
				break;
			}
			case "despawn":
			{
				if (isAngelSpawned)
					isAngelSpawned = false;
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
		final QuestState qs;
		if (npc.getNpcId() == FALLEN_ANGEL)
		{
			qs = player.getQuestState(qn);
			if (qs.getInt("cond") == 5)
			{
				qs.giveItems(FALLEN_ANGEL_BLOOD, 1);
				qs.set("cond", "6");
				isAngelSpawned = false;
			}
		}
		else
		{
			final L2PcInstance member = getRandomPartyMember(player, npc);
			if (member != null)
			{
				qs =  member.getQuestState(qn);
				if(qs == null)
					return null;
				if (Rnd.get(1000) < MOBS.get(npc.getNpcId()))
				{
					qs.giveItems(PROPHECY_FRAGMENT, 1);
					if (qs.getQuestItemsCount(PROPHECY_FRAGMENT) >= FRAGMENT_COUNT)
					{
						qs.takeItems(PROPHECY_FRAGMENT, -1);
						qs.set("cond", "5");
					}
					else
						qs.playSound(QuestState.SOUND_ITEMGET);
				}
			}
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
			case NATOOLS:
			{
				switch (qs.getState())
				{
					case STATE_STARTED:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								htmltext = "30894-01.html";
								break;
							}
							default:
							{
								htmltext = "30894-04.html";
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
			case RAYMOND:
			{
				if (qs.isStarted())
				{
					switch (qs.getCond())
					{
						case 1:
						{
							htmltext = "30289-01.html";
							break;
						}
						case 2:
						{
							if (qs.isSet("talk"))
								htmltext = "30289-03.html";
							else
							{
								qs.takeItems(CRYPTOGRAM_OF_THE_ANGEL_SEARCH, -1);
								qs.set("talk", "1");
								htmltext = "30289-02.html";
							}
							break;
						}
						case 3:
						case 4:
						case 5:
						{
							htmltext = "30289-06.html";
							break;
						}
						case 6:
						{
							qs.exitQuest(false);
							htmltext = "30289-07.html";
							break;
						}
					}
				}
				break;
			}
			case CASIAN:
			{
				if (qs.isStarted())
				{
					switch (qs.getCond())
					{
						case 1:
						case 2:
						{
							htmltext = "30612-01.html";
							break;
						}
						case 3:
						{
							if (qs.getInt("talk") == 1)
								htmltext = "30612-03.html";
							else if (qs.getInt("talk") == 2)
								htmltext = "30612-06.html";
							else
							{
								htmltext = "30612-02.html";
								qs.set("talk", "1");
							}
							break;
						}
						case 4:
						case 5:
						case 6:
						{
							htmltext = "30612-09.html";
							break;
						}
					}
				}
				break;
			}
			case ROCK:
			{
				if (qs.isStarted())
				{
					switch (qs.getCond())
					{
						case 5:
						{
							htmltext = "32368-02.html";
							break;
						}
						case 6:
						{
							htmltext = "32368-05.html";
							break;
						}
						default:
						{
							htmltext = "32368-01.html";
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
		new Q142_FallenAngelRequestOfDawn();
	}
}