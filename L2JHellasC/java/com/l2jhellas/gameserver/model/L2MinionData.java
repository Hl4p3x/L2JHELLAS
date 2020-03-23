package com.l2jhellas.gameserver.model;

import com.l2jhellas.util.Rnd;

public class L2MinionData
{
	
	private int _minionId;
	
	private int _minionAmount;
	private int _minionAmountMin;
	private int _minionAmountMax;
	
	public void setMinionId(int id)
	{
		_minionId = id;
	}
	
	public int getMinionId()
	{
		return _minionId;
	}
	
	public void setAmountMin(int amountMin)
	{
		_minionAmountMin = amountMin;
	}
	
	public void setAmountMax(int amountMax)
	{
		_minionAmountMax = amountMax;
	}
	
	public void setAmount(int amount)
	{
		_minionAmount = amount;
	}
	
	public int getAmount()
	{
		if (_minionAmountMax > _minionAmountMin)
		{
			_minionAmount = Rnd.get(_minionAmountMin, _minionAmountMax);
			return _minionAmount;
		}
		return _minionAmountMin;
	}
}