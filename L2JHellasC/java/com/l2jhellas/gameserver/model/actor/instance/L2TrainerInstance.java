package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public final class L2TrainerInstance extends L2NpcInstance
{
	
	public L2TrainerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/trainer/" + pom + ".htm";
	}
}