package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;

public final class RequestPetGetItem extends L2GameClientPacket
{
	private static final String _C__8f_REQUESTPETGETITEM = "[C] 8F RequestPetGetItem";
	
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2ItemInstance item = (L2ItemInstance) L2World.getInstance().findObject(_objectId);
		if ((item == null) || (getClient().getActiveChar() == null))
			return;
		if (getClient().getActiveChar().getPet() instanceof L2SummonInstance)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2PetInstance pet = (L2PetInstance) getClient().getActiveChar().getPet();
		if ((pet == null) || pet.isDead() || pet.isOutOfControl())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		pet.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item);
	}
	
	@Override
	public String getType()
	{
		return _C__8f_REQUESTPETGETITEM;
	}
}