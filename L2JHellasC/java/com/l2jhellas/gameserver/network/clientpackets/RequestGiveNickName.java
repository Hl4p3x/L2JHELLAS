package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2ClanMember;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class RequestGiveNickName extends L2GameClientPacket
{
	private static final String _C__55_REQUESTGIVENICKNAME = "[C] 55 RequestGiveNickName";
	
	private String _target;
	private String _title;
	
	@Override
	protected void readImpl()
	{
		_target = readS();
		_title = readS();
	}
	
	@Override
	protected void runImpl()
	{
		
		if (_title == null || _target == null)
			return;
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		if (activeChar.gotInvalidTitle(_title) || _title.length() > 16)
		{
			activeChar.sendMessage("you are not allowed to do that,please enter a valid title!");
			return;
		}
		
		if (activeChar.isinZodiac)
		{
			activeChar.sendMessage("Can't change your title while in a Zodiac Event");
			return;
		}
		
		// Noblesse can bestow a title to themselves
		if (activeChar.isNoble() && _target.matches(activeChar.getName()))
		{
			activeChar.setTitle(_title);
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.TITLE_CHANGED);
			activeChar.sendPacket(sm);
			activeChar.broadcastTitleInfo();
		}
		// Can the player change/give a title?
		else if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_GIVE_TITLE) == L2Clan.CP_CL_GIVE_TITLE)
		{
			if (activeChar.getClan().getLevel() < 3)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_LVL_3_NEEDED_TO_ENDOWE_TITLE);
				activeChar.sendPacket(sm);
				sm = null;
				return;
			}
			
			L2ClanMember member1 = activeChar.getClan().getClanMember(_target);
			if (member1 != null)
			{
				L2PcInstance member = member1.getPlayerInstance();
				if (member != null)
				{
					// is target from the same clan?
					member.setTitle(_title);
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.TITLE_CHANGED);
					member.sendPacket(sm);
					member.broadcastTitleInfo();
					sm = null;
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
					sm.addString("Target needs to be online to get a title.");
					activeChar.sendPacket(sm);
					sm = null;
				}
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
				sm.addString("Target does not belong to your clan.");
				activeChar.sendPacket(sm);
				sm = null;
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__55_REQUESTGIVENICKNAME;
	}
}