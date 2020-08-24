package com.l2jhellas.gameserver.model.zone.type;

import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.zone.L2ZoneType;
import com.l2jhellas.gameserver.network.serverpackets.AbstractNpcInfo.NpcInfo;
import com.l2jhellas.gameserver.network.serverpackets.ServerObjectInfo;

public class L2WaterZone extends L2ZoneType
{
	public L2WaterZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(ZoneId.WATER, true);
		
		if (character instanceof L2PcInstance)
			((L2PcInstance) character).broadcastUserInfo();
		else if (character instanceof L2Npc)
		{
			for (L2PcInstance player : L2World.getInstance().getVisibleObjects(character, L2PcInstance.class))
			{
				if (player != null)
				{
					if (character.getRunSpeed() == 0)
						player.sendPacket(new ServerObjectInfo((L2Npc) character, player));
					else
						player.sendPacket(new NpcInfo((L2Npc) character, player));
				}
			}
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(ZoneId.WATER, false);
		
		if (character.isPlayer())
			((L2PcInstance) character).broadcastUserInfo();
		else if (character instanceof L2Npc)
		{
			for (L2PcInstance player : L2World.getInstance().getVisibleObjects(character, L2PcInstance.class))
			{
				if (player != null)
				{
					if (character.getRunSpeed() == 0)
						player.sendPacket(new ServerObjectInfo((L2Npc) character, player));
					else
						player.sendPacket(new NpcInfo((L2Npc) character, player));
				}
			}
		}
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
	}
	
	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}