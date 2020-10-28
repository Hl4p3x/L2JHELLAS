package com.l2jhellas.gameserver.model.actor.item;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.IconData;
import com.l2jhellas.gameserver.enums.items.L2ArmorType;
import com.l2jhellas.gameserver.enums.items.L2CrystalType;
import com.l2jhellas.gameserver.enums.items.L2EtcItemType;
import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.effects.EffectTemplate;
import com.l2jhellas.gameserver.skills.funcs.Func;
import com.l2jhellas.gameserver.skills.funcs.FuncTemplate;
import com.l2jhellas.gameserver.templates.StatsSet;

public abstract class L2Item
{
	private final List<Quest> _questEvents = new ArrayList<>();
	
	public static final int TYPE1_WEAPON_RING_EARRING_NECKLACE = 0;
	public static final int TYPE1_SHIELD_ARMOR = 1;
	public static final int TYPE1_ITEM_QUESTITEM_ADENA = 4;
	
	public static final int TYPE2_WEAPON = 0;
	public static final int TYPE2_SHIELD_ARMOR = 1;
	public static final int TYPE2_ACCESSORY = 2;
	public static final int TYPE2_QUEST = 3;
	public static final int TYPE2_MONEY = 4;
	public static final int TYPE2_OTHER = 5;
	public static final int TYPE2_PET_WOLF = 6;
	public static final int TYPE2_PET_HATCHLING = 7;
	public static final int TYPE2_PET_STRIDER = 8;
	public static final int TYPE2_PET_BABY = 9;
	
	public static final int SLOT_NONE = 0x0000;
	public static final int SLOT_UNDERWEAR = 0x0001;
	public static final int SLOT_R_EAR = 0x0002;
	public static final int SLOT_L_EAR = 0x0004;
	public static final int SLOT_LR_EAR = 0x00006;
	public static final int SLOT_NECK = 0x0008;
	public static final int SLOT_R_FINGER = 0x0010;
	public static final int SLOT_L_FINGER = 0x0020;
	public static final int SLOT_LR_FINGER = 0x0030;
	public static final int SLOT_HEAD = 0x0040;
	public static final int SLOT_R_HAND = 0x0080;
	public static final int SLOT_L_HAND = 0x0100;
	public static final int SLOT_GLOVES = 0x0200;
	public static final int SLOT_CHEST = 0x0400;
	public static final int SLOT_LEGS = 0x0800;
	public static final int SLOT_FEET = 0x1000;
	public static final int SLOT_BACK = 0x2000;
	public static final int SLOT_LR_HAND = 0x4000;
	public static final int SLOT_FULL_ARMOR = 0x8000;
	public static final int SLOT_FACE = 0x010000;
	public static final int SLOT_ALLDRESS = 0x020000;
	public static final int SLOT_HAIR = 0x040000;
	public static final int SLOT_HAIRALL = 0x080000;
	
	public static final int SLOT_ALLWEAPON = SLOT_LR_HAND | SLOT_R_HAND;

	public static final int SLOT_WOLF = -100;
	public static final int SLOT_HATCHLING = -101;
	public static final int SLOT_STRIDER = -102;
	public static final int SLOT_BABYPET = -103;
	
	
	
	public static final int MATERIAL_STEEL = 0x00; // ??
	public static final int MATERIAL_FINE_STEEL = 0x01; // ??
	public static final int MATERIAL_BLOOD_STEEL = 0x02; // ??
	public static final int MATERIAL_BRONZE = 0x03; // ??
	public static final int MATERIAL_SILVER = 0x04; // ??
	public static final int MATERIAL_GOLD = 0x05; // ??
	public static final int MATERIAL_MITHRIL = 0x06; // ??
	public static final int MATERIAL_ORIHARUKON = 0x07; // ??
	public static final int MATERIAL_PAPER = 0x08; // ??
	public static final int MATERIAL_WOOD = 0x09; // ??
	public static final int MATERIAL_CLOTH = 0x0a; // ??
	public static final int MATERIAL_LEATHER = 0x0b; // ??
	public static final int MATERIAL_BONE = 0x0c; // ??
	public static final int MATERIAL_HORN = 0x0d; // ??
	public static final int MATERIAL_DAMASCUS = 0x0e; // ??
	public static final int MATERIAL_ADAMANTAITE = 0x0f; // ??
	public static final int MATERIAL_CHRYSOLITE = 0x10; // ??
	public static final int MATERIAL_CRYSTAL = 0x11; // ??
	public static final int MATERIAL_LIQUID = 0x12; // ??
	public static final int MATERIAL_SCALE_OF_DRAGON = 0x13; // ??
	public static final int MATERIAL_DYESTUFF = 0x14; // ??
	public static final int MATERIAL_COBWEB = 0x15; // ??
	public static final int MATERIAL_SEED = 0x15; // ??
	
	private final int _itemId;
	private final String _name;
	private final int _type1;// needed for item list (inventory)
	private final int _type2;// different lists for armor, weapon, etc
	private final int _weight;
	private final boolean _crystallizable;
	private final boolean _stackable;
	private final int _materialType;
	private final L2CrystalType _crystalType;

	private final int _duration;
	private final int _bodyPart;
	private final int _referencePrice;
	private final int _crystalCount;
	private final boolean _sellable;
	private final boolean _dropable;
	private final boolean _destroyable;
	private final boolean _tradeable;
	private final boolean _heroItem;
	
	protected final Enum<?> _type;
	
	protected FuncTemplate[] _funcTemplates;
	protected EffectTemplate[] _effectTemplates;
	protected L2Skill[] _skills;
	
	private static final Func[] _emptyFunctionSet = new Func[0];
	protected static final L2Effect[] _emptyEffectSet = new L2Effect[0];
	
	protected L2Item(Enum<?> type, StatsSet set)
	{
		_type = type;
		_itemId = set.getInteger("item_id");
		_name = set.getString("name");
		_type1 = set.getInteger("type1");// needed for item list (inventory)
		_type2 = set.getInteger("type2");// different lists for armor, weapon, etc
		_weight = set.getInteger("weight");
		_crystallizable = set.getBool("crystallizable");
		_stackable = set.getBool("stackable", false);
		_materialType = set.getInteger("material");
		_crystalType = set.getEnum("crystal_type", L2CrystalType.class, L2CrystalType.NONE);
		_duration = set.getInteger("duration");
		_bodyPart = set.getInteger("bodypart");
		_referencePrice = set.getInteger("price");
		_crystalCount = set.getInteger("crystal_count", 0);
		_sellable = set.getBool("sellable", true);
		_dropable = set.getBool("dropable", true);
		_destroyable = set.getBool("destroyable", true);
		_tradeable = set.getBool("tradeable", true);
		_heroItem = (_itemId >= 6611 && _itemId <= 6621) || _itemId == 6842;
		
	}
	
	public Enum<?> getItemType()
	{
		return _type;
	}
	
	public final int getDuration()
	{
		return _duration;
	}
	
	public final int getItemId()
	{
		return _itemId;
	}
	
	public final String getIcon()
	{
		return IconData.getIconByItemId(getItemId());
	}
	
	public abstract int getItemMask();
	
	public final int getMaterialType()
	{
		return _materialType;
	}
	
	public final int getType2()
	{
		return _type2;
	}
	
	public final int getWeight()
	{
		return _weight;
	}
	
	public final boolean isCrystallizable()
	{
		return _crystallizable;
	}
	
	public final L2CrystalType getCrystalType()
	{
		return _crystalType;
	}

	public final int getCrystalItemId()
	{
		return _crystalType.getCrystalId();
	}

	public final int getCrystalCount()
	{
		return _crystalCount;
	}
	
	public final int getCrystalCount(int enchantLevel)
	{
		if (enchantLevel > 3)
			switch (_type2)
			{
				case TYPE2_SHIELD_ARMOR:
				case TYPE2_ACCESSORY:
					return _crystalCount + getCrystalType().getCrystalEnchantBonusArmor() * (3 * enchantLevel - 6);
				case TYPE2_WEAPON:
					return _crystalCount + getCrystalType().getCrystalEnchantBonusWeapon() * (2 * enchantLevel - 3);
				default:
					return _crystalCount;
			}
		else if (enchantLevel > 0)
			switch (_type2)
			{
				case TYPE2_SHIELD_ARMOR:
				case TYPE2_ACCESSORY:
					return _crystalCount + getCrystalType().getCrystalEnchantBonusArmor() * enchantLevel;
				case TYPE2_WEAPON:
					return _crystalCount + getCrystalType().getCrystalEnchantBonusWeapon() * enchantLevel;
				default:
					return _crystalCount;
			}
		else
			return _crystalCount;
	}
	
	public final String getItemName()
	{
		return _name;
	}
	
	public final int getBodyPart()
	{
		return _bodyPart;
	}
	
	public final int getType1()
	{
		return _type1;
	}
	
	public final boolean isStackable()
	{
		return _stackable;
	}
	
	public boolean isConsumable()
	{
		return false;
	}
	
	public final int getReferencePrice()
	{
		return (isConsumable() ? (int) (_referencePrice * Config.RATE_CONSUMABLE_COST) : _referencePrice);
	}
	
	public final boolean isSellable()
	{
		return _sellable;
	}
	
	public final boolean isDropable()
	{
		return _dropable;
	}
	
	public final boolean isDestroyable()
	{
		return _destroyable;
	}
	
	public final boolean isTradeable()
	{
		return _tradeable;
	}
	
	public boolean isForHatchling()
	{
		return (_type2 == TYPE2_PET_HATCHLING);
	}
	
	public boolean isForStrider()
	{
		return (_type2 == TYPE2_PET_STRIDER);
	}
	
	public boolean isForWolf()
	{
		return (_type2 == TYPE2_PET_WOLF);
	}
	
	public boolean isForBabyPet()
	{
		return (_type2 == TYPE2_PET_BABY);
	}
	
	public Func[] getStatFuncs(L2ItemInstance instance, L2Character player)
	{
		if (_funcTemplates == null)
			return _emptyFunctionSet;
		List<Func> funcs = new ArrayList<>();
		for (FuncTemplate t : _funcTemplates)
		{
			Env env = new Env();
			env.player = player;
			env.target = player;
			env.item = instance;
			Func f = t.getFunc(env, this); // skill is owner
			if (f != null)
				funcs.add(f);
		}
		if (funcs.size() == 0)
			return _emptyFunctionSet;
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	public L2Effect[] getEffects(L2ItemInstance instance, L2Character player)
	{
		if (_effectTemplates == null)
			return _emptyEffectSet;
		List<L2Effect> effects = new ArrayList<>();
		for (EffectTemplate et : _effectTemplates)
		{
			Env env = new Env();
			env.player = player;
			env.target = player;
			env.item = instance;
			L2Effect e = et.getEffect(env);
			if (e != null)
				effects.add(e);
		}
		if (effects.size() == 0)
			return _emptyEffectSet;
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	public L2Effect[] getSkillEffects(L2Character caster, L2Character target)
	{
		if (_skills == null)
			return _emptyEffectSet;
		List<L2Effect> effects = new ArrayList<>();
		
		for (L2Skill skill : _skills)
		{
			if (!skill.checkCondition(caster, target, true))
				continue; // Skill condition not met
				
			if (target.getFirstEffect(skill.getId()) != null)
				target.removeEffect(target.getFirstEffect(skill.getId()));
			for (L2Effect e : skill.getEffects(caster, target))
				effects.add(e);
		}
		if (effects.size() == 0)
			return _emptyEffectSet;
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	public void attach(FuncTemplate f)
	{
		// If _functTemplates is empty, create it and add the FuncTemplate f in it
		if (_funcTemplates == null)
		{
			_funcTemplates = new FuncTemplate[]
			{
				f
			};
		}
		else
		{
			int len = _funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
			// number of components to be copied)
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}
	
	public void attach(EffectTemplate effect)
	{
		if (_effectTemplates == null)
		{
			_effectTemplates = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			int len = _effectTemplates.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
			// number of components to be copied)
			System.arraycopy(_effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplates = tmp;
		}
	}
	
	public void attach(L2Skill skill)
	{
		if (_skills == null)
		{
			_skills = new L2Skill[]
			{
				skill
			};
		}
		else
		{
			int len = _skills.length;
			L2Skill[] tmp = new L2Skill[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
			// number of components to be copied)
			System.arraycopy(_skills, 0, tmp, 0, len);
			tmp[len] = skill;
			_skills = tmp;
		}
	}
	
	public boolean isQuestItem()
	{
		return (getItemType() == L2EtcItemType.QUEST);
	}
	
	public final boolean isHeroItem()
	{
		return _heroItem;
	}
	
	public boolean isPetItem()
	{
		return (getItemType() == L2ArmorType.PET || getItemType() == L2WeaponType.PET);
	}
	
	public boolean isPotion()
	{
		return (getItemType() == L2EtcItemType.POTION);
	}
	
	@Override
	public String toString()
	{
		return _name;
	}
	
	public boolean checkCondition(L2Character activeChar, L2Object target)
	{
		List<Integer> items = Config.OLY_RESTRICTED_ITEMS_LIST;
		
		for (L2ItemInstance i : activeChar.getActingPlayer().getInventory().getItems())
		{			
			if (items.get(0).equals(i.getItemId()) || isHeroItem() && ((activeChar instanceof L2PcInstance) && activeChar.getActingPlayer().isInOlympiadMode()))
			{				
				activeChar.getActingPlayer().sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
				return false;
			}		
		}
		return true;
	}
	
	public void addQuestEvent(Quest q)
	{
		_questEvents.add(q);
	}
	
	public List<Quest> getQuestEvents()
	{
		return _questEvents;
	}
}