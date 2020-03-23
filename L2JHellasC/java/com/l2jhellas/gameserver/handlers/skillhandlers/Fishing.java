package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.instancemanager.ZoneManager;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.zone.type.L2WaterZone;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.util.MathUtil;
import com.l2jhellas.util.Rnd;

public class Fishing implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.FISHING
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
			return;
		
		final L2PcInstance player = (L2PcInstance) activeChar;
		
		if (!Config.ALLOWFISHING && !player.isGM())
		{
			player.sendMessage("Fishing is off");
			return;
		}
		
		if (player.isFishing())
		{
			if (player.GetFishCombat() != null)
				player.GetFishCombat().doDie(false);
			else
				player.EndFishing(false);
			
			player.sendPacket(SystemMessageId.FISHING_ATTEMPT_CANCELLED);
			return;
		}
		
		if (player.getActiveWeaponItem() != null && player.getActiveWeaponItem().getItemType() != L2WeaponType.ROD)
		{
			player.sendPacket(SystemMessageId.FISHING_POLE_NOT_EQUIPPED);
			return;
		}
		
		final L2ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		
		if (lure == null)
		{
			player.sendPacket(SystemMessageId.BAIT_ON_HOOK_BEFORE_FISHING);
			return;
		}
		
		player.SetLure(lure);
		
		if (player.isInBoat())
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_ON_BOAT);
			return;
		}
		
		if (player.isInCraftMode() || player.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_WHILE_USING_RECIPE_BOOK);
			return;
		}
		
		if (player.isInsideZone(ZoneId.WATER))
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_UNDER_WATER);
			return;
		}
		
		int x = 0;
		int y = 0;
		int z = 0;
		
		boolean allowFish = false;
		
		if (player.isInsideZone(ZoneId.FISHING))
		{
			if (!player.destroyItem("Consume", lure.getObjectId(), 1, player, false))
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_BAIT);
				return;
			}
			
			final int distance = Rnd.get(50) + 150;
			double convdeg = MathUtil.convertHeadingToDegree(player.getHeading());
			double radian = Math.toRadians(convdeg);
			
			x = player.getX() + (int) (Math.cos(radian) * distance);
			y = player.getY() + (int) (Math.sin(radian) * distance);
			z = 0;
			
			final L2WaterZone zone = ZoneManager.getInstance().getZone(x, y, L2WaterZone.class);
			
			if (zone != null)
			{
				z = zone.getWaterZ();
				
				if (GeoEngine.canSeeTarget(player.getZ(), z))
					allowFish = true;
			}
		}
		
		if (allowFish)
			player.startFishing(new Location(x, y, z));
		else
			player.sendPacket(SystemMessageId.CANNOT_FISH_HERE);
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}