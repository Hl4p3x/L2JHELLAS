
package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;


public class Q143_FallenAngelRequestOfDusk extends Quest
{
	
	private static final String qn = "Q143_FallenAngelRequestOfDusk";

	private static final int TOBIAS = 30297;
	private static final int CASIAN = 30612;
	private static final int NATOOLS = 30894;
	private static final int ROCK = 32368;
	private static final int ANGEL = 32369;

	private static final int SEALED_PROPHECY_PATH_OF_THE_GOD = 10354;
	private static final int PROPHECY_PATH_OF_THE_GOD = 10355;
	private static final int EMPTY_SOUND_CRYSTAL = 10356;
	private static final int ANGEL_MEDICINE = 10357;
	private static final int ANGELS_MESSAGE = 10358;

	private boolean isAngelSpawned = false;
	
	public Q143_FallenAngelRequestOfDusk()
	{
		super(143,qn, "Fallen Angel Request Of Dusk");
		addTalkId(NATOOLS, TOBIAS, CASIAN, ROCK, ANGEL);
		setItemsIds(SEALED_PROPHECY_PATH_OF_THE_GOD, PROPHECY_PATH_OF_THE_GOD, EMPTY_SOUND_CRYSTAL, ANGEL_MEDICINE, ANGELS_MESSAGE);
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
			case "30297-04.html":
			case "30612-05.html":
			case "30612-06.html":
			case "30612-07.html":
			case "30612-08.html":
			case "32369-04.html":
			case "32369-05.html":
			case "32369-07.html":
			case "32369-08.html":
			case "32369-09.html":
			case "32369-10.html":
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
				qs.set("cond", "2");
				qs.giveItems(SEALED_PROPHECY_PATH_OF_THE_GOD, 1);
				break;
			}
			case "30297-03.html":
			{
				qs.takeItems(SEALED_PROPHECY_PATH_OF_THE_GOD, -1);
				qs.set("talk", "1");
				break;
			}
			case "30297-05.html":
			{
				qs.unset("talk");
				qs.set("cond", "3");
				qs.giveItems(PROPHECY_PATH_OF_THE_GOD, 1);
				qs.giveItems(EMPTY_SOUND_CRYSTAL, 1);
				break;
			}
			case "30612-03.html":
			{
				qs.takeItems(PROPHECY_PATH_OF_THE_GOD, -1);
				qs.set("talk", "1");
				break;
			}
			case "30612-09.html":
			{
				qs.unset("talk");
				qs.set("cond", "4");
				qs.giveItems(ANGEL_MEDICINE, 1);
				break;
			}
			case "32368-04.html":
			{
				if (isAngelSpawned)
					return "32368-03.html";

				addSpawn(ANGEL, npc.getX() + 100, npc.getY() + 100, npc.getZ(), 0, false, 120000,false);
				startQuestTimer("despawn", 120000, null, player,false);
				isAngelSpawned = true;
				break;
			}
			case "32369-03.html":
			{
				qs.takeItems(ANGEL_MEDICINE, -1);
				qs.set("talk", "1");
				break;
			}
			case "32369-06.html":
			{
				qs.set("talk", "2");
				break;
			}
			case "32369-11.html":
			{
				qs.unset("talk");
				qs.takeItems(EMPTY_SOUND_CRYSTAL, -1);
				qs.giveItems(ANGELS_MESSAGE, 1);
				qs.set("cond", "5");
				npc.deleteMe();
				isAngelSpawned = false;
				break;
			}
			case "despawn":
			{
				if (isAngelSpawned)
					isAngelSpawned = false;
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
						switch (qs.getInt("cond"))
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
			case TOBIAS:
			{
				if (qs.isStarted())
				{
					switch (qs.getInt("cond"))
					{
						case 1:
						{
							htmltext = "30297-01.html";
							break;
						}
						case 2:
						{
							htmltext = qs.isSet("talk") ? "30297-04.html" : "30297-02.html";
							break;
						}
						case 3:
						case 4:
						{
							htmltext = "30297-06.html";
							break;
						}
						case 5:
						{
							qs.exitQuest(false);
							htmltext = "30297-07.html";
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
							htmltext = qs.isSet("talk") ? "30612-04.html" : "30612-02.html";
							break;
						}
						default:
						{
							htmltext = "30612-10.html";
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
						case 1:
						case 2:
						case 3:
						{
							htmltext = "32368-01.html";
							break;
						}
						case 4:
						{
							htmltext = "32368-02.html";
							break;
						}
						case 5:
						{
							htmltext = "32368-05.html";
							break;
						}
					}
				}
				break;
			}
			case ANGEL:
			{
				if (qs.isStarted())
				{
					switch (qs.getCond())
					{
						case 1:
						case 2:
						case 3:
						{
							htmltext = "32369-01.html";
							break;
						}
						case 4:
						{
							if (qs.getInt("talk") == 1)
								htmltext = "32369-04.html";
							else if (qs.getInt("talk") == 2)
								htmltext = "32369-07.html";
							else
								htmltext = "32369-02.html";
							
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
		new Q143_FallenAngelRequestOfDusk();
	}
}