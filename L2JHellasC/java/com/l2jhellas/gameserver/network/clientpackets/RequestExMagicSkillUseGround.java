package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.geometry.Point3D;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.ValidateLocation;
import com.l2jhellas.util.Util;

public final class RequestExMagicSkillUseGround extends L2GameClientPacket
{
	private static final String _C__D0_2F_REQUESTEXMAGICSKILLUSEGROUND = "[C] D0:2F RequestExMagicSkillUseGround";
	
	private int _x;
	private int _y;
	private int _z;
	private int _skillId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		_skillId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}
	
	@Override
	protected void runImpl()
	{
		// Get the current L2PcInstance of the player
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		// Get the level of the used skill
		int level = activeChar.getSkillLevel(_skillId);
		if (level <= 0)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Get the L2Skill template corresponding to the skillID received from the client
		final L2Skill skill = activeChar.getSkill(_skillId);

		if (skill == null)
			return;
				
		activeChar.setCurrentSkillWorldPosition(new Point3D(_x, _y, _z));	
		activeChar.setHeading(Util.calculateHeadingFrom(activeChar.getX(), activeChar.getY(), _x, _y));
		activeChar.broadcastPacket(new ValidateLocation(activeChar));
		activeChar.useMagic(skill, _ctrlPressed, _shiftPressed);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_2F_REQUESTEXMAGICSKILLUSEGROUND;
	}
}