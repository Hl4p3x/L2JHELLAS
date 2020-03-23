package com.l2jhellas.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.enums.player.ChatType;

public class ChatHandler implements IHandler<IChatHandler, ChatType>
{
	private final Map<ChatType, IChatHandler> _datatable;
	
	protected ChatHandler()
	{
		_datatable = new HashMap<>();
	}
	
	@Override
	public void registerHandler(IChatHandler handler)
	{
		for (ChatType type : handler.getChatTypeList())
		{
			_datatable.put(type, handler);
		}
	}
	
	@Override
	public synchronized void removeHandler(IChatHandler handler)
	{
		for (ChatType type : handler.getChatTypeList())
		{
			_datatable.remove(type);
		}
	}
	
	@Override
	public IChatHandler getHandler(ChatType chatType)
	{
		return _datatable.get(chatType);
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	public static ChatHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ChatHandler _instance = new ChatHandler();
	}
}