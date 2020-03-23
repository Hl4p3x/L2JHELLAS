package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.templates.L2Henna;

public class GMViewHennaInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final L2Henna[] _hennas = new L2Henna[3];
	private int _count;
	
	public GMViewHennaInfo(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		_count = 0;
		
		for (int i = 0; i < 3; i++)
		{
			L2Henna h = _activeChar.getHenna(i + 1);
			if (h != null)
				_hennas[_count++] = h;
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xea);
		
		writeC(_activeChar.getHennaStatINT());
		writeC(_activeChar.getHennaStatSTR());
		writeC(_activeChar.getHennaStatCON());
		writeC(_activeChar.getHennaStatMEN());
		writeC(_activeChar.getHennaStatDEX());
		writeC(_activeChar.getHennaStatWIT());
		
		writeD(3); // slots?
		
		writeD(_count); // size
		for (int i = 0; i < _count; i++)
		{
			writeD(_hennas[i].getSymbolId());
			writeD(_hennas[i].isForThisClass(_activeChar) ? _hennas[i].getSymbolId() : 0);
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 0xea GMHennaInfo";
	}
}