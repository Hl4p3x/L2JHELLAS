package com.l2jhellas.gameserver.network.serverpackets;

public class TutorialCloseHtml extends L2GameServerPacket
{
	public static final TutorialCloseHtml STATIC_PACKET = new TutorialCloseHtml();
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xa3);
	}
	
	@Override
	public String getType()
	{
		return "[S] A7 TutorialCloseHtml";
	}
}