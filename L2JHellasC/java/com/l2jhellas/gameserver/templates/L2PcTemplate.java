package com.l2jhellas.gameserver.templates;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.enums.Sex;
import com.l2jhellas.gameserver.enums.player.ClassId;
import com.l2jhellas.gameserver.enums.player.ClassRace;
import com.l2jhellas.gameserver.model.actor.item.L2Item;

public class L2PcTemplate extends L2CharTemplate
{
	
	public final ClassId classId;
	public final ClassRace race;
	public final String className;
	public final int _currentCollisionRadius;
	public final int _currentCollisionHeight;
	
	public final int spawnX;
	public final int spawnY;
	public final int spawnZ;
	
	public final int classBaseLevel;
	public final float lvlHpAdd;
	public final float lvlHpMod;
	public final float lvlCpAdd;
	public final float lvlCpMod;
	public final float lvlMpAdd;
	public final float lvlMpMod;
	
	private final List<L2Item> _items = new ArrayList<>();
	
	public L2PcTemplate(StatsSet set)
	{
		super(set);
		classId = ClassId.values()[set.getInteger("classId")];
		race = ClassRace.values()[set.getInteger("raceId")];
		className = set.getString("className");
		_currentCollisionRadius = set.getInteger("collision_radiusf");
		_currentCollisionHeight = set.getInteger("collision_heightf");
		
		spawnX = set.getInteger("spawnX");
		spawnY = set.getInteger("spawnY");
		spawnZ = set.getInteger("spawnZ");
		
		classBaseLevel = set.getInteger("classBaseLevel");
		lvlHpAdd = set.getFloat("lvlHpAdd");
		lvlHpMod = set.getFloat("lvlHpMod");
		lvlCpAdd = set.getFloat("lvlCpAdd");
		lvlCpMod = set.getFloat("lvlCpMod");
		lvlMpAdd = set.getFloat("lvlMpAdd");
		lvlMpMod = set.getFloat("lvlMpMod");
	}
	
	public void addItem(int itemId)
	{
		L2Item item = ItemTable.getInstance().getTemplate(itemId);
		if (item != null)
			_items.add(item);
	}
	
	public String getClassName()
	{
		return className;
	}
	
	public final ClassRace getRace()
	{
		return classId.getRace();
	}
	
	public int getCollisionRadius(Sex sex)
	{
		return (sex == Sex.MALE) ? collisionRadius : _currentCollisionRadius;
	}
	
	public int getCollisionHeight(Sex sex)
	{
		return (sex == Sex.MALE) ? collisionHeight : _currentCollisionHeight;
	}
	
	public L2Item[] getItems()
	{
		return _items.toArray(new L2Item[_items.size()]);
	}
}