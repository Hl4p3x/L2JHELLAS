package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.ai.CtrlEvent;
import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Rnd;

public class L2PenaltyMonsterInstance extends L2MonsterInstance
{
	private L2PcInstance _ptk;
	
	public L2PenaltyMonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public L2Character getMostHated()
	{
		return _ptk != null ? _ptk : super.getMostHated();
	}
	
	public void setPlayerToKill(L2PcInstance ptk)
	{
		if (Rnd.get(100) <= 80)
		{
			CreatureSay cs = new CreatureSay(getObjectId(), ChatType.GENERAL.getClientId(), getName(), "mmm your bait was delicious.");
			this.broadcastPacket(cs);
		}
		_ptk = ptk;
		addDamageHate(ptk, 10, 10);
		getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, ptk);
		addAttackerToAttackByList(ptk);
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		
		if (Rnd.get(100) <= 75)
		{
			CreatureSay cs = new CreatureSay(getObjectId(),ChatType.GENERAL.getClientId(), getName(), "I will tell fishes not to take your bait.");
			this.broadcastPacket(cs);
		}
		return true;
	}
}