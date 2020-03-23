package com.l2jhellas.gameserver.datatables.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jhellas.gameserver.datatables.xml.LevelingInfo.AbsorbCrystalType;
import com.l2jhellas.gameserver.engines.DocumentParser;

public class SoulCrystalsTable implements DocumentParser
{
	private final static Logger _log = Logger.getLogger(SoulCrystalsTable.class.getName());
	
	private final static Map<Integer, SoulCrystalData> _soulCrystals = new HashMap<>();
	private final static Map<Integer, LevelingInfo> _npcLevelingInfos = new HashMap<>();
	
	protected SoulCrystalsTable()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_soulCrystals.clear();
		_npcLevelingInfos.clear();
		parseDatapackFile("data/xml/soul_crystals.xml");
		_log.info("SoulCrystalsTable: Loaded " + _soulCrystals.size() + " SC(s) data and " + _npcLevelingInfos.size() + " NPC(s) data.");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		final Node first = doc.getFirstChild();
		for (Node n = first.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("crystals".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("crystal".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						Node att = attrs.getNamedItem("crystal");
						if (att == null)
						{
							_log.severe("SoulCrystalsTable: Missing \"crystal\" in \"soul_crystals.xml\", skipping.");
							continue;
						}
						int crystalItemId = Integer.parseInt(att.getNodeValue());
						
						att = attrs.getNamedItem("level");
						if (att == null)
						{
							_log.severe("SoulCrystalsTable: Missing \"level\" in \"soul_crystals.xml\" crystal=" + crystalItemId + ", skipping.");
							continue;
						}
						int level = Integer.parseInt(att.getNodeValue());
						
						att = attrs.getNamedItem("staged");
						if (att == null)
						{
							_log.severe("SoulCrystalsTable: Missing \"staged\" in \"soul_crystals.xml\" crystal=" + crystalItemId + ", skipping.");
							continue;
						}
						int stagedItemId = Integer.parseInt(att.getNodeValue());
						
						att = attrs.getNamedItem("broken");
						if (att == null)
						{
							_log.severe("SoulCrystalsTable: Missing \"broken\" in \"soul_crystals.xml\" crystal=" + crystalItemId + ", skipping.");
							continue;
						}
						int brokenItemId = Integer.parseInt(att.getNodeValue());
						
						_soulCrystals.put(crystalItemId, new SoulCrystalData(level, crystalItemId, stagedItemId, brokenItemId));
					}
				}
			}
			else if ("npcs".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("npc".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						Node att = attrs.getNamedItem("npcId");
						if (att == null)
						{
							_log.severe("SoulCrystalsTable: Missing \"npcId\" in \"soul_crystals.xml\", skipping.");
							continue;
						}
						int npcId = Integer.parseInt(att.getNodeValue());
						
						Node det = d.getFirstChild().getNextSibling();
						if (det.getNodeName().equals("detail"))
						{
							attrs = det.getAttributes();
							
							boolean skillRequired = false;
							att = attrs.getNamedItem("skill");
							if (att != null)
								skillRequired = Boolean.parseBoolean(att.getNodeValue());
							
							int chanceStage = 10;
							att = attrs.getNamedItem("chanceStage");
							if (att != null)
								chanceStage = Integer.parseInt(att.getNodeValue());
							
							int chanceBreak = 0;
							att = attrs.getNamedItem("chanceBreak");
							if (att != null)
								chanceBreak = Integer.parseInt(att.getNodeValue());
							
							AbsorbCrystalType absorbType = AbsorbCrystalType.LAST_HIT;
							att = attrs.getNamedItem("absorbType");
							if (att != null)
								absorbType = Enum.valueOf(AbsorbCrystalType.class, att.getNodeValue());
							
							int[] levelList = null;
							att = attrs.getNamedItem("levelList");
							if (att != null)
							{
								String str[] = att.getNodeValue().split(",");
								levelList = new int[str.length];
								for (int i = 0; i < str.length; i++)
								{
									Integer value = Integer.parseInt(str[i].trim());
									levelList[i] = value;
								}
							}
							
							_npcLevelingInfos.put(npcId, new LevelingInfo(absorbType, skillRequired, chanceStage, chanceBreak, levelList));
						}
					}
				}
			}
		}
	}
	
	public static final Map<Integer, SoulCrystalData> getSoulCrystalInfos()
	{
		return _soulCrystals;
	}
	
	public static final Map<Integer, LevelingInfo> getNpcInfos()
	{
		return _npcLevelingInfos;
	}
	
	public static SoulCrystalsTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SoulCrystalsTable _instance = new SoulCrystalsTable();
	}
}