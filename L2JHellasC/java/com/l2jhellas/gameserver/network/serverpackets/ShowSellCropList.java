package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class ShowSellCropList extends L2GameServerPacket
{
	private static final String _S__FE_21_SHOWSELLCROPLIST = "[S] FE:21 ShowSellCropList";
	private byte _manorId = 1;
	
	public ShowSellCropList(L2PcInstance player, byte manorId)
	{
		_manorId = manorId;
	}
	
	@Override
	public void runImpl()
	{
		// no long running
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x21);
		writeD(_manorId); // manor id? 1 gludio, 2 dion, 3 giran...
		writeD(1); // size?
		writeD(0); // ?
		writeD(5078); // crop id
		writeD(31); // level ?
		writeC(1); // ???
		writeD(1871); // reward 1 id ?
		writeC(1); // ???
		writeD(4042); // reward 2 id ?
		writeD(_manorId); // territory = manor(castle) id 1 gludio, 2
		// dion, 3
		// giran...
		writeD(3); // remaining
		writeD(10); // buy price
		writeC(1); // reward
		writeD(20); // my crops
	}
	
	@Override
	public String getType()
	{
		return _S__FE_21_SHOWSELLCROPLIST;
	}
}