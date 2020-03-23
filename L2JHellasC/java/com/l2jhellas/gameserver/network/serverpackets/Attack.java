package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.l2jhellas.gameserver.model.Hit;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.position.Location;

public class Attack extends L2GameServerPacket
{
	private static final String _S__06_ATTACK = "[S] 06 Attack";
	
	private final int _attackerObjId;
	private final boolean _soulshot;
	private final int _ssGrade;
	private final Location _attackerLoc;
	private final Location _targetLoc;
	private final List<Hit> _hits = new ArrayList<>();
	
	public Attack(L2Character attacker, L2Character target, boolean useShots, int ssGrade)
	{
		_attackerObjId = attacker.getObjectId();
		_soulshot = useShots;
		_ssGrade = ssGrade;
		_attackerLoc = new Location(attacker.getX(), attacker.getY(), attacker.getZ());
		_targetLoc = new Location(target.getX(), target.getY(), target.getZ());
	}
	
	public void addHit(L2Character target, int damage, boolean miss, boolean crit, byte shld)
	{
		_hits.add(new Hit(target, damage, miss, crit, shld, _soulshot, _ssGrade));
	}
	
	public boolean hasHits()
	{
		return !_hits.isEmpty();
	}
	
	public boolean hasSoulshot()
	{
		return _soulshot;
	}
	
	private void writeHit(Hit hit)
	{
		writeD(hit.getTargetId());
		writeD(hit.getDamage());
		writeC(hit.getFlags());
	}
	
	@Override
	protected final void writeImpl()
	{
		final Iterator<Hit> it = _hits.iterator();
		writeC(0x05);
		
		writeD(_attackerObjId);
		writeHit(it.next());
		writeD(_attackerLoc.getX());
		writeD(_attackerLoc.getY());
		writeD(_attackerLoc.getZ());
		
		writeH(_hits.size() - 1);
		while (it.hasNext())
		{
			writeHit(it.next());
		}
		
		writeD(_targetLoc.getX());
		writeD(_targetLoc.getY());
		writeD(_targetLoc.getZ());
	}
	
	@Override
	public String getType()
	{
		return _S__06_ATTACK;
	}
}