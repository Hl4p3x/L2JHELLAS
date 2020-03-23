package com.l2jhellas.gameserver.network.clientpackets;

public final class SuperCmdServerStatus extends L2GameClientPacket
{
	private static final String _C__39_02_SUPERCMDSERVERSTATUS = "[C] 39:02 SuperCmdServerStatus";
	
	@Override
	protected void readImpl()
	{
		// trigger packet
	}
	
	@Override
	protected void runImpl()
	{
		
	}
	
	@Override
	public String getType()
	{
		return _C__39_02_SUPERCMDSERVERSTATUS;
	}
}