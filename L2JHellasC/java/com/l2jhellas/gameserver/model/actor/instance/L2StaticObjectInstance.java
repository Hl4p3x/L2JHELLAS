package com.l2jhellas.gameserver.model.actor.instance;

import java.util.logging.Logger;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.network.serverpackets.ShowTownMap;
import com.l2jhellas.gameserver.network.serverpackets.StaticObject;

public class L2StaticObjectInstance extends L2Object
{
	private static Logger _log = Logger.getLogger(L2StaticObjectInstance.class.getName());
	
	public static final int INTERACTION_DISTANCE = 150;
	
	private int _staticObjectId;
	private int _type = -1; // 0 - map signs, 1 - throne , 2 - arena signs
	private int _x;
	private int _y;
	private String _texture;
	private boolean _isBusy;
	
	public int getStaticObjectId()
	{
		return _staticObjectId;
	}
	
	public void setStaticObjectId(int StaticObjectId)
	{
		_staticObjectId = StaticObjectId;
	}
	
	public L2StaticObjectInstance(int objectId)
	{
		super(objectId);
	}
	
	public int getType()
	{
		return _type;
	}
	
	public void setType(int type)
	{
		_type = type;
	}
	
	public void setMap(String texture, int x, int y)
	{
		_texture = "town_map." + texture;
		_x = x;
		_y = y;
	}
	
	private int getMapX()
	{
		return _x;
	}
	
	private int getMapY()
	{
		return _y;
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (_type < 0)
			_log.info("L2StaticObjectInstance: StaticObject with invalid type! StaticObjectId: " + getStaticObjectId());
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
		}
		else
		{
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!player.isInsideRadius(this, INTERACTION_DISTANCE, false, false))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				
				// Send a Server->Client packet ActionFailed (target is out of interaction range) to the L2PcInstance player
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				if (_type == 2)
				{
					String filename = "data/html/signboard.htm";
					String content = HtmCache.getInstance().getHtm(filename);
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					
					if (content != null)
						html.setHtml(content);
					else
						html.setHtml("<html><body>Signboard is missing:<br>" + filename + "</body></html>");
					
					player.sendPacket(html);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else if (_type == 0)
					player.sendPacket(new ShowTownMap(_texture, getMapX(), getMapY()));
				// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
	
	public boolean isBusy()
	{
		return _isBusy;
	}
	
	public void setBusy(boolean busy)
	{
		_isBusy = busy;
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new StaticObject(this));
	}
}