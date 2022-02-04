package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.datatables.xml.AdminData;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.ClanHallSiegeManager;
import com.l2jhellas.gameserver.model.L2AccessLevel;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2SiegeClan;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.scrips.siegable.SiegableHall;

public class Die extends L2GameServerPacket
{
	private static final String _S__0B_DIE = "[S] 06 Die";
	private final int _charObjId;
	private final boolean _fake;
	private boolean _sweepable;
	private L2AccessLevel _access = AdminData.getInstance().getAccessLevel(0);
	private L2Clan _clan;
	L2Character _activeChar;
	private boolean _funEvent;
	
	public Die(L2Character cha)
	{
		_activeChar = cha;
		if (cha instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) cha;
			_access = player.getAccessLevel();
			_clan = player.getClan();
			_funEvent = player.isInFunEvent();
		}
		_charObjId = cha.getObjectId();
		_fake = !cha.isDead();
		if (cha instanceof L2Attackable)
		{
			_sweepable = ((L2Attackable) cha).isSweepActive();
		}
		
	}
	
	@Override
	protected final void writeImpl()
	{
		if (_fake)
			return;
		
		writeC(0x06);	
		writeD(_charObjId);

		writeD(_funEvent ? 0 : 0x01); // to nearest village

		if (!_funEvent && _clan != null)
		{
			L2SiegeClan siegeClan = null;
			Boolean isInDefense = false;
			Castle castle = CastleManager.getInstance().getCastle(_activeChar);
			final SiegableHall hall = ClanHallSiegeManager.getInstance().getActiveSiege(_activeChar);

			if (castle != null && castle.getSiege().getIsInProgress())
			{
				// siege in progress
				siegeClan = castle.getSiege().getAttackerClan(_clan);
				if (siegeClan == null && castle.getSiege().checkIsDefender(_clan))
				{
					isInDefense = true;
				}
			}
			
			writeD(_clan.hasHideout() > 0 ? 0x01 : 0x00); // 6d 01 00 00 00 - to hide away
			writeD(_clan.hasCastle() > 0 || isInDefense ? 0x01 : 0x00); // 6d 02 00 00 00 - to castle
			writeD(siegeClan != null && !isInDefense && siegeClan.getFlag().size() > 0 || ((hall != null) && hall.getSiege().checkIsAttacker(_clan))? 0x01 : 0x00); // 6d 03 00 00 00 - to siege HQ
		}
		else
		{
			writeD(0x00); // 6d 01 00 00 00 - to hide away
			writeD(0x00); // 6d 02 00 00 00 - to castle
			writeD(0x00); // 6d 03 00 00 00 - to siege HQ
		}
		
		writeD(_sweepable ? 0x01 : 0x00); // sweepable (blue glow)
		writeD(_access.allowFixedRes() ? 0x01 : 0x00); // 6d 04 00 00 00 - to FIXED
	}
	
	@Override
	public String getType()
	{
		return _S__0B_DIE;
	}
}