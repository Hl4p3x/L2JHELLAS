package com.l2jhellas.gameserver.enums;

public enum PeriodOfDay
{
	ALL(0),
	DAY(1),
	NIGHT(2);
	
	private final int _val;
	
	private PeriodOfDay(int clientId)
	{
		_val = clientId;
	}

	public int getVal()
	{
		return _val;
	}

	public static PeriodOfDay getPeriod(int val)
	{
		for (PeriodOfDay period : values())
		{
			if (period.getVal() == val)
				return period;
		}
		return ALL;
	}
}