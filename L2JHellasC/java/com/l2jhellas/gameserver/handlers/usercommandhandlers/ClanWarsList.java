package com.l2jhellas.gameserver.handlers.usercommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.handler.IUserCommandHandler;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class ClanWarsList implements IUserCommandHandler
{
	protected static final Logger _log = Logger.getLogger(ClanWarsList.class.getName());
	
	private static final int[] COMMAND_IDS =
	{
		88,
		89,
		90
	};
	
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0] && id != COMMAND_IDS[1] && id != COMMAND_IDS[2])
			return false;
		
		L2Clan clan = activeChar.getClan();
		
		if (clan == null)
		{
			activeChar.sendMessage("You are not in a clan.");
			return false;
		}
		
		String query = null;
		if (id == 88)
		{
			// Attack List
			activeChar.sendPacket(SystemMessageId.CLANS_YOU_DECLARED_WAR_ON);
			query = "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan1=? AND clan_id=clan2 AND clan2 NOT IN (select clan1 FROM clan_wars WHERE clan2=?)";
		}
		else if (id == 89)
		{
			// Under Attack List
			activeChar.sendPacket(SystemMessageId.CLANS_THAT_HAVE_DECLARED_WAR_ON_YOU);
			query = "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan2=? AND clan_id=clan1 AND clan1 NOT IN (select clan2 FROM clan_wars WHERE clan1=?)";
		}
		else
		// ID = 90
		{
			// War List
			activeChar.sendPacket(SystemMessageId.WAR_LIST);
			query = "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan1=? AND clan_id=clan2 AND clan2 IN (select clan1 FROM clan_wars WHERE clan2=?)";
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(query))
		{
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, clan.getClanId());
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					String clanName = rset.getString("clan_name");
					int ally_id = rset.getInt("ally_id");
					
					if (ally_id > 0)
					{
						// Target With Ally
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S2_ALLIANCE).addString(clanName).addString(rset.getString("ally_name")));
					}
					else
					{
						// Target Without Ally
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_NO_ALLI_EXISTS).addString(clanName));
					}
				}
			}
			activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
		}
		catch (SQLException e)
		{
			_log.warning(ClanWarsList.class.getName() + ": Error cant find DB ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}