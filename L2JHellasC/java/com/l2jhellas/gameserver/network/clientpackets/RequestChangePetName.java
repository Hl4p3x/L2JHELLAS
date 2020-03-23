package com.l2jhellas.gameserver.network.clientpackets;


import com.l2jhellas.gameserver.datatables.sql.PetNameTable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.PetInfo;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class RequestChangePetName extends L2GameClientPacket
{
	private static final String REQUESTCHANGEPETNAME__C__89 = "[C] 89 RequestChangePetName";
	
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		L2Character activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		final L2Summon pet = activeChar.getPet();
		if (pet == null)
			return;
		
		if (pet.getName() != null)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NAMING_YOU_CANNOT_SET_NAME_OF_THE_PET));
			return;
		}
		else if (PetNameTable.getInstance().doesPetNameExist(_name, pet.getTemplate().npcId))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NAMING_ALREADY_IN_USE_BY_ANOTHER_PET));
			return;
		}
		else if ((_name.length() < 3) || (_name.length() > 16))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
			sm.addString("Your pet's name can be up to 16 characters.");
			activeChar.sendPacket(sm);
			sm = null;
			
			return;
		}
		else if (!PetNameTable.getInstance().isValidPetName(_name))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NAMING_PETNAME_CONTAINS_INVALID_CHARS));
			return;
		}
		
		pet.setName(_name);
		pet.getOwner().sendPacket(new PetInfo(pet, 2));
		pet.updateAndBroadcastStatus(1);
		// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
		pet.updateEffectIcons(true);
		
		// set the flag on the control item to say that the pet has a name
		if (pet instanceof L2PetInstance)
		{
			L2ItemInstance controlItem = pet.getOwner().getInventory().getItemByObjectId(pet.getControlItemId());
			if (controlItem != null)
			{
				controlItem.setCustomType2(1);
				controlItem.updateDatabase();
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(controlItem);
				activeChar.sendPacket(iu);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return REQUESTCHANGEPETNAME__C__89;
	}
}