package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.GMViewCharacterInfo;
import com.l2jhellas.gameserver.network.serverpackets.GMViewHennaInfo;
import com.l2jhellas.gameserver.network.serverpackets.GMViewItemList;
import com.l2jhellas.gameserver.network.serverpackets.GMViewPledgeInfo;
import com.l2jhellas.gameserver.network.serverpackets.GMViewQuestList;
import com.l2jhellas.gameserver.network.serverpackets.GMViewSkillInfo;
import com.l2jhellas.gameserver.network.serverpackets.GMViewWarehouseWithdrawList;

public final class RequestGMCommand extends L2GameClientPacket
{
	private static final String _C__6E_REQUESTGMCOMMAND = "[C] 6e RequestGMCommand";
	
	private String _targetName;
	private int _command;
	
	@Override
	protected void readImpl()
	{
		_targetName = readS();
		_command = readD();
		// _unknown = readD();
	}
	
	@Override
	protected void runImpl()
	{
		// prevent non gm or low level GMs from viewing player stuff
		if (!getClient().getActiveChar().isGM() || !getClient().getActiveChar().getAccessLevel().allowAltG())
			return;
		
		L2PcInstance player = L2World.getInstance().getPlayer(_targetName);
		
		// player name was incorrect?
		if (player == null)
			return;
		
		switch (_command)
		{
			case 1: // player status
			{
				sendPacket(new GMViewCharacterInfo(player));
				sendPacket(new GMViewHennaInfo(player));
				break;
			}
			case 2: // player clan
			{
				if (player.getClan() != null)
					sendPacket(new GMViewPledgeInfo(player.getClan(), player));
				break;
			}
			case 3: // player skills
			{
				sendPacket(new GMViewSkillInfo(player));
				break;
			}
			case 4: // player quests
			{
				sendPacket(new GMViewQuestList(player));
				break;
			}
			case 5: // player inventory
			{
				sendPacket(new GMViewItemList(player));
				sendPacket(new GMViewHennaInfo(player));
				break;
			}
			case 6: // player warehouse
			{
				// gm warehouse view to be implemented
				sendPacket(new GMViewWarehouseWithdrawList(player));
				break;
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__6E_REQUESTGMCOMMAND;
	}
}