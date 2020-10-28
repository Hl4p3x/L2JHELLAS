package com.l2jhellas.gameserver;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import Extensions.IpCatcher;
import Extensions.AchievmentsEngine.AchievementsManager;
import Extensions.Balancer.BalanceLoad;
import Extensions.RankSystem.RankLoader;
import Extensions.Vote.VoteRewardHopzone;
import Extensions.Vote.VoteRewardTopzone;
import Extensions.fake.roboto.FakePlayerManager;

import com.L2JHellasInfo;
import com.PackRoot;
import com.l2jhellas.Config;
import com.l2jhellas.Server;
import com.l2jhellas.gameserver.cache.CrestCache;
import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.communitybbs.HeroeList;
import com.l2jhellas.gameserver.communitybbs.Manager.ForumsBBSManager;
import com.l2jhellas.gameserver.controllers.GameTimeController;
import com.l2jhellas.gameserver.controllers.RecipeController;
import com.l2jhellas.gameserver.controllers.TradeController;
import com.l2jhellas.gameserver.datatables.sql.CharNameTable;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.datatables.sql.NpcBufferSkillIdsTable;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.datatables.sql.SpawnTable;
import com.l2jhellas.gameserver.datatables.xml.AdminData;
import com.l2jhellas.gameserver.datatables.xml.ArmorSetsData;
import com.l2jhellas.gameserver.datatables.xml.AugmentationData;
import com.l2jhellas.gameserver.datatables.xml.CharTemplateData;
import com.l2jhellas.gameserver.datatables.xml.DoorData;
import com.l2jhellas.gameserver.datatables.xml.ExtractableItemData;
import com.l2jhellas.gameserver.datatables.xml.FishTable;
import com.l2jhellas.gameserver.datatables.xml.HelperBuffData;
import com.l2jhellas.gameserver.datatables.xml.HennaData;
import com.l2jhellas.gameserver.datatables.xml.IconData;
import com.l2jhellas.gameserver.datatables.xml.LevelUpData;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.datatables.xml.MultisellData;
import com.l2jhellas.gameserver.datatables.xml.NpcWalkerRoutesData;
import com.l2jhellas.gameserver.datatables.xml.PetData;
import com.l2jhellas.gameserver.datatables.xml.RecipeData;
import com.l2jhellas.gameserver.datatables.xml.SkillSpellbookData;
import com.l2jhellas.gameserver.datatables.xml.SkillTreeData;
import com.l2jhellas.gameserver.datatables.xml.SoulCrystalsTable;
import com.l2jhellas.gameserver.datatables.xml.StaticObjData;
import com.l2jhellas.gameserver.datatables.xml.SummonItemsData;
import com.l2jhellas.gameserver.datatables.xml.TeleportLocationData;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.handler.AutoAnnouncementHandler;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.instancemanager.AuctionManager;
import com.l2jhellas.gameserver.instancemanager.BoatManager;
import com.l2jhellas.gameserver.instancemanager.BufferManager;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.CastleManorManager;
import com.l2jhellas.gameserver.instancemanager.ClanHallManager;
import com.l2jhellas.gameserver.instancemanager.CoupleManager;
import com.l2jhellas.gameserver.instancemanager.CrownManager;
import com.l2jhellas.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jhellas.gameserver.instancemanager.DayNightSpawnManager;
import com.l2jhellas.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jhellas.gameserver.instancemanager.DuelManager;
import com.l2jhellas.gameserver.instancemanager.FourSepulchersManager;
import com.l2jhellas.gameserver.instancemanager.GrandBossManager;
import com.l2jhellas.gameserver.instancemanager.ItemsOnGroundManager;
import com.l2jhellas.gameserver.instancemanager.MercTicketManager;
import com.l2jhellas.gameserver.instancemanager.PetitionManager;
import com.l2jhellas.gameserver.instancemanager.QuestManager;
import com.l2jhellas.gameserver.instancemanager.RaidBossPointsManager;
import com.l2jhellas.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2jhellas.gameserver.instancemanager.SiegeManager;
import com.l2jhellas.gameserver.instancemanager.ZoneManager;
import com.l2jhellas.gameserver.model.AutoChatHandler;
import com.l2jhellas.gameserver.model.AutoSpawnHandler;
import com.l2jhellas.gameserver.model.L2Manor;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoomList;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchWaitingList;
import com.l2jhellas.gameserver.model.entity.Hero;
import com.l2jhellas.gameserver.model.entity.events.engines.EventBuffer;
import com.l2jhellas.gameserver.model.entity.events.engines.EventConfig;
import com.l2jhellas.gameserver.model.entity.olympiad.Olympiad;
import com.l2jhellas.gameserver.model.entity.olympiad.OlympiadGameManager;
import com.l2jhellas.gameserver.network.L2GameClient;
import com.l2jhellas.gameserver.network.L2GamePacketHandler;
import com.l2jhellas.gameserver.scrips.boats.BoatGiranTalking;
import com.l2jhellas.gameserver.scrips.boats.BoatGludinRune;
import com.l2jhellas.gameserver.scrips.boats.BoatInnadrilTour;
import com.l2jhellas.gameserver.scrips.boats.BoatRunePrimeval;
import com.l2jhellas.gameserver.scrips.boats.BoatTalkingGludin;
import com.l2jhellas.gameserver.scrips.loaders.MasterHandler;
import com.l2jhellas.gameserver.scrips.loaders.ScriptLoader;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.taskmanager.MemoryWatchOptimize;
import com.l2jhellas.gameserver.taskmanager.PvpFlagTaskManager;
import com.l2jhellas.gameserver.taskmanager.RandomAnimationTaskManager;
import com.l2jhellas.gameserver.taskmanager.TaskManager;
import com.l2jhellas.mmocore.network.SelectorConfig;
import com.l2jhellas.mmocore.network.SelectorThread;
import com.l2jhellas.shield.antibot.AntiBot;
import com.l2jhellas.util.Util;
import com.l2jhellas.util.database.L2DatabaseFactory;
import com.l2jhellas.util.hexid.HexId;
import com.l2jhellas.util.ip.GameServerIP;
import com.l2jhellas.util.ip.IPConfigData;

public class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());
	private final SelectorThread<L2GameClient> _selectorThread;
	public static boolean _instanceOk = false;
	public static GameServer gameServer;
	private final LoginServerThread _loginThread;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	
	public Gui gui;
	long freeMemBefore = 0;
	private String optimizer = "";
	
	public SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}
	
	public GameServer() throws Exception
	{
		gameServer = this;
		long serverLoadStart = System.currentTimeMillis();
		ThreadPoolManager.getInstance();
		
		Util.printSection("Chache");
		HtmCache.getInstance();
		CrestCache.load();
		
		Util.printSection("IdFactory");
		IdFactory.getInstance();

		Util.printSection("World");
		GameTimeController.init();
		L2World.getInstance();
		MapRegionTable.getInstance();
		StaticObjData.getInstance();
		TeleportLocationData.getInstance();
			
		Util.printSection("Geodata");
		if (Config.GEODATA)
			GeoEngine.loadGeo();
		else
			_log.info(GameServer.class.getSimpleName() + ":GeoEngine disabled by Config.");	
		
		Util.printSection("Skills");
		SkillTable.getInstance();
		SkillTreeData.getInstance();
		SkillSpellbookData.getInstance();
		NpcBufferSkillIdsTable.getInstance();
		
		Util.printSection("Zone");
		ZoneManager.getInstance();
					
		Util.printSection("Items");
		ItemTable.getInstance();
		ArmorSetsData.getInstance();
		SummonItemsData.getInstance();
		SoulCrystalsTable.getInstance();	
		IconData.getInstance();
		ExtractableItemData.getInstance();
		
		Util.printSection("Npc");
		NpcData.getInstance();
		BufferManager.getInstance();
		
		Util.printSection("Announcements-AutoSpawn-Chat");
		Announcements.getInstance();
		AutoAnnouncementHandler.getInstance();
		AutoChatHandler.getInstance();
		
		Util.printSection("Characters");
		if (Config.COMMUNITY_TYPE.equals("Full"))
			ForumsBBSManager.getInstance();

		if (Config.ALLOWFISHING)
			FishTable.getInstance();
		
		if (Config.ALLOW_NPC_WALKERS)
			NpcWalkerRoutesData.getInstance();
		
		CharNameTable.getInstance();
		ClanTable.getInstance();
		CharTemplateData.getInstance();
		LevelUpData.getInstance();
		CrownManager.getInstance();
		AdminData.getInstance();
		HennaData.getInstance();
		HelperBuffData.getInstance();
		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		DuelManager.getInstance();
		AugmentationData.getInstance();

		Util.printSection("Spawn");
		SpawnTable.getInstance();
		DayNightSpawnManager.getInstance().notifyChangeMode();
		AutoSpawnHandler.getInstance();
		SevenSignsFestival.getInstance();
		SevenSigns.getInstance().spawnSevenSignsNPC();// Spawn the Orators/Preachers if in the Seal Validation period.
		DoorData.getInstance();
	
		Util.printSection("Economy");
		TradeController.getInstance();
		MultisellData.getInstance();		
				
		Util.printSection("Castles-Clan Halls");
		CastleManager.getInstance();
		SiegeManager.getInstance();
		ClanHallManager.getInstance();
		AuctionManager.getInstance();
		
		Util.printSection("RaidBos");
		RaidBossSpawnManager.getInstance();
		GrandBossManager.getInstance();
		RaidBossPointsManager.getInstance();
		
		Util.printSection("Dimensional Rift");
		DimensionalRiftManager.getInstance();
		
		Util.printSection("Misc");
		RecipeData.getInstance();
		RecipeController.getInstance();
		MonsterRace.getInstance();
		MercTicketManager.getInstance();
		PetitionManager.getInstance();
		CursedWeaponsManager.getInstance();
		PetData.getInstance();
		
		Util.printSection("FourSepulcher");
		FourSepulchersManager.getInstance().init();
		
		if (Config.SAVE_DROPPED_ITEM)
			ItemsOnGroundManager.getInstance();
		
		ItemsAutoDestroy.getInstance().CheckItemsForDestroy();
				
		Util.printSection("Tasks");
		TaskManager.getInstance();
		PvpFlagTaskManager.getInstance();
		RandomAnimationTaskManager.getInstance();
		
		Util.printSection("Manor");
		L2Manor.getInstance();
		CastleManorManager.getInstance();

		Util.printSection("Olympiad System");
		OlympiadGameManager.getInstance();
		Olympiad.getInstance();
		Hero.getInstance();
		
		Util.printSection("Scripts");
		if (!Config.ALT_DEV_NO_SCRIPT)
		{
			ScriptLoader.getInstance();
			QuestManager.getInstance().report();
			MasterHandler.getInstance();
			
		}
		else
			_log.info(GameServer.class.getSimpleName() + ": Scripts are disabled by Config.");
		
		if (Config.ALLOW_BOAT)
		{
			BoatManager.getInstance();
			BoatGiranTalking.load();
			BoatGludinRune.load();
			BoatInnadrilTour.load();
			BoatRunePrimeval.load();
			BoatTalkingGludin.load();
		}
		
		Util.printSection("Customs");
		RunCustoms();
			
		Util.printSection("Game Server Info");
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());

		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		final L2GamePacketHandler gph = new L2GamePacketHandler();
		_selectorThread = new SelectorThread<>(sc, gph, gph, gph, null);
		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (UnknownHostException e1)
			{
				_log.severe(GameServer.class.getName() + ": WARNING: The GameServer bind address is invalid, using all avaliable IPs. Reason: ");
				if (Config.DEVELOPER)
					e1.printStackTrace();
			}
		}
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
		}
		catch (IOException e)
		{
			_log.severe(GameServer.class.getName() + ": FATAL: Failed to open server socket. Reason: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
			System.exit(1);
		}
		
		_selectorThread.start();
		
		RunOptimizer();
		
		Util.printRuntimeInfo();
		_log.info(GameServer.class.getSimpleName() + ": Maximum Users On: " + Config.MAXIMUM_ONLINE_USERS);
		long serverLoadEnd = System.currentTimeMillis();
		_log.info(GameServer.class.getSimpleName() + ": Server Started in: " + ((serverLoadEnd - serverLoadStart) / 1000) + " seconds");
		
		Toolkit.getDefaultToolkit().beep();
		_loginThread = LoginServerThread.getInstance();
		_loginThread.start();
		
		_log.info(optimizer);		
		
		if (Config.ENABLE_GUI)
			gui = new Gui();		
	}
	
	private void RunOptimizer()
	{
		Util.gc(2, 100);
		freeMemBefore = MemoryWatchOptimize.getMemFree();
		
		Util.gc(2, 100);
		optimizer = String.format("%s Optimized ~%d Mb of memory", optimizer, (MemoryWatchOptimize.getMemFree() - freeMemBefore) / 0x100000);
		
	}
	
	private static void RunCustoms()
	{
		AchievementsManager.getInstance();
		
		if (Config.ALLOW_TOPZONE_VOTE_REWARD)
			VoteRewardTopzone.LoadTopZone();
		if (Config.ALLOW_HOPZONE_VOTE_REWARD)
			VoteRewardHopzone.LoadHopZone();
		
		// Rank System.
		if (Config.RANK_PVP_SYSTEM_ENABLED)
			RankLoader.load();
		else
			_log.log(Level.INFO, "Rank PvP System: Disabled");
		
		BalanceLoad.LoadEm();
		
		if (Config.ALLOW_SEQURITY_QUE)
			AntiBot.getInstance();

		if (Config.RESTART_BY_TIME_OF_DAY)
		{
			_log.info("Restart System: Auto Restart System is Enabled.");
			Restart.getInstance().StartCalculationOfNextRestartTime();
		}
		else
			_log.info("Restart System: Auto Restart System is Disabled.");
		
		if (Config.MOD_ALLOW_WEDDING)
			CoupleManager.getInstance();
		
		IpCatcher.ipsLoad();
			
		if (Config.ALLOW_FAKE_PLAYERS)
		   FakePlayerManager.initialise();
		else
			_log.log(Level.INFO, "FakePlayer System: Disabled");	
		
		EventConfig.getInstance();
		EventBuffer.getInstance();		
		HeroeList.loadFromDB();
		HeroeList.startCheck();
	}
	
	public static void main(String[] args) throws Exception
	{
		Server.serverMode = Server.MODE_GAMESERVER;
		// Pack Root
		PackRoot.load();
		
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./config/Others/log.cfg"; // Name of log file
		
		if (Config.USE_SAY_FILTER)
			new File(PackRoot.DATAPACK_ROOT, "config/Others/ChatFilter.txt").createNewFile();
		
		// Create directories
		File logFolder = new File(PackRoot.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();
		File clans = new File(PackRoot.DATAPACK_ROOT, "data/clans");
		clans.mkdir();
		File crests = new File(PackRoot.DATAPACK_ROOT, "data/crests");
		crests.mkdir();
		File pathnode = new File(PackRoot.DATAPACK_ROOT, "data/pathnode");
		pathnode.mkdir();
		File geodata = new File(PackRoot.DATAPACK_ROOT, "data/geodata");
		geodata.mkdir();
		File donates = new File(PackRoot.DATAPACK_ROOT, "data/donates");
		donates.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		InputStream is = new FileInputStream(new File(LOG_NAME));
		LogManager.getLogManager().readConfiguration(is);
		is.close();
		
		// HexID part 1 (file)
		HexId.load();
		
		// IP Config
		Util.printSection("Network");
		IPConfigData.load();
		GameServerIP.load();
		
		Util.printSection("Configs");
		Config.load();
		
		Util.printSection("General Info");
		Util.printGeneralSystemInfo();
		
		Util.printSection("DataBase");
		L2DatabaseFactory.getInstance();
		
		// HexID part 2 (database must be load after driver)
		HexId.storeDB();
		
		Util.printSection("Team");
		L2JHellasInfo.showInfo();
		
		gameServer = new GameServer();
	}
}