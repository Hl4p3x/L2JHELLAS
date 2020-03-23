package com.l2jhellas.gameserver.datatables.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.PackRoot;
import com.l2jhellas.Config;
import com.l2jhellas.gameserver.holder.IntIntHolder;
import com.l2jhellas.gameserver.model.L2Augmentation;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.clientpackets.AbstractRefinePacket;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.util.Rnd;

public class AugmentationData
{
	private static final Logger _log = Logger.getLogger(AugmentationData.class.getName());
	
	// stats
	private static final int STAT_START = 1;
	private static final int STAT_END = 14560;
	private static final int STAT_BLOCKSIZE = 3640;
	private static final int STAT_SUBBLOCKSIZE = 91;
	private static final int STATS = 13;
	
	private static final byte[] STATS1 = new byte[STAT_SUBBLOCKSIZE];
	private static final byte[] STATS2 = new byte[STAT_SUBBLOCKSIZE];
	
	// skills
	private static final int BLUE_START = 14561;
	private static final int SKILLS_BLOCKSIZE = 178;
	
	// basestats
	private static final int BASESTAT_STR = 16341;
	private static final int BASESTAT_CON = 16342;
	private static final int BASESTAT_INT = 16343;
	private static final int BASESTAT_MEN = 16344;
	
	private final List<List<augmentationStat>> _augmentationStats = new ArrayList<>(4);
	
	private final List<List<Integer>> _blueSkills = new ArrayList<>(10);
	private final List<List<Integer>> _purpleSkills = new ArrayList<>(10);
	private final List<List<Integer>> _redSkills = new ArrayList<>(10);
	private final Map<Integer, IntIntHolder> _allSkills = new HashMap<>();
	
	protected AugmentationData()
	{
		byte stm;
		
		for (stm = 0; stm < STATS; stm++)
		{
			// solo
			STATS1[stm] = stm;
			STATS2[stm] = stm;
		}
		
		for (int i = 0; i < STATS; i++)
		{
			for (int j = i + 1; j < STATS; stm++, j++)
			{
				// combined
				STATS1[stm] = (byte) i;
				STATS2[stm] = (byte) j;
			}
		}
		
		for (int i = 0; i < 4; i++)
			_augmentationStats.add(new ArrayList<>());
		
		for (int i = 0; i < 10; i++)
		{
			_blueSkills.add(new ArrayList<>());
			_purpleSkills.add(new ArrayList<>());
			_redSkills.add(new ArrayList<>());
		}
		try
		{
			loadSkills();
			loadStats();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		_log.info(AugmentationData.class.getSimpleName() + " Loaded augmentation stats");
		
	}
	
	public void reload()
	{
		_augmentationStats.clear();
		_blueSkills.clear();
		_purpleSkills.clear();
		_redSkills.clear();
		_allSkills.clear();
		getInstance();
	}
	
	private final void loadSkills() throws SAXException, IOException, ParserConfigurationException
	{
		
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		
		final File file = new File(PackRoot.DATAPACK_ROOT, "data/stats/augmentation/augmentation_skillmap.xml");
		
		if (!file.exists())
		{
			_log.severe(AugmentationData.class.getName() + ": The augmentation skillmap file is missing.");
			return;
		}
		
		final Document doc = factory.newDocumentBuilder().parse(file);
		
		final Node n = doc.getFirstChild();
		
		for (Node o = n.getFirstChild(); o != null; o = o.getNextSibling())
		{
			if ("augmentation".equalsIgnoreCase(o.getNodeName()))
			{
				NamedNodeMap attrs = o.getAttributes();
				int skillId = 0, augmentationId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
				int skillLvL = 0;
				String type = "blue";
				
				for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("skillId".equalsIgnoreCase(d.getNodeName()))
					{
						attrs = d.getAttributes();
						skillId = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
					}
					else if ("skillLevel".equalsIgnoreCase(d.getNodeName()))
					{
						attrs = d.getAttributes();
						skillLvL = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
					}
					else if ("type".equalsIgnoreCase(d.getNodeName()))
					{
						attrs = d.getAttributes();
						type = attrs.getNamedItem("val").getNodeValue();
					}
				}
				
				if (skillId == 0 || skillLvL == 0)
					continue;
				
				int k = (augmentationId - BLUE_START) / SKILLS_BLOCKSIZE;
				
				if ("blue".equalsIgnoreCase(type))
					_blueSkills.get(k).add(augmentationId);
				else if ("purple".equalsIgnoreCase(type))
					_purpleSkills.get(k).add(augmentationId);
				else
					_redSkills.get(k).add(augmentationId);
				
				_allSkills.put(augmentationId, new IntIntHolder(skillId, skillLvL));
			}
		}
	}
	
	private final void loadStats() throws SAXException, IOException, ParserConfigurationException
	{
		
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		
		final File file = new File(PackRoot.DATAPACK_ROOT, "data/stats/augmentation/augmentation_stats.xml");
		
		if (!file.exists())
		{
			_log.severe(AugmentationData.class.getName() + ": The augmentation skillmap file is missing.");
			return;
		}
		
		final Document doc = factory.newDocumentBuilder().parse(file);
		
		final Node n = doc.getFirstChild();
		
		for (Node o = n.getFirstChild(); o != null; o = o.getNextSibling())
		{
			if ("set".equalsIgnoreCase(o.getNodeName()))
			{
				NamedNodeMap attrs = o.getAttributes();
				int order = Integer.parseInt(attrs.getNamedItem("order").getNodeValue());
				final List<augmentationStat> statList = _augmentationStats.get(order);
				
				for (Node d = o.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("stat".equalsIgnoreCase(d.getNodeName()))
					{
						attrs = d.getAttributes();
						String statName = attrs.getNamedItem("name").getNodeValue();
						float soloValues[] = null, combinedValues[] = null;
						
						for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
						{
							if ("table".equalsIgnoreCase(e.getNodeName()))
							{
								attrs = e.getAttributes();
								String tableName = attrs.getNamedItem("name").getNodeValue();
								
								StringTokenizer data = new StringTokenizer(e.getFirstChild().getNodeValue());
								List<Float> array = new ArrayList<>();
								while (data.hasMoreTokens())
									array.add(Float.parseFloat(data.nextToken()));
								
								if ("#soloValues".equalsIgnoreCase(tableName))
								{
									soloValues = new float[array.size()];
									int x = 0;
									for (float value : array)
										soloValues[x++] = value;
								}
								else
								{
									combinedValues = new float[array.size()];
									int x = 0;
									for (float value : array)
										combinedValues[x++] = value;
								}
							}
						}
						statList.add(new augmentationStat(Stats.valueOfXml(statName), soloValues, combinedValues));
					}
				}
			}
		}
	}
	
	public L2Augmentation generateRandomAugmentation(L2ItemInstance item, int lifeStoneLevel, int lifeStoneGrade)
	{
		int stat12 = 0;
		int stat34 = 0;
		
		boolean generateSkill = false;
		boolean generateGlow = false;
		
		lifeStoneLevel = Math.min(lifeStoneLevel, 9);
		
		switch (lifeStoneGrade)
		{
			case AbstractRefinePacket.GRADE_NONE:
				if (Rnd.get(1, 100) <= Config.AUGMENTATION_NG_SKILL_CHANCE)
					generateSkill = true;
				if (Rnd.get(1, 100) <= Config.AUGMENTATION_NG_GLOW_CHANCE)
					generateGlow = true;
				break;
			case AbstractRefinePacket.GRADE_MID:
				if (Rnd.get(1, 100) <= Config.AUGMENTATION_MID_SKILL_CHANCE)
					generateSkill = true;
				if (Rnd.get(1, 100) <= Config.AUGMENTATION_MID_GLOW_CHANCE)
					generateGlow = true;
				break;
			case AbstractRefinePacket.GRADE_HIGH:
				if (Rnd.get(1, 100) <= Config.AUGMENTATION_HIGH_SKILL_CHANCE)
					generateSkill = true;
				if (Rnd.get(1, 100) <= Config.AUGMENTATION_HIGH_GLOW_CHANCE)
					generateGlow = true;
				break;
			case AbstractRefinePacket.GRADE_TOP:
				if (Rnd.get(1, 100) <= Config.AUGMENTATION_TOP_SKILL_CHANCE)
					generateSkill = true;
				if (Rnd.get(1, 100) <= Config.AUGMENTATION_TOP_GLOW_CHANCE)
					generateGlow = true;
				break;
		}
		
		if (!generateSkill && Rnd.get(1, 100) <= Config.AUGMENTATION_BASESTAT_CHANCE)
			stat34 = Rnd.get(BASESTAT_STR, BASESTAT_MEN);
		
		int resultColor = Rnd.get(0, 100);
		
		if (stat34 == 0 && !generateSkill)
		{
			if (resultColor <= (15 * lifeStoneGrade) + 40)
				resultColor = 1;
			else
				resultColor = 0;
		}
		else
		{
			if (resultColor <= (10 * lifeStoneGrade) + 5 || stat34 != 0)
				resultColor = 3;
			else if (resultColor <= (10 * lifeStoneGrade) + 10)
				resultColor = 1;
			else
				resultColor = 2;
		}
		
		L2Skill skill = null;
		
		if (generateSkill)
		{
			switch (resultColor)
			{
				case 1:
					stat34 = _blueSkills.get(lifeStoneLevel).get(Rnd.get(0, _blueSkills.get(lifeStoneLevel).size() - 1));
					break;
				case 2:
					stat34 = _purpleSkills.get(lifeStoneLevel).get(Rnd.get(0, _purpleSkills.get(lifeStoneLevel).size() - 1));
					break;
				case 3:
					stat34 = _redSkills.get(lifeStoneLevel).get(Rnd.get(0, _redSkills.get(lifeStoneLevel).size() - 1));
					break;
			}
			skill = _allSkills.get(stat34).getSkill();
		}
		
		int offset;
		
		if (stat34 == 0)
		{
			int temp = Rnd.get(2, 3);
			int colorOffset = resultColor * (10 * STAT_SUBBLOCKSIZE) + temp * STAT_BLOCKSIZE + 1;
			offset = (lifeStoneLevel * STAT_SUBBLOCKSIZE) + colorOffset;
			
			stat34 = Rnd.get(offset, offset + STAT_SUBBLOCKSIZE - 1);
			if (generateGlow && lifeStoneGrade >= 2)
				offset = (lifeStoneLevel * STAT_SUBBLOCKSIZE) + (temp - 2) * STAT_BLOCKSIZE + lifeStoneGrade * (10 * STAT_SUBBLOCKSIZE) + 1;
			else
				offset = (lifeStoneLevel * STAT_SUBBLOCKSIZE) + (temp - 2) * STAT_BLOCKSIZE + Rnd.get(0, 1) * (10 * STAT_SUBBLOCKSIZE) + 1;
		}
		else
		{
			if (!generateGlow)
				offset = (lifeStoneLevel * STAT_SUBBLOCKSIZE) + Rnd.get(0, 1) * STAT_BLOCKSIZE + 1;
			else
				offset = (lifeStoneLevel * STAT_SUBBLOCKSIZE) + Rnd.get(0, 1) * STAT_BLOCKSIZE + (lifeStoneGrade + resultColor) / 2 * (10 * STAT_SUBBLOCKSIZE) + 1;
		}
		
		stat12 = Rnd.get(offset, offset + STAT_SUBBLOCKSIZE - 1);
		
		return new L2Augmentation(item, ((stat34 << 16) + stat12), skill, true);
	}
	
	public List<AugStat> getAugStatsById(int augmentationId)
	{
		List<AugStat> temp = new ArrayList<>();
		// An augmentation id contains 2 short vaues so we gotta seperate them here
		// both values contain a number from 1-16380, the first 14560 values are stats
		// the 14560 stats are divided into 4 blocks each holding 3640 values
		// each block contains 40 subblocks holding 91 stat values
		// the first 13 values are so called Solo-stats and they have the highest stat increase possible
		// after the 13 Solo-stats come 78 combined stats (thats every possible combination of the 13 solo stats)
		// the first 12 combined stats (14-26) is the stat 1 combined with stat 2-13
		// the next 11 combined stats then are stat 2 combined with stat 3-13 and so on...
		// to get the idea have a look @ optiondata_client-e.dat - thats where the data came from :)
		int stats[] = new int[2];
		stats[0] = 0x0000FFFF & augmentationId;
		stats[1] = (augmentationId >> 16);
		
		for (int i = 0; i < 2; i++)
		{
			// its a stats
			if (stats[i] >= STAT_START && stats[i] <= STAT_END)
			{
				int base = stats[i] - STAT_START;
				int color = base / STAT_BLOCKSIZE; // color blocks
				int subblock = base % STAT_BLOCKSIZE; // offset in color block
				int level = subblock / STAT_SUBBLOCKSIZE; // stat level (sub-block number)
				int stat = subblock % STAT_SUBBLOCKSIZE; // offset in sub-block - stat
				
				byte stat1 = STATS1[stat];
				byte stat2 = STATS2[stat];
				
				if (stat1 == stat2) // solo
				{
					augmentationStat as = _augmentationStats.get(color).get(stat1);
					temp.add(new AugStat(as.getStat(), as.getSingleStatValue(level)));
				}
				else
				// combined
				{
					augmentationStat as = _augmentationStats.get(color).get(stat1);
					temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(level)));
					
					as = _augmentationStats.get(color).get(stat2);
					temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(level)));
				}
			}
			// base stat
			else if (stats[i] >= BASESTAT_STR && stats[i] <= BASESTAT_MEN)
			{
				switch (stats[i])
				{
					case BASESTAT_STR:
						temp.add(new AugStat(Stats.STAT_STR, 1.0f));
						break;
					case BASESTAT_CON:
						temp.add(new AugStat(Stats.STAT_CON, 1.0f));
						break;
					case BASESTAT_INT:
						temp.add(new AugStat(Stats.STAT_INT, 1.0f));
						break;
					case BASESTAT_MEN:
						temp.add(new AugStat(Stats.STAT_MEN, 1.0f));
						break;
				}
			}
		}
		return temp;
	}
	
	public class augmentationSkill
	{
		private final int _skillId;
		private final int _maxSkillLevel;
		private final int _augmentationSkillId;
		
		public augmentationSkill(int skillId, int maxSkillLevel, int augmentationSkillId)
		{
			_skillId = skillId;
			_maxSkillLevel = maxSkillLevel;
			_augmentationSkillId = augmentationSkillId;
		}
		
		public L2Skill getSkill(int level)
		{
			if (level > _maxSkillLevel)
				return SkillTable.getInstance().getInfo(_skillId, _maxSkillLevel);
			
			return SkillTable.getInstance().getInfo(_skillId, level);
		}
		
		public int getAugmentationSkillId()
		{
			return _augmentationSkillId;
		}
	}
	
	public class augmentationStat
	{
		private final Stats _stat;
		private final int _singleSize;
		private final int _combinedSize;
		private final float _singleValues[];
		private final float _combinedValues[];
		
		public augmentationStat(Stats stat, float sValues[], float cValues[])
		{
			_stat = stat;
			_singleSize = sValues.length;
			_singleValues = sValues;
			_combinedSize = cValues.length;
			_combinedValues = cValues;
		}
		
		public int getSingleStatSize()
		{
			return _singleSize;
		}
		
		public int getCombinedStatSize()
		{
			return _combinedSize;
		}
		
		public float getSingleStatValue(int i)
		{
			if (i >= _singleSize || i < 0)
				return _singleValues[_singleSize - 1];
			
			return _singleValues[i];
		}
		
		public float getCombinedStatValue(int i)
		{
			if (i >= _combinedSize || i < 0)
				return _combinedValues[_combinedSize - 1];
			
			return _combinedValues[i];
		}
		
		public Stats getStat()
		{
			return _stat;
		}
	}
	
	public class AugStat
	{
		private final Stats _stat;
		private final float _value;
		
		public AugStat(Stats stat, float value)
		{
			_stat = stat;
			_value = value;
		}
		
		public Stats getStat()
		{
			return _stat;
		}
		
		public float getValue()
		{
			return _value;
		}
	}
	
	public static final AugmentationData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AugmentationData INSTANCE = new AugmentationData();
	}
}