package com.l2jhellas.gameserver.network.serverpackets;

import java.util.Collection;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.templates.L2Weapon;

public class GMViewWarehouseWithdrawList extends L2GameServerPacket
{
	private static final String _S__95_GMViewWarehouseWithdrawList = "[S] 95 GMViewWarehouseWithdrawList";
	private final Collection<L2ItemInstance> _items;
	private final String _playerName;
	private final L2PcInstance _activeChar;
	private final int _money;
	
	public GMViewWarehouseWithdrawList(L2PcInstance cha)
	{
		_activeChar = cha;
		_items = _activeChar.getWarehouse().getItems();
		_playerName = _activeChar.getName();
		_money = _activeChar.getAdena();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x95);
		writeS(_playerName);
		writeD(_money);
		writeH(_items.size());
		
		for (L2ItemInstance item : _items)
		{
			writeH(item.getItem().getType1());
			
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			
			switch (item.getItem().getType2())
			{
				case L2Item.TYPE2_WEAPON:
				{
					writeD(item.getItem().getBodyPart());
					writeH(item.getEnchantLevel());
					writeH(((L2Weapon) item.getItem()).getSoulShotCount());
					writeH(((L2Weapon) item.getItem()).getSpiritShotCount());
					break;
				}
				
				case L2Item.TYPE2_SHIELD_ARMOR:
				case L2Item.TYPE2_ACCESSORY:
				case L2Item.TYPE2_PET_WOLF:
				case L2Item.TYPE2_PET_HATCHLING:
				case L2Item.TYPE2_PET_STRIDER:
				case L2Item.TYPE2_PET_BABY:
				{
					writeD(item.getItem().getBodyPart());
					writeH(item.getEnchantLevel());
					writeH(0x00);
					writeH(0x00);
					break;
				}
			}
			
			writeD(item.getObjectId());
			
			switch (item.getItem().getType2())
			{
				case L2Item.TYPE2_WEAPON:
				{
					if (item.isAugmented())
					{
						writeD(0x0000FFFF & item.getAugmentation().getAugmentationId());
						writeD(item.getAugmentation().getAugmentationId() >> 16);
					}
					else
					{
						writeD(0);
						writeD(0);
					}
					
					break;
				}
				
				case L2Item.TYPE2_SHIELD_ARMOR:
				case L2Item.TYPE2_ACCESSORY:
				case L2Item.TYPE2_PET_WOLF:
				case L2Item.TYPE2_PET_HATCHLING:
				case L2Item.TYPE2_PET_STRIDER:
				case L2Item.TYPE2_PET_BABY:
				{
					writeD(0);
					writeD(0);
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _S__95_GMViewWarehouseWithdrawList;
	}
}