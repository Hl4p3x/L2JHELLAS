package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.l2jhellas.gameserver.enums.items.ItemLocation;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.CastleManorManager.CropProcure;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class SellListProcure extends L2GameServerPacket
{
	private static final String _S__E9_SELLLISTPROCURE = "[S] E9 SellListProcure";
	
	private final L2PcInstance _activeChar;
	private final int _money;
	private final Map<L2ItemInstance, Integer> _sellList = new HashMap<>();
	private List<CropProcure> _procureList = new ArrayList<>();
	private final int _castle;
	
	public SellListProcure(L2PcInstance player, int castleId)
	{
		_money = player.getAdena();
		_activeChar = player;
		_castle = castleId;
		_procureList = CastleManager.getInstance().getCastleById(_castle).getCropProcure(0);
		for (CropProcure c : _procureList)
		{
			L2ItemInstance item = _activeChar.getInventory().getItemByItemId(c.getId());
			if (item != null && c.getAmount() > 0 && item.getLocation() == ItemLocation.INVENTORY)
			{
				_sellList.put(item, c.getAmount());
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xE9);
		writeD(_money); // money
		writeD(0x00); // lease ?
		writeH(_sellList.size()); // list size
		
		for (L2ItemInstance item : _sellList.keySet())
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(_sellList.get(item));// count
			writeH(item.getItem().getType2());
			writeH(0);// unknown
			writeD(0);// price, u shouldn't get any adena for crops, only raw materials
		}
	}
	
	@Override
	public String getType()
	{
		return _S__E9_SELLLISTPROCURE;
	}
}