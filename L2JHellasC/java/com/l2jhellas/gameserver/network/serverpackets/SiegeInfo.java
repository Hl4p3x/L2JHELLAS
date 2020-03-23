package com.l2jhellas.gameserver.network.serverpackets;

import java.util.Calendar;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.Castle;

public class SiegeInfo extends L2GameServerPacket
{
	private static final String _S__C9_SIEGEINFO = "[S] c9 SiegeInfo";
	private static Logger _log = Logger.getLogger(SiegeInfo.class.getName());
	private final Castle _castle;
	
	public SiegeInfo(Castle castle)
	{
		_castle = castle;
	}
	
	@Override
	protected final void writeImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		writeC(0xc9);
		writeD(_castle.getCastleId());
		writeD(((_castle.getOwnerId() == activeChar.getClanId()) && (activeChar.isClanLeader())) ? 0x01 : 0x00);
		writeD(_castle.getOwnerId());
		if (_castle.getOwnerId() > 0)
		{
			L2Clan owner = ClanTable.getInstance().getClan(_castle.getOwnerId());
			if (owner != null)
			{
				writeS(owner.getName()); // Clan Name
				writeS(owner.getLeaderName()); // Clan Leader Name
				writeD(owner.getAllyId()); // Ally ID
				writeS(owner.getAllyName()); // Ally Name
			}
			else
				_log.warning(SiegeInfo.class.getName() + ": Null owner for castle: " + _castle.getName());
		}
		else
		{
			writeS("NPC"); // Clan Name
			writeS(""); // Clan Leader Name
			writeD(0); // Ally ID
			writeS(""); // Ally Name
		}
		
		writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000));
		writeD((int) (_castle.getSiege().getSiegeDate().getTimeInMillis() / 1000));
		writeD(0x00); // number of choices?
	}
	
	@Override
	public String getType()
	{
		return _S__C9_SIEGEINFO;
	}
}