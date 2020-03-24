package com.l2jhellas.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jhellas.gameserver.network.serverpackets.KeyPacket;

public final class ProtocolVersion extends L2GameClientPacket
{
	static Logger _log = Logger.getLogger(ProtocolVersion.class.getName());
	private static final String _C__00_PROTOCOLVERSION = "[C] 00 ProtocolVersion";
	
	private int _version;
	
	@Override
	protected void readImpl()
	{
		if (readD() > 0 && readD() <= 747)
			_version = readD();
	}
	
	@Override
	protected void runImpl()
	{
		switch (_version)
		{
			case 737:
			case 740:
			case 744:
			case 746:
				getClient().sendPacket(new KeyPacket(getClient().enableCrypt()));
				break;			
			default:
				getClient().closeNow();
				break;
		}
	}
	
	@Override
	public String getType()
	{
		return _C__00_PROTOCOLVERSION;
	}
}