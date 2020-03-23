package com.l2jhellas.gameserver.network.serverpackets;

import java.util.Collection;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class WareHouseWithdrawalList extends L2GameServerPacket
{
	private static Logger _log = Logger.getLogger(WareHouseWithdrawalList.class.getName());
	
	private static final String _S__54_WAREHOUSEWITHDRAWALLIST = "[S] 42 WareHouseWithdrawalList";
	
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3; // not sure
	public static final int FREIGHT = 4; // not sure
	
	private L2PcInstance _activeChar;
	private int _playerAdena;
	private Collection<L2ItemInstance> _items;
	private int _whType;
	
	public WareHouseWithdrawalList(L2PcInstance player, int type)
	{
		_activeChar = player;
		_whType = type;
		
		_playerAdena = _activeChar.getAdena();
		if (_activeChar.getActiveWarehouse() == null)
		{
			// Something went wrong!
			_log.warning(WareHouseWithdrawalList.class.getName() + ": error while sending withdraw request to: " + _activeChar.getName());
			return;
		}
		if (_activeChar.getActiveTradeList() != null || _activeChar.getActiveEnchantItem() != null)
		{
			_activeChar.sendMessage("You can't use wh while active trade or enchant.");
			_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		_items = _activeChar.getActiveWarehouse().getItems();
		
		if (Config.DEBUG)
			for (L2ItemInstance item : _items)
				_log.fine("item:" + item.getItem().getItemName() + " type1:" + item.getItem().getType1() + " type2:" + item.getItem().getType2());
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x42);
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
	}
	
	@Override
	public String getType()
	{
		return _S__54_WAREHOUSEWITHDRAWALLIST;
	}
}