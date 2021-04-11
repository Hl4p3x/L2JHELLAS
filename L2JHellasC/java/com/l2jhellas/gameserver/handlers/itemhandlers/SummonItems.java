package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.datatables.xml.SummonItemsData;
import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.L2SummonItem;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.Ride;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.FloodAction;

public class SummonItems implements IItemHandler
{
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		final L2PcInstance activeChar = (L2PcInstance) playable;
		
		if (!(playable instanceof L2PcInstance))
			return;
		
		if (!FloodProtectors.performAction(activeChar.getClient(), FloodAction.ITEM_HANDLER))
		{
			activeChar.sendMessage("You are using this action too fast!");
			return;
		}

		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return;
		}
		
		if (activeChar.inObserverMode())
			return;
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return;
		}
		
		L2SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());
		
		if ((activeChar.getPet() != null || activeChar.isMounted()) && sitem.isPetSummon())
		{
			activeChar.sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
			return;
		}
		
		// Like L2OFF you can't summon pet in combat
		if (activeChar.isAttacking() || activeChar.isInCombat())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT);
			return;
		}
		
		if (activeChar.isParalyzed())
		{
			activeChar.sendMessage("You Cannot Use This While You Are Paralyzed");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isCursedWeaponEquiped() && sitem.isPetSummon())
		{
			activeChar.sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
			return;
		}
		
		int npcID = sitem.getNpcId();
		
		if (npcID == 0)
			return;
		
		L2NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(npcID);
		
		if (npcTemplate == null)
			return;
		
		switch (sitem.getType())
		{
			case 0: // static summons (like christmas tree)
				try
				{
					L2Spawn spawn = new L2Spawn(npcTemplate);
					
					// if (spawn == null)
					// return;
					
					spawn.setId(IdFactory.getInstance().getNextId());
					spawn.setLocx(activeChar.getX());
					spawn.setLocy(activeChar.getY());
					spawn.setLocz(activeChar.getZ());
					spawn.spawnOne();
					activeChar.destroyItem("Summon", item.getObjectId(), 1, null, false);
					activeChar.sendMessage("Created " + npcTemplate.name + " at x: " + spawn.getLocx() + " y: " + spawn.getLocy() + " z: " + spawn.getLocz());
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Target is not ingame.");
				}
				
				break;
			case 1: // pet summons
				L2PetInstance petSummon = L2PetInstance.spawnPet(npcTemplate, activeChar, item);
				
				if (petSummon == null)
					break;
				
				petSummon.setTitle(activeChar.getName());
				
				if (!petSummon.isRespawned())
				{
					petSummon.setCurrentHp(petSummon.getMaxHp());
					petSummon.setCurrentMp(petSummon.getMaxMp());
					petSummon.getStat().setExp(petSummon.getExpForThisLevel());
					petSummon.setCurrentFed(petSummon.getMaxFed());
				}
				
				petSummon.setRunning();
				
				if (!petSummon.isRespawned())
					petSummon.store();
				
				activeChar.setPet(petSummon);
				
				activeChar.sendPacket(new MagicSkillUse(activeChar, 2046, 1, 1000, 600000));
				activeChar.sendPacket(SystemMessageId.SUMMON_A_PET);
				petSummon.spawnMe(activeChar.getX() + 50, activeChar.getY() + 100, activeChar.getZ());
				petSummon.startFeed(false);
				item.setEnchantLevel(petSummon.getLevel());
				
				ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFinalizer(activeChar, petSummon), 900);
				
				if (petSummon.getCurrentFed() <= 0)
					ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFeedWait(activeChar, petSummon), 60000);
				else
					petSummon.startFeed(false);
				
				break;
			case 2: // wyvern
				if (!activeChar.disarmWeapons())
					return;
				Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, sitem.getNpcId());
				activeChar.sendPacket(mount);
				activeChar.broadcastPacket(mount);
				activeChar.setMountType(mount.getMountType());
				activeChar.setMountObjectID(item.getObjectId());
		}
	}
	
	static class PetSummonFeedWait implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2PetInstance _petSummon;
		
		PetSummonFeedWait(L2PcInstance activeChar, L2PetInstance petSummon)
		{
			_activeChar = activeChar;
			_petSummon = petSummon;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_petSummon.getCurrentFed() <= 0)
					_petSummon.unSummon(_activeChar);
				else
					_petSummon.startFeed(false);
			}
			catch (Throwable e)
			{
				if (Config.DEBUG)
					e.printStackTrace();
			}
		}
	}
	
	static class PetSummonFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2PetInstance _petSummon;
		
		PetSummonFinalizer(L2PcInstance activeChar, L2PetInstance petSummon)
		{
			_activeChar = activeChar;
			_petSummon = petSummon;
		}
		
		@Override
		public void run()
		{
			try
			{
				_activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
				_petSummon.setFollowStatus(true);
				_petSummon.setShowSummonAnimation(false);
			}
			catch (Throwable e)
			{
				if (Config.DEBUG)
					e.printStackTrace();
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return SummonItemsData.getInstance().itemIDs();
	}
}