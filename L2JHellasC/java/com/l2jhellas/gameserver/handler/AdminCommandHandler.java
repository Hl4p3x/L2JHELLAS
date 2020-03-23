package com.l2jhellas.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

public class AdminCommandHandler implements IHandler<IAdminCommandHandler, String>
{
	private final Map<String, IAdminCommandHandler> _datatable;
	
	protected AdminCommandHandler()
	{
		_datatable = new HashMap<>();
	}
	
	@Override
	public void registerHandler(IAdminCommandHandler handler)
	{
		String[] ids = handler.getAdminCommandList();
		for (String id : ids)
		{
			_datatable.put(id, handler);
		}
	}
	
	@Override
	public synchronized void removeHandler(IAdminCommandHandler handler)
	{
		String[] ids = handler.getAdminCommandList();
		for (String id : ids)
		{
			_datatable.remove(id);
		}
	}
	
	@Override
	public IAdminCommandHandler getHandler(String adminCommand)
	{
		String command = adminCommand;
		if (adminCommand.contains(" "))
		{
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		}
		return _datatable.get(command);
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	public static AdminCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AdminCommandHandler _instance = new AdminCommandHandler();
	}
}