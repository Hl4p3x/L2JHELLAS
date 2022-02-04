package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2ClanMember;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class PledgeShowMemberListAdd extends L2GameServerPacket
{
	private static final String _S__55_PLEDGESHOWMEMBERLISTADD = "[S] 55 PledgeShowMemberListAdd";
	private final String _name;
	private final int _lvl;
	private final int _classId;
	private final int _isOnline;
	private final int _pledgeType;
	private final int _race;
	private final int _sex;
	
	public PledgeShowMemberListAdd(L2PcInstance player)
	{
		_name = player.getName();
		_lvl = player.getLevel();
		_classId = player.getClassId().getId();
		_isOnline = (player.isOnline() ? player.getObjectId() : 0);
		_pledgeType = player.getPledgeType();
		_race = player.getRace().ordinal();
		_sex = player.getAppearance().getSex().ordinal();
	}
	
	public PledgeShowMemberListAdd(L2ClanMember cm)
	{
		_name = cm.getName();
		_lvl = cm.getLevel();
		_classId = cm.getClassId();
		_isOnline = (cm.isOnline() ? cm.getObjectId() : 0);
		_pledgeType = cm.getPledgeType();
		_race = cm.getPlayerInstance().getRace().ordinal();
		_sex = cm.getPlayerInstance().getAppearance().getSex().ordinal();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x55);
		writeS(_name);
		writeD(_lvl);
		writeD(_classId);
		writeD(_sex);
		writeD(_race);
		writeD(_isOnline);
		writeD(_pledgeType);
	}
	
	@Override
	public String getType()
	{
		return _S__55_PLEDGESHOWMEMBERLISTADD;
	}
}