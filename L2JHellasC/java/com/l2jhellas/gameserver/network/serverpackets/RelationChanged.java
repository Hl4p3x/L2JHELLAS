package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SummonInstance;

public class RelationChanged extends L2GameServerPacket
{
	public static final int RELATION_PVP_FLAG = 0x00002; // pvp ???
	public static final int RELATION_HAS_KARMA = 0x00004; // karma ???
	public static final int RELATION_LEADER = 0x00080; // leader
	public static final int RELATION_INSIEGE = 0x00200; // true if in siege
	public static final int RELATION_ATTACKER = 0x00400; // true when attacker
	public static final int RELATION_ALLY = 0x00800; // blue siege icon, cannot have if red
	public static final int RELATION_ENEMY = 0x01000; // true when red icon, doesn't matter with blue
	public static final int RELATION_MUTUAL_WAR = 0x08000; // double fist
	public static final int RELATION_1SIDED_WAR = 0x10000; // single fist
	
	private static final String _S__CE_RELATIONCHANGED = "[S] CE RelationChanged";
	
	private final int _objId, _relation, _autoAttackable;
	private int _karma;
	private int _pvpFlag;
	
	public RelationChanged(L2Playable activeChar, int relation, boolean autoattackable)
	{
		_objId = activeChar.getObjectId();
		_relation = relation;
		_autoAttackable = autoattackable ? 1 : 0;
		
		if (activeChar instanceof L2PcInstance)
		{
			_karma = ((L2PcInstance) activeChar).getKarma();
			_pvpFlag = ((L2PcInstance) activeChar).getPvpFlag();
		}
		else if (activeChar instanceof L2SummonInstance)
		{
			_karma = ((L2SummonInstance) activeChar).getOwner().getKarma();
			_pvpFlag = ((L2SummonInstance) activeChar).getOwner().getPvpFlag();
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xce);
		writeD(_objId);
		writeD(_relation);
		writeD(_autoAttackable);
		writeD(_karma);
		writeD(_pvpFlag);
	}
	
	@Override
	public String getType()
	{
		return _S__CE_RELATIONCHANGED;
	}
}