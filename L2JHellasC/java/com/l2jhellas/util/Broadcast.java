package com.l2jhellas.util;

import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.instancemanager.ZoneManager;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.L2WorldRegion;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.zone.L2ZoneType;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.skills.SkillTable;

public final class Broadcast
{
	public static void toPlayersTargettingMyself(L2Character character, L2GameServerPacket mov)
	{
		L2World.getInstance().forEachVisibleObject(character, L2PcInstance.class, player ->
		{
			if (player.getTarget() == character)
				player.sendPacket(mov);
		});
	}
	
	public static void toKnownPlayers(L2Character character, L2GameServerPacket mov)
	{		
		L2World.getInstance().forEachVisibleObject(character, L2PcInstance.class, player ->
		{
			player.sendPacket(mov);		
		});		
	}
	
	public static void toKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		if (radius < 0)
			radius = 1500;
		
		L2World.getInstance().forEachVisibleObjectInRange(character, L2PcInstance.class, radius, player ->
		{
			player.sendPacket(mov);
		});
		
	}
	
	public static void toSelfAndKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		if (character.isPlayer())
			character.sendPacket(mov);
		
		toKnownPlayers(character, mov);
	}
	
	public static void toSelfAndKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		if (radius < 0)
			radius = 600;
		
		if (character.isPlayer())
			character.sendPacket(mov);
		
		final boolean isMagicSkillUse = (mov instanceof MagicSkillUse);

		L2World.getInstance().forEachVisibleObjectInRange(character, L2PcInstance.class, radius, player ->
		{
			if (isMagicSkillUse && SkillTable.isShotSkill(((MagicSkillUse) mov).getSkillId()) && player.getSSRefusal())
				return;
			
			player.sendPacket(mov);
		});		
	}
	
	public static void toSelfAndKnownPlayersInRadiusSq(L2Character character, L2GameServerPacket mov, int radiusSq)
	{
		if (radiusSq < 0)
			radiusSq = 360000;
		
		if (character.isPlayer())
			character.sendPacket(mov);
		
		for (L2PcInstance player : L2World.getInstance().getVisibleObjects(character, L2PcInstance.class))
		{
			if (character.getDistanceSq(player) <= radiusSq)
				player.sendPacket(mov);
		}
	}
	
	public static void toAllOnlinePlayers(L2GameServerPacket mov)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			if (player.isOnline())
				player.sendPacket(mov);
		}
	}
	
	public static void toAllPlayersInRegion(L2WorldRegion region, L2GameServerPacket... packets)
	{
		for (L2Object object : region.getVisibleObjects().values())
		{
			if (object.isPlayer())
			{
				final L2PcInstance player = (L2PcInstance) object;
				
				for (L2GameServerPacket packet : packets)
					player.sendPacket(packet);
			}
		}
	}
	
	public static void toAllPlayersInRegion(L2WorldRegion region, L2GameServerPacket packet)
	{
		for (L2Object object : region.getVisibleObjects().values())
		{
			if (object.isPlayer())
			{
				final L2PcInstance player = (L2PcInstance) object;				
				player.sendPacket(packet);
			}
		}
	}
	
	public static <T extends L2ZoneType> void toAllPlayersInZoneType(Class<T> zoneType, L2GameServerPacket... packets)
	{
		for (L2ZoneType temp : ZoneManager.getInstance().getAllZones(zoneType))
		{
			for (L2PcInstance player : temp.getKnownTypeInside(L2PcInstance.class))
			{
				for (L2GameServerPacket packet : packets)
					player.sendPacket(packet);
			}
		}
	}
	
	public static void announceToOnlinePlayers(String text)
	{
		toAllOnlinePlayers(new CreatureSay(0, ChatType.ANNOUNCEMENT.getClientId(), "", text));
	}
	
	public static void announceToOnlinePlayers(String text, boolean critical)
	{
		toAllOnlinePlayers(new CreatureSay(0, (critical) ? ChatType.CRITICAL_ANNOUNCE.getClientId() : ChatType.ANNOUNCEMENT.getClientId(), "", text));
	}
}