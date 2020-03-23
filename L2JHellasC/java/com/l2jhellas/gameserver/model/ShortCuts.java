package com.l2jhellas.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.enums.items.L2EtcItemType;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jhellas.gameserver.network.serverpackets.ShortCutInit;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class ShortCuts
{
	private static Logger _log = Logger.getLogger(ShortCuts.class.getName());
	
	private static final String INSERT_SHORTCUT = "REPLACE INTO character_shortcuts (char_obj_id,slot,page,type,shortcut_id,level,class_index) values(?,?,?,?,?,?,?)";
	private static final String DELETE_SHORTCUT = "DELETE FROM character_shortcuts WHERE char_obj_id=? AND slot=? AND page=? AND class_index=?";
	private static final String LOAD_SHORTCUTS = "SELECT char_obj_id, slot, page, type, shortcut_id, level FROM character_shortcuts WHERE char_obj_id=? AND class_index=?";

	private static final int MAX_SHORTCUTS_PER_BAR = 12;
	
	private final L2PcInstance _owner;
	
	private Map<Integer, L2ShortCut> _shortCuts = new ConcurrentHashMap<>();
	
	public ShortCuts(L2PcInstance owner)
	{
		_owner = owner;
	}
	
	public L2ShortCut[] getAllShortCuts()
	{
		return _shortCuts.values().toArray(new L2ShortCut[_shortCuts.values().size()]);
	}
	
	public L2ShortCut getShortCut(int slot, int page)
	{
		L2ShortCut sc = _shortCuts.get(slot + (page * MAX_SHORTCUTS_PER_BAR));
		
		if (sc != null && ShortCutCheck(sc))
		{
			deleteShortCut(sc.getSlot(), sc.getPage());
			sc = null;
		}
		return sc;
	}
	
	public void registerShortCut(L2ShortCut shortcut)
	{	
		if(ShortCutCheck(shortcut))
			return;
			
		final L2ShortCut oldShortcut = _shortCuts.put(shortcut.getSlot() + (shortcut.getPage() * MAX_SHORTCUTS_PER_BAR), shortcut);
		
		if (oldShortcut != null)
			deleteShortCutFromDb(oldShortcut);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT_SHORTCUT))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, shortcut.getSlot());
			ps.setInt(3, shortcut.getPage());
			ps.setInt(4, shortcut.getType());
			ps.setInt(5, shortcut.getId());
			ps.setInt(6, shortcut.getLevel());
			ps.setInt(7, _owner.getClassIndex());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not store character shortcut: " + e.getMessage(), e);
		}
	}
	
	protected boolean ShortCutCheck(L2ShortCut shortcut)
	{		
		switch (shortcut.getType())
		{
			case L2ShortCut.TYPE_ITEM:
				final L2ItemInstance item = _owner.getInventory().getItemByObjectId(shortcut.getId());
				if (item == null)
					return true;		
				break;
			case L2ShortCut.TYPE_MACRO:
				final L2Macro macro = _owner.getMacroses().getMacro(shortcut.getId());
				if (macro == null)
					return true;
				break;			
			case L2ShortCut.TYPE_SKILL:
				final L2Skill skill = _owner.getSkill(shortcut.getId());
				if (skill == null)
					return true;		
				if (skill.getLevel() != shortcut.getLevel())
					shortcut.setLevel(skill.getLevel());
				break;				
			case L2ShortCut.TYPE_RECIPE:
				if (!_owner.hasRecipeList(shortcut.getId()))
					return true;
				break;
		}	
		
		return false;
	}
	
	public void deleteShortCut(int slot, int page)
	{
		slot += page * MAX_SHORTCUTS_PER_BAR;
		
		final L2ShortCut old = _shortCuts.remove(slot);
		if (old == null || _owner == null)
			return;
		
		deleteShortCutFromDb(old);
		if (old.getType() == L2ShortCut.TYPE_ITEM)
		{
			final L2ItemInstance item = _owner.getInventory().getItemByObjectId(old.getId());
			
			if ((item != null) && (item.getItemType() == L2EtcItemType.SHOT))
			{
				_owner.removeAutoSoulShot(item.getItemId());
				_owner.sendPacket(new ExAutoSoulShot(item.getItemId(), 0));
			}
		}
				
		_owner.sendPacket(new ShortCutInit(_owner));

		for (int shotId : _owner.getAutoSoulShot().values())
			_owner.sendPacket(new ExAutoSoulShot(shotId, 1));
	}
	
	public void deleteShortCutByObjectId(int objectId)
	{
		for (L2ShortCut shortcut : _shortCuts.values())
		{
			if (shortcut.getType() == L2ShortCut.TYPE_ITEM && shortcut.getId() == objectId)
				deleteShortCut(shortcut.getSlot(), shortcut.getPage());
		}
	}
	
	private void deleteShortCutFromDb(L2ShortCut shortcut)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_SHORTCUT))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, shortcut.getSlot());
			ps.setInt(3, shortcut.getPage());
			ps.setInt(4, _owner.getClassIndex());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not delete character shortcut: " + e.getMessage(), e);
		}
	}
	
	public void restore()
	{
		_shortCuts.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_SHORTCUTS))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, _owner.getClassIndex());
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					final int slot = rs.getInt("slot");
					final int page = rs.getInt("page");
					final L2ShortCut shortcut = new L2ShortCut(slot, page,  rs.getInt("type"), rs.getInt("shortcut_id"), rs.getInt("level"), 1);
					
					if(ShortCutCheck(shortcut))
						deleteShortCut(shortcut.getSlot(), shortcut.getPage());
					else
					    _shortCuts.put(slot + (page * MAX_SHORTCUTS_PER_BAR), shortcut);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not restore character shortcuts: " + e.getMessage(), e);
		}
	}
}