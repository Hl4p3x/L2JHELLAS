package com.l2jhellas.gameserver.instancemanager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import com.PackRoot;
import com.l2jhellas.Config;
import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.holder.BuffSkillHolder;
import com.l2jhellas.util.StringUtil;
import com.l2jhellas.util.database.L2DatabaseFactory;


public class BufferManager implements DocumentParser
{
	private static final Logger _log = Logger.getLogger(BufferManager.class.getName());

	private static final String LOAD_SCHEMES = "SELECT * FROM buffer_schemes";
	private static final String DELETE_SCHEMES = "TRUNCATE TABLE buffer_schemes";
	private static final String INSERT_SCHEME = "INSERT INTO buffer_schemes (object_id, scheme_name, skills) VALUES (?,?,?)";
	
	private final Map<Integer, Map<String, ArrayList<Integer>>> _schemesTable = new ConcurrentHashMap<>();
	private final Map<Integer, BuffSkillHolder> _availableBuffs = new LinkedHashMap<>();
	
	protected BufferManager()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile(new File(PackRoot.DATAPACK_ROOT, "data/xml/bufferSkills.xml"));
		_log.warning("Loaded: " + _availableBuffs.size() + "available buffs.");
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_SCHEMES);
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				final ArrayList<Integer> schemeList = new ArrayList<>();
				
				final String[] skills = rs.getString("skills").split(",");
				for (String skill : skills)
				{
					if (skill.isEmpty())
						break;
					
					final int skillId = Integer.valueOf(skill);
					
					if (_availableBuffs.containsKey(skillId))
						schemeList.add(skillId);
				}
				
				setScheme(rs.getInt("object_id"), rs.getString("scheme_name"), schemeList);
			}
		}
		catch (Exception e)
		{
			_log.warning("Failed to load schemes data: " + e);
		}
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "category", categoryNode ->
		{
			final String category = parseString(categoryNode.getAttributes(), "type");
			forEach(categoryNode, "buff", buffNode ->
			{
				final NamedNodeMap attrs = buffNode.getAttributes();
				final int skillId = parseInteger(attrs, "id");
				_availableBuffs.put(skillId, new BuffSkillHolder(skillId, parseInteger(attrs, "price"), category, parseString(attrs, "desc")));
			});
		}));
	}
	
	public void saveSchemes()
	{
		final StringBuilder sb = new StringBuilder();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement(DELETE_SCHEMES))
			{
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(INSERT_SCHEME))
			{
				for (Map.Entry<Integer, Map<String, ArrayList<Integer>>> player : _schemesTable.entrySet())
				{
					for (Map.Entry<String, ArrayList<Integer>> scheme : player.getValue().entrySet())
					{
						for (int skillId : scheme.getValue())
							StringUtil.append(sb, skillId, ",");
						
						if (sb.length() > 0)
							sb.setLength(sb.length() - 1);
						
						ps.setInt(1, player.getKey());
						ps.setString(2, scheme.getKey());
						ps.setString(3, sb.toString());
						ps.addBatch();
						
						sb.setLength(0);
					}
				}
				ps.executeBatch();
			}
		}
		catch (Exception e)
		{
			_log.warning("Failed to save schemes data: " + e);
		}
	}
	
	public void setScheme(int playerId, String schemeName, ArrayList<Integer> list)
	{
		if (!_schemesTable.containsKey(playerId))
			_schemesTable.put(playerId, new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
		else if (_schemesTable.get(playerId).size() >= Config.BUFFER_MAX_SCHEMES)
			return;
		
		_schemesTable.get(playerId).put(schemeName, list);
	}

	public Map<String, ArrayList<Integer>> getPlayerSchemes(int playerId)
	{
		return _schemesTable.get(playerId);
	}

	public List<Integer> getScheme(int playerId, String schemeName)
	{
		if (_schemesTable.get(playerId) == null || _schemesTable.get(playerId).get(schemeName) == null)
			return Collections.emptyList();
		
		return _schemesTable.get(playerId).get(schemeName);
	}

	public boolean getSchemeContainsSkill(int playerId, String schemeName, int skillId)
	{
		final List<Integer> skills = getScheme(playerId, schemeName);
		if (skills.isEmpty())
			return false;
		
		for (int id : skills)
		{
			if (id == skillId)
				return true;
		}
		return false;
	}

	public List<Integer> getSkillsIdsByType(String groupType)
	{
		List<Integer> skills = new ArrayList<>();
		for (BuffSkillHolder skill : _availableBuffs.values())
		{
			if (skill.getType().equalsIgnoreCase(groupType))
				skills.add(skill.getId());
		}
		return skills;
	}

	public List<String> getSkillTypes()
	{
		List<String> skillTypes = new ArrayList<>();
		for (BuffSkillHolder skill : _availableBuffs.values())
		{
			if (!skillTypes.contains(skill.getType()))
				skillTypes.add(skill.getType());
		}
		return skillTypes;
	}
	
	public BuffSkillHolder getAvailableBuff(int skillId)
	{
		return _availableBuffs.get(skillId);
	}
	
	public Map<Integer, BuffSkillHolder> getAvailableBuffs()
	{
		return _availableBuffs;
	}
	
	public static BufferManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BufferManager INSTANCE = new BufferManager();
	}
}