package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.enums.sound.Sound;
import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.PlaySound;
import com.l2jhellas.util.Rnd;

public class PaganKeys implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		8273,
		8274,
		8275
	};
	public static final int INTERACTION_DISTANCE = 100;
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		
		int itemId = item.getItemId();
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2Object target = activeChar.getTarget();
		
		if (!(target instanceof L2DoorInstance))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2DoorInstance door = (L2DoorInstance) target;
		
		if (!(activeChar.isInsideRadius(door, INTERACTION_DISTANCE, false, false)))
		{
			activeChar.sendMessage("Too far.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.getAbnormalEffect() > 0 || activeChar.isInCombat())
		{
			activeChar.sendMessage("You cannot use the key now.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		int openChance = 35;
		
		if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
			return;
		
		switch (itemId)
		{
			case 8273: // AnteroomKey
				if (door.getDoorName().startsWith("Anteroom"))
				{
					if (openChance > 0 && Rnd.get(100) < openChance)
					{
						activeChar.sendMessage("You opened Anterooms Door.");
						door.openMe();
						door.onOpen(); // Closes the door after 60sec
						activeChar.broadcastSocialActionInRadius(3);
					}
					else
					{
						// test with: activeChar.sendPacket(new
						// SystemMessage(SystemMessage.FAILED_TO_UNLOCK_DOOR));
						activeChar.sendMessage("You failed to open Anterooms Door.");
						activeChar.broadcastSocialActionInRadius(13);
						
						PlaySound _snd = Sound.INTERFACESOUND_CHARSTAT_CLOSE.getPacket();
						activeChar.sendPacket(_snd);
					}
				}
				else
				{
					activeChar.sendMessage("Incorrect Door.");
				}
				break;
			case 8274: // Chapelkey, Capel Door has a Gatekeeper?? I use this key for Altar Entrance and Chapel_Door
				if (door.getDoorName().startsWith("Altar_Entrance") || door.getDoorName().startsWith("Chapel_Door"))
				{
					if (openChance > 0 && Rnd.get(100) < openChance)
					{
						activeChar.sendMessage("You opened Altar Entrance.");
						door.openMe();
						door.onOpen();
						activeChar.broadcastSocialActionInRadius(3);
					}
					else
					{
						activeChar.sendMessage("You failed to open Altar Entrance.");
						activeChar.broadcastSocialActionInRadius(13);
						PlaySound _snd = Sound.INTERFACESOUND_CHARSTAT_CLOSE.getPacket();
						activeChar.sendPacket(_snd);
					}
				}
				else
				{
					activeChar.sendMessage("Incorrect Door.");
				}
				break;
			case 8275: // Key of Darkness
				if (door.getDoorName().startsWith("Door_of_Darkness"))
				{
					if (openChance > 0 && Rnd.get(100) < openChance)
					{
						activeChar.sendMessage("You opened Door of Darkness.");
						door.openMe();
						door.onOpen();
						activeChar.broadcastSocialActionInRadius(3);
					}
					else
					{
						activeChar.sendMessage("You failed to open Door of Darkness.");
						activeChar.broadcastSocialActionInRadius(13);
						PlaySound _snd = Sound.INTERFACESOUND_CHARSTAT_CLOSE.getPacket();
						activeChar.sendPacket(_snd);
					}
				}
				else
				{
					activeChar.sendMessage("Incorrect Door.");
				}
				break;
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}