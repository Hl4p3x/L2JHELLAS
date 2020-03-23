package com.l2jhellas.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.cache.CrestCache;
import com.l2jhellas.gameserver.cache.CrestCache.CrestType;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.util.database.L2DatabaseFactory;

public final class RequestSetAllyCrest extends L2GameClientPacket
{
	static Logger _log = Logger.getLogger(RequestSetAllyCrest.class.getName());
	private static final String _C__87_REQUESTSETALLYCREST = "[C] 87 RequestSetAllyCrest";
	
	private int _length;
	private byte[] _data;
	
	@Override
	protected void readImpl()
	{
		_length = readD();
		if (_length < 0 || _length > 192)
			return;
		
		_data = new byte[_length];
		readB(_data);
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (_length < 0)
		{
			activeChar.sendMessage("File transfer error.");
			return;
		}
		if (_length > 192)
		{
			activeChar.sendMessage("The crest file size was too big (max 192 bytes).");
			return;
		}
		
		if (activeChar.getAllyId() != 0)
		{
			L2Clan leaderclan = ClanTable.getInstance().getClan(activeChar.getAllyId());
			
			if (activeChar.getClanId() != leaderclan.getClanId() || !activeChar.isClanLeader())
				return;
			
			boolean remove = false;
			if (_length == 0 || _data.length == 0)
				remove = true;
			
			int newId = 0;
			if (!remove)
				newId = IdFactory.getInstance().getNextId();
			
			if (!remove && !CrestCache.saveCrest(CrestType.ALLY, newId, _data))
			{
				_log.warning(RequestSetAllyCrest.class.getSimpleName() + ": Error saving crest for ally " + leaderclan.getAllyName() + " [" + leaderclan.getAllyId() + "]");
				return;
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET ally_crest_id=? WHERE ally_id=?");
				statement.setInt(1, newId);
				statement.setInt(2, leaderclan.getAllyId());
				statement.executeUpdate();
				statement.close();
			}
			catch (SQLException e)
			{
				_log.warning(RequestSetAllyCrest.class.getName() + " Could not update the ally crest id:" + e.getMessage());
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
			
			for (L2Clan clan : ClanTable.getInstance().getClans())
				if (clan.getAllyId() == activeChar.getAllyId())
				{
					clan.setAllyCrestId(newId);
					for (L2PcInstance member : clan.getOnlineMembers())
						member.broadcastUserInfo();
				}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__87_REQUESTSETALLYCREST;
	}
}