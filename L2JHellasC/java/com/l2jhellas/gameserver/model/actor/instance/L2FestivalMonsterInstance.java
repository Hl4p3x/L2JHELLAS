package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.SevenSignsFestival;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2FestivalMonsterInstance extends L2MonsterInstance
{
	protected int _bonusMultiplier = 1;
	
	public L2FestivalMonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	public void setOfferingBonus(int bonusMultiplier)
	{
		_bonusMultiplier = bonusMultiplier;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker instanceof L2FestivalMonsterInstance)
			return false;
		
		return true;
	}
	
	@Override
	public boolean isAggressive()
	{
		return true;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	@Override
	public void doItemDrop(L2Character lastAttacker)
	{
		L2PcInstance killingChar = null;
		
		if (!(lastAttacker instanceof L2PcInstance))
			return;
		
		killingChar = (L2PcInstance) lastAttacker;
		L2Party associatedParty = killingChar.getParty();
		
		if (associatedParty == null)
			return;
		
		L2PcInstance partyLeader = associatedParty.getPartyMembers().get(0);
		L2ItemInstance addedOfferings = partyLeader.getInventory().addItem("Sign", SevenSignsFestival.FESTIVAL_OFFERING_ID, _bonusMultiplier, partyLeader, this);
		
		InventoryUpdate iu = new InventoryUpdate();
		
		if (addedOfferings.getCount() != _bonusMultiplier)
			iu.addModifiedItem(addedOfferings);
		else
			iu.addNewItem(addedOfferings);
		
		partyLeader.sendPacket(iu);
		
		super.doItemDrop(lastAttacker); // Normal drop
	}
}