package com.l2jhellas.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Rnd;

public class L2XmassTreeInstance extends L2Npc
{
	private final ScheduledFuture<?> _aiTask;
	
	class XmassAI implements Runnable
	{
		private final L2XmassTreeInstance _caster;
		
		protected XmassAI(L2XmassTreeInstance caster)
		{
			_caster = caster;
		}
		
		@Override
		public void run()
		{
			for (L2PcInstance player : L2World.getInstance().getVisibleObjects(_caster, L2PcInstance.class))
			{
				int i = Rnd.nextInt(3);
				handleCast(player, (4262 + i));
			}
		}
		
		private boolean handleCast(L2PcInstance player, int skillId)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
			
			if (player.getFirstEffect(skill) == null)
			{
				setTarget(player);
				doCast(skill);
				
				MagicSkillUse msu = new MagicSkillUse(_caster, player, skill.getId(), 1, skill.getHitTime(), 0);
				broadcastPacket(msu);
				return true;
			}
			return false;
		}
	}
	
	public L2XmassTreeInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_aiTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new XmassAI(this), 3000, 3000);
	}
	
	@Override
	public void deleteMe()
	{
		if (_aiTask != null)
			_aiTask.cancel(true);
		
		super.deleteMe();
	}
	
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		return 900;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
}