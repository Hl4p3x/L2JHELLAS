package com.l2jhellas.gameserver.skills;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jhellas.gameserver.engines.Item;
import com.l2jhellas.gameserver.enums.items.L2ArmorType;
import com.l2jhellas.gameserver.enums.items.L2EtcItemType;
import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.model.actor.item.L2EtcItem;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.templates.L2Armor;
import com.l2jhellas.gameserver.templates.L2Weapon;
import com.l2jhellas.gameserver.templates.StatsSet;

final class DocumentItem extends DocumentBase
{
	private Item _currentItem = null;
	private final List<L2Item> _itemsInFile = new ArrayList<>();
	private Map<Integer, Item> _itemData = new HashMap<>();
	
	public DocumentItem(Map<Integer, Item> pItemData, File file)
	{
		super(file);
		_itemData = pItemData;
	}
	
	private void setCurrentItem(Item item)
	{
		_currentItem = item;
	}
	
	@Override
	protected StatsSet getStatsSet()
	{
		return _currentItem.set;
	}
	
	@Override
	protected String getTableValue(String name)
	{
		return _tables.get(name)[_currentItem.currentLevel];
	}
	
	@Override
	protected String getTableValue(String name, int idx)
	{
		return _tables.get(name)[idx - 1];
	}
	
	@Override
	protected void parseDocument(Document doc)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						setCurrentItem(new Item());
						parseItem(d);
						_itemsInFile.add(_currentItem.item);
						resetTable();
					}
				}
			}
			else if ("item".equalsIgnoreCase(n.getNodeName()))
			{
				setCurrentItem(new Item());
				parseItem(n);
				_itemsInFile.add(_currentItem.item);
			}
		}
	}
	
	Item _item;
	
	protected void parseItem(Node n)
	{
		int itemId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
		String itemName = n.getAttributes().getNamedItem("name").getNodeValue();
		
		_currentItem.id = itemId;
		_currentItem.name = itemName;
		
	
		if ((_item = _itemData.get(_currentItem.id)) == null)
		{
			_log.warning(DocumentItem.class.getName() + ": No SQL data for Item ID: " + itemId + " - name: " + itemName);
		}
		_currentItem.set = _item.set;
		_currentItem.type = _item.type;
		
		Node first = n.getFirstChild();
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("table".equalsIgnoreCase(n.getNodeName()))
				parseTable(n);
		}
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("set".equalsIgnoreCase(n.getNodeName()))
				parseBeanSet(n, _itemData.get(_currentItem.id).set, 1);
		}
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("for".equalsIgnoreCase(n.getNodeName()))
			{
				makeItem();
				parseTemplate(n, _currentItem.item);
			}
		}
	}
	
	private void makeItem()
	{
		if (_currentItem.item != null)
			return;
		if (_currentItem.type instanceof L2ArmorType)
			_currentItem.item = new L2Armor((L2ArmorType) _currentItem.type, _currentItem.set);
		else if (_currentItem.type instanceof L2WeaponType)
			_currentItem.item = new L2Weapon((L2WeaponType) _currentItem.type, _currentItem.set);
		else if (_currentItem.type instanceof L2EtcItemType)
			_currentItem.item = new L2EtcItem((L2EtcItemType) _currentItem.type, _currentItem.set);
		else
			_log.warning(DocumentItem.class.getName() + ": Unknown item type " + _currentItem.type);
	}
	
	public List<L2Item> getItemList()
	{
		return _itemsInFile;
	}
}