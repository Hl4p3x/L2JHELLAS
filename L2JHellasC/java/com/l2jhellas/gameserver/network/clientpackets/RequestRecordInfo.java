package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.TaskPriority;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.L2Vehicle;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2StaticObjectInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.serverpackets.AbstractNpcInfo.NpcInfo;
import com.l2jhellas.gameserver.network.serverpackets.AbstractNpcInfo.SummonInfo;
import com.l2jhellas.gameserver.network.serverpackets.CharInfo;
import com.l2jhellas.gameserver.network.serverpackets.DoorInfo;
import com.l2jhellas.gameserver.network.serverpackets.DoorStatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.GetOnVehicle;
import com.l2jhellas.gameserver.network.serverpackets.PetItemList;
import com.l2jhellas.gameserver.network.serverpackets.RelationChanged;
import com.l2jhellas.gameserver.network.serverpackets.SpawnItem;
import com.l2jhellas.gameserver.network.serverpackets.SpawnItemPoly;
import com.l2jhellas.gameserver.network.serverpackets.StaticObject;
import com.l2jhellas.gameserver.network.serverpackets.UserInfo;
import com.l2jhellas.gameserver.network.serverpackets.VehicleDeparture;
import com.l2jhellas.gameserver.network.serverpackets.VehicleInfo;

public class RequestRecordInfo extends L2GameClientPacket
{
	private static final String _0__CF_REQUEST_RECORD_INFO = "[0] CF RequestRecordInfo";
	
	public TaskPriority getPriority()
	{
		return TaskPriority.PR_NORMAL;
	}
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance _activeChar = getClient().getActiveChar();
		
		if (_activeChar == null)
			return;
		
		// _activeChar.getKnownList().refreshInfos();
		_activeChar.sendPacket(new UserInfo(_activeChar));
		
		for (L2Object object : L2World.getInstance().getVisibleObjects(_activeChar, L2Object.class))
		{
			if (object == null)
				continue;
			
			if (object.getPoly().isMorphed() && object.getPoly().getPolyType().equals("item"))
				_activeChar.sendPacket(new SpawnItemPoly(object));
			else
			{
				if (object instanceof L2ItemInstance)
					_activeChar.sendPacket(new SpawnItem((L2ItemInstance) object));
				else if (object instanceof L2DoorInstance)
				{
					_activeChar.sendPacket(new DoorInfo((L2DoorInstance) object));
					_activeChar.sendPacket(new DoorStatusUpdate((L2DoorInstance) object));
				}
				else if (object instanceof L2Vehicle)
				{
					if (!_activeChar.isInBoat() && object != _activeChar.getBoat())
					{
						_activeChar.sendPacket(new VehicleInfo((L2Vehicle) object));
						_activeChar.sendPacket(new VehicleDeparture((L2Vehicle) object));
					}
				}
				else if (object instanceof L2StaticObjectInstance)
					_activeChar.sendPacket(new StaticObject((L2StaticObjectInstance) object));
				else if (object instanceof L2Npc)
					_activeChar.sendPacket(new NpcInfo((L2Npc) object, _activeChar));
				else if (object instanceof L2Summon)
				{
					L2Summon summon = (L2Summon) object;
					
					// Check if the L2PcInstance is the owner of the Pet
					if (_activeChar.equals(summon.getOwner()))
					{
						summon.broadcastStatusUpdate();
						
						if (summon instanceof L2PetInstance)
							_activeChar.sendPacket(new PetItemList((L2PetInstance) summon));
					}
					else
						_activeChar.sendPacket(new SummonInfo(summon, _activeChar,0));
					
					// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
					summon.updateEffectIcons(true);
				}
				else if (object instanceof L2PcInstance)
				{
					L2PcInstance otherPlayer = (L2PcInstance) object;
					
					if (otherPlayer.isInBoat())
					{
						otherPlayer.getPosition().setWorldPosition(otherPlayer.getBoat().getPosition().getWorldPosition());
						_activeChar.sendPacket(new CharInfo(otherPlayer));
						int relation = otherPlayer.getRelation(_activeChar);
						_activeChar.sendPacket(new RelationChanged(otherPlayer, relation, _activeChar.isAutoAttackable(otherPlayer)));
						_activeChar.sendPacket(new GetOnVehicle(otherPlayer.getObjectId(), otherPlayer.getBoat().getObjectId(), otherPlayer.getInVehiclePosition()));
					}
					else
					{
						_activeChar.sendPacket(new CharInfo(otherPlayer));
						int relation = otherPlayer.getRelation(_activeChar);
						_activeChar.sendPacket(new RelationChanged(otherPlayer, relation, _activeChar.isAutoAttackable(otherPlayer)));
					}
				}
				
				if (object instanceof L2Character)
				{
					// Update the state of the L2Character object client side by sending Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance
					L2Character obj = (L2Character) object;
					obj.getAI().describeStateToPlayer(_activeChar);
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _0__CF_REQUEST_RECORD_INFO;
	}
}