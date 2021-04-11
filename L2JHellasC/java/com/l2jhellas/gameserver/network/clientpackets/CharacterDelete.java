package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.network.serverpackets.CharDeleteFail;
import com.l2jhellas.gameserver.network.serverpackets.CharDeleteOk;
import com.l2jhellas.gameserver.network.serverpackets.CharSelectInfo;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.FloodAction;

public final class CharacterDelete extends L2GameClientPacket
{
	private static final String _C__0C_CHARACTERDELETE = "[C] 0C CharacterDelete";
	
	private int _charSlot;
	
	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (!FloodProtectors.performAction(getClient(), FloodAction.CHARACTER_SELECT))
		{
			sendPacket(new CharDeleteFail(CharDeleteFail.REASON_DELETION_FAILED));
			return;
		}
		
		switch (getClient().markToDeleteChar(_charSlot))
		{
			default:
			case -1:
				break;
			case 0:
				sendPacket(new CharDeleteOk());
				break;
			case 1:
				sendPacket(new CharDeleteFail(CharDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
				break;
			case 2:
				sendPacket(new CharDeleteFail(CharDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
				break;
		}
		final CharSelectInfo csi = new CharSelectInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1, 0);
		sendPacket(csi);
		getClient().setCharSelectSlot(csi.getCharacterSlots());
	}
	
	@Override
	public String getType()
	{
		return _C__0C_CHARACTERDELETE;
	}
}