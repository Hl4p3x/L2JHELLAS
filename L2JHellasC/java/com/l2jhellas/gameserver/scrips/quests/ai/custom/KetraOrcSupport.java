package com.l2jhellas.gameserver.scrips.quests.ai.custom;

import com.l2jhellas.gameserver.enums.skills.FrequentSkill;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.PcInventory;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.WareHouseWithdrawalList;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.util.Util;

public class KetraOrcSupport extends Quest
{
	private static final String qn = "KetraOrcSupport";
	
	private static final int KADUN = 31370; // Hierarch
	private static final int WAHKAN = 31371; // Messenger
	private static final int ASEFA = 31372; // Soul Guide
	private static final int ATAN = 31373; // Grocer
	private static final int JAFF = 31374; // Warehouse Keeper
	private static final int JUMARA = 31375; // Trader
	private static final int KURFA = 31376; // Gate Keeper
	
	private static final int HORN = 7186;
	
	private static final int[] KETRAS =
	{
		21324,
		21325,
		21327,
		21328,
		21329,
		21331,
		21332,
		21334,
		21335,
		21336,
		21338,
		21339,
		21340,
		21342,
		21343,
		21344,
		21345,
		21346,
		21347,
		21348,
		21349
	};
	
	private static final int[][] BUFF =
	{
			{4359, 1, 2}, // Focus: Requires 2 Buffalo Horns
			{4360, 1, 2}, // Death Whisper: Requires 2 Buffalo Horns
			{4345, 1, 3}, // Might: Requires 3 Buffalo Horns
			{4355, 1, 3}, // Acumen: Requires 3 Buffalo Horns
			{4352, 1, 3}, // Berserker: Requires 3 Buffalo Horns
			{4354, 1, 3}, // Vampiric Rage: Requires 3 Buffalo Horns
			{4356, 1, 6}, // Empower: Requires 6 Buffalo Horns
			{4357, 1, 6}// Haste: Requires 6 Buffalo Horns
	};
	
	private static final String[] ketraMissions =
	{
		"Q605_AllianceWithKetraOrcs",
		"Q606_WarWithVarkaSilenos",
		"Q607_ProveYourCourage",
		"Q608_SlayTheEnemyCommander",
		"Q609_MagicalPowerOfWater_Part1",
		"Q610_MagicalPowerOfWater_Part2"
	};
	
	public KetraOrcSupport()
	{
		super(-1, qn, "custom");
		
		addFirstTalkId(KADUN, WAHKAN, ASEFA, ATAN, JAFF, JUMARA, KURFA);
		addTalkId(ASEFA, JAFF, KURFA);
		addStartNpc(JAFF, KURFA);
		
		// Verify if the killer didn't kill an allied mob. Test his party aswell.
		addKillId(KETRAS);
		
		// Verify if an allied is healing/buff an enemy. Petrify him if it's the case.
		addSkillSeeId(KETRAS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;
		
		if (Util.isDigit(event))
		{
			final int eventId = Integer.parseInt(event);
			if (eventId >= 0 && eventId <= 7)
			{
				if (st.getQuestItemsCount(HORN) >= BUFF[eventId - 1][2])
				{
					st.takeItems(HORN, BUFF[eventId - 1][2]);
					npc.setTarget(player);
					npc.doCast(SkillTable.getInstance().getInfo(BUFF[eventId - 1][0], BUFF[eventId - 1][1]));
					npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
					htmltext = "31372-4.htm";
				}
			}
		}
		else if (event.equals("Withdraw"))
		{
			if (player.getWarehouse().getSize() == 0)
				htmltext = "31374-0.htm";
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				player.setActiveWarehouse(player.getWarehouse());
				player.sendPacket(new WareHouseWithdrawalList(player, 1));
			}
		}
		else if (event.equals("Teleport"))
		{
			switch (player.getAllianceWithVarkaKetra())
			{
				case 4:
					htmltext = "31376-4.htm";
					break;
				case 5:
					htmltext = "31376-5.htm";
					break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		
		final int allianceLevel = player.getAllianceWithVarkaKetra();
		
		switch (npc.getNpcId())
		{
			case KADUN:
				if (allianceLevel > 0)
					htmltext = "31370-friend.htm";
				else
					htmltext = "31370-no.htm";
				break;
			
			case WAHKAN:
				if (allianceLevel > 0)
					htmltext = "31371-friend.htm";
				else
					htmltext = "31371-no.htm";
				break;			
			case ASEFA:
				st.setState(STATE_STARTED);
				if (allianceLevel < 1)
					htmltext = "31372-3.htm";
				else if (allianceLevel < 3 && allianceLevel > 0)
					htmltext = "31372-1.htm";
				else if (allianceLevel > 2)
				{
					if (st.hasQuestItems(HORN))
						htmltext = "31372-4.htm";
					else
						htmltext = "31372-2.htm";
				}
				break;
			
			case ATAN:
				if (player.getKarma() >= 1)
					htmltext = "31373-pk.htm";
				else if (allianceLevel <= 0)
					htmltext = "31373-no.htm";
				else if (allianceLevel == 1 || allianceLevel == 2)
					htmltext = "31373-1.htm";
				else
					htmltext = "31373-2.htm";
				break;
			
			case JAFF:
				switch (allianceLevel)
				{
					case 1:
						htmltext = "31374-1.htm";
						break;
					case 2:
					case 3:
						htmltext = "31374-2.htm";
						break;
					default:
						if (allianceLevel <= 0)
							htmltext = "31374-no.htm";
						else if (player.getWarehouse().getSize() == 0)
							htmltext = "31374-3.htm";
						else
							htmltext = "31374-4.htm";
						break;
				}
				break;
			
			case JUMARA:
				switch (allianceLevel)
				{
					case 2:
						htmltext = "31375-1.htm";
						break;
					case 3:
					case 4:
						htmltext = "31375-2.htm";
						break;
					case 5:
						htmltext = "31375-3.htm";
						break;
					default:
						htmltext = "31375-no.htm";
						break;
				}
				break;
			
			case KURFA:
				if (allianceLevel <= 0)
					htmltext = "31376-no.htm";
				else if (allianceLevel > 0 && allianceLevel < 4)
					htmltext = "31376-1.htm";
				else if (allianceLevel == 4)
					htmltext = "31376-2.htm";
				else
					htmltext = "31376-3.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (player.isInParty())
		{
			for (L2PcInstance partyMember : player.getParty().getPartyMembers())
			{
				if (partyMember != null)
					testKetraDemote(partyMember);
			}
		}
		else
			testKetraDemote(player);
		
		return null;
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		// Caster is an allied.
		if (caster.isAlliedWithKetra())
		{
			// Caster's skill is a positive effect ? Go further.
			switch (skill.getSkillType())
			{
				case BUFF:
				case HEAL:
				case HEAL_PERCENT:
				case HEAL_STATIC:
				case BALANCE_LIFE:
				case HOT:
					for (L2Character target : (L2Character[]) targets)
					{
						// Character isn't existing, is dead or is current caster, we drop check.
						if (target == null || target.isDead() || target == caster)
							continue;
						
						// Target isn't a summon nor a player, we drop check.
						if (!(target instanceof L2Playable))
							continue;
						
						// Retrieve the player behind that target.
						final L2PcInstance player = target.getActingPlayer();
						
						// If player is neutral or enemy, go further.
						if (!(player.isAlliedWithKetra()))
						{
							// If the NPC got that player registered in aggro list, go further.
							if (((L2Attackable) npc).containsTarget(player))
							{
								// Save current target for future use.
								final L2Object oldTarget = npc.getTarget();
								
								// Curse the heretic or his pet.
								npc.setTarget((isPet && player.getPet() != null) ? caster.getPet() : caster);
								npc.doCast(FrequentSkill.VARKA_KETRA_PETRIFICATION.getSkill());

								// Revert to old target && drop the loop.
								npc.setTarget(oldTarget);
								break;
							}
						}
					}
					break;
				default:
					break;
			}
		}
		
		// Continue normal behavior.
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	private static void testKetraDemote(L2PcInstance player)
	{
		if (player.isAlliedWithKetra())
		{
			// Drop the alliance (old friends become aggro).
			player.setAllianceWithVarkaKetra(0);
			
			final PcInventory inventory = player.getInventory();
			
			// Drop by 1 the level of that alliance (symbolized by a quest item).
			for (int i = 7215; i >= 7211; i--)
			{
				L2ItemInstance item = inventory.getItemByItemId(i);
				if (item != null)
				{
					// Destroy the badge.
					player.destroyItemByItemId("Quest", i, item.getCount(), player, true);
					
					// Badge lvl 1 ; no addition of badge of lower level.
					if (i != 7211)
						player.addItem("Quest", i - 1, 1, player, true);
					
					break;
				}
			}
			
			for (String mission : ketraMissions)
			{
				QuestState pst = player.getQuestState(mission);
				if (pst != null)
					pst.exitQuest(true);
			}
		}
	}
	
	public static void main(String[] args)
	{
		new KetraOrcSupport();
	}
}