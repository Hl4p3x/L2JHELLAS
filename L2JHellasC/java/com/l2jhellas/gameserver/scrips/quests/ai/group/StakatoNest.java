package com.l2jhellas.gameserver.scrips.quests.ai.group;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.util.Rnd;

public class StakatoNest extends AbstractNpcAI
{
	private static final int SpikedStakatoGuard = 22107;
	private static final int FemaleSpikedStakato = 22108;
	private static final int MaleSpikedStakato1 = 22109;
	private static final int MaleSpikedStakato2 = 22110;
	
	private static final int StakatoFollower = 22112;
	private static final int CannibalisticStakatoLeader1 = 22113;
	private static final int CannibalisticStakatoLeader2 = 22114;
	
	private static final int SpikedStakatoCaptain = 22117;
	private static final int SpikedStakatoNurse1 = 22118;
	private static final int SpikedStakatoNurse2 = 22119;
	private static final int SpikedStakatoBaby = 22120;
	
	public StakatoNest()
	{
		super(StakatoNest.class.getSimpleName(), "ai/group");
		addAttackId(CannibalisticStakatoLeader1, CannibalisticStakatoLeader2);
		addKillId(MaleSpikedStakato1, FemaleSpikedStakato, SpikedStakatoNurse1, SpikedStakatoBaby);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if (npc.getCurrentHp() / npc.getMaxHp() < 0.3 && Rnd.get(100) < 5)
		{
			
			for (L2MonsterInstance follower : L2World.getInstance().getVisibleObjects(npc, L2MonsterInstance.class, 400))
			{
				if (follower.getNpcId() == StakatoFollower && !follower.isDead())
				{
					npc.broadcastPacket(new MagicSkillUse(npc, follower, (npc.getNpcId() == CannibalisticStakatoLeader2) ? 4072 : 4073, 1, 3000, 0));
					ThreadPoolManager.getInstance().scheduleGeneral(new EatTask(npc, follower), 3000L);
					break;
				}
			}
		}
		return super.onAttack(npc, player, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		switch (npc.getNpcId())
		{
			case MaleSpikedStakato1:
				for (L2MonsterInstance angryFemale : L2World.getInstance().getVisibleObjects(npc, L2MonsterInstance.class, 400))
				{
					if (angryFemale.getNpcId() == FemaleSpikedStakato && !angryFemale.isDead())
					{
						for (int i = 0; i < 3; i++)
						{
							final L2Npc guard = addSpawn(SpikedStakatoGuard, angryFemale, true, 0, false);
							attack(((L2Attackable) guard), killer);
						}
					}
				}
				break;
			
			case FemaleSpikedStakato:
				for (L2MonsterInstance morphingMale : L2World.getInstance().getVisibleObjects(npc, L2MonsterInstance.class, 400))
				{
					if (morphingMale.getNpcId() == MaleSpikedStakato1 && !morphingMale.isDead())
					{
						final L2Npc newForm = addSpawn(MaleSpikedStakato2, morphingMale, true, 0, false);
						attack(((L2Attackable) newForm), killer);
						
						morphingMale.deleteMe();
					}
				}
				break;
			
			case SpikedStakatoNurse1:
				for (L2MonsterInstance baby : L2World.getInstance().getVisibleObjects(npc, L2MonsterInstance.class, 400))
				{
					if (baby.getNpcId() == SpikedStakatoBaby && !baby.isDead())
					{
						for (int i = 0; i < 3; i++)
						{
							final L2Npc captain = addSpawn(SpikedStakatoCaptain, baby, true, 0, false);
							attack(((L2Attackable) captain), killer);
						}
					}
				}
				break;
			
			case SpikedStakatoBaby:
				for (L2MonsterInstance morphingNurse : L2World.getInstance().getVisibleObjects(npc, L2MonsterInstance.class, 400))
				{
					if (morphingNurse.getNpcId() == SpikedStakatoNurse1 && !morphingNurse.isDead())
					{
						final L2Npc newForm = addSpawn(SpikedStakatoNurse2, morphingNurse, true, 0, false);
						attack(((L2Attackable) newForm), killer);
						
						morphingNurse.deleteMe();
					}
				}
				break;
		}
		return super.onKill(npc, killer, isPet);
	}
	
	private class EatTask implements Runnable
	{
		private final L2Npc _npc;
		private final L2Npc _follower;
		
		public EatTask(L2Npc npc, L2Npc follower)
		{
			_npc = npc;
			_follower = follower;
		}
		
		@Override
		public void run()
		{
			if (_npc.isDead())
				return;
			
			if (_follower == null || _follower.isDead())
			{
				return;
			}
			
			_npc.setCurrentHp(_npc.getCurrentHp() + (_follower.getCurrentHp() / 2));
			_follower.doDie(_follower);
		}
	}
	
	public static void main(String[] args)
	{
		new StakatoNest();
	}
}