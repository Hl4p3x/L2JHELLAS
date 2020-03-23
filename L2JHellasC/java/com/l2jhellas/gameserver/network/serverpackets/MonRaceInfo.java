package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.L2Npc;

public class MonRaceInfo extends L2GameServerPacket
{
	private static final String _S__DD_MonRaceInfo = "[S] dd MonRaceInfo";
	private final int _unknown1;
	private final int _unknown2;
	private final L2Npc[] _monsters;
	private final int[][] _speeds;
	
	public MonRaceInfo(int unknown1, int unknown2, L2Npc[] monsters, int[][] speeds)
	{
		
		_unknown1 = unknown1;
		_unknown2 = unknown2;
		_monsters = monsters;
		_speeds = speeds;
	}
	
	// 0xf3;;EtcStatusUpdatePacket;ddddd
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xdd);
		
		writeD(_unknown1);
		writeD(_unknown2);
		writeD(8);
		
		for (int i = 0; i < 8; i++)
		{
			if (Config.DEBUG)
				_log.config(MonRaceInfo.class.getName() + ": MOnster " + (i + 1) + " npcid " + _monsters[i].getTemplate().getNpcId());
			writeD(_monsters[i].getObjectId()); // npcObjectID
			writeD(_monsters[i].getTemplate().npcId + 1000000); // npcID
			writeD(14107); // origin X
			writeD(181875 + (58 * (7 - i))); // origin Y
			writeD(-3566); // origin Z
			writeD(12080); // end X
			writeD(181875 + (58 * (7 - i))); // end Y
			writeD(-3566); // end Z
			writeF(_monsters[i].getTemplate().collisionHeight); // coll. height
			writeF(_monsters[i].getTemplate().collisionRadius); // coll. radius
			writeD(120); // ?? unknown
			
			for (int j = 0; j < 20; j++)
			{
				if (_unknown1 == 0)
				{
					writeC(_speeds[i][j]);
				}
				else
					writeC(0);
			}
			
			writeD(0);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__DD_MonRaceInfo;
	}
}