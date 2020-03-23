package com.l2jhellas.gameserver;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class DateRange
{
	private final Date _startDate, _endDate;
	
	public DateRange(Date from, Date to)
	{
		_startDate = from;
		_endDate = to;
	}
	
	public static DateRange parse(String dateRange, DateFormat format)
	{
		String[] date = dateRange.split("-");
		if (date.length == 2)
		{
			try
			{
				Date start = format.parse(date[0]);
				Date end = format.parse(date[1]);
				
				return new DateRange(start, end);
			}
			catch (ParseException e)
			{
				System.err.println("Invalid Date Format.");
				e.printStackTrace();
			}
		}
		return new DateRange(null, null);
	}
	
	public boolean isValid()
	{
		return _startDate == null || _endDate == null;
	}
	
	public boolean isWithinRange(Date date)
	{
		return date.after(_startDate) && date.before(_endDate);
	}
	
	public Date getEndDate()
	{
		return _endDate;
	}
	
	public Date getStartDate()
	{
		return _startDate;
	}
}