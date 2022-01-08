package com.l2jhellas.gameserver.datatables.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.model.L2PetData;

public class PetData implements DocumentParser
{
	protected static final Logger _log = Logger.getLogger(PetData.class.getName());
	
	private static Map<Integer, Map<Integer, L2PetData>> _petTable = new HashMap<>();
	
	protected PetData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_petTable.clear();
		parseDatapackFile("data/xml/pet_stats.xml");
		_log.info("PetStatsTable: Loaded " + _petTable.size() + " pets");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeName().equalsIgnoreCase("list"))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (d.getNodeName().equalsIgnoreCase("pet"))
					{
						int petId, petLevel;
						
						petId = Integer.valueOf(d.getAttributes().getNamedItem("typeID").getNodeValue());
						petLevel = Integer.valueOf(d.getAttributes().getNamedItem("level").getNodeValue());
						
						// build the petdata for this level
						L2PetData petData = new L2PetData();
						
						petData.setPetID(petId);
						petData.setPetLevel(petLevel);
						petData.setPetMaxExp(Integer.valueOf(d.getAttributes().getNamedItem("expMax").getNodeValue()));
						petData.setPetMaxHP(Integer.valueOf(d.getAttributes().getNamedItem("hpMax").getNodeValue()));
						petData.setPetMaxMP(Integer.valueOf(d.getAttributes().getNamedItem("mpMax").getNodeValue()));
						petData.setPetPAtk(Integer.valueOf(d.getAttributes().getNamedItem("patk").getNodeValue()));
						petData.setPetPDef(Integer.valueOf(d.getAttributes().getNamedItem("pdef").getNodeValue()));
						petData.setPetMAtk(Integer.valueOf(d.getAttributes().getNamedItem("matk").getNodeValue()));
						petData.setPetMDef(Integer.valueOf(d.getAttributes().getNamedItem("mdef").getNodeValue()));
						petData.setPetAccuracy(Integer.valueOf(d.getAttributes().getNamedItem("acc").getNodeValue()));
						petData.setPetEvasion(Integer.valueOf(d.getAttributes().getNamedItem("evasion").getNodeValue()));
						petData.setPetCritical(Integer.valueOf(d.getAttributes().getNamedItem("crit").getNodeValue()));
						petData.setPetSpeed(Integer.valueOf(d.getAttributes().getNamedItem("speed").getNodeValue()));
						petData.setPetAtkSpeed(Integer.valueOf(d.getAttributes().getNamedItem("atk_speed").getNodeValue()));
						petData.setPetCastSpeed(Integer.valueOf(d.getAttributes().getNamedItem("cast_speed").getNodeValue()));
						petData.setPetMaxFeed(Integer.valueOf(d.getAttributes().getNamedItem("feedMax").getNodeValue()));
						petData.setPetFeedNormal(Integer.valueOf(d.getAttributes().getNamedItem("feednormal").getNodeValue()));
						petData.setPetFeedBattle(Integer.valueOf(d.getAttributes().getNamedItem("feedbattle").getNodeValue()));
						petData.setPetMaxLoad(Integer.valueOf(d.getAttributes().getNamedItem("loadMax").getNodeValue()));
						petData.setPetRegenHP(Integer.valueOf(d.getAttributes().getNamedItem("hpregen").getNodeValue()));
						petData.setPetRegenMP(Integer.valueOf(d.getAttributes().getNamedItem("mpregen").getNodeValue()));
						petData.setPetRegenMP(Integer.valueOf(d.getAttributes().getNamedItem("mpregen").getNodeValue()));
						petData.setOwnerExpTaken(Float.valueOf(d.getAttributes().getNamedItem("owner_exp_taken").getNodeValue()));
						
						// if its the first data for this petid, we initialize its level HashMap
						if (!_petTable.containsKey(petId))
						{
							_petTable.put(petId, new HashMap<Integer, L2PetData>());
						}
						
						_petTable.get(petId).put(petLevel, petData);
						petData = null;
					}
				}
			}
		}
	}
		
	public L2PetData getPetData(int petID, int petLevel)
	{
		return _petTable.get(petID).get(petLevel);
	}
	
	public static boolean isWolf(int npcId)
	{
		return npcId == 12077;
	}
	
	public static boolean isSinEater(int npcId)
	{
		return npcId == 12564;
	}
	
	public static boolean isHatchling(int npcId)
	{
		return npcId > 12310 && npcId < 12314;
	}
	
	public static boolean isStrider(int npcId)
	{
		return npcId > 12525 && npcId < 12529;
	}
	
	public static boolean isWyvern(int npcId)
	{
		return npcId == 12621;
	}
	
	public static boolean isBaby(int npcId)
	{
		return npcId > 12779 && npcId < 12783;
	}
	
	public static boolean isPetFood(int itemId)
	{
		return itemId == 2515 || itemId == 4038 || itemId == 5168 || itemId == 6316 || itemId == 7582;
	}
	
	public static boolean isWolfFood(int itemId)
	{
		return itemId == 2515;
	}
	
	public static boolean isSinEaterFood(int itemId)
	{
		return itemId == 2515;
	}
	
	public static boolean isHatchlingFood(int itemId)
	{
		return itemId == 4038;
	}
	
	public static boolean isStriderFood(int itemId)
	{
		return itemId == 5168;
	}
	
	public static boolean isWyvernFood(int itemId)
	{
		return itemId == 6316;
	}
	
	public static boolean isBabyFood(int itemId)
	{
		return itemId == 7582;
	}
	
	public static int getFoodItemId(int npcId)
	{
		if (isWolf(npcId))
			return 2515;
		else if (isSinEater(npcId))
			return 2515;
		else if (isHatchling(npcId))
			return 4038;
		else if (isStrider(npcId))
			return 5168;
		else if (isBaby(npcId))
			return 7582;
		else
			return 0;
	}
	
	public static int getPetIdByItemId(int itemId)
	{
		switch (itemId)
		{
		// wolf pet a
			case 2375:
				return 12077;
				// Sin Eater
			case 4425:
				return 12564;
				// hatchling of wind
			case 3500:
				return 12311;
				// hatchling of star
			case 3501:
				return 12312;
				// hatchling of twilight
			case 3502:
				return 12313;
				// wind strider
			case 4422:
				return 12526;
				// Star strider
			case 4423:
				return 12527;
				// Twilight strider
			case 4424:
				return 12528;
				// Wyvern
			case 8663:
				return 12621;
				// Baby Buffalo
			case 6648:
				return 12780;
				// Baby Cougar
			case 6649:
				return 12782;
				// Baby Kookaburra
			case 6650:
				return 12781;
				// unknown item id.. should never happen
			default:
				return 0;
		}
	}
	
	public static int getHatchlingWindId()
	{
		return 12311;
	}
	
	public static int getHatchlingStarId()
	{
		return 12312;
	}
	
	public static int getHatchlingTwilightId()
	{
		return 12313;
	}
	
	public static int getStriderWindId()
	{
		return 12526;
	}
	
	public static int getStriderStarId()
	{
		return 12527;
	}
	
	public static int getStriderTwilightId()
	{
		return 12528;
	}
	
	public static int getWyvernItemId()
	{
		return 8663;
	}
	
	public static int getStriderWindItemId()
	{
		return 4422;
	}
	
	public static int getStriderStarItemId()
	{
		return 4423;
	}
	
	public static int getStriderTwilightItemId()
	{
		return 4424;
	}
	
	public static int getSinEaterItemId()
	{
		return 4425;
	}
	
	public static boolean isPetItem(int itemId)
	{
		return itemId == 2375 // wolf
			|| itemId == 4425 // Sin Eater
			|| itemId == 3500 || itemId == 3501 || itemId == 3502 // hatchlings
			|| itemId == 4422 || itemId == 4423 || itemId == 4424 // striders
			|| itemId == 8663 // Wyvern
			|| itemId == 6648 || itemId == 6649 || itemId == 6650; // Babies
	}
	
	public static boolean isMountable(int npcId)
	{
		return npcId == 12526 // wind strider
			|| npcId == 12527 // star strider
			|| npcId == 12528 // twilight strider
			|| npcId == 12621; // wyvern
	}
	
	public static PetData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PetData _instance = new PetData();
	}
}