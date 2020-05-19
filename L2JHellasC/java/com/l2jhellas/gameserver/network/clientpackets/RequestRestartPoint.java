package com.l2jhellas.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.ClanHallManager;
import com.l2jhellas.gameserver.model.L2SiegeClan;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.model.entity.ClanHall;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.Revive;

public final class RequestRestartPoint extends L2GameClientPacket
{
	static Logger _log = Logger.getLogger(RequestRestartPoint.class.getName());
	private static final String _C__6d_REQUESTRESTARTPOINT = "[C] 6d RequestRestartPoint";
	
	protected int _requestedPointType;
	protected boolean _continuation;
	
	@Override
	protected void readImpl()
	{
		_requestedPointType = readD();
	}
	
	protected void revive(L2PcInstance activeChar)
	{	
		Location loc = null;	
		Castle castle = null;
		
		if (activeChar.isInJail())
			_requestedPointType = 27;
		else if (activeChar.isFestivalParticipant())
			_requestedPointType = 4;
		
		if (activeChar.getKarma() > 0)
			loc = new Location(17836, 170178, -3507); // Should teleport to floran vilage
		else
		{
			switch (_requestedPointType)
			{
				case 1: // to clanhall
					if (activeChar.getClan().hasHideout() == 0)
					{
						activeChar.sendMessage("You may not use this respawn point!");
						return;
					}
					loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.CLAN_HALL);
					
					if (ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()) != null && ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP) != null)
						activeChar.restoreExp(ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl());
					break;
				
				case 2: // to castle
					Boolean isInDefense = false;
					castle = CastleManager.getInstance().getCastle(activeChar);
					if (castle != null && castle.getSiege().getIsInProgress())
					{
						if (castle.getSiege().checkIsDefender(activeChar.getClan()))
							isInDefense = true;
					}
					if (activeChar.getClan().hasCastle() == 0 && !isInDefense)
					{
						activeChar.sendMessage("You may not use this respawn point!");
						return;
					}
					loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.CASTLE);
					break;
				
				case 3: // to siege HQ
					L2SiegeClan siegeClan = null;
					castle = CastleManager.getInstance().getCastle(activeChar);
					
					if (castle != null && castle.getSiege().getIsInProgress())
						siegeClan = castle.getSiege().getAttackerClan(activeChar.getClan());
					
					if (siegeClan == null || siegeClan.getFlag().size() == 0)
					{
						// cheater
						activeChar.sendMessage("You may not use this respawn point!");
						return;
					}
					loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.SIEGE_FLAG);
					break;
				
				case 4: // Fixed or Player is a festival participant
					if (!activeChar.isGM() && !activeChar.isFestivalParticipant())
					{
						activeChar.sendMessage("You may not use this respawn point!");
						return;
					}
					loc = new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ()); // spawn them where they died
					break;
				
				case 27: // to jail
					if (!activeChar.isInJail())
						return;
					loc = new Location(-114356, -249645, -2984);
					break;
				
				default:
					loc = Config.ALT_RESPAWN_POINT ? new Location(Config.ALT_RESPAWN_POINT_X, Config.ALT_RESPAWN_POINT_Y, Config.ALT_RESPAWN_POINT_Z)
					: MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.TOWN);
					break;
			}
		}
		
		activeChar.setIsIn7sDungeon(false);
		
		if (activeChar.isDead())
			activeChar.doRevive();
		
		activeChar.teleToLocation(loc, true);
	}	

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		if (activeChar.isFakeDeath())
		{
			activeChar.stopFakeDeath(null);
			activeChar.broadcastPacket(new Revive(activeChar));
			return;
		}
		
		if (!activeChar.isDead())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final Castle castle = CastleManager.getInstance().getCastle(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		if (castle != null && castle.getSiege().getIsInProgress())
		{
			if (activeChar.getClan() != null && castle.getSiege().checkIsAttacker(activeChar.getClan()))
			{
				ThreadPoolManager.getInstance().scheduleGeneral(() -> revive(activeChar), castle.getSiege().getAttackerRespawnDelay());
				activeChar.sendMessage("You will be re-spawned in " + castle.getSiege().getAttackerRespawnDelay() / 1000 + " seconds.");
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(() -> revive(activeChar), castle.getSiege().getDefenderRespawnDelay());
				activeChar.sendMessage("You will be re-spawned in " + castle.getSiege().getDefenderRespawnDelay() / 1000 + " seconds.");
			}
			return;
		}
		
		revive(activeChar);
	}
	
	@Override
	public String getType()
	{
		return _C__6d_REQUESTRESTARTPOINT;
	}
}