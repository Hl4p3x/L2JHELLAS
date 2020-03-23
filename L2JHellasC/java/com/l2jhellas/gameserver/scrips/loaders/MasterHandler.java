package com.l2jhellas.gameserver.scrips.loaders;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import Extensions.Balancer.Balancer;
import Extensions.fake.roboto.admincommands.AdminFakePlayers;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.handler.AdminCommandHandler;
import com.l2jhellas.gameserver.handler.ChatHandler;
import com.l2jhellas.gameserver.handler.IHandler;
import com.l2jhellas.gameserver.handler.ItemHandler;
import com.l2jhellas.gameserver.handler.SkillHandler;
import com.l2jhellas.gameserver.handler.UserCommandHandler;
import com.l2jhellas.gameserver.handler.VoicedCommandHandler;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminAdmin;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminAnnouncements;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminAutoAnnouncements;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminBBS;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminBan;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminBanChat;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminCTFEngine;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminCache;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminChangeAccessLevel;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminClanFull;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminCreateItem;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminCursedWeapons;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminDMEngine;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminDelete;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminDeport;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminDonator;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminDoorControl;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminEditChar;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminEditNpc;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminEffects;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminEnchant;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminEventEngine;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminExpSp;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminFence;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminFightCalculator;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminGeodata;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminGmChat;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminHeal;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminHelpPage;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminHero;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminInvul;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminKick;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminKill;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminLevel;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminLogin;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminMammon;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminManor;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminMenu;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminMobGroup;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminMonsterRace;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminNoble;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminPForge;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminPathNode;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminPetition;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminPledge;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminPolymorph;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminPremium;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminReload;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminRepairChar;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminRes;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminRideWyvern;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminSearch;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminShop;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminShutdown;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminSiege;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminSkill;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminSpawn;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminTarget;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminTeleport;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminTest;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminTvTEngine;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminUnblockIp;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminWho;
import com.l2jhellas.gameserver.handlers.admincommandhandlers.AdminZone;
import com.l2jhellas.gameserver.handlers.chathandlers.ChatAll;
import com.l2jhellas.gameserver.handlers.chathandlers.ChatAlliance;
import com.l2jhellas.gameserver.handlers.chathandlers.ChatClan;
import com.l2jhellas.gameserver.handlers.chathandlers.ChatHeroVoice;
import com.l2jhellas.gameserver.handlers.chathandlers.ChatParty;
import com.l2jhellas.gameserver.handlers.chathandlers.ChatPartyRoomAll;
import com.l2jhellas.gameserver.handlers.chathandlers.ChatPartyRoomCommander;
import com.l2jhellas.gameserver.handlers.chathandlers.ChatPetition;
import com.l2jhellas.gameserver.handlers.chathandlers.ChatShout;
import com.l2jhellas.gameserver.handlers.chathandlers.ChatTell;
import com.l2jhellas.gameserver.handlers.chathandlers.ChatTrade;
import com.l2jhellas.gameserver.handlers.itemhandlers.BeastSoulShot;
import com.l2jhellas.gameserver.handlers.itemhandlers.BeastSpice;
import com.l2jhellas.gameserver.handlers.itemhandlers.BeastSpiritShot;
import com.l2jhellas.gameserver.handlers.itemhandlers.BlessedSpiritShot;
import com.l2jhellas.gameserver.handlers.itemhandlers.Book;
import com.l2jhellas.gameserver.handlers.itemhandlers.BreakingArrow;
import com.l2jhellas.gameserver.handlers.itemhandlers.CharChangePotions;
import com.l2jhellas.gameserver.handlers.itemhandlers.ChestKey;
import com.l2jhellas.gameserver.handlers.itemhandlers.ChristmasTree;
import com.l2jhellas.gameserver.handlers.itemhandlers.CompBlessedSpiritShotPacks;
import com.l2jhellas.gameserver.handlers.itemhandlers.CompShotPacks;
import com.l2jhellas.gameserver.handlers.itemhandlers.CompSpiritShotPacks;
import com.l2jhellas.gameserver.handlers.itemhandlers.CrystalCarol;
import com.l2jhellas.gameserver.handlers.itemhandlers.Crystals;
import com.l2jhellas.gameserver.handlers.itemhandlers.EnchantScrolls;
import com.l2jhellas.gameserver.handlers.itemhandlers.EnergyStone;
import com.l2jhellas.gameserver.handlers.itemhandlers.Firework;
import com.l2jhellas.gameserver.handlers.itemhandlers.FishShots;
import com.l2jhellas.gameserver.handlers.itemhandlers.Harvester;
import com.l2jhellas.gameserver.handlers.itemhandlers.JackpotSeed;
import com.l2jhellas.gameserver.handlers.itemhandlers.MOSKey;
import com.l2jhellas.gameserver.handlers.itemhandlers.MapForestOfTheDead;
import com.l2jhellas.gameserver.handlers.itemhandlers.Maps;
import com.l2jhellas.gameserver.handlers.itemhandlers.MercTicket;
import com.l2jhellas.gameserver.handlers.itemhandlers.MysteryPotion;
import com.l2jhellas.gameserver.handlers.itemhandlers.Nectar;
import com.l2jhellas.gameserver.handlers.itemhandlers.PaganKeys;
import com.l2jhellas.gameserver.handlers.itemhandlers.Potions;
import com.l2jhellas.gameserver.handlers.itemhandlers.Primeval;
import com.l2jhellas.gameserver.handlers.itemhandlers.Recipes;
import com.l2jhellas.gameserver.handlers.itemhandlers.Remedy;
import com.l2jhellas.gameserver.handlers.itemhandlers.RollingDice;
import com.l2jhellas.gameserver.handlers.itemhandlers.ScrollOfEscape;
import com.l2jhellas.gameserver.handlers.itemhandlers.ScrollOfResurrection;
import com.l2jhellas.gameserver.handlers.itemhandlers.Scrolls;
import com.l2jhellas.gameserver.handlers.itemhandlers.Seed;
import com.l2jhellas.gameserver.handlers.itemhandlers.SevenSignsRecord;
import com.l2jhellas.gameserver.handlers.itemhandlers.SoulCrystals;
import com.l2jhellas.gameserver.handlers.itemhandlers.SoulShots;
import com.l2jhellas.gameserver.handlers.itemhandlers.SpiritShot;
import com.l2jhellas.gameserver.handlers.itemhandlers.SummonItems;
import com.l2jhellas.gameserver.handlers.skillhandlers.BalanceLife;
import com.l2jhellas.gameserver.handlers.skillhandlers.BeastFeed;
import com.l2jhellas.gameserver.handlers.skillhandlers.Blow;
import com.l2jhellas.gameserver.handlers.skillhandlers.Charge;
import com.l2jhellas.gameserver.handlers.skillhandlers.ClanGate;
import com.l2jhellas.gameserver.handlers.skillhandlers.CombatPointHeal;
import com.l2jhellas.gameserver.handlers.skillhandlers.Continuous;
import com.l2jhellas.gameserver.handlers.skillhandlers.CpDam;
import com.l2jhellas.gameserver.handlers.skillhandlers.Craft;
import com.l2jhellas.gameserver.handlers.skillhandlers.DeluxeKey;
import com.l2jhellas.gameserver.handlers.skillhandlers.Disablers;
import com.l2jhellas.gameserver.handlers.skillhandlers.DrainSoul;
import com.l2jhellas.gameserver.handlers.skillhandlers.Extractable;
import com.l2jhellas.gameserver.handlers.skillhandlers.Fishing;
import com.l2jhellas.gameserver.handlers.skillhandlers.FishingSkill;
import com.l2jhellas.gameserver.handlers.skillhandlers.GetPlayer;
import com.l2jhellas.gameserver.handlers.skillhandlers.GiveSp;
import com.l2jhellas.gameserver.handlers.skillhandlers.Harvest;
import com.l2jhellas.gameserver.handlers.skillhandlers.Heal;
import com.l2jhellas.gameserver.handlers.skillhandlers.ManaHeal;
import com.l2jhellas.gameserver.handlers.skillhandlers.Manadam;
import com.l2jhellas.gameserver.handlers.skillhandlers.Mdam;
import com.l2jhellas.gameserver.handlers.skillhandlers.Pdam;
import com.l2jhellas.gameserver.handlers.skillhandlers.Recall;
import com.l2jhellas.gameserver.handlers.skillhandlers.Resurrect;
import com.l2jhellas.gameserver.handlers.skillhandlers.SiegeFlag;
import com.l2jhellas.gameserver.handlers.skillhandlers.Sow;
import com.l2jhellas.gameserver.handlers.skillhandlers.Spoil;
import com.l2jhellas.gameserver.handlers.skillhandlers.StrSiegeAssault;
import com.l2jhellas.gameserver.handlers.skillhandlers.SummonFriend;
import com.l2jhellas.gameserver.handlers.skillhandlers.SummonTreasureKey;
import com.l2jhellas.gameserver.handlers.skillhandlers.Sweep;
import com.l2jhellas.gameserver.handlers.skillhandlers.TakeCastle;
import com.l2jhellas.gameserver.handlers.skillhandlers.Unlock;
import com.l2jhellas.gameserver.handlers.usercommandhandlers.ChannelDelete;
import com.l2jhellas.gameserver.handlers.usercommandhandlers.ChannelLeave;
import com.l2jhellas.gameserver.handlers.usercommandhandlers.ChannelListUpdate;
import com.l2jhellas.gameserver.handlers.usercommandhandlers.ClanPenalty;
import com.l2jhellas.gameserver.handlers.usercommandhandlers.ClanWarsList;
import com.l2jhellas.gameserver.handlers.usercommandhandlers.DisMount;
import com.l2jhellas.gameserver.handlers.usercommandhandlers.Escape;
import com.l2jhellas.gameserver.handlers.usercommandhandlers.Loc;
import com.l2jhellas.gameserver.handlers.usercommandhandlers.Mount;
import com.l2jhellas.gameserver.handlers.usercommandhandlers.OlympiadStat;
import com.l2jhellas.gameserver.handlers.usercommandhandlers.PartyInfo;
import com.l2jhellas.gameserver.handlers.usercommandhandlers.PvpInfo;
import com.l2jhellas.gameserver.handlers.usercommandhandlers.SiegeStatus;
import com.l2jhellas.gameserver.handlers.usercommandhandlers.Time;
import com.l2jhellas.gameserver.handlers.voicedcommandhandlers.BankingCmd;
import com.l2jhellas.gameserver.handlers.voicedcommandhandlers.CastleCmd;
import com.l2jhellas.gameserver.handlers.voicedcommandhandlers.OnlinePlayersCmd;
import com.l2jhellas.gameserver.handlers.voicedcommandhandlers.PMonoffCmd;
import com.l2jhellas.gameserver.handlers.voicedcommandhandlers.PremiumCmd;
import com.l2jhellas.gameserver.handlers.voicedcommandhandlers.PvpInfoCmd;
import com.l2jhellas.gameserver.handlers.voicedcommandhandlers.ServerRestartVoteCmd;
import com.l2jhellas.gameserver.handlers.voicedcommandhandlers.TradeonoffCmd;
import com.l2jhellas.gameserver.handlers.voicedcommandhandlers.VipTeleportCmd;
import com.l2jhellas.gameserver.handlers.voicedcommandhandlers.VoiceInfoCmd;
import com.l2jhellas.gameserver.handlers.voicedcommandhandlers.WeddingCmd;
import com.l2jhellas.gameserver.handlers.voicedcommandhandlers.ZodiacRegistrationCmd;
import com.l2jhellas.util.Util;

public class MasterHandler
{
	private static final Logger _log = Logger.getLogger(MasterHandler.class.getName());
	
	public static MasterHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public MasterHandler()
	{
		Map<IHandler<?, ?>, Method> registerHandlerMethods = new HashMap<>();
		for (IHandler<?, ?> loadInstance : _loadInstances)
		{
			registerHandlerMethods.put(loadInstance, null);
			for (Method method : loadInstance.getClass().getMethods())
			{
				if (method.getName().equals("registerHandler") && !method.isBridge())
					registerHandlerMethods.put(loadInstance, method);
			}
		}
		
		registerHandlerMethods.entrySet().stream().filter(e -> e.getValue() == null).forEach(e ->
		{
			_log.warning(MasterHandler.class.getName() + ": Failed loading handlers of: " + e.getKey().getClass().getSimpleName() + " seems registerHandler function does not exist.");
		});
		
		for (Class<?> classes[] : _handlers)
		{
			for (Class<?> c : classes)
			{
				if (c == null)
					continue; // Disabled handler
				
				try
				{
					Object handler = c.newInstance();
					for (Entry<IHandler<?, ?>, Method> entry : registerHandlerMethods.entrySet())
					{
						if ((entry.getValue() != null) && entry.getValue().getParameterTypes()[0].isInstance(handler))
							entry.getValue().invoke(entry.getKey(), handler);
					}
				}
				catch (Exception e)
				{
					_log.warning(MasterHandler.class.getName() + ": " + "Failed loading handler: " + c.getSimpleName());
					continue;
				}
			}
		}
		
		Util.printSection(MasterHandler.class.getSimpleName());
		for (IHandler<?, ?> loadInstance : _loadInstances)
			_log.info(loadInstance.getClass().getSimpleName() + ": Loaded " + loadInstance.size() + " Handlers.");
	}
	
	private static final IHandler<?, ?>[] _loadInstances =
	{
		AdminCommandHandler.getInstance(),
		ChatHandler.getInstance(),
		ItemHandler.getInstance(),
		SkillHandler.getInstance(),
		UserCommandHandler.getInstance(),
		VoicedCommandHandler.getInstance(),
	};
	
	private static final Class<?>[][] _handlers =
	{// formatter:off
		{
			// Admin Command Handlers
			AdminAdmin.class,
			AdminAnnouncements.class,
			AdminAutoAnnouncements.class,
			Balancer.class,
			AdminBan.class,
			AdminBanChat.class,
			AdminBBS.class,
			AdminCache.class,
			AdminChangeAccessLevel.class,
			AdminClanFull.class,
			AdminCreateItem.class,
			AdminCTFEngine.class,
			AdminCursedWeapons.class,
			AdminDelete.class,
			AdminDeport.class,
			AdminDMEngine.class,
			AdminDonator.class,
			AdminDoorControl.class,
			AdminEditChar.class,
			AdminEditNpc.class,
			AdminEffects.class,
			AdminEnchant.class,
			AdminEventEngine.class,
			AdminExpSp.class,
			AdminFightCalculator.class,
			AdminGeodata.class,
			AdminGmChat.class,
			AdminHeal.class,
			AdminHelpPage.class,
			AdminHero.class,
			AdminInvul.class,
			AdminKick.class,
			AdminKill.class,
			AdminLevel.class,
			AdminLogin.class,
			AdminMammon.class,
			AdminManor.class,
			AdminMenu.class,
			AdminMobGroup.class,
			AdminMonsterRace.class,
			AdminNoble.class,
			AdminPathNode.class,
			AdminPetition.class,
			AdminPForge.class,
			AdminPledge.class,
			AdminPolymorph.class,
			AdminPremium.class,
			AdminReload.class,
			AdminRepairChar.class,
			AdminRes.class,
			AdminRideWyvern.class,
			AdminSearch.class,
			AdminShop.class,
			AdminShutdown.class,
			AdminSiege.class,
			AdminSkill.class,
			AdminSpawn.class,
			AdminTarget.class,
			AdminTeleport.class,
			AdminTest.class,
			AdminTvTEngine.class,
			AdminUnblockIp.class,
			AdminZone.class,
			AdminWho.class,
			AdminFence.class,
			AdminFakePlayers.class,
		},
		{
			// Chat Handlers
			ChatAll.class,
			ChatAlliance.class,
			// ChatBattlefield.class,
			ChatClan.class,
			ChatHeroVoice.class,
			ChatParty.class,
			// ChatPartyMatchRoom.class,
			ChatPartyRoomAll.class,
			ChatPartyRoomCommander.class,
			ChatPetition.class,
			ChatShout.class,
			ChatTell.class,
			ChatTrade.class,
		},
		{
			// Item Handlers
			BeastSoulShot.class,
			BeastSpice.class,
			BeastSpiritShot.class,
			BlessedSpiritShot.class,
			Book.class,
			BreakingArrow.class,
			CharChangePotions.class,
			ChestKey.class,
			ChristmasTree.class,
			CompBlessedSpiritShotPacks.class,
			CompShotPacks.class,
			CompSpiritShotPacks.class,
			CrystalCarol.class,
			Crystals.class,
			EnchantScrolls.class,
			EnergyStone.class,
			Firework.class,
			FishShots.class,
			Harvester.class,
			JackpotSeed.class,
			MapForestOfTheDead.class,
			Maps.class,
			MercTicket.class,
			MOSKey.class,
			MysteryPotion.class,
			Nectar.class,
			PaganKeys.class,
			Potions.class,
			Primeval.class,
			Recipes.class,
			Remedy.class,
			RollingDice.class,
			ScrollOfEscape.class,
			ScrollOfResurrection.class,
			Scrolls.class,
			Seed.class,
			SevenSignsRecord.class,
			SoulCrystals.class,
			SoulShots.class,
			SpiritShot.class,
			SummonItems.class,
		},
		{
			// Skill Handlers
			Extractable.class,
			BalanceLife.class,
			BeastFeed.class,
			Blow.class,
			Charge.class,
			ClanGate.class,
			CombatPointHeal.class,
			Continuous.class,
			CpDam.class,
			Craft.class,
			DeluxeKey.class,
			Disablers.class,
			DrainSoul.class,
			Fishing.class,
			FishingSkill.class,
			GetPlayer.class,
			GiveSp.class,
			Harvest.class,
			Heal.class,
			Manadam.class,
			ManaHeal.class,
			Mdam.class,
			Pdam.class,
			Recall.class,
			Resurrect.class,
			SiegeFlag.class,
			Sow.class,
			Spoil.class,
			StrSiegeAssault.class,
			SummonFriend.class,
			SummonTreasureKey.class,
			Sweep.class,
			TakeCastle.class,
			Unlock.class,
		},
		{
			// User Command Handlers
			ChannelDelete.class,
			ChannelLeave.class,
			ChannelListUpdate.class,
			ClanPenalty.class,
			ClanWarsList.class,
			DisMount.class,
			Escape.class,
			Loc.class,
			Mount.class,
			OlympiadStat.class,
			PartyInfo.class,
			(Config.RANK_PVP_SYSTEM_ENABLED && Config.PVP_INFO_USER_COMMAND_ENABLED && Config.PVP_INFO_COMMAND_ENABLED ? PvpInfo.class : null),
			SiegeStatus.class,
			Time.class,
		},
		{
			// Voiced Command Handlers
			(Config.BANKING_SYSTEM_ENABLED ? BankingCmd.class : null),
			CastleCmd.class,
			(Config.ONLINE_VOICE_ALLOW ? OnlinePlayersCmd.class : null),
			(Config.ALLOW_PLAYERS_REFUSAL ? PMonoffCmd.class : null),
			PremiumCmd.class,
			(Config.RANK_PVP_SYSTEM_ENABLED && Config.PVP_INFO_COMMAND_ENABLED && Config.RANK_PVP_SYSTEM_ENABLED && !Config.PVP_INFO_USER_COMMAND_ENABLED ? PvpInfoCmd.class : null),
			(Config.ALLOW_SERVER_RESTART_COMMAND ? ServerRestartVoteCmd.class : null),
			(Config.ALLOW_TRADEOFF_COMMAND ? TradeonoffCmd.class : null),
			(Config.ALLOW_VIPTELEPORT_COMMAND ? VipTeleportCmd.class : null),
			(Config.ALLOW_INFO_COMMAND ? VoiceInfoCmd.class : null),
			(Config.MOD_ALLOW_WEDDING ? WeddingCmd.class : null),
			(Config.ZODIAC_ENABLE ? ZodiacRegistrationCmd.class : null),
		},
	};
	
	private static class SingletonHolder
	{
		protected static final MasterHandler _instance = new MasterHandler();
	}
}