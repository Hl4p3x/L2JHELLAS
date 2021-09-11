package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;

public class PetInfo extends L2GameServerPacket
{
	private static final String _S__CA_PETINFO = "[S] b1 PetInfo";
	private final L2Summon _summon;
	private int _summonAnimation = 0;
	private int _maxFed, _curFed;
	
	public PetInfo(L2Summon summon, int val)
	{
		_summon = summon;
		_summonAnimation = val;
		
		if (_summon.isShowSummonAnimation())
			_summonAnimation = 2;
		
		if (_summon instanceof L2PetInstance)
		{
			L2PetInstance pet = (L2PetInstance) _summon;
			_curFed = pet.getCurrentFed(); // how fed it is
			_maxFed = pet.getMaxFed(); // max fed it can be
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb1);
		writeD(_summon.getSummonType());
		writeD(_summon.getObjectId());
		writeD(_summon.getTemplate().idTemplate + 1000000);
		writeD(0); 	
		writeD(_summon.getX());
		writeD(_summon.getY());
		writeD(_summon.getZ());
		writeD(_summon.getHeading());
		writeD(0);
		writeD(_summon.getMAtkSpd());
		writeD(_summon.getPAtkSpd());
		
		final int runSpd = _summon.getStat().getRunSpeed();
		final int walkSpd = _summon.getStat().getWalkSpeed();
		
		writeD(runSpd);
		writeD(walkSpd);
		writeD(runSpd);
		writeD(walkSpd);
		writeD(runSpd);
		writeD(walkSpd);
		writeD(runSpd);
		writeD(walkSpd);	
		writeF(_summon.getStat().getMovementSpeedMultiplier());
		writeF(_summon.getStat().getAttackSpeedMultiplier());
		writeF(_summon.getTemplate().getCollisionRadius());
		writeF(_summon.getTemplate().getCollisionHeight());
		writeD(_summon.getWeapon());
		writeD(_summon.getArmor());
		writeD(0);
		writeC((_summon.getOwner() != null) ? 1 : 0);
		writeC(1);
		writeC((_summon.isInCombat()) ? 1 : 0);
		writeC((_summon.isAlikeDead()) ? 1 : 0);
		writeC(_summonAnimation);
		writeS(_summon.getName());
		writeS(_summon.getTitle());
		writeD(1);
		writeD(_summon.getPvpFlag());
		writeD(_summon.getKarma());
		writeD(_curFed);
		writeD(_maxFed);
		writeD((int) _summon.getCurrentHp());
		writeD(_summon.getMaxHp());
		writeD((int) _summon.getCurrentMp());
		writeD(_summon.getMaxMp());
		writeD(_summon.getStat().getSp());
		writeD(_summon.getLevel());
		writeQ(_summon.getStat().getExp());
		writeQ(_summon.getExpForThisLevel());
		writeQ(_summon.getExpForNextLevel());
		writeD((_summon instanceof L2PetInstance) ? _summon.getInventory().getTotalWeight() : 0);
		writeD(_summon.getMaxLoad());
		writeD(_summon.getPAtk(null));
		writeD(_summon.getPDef(null));
		writeD(_summon.getMAtk(null, null));
		writeD(_summon.getMDef(null, null));
		writeD(_summon.getAccuracy());
		writeD(_summon.getEvasionRate(null));
		writeD(_summon.getCriticalHit(null, null));
		writeD(_summon.getMoveSpeed());
		writeD(_summon.getPAtkSpd());
		writeD(_summon.getMAtkSpd());		
		writeD(_summon.getAbnormalEffect());
		writeH((_summon.isMountable()) ? 1 : 0);
		writeC((_summon.isInsideZone(ZoneId.WATER)) ? 1 : (_summon.isFlying()) ? 2 : 0);	
		writeH(0);
		writeC(_summon.getTeam());
		writeD(_summon.getSoulShotsPerHit());
		writeD(_summon.getSpiritShotsPerHit());
	}
	
	@Override
	public String getType()
	{
		return _S__CA_PETINFO;
	}
}