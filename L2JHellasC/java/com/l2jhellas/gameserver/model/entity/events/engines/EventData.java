package com.l2jhellas.gameserver.model.entity.events.engines;

import java.util.LinkedList;

public class EventData
{
	public int eventX;
	public int eventY;
	public int eventZ;
	public int eventKarma;
	public int eventPvpKills;
	public int eventPkKills;
	public String eventTitle;
	public LinkedList<String> kills = new LinkedList<>();
	public boolean eventSitForced = false;
	
	public EventData(int pEventX, int pEventY, int pEventZ, int pEventkarma, int pEventpvpkills, int pEventpkkills, String pEventTitle, LinkedList<String> pKills, boolean pEventSitForced)
	{
		eventX = pEventX;
		eventY = pEventY;
		eventZ = pEventZ;
		eventKarma = pEventkarma;
		eventPvpKills = pEventpvpkills;
		eventPkKills = pEventpkkills;
		eventTitle = pEventTitle;
		kills = pKills;
		eventSitForced = pEventSitForced;
	}
}