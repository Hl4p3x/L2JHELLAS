package com.l2jhellas.gameserver.skills;

import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public final class Env
{
	public L2Character player;
	public L2Character target;
	public L2ItemInstance item;
	public L2Skill skill;
	public double value;
	public boolean skillMastery = false;
}