package com.l2jhellas.gameserver.scrips.quests.ai.group;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.network.serverpackets.NpcSay;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.util.Rnd;

public class CatsEyeBandit extends AbstractNpcAI
{
	private static final int BANDIT = 27038;
	
	private static boolean _FirstAttacked;
	
	public CatsEyeBandit()
	{
		super("CatsEyeBandit", "ai");
		int[] mobs =
		{
			BANDIT
		};
		registerMobs(mobs);
		_FirstAttacked = false;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == BANDIT)
		{
			if (_FirstAttacked)
			{
				if (Rnd.get(100) == 40)
					attacker.sendPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "You childish fool, do you think you can catch me?"));
			}
			else
			{
				_FirstAttacked = true;
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == BANDIT)
		{
			int objId = npc.getObjectId();
			if (Rnd.get(100) == 80)
				npc.broadcastPacket(new NpcSay(objId, 0, npcId, "I must do something about this shameful incident..."));
			_FirstAttacked = false;
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new CatsEyeBandit();
	}
}