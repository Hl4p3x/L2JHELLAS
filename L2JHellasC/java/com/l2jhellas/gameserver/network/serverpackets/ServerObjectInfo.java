package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;

public final class ServerObjectInfo extends L2GameServerPacket
{
	private final L2Npc _activeChar;
	private final int _x, _y, _z, _heading;
	private final int _idTemplate;
	private final boolean _isAttackable;
	private final double _collisionHeight, _collisionRadius;
	private final String _name;
	
	public ServerObjectInfo(L2Npc activeChar, L2Character actor)
	{
		_activeChar = activeChar;
		_idTemplate = _activeChar.getTemplate().idTemplate;
		_isAttackable = _activeChar.isAutoAttackable(actor);
		_collisionHeight = _activeChar.getCollisionHeight();
		_collisionRadius = _activeChar.getCollisionRadius();
		_x = _activeChar.getX();
		_y = _activeChar.getY();
		_z = _activeChar.getZ();
		_heading = _activeChar.getHeading();
		_name = _activeChar.getTemplate().isCustom() ? _activeChar.getTemplate().getName() : "";
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x8C);
		writeD(_activeChar.getObjectId());
		writeD(_idTemplate + 1000000);
		writeS(_name); // name
		writeD(_isAttackable ? 1 : 0);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeF(1.0); // movement multiplier
		writeF(1.0); // attack speed multiplier
		writeF(_collisionRadius);
		writeF(_collisionHeight);
		writeD((int) (_isAttackable ? _activeChar.getCurrentHp() : 0));
		writeD(_isAttackable ? _activeChar.getMaxHp() : 0);
		writeD(0x01); // object type
		writeD(0x00); // special effects
	}
}