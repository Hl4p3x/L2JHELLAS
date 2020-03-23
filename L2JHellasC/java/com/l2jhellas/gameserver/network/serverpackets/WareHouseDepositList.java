package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class WareHouseDepositList extends L2GameServerPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3; // not sure
	public static final int FREIGHT = 4; // not sure
	private static final String _S__53_WAREHOUSEDEPOSITLIST = "[S] 41 WareHouseDepositList";
	private final L2PcInstance _activeChar;
	private final int _playerAdena;
	private final ArrayList<L2ItemInstance> _items;
	private final int _whType;
	
	public WareHouseDepositList(L2PcInstance player, int type)
	{
		_activeChar = player;
		_whType = type;
		_playerAdena = _activeChar.getAdena();
		_items = new ArrayList<>();
		
		if (_activeChar.getActiveTradeList() != null || _activeChar.getActiveEnchantItem() != null)
		{
			_activeChar.sendMessage("You can't use wh while active trade or enchant.");
			_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		for (L2ItemInstance temp : _activeChar.getInventory().getAvailableItems(true))
			_items.add(temp);
		
		// augmented and shadow items can be stored in private wh
		if (_whType == PRIVATE)
		{
			for (L2ItemInstance temp : player.getInventory().getItems())
			{
				if (temp != null && !temp.isEquipped() && (temp.isShadowItem() || temp.isAugmented()))
					_items.add(temp);
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x41);
		writeH(_whType);
		writeD(_playerAdena);
		writeH(_items.size());
		
		for (L2ItemInstance temp : _items)
		{
			if (temp == null || temp.getItem() == null)
				continue;
			
			final L2Item item = temp.getItem();
			
			writeH(item.getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(item.getType2());
			writeH(temp.getCustomType1());
			writeD(item.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeH(0x00);
			writeD(temp.getObjectId());
			if (temp.isAugmented())
			{
				writeD(0x0000FFFF & temp.getAugmentation().getAugmentationId());
				writeD(temp.getAugmentation().getAugmentationId() >> 16);
			}
			else
				writeQ(0x00);
		}
		_items.clear();
	}
	
	@Override
	public String getType()
	{
		return _S__53_WAREHOUSEDEPOSITLIST;
	}
}