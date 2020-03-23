package com.l2jhellas.gameserver.scrips.quests.ai.group;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.util.Rnd;

public class DeluLizardmanSpecialCommander extends AbstractNpcAI
{
	private static final int LIZARDMAN = 21107;
	
	private static boolean _FirstAttacked;
	
	public DeluLizardmanSpecialCommander()
	{
		super("DeluLizardmanSpecialCommander", "ai");
		int[] mobs =
		{
			LIZARDMAN
		};
		registerMobs(mobs);
		_FirstAttacked = false;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == LIZARDMAN)
		{
			if (_FirstAttacked)
			{
				if (Rnd.get(100) == 40)
					attacker.sendPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "Come on, Ill take you on!"));
			}
			else
			{
				_FirstAttacked = true;
				attacker.sendPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), "How dare you interrupt a sacred duel! You must be taught a lesson!"));
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == LIZARDMAN)
		{
			_FirstAttacked = false;
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new DeluLizardmanSpecialCommander();
	}
}