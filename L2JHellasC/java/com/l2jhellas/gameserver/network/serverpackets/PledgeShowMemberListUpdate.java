package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2ClanMember;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class PledgeShowMemberListUpdate extends L2GameServerPacket
{
	private static final String _S__54_PLEDGESHOWMEMBERLISTUPDATE = "[S] 54 PledgeShowMemberListUpdate";
	private final int _pledgeType;
	private final int _hasSponsor;
	private final String _name;
	private final int _level;
	private final int _classId;
	private final int _isOnline;
	private final int _race;
	private final int _sex;
	
	public PledgeShowMemberListUpdate(L2PcInstance player)
	{
		_pledgeType = player.getPledgeType();
		_hasSponsor = (player.getSponsor() != 0 || player.getApprentice() != 0) ? 1 : 0;
		_name = player.getName();
		_level = player.getLevel();
		_classId = player.getClassId().getId();
		_race = player.getRace().ordinal();
		_sex = player.getAppearance().getSex().ordinal();
		_isOnline = (player.isbOnline()) ? player.getObjectId() : 0;
	}
	
	public PledgeShowMemberListUpdate(L2ClanMember player)
	{
		_name = player.getName();
		_level = player.getLevel();
		_classId = player.getClassId();
		_isOnline = (player.isOnline()) ? player.getObjectId() : 0;
		_pledgeType = player.getPledgeType();
		_hasSponsor = (player.getSponsor() != 0 || player.getApprentice() != 0) ? 1 : 0;
		
		if (_isOnline != 0)
		{
			_race = player.getPlayerInstance().getRace().ordinal();
			_sex = player.getPlayerInstance().getAppearance().getSex().ordinal();
		}
		else
		{
			_sex = 0;
			_race = 0;
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x54);
		writeS(_name);
		writeD(_level);
		writeD(_classId);
		writeD(_sex);
		writeD(_race);
		writeD(_isOnline);
		writeD(_pledgeType);
		writeD(_hasSponsor);
	}
	
	@Override
	public String getType()
	{
		return _S__54_PLEDGESHOWMEMBERLISTUPDATE;
	}
}