package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.templates.L2PcTemplate;

public class CharTemplates extends L2GameServerPacket
{
	// dddddddddddddddddddd
	private static final String _S__23_CHARTEMPLATES = "[S] 23 CharTemplates";
	private final List<L2PcTemplate> _chars = new ArrayList<>();
	
	public void addChar(L2PcTemplate template)
	{
		_chars.add(template);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x17);
		writeD(_chars.size());
		
		for (L2PcTemplate temp : _chars)
		{
			writeD(temp.getRace().ordinal());
			writeD(temp.getClassId().getId());
			writeD(0x46);
			writeD(temp.baseSTR);
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.baseDEX);
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.baseCON);
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.baseINT);
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.baseWIT);
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.baseMEN);
			writeD(0x0a);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__23_CHARTEMPLATES;
	}
}