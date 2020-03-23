package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q136_MoreThanMeetsTheEye extends Quest
{
	private static final String qn = "Q136_MoreThanMeetsTheEye";

	private static final int HARDIN = 30832;
	private static final int ERRICKIN = 30701;
	private static final int CLAYTON = 30464;

	private static final int GLASS_JAGUAR = 20250;
	private static final int GHOST1 = 20636;
	private static final int GHOST2 = 20637;
	private static final int GHOST3 = 20638;
	private static final int MIRROR = 20639;

	private static final int ECTOPLASM = 9787;
	private static final int STABILIZED_ECTOPLASM = 9786;
	private static final int ORDER = 9788;
	private static final int GLASS_JAGUAR_CRYSTAL = 9789;
	private static final int BOOK_OF_SEAL = 9790;
	private static final int TRANSFORM_BOOK = 9648;

	private static final int MIN_LEVEL = 50;
	private static final int ECTOPLASM_COUNT = 35;
	private static final int CRYSTAL_COUNT = 5;
	private static final int[] CHANCES =
	{
		0,
		40,
		90,
		290
	};
	
	public Q136_MoreThanMeetsTheEye()
	{
		super(136,qn,"More Than Meets The Eye");
		addStartNpc(HARDIN);
		addTalkId(HARDIN, ERRICKIN, CLAYTON);
		addKillId(GHOST1, GHOST2, GHOST3, GLASS_JAGUAR, MIRROR);
		
		setItemsIds(ECTOPLASM, STABILIZED_ECTOPLASM, ORDER, GLASS_JAGUAR_CRYSTAL, BOOK_OF_SEAL);
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
			case "30832-05.html":
			case "30832-06.html":
			case "30832-12.html":
			case "30832-13.html":
			case "30832-18.html":
			{
				break;
			}
			case "30832-03.htm":
			{
				qs.setState(STATE_STARTED);
				qs.set("cond", "1");
				qs.playSound(QuestState.SOUND_ACCEPT);
				break;
			}
			case "30832-07.html":
			{
				qs.set("cond", "2");
				break;
			}
			case "30832-11.html":
			{
				qs.set("talked", "2");
				break;
			}
			case "30832-14.html":
			{
				qs.unset("talked");
				qs.giveItems(ORDER, 1);
				qs.set("cond", "6");
				break;
			}
			case "30832-17.html":
			{
				qs.set("talked", "2");
				break;
			}
			case "30832-19.html":
			{
				qs.giveItems(TRANSFORM_BOOK, 1);
				qs.exitQuest(false);
				break;
			}
			case "30701-03.html":
			{
				qs.set("cond", "3");
				break;
			}
			case "30464-03.html":
			{
				qs.takeItems(ORDER, -1);
				qs.set("cond", "7");
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
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = killer.getQuestState(qn);

		if (qs == null)
			return super.onKill(npc, killer, isSummon);
		
		final int npcId = npc.getNpcId();
		if ((npcId != GLASS_JAGUAR) && qs.isCond(3))
		{
			final int count = ((npcId == MIRROR) && ((qs.getQuestItemsCount(ECTOPLASM) + 2) < ECTOPLASM_COUNT)) ? 2 : 1;
			final int index = npcId - GHOST1;
			
			if ((Rnd.get(1000) < CHANCES[index]) && ((qs.getQuestItemsCount(ECTOPLASM) + count) < ECTOPLASM_COUNT))
				qs.giveItems(ECTOPLASM, 1);

			giveItem(qs, ECTOPLASM, count, ECTOPLASM_COUNT, 4);
		}
		else if ((npcId == GLASS_JAGUAR) && qs.isCond(7))
			giveItem(qs, GLASS_JAGUAR_CRYSTAL, 1, CRYSTAL_COUNT, 8);

		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(qn);

		String htmltext = getNoQuestMsg();
		
		switch (npc.getNpcId())
		{
			case HARDIN:
			{
				switch (qs.getState())
				{
					case STATE_CREATED:
					{
						htmltext = (player.getLevel() >= MIN_LEVEL) ? "30832-01.htm" : "30832-02.htm";
						break;
					}
					case STATE_STARTED:
					{
						switch (qs.getCond())
						{
							case 1:
							{
								htmltext = "30832-04.html";
								break;
							}
							case 2:
							case 3:
							case 4:
							{
								htmltext = "30832-08.html";
								break;
							}
							case 5:
							{
								if (qs.getInt("talked") == 1)
									htmltext = "30832-10.html";
								else if (qs.getInt("talked") == 2)
									htmltext = "30832-12.html";
								else if (qs.hasQuestItems(STABILIZED_ECTOPLASM))
								{
									qs.takeItems(STABILIZED_ECTOPLASM, -1);
									qs.set("talked", "1");
									htmltext = "30832-09.html";
								}
								else
									htmltext = "30832-08.html";
								break;
							}
							case 6:
							case 7:
							case 8:
							{
								htmltext = "30832-15.html";
								break;
							}
							case 9:
							{
								if (qs.getInt("talked") == 1)
								{
									qs.set("talked", "2");
									htmltext = "30832-17.html";
								}
								else if (qs.getInt("talked") == 2)
									htmltext = "30832-18.html";
								else
								{
									qs.takeItems(BOOK_OF_SEAL, -1);
									qs.set("talked", "1");
									htmltext = "30832-16.html";
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
			case ERRICKIN:
			{
				if (qs.isStarted())
				{
					switch (qs.getCond())
					{
						case 1:
						{
							htmltext = "30701-01.html";
							break;
						}
						case 2:
						{
							htmltext = "30701-02.html";
							break;
						}
						case 3:
						{
							htmltext = "30701-04.html";
							break;
						}
						case 4:
						{
							if (qs.getQuestItemsCount(ECTOPLASM) < ECTOPLASM_COUNT)
							{
								qs.giveItems(STABILIZED_ECTOPLASM, 1);
								qs.set("cond", "5");
								htmltext = "30701-06.html";
							}
							else
							{
								qs.takeItems(ECTOPLASM, -1);
								htmltext = "30701-05.html";
							}
							break;
						}
						default:
						{
							htmltext = "30701-07.html";
							break;
						}
					}
				}
				break;
			}
			case CLAYTON:
			{
				if (qs.isStarted())
				{
					switch (qs.getCond())
					{
						case 1:
						case 2:
						case 3:
						case 4:
						case 5:
						{
							htmltext = "30464-01.html";
							break;
						}
						case 6:
						{
							htmltext = "30464-02.html";
							break;
						}
						case 7:
						{
							htmltext = "30464-04.html";
							break;
						}
						case 8:
						{
							qs.giveItems(BOOK_OF_SEAL, 1);
							qs.takeItems(GLASS_JAGUAR_CRYSTAL, -1);
							qs.set("cond", "9");
							htmltext = "30464-05.html";
							break;
						}
						default:
						{
							htmltext = "30464-06.html";
							break;
						}
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	protected void giveItem(QuestState qs, int itemId, int count, int maxCount, int cond)
	{
		qs.giveItems(itemId, count);
		
		if (qs.getQuestItemsCount(itemId) >= maxCount)
		    qs.set("cond", ""+cond);
		else
			qs.playSound(QuestState.SOUND_ITEMGET);
	}
	
	public static void main(String[] args)
	{
		new Q136_MoreThanMeetsTheEye();
	}
}