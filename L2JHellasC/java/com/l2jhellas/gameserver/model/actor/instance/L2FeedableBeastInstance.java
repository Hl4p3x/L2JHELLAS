package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.templates.L2NpcTemplate;

// This class is here mostly for convenience and for avoidance of hard-coded IDs.
// It refers to Beast (mobs) that can be attacked but can also be fed
// For example, the Beast Farm's Alpen Buffalo.
// This class is only truly used by the handlers in order to check the correctness
// of the target.  However, no additional tasks are needed, since they are all
// handled by scripted AI.
public class L2FeedableBeastInstance extends L2MonsterInstance
{
	public L2FeedableBeastInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
}
