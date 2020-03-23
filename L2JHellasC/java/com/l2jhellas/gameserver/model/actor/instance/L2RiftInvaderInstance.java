package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2RiftInvaderInstance extends L2MonsterInstance
{
	// Not longer needed since rift monster targeting control now is handled by the room zones for any mob
	public L2RiftInvaderInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
}