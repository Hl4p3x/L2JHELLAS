package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.datatables.xml.LevelingInfo;
import com.l2jhellas.gameserver.datatables.xml.SoulCrystalData;
import com.l2jhellas.gameserver.datatables.xml.SoulCrystalsTable;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Attackable.AbsorberInfo;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.util.Rnd;

public class Q350_EnhanceYourWeapon extends Quest
{
	private static final String qn = "Q350_EnhanceYourWeapon";
	
	public Q350_EnhanceYourWeapon()
	{
		super(350, qn, "Enhance Your Weapon");
		
		addStartNpc(30115, 30194, 30856);
		addTalkId(30115, 30194, 30856);
		
		for (int npcId : SoulCrystalsTable.getNpcInfos().keySet())
			addKillId(npcId);
		
		for (int crystalId : SoulCrystalsTable.getSoulCrystalInfos().keySet())
			addItemUse(crystalId);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// Start the quest.
		if (event.endsWith("-04.htm"))
		{
			st.setState(Quest.STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		// Give Red Soul Crystal.
		else if (event.endsWith("-09.htm"))
		{
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(4629, 1);
		}
		// Give Green Soul Crystal.
		else if (event.endsWith("-10.htm"))
		{
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(4640, 1);
		}
		// Give Blue Soul Crystal.
		else if (event.endsWith("-11.htm"))
		{
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(4651, 1);
		}
		// Terminate the quest.
		else if (event.endsWith("-exit.htm"))
			st.exitQuest(true);
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				if (player.getLevel() < 40)
					htmltext = npc.getNpcId() + "-lvl.htm";
				else
					htmltext = npc.getNpcId() + "-01.htm";
				break;
			
			case STATE_STARTED:
				// Check inventory for soul crystals.
				for (L2ItemInstance item : player.getInventory().getItems())
				{
					// Crystal found, show "how to" html.
					if (SoulCrystalsTable.getSoulCrystalInfos().get(item.getItemId()) != null)
						return npc.getNpcId() + "-03.htm";
				}
				// No crystal found, offer a new crystal.
				htmltext = npc.getNpcId() + "-21.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onItemUse(L2ItemInstance item, L2PcInstance user, L2Object target)
	{
		// Caster is dead.
		if (user.isDead())
			return null;
		
		// No target, or target isn't an L2Attackable.
		if (target == null || !(target instanceof L2Attackable))
			return null;
		
		final L2Attackable mob = ((L2Attackable) target);
		
		// Mob is dead or not registered in _npcInfos.
		if (mob.isDead() || !SoulCrystalsTable.getNpcInfos().containsKey(mob.getNpcId()))
			return null;
		
		// Add user to mob's absorber list.
		mob.addAbsorber(user);
		
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		// Retrieve individual mob informations.
		final LevelingInfo npcInfo = SoulCrystalsTable.getNpcInfos().get(npc.getNpcId());
		if (npcInfo == null)
			return null;
		
		// Handle npc leveling info type.
		switch (npcInfo.getAbsorbCrystalType())
		{
			case FULL_PARTY:
				final L2Attackable mob = (L2Attackable) npc;
				final int chance = Rnd.get(100);
				
				for (L2PcInstance player : getPartyMembersState(killer, npc, Quest.STATE_STARTED))
					tryToStageCrystal(player, mob, npcInfo, chance);
				break;
			
			case PARTY_ONE_RANDOM:
				final L2PcInstance player = getRandomPartyMemberState(killer, npc, Quest.STATE_STARTED);
				if (player != null)
					tryToStageCrystal(player, (L2Attackable) npc, npcInfo, Rnd.get(100));
				break;
			
			case LAST_HIT:
				if (checkPlayerState(killer, npc, Quest.STATE_STARTED) != null)
					tryToStageCrystal(killer, (L2Attackable) npc, npcInfo, Rnd.get(100));
				break;
		}
		
		return null;
	}
	
	@SuppressWarnings("static-access")
	private static void tryToStageCrystal(L2PcInstance player, L2Attackable mob, LevelingInfo npcInfo, int chance)
	{
		SoulCrystalData crystalData = null;
		L2ItemInstance crystalItem = null;
		
		// Iterate through player's inventory to find crystal(s).
		for (L2ItemInstance item : player.getInventory().getItems())
		{
			SoulCrystalData data = SoulCrystalsTable.getSoulCrystalInfos().get(item.getItemId());
			if (data == null)
				continue;
			
			// More crystals found.
			if (crystalData != null)
			{
				// Leveling requires soul crystal being used?
				if (npcInfo.skillRequired())
				{
					// Absorb list contains killer and his AbsorbInfo is registered.
					final AbsorberInfo ai = mob.getAbsorbersList().get(player.getObjectId());
					
					if (ai != null && ai.isRegistered())
						player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION);
				}
				else
					player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION);
				
				return;
			}
			
			crystalData = data;
			crystalItem = item;
		}
		
		// No crystal found, return without any notification.
		if (crystalData == null || crystalItem == null)
			return;
		
		// Leveling requires soul crystal being used?
		if (npcInfo.skillRequired())
		{
			// Absorb list doesn't contain killer or his AbsorbInfo is not registered.
			final AbsorberInfo ai = mob.getAbsorbersList().get(player.getObjectId());
			if (ai == null || !ai.isRegistered())
				return;
			
			// Check if Absorb list contains valid crystal and whether it was used properly.
			if (!ai.isValid(crystalItem.getObjectId()))
			{
				player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
				return;
			}
		}
		
		// Check, if npc stages this type of crystal.
		if (!npcInfo.isInLevelList(crystalData.getLevel()))
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
			return;
		}
		
		// Check level difference limitation, dark blue monsters does not stage.
		if (player.getLevel() - mob.getLevel() > 8)
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
			return;
		}
		
		// Lucky, crystal successfully stages.
		if (chance < npcInfo.getChanceStage())
			exchangeCrystal(player, crystalData, true);
		// Bad luck, crystal accidentally breaks.
		else if (chance < (npcInfo.getChanceStage() + npcInfo.getChanceBreak()))
			exchangeCrystal(player, crystalData, false);
		// Bad luck, crystal doesn't stage.
		else
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED);
	}
	
	private static void exchangeCrystal(L2PcInstance player, SoulCrystalData scd, boolean stage)
	{
		QuestState st = player.getQuestState(qn);
		
		st.takeItems(scd.getCrystalItemId(), 1);
		if (stage)
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_SUCCEEDED);
			st.giveItems(scd.getStagedItemId(), 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		else
		{
			int broken = scd.getBrokenItemId();
			if (broken != 0)
			{
				player.sendPacket(SystemMessageId.SOUL_CRYSTAL_BROKE);
				st.giveItems(broken, 1);
			}
		}
	}
	
	public static void main(String[] args)
	{
		new Q350_EnhanceYourWeapon();
	}
}