package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;


public class Q138_TempleChampionPart2 extends Quest
{
	private static final String qn = "Q138_TempleChampionPart2";

	private static final int SYLVAIN = 30070;
	private static final int PUPINA = 30118;
	private static final int ANGUS = 30474;
	private static final int SLA = 30666;
	private static final int MOBS[] =
	{
		20176,
		20550, 
		20551, 
		20552, 
	};

	private static final int TEMPLE_MANIFESTO = 10341;
	private static final int RELICS_OF_THE_DARK_ELF_TRAINEE = 10342;
	private static final int ANGUS_RECOMMENDATION = 10343;
	private static final int PUPINAS_RECOMMENDATION = 10344;
	
	public Q138_TempleChampionPart2()
	{
		super(138,qn,"Temple Champion Part2");
		addStartNpc(SYLVAIN);
		addTalkId(SYLVAIN, PUPINA, ANGUS, SLA);
		addKillId(MOBS);
		setItemsIds(TEMPLE_MANIFESTO, RELICS_OF_THE_DARK_ELF_TRAINEE, ANGUS_RECOMMENDATION, PUPINAS_RECOMMENDATION);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(qn);
		
		if (qs == null)
			return getNoQuestMsg();

		switch (event)
		{
			case "30070-02.htm":
			{
				qs.setState(STATE_STARTED);
				qs.set("cond", "1");
				qs.playSound(QuestState.SOUND_ACCEPT);
				qs.giveItems(TEMPLE_MANIFESTO, 1);
				break;
			}
			case "30070-05.html":
			{
				qs.exitQuest(false);
				break;
			}
			case "30070-03.html":
			{
				qs.set("cond", "2");
				break;
			}
			case "30118-06.html":
			{
				qs.set("cond", "3");
				break;
			}
			case "30118-09.html":
			{
				qs.set("cond", "6");
				qs.giveItems(PUPINAS_RECOMMENDATION, 1);
				break;
			}
			case "30474-02.html":
			{
				qs.set("cond", "4");
				break;
			}
			case "30666-02.html":
			{
				if (qs.hasQuestItems(PUPINAS_RECOMMENDATION))
				{
					qs.set("talk", "1");
					qs.takeItems(PUPINAS_RECOMMENDATION, -1);
				}
				break;
			}
			case "30666-03.html":
			{
				if (qs.hasQuestItems(TEMPLE_MANIFESTO))
				{
					qs.set("talk", "2");
					qs.takeItems(TEMPLE_MANIFESTO, -1);
				}
				break;
			}
			case "30666-08.html":
			{
				qs.set("cond", "7");
				qs.unset("talk");
				break;
			}
		}
		return event;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState qs = player.getQuestState(qn);
		if ((qs != null) && qs.isStarted() && qs.isCond(4) && (qs.getQuestItemsCount(RELICS_OF_THE_DARK_ELF_TRAINEE) < 10))
		{
			qs.giveItems(RELICS_OF_THE_DARK_ELF_TRAINEE, 1);
			if (qs.getQuestItemsCount(RELICS_OF_THE_DARK_ELF_TRAINEE) >= 10)
				qs.playSound(QuestState.SOUND_MIDDLE);
			else
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
			case SYLVAIN:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						htmltext = "30070-02.htm";
						break;
					}
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
					{
						htmltext = "30070-03.html";
						break;
					}
					case 7:
					{
						htmltext = "30070-04.html";
						break;
					}
					default:
					{
						if (qs.isCompleted())
						{
							return getAlreadyCompletedMsg();
						}
						final QuestState qst = player.getQuestState(Q137_TempleChampionPart1.class.getSimpleName());
						htmltext = (player.getLevel() >= 36) ? ((qst != null) && qst.isCompleted()) ? "30070-01.htm" : "30070-00a.htm" : "30070-00.htm";
						break;
					}
				}
				break;
			}
			case PUPINA:
			{
				switch (qs.getCond())
				{
					case 2:
					{
						htmltext = "30118-01.html";
						break;
					}
					case 3:
					case 4:
					{
						htmltext = "30118-07.html";
						break;
					}
					case 5:
					{
						htmltext = "30118-08.html";
						
						if (qs.hasQuestItems(ANGUS_RECOMMENDATION))
							qs.takeItems( ANGUS_RECOMMENDATION, -1);

						break;
					}
					case 6:
					{
						htmltext = "30118-10.html";
						break;
					}
				}
				break;
			}
			case ANGUS:
			{
				switch (qs.getCond())
				{
					case 3:
					{
						htmltext = "30474-01.html";
						break;
					}
					case 4:
					{
						if (qs.getQuestItemsCount(RELICS_OF_THE_DARK_ELF_TRAINEE) >= 10)
						{
							qs.takeItems(RELICS_OF_THE_DARK_ELF_TRAINEE, -1);
							qs.giveItems(ANGUS_RECOMMENDATION, 1);
							qs.set("cond", "5");
							htmltext = "30474-04.html";
						}
						else
						{
							htmltext = "30474-03.html";
						}
						break;
					}
					case 5:
					{
						htmltext = "30474-05.html";
						break;
					}
				}
				break;
			}
			case SLA:
			{
				switch (qs.getCond())
				{
					case 6:
					{
						switch (qs.getInt("talk"))
						{
							case 1:
							{
								htmltext = "30666-02.html";
								break;
							}
							case 2:
							{
								htmltext = "30666-03.html";
								break;
							}
							default:
							{
								htmltext = "30666-01.html";
								break;
							}
						}
						break;
					}
					case 7:
					{
						htmltext = "30666-09.html";
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q138_TempleChampionPart2();
	}
}
