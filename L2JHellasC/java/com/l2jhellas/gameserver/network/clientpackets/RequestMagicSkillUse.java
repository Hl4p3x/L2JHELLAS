package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class RequestMagicSkillUse extends L2GameClientPacket
{
	private static final String _C__2F_REQUESTMAGICSKILLUSE = "[C] 2F RequestMagicSkillUse";
	
	private int _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	@Override
	protected void readImpl()
	{
		_magicId = readD(); // Identifier of the used skill
		_ctrlPressed = readD() != 0; // True if it's a ForceAttack : Ctrl pressed
		_shiftPressed = readC() != 0; // True if Shift pressed
	}
	
	@Override
	protected void runImpl()
	{
		// Get the current L2PcInstance of the player
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null || activeChar.isOnline() == 0)
			return;

		activeChar.ReqMagicSkillUse(_magicId, _ctrlPressed, _shiftPressed);
	}
	
	@Override
	public String getType()
	{
		return _C__2F_REQUESTMAGICSKILLUSE;
	}
}