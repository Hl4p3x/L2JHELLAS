package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.effects.EffectCharge;

public class EtcStatusUpdate extends L2GameServerPacket
{
	private static final String _S__F3_ETCSTATUSUPDATE = "[S] F3 EtcStatusUpdate";
	
	private final L2PcInstance _activeChar;
	private final EffectCharge _effect;
	
	public EtcStatusUpdate(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		_effect = (EffectCharge) _activeChar.getFirstEffect(L2Effect.EffectType.CHARGE);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xF3); // several icons to a separate line (0 = disabled)		
		writeD(_effect != null ? _effect.getLevel() : 0x00); 
		writeD(_activeChar.getWeightPenalty()); // 1-4 weight penalty, lvl (1=50%, 2=66.6%, 3=80%, 4=100%)
		writeD((_activeChar.getMessageRefusal() || _activeChar.isChatBanned()) ? 1 : 0); // 1 = block all chat
		writeD(_activeChar.isInsideZone(ZoneId.DANGER_AREA) ? 1 : 0);
		writeD(Math.min(_activeChar.getExpertisePenalty(), 1)); // 1 = grade penalty
		writeD(_activeChar.getCharmOfCourage() ? 1 : 0); // 1 = charm of courage (no xp loss in siege..)
		writeD(_activeChar.getDeathPenaltyBuffLevel()); // 1-15 death penalty, lvl (combat ability decreased due to death)
	}
	
	@Override
	public String getType()
	{
		return _S__F3_ETCSTATUSUPDATE;
	}
}