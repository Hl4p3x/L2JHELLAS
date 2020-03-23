package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.ai.L2CharacterAI;
import com.l2jhellas.gameserver.ai.L2NpcWalkerAI;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2NpcWalkerInstance extends L2Npc
{
	
	public L2NpcWalkerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setAI(new L2NpcWalkerAI(this));
	}
	
	@Override
	public void setAI(L2CharacterAI newAI)
	{
		if (_ai == null)
			super.setAI(newAI);
	}
	
	@Override
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake)
	{
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		return false;
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		return super.getAI();
	}
	
	@Override
	public void detachAI()
	{
	}

}