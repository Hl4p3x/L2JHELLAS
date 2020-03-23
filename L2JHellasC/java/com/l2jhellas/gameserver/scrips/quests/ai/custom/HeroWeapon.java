package com.l2jhellas.gameserver.scrips.quests.ai.custom;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Util;

public class HeroWeapon extends Quest
{
	private static final int[] WEAPON_IDS =
	{
		6611,
		6612,
		6613,
		6614,
		6615,
		6616,
		6617,
		6618,
		6619,
		6620,
		6621
	};
	
	public HeroWeapon()
	{
		super(-1, "HeroWeapon", "custom");
		
		addStartNpc(31690, 31769, 31770, 31771, 31772, 31773);
		addTalkId(31690, 31769, 31770, 31771, 31772, 31773);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		
		int weaponId = Integer.valueOf(event);
		if (Util.contains(WEAPON_IDS, weaponId))
			st.giveItems(weaponId, 1);
		
		st.exitQuest(true);
		return null;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
			newQuestState(player);
		
		if (st != null)
		{
			if (player.isHero())
			{
				if (hasHeroWeapon(player))
				{
					htmltext = "already_have_weapon.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "weapon_list.htm";
			}
			else
			{
				htmltext = "no_hero.htm";
				st.exitQuest(true);
			}
		}
		
		return htmltext;
	}
	
	private static boolean hasHeroWeapon(L2PcInstance player)
	{
		for (int i : WEAPON_IDS)
		{
			if (player.getInventory().getItemByItemId(i) != null)
				return true;
		}
		
		return false;
	}
	
	public static void main(String[] args)
	{
		new HeroWeapon();
	}
}