package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.L2Clan.RankPrivs;

public class PledgePowerGradeList extends L2GameServerPacket
{
	private static final String _S__FE_3B_PLEDGEPOWERGRADELIST = "[S] FE:3B PledgePowerGradeList";
	private final RankPrivs[] _privs;
	
	public PledgePowerGradeList(RankPrivs[] privs)
	{
		_privs = privs;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x3b);
		writeD(_privs.length);
		for (RankPrivs _priv : _privs)
		{
			writeD(_priv.getRank());
			writeD(_priv.getParty());
			if (Config.DEBUG)
				_log.warning(PledgePowerGradeList.class.getName() + ": rank: " + _priv.getRank() + " party: " + _priv.getParty());
		}
		
	}
	
	@Override
	public String getType()
	{
		return _S__FE_3B_PLEDGEPOWERGRADELIST;
	}
}
