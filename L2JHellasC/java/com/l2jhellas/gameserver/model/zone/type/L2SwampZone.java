package com.l2jhellas.gameserver.model.zone.type;

import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.model.zone.L2CastleZoneType;
import com.l2jhellas.gameserver.network.serverpackets.ShowCastleTrap;

public class L2SwampZone extends L2CastleZoneType
{
	private int _moveBonus;
	private int _castleId;
	private int _castlTrapeId;
	
	public L2SwampZone(int id)
	{
		super(id);
		
		_moveBonus = -50;
		
		_castleId = 0;
		_castlTrapeId = 0;
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("move_bonus"))
			_moveBonus = Integer.parseInt(value);
		
		else if (name.equals("castleId"))
			_castleId = Integer.parseInt(value);
		
		else if (name.equals("castlTrapeId"))
			_castlTrapeId = Integer.parseInt(value);
		
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		// Castle traps are active only during siege, or if they're activated.
		// if (getCastle() != null && (!isEnabled() || !getCastle().getSiege().getIsInProgress()))
		// return;
		
		// Active only for attacker? part1 for tests
		if (character instanceof L2PcInstance && ActiveonlyForAttacker((character)))
		{
			character.setInsideZone(ZoneId.SWAMP, true);
			((L2PcInstance) character).sendPacket(new ShowCastleTrap(_castlTrapeId, 1));
			((L2PcInstance) character).broadcastUserInfo();
		}
	}
	
	@Override
	public Castle getCastle()
	{
		return CastleManager.getInstance().getCastleById(_castleId);
	}
	
	// Active only for attacker? part1 for tests
	private boolean ActiveonlyForAttacker(L2Character attacker)
	{
		return (_castlTrapeId > 0 && attacker != null && getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress() && getCastle().getSiege().checkIsAttacker(((L2PcInstance) attacker).getClan()));
	}
	
	// Active only for attacker? testActive part1
	// private boolean ActiveonlyForTest(L2Character attacker)
	// {
	// return (_castlTrapeId > 0 && attacker != null && getCastle() != null);
	// }
	
	@Override
	protected void onExit(L2Character character)
	{
		// don't broadcast info if not needed
		if (character.isInsideZone(ZoneId.SWAMP))
		{
			if (ActiveonlyForAttacker((character)))
			{
				character.setInsideZone(ZoneId.SWAMP, false);
				((L2PcInstance) character).sendPacket(new ShowCastleTrap(_castlTrapeId, 0));
				((L2PcInstance) character).broadcastUserInfo();
			}
		}
	}
	
	public int getMoveBonus()
	{
		return _moveBonus;
	}
}