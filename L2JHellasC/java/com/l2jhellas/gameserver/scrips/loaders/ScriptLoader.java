package com.l2jhellas.gameserver.scrips.loaders;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.scrips.quests.*;
import com.l2jhellas.gameserver.scrips.quests.ai.custom.EchoCrystals;
import com.l2jhellas.gameserver.scrips.quests.ai.custom.HeroCirclet;
import com.l2jhellas.gameserver.scrips.quests.ai.custom.HeroWeapon;
import com.l2jhellas.gameserver.scrips.quests.ai.custom.KetraOrcSupport;
import com.l2jhellas.gameserver.scrips.quests.ai.custom.MissQueen;
import com.l2jhellas.gameserver.scrips.quests.ai.custom.NpcLocationInfo;
import com.l2jhellas.gameserver.scrips.quests.ai.custom.Q998_FallenAngelSelect;
import com.l2jhellas.gameserver.scrips.quests.ai.custom.RaidbossInfo;
import com.l2jhellas.gameserver.scrips.quests.ai.custom.ShadowWeapon;
import com.l2jhellas.gameserver.scrips.quests.ai.custom.VarkaSilenosSupport;
import com.l2jhellas.gameserver.scrips.quests.ai.group.AncientEGG;
import com.l2jhellas.gameserver.scrips.quests.ai.group.CatsEyeBandit;
import com.l2jhellas.gameserver.scrips.quests.ai.group.Chests;
import com.l2jhellas.gameserver.scrips.quests.ai.group.DeluLizardmanSpecialAgent;
import com.l2jhellas.gameserver.scrips.quests.ai.group.DeluLizardmanSpecialCommander;
import com.l2jhellas.gameserver.scrips.quests.ai.group.FairyTrees;
import com.l2jhellas.gameserver.scrips.quests.ai.group.FeedableBeasts;
import com.l2jhellas.gameserver.scrips.quests.ai.group.FleeingNPCs;
import com.l2jhellas.gameserver.scrips.quests.ai.group.FrenzyOnAttack;
import com.l2jhellas.gameserver.scrips.quests.ai.group.FrozenLabyrinth;
import com.l2jhellas.gameserver.scrips.quests.ai.group.GatekeeperZombies;
import com.l2jhellas.gameserver.scrips.quests.ai.group.HotSpringDisease;
import com.l2jhellas.gameserver.scrips.quests.ai.group.KarulBugbear;
import com.l2jhellas.gameserver.scrips.quests.ai.group.L2AttackableAIScript;
import com.l2jhellas.gameserver.scrips.quests.ai.group.Monastery;
import com.l2jhellas.gameserver.scrips.quests.ai.group.OlMahumGeneral;
import com.l2jhellas.gameserver.scrips.quests.ai.group.PlainsOfDion;
import com.l2jhellas.gameserver.scrips.quests.ai.group.PolymorphingAngel;
import com.l2jhellas.gameserver.scrips.quests.ai.group.PolymorphingOnAttack;
import com.l2jhellas.gameserver.scrips.quests.ai.group.PrimevalIsle;
import com.l2jhellas.gameserver.scrips.quests.ai.group.RetreatOnAttack;
import com.l2jhellas.gameserver.scrips.quests.ai.group.ScarletStokateNoble;
import com.l2jhellas.gameserver.scrips.quests.ai.group.SearchingMaster;
import com.l2jhellas.gameserver.scrips.quests.ai.group.SeeThroughSilentMove;
import com.l2jhellas.gameserver.scrips.quests.ai.group.SpeakingNPCs;
import com.l2jhellas.gameserver.scrips.quests.ai.group.Splendor;
import com.l2jhellas.gameserver.scrips.quests.ai.group.StakatoNest;
import com.l2jhellas.gameserver.scrips.quests.ai.group.SummonMinions;
import com.l2jhellas.gameserver.scrips.quests.ai.group.TimakOrcSupplier;
import com.l2jhellas.gameserver.scrips.quests.ai.group.TimakOrcTroopLeader;
import com.l2jhellas.gameserver.scrips.quests.ai.group.TurekOrcFootman;
import com.l2jhellas.gameserver.scrips.quests.ai.group.TurekOrcOverlord;
import com.l2jhellas.gameserver.scrips.quests.ai.group.TurekOrcWarlord;
import com.l2jhellas.gameserver.scrips.quests.ai.group.VarkaKetraAlly;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.Anays;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.Antharas;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.Baium;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.Core;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.DrChaos;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.FleeNpc;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.Frintezza;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.Gordon;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.IceFairySirra;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.Orfen;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.QueenAnt;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.Sailren;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.Valakas;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.VanHalter;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.Zaken;
import com.l2jhellas.gameserver.scrips.quests.ai.teleports.ElrokiTeleporters;
import com.l2jhellas.gameserver.scrips.quests.ai.teleports.GatekeeperSpirit;
import com.l2jhellas.gameserver.scrips.quests.ai.teleports.GrandBossTeleporters;
import com.l2jhellas.gameserver.scrips.quests.ai.teleports.HuntingGroundsTeleport;
import com.l2jhellas.gameserver.scrips.quests.ai.teleports.NewbieTravelToken;
import com.l2jhellas.gameserver.scrips.quests.ai.teleports.NoblesseTeleport;
import com.l2jhellas.gameserver.scrips.quests.ai.teleports.OracleTeleport;
import com.l2jhellas.gameserver.scrips.quests.ai.teleports.PaganTeleporters;
import com.l2jhellas.gameserver.scrips.quests.ai.teleports.RaceTrack;
import com.l2jhellas.gameserver.scrips.quests.ai.teleports.TeleportWithCharm;
import com.l2jhellas.gameserver.scrips.quests.ai.teleports.ToIVortex;
import com.l2jhellas.gameserver.scrips.quests.ai.vilagemaster.Alliance;
import com.l2jhellas.gameserver.scrips.quests.ai.vilagemaster.Clan;
import com.l2jhellas.gameserver.scrips.quests.ai.vilagemaster.FirstClassChange;
import com.l2jhellas.gameserver.scrips.quests.ai.vilagemaster.SecondClassChange;

public class ScriptLoader
{
	private static final Logger _log = Logger.getLogger(ScriptLoader.class.getName());
	
	public static ScriptLoader getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public ScriptLoader()
	{
		teleiwne();
	}
	
	private static final Class<?>[] Quests =
	{
		// quests
		Q001_LettersOfLove.class,
		Q002_WhatWomenWant.class,
		Q003_WillTheSealBeBroken.class,
		Q004_LongliveThePaagrioLord.class,
		Q005_MinersFavor.class,
		Q006_StepIntoTheFuture.class,
		Q007_ATripBegins.class,
		Q008_AnAdventureBegins.class,
		Q009_IntoTheCityOfHumans.class,
		Q010_IntoTheWorld.class,
		Q011_SecretMeetingWithKetraOrcs.class,
		Q012_SecretMeetingWithVarkaSilenos.class,
		Q013_ParcelDelivery.class,
		Q014_WhereaboutsOfTheArchaeologist.class,
		Q015_SweetWhispers.class,
		Q016_TheComingDarkness.class,
		Q017_LightAndDarkness.class,
		Q018_MeetingWithTheGoldenRam.class,
		Q019_GoToThePastureland.class,
		Q020_BringUpWithLove.class,
		Q021_HiddenTruth.class,
		Q022_TragedyInVonHellmannForest.class,
		Q023_LidiasHeart.class,
		Q024_InhabitantsOfTheForestOfTheDead.class,
		Q025_HidingBehindTheTruth.class,
		Q027_ChestCaughtWithABaitOfWind.class,
		Q028_ChestCaughtWithABaitOfIcyAir.class,
		Q029_ChestCaughtWithABaitOfEarth.class,
		Q030_ChestCaughtWithABaitOfFire.class,
		Q031_SecretBuriedInTheSwamp.class,
		Q032_AnObviousLie.class,
		Q033_MakeAPairOfDressShoes.class,
		Q034_InSearchOfCloth.class,
		Q035_FindGlitteringJewelry.class,
		Q036_MakeASewingKit.class,
		Q037_MakeFormalWear.class,
		Q038_DragonFangs.class,
		Q039_RedEyedInvaders.class,
		Q042_HelpTheUncle.class,
		Q043_HelpTheSister.class,
		Q044_HelpTheSon.class,
		Q045_ToTalkingIsland.class,
		Q046_OnceMoreInTheArmsOfTheMotherTree.class,
		Q047_IntoTheDarkForest.class,
		Q048_ToTheImmortalPlateau.class,
		Q049_TheRoadHome.class,
		Q050_LanoscosSpecialBait.class,
		Q051_OFullesSpecialBait.class,
		Q052_WilliesSpecialBait.class,
		Q053_LinnaeusSpecialBait.class,
		Q101_SwordOfSolidarity.class,
		Q102_SeaOfSporesFever.class,
		Q103_SpiritOfCraftsman.class,
		Q104_SpiritOfMirrors.class,
		Q105_SkirmishWithTheOrcs.class,
		Q106_ForgottenTruth.class,
		Q107_MercilessPunishment.class,
		Q108_JumbleTumbleDiamondFuss.class,
		Q109_InSearchOfTheNest.class,
		Q110_ToThePrimevalIsle.class,
		Q111_ElrokianHuntersProof.class,
		Q112_WalkOfFate.class,
		Q113_StatusOfTheBeaconTower.class,
		Q114_ResurrectionOfAnOldManager.class,
		Q115_TheOtherSideOfTruth.class,
		Q116_BeyondTheHillsOfWinter.class,
		Q117_TheOceanOfDistantStars.class,
		Q118_ToLeadAndBeLed.class,
		Q119_LastImperialPrince.class,
		Q120_PavelsLastResearch.class,
		Q121_PavelTheGiant.class,
		Q122_OminousNews.class,
		Q123_TheLeaderAndTheFollower.class,
		Q124_MeetingTheElroki.class,
		Q125_TheNameOfEvil_1.class,
		Q126_TheNameOfEvil_2.class,
		Q127_KamaelAWindowToTheFuture.class,
		Q136_MoreThanMeetsTheEye.class,
		Q137_TempleChampionPart1.class,
		Q138_TempleChampionPart2.class,
		Q139_ShadowFoxPart1.class,
		Q140_ShadowFoxPart2.class,
		Q141_ShadowFoxPart3.class,
		Q142_FallenAngelRequestOfDawn.class,
		Q143_FallenAngelRequestOfDusk.class,
		Q151_CureForFeverDisease.class,
		Q152_ShardsOfGolem.class,
		Q153_DeliverGoods.class,
		Q154_SacrificeToTheSea.class,
		Q155_FindSirWindawood.class,
		Q156_MillenniumLove.class,
		Q157_RecoverSmuggledGoods.class,
		Q158_SeedOfEvil.class,
		Q159_ProtectTheWaterSource.class,
		Q160_NerupasRequest.class,
		Q161_FruitOfTheMotherTree.class,
		Q162_CurseOfTheUndergroundFortress.class,
		Q163_LegacyOfThePoet.class,
		Q164_BloodFiend.class,
		Q165_ShilensHunt.class,
		Q166_MassOfDarkness.class,
		Q167_DwarvenKinship.class,
		Q168_DeliverSupplies.class,
		Q169_OffspringOfNightmares.class,
		Q170_DangerousSeduction.class,
		Q171_ActsOfEvil.class,
		Q211_TrialOfTheChallenger.class,
		Q212_TrialOfDuty.class,
		Q213_TrialOfTheSeeker.class,
		Q214_TrialOfTheScholar.class,
		Q215_TrialOfThePilgrim.class,
		Q216_TrialOfTheGuildsman.class,
		Q217_TestimonyOfTrust.class,
		Q218_TestimonyOfLife.class,
		Q219_TestimonyOfFate.class,
		Q220_TestimonyOfGlory.class,
		Q222_TestOfTheDuelist.class,
		Q223_TestOfTheChampion.class,
		Q224_TestOfSagittarius.class,
		Q225_TestOfTheSearcher.class,
		Q226_TestOfTheHealer.class,
		Q228_TestOfMagus.class,
		Q230_TestOfTheSummoner.class,
		Q231_TestOfTheMaestro.class,
		Q232_TestOfTheLord.class,
		Q233_TestOfTheWarSpirit.class,
		Q234_FatesWhisper.class,
		Q235_MimirsElixir.class,
		Q241_PossessorOfAPreciousSoul.class,
		Q242_PossessorOfAPreciousSoul.class,
		Q246_PossessorOfAPreciousSoul.class,
		Q247_PossessorOfAPreciousSoul.class,
		(Config.ALLOW_TUTORIAL ? Q255_Tutorial.class : null),
		Q257_TheGuardIsBusy.class,
		Q258_BringWolfPelts.class,
		Q259_RanchersPlea.class,
		Q260_HuntTheOrcs.class,
		Q261_CollectorsDream.class,
		Q262_TradeWithTheIvoryTower.class,
		Q263_OrcSubjugation.class,
		Q264_KeenClaws.class,
		Q265_ChainsOfSlavery.class,
		Q266_PleasOfPixies.class,
		Q267_WrathOfVerdure.class,
		Q271_ProofOfValor.class,
		Q272_WrathOfAncestors.class,
		Q273_InvadersOfTheHolyLand.class,
		Q274_SkirmishWithTheWerewolves.class,
		Q275_DarkWingedSpies.class,
		Q276_TotemOfTheHestui.class,
		Q277_GatekeepersOffering.class,
		Q291_RevengeOfTheRedbonnet.class,
		Q292_BrigandsSweep.class,
		Q293_TheHiddenVeins.class,
		Q294_CovertBusiness.class,
		Q295_DreamingOfTheSkies.class,
		Q296_TarantulasSpiderSilk.class,
		Q297_GatekeepersFavor.class,
		Q298_LizardmensConspiracy.class,
		Q299_GatherIngredientsForPie.class,
		Q300_HuntingLetoLizardman.class,
		Q303_CollectArrowheads.class,
		Q306_CrystalsOfFireAndIce.class,
		Q313_CollectSpores.class,
		Q316_DestroyPlagueCarriers.class,
		Q317_CatchTheWind.class,
		Q319_ScentOfDeath.class,
		Q320_BonesTellTheFuture.class,
		Q324_SweetestVenom.class,
		Q325_GrimCollector.class,
		Q326_VanquishRemnants.class,
		Q327_RecoverTheFarmland.class,
		Q328_SenseForBusiness.class,
		Q329_CuriosityOfADwarf.class,
		Q330_AdeptOfTaste.class,
		Q331_ArrowOfVengeance.class,
		Q333_HuntOfTheBlackLion.class,
		Q334_TheWishingPotion.class,
		Q335_TheSongOfTheHunter.class,
		Q336_CoinsOfMagic.class,
		Q337_AudienceWithTheLandDragon.class,
		Q338_AlligatorHunter.class,
		Q340_SubjugationOfLizardmen.class,
		Q341_HuntingForWildBeasts.class,
		Q343_UnderTheShadowOfTheIvoryTower.class,
		Q344_1000YearsTheEndOfLamentation.class,
		Q345_MethodToRaiseTheDead.class,
		Q347_GoGetTheCalculator.class,
		Q348_AnArrogantSearch.class,
		Q350_EnhanceYourWeapon.class,
		Q351_BlackSwan.class,
		Q352_HelpRoodRaiseANewPet.class,
		Q353_PowerOfDarkness.class,
		Q354_ConquestOfAlligatorIsland.class,
		Q355_FamilyHonor.class,
		Q356_DigUpTheSeaOfSpores.class,
		Q357_WarehouseKeepersAmbition.class,
		Q358_IllegitimateChildOfAGoddess.class,
		Q359_ForSleeplessDeadmen.class,
		Q360_PlunderTheirSupplies.class,
		Q362_BardsMandolin.class,
		Q363_SorrowfulSoundOfFlute.class,
		Q364_JovialAccordion.class,
		Q365_DevilsLegacy.class,
		Q366_SilverHairedShaman.class,
		Q367_ElectrifyingRecharge.class,
		Q368_TrespassingIntoTheSacredArea.class,
		Q369_CollectorOfJewels.class,
		Q370_AnElderSowsSeeds.class,
		Q371_ShriekOfGhosts.class,
		Q372_LegacyOfInsolence.class,
		Q373_SupplierOfReagents.class,
		Q374_WhisperOfDreams_Part1.class,
		Q375_WhisperOfDreams_Part2.class,
		Q376_ExplorationOfTheGiantsCave_Part1.class,
		Q377_ExplorationOfTheGiantsCave_Part2.class,
		Q378_MagnificentFeast.class,
		Q379_FantasyWine.class,
		Q380_BringOutTheFlavorOfIngredients.class,
		Q381_LetsBecomeARoyalMember.class,
		Q382_KailsMagicCoin.class,
		Q383_SearchingForTreasure.class,
		Q384_WarehouseKeepersPastime.class,
		Q385_YokeOfThePast.class,
		Q386_StolenDignity.class,
		Q401_PathToAWarrior.class,
		Q402_PathToAHumanKnight.class,
		Q403_PathToARogue.class,
		Q404_PathToAHumanWizard.class,
		Q405_PathToACleric.class,
		Q406_PathToAnElvenKnight.class,
		Q407_PathToAnElvenScout.class,
		Q408_PathToAnElvenWizard.class,
		Q409_PathToAnElvenOracle.class,
		Q410_PathToAPalusKnight.class,
		Q411_PathToAnAssassin.class,
		Q412_PathToADarkWizard.class,
		Q413_PathToAShillienOracle.class,
		Q414_PathToAnOrcRaider.class,
		Q415_PathToAMonk.class,
		Q416_PathToAnOrcShaman.class,
		Q417_PathToBecomeAScavenger.class,
		Q418_PathToAnArtisan.class,
		Q419_GetAPet.class,
		Q420_LittleWing.class,
		Q421_LittleWingsBigAdventure.class,
		Q422_RepentYourSins.class,
		Q426_QuestForFishingShot.class,
		Q431_WeddingMarch.class,
		Q432_BirthdayPartySong.class,
		Q501_ProofOfClanAlliance.class,
		Q503_PursuitOfClanAmbition.class,
		Q505_BloodOffering.class,
		Q508_AClansReputation.class,
		Q509_TheClansPrestige.class,
		Q510_AClansReputation.class,
		Q601_WatchingEyes.class,
		Q602_ShadowOfLight.class,
		Q603_DaimonTheWhiteEyed_Part1.class,
		Q604_DaimonTheWhiteEyed_Part2.class,
		Q605_AllianceWithKetraOrcs.class,
		Q606_WarWithVarkaSilenos.class,
		Q607_ProveYourCourage.class,
		Q608_SlayTheEnemyCommander.class,
		Q609_MagicalPowerOfWater_Part1.class,
		Q610_MagicalPowerOfWater_Part2.class,
		Q611_AllianceWithVarkaSilenos.class,
		Q612_WarWithKetraOrcs.class,
		Q613_ProveYourCourage.class,
		Q614_SlayTheEnemyCommander.class,
		Q615_MagicalPowerOfFire_Part1.class,
		Q616_MagicalPowerOfFire_Part2.class,
		Q617_GatherTheFlames.class,
		Q618_IntoTheFlame.class,
		Q619_RelicsOfTheOldEmpire.class,
		Q620_FourGoblets.class,
		Q621_EggDelivery.class,
		Q622_SpecialtyLiquorDelivery.class,
		Q623_TheFinestFood.class,
		Q624_TheFinestIngredients_Part1.class,
		Q625_TheFinestIngredients_Part2.class,
		Q626_ADarkTwilight.class,
		Q627_HeartInSearchOfPower.class,
		Q628_HuntOfTheGoldenRamMercenaryForce.class,
		Q629_CleanUpTheSwampOfScreams.class,
		Q631_DeliciousTopChoiceMeat.class,
		Q632_NecromancersRequest.class,
		Q633_InTheForgottenVillage.class,
		Q634_InSearchOfFragmentsOfDimension.class,
		Q635_InTheDimensionalRift.class,
		Q636_TruthBeyondTheGate.class,
		Q637_ThroughTheGateOnceMore.class,
		Q638_SeekersOfTheHolyGrail.class,
		Q639_GuardiansOfTheHolyGrail.class,
		Q640_TheZeroHour.class,
		Q641_AttackSailren.class,
		Q642_APowerfulPrimevalCreature.class,
		Q643_RiseAndFallOfTheElrokiTribe.class,
		Q644_GraveRobberAnnihilation.class,
		Q645_GhostsOfBatur.class,
		Q646_SignsOfRevolt.class,
		Q647_InfluxOfMachines.class,
		Q648_AnIceMerchantsDream.class,
		Q649_ALooterAndARailroadMan.class,
		Q650_ABrokenDream.class,
		Q651_RunawayYouth.class,
		Q652_AnAgedExAdventurer.class,
		Q653_WildMaiden.class,
		Q654_JourneyToASettlement.class,
		Q659_IdRatherBeCollectingFairyBreath.class,
		Q660_AidingTheFloranVillage.class,
		Q661_MakingTheHarvestGroundsSafe.class,
		Q662_AGameOfCards.class,
		Q663_SeductiveWhispers.class,
		Q688_DefeatTheElrokianRaiders.class,
		Q998_FallenAngelSelect.class,
		(Config.ALLOW_TUTORIAL ? Q999_T1Tutorial.class : null),
		
		AncientEGG.class,
		CatsEyeBandit.class,
		Chests.class,
		DeluLizardmanSpecialAgent.class,
		DeluLizardmanSpecialCommander.class,
		FairyTrees.class,
		FeedableBeasts.class,
		FleeingNPCs.class,
		FrenzyOnAttack.class,
		FrozenLabyrinth.class,
		GatekeeperZombies.class,
		HotSpringDisease.class,
		KarulBugbear.class,
		L2AttackableAIScript.class,
		Monastery.class,
		OlMahumGeneral.class,
		PlainsOfDion.class,
		PolymorphingAngel.class,
		PolymorphingOnAttack.class,
		PrimevalIsle.class,
		RetreatOnAttack.class,
		ScarletStokateNoble.class,
		SearchingMaster.class,
		SeeThroughSilentMove.class,
		SpeakingNPCs.class,
		Splendor.class,
		StakatoNest.class,
		SummonMinions.class,
		TimakOrcSupplier.class,
		TimakOrcTroopLeader.class,
		TurekOrcFootman.class,
		TurekOrcOverlord.class,
		TurekOrcWarlord.class,
		VarkaKetraAlly.class,
		
		// customs
		EchoCrystals.class,
		HeroCirclet.class,
		HeroWeapon.class,
		KetraOrcSupport.class,
		MissQueen.class,
		NpcLocationInfo.class,
		RaidbossInfo.class,
		ShadowWeapon.class,
		VarkaSilenosSupport.class,
		
		// teleports
		ElrokiTeleporters.class,
		GatekeeperSpirit.class,
		GrandBossTeleporters.class,
		HuntingGroundsTeleport.class,
		NewbieTravelToken.class,
		NoblesseTeleport.class,
		OracleTeleport.class,
		PaganTeleporters.class,
		RaceTrack.class,
		TeleportWithCharm.class,
		ToIVortex.class,
		
		// VilageMaster
		Alliance.class,
		Clan.class,
		FirstClassChange.class,
		SecondClassChange.class,
		
		// bosses
		Anays.class,
		Antharas.class,
		Baium.class,
		Core.class,
		DrChaos.class,
		FleeNpc.class,
		Frintezza.class,
		Gordon.class,
		IceFairySirra.class,
		Orfen.class,
		QueenAnt.class,
		Sailren.class,
		Valakas.class,
		VanHalter.class,
		Zaken.class
	};
	
	private static void teleiwne()
	{
		for (Class<?> _qs : Quests)
		{
			if (_qs == null)
				continue;
			try
			{
				_qs.newInstance();
			}
			catch (Exception e)
			{
				_log.severe(ScriptLoader.class.getSimpleName() + ": Failed loading " + _qs.getSimpleName() + ":");
				e.printStackTrace();
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final ScriptLoader _instance = new ScriptLoader();
	}
}