package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.network.serverpackets.FlyToLocation;
import com.l2jhellas.gameserver.network.serverpackets.FlyToLocation.FlyType;
import com.l2jhellas.gameserver.network.serverpackets.ValidateLocation;
import com.l2jhellas.gameserver.skills.Env;

public class EffectThrowUp extends L2Effect
{
	private int _x;
	private int _y;
	private int _z;
	
	public EffectThrowUp(Env env, EffectTemplate template)
	{		
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.THROW_UP;
	}
	
	@Override
	public boolean onStart()
	{
		final int curX = getEffected().getX();
		final int curY = getEffected().getY();
		final int curZ = getEffected().getZ();
		
		final double dx = getEffector().getX() - curX;
		final double dy = getEffector().getY() - curY;
		final double dz = getEffector().getZ() - curZ;
		
		final double distance = Math.sqrt(dx * dx + dy * dy);
		if (distance < 1 || distance > 2000)
			return false;
		
		int offset = Math.min((int) distance + 550, 1400);

		offset += Math.abs(dz);
		
		if (offset < 5)
			offset = 5;
		
		double sin = dy / distance;
		double cos = dx / distance;
		
		_x = getEffector().getX() - (int) (offset * cos);
		_y = getEffector().getY() - (int) (offset * sin);
		_z = getEffected().getZ();
		
		final Location loc = Config.GEODATA ? GeoEngine.moveCheck(curX, curY,curZ, _x,_y) : new Location(_x,_y,_z);

		_x = loc.getX();
		_y = loc.getY();
		
		getEffected().abortAllAttacks();
		
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				
		getEffected().broadcastPacket(new FlyToLocation(getEffected(), _x, _y, _z, FlyType.THROW_UP));
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public void onExit()
	{
		
		getEffected().setXYZ(_x, _y, _z);
		getEffected().broadcastPacket(new ValidateLocation(getEffected()));
	}
}