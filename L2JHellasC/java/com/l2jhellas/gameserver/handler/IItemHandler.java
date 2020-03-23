package com.l2jhellas.gameserver.handler;

import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public interface IItemHandler
{
	public void useItem(L2Playable playable, L2ItemInstance item);
	
	public int[] getItemIds();
}