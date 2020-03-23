package com.l2jhellas.gameserver.network.serverpackets;

public class PledgeShowMemberListDelete extends L2GameServerPacket
{
	private static final String _S__6B_PLEDGESHOWMEMBERLISTDELETE = "[S] 56 PledgeShowMemberListDelete";
	private final String _player;
	
	public PledgeShowMemberListDelete(String playerName)
	{
		_player = playerName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x56);
		writeS(_player);
	}
	
	@Override
	public String getType()
	{
		return _S__6B_PLEDGESHOWMEMBERLISTDELETE;
	}
}