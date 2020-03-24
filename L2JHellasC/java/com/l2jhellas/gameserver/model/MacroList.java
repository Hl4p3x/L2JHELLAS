package com.l2jhellas.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.model.L2Macro.L2MacroCmd;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.SendMacroList;
import com.l2jhellas.util.StringUtil;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class MacroList
{	
	private static Logger _log = Logger.getLogger(MacroList.class.getName());
	
	private final L2PcInstance _owner;
	private int _revision;
	private int _macroId;
	private final Map<Integer, L2Macro> _macroses = new LinkedHashMap<>();
	
	public MacroList(L2PcInstance owner)
	{
		_owner = owner;
		_revision = 1;
		_macroId = 1000;
	}
	
	public int getRevision()
	{
		return _revision;
	}
	
	public L2Macro[] getAllMacroses()
	{
		return _macroses.values().toArray(new L2Macro[_macroses.size()]);
	}
	
	public L2Macro getMacro(int id)
	{
		return _macroses.get(id);
	}

	public void registerMacro(L2Macro macro)
	{
		if (macro.id == 0)
		{
			macro.id = _macroId++;
			
			while (_macroses.get(macro.id) != null)
				macro.id = _macroId++;
			
			_macroses.put(macro.id, macro);
		}
		else
		{
			final L2Macro old = _macroses.put(macro.id, macro);
			if (old != null)
				deleteMacroFromDb(old);
		}
		registerMacroInDb(macro);
		sendUpdate();
	}
	
	public void deleteMacro(int id)
	{
		final L2Macro toRemove = _macroses.get(id);
		
		if (toRemove != null)
			deleteMacroFromDb(toRemove);
		
		_macroses.remove(id);
		
		L2ShortCut[] allShortCuts = _owner.getAllShortCuts();
		for (L2ShortCut sc : allShortCuts)
		{
			if (sc.getId() == id && sc.getType() == L2ShortCut.TYPE_MACRO)
				_owner.deleteShortCut(sc.getSlot(), sc.getPage());
		}
		
		sendUpdate();
	}
	
	public void sendUpdate()
	{
		_revision++;
		
		L2Macro[] all = getAllMacroses();
		
		if (all.length == 0)
			_owner.sendPacket(new SendMacroList(_revision, all.length, null));
		else
		{
			for (L2Macro m : all)
				_owner.sendPacket(new SendMacroList(_revision, all.length, m));
		}
	}
	
	private static final String INSERT_MACRO = "REPLACE INTO character_macroses (char_obj_id,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)";
	
	private void registerMacroInDb(L2Macro macro)
	{
		final StringBuilder sb = new StringBuilder(300);
		for (L2MacroCmd cmd : macro.commands)
		{
			StringUtil.append(sb, cmd.type, ",", cmd.d1, ",", cmd.d2);
			if (cmd.cmd != null && cmd.cmd.length() > 0)
				StringUtil.append(sb, ",", cmd.cmd);
			
			sb.append(';');
		}
		
		if (sb.length() > 255)
			sb.setLength(255);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT_MACRO))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, macro.id);
			ps.setInt(3, macro.icon);
			ps.setString(4, macro.name);
			ps.setString(5, macro.descr);
			ps.setString(6, macro.acronym);
			ps.setString(7, sb.toString());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warning(MacroList.class.getSimpleName() + ": could not store macro:");
		}
	}
	
	private static final String DELETE_MACRO = "DELETE FROM character_macroses WHERE char_obj_id=? AND id=?";
	
	private void deleteMacroFromDb(L2Macro macro)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_MACRO))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, macro.id);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warning(MacroList.class.getSimpleName() + ": could not delete macro:");
		}
	}
	
	private static final String LOAD_MACROS = "SELECT char_obj_id, id, icon, name, descr, acronym, commands FROM character_macroses WHERE char_obj_id=?";

	public void restore()
	{
		_macroses.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_MACROS))
		{
			ps.setInt(1, _owner.getObjectId());
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					final int id = rs.getInt("id");
					final int icon = rs.getInt("icon");
					final String name = rs.getString("name");
					final String descr = rs.getString("descr");
					final String acronym = rs.getString("acronym");
					
					final List<L2MacroCmd> commands = new ArrayList<>();
					final StringTokenizer st1 = new StringTokenizer(rs.getString("commands"), ";");
					
					while (st1.hasMoreTokens())
					{
						final StringTokenizer st = new StringTokenizer(st1.nextToken(), ",");
						if (st.countTokens() < 3)
							continue;
						
						final int type = Integer.parseInt(st.nextToken());
						final int d1 = Integer.parseInt(st.nextToken());
						final int d2 = Integer.parseInt(st.nextToken());
						
						String cmd = "";
						if (st.hasMoreTokens())
							cmd = st.nextToken();
						
						final L2MacroCmd mcmd = new L2MacroCmd(commands.size(), type, d1, d2, cmd);
						commands.add(mcmd);
					}
					
					final L2Macro macro = new L2Macro(id, icon, name, descr, acronym, commands.toArray(new L2MacroCmd[commands.size()]));
					_macroses.put(macro.id, macro);
				}
			}
		}
		catch (Exception e)
		{
			_log.warning(MacroList.class.getSimpleName() + ": could not store shortcuts:");
		}
	}
}