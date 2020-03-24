package com.l2jhellas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import Extensions.RankSystem.Rank;
import Extensions.RankSystem.RankTable;

import com.l2jhellas.gameserver.geodata.PathFindBuffers;
import com.l2jhellas.util.StringUtil;

public final class Config
{
	protected static final Logger _log = Logger.getLogger(Config.class.getName());
	
	// Admin Folder
	private static final String ADMIN_CONFIG_FILE = "./config/Main/Admin.ini";
	// Main Folder
	private static final String ALT_SETTINGS_FILE = "./config/Main/Altsettings.ini";
	private static final String CLAN_CONFIG_FILE = "./config/Main/Clan.ini";
	public static final String SIEGE_CONFIGURATION_FILE = "./config/Main/Siege.ini";
	private static final String GEO_FILE = "./config/Main/Geodata.ini";
	private static final String OPTIONS_FILE = "./config/Main/Options.ini";
	private static final String SEVENSIGNS_FILE = "./config/Main/Sevensigns.ini";
	
	private static final String RATES_CONFIG_FILE = "./config/Main/Rates.ini";
	private static final String OTHER_CONFIG_FILE = "./config/Main/Other.ini";
	private static final String PVP_CONFIG_FILE = "./config/Main/PvP.ini";
	private static final String GRANDBOSS_CONFIG_FILE = "./config/Main/GrandBoss.ini";
	// Events Folder
	private static final String EVENT_ZODIAC_CONFIG_FILE = "./config/Events/Zodiac.ini";
	private static final String EVENT_TVT_CONFIG_FILE = "./config/Events/TVT.ini";
	private static final String EVENT_DM_CONFIG_FILE = "./config/Events/DM.ini";
	private static final String EVENT_CTF_CONFIG_FILE = "./config/Events/CTF.ini";
	private static final String EVENT_WEDDING_CONFIG_FILE = "./config/Events/Wedding.ini";
	// Mods Folder
	private static final String MOD_L2JHellas_CONFIG_FILE = "./config/Mods/L2JHellas.ini";
	private static final String MOD_RANK_CONFIG_FILE = "./config/Mods/Rank PvP System.ini";
	private static final String MOD_AUTOMATION_CONFIG_FILE = "./config/Mods/Automatation.ini";
	private static final String MOD_SMART_CB_CONFIG_FILE = "./config/Mods/Smart Community Board.ini";
	private static final String MOD_CUSTOM_NPC_CONFIG_FILE = "./config/Mods/Custom Npcs.ini";
	// Olympiad Folder
	private static final String OLYMPIAD_FILE = "./config/Olympiad/OlySettings.ini";
	// Others
	private static final String CHAT_FILTER_FILE = "./config/Others/ChatFilter.txt";
	
	private static final String SERVER_VERSION_FILE = "./config/Version/L2J Hellas Version.ini";

	// Network Folder
	private static final String CONFIGURATION_FILE = "./config/Network/GameServer.ini";
	private static final String HEXID_FILE = "./config/Network/HexId/hexid.txt";
	private static final String GS_IP = "./config/Network/IPConfig/IPGameServer.ini";
	
	private static final String LOGIN_CONFIGURATION_FILE = "./config/Network/LoginServer.ini";
	private static final String LS_IP = "./config/Network/IPConfig/IPLoginServer.ini";
	
	public static boolean GEODATA;
	public static int GEO_X_FIRST, GEO_Y_FIRST, GEO_X_LAST, GEO_Y_LAST;
	public static boolean CONTROL_HEIGHT_DAMAGE;
	public static boolean PATH_CLEAN;
	public static boolean ALLOW_DOORS;
	public static boolean SIMPLE_PATHFIND_FOR_MOBS;
	public static boolean ALLOW_FALL_FROM_WALLS;
	public static int PATHFIND_BOOST;
	public static boolean PATHFIND_DIAGONAL;
	public static int PATHFIND_MAX_Z_DIFF;
	public static int MAX_Z_DIFF;
	public static int MIN_LAYER_HEIGHT;
	public static double WEIGHT0;
	public static double WEIGHT1;
	public static double WEIGHT2;
	public static boolean COMPACT_GEO;
	public static int COORD_SYNCHRONIZE;
	
	// ================================
	// Champions config
	// ================================
	public static boolean CHAMPION_ENABLE = false;//Allow champions?	
	public static boolean CHAMPION_PASSIVE = false;	//champion aggro 
	public static boolean CHAMPION_BOSS = false;//Allow Boss or RaidBoss to be a Champion, True/False
	public static boolean CHAMPION_MINIONS = false;	//Allow Minions to be a Champion when Leader is a Champion, True/False

	public static String CHAMPION_TITLE = "Mob Plus"; // champion monster title
	
	public static int CHAMPION_FREQUENCY = 1; //Chance (100000 for 100% chance) that spawn will be Champion
	public static int CHAMPION_HP = 10;	
	public static int CHAMPION_ADENA = 10;
	public static int CHAMPION_REWARDS = 10;
	public static int CHAMPION_EXP_SP = 10;
	public static int CHAMPION_MIN_LEVEL = 20;
	public static int CHAMPION_MAX_LEVEL = 70;
	
	public static int CHAMPION_SPCL_CHANCE = 0; //Chance in % (default: 0)
	public static int CHAMPION_SPCL_ITEM = 6393; // champion special item id
	public static int CHAMPION_SPCL_QTY =1; // champion special item count
	public static int CHAMPION_SPCL_LVL_DIFF = 0; //If player level diff with mob level is more this value - don't drop an special reward item.
	
	public static float CHAMPION_HP_REGEN = 2;
	public static float CHAMPION_ATK =1;
	public static float CHAMPION_SPD_ATK =1;
	// ================================
	// Champions config end
	// ================================
	
	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_RAID;
	public static boolean AUTO_LOOT_GRAND;
	public static boolean AUTO_LOOT_HERBS;
	public static boolean AUTO_LEARN_SKILLS;
	public static boolean CHECK_SKILLS_ON_ENTER;
	public static String ALLOWED_SKILLS;
	public static List<Integer> ALLOWED_SKILLS_LIST = new ArrayList<>();
	public static boolean LIFE_CRYSTAL_NEEDED;
	public static boolean SP_BOOK_NEEDED;
	public static boolean ES_SP_BOOK_NEEDED;
	public static boolean ALT_GAME_SKILL_LEARN;
	public static boolean NPCS_ATTACKABLE;
	public static int ALT_PARTY_RANGE;
	public static int ALT_PARTY_RANGE2;
	public static double ALT_WEIGHT_LIMIT;
	public static boolean ALT_GAME_MAGICFAILURES;
	public static boolean ALT_GAME_CANCEL_BOW;
	public static boolean ALT_GAME_CANCEL_CAST;
	public static boolean ALT_GAME_SHIELD_BLOCKS;
	public static int ALT_PERFECT_SHLD_BLOCK;
	public static boolean ALT_GAME_MOB_ATTACK_AI;
	public static boolean ALT_MOB_AGRO_IN_PEACEZONE;
	public static boolean ALT_GAME_FREIGHTS;
	public static int ALT_GAME_FREIGHT_PRICE;
	public static float ALT_GAME_EXPONENT_XP;
	public static float ALT_GAME_EXPONENT_SP;
	public static boolean ALLOW_SATURDAY_RATE_XP_SP;
	public static float SATURDAY_RATE_XP;
	public static float SATURDAY_RATE_SP;
	public static boolean ALT_GAME_TIREDNESS;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
	public static boolean ALT_GAME_FREE_TELEPORT;
	public static boolean IS_CRAFTING_ENABLED;
	public static int DWARF_RECIPE_LIMIT;
	public static int COMMON_RECIPE_LIMIT;
	public static boolean ALT_GAME_CREATION;
	public static double ALT_GAME_CREATION_SPEED;
	public static double ALT_GAME_CREATION_XP_RATE;
	public static double ALT_GAME_CREATION_SP_RATE;
	public static boolean ALT_BLACKSMITH_USE_RECIPES;
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static byte MAX_SUBCLASS;
	public static byte MAX_SUBCLASS_LEVEL;
	public static byte BASE_SUBCLASS_LEVEL;
	public static int MAX_PATK_SPEED;
	public static int MAX_MATK_SPEED;
	public static boolean DISABLE_GRADE_PENALTY;
	public static boolean RESTORE_EFFECTS_ON_SUBCLASS_CHANGE;
	public static boolean DISABLE_WEIGHT_PENALTY;
	
	public static boolean ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE;
	public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;
	public static boolean REMOVE_CASTLE_CIRCLETS;
	public static int ALT_MANOR_REFRESH_TIME;
	public static int ALT_MANOR_REFRESH_MIN;
	public static int ALT_MANOR_APPROVE_TIME;
	public static int ALT_MANOR_APPROVE_MIN;
	public static int ALT_MANOR_MAINTENANCE_PERIOD;
	public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;
	public static int ALT_MANOR_SAVE_PERIOD_RATE;
	public static int ALT_LOTTERY_PRIZE;
	public static int ALT_LOTTERY_TICKET_PRICE;
	public static float ALT_LOTTERY_5_NUMBER_RATE;
	public static float ALT_LOTTERY_4_NUMBER_RATE;
	public static float ALT_LOTTERY_3_NUMBER_RATE;
	public static int ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
	public static int FS_TIME_ATTACK;
	public static int FS_TIME_COOLDOWN;
	public static int FS_TIME_ENTRY;
	public static int FS_TIME_WARMUP;
	public static int FS_PARTY_MEMBER_COUNT;
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_SPAWN_DELAY;
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_AUTO_JUMPS_TIME_MIN;
	public static int RIFT_AUTO_JUMPS_TIME_MAX;
	public static float RIFT_BOSS_ROOM_TIME_MUTIPLY;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	public static int CRUMA_TOWER_LEVEL_RESTRICT;
	public static boolean ALT_RESPAWN_POINT;
	public static int ALT_RESPAWN_POINT_X;
	public static int ALT_RESPAWN_POINT_Y;
	public static int ALT_RESPAWN_POINT_Z;
	public static float DANCE_TIME_MULTIPLIER;
	public static float BUFF_TIME_MULTIPLIER;
	public static float SPIRIT_TIME_MULTIPLIER;
	public static byte BUFFS_MAX_AMOUNT;
	public static byte DEBUFFS_MAX_AMOUNT;
	public static boolean ALT_GAME_VIEWNPC;
	public static boolean PLAYER_ALT_GAME_VIEWNPC;
	
	public static int ALT_CLAN_JOIN_DAYS;
	public static int ALT_CLAN_CREATE_DAYS;
	public static int ALT_CLAN_DISSOLVE_DAYS;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;
	public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;
	public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;
	public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
	public static int ALT_CLAN_MEMBERS_FOR_WAR;
	public static int ALT_REPUTATION_SCORE_PER_KILL;
	public static boolean CASTLE_SHIELD;
	public static boolean CLANHALL_SHIELD;
	public static boolean APELLA_ARMORS;
	public static boolean OATH_ARMORS;
	public static boolean CASTLE_CROWN;
	public static boolean CASTLE_CIRCLETS;
	public static long CH_TELE_FEE_RATIO;
	public static int CH_TELE1_FEE;
	public static int CH_TELE2_FEE;
	public static int CH_TELE3_FEE;
	public static int CH_TELE4_FEE;
	public static long CH_SUPPORT_FEE_RATIO;
	public static int CH_SUPPORT1_FEE;
	public static int CH_SUPPORT2_FEE;
	public static int CH_SUPPORT3_FEE;
	public static int CH_SUPPORT4_FEE;
	public static int CH_SUPPORT5_FEE;
	public static int CH_SUPPORT6_FEE;
	public static int CH_SUPPORT7_FEE;
	public static int CH_SUPPORT8_FEE;
	public static int CH_SUPPORT9_FEE;
	public static long CH_MPREG_FEE_RATIO;
	public static int CH_MPREG1_FEE;
	public static int CH_MPREG2_FEE;
	public static int CH_MPREG3_FEE;
	public static int CH_MPREG4_FEE;
	public static int CH_MPREG5_FEE;
	public static long CH_HPREG_FEE_RATIO;
	public static int CH_HPREG1_FEE;
	public static int CH_HPREG2_FEE;
	public static int CH_HPREG3_FEE;
	public static int CH_HPREG4_FEE;
	public static int CH_HPREG5_FEE;
	public static int CH_HPREG6_FEE;
	public static int CH_HPREG7_FEE;
	public static int CH_HPREG8_FEE;
	public static int CH_HPREG9_FEE;
	public static int CH_HPREG10_FEE;
	public static int CH_HPREG11_FEE;
	public static int CH_HPREG12_FEE;
	public static int CH_HPREG13_FEE;
	public static long CH_EXPREG_FEE_RATIO;
	public static int CH_EXPREG1_FEE;
	public static int CH_EXPREG2_FEE;
	public static int CH_EXPREG3_FEE;
	public static int CH_EXPREG4_FEE;
	public static int CH_EXPREG5_FEE;
	public static int CH_EXPREG6_FEE;
	public static int CH_EXPREG7_FEE;
	public static long CH_ITEM_FEE_RATIO;
	public static int CH_ITEM1_FEE;
	public static int CH_ITEM2_FEE;
	public static int CH_ITEM3_FEE;
	public static long CH_CURTAIN_FEE_RATIO;
	public static int CH_CURTAIN1_FEE;
	public static int CH_CURTAIN2_FEE;
	public static long CH_FRONT_FEE_RATIO;
	public static int CH_FRONT1_FEE;
	public static int CH_FRONT2_FEE;
	
	public static String SERVER_VERSION;
	public static String SERVER_BUILD_DATE;
	
	public static boolean GM_HERO_AURA;
	public static boolean GM_STARTUP_INVULNERABLE;
	public static boolean GM_STARTUP_INVISIBLE;
	public static boolean GM_STARTUP_SILENCE;
	public static boolean GM_STARTUP_AUTO_LIST;
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
					
	public static boolean REMOVE_BUFFS_ON_DIE;
	public static boolean ALLOW_POTS_IN_PVP;
	public static boolean ALLOW_SOE_IN_PVP;
	public static String KARMA_NONDROPPABLE_PET_ITEMS;
	public static List<Integer> KARMA_LIST_NONDROPPABLE_PET_ITEMS = new ArrayList<>();
	public static String KARMA_NONDROPPABLE_ITEMS;
	public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<>();
	public static int KARMA_PK_LIMIT;
	public static int PVP_NORMAL_TIME;
	public static int PVP_PVP_TIME;
	
	public static boolean ALLOW_FAKE_PLAYERS;
	public static boolean BANKING_SYSTEM_ENABLED;
	public static int BANKING_SYSTEM_GOLDBARS;
	public static int BANKING_SYSTEM_ADENA;
	public static int BANKING_SYSTEM_ITEM;
	public static boolean BANKINGALTERNATE_SYSTEM_ENABLED;
	public static int BANKINGALTERNATE_SYSTEM_GOLDBARS;
	public static int BANKINGALTERNATE_SYSTEM_ADENA;
	public static boolean CHAR_TITLE;
	public static String ADD_CHAR_TITLE;
	public static boolean ALLOW_CREATE_LVL;
	public static int CUSTOM_START_LVL;
	public static boolean ALLOW_HERO_SUBSKILL;
	public static int HERO_COUNT;
	public static int GM_OVER_ENCHANT;
	public static int MAX_RUN_SPEED;
	public static int MAX_PCRIT_RATE;
	public static int MAX_MCRIT_RATE;
	public static int ALT_MAX_EVASION;
	public static boolean ONLINE_VOICE_ALLOW;
	public static boolean ALT_GAME_FLAGGED_PLAYER_CAN_USE_GK;
	public static boolean ALLOW_PLAYERS_REFUSAL;
	public static boolean ALLOW_INFO_COMMAND;
	public static boolean ALLOW_TRADEOFF_COMMAND;
	public static boolean ALT_SUBCLASS_SKILLS;
	public static boolean DONATOR_NAME_COLOR_ENABLED;
	public static int DONATOR_NAME_COLOR;
	public static boolean DONATOR_TITLE_COLOR_ENABLED;
	public static int DONATOR_TITLE_COLOR;
	public static boolean ALLOW_VIPTELEPORT_COMMAND;
	public static int VIP_X;
	public static int VIP_Y;
	public static int VIP_Z;
	public static int ADD_EXP;
	public static int ADD_SP;
	public static String ABORT_RR;
	public static boolean SPAWN_CHAR;
	public static int SPAWN_X;
	public static int SPAWN_Y;
	public static int SPAWN_Z;
	public static boolean USE_SAY_FILTER;
	public static String CHAT_FILTER_CHARS;
	public static List<String> FILTER_LIST = new ArrayList<>();
	public static boolean ALLOW_DAGGERS_WEAR_HEAVY;
	public static boolean ALLOW_ARCHERS_WEAR_HEAVY;
	public static boolean CLASS_AUTO_EQUIP_AW;
	
	public static boolean ALLOW_USE_HERO_ITEM_ON_SUBCLASS;
	public static int SOUL_CRYSTAL_BREAK_CHANCE;
	public static int SOUL_CRYSTAL_LEVEL_CHANCE;
	public static boolean MAX_LVL_AFTER_SUB;
	
	public static Map<Integer, Integer> CLAN_SKILLS;
	public static byte CLAN_LEVEL;
	public static int REPUTATION_QUANTITY;
	
	public static int TOP_PLAYER_ROW_HEIGHT;
	public static int TOP_PLAYER_RESULTS;
	public static int RAID_LIST_ROW_HEIGHT;
	public static int RAID_LIST_RESULTS;
	public static boolean RAID_LIST_SORT_ASC;
	public static boolean ALLOW_REAL_ONLINE_STATS;
	public static String ALLOW_CLASS_MASTERSCB;
	public static String CLASS_MASTERS_PRICECB;
	public static int CLASS_MASTERS_PRICE_ITEMCB;
	public static int[] CLASS_MASTERS_PRICE_LISTCB = new int[4];
	public static List<Integer> ALLOW_CLASS_MASTERS_LISTCB = new ArrayList<>();
	
	public static boolean ALLOW_SERVER_RESTART_COMMAND;
	public static int VOTES_NEEDED_FOR_RESTART;
	public static boolean RESTART_BY_TIME_OF_DAY;
	public static int RESTART_SECONDS;
	public static String[] RESTART_INTERVAL_BY_TIME_OF_DAY;
	public static boolean ENABLE_GUI;
	public static boolean LOGIN_SERVER_SCHEDULE_RESTART;
	public static long LOGIN_SERVER_SCHEDULE_RESTART_TIME;
	public static boolean ALLOW_PRIVATE_ANTI_BOT;
	public static boolean ALLOW_SEQURITY_QUE;
	public static int ENCHANT_BOT_CHANCE;
	public static int SECURITY_QUE_TIME;
	public static boolean ALLOW_HOPZONE_VOTE_REWARD;
	public static String HOPZONE_SERVER_LINK;
	public static int HOPZONE_VOTES_DIFFERENCE;
	public static int HOPZONE_REWARD_CHECK_TIME;
	public static int[][] HOPZONE_REWARD;
	public static int HOPZONE_BOXES_ALLOWED;
	public static boolean ALLOW_TOPZONE_VOTE_REWARD;
	public static String TOPZONE_SERVER_LINK;
	public static int TOPZONE_VOTES_DIFFERENCE;
	public static int TOPZONE_REWARD_CHECK_TIME;
	public static int[][] TOPZONE_REWARD;
	public static int TOPZONE_BOXES_ALLOWED;
	
	public static boolean RANK_PVP_SYSTEM_ENABLED;
	public static int LEGAL_KILL_MIN_LVL;
	public static boolean PVP_COUNTER_FOR_ALTT_ENABLED;
	public static boolean PVP_COUNTER_FOR_ALTT_LEGAL_KILLS_ONLY;
	public static boolean LEGAL_KILL_FOR_PK_KILLER_ENABLED;
	public static boolean LEGAL_KILL_FOR_INNOCENT_KILL_ENABLED;
	public static int PROTECTION_TIME_RESET;
	public static int LEGAL_KILL_PROTECTION;
	public static int DAILY_LEGAL_KILL_PROTECTION;
	
	public static boolean GM_IGNORE_ENABLED;
	
	// PvP + RPC Reward:
	public static boolean RPC_REWARD_ENABLED;
	public static long RPC_REWARD_AMOUNT;
	public static int RPC_REWARD_MIN_LVL;
	
	public static boolean PVP_REWARD_ENABLED;
	public static int PVP_REWARD_ID;
	public static long PVP_REWARD_AMOUNT;
	public static int PVP_REWARD_MIN_LVL;
	
	public static boolean REWARD_FOR_PK_KILLER_ENABLED;
	public static boolean REWARD_FOR_INNOCENT_KILL_ENABLED;
	
	public static boolean RANK_PVP_REWARD_ENABLED;
	public static int RANK_PVP_REWARD_MIN_LVL;
	public static boolean REWARD_LEGAL_KILL_ENABLED;
	public static boolean RANK_LEVEL_REWARD_ENABLED;
	public static boolean RANK_SKILL_REWARD_ENABLED;
	
	// Ranks:
	public static boolean RANKS_ENABLED;
	public static int RANK_POINTS_MIN_LVL;
	public static boolean RANK_POINTS_CUT_ENABLED;
	public static boolean RANK_RPC_ENABLED;
	public static boolean RANK_RPC_CUT_ENABLED;
	
	public static boolean RANK_POINTS_DOWN_COUNT_ENABLED;
	public static List<Integer> RANK_POINTS_DOWN_AMOUNTS = new ArrayList<>();
	
	public static boolean RANK_SHOUT_INFO_ON_KILL_ENABLED;
	public static boolean RANK_SHOUT_BONUS_INFO_ON_KILL_ENABLED;
	
	public static boolean RPC_EXCHANGE_ENABLED;
	public static boolean RPC_EXCHANGE_CONFIRM_BOX_ENABLED;
	
	public static boolean PVP_EXP_DECREASE_ENABLED;
	public static int PVP_EXP_DECREASE_METHOD;
	public static int PVP_EXP_DECREASE_CONSTANT;
	public static double PVP_EXP_DECREASE_FRACTION;
	public static boolean PVP_EXP_DECREASE_ON_LEGAL_KILL_ENABLED;
	
	// War Kills:
	public static boolean WAR_KILLS_ENABLED;
	public static double WAR_RANK_POINTS_RATIO;
	
	// Combo Kill:
	public static boolean COMBO_KILL_ENABLED;
	public static boolean COMBO_KILL_PROTECTION_WITH_LEGAL_KILL_ENABLED;
	public static boolean COMBO_KILL_PROTECTION_NO_REPEAT_ENABLED;
	
	public static Map<Integer, String> COMBO_KILL_LOCAL_AREA_MESSAGES = new HashMap<>();
	public static Map<Integer, String> COMBO_KILL_GLOBAL_AREA_MESSAGES = new HashMap<>();
	
	public static boolean COMBO_KILL_ALT_MESSAGES_ENABLED;
	public static String COMBO_KILL_ALT_MESSAGE;
	public static int COMBO_KILL_ALT_GLOBAL_MESSAGE_LVL;
	
	public static boolean COMBO_KILL_DEFEAT_MESSAGE_ENABLED;
	public static int COMBO_KILL_DEFEAT_MESSAGE_MIN_LVL;
	public static String COMBO_KILL_DEFEAT_MESSAGE;
	
	public static int COMBO_KILL_RESETER;
	public static boolean COMBO_KILL_RANK_POINTS_RATIO_ENABLED;
	public static Map<Integer, Double> COMBO_KILL_RANK_POINTS_RATIO = new HashMap<>();
	
	// Title & Nick Color:
	public static boolean NICK_COLOR_ENABLED;
	public static boolean TITLE_COLOR_ENABLED;
	
	// Zones:
	public static List<Integer> ALLOWED_ZONES_IDS = new ArrayList<>();
	public static List<Integer> RESTRICTED_ZONES_IDS = new ArrayList<>();
	public static List<Integer> DEATH_MANAGER_RESTRICTED_ZONES_IDS = new ArrayList<>();
	public static Map<Integer, Double> RANK_POINTS_BONUS_ZONES_IDS = new HashMap<>();
	
	// pvpinfo command, pvp status window, death manager:
	public static boolean PVP_INFO_COMMAND_ENABLED;
	public static boolean PVP_INFO_USER_COMMAND_ENABLED;
	public static int PVP_INFO_USER_COMMAND_ID;
	
	public static boolean PVP_INFO_COMMAND_ON_DEATH_ENABLED;
	public static boolean DEATH_MANAGER_DETAILS_ENABLED;
	public static boolean DEATH_MANAGER_SHOW_ITEMS_ENABLED;
	
	public static boolean TOTAL_KILLS_IN_SHOUT_ENABLED;
	public static boolean TOTAL_KILLS_IN_PVPINFO_ENABLED;
	public static boolean TOTAL_KILLS_ON_ME_IN_PVPINFO_ENABLED;
	public static boolean SHOW_PLAYER_LEVEL_IN_PVPINFO_ENABLED;
	
	// Anti-Farm:
	public static boolean ANTI_FARM_CLAN_ALLY_ENABLED;
	public static boolean ANTI_FARM_PARTY_ENABLED;
	public static boolean ANTI_FARM_IP_ENABLED;
	
	// Top List (Community Board):
	public static boolean TOP_LIST_ENABLED;
	public static long TOP_LIST_IGNORE_TIME_LIMIT;
	
	// Database:
	public static long PVP_TABLE_UPDATE_INTERVAL;
	public static List<Long> TOP_TABLE_UPDATE_TIMES = new ArrayList<>();
	public static boolean RPC_TABLE_FORCE_UPDATE_ENABLED;
	
	public static boolean DATABASE_CLEANER_ENABLED;
	public static long DATABASE_CLEANER_REPEAT_TIME;
	
	// Image:
	public static boolean SERVER_SIDE_IMAGES_ENABLED;
	public static int IMAGE_PREFIX;
	
	// Button style:
	public static String BUTTON_UP;
	public static String BUTTON_DOWN;
	public static String BUTTON_W;
	public static String BUTTON_H;
	public static String BUTTON_BIG_W;
	public static String BUTTON_BIG_H;
	
	public static boolean ENABLE_CACHE_INFO;
	public static boolean SHOW_NPC_CREST;
	public static boolean ALLOW_CLASS_MASTER;
	public static boolean ALLOW_REMOTE_CLASS_MASTER;
	public static boolean PROTECTOR_PLAYER_PK;
	public static boolean PROTECTOR_PLAYER_PVP;
	public static int PROTECTOR_RADIUS_ACTION;
	public static int PROTECTOR_SKILLID;
	public static int PROTECTOR_SKILLLEVEL;
	public static int PROTECTOR_SKILLTIME;
	public static boolean SEND_MESSAGE;
	public static String PROTECTOR_MESSAGE;
	public static boolean NPCBUFFER_FEATURE_ENABLED;
	public static int NPCBUFFER_STATIC_BUFF_COST;
	public static boolean NPC_NOBLES_ENABLE;
	public static int NPC_NOBLESS_ID;
	public static int NPC_NOBLESS_QUANTITY;
	public static int BOSS_RESPAWN_NPC_ID;
	public static int[] BOSS_RESPAWN_INFO;
	public static boolean RAID_INFO_SHOW_TIME;
	
	public static int ALT_OLY_START_TIME;
	public static int ALT_OLY_MIN;
	public static long ALT_OLY_CPERIOD;
	public static long ALT_OLY_BATTLE;
	public static long ALT_OLY_WPERIOD;
	public static long ALT_OLY_VPERIOD;
	public static int ALT_OLY_WAIT_TIME;
	public static int ALT_OLY_START_POINTS;
	public static int ALT_OLY_WEEKLY_POINTS;
	public static int ALT_OLY_MIN_MATCHES;
	public static int ALT_OLY_CLASSED;
	public static int ALT_OLY_NONCLASSED;
	public static int[][] ALT_OLY_CLASSED_REWARD;
	public static int[][] ALT_OLY_NONCLASSED_REWARD;
	public static int ALT_OLY_COMP_RITEM;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_HERO_POINTS;
	public static int ALT_OLY_RANK1_POINTS;
	public static int ALT_OLY_RANK2_POINTS;
	public static int ALT_OLY_RANK3_POINTS;
	public static int ALT_OLY_RANK4_POINTS;
	public static int ALT_OLY_RANK5_POINTS;
	public static int ALT_OLY_MAX_POINTS;
	public static int ALT_OLY_DIVIDER_CLASSED;
	public static int ALT_OLY_DIVIDER_NON_CLASSED;
	public static boolean ALT_OLY_ANNOUNCE_GAMES;
	public static int OLY_ENCHANT_LIMIT;
	public static boolean OLY_SAME_IP;
	public static List<Integer> OLY_RESTRICTED_ITEMS_LIST = new ArrayList<>();
	
	public static int ZONE_TOWN;
	public static boolean ALLOW_TUTORIAL;
	public static String DEFAULT_GLOBAL_CHAT;
	public static String DEFAULT_TRADE_CHAT;
	public static int DEFAULT_PUNISH;
	public static int DEFAULT_PUNISH_PARAM;
	public static boolean BYPASS_VALIDATION;
	public static int DELETE_DAYS;
	public static boolean ALLOW_DISCARDITEM;
	public static int AUTODESTROY_ITEM_AFTER;
	public static String PROTECTED_ITEMS;
	public static List<Integer> LIST_PROTECTED_ITEMS = new ArrayList<>();
	public static boolean DESTROY_DROPPED_PLAYER_ITEM;
	public static boolean DESTROY_EQUIPABLE_PLAYER_ITEM;
	public static boolean SAVE_DROPPED_ITEM;
	public static boolean EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD;
	public static int SAVE_DROPPED_ITEM_INTERVAL;
	public static boolean CLEAR_DROPPED_ITEM_TABLE;
	public static boolean AUTODELETE_INVALID_QUEST_DATA;
	public static boolean MULTIPLE_ITEM_DROP;
	public static boolean FORCE_INVENTORY_UPDATE;
	public static int MAX_DRIFT_RANGE;
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;
	public static int MIN_MONSTER_ANIMATION;
	public static int MAX_MONSTER_ANIMATION;
	public static boolean SERVER_NEWS;
	public static boolean SHOW_NPC_LVL;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean WAREHOUSE_CACHE;
	public static int WAREHOUSE_CACHE_TIME;
	public static boolean ALLOW_FREIGHT;
	public static boolean ALLOW_WEAR;
	public static int WEAR_DELAY;
	public static int WEAR_PRICE;
	public static boolean ALLOW_LOTTERY;
	public static boolean ALLOW_RACE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_RENTPET;
	public static boolean ALLOWFISHING;
	public static boolean ALLOW_BOAT;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean ALLOW_MANOR;
	public static boolean ALLOW_NPC_WALKERS;

	public static boolean GM_EDIT;
	public static boolean ONLY_GM_ITEMS_FREE;
	public static boolean RAID_DISABLE_CURSE;
	public static String COMMUNITY_TYPE;
	public static String BBS_DEFAULT;
	public static boolean SHOW_LEVEL_COMMUNITYBOARD;
	public static boolean SHOW_STATUS_COMMUNITYBOARD;
	public static int NAME_PAGE_SIZE_COMMUNITYBOARD;
	public static int NAME_PER_ROW_COMMUNITYBOARD;
	public static int AI_MAX_THREAD;
	public static boolean GRIDS_ALWAYS_ON;
	public static int GRID_NEIGHBOR_TURNON_TIME;
	public static int GRID_NEIGHBOR_TURNOFF_TIME;
	
	public static int STARTING_ADENA;
	public static int STARTING_ANCIENT;
	public static int WYVERN_SPEED;
	public static int STRIDER_SPEED;
	public static boolean ALLOW_WYVERN_UPGRADER;
	public static boolean EFFECT_CANCELING;
	public static boolean ALLOW_GUARDS;
	public static boolean DEEPBLUE_DROP_RULES;
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	public static int FREIGHT_SLOTS;

	// Augment chance
	public static int AUGMENTATION_NG_SKILL_CHANCE;
	public static int AUGMENTATION_MID_SKILL_CHANCE;
	public static int AUGMENTATION_HIGH_SKILL_CHANCE;
	public static int AUGMENTATION_TOP_SKILL_CHANCE;
	public static int AUGMENTATION_BASESTAT_CHANCE;
	// Augment glow
	public static int AUGMENTATION_NG_GLOW_CHANCE;
	public static int AUGMENTATION_MID_GLOW_CHANCE;
	public static int AUGMENTATION_HIGH_GLOW_CHANCE;
	public static int AUGMENTATION_TOP_GLOW_CHANCE;
	
	public static double HP_REGEN_MULTIPLIER;
	public static double MP_REGEN_MULTIPLIER;
	public static double CP_REGEN_MULTIPLIER;
	public static double RAID_HP_REGEN_MULTIPLIER;
	public static double RAID_MP_REGEN_MULTIPLIER;
	public static double RAID_P_DEFENCE_MULTIPLIER;
	public static double RAID_M_DEFENCE_MULTIPLIER;
	public static float RAID_MIN_RESPAWN_MULTIPLIER;
	public static float RAID_MAX_RESPAWN_MULTIPLIER;
	public static double RAID_MINION_RESPAWN_TIMER;
	public static boolean RB_IMMEDIATE_INFORM;
	public static int PLAYER_SPAWN_PROTECTION;
	public static int UNSTUCK_INTERVAL;
	public static double RESPAWN_RESTORE_CP;
	public static double RESPAWN_RESTORE_HP;
	public static double RESPAWN_RESTORE_MP;
	public static boolean RESPAWN_RANDOM_ENABLED;
	public static int RESPAWN_RANDOM_MAX_OFFSET;
	public static int MAX_PVTSTORE_SLOTS_DWARF;
	public static int MAX_PVTSTORE_SLOTS_OTHER;
	public static boolean STORE_SKILL_COOLTIME;
	public static String PET_RENT_NPC;
	public static List<Integer> LIST_PET_RENT_NPC = new ArrayList<>();
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	public static boolean JAIL_DISABLE_CHAT;
	public static int DEATH_PENALTY_CHANCE;
	
	// Variable doesn't found in any .ini file.
	public static int MAX_ITEM_IN_PACKET;
	
	// Zodiac Settings
	public static boolean ZODIAC_ENABLE;
	public static int ZODIAC_REWARD;
	public static int ZODIAC_REWARD_COUN;
	public static int ZODIAC_VOTE_MINUTES;
	public static boolean SAME_IP_ZODIAC;
	public static int TIME_TO_REGISTER;
	public static int INITIAL_START;
	public static int BETWEEN_EVENTS;
	// TvT Settings
	public static boolean TVT_ALLOW_AUTOEVENT;
	public static String TVT_EVENT_TIMES;
	public static boolean TVT_ALLOW_INTERFERENCE;
	public static boolean TVT_ALLOW_POTIONS;
	public static boolean TVT_ALLOW_SUMMON;
	public static boolean TVT_ON_START_REMOVE_ALL_EFFECTS;
	public static boolean TVT_ON_START_UNSUMMON_PET;
	public static boolean TVT_REVIVE_RECOVERY;
	public static boolean TVT_ANNOUNCE_TEAM_STATS;
	public static boolean TVT_CLOSE_COLISEUM_DOORS;
	public static boolean TVT_ALLOW_ENEMY_HEALING;
	public static boolean TVT_ALLOW_TEAM_CASTING;
	public static boolean TVT_ALLOW_TEAM_ATTACKING;
	public static boolean TVT_ANNOUNCE_REGISTRATION_LOC_NPC;
	public static boolean TVT_ANNOUNCE_SIGNUPS;
	public static boolean TVT_JOIN_CURSED;
	public static boolean TVT_PRICE_NO_KILLS;
	public static boolean TVT_AURA;
	public static String TVT_EVEN_TEAMS;
	public static boolean TVT_ANNOUNCE_REWARD;
	public static int FIRST_TVT_DELAY;
	public static long TVT_REVIVE_DELAY;

	// DM Settings
	public static boolean DM_ALLOW_INTERFERENCE;
	public static boolean DM_ALLOW_POTIONS;
	public static boolean DM_ALLOW_SUMMON;
	public static boolean DM_ON_START_REMOVE_ALL_EFFECTS;
	public static boolean DM_ON_START_UNSUMMON_PET;
	public static boolean ARENA_ENABLED;
	public static int ARENA_INTERVAL;
	public static int ARENA_REWARD_ID;
	public static int ARENA_REWARD_COUNT;
	public static boolean FISHERMAN_ENABLED;
	public static int FISHERMAN_INTERVAL;
	public static int FISHERMAN_REWARD_ID;
	public static int FISHERMAN_REWARD_COUNT;
	public static long DM_REVIVE_DELAY;
	// CTF Settings
	public static boolean ALLOW_CTF_AUTOEVENT;
	public static String CTF_EVENT_TIMES;
	public static String CTF_EVEN_TEAMS;
	public static boolean CTF_ALLOW_INTERFERENCE;
	public static boolean CTF_ALLOW_POTIONS;
	public static boolean CTF_ALLOW_SUMMON;
	public static boolean CTF_ON_START_REMOVE_ALL_EFFECTS;
	public static boolean CTF_ON_START_UNSUMMON_PET;
	public static boolean CTF_ANNOUNCE_TEAM_STATS;
	public static boolean CTF_ANNOUNCE_REWARD;
	public static boolean CTF_JOIN_CURSED;
	public static boolean CTF_REVIVE_RECOVERY;
	public static long CTF_REVIVE_DELAY;
	
	// Wedding Settings
	public static boolean MOD_ALLOW_WEDDING;
	public static int MOD_WEDDING_PRICE;
	public static boolean MOD_WEDDING_PUNISH_INFIDELITY;
	public static boolean MOD_WEDDING_TELEPORT;
	public static int MOD_WEDDING_TELEPORT_PRICE;
	public static int MOD_WEDDING_TELEPORT_DURATION;
	public static boolean MOD_WEDDING_SAMESEX;
	public static boolean MOD_WEDDING_FORMALWEAR;
	public static boolean CUPID_TO_PLAYERS;
	public static int MOD_WEDDING_DIVORCE_COSTS;
	public static boolean MOD_WEDDING_ANNOUNCE;
	
	// Premium Settings
	public static boolean USE_PREMIUMSERVICE;
	public static float PREMIUM_RATE_XP;
	public static float PREMIUM_RATE_SP;
	public static float PREMIUM_RATE_DROP_ADENA;
	public static float PREMIUM_RATE_DROP_SPOIL;
	public static float PREMIUM_RATE_DROP_ITEMS;
	public static float PREMIUM_RATE_DROP_QUEST;
	public static float PREMIUM_RATE_DROP_ITEMS_BY_RAID;
	public static int PREMIUM_PLAYER_DROP_LIMIT;
	public static int PREMIUM_PLAYER_RATE_DROP;
	public static int PREMIUM_PLAYER_RATE_DROP_ITEM;
	public static int PREMIUM_PLAYER_RATE_DROP_EQUIP;
	public static int PREMIUM_PLAYER_RATE_DROP_EQUIP_WEAPON;
	public static int PREMIUM_KARMA_DROP_LIMIT;
	public static int PREMIUM_KARMA_RATE_DROP;
	public static int PREMIUM_KARMA_RATE_DROP_ITEM;
	public static int PREMIUM_KARMA_RATE_DROP_EQUIP;
	public static int PREMIUM_KARMA_RATE_DROP_EQUIP_WEAPON;
	
	// Normal Settings
	public static double RATE_QUEST_DROP;
	public static double RATE_QUEST_REWARD;
	public static double RATE_QUEST_REWARD_XP;
	public static double RATE_QUEST_REWARD_SP;
	public static double RATE_QUEST_REWARD_ADENA;
	
	public static float RATE_XP;
	public static float RATE_SP;
	public static float RATE_PARTY_XP;
	public static float RATE_PARTY_SP;
	public static float RATE_DROP_ADENA;
	public static float RATE_CONSUMABLE_COST;
	public static float RATE_DROP_ITEMS;
	public static float RATE_DROP_SPOIL;
	public static int RATE_DROP_MANOR;
	public static String PARTY_XP_CUTOFF_METHOD;
	public static double PARTY_XP_CUTOFF_PERCENT;
	public static int PARTY_XP_CUTOFF_LEVEL;
	public static float RATE_DROP_QUEST;
	public static float RATE_QUESTS_REWARD;
	public static float RATE_KARMA_EXP_LOST;
	public static float RATE_SIEGE_GUARDS_PRICE;
	public static int PLAYER_DROP_LIMIT;
	public static int PLAYER_RATE_DROP;
	public static int PLAYER_RATE_DROP_ITEM;
	public static int PLAYER_RATE_DROP_EQUIP;
	public static int PLAYER_RATE_DROP_EQUIP_WEAPON;
	public static int KARMA_DROP_LIMIT;
	public static int KARMA_RATE_DROP;
	public static int KARMA_RATE_DROP_ITEM;
	public static int KARMA_RATE_DROP_EQUIP;
	public static int KARMA_RATE_DROP_EQUIP_WEAPON;
	public static float PET_XP_RATE;
	public static int PET_FOOD_RATE;
	public static float SINEATER_XP_RATE;
	public static float RATE_DROP_COMMON_HERBS;
	public static float RATE_DROP_MP_HP_HERBS;
	public static float RATE_DROP_GREATER_HERBS;
	public static float RATE_DROP_SUPERIOR_HERBS;
	public static float RATE_DROP_SPECIAL_HERBS;
	public static float RATE_DROP_ITEMS_BY_RAID;

	public static int ENCHANT_CHANCE_WEAPON;
	public static int ENCHANT_CHANCE_ARMOR;
	public static int ENCHANT_CHANCE_JEWELRY;
	public static int ENCHANT_CHANCE_WEAPON_BLESSED;
	public static int ENCHANT_CHANCE_ARMOR_BLESSED;
	public static int ENCHANT_CHANCE_JEWELRY_BLESSED;
	public static int ENCHANT_MAX_ALLOWED_WEAPON;
	public static int ENCHANT_MAX_ALLOWED_ARMOR;
	public static int ENCHANT_MAX_ALLOWED_JEWELRY;
	public static int ENCHANT_MAX_WEAPON;
	public static int ENCHANT_MAX_ARMOR;
	public static int ENCHANT_MAX_JEWELRY;
	public static int ENCHANT_SAFE_MAX;
	public static int ENCHANT_SAFE_MAX_FULL;
	
	public static int Antharas_Wait_Time;
	public static int Valakas_Wait_Time;
	public static int Random_Of_Valakas_Spawn;
	public static int Interval_Of_Baium_Spawn;
	public static int Random_Of_Baium_Spawn;
	public static int Interval_Of_Antharas_Spawn;
	public static int Random_Of_Antharas_Spawn;
	public static int Interval_Of_Valakas_Spawn;
	public static int Interval_Of_Core_Spawn;
	public static int Random_Of_Core_Spawn;
	public static int Interval_Of_Orfen_Spawn;
	public static int Random_Of_Orfen_Spawn;
	public static int Interval_Of_QueenAnt_Spawn;
	public static int Random_Of_QueenAnt_Spawn;
	public static int Interval_Of_Zaken_Spawn;
	public static int Random_Of_Zaken_Spawn;
	public static int Interval_Of_Sailren_Spawn;
	public static int Random_Of_Sailren_Spawn;
	
	public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static int ALT_FESTIVAL_MIN_PLAYER;
	public static int ALT_MAXIMUM_PLAYER_CONTRIB;
	public static long ALT_FESTIVAL_MANAGER_START;
	public static long ALT_FESTIVAL_LENGTH;
	public static long ALT_FESTIVAL_CYCLE_LENGTH;
	public static long ALT_FESTIVAL_FIRST_SPAWN;
	public static long ALT_FESTIVAL_FIRST_SWARM;
	public static long ALT_FESTIVAL_SECOND_SPAWN;
	public static long ALT_FESTIVAL_SECOND_SWARM;
	public static long ALT_FESTIVAL_CHEST_SPAWN;
	
	public static boolean DEBUG;
	public static boolean DEBUG_LOGGER;
	public static boolean ALT_DEV_NO_SPAWNS;
	public static boolean ALT_DEV_NO_SCRIPT;
	public static boolean ALT_DEV_NO_RB;
	public static boolean VERBOSE_LOADING;
	public static boolean ATTEMPT_COMPILATION;
	public static boolean PURGE_ERROR_LOG;
	public static int REQUEST_ID;
	public static boolean ACCEPT_ALTERNATE_ID;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static int DATABASE_MAX_CONNECTIONS;
	public static String CNAME_TEMPLATE;
	public static String PET_NAME_TEMPLATE;
	public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
	public static int MAXIMUM_ONLINE_USERS;
	public static boolean ALLOW_DUALBOX;
	public static boolean ENABLE_PACKET_PROTECTION;
	public static int MAX_UNKNOWN_PACKETS;
	public static int UNKNOWN_PACKETS_PUNISHMENT;
	public static boolean TEST_SERVER;
	public static boolean SERVER_LIST_TESTSERVER;
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	public static boolean SERVER_LIST_BRACKET;
	public static boolean SERVER_LIST_CLOCK;
	public static boolean SERVER_GMONLY;
	public static int THREAD_P_EFFECTS;
	public static int THREAD_P_GENERAL;
	public static int GENERAL_PACKET_THREAD_CORE_SIZE;
	public static int IO_PACKET_THREAD_CORE_SIZE;
	public static int GENERAL_THREAD_CORE_SIZE;
	public static int PACKET_LIFETIME;
	
	public static String EXTERNAL_HOSTNAME;
	public static String INTERNAL_HOSTNAME;
	public static String GAMESERVER_HOSTNAME;
	public static int PORT_GAME;
	public static int GAME_SERVER_LOGIN_PORT;
	public static String GAME_SERVER_LOGIN_HOST;
	
	public static int SERVER_ID;
	public static byte[] HEX_ID;
	
	public static String LOGIN_BIND_ADDRESS;
	public static int PORT_LOGIN;
	public static int LOGIN_TRY_BEFORE_BAN;
	public static int LOGIN_BLOCK_AFTER_BAN;
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static boolean SHOW_LICENCE;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static int IP_UPDATE_TIME;
	public static boolean DEVELOPER;
	public static boolean FORCE_GGAUTH;
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;
	public static boolean RESERVE_HOST_ON_LOGIN = false;
	
	public static int MMO_SELECTOR_SLEEP_TIME = 20; // default 20
	public static int MMO_MAX_SEND_PER_PASS = 80; // default 80
	public static int MMO_MAX_READ_PER_PASS = 80; // default 80
	public static int MMO_HELPER_BUFFER_COUNT = 20; // default 20
	
	public static int CLIENT_PACKET_QUEUE_SIZE = MMO_MAX_READ_PER_PASS + 2; // default MMO_MAX_READ_PER_PASS + 2
	public static int CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = MMO_MAX_READ_PER_PASS + 1; // default MMO_MAX_READ_PER_PASS + 1
	public static int CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND = 160; // default 160
	public static int CLIENT_PACKET_QUEUE_MEASURE_INTERVAL = 5; // default 5
	public static int CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND = 80; // default 80
	public static int CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN = 2; // default 2
	public static int CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN = 1; // default 1
	public static int CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN = 1; // default 1
	public static int CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN = 5; // default 5
	
	/** Flood Protectors */
	public static int ROLL_DICE;
	public static int HERO_VOICE;
	public static int SUBCLASS;
	public static int DROP_ITEM;
	public static int SERVER_BYPASS;
	public static int MULTISELL;
	public static int MANUFACTURE;
	public static int MANOR;
	public static int SENDMAIL;
	public static int CHARACTER_SELECT;
	public static int GLOBAL_CHAT;
	public static int TRADE_CHAT;
	public static int USE_ITEM;
	public static int SOCIAL;
	
	public static enum ObjectMapType
	{
		L2ObjectHashMap,
		WorldObjectMap
	}
	
	public static enum ObjectSetType
	{
		L2ObjectHashSet,
		WorldObjectSet
	}
	
	public static void load()
	{
		if (Server.serverMode == Server.MODE_GAMESERVER)
		{
			Properties altSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(ALT_SETTINGS_FILE)))
			{
				altSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + ALT_SETTINGS_FILE + " settings!");
			}
			// auto loot
			AUTO_LOOT = Boolean.parseBoolean(altSettings.getProperty("AutoLoot", "False"));
			AUTO_LOOT_RAID = Boolean.parseBoolean(altSettings.getProperty("AutoLootRaid", "False"));
			AUTO_LOOT_GRAND = Boolean.parseBoolean(altSettings.getProperty("AutoLootGrand", "False"));
			AUTO_LOOT_HERBS = Boolean.parseBoolean(altSettings.getProperty("AutoLootHerbs", "False"));
			// skills
			AUTO_LEARN_SKILLS = Boolean.parseBoolean(altSettings.getProperty("AutoLearnSkills", "False"));
			CHECK_SKILLS_ON_ENTER = Boolean.parseBoolean(altSettings.getProperty("CheckSkillsOnEnter", "False"));
			ALLOWED_SKILLS = altSettings.getProperty("AllowedSkills", "541,542,543,544,545,546,547,548,549,550,551,552,553,554,555,556,557,558,617,618,619");
			ALLOWED_SKILLS_LIST = new ArrayList<>();
			for (String id : ALLOWED_SKILLS.trim().split(","))
			{
				ALLOWED_SKILLS_LIST.add(Integer.parseInt(id.trim()));
			}
			LIFE_CRYSTAL_NEEDED = Boolean.parseBoolean(altSettings.getProperty("LifeCrystalNeeded", "True"));
			SP_BOOK_NEEDED = Boolean.parseBoolean(altSettings.getProperty("SpBookNeeded", "True"));
			ES_SP_BOOK_NEEDED = Boolean.parseBoolean(altSettings.getProperty("EnchantSkillSpBookNeeded", "True"));
			ALT_GAME_SKILL_LEARN = Boolean.parseBoolean(altSettings.getProperty("AltGameSkillLearn", "False"));
			// other
			NPCS_ATTACKABLE = Boolean.parseBoolean(altSettings.getProperty("AttackableNpcs", "False"));
			ALT_PARTY_RANGE = Integer.parseInt(altSettings.getProperty("AltPartyRange", "1600"));
			ALT_PARTY_RANGE2 = Integer.parseInt(altSettings.getProperty("AltPartyRange2", "1400"));
			ALT_WEIGHT_LIMIT = Double.parseDouble(altSettings.getProperty("AltWeightLimit", "1"));
			ALT_GAME_MAGICFAILURES = Boolean.parseBoolean(altSettings.getProperty("MagicFailures", "False"));
			ALT_GAME_CANCEL_BOW = altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("bow") || altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
			ALT_GAME_CANCEL_CAST = altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("cast") || altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
			ALT_GAME_SHIELD_BLOCKS = Boolean.parseBoolean(altSettings.getProperty("AltShieldBlocks", "False"));
			ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(altSettings.getProperty("AltPerfectShieldBlockRate", "10"));
			ALT_GAME_MOB_ATTACK_AI = Boolean.parseBoolean(altSettings.getProperty("AltGameMobAttackAI", "False"));
			ALT_MOB_AGRO_IN_PEACEZONE = Boolean.parseBoolean(altSettings.getProperty("AltMobAgroInPeaceZone", "True"));
			ALT_GAME_FREIGHTS = Boolean.parseBoolean(altSettings.getProperty("AltGameFreights", "False"));
			ALT_GAME_FREIGHT_PRICE = Integer.parseInt(altSettings.getProperty("AltGameFreightPrice", "1000"));
			ALT_GAME_EXPONENT_XP = Float.parseFloat(altSettings.getProperty("AltGameExponentXp", "0."));
			ALT_GAME_EXPONENT_SP = Float.parseFloat(altSettings.getProperty("AltGameExponentSp", "0."));
			ALT_GAME_TIREDNESS = Boolean.parseBoolean(altSettings.getProperty("AltGameTiredness", "False"));
			// altkarma
			ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanBeKilledInPeaceZone", "False"));
			ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanShop", "True"));
			ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanUseGK", "False"));
			ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanTeleport", "True"));
			ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanTrade", "True"));
			ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanUseWareHouse", "True"));
			ALT_GAME_FREE_TELEPORT = Boolean.parseBoolean(altSettings.getProperty("AltFreeTeleporting", "False"));
			
			// crafting
			IS_CRAFTING_ENABLED = Boolean.parseBoolean(altSettings.getProperty("CraftingEnabled", "True"));
			DWARF_RECIPE_LIMIT = Integer.parseInt(altSettings.getProperty("DwarfRecipeLimit", "50"));
			COMMON_RECIPE_LIMIT = Integer.parseInt(altSettings.getProperty("CommonRecipeLimit", "50"));
			ALT_GAME_CREATION = Boolean.parseBoolean(altSettings.getProperty("AltGameCreation", "False"));
			ALT_GAME_CREATION_SPEED = Double.parseDouble(altSettings.getProperty("AltGameCreationSpeed", "1"));
			ALT_GAME_CREATION_XP_RATE = Double.parseDouble(altSettings.getProperty("AltGameCreationRateXp", "1"));
			ALT_GAME_CREATION_SP_RATE = Double.parseDouble(altSettings.getProperty("AltGameCreationRateSp", "1"));
			ALLOW_SATURDAY_RATE_XP_SP = Boolean.parseBoolean(altSettings.getProperty("AllowSaturDayXpSp", "False"));
			SATURDAY_RATE_XP = Float.parseFloat(altSettings.getProperty("SaturDayXp", "2."));
			SATURDAY_RATE_SP = Float.parseFloat(altSettings.getProperty("SaturDaySp", "2."));
			ALT_BLACKSMITH_USE_RECIPES = Boolean.parseBoolean(altSettings.getProperty("AltBlacksmithUseRecipes", "True"));
			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.parseBoolean(altSettings.getProperty("AltSubClassWithoutQuests", "True"));
			MAX_SUBCLASS = Byte.parseByte(altSettings.getProperty("MaxSubclass", "3"));
			BASE_SUBCLASS_LEVEL = Byte.parseByte(altSettings.getProperty("BaseSubclassLevel", "40"));
			MAX_SUBCLASS_LEVEL = Byte.parseByte(altSettings.getProperty("MaxSubclassLevel", "81"));
			MAX_PATK_SPEED = Integer.parseInt(altSettings.getProperty("MaxPAtkSpeed", "1500"));
			MAX_MATK_SPEED = Integer.parseInt(altSettings.getProperty("MaxMAtkSpeed", "2000"));
			RESTORE_EFFECTS_ON_SUBCLASS_CHANGE = Boolean.parseBoolean(altSettings.getProperty("RestoreEffectsOnSub", "False"));
			DISABLE_GRADE_PENALTY = Boolean.parseBoolean(altSettings.getProperty("DisableGradePenalty", "False"));
			DISABLE_WEIGHT_PENALTY = Boolean.parseBoolean(altSettings.getProperty("DisableWeightPenalty", "False"));
			// manor
			ALT_MANOR_REFRESH_TIME = Integer.parseInt(altSettings.getProperty("AltManorRefreshTime", "20"));
			ALT_MANOR_REFRESH_MIN = Integer.parseInt(altSettings.getProperty("AltManorRefreshMin", "00"));
			ALT_MANOR_APPROVE_TIME = Integer.parseInt(altSettings.getProperty("AltManorApproveTime", "6"));
			ALT_MANOR_APPROVE_MIN = Integer.parseInt(altSettings.getProperty("AltManorApproveMin", "00"));
			ALT_MANOR_MAINTENANCE_PERIOD = Integer.parseInt(altSettings.getProperty("AltManorMaintenancePeriod", "360000"));
			ALT_MANOR_SAVE_ALL_ACTIONS = Boolean.parseBoolean(altSettings.getProperty("AltManorSaveAllActions", "False"));
			ALT_MANOR_SAVE_PERIOD_RATE = Integer.parseInt(altSettings.getProperty("AltManorSavePeriodRate", "2"));
			// lottery
			ALT_LOTTERY_PRIZE = Integer.parseInt(altSettings.getProperty("AltLotteryPrize", "50000"));
			ALT_LOTTERY_TICKET_PRICE = Integer.parseInt(altSettings.getProperty("AltLotteryTicketPrice", "2000"));
			ALT_LOTTERY_5_NUMBER_RATE = Float.parseFloat(altSettings.getProperty("AltLottery5NumberRate", "0.6"));
			ALT_LOTTERY_4_NUMBER_RATE = Float.parseFloat(altSettings.getProperty("AltLottery4NumberRate", "0.2"));
			ALT_LOTTERY_3_NUMBER_RATE = Float.parseFloat(altSettings.getProperty("AltLottery3NumberRate", "0.2"));
			ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = Integer.parseInt(altSettings.getProperty("AltLottery2and1NumberPrize", "200"));
			// 4sepulchers
			FS_TIME_ATTACK = Integer.parseInt(altSettings.getProperty("TimeOfAttack", "50"));
			FS_TIME_COOLDOWN = Integer.parseInt(altSettings.getProperty("TimeOfCoolDown", "5"));
			FS_TIME_ENTRY = Integer.parseInt(altSettings.getProperty("TimeOfEntry", "3"));
			FS_TIME_WARMUP = Integer.parseInt(altSettings.getProperty("TimeOfWarmUp", "2"));
			FS_PARTY_MEMBER_COUNT = Integer.parseInt(altSettings.getProperty("NumberOfNecessaryPartyMembers", "4"));
			if (FS_TIME_ATTACK <= 0)
			{
				FS_TIME_ATTACK = 50;
			}
			if (FS_TIME_COOLDOWN <= 0)
			{
				FS_TIME_COOLDOWN = 5;
			}
			if (FS_TIME_ENTRY <= 0)
			{
				FS_TIME_ENTRY = 3;
			}
			if (FS_TIME_ENTRY <= 0)
			{
				FS_TIME_ENTRY = 3;
			}
			if (FS_TIME_ENTRY <= 0)
			{
				FS_TIME_ENTRY = 3;
			}
			// rift
			RIFT_MIN_PARTY_SIZE = Integer.parseInt(altSettings.getProperty("RiftMinPartySize", "5"));
			RIFT_MAX_JUMPS = Integer.parseInt(altSettings.getProperty("MaxRiftJumps", "4"));
			RIFT_SPAWN_DELAY = Integer.parseInt(altSettings.getProperty("RiftSpawnDelay", "10000"));
			RIFT_AUTO_JUMPS_TIME_MIN = Integer.parseInt(altSettings.getProperty("AutoJumpsDelayMin", "480"));
			RIFT_AUTO_JUMPS_TIME_MAX = Integer.parseInt(altSettings.getProperty("AutoJumpsDelayMax", "600"));
			RIFT_BOSS_ROOM_TIME_MUTIPLY = Float.parseFloat(altSettings.getProperty("BossRoomTimeMultiply", "1.5"));
			RIFT_ENTER_COST_RECRUIT = Integer.parseInt(altSettings.getProperty("RecruitCost", "18"));
			RIFT_ENTER_COST_SOLDIER = Integer.parseInt(altSettings.getProperty("SoldierCost", "21"));
			RIFT_ENTER_COST_OFFICER = Integer.parseInt(altSettings.getProperty("OfficerCost", "24"));
			RIFT_ENTER_COST_CAPTAIN = Integer.parseInt(altSettings.getProperty("CaptainCost", "27"));
			RIFT_ENTER_COST_COMMANDER = Integer.parseInt(altSettings.getProperty("CommanderCost", "30"));
			RIFT_ENTER_COST_HERO = Integer.parseInt(altSettings.getProperty("HeroCost", "33"));
			CRUMA_TOWER_LEVEL_RESTRICT = Integer.parseInt(altSettings.getProperty("CrumaTowerLevelRestrict", "56"));
			// spawns
			ALT_RESPAWN_POINT = Boolean.parseBoolean(altSettings.getProperty("AllowCustomRespawnPoint", "False"));
			ALT_RESPAWN_POINT_X = Integer.parseInt(altSettings.getProperty("CustomRespawnPointX", "0"));
			ALT_RESPAWN_POINT_Y = Integer.parseInt(altSettings.getProperty("CustomRespawnPointY", "0"));
			ALT_RESPAWN_POINT_Z = Integer.parseInt(altSettings.getProperty("CustomRespawnPointZ", "0"));
			// buffs
			DANCE_TIME_MULTIPLIER = Float.parseFloat(altSettings.getProperty("DanceTimeMultiplier", "1"));
			SPIRIT_TIME_MULTIPLIER = Float.parseFloat(altSettings.getProperty("SpiritMultiplier", "1"));
			BUFF_TIME_MULTIPLIER = Float.parseFloat(altSettings.getProperty("BuffMultiplier", "1"));
			BUFFS_MAX_AMOUNT = Byte.parseByte(altSettings.getProperty("MaxBuffAmount", "24"));
			DEBUFFS_MAX_AMOUNT = Byte.parseByte(altSettings.getProperty("MaxDebuffAmount", "6"));
			// other
			ALT_GAME_VIEWNPC = Boolean.parseBoolean(altSettings.getProperty("AltGameViewNpc", "False"));
			PLAYER_ALT_GAME_VIEWNPC = Boolean.parseBoolean(altSettings.getProperty("PlayerAltGameViewNpc", "False"));

			Properties clanSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(CLAN_CONFIG_FILE)))
			{
				clanSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + CLAN_CONFIG_FILE + " settings!");
			}
			
			// clan
			ALT_CLAN_JOIN_DAYS = Integer.parseInt(clanSettings.getProperty("DaysBeforeJoinAClan", "5"));
			ALT_CLAN_CREATE_DAYS = Integer.parseInt(clanSettings.getProperty("DaysBeforeCreateAClan", "10"));
			ALT_CLAN_DISSOLVE_DAYS = Integer.parseInt(clanSettings.getProperty("DaysToPassToDissolveAClan", "7"));
			ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = Integer.parseInt(clanSettings.getProperty("DaysBeforeJoinAllyWhenLeaved", "1"));
			ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = Integer.parseInt(clanSettings.getProperty("DaysBeforeJoinAllyWhenDismissed", "1"));
			ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = Integer.parseInt(clanSettings.getProperty("DaysBeforeAcceptNewClanWhenDismissed", "1"));
			ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = Integer.parseInt(clanSettings.getProperty("DaysBeforeCreateNewAllyWhenDissolved", "10"));
			ALT_MAX_NUM_OF_CLANS_IN_ALLY = Integer.parseInt(clanSettings.getProperty("AltMaxNumOfClansInAlly", "3"));
			ALT_CLAN_MEMBERS_FOR_WAR = Integer.parseInt(clanSettings.getProperty("AltClanMembersForWar", "15"));
			ALT_REPUTATION_SCORE_PER_KILL = Integer.parseInt(clanSettings.getProperty("ReputationScorePerKill", "5"));
			ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE = Boolean.parseBoolean(clanSettings.getProperty("AltNewCharAlwaysIsNewbie", "False"));
			ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Boolean.parseBoolean(clanSettings.getProperty("AltMembersCanWithdrawFromClanWH", "False"));
			REMOVE_CASTLE_CIRCLETS = Boolean.parseBoolean(clanSettings.getProperty("RemoveCastleCirclets", "True"));

			// Equipment Restriction
			CASTLE_SHIELD = Boolean.parseBoolean(clanSettings.getProperty("CastleShieldRestriction", "True"));
			CLANHALL_SHIELD = Boolean.parseBoolean(clanSettings.getProperty("ClanHallShieldRestriction", "True"));
			APELLA_ARMORS = Boolean.parseBoolean(clanSettings.getProperty("ApellaArmorsRestriction", "True"));
			OATH_ARMORS = Boolean.parseBoolean(clanSettings.getProperty("OathArmorsRestriction", "True"));
			CASTLE_CROWN = Boolean.parseBoolean(clanSettings.getProperty("CastleLordsCrownRestriction", "True"));
			CASTLE_CIRCLETS = Boolean.parseBoolean(clanSettings.getProperty("CastleCircletsRestriction", "True"));

			CH_TELE_FEE_RATIO = Long.valueOf(clanSettings.getProperty("ClanHallTeleportFunctionFeeRation", "86400000"));
			CH_TELE1_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallTeleportFunctionFeeLvl1", "86400000"));
			CH_TELE2_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallTeleportFunctionFeeLvl2", "86400000"));
			CH_TELE3_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallTeleportFunctionFeeLvl3", "86400000"));
			CH_TELE4_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallTeleportFunctionFeeLvl4", "86400000"));
			CH_SUPPORT_FEE_RATIO = Long.valueOf(clanSettings.getProperty("ClanHallSupportFunctionFeeRation", "86400000"));
			CH_SUPPORT1_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallSupportFeeLvl1", "86400000"));
			CH_SUPPORT2_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallSupportFeeLvl2", "86400000"));
			CH_SUPPORT3_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallSupportFeeLvl3", "86400000"));
			CH_SUPPORT4_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallSupportFeeLvl4", "86400000"));
			CH_SUPPORT5_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallSupportFeeLvl5", "86400000"));
			CH_SUPPORT6_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallSupportFeeLvl6", "86400000"));
			CH_SUPPORT7_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallSupportFeeLvl7", "86400000"));
			CH_SUPPORT8_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallSupportFeeLvl8", "86400000"));
			CH_SUPPORT9_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallSupportFeeLvl9", "86400000"));
			CH_MPREG_FEE_RATIO = Long.valueOf(clanSettings.getProperty("ClanHallMpRegenerationFunctionFeeRation", "86400000"));
			CH_MPREG1_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallMpRegenerationFeeLvl1", "86400000"));
			CH_MPREG2_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallMpRegenerationFeeLvl2", "86400000"));
			CH_MPREG3_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallMpRegenerationFeeLvl3", "86400000"));
			CH_MPREG4_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallMpRegenerationFeeLvl4", "86400000"));
			CH_MPREG5_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallMpRegenerationFeeLvl5", "86400000"));
			CH_HPREG_FEE_RATIO = Long.valueOf(clanSettings.getProperty("ClanHallHpRegenerationFunctionFeeRation", "86400000"));
			CH_HPREG1_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallHpRegenerationFeeLvl1", "86400000"));
			CH_HPREG2_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallHpRegenerationFeeLvl2", "86400000"));
			CH_HPREG3_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallHpRegenerationFeeLvl3", "86400000"));
			CH_HPREG4_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallHpRegenerationFeeLvl4", "86400000"));
			CH_HPREG5_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallHpRegenerationFeeLvl5", "86400000"));
			CH_HPREG6_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallHpRegenerationFeeLvl6", "86400000"));
			CH_HPREG7_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallHpRegenerationFeeLvl7", "86400000"));
			CH_HPREG8_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallHpRegenerationFeeLvl8", "86400000"));
			CH_HPREG9_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallHpRegenerationFeeLvl9", "86400000"));
			CH_HPREG10_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallHpRegenerationFeeLvl10", "86400000"));
			CH_HPREG11_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallHpRegenerationFeeLvl11", "86400000"));
			CH_HPREG12_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallHpRegenerationFeeLvl12", "86400000"));
			CH_HPREG13_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallHpRegenerationFeeLvl13", "86400000"));
			CH_EXPREG_FEE_RATIO = Long.valueOf(clanSettings.getProperty("ClanHallExpRegenerationFunctionFeeRation", "86400000"));
			CH_EXPREG1_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallExpRegenerationFeeLvl1", "86400000"));
			CH_EXPREG2_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallExpRegenerationFeeLvl2", "86400000"));
			CH_EXPREG3_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallExpRegenerationFeeLvl3", "86400000"));
			CH_EXPREG4_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallExpRegenerationFeeLvl4", "86400000"));
			CH_EXPREG5_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallExpRegenerationFeeLvl5", "86400000"));
			CH_EXPREG6_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallExpRegenerationFeeLvl6", "86400000"));
			CH_EXPREG7_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallExpRegenerationFeeLvl7", "86400000"));
			CH_ITEM_FEE_RATIO = Long.valueOf(clanSettings.getProperty("ClanHallItemCreationFunctionFeeRation", "86400000"));
			CH_ITEM1_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallItemCreationFunctionFeeLvl1", "86400000"));
			CH_ITEM2_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallItemCreationFunctionFeeLvl2", "86400000"));
			CH_ITEM3_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallItemCreationFunctionFeeLvl3", "86400000"));
			CH_CURTAIN_FEE_RATIO = Long.valueOf(clanSettings.getProperty("ClanHallCurtainFunctionFeeRation", "86400000"));
			CH_CURTAIN1_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallCurtainFunctionFeeLvl1", "86400000"));
			CH_CURTAIN2_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallCurtainFunctionFeeLvl2", "86400000"));
			CH_FRONT_FEE_RATIO = Long.valueOf(clanSettings.getProperty("ClanHallFrontPlatformFunctionFeeRation", "86400000"));
			CH_FRONT1_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallFrontPlatformFunctionFeeLvl1", "86400000"));
			CH_FRONT2_FEE = Integer.valueOf(clanSettings.getProperty("ClanHallFrontPlatformFunctionFeeLvl2", "86400000"));
			
			Properties versionSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(SERVER_VERSION_FILE)))
			{
				versionSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + SERVER_VERSION_FILE + " settings!");
			}
			SERVER_VERSION = versionSettings.getProperty("version", "Unsupported Custom Version.");
			SERVER_BUILD_DATE = versionSettings.getProperty("builddate", "Undefined Date.");
			
			Properties AdminSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(ADMIN_CONFIG_FILE)))
			{
				AdminSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + ADMIN_CONFIG_FILE + " settings!");
			}
			GM_HERO_AURA = Boolean.parseBoolean(AdminSettings.getProperty("GMHeroAura", "False"));
			GM_STARTUP_INVULNERABLE = Boolean.parseBoolean(AdminSettings.getProperty("GMStartupInvulnerable", "True"));
			GM_STARTUP_INVISIBLE = Boolean.parseBoolean(AdminSettings.getProperty("GMStartupInvisible", "True"));
			GM_STARTUP_SILENCE = Boolean.parseBoolean(AdminSettings.getProperty("GMStartupSilence", "False"));
			GM_STARTUP_AUTO_LIST = Boolean.parseBoolean(AdminSettings.getProperty("GMStartupAutoList", "False"));
			PETITIONING_ALLOWED = Boolean.parseBoolean(AdminSettings.getProperty("PetitioningAllowed", "True"));
			MAX_PETITIONS_PER_PLAYER = Integer.parseInt(AdminSettings.getProperty("MaxPetitionsPerPlayer", "5"));
			MAX_PETITIONS_PENDING = Integer.parseInt(AdminSettings.getProperty("MaxPetitionsPending", "25"));

			Properties PvPSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(PVP_CONFIG_FILE)))
			{
				PvPSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + PVP_CONFIG_FILE + " settings!");
			}
			ALLOW_POTS_IN_PVP = Boolean.parseBoolean(PvPSettings.getProperty("AllowPotsInPvP", "True"));
			ALLOW_SOE_IN_PVP = Boolean.parseBoolean(PvPSettings.getProperty("AllowSoEInPvP", "True"));
			REMOVE_BUFFS_ON_DIE = Boolean.parseBoolean(PvPSettings.getProperty("RemoveBuffsOnDie", "True"));
			KARMA_NONDROPPABLE_PET_ITEMS = PvPSettings.getProperty("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650");
			KARMA_LIST_NONDROPPABLE_PET_ITEMS = new ArrayList<>();
			for (String petid : KARMA_NONDROPPABLE_PET_ITEMS.split(","))
			{
				KARMA_LIST_NONDROPPABLE_PET_ITEMS.add(Integer.parseInt(petid));
			}
			KARMA_NONDROPPABLE_ITEMS = PvPSettings.getProperty("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621");
			KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<>();
			for (String petid : KARMA_NONDROPPABLE_ITEMS.split(","))
			{
				KARMA_LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(petid));
			}
			KARMA_PK_LIMIT = Integer.parseInt(PvPSettings.getProperty("MinimumPKRequiredToDrop", "5"));
			PVP_NORMAL_TIME = Integer.parseInt(PvPSettings.getProperty("PvPVsNormalTime", "15000"));
			PVP_PVP_TIME = Integer.parseInt(PvPSettings.getProperty("PvPVsPvPTime", "30000"));
			
			Properties L2JHellasSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(MOD_L2JHellas_CONFIG_FILE)))
			{
				L2JHellasSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + MOD_L2JHellas_CONFIG_FILE + " settings!");
			}
			ALLOW_FAKE_PLAYERS = Boolean.parseBoolean(L2JHellasSettings.getProperty("AllowFakePlayers", "False"));
			BANKING_SYSTEM_ENABLED = Boolean.parseBoolean(L2JHellasSettings.getProperty("BankingEnabled", "False"));
			BANKING_SYSTEM_GOLDBARS = Integer.parseInt(L2JHellasSettings.getProperty("BankingGoldbarCount", "1"));
			BANKING_SYSTEM_ADENA = Integer.parseInt(L2JHellasSettings.getProperty("BankingAdenaCount", "500000000"));
			BANKING_SYSTEM_ITEM = Integer.parseInt(L2JHellasSettings.getProperty("BankingItemId", "3470"));
			CHAR_TITLE = Boolean.parseBoolean(L2JHellasSettings.getProperty("CharTitle", "False"));
			ADD_CHAR_TITLE = L2JHellasSettings.getProperty("CharAddTitle", "l2jhellas");
			ALLOW_CREATE_LVL = Boolean.parseBoolean(L2JHellasSettings.getProperty("AllowCustomStartLvl", "False"));
			CUSTOM_START_LVL = Integer.parseInt(L2JHellasSettings.getProperty("CustomStartLvl", "1"));
			ALLOW_HERO_SUBSKILL = Boolean.parseBoolean(L2JHellasSettings.getProperty("CustomHeroSubSkill", "False"));
			HERO_COUNT = Integer.parseInt(L2JHellasSettings.getProperty("HeroCount", "1"));
			GM_OVER_ENCHANT = Integer.parseInt(L2JHellasSettings.getProperty("GMOverEnchant", "0"));
			MAX_RUN_SPEED = Integer.parseInt(L2JHellasSettings.getProperty("MaxRunSpeed", "500"));
			MAX_PCRIT_RATE = Integer.parseInt(L2JHellasSettings.getProperty("MaxPCritRate", "500"));
			MAX_MCRIT_RATE = Integer.parseInt(L2JHellasSettings.getProperty("MaxMCritRate", "300"));
			ALT_MAX_EVASION = Integer.parseInt(L2JHellasSettings.getProperty("MaxEvasion", "200"));
			SOUL_CRYSTAL_BREAK_CHANCE = Integer.parseInt(L2JHellasSettings.getProperty("SoulCrystalBreakChance", "10"));
			SOUL_CRYSTAL_LEVEL_CHANCE = Integer.parseInt(L2JHellasSettings.getProperty("SoulCrystalLevelChance", "32"));
			ONLINE_VOICE_ALLOW = Boolean.parseBoolean(L2JHellasSettings.getProperty("AllowOnlineCommand", "False"));
			ALT_GAME_FLAGGED_PLAYER_CAN_USE_GK = Boolean.parseBoolean(L2JHellasSettings.getProperty("AltFlaggedPlayerCanUseGK", "False"));
			ALLOW_PLAYERS_REFUSAL = Boolean.parseBoolean(L2JHellasSettings.getProperty("AllowPlayersRefusal", "False"));
			ALLOW_INFO_COMMAND = Boolean.parseBoolean(L2JHellasSettings.getProperty("AllowVoiceInfoCommand", "False"));
			ALLOW_TRADEOFF_COMMAND = Boolean.parseBoolean(L2JHellasSettings.getProperty("AllowTradeoffCommand", "False"));
			ALT_SUBCLASS_SKILLS = Boolean.parseBoolean(L2JHellasSettings.getProperty("AltSubClassSkills", "False"));
			DONATOR_NAME_COLOR_ENABLED = Boolean.parseBoolean(L2JHellasSettings.getProperty("DonatorNameColorEnabled", "False"));
			DONATOR_NAME_COLOR = Integer.decode("0x" + L2JHellasSettings.getProperty("DonatorColorName", "00FFFF"));
			DONATOR_TITLE_COLOR_ENABLED = Boolean.parseBoolean(L2JHellasSettings.getProperty("DonatorTitleColorEnabled", "False"));
			DONATOR_TITLE_COLOR = Integer.decode("0x" + L2JHellasSettings.getProperty("DonatorColorTitle", "0000FF"));
			ALLOW_VIPTELEPORT_COMMAND = Boolean.parseBoolean(L2JHellasSettings.getProperty("AllowVipTeleportCommand", "False"));
			VIP_X = Integer.parseInt(L2JHellasSettings.getProperty("LocationX", "0"));
			VIP_Y = Integer.parseInt(L2JHellasSettings.getProperty("LocationY", "0"));
			VIP_Z = Integer.parseInt(L2JHellasSettings.getProperty("LocationZ", "0"));
			ADD_EXP = Integer.parseInt(L2JHellasSettings.getProperty("AddExpAtPvp", "0"));
			ADD_SP = Integer.parseInt(L2JHellasSettings.getProperty("AddSpAtPvp", "0"));
			ABORT_RR = L2JHellasSettings.getProperty("AbortRestart", "L2JHellas");
			SPAWN_CHAR = Boolean.parseBoolean(L2JHellasSettings.getProperty("CustomSpawn", "False"));
			SPAWN_X = Integer.parseInt(L2JHellasSettings.getProperty("SpawnX", "0"));
			SPAWN_Y = Integer.parseInt(L2JHellasSettings.getProperty("SpawnY", "0"));
			SPAWN_Z = Integer.parseInt(L2JHellasSettings.getProperty("SpawnZ", "0"));
			USE_SAY_FILTER = Boolean.parseBoolean(L2JHellasSettings.getProperty("UseChatFilter", "False"));
			CHAT_FILTER_CHARS = L2JHellasSettings.getProperty("ChatFilterChars", "***");
			ALLOW_DAGGERS_WEAR_HEAVY = Boolean.parseBoolean(L2JHellasSettings.getProperty("AllowDaggersUseHeavy", "True"));
			ALLOW_ARCHERS_WEAR_HEAVY = Boolean.parseBoolean(L2JHellasSettings.getProperty("AllowArchersUseHeavy", "True"));
			
			Properties RankSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(MOD_RANK_CONFIG_FILE)))
			{
				RankSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + MOD_L2JHellas_CONFIG_FILE + " settings!");
			}
			RANK_PVP_SYSTEM_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RankPvpSystemEnabled", "false"));
			
			PVP_COUNTER_FOR_ALTT_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("PvpCounterForAltTEnabled", "false"));
			PVP_COUNTER_FOR_ALTT_LEGAL_KILLS_ONLY = Boolean.parseBoolean(RankSettings.getProperty("PvpCounterForAltTLegalKillsOnly", "false"));
			
			DATABASE_CLEANER_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("DatabaseCleanerEnabled", "false"));
			DATABASE_CLEANER_REPEAT_TIME = Integer.parseInt(RankSettings.getProperty("DatabaseCleanerRepeatTime", "0"));
			if (DATABASE_CLEANER_REPEAT_TIME <= 0)
			{
				DATABASE_CLEANER_ENABLED = false;
				_log.warning(Config.class.getName() + ": [DatabaseCleanerRepeatTime]: invalid config property -> \"" + DATABASE_CLEANER_REPEAT_TIME + "\"");
			}
			else
			{
				DATABASE_CLEANER_REPEAT_TIME *= 86400000;
			}
			
			RPC_REWARD_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RpcRewardEnabled", "false"));
			RPC_REWARD_AMOUNT = Integer.parseInt(RankSettings.getProperty("RpcRewardAmmount", "1"));
			RPC_REWARD_MIN_LVL = Integer.parseInt(RankSettings.getProperty("RpcRewardMinLvl", "76"));
			
			PVP_REWARD_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("PvpRewardEnabled", "false"));
			PVP_REWARD_ID = Integer.parseInt(RankSettings.getProperty("PvpRewardId", "57"));
			PVP_REWARD_AMOUNT = Integer.parseInt(RankSettings.getProperty("PvpRewardAmount", "1"));
			PVP_REWARD_MIN_LVL = Integer.parseInt(RankSettings.getProperty("PvpRewardMinLvl", "76"));
			
			REWARD_FOR_PK_KILLER_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RewardForPkKillerEnabled", "true"));
			RANK_PVP_REWARD_MIN_LVL = Integer.parseInt(RankSettings.getProperty("RankPvpRewardMinLvl", "76"));
			REWARD_FOR_INNOCENT_KILL_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RewardForInnocentKillEnabled", "false"));
			
			RANK_PVP_REWARD_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RankPvpRewardEnabled", "true"));
			REWARD_LEGAL_KILL_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RewardLegalKillEnabled", "true"));
			RANK_LEVEL_REWARD_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RankLevelRewardEnabled", "true"));
			RANK_SKILL_REWARD_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RankSkillRewardEnabled", "true"));
			
			RANKS_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RanksEnabled", "false"));
			RANK_POINTS_MIN_LVL = Integer.parseInt(RankSettings.getProperty("RankPointsMinLvl", "76"));
			RANK_POINTS_CUT_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RankPointsCutEnabled", "true"));
			
			RANK_RPC_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RankRpcEnabled", "false"));
			RANK_RPC_CUT_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RankRpcCutEnabled", "true"));
			
			// set rank's:
			String id1[] = RankSettings.getProperty("RankNames", "").split(",");
			String id2[] = RankSettings.getProperty("RankMinPoints", "").split(",");
			String id3[] = RankSettings.getProperty("RankPointsForKill", "").split(",");
			String id5[] = RankSettings.getProperty("RankRpcAmount", "").split(",");
			String id6[] = RankSettings.getProperty("NickColors", "").split(",");
			String id7[] = RankSettings.getProperty("TitleColors", "").split(",");
			
			if (RANK_PVP_SYSTEM_ENABLED || RANK_RPC_ENABLED || RANKS_ENABLED)
			{
				if (id1.length != id2.length || id1.length != id3.length || id1.length != id5.length || id1.length != id6.length || id1.length != id7.length)
				{
					_log.info(Config.class.getName() + "[RankPvpSystemConfig]: Arrays sizes should be the same!");
					
					_log.info(Config.class.getName() + "RANK_NAMES         		 :" + id1.length);
					_log.info(Config.class.getName() + "RANK_MIN_POINTS    		 :" + id2.length);
					_log.info(Config.class.getName() + "RANK_POINTS_FOR_KILL		 :" + id3.length);
					_log.info(Config.class.getName() + "RANK_RPC_AMOUNT		 	 :" + id5.length);
					_log.info(Config.class.getName() + "RANK_NICK_COLORS    		 :" + id6.length);
					_log.info(Config.class.getName() + "RANK_TITLE_COLORS  		 :" + id7.length);
				}
				else if (id1.length == 0 || id2.length == 0 || id3.length == 0 || id5.length == 0 || id6.length == 0 || id7.length == 0)
				{
					_log.info(Config.class.getName() + "[RankPvpSystemConfig]: Arrays sizes must be greater than 0!");
					
					_log.info(Config.class.getName() + "RANK_NAMES         		 :" + id1.length);
					_log.info(Config.class.getName() + "RANK_MIN_POINTS    		 :" + id2.length);
					_log.info(Config.class.getName() + "RANK_POINTS_FOR_KILL		 :" + id3.length);
					_log.info(Config.class.getName() + "RANK_RPC_AMOUNT		 	 :" + id5.length);
					_log.info(Config.class.getName() + "RANK_NICK_COLORS    		 :" + id6.length);
					_log.info(Config.class.getName() + "RANK_TITLE_COLORS  		 :" + id7.length);
				}
				else if (id2.length > 0 && Integer.parseInt(id2[id2.length - 1]) != 0)
				{
					_log.info(Config.class.getName() + "[RankMinPoints]: Last value must equal 0! Example: ...,6,5,4,3,2,1,0");
				}
				else
				{
					for (int i = 1; i <= id1.length; i++)
					{
						Rank rank = new Rank();
						
						rank.setId(i);
						rank.setName(id1[id1.length - i]);
						rank.setMinPoints(Long.parseLong(id2[id1.length - i]));
						rank.setPointsForKill(Integer.parseInt(id3[id1.length - i]));
						
						rank.setRpc(Integer.parseInt(id5[id1.length - i]));
						rank.setNickColor(Integer.decode("0x" + id6[id1.length - i]));
						rank.setTitleColor(Integer.decode("0x" + id7[id1.length - i]));
						
						RankTable.getInstance().getRankList().put(i, rank);
					}
				}
			}
			
			NICK_COLOR_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("NickColorEnabled", "false"));
			TITLE_COLOR_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("TitleColorEnabled", "false"));
			
			RANK_POINTS_DOWN_COUNT_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RankPointsDownCountEnabled", "false"));
			RANK_POINTS_DOWN_AMOUNTS = new ArrayList<>();
			for (String id4 : RankSettings.getProperty("RankPointsDownAmounts", "").split(","))
			{
				RANK_POINTS_DOWN_AMOUNTS.add(Integer.parseInt(id4));
			}
			
			RANK_SHOUT_INFO_ON_KILL_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RankShoutInfoOnKillEnabled", "false"));
			RANK_SHOUT_BONUS_INFO_ON_KILL_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RankShoutBonusInfoOnKillEnabled", "false"));
			
			WAR_KILLS_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("WarKillsEnabled", "false"));
			if (WAR_KILLS_ENABLED)
			{
				WAR_RANK_POINTS_RATIO = Double.parseDouble(RankSettings.getProperty("WarRankPointsRatio", "1.0"));
			}
			else
			{
				WAR_RANK_POINTS_RATIO = 1.0;
			}
			
			COMBO_KILL_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("ComboKillEnabled", "false"));
			COMBO_KILL_PROTECTION_WITH_LEGAL_KILL_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("ComboKillProtectionWithLegalKillEnabled", "false"));
			COMBO_KILL_PROTECTION_NO_REPEAT_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("ComboKillProtectionNoRepeatEnabled", "false"));
			
			PVP_EXP_DECREASE_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("PvpExpDecreaseEnabled", "false"));
			PVP_EXP_DECREASE_METHOD = Integer.parseInt(RankSettings.getProperty("PvpExpDecreaseMethod", "1"));
			if (PVP_EXP_DECREASE_METHOD <= 0 || PVP_EXP_DECREASE_METHOD > 3)
				PVP_EXP_DECREASE_METHOD = 1;
			
			PVP_EXP_DECREASE_CONSTANT = Integer.parseInt(RankSettings.getProperty("PvpExpDecreaseConstant", "1"));
			if (PVP_EXP_DECREASE_CONSTANT <= 0)
				PVP_EXP_DECREASE_CONSTANT = 1;
			
			PVP_EXP_DECREASE_FRACTION = Double.parseDouble(RankSettings.getProperty("PvpExpDecreaseFraction", "1.0"));
			if (PVP_EXP_DECREASE_FRACTION <= 0.0 || PVP_EXP_DECREASE_FRACTION > 1.0)
				PVP_EXP_DECREASE_FRACTION = 1.0;
			
			PVP_EXP_DECREASE_ON_LEGAL_KILL_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("PvpExpDecreaseOnLegalKillEnabled", "true"));
			
			String propertyValue = RankSettings.getProperty("ComboKillLocalAreaMessages");
			if ((propertyValue != null) && (propertyValue.length() > 0))
			{
				String[] propertySplit = propertyValue.split(";");
				if (propertySplit.length > 0)
				{
					for (String value : propertySplit)
					{
						String[] valueSplit = value.split(",");
						if (valueSplit.length != 2)
						{
							_log.warning(Config.class.getName() + StringUtil.concat("[ComboKillLocalAreaMessages]: invalid config property -> \"", value, "\""));
						}
						else
						{
							try
							{
								COMBO_KILL_LOCAL_AREA_MESSAGES.put(Integer.parseInt(valueSplit[0]), valueSplit[1]);
							}
							catch (NumberFormatException nfe)
							{
								if (!value.isEmpty())
								{
									_log.warning(Config.class.getName() + StringUtil.concat("[ComboKillLocalAreaMessages]: invalid config property -> \"", valueSplit[0], "\"", valueSplit[1]));
								}
							}
						}
					}
				}
			}
			
			propertyValue = RankSettings.getProperty("ComboKillGlobalAreaMessages", "");
			if ((propertyValue != null) && (propertyValue.length() > 0))
			{
				String[] propertySplit = RankSettings.getProperty("ComboKillGlobalAreaMessages").split(";");
				if (propertySplit.length > 0)
				{
					for (String value : propertySplit)
					{
						String[] valueSplit = value.split(",");
						if (valueSplit.length != 2)
						{
							_log.warning(Config.class.getName() + StringUtil.concat("[ComboKillGlobalAreaMessages]: invalid config property -> \"", value, "\""));
						}
						else
						{
							try
							{
								COMBO_KILL_GLOBAL_AREA_MESSAGES.put(Integer.parseInt(valueSplit[0]), valueSplit[1]);
							}
							catch (NumberFormatException nfe)
							{
								if (!value.isEmpty())
								{
									_log.warning(Config.class.getName() + StringUtil.concat("[ComboKillGlobalAreaMessages]: invalid config property -> \"", valueSplit[0], "\"", valueSplit[1]));
								}
							}
						}
					}
				}
			}
			
			COMBO_KILL_ALT_MESSAGES_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("ComboKillAltMessagesEnabled", "false"));
			COMBO_KILL_ALT_MESSAGE = RankSettings.getProperty("ComboKillAltMessage", "%killer% have %combo_level% Combo kills!");
			COMBO_KILL_ALT_GLOBAL_MESSAGE_LVL = Integer.parseInt(RankSettings.getProperty("ComboKillAltGlobalMessageMinLvl", "0"));
			
			COMBO_KILL_DEFEAT_MESSAGE_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("ComboKillDefeatMessageEnabled", "true"));
			COMBO_KILL_DEFEAT_MESSAGE_MIN_LVL = Integer.parseInt(RankSettings.getProperty("ComboKillDefeatMessageMinComboLvl", "0"));
			COMBO_KILL_DEFEAT_MESSAGE = RankSettings.getProperty("ComboKillDefeatMessage", "%killer% is defeated with %combo_level% combo lvl!!!");
			
			COMBO_KILL_RESETER = Integer.parseInt(RankSettings.getProperty("ComboKillReseter", "0"));
			COMBO_KILL_RANK_POINTS_RATIO_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("ComboKillRankPointsRatioEnabled", "false"));
			
			propertyValue = RankSettings.getProperty("ComboKillRankPointsRatio", "");
			if ((propertyValue != null) && (propertyValue.length() > 0))
			{
				String[] propertySplit = RankSettings.getProperty("ComboKillRankPointsRatio").split(";");
				if (propertySplit.length > 0)
				{
					for (String value : propertySplit)
					{
						String[] valueSplit = value.split(",");
						if (valueSplit.length != 2)
						{
							_log.warning(Config.class.getName() + StringUtil.concat("[ComboKillRankPointsRatio]: invalid config property -> \"", value, "\""));
						}
						else
						{
							try
							{
								COMBO_KILL_RANK_POINTS_RATIO.put(Integer.parseInt(valueSplit[0]), Double.parseDouble(valueSplit[1]));
							}
							catch (NumberFormatException nfe)
							{
								if (!value.isEmpty())
								{
									_log.warning(Config.class.getName() + StringUtil.concat("[ComboKillRankPointsRatio]: invalid config property -> \"", valueSplit[0], "\"", valueSplit[1]));
								}
							}
						}
					}
				}
			}
			
			// additional security for combo kill system:
			if ((COMBO_KILL_LOCAL_AREA_MESSAGES.size() == 0) && (COMBO_KILL_GLOBAL_AREA_MESSAGES.size() == 0))
			{
				COMBO_KILL_ENABLED = false;
			}
			
			int i = 0;
			String tempStr = RankSettings.getProperty("AllowedZonesIds");
			if ((tempStr != null) && (tempStr.length() > 0))
			{
				for (String rZoneId : tempStr.split(","))
				{
					try
					{
						ALLOWED_ZONES_IDS.add(i, Integer.parseInt(rZoneId));
					}
					catch (Exception e)
					{
						_log.warning(Config.class.getName() + e.getMessage());
					}
					i++;
				}
			}
			
			i = 0;
			tempStr = RankSettings.getProperty("RestrictedZonesIds");
			if ((tempStr != null) && (tempStr.length() > 0))
			{
				for (String rZoneId : tempStr.split(","))
				{
					try
					{
						RESTRICTED_ZONES_IDS.add(i, Integer.parseInt(rZoneId));
					}
					catch (Exception e)
					{
						_log.warning(Config.class.getName() + e.getMessage());
					}
					i++;
				}
			}
			LEGAL_KILL_MIN_LVL = Integer.parseInt(RankSettings.getProperty("LegalKillMinLvl", "1"));
			LEGAL_KILL_FOR_PK_KILLER_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("LegalKillForPkKillerEnabled", "true"));
			LEGAL_KILL_FOR_INNOCENT_KILL_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("LegalKillForInnocentKillEnabled", "false"));
			PROTECTION_TIME_RESET = Integer.parseInt(RankSettings.getProperty("ProtectionTimeReset", "0"));
			if (PROTECTION_TIME_RESET > 0)
				PROTECTION_TIME_RESET = PROTECTION_TIME_RESET * 60000;
			else if (PROTECTION_TIME_RESET < 0)
				PROTECTION_TIME_RESET = 0;
			
			LEGAL_KILL_PROTECTION = Integer.parseInt(RankSettings.getProperty("LegalKillProtection", "0"));
			DAILY_LEGAL_KILL_PROTECTION = Integer.parseInt(RankSettings.getProperty("DailyLegalKillProtection", "0"));
			
			GM_IGNORE_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("GMIgnoreEnabled", "true"));
			
			PVP_INFO_COMMAND_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("PvpInfoCommandEnabled", "true"));
			PVP_INFO_USER_COMMAND_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("PvpInfoUserCommandEnabled", "false"));
			PVP_INFO_USER_COMMAND_ID = Integer.parseInt(RankSettings.getProperty("PvpInfoUserCommandId", "114"));
			
			PVP_INFO_COMMAND_ON_DEATH_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("PvpInfoCommandShowOnDeathEnabled", "true"));
			DEATH_MANAGER_DETAILS_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("DeathManagerDetailsEnabled", "true"));
			DEATH_MANAGER_SHOW_ITEMS_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("DeathManagerShowItemsEnabled", "true"));
			
			i = 0;
			tempStr = RankSettings.getProperty("DeathManagerRestrictedZonesIds");
			if ((tempStr != null) && (tempStr.length() > 0))
			{
				for (String rZoneId : tempStr.split(","))
				{
					try
					{
						DEATH_MANAGER_RESTRICTED_ZONES_IDS.add(i, Integer.parseInt(rZoneId));
					}
					catch (Exception e)
					{
						_log.warning(Config.class.getName() + e.getMessage());
					}
					i++;
				}
			}
			
			propertyValue = RankSettings.getProperty("RankPointsBonusZonesIds", "");
			if ((propertyValue != null) && (propertyValue.length() > 0))
			{
				String[] propertySplit = RankSettings.getProperty("RankPointsBonusZonesIds", "").split(";");
				if (propertySplit.length > 0)
				{
					for (String value : propertySplit)
					{
						String[] valueSplit = value.split(",");
						if (valueSplit.length != 2)
						{
							_log.warning(Config.class.getName() + StringUtil.concat("[RankPointsBonusZonesIds]: invalid config property -> \"", value, "\""));
						}
						else
						{
							try
							{
								RANK_POINTS_BONUS_ZONES_IDS.put(Integer.parseInt(valueSplit[0]), Double.parseDouble(valueSplit[1]));
							}
							catch (NumberFormatException nfe)
							{
								if (!value.isEmpty())
								{
									_log.warning(Config.class.getName() + StringUtil.concat("[RankPointsBonusZonesIds]: invalid config property -> \"", valueSplit[0], "\"", valueSplit[1]));
								}
							}
						}
					}
				}
			}
			
			TOTAL_KILLS_IN_SHOUT_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("TotalKillsInShoutEnabled", "true"));
			TOTAL_KILLS_IN_PVPINFO_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("TotalKillsInPvpInfoEnabled", "true"));
			TOTAL_KILLS_ON_ME_IN_PVPINFO_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("TotalKillsOnMeInPvpInfoEnabled", "true"));
			SHOW_PLAYER_LEVEL_IN_PVPINFO_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("ShowPlayerLevelInPvpInfoEnabled", "true"));
			
			RPC_EXCHANGE_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RpcExchangeEnabled", "true"));
			RPC_EXCHANGE_CONFIRM_BOX_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RpcExchangeConfirmBoxEnabled", "true"));
			
			ANTI_FARM_CLAN_ALLY_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("AntiFarmClanAllyEnabled", "true"));
			ANTI_FARM_PARTY_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("AntiFarmPartyEnabled", "true"));
			ANTI_FARM_IP_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("AntiFarmIpEnabled", "true"));
			
			PVP_TABLE_UPDATE_INTERVAL = (Integer.parseInt(RankSettings.getProperty("PvpTableUpdateInterval", "1")) * 60000);
			if (PVP_TABLE_UPDATE_INTERVAL < 1)
			{
				_log.warning(Config.class.getName() + StringUtil.concat("[PvpTableUpdateInterval]: invalid config property -> \"", Long.toString(PVP_TABLE_UPDATE_INTERVAL), "\""));
				PVP_TABLE_UPDATE_INTERVAL = 60000;
			}
			RPC_TABLE_FORCE_UPDATE_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("RpcTableForceUpdate", "true"));
			
			// top table update times:
			propertyValue = RankSettings.getProperty("TopTableUpdateTimes", "3:00");
			List<Long> temp_time_list = new ArrayList<>();
			if ((propertyValue != null) && (propertyValue.length() > 0))
			{
				String[] propertySplit = RankSettings.getProperty("TopTableUpdateTimes", "").split(",");
				if (propertySplit.length > 0)
				{
					for (String value : propertySplit)
					{
						String[] hm = value.split(":");
						if (hm.length != 2) // hm table length
						{
							_log.warning(Config.class.getName() + StringUtil.concat("[TopTableUpdateTimes]: invalid config property -> \"", value, "\""));
						}
						else
						{
							try
							{
								// prepare h & m:
								String h_s = hm[0];
								String m_s = hm[1];
								
								if (m_s.length() != 2)
								{
									_log.warning(Config.class.getName() + StringUtil.concat("[TopTableUpdateTimes]: invalid config property -> \"", value, "\" minutes format incorrect [hh:mm]"));
									break;
								}
								
								// check times like: [0]3:15, 3:[0]5.
								if (h_s.startsWith("0") && h_s.length() == 2)
								{
									h_s = h_s.substring(1);
								}
								if (m_s.startsWith("0") && m_s.length() == 2)
								{
									m_s = m_s.substring(1);
								}
								if (h_s.equals("0"))
								{
									h_s = "24";
								}
								
								// calculate all times for times in ms from time 0:00.
								long h = Long.parseLong(h_s);
								long m = Long.parseLong(m_s);
								
								if (h < 0 || m < 0 || h > 24 || m > 59)
								{
									_log.warning(Config.class.getName() + StringUtil.concat("[TopTableUpdateTimes]: invalid config property -> \"", value, "\" minutes format incorrect [hh:mm]"));
									break;
								}
								
								long mili = (h * 60 + m) * 60000 - (3600000); // - 1h
								
								if (mili >= 0 && !temp_time_list.contains(mili))
									temp_time_list.add(mili);
							}
							catch (NumberFormatException nfe)
							{
							}
						}
					}
				}
			}
			
			class LongComparator implements Comparator<Long>
			{
				@Override
				public int compare(Long v1, Long v2)
				{
					if (v1 > v2)
					{
						return 1;
					}
					else if (v1 == v2)
					{
						return 0;
					}
					else
					{
						return -1;
					}
				}
			}
			
			Comparator<Long> comp = new LongComparator();
			Long[] aol = new Long[temp_time_list.size()];
			Arrays.sort(temp_time_list.toArray(aol), comp);
			TOP_TABLE_UPDATE_TIMES = Arrays.asList(aol);
			
			TOP_LIST_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("TopListEnabled", "true"));
			TOP_LIST_IGNORE_TIME_LIMIT = Integer.parseInt(RankSettings.getProperty("TopListIgnoreTimeLimit", "0"));
			if (TOP_LIST_IGNORE_TIME_LIMIT > 0)
			{
				TOP_LIST_IGNORE_TIME_LIMIT *= 86400000; // in milliseconds
			}
			
			SERVER_SIDE_IMAGES_ENABLED = Boolean.parseBoolean(RankSettings.getProperty("ServerSideImagesEnabled", "true"));
			IMAGE_PREFIX = Integer.parseInt(RankSettings.getProperty("ImagePrefix", "1"));
			
			// Buttons style:
			BUTTON_UP = RankSettings.getProperty("ButtonFore", "L2UI_ch3.BigButton3_over");
			BUTTON_DOWN = RankSettings.getProperty("ButtonBack", "L2UI_ch3.BigButton3");
			BUTTON_W = RankSettings.getProperty("ButtonWidth", "134");
			BUTTON_H = RankSettings.getProperty("ButtonHeight", "21");
			BUTTON_BIG_W = RankSettings.getProperty("ButtonBigWidth", "180");
			BUTTON_BIG_H = RankSettings.getProperty("ButtonBigHeight", "24");
			
			if (USE_SAY_FILTER)
			{
				FILTER_LIST.clear();
				try (LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(new File(CHAT_FILTER_FILE)))))
				{
					String line = null;
					while ((line = lnr.readLine()) != null)
					{
						if (line.trim().length() == 0 || line.startsWith("#"))
							continue;
						
						FILTER_LIST.add(line.trim());
					}
					_log.info("Chat Filter: Loaded " + FILTER_LIST.size() + " words ");
				}
				catch (Exception e)
				{
					_log.warning(Config.class.getName() + ": Config: Failed to Load " + CHAT_FILTER_FILE + " File.");
					if (Config.DEVELOPER)
					{
						e.printStackTrace();
					}
				}
			}
			ALLOW_USE_HERO_ITEM_ON_SUBCLASS = Boolean.parseBoolean(L2JHellasSettings.getProperty("AllowUseHeroItemOnSub", "False"));
			MAX_LVL_AFTER_SUB = Boolean.parseBoolean(L2JHellasSettings.getProperty("MaxLvLAfterSub", "False"));
			
			String AioSkillsSplit[] = L2JHellasSettings.getProperty("ClanSkills", "").split(";");
			CLAN_SKILLS = new HashMap<>(AioSkillsSplit.length);
			String arr[] = AioSkillsSplit;
			int len = arr.length;
			for (int ii = 0; ii < len; ii++)
			{
				String skill = arr[ii];
				String skillSplit[] = skill.split(",");
				if (skillSplit.length != 2)
				{
					_log.warning(Config.class.getName() + ": [Clan System]: invalid config property in Mods/L2JHellas.ini -> ClanSkills " + skill);
					continue;
				}
				try
				{
					CLAN_SKILLS.put(Integer.valueOf(Integer.parseInt(skillSplit[0])), Integer.valueOf(Integer.parseInt(skillSplit[1])));
					continue;
				}
				catch (NumberFormatException nfe)
				{
				}
				if (!skill.equals(""))
				{
					_log.warning(Config.class.getName() + ": [Clan System]: invalid config property in Mods/L2JHellas.ini -> ClanSkills " + skillSplit[0] + " " + skillSplit[1]);
				}
			}
			
			CLAN_LEVEL = Byte.parseByte(L2JHellasSettings.getProperty("ClanSetLevel", "8"));
			REPUTATION_QUANTITY = Integer.parseInt(L2JHellasSettings.getProperty("ReputationScore", "10000"));
			
			Properties autoSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(MOD_AUTOMATION_CONFIG_FILE)))
			{
				autoSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + MOD_AUTOMATION_CONFIG_FILE + " settings!");
			}
			ALLOW_SERVER_RESTART_COMMAND = Boolean.parseBoolean(autoSettings.getProperty("AllowServerRestartCommand", "False"));
			VOTES_NEEDED_FOR_RESTART = Integer.parseInt(autoSettings.getProperty("VotesNeededForRestart", "20"));
			RESTART_BY_TIME_OF_DAY = Boolean.parseBoolean(autoSettings.getProperty("EnableRestartSystem", "false"));
			RESTART_SECONDS = Integer.parseInt(autoSettings.getProperty("RestartSeconds", "360"));
			RESTART_INTERVAL_BY_TIME_OF_DAY = (autoSettings.getProperty("RestartByTimeOfDay", "23:59").split(","));
			ENABLE_GUI = Boolean.parseBoolean(autoSettings.getProperty("EnableGui", "True"));
			LOGIN_SERVER_SCHEDULE_RESTART = Boolean.parseBoolean(autoSettings.getProperty("LoginRestartSchedule", "False"));
			LOGIN_SERVER_SCHEDULE_RESTART_TIME = Long.parseLong(autoSettings.getProperty("LoginRestartTime", "24"));
			ALLOW_SEQURITY_QUE = Boolean.valueOf(autoSettings.getProperty("AllowSecurityQuestion", "True"));
			SECURITY_QUE_TIME = Integer.parseInt(autoSettings.getProperty("SecurityTime", "20"));
			ALLOW_HOPZONE_VOTE_REWARD = Boolean.valueOf(autoSettings.getProperty("AllowHopzoneVoteReward", "false"));
			HOPZONE_SERVER_LINK = autoSettings.getProperty("HopzoneServerLink", "null");
			HOPZONE_VOTES_DIFFERENCE = Integer.parseInt(autoSettings.getProperty("HopzoneVotesDifference", "5"));
			HOPZONE_REWARD_CHECK_TIME = Integer.parseInt(autoSettings.getProperty("HopzoneRewardCheckTime", "5"));
			HOPZONE_REWARD = parseItemsList(autoSettings.getProperty("HopzoneReward", "57,100000000"));
			HOPZONE_BOXES_ALLOWED = Integer.parseInt(autoSettings.getProperty("HopzoneDualboxesAllowed", "1"));
			ALLOW_TOPZONE_VOTE_REWARD = Boolean.valueOf(autoSettings.getProperty("AllowTopzoneVoteReward", "false"));
			TOPZONE_SERVER_LINK = autoSettings.getProperty("TopzoneServerLink", "null");
			TOPZONE_VOTES_DIFFERENCE = Integer.parseInt(autoSettings.getProperty("TopzoneVotesDifference", "5"));
			TOPZONE_REWARD_CHECK_TIME = Integer.parseInt(autoSettings.getProperty("TopzoneRewardCheckTime", "5"));
			TOPZONE_REWARD = parseItemsList(autoSettings.getProperty("TopzoneReward", "57,100000000"));
			TOPZONE_BOXES_ALLOWED = Integer.parseInt(autoSettings.getProperty("TopzoneDualboxesAllowed", "1"));
			ALLOW_PRIVATE_ANTI_BOT = Boolean.valueOf(autoSettings.getProperty("AllowPrivateAntiBot", "False"));
			ENCHANT_BOT_CHANCE = Integer.parseInt(autoSettings.getProperty("PrivateBotChance", "15"));
			
			Properties smartCBSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(MOD_SMART_CB_CONFIG_FILE)))
			{
				smartCBSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + MOD_SMART_CB_CONFIG_FILE + " settings!");
			}
			TOP_PLAYER_ROW_HEIGHT = Integer.parseInt(smartCBSettings.getProperty("TopPlayerRowHeight", "19"));
			TOP_PLAYER_RESULTS = Integer.parseInt(smartCBSettings.getProperty("TopPlayerResults", "20"));
			RAID_LIST_ROW_HEIGHT = Integer.parseInt(smartCBSettings.getProperty("RaidListRowHeight", "18"));
			RAID_LIST_RESULTS = Integer.parseInt(smartCBSettings.getProperty("RaidListResults", "20"));
			RAID_LIST_SORT_ASC = Boolean.parseBoolean(smartCBSettings.getProperty("RaidListSortAsc", "True"));
			ALLOW_REAL_ONLINE_STATS = Boolean.parseBoolean(smartCBSettings.getProperty("AllowRealOnlineStats", "True"));
			ALLOW_CLASS_MASTERSCB = smartCBSettings.getProperty("AllowClassMastersCB", "0");
			CLASS_MASTERS_PRICE_ITEMCB = Integer.parseInt(smartCBSettings.getProperty("ClassMastersPriceItemCB", "57"));
			if ((ALLOW_CLASS_MASTERSCB.length() != 0) && (!ALLOW_CLASS_MASTERSCB.equals("0")))
				for (String classid : ALLOW_CLASS_MASTERSCB.split(","))
					ALLOW_CLASS_MASTERS_LISTCB.add(Integer.valueOf(Integer.parseInt(classid)));
			CLASS_MASTERS_PRICECB = smartCBSettings.getProperty("ClassMastersPriceCB", "0,0,0");
			if (CLASS_MASTERS_PRICECB.length() >= 5)
			{
				int level = 0;
				for (String classid : CLASS_MASTERS_PRICECB.split(","))
				{
					CLASS_MASTERS_PRICE_LISTCB[level] = Integer.parseInt(classid);
					level++;
				}
			}
			
			Properties CustomNpcSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(MOD_CUSTOM_NPC_CONFIG_FILE)))
			{
				CustomNpcSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + MOD_CUSTOM_NPC_CONFIG_FILE + " settings!");
			}
			ENABLE_CACHE_INFO = Boolean.parseBoolean(CustomNpcSettings.getProperty("EnableCacheInfo", "False"));
			SHOW_NPC_CREST = Boolean.parseBoolean(CustomNpcSettings.getProperty("ShowNpcCrest", "False"));
			PROTECTOR_PLAYER_PK = Boolean.parseBoolean(CustomNpcSettings.getProperty("ProtectorPlayerPK", "false"));
			PROTECTOR_PLAYER_PVP = Boolean.parseBoolean(CustomNpcSettings.getProperty("ProtectorPlayerPVP", "false"));
			PROTECTOR_RADIUS_ACTION = Integer.parseInt(CustomNpcSettings.getProperty("ProtectorRadiusAction", "500"));
			PROTECTOR_SKILLID = Integer.parseInt(CustomNpcSettings.getProperty("ProtectorSkillId", "1069"));
			PROTECTOR_SKILLLEVEL = Integer.parseInt(CustomNpcSettings.getProperty("ProtectorSkillLevel", "42"));
			PROTECTOR_SKILLTIME = Integer.parseInt(CustomNpcSettings.getProperty("ProtectorSkillTime", "800"));
			SEND_MESSAGE = Boolean.parseBoolean(CustomNpcSettings.getProperty("SendMessage", "false"));
			PROTECTOR_MESSAGE = CustomNpcSettings.getProperty("ProtectorMessage", "Protector, not spawnkilling here, go read the rules !!!");
			NPCBUFFER_FEATURE_ENABLED = Boolean.valueOf(CustomNpcSettings.getProperty("NPCBufferEnabled", "False"));
			NPCBUFFER_STATIC_BUFF_COST = Integer.parseInt(CustomNpcSettings.getProperty("NPCBufferStaticCostPerBuff", "-1"));
			ALLOW_CLASS_MASTER = Boolean.valueOf(CustomNpcSettings.getProperty("AllowClassMaster", "False"));
			ALLOW_REMOTE_CLASS_MASTER = Boolean.valueOf(CustomNpcSettings.getProperty("AllowRemoteClassMaster", "False"));
			CLASS_AUTO_EQUIP_AW = Boolean.parseBoolean(CustomNpcSettings.getProperty("AutoEquipArmorWeapons", "False"));
			NPC_NOBLES_ENABLE = Boolean.parseBoolean(CustomNpcSettings.getProperty("NobleManager", "false"));
			NPC_NOBLESS_ID = Integer.parseInt(CustomNpcSettings.getProperty("NobleID", "57"));
			NPC_NOBLESS_QUANTITY = Integer.parseInt(CustomNpcSettings.getProperty("NobleQuantity", "10000"));
			
			String[] notenchantable = CustomNpcSettings.getProperty("BossList", "29028,29019,29020,29022,29001,29014,29006").split(",");
			BOSS_RESPAWN_INFO = new int[notenchantable.length];
			for (int info = 0; info < notenchantable.length; info++)
				BOSS_RESPAWN_INFO[info] = Integer.parseInt(notenchantable[info]);
			Arrays.sort(BOSS_RESPAWN_INFO);
			RAID_INFO_SHOW_TIME = Boolean.parseBoolean(CustomNpcSettings.getProperty("InfoShowTime", "False"));
			
			Properties OlySettings = new Properties();
			try (InputStream is = new FileInputStream(new File(OLYMPIAD_FILE)))
			{
				OlySettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + OLYMPIAD_FILE + " settings!");
			}
			
			ALT_OLY_START_TIME = Integer.parseInt(OlySettings.getProperty("AltOlyStartTime", "18"));
			ALT_OLY_MIN = Integer.parseInt(OlySettings.getProperty("AltOlyMin", "00"));
			ALT_OLY_CPERIOD = Long.parseLong(OlySettings.getProperty("AltOlyCPeriod", "21600000"));
			ALT_OLY_BATTLE = Long.parseLong(OlySettings.getProperty("AltOlyBattle", "180000"));
			ALT_OLY_WPERIOD = Long.parseLong(OlySettings.getProperty("AltOlyWPeriod", "604800000"));
			ALT_OLY_VPERIOD = Long.parseLong(OlySettings.getProperty("AltOlyVPeriod", "86400000"));
			ALT_OLY_WAIT_TIME = Integer.parseInt(OlySettings.getProperty("AltOlyWaitTime", "30"));
			ALT_OLY_START_POINTS = Integer.parseInt(OlySettings.getProperty("AltOlyStartPoints", "18"));
			ALT_OLY_WEEKLY_POINTS = Integer.parseInt(OlySettings.getProperty("AltOlyWeeklyPoints", "3"));
			ALT_OLY_MIN_MATCHES = Integer.parseInt(OlySettings.getProperty("AltOlyMinMatchesToBeClassed", "5"));
			ALT_OLY_CLASSED = Integer.parseInt(OlySettings.getProperty("AltOlyClassedParticipants", "5"));
			ALT_OLY_NONCLASSED = Integer.parseInt(OlySettings.getProperty("AltOlyNonClassedParticipants", "9"));
			ALT_OLY_CLASSED_REWARD = parseItemsList(OlySettings.getProperty("AltOlyClassedReward", "6651,50"));
			ALT_OLY_NONCLASSED_REWARD = parseItemsList(OlySettings.getProperty("AltOlyNonClassedReward", "6651,30"));
			ALT_OLY_COMP_RITEM = Integer.parseInt(OlySettings.getProperty("AltOlyCompRewItem", "6651"));
			ALT_OLY_GP_PER_POINT = Integer.parseInt(OlySettings.getProperty("AltOlyGPPerPoint", "1000"));
			ALT_OLY_HERO_POINTS = Integer.parseInt(OlySettings.getProperty("AltOlyHeroPoints", "300"));
			ALT_OLY_RANK1_POINTS = Integer.parseInt(OlySettings.getProperty("AltOlyRank1Points", "100"));
			ALT_OLY_RANK2_POINTS = Integer.parseInt(OlySettings.getProperty("AltOlyRank2Points", "75"));
			ALT_OLY_RANK3_POINTS = Integer.parseInt(OlySettings.getProperty("AltOlyRank3Points", "55"));
			ALT_OLY_RANK4_POINTS = Integer.parseInt(OlySettings.getProperty("AltOlyRank4Points", "40"));
			ALT_OLY_RANK5_POINTS = Integer.parseInt(OlySettings.getProperty("AltOlyRank5Points", "30"));
			ALT_OLY_MAX_POINTS = Integer.parseInt(OlySettings.getProperty("AltOlyMaxPoints", "10"));
			ALT_OLY_DIVIDER_CLASSED = Integer.parseInt(OlySettings.getProperty("AltOlyDividerClassed", "3"));
			ALT_OLY_DIVIDER_NON_CLASSED = Integer.parseInt(OlySettings.getProperty("AltOlyDividerNonClassed", "3"));
			ALT_OLY_ANNOUNCE_GAMES = Boolean.parseBoolean(OlySettings.getProperty("AltOlyAnnounceGames", "True"));
			OLY_ENCHANT_LIMIT = Integer.parseInt(OlySettings.getProperty("OlyMaxEnchant", "-1"));
			OLY_SAME_IP = Boolean.parseBoolean(OlySettings.getProperty("OlySameIp", "True"));
			OLY_RESTRICTED_ITEMS_LIST = new ArrayList<>();
			for (String olyId : OlySettings.getProperty("OlyRestrictedItems", "0").split(","))
			{
				OLY_RESTRICTED_ITEMS_LIST.add(Integer.parseInt(olyId));
			}
			
			Properties geoSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(GEO_FILE)))
			{
				geoSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + GEO_FILE + " settings!");
			}
			GEODATA = Boolean.parseBoolean(geoSettings.getProperty("GeoData", "false"));
			GEO_X_FIRST = Integer.parseInt(geoSettings.getProperty("GeoFirstX", "15"));
			GEO_Y_FIRST = Integer.parseInt(geoSettings.getProperty("GeoFirstY", "10"));
			GEO_X_LAST = Integer.parseInt(geoSettings.getProperty("GeoLastX", "26"));
			GEO_Y_LAST = Integer.parseInt(geoSettings.getProperty("GeoLastY", "26"));
			
			CONTROL_HEIGHT_DAMAGE = Boolean.parseBoolean(geoSettings.getProperty("ControlHeightDamage", "true"));
			PATH_CLEAN = Boolean.parseBoolean(geoSettings.getProperty("PathClean", "true"));
			ALLOW_DOORS = Boolean.parseBoolean(geoSettings.getProperty("AllowDoors", "false"));
			SIMPLE_PATHFIND_FOR_MOBS = Boolean.parseBoolean(geoSettings.getProperty("SimplePathFindForMobs", "true"));
			ALLOW_FALL_FROM_WALLS = Boolean.parseBoolean(geoSettings.getProperty("AllowFallFromWalls", "false"));
			PATHFIND_BOOST = Integer.parseInt(geoSettings.getProperty("PathFindBoost", "2"));
			PATHFIND_DIAGONAL = Boolean.parseBoolean(geoSettings.getProperty("PathFindDiagonal", "true"));
			PATHFIND_MAX_Z_DIFF = Integer.parseInt(geoSettings.getProperty("PathFindMaxZDiff", "32"));
			MAX_Z_DIFF = Integer.parseInt(geoSettings.getProperty("MaxZDiff", "64"));
			MIN_LAYER_HEIGHT = Integer.parseInt(geoSettings.getProperty("MinLayerHeight", "64"));
			WEIGHT0 = Double.parseDouble(geoSettings.getProperty("Weight0", "0.5"));
			WEIGHT1 = Double.parseDouble(geoSettings.getProperty("Weight1", "2.0"));
			WEIGHT2 = Double.parseDouble(geoSettings.getProperty("Weight2", "1.0"));
			PathFindBuffers.initBuffers(geoSettings.getProperty("PathFindBuffers", "8x100;8x128;8x192;4x256;2x320;2x384;1x500"));
			COMPACT_GEO = Boolean.parseBoolean(geoSettings.getProperty("CompactGeoData", "false"));
			COORD_SYNCHRONIZE = Integer.parseInt(geoSettings.getProperty("CoordSynchronize", "-1"));
			
			Properties optionsSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(OPTIONS_FILE)))
			{
				optionsSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + OPTIONS_FILE + " settings!");
			}
			ZONE_TOWN = Integer.parseInt(optionsSettings.getProperty("ZoneTown", "0"));
			ALLOW_TUTORIAL = Boolean.valueOf(optionsSettings.getProperty("AllowTutorial", "True"));
			DEFAULT_GLOBAL_CHAT = optionsSettings.getProperty("GlobalChat", "GLOBAL");
			DEFAULT_TRADE_CHAT = optionsSettings.getProperty("TradeChat", "ON");
			DEFAULT_PUNISH = Integer.parseInt(optionsSettings.getProperty("DefaultPunish", "2"));
			DEFAULT_PUNISH_PARAM = Integer.parseInt(optionsSettings.getProperty("DefaultPunishParam", "0"));
			BYPASS_VALIDATION = Boolean.valueOf(optionsSettings.getProperty("BypassValidation", "True"));
			DELETE_DAYS = Integer.parseInt(optionsSettings.getProperty("DeleteCharAfterDays", "7"));
			ALLOW_DISCARDITEM = Boolean.valueOf(optionsSettings.getProperty("AllowDiscardItem", "True"));
			AUTODESTROY_ITEM_AFTER = Integer.parseInt(optionsSettings.getProperty("AutoDestroyDroppedItemAfter", "0"));
			PROTECTED_ITEMS = optionsSettings.getProperty("ListOfProtectedItems");
			LIST_PROTECTED_ITEMS = new ArrayList<>();
			for (String listid : PROTECTED_ITEMS.split(","))
			{
				LIST_PROTECTED_ITEMS.add(Integer.parseInt(listid));
			}
			DESTROY_DROPPED_PLAYER_ITEM = Boolean.valueOf(optionsSettings.getProperty("DestroyPlayerDroppedItem", "True"));
			DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.valueOf(optionsSettings.getProperty("DestroyEquipableItem", "False"));
			SAVE_DROPPED_ITEM = Boolean.valueOf(optionsSettings.getProperty("SaveDroppedItem", "False"));
			EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.valueOf(optionsSettings.getProperty("EmptyDroppedItemTableAfterLoad", "False"));
			SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(optionsSettings.getProperty("SaveDroppedItemInterval", "0")) * 60000;
			CLEAR_DROPPED_ITEM_TABLE = Boolean.valueOf(optionsSettings.getProperty("ClearDroppedItemTable", "False"));
			AUTODELETE_INVALID_QUEST_DATA = Boolean.valueOf(optionsSettings.getProperty("AutoDeleteInvalidQuestData", "False"));
			MULTIPLE_ITEM_DROP = Boolean.valueOf(optionsSettings.getProperty("MultipleItemDrop", "True"));
			FORCE_INVENTORY_UPDATE = Boolean.valueOf(optionsSettings.getProperty("ForceInventoryUpdate", "False"));
			MAX_DRIFT_RANGE = Integer.parseInt(optionsSettings.getProperty("MaxDriftRange", "300"));
			MIN_NPC_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MinNPCAnimation", "10"));
			MAX_NPC_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MaxNPCAnimation", "20"));
			MIN_MONSTER_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MinMonsterAnimation", "5"));
			MAX_MONSTER_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MaxMonsterAnimation", "20"));
			SERVER_NEWS = Boolean.valueOf(optionsSettings.getProperty("ShowServerNews", "False"));
			SHOW_NPC_LVL = Boolean.valueOf(optionsSettings.getProperty("ShowNpcLevel", "False"));
			ALLOW_WAREHOUSE = Boolean.valueOf(optionsSettings.getProperty("AllowWarehouse", "True"));
			WAREHOUSE_CACHE = Boolean.valueOf(optionsSettings.getProperty("WarehouseCache", "False"));
			WAREHOUSE_CACHE_TIME = Integer.parseInt(optionsSettings.getProperty("WarehouseCacheTime", "15"));
			ALLOW_FREIGHT = Boolean.valueOf(optionsSettings.getProperty("AllowFreight", "True"));
			ALLOW_WEAR = Boolean.valueOf(optionsSettings.getProperty("AllowWear", "False"));
			WEAR_DELAY = Integer.parseInt(optionsSettings.getProperty("WearDelay", "5"));
			WEAR_PRICE = Integer.parseInt(optionsSettings.getProperty("WearPrice", "10"));
			ALLOW_LOTTERY = Boolean.valueOf(optionsSettings.getProperty("AllowLottery", "False"));
			ALLOW_RACE = Boolean.valueOf(optionsSettings.getProperty("AllowRace", "False"));
			ALLOW_WATER = Boolean.valueOf(optionsSettings.getProperty("AllowWater", "False"));
			ALLOW_RENTPET = Boolean.valueOf(optionsSettings.getProperty("AllowRentPet", "False"));
			ALLOWFISHING = Boolean.valueOf(optionsSettings.getProperty("AllowFishing", "False"));
			ALLOW_BOAT = Boolean.valueOf(optionsSettings.getProperty("AllowBoat", "False"));
			ALLOW_CURSED_WEAPONS = Boolean.valueOf(optionsSettings.getProperty("AllowCursedWeapons", "False"));
			ALLOW_MANOR = Boolean.parseBoolean(optionsSettings.getProperty("AllowManor", "False"));
			ALLOW_NPC_WALKERS = Boolean.valueOf(optionsSettings.getProperty("AllowNpcWalkers", "True"));
			GM_EDIT = Boolean.valueOf(optionsSettings.getProperty("GMEdit", "False"));
			ONLY_GM_ITEMS_FREE = Boolean.valueOf(optionsSettings.getProperty("OnlyGMItemsFree", "True"));
			RAID_DISABLE_CURSE = Boolean.parseBoolean(optionsSettings.getProperty("DisableRaidCurse", "False"));
			COMMUNITY_TYPE = optionsSettings.getProperty("CommunityType", "old").toLowerCase();
			BBS_DEFAULT = optionsSettings.getProperty("BBSDefault", "_bbshome");
			SHOW_LEVEL_COMMUNITYBOARD = Boolean.valueOf(optionsSettings.getProperty("ShowLevelOnCommunityBoard", "False"));
			SHOW_STATUS_COMMUNITYBOARD = Boolean.valueOf(optionsSettings.getProperty("ShowStatusOnCommunityBoard", "True"));
			NAME_PAGE_SIZE_COMMUNITYBOARD = Integer.parseInt(optionsSettings.getProperty("NamePageSizeOnCommunityBoard", "50"));
			NAME_PER_ROW_COMMUNITYBOARD = Integer.parseInt(optionsSettings.getProperty("NamePerRowOnCommunityBoard", "5"));
			AI_MAX_THREAD = Integer.parseInt(optionsSettings.getProperty("AiMaxThread", "10"));
			GRIDS_ALWAYS_ON = Boolean.parseBoolean(optionsSettings.getProperty("GridsAlwaysOn", "False"));
			GRID_NEIGHBOR_TURNON_TIME = Integer.parseInt(optionsSettings.getProperty("GridNeighborTurnOnTime", "30"));
			GRID_NEIGHBOR_TURNOFF_TIME = Integer.parseInt(optionsSettings.getProperty("GridNeighborTurnOffTime", "300"));
			
			ROLL_DICE = Integer.valueOf(optionsSettings.getProperty("FloodRollDice", "100"));
			HERO_VOICE = Integer.valueOf(optionsSettings.getProperty("FloodHeroVoice", "100"));
			SUBCLASS = Integer.valueOf(optionsSettings.getProperty("FloodSubclass", "100"));
			DROP_ITEM = Integer.valueOf(optionsSettings.getProperty("FloodDropItem", "100"));
			SERVER_BYPASS = Integer.valueOf(optionsSettings.getProperty("FloodServerBypass", "100"));
			MULTISELL = Integer.valueOf(optionsSettings.getProperty("FloodMultisell", "100"));
			MANUFACTURE = Integer.valueOf(optionsSettings.getProperty("FloodManufacture", "100"));
			MANOR = Integer.valueOf(optionsSettings.getProperty("FloodManor", "100"));
			SENDMAIL = Integer.valueOf(optionsSettings.getProperty("FloodSendMail", "100"));
			CHARACTER_SELECT = Integer.valueOf(optionsSettings.getProperty("FloodCharacterSelect", "100"));
			GLOBAL_CHAT = Integer.valueOf(optionsSettings.getProperty("FloodGlobalChat", "100"));
			TRADE_CHAT = Integer.valueOf(optionsSettings.getProperty("FloodTradeChat", "100"));
			USE_ITEM = Integer.valueOf(optionsSettings.getProperty("FloodUseItem", "100"));
			SOCIAL = Integer.valueOf(optionsSettings.getProperty("FloodSocial", "100"));
			
			Properties otherSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(OTHER_CONFIG_FILE)))
			{
				otherSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + OTHER_CONFIG_FILE + " settings!");
			}
			STARTING_ADENA = Integer.parseInt(otherSettings.getProperty("StartingAdena", "100"));
			STARTING_ANCIENT = Integer.parseInt(otherSettings.getProperty("StartingAncientAdena", "100"));
			WYVERN_SPEED = Integer.parseInt(otherSettings.getProperty("WyvernSpeed", "100"));
			STRIDER_SPEED = Integer.parseInt(otherSettings.getProperty("StriderSpeed", "80"));
			ALLOW_WYVERN_UPGRADER = Boolean.valueOf(otherSettings.getProperty("AllowWyvernUpgrader", "False"));
			EFFECT_CANCELING = Boolean.valueOf(otherSettings.getProperty("CancelLesserEffect", "True"));
			ALLOW_GUARDS = Boolean.valueOf(otherSettings.getProperty("AllowGuards", "False"));
			DEEPBLUE_DROP_RULES = Boolean.parseBoolean(otherSettings.getProperty("UseDeepBlueDropRules", "True"));
			INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForNoDwarf", "80"));
			INVENTORY_MAXIMUM_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForDwarf", "100"));
			INVENTORY_MAXIMUM_GM = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForGMPlayer", "250"));
			WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForNoDwarf", "100"));
			WAREHOUSE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForDwarf", "120"));
			WAREHOUSE_SLOTS_CLAN = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForClan", "150"));
			FREIGHT_SLOTS = Integer.parseInt(otherSettings.getProperty("MaximumFreightSlots", "20"));
			
			AUGMENTATION_NG_SKILL_CHANCE = Integer.parseInt(otherSettings.getProperty("AugmentationNGSkillChance", "15"));
			AUGMENTATION_MID_SKILL_CHANCE = Integer.parseInt(otherSettings.getProperty("AugmentationMidSkillChance", "30"));
			AUGMENTATION_HIGH_SKILL_CHANCE = Integer.parseInt(otherSettings.getProperty("AugmentationHighSkillChance", "45"));
			AUGMENTATION_TOP_SKILL_CHANCE = Integer.parseInt(otherSettings.getProperty("AugmentationTopSkillChance", "60"));
			AUGMENTATION_BASESTAT_CHANCE = Integer.parseInt(otherSettings.getProperty("AugmentationBaseStatChance", "1"));
			
			AUGMENTATION_NG_GLOW_CHANCE = Integer.parseInt(otherSettings.getProperty("AugmentationNGGlowChance", "0"));
			AUGMENTATION_MID_GLOW_CHANCE = Integer.parseInt(otherSettings.getProperty("AugmentationMidGlowChance", "40"));
			AUGMENTATION_HIGH_GLOW_CHANCE = Integer.parseInt(otherSettings.getProperty("AugmentationHighGlowChance", "70"));
			AUGMENTATION_TOP_GLOW_CHANCE = Integer.parseInt(otherSettings.getProperty("AugmentationTopGlowChance", "100"));
			
			HP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("HpRegenMultiplier", "100")) / 100;
			MP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("MpRegenMultiplier", "100")) / 100;
			CP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("CpRegenMultiplier", "100")) / 100;
			RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidHpRegenMultiplier", "100")) / 100;
			RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidMpRegenMultiplier", "100")) / 100;
			RAID_P_DEFENCE_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidPhysicalDefenceMultiplier", "100")) / 100;
			RAID_M_DEFENCE_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidMagicalDefenceMultiplier", "100")) / 100;
			RAID_MIN_RESPAWN_MULTIPLIER = Float.parseFloat(otherSettings.getProperty("RaidMinRespawnMultiplier", "1.0"));
			RAID_MAX_RESPAWN_MULTIPLIER = Float.parseFloat(otherSettings.getProperty("RaidMaxRespawnMultiplier", "1.0"));
			RAID_MINION_RESPAWN_TIMER = Integer.parseInt(otherSettings.getProperty("RaidMinionRespawnTime", "300000"));
			RB_IMMEDIATE_INFORM = Boolean.parseBoolean(otherSettings.getProperty("RaidImmediateDBupdate", "True"));
			PLAYER_SPAWN_PROTECTION = Integer.parseInt(otherSettings.getProperty("PlayerSpawnProtection", "0"));
			UNSTUCK_INTERVAL = Integer.parseInt(otherSettings.getProperty("UnstuckInterval", "300"));
			RESPAWN_RESTORE_CP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreCP", "0")) / 100;
			RESPAWN_RESTORE_HP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreHP", "70")) / 100;
			RESPAWN_RESTORE_MP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreMP", "70")) / 100;
			RESPAWN_RANDOM_ENABLED = Boolean.parseBoolean(otherSettings.getProperty("RespawnRandomInTown", "False"));
			RESPAWN_RANDOM_MAX_OFFSET = Integer.parseInt(otherSettings.getProperty("RespawnRandomMaxOffset", "50"));
			MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsDwarf", "5"));
			MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsOther", "4"));
			STORE_SKILL_COOLTIME = Boolean.parseBoolean(otherSettings.getProperty("StoreSkillCooltime", "True"));
			PET_RENT_NPC = otherSettings.getProperty("ListPetRentNpc", "30827");
			LIST_PET_RENT_NPC = new ArrayList<>();
			for (String listid : PET_RENT_NPC.split(","))
			{
				LIST_PET_RENT_NPC.add(Integer.parseInt(listid));
			}
			ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(otherSettings.getProperty("AnnounceMammonSpawn", "True"));
			JAIL_DISABLE_CHAT = Boolean.valueOf(otherSettings.getProperty("JailDisableChat", "True"));
			DEATH_PENALTY_CHANCE = Integer.parseInt(otherSettings.getProperty("DeathPenaltyChance", "20"));
			
			// Configuration values not found in config files
			MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));
			
			Properties EventZodiacSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(EVENT_ZODIAC_CONFIG_FILE)))
			{
				EventZodiacSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + EVENT_ZODIAC_CONFIG_FILE + " settings!");
			}
			ZODIAC_ENABLE = Boolean.parseBoolean(EventZodiacSettings.getProperty("Zodiac", "true"));
			ZODIAC_REWARD = Integer.parseInt(EventZodiacSettings.getProperty("ZodiadReward", "3470"));
			ZODIAC_REWARD_COUN = Integer.parseInt(EventZodiacSettings.getProperty("ZodiacdRewardc", "1"));
			ZODIAC_VOTE_MINUTES = Integer.parseInt(EventZodiacSettings.getProperty("VotingMin", "5"));
			SAME_IP_ZODIAC = Boolean.parseBoolean(EventZodiacSettings.getProperty("SameIpProtection", "true"));
			INITIAL_START = Integer.parseInt(EventZodiacSettings.getProperty("MinutesInitial", "10"));
			TIME_TO_REGISTER = Integer.parseInt(EventZodiacSettings.getProperty("RegisterTime", "10"));
			BETWEEN_EVENTS = Integer.parseInt(EventZodiacSettings.getProperty("MinutesAfterEvent", "60"));
			
			Properties EventTVTSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(EVENT_TVT_CONFIG_FILE)))
			{
				EventTVTSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + EVENT_TVT_CONFIG_FILE + " settings!");
			}
			TVT_ALLOW_AUTOEVENT = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTAutomatedEvent", "true"));
			TVT_EVENT_TIMES = EventTVTSettings.getProperty("TvTStartUpTimes", "17:00,18:00,19:00");
			FIRST_TVT_DELAY = Integer.parseInt(EventTVTSettings.getProperty("FirstEventDelay", "10"));
			TVT_AURA = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTAura", "true"));
			TVT_JOIN_CURSED = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTJoinWithCursedWeapon", "true"));
			TVT_PRICE_NO_KILLS = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTPriceNoKills", "false"));
			TVT_ALLOW_ENEMY_HEALING = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTAllowEnemyHealing", "false"));
			TVT_ALLOW_TEAM_CASTING = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTAllowTeamCasting", "false"));
			TVT_ALLOW_TEAM_ATTACKING = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTAllowTeamAttacking", "false"));
			TVT_CLOSE_COLISEUM_DOORS = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTCloseColiseumDoors", "false"));
			TVT_ALLOW_INTERFERENCE = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTAllowInterference", "false"));
			TVT_ALLOW_POTIONS = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTAllowPotions", "false"));
			TVT_ALLOW_SUMMON = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTAllowSummon", "false"));
			TVT_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTOnStartRemoveAllEffects", "true"));
			TVT_ON_START_UNSUMMON_PET = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTOnStartUnsummonPet", "true"));
			TVT_REVIVE_RECOVERY = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTReviveRecovery", "false"));
			TVT_ANNOUNCE_TEAM_STATS = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTAnnounceTeamStats", "false"));
			TVT_EVEN_TEAMS = EventTVTSettings.getProperty("TvTEvenTeams", "BALANCE");
			TVT_ANNOUNCE_SIGNUPS = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTAnnounceSignUp", "false"));
			TVT_ANNOUNCE_REGISTRATION_LOC_NPC = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTAnnounceLocNpc", "true"));
			TVT_ANNOUNCE_TEAM_STATS = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTAnnounceTeamStats", "false"));
			TVT_ANNOUNCE_REWARD = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTAnnounceReward", "false"));
			TVT_PRICE_NO_KILLS = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTPriceNoKills", "false"));
			TVT_JOIN_CURSED = Boolean.parseBoolean(EventTVTSettings.getProperty("TvTJoinWithCursedWeapon", "true"));
			TVT_REVIVE_DELAY = Long.parseLong(EventTVTSettings.getProperty("TVTReviveDelay", "20000"));
			if (TVT_REVIVE_DELAY < 1000)
			{
				TVT_REVIVE_DELAY = 1000; // can't be set less then 1 second
			}
			
			Properties EventDMSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(EVENT_DM_CONFIG_FILE)))
			{
				EventDMSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + EVENT_DM_CONFIG_FILE + " settings!");
			}
			DM_ALLOW_INTERFERENCE = Boolean.parseBoolean(EventDMSettings.getProperty("DMAllowInterference", "false"));
			DM_ALLOW_POTIONS = Boolean.parseBoolean(EventDMSettings.getProperty("DMAllowPotions", "false"));
			DM_ALLOW_SUMMON = Boolean.parseBoolean(EventDMSettings.getProperty("DMAllowSummon", "false"));
			DM_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(EventDMSettings.getProperty("DMOnStartRemoveAllEffects", "true"));
			DM_ON_START_UNSUMMON_PET = Boolean.parseBoolean(EventDMSettings.getProperty("DMOnStartUnsummonPet", "true"));
			DM_REVIVE_DELAY = Long.parseLong(EventDMSettings.getProperty("DMReviveDelay", "20000"));
			if (DM_REVIVE_DELAY < 1000)
			{
				DM_REVIVE_DELAY = 1000; // can't be set less then 1 second
			}
			
			Properties EventCTFSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(EVENT_CTF_CONFIG_FILE)))
			{
				EventCTFSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + EVENT_CTF_CONFIG_FILE + " settings!");
			}
			ALLOW_CTF_AUTOEVENT = Boolean.parseBoolean(EventCTFSettings.getProperty("CTFAutomatedEvent", "true"));
			CTF_EVENT_TIMES = EventCTFSettings.getProperty("CTFStartUpTimes", "17:00,18:00,19:00");
			CTF_EVEN_TEAMS = EventCTFSettings.getProperty("CTFEvenTeams", "BALANCE");
			CTF_ALLOW_INTERFERENCE = Boolean.parseBoolean(EventCTFSettings.getProperty("CTFAllowInterference", "false"));
			CTF_ALLOW_POTIONS = Boolean.parseBoolean(EventCTFSettings.getProperty("CTFAllowPotions", "false"));
			CTF_ALLOW_SUMMON = Boolean.parseBoolean(EventCTFSettings.getProperty("CTFAllowSummon", "false"));
			CTF_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(EventCTFSettings.getProperty("CTFOnStartRemoveAllEffects", "true"));
			CTF_ON_START_UNSUMMON_PET = Boolean.parseBoolean(EventCTFSettings.getProperty("CTFOnStartUnsummonPet", "true"));
			CTF_ANNOUNCE_TEAM_STATS = Boolean.parseBoolean(EventCTFSettings.getProperty("CTFAnnounceTeamStats", "false"));
			CTF_ANNOUNCE_REWARD = Boolean.parseBoolean(EventCTFSettings.getProperty("CTFAnnounceReward", "false"));
			CTF_JOIN_CURSED = Boolean.parseBoolean(EventCTFSettings.getProperty("CTFJoinWithCursedWeapon", "true"));
			CTF_REVIVE_RECOVERY = Boolean.parseBoolean(EventCTFSettings.getProperty("CTFReviveRecovery", "false"));
			CTF_REVIVE_DELAY = Long.parseLong(EventCTFSettings.getProperty("CTFReviveDelay", "20000"));
			if (CTF_REVIVE_DELAY < 1000)
			{
				CTF_REVIVE_DELAY = 1000; // can't be set less then 1 second
			}

			Properties EventWEDDINGSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(EVENT_WEDDING_CONFIG_FILE)))
			{
				EventWEDDINGSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + EVENT_WEDDING_CONFIG_FILE + " settings!");
			}
			MOD_ALLOW_WEDDING = Boolean.valueOf(EventWEDDINGSettings.getProperty("AllowWedding", "False"));
			MOD_WEDDING_PRICE = Integer.parseInt(EventWEDDINGSettings.getProperty("WeddingPrice", "250000000"));
			MOD_WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(EventWEDDINGSettings.getProperty("WeddingPunishInfidelity", "True"));
			MOD_WEDDING_TELEPORT = Boolean.parseBoolean(EventWEDDINGSettings.getProperty("WeddingTeleport", "True"));
			MOD_WEDDING_TELEPORT_PRICE = Integer.parseInt(EventWEDDINGSettings.getProperty("WeddingTeleportPrice", "50000"));
			MOD_WEDDING_TELEPORT_DURATION = Integer.parseInt(EventWEDDINGSettings.getProperty("WeddingTeleportDuration", "60"));
			MOD_WEDDING_SAMESEX = Boolean.parseBoolean(EventWEDDINGSettings.getProperty("WeddingAllowSameSex", "False"));
			MOD_WEDDING_FORMALWEAR = Boolean.parseBoolean(EventWEDDINGSettings.getProperty("WeddingFormalWear", "True"));
			CUPID_TO_PLAYERS = Boolean.parseBoolean(EventWEDDINGSettings.getProperty("CupidToPlayers", "True"));
			MOD_WEDDING_DIVORCE_COSTS = Integer.parseInt(EventWEDDINGSettings.getProperty("WeddingDivorceCosts", "20"));
			MOD_WEDDING_ANNOUNCE = Boolean.parseBoolean(EventWEDDINGSettings.getProperty("AnnounceWeddings", "True"));
			
			Properties ratesSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(RATES_CONFIG_FILE)))
			{
				ratesSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + RATES_CONFIG_FILE + " settings!");
			}
			// Premium Service
			USE_PREMIUMSERVICE = Boolean.parseBoolean(ratesSettings.getProperty("UsePremiumServices", "False"));
			PREMIUM_RATE_XP = Float.parseFloat(ratesSettings.getProperty("PremiumRateXp", "2"));
			PREMIUM_RATE_SP = Float.parseFloat(ratesSettings.getProperty("PremiumRateSp", "2"));
			PREMIUM_RATE_DROP_ADENA = Float.parseFloat(ratesSettings.getProperty("PremiumRateDropAdena", "2"));
			PREMIUM_RATE_DROP_SPOIL = Float.parseFloat(ratesSettings.getProperty("PremiumRateDropSpoil", "2"));
			PREMIUM_RATE_DROP_ITEMS = Float.parseFloat(ratesSettings.getProperty("PremiumRateDropItems", "2"));
			PREMIUM_RATE_DROP_QUEST = Float.parseFloat(ratesSettings.getProperty("PremiumRateDropQuest", "2"));
			PREMIUM_RATE_DROP_ITEMS_BY_RAID = Float.parseFloat(ratesSettings.getProperty("PremiumRateRaidDropItems", "2"));
			PREMIUM_PLAYER_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("PremiumPlayerDropLimit", "0"));
			PREMIUM_PLAYER_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("PremiumPlayerRateDrop", "0"));
			PREMIUM_PLAYER_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("PremiumPlayerRateDropItem", "0"));
			PREMIUM_PLAYER_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("PremiumPlayerRateDropEquip", "0"));
			PREMIUM_PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("PremiumPlayerRateDropEquipWeapon", "0"));
			PREMIUM_KARMA_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("PremiumKarmaDropLimit", "5"));
			PREMIUM_KARMA_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("PremiumKarmaRateDrop", "35"));
			PREMIUM_KARMA_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("PremiumKarmaRateDropItem", "25"));
			PREMIUM_KARMA_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("PremiumKarmaRateDropEquip", "20"));
			PREMIUM_KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("PremiumKarmaRateDropEquipWeapon", "5"));
			
			RATE_QUEST_DROP = Float.parseFloat(ratesSettings.getProperty("RateQuestDrop", "1"));
			RATE_QUEST_REWARD = Float.parseFloat(ratesSettings.getProperty("RateQuestReward", "1"));
			RATE_QUEST_REWARD_XP = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardXP", "1"));
			RATE_QUEST_REWARD_SP = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardSP", "1"));
			RATE_QUEST_REWARD_ADENA = Float.parseFloat(ratesSettings.getProperty("RateQuestRewardAdena", "1"));
			
			RATE_XP = Float.parseFloat(ratesSettings.getProperty("RateXp", "1"));
			RATE_SP = Float.parseFloat(ratesSettings.getProperty("RateSp", "1"));
			RATE_PARTY_XP = Float.parseFloat(ratesSettings.getProperty("RatePartyXp", "1"));
			RATE_PARTY_SP = Float.parseFloat(ratesSettings.getProperty("RatePartySp", "1"));
			RATE_DROP_ADENA = Float.parseFloat(ratesSettings.getProperty("RateDropAdena", "1"));
			RATE_CONSUMABLE_COST = Float.parseFloat(ratesSettings.getProperty("RateConsumableCost", "1"));
			RATE_DROP_ITEMS = Float.parseFloat(ratesSettings.getProperty("RateDropItems", "1"));
			RATE_DROP_SPOIL = Float.parseFloat(ratesSettings.getProperty("RateDropSpoil", "1"));
			RATE_DROP_MANOR = Integer.parseInt(ratesSettings.getProperty("RateDropManor", "1"));
			PARTY_XP_CUTOFF_METHOD = ratesSettings.getProperty("PartyXpCutoffMethod", "percentage");
			PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(ratesSettings.getProperty("PartyXpCutoffPercent", "3."));
			PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(ratesSettings.getProperty("PartyXpCutoffLevel", "30"));
			RATE_DROP_QUEST = Float.parseFloat(ratesSettings.getProperty("RateDropQuest", "1"));
			RATE_QUESTS_REWARD = Float.parseFloat(ratesSettings.getProperty("RateQuestsReward", "1"));
			RATE_KARMA_EXP_LOST = Float.parseFloat(ratesSettings.getProperty("RateKarmaExpLost", "1"));
			RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(ratesSettings.getProperty("RateSiegeGuardsPrice", "1"));
			PLAYER_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("PlayerDropLimit", "0"));
			PLAYER_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDrop", "0"));
			PLAYER_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropItem", "0"));
			PLAYER_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquip", "0"));
			PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquipWeapon", "0"));
			KARMA_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("KarmaDropLimit", "10"));
			KARMA_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDrop", "70"));
			KARMA_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropItem", "50"));
			KARMA_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquip", "40"));
			KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquipWeapon", "10"));
			PET_XP_RATE = Float.parseFloat(ratesSettings.getProperty("PetXpRate", "1"));
			PET_FOOD_RATE = Integer.parseInt(ratesSettings.getProperty("PetFoodRate", "1"));
			SINEATER_XP_RATE = Float.parseFloat(ratesSettings.getProperty("SinEaterXpRate", "1"));
			RATE_DROP_COMMON_HERBS = Float.parseFloat(ratesSettings.getProperty("RateCommonHerbs", "15"));
			RATE_DROP_MP_HP_HERBS = Float.parseFloat(ratesSettings.getProperty("RateHpMpHerbs", "10"));
			RATE_DROP_GREATER_HERBS = Float.parseFloat(ratesSettings.getProperty("RateGreaterHerbs", "4"));
			RATE_DROP_SUPERIOR_HERBS = Float.parseFloat(ratesSettings.getProperty("RateSuperiorHerbs", "0.8")) * 10;
			RATE_DROP_SPECIAL_HERBS = Float.parseFloat(ratesSettings.getProperty("RateSpecialHerbs", "0.2")) * 10;
			RATE_DROP_ITEMS_BY_RAID = Float.parseFloat(ratesSettings.getProperty("RateRaidDropItems", "1"));
			ENCHANT_CHANCE_WEAPON = Integer.parseInt(ratesSettings.getProperty("EnchantChanceWeapon", "60"));
			ENCHANT_CHANCE_ARMOR = Integer.parseInt(ratesSettings.getProperty("EnchantChanceArmor", "60"));
			ENCHANT_CHANCE_JEWELRY = Integer.parseInt(ratesSettings.getProperty("EnchantChanceJewelry", "60"));
			ENCHANT_CHANCE_WEAPON_BLESSED = Integer.parseInt(ratesSettings.getProperty("EnchantChanceWeaponBlessed", "80"));
			ENCHANT_CHANCE_ARMOR_BLESSED = Integer.parseInt(ratesSettings.getProperty("EnchantChanceArmorBlessed", "80"));
			ENCHANT_CHANCE_JEWELRY_BLESSED = Integer.parseInt(ratesSettings.getProperty("EnchantChanceJewelryBlessed", "80"));
			ENCHANT_MAX_ALLOWED_WEAPON = Integer.parseInt(ratesSettings.getProperty("EnchantMaxAllowedWeapon", "65535"));
			ENCHANT_MAX_ALLOWED_ARMOR = Integer.parseInt(ratesSettings.getProperty("EnchantMaxAllowedArmor", "65535"));
			ENCHANT_MAX_ALLOWED_JEWELRY = Integer.parseInt(ratesSettings.getProperty("EnchantMaxAllowedJewelry", "65535"));
			ENCHANT_MAX_WEAPON = Integer.parseInt(ratesSettings.getProperty("EnchantMaxWeapon", "25"));
			ENCHANT_MAX_ARMOR = Integer.parseInt(ratesSettings.getProperty("EnchantMaxArmor", "25"));
			ENCHANT_MAX_JEWELRY = Integer.parseInt(ratesSettings.getProperty("EnchantMaxJewelry", "25"));
			ENCHANT_SAFE_MAX = Integer.parseInt(ratesSettings.getProperty("EnchantSafeMax", "3"));
			ENCHANT_SAFE_MAX_FULL = Integer.parseInt(ratesSettings.getProperty("EnchantSafeMaxFull", "4"));
			
			Properties grandbossSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(GRANDBOSS_CONFIG_FILE)))
			{
				grandbossSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + GRANDBOSS_CONFIG_FILE + " settings!");
			}
			Antharas_Wait_Time = Integer.parseInt(grandbossSettings.getProperty("AntharasWaitTime", "10"));
			if (Antharas_Wait_Time < 3 || Antharas_Wait_Time > 60)
			{
				Antharas_Wait_Time = 10;
			}
			Valakas_Wait_Time = Integer.parseInt(grandbossSettings.getProperty("ValakasWaitTime", "30"));
			Interval_Of_Valakas_Spawn = Integer.parseInt(grandbossSettings.getProperty("IntervalOfValakasSpawn", "192"));
			Random_Of_Valakas_Spawn = Integer.parseInt(grandbossSettings.getProperty("RandomOfValakasSpawn", "145"));
			Antharas_Wait_Time = Antharas_Wait_Time * 60000;
			Interval_Of_Antharas_Spawn = Integer.parseInt(grandbossSettings.getProperty("IntervalOfAntharasSpawn", "192"));
			Interval_Of_Baium_Spawn = Integer.parseInt(grandbossSettings.getProperty("IntervalOfBaiumSpawn", "121"));
			Random_Of_Baium_Spawn = Integer.parseInt(grandbossSettings.getProperty("RandomOfBaiumSpawn", "8"));
			if (Interval_Of_Antharas_Spawn < 1 || Interval_Of_Antharas_Spawn > 192)
			{
				Interval_Of_Antharas_Spawn = 192;
			}
			Interval_Of_Antharas_Spawn = Interval_Of_Antharas_Spawn * 3600000;
			Random_Of_Antharas_Spawn = Integer.parseInt(grandbossSettings.getProperty("RandomOfAntharasSpawn", "145"));
			if (Random_Of_Antharas_Spawn < 1 || Random_Of_Antharas_Spawn > 192)
			{
				Random_Of_Antharas_Spawn = 145;
			}
			Random_Of_Antharas_Spawn = Random_Of_Antharas_Spawn * 3600000;
			Interval_Of_Core_Spawn = Integer.parseInt(grandbossSettings.getProperty("IntervalOfCoreSpawn", "27"));
			if (Interval_Of_Core_Spawn < 1 || Interval_Of_Core_Spawn > 192)
			{
				Interval_Of_Core_Spawn = 27;
			}
			Interval_Of_Core_Spawn = Interval_Of_Core_Spawn * 3600000;
			Random_Of_Core_Spawn = Integer.parseInt(grandbossSettings.getProperty("RandomOfCoreSpawn", "47"));
			if (Random_Of_Core_Spawn < 1 || Random_Of_Core_Spawn > 192)
			{
				Random_Of_Core_Spawn = 47;
			}
			Random_Of_Core_Spawn = Random_Of_Core_Spawn * 3600000;
			Interval_Of_Orfen_Spawn = Integer.parseInt(grandbossSettings.getProperty("IntervalOfOrfenSpawn", "28"));
			if (Interval_Of_Orfen_Spawn < 1 || Interval_Of_Orfen_Spawn > 192)
			{
				Interval_Of_Orfen_Spawn = 28;
			}
			Interval_Of_Orfen_Spawn = Interval_Of_Orfen_Spawn * 3600000;
			Random_Of_Orfen_Spawn = Integer.parseInt(grandbossSettings.getProperty("RandomOfOrfenSpawn", "41"));
			if (Random_Of_Orfen_Spawn < 1 || Random_Of_Orfen_Spawn > 192)
			{
				Random_Of_Orfen_Spawn = 41;
			}
			Random_Of_Orfen_Spawn = Random_Of_Orfen_Spawn * 3600000;
			Interval_Of_QueenAnt_Spawn = Integer.parseInt(grandbossSettings.getProperty("IntervalOfQueenAntSpawn", "19"));
			if (Interval_Of_QueenAnt_Spawn < 1 || Interval_Of_QueenAnt_Spawn > 192)
			{
				Interval_Of_QueenAnt_Spawn = 19;
			}
			Interval_Of_QueenAnt_Spawn = Interval_Of_QueenAnt_Spawn * 3600000;
			Random_Of_QueenAnt_Spawn = Integer.parseInt(grandbossSettings.getProperty("RandomOfQueenAntSpawn", "35"));
			if (Random_Of_QueenAnt_Spawn < 1 || Random_Of_QueenAnt_Spawn > 192)
			{
				Random_Of_QueenAnt_Spawn = 35;
			}
			Random_Of_QueenAnt_Spawn = Random_Of_QueenAnt_Spawn * 3600000;
			Interval_Of_Zaken_Spawn = Integer.parseInt(grandbossSettings.getProperty("IntervalOfZakenSpawn", "19"));
			if (Interval_Of_Zaken_Spawn < 1 || Interval_Of_Zaken_Spawn > 192)
			{
				Interval_Of_Zaken_Spawn = 19;
			}
			Interval_Of_Zaken_Spawn = Interval_Of_Zaken_Spawn * 3600000;
			Random_Of_Zaken_Spawn = Integer.parseInt(grandbossSettings.getProperty("RandomOfZakenSpawn", "35"));
			if (Random_Of_Zaken_Spawn < 1 || Random_Of_Zaken_Spawn > 192)
			{
				Random_Of_Zaken_Spawn = 35;
			}
			Random_Of_Zaken_Spawn = Random_Of_Zaken_Spawn * 3600000;
			Interval_Of_Sailren_Spawn = Integer.parseInt(grandbossSettings.getProperty("IntervalOfSailrenSpawn", "12"));
			if (Interval_Of_Sailren_Spawn < 1 || Interval_Of_Sailren_Spawn > 192)
			{
				Interval_Of_Sailren_Spawn = 12;
			}
			Interval_Of_Sailren_Spawn = Interval_Of_Sailren_Spawn * 3600000;
			Random_Of_Sailren_Spawn = Integer.parseInt(grandbossSettings.getProperty("RandomOfSailrenSpawn", "24"));
			if (Random_Of_Sailren_Spawn < 1 || Random_Of_Sailren_Spawn > 192)
			{
				Random_Of_Sailren_Spawn = 24;
			}
			Random_Of_Sailren_Spawn = Random_Of_Sailren_Spawn * 3600000;
			
			Properties SevenSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(SEVENSIGNS_FILE)))
			{
				SevenSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + SEVENSIGNS_FILE + " settings!");
			}
			ALT_GAME_REQUIRE_CASTLE_DAWN = Boolean.parseBoolean(SevenSettings.getProperty("AltRequireCastleForDawn", "False"));
			ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.parseBoolean(SevenSettings.getProperty("AltRequireClanCastle", "False"));
			ALT_FESTIVAL_MIN_PLAYER = Integer.parseInt(SevenSettings.getProperty("AltFestivalMinPlayer", "5"));
			ALT_MAXIMUM_PLAYER_CONTRIB = Integer.parseInt(SevenSettings.getProperty("AltMaxPlayerContrib", "1000000"));
			ALT_FESTIVAL_MANAGER_START = Long.parseLong(SevenSettings.getProperty("AltFestivalManagerStart", "120000"));
			ALT_FESTIVAL_LENGTH = Long.parseLong(SevenSettings.getProperty("AltFestivalLength", "1080000"));
			ALT_FESTIVAL_CYCLE_LENGTH = Long.parseLong(SevenSettings.getProperty("AltFestivalCycleLength", "2280000"));
			ALT_FESTIVAL_FIRST_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalFirstSpawn", "120000"));
			ALT_FESTIVAL_FIRST_SWARM = Long.parseLong(SevenSettings.getProperty("AltFestivalFirstSwarm", "300000"));
			ALT_FESTIVAL_SECOND_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalSecondSpawn", "540000"));
			ALT_FESTIVAL_SECOND_SWARM = Long.parseLong(SevenSettings.getProperty("AltFestivalSecondSwarm", "720000"));
			ALT_FESTIVAL_CHEST_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalChestSpawn", "900000"));
			
			Properties serverSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(CONFIGURATION_FILE)))
			{
				serverSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + CONFIGURATION_FILE + " settings!");
			}
			// Dev's Config
			DEBUG = Boolean.parseBoolean(serverSettings.getProperty("Debug", "False"));
			DEBUG_LOGGER = Boolean.parseBoolean(serverSettings.getProperty("DebugLogger", "False"));
			DEVELOPER = Boolean.parseBoolean(serverSettings.getProperty("Developer", "False"));
			ALT_DEV_NO_SPAWNS = Boolean.parseBoolean(serverSettings.getProperty("AltDevNoSpawns", "False"));
			ALT_DEV_NO_SCRIPT = Boolean.parseBoolean(serverSettings.getProperty("AltDevNoScripts", "False"));
			ALT_DEV_NO_RB = Boolean.parseBoolean(serverSettings.getProperty("AltDevNoRaidbosses", "False"));
			VERBOSE_LOADING = Boolean.parseBoolean(serverSettings.getProperty("VerboseLoading", "False"));
			ATTEMPT_COMPILATION = Boolean.parseBoolean(serverSettings.getProperty("AttemptCompilation", "False"));
			PURGE_ERROR_LOG = Boolean.parseBoolean(serverSettings.getProperty("PurgeErrorLog", "False"));
			REQUEST_ID = Integer.parseInt(serverSettings.getProperty("RequestServerID", "0"));
			ACCEPT_ALTERNATE_ID = Boolean.parseBoolean(serverSettings.getProperty("AcceptAlternateID", "True"));
			DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
			DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
			DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
			DATABASE_MAX_CONNECTIONS = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
			CNAME_TEMPLATE = serverSettings.getProperty("CnameTemplate", ".*");
			PET_NAME_TEMPLATE = serverSettings.getProperty("PetNameTemplate", ".*");
			MAX_CHARACTERS_NUMBER_PER_ACCOUNT = Integer.parseInt(serverSettings.getProperty("CharMaxNumber", "0"));
			MAXIMUM_ONLINE_USERS = Integer.parseInt(serverSettings.getProperty("MaximumOnlineUsers", "100"));
			ALLOW_DUALBOX = Boolean.parseBoolean(serverSettings.getProperty("AllowDualBox", "True"));
			ENABLE_PACKET_PROTECTION = Boolean.parseBoolean(serverSettings.getProperty("PacketProtection", "False"));
			MAX_UNKNOWN_PACKETS = Integer.parseInt(serverSettings.getProperty("UnknownPacketsBeforeBan", "5"));
			UNKNOWN_PACKETS_PUNISHMENT = Integer.parseInt(serverSettings.getProperty("UnknownPacketsPunishment", "2"));
			TEST_SERVER = Boolean.parseBoolean(serverSettings.getProperty("TestServer", "False"));
			SERVER_LIST_TESTSERVER = Boolean.parseBoolean(serverSettings.getProperty("ListTestServer", "False"));
			EVERYBODY_HAS_ADMIN_RIGHTS = Boolean.parseBoolean(serverSettings.getProperty("EverybodyHasAdminRights", "False"));
			SERVER_LIST_BRACKET = Boolean.valueOf(serverSettings.getProperty("ServerListBrackets", "False"));
			SERVER_LIST_CLOCK = Boolean.valueOf(serverSettings.getProperty("ServerListClock", "False"));
			SERVER_GMONLY = Boolean.valueOf(serverSettings.getProperty("ServerGMOnly", "False"));
			THREAD_P_EFFECTS = Integer.parseInt(serverSettings.getProperty("ThreadPoolSizeEffects", "6"));
			THREAD_P_GENERAL = Integer.parseInt(serverSettings.getProperty("ThreadPoolSizeGeneral", "15"));
			GENERAL_PACKET_THREAD_CORE_SIZE = Integer.parseInt(serverSettings.getProperty("GeneralPacketThreadCoreSize", "4"));
			IO_PACKET_THREAD_CORE_SIZE = Integer.parseInt(serverSettings.getProperty("UrgentPacketThreadCoreSize", "2"));
			GENERAL_THREAD_CORE_SIZE = Integer.parseInt(serverSettings.getProperty("GeneralThreadCoreSize", "4"));
			PACKET_LIFETIME = Integer.parseInt(serverSettings.getProperty("PacketLifeTime", "0"));
			
			Properties gsIpSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(GS_IP)))
			{
				gsIpSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + GS_IP + " settings!");
			}
			// Dev's Config
			EXTERNAL_HOSTNAME = gsIpSettings.getProperty("ExternalHostname", "*");
			INTERNAL_HOSTNAME = gsIpSettings.getProperty("InternalHostname", "*");
			GAME_SERVER_LOGIN_HOST = gsIpSettings.getProperty("LoginHost", "127.0.0.1");
			GAMESERVER_HOSTNAME = gsIpSettings.getProperty("GameserverHostname");
			PORT_GAME = Integer.parseInt(gsIpSettings.getProperty("GameserverPort", "7777"));
			GAME_SERVER_LOGIN_PORT = Integer.parseInt(gsIpSettings.getProperty("LoginPort", "9014"));
			
			Properties hexidSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(HEXID_FILE)))
			{
				hexidSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + HEXID_FILE + " settings!");
			}
			SERVER_ID = Integer.parseInt(hexidSettings.getProperty("ServerID"));
			HEX_ID = new BigInteger(hexidSettings.getProperty("HexID"), 16).toByteArray();
			_log.info(Config.class.getName() + "Configuration Files Loaded.");
		}
		else if (Server.serverMode == Server.MODE_LOGINSERVER)
		{
			
			Properties serverSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(LOGIN_CONFIGURATION_FILE)))
			{
				serverSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + LOGIN_CONFIGURATION_FILE + " settings!");
			}
			ACCEPT_NEW_GAMESERVER = Boolean.parseBoolean(serverSettings.getProperty("AcceptNewGameServer", "True"));
			REQUEST_ID = Integer.parseInt(serverSettings.getProperty("RequestServerID", "0"));
			ACCEPT_ALTERNATE_ID = Boolean.parseBoolean(serverSettings.getProperty("AcceptAlternateID", "True"));
			LOGIN_TRY_BEFORE_BAN = Integer.parseInt(serverSettings.getProperty("LoginTryBeforeBan", "10"));
			LOGIN_BLOCK_AFTER_BAN = Integer.parseInt(serverSettings.getProperty("LoginBlockAfterBan", "600"));
			DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
			DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
			DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
			DATABASE_MAX_CONNECTIONS = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
			SHOW_LICENCE = Boolean.parseBoolean(serverSettings.getProperty("ShowLicence", "True"));
			IP_UPDATE_TIME = Integer.parseInt(serverSettings.getProperty("IpUpdateTime", "15"));
			FORCE_GGAUTH = Boolean.parseBoolean(serverSettings.getProperty("ForceGGAuth", "False"));
			AUTO_CREATE_ACCOUNTS = Boolean.parseBoolean(serverSettings.getProperty("AutoCreateAccounts", "False"));
			FLOOD_PROTECTION = Boolean.parseBoolean(serverSettings.getProperty("EnableFloodProtection", "True"));
			FAST_CONNECTION_LIMIT = Integer.parseInt(serverSettings.getProperty("FastConnectionLimit", "15"));
			NORMAL_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("NormalConnectionTime", "700"));
			FAST_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("FastConnectionTime", "350"));
			MAX_CONNECTION_PER_IP = Integer.parseInt(serverSettings.getProperty("MaxConnectionPerIP", "50"));
			
			Properties lsIpSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(LS_IP)))
			{
				lsIpSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + LS_IP + " settings!");
			}
			GAME_SERVER_LOGIN_HOST = lsIpSettings.getProperty("LoginHostname", "127.0.0.1");
			GAME_SERVER_LOGIN_PORT = Integer.parseInt(lsIpSettings.getProperty("LoginPort", "9014"));
			LOGIN_BIND_ADDRESS = lsIpSettings.getProperty("LoginserverHostname", "*");
			PORT_LOGIN = Integer.parseInt(lsIpSettings.getProperty("LoginserverPort", "2106"));
			INTERNAL_HOSTNAME = lsIpSettings.getProperty("InternalHostname", "localhost");
			EXTERNAL_HOSTNAME = lsIpSettings.getProperty("ExternalHostname", "localhost");
			
			Properties versionSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(SERVER_VERSION_FILE)))
			{
				versionSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(Config.class.getName() + ": Error while loading " + SERVER_VERSION_FILE + " settings!");
			}
			SERVER_VERSION = versionSettings.getProperty("version", "Unsupported Custom Version.");
			SERVER_BUILD_DATE = versionSettings.getProperty("builddate", "Undefined Date.");
			_log.info(Config.class.getName() + "Configuration Files Loaded.");
		}
		else
		{
			_log.severe("Could not Load Config: server mode was not set!");
		}
	}
	
	public static boolean setParameterValue(String pName, String pValue)
	{
		switch (pName.trim().toLowerCase())
		{
		// TODO next fixes here on admin panel refactor
			case "ratexp":
				RATE_XP = Float.parseFloat(pValue);
				break;
			case "PremiumRateXp":
				PREMIUM_RATE_XP = Float.parseFloat(pValue);
				break;
			case "PremiumRateSp":
				PREMIUM_RATE_SP = Float.parseFloat(pValue);
				break;
			case "PremiumRateDropSpoil":
				PREMIUM_RATE_DROP_SPOIL = Float.parseFloat(pValue);
				break;
			case "PremiumRateDropItems":
				PREMIUM_RATE_DROP_ITEMS = Float.parseFloat(pValue);
				break;
			case "PremiumRateDropQuest":
				PREMIUM_RATE_DROP_QUEST = Float.parseFloat(pValue);
				break;
			case "PremiumRateRaidDropItems":
				PREMIUM_RATE_DROP_ITEMS_BY_RAID = Float.parseFloat(pValue);
				break;
			case "RateXp":
				RATE_XP = Float.parseFloat(pValue);
				break;
			case "RateSp":
				RATE_SP = Float.parseFloat(pValue);
				break;
			case "RatePartyXp":
				RATE_PARTY_XP = Float.parseFloat(pValue);
				break;
			case "RatePartySp":
				RATE_PARTY_SP = Float.parseFloat(pValue);
				break;
			case "RateQuestsReward":
				RATE_QUESTS_REWARD = Float.parseFloat(pValue);
				break;
			case "RateDropAdena":
				RATE_DROP_ADENA = Float.parseFloat(pValue);
				break;
			case "RateConsumableCost":
				RATE_CONSUMABLE_COST = Float.parseFloat(pValue);
				break;
			case "RateDropItems":
				RATE_DROP_ITEMS = Float.parseFloat(pValue);
				break;
			case "RateDropSpoil":
				RATE_DROP_SPOIL = Float.parseFloat(pValue);
				break;
			case "RateDropManor":
				RATE_DROP_MANOR = Integer.parseInt(pValue);
				break;
			case "RateDropQuest":
				RATE_DROP_QUEST = Float.parseFloat(pValue);
				break;
			case "RateKarmaExpLost":
				RATE_KARMA_EXP_LOST = Float.parseFloat(pValue);
				break;
			case "RateSiegeGuardsPrice":
				RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(pValue);
				break;
			case "PlayerDropLimit":
				PLAYER_DROP_LIMIT = Integer.parseInt(pValue);
				break;
			case "PlayerRateDrop":
				PLAYER_RATE_DROP = Integer.parseInt(pValue);
				break;
			case "PlayerRateDropItem":
				PLAYER_RATE_DROP_ITEM = Integer.parseInt(pValue);
				break;
			case "PlayerRateDropEquip":
				PLAYER_RATE_DROP_EQUIP = Integer.parseInt(pValue);
				break;
			case "PlayerRateDropEquipWeapon":
				PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
				break;
			case "KarmaDropLimit":
				KARMA_DROP_LIMIT = Integer.parseInt(pValue);
				break;
			case "KarmaRateDrop":
				KARMA_RATE_DROP = Integer.parseInt(pValue);
				break;
			case "KarmaRateDropItem":
				KARMA_RATE_DROP_ITEM = Integer.parseInt(pValue);
				break;
			case "KarmaRateDropEquip":
				KARMA_RATE_DROP_EQUIP = Integer.parseInt(pValue);
				break;
			case "KarmaRateDropEquipWeapon":
				KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
				break;
			case "AutoDestroyDroppedItemAfter":
				AUTODESTROY_ITEM_AFTER = Integer.parseInt(pValue);
				break;
			case "DestroyPlayerDroppedItem":
				DESTROY_DROPPED_PLAYER_ITEM = Boolean.valueOf(pValue);
				break;
			case "DestroyEquipableItem":
				DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.valueOf(pValue);
				break;
			case "SaveDroppedItem":
				SAVE_DROPPED_ITEM = Boolean.valueOf(pValue);
				break;
			case "EmptyDroppedItemTableAfterLoad":
				EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.valueOf(pValue);
				break;
			case "SaveDroppedItemInterval":
				SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(pValue);
				break;
			case "ClearDroppedItemTable":
				CLEAR_DROPPED_ITEM_TABLE = Boolean.valueOf(pValue);
				break;
			case "MultipleItemDrop":
				MULTIPLE_ITEM_DROP = Boolean.valueOf(pValue);
				break;
			case "CoordSynchronize":
				COORD_SYNCHRONIZE = Integer.parseInt(pValue);
				break;
			case "DeleteCharAfterDays":
				DELETE_DAYS = Integer.parseInt(pValue);
				break;
			case "AllowDiscardItem":
				ALLOW_DISCARDITEM = Boolean.valueOf(pValue);
				break;
			case "AllowFreight":
				ALLOW_FREIGHT = Boolean.valueOf(pValue);
				break;
			case "AllowWarehouse":
				ALLOW_WAREHOUSE = Boolean.valueOf(pValue);
				break;
			case "AllowWear":
				ALLOW_WEAR = Boolean.valueOf(pValue);
				break;
			case "WearDelay":
				WEAR_DELAY = Integer.parseInt(pValue);
				break;
			case "WearPrice":
				WEAR_PRICE = Integer.parseInt(pValue);
				break;
			case "AllowWater":
				ALLOW_WATER = Boolean.valueOf(pValue);
				break;
			case "AllowRentPet":
				ALLOW_RENTPET = Boolean.valueOf(pValue);
				break;
			case "AllowBoat":
				ALLOW_BOAT = Boolean.valueOf(pValue);
				break;
			case "AllowCursedWeapons":
				ALLOW_CURSED_WEAPONS = Boolean.valueOf(pValue);
				break;
			case "AllowManor":
				ALLOW_MANOR = Boolean.valueOf(pValue);
				break;
			case "BypassValidation":
				BYPASS_VALIDATION = Boolean.valueOf(pValue);
				break;
			case "CommunityType":
				COMMUNITY_TYPE = pValue.toLowerCase();
				break;
			case "BBSDefault":
				BBS_DEFAULT = pValue;
				break;
			case "ShowLevelOnCommunityBoard":
				SHOW_LEVEL_COMMUNITYBOARD = Boolean.valueOf(pValue);
				break;
			case "ShowStatusOnCommunityBoard":
				SHOW_STATUS_COMMUNITYBOARD = Boolean.valueOf(pValue);
				break;
			case "NamePageSizeOnCommunityBoard":
				NAME_PAGE_SIZE_COMMUNITYBOARD = Integer.parseInt(pValue);
				break;
			case "NamePerRowOnCommunityBoard":
				NAME_PER_ROW_COMMUNITYBOARD = Integer.parseInt(pValue);
				break;
			case "ShowServerNews":
				SERVER_NEWS = Boolean.valueOf(pValue);
				break;
			case "ShowNpcLevel":
				SHOW_NPC_LVL = Boolean.valueOf(pValue);
				break;
			case "ForceInventoryUpdate":
				FORCE_INVENTORY_UPDATE = Boolean.valueOf(pValue);
				break;
			case "AutoDeleteInvalidQuestData":
				AUTODELETE_INVALID_QUEST_DATA = Boolean.valueOf(pValue);
				break;
			case "MaximumOnlineUsers":
				MAXIMUM_ONLINE_USERS = Integer.parseInt(pValue);
				break;
			case "ZoneTown":
				ZONE_TOWN = Integer.parseInt(pValue);
				break;
			case "ReputationScorePerKill":
				ALT_REPUTATION_SCORE_PER_KILL = Integer.parseInt(pValue);
				break;
			case "UseDeepBlueDropRules":
				DEEPBLUE_DROP_RULES = Boolean.valueOf(pValue);
				break;
			case "AllowGuards":
				ALLOW_GUARDS = Boolean.valueOf(pValue);
				break;
			case "CancelLesserEffect":
				EFFECT_CANCELING = Boolean.valueOf(pValue);
				break;
			case "WyvernSpeed":
				WYVERN_SPEED = Integer.parseInt(pValue);
				break;
			case "StriderSpeed":
				STRIDER_SPEED = Integer.parseInt(pValue);
				break;
			case "MaximumSlotsForNoDwarf":
				INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(pValue);
				break;
			case "MaximumSlotsForDwarf":
				INVENTORY_MAXIMUM_DWARF = Integer.parseInt(pValue);
				break;
			case "MaximumSlotsForGMPlayer":
				INVENTORY_MAXIMUM_GM = Integer.parseInt(pValue);
				break;
			case "MaximumWarehouseSlotsForNoDwarf":
				WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(pValue);
				break;
			case "MaximumWarehouseSlotsForDwarf":
				WAREHOUSE_SLOTS_DWARF = Integer.parseInt(pValue);
				break;
			case "MaximumWarehouseSlotsForClan":
				WAREHOUSE_SLOTS_CLAN = Integer.parseInt(pValue);
				break;
			case "MaximumFreightSlots":
				FREIGHT_SLOTS = Integer.parseInt(pValue);
				break;
			case "HpRegenMultiplier":
				HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
				break;
			case "MpRegenMultiplier":
				MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
				break;
			case "CpRegenMultiplier":
				CP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
				break;
			case "RaidHpRegenMultiplier":
				RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
				break;
			case "RaidMpRegenMultiplier":
				RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
				break;
			case "RaidPhysicalDefenceMultiplier":
				RAID_P_DEFENCE_MULTIPLIER = Double.parseDouble(pValue) / 100;
				break;
			case "RaidMagicalDefenceMultiplier":
				RAID_M_DEFENCE_MULTIPLIER = Double.parseDouble(pValue) / 100;
				break;
			case "RaidMinionRespawnTime":
				RAID_MINION_RESPAWN_TIMER = Integer.parseInt(pValue);
				break;
			case "StartingAdena":
				STARTING_ADENA = Integer.parseInt(pValue);
				break;
			case "StartingAncientAdena":
				STARTING_ANCIENT = Integer.parseInt(pValue);
				break;
			case "UnstuckInterval":
				UNSTUCK_INTERVAL = Integer.parseInt(pValue);
				break;
			case "PlayerSpawnProtection":
				PLAYER_SPAWN_PROTECTION = Integer.parseInt(pValue);
				break;
			case "PartyXpCutoffMethod":
				PARTY_XP_CUTOFF_METHOD = pValue;
				break;
			case "PartyXpCutoffPercent":
				PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(pValue);
				break;
			case "PartyXpCutoffLevel":
				PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(pValue);
				break;
			case "RespawnRestoreCP":
				RESPAWN_RESTORE_CP = Double.parseDouble(pValue) / 100;
				break;
			case "RespawnRestoreHP":
				RESPAWN_RESTORE_HP = Double.parseDouble(pValue) / 100;
				break;
			case "RespawnRestoreMP":
				RESPAWN_RESTORE_MP = Double.parseDouble(pValue) / 100;
				break;
			case "MaxPvtStoreSlotsDwarf":
				MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(pValue);
				break;
			case "MaxPvtStoreSlotsOther":
				MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(pValue);
				break;
			case "StoreSkillCooltime":
				STORE_SKILL_COOLTIME = Boolean.valueOf(pValue);
				break;
			case "AnnounceMammonSpawn":
				ANNOUNCE_MAMMON_SPAWN = Boolean.valueOf(pValue);
				break;
			case "AltGameTiredness":
				ALT_GAME_TIREDNESS = Boolean.valueOf(pValue);
				break;
			case "AltGameCreation":
				ALT_GAME_CREATION = Boolean.valueOf(pValue);
				break;
			case "AltGameCreationSpeed":
				ALT_GAME_CREATION_SPEED = Double.parseDouble(pValue);
				break;
			case "AltGameCreationXpRate":
				ALT_GAME_CREATION_XP_RATE = Double.parseDouble(pValue);
				break;
			case "AltGameCreationSpRate":
				ALT_GAME_CREATION_SP_RATE = Double.parseDouble(pValue);
				break;
			case "AltWeightLimit":
				ALT_WEIGHT_LIMIT = Double.parseDouble(pValue);
				break;
			case "AltBlacksmithUseRecipes":
				ALT_BLACKSMITH_USE_RECIPES = Boolean.valueOf(pValue);
				break;
			case "AltGameSkillLearn":
				ALT_GAME_SKILL_LEARN = Boolean.valueOf(pValue);
				break;
			case "RemoveCastleCirclets":
				REMOVE_CASTLE_CIRCLETS = Boolean.valueOf(pValue);
				break;
			case "AltGameCancelByHit":
				ALT_GAME_CANCEL_BOW = pValue.equalsIgnoreCase("bow") || pValue.equalsIgnoreCase("all");
				ALT_GAME_CANCEL_CAST = pValue.equalsIgnoreCase("cast") || pValue.equalsIgnoreCase("all");
				break;
			case "AltShieldBlocks":
				ALT_GAME_SHIELD_BLOCKS = Boolean.valueOf(pValue);
				break;
			case "AltPerfectShieldBlockRate":
				ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(pValue);
				break;
			case "MagicFailures":
				ALT_GAME_MAGICFAILURES = Boolean.valueOf(pValue);
				break;
			case "AltGameMobAttackAI":
				ALT_GAME_MOB_ATTACK_AI = Boolean.valueOf(pValue);
				break;
			case "AltMobAgroInPeaceZone":
				ALT_MOB_AGRO_IN_PEACEZONE = Boolean.valueOf(pValue);
				break;
			case "AltGameExponentXp":
				ALT_GAME_EXPONENT_XP = Float.parseFloat(pValue);
				break;
			case "AltGameExponentSp":
				ALT_GAME_EXPONENT_SP = Float.parseFloat(pValue);
				break;
			case "AltGameFreights":
				ALT_GAME_FREIGHTS = Boolean.valueOf(pValue);
				break;
			case "AltGameFreightPrice":
				ALT_GAME_FREIGHT_PRICE = Integer.parseInt(pValue);
				break;
			case "AltPartyRange":
				ALT_PARTY_RANGE = Integer.parseInt(pValue);
				break;
			case "AltPartyRange2":
				ALT_PARTY_RANGE2 = Integer.parseInt(pValue);
				break;
			case "CraftingEnabled":
				IS_CRAFTING_ENABLED = Boolean.valueOf(pValue);
				break;
			case "LifeCrystalNeeded":
				LIFE_CRYSTAL_NEEDED = Boolean.valueOf(pValue);
				break;
			case "SpBookNeeded":
				SP_BOOK_NEEDED = Boolean.valueOf(pValue);
				break;
			case "AutoLoot":
				AUTO_LOOT = Boolean.valueOf(pValue);
				break;
			case "AutoLootHerbs":
				AUTO_LOOT_HERBS = Boolean.valueOf(pValue);
				break;
			case "AltKarmaPlayerCanBeKilledInPeaceZone":
				ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.valueOf(pValue);
				break;
			case "AltFlaggedPlayerCanUseGK":
				ALT_GAME_FLAGGED_PLAYER_CAN_USE_GK = Boolean.parseBoolean(pValue);
				break;
			case "AltKarmaPlayerCanShop":
				ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.valueOf(pValue);
				break;
			case "AltKarmaPlayerCanUseGK":
				ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.valueOf(pValue);
				break;
			case "AltKarmaPlayerCanTeleport":
				ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.valueOf(pValue);
				break;
			case "AltKarmaPlayerCanTrade":
				ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.valueOf(pValue);
				break;
			case "AltKarmaPlayerCanUseWareHouse":
				ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.valueOf(pValue);
				break;
			case "AltRequireCastleForDawn":
				ALT_GAME_REQUIRE_CASTLE_DAWN = Boolean.valueOf(pValue);
				break;
			case "AltRequireClanCastle":
				ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.valueOf(pValue);
				break;
			case "AltFreeTeleporting":
				ALT_GAME_FREE_TELEPORT = Boolean.valueOf(pValue);
				break;
			case "AltSubClassSkills":
				ALT_SUBCLASS_SKILLS = Boolean.valueOf(pValue);
				break;
			case "MaxPAtkSpeed":
				MAX_PATK_SPEED = Integer.parseInt(pValue);
				break;
			case "MaxMAtkSpeed":
				MAX_MATK_SPEED = Integer.parseInt(pValue);
				break;
			case "AllowPotsInPvP":
				ALLOW_POTS_IN_PVP = Boolean.parseBoolean(pValue);
				break;
			case "DisableGradePenalty":
				DISABLE_GRADE_PENALTY = Boolean.valueOf(pValue);
				break;
			case "DisableWeightPenalty":
				DISABLE_WEIGHT_PENALTY = Boolean.valueOf(pValue);
				break;
			case "AltSubClassWithoutQuests":
				ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.valueOf(pValue);
				break;
			case "AltNewCharAlwaysIsNewbie":
				ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE = Boolean.valueOf(pValue);
				break;
			case "AltMembersCanWithdrawFromClanWH":
				ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Boolean.valueOf(pValue);
				break;
			case "DwarfRecipeLimit":
				DWARF_RECIPE_LIMIT = Integer.parseInt(pValue);
				break;
			case "CommonRecipeLimit":
				COMMON_RECIPE_LIMIT = Integer.parseInt(pValue);
				break;
			case "CastleShieldRestriction":
				CASTLE_SHIELD = Boolean.valueOf(pValue);
				break;
			case "ClanHallShieldRestriction":
				CLANHALL_SHIELD = Boolean.valueOf(pValue);
				break;
			case "ApellaArmorsRestriction":
				APELLA_ARMORS = Boolean.valueOf(pValue);
				break;
			case "OathArmorsRestriction":
				OATH_ARMORS = Boolean.valueOf(pValue);
				break;
			case "CastleLordsCrownRestriction":
				CASTLE_CROWN = Boolean.valueOf(pValue);
				break;
			case "CastleCircletsRestriction":
				CASTLE_CIRCLETS = Boolean.valueOf(pValue);
				break;
			case "AllowWedding":
				MOD_ALLOW_WEDDING = Boolean.valueOf(pValue);
				break;
			case "WeddingPrice":
				MOD_WEDDING_PRICE = Integer.parseInt(pValue);
				break;
			case "AutoLootRaid":
				AUTO_LOOT_RAID = Boolean.parseBoolean(pValue);
				break;
			case "AutoLootGrand":
				AUTO_LOOT_GRAND = Boolean.parseBoolean(pValue);
				break;
			case "WeddingPunishInfidelity":
				MOD_WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(pValue);
				break;
			case "WeddingTeleport":
				MOD_WEDDING_TELEPORT = Boolean.parseBoolean(pValue);
				break;
			case "WeddingTeleportPrice":
				MOD_WEDDING_TELEPORT_PRICE = Integer.parseInt(pValue);
				break;
			case "WeddingTeleportDuration":
				MOD_WEDDING_TELEPORT_DURATION = Integer.parseInt(pValue);
				break;
			case "WeddingAllowSameSex":
				MOD_WEDDING_SAMESEX = Boolean.parseBoolean(pValue);
				break;
			case "WeddingFormalWear":
				MOD_WEDDING_FORMALWEAR = Boolean.parseBoolean(pValue);
				break;
			case "WeddingDivorceCosts":
				MOD_WEDDING_DIVORCE_COSTS = Integer.parseInt(pValue);
				break;
			case "AddExpAtPvp":
				ADD_EXP = Integer.parseInt(pValue);
				break;
			case "AddSpAtPvp":
				ADD_SP = Integer.parseInt(pValue);
				break;
			case "AbortRestart":
				ABORT_RR = (pValue);
				break;
			case "CTFEvenTeams":
				CTF_EVEN_TEAMS = pValue;
				break;
			case "CTFAllowInterference":
				CTF_ALLOW_INTERFERENCE = Boolean.parseBoolean(pValue);
				break;
			case "CTFAllowPotions":
				CTF_ALLOW_POTIONS = Boolean.parseBoolean(pValue);
				break;
			case "CTFAllowSummon":
				CTF_ALLOW_SUMMON = Boolean.parseBoolean(pValue);
				break;
			case "CTFOnStartRemoveAllEffects":
				CTF_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(pValue);
				break;
			case "CTFOnStartUnsummonPet":
				CTF_ON_START_UNSUMMON_PET = Boolean.parseBoolean(pValue);
				break;
			case "CTFReviveDelay":
				CTF_REVIVE_DELAY = Long.parseLong(pValue);
				break;
			case "TvTAllowInterference":
				TVT_ALLOW_INTERFERENCE = Boolean.parseBoolean(pValue);
				break;
			case "TvTAllowPotions":
				TVT_ALLOW_POTIONS = Boolean.parseBoolean(pValue);
				break;
			case "TvTAllowSummon":
				TVT_ALLOW_SUMMON = Boolean.parseBoolean(pValue);
				break;
			case "TvTOnStartRemoveAllEffects":
				TVT_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(pValue);
				break;
			case "TvTOnStartUnsummonPet":
				TVT_ON_START_UNSUMMON_PET = Boolean.parseBoolean(pValue);
				break;
			case "TvTEvenTeams":
				TVT_EVEN_TEAMS = pValue;
				break;
			case "DMAllowInterference":
				DM_ALLOW_INTERFERENCE = Boolean.parseBoolean(pValue);
				break;
			case "DMAllowPotions":
				DM_ALLOW_POTIONS = Boolean.parseBoolean(pValue);
				break;
			case "DMAllowSummon":
				DM_ALLOW_SUMMON = Boolean.parseBoolean(pValue);
				break;
			case "DMOnStartRemoveAllEffects":
				DM_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(pValue);
				break;
			case "DMOnStartUnsummonPet":
				DM_ON_START_UNSUMMON_PET = Boolean.parseBoolean(pValue);
				break;
			case "EnchantChanceWeapon":
				ENCHANT_CHANCE_WEAPON = Integer.parseInt(pValue);
				break;
			case "EnchantChanceArmor":
				ENCHANT_CHANCE_ARMOR = Integer.parseInt(pValue);
				break;
			case "EnchantChanceJewelry":
				ENCHANT_CHANCE_JEWELRY = Integer.parseInt(pValue);
				break;
			case "EnchantMaxWeapon":
				ENCHANT_MAX_WEAPON = Integer.parseInt(pValue);
				break;
			case "EnchantMaxArmor":
				ENCHANT_MAX_ARMOR = Integer.parseInt(pValue);
				break;
			case "EnchantMaxJewelry":
				ENCHANT_MAX_JEWELRY = Integer.parseInt(pValue);
				break;
			case "EnchantSafeMax":
				ENCHANT_SAFE_MAX = Integer.parseInt(pValue);
				break;
			case "EnchantSafeMaxFull":
				ENCHANT_SAFE_MAX_FULL = Integer.parseInt(pValue);
				break;
			case "MinimumPKRequiredToDrop":
				KARMA_PK_LIMIT = Integer.parseInt(pValue);
				break;
			case "PvPVsNormalTime":
				PVP_NORMAL_TIME = Integer.parseInt(pValue);
				break;
			case "PvPVsPvPTime":
				PVP_PVP_TIME = Integer.parseInt(pValue);
				break;
			case "GlobalChat":
				DEFAULT_GLOBAL_CHAT = pValue;
				break;
			case "TradeChat":
				DEFAULT_TRADE_CHAT = pValue;
				break;
			default:
				try
				{
					// TODO: stupid GB configs...
					if (!pName.startsWith("Interval_") && !pName.startsWith("Random_"))
					{
						pName = pName.toUpperCase();
					}
					Field clazField = Config.class.getField(pName);
					int modifiers = clazField.getModifiers();
					// just in case :)
					if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers) || Modifier.isFinal(modifiers))
					{
						throw new SecurityException("Cannot modify non public, non static or final config!");
					}
					
					if (clazField.getType() == int.class)
					{
						clazField.setInt(clazField, Integer.parseInt(pValue));
					}
					else if (clazField.getType() == short.class)
					{
						clazField.setShort(clazField, Short.parseShort(pValue));
					}
					else if (clazField.getType() == byte.class)
					{
						clazField.setByte(clazField, Byte.parseByte(pValue));
					}
					else if (clazField.getType() == long.class)
					{
						clazField.setLong(clazField, Long.parseLong(pValue));
					}
					else if (clazField.getType() == float.class)
					{
						clazField.setFloat(clazField, Float.parseFloat(pValue));
					}
					else if (clazField.getType() == double.class)
					{
						clazField.setDouble(clazField, Double.parseDouble(pValue));
					}
					else if (clazField.getType() == boolean.class)
					{
						clazField.setBoolean(clazField, Boolean.parseBoolean(pValue));
					}
					else if (clazField.getType() == String.class)
					{
						clazField.set(clazField, pValue);
					}
					else
					{
						return false;
					}
				}
				catch (NoSuchFieldException e)
				{
					return false;
				}
				catch (Exception e)
				{
					_log.warning(Config.class.getName() + ": Grandboss configuration error");
					return false;
				}
		}
		return true;
	}

	public static void saveHexid(int serverId, String string)
	{
		Config.saveHexid(serverId, string, HEXID_FILE);
	}
	
	public static void saveHexid(int serverId, String hexId, String fileName)
	{
		File file = new File(fileName);
		try (OutputStream out = new FileOutputStream(file))
		{
			Properties hexSetting = new Properties();
			// Create a new empty file only if it doesn't exist
			file.createNewFile();
			hexSetting.setProperty("ServerID", String.valueOf(serverId));
			hexSetting.setProperty("HexID", hexId);
			hexSetting.store(out, "the hexID to auth into login.");
		}
		catch (Exception e)
		{
			_log.warning(Config.class.getName() + ": Config: Failed to save hex id to " + fileName + " File.");
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
		}
	}
	
	private static int[][] parseItemsList(String line)
	{
		final String[] propertySplit = line.split(";");
		if (propertySplit.length == 0)
			return null;
		
		int i = 0;
		String[] valueSplit;
		final int[][] result = new int[propertySplit.length][];
		for (String value : propertySplit)
		{
			valueSplit = value.split(",");
			if (valueSplit.length != 2)
			{
				_log.warning(Config.class.getName() + StringUtil.concat("parseItemsList[Config.load()]: invalid entry -> \"", valueSplit[0], "\", should be itemId,itemNumber"));
				return null;
			}
			
			result[i] = new int[2];
			try
			{
				result[i][0] = Integer.parseInt(valueSplit[0]);
			}
			catch (NumberFormatException e)
			{
				_log.warning(Config.class.getName() + StringUtil.concat("parseItemsList[Config.load()]: invalid itemId -> \"", valueSplit[0], "\""));
				return null;
			}
			
			try
			{
				result[i][1] = Integer.parseInt(valueSplit[1]);
			}
			catch (NumberFormatException e)
			{
				_log.warning(Config.class.getName() + StringUtil.concat("parseItemsList[Config.load()]: invalid item number -> \"", valueSplit[1], "\""));
				return null;
			}
			i++;
		}
		return result;
	}
}