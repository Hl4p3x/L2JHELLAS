package com.l2jhellas.gameserver.scrips.quests.ai.group;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.util.Rnd;

public class PolymorphingOnAttack extends AbstractNpcAI
{
	
	private static final Map<Integer, int[]> MOBSPAWNS = new HashMap<>();
	{
		MOBSPAWNS.put(21258, new int[]
		{
			21259,
			100,
			100,
			-1
		}); // Fallen Orc Shaman -> Sharp Talon Tiger (always polymorphs)
		MOBSPAWNS.put(21261, new int[]
		{
			21262,
			100,
			20,
			0
		}); // Ol Mahum Transcender 1st stage
		MOBSPAWNS.put(21262, new int[]
		{
			21263,
			100,
			10,
			1
		}); // Ol Mahum Transcender 2nd stage
		MOBSPAWNS.put(21263, new int[]
		{
			21264,
			100,
			5,
			2
		}); // Ol Mahum Transcender 3rd stage
		MOBSPAWNS.put(21265, new int[]
		{
			21271,
			100,
			33,
			0
		}); // Cave Ant Larva -> Cave Ant
		MOBSPAWNS.put(21266, new int[]
		{
			21269,
			100,
			100,
			-1
		}); // Cave Ant Larva -> Cave Ant (always polymorphs)
		MOBSPAWNS.put(21267, new int[]
		{
			21270,
			100,
			100,
			-1
		}); // Cave Ant Larva -> Cave Ant Soldier (always polymorphs)
		MOBSPAWNS.put(21271, new int[]
		{
			21272,
			66,
			10,
			1
		}); // Cave Ant -> Cave Ant Soldier
		MOBSPAWNS.put(21272, new int[]
		{
			21273,
			33,
			5,
			2
		}); // Cave Ant Soldier -> Cave Noble Ant
		MOBSPAWNS.put(21521, new int[]
		{
			21522,
			100,
			30,
			-1
		}); // Claws of Splendor
		MOBSPAWNS.put(21527, new int[]
		{
			21528,
			100,
			30,
			-1
		}); // Anger of Splendor
		MOBSPAWNS.put(21533, new int[]
		{
			21534,
			100,
			30,
			-1
		}); // Alliance of Splendor
		MOBSPAWNS.put(21537, new int[]
		{
			21538,
			100,
			30,
			-1
		}); // Fang of Splendor
	}
	
	protected static final String[][] MOBTEXTS =
	{
		new String[]
		{
			"Enough fooling around. Get ready to die!",
			"You idiot! I've just been toying with you!",
			"Now the fun starts!"
		},
		new String[]
		{
			"I must admit, no one makes my blood boil quite like you do!",
			"Now the battle begins!",
			"Witness my true power!"
		},
		new String[]
		{
			"Prepare to die!",
			"I'll double my strength!",
			"You have more skill than I thought"
		}
	};
	
	public PolymorphingOnAttack()
	{
		super(PolymorphingOnAttack.class.getSimpleName(), "ai/group");
		for (int[] spawns : MOBSPAWNS.values())
			registerMobs(spawns, QuestEventType.ON_ATTACK);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.isVisible() && !npc.isDead())
		{
			final int[] tmp = MOBSPAWNS.get(npc.getNpcId());
			if (tmp != null)
			{
				if (npc.getCurrentHp() <= (npc.getMaxHp() * tmp[1] / 100.0) && Rnd.get(100) < tmp[2])
				{
					if (tmp[3] >= 0)
					{
						String text = MOBTEXTS[tmp[3]][Rnd.get(MOBTEXTS[tmp[3]].length)];
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL.getClientId(), npc.getName(), text));
					}
					npc.deleteMe();
					
					L2Attackable newNpc = (L2Attackable) addSpawn(tmp[0], npc.getX(), npc.getY(), npc.getZ() + 10, npc.getHeading(), false, 0, true);
					attack(newNpc, ((isPet) ? attacker.getPet() : attacker));
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new PolymorphingOnAttack();
	}
}