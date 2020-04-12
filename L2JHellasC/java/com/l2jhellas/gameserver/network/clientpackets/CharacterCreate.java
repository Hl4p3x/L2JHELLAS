package com.l2jhellas.gameserver.network.clientpackets;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.CharNameTable;
import com.l2jhellas.gameserver.datatables.xml.CharTemplateData;
import com.l2jhellas.gameserver.datatables.xml.SkillTreeData;
import com.l2jhellas.gameserver.enums.Sex;
import com.l2jhellas.gameserver.enums.player.PlayerCreateFailReason;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.instancemanager.QuestManager;
import com.l2jhellas.gameserver.model.L2ShortCut;
import com.l2jhellas.gameserver.model.L2SkillLearn;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.base.Experience;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.network.L2GameClient;
import com.l2jhellas.gameserver.network.serverpackets.CharCreateFail;
import com.l2jhellas.gameserver.network.serverpackets.CharCreateOk;
import com.l2jhellas.gameserver.network.serverpackets.CharSelectInfo;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.L2PcTemplate;
import com.l2jhellas.util.Util;

@SuppressWarnings("unused")
public final class CharacterCreate extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(CharacterCreate.class.getName());
	private static final String _C__0B_CHARACTERCREATE = "[C] 0B CharacterCreate";
	// cSdddddddddddd
	private String _name;
	private int _race;
	private byte _sex;
	private int _classId;
	private int _int;
	private int _str;
	private int _con;
	private int _men;
	private int _dex;
	private int _wit;
	private byte _hairStyle;
	private byte _hairColor;
	private byte _face;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
		_race = readD();
		_sex = (byte) readD();
		_classId = readD();
		_int = readD();
		_str = readD();
		_con = readD();
		_men = readD();
		_dex = readD();
		_wit = readD();
		_hairStyle = (byte) readD();
		_hairColor = (byte) readD();
		_face = (byte) readD();
	}
	
	@Override
	protected void runImpl()
	{		
		if ((_name.length() < 3) || (_name.length() > 16) || !Util.isAlphaNumeric(_name) || !isValidName(_name))
		{
			sendPacket(new CharCreateFail((_name.length() > 16) ? PlayerCreateFailReason.REASON_16_ENG_CHARS : PlayerCreateFailReason.REASON_INCORRECT_NAME));
			return;
		}
		
		if (_face > 2 || _face < 0 || _race > 4 || _race < 0)
		{
			sendPacket(new CharCreateFail(PlayerCreateFailReason.REASON_CREATION_FAILED));
			return;
		}
		
		if (_hairStyle < 0 || (_sex == 0 && _hairStyle > 4) || (_sex != 0 && _hairStyle > 6))
		{
			sendPacket(new CharCreateFail(PlayerCreateFailReason.REASON_CREATION_FAILED));
			return;
		}
		
		if (_hairColor > 3 || _hairColor < 0)
		{
			sendPacket(new CharCreateFail(PlayerCreateFailReason.REASON_CREATION_FAILED));
			return;
		}
		
		if (CharNameTable.getInstance().accountCharNumber(getClient().getAccountName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT && Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0)
		{
			sendPacket(new CharCreateFail(PlayerCreateFailReason.REASON_TOO_MANY_CHARACTERS));
			return;
		}
		
		if (CharNameTable.getInstance().doesCharNameExist(_name))
		{
			sendPacket(new CharCreateFail(PlayerCreateFailReason.REASON_NAME_ALREADY_EXISTS));
			return;
		}

		final L2PcTemplate template = CharTemplateData.getInstance().getTemplate(_classId);
		
		if (template == null || template.classBaseLevel > 1)
		{
			sendPacket(new CharCreateFail(PlayerCreateFailReason.REASON_CREATION_FAILED));
			return;
		}
		
		int objectId = IdFactory.getInstance().getNextId();
		
		final L2PcInstance newChar = L2PcInstance.create(objectId, template, getClient().getAccountName(), _name, _hairStyle, _hairColor, _face, Sex.values()[_sex]);
		
		if (newChar == null)
		{
			sendPacket(new CharCreateFail(PlayerCreateFailReason.REASON_CREATION_FAILED));
			return;
		}
		
		// set hp/mp/cp
		newChar.setCurrentCp(0);
		newChar.setCurrentHp(newChar.getMaxHp());
		newChar.setCurrentMp(newChar.getMaxMp());
		
		// send acknowledgment
		CharCreateOk cco = new CharCreateOk();
		sendPacket(cco);
		
		initNewChar(getClient(), newChar);
	}
	
	private static boolean isValidName(String text)
	{
		boolean result = true;
		String test = text;
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.CNAME_TEMPLATE);
		}
		catch (PatternSyntaxException e) // case of illegal pattern
		{
			_log.warning(CharacterCreate.class.getName() + ": ERROR : Character name pattern of config is wrong!using default .*");
			pattern = Pattern.compile(".*");
		}
		Matcher regexp = pattern.matcher(test);
		if (!regexp.matches())
		{
			result = false;
		}
		return result;
	}
	
	private void initNewChar(L2GameClient client, L2PcInstance newChar)
	{
		L2World.getInstance().storeObject(newChar);
		
		final L2PcTemplate template = newChar.getTemplate();
		
		newChar.addAdena("Init", Config.STARTING_ADENA, null, false);
		newChar.addAncientAdena("Init", Config.STARTING_ANCIENT, null, false);
		
		newChar.getAppearance().setInvisible();
		newChar.getPosition().setXYZ(template.spawnX, template.spawnY, template.spawnZ);

		if (Config.ALLOW_CREATE_LVL)
		{
			long tXp = Experience.LEVEL[Config.CUSTOM_START_LVL];
			newChar.addExpAndSp(tXp, 0);
		}
		if (Config.CHAR_TITLE)
		{
			newChar.setTitle(Config.ADD_CHAR_TITLE);
		}
		
		newChar.registerShortCut(new L2ShortCut(0, 0, 3, 2, -1, 1));
		newChar.registerShortCut(new L2ShortCut(3, 0, 3, 5, -1, 1));
		newChar.registerShortCut(new L2ShortCut(10, 0, 3, 0, -1, 1));
		
		L2Item[] items = template.getItems();
		for (L2Item item2 : items)
		{
			L2ItemInstance item = newChar.getInventory().addItem("Init", item2.getItemId(), 1, newChar, null);
			
			if (item.getItemId() == 5588)
				newChar.registerShortCut(new L2ShortCut(11, 0, 1, item.getObjectId(), -1, 1));
			
			if (item.isEquipable())
			{
				if (newChar.getActiveWeaponItem() == null || !(item.getItem().getType2() != L2Item.TYPE2_WEAPON))
					newChar.getInventory().equipItemAndRecord(item);
			}
		}
		
		L2SkillLearn[] startSkills = SkillTreeData.getInstance().getAvailableSkills(newChar, newChar.getClassId());
		for (L2SkillLearn startSkill : startSkills)
		{
			newChar.addSkill(SkillTable.getInstance().getInfo(startSkill.getId(), startSkill.getLevel()), true);
			if (startSkill.getId() == 1001 || startSkill.getId() == 1177)
				newChar.registerShortCut(new L2ShortCut(1, 0, 2, startSkill.getId(), 1, 1));
			if (startSkill.getId() == 1216)
				newChar.registerShortCut(new L2ShortCut(10, 0, 2, startSkill.getId(), 1, 1));
		}
		
		if (Config.ALLOW_TUTORIAL)
			startTutorialQuest(newChar);
		
		newChar.store();
		newChar.deleteMe(); // release the world of this character and it's inventory
		
		// send char list
		CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1);
		client.getConnection().sendPacket(cl);
		getClient().setCharSelectSlot(cl.getCharacterSlots());
	}
	
	public static void startTutorialQuest(L2PcInstance player)
	{
		if (player.getQuestState("Q255_Tutorial") == null)
		{
			final Quest quest = QuestManager.getInstance().getQuest(255);
			if (quest != null)
				quest.newQuestState(player).setState(Quest.STATE_STARTED);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__0B_CHARACTERCREATE;
	}
}