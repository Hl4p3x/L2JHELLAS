package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.PackageSendableList;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.FloodAction;

public final class RequestPackageSendableItemList extends L2GameClientPacket
{
	private static final String _C_9E_REQUESTPACKAGESENDABLEITEMLIST = "[C] 9E RequestPackageSendableItemList";
	private int _objectID;
	
	@Override
	protected void readImpl()
	{
		_objectID = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
			return;
		
		if (!FloodProtectors.performAction(getClient(), FloodAction.MANUFACTURE))
		{
			player.sendMessage("You depositing items too fast.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.getObjectId() == _objectID)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2ItemInstance[] items = getClient().getActiveChar().getInventory().getAvailableItems(true);
		// build list...
		sendPacket(new PackageSendableList(items, _objectID));
	}
	
	@Override
	public String getType()
	{
		return _C_9E_REQUESTPACKAGESENDABLEITEMLIST;
	}
}