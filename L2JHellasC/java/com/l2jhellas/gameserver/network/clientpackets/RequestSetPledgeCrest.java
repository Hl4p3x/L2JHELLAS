package com.l2jhellas.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.cache.CrestCache;
import com.l2jhellas.gameserver.cache.CrestCache.CrestType;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.util.database.L2DatabaseFactory;

public final class RequestSetPledgeCrest extends L2GameClientPacket
{
	private static final String _C__53_REQUESTSETPLEDGECREST = "[C] 53 RequestSetPledgeCrest";
	static Logger _log = Logger.getLogger(RequestSetPledgeCrest.class.getName());
	
	private int _length;
	private byte[] _data;
	
	@Override
	protected void readImpl()
	{
		_length = readD();
		if (_length < 0 || _length > 256)
			return;
		
		_data = new byte[_length];
		readB(_data);
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final L2Clan clan = activeChar.getClan();
		if (clan == null)
			return;
		
		if (clan.getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_SET_CREST_WHILE_DISSOLUTION_IN_PROGRESS);
			return;
		}
		
		if (_length < 0)
		{
			activeChar.sendMessage("File transfer error.");
			return;
		}
		
		if (_length > 256)
		{
			activeChar.sendMessage("The clan crest file size was too big (max 256 bytes).");
			return;
		}
		
		boolean updated = false;
		int crestId = -1;
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_REGISTER_CREST) == L2Clan.CP_CL_REGISTER_CREST)
		{
			if (_length == 0 || _data.length == 0)
			{
				if (clan.getCrestId() == 0)
					return;
				
				crestId = 0;
				activeChar.sendPacket(SystemMessageId.CLAN_CREST_HAS_BEEN_DELETED);
				updated = true;
			}
			else
			{
				if (clan.getLevel() < 3)
				{
					activeChar.sendPacket(SystemMessageId.CLAN_LVL_3_NEEDED_TO_SET_CREST);
					return;
				}
				
				crestId = IdFactory.getInstance().getNextId();
				if (!CrestCache.saveCrest(CrestType.PLEDGE, crestId, _data))
				{
					_log.warning(RequestSetPledgeCrest.class.getSimpleName() + ": Error saving crest for clan " + clan.getName() + " [" + clan.getClanId() + "]");
					return;
				}
				updated = true;
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET crest_id=? WHERE clan_id=?");
				statement.setInt(1, crestId);
				statement.setInt(2, clan.getClanId());
				statement.executeUpdate();
				statement.close();
			}
			catch (SQLException e)
			{
				_log.warning(RequestSetPledgeCrest.class.getName() + " Could not update the crest id:");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
			
			clan.setCrestId(crestId);
			clan.setHasCrest(true);
			
			if (updated && crestId != -1)
				for (L2PcInstance member : clan.getOnlineMembers())
					member.broadcastUserInfo();
		}
	}
	
	@Override
	public String getType()
	{
		return _C__53_REQUESTSETPLEDGECREST;
	}
}