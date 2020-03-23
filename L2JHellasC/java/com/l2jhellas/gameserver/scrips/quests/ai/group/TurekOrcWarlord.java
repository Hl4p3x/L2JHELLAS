package com.l2jhellas.gameserver.scrips.quests.ai.group;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.util.Rnd;

public class TurekOrcWarlord extends AbstractNpcAI
{
	private static final int TUREKW = 20495;
	
	private static boolean _FirstAttacked;
	
	public TurekOrcWarlord()
	{
		super("TurekOrcWarlord", "ai");
		int[] mobs =
		{
			TUREKW
		};
		registerMobs(mobs);
		_FirstAttacked = false;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == TUREKW)
		{
			if (_FirstAttacked)
			{
				if (Rnd.get(100) == 40)
					attacker.sendPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "You wont take me down easily."));
			}
			else
			{
				_FirstAttacked = true;
				attacker.sendPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "The battle has just begun!"));
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == TUREKW)
		{
			_FirstAttacked = false;
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new TurekOrcWarlord();
	}
}