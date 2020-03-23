package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class ExStorageMaxCount extends L2GameServerPacket
{
	private static final String _S__FE_2E_EXSTORAGEMAXCOUNT = "[S] FE:2E ExStorageMaxCount";
	private final L2PcInstance _activeChar;
	private final int _inventory;
	private final int _warehouse;
	private final int _freight;
	private final int _privateSell;
	private final int _privateBuy;
	private final int _receipeD;
	private final int _recipe;
	
	public ExStorageMaxCount(L2PcInstance character)
	{
		_activeChar = character;
		_inventory = _activeChar.getInventoryLimit();
		_warehouse = _activeChar.getWareHouseLimit();
		_privateSell = _activeChar.getPrivateSellStoreLimit();
		_privateBuy = _activeChar.getPrivateBuyStoreLimit();
		_freight = _activeChar.getFreightLimit();
		_receipeD = _activeChar.getDwarfRecipeLimit();
		_recipe = _activeChar.getCommonRecipeLimit();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x2e);
		
		writeD(_inventory);
		writeD(_warehouse);
		writeD(_freight);
		writeD(_privateSell);
		writeD(_privateBuy);
		writeD(_receipeD);
		writeD(_recipe);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_2E_EXSTORAGEMAXCOUNT;
	}
}