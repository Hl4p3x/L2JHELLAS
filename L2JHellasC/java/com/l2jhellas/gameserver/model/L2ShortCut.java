package com.l2jhellas.gameserver.model;

public class L2ShortCut
{
	public final static int TYPE_ITEM = 1;
	public final static int TYPE_SKILL = 2;
	public final static int TYPE_ACTION = 3;
	public final static int TYPE_MACRO = 4;
	public final static int TYPE_RECIPE = 5;
	
	private int _slot;
	private int _page;
	private int _type;
	private int _id;
	private int _level;
	private int _getCharacterType;
	
	public L2ShortCut(int slotId, int pageId, int shortcutType, int shortcutId, int shortcutLevel, int getCharacterType)
	{
		_slot = slotId;
		_page = pageId;
		_type = shortcutType;
		_id = shortcutId;
		_level = shortcutLevel;
		_getCharacterType = getCharacterType;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public void setLevel(int level)
	{
		_level = level;
	}
	
	public int getPage()
	{
		return _page;
	}
	
	public int getSlot()
	{
		return _slot;
	}
	
	public int getType()
	{
		return _type;
	}
	
	public int getCharacterType()
	{
		return _getCharacterType;
	}
}