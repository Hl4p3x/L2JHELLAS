package com.l2jhellas.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.model.actor.L2Npc;

public class L2SiegeClan
{
	private int _clanId = 0;
	private List<L2Npc> _flag = new ArrayList<>();
	private int _numFlagsAdded = 0;
	private SiegeClanType _type;
	
	public enum SiegeClanType
	{
		OWNER,
		DEFENDER,
		ATTACKER,
		DEFENDER_PENDING
	}
	
	public L2SiegeClan(int clanId, SiegeClanType type)
	{
		_clanId = clanId;
		_type = type;
	}
	
	public int getNumFlags()
	{
		return _numFlagsAdded;
	}
	
	public void addFlag(L2Npc flag)
	{
		_numFlagsAdded++;
		getFlag().add(flag);
	}
	
	public boolean removeFlag(L2Npc flag)
	{
		if (flag == null)
			return false;
		
		getFlag().remove(flag);
		flag.deleteMe();
		_numFlagsAdded--; // remove flag count
		
		return true;
	}
	
	public void removeFlags()
	{
		for (L2Npc flag : getFlag())
			removeFlag(flag);
	}
	
	public final int getClanId()
	{
		return _clanId;
	}
	
	public final List<L2Npc> getFlag()
	{
		if (_flag == null)
			_flag = new ArrayList<>();
		return _flag;
	}
	
	public SiegeClanType getType()
	{
		return _type;
	}
	
	public void setType(SiegeClanType setType)
	{
		_type = setType;
	}
}