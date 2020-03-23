package com.l2jhellas.gameserver.model;

import com.l2jhellas.gameserver.datatables.xml.AdminData;
import com.l2jhellas.gameserver.templates.StatsSet;

public class L2AdminCommandAccessRight
{
	
	private String _adminCommand = null;
	
	private final int _accessLevel;
	private final boolean _requireConfirm;
	
	public L2AdminCommandAccessRight(StatsSet set)
	{
		_adminCommand = set.getString("command");
		_requireConfirm = set.getBool("confirmDlg", false);
		_accessLevel = set.getInteger("accessLevel", 7);
	}
	
	public L2AdminCommandAccessRight(String command, boolean confirm, int level)
	{
		_adminCommand = command;
		_requireConfirm = confirm;
		_accessLevel = level;
	}
	
	public String getAdminCommand()
	{
		return _adminCommand;
	}
	
	public boolean hasAccess(L2AccessLevel characterAccessLevel)
	{
		L2AccessLevel accessLevel = AdminData.getInstance().getAccessLevel(_accessLevel);
		return (accessLevel.getLevel() == characterAccessLevel.getLevel() || characterAccessLevel.hasChildAccess(accessLevel));
	}
	
	public boolean getRequireConfirm()
	{
		return _requireConfirm;
	}
}