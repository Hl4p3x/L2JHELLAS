package com.l2jhellas.gameserver.model.actor.instance;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Extensions.IpCatcher;
import Extensions.AchievmentsEngine.AchievementsManager;
import Extensions.RankSystem.RPSCookie;
import Extensions.RankSystem.RankPvpSystem;

import com.PackRoot;
import com.l2jhellas.Config;
import com.l2jhellas.gameserver.Announcements;
import com.l2jhellas.gameserver.ItemsAutoDestroy;
import com.l2jhellas.gameserver.LoginServerThread;
import com.l2jhellas.gameserver.SevenSigns;
import com.l2jhellas.gameserver.SevenSignsFestival;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.ai.CtrlEvent;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.ai.L2CharacterAI;
import com.l2jhellas.gameserver.ai.L2PlayerAI;
import com.l2jhellas.gameserver.ai.NextAction;
import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.cache.WarehouseCache;
import com.l2jhellas.gameserver.communitybbs.BB.Forum;
import com.l2jhellas.gameserver.communitybbs.Manager.ForumsBBSManager;
import com.l2jhellas.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jhellas.gameserver.controllers.GameTimeController;
import com.l2jhellas.gameserver.controllers.RecipeController;
import com.l2jhellas.gameserver.datatables.sql.CharNameTable;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.datatables.xml.AdminData;
import com.l2jhellas.gameserver.datatables.xml.CharTemplateData;
import com.l2jhellas.gameserver.datatables.xml.FishTable;
import com.l2jhellas.gameserver.datatables.xml.HennaData;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.datatables.xml.RecipeData;
import com.l2jhellas.gameserver.datatables.xml.SkillTreeData;
import com.l2jhellas.gameserver.enums.PolyType;
import com.l2jhellas.gameserver.enums.Sex;
import com.l2jhellas.gameserver.enums.Team;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.enums.items.L2ArmorType;
import com.l2jhellas.gameserver.enums.items.L2EtcItemType;
import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.enums.player.ClassId;
import com.l2jhellas.gameserver.enums.player.ClassRace;
import com.l2jhellas.gameserver.enums.player.ClassType;
import com.l2jhellas.gameserver.enums.player.DuelState;
import com.l2jhellas.gameserver.enums.player.PartyLootType;
import com.l2jhellas.gameserver.enums.player.PlayerExpLost;
import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.enums.skills.HeroSkills;
import com.l2jhellas.gameserver.enums.skills.L2SkillTargetType;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.enums.skills.NobleSkills;
import com.l2jhellas.gameserver.enums.sound.Music;
import com.l2jhellas.gameserver.enums.sound.Sound;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.geometry.Point3D;
import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.handler.ItemHandler;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.ClanHallManager;
import com.l2jhellas.gameserver.instancemanager.CoupleManager;
import com.l2jhellas.gameserver.instancemanager.CrownManager;
import com.l2jhellas.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jhellas.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jhellas.gameserver.instancemanager.DuelManager;
import com.l2jhellas.gameserver.instancemanager.GrandBossManager;
import com.l2jhellas.gameserver.instancemanager.ItemsOnGroundManager;
import com.l2jhellas.gameserver.instancemanager.QuestManager;
import com.l2jhellas.gameserver.instancemanager.SiegeManager;
import com.l2jhellas.gameserver.instancemanager.ZoneManager;
import com.l2jhellas.gameserver.model.BlockList;
import com.l2jhellas.gameserver.model.Couple;
import com.l2jhellas.gameserver.model.FishData;
import com.l2jhellas.gameserver.model.ForceBuff;
import com.l2jhellas.gameserver.model.L2AccessLevel;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2Clan.SubPledge;
import com.l2jhellas.gameserver.model.L2ClanMember;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Fishing;
import com.l2jhellas.gameserver.model.L2Macro;
import com.l2jhellas.gameserver.model.L2ManufactureList;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Radar;
import com.l2jhellas.gameserver.model.L2RecipeList;
import com.l2jhellas.gameserver.model.L2Request;
import com.l2jhellas.gameserver.model.L2ShortCut;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2SkillLearn;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.MacroList;
import com.l2jhellas.gameserver.model.PcFreight;
import com.l2jhellas.gameserver.model.PcInventory;
import com.l2jhellas.gameserver.model.PcWarehouse;
import com.l2jhellas.gameserver.model.PetInventory;
import com.l2jhellas.gameserver.model.ShortCuts;
import com.l2jhellas.gameserver.model.TradeList;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.L2Vehicle;
import com.l2jhellas.gameserver.model.actor.appearance.PcAppearance;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoom;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoomList;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchWaitingList;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.model.actor.item.ItemContainer;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.actor.stat.PcStat;
import com.l2jhellas.gameserver.model.actor.status.PcStatus;
import com.l2jhellas.gameserver.model.base.Experience;
import com.l2jhellas.gameserver.model.base.SubClass;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.model.entity.ClanHall;
import com.l2jhellas.gameserver.model.entity.Hero;
import com.l2jhellas.gameserver.model.entity.Siege;
import com.l2jhellas.gameserver.model.entity.events.CTF;
import com.l2jhellas.gameserver.model.entity.events.DM;
import com.l2jhellas.gameserver.model.entity.events.TvT;
import com.l2jhellas.gameserver.model.entity.events.engines.L2Event;
import com.l2jhellas.gameserver.model.entity.events.engines.ZodiacMain;
import com.l2jhellas.gameserver.model.entity.olympiad.Olympiad;
import com.l2jhellas.gameserver.model.entity.olympiad.OlympiadGameManager;
import com.l2jhellas.gameserver.model.entity.olympiad.OlympiadGameTask;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.gameserver.model.zone.type.L2BossZone;
import com.l2jhellas.gameserver.network.L2GameClient;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.AbstractNpcInfo.PolymorphInfo;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.ChairSit;
import com.l2jhellas.gameserver.network.serverpackets.ChangeWaitType;
import com.l2jhellas.gameserver.network.serverpackets.CharInfo;
import com.l2jhellas.gameserver.network.serverpackets.ConfirmDlg;
import com.l2jhellas.gameserver.network.serverpackets.DeleteObject;
import com.l2jhellas.gameserver.network.serverpackets.Die;
import com.l2jhellas.gameserver.network.serverpackets.EnchantResult;
import com.l2jhellas.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jhellas.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import com.l2jhellas.gameserver.network.serverpackets.ExFishingEnd;
import com.l2jhellas.gameserver.network.serverpackets.ExFishingStart;
import com.l2jhellas.gameserver.network.serverpackets.ExOlympiadMode;
import com.l2jhellas.gameserver.network.serverpackets.ExOlympiadUserInfo;
import com.l2jhellas.gameserver.network.serverpackets.ExSetCompassZoneCode;
import com.l2jhellas.gameserver.network.serverpackets.ExStorageMaxCount;
import com.l2jhellas.gameserver.network.serverpackets.FinishRotation;
import com.l2jhellas.gameserver.network.serverpackets.FriendList;
import com.l2jhellas.gameserver.network.serverpackets.GetOnVehicle;
import com.l2jhellas.gameserver.network.serverpackets.HennaInfo;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jhellas.gameserver.network.serverpackets.LeaveWorld;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillCanceld;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.MoveToPawn;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.network.serverpackets.ObservationMode;
import com.l2jhellas.gameserver.network.serverpackets.ObservationReturn;
import com.l2jhellas.gameserver.network.serverpackets.PartySmallWindowUpdate;
import com.l2jhellas.gameserver.network.serverpackets.PetInventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.PlaySound;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowMemberListAll;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jhellas.gameserver.network.serverpackets.PledgeSkillList;
import com.l2jhellas.gameserver.network.serverpackets.PledgeStatusChanged;
import com.l2jhellas.gameserver.network.serverpackets.PrivateStoreListBuy;
import com.l2jhellas.gameserver.network.serverpackets.PrivateStoreListSell;
import com.l2jhellas.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import com.l2jhellas.gameserver.network.serverpackets.PrivateStoreManageListSell;
import com.l2jhellas.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import com.l2jhellas.gameserver.network.serverpackets.PrivateStoreMsgSell;
import com.l2jhellas.gameserver.network.serverpackets.QuestList;
import com.l2jhellas.gameserver.network.serverpackets.RecipeShopManageList;
import com.l2jhellas.gameserver.network.serverpackets.RecipeShopMsg;
import com.l2jhellas.gameserver.network.serverpackets.RecipeShopSellList;
import com.l2jhellas.gameserver.network.serverpackets.RelationChanged;
import com.l2jhellas.gameserver.network.serverpackets.Revive;
import com.l2jhellas.gameserver.network.serverpackets.Ride;
import com.l2jhellas.gameserver.network.serverpackets.SendTradeDone;
import com.l2jhellas.gameserver.network.serverpackets.ServerClose;
import com.l2jhellas.gameserver.network.serverpackets.SetupGauge;
import com.l2jhellas.gameserver.network.serverpackets.ShortCutInit;
import com.l2jhellas.gameserver.network.serverpackets.ShortCutRegister;
import com.l2jhellas.gameserver.network.serverpackets.SignsSky;
import com.l2jhellas.gameserver.network.serverpackets.SkillCoolTime;
import com.l2jhellas.gameserver.network.serverpackets.SkillList;
import com.l2jhellas.gameserver.network.serverpackets.Snoop;
import com.l2jhellas.gameserver.network.serverpackets.SocialAction;
import com.l2jhellas.gameserver.network.serverpackets.StaticObject;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.StopMove;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.network.serverpackets.TargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.TargetUnselected;
import com.l2jhellas.gameserver.network.serverpackets.TitleUpdate;
import com.l2jhellas.gameserver.network.serverpackets.TradePressOtherOk;
import com.l2jhellas.gameserver.network.serverpackets.TradePressOwnOk;
import com.l2jhellas.gameserver.network.serverpackets.TradeStart;
import com.l2jhellas.gameserver.network.serverpackets.UserInfo;
import com.l2jhellas.gameserver.network.serverpackets.ValidateLocation;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.effects.EffectTemplate;
import com.l2jhellas.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jhellas.gameserver.taskmanager.PvpFlagTaskManager;
import com.l2jhellas.gameserver.taskmanager.WaterTaskManager;
import com.l2jhellas.gameserver.templates.L2Armor;
import com.l2jhellas.gameserver.templates.L2Henna;
import com.l2jhellas.gameserver.templates.L2PcTemplate;
import com.l2jhellas.gameserver.templates.L2Weapon;
import com.l2jhellas.shield.antibot.AntiBot;
import com.l2jhellas.util.Broadcast;
import com.l2jhellas.util.IllegalPlayerAction;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class L2PcInstance extends L2Playable
{
	protected static Logger _log = Logger.getLogger(L2PcInstance.class.getName());
	
	// Character Skills
	private static final String RESTORE_SKILLS_FOR_CHAR = "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?";
	private static final String RESTORE_SKILLS_FOR_CHAR_ALT_SUBCLASS = "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=?";
	private static final String ADD_NEW_SKILL = "INSERT INTO character_skills (char_obj_id,skill_id,skill_level,skill_name,class_index) VALUES (?,?,?,?,?)";
	private static final String UPDATE_CHARACTER_SKILL_LEVEL = "UPDATE character_skills SET skill_level=? WHERE skill_id=? AND char_obj_id=? AND class_index=?";
	private static final String DELETE_SKILL_FROM_CHAR = "DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?";
	private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?";
	private static final String ADD_SKILL_SAVE = "INSERT INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)";
	private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime FROM character_skills_save WHERE char_obj_id=? AND class_index=? AND restore_type=? ORDER BY buff_index ASC";
	private static final String DELETE_SKILL_SAVE = "DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?";
	
	// Character Character
	private static final String UPDATE_CHARACTER = "UPDATE characters SET level=?, maxHp=?, curHp=?, maxCp=?, curCp=?, maxMp=?, curMp=?, str=?, con=?, dex=?, _int=?, men=?, wit=?, face=?, hairStyle=?, hairColor=?, heading=?, x=?, y=?, z=?, exp=?, expBeforeDeath=?, sp=?, karma=?, pvpkills=?, pkkills=?, rec_have=?, rec_left=?, clanid=?, maxload=?, race=?, classid=?, deletetime=?, title=?, accesslevel=?, online=?, isin7sdungeon=?, clan_privs=?, wantspeace=?, base_class=?, onlinetime=?, in_jail=?, jail_timer=?, newbie=?, nobless=?, power_grade=?, subpledge=?, last_recom_date=?, lvl_joined_academy=?, apprentice=?, sponsor=?, varka_ketra_ally=?, clan_join_expiry_time=?, clan_create_expiry_time=?, char_name=?, death_penalty_level=?, chat_filter_count=? WHERE obj_id=?";
	private static final String RESTORE_CHARACTER = "SELECT account_name, obj_Id, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, runSpd, walkSpd, str, con, dex, _int, men, wit, face, hairStyle, hairColor, sex, heading, x, y, z, movement_multiplier, attack_speed_multiplier, colRad, colHeight, exp, expBeforeDeath, sp, karma, pvpkills, pkkills, clanid, maxload, race, classid, deletetime, cancraft, title, rec_have, rec_left, accesslevel, online, char_slot, lastAccess, clan_privs, wantspeace, base_class, onlinetime, isin7sdungeon, in_jail, jail_timer, newbie, nobless, power_grade, subpledge, last_recom_date, lvl_joined_academy, apprentice, sponsor, varka_ketra_ally, clan_join_expiry_time, clan_create_expiry_time, death_penalty_level, hero, donator, chatban_timer, chatban_reason, chat_filter_count FROM characters WHERE obj_Id=?";
	private static final String RESTORE_CHAR_SUBCLASSES = "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE char_obj_id=? ORDER BY class_index ASC";
	private static final String INSERT_CHARACTER = "INSERT INTO characters (account_name,obj_Id,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,acc,crit,evasion,mAtk,mDef,mSpd,pAtk,pDef,pSpd,runSpd,walkSpd,str,con,dex,_int,men,wit,face,hairStyle,hairColor,sex,movement_multiplier,attack_speed_multiplier,colRad,colHeight,exp,sp,karma,pvpkills,pkkills,clanid,maxload,race,classid,deletetime,cancraft,title,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,newbie,nobless,power_grade,last_recom_date) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	// Character PremiumService
	private static final String INSERT_PREMIUMSERVICE = "INSERT INTO account_premium (account_name,premium_service,enddate) values(?,?,?) ON DUPLICATE KEY UPDATE premium_service = ?, enddate = ?";
	private static final String RESTORE_PREMIUMSERVICE = "SELECT premium_service,enddate FROM account_premium WHERE account_name=?";
	private static final String UPDATE_PREMIUMSERVICE = "UPDATE account_premium SET premium_service=?,enddate=? WHERE account_name=?";
	
	// Character Ban chat
	private static final String BAN_CHAT_SET = "UPDATE characters SET chatban_timer=?, chatban_reason=? WHERE char_name LIKE ?";
	private static final String BAN_CHAT_GET = "SELECT chatban_timer, chatban_reason FROM characters WHERE char_name LIKE ?";
	
	// Character Subclasses
	private static final String ADD_CHAR_SUBCLASS = "INSERT INTO character_subclasses (char_obj_id,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
	private static final String UPDATE_CHAR_SUBCLASS = "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE char_obj_id=? AND class_index =?";
	private static final String DELETE_CHAR_SUBCLASS = "DELETE FROM character_subclasses WHERE char_obj_id=? AND class_index=?";
	
	// Character Hennas
	private static final String RESTORE_CHAR_HENNAS = "SELECT slot,symbol_id FROM character_hennas WHERE char_obj_id=? AND class_index=?";
	private static final String ADD_CHAR_HENNA = "INSERT INTO character_hennas (char_obj_id,symbol_id,slot,class_index) VALUES (?,?,?,?)";
	private static final String DELETE_CHAR_HENNA = "DELETE FROM character_hennas WHERE char_obj_id=? AND slot=? AND class_index=?";
	private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?";
	private static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?";
	
	// Character Recommendations
	private static final String RESTORE_CHAR_RECOMS = "SELECT char_id,target_id FROM character_recommends WHERE char_id=?";
	private static final String ADD_CHAR_RECOM = "INSERT INTO character_recommends (char_id,target_id) VALUES (?,?)";
	private static final String DELETE_CHAR_RECOMS = "DELETE FROM character_recommends WHERE char_id=?";
	
	public static final int REQUEST_TIMEOUT = 15;
	
	public static final int STORE_PRIVATE_NONE = 0;
	public static final int STORE_PRIVATE_SELL = 1;
	public static final int STORE_PRIVATE_BUY = 3;
	public static final int STORE_PRIVATE_MANUFACTURE = 5;
	public static final int STORE_PRIVATE_PACKAGE_SELL = 8;
	
	private static final int[] EXPERTISE_LEVELS =
	{
		SkillTreeData.getInstance().getExpertiseLevel(0), // NONE
		SkillTreeData.getInstance().getExpertiseLevel(1), // D
		SkillTreeData.getInstance().getExpertiseLevel(2), // C
		SkillTreeData.getInstance().getExpertiseLevel(3), // B
		SkillTreeData.getInstance().getExpertiseLevel(4), // A
		SkillTreeData.getInstance().getExpertiseLevel(5), // S
	};
	
	private static final int[] COMMON_CRAFT_LEVELS =
	{
		5,
		20,
		28,
		36,
		43,
		49,
		55,
		62
	};
		
	protected boolean sittingTaskLaunched;
	protected boolean _inventoryDisable = false;

	public boolean PassedProt;
	public boolean isinZodiac = false;
	public boolean _exploring = false;
	public boolean _allowTrade = true;

	private boolean _isStored = false;
	private boolean _isInDuel = false;
	private boolean _posticipateSit;
	private boolean _isOnline = false;
	private boolean _isIn7sDungeon = false;
	private boolean _InvullBuffs = false;	
	private boolean _inJail = false;			
	private boolean _isSilentMoving = false;	
	private boolean _inCrystallize;	
	private boolean _inCraftMode;	
	private boolean _inOlympiadMode = false;
	private boolean _OlympiadStart = false;	
	private boolean _newbie;	
	private boolean _noble = false;
	private boolean _hero = false;	
	private boolean _waitTypeSitting;	
	private boolean _relax;	
	private boolean _messageRefusal = false; // message refusal mode
	private boolean _dietMode = false; // ignore weight penalty
	private boolean _tradeRefusal = false; // Trade refusal
	private boolean _exchangeRefusal = false; // Exchange refusal
	private boolean _observerMode = false;				
	private boolean _IsWearingFormalWear = false;
	private boolean _revivePet = false;
	
	public int tempAc = 0;
	public int botx, boty, botz;
	public int ZodiacPoints;
	public int CountIps;
	public int OriginalColor;	
	public int _telemode = 0;
	
	private int _herbstask = 0;
	private int _recomHave; // how much I was recommended by others	
	private int _recomLeft; // how many recommendations I can give to others	
	private int _karma=0;	
	private int _pvpKills = 0;
	private int _pkKills = 0;	
	private int _charId = 0x00030b7a;
	private int _curWeightPenalty = 0;	
	private int _lastCompassZone; // the last compass zone update send to the client
	private int _olympiadGameId = -1;
	private int _olympiadSide = -1;		
	private int _mountType;	
	private int _mountObjectID = 0;
	private int _duelId = 0;
	private int _questNpcObject = 0;
	private int _clanId;	
	private int _apprentice = 0;
	private int _sponsor = 0;
	private int _powerGrade = 0;
	private int _clanPrivileges = 0;	
	private int _pledgeClass = 0;
	private int _pledgeType = 0;	
	private int _lvlJoinedAcademy = 0;	
	private int _wantsPeace = 0;
	private int _deathPenaltyBuffLevel = 0;	
	private int _expertiseIndex; // index in EXPERTISE_LEVELS
	private int _expertisePenalty = 0;
	private int _cursedWeaponEquipedId = 0;
	private int _reviveRequested = 0;
	private int _alliedVarkaKetra = 0;

	// hennas
	private final L2Henna[] _henna = new L2Henna[3];
	private int _hennaSTR;
	private int _hennaINT;
	private int _hennaDEX;
	private int _hennaMEN;
	private int _hennaWIT;
	private int _hennaCON;

	protected int _baseClass;
	protected int _activeClass;
	protected int _classIndex = 0;
	
	private double _revivePower = 0;
	private double _cpUpdateIncCheck = .0;
	private double _cpUpdateDecCheck = .0;
	private double _cpUpdateInterval = .0;
	private double _mpUpdateIncCheck = .0;
	private double _mpUpdateDecCheck = .0;
	private double _mpUpdateInterval = .0;

	private long _clanJoinExpiryTime;
	private long _clanCreateExpiryTime;
	private long _lastRecomUpdate;
	private long _deleteTimer;	
	private long _onlineTime;
	private long _onlineBeginTime;
	private long _lastAccess;
	private long _uptime;
	private long _expBeforeDeath;
	private long _protectEndTime = 0;
	private long _recentFakeDeathEndTime = 0;
	
	private byte _pvpFlag;	
	private byte _siegeState = 0;
	
	public String OriginalTitle;
	private String _accountName;
		
	protected L2GameClient _client;

	private L2Clan _clan;
	private PcAppearance _appearance;
				
	private long _jailTimer = 0;
	private ScheduledFuture<?> _jailTask;
	
	private PunishLevel _punishLevel = PunishLevel.NONE; // TODO Clean up, delete old methods and add this one...
	private long _punishTimer = 0;
	private ScheduledFuture<?> _punishTask;

	private DuelState _duelState = DuelState.NO_DUEL;
	private SystemMessageId _noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
	
	private final RPSCookie _RPSCookie = new RPSCookie();	
	public RPSCookie getRPSCookie()
	{
		return _RPSCookie;
	}
	
	private Point3D _inBoatPosition;
	private Point3D _currentSkillWorldPosition;
	private ClassId _skillLearningClassId;
	private L2Vehicle _vehicle = null;
	private PcWarehouse _warehouse;
	private L2NpcInstance _lastFolkNpc = null;
	private L2Summon _summon = null;
	private L2TamedBeastInstance _tamedBeast = null;
	private L2Radar _radar;	
	private L2AccessLevel _accessLevel;
	private L2Weapon _fistsWeaponItem;
	private L2Party _party;
	private L2ItemInstance _arrowItem;
	
	private Map<Integer, SubClass> _subClasses;
	private final Map<Integer, String> _chars = new HashMap<>();
	private final Map<Integer, L2RecipeList> _dwarvenRecipeBook = new HashMap<>();
	private final Map<Integer, L2RecipeList> _commonRecipeBook = new HashMap<>();
	
	protected Map<Integer, L2CubicInstance> _cubics = new HashMap<>();
	protected Map<Integer, Integer> _activeSoulShots = new HashMap<>();

	private final Location _obsLocation = new Location(0, 0, 0);	
	private final Point3D _lastClientPosition = new Point3D(0, 0, 0);
	private final Point3D _lastServerPosition = new Point3D(0, 0, 0);

	private final List<Integer> _recomChars = new ArrayList<>();
		
	private final PcInventory _inventory = new PcInventory(this);
	private final PcFreight _freight = new PcFreight(this);
	private StoreType _privatestore = StoreType.NONE;
	private final BlockList _blockList = new BlockList(this);

	private TradeList _activeTradeList;
	private ItemContainer _activeWarehouse;
	private L2ManufactureList _createList;
	private TradeList _sellList;
	private TradeList _buyList;

	private final List<QuestState> _quests = new ArrayList<>();
	private final List<QuestState> _notifyQuestOfDeathList = new ArrayList<>();
	
	private final ShortCuts _shortCuts = new ShortCuts(this);	
	private final MacroList _macroses = new MacroList(this);
	
	private final List<L2PcInstance> _snoopListener = new ArrayList<>();
	private final List<L2PcInstance> _snoopedPlayer = new ArrayList<>();
	
	// Chat ban
	private boolean _chatBanned = false; // Chat Banned
	private long _banchat_timer = 0;
	private ScheduledFuture<?> _BanChatTask;
	
	// this is needed to find the inviting player for Party response
	// there can only be one active party request at once
	private L2PcInstance _activeRequester;
	private long _requestExpireTime = 0;
	private final L2Request _request = new L2Request(this);

	private boolean _isEnchanting = false;
	private L2ItemInstance _activeEnchantItem = null;
			
	public final ReentrantLock soulShotLock = new ReentrantLock();

	public int eventX;
	public int eventY;
	public int eventZ;
	public int eventkarma;
	public int eventpvpkills;
	public int eventpkkills;
	public String eventTitle;
	public LinkedList<String> kills = new LinkedList<>();
	public boolean eventSitForced = false;
	public boolean atEvent = false;
	
	public String _teamNameTvT, _originalTitleTvT;
	public int _originalNameColorTvT, _countTvTkills, _countTvTdies, _originalKarmaTvT;
	public boolean _inEventTvT = false;
	
	public String _teamNameCTF, _teamNameHaveFlagCTF, _originalTitleCTF;
	public int _originalNameColorCTF, _originalKarmaCTF, _countCTFflags;
	public boolean _inEventCTF = false, _haveFlagCTF = false;
	public Future<?> _posCheckerCTF = null;
	
	public boolean _isVIP = false, _inEventVIP = false, _isNotVIP = false, _isTheVIP = false;
	public int _originalNameColourVIP, _originalKarmaVIP;
	
	public boolean _voteRestart = false;
	
	public int _originalNameColorDM, _countDMkills, _originalKarmaDM;
	public boolean _inEventDM = false;
	
	private final int _loto[] = new int[5];
	// public static int _loto_nums[] = {0,1,2,3,4,5,6,7,8,9,};
	
	private final int _race[] = new int[2];
		
	private Team _team = Team.NONE;
		
	private L2Fishing _fishCombat;
	private boolean _fishing = false;
	private int _fishx = 0;
	private int _fishy = 0;
	private int _fishz = 0;
	
	private ScheduledFuture<?> _taskRentPet;
	
	private final List<Integer> _friendList = new ArrayList<>();
	
	private final List<String> _validBypass = new ArrayList<>();
	private final List<String> _validBypass2 = new ArrayList<>();
	
	private Forum _forumMail;
	private Forum _forumMemo;
	
	private SkillDat _currentSkill;
	private SkillDat _currentPetSkill;
	private SkillDat _queuedSkill;

		public L2PcInstance getPlayer()
		{
			return L2PcInstance.this;
		}

		@Override
		public void doAttack(L2Character target)
		{
			
			if (isInsidePeaceZone(L2PcInstance.this, target))
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_ATK_PEACEZONE));
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			super.doAttack(target);
			
			// cancel the recent fake-death protection instantly if the player
			// attacks or casts spells
			getPlayer().setRecentFakeDeath(false);
			
			if (getCubics() != null)
			{
				for (L2CubicInstance cubic : getCubics().values())
				{
					if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
						cubic.doAction(target);
				}
			}
		}
		
		@Override
		public void doCast(L2Skill skill)
		{
			super.doCast(skill);
			
			// cancel the recent fake-death protection instantly if the player attacks or casts spells
			getPlayer().setRecentFakeDeath(false);
			if (skill == null)
				return;
			if (!skill.isOffensive())
				return;
			
			switch (skill.getTargetType())
			{
				case TARGET_SIGNET_GROUND:
				case TARGET_SIGNET:
					return;
					
				default:
				{
					L2Object mainTarget = skill.getFirstOfTargetList(L2PcInstance.this);
					if ((mainTarget == null) || !(mainTarget instanceof L2Character))
						return;
					
					if (getCubics() != null)
					{
						for (L2CubicInstance cubic : getCubics().values())
						{
							if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
								cubic.doAction((L2Character) mainTarget);
						}
					}
				}
					break;
			}
		}
	
	
	@Override
	public void startForceBuff(L2Character target, L2Skill skill)
	{
		if (!(target instanceof L2PcInstance))
			return;
		
		if (skill.getSkillType() != L2SkillType.FORCE_BUFF)
			return;
		
		if (_forceBuff == null)
			_forceBuff = new ForceBuff(this, (L2PcInstance) target, skill);
	}
	
	public enum PunishLevel
	{
		NONE(0, ""),
		CHAT(1, "chat banned"),
		JAIL(2, "jailed"),
		CHAR(3, "banned"),
		ACC(4, "banned");
		
		private int punValue;
		private String punString;
		
		PunishLevel(int value, String string)
		{
			punValue = value;
			punString = string;
		}
		
		public int value()
		{
			return punValue;
		}
		
		public String string()
		{
			return punString;
		}
	}

	private void createPSdb()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(INSERT_PREMIUMSERVICE);
			statement.setString(1, _accountName);
			statement.setInt(2, 0);
			statement.setLong(3, 0);
			statement.setInt(4, 0);
			statement.setLong(5, 0);
			statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": Could not insert char data: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
			return;
		}
		
	}
	
	private static void PStimeOver(String account)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(UPDATE_PREMIUMSERVICE);
			statement.setInt(1, 0);
			statement.setLong(2, 0);
			statement.setString(3, account);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getSimpleName() + ": L2PcInstance: Premium Service Could not increase data.");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private static void restorePremServiceData(L2PcInstance player, String account)
	{
		boolean sucess = false;
		if (Config.USE_PREMIUMSERVICE)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement(RESTORE_PREMIUMSERVICE);
				statement.setString(1, account);
				try (ResultSet rset = statement.executeQuery())
				{
					while (rset.next())
					{
						if (rset.getLong("enddate") <= System.currentTimeMillis())
						{
							PStimeOver(account);
							player.setPremiumService(0);
						}
						else
						{
							player.setPremiumService(rset.getInt("premium_service"));
							sucess = true;
						}
					}
					
					if (sucess == false)
					{
						player.createPSdb();
						player.setPremiumService(0);
					}
					
					rset.close();
					statement.close();
				}
			}
			catch (SQLException e)
			{
				_log.warning(L2PcInstance.class.getSimpleName() + ": : PremiumService: Could not restore PremiumService data for:" + account + ".");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
		}
		else
		{
			player.createPSdb();
			player.setPremiumService(0);
		}
	}
	
	public class HerbTask implements Runnable
	{
		private final String _process;
		private final int _itemId;
		private final int _count;
		private final L2Object _reference;
		private final boolean _sendMessage;
		
		HerbTask(String process, int itemId, int count, L2Object reference, boolean sendMessage)
		{
			_process = process;
			_itemId = itemId;
			_count = count;
			_reference = reference;
			_sendMessage = sendMessage;
		}
		
		@Override
		public void run()
		{
			try
			{
				addItem(_process, _itemId, _count, _reference, _sendMessage);
			}
			catch (Throwable t)
			{
			}
		}
	}
	
	// MOD Wedding
	private boolean _married = false;
	private int _partnerId = 0;
	private int _coupleId = 0;
	private boolean _engagerequest = false;
	private int _engageid = 0;
	private boolean _marryrequest = false;
	private boolean _marryaccepted = false;
	
	// summon friend
	
	private final SummonRequest _summonRequest = new SummonRequest();
	
	protected static class SummonRequest
	{	
		private L2PcInstance _target = null;	
		private L2Skill _skill = null;
		
		public void setTarget(L2PcInstance destination, L2Skill skill)
		{
			_target = destination;
			_skill = skill;
		}
		
		public L2PcInstance getTarget()
		{
			return _target;
		}
		
		public L2Skill getSkill()
		{
			return _skill;
		}
	}
	
	// Current force buff this caster is casting to a target
	protected ForceBuff _forceBuff;
	
	public class SkillDat
	{
		private final L2Skill _skill;
		private final boolean _ctrlPressed;
		private final boolean _shiftPressed;
		
		protected SkillDat(L2Skill skill, boolean ctrlPressed, boolean shiftPressed)
		{
			_skill = skill;
			_ctrlPressed = ctrlPressed;
			_shiftPressed = shiftPressed;
		}
		
		public boolean isCtrlPressed()
		{
			return _ctrlPressed;
		}
		
		public boolean isShiftPressed()
		{
			return _shiftPressed;
		}
		
		public L2Skill getSkill()
		{
			return _skill;
		}
		
		public int getSkillId()
		{
			return (getSkill() != null) ? getSkill().getId() : -1;
		}
	}
		
	public static L2PcInstance create(int objectId, L2PcTemplate template, String accountName, String name, byte hairStyle, byte hairColor, byte face, Sex sex)
	{
		// Create a new L2PcInstance with an account name
		PcAppearance app = new PcAppearance(face, hairColor, hairStyle, sex);
		L2PcInstance player = new L2PcInstance(objectId, template, accountName, app);
		
		// Set the name of the L2PcInstance
		player.setName(name);
		
		// Set the base class ID to that of the actual class ID.
		player.setBaseClass(player.getClassId());
		
		if (Config.ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE)
			player.setNewbie(true);
		
		// Add the player in the characters table of the database
		boolean ok = player.createDb();
		
		if (!ok)
			return null;
		
		return player;
	}
	
	public static L2PcInstance createDummyPlayer(int objectId, String name)
	{
		// Create a new L2PcInstance with an account name
		L2PcInstance player = new L2PcInstance(objectId);
		player.setName(name);	
		return player;
	}
	
	public String getAccountName()
	{
		return getClient().getAccountName();
	}
	
	public Map<Integer, String> getAccountChars()
	{
		return _chars;
	}
	
	public static L2PcInstance load(int objectId)
	{
		return restore(objectId);
	}
	
	private void initPcStatusUpdateValues()
	{
		_cpUpdateInterval = getMaxCp() / 352.0;
		_cpUpdateIncCheck = getMaxCp();
		_cpUpdateDecCheck = getMaxCp() - _cpUpdateInterval;
		_mpUpdateInterval = getMaxMp() / 352.0;
		_mpUpdateIncCheck = getMaxMp();
		_mpUpdateDecCheck = getMaxMp() - _mpUpdateInterval;
	}
	
	protected L2PcInstance(int objectId, L2PcTemplate template, String accountName, PcAppearance app)
	{
		super(objectId, template);
		getStat(); // init stats
		getStatus(); // init status
		super.initCharStatusUpdateValues();
		initPcStatusUpdateValues();
		
		_accountName = accountName;
		_appearance = app;
		
		// Create an AI
		_ai = new L2PlayerAI(this);
		
		// Create a L2Radar object
		_radar = new L2Radar(this);
		
		Formulas.addFuncsToNewPlayer(this);
		
		// Retrieve from the database all skills of this L2PcInstance and add
		// them to _skills
		// Retrieve from the database all items of this L2PcInstance and add
		// them to _inventory
		getInventory().restore();
		if (!Config.WAREHOUSE_CACHE)
		{
			getWarehouse();
		}
		getFreight().restore();
	}
	
	protected L2PcInstance(int objectId)
	{
		super(objectId, null);
		getStat(); // init stats
		getStatus(); // init status
		super.initCharStatusUpdateValues();
		initPcStatusUpdateValues();
	}
	
	@Override
	public final PcStat getStat()
	{
		if (super.getStat() == null || !(super.getStat() instanceof PcStat))
			setStat(new PcStat(this));

		return (PcStat) super.getStat();
	}
	
	@Override
	public final PcStatus getStatus()
	{
		if (super.getStatus() == null || !(super.getStatus() instanceof PcStatus))
			setStatus(new PcStatus(this));

		return (PcStatus) super.getStatus();
	}
	
	public final PcAppearance getAppearance()
	{
		return _appearance;
	}
	
	public final L2PcTemplate getBaseTemplate()
	{
		return CharTemplateData.getInstance().getTemplate(_baseClass);
	}
	
	@Override
	public final L2PcTemplate getTemplate()
	{
		return (L2PcTemplate) super.getTemplate();
	}
	
	public void setTemplate(ClassId newclass)
	{
		super.setTemplate(CharTemplateData.getInstance().getTemplate(newclass));
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
					_ai = new L2PlayerAI(this);
			}
		}
		
		return _ai;
	}
	
	@Override
	public final int getLevel()
	{
		return getStat().getLevel();
	}
	
	public boolean isNewbie()
	{
		return _newbie;
	}
	
	public void setNewbie(boolean isNewbie)
	{
		_newbie = isNewbie;
	}
	
	public void setBaseClass(int baseClass)
	{
		_baseClass = baseClass;
	}
	
	public void setBaseClass(ClassId classId)
	{
		_baseClass = classId.ordinal();
	}
	
	public boolean isInStoreMode()
	{
		return (getPrivateStoreType() != StoreType.NONE);
	}
	
	// public boolean isInCraftMode() { return (getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE); }
	
	public boolean isInCraftMode()
	{
		return _inCraftMode;
	}
	
	public void isInCraftMode(boolean b)
	{
		_inCraftMode = b;
	}
	
	public Collection<L2RecipeList> getCommonRecipeBook()
	{
		return _commonRecipeBook.values();
	}
	
	public Collection<L2RecipeList> getDwarvenRecipeBook()
	{
		return _dwarvenRecipeBook.values();
	}
	
	public void registerCommonRecipeList(L2RecipeList recipe)
	{
		_commonRecipeBook.put(recipe.getId(), recipe);
	}
	
	public void registerDwarvenRecipeList(L2RecipeList recipe)
	{
		_dwarvenRecipeBook.put(recipe.getId(), recipe);
	}
	
	public boolean hasRecipeList(int recipeId)
	{
		if (_dwarvenRecipeBook.containsKey(recipeId))
			return true;
		else if (_commonRecipeBook.containsKey(recipeId))
			return true;
		else
			return false;
	}
	
	public void unregisterRecipeList(int recipeId)
	{
		if (_dwarvenRecipeBook.containsKey(recipeId))
			_dwarvenRecipeBook.remove(recipeId);
		else if (_commonRecipeBook.containsKey(recipeId))
			_commonRecipeBook.remove(recipeId);
		
		L2ShortCut[] allShortCuts = getAllShortCuts();
		
		for (L2ShortCut sc : allShortCuts)
		{
			if (sc != null && sc.getId() == recipeId && sc.getType() == L2ShortCut.TYPE_RECIPE)
				deleteShortCut(sc.getSlot(), sc.getPage());
		}
	}
	
	public int getLastQuestNpcObject()
	{
		return _questNpcObject;
	}
	
	public void setLastQuestNpcObject(int npcId)
	{
		_questNpcObject = npcId;
	}
	
	@Override
	public void onActionShift(L2PcInstance player)
	{
		if (player.isGM())
			showCharacterInfo(player, this);
		
		super.onActionShift(player);
	}
	
	private static QuestState[] addToQuestStateArray(QuestState[] questStateArray, QuestState state)
	{
		int len = questStateArray.length;
		QuestState[] tmp = new QuestState[len + 1];
		for (int i = 0; i < len; i++)
		{
			tmp[i] = questStateArray[i];
		}
		tmp[len] = state;
		return tmp;
	}
	
	public Quest[] getAllActiveQuests(boolean completed)
	{
		ArrayList<Quest> quests = new ArrayList<>();
		
		for (QuestState qs : _quests)
		{
			if (qs == null || completed && qs.isCreated() || !completed && !qs.isStarted())
				continue;
			
			Quest quest = qs.getQuest();
			
			if (quest == null || !quest.isRealQuest())
				continue;
			
			quests.add(quest);
		}
		
		return quests.toArray(new Quest[quests.size()]);
	}
	
	public QuestState[] getQuestsForAttacks(L2Npc npc)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;
		
		// Go through the QuestState of the L2PcInstance quests
		for (Quest quest : npc.getTemplate().getEventQuests(QuestEventType.ON_ATTACK))
		{
			// Check if the Identifier of the L2Attackable attck is needed for the current quest
			if (getQuestState(quest.getName()) != null)
			{
				// Copy the current L2PcInstance QuestState in the QuestState table
				if (states == null)
				{
					states = new QuestState[]
					{
						getQuestState(quest.getName())
					};
				}
				else
				{
					states = addToQuestStateArray(states, getQuestState(quest.getName()));
				}
			}
		}
		
		// Return a table containing all QuestState to modify
		return states;
	}
	
	public QuestState[] getQuestsForKills(L2Npc npc)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;
		
		// Go through the QuestState of the L2PcInstance quests
		for (Quest quest : npc.getTemplate().getEventQuests(QuestEventType.ON_KILL))
		{
			// Check if the Identifier of the L2Attackable killed is needed for the current quest
			if (getQuestState(quest.getName()) != null)
			{
				// Copy the current L2PcInstance QuestState in the QuestState table
				if (states == null)
				{
					states = new QuestState[]
					{
						getQuestState(quest.getName())
					};
				}
				else
				{
					states = addToQuestStateArray(states, getQuestState(quest.getName()));
				}
			}
		}
		
		// Return a table containing all QuestState to modify
		return states;
	}
	
	public QuestState[] getQuestsForTalk(int npcId)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;
		
		// Go through the QuestState of the L2PcInstance quests
		List<Quest> quests = NpcData.getInstance().getTemplate(npcId).getEventQuests(QuestEventType.ON_TALK);
		if (quests != null)
		{
			for (Quest quest : quests)
			{
				if (quest != null)
				{
					// Copy the current L2PcInstance QuestState in the QuestState table
					if (getQuestState(quest.getName()) != null)
					{
						if (states == null)
						{
							states = new QuestState[]
							{
								getQuestState(quest.getName())
							};
						}
						else
						{
							states = addToQuestStateArray(states, getQuestState(quest.getName()));
						}
					}
				}
			}
		}
		
		// Return a table containing all QuestState to modify
		return states;
	}
	
	public void processQuestEvent(String questName, String event)
	{
		Quest quest = QuestManager.getInstance().getQuest(questName);
		
		if (quest == null)
			return;
		
		QuestState qs = getQuestState(questName);
		if (qs == null)
			return;
		
		L2Object object = L2World.getInstance().findObject(getLastQuestNpcObject());
		if (!(object instanceof L2Npc) || !isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
			return;
		
		final L2Npc npc = (L2Npc) object;
		
		final List<Quest> scripts = npc.getTemplate().getEventQuests(QuestEventType.ON_TALK);
		if (scripts != null)
		{
			for (Quest script : scripts)
			{
				if (script == null || !script.equals(quest))
					continue;
				
				quest.notifyEvent(event, npc, this);
				break;
			}
		}
	}
	
	public L2ShortCut[] getAllShortCuts()
	{
		return _shortCuts.getAllShortCuts();
	}
	
	public L2ShortCut getShortCut(int slot, int page)
	{
		return _shortCuts.getShortCut(slot, page);
	}
	
	public void registerShortCut(L2ShortCut shortcut)
	{
		_shortCuts.registerShortCut(shortcut);
	}
	
	public void deleteShortCut(int slot, int page)
	{
		_shortCuts.deleteShortCut(slot, page);
	}
	
	public void registerMacro(L2Macro macro)
	{
		_macroses.registerMacro(macro);
	}
	
	public void deleteMacro(int id)
	{
		_macroses.deleteMacro(id);
	}
	
	public MacroList getMacroses()
	{
		return _macroses;
	}
	
	public void setSiegeState(byte siegeState)
	{
		_siegeState = siegeState;
	}
	
	public byte getSiegeState()
	{
		return _siegeState;
	}
	
	public void setPvpFlag(int pvpFlag)
	{
		_pvpFlag = (byte) pvpFlag;
	}
	
	@Override
	public byte getPvpFlag()
	{
		return _pvpFlag;
	}
	
	public void updatePvPFlag(int value)
	{
		if (getPvpFlag() == value)
			return;
		
		setPvpFlag(value);
		sendPacket(new UserInfo(this));
		
		if (getPet() != null)
			sendPacket(new RelationChanged(getPet(), getRelation(this), false));

		broadcastRelationChanged();
	}
	
	@Override
	public void revalidateZone(boolean force)
	{
		
		super.revalidateZone(force);
		
		if (Config.ALLOW_WATER)
			checkWaterState();
		
		if (isInsideZone(ZoneId.SIEGE))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
				return;
			
			_lastCompassZone = ExSetCompassZoneCode.SIEGEWARZONE2;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.SIEGEWARZONE2));
		}
		else if (isInsideZone(ZoneId.PVP))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.PVPZONE)
				return;
			
			_lastCompassZone = ExSetCompassZoneCode.PVPZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.PVPZONE));
		}
		else if (isIn7sDungeon())
		{
			if (_lastCompassZone == ExSetCompassZoneCode.SEVENSIGNSZONE)
				return;
			
			_lastCompassZone = ExSetCompassZoneCode.SEVENSIGNSZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.SEVENSIGNSZONE));
		}
		else if (isInsideZone(ZoneId.PEACE))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.PEACEZONE)
				return;
			
			_lastCompassZone = ExSetCompassZoneCode.PEACEZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.PEACEZONE));
		}
		else
		{
			if (_lastCompassZone == ExSetCompassZoneCode.GENERALZONE)
				return;
			
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
				updatePvPStatus();
			
			_lastCompassZone = ExSetCompassZoneCode.GENERALZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.GENERALZONE));
		}
	}
	
	public boolean hasDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN) >= 1;
	}
	
	public int getDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN);
	}
	
	public boolean hasCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON) >= 1;
	}
	
	public int getCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON);
	}
	
	public int getPkKills()
	{
		return _pkKills;
	}
	
	public void setPkKills(int pkKills)
	{
		_pkKills = pkKills;
	}
	
	public long getDeleteTimer()
	{
		return _deleteTimer;
	}
	
	public void setDeleteTimer(long deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}
	
	public int getCurrentLoad()
	{
		return _inventory.getTotalWeight();
	}
	
	public long getLastRecomUpdate()
	{
		return _lastRecomUpdate;
	}
	
	public void setLastRecomUpdate(long date)
	{
		_lastRecomUpdate = date;
	}
	
	public int getRecomHave()
	{
		return _recomHave;
	}
	
	protected void incRecomHave()
	{
		if (_recomHave < 255)
			_recomHave++;
	}
	
	public void setRecomHave(int value)
	{
		_recomHave = Math.min(Math.max(value, 0), 255);
	}
	
	public int getRecomLeft()
	{
		return _recomLeft;
	}
	
	protected void decRecomLeft()
	{
		if (_recomLeft > 0)
			_recomLeft--;
	}
	
	public void giveRecom(L2PcInstance target)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(ADD_CHAR_RECOM);
			statement.setInt(1, getObjectId());
			statement.setInt(2, target.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Could not update char recommendations: " + e);
		}
		
		target.incRecomHave();
		decRecomLeft();
		_recomChars.add(target.getObjectId());
	}
	
	public boolean canRecom(L2PcInstance target)
	{
		return !_recomChars.contains(target.getObjectId());
	}
	
	public void setExpBeforeDeath(long exp)
	{
		_expBeforeDeath = exp;
	}
	
	public long getExpBeforeDeath()
	{
		return _expBeforeDeath;
	}
	
	@Override
	public int getKarma()
	{
		return _karma;
	}
	
	public void setKarma(int karma)
	{
		if (karma < 0)
			karma = 0;
		
		if(_karma == karma)
			return;
		
		if (_karma == 0 && karma > 0)
		{
			L2World.getInstance().forEachVisibleObject(this, L2GuardInstance.class, object ->
			{
				if (object.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
					object.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			});
		}
		
		_karma = karma;
		broadcastKarma();
		
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1).addNumber(karma));
		
	}

	public int getMaxLoad()
	{
		// Weight Limit = (CON Modifier*69000)*Skills
		// Source http://l2p.bravehost.com/weightlimit.html (May 2007)
		// Fitted exponential curve to the data
		int con = getCON();
		if (con < 1)
			return 31000;
		if (con > 59)
			return 176000;
		double baseLoad = Math.pow(1.029993928, con) * 30495.627366;
		return (int) calcStat(Stats.MAX_LOAD, baseLoad * Config.ALT_WEIGHT_LIMIT, this, null);
	}
	
	public int getExpertisePenalty()
	{
		return _expertisePenalty;
	}
	
	public int getWeightPenalty()
	{
		if (_dietMode)
			return 0;
		return _curWeightPenalty;
	}
	
	public void refreshOverloaded()
	{
		int maxLoad = getMaxLoad();
		if (maxLoad > 0)
		{
			long weightproc = getCurrentLoad() * 1000L / maxLoad;
			int newWeightPenalty;
			if (Config.DISABLE_WEIGHT_PENALTY || _dietMode)
			{
				newWeightPenalty = 0;
				super.removeSkill(getKnownSkill(4270));
				
				sendPacket(new UserInfo(this));
				sendPacket(new EtcStatusUpdate(this));
				broadcastUserInfo();
			}
			else
			{
				if (weightproc < 500 || _dietMode)
					newWeightPenalty = 0;
				else if (weightproc < 666)
					newWeightPenalty = 1;
				else if (weightproc < 800)
					newWeightPenalty = 2;
				else if (weightproc < 1000)
					newWeightPenalty = 3;
				else
					newWeightPenalty = 4;
				
				if (_curWeightPenalty != newWeightPenalty)
				{
					_curWeightPenalty = newWeightPenalty;
					if (newWeightPenalty > 0)
					{
						super.addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty));
						setIsOverloaded(getCurrentLoad() > maxLoad);
					}
					else
					{
						super.removeSkill(getKnownSkill(4270));
						setIsOverloaded(false);
					}
					
					sendPacket(new UserInfo(this));
					sendPacket(new EtcStatusUpdate(this));
					broadcastUserInfo();
				}
			}
		}
	}
	
	public void refreshExpertisePenalty()
	{
		int newPenalty = 0;
		
		for (L2ItemInstance item : getInventory().getItems())
		{
			if (item != null && item.isEquipped())
			{
				int crystaltype = item.getItem().getCrystalType().getId();
				
				if (crystaltype > newPenalty)
					newPenalty = crystaltype;
			}
		}
		
		newPenalty = newPenalty - getExpertiseIndex();
		
		if (newPenalty <= 0 || Config.DISABLE_GRADE_PENALTY)
			newPenalty = 0;
		
		if (getExpertisePenalty() != newPenalty)
		{
			_expertisePenalty = newPenalty;
			
			if (newPenalty > 0)
				super.addSkill(SkillTable.getInstance().getInfo(4267, 1)); // level used to be new Penalty
			else
				super.removeSkill(getKnownSkill(4267));
			
			sendSkillList();
			sendPacket(new EtcStatusUpdate(this));
		}
	}
	
	public void CheckIfWeaponIsAllowed()
	{
		if (isGM())
			return;
		
		// Iterate through all effects currently on the character.
		for (L2Effect currenteffect : getAllEffects())
		{
			L2Skill effectSkill = currenteffect.getSkill();
			
			if (currenteffect.getSkill().isToggle())
				currenteffect.exit();
			else if (!effectSkill.isOffensive() && !(effectSkill.getTargetType() == L2SkillTargetType.TARGET_PARTY && effectSkill.getSkillType() == L2SkillType.BUFF))
			{
				// Check to rest to assure current effect meets weapon
				// requirements.
				if (!effectSkill.getWeaponDependancy(this))
				{
					sendMessage(effectSkill.getName() + " cannot be used with this weapon.");
					if (Config.DEBUG)
					{
						_log.info("   | Skill " + effectSkill.getName() + " has been disabled for (" + getName() + "); Reason: Incompatible Weapon Type.");
					}
					currenteffect.exit();
				}
			}
			continue;
		}
	}
	
	public void checkSSMatch(L2ItemInstance equipped, L2ItemInstance unequipped)
	{
		if (unequipped == null)
			return;
		
		if (unequipped.getItem().getType2() == L2Item.TYPE2_WEAPON && (equipped == null ? true : equipped.getItem().getCrystalType() != unequipped.getItem().getCrystalType()) && getInventory().getItems() != null)
		{
			for (L2ItemInstance ss : getInventory().getItems())
			{
				int _itemId = ss.getItemId();
				
				if (((_itemId >= 2509 && _itemId <= 2514) || (_itemId >= 3947 && _itemId <= 3952) || (_itemId <= 1804 && _itemId >= 1808) || _itemId == 5789 || _itemId == 5790 || _itemId == 1835) && ss.getItem().getCrystalType() == unequipped.getItem().getCrystalType())
				{
					sendPacket(new ExAutoSoulShot(_itemId, 0));			
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addString(ss.getItemName()));
				}
			}
		}
	}
	
	public int getPvpKills()
	{
		return _pvpKills;
	}
	
	public void setInvullBuffs(boolean InvullBuffs)
	{
		_InvullBuffs = InvullBuffs;
	}
	
	public boolean isInvullBuffs()
	{
		return _InvullBuffs;
	}
	
	public void setPvpKills(int pvpKills)
	{
		_pvpKills = pvpKills;
	}
	
	public ClassId getClassId()
	{
		return getTemplate().classId;
	}
	
	public void setClassId(int Id)
	{
		
		if (getLvlJoinedAcademy() != 0 && _clan != null && ClassId.VALUES[Id].level() == 2)
		{
			if (getLvlJoinedAcademy() <= 16)
				_clan.setReputationScore(_clan.getReputationScore() + 400, true);
			else if (getLvlJoinedAcademy() >= 39)
				_clan.setReputationScore(_clan.getReputationScore() + 170, true);
			else
				_clan.setReputationScore(_clan.getReputationScore() + (400 - (getLvlJoinedAcademy() - 16) * 10), true);

			_clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(_clan));
			setLvlJoinedAcademy(0);
			// oust pledge member from the academy, cuz he has finished his 2nd class transfer
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED);
			msg.addString(getName());
			_clan.broadcastToOnlineMembers(msg);
			_clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(getName()));
			_clan.removeClanMember(getName(), 0);
			sendPacket(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED);
			
			// receive graduation gift
			getInventory().addItem("Gift", 8181, 1, this, null); // give academy circlet
			getInventory().updateDatabase(); // update database
		}
		
		if (isSubClassActive())
			getSubClasses().get(_classIndex).setClassId(Id);
		
		broadcastPacket(new MagicSkillUse(this, this, 5103, 1, 1000, 0));
		setClassTemplate(Id);
		
		if (getClassId().level() == 3)
			sendPacket(SystemMessageId.THIRD_CLASS_TRANSFER);
		else
			sendPacket(SystemMessageId.CLASS_TRANSFER);
		
		// Update class icon in party and clan
		if (_party != null)
			_party.broadcastToPartyMembers(new PartySmallWindowUpdate(this));
		
		if (_clan != null)
			_clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
		
		if (Config.AUTO_LEARN_SKILLS)
			rewardSkills();
	}
	
	public long getExp()
	{
		return getStat().getExp();
	}
	
	public void setActiveEnchantItem(L2ItemInstance scroll)
	{
		// If we dont have a Enchant Item, we are not enchanting.
		if (scroll == null)
			setIsEnchanting(false);

		_activeEnchantItem = scroll;
	}
	
	public L2ItemInstance getActiveEnchantItem()
	{
		return _activeEnchantItem;
	}
	
	public void setIsEnchanting(boolean val)
	{
		_isEnchanting = val;
	}
	
	public boolean isEnchanting()
	{
		return _isEnchanting;
	}
	
	public void setFistsWeaponItem(L2Weapon weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}
	
	public L2Weapon getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}
	
	public L2Weapon findFistsWeaponItem(int classId)
	{
		L2Weapon weaponItem = null;
		if ((classId >= 0x00) && (classId <= 0x09))
		{
			// human fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(246);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x0a) && (classId <= 0x11))
		{
			// human mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(251);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x12) && (classId <= 0x18))
		{
			// elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(244);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x19) && (classId <= 0x1e))
		{
			// elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(249);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x1f) && (classId <= 0x25))
		{
			// dark elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(245);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x26) && (classId <= 0x2b))
		{
			// dark elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(250);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x2c) && (classId <= 0x30))
		{
			// orc fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(248);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x31) && (classId <= 0x34))
		{
			// orc mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(252);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x35) && (classId <= 0x39))
		{
			// dwarven fists
			L2Item temp = ItemTable.getInstance().getTemplate(247);
			weaponItem = (L2Weapon) temp;
		}
		
		return weaponItem;
	}
	
	public void rewardSkills()
	{
		// Get the Level of the L2PcInstance
		int lvl = getLevel();
		
		// Remove beginner Lucky skill
		if (lvl > 9)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(194, 1);
			skill = removeSkill(skill);
			
			if (Config.DEBUG && skill != null)
			{
				_log.fine("removed skill 'Lucky' from " + getName());
			}
		}
		
		// Calculate the current higher Expertise of the L2PcInstance
		for (int i = 0; i < EXPERTISE_LEVELS.length; i++)
		{
			if (lvl >= EXPERTISE_LEVELS[i])
			{
				setExpertiseIndex(i);
			}
		}
		
		// Add the Expertise skill corresponding to its Expertise level
		if (getExpertiseIndex() > 0)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(239, getExpertiseIndex());
			addSkill(skill, true);
			
			if (Config.DEBUG)
			{
				_log.fine("awarded " + getName() + " with new expertise.");
			}
			
		}
		else
		{
			if (Config.DEBUG)
			{
				_log.fine("No skills awarded at lvl: " + lvl);
			}
		}
		
		// Active skill dwarven craft
		
		if (getSkillLevel(1321) < 1 && getRace() == ClassRace.DWARF)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(1321, 1);
			addSkill(skill, true);
		}
		
		// Active skill common craft
		if (getSkillLevel(1322) < 1)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(1322, 1);
			addSkill(skill, true);
		}
		
		for (int i = 0; i < COMMON_CRAFT_LEVELS.length; i++)
		{
			if (lvl >= COMMON_CRAFT_LEVELS[i] && getSkillLevel(1320) < (i + 1))
			{
				L2Skill skill = SkillTable.getInstance().getInfo(1320, (i + 1));
				addSkill(skill, true);
			}
		}
		
		// Auto-Learn skills if activated
		if (Config.AUTO_LEARN_SKILLS)
		{
			giveAvailableSkills();
		}
		sendSkillList();
		// This function gets called on login, so not such a bad place to check
		// weight
		refreshOverloaded(); // Update the overloaded status of the L2PcInstance
		refreshExpertisePenalty(); // Update the expertise status of the
		// L2PcInstance
	}
	
	private void regiveTemporarySkills()
	{
		// Do not call this on enterworld or char load
		// Add noble skills if noble
		if (isNoble())
			setNoble(true);
		
		// Add Hero skills if hero
		if (isHero())
			setHero(true);
		
		// Add clan skills
		if (getClan() != null && getClan().getReputationScore() >= 0)
		{
			L2Skill[] skills = getClan().getAllSkills();
			for (L2Skill sk : skills)
			{
				if (sk.getMinPledgeClass() <= getPledgeClass())
					addSkill(sk, false);
			}
		}
		
		// Reload passive skills from armors / jewels / weapons
		getInventory().reloadEquippedItems();
	}
	
	public void giveAvailableSkills()
	{
		int skillCounter = 0;
		Collection<L2SkillLearn> skills = SkillTreeData.getInstance().getAllAvailableSkills(this, getClassId());
		for (final L2SkillLearn sk : skills)
		{
			final L2Skill skill = SkillTable.getInstance().getInfo(sk.getId(), sk.getLevel());

			if(skill==null)
				continue;
			
			if (getKnownSkill(skill.getId()) == skill)
				continue;
			
			if (getSkillLevel(sk.getId()) == -1)
				skillCounter++;
			
			if (sk.getId() == 4267 || sk.getId() == 4270)
				continue;

			if (skill.isToggle())
			{
				final L2Effect toggleEffect = getFirstEffect(sk.getId());
				if (toggleEffect != null)
				{
					toggleEffect.exit(false);
					skill.getEffects(this, this);
				}
			}
			
			addSkill(skill, true);
		}
		
		if (skillCounter > 0)
			sendMessage("You have learned " + skillCounter + " new skills.");
	}

	public void setExp(long exp)
	{
		getStat().setExp(exp);
	}
	
	public ClassRace getRace()
	{
		return (isSubClassActive()) ? getBaseTemplate().getRace() : getTemplate().getRace();
	}
	
	public L2Radar getRadar()
	{
		return _radar;
	}
	
	public int getSp()
	{
		return getStat().getSp();
	}
	
	public void setSp(int sp)
	{
		super.getStat().setSp(sp);
	}
	
	public boolean isCastleLord(int castleId)
	{
		final L2Clan clan = getClan();
		
		// player has clan and is the clan leader, check the castle info
		if ((clan != null) && (clan.getLeader().getPlayerInstance() == this))
		{
			// if the clan has a castle and it is actually the queried castle,
			// return true
			Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
			if ((castle != null) && castle.getCastleId() == castleId)
				return true;
		}
		
		return false;
	}
	
	@Override
	public int getClanId()
	{
		return _clanId;
	}
	
	@Override
	public int getAllyId()
	{
		return _clan == null ? 0 : _clan.getAllyId();
	}
	
	public int getClanCrestId()
	{
		if (_clan != null && _clan.hasCrest())
			return _clan.getCrestId();
		
		return 0;
	}
	
	public int getClanCrestLargeId()
	{
		if (_clan != null && _clan.hasCrestLarge())
			return _clan.getCrestLargeId();
		
		return 0;
	}
	
	public long getClanJoinExpiryTime()
	{
		return _clanJoinExpiryTime;
	}
	
	public void setClanJoinExpiryTime(long time)
	{
		_clanJoinExpiryTime = time;
	}
	
	public long getClanCreateExpiryTime()
	{
		return _clanCreateExpiryTime;
	}
	
	public void setClanCreateExpiryTime(long time)
	{
		_clanCreateExpiryTime = time;
	}
	
	public void setOnlineTime(long time)
	{
		_onlineTime = time;
		_onlineBeginTime = System.currentTimeMillis();
	}
	
	public PcInventory getInventory()
	{
		return _inventory;
	}
	
	public void removeItemFromShortCut(int objectId)
	{
		_shortCuts.deleteShortCutByObjectId(objectId);
	}
	
	public boolean isSitting()
	{
		return _waitTypeSitting || sittingTaskLaunched;
	}
	
	public void setIsSitting(boolean state)
	{
		_waitTypeSitting = state;
	}
	
	public boolean isSittingTaskLunched()
	{
		return sittingTaskLaunched;
	}
	
	public void setPosticipateSit(boolean act)
	{
		_posticipateSit = act;
	}
	
	public boolean getPosticipateSit()
	{
		return _posticipateSit;
	}
	
	public final void startFakeDeath()
	{
		if (isFakeDeath() || isSittingTaskLunched())
			return;
		
		setIsFakeDeath(true);
		
		abortAttack();
		abortCast();
		stopMove(null);
		
		getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null);
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_START_FAKEDEATH));
	}
	
	public final void stopFakeDeath(L2Effect effect)
	{
		if (!isFakeDeath() || isSittingTaskLunched())
			return;
		
		if (effect == null)
			stopEffects(L2Effect.EffectType.FAKE_DEATH);
		else
			removeEffect(effect);
		
		setIsFakeDeath(false);
		setRecentFakeDeath(true);
		
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STOP_FAKEDEATH));
		broadcastPacket(new Revive(this));
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);		
		ThreadPoolManager.getInstance().scheduleGeneral(() -> setIsParalyzed(false), (int) (2500 / getStat().getMovementSpeedMultiplier()));
		setIsParalyzed(true);
	}
	
	public void sitDown()
	{
		if (isFakeDeath())
			stopFakeDeath(null);

		if (isMoving())
		// since you are moving and want sit down
		// the posticipate sitdown task will be always true
		{
			setPosticipateSit(true);
			return;
		}
		
		// we are going to sitdown, so posticipate is false
		setPosticipateSit(false);
		
		if (isCastingNow() && !_relax)
		{
			sendMessage("Cannot sit while casting.");
			return;
		}
		
		if (sittingTaskLaunched) // if already started the task just return
			return;
		
		if (!_waitTypeSitting && !isAttackingDisabled() && !isOutOfControl() && !isImmobilized())
		{
			breakAttack();
			setIsSitting(true);
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));
			sittingTaskLaunched = true;
			// Schedule a sit down task to wait for the animation to finish
			ThreadPoolManager.getInstance().scheduleGeneral(new SitDownTask(this), 2500);
			setIsParalyzed(true);
		}
		
	}
	
	class SitDownTask implements Runnable
	{
		L2PcInstance _player;
		
		SitDownTask(L2PcInstance player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			setIsSitting(true);
			_player.setIsParalyzed(false);
			sittingTaskLaunched = false;
			_player.getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		}
	}
	
	class StandUpTask implements Runnable
	{
		L2PcInstance _player;
		
		StandUpTask(L2PcInstance player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			_player.setIsSitting(false);
			_player.setIsImmobilized(false);
			_player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}
	
	public void standUp()
	{
		if (isFakeDeath())
		{
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
			// Schedule a stand up task to wait for the animation to finish
			setIsImmobilized(true);
			ThreadPoolManager.getInstance().scheduleGeneral(new StandUpTask(this), 2500);
			stopFakeDeath(null);
		}
		
		if (sittingTaskLaunched)
			return;
		
		if (L2Event.active && eventSitForced)
			sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up ...");
		else if (TvT._sitForced && _inEventTvT || CTF._sitForced && _inEventCTF || DM._sitForced && _inEventDM)
			sendMessage("The Admin/GM handle if you sit or stand in this match!");
		else if (_waitTypeSitting && !isInStoreMode() && !isAlikeDead())
		{
			if (_relax)
			{
				setRelax(false);
				stopEffects(L2Effect.EffectType.RELAXING);
			}
			
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
			// Schedule a stand up task to wait for the animation to finish
			setIsImmobilized(true);
			ThreadPoolManager.getInstance().scheduleGeneral(new StandUpTask(this), 2500);
		}
	}
	
	public void setRelax(boolean val)
	{
		_relax = val;
	}
	
	public PcWarehouse getWarehouse()
	{
		if (_warehouse == null)
		{
			_warehouse = new PcWarehouse(this);
			_warehouse.restore();
		}
		
		if (Config.WAREHOUSE_CACHE)
			WarehouseCache.getInstance().addCacheTask(this);
		
		return _warehouse;
	}
	
	public void clearWarehouse()
	{
		if (_warehouse != null)
			_warehouse.deleteMe();
		_warehouse = null;
	}
	
	public PcFreight getFreight()
	{
		return _freight;
	}
	
	public int getCharId()
	{
		return _charId;
	}
	
	public void setCharId(int charId)
	{
		_charId = charId;
	}
	
	public int getAdena()
	{
		return _inventory.getAdena();
	}
	
	public int getAncientAdena()
	{
		return _inventory.getAncientAdena();
	}
	
	public void addAdena(String process, int count, L2Object reference, boolean sendMessage)
	{
		if (sendMessage)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA);
			sm.addNumber(count);
			sendPacket(sm);
		}
		
		if (count > 0)
		{
			_inventory.addAdena(process, count, this, reference);
			
			// Send update packet
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(_inventory.getAdenaInstance());
				sendPacket(iu);
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
		}
	}
	
	public boolean reduceAdena(String process, int count, L2Object reference, boolean sendMessage)
	{
		if (count > getAdena())
		{
			if (sendMessage)
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);

			return false;
		}
		
		if (count > 0)
		{
			L2ItemInstance adenaItem = _inventory.getAdenaInstance();
			_inventory.reduceAdena(process, count, this, reference);
			
			// Send update packet
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(adenaItem);
				sendPacket(iu);
			}
			else
				sendPacket(new ItemList(this, false));
			
			if (sendMessage)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED_ADENA);
				sm.addNumber(count);
				sendPacket(sm);
			}
		}
		
		return true;
	}
	
	public void addAncientAdena(String process, int count, L2Object reference, boolean sendMessage)
	{
		if (sendMessage)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
			sm.addNumber(count);
			sendPacket(sm);
		}
		
		if (count > 0)
		{
			_inventory.addAncientAdena(process, count, this, reference);
			
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(_inventory.getAncientAdenaInstance());
				sendPacket(iu);
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
		}
	}
	
	public boolean reduceAncientAdena(String process, int count, L2Object reference, boolean sendMessage)
	{
		if (count > getAncientAdena())
		{
			if (sendMessage)
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);		
			
			return false;
		}
		
		if (count > 0)
		{
			L2ItemInstance ancientAdenaItem = _inventory.getAncientAdenaInstance();
			_inventory.reduceAncientAdena(process, count, this, reference);
			
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(ancientAdenaItem);
				sendPacket(iu);
			}
			else
				sendPacket(new ItemList(this, false));
			
			if (sendMessage)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
				sm.addNumber(count);
				sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
				sendPacket(sm);
			}
		}
		
		return true;
	}
	
	public void addItem(String process, int itemId, long countL, L2Object reference, boolean sendMessage)
	{
		int count = 0;
		count = (int) countL;
		if (count != countL)
		{
			count = 1;
		}
		
		if (count > 0)
		{
			// Sends message to client if requested
			if (sendMessage && (!isCastingNow() && ItemTable.getInstance().createDummyItem(itemId).getItemType() == L2EtcItemType.HERB || ItemTable.getInstance().createDummyItem(itemId).getItemType() != L2EtcItemType.HERB))
			{
				if (count > 1)
				{
					if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
						sm.addItemName(itemId);
						sm.addNumber(count);
						sendPacket(sm);
						sm = null;
					}
					else
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
						sm.addItemName(itemId);
						sm.addNumber(count);
						sendPacket(sm);
						sm = null;
					}
				}
				else
				{
					if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
						sm.addItemName(itemId);
						sendPacket(sm);
						sm = null;
					}
					else
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
						sm.addItemName(itemId);
						sendPacket(sm);
						sm = null;
					}
				}
			}
			// Auto use herbs - autoloot
			if (ItemTable.getInstance().createDummyItem(itemId).getItemType() == L2EtcItemType.HERB) // If item is herb dont add it to iv :]
			{
				if (!isCastingNow())
				{
					L2ItemInstance herb = new L2ItemInstance(_charId, itemId);
					IItemHandler handler = ItemHandler.getInstance().getHandler(herb.getItemId());
					
					if (handler == null)
					{
						_log.warning(L2PcInstance.class.getName() + ": No item handler registered for Herb - item ID " + herb.getItemId() + ".");
					}
					else
					{
						handler.useItem(this, herb);
						
						if (_herbstask >= 100)
						{
							_herbstask -= 100;
						}
						
						handler = null;
					}
					
					herb = null;
				}
				else
				{
					_herbstask += 100;
					ThreadPoolManager.getInstance().scheduleAi(new HerbTask(process, itemId, count, reference, sendMessage), _herbstask);
				}
			}
			else
			{
				// Add the item to inventory
				L2ItemInstance item = _inventory.addItem(process, itemId, count, this, reference);
				
				// Send inventory update packet
				if (!Config.FORCE_INVENTORY_UPDATE)
				{
					InventoryUpdate playerIU = new InventoryUpdate();
					playerIU.addItem(item);
					sendPacket(playerIU);
				}
				else
				{
					sendPacket(new ItemList(this, false));
				}
				
				// Update current load as well
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
				sendPacket(su);
				su = null;
				
				// If over capacity, drop the item
				if (!isGM() && !_inventory.validateCapacity(item))
				{
					dropItem("InvDrop", item, null, true);
				}
				else if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
				{
					CursedWeaponsManager.getInstance().activate(this, item);
				}
				
				item = null;
			}
		}
	}
	
	public void addItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		if (item.getCount() > 0)
		{
			// Sends message to client if requested
			if (sendMessage)
			{
				if (item.getCount() > 1)
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2).addItemName(item).addNumber(item.getCount()));
				else if (item.getEnchantLevel() > 0)
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2).addNumber(item.getEnchantLevel()).addItemName(item));
				else
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1).addItemName(item));
			}
			
			// Add the item to inventory
			L2ItemInstance newitem = _inventory.addItem(process, item, this, reference);
			
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(newitem);
			sendPacket(playerIU);

			sendPacket(new ItemList(this, false));

			// Update current load as well
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
			sendPacket(su);
			
			// If over capacity, drop the item
			if (!isGM() && !_inventory.validateCapacity(0))
			{
				dropItem("InvDrop", newitem, null, true);
			}
			else if (CursedWeaponsManager.getInstance().isCursed(newitem.getItemId()))
			{
				CursedWeaponsManager.getInstance().activate(this, newitem);
			}
		}
	}
	
	public void addItem(String process, int itemId, int count, L2Object reference, boolean sendMessage)
	{
		if (count > 0)
		{
			// Sends message to client if requested
			if (sendMessage && (!isCastingNow() && ItemTable.getInstance().createDummyItem(itemId).getItemType() == L2EtcItemType.HERB || ItemTable.getInstance().createDummyItem(itemId).getItemType() != L2EtcItemType.HERB))
			{
				if (count > 1)
				{
					if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
						sm.addItemName(itemId);
						sm.addNumber(count);
						sendPacket(sm);
					}
					else
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
						sm.addItemName(itemId);
						sm.addNumber(count);
						sendPacket(sm);
					}
				}
				else
				{
					if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
						sm.addItemName(itemId);
						sendPacket(sm);
					}
					else
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
						sm.addItemName(itemId);
						sendPacket(sm);
					}
				}
			}
			// Auto use herbs - autoloot
			if (ItemTable.getInstance().createDummyItem(itemId).getItemType() == L2EtcItemType.HERB) // If item is herb dont add it to iv :]
			{
				if (!isCastingNow())
				{
					L2ItemInstance herb = new L2ItemInstance(_charId, itemId);
					IItemHandler handler = ItemHandler.getInstance().getHandler(herb.getItemId());
					if (handler == null)
					{
						_log.warning(L2PcInstance.class.getName() + ": No item handler registered for Herb - item ID " + herb.getItemId() + ".");
					}
					else
					{
						handler.useItem(this, herb);
						if (_herbstask >= 100)
						{
							_herbstask -= 100;
						}
					}
				}
				else
				{
					_herbstask += 100;
					ThreadPoolManager.getInstance().scheduleAi(new HerbTask(process, itemId, count, reference, sendMessage), _herbstask);
				}
			}
			else
			{
				// Add the item to inventory
				L2ItemInstance item = _inventory.addItem(process, itemId, count, this, reference);
				
				InventoryUpdate playerIU = new InventoryUpdate();
				playerIU.addItem(item);
				sendPacket(playerIU);
				sendPacket(new ItemList(this, false));
				
				// Update current load as well
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
				sendPacket(su);
				
				// If over capacity, drop the item
				if (!isGM() && !_inventory.validateCapacity(0))
				{
					dropItem("InvDrop", item, null, true);
				}
				else if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
				{
					CursedWeaponsManager.getInstance().activate(this, item);
				}
			}
		}
	}
	
	public boolean destroyItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		int oldCount = item.getCount();
		item = _inventory.destroyItem(process, item, this, reference);
		
		if (item == null)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			
			return false;
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
			sm.addNumber(oldCount);
			sm.addItemName(item.getItemId());
			sendPacket(sm);
		}
		
		return true;
	}
	
	@Override
	public boolean destroyItem(String process, int objectId, int count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = _inventory.getItemByObjectId(objectId);
		
		if (item == null || item.getCount() < count || _inventory.destroyItem(process, objectId, count, this, reference) == null)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			
			return false;
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
			sm.addNumber(count);
			sm.addItemName(item.getItemId());
			sendPacket(sm);
		}
		
		return true;
	}
	
	public boolean destroyItemWithoutTrace(String process, int objectId, int count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = _inventory.getItemByObjectId(objectId);
		
		if (item == null || item.getCount() < count)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			return false;
		}
		
		// Adjust item quantity
		if (item.getCount() > count)
		{
			synchronized (item)
			{
				item.changeCountWithoutTrace(process, -count, this, reference);
				item.setLastChange(L2ItemInstance.MODIFIED);
				
				// could do also without saving, but let's save approx 1 of 10
				if (GameTimeController.getInstance().getGameTicks() % 10 == 0)
				{
					item.updateDatabase();
				}
				_inventory.refreshWeight();
			}
		}
		else
		{
			// Destroy entire item and save to database
			_inventory.destroyItem(process, item, this, reference);
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
			sm.addNumber(count);
			sm.addItemName(item.getItemId());
			sendPacket(sm);
		}
		
		return true;
	}
	
	public boolean destroyItemsByList(String process, List<int[]> list, L2Object reference, boolean sendMessage, int payType)
	{
		for (int[] l : list)
		{
			switch (payType)
			{
				case 0:
				{
					if (!destroyItemByItemId(process, l[0], l[1], reference, sendMessage))
						return false;
				}
				case 1:
				{
					break;
				}
				case 2:
				{
					if (!destroyItemByItemId(process, l[0], l[1] / 2, reference, sendMessage))
						return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = _inventory.getItemByItemId(itemId);
		
		if (item == null || item.getCount() < count || _inventory.destroyItemByItemId(process, itemId, count, this, reference) == null)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			
			return false;
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}
		else
			sendPacket(new ItemList(this, false));
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
		{
			final L2GameServerPacket packet = count > 1 ? SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(itemId).addItemNumber(count) : SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(itemId);
			sendPacket(packet);
		}
		
		return true;
	}
	
	public void destroyWearedItems(String process, L2Object reference, boolean sendMessage)
	{
		
		// Go through all Items of the inventory
		for (L2ItemInstance item : getInventory().getItems())
		{
			// Check if the item is a Try On item in order to remove it
			if (item.isWear())
			{
				if (item.isEquipped())
					getInventory().unEquipItemInSlotAndRecord(item.getEquipSlot());
				
				if (_inventory.destroyItem(process, item, this, reference) == null)
					continue;
				
				// Send an Unequipped Message in system window of the player for each Item
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(item.getItemId());
				sendPacket(sm);
				
			}
		}
		
		// Send the StatusUpdate Server->Client Packet to the player with new
		// CUR_LOAD (0x0e) information
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Send the ItemList Server->Client Packet to the player in order to
		// refresh its Inventory
		ItemList il = new ItemList(this, true);
		sendPacket(il);
		
		// Send a Server->Client packet UserInfo to this L2PcInstance and
		// CharInfo to all L2PcInstance in its _KnownPlayers
		broadcastUserInfo();
		
		// Sends message to client if requested
		sendMessage("Trying-on mode has ended.");
		
	}
	
	public L2ItemInstance transferItem(String process, int objectId, int count, Inventory target, L2Object reference)
	{
		L2ItemInstance oldItem = checkItemManipulation(objectId, count, "transfer");
		if (oldItem == null)
			return null;
		L2ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, this, reference);
		if (newItem == null)
			return null;
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			
			if (oldItem.getCount() > 0 && oldItem != newItem)
			{
				playerIU.addModifiedItem(oldItem);
			}
			else
			{
				playerIU.addRemovedItem(oldItem);
			}
			
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate playerSU = new StatusUpdate(getObjectId());
		playerSU.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(playerSU);
		
		// Send target update packet
		if (target instanceof PcInventory)
		{
			L2PcInstance targetPlayer = ((PcInventory) target).getOwner();
			
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate playerIU = new InventoryUpdate();
				
				if (newItem.getCount() > count)
				{
					playerIU.addModifiedItem(newItem);
				}
				else
				{
					playerIU.addNewItem(newItem);
				}
				
				targetPlayer.sendPacket(playerIU);
			}
			else
			{
				targetPlayer.sendPacket(new ItemList(targetPlayer, false));
			}
			
			// Update current load as well
			playerSU = new StatusUpdate(targetPlayer.getObjectId());
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
			targetPlayer.sendPacket(playerSU);
		}
		else if (target instanceof PetInventory)
		{
			PetInventoryUpdate petIU = new PetInventoryUpdate();
			
			if (newItem.getCount() > count)
			{
				petIU.addModifiedItem(newItem);
			}
			else
			{
				petIU.addNewItem(newItem);
			}
			
			((PetInventory) target).getOwner().getOwner().sendPacket(petIU);
		}
		
		return newItem;
	}
	
	public boolean dropItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		if (_freight.getItemByObjectId(item.getObjectId()) != null)
		{			
			this.sendPacket(ActionFailed.STATIC_PACKET);
			Util.handleIllegalPlayerAction(this, "Warning!! Character " + getName() + " of account " + getAccountName() + " tried to drop Freight Items", IllegalPlayerAction.PUNISH_KICK);
			return false;
			
		}
		item = _inventory.dropItem(process, item, this, reference);
		
		if (item == null)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			
			return false;
		}
		
		item.dropMe(this, getClientX() + Rnd.get(50) - 25, getClientY() + Rnd.get(50) - 25, getClientZ() + 20);
		
		if (Config.AUTODESTROY_ITEM_AFTER > 0 && Config.DESTROY_DROPPED_PLAYER_ITEM && !Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
		{
			if ((item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM) || !item.isEquipable())
				ItemsAutoDestroy.getInstance().addItem(item);
		}
		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			if (!item.isEquipable() || (item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM))
				item.setProtected(false);
			else
				item.setProtected(true);
		}
		else
			item.setProtected(true);
		
		InventoryUpdate playerIU = new InventoryUpdate();
		playerIU.addItem(item);
		sendPacket(playerIU);
		sendPacket(new ItemList(this, false));
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1);
			sm.addItemName(item.getItemId());
			sendPacket(sm);
		}
		
		return true;
	}
	
	public L2ItemInstance dropItem(String process, int objectId, int count, int x, int y, int z, L2Object reference, boolean sendMessage)
	{	
		if (_freight.getItemByObjectId(objectId) != null)
		{			
			this.sendPacket(ActionFailed.STATIC_PACKET);			
			Util.handleIllegalPlayerAction(this, "Warning!! Character " + getName() + " of account " + getAccountName() + " tried to drop Freight Items", IllegalPlayerAction.PUNISH_KICK);
			return null;		
		}
		
		final L2ItemInstance invitem = _inventory.getItemByObjectId(objectId);
		final L2ItemInstance item = _inventory.dropItem(process, objectId, count, this, reference);
		
		if (item == null)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			
			return null;
		}
		
		item.dropMe(this, x, y, z);
		
		if (Config.AUTODESTROY_ITEM_AFTER > 0 && Config.DESTROY_DROPPED_PLAYER_ITEM && !Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
		{
			if ((item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM) || !item.isEquipable())
				ItemsAutoDestroy.getInstance().addItem(item);
		}
		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			if (!item.isEquipable() || (item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM))
				item.setProtected(false);
			else
				item.setProtected(true);
		}
		else
			item.setProtected(true);
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(invitem);
			sendPacket(playerIU);
		}
		else
			sendPacket(new ItemList(this, false));
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1);
			sm.addItemName(item.getItemId());
			sendPacket(sm);
		}
		
		return item;
	}
	
	public L2ItemInstance checkItemManipulation(int objectId, int count, String action)
	{
		// TODO: if we remove objects that are not visisble from the L2World, we'll have to remove this check
		if (L2World.getInstance().findObject(objectId) == null)
		{
			_log.finest(getObjectId() + ": player tried to " + action + " item not available in L2World");
			return null;
		}
		
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		
		if (item == null || item.getOwnerId() != getObjectId())
		{
			_log.finest(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return null;
		}
		
		if (count < 0 || (count > 1 && !item.isStackable()))
		{
			_log.finest(getObjectId() + ": player tried to " + action + " item with invalid count: " + count);
			return null;
		}
		
		if (count > item.getCount())
		{
			_log.finest(getObjectId() + ": player tried to " + action + " more items than he owns");
			return null;
		}
		
		// Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
		if (getPet() != null && getPet().getControlItemId() == objectId || getMountObjectID() == objectId)
		{
			if (Config.DEBUG)
			{
				_log.finest(getObjectId() + ": player tried to " + action + " item controling pet");
			}
			
			return null;
		}
		
		if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
		{
			if (Config.DEBUG)
			{
				_log.finest(getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
			}
			
			return null;
		}
		
		if (item.isWear())
			// cannot drop/trade wear-items
			return null;
		
		// We cannot put a Weapon with Augmention in WH while casting (Possible Exploit)
		if (item.isAugmented() && (isCastingNow() || isCastingNow()))
			return null;
		
		return item;
	}
	
	public void setProtection(boolean protect)
	{
		if (Config.DEBUG && (protect || (_protectEndTime > 0)))
			_log.config(L2PcInstance.class.getName() + getName() + ": Protection " + (protect ? "ON " + (GameTimeController.getInstance().getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * GameTimeController.TICKS_PER_SECOND) : "OFF") + " (currently " + GameTimeController.getInstance().getGameTicks() + ")");

		_protectEndTime = protect ? GameTimeController.getInstance().getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * GameTimeController.TICKS_PER_SECOND : 0;
		
	}
	
	public void setRecentFakeDeath(boolean protect)
	{
		_recentFakeDeathEndTime = protect ? GameTimeController.getInstance().getGameTicks() * GameTimeController.TICKS_PER_SECOND : 0;
	}
	
	public boolean isRecentFakeDeath()
	{
		return _recentFakeDeathEndTime > GameTimeController.getInstance().getGameTicks();
	}
	
	public L2GameClient getClient()
	{
		return _client;
	}
	
	public void setClient(L2GameClient client)
	{
		_client = client;
	}
	
	public void closeNetConnection(boolean close)
	{
		L2GameClient client = _client;
		if (client != null)
		{
			if (client.isDetached())
				client.cleanMe(true);
			else
			{
				if (!client.getConnection().isClosed())
				{
					if (close)
						client.close(new LeaveWorld());
					else
						client.close(ServerClose.STATIC_PACKET);
				}
			}
		}
	}
	
	public Point3D getCurrentSkillWorldPosition()
	{
		return _currentSkillWorldPosition;
	}
	
	public void setCurrentSkillWorldPosition(Point3D worldPosition)
	{
		_currentSkillWorldPosition = worldPosition;
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		
		if (((TvT._started && !Config.TVT_ALLOW_INTERFERENCE) || (CTF._started && !Config.CTF_ALLOW_INTERFERENCE) || (DM._started && !Config.DM_ALLOW_INTERFERENCE) && !player.isGM()))
		{
			if ((_inEventTvT && !player._inEventTvT) || (!_inEventTvT && player._inEventTvT))
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if ((_inEventCTF && !player._inEventCTF) || (!_inEventCTF && player._inEventCTF))
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if ((_inEventDM && !player._inEventDM) || (!_inEventDM && player._inEventDM))
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if ((_inEventVIP && !player._inEventVIP) || (!_inEventVIP && player._inEventVIP))
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// Check if the L2PcInstance is confused
		if (player.isOutOfControl())
		{
			// Send a Server->Client packet ActionFailed to the player
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the player already target this L2PcInstance
		if (player.getTarget() != this)
		{
			player.setTarget(this);
		}
		else
		{
			// Check if this L2PcInstance has a Private Store
			if (getPrivateStoreType() != StoreType.NONE)
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				return;
			}
			// Check if this L2PcInstance is autoAttackable
			if (isAutoAttackable(player) || (player._inEventTvT && TvT._started) || (player._inEventCTF && CTF._started) || (player._inEventDM && DM._started) && !isGM())
			{
				// Player with lvl < 21 can't attack a cursed weapon holder
				// And a cursed weapon holder can't attack players with lvl < 21
				if ((isCursedWeaponEquiped() && player.getLevel() < 21) || (player.isCursedWeaponEquiped() && getLevel() < 21))
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				
				if (((Config.GEODATA) ? GeoEngine.canSeeTarget(player, this, isFlying()) : GeoEngine.canSeeTarget(player, this)))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
					player.onActionRequest();
				}
			}
			else
			{
				if (player != this && ((Config.GEODATA) ? GeoEngine.canSeeTarget(player, this, isFlying()) : GeoEngine.canSeeTarget(player, this)))
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private boolean needCpUpdate(int barPixels)
	{
		double currentCp = getCurrentCp();
		
		if (currentCp <= 1.0 || getMaxCp() < barPixels)
			return true;
		
		if (currentCp <= _cpUpdateDecCheck || currentCp >= _cpUpdateIncCheck)
		{
			if (currentCp == getMaxCp())
			{
				_cpUpdateIncCheck = currentCp + 1;
				_cpUpdateDecCheck = currentCp - _cpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentCp / _cpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_cpUpdateDecCheck = _cpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_cpUpdateIncCheck = _cpUpdateDecCheck + _cpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	private boolean needMpUpdate(int barPixels)
	{
		double currentMp = getCurrentMp();
		
		if (currentMp <= 1.0 || getMaxMp() < barPixels)
			return true;
		
		if (currentMp <= _mpUpdateDecCheck || currentMp >= _mpUpdateIncCheck)
		{
			if (currentMp == getMaxMp())
			{
				_mpUpdateIncCheck = currentMp + 1;
				_mpUpdateDecCheck = currentMp - _mpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentMp / _mpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_mpUpdateDecCheck = _mpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_mpUpdateIncCheck = _mpUpdateDecCheck + _mpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	public int getAllyCrestId()
	{
		if (getClanId() == 0)
			return 0;
		if (getClan().getAllyId() == 0)
			return 0;
		return getClan().getAllyCrestId();
	}
	
	@Override
	protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
	{
		super.onHitTimer(target, damage, crit, miss, soulshot, shld);
	}
	
	//public void queryGameGuard()
	//{
	//	getClient().setGameGuardOk(false);
	//	this.sendPacket(new GameGuardQuery());
	//	if (Config.GAMEGUARD_ENFORCE)
	//	{
	//		ThreadPoolManager.getInstance().scheduleGeneral(new GameGuardCheck(), 30 * 1000);
	//	}
	//}
	
	//class GameGuardCheck implements Runnable
	//{
		//@Override
	//	public void run()
	//	{
	//		if (_client != null && !getClient().isAuthedGG() && isOnline() == 1)
	//		{
	//			AdminData.getInstance().broadcastMessageToGMs("Client " + getClient() + " failed to reply GameGuard query and is being kicked!");
	//			_log.info("Client " + getClient() + " failed to reply GameGuard query and is being kicked!");
	//			getClient().close(new LeaveWorld());
	//		}
	//	}
	//}

	public void doInteract(L2Character target)
	{
		if (target instanceof L2PcInstance)
		{
			L2PcInstance temp = (L2PcInstance) target;
			sendPacket(new MoveToPawn(this, temp, L2Npc.INTERACTION_DISTANCE));
			
			switch (temp.getPrivateStoreType())
			{
				case SELL:
				case PACKAGE_SELL:
					sendPacket(new PrivateStoreListSell(this, temp));
					break;
				
				case BUY:
					sendPacket(new PrivateStoreListBuy(this, temp));
					break;
				
				case MANUFACTURE:
					sendPacket(new RecipeShopSellList(this, temp));
					break;
			}
		}
		else
		{
			if (target != null)
				target.onAction(this);
		}
	}
	
	public void doAutoLoot(L2Attackable target, L2Attackable.RewardItem item)
	{
		if (isInParty())
			getParty().distributeItem(this, item, false, target);
		else if (item.getItemId() == 57)
			addAdena("Loot", item.getCount(), target, true);
		else
			addItem("Loot", item.getItemId(), item.getCount(), target, true);
	}
	
	public void doPickupItem(L2Object object)
	{
		if (object == null || isAlikeDead() || !isVisible())
			return;
		
		// Set the AI Intention to AI_INTENTION_IDLE
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		// Check if the L2Object to pick up is a L2ItemInstance
		if (!(object instanceof L2ItemInstance))
		{
			// dont try to pickup anything that is not an item :)
			_log.warning(L2PcInstance.class.getName() + ": trying to pickup wrong target." + getTarget());
			return;
		}
		
		L2ItemInstance target = (L2ItemInstance) object;
		
		sendPacket(ActionFailed.STATIC_PACKET);
		sendPacket(new StopMove(this));
		
		synchronized (target)
		{
			// Check if the target to pick up is visible
			if (!target.isVisible())
			{
				sendPacket(new DeleteObject(target));
				// Send a Server->Client packet ActionFailed to this L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// you cant pickup items like l2off
			if (getPrivateStoreType() != StoreType.NONE)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (!getInventory().validateWeight(target.getCount() * target.getItem().getWeight()))
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
				return;
			}
			
			if (((isInParty() && getParty().getDistributionType() == PartyLootType.FINDERS_KEEPERS) || !isInParty()) && !_inventory.validateCapacity(target))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.SLOTS_FULL);
				return;
			}
			
			if (isInvul() && !isGM())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
				smsg.addItemName(target.getItemId());
				sendPacket(smsg);
				return;
			}
			
			if (getActiveTradeList() != null)
			{
				sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
				return;
			}
			
			if (target.getOwnerId() != 0 && target.getOwnerId() != getObjectId() && !isInLooterParty(target.getOwnerId()))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				
				if (target.getItemId() == 57)
				{
					SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA);
					smsg.addNumber(target.getCount());
					sendPacket(smsg);
				}
				else if (target.getCount() > 1)
				{
					SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S);
					smsg.addItemName(target.getItemId());
					smsg.addNumber(target.getCount());
					sendPacket(smsg);
				}
				else
				{
					SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
					smsg.addItemName(target.getItemId());
					sendPacket(smsg);
				}
				
				return;
			}
			
			if (target.getItemLootShedule() != null && (target.getOwnerId() == getObjectId() || isInLooterParty(target.getOwnerId())))
				target.resetOwnerTimer();
			
			// Remove the L2ItemInstance from the world and send server->client GetItem packets
			target.pickupMe(this);
			if (Config.SAVE_DROPPED_ITEM)
				ItemsOnGroundManager.getInstance().removeObject(target);
		}
		
		// Auto use herbs - pick up
		if (target.getItemType() == L2EtcItemType.HERB)
		{
			IItemHandler handler = ItemHandler.getInstance().getHandler(target.getItemId());
			if (handler == null)
			{
				_log.fine("No item handler registered for item ID " + target.getItemId() + ".");
			}
			else
			{
				handler.useItem(this, target);
			}
			ItemTable.getInstance().destroyItem("Consume", target, this, null);
		}
		// Cursed Weapons are not distributed
		else if (CursedWeaponsManager.getInstance().isCursed(target.getItemId()))
		{
			addItem("Pickup", target, null, true);
		}
		else
		{
			// if item is instance of L2ArmorType or L2WeaponType broadcast an "Attention" system message
			if (target.getItemType() instanceof L2ArmorType || target.getItemType() instanceof L2WeaponType)
			{
				if (target.getEnchantLevel() > 0)
				{
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2_S3);
					msg.addString(getName());
					msg.addNumber(target.getEnchantLevel());
					msg.addItemName(target.getItemId());
					broadcastPacket(msg, 1400);
				}
				else
				{
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2);
					msg.addString(getName());
					msg.addItemName(target.getItemId());
					broadcastPacket(msg, 1400);
				}
			}
			
			// Check if a Party is in progress
			if (isInParty())
			{
				getParty().distributeItem(this, target);
			}
			else if (target.getItemId() == 57 && getInventory().getAdenaInstance() != null)
			{
				addAdena("Pickup", target.getCount(), null, true);
				ItemTable.getInstance().destroyItem("Pickup", target, this, null);
			}
			// Target is regular item
			else
			{
				addItem("Pickup", target, null, true);
			}
			
			// Like L2OFF Auto-Equip arrows if player has a bow and player picks up arrows.
			if (target.getItem() != null && target.getItem().getItemType() == L2EtcItemType.ARROW && getActiveWeaponInstance() != null && getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND).getItemType() == L2WeaponType.BOW)
				checkAndEquipArrows();
		}
	}
	
	@Override
	public void setTarget(L2Object newTarget)
	{
		if (newTarget != null && !newTarget.isVisible())
			newTarget = null;
		
		if (newTarget != null)
		{
			boolean isParty = (((newTarget instanceof L2PcInstance) && isInParty() && getParty().getPartyMembers().contains(newTarget)));
			
			// Check if the new target is visible
			if (!isParty && (!newTarget.isVisible() || Math.abs(newTarget.getZ() - getZ()) > 1000))
				newTarget = null;
		}
		
		// Can't target and attack festival monsters if not participant
		if ((newTarget instanceof L2FestivalMonsterInstance) && !isFestivalParticipant())
			newTarget = null;
		// Can't target and attack rift invaders if not in the same room
		else if (isInParty() && getParty().isInDimensionalRift())
		{
			byte riftType = getParty().getDimensionalRift().getType();
			byte riftRoom = getParty().getDimensionalRift().getCurrentRoom();
			
			if (newTarget != null && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget.getX(), newTarget.getY(), newTarget.getZ()))
				newTarget = null;
		}
		
		// Get the current target
		L2Object oldTarget = getTarget();
		
		if (oldTarget != null)
		{
			if (oldTarget.equals(newTarget))
				return; // no target change
				
			// Remove the Player from the _statusListener of the old target if it was a Creature
			if (oldTarget instanceof L2Character)
				((L2Character) oldTarget).removeStatusListener(this);
		}
		
		// Verify if it's a static object.
		if (newTarget instanceof L2StaticObjectInstance)
		{
			sendPacket(new MyTargetSelected(newTarget.getObjectId(), 0));
			sendPacket(new StaticObject((L2StaticObjectInstance) newTarget));
			
		}
		// Add the Player to the _statusListener of the new target if it's a Creature
		else if (newTarget instanceof L2Character)
		{
			final L2Character target = (L2Character) newTarget;
			
			// Validate location of the new target.
			if (newTarget.getObjectId() != getObjectId())
				sendPacket(new ValidateLocation(target));
			
			// Show the client his new target.
			sendPacket(new MyTargetSelected(target.getObjectId(), (target.isAutoAttackable(this) || target instanceof L2Summon) ? getLevel() - target.getLevel() : 0));
			
			target.addStatusListener(this);
			
			// Send max/current hp.
			final StatusUpdate su = new StatusUpdate(target.getObjectId());
			su.addAttribute(StatusUpdate.MAX_HP, target.getMaxHp());
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			sendPacket(su);
			
			Broadcast.toKnownPlayers(this, new TargetSelected(getObjectId(), newTarget.getObjectId(), getX(), getY(), getZ()));
		}
		
		if (newTarget == null && getTarget() != null)
		{
			broadcastPacket(new TargetUnselected(this));
			setLastFolkNPC(null);
		}
		else
		{
			if (newTarget instanceof L2NpcInstance)
				setLastFolkNPC((L2NpcInstance) newTarget);
		}
		
		// Target the new L2Object
		super.setTarget(newTarget);
	}
	
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		
		if (weapon == null)
			return getFistsWeaponItem();
		
		return (L2Weapon) weapon.getItem();
	}
	
	public L2ItemInstance getChestArmorInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
	}
	
	public L2Armor getActiveChestArmorItem()
	{
		L2ItemInstance armor = getChestArmorInstance();
		
		if (armor == null)
			return null;
		
		return (L2Armor) armor.getItem();
	}
	
	public boolean isWearingHeavyArmor()
	{
		L2ItemInstance armor = getChestArmorInstance();
		
		if (armor == null)
			return false;
		
		if ((L2ArmorType) armor.getItemType() == L2ArmorType.HEAVY)
			return true;
		
		return false;
	}
	
	public boolean isWearingLightArmor()
	{
		L2ItemInstance armor = getChestArmorInstance();
		
		if ((L2ArmorType) armor.getItemType() == L2ArmorType.LIGHT)
			return true;
		
		return false;
	}
	
	public boolean isWearingMagicArmor()
	{
		L2ItemInstance armor = getChestArmorInstance();
		
		if ((L2ArmorType) armor.getItemType() == L2ArmorType.MAGIC)
			return true;
		
		return false;
	}
	
	public boolean isWearingFormalWear()
	{
		return _IsWearingFormalWear;
	}
	
	public void setIsWearingFormalWear(boolean value)
	{
		_IsWearingFormalWear = value;
	}
	
	public boolean isMarried()
	{
		return _married;
	}
	
	public void setMarried(boolean state)
	{
		_married = state;
	}
	
	public boolean isEngageRequest()
	{
		return _engagerequest;
	}
	
	public void setEngageRequest(boolean state, int playerid)
	{
		_engagerequest = state;
		_engageid = playerid;
	}
	
	public void setMaryRequest(boolean state)
	{
		_marryrequest = state;
	}
	
	public boolean isMaryRequest()
	{
		return _marryrequest;
	}
	
	public void setMarryAccepted(boolean state)
	{
		_marryaccepted = state;
	}
	
	public boolean isMarryAccepted()
	{
		return _marryaccepted;
	}
	
	public int getEngageId()
	{
		return _engageid;
	}
	
	public int getPartnerId()
	{
		return _partnerId;
	}
	
	public void setPartnerId(int partnerid)
	{
		_partnerId = partnerid;
	}
	
	public int getCoupleId()
	{
		return _coupleId;
	}
	
	public void setCoupleId(int coupleId)
	{
		_coupleId = coupleId;
	}
	
	public void EngageAnswer(int answer)
	{
		if (_engagerequest == false)
			return;
		else if (_engageid == 0)
			return;
		else
		{
			L2PcInstance ptarget = L2World.getInstance().getPlayer(_engageid);
			setEngageRequest(false, 0);
			if (ptarget != null)
			{
				if (answer == 1)
				{
					CoupleManager.getInstance().createCouple(ptarget, L2PcInstance.this);
					ptarget.sendMessage("Request to Engage has been >ACCEPTED<");
				}
				else
				{
					ptarget.sendMessage("Request to Engage has been >DENIED<!");
				}
			}
		}
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		L2ItemInstance weapon = getSecondaryWeaponInstance();
		
		if (weapon == null)
			return getFistsWeaponItem();
		
		L2Item item = weapon.getItem();
		
		if (item instanceof L2Weapon)
			return (L2Weapon) item;
		
		return null;
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		// Kill the L2PcInstance
		if (!super.doDie(killer))
			return false;

		synchronized (this)
		{
			if (isFakeDeath())
				stopFakeDeath(null);
		}
		
		Castle castle = null;
		if (getClan() != null)
		{
			castle = CastleManager.getInstance().getCastleByOwner(getClan());
			if (castle != null)
			{
				castle.destroyClanGate();
				castle = null;
			}
		}
		
		if (killer != null)
		{
			L2PcInstance pk = killer.getActingPlayer();
			
			if (pk != null && pk._inEventTvT && _inEventTvT)
			{
				if (TvT._teleport || TvT._started)
				{
					if (!(pk._teamNameTvT.equals(_teamNameTvT)))
					{
						PlaySound ps = Sound.ITEMSOUND_QUEST_ITEMGET.getPacket();
						_countTvTdies++;
						pk._countTvTkills++;
						pk.setTitle("Kills: " + pk._countTvTkills);
						pk.sendPacket(ps);
						TvT.setTeamKillsCount(pk._teamNameTvT, TvT.teamKillsCount(pk._teamNameTvT) + 1);
					}
					else
					{
						pk.sendMessage("You are a teamkiller! Teamkills are not allowed, you will get death penalty and your team will lose one kill!");
						
						// Give Penalty for Team-Kill:
						// 1. Death Penalty + 5
						// 2. Team will lost 1 Kill
						if (pk.getDeathPenaltyBuffLevel() < 10)
						{
							pk.setDeathPenaltyBuffLevel(pk.getDeathPenaltyBuffLevel() + 4);
							pk.increaseDeathPenaltyBuffLevel();
						}
						TvT.setTeamKillsCount(_teamNameTvT, TvT.teamKillsCount(_teamNameTvT) - 1);
					}
					sendMessage("You will be revived and teleported to team spot in " + Config.TVT_REVIVE_DELAY / 1000 + " seconds!");
					ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
					{
						@Override
						public void run()
						{
							teleToLocation(TvT._teamsX.get(TvT._teams.indexOf(_teamNameTvT)), TvT._teamsY.get(TvT._teams.indexOf(_teamNameTvT)), TvT._teamsZ.get(TvT._teams.indexOf(_teamNameTvT)), false);
							doRevive();
						}
					}, Config.TVT_REVIVE_DELAY);
				}
			}
			else if (_inEventTvT)
			{
				if (TvT._teleport || TvT._started)
				{
					sendMessage("You will be revived and teleported to team spot in " + Config.TVT_REVIVE_DELAY / 1000 + " seconds!");
					ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
					{
						@Override
						public void run()
						{
							teleToLocation(TvT._teamsX.get(TvT._teams.indexOf(_teamNameTvT)), TvT._teamsY.get(TvT._teams.indexOf(_teamNameTvT)), TvT._teamsZ.get(TvT._teams.indexOf(_teamNameTvT)), false);
							doRevive();
						}
					}, Config.TVT_REVIVE_DELAY);
				}
			}
			else if (_inEventCTF)
			{
				if (CTF._teleport || CTF._started)
				{
					sendMessage("You will be revived and teleported to team flag in " + Config.CTF_REVIVE_DELAY / 1000 + " seconds!");
					
					if (_haveFlagCTF)
					{
						removeCTFFlagOnDie();
					}
					
					ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
					{
						@Override
						public void run()
						{
							teleToLocation(CTF._teamsX.get(CTF._teams.indexOf(_teamNameCTF)), CTF._teamsY.get(CTF._teams.indexOf(_teamNameCTF)), CTF._teamsZ.get(CTF._teams.indexOf(_teamNameCTF)), false);
							doRevive();
						}
					}, Config.CTF_REVIVE_DELAY);
				}
			}
			else if ((killer instanceof L2PcInstance && ((L2PcInstance) killer)._inEventDM) && _inEventDM)
			{
				if (DM._teleport || DM._started)
				{
					((L2PcInstance) killer)._countDMkills++;
					
					sendMessage("You will be revived and teleported to spot in " + Config.DM_REVIVE_DELAY / 1000 + " seconds!");
					ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
					{
						@Override
						public void run()
						{
							teleToLocation(DM._playerX, DM._playerY, DM._playerZ, false);
							doRevive();
						}
					}, Config.DM_REVIVE_DELAY);
				}
			}
			else if (_inEventDM)
			{
				if (DM._teleport || DM._started)
				{
					sendMessage("You will be revived and teleported to spot in " + Config.DM_REVIVE_DELAY / 1000 + " seconds!");
					ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
					{
						@Override
						public void run()
						{
							teleToLocation(DM._playerX, DM._playerY, DM._playerZ, false);
							doRevive();
						}
					}, Config.DM_REVIVE_DELAY);
				}
			}
			else if (pk != null)
			{
				clanWarKill = (pk.getClan() != null && getClan() != null && !isAcademyMember() && !pk.isAcademyMember() && _clan.isAtWarWith(pk.getClanId()) && pk.getClan().isAtWarWith(getClanId()));
				playerKill = true;
			}

			if (isinZodiac)
				ZodiacMain.OnDeath(this, pk);

			if (atEvent && pk != null)
				pk.kills.add(getName());
			
			// Clear resurrect xp calculation
			setExpBeforeDeath(0);
			
			if (isCursedWeaponEquiped())
			{
				CursedWeaponsManager.getInstance().drop(_cursedWeaponEquipedId, killer);
			}
			else
			{
				if (pk == null || !pk.isCursedWeaponEquiped())
				{
					// if (getKarma() > 0)
					onDieDropItem(killer); // Check if any item should be dropped
					
					if (!(isInsideZone(ZoneId.PVP) && !isInsideZone(ZoneId.SIEGE)))
					{
						boolean isKillerPc = (killer instanceof L2PcInstance);
						if (isKillerPc && ((L2PcInstance) killer).getClan() != null && getClan() != null && !isAcademyMember() && !(((L2PcInstance) killer).isAcademyMember()) && _clan.isAtWarWith(((L2PcInstance) killer).getClanId()) && ((L2PcInstance) killer).getClan().isAtWarWith(_clan.getClanId()))
						{
							if (getClan().getReputationScore() > 0)
							// when your reputation score is 0 or below, the other clan cannot acquire any reputation points
							{
								((L2PcInstance) killer).getClan().setReputationScore(((L2PcInstance) killer).getClan().getReputationScore() + Config.ALT_REPUTATION_SCORE_PER_KILL, true);
								getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(_clan));
								((L2PcInstance) killer).getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(((L2PcInstance) killer).getClan()));
							}
							if (((L2PcInstance) killer).getClan().getReputationScore() > 0)
							// when the opposing sides reputation score is 0 or below, your clans reputation score does not decrease
							{
								_clan.setReputationScore(_clan.getReputationScore() - Config.ALT_REPUTATION_SCORE_PER_KILL, true);
								getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(_clan));
								((L2PcInstance) killer).getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(((L2PcInstance) killer).getClan()));
							}
						}
						// Reduce player's xp and karma.
						final int lvl = getLevel();
						if (lvl > 9  && !hasSkill(L2Skill.SKILL_LUCKY))
							deathPenalty(pk != null && getClan() != null && pk.getClan() != null && (getClan().isAtWarWith(pk.getClanId()) || pk.getClan().isAtWarWith(getClanId())), pk != null, killer instanceof L2SiegeGuardInstance);
					}
				}
			}
		}
		
		setPvpFlag(0); // Clear the pvp flag
		
		// Unsummon Cubics
		if (_cubics.size() > 0)
		{
			for (L2CubicInstance cubic : _cubics.values())
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			
			_cubics.clear();
		}
		
		if (_forceBuff != null)
		{
			_forceBuff.delete();
		}
		
		for (L2Character character : L2World.getInstance().getVisibleObjects(this, L2Character.class))
			if (character.getForceBuff() != null && character.getForceBuff().getTarget() == this)
			{
				character.abortCast();
			}
		
		if (isInParty() && getParty().isInDimensionalRift())
		{
			getParty().getDimensionalRift().getDeadMemberList().add(this);
		}
		
		// calculate death penalty buff
		calculateDeathPenaltyBuffLevel(killer);
		
		stopRentPet();
		WaterTaskManager.getInstance().remove(this);
		return true;
	}
	
	private void onDieDropItem(L2Character killer)
	{
		if (atEvent || (TvT._started && _inEventTvT) || (DM._started && _inEventDM) || (CTF._started && _inEventCTF) || killer == null)
			return;
		
		if ((getKarma() <= 0) && (killer instanceof L2PcInstance) && (((L2PcInstance) killer).getClan() != null) && (getClan() != null) && (((L2PcInstance) killer).getClan().isAtWarWith(getClanId())
		// ||
		// this.getClan().isAtWarWith(((L2PcInstance)killer).getClanId())
		))
			return;
		
		if (!isInsideZone(ZoneId.PVP) && !isGM())
		{
			boolean isKillerNpc = (killer instanceof L2Npc);
			int pkLimit = Config.KARMA_PK_LIMIT;
			
			int dropEquip = 0;
			int dropEquipWeapon = 0;
			int dropItem = 0;
			int dropLimit = 0;
			int dropPercent = 0;
			
			if (getKarma() > 0 && getPkKills() >= pkLimit)
			{
				if (getPremiumService() == 1)
				{
					dropPercent = Config.PREMIUM_KARMA_RATE_DROP;
					dropEquip = Config.PREMIUM_KARMA_RATE_DROP_EQUIP;
					dropEquipWeapon = Config.PREMIUM_KARMA_RATE_DROP_EQUIP_WEAPON;
					dropItem = Config.PREMIUM_KARMA_RATE_DROP_ITEM;
					dropLimit = Config.PREMIUM_KARMA_DROP_LIMIT;
				}
				else
				{
					dropPercent = Config.KARMA_RATE_DROP;
					dropEquip = Config.KARMA_RATE_DROP_EQUIP;
					dropEquipWeapon = Config.KARMA_RATE_DROP_EQUIP_WEAPON;
					dropItem = Config.KARMA_RATE_DROP_ITEM;
					dropLimit = Config.KARMA_DROP_LIMIT;
				}
			}
			else if (isKillerNpc && getLevel() > 4 && !isFestivalParticipant())
			{
				if (getPremiumService() == 1)
				{
					dropPercent = Config.PREMIUM_PLAYER_RATE_DROP;
					dropEquip = Config.PREMIUM_PLAYER_RATE_DROP_EQUIP;
					dropEquipWeapon = Config.PREMIUM_PLAYER_RATE_DROP_EQUIP_WEAPON;
					dropItem = Config.PREMIUM_PLAYER_RATE_DROP_ITEM;
					dropLimit = Config.PREMIUM_PLAYER_DROP_LIMIT;
				}
				else
				{
					dropPercent = Config.PLAYER_RATE_DROP;
					dropEquip = Config.PLAYER_RATE_DROP_EQUIP;
					dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON;
					dropItem = Config.PLAYER_RATE_DROP_ITEM;
					dropLimit = Config.PLAYER_DROP_LIMIT;
				}
			}
			
			int dropCount = 0;
			int itemDropPercent = 0;
			List<Integer> nonDroppableList = new ArrayList<>();
			List<Integer> nonDroppableListPet = new ArrayList<>();
			nonDroppableList = Config.KARMA_LIST_NONDROPPABLE_ITEMS;
			nonDroppableListPet = Config.KARMA_LIST_NONDROPPABLE_ITEMS;
			if (dropPercent > 0 && Rnd.get(100) < dropPercent)
			{
				
				for (L2ItemInstance itemDrop : getInventory().getItems())
				{
					if (itemDrop == null)
						break;
					
					// Don't drop
					if (!itemDrop.isDropable() || itemDrop.isAugmented() || // Dont drop augmented items
					itemDrop.isShadowItem() || // Dont drop Shadow Items
					itemDrop.getItemId() == 57 || // Adena
					itemDrop.getItem().getType2() == L2Item.TYPE2_QUEST || // Quest Items
					nonDroppableList.contains(itemDrop.getItemId()) || // Item listed in the non droppable item list
					nonDroppableListPet.contains(itemDrop.getItemId()) || // Item listed in the non droppable pet item list
					getPet() != null && getPet().getControlItemId() == itemDrop.getItemId() // Control Item of active pet
					)
					{
						continue;
					}
					if (itemDrop.isEquipped())
					{
						// Set proper chance according to Item type of equipped Item
						itemDropPercent = itemDrop.getItem().getType2() == L2Item.TYPE2_WEAPON ? dropEquipWeapon : dropEquip;
						getInventory().unEquipItemInSlot(itemDrop.getLocationSlot());
					}
					else
						itemDropPercent = dropItem; // Item in inventory
						
					// NOTE: Each time an item is dropped, the chance of another item being dropped gets lesser (dropCount * 2)
					if (Rnd.get(100) < itemDropPercent)
					{
						dropItem("DieDrop", itemDrop, killer, true);
						
						if (++dropCount >= dropLimit)
							break;
						
					}
				}
			}
		}
	}
	
	public void onKillUpdatePvPKarma(L2Character target)
	{
		if (target == null)
			return;
		if (!(target instanceof L2Playable))
			return;
		
		// Rank PvP System by Masterio
		_RPSCookie.runPvpTask(this, target);
		
		if (_inEventCTF || _inEventTvT || _inEventVIP || _inEventDM || isinZodiac)
			return;
		
		L2PcInstance targetPlayer = null;
		if (target instanceof L2PcInstance)
		{
			targetPlayer = (L2PcInstance) target;
		}
		else if (target instanceof L2Summon)
		{
			targetPlayer = ((L2Summon) target).getOwner();
		}
		
		if (targetPlayer == null)
			return; // Target player is null
		if (targetPlayer == this)
			return; // Target player is self
			
		if (isCursedWeaponEquiped())
		{
			CursedWeaponsManager.getInstance().increaseKills(_cursedWeaponEquipedId);
			return;
		}
		
		// If in duel and you kill (only can kill l2summon), do nothing
		if (isInDuel() && targetPlayer.isInDuel())
			return;
		
		// If in Arena, do nothing
		if (isInsideZone(ZoneId.PVP) && targetPlayer.isInsideZone(ZoneId.PVP))
		{
			// Until the zone was a siege zone. Check also if victim was a player. Randomers aren't counted.
			if (target instanceof L2PcInstance && getSiegeState() > 0 && targetPlayer.getSiegeState() > 0 && getSiegeState() != targetPlayer.getSiegeState())
			{
				// Now check clan relations.
				final L2Clan killerClan = getClan();
				if (killerClan != null)
					killerClan.addSiegeKill();
				
				final L2Clan targetClan = targetPlayer.getClan();
				if (targetClan != null)
					targetClan.addSiegeDeath();
			}
			return;
		}
		
		// Check if it's pvp (cases : regular, wars, victim is PKer)
		if (checkIfPvP(target) || (targetPlayer.getClan() != null && getClan() != null && getClan().isAtWarWith(targetPlayer.getClanId()) && targetPlayer.getClan().isAtWarWith(getClanId()) && targetPlayer.getPledgeType() != L2Clan.SUBUNIT_ACADEMY && getPledgeType() != L2Clan.SUBUNIT_ACADEMY) || (targetPlayer.getKarma() > 0))
		{
			if (target instanceof L2PcInstance)
			{
				// Add PvP point to attacker.
				increasePvpKills();
			}
		}
		// Otherwise, killer is considered as a PKer.
		else if (targetPlayer.getKarma() == 0 && targetPlayer.getPvpFlag() == 0)
		{
			// PK Points are increased only if you kill a player.
			if (target instanceof L2PcInstance)
				setPkKills(getPkKills() + 1);
			
			increasePkKillsAndKarma(targetPlayer.getLevel(), target instanceof L2Summon);
			
			// Send UserInfo packet to attacker with its Karma and PK Counter
			sendPacket(new UserInfo(this));
		}
	}
	
	public void increasePvpKills()
	{
		if ((TvT._started && _inEventTvT) || isinZodiac || (DM._started && _inEventDM) || (CTF._started && _inEventCTF))
			return;
		
		setPvpKills(getPvpKills() + 1);
		
		broadcastUserInfo();
		// Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
		sendPacket(new UserInfo(this));
	}
	
	@Override
	public boolean isInFunEvent()
	{
		return (atEvent || (TvT._started && _inEventTvT) || isinZodiac || (DM._started && _inEventDM) || (CTF._started && _inEventCTF) && !isGM());
	}
	
	public void increasePkKillsAndKarma(int targLV, boolean isSummon)
	{
		if ((TvT._started && _inEventTvT) || isinZodiac || (DM._started && _inEventDM) || (CTF._started && _inEventCTF))
			return;
		
		int newKarma = Formulas.calculateKarmaGain(getPkKills(), isSummon);
		
		// prevent overflow
		if (getKarma() > (Integer.MAX_VALUE - newKarma))
		{
			newKarma = Integer.MAX_VALUE - getKarma();
		}
		
		setKarma(getKarma() + newKarma);
		broadcastUserInfo();
		// Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
		sendPacket(new UserInfo(this));
	}
	
	public void updatePvPStatus()
	{
		if ((TvT._started && _inEventTvT) || isinZodiac || (DM._started && _inEventDM) || (CTF._started && _inEventCTF))
			return;
		
		if (isInsideZone(ZoneId.PVP) || isInOlympiadMode() && isOlympiadStart())
			return;

		PvpFlagTaskManager.getInstance().add(this, Config.PVP_NORMAL_TIME);
		
		if (getPvpFlag() == 0)
			updatePvPFlag(1);
	}
	
	public void updatePvPStatus(L2Character target)
	{
		L2PcInstance player_target = null;
		
		if (target instanceof L2PcInstance)
		{
			player_target = (L2PcInstance) target;
		}
		else if (target instanceof L2Summon)
		{
			player_target = ((L2Summon) target).getOwner();
		}
		
		if (player_target == null || isInOlympiadMode() && isOlympiadStart())
			return;

		if ((TvT._started && _inEventTvT && player_target._inEventTvT) || isinZodiac || (DM._started && _inEventDM && player_target._inEventDM) || (CTF._started && _inEventCTF && player_target._inEventCTF))
			return;
		
		if (isInDuel() && player_target.getDuelId() == getDuelId())
			return;
		
		if ((!isInsideZone(ZoneId.PVP) || !player_target.isInsideZone(ZoneId.PVP)) && player_target.getKarma() == 0)
		{
			PvpFlagTaskManager.getInstance().add(this, checkIfPvP(player_target) ? Config.PVP_PVP_TIME : Config.PVP_NORMAL_TIME);
			
			if (getPvpFlag() == 0)
				updatePvPFlag(1);
		}
	}
	
	public void restoreExp(double restorePercent)
	{
		if (getExpBeforeDeath() > 0)
		{
			// Restore the specified % of lost experience.
			getStat().addExp(Math.round(((getExpBeforeDeath() - getExp()) * restorePercent) / 100));
			setExpBeforeDeath(0);
		}
	}
	
	public void deathPenalty(boolean atWar, boolean killedByPlayable, boolean killedBySiegeNpc)
	{
		
		if (isInsideZone(ZoneId.PVP))
		{
			// No xp loss for siege participants inside siege zone.
			if (isInsideZone(ZoneId.SIEGE))
			{
				if (isInSiege() && (killedByPlayable || killedBySiegeNpc))
					return;
			}
			// No xp loss for arenas participants killed by playable.
			else if (killedByPlayable)
				return;
		}
		
		// Get the level of the Player
		final int lvl = getLevel();
		
		// The death steal you some Exp
		double percentLost = PlayerExpLost.getExpLost(lvl);
		
		if (getKarma() > 0)
			percentLost *= Config.RATE_KARMA_EXP_LOST;
		
		if (isFestivalParticipant() || atWar || isInsideZone(ZoneId.SIEGE))
			percentLost /= 4.0;
		
		// Calculate the Experience loss
		long lostExp = lvl < Experience.MAX_LEVEL ? Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100) : 
		Math.round((getStat().getExpForLevel(Experience.MAX_LEVEL) - getStat().getExpForLevel(Experience.MAX_LEVEL - 1)) * percentLost / 100);
		
		// Get the Experience before applying penalty
		setExpBeforeDeath(getExp());
		
		// Set new karma
		updateKarmaLoss(lostExp);
		
		// Set the new Experience value of the Player
		getStat().removeExp(lostExp);
	}
	
	public void updateKarmaLoss(long exp)
	{
		if (!isCursedWeaponEquiped() && getKarma() > 0)
		{
			int karmaLost = Formulas.calculateKarmaLost(getLevel(), exp);
			if (karmaLost > 0)
				setKarma(getKarma() - karmaLost);
		}
	}
	
	public void increaseLevel()
	{
		// Set the current HP and MP of the L2Character, Launch/Stop a HP/MP/CP
		// Regeneration Task and send StatusUpdate packet to all other
		// L2PcInstance to inform (exclusive broadcast)
		if (!isDead())
		{
			setCurrentHpMp(getMaxHp(), getMaxMp());
			setCurrentCp(getMaxCp());
		}
	}
	
	public void stopAllTimers()
	{
		stopHpMpRegeneration();
		stopWarnUserTakeBreak();
		
		stopRentPet();
		stopJailTask(true);
		
		AttackStanceTaskManager.getInstance().remove(this);
		PvpFlagTaskManager.getInstance().remove(this);
		WaterTaskManager.getInstance().remove(this);
	}
	
	@Override
	public L2Summon getPet()
	{
		return _summon;
	}
	
	public void setPet(L2Summon summon)
	{
		_summon = summon;
	}
	
	public L2TamedBeastInstance getTrainedBeast()
	{
		return _tamedBeast;
	}
	
	public void setTrainedBeast(L2TamedBeastInstance tamedBeast)
	{
		_tamedBeast = tamedBeast;
	}
	
	public L2Request getRequest()
	{
		return _request;
	}
	
	public synchronized void setActiveRequester(L2PcInstance requester)
	{
		_activeRequester = requester;
	}
	
	public L2PcInstance getActiveRequester()
	{
		return _activeRequester;
	}
	
	public boolean isProcessingRequest()
	{
		return _activeRequester != null || _requestExpireTime > GameTimeController.getInstance().getGameTicks();
	}
	
	public boolean isProcessingTransaction()
	{
		return _activeRequester != null || _activeTradeList != null || _requestExpireTime > GameTimeController.getInstance().getGameTicks();
	}
	
	public void onTransactionRequest(L2PcInstance partner)
	{
		_requestExpireTime = GameTimeController.getInstance().getGameTicks() + REQUEST_TIMEOUT * GameTimeController.TICKS_PER_SECOND;
		partner.setActiveRequester(this);
	}
	
	public void onTransactionResponse()
	{
		_requestExpireTime = 0;
	}
	
	public void setActiveWarehouse(ItemContainer warehouse)
	{
		_activeWarehouse = warehouse;
	}
	
	public ItemContainer getActiveWarehouse()
	{
		return _activeWarehouse;
	}
	
	public void setActiveTradeList(TradeList tradeList)
	{
		_activeTradeList = tradeList;
	}
	
	public TradeList getActiveTradeList()
	{
		return _activeTradeList;
	}
	
	public void onTradeStart(L2PcInstance partner)
	{
		_activeTradeList = new TradeList(this);
		_activeTradeList.setPartner(partner);
		
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BEGIN_TRADE_WITH_S1).addString(partner.getName()));
		sendPacket(new TradeStart(this));
	}
	
	public void onTradeConfirm(L2PcInstance partner)
	{
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CONFIRMED_TRADE).addString(partner.getName()));
		
		partner.sendPacket(TradePressOwnOk.STATIC_PACKET);
		sendPacket(TradePressOtherOk.STATIC_PACKET);
	}
	
	public void onTradeCancel(L2PcInstance partner)
	{
		if (_activeTradeList == null)
			return;
		
		_activeTradeList.lock();
		_activeTradeList = null;
		
		sendPacket(new SendTradeDone(0));
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S1_CANCELED_TRADE);
		msg.addString(partner.getName());
		sendPacket(msg);
	}
	
	public boolean isRequestExpired()
	{
		return !(_requestExpireTime > GameTimeController.getInstance().getGameTicks());
	}
	
	public void onTradeFinish(boolean successfull)
	{
		_activeTradeList = null;
		sendPacket(new SendTradeDone(1));
		if (successfull)
			sendPacket(SystemMessageId.TRADE_SUCCESSFUL);
	}
	
	public void startTrade(L2PcInstance partner)
	{
		onTradeStart(partner);
		partner.onTradeStart(this);
	}
	
	public void cancelActiveTrade()
	{
		if (_activeTradeList == null)
			return;
		
		L2PcInstance partner = _activeTradeList.getPartner();
		if (partner != null)
			partner.onTradeCancel(this);
		onTradeCancel(this);
	}
	
	public L2ManufactureList getCreateList()
	{
		return _createList;
	}
	
	public void setCreateList(L2ManufactureList x)
	{
		_createList = x;
	}
	
	public TradeList getSellList()
	{
		if (_sellList == null)
			_sellList = new TradeList(this);
		return _sellList;
	}
	
	public TradeList getBuyList()
	{
		if (_buyList == null)
			_buyList = new TradeList(this);
		return _buyList;
	}
	
	public void setPrivateStoreType(StoreType type)
	{
		_privatestore = type;
	}
	
	public StoreType getPrivateStoreType()
	{
		return _privatestore;
	}
	
	public void setSkillLearningClassId(ClassId classId)
	{
		_skillLearningClassId = classId;
	}
	
	public ClassId getSkillLearningClassId()
	{
		return _skillLearningClassId;
	}
	
	public void setClan(L2Clan clan)
	{
		_clan = clan;
		setTitle("");
		
		if (clan == null)
		{
			_clanId = 0;
			_clanPrivileges = 0;
			_pledgeType = 0;
			_powerGrade = 0;
			_lvlJoinedAcademy = 0;
			_apprentice = 0;
			_sponsor = 0;
			return;
		}
		
		if (!clan.isMember(getName()))
		{
			// char has been kicked from clan
			setClan(null);
			return;
		}
		
		_clanId = clan.getClanId();
	}
	
	public L2Clan getClan()
	{
		return _clan;
	}
	
	public boolean isClanLeader()
	{
		return getClan() != null && getObjectId() == getClan().getLeaderId();
	}
	
	@Override
	protected void reduceArrowCount()
	{
		L2ItemInstance arrows = getInventory().destroyItem("Consume", getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, this, null);
		
		if (Config.DEBUG)
		{
			_log.fine("arrow count:" + (arrows == null ? 0 : arrows.getCount()));
		}
		
		if (arrows == null || arrows.getCount() == 0)
		{
			getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
			_arrowItem = null;
			
			if (Config.DEBUG)
			{
				_log.fine("removed arrows count");
			}
			sendPacket(new ItemList(this, false));
		}
		else
		{
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(arrows);
				sendPacket(iu);
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
		}
	}
	
	@Override
	protected boolean checkAndEquipArrows()
	{
		// Check if nothing is equiped in left hand
		if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			// Get the L2ItemInstance of the arrows needed for this bow
			_arrowItem = getInventory().findArrowForBow(getActiveWeaponItem());
			
			if (_arrowItem != null)
			{
				// Equip arrows needed in left hand
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);
				
				// Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
				ItemList il = new ItemList(this, false);
				sendPacket(il);
			}
		}
		else
		{
			// Get the L2ItemInstance of arrows equiped in left hand
			_arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}
		
		return _arrowItem != null;
	}
	
	public boolean disarmWeapons()
	{
		// Don't allow disarming a cursed weapon
		if (isCursedWeaponEquiped())
			return false;
		
		// Unequip the weapon
		L2ItemInstance wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn == null)
		{
			wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
		}
		if (wpn != null)
		{
			if (wpn.isWear())
				return false;
			
			// Remove augementation boni on unequip
			if (wpn.isAugmented())
			{
				wpn.getAugmentation().removeBoni(this);
			}
			
			L2ItemInstance[] unequiped = getInventory().unEquipItemInBodySlotAndRecord(wpn);
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequiped)
			{
				iu.addModifiedItem(element);
			}
			sendPacket(iu);
			
			abortAttack();
			broadcastUserInfo();
			
			// this can be 0 if the user pressed the right mousebutton twice very fast
			if (unequiped.length > 0)
			{
				SystemMessage sm = null;
				if (unequiped[0].getEnchantLevel() > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(unequiped[0].getEnchantLevel());
					sm.addItemName(unequiped[0].getItemId());
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(unequiped[0].getItemId());
				}
				sendPacket(sm);
			}
		}
		
		// Unequip the shield
		L2ItemInstance sld = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (sld != null)
		{
			if (sld.isWear())
				return false;
			
			L2ItemInstance[] unequiped = getInventory().unEquipItemInBodySlotAndRecord(sld);
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequiped)
			{
				iu.addModifiedItem(element);
			}
			sendPacket(iu);
			
			abortAttack();
			broadcastUserInfo();
			
			// this can be 0 if the user pressed the right mousebutton twice very fast
			if (unequiped.length > 0)
			{
				SystemMessage sm = null;
				if (unequiped[0].getEnchantLevel() > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(unequiped[0].getEnchantLevel());
					sm.addItemName(unequiped[0].getItemId());
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(unequiped[0].getItemId());
				}
				sendPacket(sm);
			}
		}
		return true;
	}
	
	@Override
	public boolean isUsingDualWeapon()
	{
		L2Weapon weaponItem = getActiveWeaponItem();
		if (weaponItem == null)
			return false;
		
		if (weaponItem.getItemType() == L2WeaponType.DUAL)
			return true;
		else if (weaponItem.getItemType() == L2WeaponType.DUALFIST)
			return true;
		else if (weaponItem.getItemId() == 248) // orc fighter fists
			return true;
		else if (weaponItem.getItemId() == 252) // orc mage fists
			return true;
		else
			return false;
	}
	
	public void setUptime(long time)
	{
		_uptime = time;
	}
	
	public long getUptime()
	{
		return System.currentTimeMillis() - _uptime;
	}
	
	@Override
	public boolean isInvul()
	{
		return (_isInvul || (_isTeleporting || _protectEndTime > GameTimeController.getInstance().getGameTicks()));
	}
	
	@Override
	public boolean isInParty()
	{
		return _party != null;
	}
	
	public void setParty(L2Party party)
	{
		_party = party;
	}
	
	public void joinParty(L2Party party)
	{
		if (party != null)
		{
			// First set the party otherwise this wouldn't be considered
			// as in a party into the L2Character.updateEffectIcons() call.
			_party = party;
			party.addPartyMember(this);
		}
	}
	
	public void leaveParty()
	{
		if (isInParty())
		{
			_party.removePartyMember(this);
			_party = null;
		}
	}
	
	@Override
	public L2Party getParty()
	{
		return _party;
	}
	
	public boolean isGM()
	{
		return getAccessLevel().isGm();
	}
	
	public void setAccessLevel(int level)
	{
		_accessLevel = AdminData.getInstance().getAccessLevel(level);
		getAppearance().setNameColor(_accessLevel.getNameColor());
		getAppearance().setTitleColor(_accessLevel.getTitleColor());
		broadcastUserInfo();
		
		CharNameTable.getInstance().addName(this);
		
		if (!AdminData.getInstance().hasAccessLevel(level))
			_log.warning(L2PcInstance.class.getName() + ": Tryed to set unregistered access level " + level + " for " + toString() + ". Setting access level without privileges!");
	}
	
	public void setAccountAccesslevel(int level)
	{
		LoginServerThread.getInstance().sendAccessLevel(getAccountName(), level);
	}
	
	public L2AccessLevel getAccessLevel()
	{
		if (Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			return AdminData.getInstance().getMasterAccessLevel();
		else if (_accessLevel == null)
		{
			setAccessLevel(0);
		}
		
		return _accessLevel;
	}
	
	@Override
	public double getLevelMod()
	{
		return (100.0 - 11 + getLevel()) / 100.0;
	}
	
	public void cancelCastMagic()
	{
		// Set the Intention of the AI to AI_INTENTION_IDLE
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		// Enable all skills (set _allSkillsDisabled to False)
		enableAllSkills();
		
		// Send a Server->Client Packet MagicSkillCanceld to the L2PcInstance
		// and all L2PcInstance in the _KnownPlayers of the L2Character (broadcast)
		MagicSkillCanceld msc = new MagicSkillCanceld(getObjectId());
		
		// Broadcast the packet to self and known players.
		Broadcast.toSelfAndKnownPlayersInRadius(this, msc, 810000);
	}
	
	public void updateAndBroadcastStatus(int broadcastType)
	{
		refreshOverloaded();
		refreshExpertisePenalty();
		// Send a Server->Client packet UserInfo to this L2PcInstance and
		// CharInfo to all L2PcInstance in its _KnownPlayers (broadcast)
		if (broadcastType == 1)
		{
			this.sendPacket(new UserInfo(this));
		}
		if (broadcastType == 2)
		{
			broadcastUserInfo();
		}
	}
	
	public void broadcastKarma()
	{
		sendPacket(new UserInfo(this));

		if (getPet() != null)
			sendPacket(new RelationChanged(getPet(), getRelation(this), false));

		broadcastRelationChanged();
	}
	
	public void setOnlineStatus(boolean isOnline)
	{
		if (_isOnline != isOnline)
		{
			_isOnline = isOnline;
		}
		// Update the characters table of the database with online status and lastAccess (called when login and logout)
		updateOnlineStatus();
	}
	
	public void setIsIn7sDungeon(boolean isIn7sDungeon)
	{
		if (_isIn7sDungeon != isIn7sDungeon)
		{
			_isIn7sDungeon = isIn7sDungeon;
		}
		
		updateIsIn7sDungeonStatus();
	}
	
	public void updateOnlineStatus()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_Id=?");
			statement.setInt(1, isOnline());
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": could not set char online status:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void updateIsIn7sDungeonStatus()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET isIn7sDungeon=?, lastAccess=? WHERE obj_Id=?");
			statement.setInt(1, isIn7sDungeon() ? 1 : 0);
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": could not set char isIn7sDungeon status:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private boolean createDb()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement(INSERT_CHARACTER);
			statement.setString(1, _accountName);
			statement.setInt(2, getObjectId());
			statement.setString(3, getName());
			statement.setInt(4, getLevel());
			statement.setInt(5, getMaxHp());
			statement.setDouble(6, getCurrentHp());
			statement.setInt(7, getMaxCp());
			statement.setDouble(8, getCurrentCp());
			statement.setInt(9, getMaxMp());
			statement.setDouble(10, getCurrentMp());
			statement.setInt(11, getAccuracy());
			statement.setInt(12, getCriticalHit(null, null));
			statement.setInt(13, getEvasionRate(null));
			statement.setInt(14, getMAtk(null, null));
			statement.setInt(15, getMDef(null, null));
			statement.setInt(16, getMAtkSpd());
			statement.setInt(17, getPAtk(null));
			statement.setInt(18, getPDef(null));
			statement.setInt(19, getPAtkSpd());
			statement.setDouble(20, getRunSpeed());
			statement.setDouble(21, getWalkSpeed());
			statement.setInt(22, getSTR());
			statement.setInt(23, getCON());
			statement.setInt(24, getDEX());
			statement.setInt(25, getINT());
			statement.setInt(26, getMEN());
			statement.setInt(27, getWIT());
			statement.setInt(28, getAppearance().getFace());
			statement.setInt(29, getAppearance().getHairStyle());
			statement.setInt(30, getAppearance().getHairColor());
			statement.setInt(31, getAppearance().getSex().ordinal());
			statement.setDouble(32, 1);
			statement.setDouble(33, 1);
			
			statement.setDouble(34, getTemplate().getCollisionRadius(getAppearance().getSex()));
			statement.setDouble(35, getTemplate().getCollisionHeight(getAppearance().getSex()));
			
			statement.setLong(36, getExp());
			statement.setInt(37, getSp());
			statement.setInt(38, getKarma());
			statement.setInt(39, getPvpKills());
			statement.setInt(40, getPkKills());
			statement.setInt(41, getClanId());
			statement.setInt(42, getMaxLoad());
			statement.setInt(43, getRace().ordinal());
			statement.setInt(44, getClassId().getId());
			statement.setLong(45, getDeleteTimer());
			statement.setInt(46, hasDwarvenCraft() ? 1 : 0);
			statement.setString(47, getTitle());
			statement.setInt(48, getAccessLevel().getLevel());
			statement.setInt(49, isOnline());
			statement.setInt(50, isIn7sDungeon() ? 1 : 0);
			statement.setInt(51, getClanPrivileges());
			statement.setInt(52, getWantsPeace());
			statement.setInt(53, getBaseClass());
			statement.setInt(54, isNewbie() ? 1 : 0);
			statement.setInt(55, isNoble() ? 1 : 0);
			statement.setLong(56, 0);
			statement.setLong(57, System.currentTimeMillis());
			statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": Could not insert char data: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static L2PcInstance restore(int objectId)
	{
		L2PcInstance player = null;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Retrieve the L2PcInstance from the characters table of the database
			PreparedStatement statement = con.prepareStatement(RESTORE_CHARACTER);
			statement.setInt(1, objectId);
			ResultSet rset = statement.executeQuery();
			
			double currentCp = 0;
			double currentHp = 0;
			double currentMp = 0;
			
			while (rset.next())
			{
				final int activeClassId = rset.getInt("classid");
				final L2PcTemplate template = CharTemplateData.getInstance().getTemplate(activeClassId);
				PcAppearance app = new PcAppearance(rset.getByte("face"), rset.getByte("hairColor"), rset.getByte("hairStyle"), Sex.values()[rset.getInt("sex")]);
				
				player = new L2PcInstance(objectId, template, rset.getString("account_name"), app);
				
				player.setName(rset.getString("char_name"));
				player._lastAccess = rset.getLong("lastAccess");
				
				player.getStat().setExp(rset.getLong("exp"));
				player.setExpBeforeDeath(rset.getLong("expBeforeDeath"));
				player.getStat().setLevel(rset.getByte("level"));
				player.getStat().setSp(rset.getInt("sp"));
				
				player.setWantsPeace(rset.getInt("wantspeace"));
				
				player.setHeading(rset.getInt("heading"));
				
				player.setKarma(rset.getInt("karma"));
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));
				player.setOnlineTime(rset.getLong("onlinetime"));
				player.setNewbie(rset.getInt("newbie") == 1);
				player.setNoble(rset.getInt("nobless") == 1);
				
				// l2jhellas Donator and Hero Mod
				player.setHero(rset.getInt("hero") == 1);
				player.setDonator(rset.getInt("donator") == 1);
				player.setClanJoinExpiryTime(rset.getLong("clan_join_expiry_time"));
				if (player.getClanJoinExpiryTime() < System.currentTimeMillis())
				{
					player.setClanJoinExpiryTime(0);
				}
				player.setClanCreateExpiryTime(rset.getLong("clan_create_expiry_time"));
				if (player.getClanCreateExpiryTime() < System.currentTimeMillis())
				{
					player.setClanCreateExpiryTime(0);
				}
				
				int clanId = rset.getInt("clanid");
				player.setPowerGrade((int) rset.getLong("power_grade"));
				player.setPledgeType(rset.getInt("subpledge"));
				player.setLastRecomUpdate(rset.getLong("last_recom_date"));
				// player.setApprentice(rset.getInt("apprentice"));
				
				if (clanId > 0)
				{
					player.setClan(ClanTable.getInstance().getClan(clanId));
				}
				
				if (player.getClan() != null)
				{
					if (player.getClan().getLeaderId() != player.getObjectId())
					{
						if (player.getPowerGrade() == 0)
						{
							player.setPowerGrade(5);
						}
						player.setClanPrivileges(player.getClan().getRankPrivs(player.getPowerGrade()));
					}
					else
					{
						player.setClanPrivileges(L2Clan.CP_ALL);
						player.setPowerGrade(1);
					}
				}
				else
				{
					player.setClanPrivileges(L2Clan.CP_NOTHING);
				}
				
				player.setDeleteTimer(rset.getLong("deletetime"));
				
				player.setTitle(rset.getString("title"));
				player.setAccessLevel(rset.getInt("accesslevel"));
				player.setFistsWeaponItem(player.findFistsWeaponItem(activeClassId));
				player.setUptime(System.currentTimeMillis());
				
				currentHp = rset.getDouble("curHp");
				player.setCurrentHp(rset.getDouble("curHp"));
				currentCp = rset.getDouble("curCp");
				player.setCurrentCp(rset.getDouble("curCp"));
				currentMp = rset.getDouble("curMp");
				player.setCurrentMp(rset.getDouble("curMp"));
				
				// Check recs
				player.checkRecom(rset.getInt("rec_have"), rset.getInt("rec_left"));
				
				player._classIndex = 0;
				try
				{
					player.setBaseClass(rset.getInt("base_class"));
				}
				catch (Exception e)
				{
					player.setBaseClass(activeClassId);
					_log.warning(L2PcInstance.class.getSimpleName() + ": setting base class");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				
				// Restore Subclass Data (cannot be done earlier in function)
				if (restoreSubClassData(player))
				{
					if (activeClassId != player.getBaseClass())
					{
						for (SubClass subClass : player.getSubClasses().values())
							if (subClass.getClassId() == activeClassId)
							{
								player._classIndex = subClass.getClassIndex();
							}
					}
				}
				if (player.getClassIndex() == 0 && activeClassId != player.getBaseClass())
				{
					// Subclass in use but doesn't exist in DB -
					// a possible restart-while-modifysubclass cheat has been attempted.
					// Switching to use base class
					player.setClassId(player.getBaseClass());
					_log.warning(L2PcInstance.class.getSimpleName() + ": Player " + player.getName() + " reverted to base class. Possibly has tried a relogin exploit while subclassing.");
				}
				else
				{
					player._activeClass = activeClassId;
				}
				
				player.setApprentice(rset.getInt("apprentice"));
				player.setSponsor(rset.getInt("sponsor"));
				player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
				player.setIsIn7sDungeon((rset.getInt("isin7sdungeon") == 1) ? true : false);
				player.setInJail((rset.getInt("in_jail") == 1) ? true : false);
				if (player.isInJail())
				{
					player.setJailTimer(rset.getLong("jail_timer"));
				}
				else
				{
					player.setJailTimer(0);
				}
				
				CursedWeaponsManager.getInstance().checkPlayer(player);
				
				player.setAllianceWithVarkaKetra(rset.getInt("varka_ketra_ally"));
				
				player.setDeathPenaltyBuffLevel(rset.getInt("death_penalty_level"));
				
				player.setChatFilterCount(rset.getInt("chat_filter_count"));
				restorePremServiceData(player, rset.getString("account_name"));
				// Add the L2PcInstance object in _allObjects L2World.getInstance().storeObject(player);
				// Set the x,y,z position of the L2PcInstance and make it invisible
				player.setXYZInvisible(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
				// Retrieve the name and ID of the other characters assigned to this account.
				try (PreparedStatement stmt = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id<>?"))
				{
					stmt.setString(1, player._accountName);
					stmt.setInt(2, objectId);
					try (ResultSet chars = stmt.executeQuery())
					{
						while (chars.next())
						{
							Integer charId = chars.getInt("obj_Id");
							String charName = chars.getString("char_name");
							player._chars.put(charId, charName);
						}
						chars.close();
					}
					stmt.close();
				}
			}
			
			// Retrieve from the database all secondary data of this L2PcInstance
			// and reward expertise/lucky skills if necessary.
			// Note that Clan, Noblesse and Hero skills are given separately and not here.
			
			if(player!= null)
			{
			player.restoreCharData();
			player.rewardSkills();
			
			// Restore current Cp, HP and MP values
			player.setCurrentCp(currentCp);
			player.setCurrentHp(currentHp);
			player.setCurrentMp(currentMp);
			
			if (currentHp < 0.5)
			{
				player.stopHpMpRegeneration();
			}
			
			// Restore pet if exists in the world
			player.setPet(L2World.getInstance().getPet(player.getObjectId()));
			if (player.getPet() != null)
			{
				player.getPet().setOwner(player);
			}
			// Update the overloaded status of the L2PcInstance
			player.refreshOverloaded();
			player.restoreFriendList();
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(L2PcInstance.class.getSimpleName() + ": : Could not restore char data: ");
				e.printStackTrace();
		}
		
		return player;
	}
	
	public Forum getMail()
	{
		if (_forumMail == null)
		{
			setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));
			
			if (_forumMail == null)
			{
				ForumsBBSManager.getInstance().createNewForum(getName(), ForumsBBSManager.getInstance().getForumByName("MailRoot"), Forum.MAIL, Forum.OWNERONLY, getObjectId());
				setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));
			}
		}
		
		return _forumMail;
	}
	
	public void setMail(Forum forum)
	{
		_forumMail = forum;
	}
	
	public Forum getMemo()
	{
		if (_forumMemo == null)
		{
			setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));
			
			if (_forumMemo == null)
			{
				ForumsBBSManager.getInstance().createNewForum(_accountName, ForumsBBSManager.getInstance().getForumByName("MemoRoot"), Forum.MEMO, Forum.OWNERONLY, getObjectId());
				setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));
			}
		}
		
		return _forumMemo;
	}
	
	public void setMemo(Forum forum)
	{
		_forumMemo = forum;
	}
	
	private static boolean restoreSubClassData(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_SUBCLASSES);
			statement.setInt(1, player.getObjectId());
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final SubClass subClass = new SubClass(rset.getInt("class_id"), rset.getInt("class_index"), rset.getLong("exp"), rset.getInt("sp"), rset.getByte("level"));
				player.getSubClasses().put(subClass.getClassIndex(), subClass);
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getSimpleName() + ": : Could not restore classes for " + player.getName() + ": ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		return true;
	}
	
	public boolean isMemberOfSameTeam(L2Character attacker, L2Character target)
	{
		if ((attacker instanceof L2PcInstance && ((L2PcInstance) attacker)._inEventTvT && ((L2PcInstance) attacker)._teamNameTvT == ((L2PcInstance) target)._teamNameTvT && ((L2PcInstance) target)._inEventTvT))
			return true;
		
		return false;
	}
	
	private void restoreCharData()
	{
		// Retrieve from the database all skills of this L2PcInstance and add them to _skills.
		restoreSkills();
		
		// Retrieve from the database all macroses of this L2PcInstance and add them to _macroses.
		_macroses.restore();
		
		// Retrieve from the database all shortCuts of this L2PcInstance and add them to _shortCuts.
		_shortCuts.restore();
		
		// Retrieve from the database all henna of this L2PcInstance and add them to _henna.
		restoreHenna();
		
		// Retrieve from the database all recom data of this L2PcInstance and add to _recomChars.
		restoreRecom();
		
		// Retrieve from the database the recipe book of this L2PcInstance.
		if (!isSubClassActive())
		{
			restoreRecipeBook();
		}
	}
	
	private void storeRecipeBook()
	{
		// If the player is on a sub-class don't even attempt to store a recipe
		// book.
		if (isSubClassActive())
			return;
		if (getCommonRecipeBook().size() == 0 && getDwarvenRecipeBook().size() == 0)
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, getObjectId());
			statement.execute();
			statement.close();
			
			Collection<L2RecipeList> recipes = getCommonRecipeBook();
			
			for (L2RecipeList recipe : recipes)
			{
				statement = con.prepareStatement("INSERT INTO character_recipebook (char_id, id, type) values(?,?,0)");
				statement.setInt(1, getObjectId());
				statement.setInt(2, recipe.getId());
				statement.execute();
				statement.close();
			}
			
			recipes = getDwarvenRecipeBook();
			for (L2RecipeList recipe : recipes)
			{
				statement = con.prepareStatement("INSERT INTO character_recipebook (char_id, id, type) values(?,?,1)");
				statement.setInt(1, getObjectId());
				statement.setInt(2, recipe.getId());
				statement.execute();
				statement.close();
			}
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": Could not store recipe book data: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private void restoreRecipeBook()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, type FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, getObjectId());
			ResultSet rset = statement.executeQuery();
			
			L2RecipeList recipe;
			while (rset.next())
			{
				recipe = RecipeData.getInstance().getRecipeList(rset.getInt("id") - 1);
				
				if (rset.getInt("type") == 1)
				{
					registerDwarvenRecipeList(recipe);
				}
				else
				{
					registerCommonRecipeList(recipe);
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": Could not restore recipe book data:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public synchronized void store()
	{
		// update client coords, if these look like true
		if (isInsideRadius(getClientX(), getClientY(), 1000, true))
		{
			setXYZ(getClientX(), getClientY(), getClientZ());
		}
		
		storeCharBase();
		storeCharSub();
		storeEffect();
		storeRecipeBook();
	}
	
	private void storeCharBase()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Get the exp, level, and sp of base class to store in base table
			int currentClassIndex = getClassIndex();
			_classIndex = 0;
			long exp = getStat().getExp();
			int level = getStat().getLevel();
			int sp = getStat().getSp();
			_classIndex = currentClassIndex;
			
			PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER);
			
			// Update base class
			statement.setInt(1, level);
			statement.setInt(2, getMaxHp());
			statement.setDouble(3, getCurrentHp());
			statement.setInt(4, getMaxCp());
			statement.setDouble(5, getCurrentCp());
			statement.setInt(6, getMaxMp());
			statement.setDouble(7, getCurrentMp());
			statement.setInt(8, getSTR());
			statement.setInt(9, getCON());
			statement.setInt(10, getDEX());
			statement.setInt(11, getINT());
			statement.setInt(12, getMEN());
			statement.setInt(13, getWIT());
			statement.setInt(14, getAppearance().getFace());
			statement.setInt(15, getAppearance().getHairStyle());
			statement.setInt(16, getAppearance().getHairColor());
			statement.setInt(17, getHeading());
			statement.setInt(18, _observerMode ? _obsLocation.getX() : getX());
			statement.setInt(19, _observerMode ? _obsLocation.getY() : getY());
			statement.setInt(20, _observerMode ? _obsLocation.getZ() : getZ());
			statement.setLong(21, exp);
			statement.setLong(22, getExpBeforeDeath());
			statement.setInt(23, sp);
			statement.setInt(24, getKarma());
			statement.setInt(25, getPvpKills());
			statement.setInt(26, getPkKills());
			statement.setInt(27, getRecomHave());
			statement.setInt(28, getRecomLeft());
			statement.setInt(29, getClanId());
			statement.setInt(30, getMaxLoad());
			statement.setInt(31, getRace().ordinal());
			
			// if (!isSubClassActive())
			
			// else
			// statement.setInt(30, getBaseTemplate().race.ordinal());
			
			statement.setInt(32, getClassId().getId());
			statement.setLong(33, getDeleteTimer());
			statement.setString(34, getTitle());
			statement.setInt(35, getAccessLevel().getLevel());
			statement.setInt(36, isOnline());
			statement.setInt(37, isIn7sDungeon() ? 1 : 0);
			statement.setInt(38, getClanPrivileges());
			statement.setInt(39, getWantsPeace());
			statement.setInt(40, getBaseClass());
			
			long totalOnlineTime = _onlineTime;
			
			if (_onlineBeginTime > 0)
			{
				totalOnlineTime += (System.currentTimeMillis() - _onlineBeginTime) / 1000;
			}
			
			statement.setLong(41, totalOnlineTime);
			statement.setInt(42, isInJail() ? 1 : 0);
			statement.setLong(43, getJailTimer());
			statement.setInt(44, isNewbie() ? 1 : 0);
			statement.setInt(45, isNoble() ? 1 : 0);
			statement.setLong(46, getPowerGrade());
			statement.setInt(47, getPledgeType());
			statement.setLong(48, getLastRecomUpdate());
			statement.setInt(49, getLvlJoinedAcademy());
			statement.setLong(50, getApprentice());
			statement.setLong(51, getSponsor());
			statement.setInt(52, getAllianceWithVarkaKetra());
			statement.setLong(53, getClanJoinExpiryTime());
			statement.setLong(54, getClanCreateExpiryTime());
			statement.setString(55, getName());
			statement.setLong(56, getDeathPenaltyBuffLevel());
			statement.setInt(57, getChatFilterCount());
			statement.setInt(58, getObjectId());
			
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": Could not store char base data: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private void storeCharSub()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			if (getTotalSubClasses() > 0)
			{
				for (SubClass subClass : getSubClasses().values())
				{
					PreparedStatement statement = con.prepareStatement(UPDATE_CHAR_SUBCLASS);
					statement.setLong(1, subClass.getExp());
					statement.setInt(2, subClass.getSp());
					statement.setInt(3, subClass.getLevel());
					statement.setInt(4, subClass.getClassId());
					statement.setInt(5, getObjectId());
					statement.setInt(6, subClass.getClassIndex());
					
					statement.execute();
					statement.close();
				}
			}
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": Could not store sub class data for " + getName() + ": ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private synchronized void storeEffect()
	{
		if (!Config.STORE_SKILL_COOLTIME)
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Delete all current stored effects for char to avoid dupe
			PreparedStatement statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.execute();
			statement.close();
			
			int buff_index = 0;
			
			// Store all effect data along with calulated remaining
			// reuse delays for matching skills. 'restore_type'= 0.
			for (L2Effect effect : getAllEffects())
			{
				if (effect != null && effect.getInUse() && !effect.getSkill().isToggle())
				{
					int skillId = effect.getSkill().getId();
					buff_index++;
					
					statement = con.prepareStatement(ADD_SKILL_SAVE);
					statement.setInt(1, getObjectId());
					statement.setInt(2, skillId);
					statement.setInt(3, effect.getSkill().getLevel());
					statement.setInt(4, effect.getCount());
					statement.setInt(5, effect.getTime());
					
					if (_reuseTimeStamps.containsKey(skillId))
					{
						TimeStamp t = _reuseTimeStamps.remove(skillId);
						statement.setLong(6, t.hasNotPassed() ? t.getReuse() : 0);
						statement.setLong(7, t.hasNotPassed() ? t.getStamp() : 0);
					}
					else
					{
						statement.setLong(6, 0);
						statement.setLong(7, 0);
					}
					
					statement.setInt(8, 0);
					statement.setInt(9, getClassIndex());
					statement.setInt(10, buff_index);
					statement.execute();
					statement.close();
				}
			}
			
			// Store the reuse delays of remaining skills which
			// lost effect but still under reuse delay. 'restore_type' 1.
			for (TimeStamp t : _reuseTimeStamps.values())
			{
				if (t.hasNotPassed())
				{
					statement = con.prepareStatement(ADD_SKILL_SAVE);
					statement.setInt(1, getObjectId());
					statement.setInt(2, t.getSkill());
					statement.setInt(3, 1);
					statement.setInt(4, -1);
					statement.setInt(5, -1);
					statement.setLong(6, t.getReuse());
					statement.setLong(7, t.getStamp());
					statement.setInt(8, 0);
					statement.setInt(9, getClassIndex());
					statement.setInt(10, ++buff_index);
					statement.execute();
					statement.close();
				}
			}
			statement.close();
			_reuseTimeStamps.clear();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": Could not store char effect data: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public int ChatFilterCount = 0;
	
	public void setChatFilterCount(int cfcount)
	{
		ChatFilterCount = cfcount;
	}
	
	public int getChatFilterCount()
	{
		return ChatFilterCount;
	}
	
	public int isOnline()
	{
		return (_isOnline ? 1 : 0);
	}
	
	public boolean isbOnline()
	{
		return (_isOnline ? true : false);
	}
	
	public boolean isIn7sDungeon()
	{
		return _isIn7sDungeon;
	}
	
	public L2Skill addSkill(L2Skill newSkill, boolean store)
	{
		_learningSkill = true;
		
		if (newSkill.isToggle())
		{
			final L2Effect toggleEffect = getFirstEffect(newSkill.getId());
			if (toggleEffect != null)
			{
				toggleEffect.exit();
				newSkill.getEffects(this, this);
			}
		}
		// Add a skill to the L2PcInstance _skills and its Func objects to the
		// calculator set of the L2PcInstance
		L2Skill oldSkill = super.addSkill(newSkill);
		
		// Add or update a L2PcInstance skill in the character_skills table of the database
		if (store)
		{
			storeSkill(newSkill, oldSkill, -1);
		}
		
		_learningSkill = false;
		
		return oldSkill;
	}
	
	public L2Skill removeSkill(L2Skill skill, boolean store)
	{
		if (store)
			return removeSkill(skill);

		return super.removeSkill(skill);
	}
	
	@Override
	public L2Skill removeSkill(L2Skill skill)
	{
		// Remove a skill from the L2Character and its Func objects from
		// calculator set of the L2Character
		L2Skill oldSkill = super.removeSkill(skill);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Remove or update a L2PcInstance skill from the character_skills table of the database
			if (oldSkill != null)
			{
				PreparedStatement statement = con.prepareStatement(DELETE_SKILL_FROM_CHAR);
				statement.setInt(1, oldSkill.getId());
				statement.setInt(2, getObjectId());
				statement.setInt(3, getClassIndex());
				statement.execute();
				statement.close();
			}
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": Error could not delete skill: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		L2ShortCut[] allShortCuts = getAllShortCuts();
		
		for (L2ShortCut sc : allShortCuts)
		{
			if (sc != null && skill != null && sc.getId() == skill.getId() && sc.getType() == L2ShortCut.TYPE_SKILL)
			{
				deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
		
		return oldSkill;
	}
	
	private void storeSkill(L2Skill newSkill, L2Skill oldSkill, int newClassIndex)
	{
		int classIndex = _classIndex;
		
		if (newClassIndex > -1)
		{
			classIndex = newClassIndex;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			if (oldSkill != null && newSkill != null)
			{
				PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER_SKILL_LEVEL);
				statement.setInt(1, newSkill.getLevel());
				statement.setInt(2, oldSkill.getId());
				statement.setInt(3, getObjectId());
				statement.setInt(4, classIndex);
				statement.execute();
				statement.close();
			}
			else if (newSkill != null)
			{
				PreparedStatement statement = con.prepareStatement(ADD_NEW_SKILL);
				statement.setInt(1, getObjectId());
				statement.setInt(2, newSkill.getId());
				statement.setInt(3, newSkill.getLevel());
				statement.setString(4, newSkill.getName());
				statement.setInt(5, classIndex);
				statement.execute();
				statement.close();
			}
			else
			{
				_log.warning(L2PcInstance.class.getName() + ": could not store new skill. its NULL");
			}
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": Error could not store char skills: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void checkAllowedSkills()
	{
		if (isGM())
			return;
		
		Collection<L2SkillLearn> skillTree = SkillTreeData.getInstance().getAllowedSkills(getClassId());
		skill_loop:
		for (L2Skill skill : getAllSkills())
		{
			int skillid = skill.getId();
			
			// Loop through all skills in players skilltree
			for (L2SkillLearn temp : skillTree)
			{
				// If the skill was found and the level is possible to obtain
				// for his class everything is ok
				if (temp.getId() == skillid)
				{
					continue skill_loop;
				}
			}
			
			// Exclude noble skills
			if (isNoble() && NobleSkills.isNobleSkill(skillid))
			{
				continue skill_loop;
			}
			// Exclude hero skills
			if (isHero() && HeroSkills.isHeroSkill(skillid))
			{
				continue skill_loop;
			}
			// Exclude cursed weapon skills
			if (isCursedWeaponEquiped() && skillid == CursedWeaponsManager.getInstance().getCursedWeapon(_cursedWeaponEquipedId).getSkillId())
			{
				continue skill_loop;
			}
			// Exclude clan skills
			if (getClan() != null && (skillid >= 370 && skillid <= 391))
			{
				continue skill_loop;
			}
			// Exclude seal of ruler / build siege hq
			if (getClan() != null && getClan().getLeaderId() == getObjectId() && (skillid == 246 || skillid == 247))
			{
				continue skill_loop;
			}
			// Exclude fishing skills and common skills + dwarfen craft
			if (skillid >= 1312 && skillid <= 1322)
			{
				continue skill_loop;
			}
			if (skillid >= 1368 && skillid <= 1373)
			{
				continue skill_loop;
			}
			// Exclude sa / enchant bonus / penality etc. skills
			if (skillid >= 3000 && skillid < 7000)
			{
				continue skill_loop;
			}
			// Exclude Skills from AllowedSkills in options.ini
			if (Config.ALLOWED_SKILLS_LIST.contains(skillid))
			{
				continue skill_loop;
			}
			// Exclude VIP character
			if (isDonator())
			{
				continue skill_loop;
			}
			// Remove skill and do a lil log message
			removeSkill(skill);
		}
	}
	
	private void restoreSkills()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Retrieve all skills of this L2PcInstance from the database
			PreparedStatement statement = con.prepareStatement(Config.ALT_SUBCLASS_SKILLS ? RESTORE_SKILLS_FOR_CHAR_ALT_SUBCLASS : RESTORE_SKILLS_FOR_CHAR);
			
			statement.setInt(1, getObjectId());
			
			if (!Config.ALT_SUBCLASS_SKILLS)
			{
				statement.setInt(2, getClassIndex());
			}
			
			ResultSet rset = statement.executeQuery();
			
			// Go though the recordset of this SQL query
			while (rset.next())
			{
				int id = rset.getInt("skill_id");
				int level = rset.getInt("skill_level");
				
				if (id > 9000)
				{
					continue; // fake skills for base stats
				}
				
				// Create a L2Skill object for each record
				L2Skill skill = SkillTable.getInstance().getInfo(id, level);
				
				// Add the L2Skill object to the L2Character _skills and its
				// Func objects to the calculator set of the L2Character
				super.addSkill(skill);
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": Could not restore character skills: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void restoreEffects()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(RESTORE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.setInt(3, 0);
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				int effectCount = rset.getInt("effect_count");
				int effectCurTime = rset.getInt("effect_cur_time");
				long reuseDelay = rset.getLong("reuse_delay");
				long systime = rset.getLong("systime");
				
				final L2Skill skill = SkillTable.getInstance().getInfo(rset.getInt("skill_id"), rset.getInt("skill_level"));
				if (skill == null)
					continue;
				
				final long remainingTime = systime - System.currentTimeMillis();
				if (remainingTime > 10)
				{
					disableSkill(skill.getId(), remainingTime);
					addTimeStamp(skill, reuseDelay, systime);
				}
				
				if (skill.hasEffects() && effectCount != -1)
				{
					Env env = new Env();
					env.player = this;
					env.target = this;
					env.skill = skill;
					L2Effect ef;
					for (EffectTemplate et : skill.getEffectTemplates())
					{
						ef = et.getEffect(env);
						if (ef != null)
						{
							ef.setCount(effectCount);
							ef.setFirstTime(effectCurTime);
							ef.scheduleEffect();
						}
					}
				}
			}
			rset.close();
			statement.close();
			
			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(L2PcInstance.class.getSimpleName() + ": Could not restore " + this + " active effect data: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private void restoreHenna()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_HENNAS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			ResultSet rset = statement.executeQuery();
			
			for (int i = 0; i < 3; i++)
				_henna[i] = null;
			
			while (rset.next())
			{
				int slot = rset.getInt("slot");
				
				if (slot < 1 || slot > 3)
					continue;
				
				int symbolId = rset.getInt("symbol_id");
				if (symbolId != 0)
				{
					L2Henna tpl = HennaData.getInstance().getTemplate(symbolId);
					if (tpl != null)
						_henna[slot - 1] = tpl;
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": could not restore henna: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();
	}
	
	private void restoreRecom()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_RECOMS);
			statement.setInt(1, getObjectId());
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				_recomChars.add(rset.getInt("target_id"));
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": could not restore recommendations: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public int getHennaEmptySlots()
	{
		int totalSlots = 0;
		if (getClassId().level() == 1)
			totalSlots = 2;
		else
			totalSlots = 3;
		
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] != null)
				totalSlots--;
		}
		
		if (totalSlots <= 0)
			return 0;
		
		return totalSlots;
	}
	
	public boolean removeHenna(int slot)
	{
		if (slot < 1 || slot > 3)
			return false;
		
		slot--;
		
		if (_henna[slot] == null)
			return false;
		
		L2Henna henna = _henna[slot];
		_henna[slot] = null;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNA);
			
			statement.setInt(1, getObjectId());
			statement.setInt(2, slot + 1);
			statement.setInt(3, getClassIndex());
			
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": could not remove char henna: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();
		
		// Send HennaInfo packet to this L2PcInstance
		sendPacket(new HennaInfo(this));
		
		// Send UserInfo packet to this L2PcInstance
		sendPacket(new UserInfo(this));
		
		reduceAdena("Henna", henna.getPrice() / 5, this, false);
		
		// Add the recovered dyes to the player's inventory and notify them.
		addItem("Henna", henna.getDyeId(), L2Henna.getAmountDyeRequire() / 2, this, true);
		sendPacket(SystemMessageId.SYMBOL_DELETED);
		return true;
	}
	
	public void addHenna(L2Henna henna)
	{
		if (getHennaEmptySlots() == 0)
		{
			sendMessage("You may not have more than three equipped symbols at a time.");
			return;
		}
		
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] == null)
			{
				_henna[i] = henna;
				
				// Calculate Henna modifiers of this L2PcInstance
				recalcHennaStats();
				
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement statement = con.prepareStatement(ADD_CHAR_HENNA);
					
					statement.setInt(1, getObjectId());
					statement.setInt(2, henna.getSymbolId());
					statement.setInt(3, i + 1);
					statement.setInt(4, getClassIndex());
					
					statement.execute();
					statement.close();
				}
				catch (SQLException e)
				{
					_log.warning(L2PcInstance.class.getName() + ": could not save char henna: ");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				
				sendPacket(new HennaInfo(this));
				sendPacket(new UserInfo(this));
				sendPacket(SystemMessageId.SYMBOL_ADDED);
				return;
			}
		}
	}
	
	private void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;
		
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] == null)
				continue;
			
			_hennaINT += _henna[i].getStatINT();
			_hennaSTR += _henna[i].getStatSTR();
			_hennaMEN += _henna[i].getStatMEN();
			_hennaCON += _henna[i].getStatCON();
			_hennaWIT += _henna[i].getStatWIT();
			_hennaDEX += _henna[i].getStatDEX();
		}
		
		if (_hennaINT > 5)
			_hennaINT = 5;
		
		if (_hennaSTR > 5)
			_hennaSTR = 5;
		
		if (_hennaMEN > 5)
			_hennaMEN = 5;
		
		if (_hennaCON > 5)
			_hennaCON = 5;
		
		if (_hennaWIT > 5)
			_hennaWIT = 5;
		
		if (_hennaDEX > 5)
			_hennaDEX = 5;
	}
	
	public L2Henna[] getHennaList()
	{
		return _henna;
	}
	
	public L2Henna getHenna(int slot)
	{
		if (slot < 1 || slot > 3)
			return null;
		
		return _henna[slot - 1];
	}
	
	public int getHennaStatINT()
	{
		return _hennaINT;
	}
	
	public int getHennaStatSTR()
	{
		return _hennaSTR;
	}
	
	public int getHennaStatCON()
	{
		return _hennaCON;
	}
	
	public int getHennaStatMEN()
	{
		return _hennaMEN;
	}
	
	public int getHennaStatWIT()
	{
		return _hennaWIT;
	}
	
	public int getHennaStatDEX()
	{
		return _hennaDEX;
	}
	
	public void setChatBanned(boolean isBanned)
	{
		_chatBanned = isBanned;
		
		stopBanChatTask();
		if (isChatBanned())
		{
			sendMessage("You have been chat banned by a server admin.");
			if (_banchat_timer > 0)
			{
				_BanChatTask = ThreadPoolManager.getInstance().scheduleGeneral(new SchedChatUnban(this), _banchat_timer);
			}
		}
		else
		{
			sendMessage("Your chat ban has been lifted.");
			setBanChatTimer(0);
		}
		sendPacket(new EtcStatusUpdate(this));
	}
	
	public void setChatBannedForAnnounce(boolean isBanned)
	{
		_chatBanned = isBanned;
		
		stopBanChatTask();
		if (isChatBanned())
		{
			sendMessage("Server admin making announce now, you can't chat.");
			_BanChatTask = ThreadPoolManager.getInstance().scheduleGeneral(new SchedChatUnban(this), _banchat_timer);
		}
		else
		{
			sendMessage("Your chat ban has been lifted.");
			setBanChatTimer(0);
		}
		sendPacket(new EtcStatusUpdate(this));
	}
	
	public void setBanChatTimer(long timer)
	{
		_banchat_timer = timer;
	}
	
	public long getBanChatTimer()
	{
		if (_BanChatTask != null)
			return _BanChatTask.getDelay(TimeUnit.MILLISECONDS);
		return _banchat_timer;
	}
	
	public void stopBanChatTask()
	{
		if (_BanChatTask != null)
		{
			_BanChatTask.cancel(false);
			_BanChatTask = null;
		}
	}
	
	private class SchedChatUnban implements Runnable
	{
		L2PcInstance _player;
		
		@SuppressWarnings("unused")
		protected long _startedAt;
		
		protected SchedChatUnban(L2PcInstance player)
		{
			_player = player;
			_startedAt = System.currentTimeMillis();
		}
		
		@Override
		public void run()
		{
			_player.setChatBanned(false);
		}
	}
	
	public L2Object _saymode = null;
	
	public L2Object getSayMode()
	{
		return _saymode;
	}
	
	public void setSayMode(L2Object say)
	{
		_saymode = say;
	}
	
	public boolean isChatBanned()
	{
		return _chatBanned;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker == null)
			return false;

		// Check if the attacker isn't the L2PcInstance Pet
		if (attacker == this || attacker == getPet() || attacker instanceof L2NpcInstance)
			return false;
		
		// Check if the attacker is monster
		if (attacker instanceof L2MonsterInstance)
			return true;
		
		// Check if the attacker is not in the same party
		if (getParty() != null && getParty().getPartyMembers().contains(attacker))
			return false;
		
		// Check if the attacker is a L2Playable
		if (attacker instanceof L2Playable)
		{
			if (isInsideZone(ZoneId.PEACE))
				return false;
			
			final L2PcInstance cha = attacker.getActingPlayer();
			
			// Check if the attacker is in olympiad and olympiad start
			if (attacker instanceof L2PcInstance && cha.isInOlympiadMode())
			{
				if (isInOlympiadMode() && isOlympiadStart() && cha.getOlympiadGameId() == getOlympiadGameId())
					return true;
				
				return false;
			}
			
			// is AutoAttackable if both players are in the same duel and the duel is still going on
			if (getDuelState() == DuelState.DUELLING && getDuelId() == cha.getDuelId())
				return true;
			
			if (getClan() != null)
			{
				final Siege siege = SiegeManager.getSiege(getX(), getY(), getZ());

				if (siege != null)
				{		
					// Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Defender clan
					if (siege.checkIsDefender(((L2PcInstance) attacker).getClan()) && siege.checkIsDefender(getClan()))
						return false;
					
					// Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Attacker clan
					if (siege.checkIsAttacker(((L2PcInstance) attacker).getClan()) && siege.checkIsAttacker(getClan()))
						return false;
				}
				
				// Check if clan is at war
				if (((L2PcInstance) attacker).getClan() != null && (getClan().isAtWarWith(((L2PcInstance) attacker).getClanId()) && ((L2PcInstance) attacker).getClan().isAtWarWith(getClanId()) && getWantsPeace() == 0 && ((L2PcInstance) attacker).getWantsPeace() == 0 && !isAcademyMember()))
					return true;
			}

			if (isInsideZone(ZoneId.PVP) && !isInsideZone(ZoneId.SIEGE) && attacker.isInsideZone(ZoneId.PVP) && !attacker.isInsideZone(ZoneId.SIEGE))		
				return true;
			// Check if the attacker is not in the same clan.
			if (getClan() != null && getClan().isMember(cha.getName()))
				return false;
			// Check if the attacker is not in the same ally.
			if (getAllyId() != 0 && getAllyId() == cha.getAllyId())
				return false;
			if (isInsideZone(ZoneId.PVP) && attacker.isInsideZone(ZoneId.PVP))
				return true;
		}
		
		if (attacker instanceof L2SiegeGuardInstance)
		{
			if (getClan() != null)
			{
				Siege siege = SiegeManager.getSiege(this);
				return (siege != null && siege.checkIsAttacker(getClan()));
			}
		}
		
		if (attacker instanceof L2GuardInstance)
			return getKarma() > 0; 
		
		// Check if the Player has Karma
		if (getKarma() > 0 || getPvpFlag() > 0)
			return true;
		
		return false;
	}
	
	public void useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		if (skill == null)
		{
			abortCast();
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (skill.isPassive() || isDead() || isOutOfControl())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (inObserverMode())
		{
			sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			abortCast();
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (isSitting() && !skill.isPotion())
		{
			sendPacket(SystemMessageId.CANT_MOVE_SITTING);		
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (isSkillDisabled(skill.getId()))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE).addSkillName(skill));
			return;
		}
		
		if (skill.isToggle())
		{
			// Like L2OFF you can't use fake death if you are mounted
			if (skill.getId() == 60 && isMounted())
				return;
			
			L2Effect effect = getFirstEffect(skill);
			
			if (effect != null)
			{
				effect.exit();		
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// Check if it's ok to summon siege golem (13), Wild Hog Cannon (299), Swoop Cannon (448)
		if ((skill.getId() == 13 || skill.getId() == 299 || skill.getId() == 448) && !SiegeManager.checkIfOkToSummon(this, false))
			return;
		
		// ************************************* Check Casting in Progress *******************************************
		
		// If a skill is currently being used, queue this one if this is not the same
		// Note that this check is currently imperfect: getCurrentSkill() isn't always null when a skill has
		// failed to cast, or the casting is not yet in progress when this is rechecked
		if (getCurrentSkill() != null && isCastingNow())
		{
			// Check if new skill different from current skill in progress
			if (skill.getId() == getCurrentSkill().getSkillId())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (Config.DEBUG && getQueuedSkill() != null)
				_log.info(getQueuedSkill().getSkill().getName() + " is already queued for " + getName() + ".");
			
			// Create a new SkillDat object and queue it in the player _queuedSkill
			setQueuedSkill(skill, forceUse, dontMove);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (getQueuedSkill() != null)
		{
			// casting has been aborted
			setQueuedSkill(null, false, false);
		}
		
		// ************************************* Check Target *******************************************
		
		// Create and set a L2Object containing the target of the skill
		L2Object target = null;
		L2SkillTargetType sklTargetType = skill.getTargetType();
		L2SkillType SkillType = skill.getSkillType();
		Point3D worldPosition = getCurrentSkillWorldPosition();
		
		if (sklTargetType == L2SkillTargetType.TARGET_SIGNET_GROUND && worldPosition == null)
		{
			_log.info("WorldPosition is null for skill: " + skill.getName() + ", player: " + getName() + ".");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		switch (sklTargetType)
		{
		// Target the player if skill type is AURA, PARTY, CLAN or SELF
			case TARGET_AURA:
			case TARGET_SIGNET_GROUND:
			case TARGET_SIGNET:
			case TARGET_PARTY:
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_SELF:
				target = this;
				break;
			case TARGET_PET:
				target = getPet();
				break;
			default:
				target = skill.getFirstOfTargetList(this);
				break;
		}
		
		// Check the validity of the target
		if (target == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Like L2OFF you can't heal random purple people without using CTRL
		SkillDat skilldat = getCurrentSkill();
		if (skilldat != null && skill.getSkillType() == L2SkillType.HEAL && !skilldat.isCtrlPressed() && target instanceof L2PcInstance && ((L2PcInstance) target).getPvpFlag() == 1)
		{
			if ((getClanId() == 0 || ((L2PcInstance) target).getClanId() == 0) || (getClanId() != ((L2PcInstance) target).getClanId()))
			{
				if ((getAllyId() == 0 || ((L2PcInstance) target).getAllyId() == 0) || (getAllyId() != ((L2PcInstance) target).getAllyId()))
				{
					if ((getParty() == null || ((L2PcInstance) target).getParty() == null) || (!getParty().equals(((L2PcInstance) target).getParty())))
					{
						sendPacket(SystemMessageId.INCORRECT_TARGET);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
			}
		}
		
		// Are the target and the player in the same duel?
		if (isInDuel())
		{
			if (!(target instanceof L2PcInstance && ((L2PcInstance) target).getDuelId() == getDuelId()))
			{
				sendMessage("You cannot do this while duelling.");
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if (_inEventTvT && TvT._started && !Config.TVT_ALLOW_ENEMY_HEALING)
		{
			if (target instanceof L2PcInstance && skill.getSkillType() == L2SkillType.HEAL)
			{
				if (!isMemberOfSameTeam((this), (L2PcInstance) target))
				{
					sendPacket(SystemMessageId.INCORRECT_TARGET);
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				// prevent usage of skills in same team members on TvT if tvt is on figthing period and if config is enabled
				if (_inEventTvT && TvT._started && !Config.TVT_ALLOW_TEAM_CASTING)
				{
					if (target instanceof L2PcInstance && skill.getTargetType() != L2SkillTargetType.TARGET_SELF && skill.getSkillType() != L2SkillType.BUFF)
					{
						if (isMemberOfSameTeam((this), (L2PcInstance) target))
						{
							if (skill.getSkillType() == L2SkillType.PDAM || skill.getSkillType() == L2SkillType.MDAM || (skill.getSkillType() == L2SkillType.BLOW))
							{
								sendPacket(SystemMessageId.INCORRECT_TARGET);
								sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}
						}
					}
				}
			}
		}

		if (_inEventCTF && !Config.CTF_ALLOW_SUMMON && CTF._started && SkillType == L2SkillType.SUMMON)
			sendPacket(SystemMessageId.NOTHING_HAPPENED);
		
		// ************************************* Check Consumables *******************************************
		
		// Check if the caster has enough MP
		if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.NOT_ENOUGH_MP);
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the caster has enough HP
		if (getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.NOT_ENOUGH_HP);
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the spell consummes an Item
		if (skill.getItemConsume() > 0)
		{
			// Get the L2ItemInstance consummed by the spell
			L2ItemInstance requiredItems = getInventory().getItemByItemId(skill.getItemConsumeId());
			
			// Check if the caster owns enought consummed Item to cast
			if (requiredItems == null || requiredItems.getCount() < skill.getItemConsume())
			{
				// Checked: when a summon skill failed, server show required consume item count
				if (SkillType == L2SkillType.SUMMON)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SUMMONING_SERVITOR_COSTS_S2_S1);
					sm.addItemName(skill.getItemConsumeId());
					sm.addNumber(skill.getItemConsume());
					sendPacket(sm);
					return;
				}
				// Send a System Message to the caster
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return;
			}
		}
		
		// ************************************* Check Casting Conditions *******************************************
		
		// Check if the caster own the weapon needed
		if (!skill.getWeaponDependancy(this))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if all casting conditions are completed
		if (!skill.checkCondition(this, target, false))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// ************************************* Check Player State *******************************************
		
		// Abnormal effects(ex : Stun, Sleep...) are checked in L2Character useMagic()
		
		// Check if the player use "Fake Death" skill
		if (isAlikeDead() && !skill.isPotion() && skill.getSkillType() != L2SkillType.FAKE_DEATH)
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (isFishing() && (SkillType != L2SkillType.PUMPING && SkillType != L2SkillType.REELING && SkillType != L2SkillType.FISHING))
		{
			// Only fishing skills are available
			sendPacket(SystemMessageId.ONLY_FISHING_SKILLS_NOW);
			return;
		}
		
		// ************************************* Check Skill Type *******************************************
		
		// Check if this is offensive magic skill
		if (skill.isOffensive())
		{
			
			if (isInsidePeaceZone(this, target))
			{
				sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			final OlympiadGameTask OlyTask = OlympiadGameManager.getInstance().getOlympiadTask(getOlympiadGameId());
			if (isInOlympiadMode() && isOlympiadStart() && OlyTask != null && !OlyTask.isGameStarted() && OlyTask.isInTimerTime() && skill.getId() != 347 )
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (!(target instanceof L2MonsterInstance) && SkillType == L2SkillType.CONFUSE_MOB_ONLY)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Check if a Forced ATTACK is in progress on non-attackable target
			if (!target.isAutoAttackable(this) && !forceUse && sklTargetType != L2SkillTargetType.TARGET_AURA && sklTargetType != L2SkillTargetType.TARGET_CLAN && sklTargetType != L2SkillTargetType.TARGET_ALLY && sklTargetType != L2SkillTargetType.TARGET_PARTY && sklTargetType != L2SkillTargetType.TARGET_SELF)
			{
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (!target.isAutoAttackable(this) && !forceUse)
			{
				switch (sklTargetType)
				{
					case TARGET_AURA:
					case TARGET_CLAN:
					case TARGET_ALLY:
					case TARGET_PARTY:
					case TARGET_SELF:
					case TARGET_SIGNET_GROUND:
					case TARGET_SIGNET:
						break;
					
					default: // Send a Server->Client packet ActionFailed to the L2PcInstance
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
				}
			}
			// Check if the target is in the skill cast range
			if (dontMove)
			{
				// Calculate the distance between the L2PcInstance and the target
				if (sklTargetType == L2SkillTargetType.TARGET_SIGNET_GROUND)
				{
					if (!isInsideRadius(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), skill.getCastRange() + getTemplate().getCollisionRadius(getAppearance().getSex()), false, false))
					{
						// Send a System Message to the caster
						sendPacket(SystemMessageId.TARGET_TOO_FAR);
						
						// Send a Server->Client packet ActionFailed to the L2PcInstance
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
				else if (skill.getCastRange() > 0 && !isInsideRadius(target, skill.getCastRange() + getTemplate().getCollisionRadius(getAppearance().getSex()), false, false))
				{
					// Send a System Message to the caster
					sendPacket(SystemMessageId.TARGET_TOO_FAR);
					
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				else if (SkillType == L2SkillType.SIGNET) // Check range for SIGNET skills
				{
					if (!isInsideRadius(getCurrentSkillWorldPosition().getX(), getCurrentSkillWorldPosition().getY(), getCurrentSkillWorldPosition().getZ(), skill.getCastRange() + getTemplate().getCollisionRadius(), false, false))
					{
						// Send a System Message to the caster
						sendPacket(SystemMessageId.TARGET_TOO_FAR);
						// Send a Server->Client packet ActionFailed to the L2PcInstance
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
			}
		}
		
		if (!skill.isOffensive() && target instanceof L2MonsterInstance && !forceUse)
		{
			// check if the target is a monster and if force attack is set.. if not then we don't want to cast.
			switch (sklTargetType)
			{
				case TARGET_PET:
				case TARGET_AURA:
				case TARGET_CLAN:
				case TARGET_SELF:
				case TARGET_PARTY:
				case TARGET_ALLY:
				case TARGET_CORPSE_MOB:
				case TARGET_AREA_CORPSE_MOB:
				case TARGET_SIGNET_GROUND:
				case TARGET_SIGNET:
					break;
				
				default:
					switch (SkillType)
					{
						case BEAST_FEED:
						case DELUXE_KEY_UNLOCK:
						case UNLOCK:
							break;
						
						default:
							sendPacket(ActionFailed.STATIC_PACKET);
							return;
					}
			}
		}
		
		// Check if the skill is Spoil type and if the target isn't already spoiled
		if (SkillType == L2SkillType.SPOIL)
		{
			if (!(target instanceof L2MonsterInstance))
			{
				// Send a System Message to the L2PcInstance
				sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// Check if the skill is Sweep type and if conditions not apply
		if (SkillType == L2SkillType.SWEEP && target instanceof L2Attackable)
		{
			
			if (((L2Attackable) target).isDead())
			{
				final int spoilerId = ((L2Attackable) target).getIsSpoiledBy();

				if (spoilerId == 0)
				{
					// Send a System Message to the L2PcInstance
					sendPacket(SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED);
					
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				if (getObjectId() != spoilerId && !isInLooterParty(spoilerId))
				{
					// Send a System Message to the L2PcInstance
					sendPacket(SystemMessageId.SWEEP_NOT_ALLOWED);
					
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		// Check if the skill is Drain Soul (Soul Crystals) and if the target is a MOB
		if (SkillType == L2SkillType.DRAIN_SOUL)
		{
			if (!(target instanceof L2MonsterInstance))
			{
				// Send a System Message to the L2PcInstance
				sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// Check if this is a Pvp skill and target isn't a non-flagged/non-karma player
		switch (sklTargetType)
		{
			case TARGET_PARTY:
			case TARGET_ALLY: // For such skills, checkPvpSkill() is called from
				// L2Skill.getTargetList()
			case TARGET_CLAN: // For such skills, checkPvpSkill() is called from
				// L2Skill.getTargetList()
			case TARGET_AURA:
			case TARGET_SIGNET_GROUND:
			case TARGET_SIGNET:
			case TARGET_SELF:
				break;
			default:
				if (!checkPvpSkill(target, skill))
				{
					// Send a System Message to the L2PcInstance
					sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
		}
		
		if (sklTargetType == L2SkillTargetType.TARGET_HOLY && !checkIfOkToCastSealOfRule(CastleManager.getInstance().getCastle(this), false, skill))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return;
		}
		
		if (SkillType == L2SkillType.SIEGEFLAG && !checkIfOkToPlaceFlag(this, false))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return;
		}
		else if (SkillType == L2SkillType.STRSIEGEASSAULT && !checkIfOkToUseStriderSiegeAssault(this, false))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return;
		}
		if ((target instanceof L2GrandBossInstance) && ((L2GrandBossInstance) target).getNpcId() == 29022)
		{
			if (Math.abs(getClientZ() - target.getZ()) > 200)
			{
				sendPacket(SystemMessageId.CANT_SEE_TARGET);
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		// GeoData Los Check here
		if (skill.getCastRange() > 0)
		{
			
			if (sklTargetType == L2SkillTargetType.TARGET_GROUND)
			{
				if (((Config.GEODATA) ? !GeoEngine.canSeeCoord(getX(), getY(), getZ(), worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), isFlying()) : !GeoEngine.canSeeTarget(this, target)))
				{
					sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			else if (((Config.GEODATA) ? !GeoEngine.canSeeTarget(this, target, isFlying()) : !GeoEngine.canSeeTarget(this, target)))
			{
				sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// If all conditions are checked, create a new SkillDat object and set the player _currentSkill
		setCurrentSkill(skill, forceUse, dontMove);
		
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}
	
	public boolean isInLooterParty(int LooterId)
	{
		L2PcInstance looter = L2World.getInstance().getPlayer(LooterId);
		
		// if L2PcInstance is in a CommandChannel
		if (isInParty() && getParty().isInCommandChannel() && looter != null)
			return getParty().getCommandChannel().getMembers().contains(looter);
		
		if (isInParty() && looter != null)
			return getParty().getPartyMembers().contains(looter);
		
		return false;
	}
	
	public boolean checkPvpSkill(L2Object target, L2Skill skill)
	{
		if (target != null && (target instanceof L2PcInstance || target instanceof L2Summon))
		{
			L2PcInstance character;
			if (target instanceof L2Summon)
			{
				if (((L2Summon) target).isInsideZone(ZoneId.PVP))
					return true;
				character = ((L2Summon) target).getOwner();
			}
			else
			{
				character = (L2PcInstance) target;
			}
			
			if ((_inEventTvT && TvT._started) || (_inEventDM && DM._started) || (_inEventCTF && CTF._started) || isinZodiac)
				return true;
			
			// check for PC->PC Pvp status
			if (character != this && // target is not self and
			!(isInDuel() && character.getDuelId() == getDuelId()) && // self is not in a duel and attacking opponent
			!isInsideZone(ZoneId.PVP) && // Pc is not in PvP zone
			!character.isInsideZone(ZoneId.PVP) // target is not in PvP zone
			)
			{
				if (skill.isPvpSkill()) // pvp skill
				{
					if (getClan() != null && character.getClan() != null)
					{
						if (getClan().isAtWarWith(character.getClan().getClanId()) && character.getClan().isAtWarWith(getClan().getClanId()))
							return true; // in clan war player can attack whites even with sleep etc.
					}
					if (character.getPvpFlag() == 0 && // target's pvp flag is not set and
					character.getKarma() == 0 // target has no karma
					)
						return false;
				}
				else if (getCurrentSkill() != null && !getCurrentSkill().isCtrlPressed() && skill.isOffensive())
				{
					if (getClan() != null && character.getClan() != null)
					{
						if (getClan().isAtWarWith(character.getClan().getClanId()) && character.getClan().isAtWarWith(getClan().getClanId()))
							return true; // in clan war player can attack whites even without ctrl
					}
					if (character.getPvpFlag() == 0 && // target's pvp flag is not set and
					character.getKarma() == 0 // target has no karma
					)
						return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public void consumeItem(int itemConsumeId, int itemCount)
	{
		if (itemConsumeId != 0 && itemCount != 0)
		{
			destroyItemByItemId("Consume", itemConsumeId, itemCount, null, false);
		}
	}
	
	public boolean isMageClass()
	{
		return getClassId().getType() != ClassType.FIGHTER;
	}
	
	public boolean isMounted()
	{
		return _mountType > 0;
	}
	
	public boolean checkLandingState()
	{
		// Check if char is in a no landing zone
		if (isInsideZone(ZoneId.NO_LANDING))
			return true;
		else
		// if this is a castle that is currently being sieged, and the rider is NOT a castle owner he cannot land.
		// castle owner is the leader of the clan that owns the castle where the pc is
		if (isInsideZone(ZoneId.SIEGE) && !(getClan() != null && CastleManager.getInstance().getCastle(this) == CastleManager.getInstance().getCastleByOwner(getClan()) && this == getClan().getLeader().getPlayerInstance()))
			return true;
		
		return false;
	}
	
	// returns false if the change of mount type fails.
	public boolean setMountType(int mountType)
	{
		if (checkLandingState() && mountType == 2)
			return false;
		
		switch (mountType)
		{
			case 0:
				setIsFlying(false);
				setIsRiding(false);
				break; // Dismounted
			case 1:
				setIsRiding(true);
				if (isNoble())
				{
					L2Skill striderAssaultSkill = SkillTable.getInstance().getInfo(325, 1);
					addSkill(striderAssaultSkill, false); // not saved to DB
				}
				break;
			case 2:
				setIsFlying(true);
				break; // Flying Wyvern
		}
		
		_mountType = mountType;
		
		// Send a Server->Client packet InventoryUpdate to the L2PcInstance in order to update speed
		UserInfo ui = new UserInfo(this);
		sendPacket(ui);
		return true;
	}
	
	public int getMountType()
	{
		return _mountType;
	}
	
	@Override
	public void updateAbnormalEffect()
	{
		broadcastUserInfo();
	}
	
	public void tempInvetoryDisable()
	{
		_inventoryDisable = true;
		
		ThreadPoolManager.getInstance().scheduleGeneral(new InventoryEnable(), 1500);
	}
	
	public void removeFromBossZone()
	{
		try
		{
			for (L2BossZone _zone : GrandBossManager.getInstance().getZones())
			{
				_zone.removePlayer(this);
			}
		}
		catch (Exception e)
		{
			_log.warning(L2PcInstance.class.getName() + ": Exception on removeFromBossZone(): ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public boolean isInvetoryDisabled()
	{
		return _inventoryDisable;
	}
	
	class InventoryEnable implements Runnable
	{
		@Override
		public void run()
		{
			_inventoryDisable = false;
		}
	}
	
	public Map<Integer, L2CubicInstance> getCubics()
	{
		return _cubics;
	}
	
	public void addCubic(int id, int level)
	{
		_cubics.put(id, new L2CubicInstance(this, id, level));
	}
	
	public void delCubic(int id)
	{
		_cubics.remove(id);
	}
	
	public L2CubicInstance getCubic(int id)
	{
		return _cubics.get(id);
	}
	
	@Override
	public String toString()
	{
		return "Player " + getName();
	}
	
	public int getEnchantEffect()
	{
		final L2ItemInstance wpn = getActiveWeaponInstance();	
		return (wpn == null) ? 0 : Math.min(127, wpn.getEnchantLevel());
	}
	
	public void setLastFolkNPC(L2NpcInstance folkNpc)
	{
		_lastFolkNpc = folkNpc;
	}
	
	public L2NpcInstance getLastFolkNPC()
	{
		return _lastFolkNpc;
	}
	
	public void setSilentMoving(boolean flag)
	{
		_isSilentMoving = flag;
	}
	
	public boolean isSilentMoving()
	{
		return _isSilentMoving;
	}
	
	public boolean isFestivalParticipant()
	{
		return SevenSignsFestival.getInstance().isParticipant(this);
	}
	
	public void addAutoSoulShot(int itemId)
	{
		_activeSoulShots.put(itemId, itemId);
	}
	
	public void removeAutoSoulShot(int itemId)
	{
		_activeSoulShots.remove(itemId);
	}
	
	public Map<Integer, Integer> getAutoSoulShot()
	{
		return _activeSoulShots;
	}
	
	public void rechargeAutoSoulShot(boolean physical, boolean magic, boolean summon)
	{
		L2ItemInstance item;
		IItemHandler handler;
		
		if (_activeSoulShots == null || _activeSoulShots.size() == 0)
			return;
		
		for (int itemId : _activeSoulShots.values())
		{
			item = getInventory().getItemByItemId(itemId);
			
			if (item != null)
			{
				if (magic)
				{
					if (!summon)
					{
						if (itemId == 2509 || itemId == 2510 || itemId == 2511 || itemId == 2512 || itemId == 2513 || itemId == 2514 || itemId == 3947 || itemId == 3948 || itemId == 3949 || itemId == 3950 || itemId == 3951 || itemId == 3952 || itemId == 5790)
						{
							handler = ItemHandler.getInstance().getHandler(itemId);
							
							if (handler != null)
							{
								handler.useItem(this, item);
							}
						}
					}
					else
					{
						if (itemId == 6646 || itemId == 6647)
						{
							handler = ItemHandler.getInstance().getHandler(itemId);
							
							if (handler != null)
							{
								handler.useItem(this, item);
							}
						}
					}
				}
				
				if (physical)
				{
					if (!summon)
					{
						if (itemId == 1463 || itemId == 1464 || itemId == 1465 || itemId == 1466 || itemId == 1467 || itemId == 1835 || itemId == 5789)
						// || itemId == 6535 || itemId == 6536 || itemId == 6537 || itemId == 6538 || itemId == 6539 || itemId == 6540)
						{
							handler = ItemHandler.getInstance().getHandler(itemId);
							
							if (handler != null)
							{
								handler.useItem(this, item);
							}
						}
					}
					else
					{
						if (itemId == 6645)
						{
							handler = ItemHandler.getInstance().getHandler(itemId);
							
							if (handler != null)
							{
								handler.useItem(this, item);
							}
						}
					}
				}
			}
			else
			{
				removeAutoSoulShot(itemId);
			}
		}
	}
	
	private ScheduledFuture<?> _taskWarnUserTakeBreak;
	
	class WarnUserTakeBreak implements Runnable
	{
		@Override
		public void run()
		{
			if (isOnline() == 1)
			{
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.PLAYING_FOR_LONG_TIME);
				L2PcInstance.this.sendPacket(msg);
			}
			else
			{
				stopWarnUserTakeBreak();
			}
		}
	}
	
	class RentPetTask implements Runnable
	{
		@Override
		public void run()
		{
			stopRentPet();
		}
	}
	
	public ScheduledFuture<?> _taskforfish;
	
	class LookingForFishTask implements Runnable
	{
		private final boolean _isNoob, _isUpperGrade;
		private final int _fishGroup;
		private final double _fishGutsCheck;
		private final long _endTaskTime;
		
		protected LookingForFishTask(int startCombatTime, double fishGutsCheck, int fishGroup, boolean isNoob, boolean isUpperGrade)
		{
			_fishGutsCheck = fishGutsCheck;
			_endTaskTime = System.currentTimeMillis() + (startCombatTime * 1000) + 10000;
			_fishGroup = fishGroup;
			_isNoob = isNoob;
			_isUpperGrade = isUpperGrade;
		}
		
		@Override
		public void run()
		{
			if (System.currentTimeMillis() >= _endTaskTime)
			{
				EndFishing(false);
				return;
			}
			if (_fishGroup == -1)
				return;
			int check = Rnd.get(1000);
			if (_fishGutsCheck > check)
			{
				stopLookingForFishTask();
				StartFishCombat(_isNoob, _isUpperGrade);
			}
		}
		
	}
	
	public int getClanPrivileges()
	{
		return _clanPrivileges;
	}
	
	public void setClanPrivileges(int n)
	{
		_clanPrivileges = n;
	}
	
	public boolean getAllowTrade()
	{
		return _allowTrade;
	}
	
	public void setAllowTrade(boolean a)
	{
		_allowTrade = a;
	}
	
	// baron etc
	public void setPledgeClass(int classId)
	{
		_pledgeClass = classId;
	}
	
	public int getPledgeClass()
	{
		return _pledgeClass;
	}
	
	public void setPledgeType(int typeId)
	{
		_pledgeType = typeId;
	}
	
	public int getPledgeType()
	{
		return _pledgeType;
	}
	
	public int getApprentice()
	{
		return _apprentice;
	}
	
	public void setApprentice(int apprentice_id)
	{
		_apprentice = apprentice_id;
	}
	
	public int getSponsor()
	{
		return _sponsor;
	}
	
	public void setSponsor(int sponsor_id)
	{
		_sponsor = sponsor_id;
	}
	
	public void sendMessage(String message)
	{
		sendPacket(SystemMessage.sendString(message));
	}
	
	public void dropAllSummons()
	{
		// Delete summons and pets
		if (getPet() != null)
			getPet().unSummon(this);
		
		// Delete trained beasts
		if (getTrainedBeast() != null)
			getTrainedBeast().deleteMe();
		
		if (getCubics().size() > 0)
		{
			for (L2CubicInstance cubic : getCubics().values())
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			
			getCubics().clear();
		}
	}
	
	public void enterObserverMode(int x, int y, int z)
	{
		dropAllSummons();
		
		if (getParty() != null)
			getParty().removePartyMember(this);
		
		standUp();
		
		_obsLocation.set(getPosition().getWorldPosition());
		
		setTarget(null);
		stopMove(null);
		setIsParalyzed(true);
		setIsInvul(true);
		getAppearance().setInvisible();
		teleToLocation(x, y, z, false);
		sendPacket(new ObservationMode(x, y, z));
		_observerMode = true;
	}
	
	public void enterOlympiadObserverMode(int x, int y, int z, int id)
	{
		dropAllSummons();
		
		if (getParty() != null)
			getParty().removePartyMember(this);
		
		_olympiadGameId = id;
		
		standUp();
		
		if (!inObserverMode())
			_obsLocation.set(getPosition().getWorldPosition());
		
		setTarget(null);
		setIsInvul(true);
		
		getAppearance().setInvisible();
		
		teleToLocation(x, y, z, true);
		sendPacket(new ExOlympiadMode(3));
		_observerMode = true;
	}
	
	public void leaveObserverMode()
	{
		setTarget(null);
		setIsParalyzed(false);
		
		if (!isGM())
		{
			getAppearance().setVisible();
			setIsInvul(false);
		}
		
		if (hasAI())
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		
		sendPacket(new ObservationReturn(this));
		teleToLocation(_obsLocation.getX(), _obsLocation.getY(), _obsLocation.getZ(), false);
		
		broadcastUserInfo();
		
		_observerMode = false;
		_obsLocation.clean();
	}
	
	public void leaveOlympiadObserverMode()
	{
		setTarget(null);
		
		if (!isGM())
		{
			getAppearance().setVisible();
			setIsInvul(false);
		}
		if (hasAI())
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		_olympiadGameId = -1;
		
		sendPacket(new ExOlympiadMode(0));
		
		teleToLocation(_obsLocation.getX(), _obsLocation.getY(), _obsLocation.getZ(), false);
		
		broadcastUserInfo();
		
		_obsLocation.clean();
		_observerMode = false;
	}
	
	public void updateNameTitleColor()
	{
		// Donator Color and title update
		// Note: this code can be used for GM's too
		if (isDonator())
		{
			getAppearance().setNameColor(Config.DONATOR_NAME_COLOR);
		}
		broadcastUserInfo();
	}
	
	public void setOlympiadSide(int i)
	{
		_olympiadSide = i;
	}
	
	public int getOlympiadSide()
	{
		return _olympiadSide;
	}
	
	public void setOlympiadGameId(int id)
	{
		_olympiadGameId = id;
	}
	
	public int getOlympiadGameId()
	{
		return _olympiadGameId;
	}
	
	public int getObsX()
	{
		return _obsLocation.getX();
	}
	
	public int getObsY()
	{
		return _obsLocation.getY();
	}
	
	public int getObsZ()
	{
		return _obsLocation.getZ();
	}
	
	public boolean inObserverMode()
	{
		return _observerMode;
	}
	
	public int getTeleMode()
	{
		return _telemode;
	}
	
	public void setTeleMode(int mode)
	{
		_telemode = mode;
	}
	
	public void setLoto(int i, int val)
	{
		_loto[i] = val;
	}
	
	public int getLoto(int i)
	{
		return _loto[i];
	}
	
	public void setRace(int i, int val)
	{
		_race[i] = val;
	}
	
	public int getRace(int i)
	{
		return _race[i];
	}
	
	public boolean getMessageRefusal()
	{
		return _messageRefusal;
	}
	
	public void setMessageRefusal(boolean mode)
	{
		_messageRefusal = mode;
		sendPacket(_messageRefusal ? SystemMessageId.MESSAGE_REFUSAL_MODE : SystemMessageId.MESSAGE_ACCEPTANCE_MODE);
		sendPacket(new EtcStatusUpdate(this));
	}
	
	public void setDietMode(boolean mode)
	{
		_dietMode = mode;
	}
	
	public boolean getDietMode()
	{
		return _dietMode;
	}
	
	public void setTradeRefusal(boolean mode)
	{
		_tradeRefusal = mode;
		sendMessage(_tradeRefusal ? "Trade refusal is enabled." : "Trade refusal is disabled.");
	}
	
	public boolean getTradeRefusal()
	{
		return _tradeRefusal;
	}
	
	public void setExchangeRefusal(boolean mode)
	{
		_exchangeRefusal = mode;
	}
	
	public boolean getExchangeRefusal()
	{
		return _exchangeRefusal;
	}
	
	public BlockList getBlockList()
	{
		return _blockList;
	}
	
	public int getCount()
	{
		
		String HERO_COUNT = "SELECT count FROM heroes WHERE char_id=?";
		int _count = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(HERO_COUNT);
			statement.setString(1, getName());
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				_count = (rset.getInt("count"));
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": Error get heroes.");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		if (_count != 0)
			return _count;

		return 0;
	}
	
	public void reloadPVPHeroAura()
	{
		sendPacket(new UserInfo(this));
	}
	
	public void setHero(boolean hero)
	{
		if (hero && _baseClass == _activeClass)
		{
			for (HeroSkills sk : HeroSkills.getAllSkills())
			{
				L2Skill skill = SkillTable.getInstance().getInfo(sk.getSkillId(),sk.getLevel());
				addSkill(skill,false);
			}
		}
		else if (getCount() >= Config.HERO_COUNT && hero && Config.ALLOW_HERO_SUBSKILL)
		{
			for (HeroSkills sk : HeroSkills.getAllSkills())
			{
				L2Skill skill = SkillTable.getInstance().getInfo(sk.getSkillId(),sk.getLevel());
				addSkill(skill,false);
			}
		}
		else
		{
			
			for (HeroSkills sk : HeroSkills.getAllSkills())
			{
				L2Skill skill = SkillTable.getInstance().getInfo(sk.getSkillId(),sk.getLevel());
				super.removeSkill(skill);
			}
		}
		_hero = hero;
		
		sendSkillList();
	}
	
	public void setIsInOlympiadMode(boolean b)
	{
		_inOlympiadMode = b;
	}
	
	public void setIsOlympiadStart(boolean b)
	{
		_OlympiadStart = b;
	}
	
	public boolean isOlympiadStart()
	{
		return _OlympiadStart;
	}
	
	public boolean isHero()
	{
		return _hero;
	}
	
	public boolean isInOlympiadMode()
	{
		return _inOlympiadMode;
	}
	
	public boolean isInDuel()
	{
		return _isInDuel;
	}
	
	public int getDuelId()
	{
		return _duelId;
	}
	
	public void setDuelState(DuelState state)
	{
		_duelState = state;
	}
	
	public DuelState getDuelState()
	{
		return _duelState;
	}
	
	public void setIsInDuel(int duelId)
	{
		if (duelId > 0)
		{
			_isInDuel = true;
			_duelState = DuelState.ON_COUNTDOWN;
			_duelId = duelId;
		}
		else
		{
			if (_duelState == DuelState.DEAD)
			{
				enableAllSkills();
				getStatus().startHpMpRegeneration();
			}
			_isInDuel = false;
			_duelState = DuelState.NO_DUEL;
			_duelId = 0;
		}
	}
	
	public SystemMessage getNoDuelReason()
	{
		SystemMessage sm = SystemMessage.getSystemMessage(_noDuelReason);
		sm.addString(getName());
		_noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
		return sm;
	}
	
	public boolean canDuel()
	{
		if (isInCombat() || isInJail())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_BATTLE;
			return false;
		}
		if (isDead() || isAlikeDead() || (getCurrentHp() < getMaxHp() / 2 || getCurrentMp() < getMaxMp() / 2))
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_HP_OR_MP_IS_BELOW_50_PERCENT;
			return false;
		}
		if (isInDuel())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL;
			return false;
		}
		
		if (isInOlympiadMode())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD;
			return false;
		}
		if (isCursedWeaponEquiped())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_IN_A_CHAOTIC_STATE;
			return false;
		}
		if (getPrivateStoreType() != StoreType.NONE)
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
			return false;
		}
		if (isMounted() || isInBoat())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER;
			return false;
		}
		if (isFishing())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_FISHING;
			return false;
		}
		if (isInsideZone(ZoneId.PVP) || isInsideZone(ZoneId.PEACE) || isInsideZone(ZoneId.SIEGE))
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_S1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA;
			return false;
		}
		return true;
	}
	
	public boolean isNoble()
	{
		return _noble;
	}
	
	public void setNoble(boolean val)
	{
		if (val)
		{
			for (NobleSkills sk : NobleSkills.getAllSkills())
			{
				L2Skill skill = SkillTable.getInstance().getInfo(sk.getSkillId(),sk.getLevel());
				addSkill(skill,false);
			}
		}
		else
		{		
			for (NobleSkills sk : NobleSkills.getAllSkills())
			{
				L2Skill skill = SkillTable.getInstance().getInfo(sk.getSkillId(),sk.getLevel());
				super.removeSkill(skill);
			}
		}
		// from Sql
		_noble = val;
		
		sendSkillList();
	}
	
	public void setLvlJoinedAcademy(int lvl)
	{
		_lvlJoinedAcademy = lvl;
	}
	
	public int getLvlJoinedAcademy()
	{
		return _lvlJoinedAcademy;
	}
	
	public boolean isAcademyMember()
	{
		return _lvlJoinedAcademy > 0;
	}
	
	public void setTeam(Team team)
	{
		_team = team;
	}
	
	public Team getTeam()
	{
		return _team;
	}
	
	public void setWantsPeace(int wantsPeace)
	{
		_wantsPeace = wantsPeace;
	}
	
	public int getWantsPeace()
	{
		return _wantsPeace;
	}
	
	public boolean isFishing()
	{
		return _fishing;
	}
	
	public void setFishing(boolean fishing)
	{
		_fishing = fishing;
	}
	
	public void ClanSkills()
	{
		for (Iterator<?> i = Config.CLAN_SKILLS.keySet().iterator(); i.hasNext(); broadcastUserInfo())
		{
			Integer skillid = (Integer) i.next();
			int skilllvl = Config.CLAN_SKILLS.get(skillid).intValue();
			L2Skill skill = SkillTable.getInstance().getInfo(skillid.intValue(), skilllvl);
			if (skill != null)
			{
				addSkill(skill, true);
			}
			getClan().addNewSkill(skill);
			sendSkillList();
		}
		
		L2Clan clan = getClan();
		clan.setReputationScore(clan.getReputationScore() + Config.REPUTATION_QUANTITY, true);
		sendMessage((new StringBuilder()).append("Admin give to you ").append(Config.REPUTATION_QUANTITY).append(" Reputation Points.").toString());
		sendMessage("GM give to you all Clan Skills.");
	}
	
	public void setAllianceWithVarkaKetra(int sideAndLvlOfAlliance)
	{
		// [-5,-1] varka, 0 neutral, [1,5] ketra
		_alliedVarkaKetra = sideAndLvlOfAlliance;
	}
	
	public int getAllianceWithVarkaKetra()
	{
		return _alliedVarkaKetra;
	}
	
	public boolean isAlliedWithVarka()
	{
		return (_alliedVarkaKetra < 0);
	}
	
	public boolean isAlliedWithKetra()
	{
		return (_alliedVarkaKetra > 0);
	}

	public void sendSkillList()
	{
		final boolean isWearingFormalWear = isWearingFormalWear();
		final boolean isClanDisabled = getClan() != null && getClan().getReputationScore() < 0;
		
		final SkillList sl = new SkillList();
		
		for (L2Skill skill : getAllSkills())
		{
			if (skill.getId() > 9000 && skill.getId() < 9007)
				continue;
			
			sl.addSkill(skill.getId(), skill.getLevel(), skill.isPassive(), isWearingFormalWear || (skill.isClanSkill() && isClanDisabled));
			
			for (L2ShortCut sc : getAllShortCuts())
			{
				if (sc.getId() == skill.getId() && sc.getType() == L2ShortCut.TYPE_SKILL)
				{
					L2ShortCut Nsc = new L2ShortCut(sc.getSlot(), sc.getPage(), L2ShortCut.TYPE_SKILL, skill.getId(), skill.getLevel(), 1);
					sendPacket(new ShortCutRegister(this, Nsc));
					registerShortCut(Nsc);
				}
			}
		}
		
		sendPacket(sl);
	}
	
	public boolean addSubClass(int classId, int classIndex)
	{
		if (getTotalSubClasses() == Config.MAX_SUBCLASS || classIndex == 0)
			return false;
		
		if (getSubClasses().containsKey(classIndex))
			return false;
		
		final SubClass newClass = new SubClass(classId, classIndex);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Store the basic info about this new sub-class.
			PreparedStatement statement = con.prepareStatement(ADD_CHAR_SUBCLASS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, newClass.getClassId());
			statement.setLong(3, newClass.getExp());
			statement.setInt(4, newClass.getSp());
			statement.setInt(5, newClass.getLevel());
			statement.setInt(6, newClass.getClassIndex()); // <-- Added
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": Could not add character sub class for " + getName() + ": ");
			if (Config.DEVELOPER)
				e.printStackTrace();
			return false;
		}
		
		// Commit after database INSERT incase exception is thrown.
		getSubClasses().put(newClass.getClassIndex(), newClass);
		
		if (Config.DEBUG)
		{
			_log.info(getName() + " added class ID " + classId + " as a sub class at index " + classIndex + ".");
		}
		
		ClassId subTemplate = ClassId.values()[classId];
		Collection<L2SkillLearn> skillTree = SkillTreeData.getInstance().getAllowedSkills(subTemplate);
		
		if (skillTree == null)
			return true;
		
		Map<Integer, L2Skill> prevSkillList = new HashMap<>();
		
		for (L2SkillLearn skillInfo : skillTree)
		{
			if (skillInfo.getMinLevel() <= 40)
			{
				L2Skill prevSkill = prevSkillList.get(skillInfo.getId());
				L2Skill newSkill = SkillTable.getInstance().getInfo(skillInfo.getId(), skillInfo.getLevel());
				
				if (prevSkill != null && (prevSkill.getLevel() > newSkill.getLevel()))
				{
					continue;
				}
				
				prevSkillList.put(newSkill.getId(), newSkill);
				storeSkill(newSkill, prevSkill, classIndex);
			}
		}
		
		if (Config.DEBUG)
		{
			_log.info(getName() + " was given " + getAllSkills().length + " skills for their new sub class.");
		}
		
		return true;
	}
	
	public boolean modifySubClass(int classIndex, int newClassId)
	{
		int oldClassId = getSubClasses().get(classIndex).getClassId();
		
		if (Config.DEBUG)
		{
			_log.info(getName() + " has requested to modify sub class index " + classIndex + " from class ID " + oldClassId + " to " + newClassId + ".");
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Remove all henna info stored for this sub-class.
			PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNAS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();
			
			// Remove all shortcuts info stored for this sub-class.
			statement = con.prepareStatement(DELETE_CHAR_SHORTCUTS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();
			
			// Remove all effects info stored for this sub-class.
			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();
			
			// Remove all skill info stored for this sub-class.
			statement = con.prepareStatement(DELETE_CHAR_SKILLS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();
			
			// Remove all basic info stored about this sub-class.
			statement = con.prepareStatement(DELETE_CHAR_SUBCLASS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": Could not modify sub class for " + getName() + " to class index " + classIndex + ": ");
			if (Config.DEVELOPER)
				e.printStackTrace();
			
			// This must be done in order to maintain data consistency.
			getSubClasses().remove(classIndex);
			return false;
		}
		getSubClasses().remove(classIndex);
		return addSubClass(newClassId, classIndex);
	}
	
	public boolean isSubClassActive()
	{
		return _classIndex > 0;
	}
	
	public Map<Integer, SubClass> getSubClasses()
	{
		if (_subClasses == null)
		{
			_subClasses = new HashMap<>();
		}
		
		return _subClasses;
	}
	
	public int getTotalSubClasses()
	{
		return getSubClasses().size();
	}
	
	public int getBaseClass()
	{
		return _baseClass;
	}
	
	public int getActiveClass()
	{
		return _activeClass;
	}
	
	public int getClassIndex()
	{
		return _classIndex;
	}
	
	private void setClassTemplate(int classId)
	{
		_activeClass = classId;
		
		L2PcTemplate t = CharTemplateData.getInstance().getTemplate(classId);
		
		if (t == null)
		{
			_log.severe("Missing template for classId: " + classId);
			throw new Error();
		}
		
		// Set the template of the L2PcInstance
		setTemplate(t);
	}
	
	public synchronized boolean setActiveClass(int classIndex)
	{
		if (isInCombat() || getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
		{
			sendMessage("Impossible switch class if in combat");
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		// Remove active item skills before saving char to database
		// because next time when choosing this class, weared items can be different
		for (L2ItemInstance temp : getInventory().getAugmentedItems())
			if (temp != null && temp.isEquipped())
			{
				temp.getAugmentation().removeBoni(this);
			}
		
		// Delete a force buff upon class change.
		if (_forceBuff != null)
		{
			abortCast();
		}
		
		// Stop casting for any player that may be casting a force buff on this l2pcinstance.
		for (L2Character character : L2World.getInstance().getVisibleObjects(this, L2Character.class))
		{
			if (character.getForceBuff() != null && character.getForceBuff().getTarget() == this)
			{
				character.abortCast();
			}
		}
		
		store();
		
		if (classIndex == 0)
		{
			setClassTemplate(getBaseClass());
		}
		else
		{
			try
			{
				setClassTemplate(getSubClasses().get(classIndex).getClassId());
			}
			catch (Exception e)
			{
				_log.warning(L2PcInstance.class.getName() + ": Could not switch " + getName() + "'s sub class to class index " + classIndex + ": ");
				if (Config.DEVELOPER)
					e.printStackTrace();
				return false;
			}
		}
		_classIndex = classIndex;
		
		if (isInParty())
		{
			getParty().recalculatePartyLevel();
		}
		
		for (L2ItemInstance temp : getInventory().getAugmentedItems())
			if (temp != null && temp.isEquipped())
			{
				temp.getAugmentation().removeBoni(this);
			}
		
		if (getPet() != null && getPet() instanceof L2SummonInstance)
		{
			getPet().unSummon(this);
		}
		
		if (getCubics().size() > 0)
		{
			for (L2CubicInstance cubic : getCubics().values())
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			
			getCubics().clear();
		}
		
		// Delete a force buff upon class change.
		if (_forceBuff != null)
		{
			_forceBuff.delete();
		}
		
		// Stop casting for any player that may be casting a force buff on this l2pcinstance.
		
		for (L2Character character : L2World.getInstance().getVisibleObjects(this, L2Character.class))
		{
			if (character.getForceBuff() != null && character.getForceBuff().getTarget() == this)
			{
				character.abortCast();
			}
		}
		
		synchronized (getAllSkills())
		{
			
			for (final L2Skill oldSkill : getAllSkills())
			{
				super.removeSkill(oldSkill);
			}
			
		}
		// Yesod: Rebind CursedWeapon passive.
		if (isCursedWeaponEquiped())
		{
			CursedWeaponsManager.getInstance().givePassive(_cursedWeaponEquipedId);
		}
		
		stopAllEffects();
		
		if (isSubClassActive())
		{
			_dwarvenRecipeBook.clear();
			_commonRecipeBook.clear();
		}
		else
		{
			restoreRecipeBook();
		}
		
		// Restore any Death Penalty Buff
		restoreDeathPenaltyBuffLevel();
		
		restoreSkills();
		regiveTemporarySkills();
		rewardSkills();
		if (Config.RESTORE_EFFECTS_ON_SUBCLASS_CHANGE)
		{
			restoreEffects();
		}
		sendPacket(new EtcStatusUpdate(this));
		
		// if player has quest 422: Repent Your Sins, remove it
		QuestState st = getQuestState("422_RepentYourSins");
		
		if (st != null)
		{
			st.exitQuest(true);
		}
		
		for (int i = 0; i < 3; i++)
		{
			_henna[i] = null;
		}
		
		restoreHenna();
		sendPacket(new HennaInfo(this));
		
		if (getCurrentHp() > getMaxHp())
		{
			setCurrentHp(getMaxHp());
		}
		if (getCurrentMp() > getMaxMp())
		{
			setCurrentMp(getMaxMp());
		}
		if (getCurrentCp() > getMaxCp())
		{
			setCurrentCp(getMaxCp());
		}
		broadcastUserInfo();
		refreshOverloaded();
		refreshExpertisePenalty();
		
		// Clear resurrect xp calculation
		setExpBeforeDeath(0);
		
		// _macroses.restore();
		// _macroses.sendUpdate();
		_shortCuts.restore();
		sendPacket(new ShortCutInit(this));
		
		broadcastSocialActionInRadius(15);
		sendPacket(new SkillCoolTime(this));
		
		// decayMe();
		// spawnMe(getX(), getY(), getZ());
		
		return true;
	}
	
	public void stopWarnUserTakeBreak()
	{
		if (_taskWarnUserTakeBreak != null)
		{
			_taskWarnUserTakeBreak.cancel(true);
			_taskWarnUserTakeBreak = null;
		}
	}
	
	public void startWarnUserTakeBreak()
	{
		if (_taskWarnUserTakeBreak == null)
		{
			_taskWarnUserTakeBreak = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WarnUserTakeBreak(), 7200000, 7200000);
		}
	}
	
	public void stopRentPet()
	{
		if (_taskRentPet != null)
		{
			// if the rent of a wyvern expires while over a flying zone, tp to down before unmounting
			if (checkLandingState() && getMountType() == 2)
			{
				teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
			}
			
			if (setMountType(0)) // this should always be true now, since we teleported already
			{
				_taskRentPet.cancel(true);
				Ride dismount = new Ride(getObjectId(), Ride.ACTION_DISMOUNT, 0);
				sendPacket(dismount);
				broadcastPacket(dismount);
				_taskRentPet = null;
			}
		}
	}
	
	public void startRentPet(int seconds)
	{
		if (_taskRentPet == null)
		{
			_taskRentPet = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RentPetTask(), seconds * 1000L, seconds * 1000L);
		}
	}
	
	public boolean isRentedPet()
	{
		if (_taskRentPet != null)
			return true;
		
		return false;
	}
	
	public void checkWaterState()
	{
		if (isInsideZone(ZoneId.WATER))
		{
			if (getZ() > -3820)
			{
				WaterTaskManager.getInstance().remove(this);
				return;
			}
			WaterTaskManager.getInstance().add(this);
		}
		else
		{
			WaterTaskManager.getInstance().remove(this);
			return;
		}
	}
	
	public void onPlayerEnter()
	{
		startWarnUserTakeBreak();
		
		if (SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod())
		{
			if (!isGM() && isIn7sDungeon() && SevenSigns.getInstance().getPlayerCabal(this) != SevenSigns.getInstance().getCabalHighestScore())
			{
				teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
				setIsIn7sDungeon(false);
				sendMessage("You have been teleported to the nearest town due to the beginning of the Seal Validation period.");
			}
		}
		else
		{
			if (!isGM() && isIn7sDungeon() && SevenSigns.getInstance().getPlayerCabal(this) == SevenSigns.CABAL_NULL)
			{
				teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
				setIsIn7sDungeon(false);
				sendMessage("You have been teleported to the nearest town because you have not signed for any cabal.");
			}
		}
		
		// jail task
		updateJailState();
		revalidateZone(true);
	}
	
	public long getLastAccess()
	{
		return _lastAccess;
	}
	
	private void checkRecom(int recsHave, int recsLeft)
	{
		Calendar check = Calendar.getInstance();
		check.setTimeInMillis(_lastRecomUpdate);
		check.add(Calendar.DAY_OF_MONTH, 1);
		
		Calendar min = Calendar.getInstance();
		
		_recomHave = recsHave;
		_recomLeft = recsLeft;
		
		if (getStat().getLevel() < 10 || check.after(min))
			return;
		
		restartRecom();
	}
	
	public void restartRecom()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(DELETE_CHAR_RECOMS);
			statement.setInt(1, getObjectId());
			statement.execute();
			statement.close();
			
			_recomChars.clear();
		}
		catch (Exception e)
		{
			_log.warning("could not clear char recommendations: " + e);
		}
		
		if (getStat().getLevel() < 20)
		{
			_recomLeft = 3;
			_recomHave--;
		}
		else if (getStat().getLevel() < 40)
		{
			_recomLeft = 6;
			_recomHave -= 2;
		}
		else
		{
			_recomLeft = 9;
			_recomHave -= 3;
		}
		
		if (_recomHave < 0)
			_recomHave = 0;
		
		// If we have to update last update time, but it's now before 13, we should set it to yesterday
		Calendar update = Calendar.getInstance();
		if (update.get(Calendar.HOUR_OF_DAY) < 13)
			update.add(Calendar.DAY_OF_MONTH, -1);
		
		update.set(Calendar.HOUR_OF_DAY, 13);
		_lastRecomUpdate = update.getTimeInMillis();
	}
	
	@Override
	public void doRevive()
	{
		super.doRevive();
		updateEffectIcons();
		sendPacket(new EtcStatusUpdate(this));
		_reviveRequested = 0;
		_revivePower = 0;
		if (isInParty() && getParty().isInDimensionalRift())
		{
			if (!DimensionalRiftManager.getInstance().checkIfInPeaceZone(getX(), getY(), getZ()))
			{
				getParty().getDimensionalRift().memberRessurected(this);
			}
		}
		if ((_inEventTvT && TvT._started && Config.TVT_REVIVE_RECOVERY) || (_inEventCTF && CTF._started && Config.CTF_REVIVE_RECOVERY))
		{
			getStatus().setCurrentHp(getMaxHp());
			getStatus().setCurrentMp(getMaxMp());
			getStatus().setCurrentCp(getMaxCp());
		}
	}
	
	@Override
	public void doRevive(double revivePower)
	{
		doRevive();
		// Restore the player's lost experience, depending on the % return of the skill used (based on its power).
		restoreExp(revivePower);
	}
	
	public void reviveRequest(L2PcInstance Reviver, L2Skill skill, boolean Pet)
	{
		if (_reviveRequested == 1)
		{
			if (_revivePet == Pet)
			{
				Reviver.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
			}
			else
			{
				if (Pet)
				{
					Reviver.sendPacket(SystemMessageId.CANNOT_RES_PET2);
					// A pet cannot be resurrected while it's owner is in the process of resurrecting.
				}
				else
				{
					Reviver.sendPacket(SystemMessageId.MASTER_CANNOT_RES);
					// While a pet is attempting to resurrect, it cannot help in resurrecting its master.
				}
			}
			return;
		}
		if ((Pet && getPet() != null && getPet().isDead()) || (!Pet && isDead()))
		{
			_reviveRequested = 1;
			if (isPhoenixBlessed())
			{
				_revivePower = 100;
			}
			else
			{
				_revivePower = Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), Reviver.getWIT());
			}
			
			_revivePet = Pet;
			ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST_BY_S1.getId());
			dlg.addString(Reviver.getName());
			sendPacket(dlg);
			dlg = null;
		}
	}
	
	public void reviveAnswer(int answer)
	{
		if (_reviveRequested != 1 || (!isDead() && !_revivePet) || (_revivePet && getPet() != null && !getPet().isDead()))
			return;
		
		// If character refuse a PhoenixBlessed autoress, cancel all buffs he had
		if (answer == 0 && ((L2Playable) this).isPhoenixBlessed())
		{
			((L2Playable) this).stopPhoenixBlessing(null);
			stopAllEffects();
		}
		if (answer == 1)
		{
			if (!_revivePet)
			{
				if (_revivePower != 0)
				{
					doRevive(_revivePower);
				}
				else
				{
					doRevive();
				}
			}
			else if (getPet() != null)
			{
				if (_revivePower != 0)
				{
					getPet().doRevive(_revivePower);
				}
				else
				{
					getPet().doRevive();
				}
			}
		}
		_reviveRequested = 0;
		_revivePower = 0;
	}
	
	public boolean isReviveRequested()
	{
		return (_reviveRequested == 1);
	}
	
	public boolean isRevivingPet()
	{
		return _revivePet;
	}
	
	public void removeReviving()
	{
		_reviveRequested = 0;
		_revivePower = 0;
	}
	
	public void onActionRequest()
	{
		if (isSpawnProtected())
		{
			stopAbnormalEffect(2097152);
			setProtection(false);
			sendMessage("You are no longer under teleport protection.");
		}
	}
	
	public void setExpertiseIndex(int expertiseIndex)
	{
		_expertiseIndex = expertiseIndex;
	}
	
	public int getExpertiseIndex()
	{
		return _expertiseIndex;
	}
	
	@Override
	public final void onTeleported()
	{
		super.onTeleported();
		
		// Force a revalidation
		revalidateZone(true);
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0 && !isInOlympiadMode() && !inObserverMode())
		{
			startAbnormalEffect(2097152);
			setProtection(true);
		}
		
		if (!isGM())
			stopAllToggles();
		
		if (Config.ALLOW_WATER)
			checkWaterState();
		
		if (getTrainedBeast() != null)
		{
			getTrainedBeast().getAI().stopFollow();
			getTrainedBeast().teleToLocation(getX() + Rnd.get(-30, 30), getY() + Rnd.get(-30, 30), getZ(), false);
			getTrainedBeast().getAI().startFollow(this);
		}
		
		// Modify the position of the pet if necessary
		if (getPet() != null)
		{
			getPet().setFollowStatus(false);
			getPet().teleToLocation(getX() + Rnd.get(-30, 30), getY() + Rnd.get(-30, 30), getZ(), false);
			getPet().setFollowStatus(true);
			getPet().updateAndBroadcastStatus(0);
		}
		// To be sure update also the pvp flag / war tag status
		if (!inObserverMode())
			broadcastUserInfo();
	}
	
	public void setLastClientPosition(int x, int y, int z)
	{
		_lastClientPosition.setXYZ(x, y, z);
	}
	
	public boolean checkLastClientPosition(int x, int y, int z)
	{
		return _lastClientPosition.equals(x, y, z);
	}
	
	public boolean isSpawnProtected()
	{
		return _protectEndTime > GameTimeController.getInstance().getGameTicks();
	}
	
	public int getLastClientDistance(int x, int y, int z)
	{
		double dx = (x - _lastClientPosition.getX());
		double dy = (y - _lastClientPosition.getY());
		double dz = (z - _lastClientPosition.getZ());
		
		return (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	public void setLastServerPosition(int x, int y, int z)
	{
		_lastServerPosition.setXYZ(x, y, z);
	}
	
	public Point3D getLastServerPosition()
	{
		return _lastServerPosition;
	}

	public boolean checkLastServerPosition(int x, int y, int z)
	{
		return _lastServerPosition.equals(x, y, z);
	}
	
	public int getLastServerDistance(int x, int y, int z)
	{
		double dx = (x - _lastServerPosition.getX());
		double dy = (y - _lastServerPosition.getY());
		double dz = (z - _lastServerPosition.getZ());
		
		return (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	@Override
	public void addExpAndSp(long addToExp, int addToSp)
	{
		getStat().addExpAndSp(addToExp, addToSp);
	}
	
	public void removeExpAndSp(long removeExp, int removeSp)
	{
		getStat().removeExpAndSp(removeExp, removeSp);
	}
	
	@Override
	public void reduceCurrentHp(double i, L2Character attacker)
	{
		getStatus().reduceHp(i, attacker);
		
		// notify the tamed beast of attacks
		if (getTrainedBeast() != null)
		{
			getTrainedBeast().onOwnerGotAttacked(attacker);
		}
	}
	
	@Override
	public void reduceCurrentHp(double value, L2Character attacker, boolean awake)
	{
		getStatus().reduceHp(value, attacker, awake);
		
		// notify the tamed beast of attacks
		if (getTrainedBeast() != null)
		{
			getTrainedBeast().onOwnerGotAttacked(attacker);
		}
	}
	
	public void broadcastSnoop(int type, String name, String _text)
	{
		if (_snoopListener.size() > 0)
		{
			Snoop sn = new Snoop(getObjectId(), getName(), type, name, _text);
			
			for (L2PcInstance pci : _snoopListener)
				if (pci != null)
				{
					pci.sendPacket(sn);
				}
		}
	}
	
	public void teleportAnswer(int answer, int requesterId)
	{
		if (_summonRequest.getTarget() == null)
			return;
		if (answer == 1 && _summonRequest.getTarget().getObjectId() == requesterId)
		{
			teleToTarget(this, _summonRequest.getTarget(), _summonRequest.getSkill());
		}
		_summonRequest.setTarget(null, null);
	}
	
	public static void teleToTarget(L2PcInstance targetChar, L2PcInstance summonerChar, L2Skill summonSkill)
	{
		if (targetChar == null || summonerChar == null || summonSkill == null)
			return;
		
		if (!checkSummonerStatus(summonerChar))
			return;
		if (!checkSummonTargetStatus(targetChar, summonerChar))
			return;
		
		int itemConsumeId = summonSkill.getTargetConsumeId();
		int itemConsumeCount = summonSkill.getTargetConsume();
		if (itemConsumeId != 0 && itemConsumeCount != 0)
		{
			// Delete by rocknow
			if (targetChar.getInventory().getInventoryItemCount(itemConsumeId, 0) < itemConsumeCount)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING);
				sm.addItemName(summonSkill.getTargetConsumeId());
				targetChar.sendPacket(sm);
				return;
			}
			targetChar.getInventory().destroyItemByItemId("Consume", itemConsumeId, itemConsumeCount, summonerChar, targetChar);
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
			sm.addItemName(summonSkill.getTargetConsumeId());
			targetChar.sendPacket(sm);
		}
		targetChar.teleToLocation(summonerChar.getX(), summonerChar.getY(), summonerChar.getZ(), true);
	}
	
	public static boolean checkSummonTargetStatus(L2Object target, L2PcInstance summonerChar)
	{
		if (target == null || !(target instanceof L2PcInstance))
			return false;
		
		L2PcInstance targetChar = (L2PcInstance) target;
		
		if (targetChar.isAlikeDead() || targetChar.inObserverMode() || targetChar.isInStoreMode() || targetChar.isRooted() || targetChar.isInCombat())
			return false;
		
		if (targetChar.isInOlympiadMode())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD));
			return false;
		}
		
		if (targetChar.isFestivalParticipant() || targetChar.isFlying() || targetChar.isInCombat() || targetChar.isInsideZone(ZoneId.NO_SUMMON_FRIEND))
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
			return false;
		}
		return true;
	}
	
	public static boolean checkSummonerStatus(L2PcInstance summonerChar)
	{
		if (summonerChar == null)
			return false;
		
		if (summonerChar.isInOlympiadMode())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return false;
		}
		
		if (summonerChar.inObserverMode())
		{
			return false;
		}
		
		if (summonerChar.isInsideZone(ZoneId.NO_SUMMON_FRIEND) || summonerChar.isFlying() || summonerChar.isMounted())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
			return false;
		}
		return true;
	}
	
	public void addSnooper(L2PcInstance pci)
	{
		if (!_snoopListener.contains(pci))
		{
			_snoopListener.add(pci);
		}
	}
	
	public void removeSnooper(L2PcInstance pci)
	{
		_snoopListener.remove(pci);
	}
	
	public void addSnooped(L2PcInstance pci)
	{
		if (!_snoopedPlayer.contains(pci))
		{
			_snoopedPlayer.add(pci);
		}
	}
	
	public void removeSnooped(L2PcInstance pci)
	{
		_snoopedPlayer.remove(pci);
	}
	
	public synchronized void addBypass(String bypass)
	{
		if (bypass == null)
			return;
		_validBypass.add(bypass);
	}
	
	public synchronized void addBypass2(String bypass)
	{
		if (bypass == null)
			return;
		_validBypass2.add(bypass);
	}
	
	public synchronized boolean validateBypass(String cmd)
	{
		if (!Config.BYPASS_VALIDATION)
			return true;
		
		for (String bp : _validBypass)
		{
			if (bp == null)
				continue;
			
			if (bp.equals(cmd))
				return true;
		}
		
		for (String bp : _validBypass2)
		{
			if (bp == null)
				continue;
			
			if (cmd.startsWith(bp))
				return true;
		}
		
		return false;
	}
	
	public boolean validateItemManipulation(int objectId, String action)
	{
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		
		if (item == null || item.getOwnerId() != getObjectId())
		{
			_log.finest(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return false;
		}
		
		// Pet is summoned and not the item that summoned the pet AND not the
		// buggle from strider you're mounting
		if (getPet() != null && getPet().getControlItemId() == objectId || getMountObjectID() == objectId)
		{
			if (Config.DEBUG)
			{
				_log.finest(getObjectId() + ": player tried to " + action + " item controling pet");
			}
			
			return false;
		}
		
		if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
		{
			if (Config.DEBUG)
			{
				_log.finest(getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
			}
			
			return false;
		}
		
		if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
			// can not trade a cursed weapon
			return false;
		
		if (item.isWear())
			// cannot drop/trade wear-items
			return false;
		
		return true;
	}
	
	public synchronized void clearBypass()
	{
		_validBypass.clear();
		_validBypass2.clear();
	}
	
	public void setInCrystallize(boolean inCrystallize)
	{
		_inCrystallize = inCrystallize;
	}
	
	public boolean isInCrystallize()
	{
		return _inCrystallize;
	}
	
	public Point3D getInBoatPosition()
	{
		return _inBoatPosition;
	}
	
	public void setInBoatPosition(Point3D pt)
	{
		_inBoatPosition = pt;
	}
	
	@Override
	public void deleteMe()
	{
		try
		{
			abortAttack();
			abortCast();
			stopMove(null);
			setTarget(null);
			
			// Check if the L2PcInstance is in observer mode to set its position to
			// its position before entering in observer mode
			if (inObserverMode())
			{
				setXYZ(_obsLocation.getX(), _obsLocation.getY(), _obsLocation.getZ());
			}
			
			PartyMatchWaitingList.getInstance().removePlayer(this);
			if (_partyroom != 0)
			{
				PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_partyroom);
				if (room != null)
					room.deleteMember(this);
			}
			
			Castle castle = null;
			if (getClan() != null)
			{
				castle = CastleManager.getInstance().getCastleByOwner(getClan());
				if (castle != null)
				{
					castle.destroyClanGate();
				}
			}
			
			setOnlineStatus(false);
			// Stop the HP/MP/CP Regeneration task (scheduled tasks)
			stopAllTimers();
			// Stop crafting, if in progress
			RecipeController.getInstance().requestMakeItemAbort(this);
			
			// Cancel Attak or Cast
			setTarget(null);
			
			// Remove from world regions zones
			ZoneManager.getInstance().getRegion(this).removeFromZones(this);
			
			if (_forceBuff != null)
			{
				_forceBuff.delete();
			}
			for (L2Character character : L2World.getInstance().getVisibleObjects(this, L2Character.class))
				if (character.getForceBuff() != null && character.getForceBuff().getTarget() == this)
				{
					character.abortCast();
				}
			for (L2Effect effect : getAllEffects())
			{
				switch (effect.getEffectType())
				{
					case SIGNET_GROUND:
					case SIGNET:
					case SIGNET_EFFECT:
						effect.exit();
						break;
					default:
						break;
				}
			}
			
			// If a Party is in progress, leave it
			if (isInParty())
			{
				leaveParty();
			}
			
			// If the L2PcInstance has Pet, unsummon it
			if (getPet() != null)
			{
				getPet().unSummon(this);
			}
			
			if (getClanId() != 0 && getClan() != null)
			{
				// set the status for pledge member list to OFFLINE
				L2ClanMember clanMember = getClan().getClanMember(getName());
				if (clanMember != null)
				{
					clanMember.setPlayerInstance(null);
				}
			}
			
			if (getActiveRequester() != null)
			{
				// deals with sudden exit in the middle of transaction
				setActiveRequester(null);
			}
			
			// If the L2PcInstance is a GM, remove it from the GM List
			if (isGM())
			{
				AdminData.getInstance().deleteGm(this);
			}
			
			// Update database with items in its inventory and remove them from the world
			getInventory().deleteMe();
			
			// Update database with items in its warehouse and remove them from the world
			clearWarehouse();
			if (Config.WAREHOUSE_CACHE)
			{
				WarehouseCache.getInstance().remCacheTask(this);
			}
			
			// Update database with items in its freight and remove them from the world
			getFreight().deleteMe();
			
			if (getClanId() > 0)
			{
				getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);
				// ClanTable.getInstance().getClan(getClanId()).broadcastToOnlineMembers(new PledgeShowMemberListAdd(this));
			}
			
			if (isSeated())
			{
				final L2Object obj = L2World.getInstance().findObject(getMountObjectID());
				((L2StaticObjectInstance) obj).setBusy(false);
			}
			
			for (L2PcInstance player : _snoopedPlayer)
			{
				player.removeSnooper(this);
			}
			
			for (L2PcInstance player : _snoopListener)
			{
				player.removeSnooped(this);
			}
			
			notifyFriends(false);
			getBlockList().playerLogout();
			
			// Remove the L2PcInstance from the world
			decayMe();
			
		}
		catch (Exception e)
		{
			_log.warning(L2PcInstance.class.getSimpleName() + ": Exception on deleteMe()" + e.getMessage());
		}
	}
	
	private FishData _fish;
	
	public void startFishing(Location loc)
	{
		stopMove(null);
		setIsImmobilized(true);
		setFishing(true);
		
		_fishx = loc.getX();
		_fishy = loc.getY();
		_fishz = loc.getZ();
		// Starts fishing
		int group = GetRandomGroup();
		
		List<FishData> fish = FishTable.getInstance().getFish(GetRandomFishLvl(), GetRandomFishType(group), group);
		
		if (fish == null)
		{
			EndFishing(false);
			return;
		}
		_fish = fish.get(Rnd.get(fish.size())).clone();
		fish.clear();
		sendPacket(SystemMessageId.CAST_LINE_AND_START_FISHING);
		
		if (!GameTimeController.getInstance().isNight() && _lure.isNightLure())
			_fish.setFishGroup(-1);
		
		broadcastPacket(new ExFishingStart(this, _fish.getFishGroup(), loc, _lure.isNightLure()));
		sendPacket(Music.SF_P_01.getPacket());
		
		StartLookingForFishTask();
	}
	
	public void stopLookingForFishTask()
	{
		if (_taskforfish != null)
		{
			_taskforfish.cancel(false);
			_taskforfish = null;
		}
	}
	
	public void StartLookingForFishTask()
	{
		if (!isDead() && _taskforfish == null)
		{
			int checkDelay = 0;
			boolean isNoob = false;
			boolean isUpperGrade = false;
			
			if (_lure != null)
			{
				int lureid = _lure.getItemId();
				isNoob = _fish.getFishGroup() == 0;
				isUpperGrade = _fish.getFishGroup() == 2;
				
				if (lureid == 6519 || lureid == 6522 || lureid == 6525 || lureid == 8505 || lureid == 8508 || lureid == 8511)
					checkDelay = _fish.getGutsCheckTime() * 133;
				else if (lureid == 6520 || lureid == 6523 || lureid == 6526 || (lureid >= 8505 && lureid <= 8513) || (lureid >= 7610 && lureid <= 7613) || (lureid >= 7807 && lureid <= 7809) || (lureid >= 8484 && lureid <= 8486))
					checkDelay = _fish.getGutsCheckTime() * 100;
				else if (lureid == 6521 || lureid == 6524 || lureid == 6527 || lureid == 8507 || lureid == 8510 || lureid == 8513)
					checkDelay = _fish.getGutsCheckTime() * 66;
				
			}
			_taskforfish = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new LookingForFishTask(_fish.getStartCombatTime(), _fish.getFishGuts(), _fish.getFishGroup(), isNoob, isUpperGrade), 10000, checkDelay);
		}
	}
	
	private int GetRandomGroup()
	{
		switch (_lure.getItemId())
		{
			case 7807: // green for beginners
			case 7808: // purple for beginners
			case 7809: // yellow for beginners
			case 8486: // prize-winning for beginners
				return 0;
			case 8485: // prize-winning luminous
			case 8506: // green luminous
			case 8509: // purple luminous
			case 8512: // yellow luminous
				return 2;
			default:
				return 1;
		}
	}
	
	private int GetRandomFishType(int group)
	{
		int check = Rnd.get(100);
		int type = 1;
		switch (group)
		{
			case 0: // fish for novices
				switch (_lure.getItemId())
				{
					case 7807: // green lure, preferred by fast-moving (nimble) fish (type 5)
						if (check <= 54)
						{
							type = 5;
						}
						else if (check <= 77)
						{
							type = 4;
						}
						else
						{
							type = 6;
						}
						break;
					case 7808: // purple lure, preferred by fat fish (type 4)
						if (check <= 54)
						{
							type = 4;
						}
						else if (check <= 77)
						{
							type = 6;
						}
						else
						{
							type = 5;
						}
						break;
					case 7809: // yellow lure, preferred by ugly fish (type 6)
						if (check <= 54)
						{
							type = 6;
						}
						else if (check <= 77)
						{
							type = 5;
						}
						else
						{
							type = 4;
						}
						break;
					case 8486: // prize-winning fishing lure for beginners
						if (check <= 33)
						{
							type = 4;
						}
						else if (check <= 66)
						{
							type = 5;
						}
						else
						{
							type = 6;
						}
						break;
				}
				break;
			case 1: // normal fish
				switch (_lure.getItemId())
				{
					case 7610:
					case 7611:
					case 7612:
					case 7613:
						type = 3;
						break;
					case 6519: // all theese lures (green) are prefered by fast-moving (nimble) fish (type 1)
					case 8505:
					case 6520:
					case 6521:
					case 8507:
						if (check <= 54)
						{
							type = 1;
						}
						else if (check <= 74)
						{
							type = 0;
						}
						else if (check <= 94)
						{
							type = 2;
						}
						else
						{
							type = 3;
						}
						break;
					case 6522: // all theese lures (purple) are prefered by fat fish (type 0)
					case 8508:
					case 6523:
					case 6524:
					case 8510:
						if (check <= 54)
						{
							type = 0;
						}
						else if (check <= 74)
						{
							type = 1;
						}
						else if (check <= 94)
						{
							type = 2;
						}
						else
						{
							type = 3;
						}
						break;
					case 6525: // all theese lures (yellow) are prefered by ugly fish (type 2)
					case 8511:
					case 6526:
					case 6527:
					case 8513:
						if (check <= 55)
						{
							type = 2;
						}
						else if (check <= 74)
						{
							type = 1;
						}
						else if (check <= 94)
						{
							type = 0;
						}
						else
						{
							type = 3;
						}
						break;
					case 8484: // prize-winning fishing lure
						if (check <= 33)
						{
							type = 0;
						}
						else if (check <= 66)
						{
							type = 1;
						}
						else
						{
							type = 2;
						}
						break;
				}
				break;
			case 2: // upper grade fish, luminous lure
				switch (_lure.getItemId())
				{
					case 8506: // green lure, preferred by fast-moving (nimble) fish (type 8)
						if (check <= 54)
						{
							type = 8;
						}
						else if (check <= 77)
						{
							type = 7;
						}
						else
						{
							type = 9;
						}
						break;
					case 8509: // purple lure, preferred by fat fish (type 7)
						if (check <= 54)
						{
							type = 7;
						}
						else if (check <= 77)
						{
							type = 9;
						}
						else
						{
							type = 8;
						}
						break;
					case 8512: // yellow lure, preferred by ugly fish (type 9)
						if (check <= 54)
						{
							type = 9;
						}
						else if (check <= 77)
						{
							type = 8;
						}
						else
						{
							type = 7;
						}
						break;
					case 8485: // prize-winning fishing lure
						if (check <= 33)
						{
							type = 7;
						}
						else if (check <= 66)
						{
							type = 8;
						}
						else
						{
							type = 9;
						}
						break;
				}
		}
		return type;
	}
	
	private int GetRandomFishLvl()
	{
		L2Effect[] effects = getAllEffects();
		int skilllvl = getSkillLevel(1315);
		for (L2Effect e : effects)
		{
			if (e.getSkill().getId() == 2274)
			{
				skilllvl = (int) e.getSkill().getPower(this);
			}
		}
		if (skilllvl <= 0)
			return 1;
		int randomlvl;
		int check = Rnd.get(100);
		
		if (check <= 50)
		{
			randomlvl = skilllvl;
		}
		else if (check <= 85)
		{
			randomlvl = skilllvl - 1;
			if (randomlvl <= 0)
			{
				randomlvl = 1;
			}
		}
		else
		{
			randomlvl = skilllvl + 1;
			if (randomlvl > 27)
			{
				randomlvl = 27;
			}
		}
		
		return randomlvl;
	}
	
	public void StartFishCombat(boolean isNoob, boolean isUpperGrade)
	{
		_fishCombat = new L2Fishing(this, _fish, isNoob, isUpperGrade);
	}
	
	public void EndFishing(boolean win)
	{
		ExFishingEnd efe = new ExFishingEnd(win, this);
		broadcastPacket(efe);
		_fishing = false;
		_fishx = 0;
		_fishy = 0;
		_fishz = 0;
		broadcastUserInfo();
		if (_fishCombat == null)
		{
			sendPacket(SystemMessageId.BAIT_LOST_FISH_GOT_AWAY);
		}
		_fishCombat = null;
		_lure = null;
		// Ends fishing
		sendPacket(SystemMessageId.REEL_LINE_AND_STOP_FISHING);
		setIsImmobilized(false);
		stopLookingForFishTask();
		setFishing(false);
	}
	
	public L2Fishing GetFishCombat()
	{
		return _fishCombat;
	}
	
	public int GetFishx()
	{
		return _fishx;
	}
	
	public int GetFishy()
	{
		return _fishy;
	}
	
	public int GetFishz()
	{
		return _fishz;
	}
	
	public void SetLure(L2ItemInstance lure)
	{
		_lure = lure;
	}
	
	public L2ItemInstance GetLure()
	{
		return _lure;
	}
	
	public int getInventoryLimit()
	{
		if (isGM())
			return Config.INVENTORY_MAXIMUM_GM;
		
		return ((getRace() == ClassRace.DWARF) ? Config.INVENTORY_MAXIMUM_DWARF : Config.INVENTORY_MAXIMUM_NO_DWARF) + (int) getStat().calcStat(Stats.INV_LIM, 0, null, null);
	}
	
	public int getWareHouseLimit()
	{
		return ((getRace() == ClassRace.DWARF) ? Config.WAREHOUSE_SLOTS_DWARF : Config.WAREHOUSE_SLOTS_NO_DWARF) + (int) getStat().calcStat(Stats.WH_LIM, 0, null, null);
	}
	
	public int getPrivateSellStoreLimit()
	{
		return ((getRace() == ClassRace.DWARF) ? Config.MAX_PVTSTORE_SLOTS_DWARF : Config.MAX_PVTSTORE_SLOTS_OTHER) + (int) getStat().calcStat(Stats.P_SELL_LIM, 0, null, null);
	}
	
	public int getPrivateBuyStoreLimit()
	{
		return ((getRace() == ClassRace.DWARF) ? Config.MAX_PVTSTORE_SLOTS_DWARF : Config.MAX_PVTSTORE_SLOTS_OTHER) + (int) getStat().calcStat(Stats.P_BUY_LIM, 0, null, null);
	}
	
	public int getFreightLimit()
	{
		return Config.FREIGHT_SLOTS + (int) getStat().calcStat(Stats.FREIGHT_LIM, 0, null, null);
	}
	
	public int getDwarfRecipeLimit()
	{
		return Config.DWARF_RECIPE_LIMIT + (int) getStat().calcStat(Stats.REC_D_LIM, 0, null, null);
	}
	
	public int getCommonRecipeLimit()
	{
		return Config.COMMON_RECIPE_LIMIT + (int) getStat().calcStat(Stats.REC_C_LIM, 0, null, null);
	}
	
	public void setMountObjectID(int newID)
	{
		_mountObjectID = newID;
	}
	
	public int getMountObjectID()
	{
		return _mountObjectID;
	}
	
	private L2ItemInstance _lure = null;
	
	public SkillDat getCurrentSkill()
	{
		return _currentSkill;
	}
	
	public void restoreHP()
	{
		getStatus().setCurrentHp(getMaxHp());
	}
	
	public void restoreMP()
	{
		getStatus().setCurrentMp(getMaxMp());
	}
	
	public void restoreCP()
	{
		getStatus().setCurrentCp(getMaxCp());
	}
	
	public void setCurrentSkill(L2Skill currentSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if (currentSkill == null)
		{
			if (Config.DEBUG)
			{
				_log.info("Setting current skill: NULL for " + getName() + ".");
			}
			
			_currentSkill = null;
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.info("Setting current skill: " + currentSkill.getName() + " (ID: " + currentSkill.getId() + ") for " + getName() + ".");
		}
		
		_currentSkill = new SkillDat(currentSkill, ctrlPressed, shiftPressed);
	}
	
	public SkillDat getQueuedSkill()
	{
		return _queuedSkill;
	}
	
	public void setQueuedSkill(L2Skill queuedSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if (queuedSkill == null)
		{
			if (Config.DEBUG)
			{
				_log.info("Setting queued skill: NULL for " + getName() + ".");
			}
			
			_queuedSkill = null;
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.info("Setting queued skill: " + queuedSkill.getName() + " (ID: " + queuedSkill.getId() + ") for " + getName() + ".");
		}
		
		_queuedSkill = new SkillDat(queuedSkill, ctrlPressed, shiftPressed);
	}
	
	public boolean isInJail()
	{
		return _inJail;
	}
	
	public void setInJail(boolean state)
	{
		_inJail = state;
	}
	
	public void setInJail(boolean state, int delayInMinutes)
	{
		_inJail = state;
		_jailTimer = 0;
		// Remove the task if any
		stopJailTask(false);
		
		if (_inJail)
		{
			if (delayInMinutes > 0)
			{
				_jailTimer = delayInMinutes * 60000L; // in millisec
				
				// start the countdown
				_jailTask = ThreadPoolManager.getInstance().scheduleGeneral(new JailTask(this), _jailTimer);
				sendMessage("You are in jail for " + delayInMinutes + " minutes.");
			}
			
			// Open a Html message to inform the player
			NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
			String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_in.htm");
			if (jailInfos != null)
			{
				htmlMsg.setHtml(jailInfos);
			}
			else
			{
				htmlMsg.setHtml("<html><body>You have been put in jail by an admin.</body></html>");
			}
			sendPacket(htmlMsg);
			
			teleToLocation(-114356, -249645, -2984, true); // Jail
		}
		else
		{
			// Open a Html message to inform the player
			NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
			String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_out.htm");
			if (jailInfos != null)
			{
				htmlMsg.setHtml(jailInfos);
			}
			else
			{
				htmlMsg.setHtml("<html><body>You are free for now, respect server rules!</body></html>");
			}
			sendPacket(htmlMsg);
			
			teleToLocation(17836, 170178, -3507, true); // Floran
		}
		
		// store in database
		storeCharBase();
	}
	
	public long getJailTimer()
	{
		return _jailTimer;
	}
	
	public void setJailTimer(long time)
	{
		_jailTimer = time;
	}
	
	private void updateJailState()
	{
		if (isInJail())
		{
			// If jail time is elapsed, free the player
			if (_jailTimer > 0)
			{
				// restart the countdown
				_jailTask = ThreadPoolManager.getInstance().scheduleGeneral(new JailTask(this), _jailTimer);
				sendMessage("You are still in jail for " + Math.round(_jailTimer / 60000) + " minutes.");
			}
			
			// If player escaped, put him back in jail
			if (!isInsideZone(ZoneId.JAIL))
			{
				teleToLocation(-114356, -249645, -2984, true);
			}
		}
	}
	
	public void stopJailTask(boolean save)
	{
		if (_jailTask != null)
		{
			if (save)
			{
				long delay = _jailTask.getDelay(TimeUnit.MILLISECONDS);
				if (delay < 0)
				{
					delay = 0;
				}
				setJailTimer(delay);
			}
			_jailTask.cancel(false);
			_jailTask = null;
		}
	}
	
	private class JailTask implements Runnable
	{
		L2PcInstance _player;
		
		@SuppressWarnings("unused")
		protected long _startedAt;
		
		protected JailTask(L2PcInstance player)
		{
			_player = player;
			_startedAt = System.currentTimeMillis();
		}
		
		@Override
		public void run()
		{
			_player.setInJail(false, 0);
		}
	}
	
	public int getPowerGrade()
	{
		return _powerGrade;
	}
	
	public void setPowerGrade(int power)
	{
		_powerGrade = power;
	}
	
	public boolean isCursedWeaponEquiped()
	{
		return _cursedWeaponEquipedId != 0;
	}
	
	public void setCursedWeaponEquipedId(int value)
	{
		_cursedWeaponEquipedId = value;
	}
	
	public int getCursedWeaponEquipedId()
	{
		return _cursedWeaponEquipedId;
	}
	
	private boolean _charmOfCourage = false;
	
	public boolean getCharmOfCourage()
	{
		return _charmOfCourage;
	}
	
	public void setCharmOfCourage(boolean val)
	{
		_charmOfCourage = val;
		sendPacket(new EtcStatusUpdate(this));
	}
	
	public int getDeathPenaltyBuffLevel()
	{
		return _deathPenaltyBuffLevel;
	}
	
	public void setDeathPenaltyBuffLevel(int level)
	{
		_deathPenaltyBuffLevel = level;
	}
	
	public void calculateDeathPenaltyBuffLevel(L2Character killer)
	{
		if (!(Config.DEATH_PENALTY_CHANCE == 0))
		{
			if (Rnd.get(100) <= Config.DEATH_PENALTY_CHANCE && !(killer instanceof L2PcInstance) && !(isGM()) && !(getCharmOfLuck() && (killer instanceof L2GrandBossInstance || killer instanceof L2RaidBossInstance)))
			{
				increaseDeathPenaltyBuffLevel();
			}
		}
	}
	
	public void increaseDeathPenaltyBuffLevel()
	{
		if (getDeathPenaltyBuffLevel() >= 15) // maximum level reached
			return;
		
		if (getDeathPenaltyBuffLevel() != 0)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());
			
			if (skill != null)
			{
				removeSkill(skill, true);
			}
		}
		
		_deathPenaltyBuffLevel++;
		
		addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
		sendPacket(new EtcStatusUpdate(this));
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
		sm.addNumber(getDeathPenaltyBuffLevel());
		sendPacket(sm);
	}
	
	public void reduceDeathPenaltyBuffLevel()
	{
		if (getDeathPenaltyBuffLevel() <= 0)
			return;
		
		L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());
		
		if (skill != null)
		{
			removeSkill(skill, true);
		}
		
		_deathPenaltyBuffLevel--;
		
		if (getDeathPenaltyBuffLevel() > 0)
		{
			addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
			sendPacket(new EtcStatusUpdate(this));
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
			sm.addNumber(getDeathPenaltyBuffLevel());
			sendPacket(sm);
		}
		else
		{
			sendPacket(new EtcStatusUpdate(this));
			sendPacket(SystemMessageId.DEATH_PENALTY_LIFTED);
		}
	}
	
	public void restoreDeathPenaltyBuffLevel()
	{
		L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());
		
		if (skill != null)
		{
			removeSkill(skill, true);
		}
		
		if (getDeathPenaltyBuffLevel() > 0)
		{
			addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
			// SystemMessage sm = new
			// SystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
			// sm.addNumber(getDeathPenaltyBuffLevel());
			// sendPacket(sm);
		}
		// sendPacket(new EtcStatusUpdate(this));
	}
	
	// open/close gates
	private final GatesRequest _gatesRequest = new GatesRequest();
	
	protected static class GatesRequest
	{
		private L2DoorInstance _target = null;
		
		public void setTarget(L2DoorInstance door)
		{
			_target = door;
		}
		
		public L2DoorInstance getDoor()
		{
			return _target;
		}
	}
	
	public void gatesRequest(L2DoorInstance door)
	{
		_gatesRequest.setTarget(door);
	}
	
	public void gatesAnswer(int answer, int type)
	{
		if (_gatesRequest.getDoor() == null)
			return;
		
		if (answer == 1 && getTarget() == _gatesRequest.getDoor() && type == 1)
			_gatesRequest.getDoor().openMe();
		else if (answer == 1 && getTarget() == _gatesRequest.getDoor() && type == 0)
			_gatesRequest.getDoor().closeMe();
		
		_gatesRequest.setTarget(null);
	}
	
	private final Map<Integer, TimeStamp> _reuseTimeStamps = new ConcurrentHashMap<>();
	
	public static class TimeStamp
	{
		
		public long getStamp()
		{
			return stamp;
		}
		
		public int getSkill()
		{
			return skill;
		}
		
		public long getReuse()
		{
			return reuse;
		}
		
		public long getRemaining()
		{
			return Math.max(stamp - System.currentTimeMillis(), 0L);
		}
		
		public boolean hasNotPassed()
		{
			return System.currentTimeMillis() < stamp;
		}
		
		private final int skill;
		private final long reuse;
		private final long stamp;
		
		protected TimeStamp(int _skill, long _reuse)
		{
			skill = _skill;
			reuse = _reuse;
			stamp = System.currentTimeMillis() + reuse;
		}
		
		protected TimeStamp(int _skill, long _reuse, long _systime)
		{
			skill = _skill;
			reuse = _reuse;
			stamp = _systime;
		}
	}
	
	@Override
	public void addTimeStamp(int s, int r)
	{
		_reuseTimeStamps.put(s, new TimeStamp(s, r));
	}
	
	public void addTimeStamp(L2Skill skill, long reuse)
	{
		_reuseTimeStamps.put(skill.getId(), new TimeStamp(skill.getId(), reuse));
	}
	
	public void addTimeStamp(L2Skill skill, long reuse, long systime)
	{
		_reuseTimeStamps.put(skill.getId(), new TimeStamp(skill.getId(), reuse, systime));
	}
	
	@Override
	public void removeTimeStamp(int s)
	{
		_reuseTimeStamps.remove(s);
	}
	
	public Collection<TimeStamp> getReuseTimeStamps()
	{
		return _reuseTimeStamps.values();
	}
	
	public Map<Integer, TimeStamp> getReuseTimeStamp()
	{
		return _reuseTimeStamps;
	}
	
	public void resetSkillTime(boolean ssl)
	{
		L2Skill arr$[] = getAllSkills();
		int len$ = arr$.length;
		for (int i$ = 0; i$ < len$; i$++)
		{
			L2Skill skill = arr$[i$];
			if (skill != null && skill.isActive() && skill.getId() != 1324)
				enableSkill(skill.getId());
		}
		
		if (ssl)
			sendSkillList();
		sendPacket(new SkillCoolTime(this));
	}
	
	@Override
	public L2PcInstance getActingPlayer()
	{
		return this;
	}
	
	@Override
	public final void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if (miss)
		{
			sendPacket(SystemMessageId.MISSED_TARGET);
			return;
		}
		
		if (pcrit)
			sendPacket(SystemMessageId.CRITICAL_HIT);
		if (mcrit)
			sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC);

		if (target.isInvul())
		{
			if (target.isParalyzed())
				sendPacket(SystemMessageId.OPPONENT_PETRIFIED);
			else
				sendPacket(SystemMessageId.ATTACK_WAS_BLOCKED);
		}
		else
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG).addNumber(damage));
	
		if (isInOlympiadMode() && target instanceof L2PcInstance && ((L2PcInstance) target).isInOlympiadMode() && ((L2PcInstance) target).getOlympiadGameId() == getOlympiadGameId())
			OlympiadGameManager.getInstance().notifyCompetitorDamage(this, damage);
		
	}
	
	public void checkBanChat(boolean notEnterWorld)
	{
		long banLength = 0;
		String banReason = "";
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(BAN_CHAT_GET);
			statement.setString(1, getName());
			ResultSet rset = statement.executeQuery();
			rset.next();
			banLength = rset.getLong("chatban_timer");
			banReason = rset.getString("chatban_reason");
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": Could not select chat ban info:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		Calendar serv_time = Calendar.getInstance();
		long nowTime = serv_time.getTimeInMillis();
		banLength = (banLength - nowTime) / 1000;
		
		if (banLength > 0)
		{
			_chatBanned = true;
			setChatBanned(true, banLength, banReason);
		}
		else if (_chatBanned && notEnterWorld)
		{
			_chatBanned = false;
			setChatBanned(false, 0, "");
		}
	}
	
	public void setChatBanned(boolean isBanned, long banLength, String banReason)
	{
		_chatBanned = isBanned;
		long banLengthMSec = 0;
		
		if (isChatBanned())
		{
			Calendar serv_time = Calendar.getInstance();
			long nowTime = serv_time.getTimeInMillis();
			banLengthMSec = nowTime + (banLength * 1000);
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(BAN_CHAT_SET);
			statement.setLong(1, banLengthMSec);
			statement.setString(2, banReason);
			statement.setString(3, getName());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": Could not save chat ban info:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		if (isChatBanned())
		{
			long banLengthMins = banLength / 60;
			sendMessage("Your chat is banned (" + banLengthMins + " min)");
			if (banReason == "")
			{
				banReason = "-";
			}
			sendMessage("Reason: " + banReason);
		}
		else
		{
			sendMessage("Your chat ban has been lifted.");
		}
	}
	
	@Override
	public ForceBuff getForceBuff()
	{
		return _forceBuff;
	}
	
	public void setForceBuff(ForceBuff fb)
	{
		_forceBuff = fb;
	}
	
	public void dropItem(L2MonsterInstance npc, L2PcInstance player, int itemId, int count)
	{
		npc.dropItem(player, itemId, count);
	}
	
	public PunishLevel getPunishLevel()
	{
		return _punishLevel;
	}
	
	public boolean isInJail_()
	{
		return _punishLevel == PunishLevel.JAIL;
	}
	
	public boolean isChatBanned_()
	{
		return _punishLevel == PunishLevel.CHAT;
	}
	
	public void setPunishLevel(int state)
	{
		switch (state)
		{
			case 0:
			{
				_punishLevel = PunishLevel.NONE;
				break;
			}
			case 1:
			{
				_punishLevel = PunishLevel.CHAT;
				break;
			}
			case 2:
			{
				_punishLevel = PunishLevel.JAIL;
				break;
			}
			case 3:
			{
				_punishLevel = PunishLevel.CHAR;
				break;
			}
			case 4:
			{
				_punishLevel = PunishLevel.ACC;
				break;
			}
		}
	}
	
	public void setPunishLevel(PunishLevel state, int delayInMinutes)
	{
		long delayInMilliseconds = delayInMinutes * 60000L;
		switch (state)
		{
			case NONE: // Remove Punishments
			{
				switch (_punishLevel)
				{
					case CHAT:
					{
						_punishLevel = state;
						stopPunishTask(true);
						sendPacket(new EtcStatusUpdate(this));
						sendMessage("Your Chat ban has been lifted");
						break;
					}
					case JAIL:
					{
						_punishLevel = state;
						// Open a Html message to inform the player
						NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
						String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_out.htm");
						if (jailInfos != null)
						{
							htmlMsg.setHtml(jailInfos);
						}
						else
						{
							htmlMsg.setHtml("<html><body>You are free for now, respect server rules!</body></html>");
						}
						sendPacket(htmlMsg);
						stopPunishTask(true);
						teleToLocation(17836, 170178, -3507, true); // Floran
						break;
					}
					default:
						break;
				}
				break;
			}
			case CHAT: // Chat Ban
			{
				_punishLevel = state;
				_punishTimer = 0;
				sendPacket(new EtcStatusUpdate(this));
				// Remove the task if any
				stopPunishTask(false);
				
				if (delayInMinutes > 0)
				{
					_punishTimer = delayInMilliseconds;
					
					// start the countdown
					_punishTask = ThreadPoolManager.getInstance().scheduleGeneral(new PunishTask(this), _punishTimer);
					sendMessage("You are chat banned for " + delayInMinutes + " minutes.");
				}
				else
				{
					sendMessage("You have been chat banned");
				}
				break;
				
			}
			case JAIL: // Jail Player
			{
				_punishLevel = state;
				_punishTimer = 0;
				// Remove the task if any
				stopPunishTask(false);
				
				if (delayInMinutes > 0)
				{
					_punishTimer = delayInMilliseconds;
					
					// start the countdown
					_punishTask = ThreadPoolManager.getInstance().scheduleGeneral(new PunishTask(this), _punishTimer);
					sendMessage("You are in jail for " + delayInMinutes + " minutes.");
				}
				
				// Open a Html message to inform the player
				NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
				String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_in.htm");
				if (jailInfos != null)
				{
					htmlMsg.setHtml(jailInfos);
				}
				else
				{
					htmlMsg.setHtml("<html><body>You have been put in jail by an admin.</body></html>");
				}
				sendPacket(htmlMsg);
				setInstanceId(0);
				
				teleToLocation(-114356, -249645, -2984, false); // Jail
				break;
			}
			case CHAR: // Ban Character
			case ACC: // Ban Account
			default:
			{
				_punishLevel = state;
				break;
			}
		}
		
		// store in database
		storeCharBase();
	}
	
	public long getPunishTimer()
	{
		return _punishTimer;
	}
	
	public void setPunishTimer(long time)
	{
		_punishTimer = time;
	}
	
	public void stopPunishTask(boolean save)
	{
		if (_punishTask != null)
		{
			if (save)
			{
				long delay = _punishTask.getDelay(TimeUnit.MILLISECONDS);
				if (delay < 0)
				{
					delay = 0;
				}
				setPunishTimer(delay);
			}
			_punishTask.cancel(false);
			_punishTask = null;
		}
	}
	
	private class PunishTask implements Runnable
	{
		L2PcInstance _player;
		
		@SuppressWarnings("unused")
		protected long _startedAt;
		
		protected PunishTask(L2PcInstance player)
		{
			_player = player;
			_startedAt = System.currentTimeMillis();
		}
		
		@Override
		public void run()
		{
			_player.setPunishLevel(PunishLevel.NONE, 0);
		}
	}
	
	public void systemSendMessage(SystemMessageId id)
	{
		sendPacket(id);
	}
	
	public void removeCTFFlagOnDie()
	{
		CTF._flagsTaken.set(CTF._teams.indexOf(_teamNameHaveFlagCTF), false);
		CTF.spawnFlag(_teamNameHaveFlagCTF);
		CTF.removeFlagFromPlayer(this);
		broadcastUserInfo();
		_haveFlagCTF = false;
		CTF.AnnounceToPlayers(false, CTF._eventName + "(CTF): " + _teamNameHaveFlagCTF + "'s flag returned.");
	}

	private boolean _isVoting = false;
	
	public final boolean isVoting()
	{
		return _isVoting;
	}
	
	public final void setIsVoting(boolean value)
	{
		_isVoting = value;
	}
	
	public boolean awaitingAnswer = false;

	private final List<Integer> _completedAchievements = new ArrayList<>();
	
	public long getOnlineTime()
	{
		return _onlineTime;
	}
	
	public List<Integer> getCompletedAchievements()
	{
		return _completedAchievements;
	}
	
	public boolean readyAchievementsList()
	{
		if (_completedAchievements.isEmpty())
			return false;
		return true;
	}
	
	public void saveAchievemntData()
	{
		
	}
	
	public void getAchievemntData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM achievements WHERE owner_id=" + getObjectId());
			ResultSet rs = statement.executeQuery();
			String values = "owner_id";
			String in = Integer.toString(getObjectId());
			String questionMarks = in;
			int ilosc = AchievementsManager.getInstance().getAchievementList().size();
			
			if (rs.next())
			{
				_completedAchievements.clear();
				for (int i = 1; i <= ilosc; i++)
				{
					int a = rs.getInt("a" + i);
					
					if (!_completedAchievements.contains(i))
					{
						if (a == 1 || String.valueOf(a).startsWith("1"))
							_completedAchievements.add(i);
					}
				}
				rs.close();
			}
			else
			{
				// Player hasnt entry in database, means we have to create it.
				for (int i = 1; i <= ilosc; i++)
				{
					values += ", a" + i;
					questionMarks += ", 0";
				}
				
				String s = "INSERT INTO achievements(" + values + ") VALUES (" + questionMarks + ")";
				PreparedStatement insertStatement = con.prepareStatement(s);
				insertStatement.execute();
				insertStatement.close();
			}
			rs.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": [ACHIEVEMENTS ENGINE GETDATA]");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void saveAchievementData(int achievementID, int objid)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			Statement statement = con.createStatement();
			if (achievementID == 4 || achievementID == 6 || achievementID == 11 || achievementID == 13)
			{
				statement.executeUpdate("UPDATE achievements SET a" + achievementID + "=1" + objid + " WHERE owner_id=" + getObjectId());
			}
			else
			{
				statement.executeUpdate("UPDATE achievements SET a" + achievementID + "=1 WHERE owner_id=" + getObjectId());
			}
			
			statement.close();
			
			if (!_completedAchievements.contains(achievementID))
			{
				_completedAchievements.add(achievementID);
			}
		}
		catch (SQLException e)
		{
			_log.warning(L2PcInstance.class.getName() + ": [ACHIEVEMENTS SAVE GETDATA]");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public SkillDat getCurrentPetSkill()
	{
		return _currentPetSkill;
	}
	
	public void setCurrentPetSkill(L2Skill currentSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if (currentSkill == null)
		{
			if (Config.DEBUG)
				_log.info("Setting current pet skill: NULL for " + getName() + ".");
			
			_currentPetSkill = null;
			
			return;
		}
		if (Config.DEBUG)
			_log.info("Setting current Pet skill: " + currentSkill.getName() + " (ID: " + currentSkill.getId() + ") for " + getName() + ".");
		_currentPetSkill = new SkillDat(currentSkill, ctrlPressed, shiftPressed);
	}
	
	public boolean isStored()
	{
		return _isStored;
	}
	
	public void setStored(boolean a)
	{
		_isStored = a;
	}
	
	public void endDuel()
	{
		if (isInDuel() && getDuelState() == DuelState.DUELLING)
			setDuelState(DuelState.INTERRUPTED);
	}
	
	public void EnterWolrd()
	{
		final IpCatcher ipc = new IpCatcher();
		
		if (ipc.isCatched(this))
			closeNetConnection(true);
		
		if (isGM())
		{
			if (Config.GM_STARTUP_INVULNERABLE && AdminData.getInstance().hasAccess("admin_invul", getAccessLevel()))
				setIsInvul(true);
			if (Config.GM_STARTUP_INVISIBLE && AdminData.getInstance().hasAccess("admin_invisible", getAccessLevel()))
				getAppearance().setInvisible();
			if (Config.GM_STARTUP_SILENCE && AdminData.getInstance().hasAccess("admin_silence", getAccessLevel()))
				setMessageRefusal(true);
			if (Config.GM_STARTUP_AUTO_LIST && AdminData.getInstance().hasAccess("admin_gmliston", getAccessLevel()))
				AdminData.getInstance().addGm(this, false);
			else
				AdminData.getInstance().addGm(this, true);
		}
		
		if (getLevel() > 9 && hasSkill(L2Skill.SKILL_LUCKY))
			removeSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_LUCKY, 1));
		
		standUp();
		
		if (isDead())
		{
			doDie(this);
			doRevive();
		}
				
		setRunning();
		checkBanChat(false);
		
		final L2Clan clan = getClan();
		
		if (clan != null)
		{
			sendPacket(new PledgeSkillList(clan));
			notifyClanMembers();
			notifySponsorOrApprentice();
			
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
					continue;
				if (siege.checkIsAttacker(clan))
					setSiegeState((byte) 1);
				else if (siege.checkIsDefender(clan))
					setSiegeState((byte) 2);
			}

			final ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(clan);
			if (clanHall != null)
				if (!clanHall.getPaid())
					sendPacket(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
			
			sendPacket(new PledgeShowMemberListAll(clan, 0));
			
			for (SubPledge sp : clan.getAllSubPledges())
			{
				if (sp == null)
					continue;
				
				sendPacket(new PledgeShowMemberListAll(clan, sp.getId()));
			}
			sendPacket(new PledgeStatusChanged(clan));
		}

		spawnMe(getX(), getY(), getZ());
		sendPacket(new ValidateLocation(this));
		sendPacket(new FinishRotation(this));
		
        EnterWorldchecks();
		
		// buff and status icons
		if (Config.STORE_SKILL_COOLTIME)
			restoreEffects();
		
		// check for crowns
		CrownManager.getInstance().checkCrowns(this);
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0)
			setProtection(true);
		
		if (L2Event.active && L2Event.connectionLossData.containsKey(getName()) && L2Event.isOnEvent(this))
			L2Event.restoreChar(this);
		else if (L2Event.connectionLossData.containsKey(getName()))
			L2Event.restoreAndTeleChar(this);
		
		if (TvT._savePlayers.contains(getName()))
			TvT.addDisconnectedPlayer(this);
		
		if (CTF._savePlayers.contains(getName()))
			CTF.addDisconnectedPlayer(this);
		
		if (DM._savePlayers.contains(getName()))
			DM.addDisconnectedPlayer(this);
		
		if (SevenSigns.getInstance().isSealValidationPeriod())
			sendPacket(new SignsSky());
		
		if ((getFirstEffect(426) != null) || (getFirstEffect(427) != null))
		{
			stopSkillEffects(426);
			stopSkillEffects(427);
			updateEffectIcons();
		}
		if (getAllEffects() != null)
		{
			for (L2Effect e : getAllEffects())
			{
				if (e.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME)
				{
					stopEffects(L2Effect.EffectType.HEAL_OVER_TIME);
					removeEffect(e);
				}
				
				if (e.getEffectType() == L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME)
				{
					stopEffects(L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME);
					removeEffect(e);
				}
			}
		}

		setDonator(getPremiumService() == 1 ? true : false);
		
		if (isDonator() && Config.DONATOR_NAME_COLOR_ENABLED)
			getAppearance().setNameColor(Config.DONATOR_NAME_COLOR);
		
		if (isDonator() && Config.DONATOR_TITLE_COLOR_ENABLED)
			getAppearance().setTitleColor(Config.DONATOR_TITLE_COLOR);
		
		if (Config.RANK_PVP_SYSTEM_ENABLED)
			RankPvpSystem.updateNickAndTitleColor(this, null);
		
		// apply augmentation bonus for equipped items
		for (L2ItemInstance temp : getInventory().getAugmentedItems())
		{
			if (temp != null && temp.isEquipped())
				temp.getAugmentation().applyBoni(this);
		}
						
		if (ZodiacMain.voting && !ZodiacMain.HasVoted(this))
			ZodiacMain.showHtmlWindow(this);
		
		if (Config.SERVER_NEWS)
		{
			if (isGM())
			{
				final String Welcome_Path = "data/html/welcomeGM.htm";
				File mainText = new File(PackRoot.DATAPACK_ROOT, Welcome_Path);
				if (mainText.exists())
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(Welcome_Path);
					html.replace("%name%", getName());
					sendPacket(html);
				}
				
			}
			else
			{
				final String Welcome_Path = "data/html/welcomeP.htm";
				File mainText = new File(PackRoot.DATAPACK_ROOT, Welcome_Path);
				if (mainText.exists())
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(Welcome_Path);
					html.replace("%name%", getName());
					sendPacket(html);
				}
			}
		}
		
		if (Config.ALLOW_REMOTE_CLASS_MASTER)
		{
			int lvlnow = getClassId().level();
			if (getLevel() >= 20 && lvlnow == 0)
				L2ClassMasterInstance.ClassMaster.onAction(this);
			else if (getLevel() >= 40 && lvlnow == 1)
				L2ClassMasterInstance.ClassMaster.onAction(this);
			else if (getLevel() >= 76 && lvlnow == 2)
				L2ClassMasterInstance.ClassMaster.onAction(this);
		}

		if (AntiBot.isvoting)
			AntiBot.showHtmlWindow(this);
		
		if (Hero.getInstance().getHeroes() != null && Hero.getInstance().getHeroes().containsKey(getObjectId()))
			setHero(true);
			
		onPlayerEnter();
		Quest.playerEnter(this);
		
		if (Config.ALLOW_TUTORIAL)
			loadTutorial();
		
		recalcHennaStats();
		
		getMacroses().sendUpdate();
		sendPacket(new QuestList(this));
		sendPacket(new HennaInfo(this));
		sendPacket(new FriendList(this));
		sendPacket(new EtcStatusUpdate(this));
		sendPacket(new ItemList(this, false));
		sendPacket(new ShortCutInit(this));
		sendPacket(new ExStorageMaxCount(this));
		
		if (isAlikeDead())
			sendPacket(new Die(this));// no broadcast needed since the player will already spawn dead to others
			
		sendSkillList();
				
		// add char to online characters
		setOnlineStatus(true);
		
		Announcements.getInstance().showAnnouncements(this);
		
		// engage and notify Partner
		if (Config.MOD_ALLOW_WEDDING)
		{
			engage();
			notifyPartner(getPartnerId());
		}
		
		if (Config.ALLOW_WATER)
			checkWaterState();
		
		// check player skills
		if (Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN && !Config.ALT_SUBCLASS_SKILLS)
			checkAllowedSkills();
		
		setPledgeClass();
		
		notifyFriends(true);
		
		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(getX(), getY(), getZ(), false))
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(this);
		
		if (getClanJoinExpiryTime() > System.currentTimeMillis())
			sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
		
		if (!isGM() && getSiegeState() < 2 && isInsideZone(ZoneId.SIEGE))
		{
			// Attacker or spectator logging in to a siege zone. Actually should
			// be checked for inside castle only?
			teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
			sendMessage("You have been teleported to the nearest town due to you being in siege zone.");
		}

		sendPacket(new SkillCoolTime(this));
		
		broadcastUserInfo();

		sendPacket(SystemMessageId.WELCOME_TO_LINEAGE);
		
		if (Olympiad.getInstance().playerInStadia(this))
		{
			teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
			sendMessage("You have been teleported to the nearest town.");
		}

		RegionBBSManager.getInstance().changeCommunityBoard();
		
		sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	protected void EnterWorldchecks()
	{		
		if (!isGM())
		{
			final L2Clan clan = getClan();

			if (Config.APELLA_ARMORS && (clan == null || getPledgeClass() < 5))
			{
				int i;
				for (i = 7860; i < 7879; i++)
				{
					L2ItemInstance apella = getInventory().getItemByItemId(i);
					if (apella != null)
					{
						if (apella.isEquipped())
							getInventory().unEquipItemInSlot(apella.getEquipSlot());
					}
				}
			}
			
			if (Config.OATH_ARMORS && clan == null)
			{
				int i;
				for (i = 7850; i < 7859; i++)
				{
					L2ItemInstance oath = getInventory().getItemByItemId(i);
					if (oath != null)
					{
						if (oath.isEquipped())
							getInventory().unEquipItemInSlot(oath.getEquipSlot());
					}
				}
			}
			
		    for (L2ItemInstance i : getInventory().getItems())
		    {
			     if (i.getItemType() != L2EtcItemType.PET_COLLAR)
			     {
					 if (i.getEnchantLevel() > Config.ENCHANT_MAX_ALLOWED_WEAPON || i.getEnchantLevel() > Config.ENCHANT_MAX_ALLOWED_ARMOR || i.getEnchantLevel() > Config.ENCHANT_MAX_ALLOWED_JEWELRY)
					 {
						 getInventory().destroyItem(null, i, this, null);
						 sendMessage("[Server]:You have items over enchanted you will be kicked!");
						 setPunishLevel(L2PcInstance.PunishLevel.JAIL, 1200);
						 Util.handleIllegalPlayerAction(this, "Player " + getName() + " have item over enchanted ", Config.DEFAULT_PUNISH);
						 _log.info("Over enchanted item {" + i + "} has been removed from " + getName() + ".");
					 }
			     }
		    }
		    
			if (!Config.ALLOW_DUALBOX)
			{
				String thisip = getClient().getConnection().getInetAddress().getHostAddress();
				Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers().values();
				L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);
				for (L2PcInstance player : players)
				{
					if (player.getClient().getConnection().getInetAddress().getHostAddress() == null)
						return;
					String ip = player.getClient().getConnection().getInetAddress().getHostAddress();
					if (thisip.equals(ip) && (this != player))
					{
						player.sendMessage("I'm sorry, but multibox is not allowed here.");
						player.sendPacket(new LeaveWorld());
						closeNetConnection(true);
					}
				}
			}
		}				
	}
	
	public void useEquippableItem(int objectId, boolean abortAttack)
	{
		final L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		
		if (item == null)
			return;
		
		L2ItemInstance[] items = null;
		final boolean isEquipped = item.isEquipped();
		final int oldInvLimit = getInventoryLimit();
		SystemMessage sm = null;
		

		if (item.getItem() instanceof L2Weapon)
		{
			item.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
			item.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
		}
		
		if (isEquipped)
		{
			if (item.getEnchantLevel() > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				sm.addNumber(item.getEnchantLevel());
				sm.addItemName(item.getItemId());
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(item.getItemId());
			}
			sendPacket(sm);
			
			items = getInventory().unEquipItemInBodySlotAndRecord(item);
			WeddingSKillCheck(item, false);
			
			if (item.getItemType().equals(L2WeaponType.BOW))
			{
				for (L2Skill skill : getAllSkills())
				{
					if (skill.getId() == 313)
						stopSkillEffects(skill.getId());
				}
			}
		}
		else
		{
			items = getInventory().equipItemAndRecord(item);
			
			if (item.isEquipped())
			{
				if (item.getEnchantLevel() > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(item.getItemId());
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
					sm.addItemName(item.getItemId());
				}
				sendPacket(sm);
				
				// Consume mana - will start a task if required; returns if item is not a shadow item
				item.decreaseMana(false);
				if (item.getItem() instanceof L2Weapon)
				{
					// charge Soulshot/Spiritshot like L2OFF
					rechargeAutoSoulShot(true, true, false);
					item.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
					item.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					CheckIfWeaponIsAllowed();
				}
				WeddingSKillCheck(item, true);
			}
			else
				sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
		}
		
		refreshExpertisePenalty();
		broadcastUserInfo();
		
		InventoryUpdate iu = new InventoryUpdate();
		iu.addItems(Arrays.asList(items));
		sendPacket(iu);
		
		if (abortAttack)
			abortAttack();
		
		if (getInventoryLimit() != oldInvLimit)
			sendPacket(new ExStorageMaxCount(this));
	}
	
	void WeddingSKillCheck(L2ItemInstance item, boolean equiped)
	{
		if (equiped && item.getItemId() == 9140)
			addSkill(SkillTable.getInstance().getInfo(3261, 1));
		else if (item.getItemId() == 9140)
			removeSkill(SkillTable.getInstance().getInfo(3261, 1));
	}
	
	public void giveClassItems(ClassId classId)
	{
		List<Integer> items = new ArrayList<>();

		switch (classId)
		{
			case GHOST_HUNTER:
			case WIND_RIDER:
			case ADVENTURER:
				items = Arrays.asList(6590,6379,6380,6381,6382,920,858,858,889,889);
				break;
			case SAGGITARIUS:
			case MOONLIGHT_SENTINEL:
			case GHOST_SENTINEL:
				items = Arrays.asList(7577,6379,6380,6381,6382,920,858,858,889,889);
				break;
			case DUELIST:
				items = Arrays.asList(6580,6373,6374,6375,6376,6378,920,858,858,889,889);
				break;
			case TITAN:
				items = Arrays.asList(6605,6373,6374,6375,6376,6378,920,858,858,889,889);
				break;
			case GRAND_KHAVATARI:
				items = Arrays.asList(6604,6379,6380,6381,6382,920,858,858,889,889);
				break;
			case PHOENIX_KNIGHT:
				items = Arrays.asList(6581,6373,6374,6375,6376,6377,6378,920,858,858,889,889);
				break;
			case FORTUNE_SEEKER:
			case MAESTRO:
			case SHILLIEN_TEMPLAR:
				items = Arrays.asList(6585,6373,6374,6375,6376,6377,6378,920,858,858,889,889);
				break;
			case DREADNOUGHT:
				items = Arrays.asList(6601,6373,6374,6375,6376,6378,920,858,858,889,889);
				break;
			case HELL_KNIGHT:
			case EVAS_TEMPLAR:
				items = Arrays.asList(6581,6373,6374,6375,6376,6377,6378,920,858,858,889,889);
				break;
			case SWORD_MUSE:
				items = Arrays.asList(6581,6379,6380,6381,6382,920,858,858,889,889,6377);
				break;
			case SPECTRAL_DANCER:
				items = Arrays.asList(6580,6379,6380,6381,6382,920,858,858,889,889);
				break;
			default:
				if (isMageClass())
					items = Arrays.asList(6608,2407,5767,5779,512,920,858,858,889,889);
				break;
		}

		if (items.isEmpty())
			return;
		
		for (int id : items)
		{
			getInventory().addItem("Armors", id, 1, this, null);
			getInventory().equipItemAndRecord(getInventory().getItemByItemId(id));
		}
		
	    getInventory().reloadEquippedItems();

		sendPacket(new ItemList(this, false));
		sendPacket(new StatusUpdate(getObjectId()));
		sendPacket(new ActionFailed());
	}
	
	public void checkItemRestriction()
	{
		for (L2ItemInstance equippedItem : getInventory().getItems())
		{
			if (equippedItem != null && !equippedItem.getItem().checkCondition(this, this))
			{
				getInventory().unEquipItemInSlot(equippedItem.getItemId());
				
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(equippedItem);
				sendPacket(iu);
				
				SystemMessage sm = null;
				if (equippedItem.getEnchantLevel() > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(equippedItem.getEnchantLevel());
					sm.addItemName(equippedItem);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(equippedItem);
				}
				sendPacket(sm);
			}
		}
	}
	
	public void disableAutoShotsAll()
	{
		for (int itemId : _activeSoulShots.values())
		{
			sendPacket(new ExAutoSoulShot(itemId, 0));
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(itemId));
		}
		_activeSoulShots.clear();
	}
	
	public boolean disableAutoShot(int itemId)
	{
		if (_activeSoulShots.containsKey(itemId))
		{
			removeAutoSoulShot(itemId);
			sendPacket(new ExAutoSoulShot(itemId, 0));
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(itemId));
			return true;
		}
		
		return false;
	}
	
	public L2Summon getOwner()
	{
		return getOwner();
	}
	
	private void engage()
	{
		int _chaid = getObjectId();
		
		for (Couple cl : CoupleManager.getInstance().getCouples())
			if (cl.getPlayer1Id() == _chaid || cl.getPlayer2Id() == _chaid)
			{
				if (cl.getMaried())
					setMarried(true);
				
				setCoupleId(cl.getId());
				
				if (cl.getPlayer1Id() == _chaid)
					setPartnerId(cl.getPlayer2Id());
				else
					setPartnerId(cl.getPlayer1Id());
			}
	}
	
	private void notifyPartner(int partnerId)
	{
		if (getPartnerId() != 0)
		{
			L2PcInstance partner;
			partner = L2World.getInstance().getPlayer(getPartnerId());
			
			if (partner != null)
				partner.sendMessage("Your Partner has logged in.");
			
			partner = null;
		}
	}
	
	private void notifyFriends(boolean login)
	{
		for (int id : _friendList)
		{
			L2PcInstance friend = L2World.getInstance().getPlayer(id);
			if (friend != null)
			{
				friend.sendPacket(new FriendList(friend));
				
				if (login)
					friend.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN).addCharName(this));
			}
		}
	}
	
	private void notifyClanMembers()
	{
		L2Clan clan = getClan();
		if (clan != null)
		{
			clan.broadcastClanStatus();
			clan.getClanMember(getName()).setPlayerInstance(this);
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN);
			msg.addString(getName());
			clan.broadcastToOtherOnlineMembers(msg, this);
			msg = null;
			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);
			if (clan.isNoticeEnabled())
			{
				sendPacket(new NpcHtmlMessage(1,
				
				"<html><title>Clan Announcements</title><body>" + "<br><center>" + "<font color=\"CCAA00\">" + getClan().getName() + "</font> <font color=\"6655FF\">Clan Alert Message</font></center><br>" + "<img src=\"L2UI.SquareWhite\" width=270 height=1><br>" + getClan().getNotice().replaceAll("\r\n", "<br>") + "</body></html>"));
				
			}
		}
	}
	
	private void notifySponsorOrApprentice()
	{
		if (getSponsor() != 0)
		{
			L2PcInstance sponsor = L2World.getInstance().getPlayer(getSponsor());
			
			if (sponsor != null)
			{
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
				msg.addString(getName());
				sponsor.sendPacket(msg);
			}
		}
		else if (getApprentice() != 0)
		{
			L2PcInstance apprentice = L2World.getInstance().getPlayer(getApprentice());
			
			if (apprentice != null)
			{
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOUR_SPONSOR_S1_HAS_LOGGED_IN);
				msg.addString(getName());
				apprentice.sendPacket(msg);
			}
		}
	}

	private void setPledgeClass()
	{
		int pledgeClass = 0;
		if (getClan() != null)
			pledgeClass = getClan().getClanMember(getObjectId()).calculatePledgeClass(this);
		
		if (isNoble() && pledgeClass < 5)
			pledgeClass = 5;
		
		if (isHero())
			pledgeClass = 8;
		
		setPledgeClass(pledgeClass);
	}
	
	private void loadTutorial()
	{
		final Quest qs = QuestManager.getInstance().getQuest(255);
		
		if (qs != null)
			qs.notifyEvent("UC", null, this);
	}
	
	public void setQuestState(QuestState qs)
	{
		_quests.add(qs);
	}
	
	public void delQuestState(QuestState qs)
	{
		_quests.remove(qs);
	}
	
	public boolean hasQuestCompleted(String quest)
	{
		final QuestState qs = getQuestState(quest);
		return (qs != null) && qs.isCompleted();
	}
	
	public QuestState getQuestState(String quest)
	{
		for (QuestState qs : _quests)
		{
			if (quest.equals(qs.getQuest().getName()))
				return qs;
		}
		return null;
	}
	
	public List<Quest> getAllQuests(boolean completed)
	{
		List<Quest> quests = new ArrayList<>();
		
		for (QuestState qs : _quests)
		{
			if (qs == null || completed && qs.isCreated() || !completed && !qs.isStarted())
				continue;
			
			Quest quest = qs.getQuest();
			if (quest == null || !quest.isRealQuest())
				continue;
			
			quests.add(quest);
		}
		
		return quests;
	}
	
	@Override
	public void addNotifyQuestOfDeath(QuestState qs)
	{
		if (qs == null)
			return;
		
		if (!_notifyQuestOfDeathList.contains(qs))
			_notifyQuestOfDeathList.add(qs);
	}
	
	public void removeNotifyQuestOfDeath(QuestState qs)
	{
		if (qs == null)
			return;
		
		_notifyQuestOfDeathList.remove(qs);
	}
	
	@Override
	public List<QuestState> getNotifyQuestOfDeath()
	{
		return _notifyQuestOfDeathList;
	}
	
	public boolean isInBoat()
	{
		return _vehicle != null;
	}
	
	public L2Vehicle getBoat()
	{
		return _vehicle;
	}
	
	public L2Vehicle getVehicle()
	{
		return _vehicle;
	}
	
	public void setVehicle(L2Vehicle v)
	{
		if (v == null && _vehicle != null)
			_vehicle.removePassenger(this);
		
		_vehicle = v;
	}
	
	public Point3D getInVehiclePosition()
	{
		return _inBoatPosition;
	}
	
	public void setInVehiclePosition(Point3D pt)
	{
		_inBoatPosition = pt;
	}
	
	@Override
	public final void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		super.stopAllEffectsExceptThoseThatLastThroughDeath();
		updateAndBroadcastStatus(2);
	}
	
	public static void gatherCharacterInfo(L2PcInstance activeChar, L2PcInstance player, String filename)
	{
		String ip = "N/A";
		String account = "N/A";
		try
		{
			String clientInfo = player.getClient().toString();
			account = clientInfo.substring(clientInfo.indexOf("Account: ") + 9, clientInfo.indexOf(" - IP: "));
			ip = clientInfo.substring(clientInfo.indexOf(" - IP: ") + 7, clientInfo.lastIndexOf("]"));
		}
		catch (Exception e)
		{
		}
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/" + filename);
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%level%", String.valueOf(player.getLevel()));
		adminReply.replace("%clan%", String.valueOf(ClanTable.getInstance().getClan(player.getClanId())));
		adminReply.replace("%xp%", String.valueOf(player.getExp()));
		adminReply.replace("%sp%", String.valueOf(player.getSp()));
		adminReply.replace("%class%", player.getTemplate().className);
		adminReply.replace("%ordinal%", String.valueOf(player.getClassId().ordinal()));
		adminReply.replace("%classid%", String.valueOf(player.getClassId()));
		adminReply.replace("%x%", String.valueOf(player.getX()));
		adminReply.replace("%y%", String.valueOf(player.getY()));
		adminReply.replace("%z%", String.valueOf(player.getZ()));
		adminReply.replace("%currenthp%", String.valueOf((int) player.getCurrentHp()));
		adminReply.replace("%maxhp%", String.valueOf(player.getMaxHp()));
		adminReply.replace("%karma%", String.valueOf(player.getKarma()));
		adminReply.replace("%currentmp%", String.valueOf((int) player.getCurrentMp()));
		adminReply.replace("%maxmp%", String.valueOf(player.getMaxMp()));
		adminReply.replace("%pvpflag%", String.valueOf(player.getPvpFlag()));
		adminReply.replace("%currentcp%", String.valueOf((int) player.getCurrentCp()));
		adminReply.replace("%maxcp%", String.valueOf(player.getMaxCp()));
		adminReply.replace("%pvpkills%", String.valueOf(player.getPvpKills()));
		adminReply.replace("%pkkills%", String.valueOf(player.getPkKills()));
		adminReply.replace("%currentload%", String.valueOf(player.getCurrentLoad()));
		adminReply.replace("%maxload%", String.valueOf(player.getMaxLoad()));
		adminReply.replace("%percent%", String.valueOf(Util.roundTo(((float) player.getCurrentLoad() / (float) player.getMaxLoad()) * 100, 2)));
		adminReply.replace("%patk%", String.valueOf(player.getPAtk(null)));
		adminReply.replace("%matk%", String.valueOf(player.getMAtk(null, null)));
		adminReply.replace("%pdef%", String.valueOf(player.getPDef(null)));
		adminReply.replace("%mdef%", String.valueOf(player.getMDef(null, null)));
		adminReply.replace("%accuracy%", String.valueOf(player.getAccuracy()));
		adminReply.replace("%evasion%", String.valueOf(player.getEvasionRate(null)));
		adminReply.replace("%critical%", String.valueOf(player.getCriticalHit(null, null)));
		adminReply.replace("%runspeed%", String.valueOf(player.getRunSpeed()));
		adminReply.replace("%patkspd%", String.valueOf(player.getPAtkSpd()));
		adminReply.replace("%matkspd%", String.valueOf(player.getMAtkSpd()));
		adminReply.replace("%access%", String.valueOf(player.getAccessLevel().getLevel()));
		adminReply.replace("%account%", account);
		adminReply.replace("%ip%", ip);
		activeChar.sendPacket(adminReply);
	}
	
	public static void showCharacterInfo(L2PcInstance activeChar, L2PcInstance player)
	{
		if (player == null)
		{
			L2Object target = activeChar.getTarget();
			if (target instanceof L2PcInstance)
				player = (L2PcInstance) target;
			else
				return;
		}
		else
			activeChar.setTarget(player);
		gatherCharacterInfo(activeChar, player, "charinfo.htm");
	}
	
	public boolean checkIfOkToCastSealOfRule(Castle castle, boolean isCheckOnly, L2Skill skill)
	{
		SystemMessage sm;
		if (castle == null || castle.getCastleId() <= 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
		}
		else if (!castle.getSiege().getIsInProgress())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
		}
		else if (!Util.checkIfInRange(200, this, getTarget(), true))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
		}
		else if (castle.getSiege().getAttackerClan(getClan()) == null)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
		}
		else
		{
			if (!isCheckOnly)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.OPPONENT_STARTED_ENGRAVING);
				sendPacket(sm);
			}
			return true;
		}
		sendPacket(sm);
		return false;
	}
	
	private boolean _learningSkill = false;
	
	public boolean isLearningSkill()
	{
		return _learningSkill;
	}
	
	private ScheduledFuture<?> _dismountTask;
	
	protected class Dismount implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				dismount();
			}
			catch (Exception e)
			{
				_log.warning(L2PcInstance.class.getSimpleName() + ": Exception on dismount(): ");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
		}
	}
	
	public void enteredNoLanding(int delay)
	{
		_dismountTask = ThreadPoolManager.getInstance().scheduleGeneral(new Dismount(), delay * 1000);
	}
	
	public void exitedNoLanding()
	{
		if (_dismountTask != null)
		{
			_dismountTask.cancel(true);
			_dismountTask = null;
		}
	}
	
	boolean _isInSiege = false;
	
	public void setIsInSiege(boolean b)
	{
		_isInSiege = b;
	}
	
	public boolean isInSiege()
	{
		return _isInSiege;
	}
	
	public boolean isSeated()
	{
		return _mountObjectID > 0;
	}
	
	private int _partyroom = 0;
	
	public boolean isPartyWaiting()
	{
		return PartyMatchWaitingList.getInstance().getPlayers().contains(this);
	}
	
	public void setPartyRoom(int id)
	{
		_partyroom = id;
	}
	
	public int getPartyRoom()
	{
		return _partyroom;
	}
	
	public boolean isInPartyMatchRoom()
	{
		return _partyroom > 0;
	}
	
	public void cancellEnchant()
	{
		setActiveEnchantItem(null);
		sendPacket(EnchantResult.CANCELLED);
	}
	
	public boolean canEnchant()
	{
		
		Collection<L2WarehouseInstance> knowns = L2World.getInstance().getVisibleObjects(this, L2WarehouseInstance.class, 400);
		
		for (L2WarehouseInstance wh : knowns)
		{
			if (wh != null)
			{
				sendMessage("You cannot enchant near warehouse.");
				cancellEnchant();
				return false;
			}
		}
		
		if (isProcessingTransaction())
		{
			sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
			cancellEnchant();
			return false;
		}
		
		if (isOnline() == 0)
		{
			setActiveEnchantItem(null);
			return false;
		}
		
		if (getPrivateStoreType() != StoreType.NONE)
		{
			sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			cancellEnchant();
			return false;
		}
		
		if (isInStoreMode())
		{
			sendPacket(SystemMessageId.ITEMS_UNAVAILABLE_FOR_STORE_MANUFACTURE);
			cancellEnchant();
			return false;
		}
		
		if (getActiveTradeList() != null)
		{
			sendMessage("You can't enchant items while trading.");
			cancellEnchant();
			return false;
		}
		return true;
	}
	
	public void ReqMagicSkillUse(int skillId, boolean ctrlPressed, boolean shiftPressed)
	{
		
		final L2Skill skill = getSkill(skillId);
		
		if (skill == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (isDead())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Get the level of the used skill
		final int level = getSkillLevel(skillId);
		if (level <= 0)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (isOutOfControl())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// players mounted on pets cannot use any toggle skills
		if (skill.isToggle() && isMounted())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		// If Alternate rule Karma punishment is set to true, forbid skill Return to player with Karma
		if (skill.getSkillType() == L2SkillType.RECALL && !Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && getKarma() > 0)
			return;
			
		if (isAttackingNow())
		{
			if (skill.isToggle())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
				
			getAI().setNextAction(new NextAction(CtrlEvent.EVT_READY_TO_ACT, CtrlIntention.AI_INTENTION_CAST, new Runnable()
			{
				@Override
				public void run()
				{
					useMagic(skill, ctrlPressed, shiftPressed);
				}
			}));
		}
		else
			useMagic(skill, ctrlPressed, shiftPressed);
			
		if (Config.DEBUG)
			_log.info("RequestMagicSkillUse:Char:" + getName() + " skill:" + skill.getName() + ",ControlPressed:" + ctrlPressed + ",shiftPressed:" + shiftPressed);	
	}
	
	boolean clanWarKill = false;
	boolean playerKill = false;
	
	public void SitStand(final L2Object target, boolean sitting)
	{
		
		final boolean isTh = target != null && target instanceof L2StaticObjectInstance && ((L2StaticObjectInstance) target).getType() == 1;
		final boolean ThroneIsBusy = isTh && target !=null && ((L2StaticObjectInstance) target).isBusy();
		
		if (isFakeDeath())
		{
			stopFakeDeath(null);
			return;
		}
		
		if (sitting)
		{
			if (getMountObjectID() != 0)
			{
				final L2Object obj = L2World.getInstance().findObject(getMountObjectID());
				((L2StaticObjectInstance) obj).setBusy(false);
				setMountObjectID(0);
			}
			
			standUp();
		}
		else
		{
			if (isMoving())
			{
				NextAction nextAction = new NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.AI_INTENTION_MOVE_TO, () ->
				{
					if (getMountType() != 0)
						return;
					
					sitDown();
					
					if (!ThroneIsBusy && target != null && isInsideRadius(target, L2Npc.INTERACTION_DISTANCE, false, false))
					{
						((L2StaticObjectInstance) target).setBusy(true);
						setMountObjectID(target.getObjectId());
						broadcastPacket(new ChairSit(this, ((L2StaticObjectInstance) target).getStaticObjectId()));
						
					}
					
				});
				
				getAI().setNextAction(nextAction);
			}
			else
			{
				sitDown();
				
				if (isTh && !ThroneIsBusy && target != null && isInsideRadius(target, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					((L2StaticObjectInstance) target).setBusy(true);
					setMountObjectID(target.getObjectId());
					broadcastPacket(new ChairSit(this, ((L2StaticObjectInstance) target).getStaticObjectId()));
					
				}
			}
		}
	}
	
	public boolean canRequestTrade(L2PcInstance target)
	{
		
		if (target == null || target.equals(this) || !isInSurroundingRegion(target))
		{
			sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return false;
		}
		
		if (!getAccessLevel().allowTransaction())
		{
			sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		
		if (target.isInFunEvent() || isInFunEvent())
		{
			sendMessage("You or your target cannot trade during events.");
			return false;
		}
		
		if (target.isInOlympiadMode() || isInOlympiadMode())
		{
			sendMessage("You or your target cannot trade during Olympiad.");
			return false;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && (getKarma() > 0 || target.getKarma() > 0))
		{
			sendMessage("You cannot trade in a chaotic state.");
			return false;
		}
		
		if (isInStoreMode() || target.isInStoreMode())
		{
			sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return false;
		}
		
		if (isProcessingTransaction())
		{
			sendPacket(SystemMessageId.ALREADY_TRADING);
			return false;
		}
		
		if (target.isProcessingRequest() || target.isProcessingTransaction())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addPcName(target);
			sendPacket(sm);
			return false;
		}
		
		if (target.getTradeRefusal())
		{
			sendMessage("Your target is in trade refusal mode.");
			return false;
		}
		
		if (BlockList.isBlocked(target, this))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST).addPcName(target);
			sendPacket(sm);
			return false;
		}
		
		if (Util.calculateDistance(this, target, true) > L2Npc.INTERACTION_DISTANCE)
		{
			sendPacket(SystemMessageId.TARGET_TOO_FAR);
			return false;
		}
		
		return true;
		
	}
	
	public boolean canOpenPrivateStore()
	{
		if (getActiveTradeList() != null)
			cancelActiveTrade();
		
		if (isInDuel() || AttackStanceTaskManager.getInstance().isInAttackStance(this) || isInOlympiadMode() || isCastingNow())
		{
			sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
			return false;
		}
		
		if (isInsideZone(ZoneId.NO_STORE))
		{
			sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			return false;
		}
		
		if (isAlikeDead() || isMounted() || isProcessingRequest())
			return false;
		
		if (isSitting() && !isInStoreMode())
			return false;
		
		return true;
	}
	
	public void openPrivateBuyStore()
	{
		if (!canOpenPrivateStore())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (getPrivateStoreType() == StoreType.BUY || getPrivateStoreType() == StoreType.BUY_MANAGE)
			setPrivateStoreType(StoreType.NONE);
			
		if (getPrivateStoreType() == StoreType.NONE)
		{
			standUp();
				
			setPrivateStoreType(StoreType.BUY_MANAGE);
			sendPacket(new PrivateStoreManageListBuy(this));
		}		
	}
	
	public void openPrivateSellStore(boolean isPackageSale)
	{
		if (!canOpenPrivateStore())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (getPrivateStoreType() == StoreType.SELL || getPrivateStoreType() == StoreType.SELL_MANAGE || getPrivateStoreType() == StoreType.PACKAGE_SELL)
			setPrivateStoreType(StoreType.NONE);
			
		if (getPrivateStoreType() == StoreType.NONE)
		{
			standUp();
				
			setPrivateStoreType(StoreType.SELL_MANAGE);
			sendPacket(new PrivateStoreManageListSell(this, isPackageSale));
		}		
	}
	
	public void openWorkshop(boolean isDwarven)
	{
		if (!canOpenPrivateStore())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (isInStoreMode())
			setPrivateStoreType(StoreType.NONE);
			
		if (getPrivateStoreType() == StoreType.NONE)
		{
			standUp();
				
			if (getCreateList() == null)
				setCreateList(new L2ManufactureList());
				
			sendPacket(new RecipeShopManageList(this, isDwarven));
		}	
	}
	
	public List<Integer> getFriendList()
	{
		return _friendList;
	}
	
	private void restoreFriendList()
	{
		_friendList.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id = ?");
			statement.setInt(1, getObjectId());
			ResultSet rset = statement.executeQuery();
			
			int friendId;
			while (rset.next())
			{
				friendId = rset.getInt("friend_id");
				if (friendId == getObjectId())
					continue;
				
				_friendList.add(friendId);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error found in " + getName() + "'s friendlist: " + e.getMessage(), e);
		}
	}
	
	private static final int FALLING_VALIDATION_DELAY = 10000;
	private volatile long _fallingTimestamp;
	
	public final boolean isFalling(int z)
	{
		if (isDead() || isFlying() || isInsideZone(ZoneId.WATER))
			return false;
		
		if (System.currentTimeMillis() < _fallingTimestamp)
			return true;
		
		final int deltaZ = getZ() - z;
		if (deltaZ <= 333)
			return false;
		
		final int damage = (int) Formulas.calcFallDam(this, deltaZ);
		if (damage > 0)
		{
			reduceCurrentHp(Math.min(damage, getCurrentHp() - 1), null);
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FALL_DAMAGE_S1).addNumber(damage));
		}
		
		sendPacket(new ValidateLocation(this));
		
		setFalling();
		
		return false;
	}
	
	public final void setFalling()
	{
		_fallingTimestamp = System.currentTimeMillis() + FALLING_VALIDATION_DELAY;
	}
	
	// check for space
	public boolean gotInvalidTitle(String title)
	{
		Pattern pat = Pattern.compile("\\s");
		Matcher match = pat.matcher(title);
		
		return match.find();
	}
			
	@Override
	public void broadcastStatusUpdate()
	{
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
		su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		sendPacket(su);
		
		final boolean needCpUpdate = needCpUpdate(352);
		final boolean needHpUpdate = needHpUpdate(352);
		
		// Check if a party is in progress and party window update is usefull
		if (isInParty() && (needCpUpdate || needHpUpdate || needMpUpdate(352)))
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));
		
		if (isInOlympiadMode() && isOlympiadStart() && (needCpUpdate || needHpUpdate))
		{
			ExOlympiadUserInfo olyInfo = new ExOlympiadUserInfo(this, 1);
			
			for (L2PcInstance player : L2World.getInstance().getVisibleObjects(this, L2PcInstance.class))
			{
				if (player.getOlympiadGameId() == getOlympiadGameId() && player.isOlympiadStart())
				{
					if (Config.DEBUG)
					{
						_log.fine("Send status for Olympia window of " + getObjectId() + "(" + getName() + ") to " + player.getObjectId() + "(" + player.getName() + "). CP: " + getCurrentCp() + " HP: " + getCurrentHp() + " MP: " + getCurrentMp());
					}
					player.sendPacket(olyInfo);
				}
			}
			
			if (isInOlympiadMode() && isOlympiadStart() && (needCpUpdate || needHpUpdate))
			{
				final OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(getOlympiadGameId());
				if (game != null && game.isBattleStarted())
					game.getZone().broadcastStatusUpdate(this);
			}
		}
		
		if (isInDuel() && (needCpUpdate || needHpUpdate))
		{
			ExDuelUpdateUserInfo update = new ExDuelUpdateUserInfo(this);
			DuelManager.getInstance().broadcastToOppositeTeam(this, update);
		}
	}

	public int getRelation(L2PcInstance target)
	{
		int result = 0;
		
		// karma and pvp may not be required
		if (getPvpFlag() != 0)
			result |= RelationChanged.RELATION_PVP_FLAG;
		if (getKarma() > 0)
			result |= RelationChanged.RELATION_HAS_KARMA;
		
		if (isClanLeader())
			result |= RelationChanged.RELATION_LEADER;
		
		if (getSiegeState() != 0)
		{
			result |= RelationChanged.RELATION_INSIEGE;
			if (getSiegeState() != target.getSiegeState())
				result |= RelationChanged.RELATION_ENEMY;
			else
				result |= RelationChanged.RELATION_ALLY;
			if (getSiegeState() == 1)
				result |= RelationChanged.RELATION_ATTACKER;
		}
		
		if (getClan() != null && target != null && target.getClan() != null)
		{
			if (target.getPledgeType() != L2Clan.SUBUNIT_ACADEMY && target.getClan().isAtWarWith(getClan().getClanId()))
			{
				result |= RelationChanged.RELATION_1SIDED_WAR;
				if (getClan().isAtWarWith(target.getClan().getClanId()))
					result |= RelationChanged.RELATION_MUTUAL_WAR;
			}
		}
		return result;
	}
	
	public void broadcastRelationChanged()
	{
		L2World.getInstance().forEachVisibleObject(this, L2PcInstance.class, player ->
		{
			if (isVisible())	
			    sendRelationChanged(player);
		});
	}
	
	public void sendRelationChanged(L2PcInstance target)
	{		
		final int relation = getRelation(target);
		final boolean isAutoAttackable = isAutoAttackable(target);
		
		if(isVisible())
		   target.sendPacket(new RelationChanged(this, relation, isAutoAttackable));
		
		if (getPet() != null && getPet().isVisible())
           target.sendPacket(new RelationChanged(getPet(), relation, isAutoAttackable));	
	}

	public final void broadcastUserInfo()
	{
		L2GameServerPacket packet = getPoly().getPolyType() == PolyType.NPC ? new PolymorphInfo(this, getPoly().getPolyTemplate()) : new CharInfo(this);

		sendPacket(new UserInfo(this));
		
		if(getPet()!=null)
			getPet().broadcastInfo();
		
		L2World.getInstance().forEachVisibleObject(this, L2PcInstance.class, player ->
		{
			player.sendPacket(packet);	
			sendRelationChanged(player);
		});			
	}
	
	public final void broadcastTitleInfo()
	{
		sendPacket(new UserInfo(this));
		Broadcast.toKnownPlayers(this, new TitleUpdate(this));
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (isInBoat())
		{
			getPosition().setWorldPosition(getBoat().getPosition().getWorldPosition());
			activeChar.sendPacket(new GetOnVehicle(getObjectId(), getBoat().getObjectId(), getInVehiclePosition()));
		}
		
		L2GameServerPacket packet = getPoly().getPolyType() == PolyType.NPC ? new PolymorphInfo(this, getPoly().getPolyTemplate()) : new CharInfo(this);
		activeChar.sendPacket(packet);
		
		sendRelationChanged(activeChar);
		activeChar.sendRelationChanged(this);
		
		if (isSeated())
		{
			final L2Object throne = L2World.getInstance().findObject(getMountObjectID());
			if (throne instanceof L2StaticObjectInstance)
				activeChar.sendPacket(new ChairSit(this, ((L2StaticObjectInstance) throne).getStaticObjectId()));
		}
		
		switch (getPrivateStoreType())
		{
			case SELL:
			case PACKAGE_SELL:
				activeChar.sendPacket(new PrivateStoreMsgSell(this));
				break;
			
			case BUY:
				activeChar.sendPacket(new PrivateStoreMsgBuy(this));
				break;
			
			case MANUFACTURE:
				activeChar.sendPacket(new RecipeShopMsg(this));
				break;
		}
	}
	
	
	@Override
	public void sendPacket(L2GameServerPacket packet)
	{
		if (_client != null)
			_client.sendPacket(packet);
	}
	
	public void sendPacket(L2GameServerPacket... packets)
	{	
		if (_client != null)
		{
			for (L2GameServerPacket packet : packets)
				_client.sendPacket(packet);
		}
	}
	
	public void sendPacket(SystemMessageId id)
	{
		sendPacket(SystemMessage.getSystemMessage(id));
	}
	
	@Override
	public final L2Skill getKnownSkill(int skillId)
	{
		return super.getKnownSkill(skillId);
	}
	
	public void mountPlayer(L2Summon pet)
	{
			// mount
			if ((pet != null) && pet.isMountable() && !isMounted() && !isBetrayed())
			{
				if (isDead())
				{
					// A strider cannot be ridden when dead
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
					sendPacket(msg);
					msg = null;
				}
				else if (pet.isDead())
				{
					// A dead strider cannot be ridden.
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
					sendPacket(msg);
					msg = null;
				}
				else if (pet.isInCombat() || pet.isRooted())
				{
					// A strider in battle cannot be ridden
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
					sendPacket(msg);
					msg = null;
				}
				else if (isInCombat())
				{
					// A strider cannot be ridden while in battle
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
					sendPacket(msg);
					msg = null;
				}
				else if (isSitting() || isMoving())
				{
					// A strider can be ridden only when standing
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
					sendPacket(msg);
					msg = null;
				}
				else if (isFishing())
				{
					// You can't mount, dismount, break and drop items while fishing
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
					sendPacket(msg);
					msg = null;
				}
				else if (isCursedWeaponEquiped())
				{
					// You can't mount, dismount, break and drop items while wielding a cursed weapon
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
					sendPacket(msg);
				}
				else if (!pet.isDead() && !isMounted())
				{
					if (!disarmWeapons())
						return;
					
					Ride mount = new Ride(getObjectId(), Ride.ACTION_MOUNT, pet.getTemplate().npcId);
					broadcastPacket(mount);
					setMountType(mount.getMountType());
					setMountObjectID(pet.getControlItemId());
					pet.unSummon(this);
				}
			}
			else if (isRentedPet())
			{
				stopRentPet();
			}
			else if (isMounted())
			{
				if (setMountType(0))
				{
					if (isFlying())
						removeSkill(SkillTable.getInstance().getInfo(4289, 1));
					Ride dismount = new Ride(getObjectId(), Ride.ACTION_DISMOUNT, 0);
					broadcastPacket(dismount);
					setMountObjectID(0);
					broadcastStatusUpdate();
					broadcastUserInfo();
				}
			}	
	}
	
	
	public void dismount()
	{	
		if (getActiveTradeList() != null)
			cancelActiveTrade();
		
		final Ride dismount = new Ride(getObjectId(), Ride.ACTION_DISMOUNT, 0);
		sendPacket(new SetupGauge(3, 0, 0));
		setMountType(0);
		setMountObjectID(0);
		broadcastPacket(dismount);
		broadcastUserInfo();
	}
	
	public void mount(int RideId)
	{	
		if (!disarmWeapons())
			return;
		
		if (getPet() != null)
			getPet().unSummon(this);
		
		setRunning();
		stopAllToggles();
		
		final Ride RideMount = new Ride(getObjectId(), Ride.ACTION_MOUNT, RideId);
		broadcastPacket(RideMount);
		setMountType(RideMount.getMountType());
		setMountObjectID(RideId);
		broadcastUserInfo();
	}
	
	public final void stopAllToggles()
	{
		_effects.stopAllToggles();
	}
	
	public L2Skill getSkill(int skillId)
	{
		return _skills.get(skillId);
	}
	
	public void removeMeFromPartyMatch()
	{
		PartyMatchWaitingList.getInstance().removePlayer(this);
		if (_partyroom != 0)
		{
			PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_partyroom);
			if (room != null)
				room.deleteMember(this);
		}
	}
	
	public void broadcastSocialActionInRadius(int socialId)
	{
		broadcastPacket(new SocialAction(getObjectId(), socialId), 1250);
	}
	
	public final int WriteAugmentation(L2ShortCut sc)
	{
		if (sc == null)
			return 0;
		
		final L2ItemInstance item = getInventory().getItemByObjectId(sc.getId());
		
		return item == null || !item.isAugmented() ? 0 : item.getAugmentation().getAugmentationId();
	}
	
	public void showFishingHelp()
	{
		String htmFile = "data/html/help/fishing/7561-1.htm";
		String htmContent = HtmCache.getInstance().getHtmForce(htmFile);
		NpcHtmlMessage infoHtml = new NpcHtmlMessage(1);
		infoHtml.setHtml(htmContent);
		sendPacket(infoHtml);
	}

	public void SummonRotate(L2Summon summon, double distance)
	{
		//AbsolutePower rotate summon to face the owner, for now only when the click distance is <= 250.
		if (summon.isAutoFollow() && distance != 0 && distance <= 250 && !summon.isDead() && !summon.isAttackingNow() && !summon.isCastingNow())
			summon.broadcastPacket(new MoveToPawn(summon, this, 70));		
	}
	
	public void rechargeShots(boolean physical, boolean magic)
	{
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		
		if (weaponInst == null)
			return;
		
		weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_SOULSHOT);
		Broadcast.toSelfAndKnownPlayersInRadius(this, new MagicSkillUse(this, this, 2154, 1, 0, 0), 600);
		Broadcast.toSelfAndKnownPlayersInRadius(this, new MagicSkillUse(this, this, 2164, 1, 0, 0), 600);
		
	}
}