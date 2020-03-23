package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.datatables.xml.HennaData;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.HennaEquipList;

public final class RequestHennaList extends L2GameClientPacket
{
	private static final String _C__BA_RequestHennaList = "[C] ba RequestHennaList";
	
	// This is just a trigger packet...
	@SuppressWarnings("unused")
	private int _unknown;
	
	@Override
	protected void readImpl()
	{
		_unknown = readD(); // ??
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		activeChar.sendPacket(new HennaEquipList(activeChar, HennaData.getInstance().getAvailableHenna(activeChar.getClassId().getId())));
	}
	
	@Override
	public String getType()
	{
		return _C__BA_RequestHennaList;
	}
}