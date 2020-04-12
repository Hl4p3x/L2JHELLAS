package com.l2jhellas.gameserver.datatables.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.PackRoot;
import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.model.L2AccessLevel;
import com.l2jhellas.gameserver.model.L2AdminCommandAccessRight;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jhellas.gameserver.network.serverpackets.PlaySound;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.templates.StatsSet;

public class AdminData implements DocumentParser
{
	private static final Logger _log = Logger.getLogger(AdminData.class.getName());
	
	private final Map<Integer, L2AccessLevel> _accessLevels = new HashMap<>();
	private final Map<String, L2AdminCommandAccessRight> _adminCommandAccessRights = new HashMap<>();
	private final Map<L2PcInstance, Boolean> _gmList = new ConcurrentHashMap<>();
	private int _highestLevel = 0;
	
	protected AdminData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_accessLevels.clear();
		_adminCommandAccessRights.clear();
		parseFile(new File(PackRoot.DATAPACK_ROOT, "data/xml/accessLevels.xml"));
		_log.info(AdminData.class.getSimpleName() + ": Loaded: " + _accessLevels.size() + " Access Levels.");
		parseFile(new File(PackRoot.DATAPACK_ROOT, "data/xml/adminCommands.xml"));
		_log.info(AdminData.class.getSimpleName() + ": Loaded: " + _adminCommandAccessRights.size() + " Access Commands.");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		NamedNodeMap attrs;
		Node attr;
		StatsSet set;
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("access".equalsIgnoreCase(d.getNodeName()))
					{
						set = new StatsSet();
						attrs = d.getAttributes();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							attr = attrs.item(i);
							set.set(attr.getNodeName(), attr.getNodeValue());
						}
						final L2AccessLevel level = new L2AccessLevel(set);
						if (level.getLevel() > _highestLevel)
						{
							_highestLevel = level.getLevel();
						}
						_accessLevels.put(level.getLevel(), level);
					}
					else if ("admin".equalsIgnoreCase(d.getNodeName()))
					{
						set = new StatsSet();
						attrs = d.getAttributes();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							attr = attrs.item(i);
							set.set(attr.getNodeName(), attr.getNodeValue());
						}
						final L2AdminCommandAccessRight command = new L2AdminCommandAccessRight(set);
						_adminCommandAccessRights.put(command.getAdminCommand(), command);
					}
				}
			}
		}
	}
	
	public void reload()
	{
		_accessLevels.clear();
		_adminCommandAccessRights.clear();
		load();
	}
	
	public L2AccessLevel getAccessLevel(int accessLevelNum)
	{
		if (accessLevelNum < 0)
			return _accessLevels.get(-1);
		else if (!_accessLevels.containsKey(accessLevelNum))
		{
			_accessLevels.put(accessLevelNum, new L2AccessLevel());
		}
		return _accessLevels.get(accessLevelNum);
	}
	
	public L2AccessLevel getMasterAccessLevel()
	{
		return _accessLevels.get(_highestLevel);
	}
	
	public boolean hasAccessLevel(int id)
	{
		return _accessLevels.containsKey(id);
	}
	
	public boolean hasAccess(String adminCommand, L2AccessLevel accessLevel)
	{
		L2AdminCommandAccessRight acar = _adminCommandAccessRights.get(adminCommand);
		
		if (acar == null)
		{
			// Trying to avoid the spam for next time when the gm would try to use the same command
			if (accessLevel.getLevel() > 0 && accessLevel.getLevel() == _highestLevel)
			{
				acar = new L2AdminCommandAccessRight(adminCommand, true, accessLevel.getLevel());
				_adminCommandAccessRights.put(adminCommand, acar);
				_log.info("AdminCommandAccessRights: No rights defined for admin command " + adminCommand + " auto setting accesslevel: " + accessLevel.getLevel() + " !");
			}
			else
			{
				_log.info("AdminCommandAccessRights: No rights defined for admin command " + adminCommand + " !");
				return false;
			}
		}
		
		return acar.hasAccess(accessLevel);
	}
	
	public boolean requireConfirm(String command)
	{
		L2AdminCommandAccessRight acar = _adminCommandAccessRights.get(command);
		if (acar == null)
		{
			_log.info("AdminCommandAccessRights: No rights defined for admin command " + command + ".");
			return false;
		}
		return acar.getRequireConfirm();
	}
	
	public List<L2PcInstance> getAllGms(boolean includeHidden)
	{
		final List<L2PcInstance> tmpGmList = new ArrayList<>();
		
		for (Entry<L2PcInstance, Boolean> entry : _gmList.entrySet())
		{
			if (includeHidden || !entry.getValue())
			{
				tmpGmList.add(entry.getKey());
			}
		}
		
		return tmpGmList;
	}
	
	public List<String> getAllGmNames(boolean includeHidden)
	{
		final List<String> tmpGmList = new ArrayList<>();
		
		for (Entry<L2PcInstance, Boolean> entry : _gmList.entrySet())
		{
			if (!entry.getValue())
				tmpGmList.add(entry.getKey().getName());
			else if (includeHidden)
				tmpGmList.add(entry.getKey().getName() + " (invis)");
		}
		
		return tmpGmList;
	}
	
	public void addGm(L2PcInstance player, boolean hidden)
	{
		_gmList.put(player, hidden);
	}
	
	public void deleteGm(L2PcInstance player)
	{
		if (_gmList.containsKey(player))
		    _gmList.remove(player);
	}
	
	public void showGm(L2PcInstance player)
	{
		if (_gmList.containsKey(player))
			_gmList.put(player, false);
	}
	
	public void hideGm(L2PcInstance player)
	{
		if (_gmList.containsKey(player))
			_gmList.put(player, true);
	}
	
	public boolean isGmOnline(boolean includeHidden)
	{
		for (Entry<L2PcInstance, Boolean> entry : _gmList.entrySet())
		{
			if (includeHidden || !entry.getValue())
				return true;
		}
		
		return false;
	}
	
	public void sendListToPlayer(L2PcInstance player)
	{
		if (isGmOnline(player.isGM()))
		{
			player.sendPacket(SystemMessageId.GM_LIST);

			for (String name : getAllGmNames(player.isGM()))
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GM_S1).addString(name));
		}
		else
		{
			player.sendPacket(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW);
			player.sendPacket(PlaySound.createSound("systemmsg_e.702"));
		}
	}
	
	public void broadcastToGMs(L2GameServerPacket packet)
	{
		for (L2PcInstance gm : getAllGms(true))
		{
			gm.sendPacket(packet);
		}
	}
	
	public void broadcastMessageToGMs(String message)
	{
		for (L2PcInstance gm : getAllGms(true))
		{
			gm.sendMessage(message);
		}
	}
	
	public static AdminData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AdminData _instance = new AdminData();
	}
}
