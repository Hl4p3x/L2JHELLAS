package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class RequestAutoSoulShot extends L2GameClientPacket
{
	private static final String _C__CF_REQUESTAUTOSOULSHOT = "[C] CF RequestAutoSoulShot";
	
	// format cd
	private int _itemId;
	private int _type; // 1 = on : 0 = off;
	
	@Override
	protected void readImpl()
	{
		_itemId = readD();
		_type = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_AUTO_USE_LACK_OF_S1).addItemName(_itemId));
			return;
		}
		
		if (activeChar.getPrivateStoreType() == StoreType.NONE && activeChar.getActiveRequester() == null && !activeChar.isDead())
		{
			final L2ItemInstance item = activeChar.getInventory().getItemByItemId(_itemId);
			
			if (item != null)
			{
				if (_type == 1)
				{
					// Fishing shots are not automatic on retail
					if (_itemId < 6535 || _itemId > 6540)
					{
						// Attempt to charge first shot on activation
						if (_itemId == 6645 || _itemId == 6646 || _itemId == 6647)
						{
							activeChar.addAutoSoulShot(_itemId);
							activeChar.sendPacket(new ExAutoSoulShot(_itemId, _type));
							
							// start the auto soulshot use
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO).addString(item.getItemName()));
							
							final L2Summon pet = activeChar.getPet();
							
							if(pet != null)
								pet.rechargeShots(true, true);
						}
						else
						{
							if (activeChar.getActiveWeaponItem() != activeChar.getFistsWeaponItem() && item.getItem().getCrystalType() == activeChar.getActiveWeaponItem().getCrystalType())
							{
								if (_itemId >= 3947 && _itemId <= 3952 && activeChar.isInOlympiadMode())
									activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT).addString(item.getItemName()));
								else
								{
									activeChar.addAutoSoulShot(_itemId);
									activeChar.sendPacket(new ExAutoSoulShot(_itemId, _type));
									
									// start the auto soulshot use
									activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO).addString(item.getItemName()));								
									activeChar.rechargeShots(true, true);
								}
							}
							else
							{
								if ((_itemId >= 2509 && _itemId <= 2514) || (_itemId >= 3947 && _itemId <= 3952) || _itemId == 5790)
									activeChar.sendPacket(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH);
								else
									activeChar.sendPacket(SystemMessageId.SOULSHOTS_GRADE_MISMATCH);
							}
						}
					}
				}
				else if (_type == 0)
				{
					// cancel the auto soulshot use
					activeChar.removeAutoSoulShot(_itemId);
					activeChar.sendPacket(new ExAutoSoulShot(_itemId, _type));					
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addString(item.getItemName()));
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__CF_REQUESTAUTOSOULSHOT;
	}
}