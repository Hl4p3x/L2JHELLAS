package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.templates.L2Henna;

public final class HennaInfo extends L2GameServerPacket
{
	private static final String _S__E4_HennaInfo = "[S] E4 HennaInfo";
	
	private final L2PcInstance _activeChar;
	private final List<L2Henna> _hennas = new ArrayList<>();
	
	public HennaInfo(L2PcInstance player)
	{
		_activeChar = player;
		
		for (L2Henna henna : _activeChar.getHennaList())
		{
			if (henna != null)
				_hennas.add(henna);
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe4);
		
		writeC(_activeChar.getHennaStatINT()); // equip INT
		writeC(_activeChar.getHennaStatSTR()); // equip STR
		writeC(_activeChar.getHennaStatCON()); // equip CON
		writeC(_activeChar.getHennaStatMEN()); // equip MEM
		writeC(_activeChar.getHennaStatDEX()); // equip DEX
		writeC(_activeChar.getHennaStatWIT()); // equip WIT
		
		// Henna slots
		int classId = _activeChar.getClassId().level();
		if (classId == 1)
			writeD(2);
		else if (classId > 1)
			writeD(3);
		else
			writeD(0);
		
		writeD(_hennas.size()); // size
		
		for (L2Henna henna : _hennas)
		{
			writeD(henna.getSymbolId());
			writeD(henna.isForThisClass(_activeChar) ? henna.getSymbolId() : 0);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__E4_HennaInfo;
	}
}