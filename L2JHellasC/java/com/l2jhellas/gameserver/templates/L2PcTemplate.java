package com.l2jhellas.gameserver.templates;

import java.util.List;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.enums.Sex;
import com.l2jhellas.gameserver.enums.player.ClassId;
import com.l2jhellas.gameserver.enums.player.ClassRace;
import com.l2jhellas.gameserver.holder.GeneralSkillNode;
import com.l2jhellas.gameserver.holder.ItemTemplateHolder;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.util.Rnd;

public class L2PcTemplate extends L2CharTemplate
{
	private final ClassId _classId;
	
	private final int[] _safeFallHeight;
	
	private final int _baseSwimSpd;
	
	private final double _collisionRadiusFemale;
	private final double _collisionHeightFemale;
	
	private final List<Location> _spawnLocations;
	
	private final int _classBaseLevel;
	
	private final double[] _hpTable;
	private final double[] _mpTable;
	private final double[] _cpTable;
	
	private final double[] _hpRegenTable;
	private final double[] _mpRegenTable;
	private final double[] _cpRegenTable;
	
	private final List<ItemTemplateHolder> _items;
	private final List<GeneralSkillNode> _skills;
	
	private final L2Weapon _fists;
	
	public L2PcTemplate(StatsSet set)
	{
		super(set);

		_classId = ClassId.VALUES[set.getInteger("id")];
		
		_safeFallHeight = set.getIntegerArray("safeFallHeight");
		
		_baseSwimSpd = set.getInteger("swimSpd", 1);
		
		_collisionRadiusFemale = set.getDouble("radiusFemale");
		_collisionHeightFemale = set.getDouble("heightFemale");
		
		_spawnLocations = set.getList("spawnLocations");
		
		_classBaseLevel = set.getInteger("baseLvl");
		
		_hpTable = set.getDoubleArray("hpTable");
		_mpTable = set.getDoubleArray("mpTable");
		_cpTable = set.getDoubleArray("cpTable");
		
		_hpRegenTable = set.getDoubleArray("hpRegenTable");
		_mpRegenTable = set.getDoubleArray("mpRegenTable");
		_cpRegenTable = set.getDoubleArray("cpRegenTable");
		
		_items = set.getList("items");
		_skills = set.getList("skills");
		
		_fists = (L2Weapon) ItemTable.getInstance().getTemplate(set.getInteger("fists"));
	}
	
	public final ClassId getClassId()
	{
		return _classId;
	}
	
	public final ClassRace getRace()
	{
		return _classId.getRace();
	}
	
	public final String getClassName()
	{
		return _classId.toString();
	}
	
	public int getCollisionRadius(Sex sex)
	{
		return (int) ((sex == Sex.MALE) ? collisionRadius : _collisionRadiusFemale);
	}
	
	public int getCollisionHeight(Sex sex)
	{
		return (int) ((sex == Sex.MALE) ? collisionHeight : _collisionHeightFemale);
	}
	
	public final int getClassBaseLevel()
	{
		return _classBaseLevel;
	}
	
	public final Location getRandomSpawn()
	{
		final Location loc = Config.SPAWN_CHAR ? new Location(Config.SPAWN_X,Config.SPAWN_Y,Config.SPAWN_Z) 
		: Rnd.get(_spawnLocations);
		return (loc == null) ? new Location() : loc;
	}
	
	@Override
	public final double getBaseHpMax(int level)
	{
		return _hpTable[level - 1];
	}
	
	@Override
	public final double getBaseMpMax(int level)
	{
		return _mpTable[level - 1];
	}
	
	public final double getBaseCpMax(int level)
	{
		return _cpTable[level - 1];
	}
	
	@Override
	public final double getBaseHpRegen(int level)
	{
		return _hpRegenTable[level - 1];
	}
	
	@Override
	public final double getBaseMpRegen(int level)
	{
		return _mpRegenTable[level - 1];
	}
	
	public final double getBaseCpRegen(int level)
	{
		return _cpRegenTable[level - 1];
	}
	
	public final int getSafeFallHeight(Sex sex)
	{
		return (sex == Sex.MALE) ? _safeFallHeight[1] : _safeFallHeight[0];
	}
	
	public final int getBaseSwimSpeed()
	{
		return _baseSwimSpd;
	}

	public final List<ItemTemplateHolder> getItems()
	{
		return _items;
	}
	public final L2Weapon getFists()
	{
		return _fists;
	}
	
	public final List<GeneralSkillNode> getSkills()
	{
		return _skills;
	}

	public GeneralSkillNode findSkill(int id, int level)
	{
		return _skills.stream().filter(s -> s.getId() == id && s.getValue() == level).findFirst().orElse(null);
	}
}