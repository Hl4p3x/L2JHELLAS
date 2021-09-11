package Extensions.fake.roboto.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Extensions.fake.roboto.FakePlayer;
import Extensions.fake.roboto.FakePlayerNameManager;
import Extensions.fake.roboto.ai.AdventurerAI;
import Extensions.fake.roboto.ai.ArchmageAI;
import Extensions.fake.roboto.ai.CardinalAI;
import Extensions.fake.roboto.ai.DominatorAI;
import Extensions.fake.roboto.ai.DreadnoughtAI;
import Extensions.fake.roboto.ai.DuelistAI;
import Extensions.fake.roboto.ai.EnchanterAI;
import Extensions.fake.roboto.ai.FakePlayerAI;
import Extensions.fake.roboto.ai.FallbackAI;
import Extensions.fake.roboto.ai.GhostHunterAI;
import Extensions.fake.roboto.ai.GhostSentinelAI;
import Extensions.fake.roboto.ai.GrandKhavatariAI;
import Extensions.fake.roboto.ai.MoonlightSentinelAI;
import Extensions.fake.roboto.ai.MysticMuseAI;
import Extensions.fake.roboto.ai.SaggitariusAI;
import Extensions.fake.roboto.ai.SoultakerAI;
import Extensions.fake.roboto.ai.StormScreamerAI;
import Extensions.fake.roboto.ai.TitanAI;
import Extensions.fake.roboto.ai.WindRiderAI;
import Extensions.fake.roboto.ai.walker.GiranWalkerAI;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.PlayerDataTemplate;
import com.l2jhellas.gameserver.enums.Sex;
import com.l2jhellas.gameserver.enums.player.ClassId;
import com.l2jhellas.gameserver.enums.player.ClassRace;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.appearance.PcAppearance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.base.Experience;
import com.l2jhellas.gameserver.templates.L2PcTemplate;
import com.l2jhellas.util.Rnd;

public class FakeHelpers
{
	
	public static int[][] getFighterBuffs()
	{
		return new int[][]
		{
			{
				1204,
				2
			}, // wind walk
			{
				1040,
				3
			}, // shield
			{
				1035,
				4
			}, // Mental Shield
			{
				1045,
				6
			}, // Bless the Body
			{
				1068,
				3
			}, // might
			{
				1062,
				2
			}, // besekers
			{
				1086,
				2
			}, // haste
			{
				1077,
				3
			}, // focus
			{
				1388,
				3
			}, // Greater Might
			{
				1036,
				2
			}, // magic barrier
			{
				274,
				1
			}, // dance of fire
			{
				273,
				1
			}, // dance of fury
			{
				268,
				1
			}, // dance of wind
			{
				271,
				1
			}, // dance of warrior
			{
				267,
				1
			}, // Song of Warding
			{
				349,
				1
			}, // Song of Renewal
			{
				264,
				1
			}, // song of earth
			{
				269,
				1
			}, // song of hunter
			{
				364,
				1
			}, // song of champion
			{
				1363,
				1
			}, // chant of victory
			{
				4699,
				5
			}
		// Blessing of Queen
		};
	}
	
	public static int[][] getMageBuffs()
	{
		return new int[][]
		{
			{
				1204,
				2
			}, // wind walk
			{
				1040,
				3
			}, // shield
			{
				1035,
				4
			}, // Mental Shield
			{
				4351,
				6
			}, // Concentration
			{
				1036,
				2
			}, // Magic Barrier
			{
				1045,
				6
			}, // Bless the Body
			{
				1303,
				2
			}, // Wild Magic
			{
				1085,
				3
			}, // acumen
			{
				1062,
				2
			}, // besekers
			{
				1059,
				3
			}, // empower
			{
				1389,
				3
			}, // Greater Shield
			{
				273,
				1
			}, // dance of the mystic
			{
				276,
				1
			}, // dance of concentration
			{
				365,
				1
			}, // Dance of Siren
			{
				264,
				1
			}, // song of earth
			{
				268,
				1
			}, // song of wind
			{
				267,
				1
			}, // Song of Warding
			{
				349,
				1
			}, // Song of Renewal
			{
				1413,
				1
			}, // Magnus\' Chant
			{
				4703,
				4
			}
		// Gift of Seraphim
		};
	}
	
	public static Class<? extends L2Character> getTestTargetClass()
	{
		return FakePlayer.class;
	}
	
	public static int getTestTargetRange()
	{
		return 2000;
	}
	
	public static FakePlayer createRandomFakePlayer()
	{
		int objectId = IdFactory.getInstance().getNextId();
		String accountName = "AutoPilot";
		String playerName = FakePlayerNameManager.INSTANCE.getRandomAvailableName();
		
		ClassId classId = getThirdClasses().get(Rnd.get(0, getThirdClasses().size() - 1));
		
		final L2PcTemplate template = PlayerDataTemplate.getInstance().getTemplate(classId);
		PcAppearance app = getRandomAppearance(template.getRace());
		FakePlayer player = new FakePlayer(objectId, template, accountName, app);
		
		player.setName(playerName);
		player.setAccessLevel(0);
		player.setBaseClass(player.getClassId());
		setLevel(player, 81);
		player.rewardSkills();
		
		giveArmorsByClass(player);
		giveWeaponsByClass(player, true);
		player.heal();
		
		return player;
	}
	
	public static void giveArmorsByClass(FakePlayer player)
	{
		List<Integer> itemIds = new ArrayList<>();
		switch (player.getClassId())
		{
			case ARCHMAGE:
			case SOULTAKER:
			case HIEROPHANT:
			case ARCANA_LORD:
			case CARDINAL:
			case MYSTIC_MUSE:
			case ELEMENTAL_MASTER:
			case EVAS_SAINT:
			case STORM_SCREAMER:
			case SPECTRAL_MASTER:
			case SHILLIEN_SAINT:
			case DOMINATOR:
			case DOOMCRYER:
				itemIds = Arrays.asList(2407, 512, 5767, 5779, 858, 858, 889, 889, 920);
				break;
			case DUELIST:
			case DREADNOUGHT:
			case PHOENIX_KNIGHT:
			case SWORD_MUSE:
			case HELL_KNIGHT:
			case SPECTRAL_DANCER:
			case EVAS_TEMPLAR:
			case SHILLIEN_TEMPLAR:
			case TITAN:
			case MAESTRO:
				itemIds = Arrays.asList(6373, 6374, 6375, 6376, 6378, 858, 858, 889, 889, 920);
				break;
			case SAGGITARIUS:
			case ADVENTURER:
			case WIND_RIDER:
			case MOONLIGHT_SENTINEL:
			case GHOST_HUNTER:
			case GHOST_SENTINEL:
			case FORTUNE_SEEKER:
			case GRAND_KHAVATARI:
				itemIds = Arrays.asList(6379, 6380, 6381, 6382, 858, 858, 889, 889, 920);
				break;
			default:
				break;
		}
		for (int id : itemIds)
		{
			player.getInventory().addItem("Armors", id, 1, player, null);
			L2ItemInstance item = player.getInventory().getItemByItemId(id);
			player.getInventory().equipItemAndRecord(item);
		}
		player.getInventory().reloadEquippedItems();
		player.broadcastUserInfo();

	}
	
	public static void giveWeaponsByClass(FakePlayer player, boolean randomlyEnchant)
	{
		List<Integer> itemIds = new ArrayList<>();
		switch (player.getClassId())
		{
			case FORTUNE_SEEKER:
			case GHOST_HUNTER:
			case WIND_RIDER:
			case ADVENTURER:
				itemIds = Arrays.asList(6590);
				break;
			case SAGGITARIUS:
			case MOONLIGHT_SENTINEL:
			case GHOST_SENTINEL:
				itemIds = Arrays.asList(7577);
				break;
			case PHOENIX_KNIGHT:
			case SWORD_MUSE:
			case HELL_KNIGHT:
			case EVAS_TEMPLAR:
			case SHILLIEN_TEMPLAR:
				itemIds = Arrays.asList(6583, 6377);
				break;
			case MAESTRO:
				itemIds = Arrays.asList(6585, 6377);
				break;
			case TITAN:
				itemIds = Arrays.asList(6607);
				break;
			case DUELIST:
			case SPECTRAL_DANCER:
				itemIds = Arrays.asList(6580);
				break;
			case DREADNOUGHT:
				itemIds = Arrays.asList(6599);
				break;
			case ARCHMAGE:
			case SOULTAKER:
			case HIEROPHANT:
			case ARCANA_LORD:
			case CARDINAL:
			case MYSTIC_MUSE:
			case ELEMENTAL_MASTER:
			case EVAS_SAINT:
			case STORM_SCREAMER:
			case SPECTRAL_MASTER:
			case SHILLIEN_SAINT:
			case DOMINATOR:
			case DOOMCRYER:
				itemIds = Arrays.asList(6608);
				break;
			case GRAND_KHAVATARI:
				itemIds = Arrays.asList(6602);
				break;
			default:
				break;
		}
		for (int id : itemIds)
		{
			player.getInventory().addItem("Weapon", id, 1, player, null);
			L2ItemInstance item = player.getInventory().getItemByItemId(id);
			if (randomlyEnchant)
				item.setEnchantLevel(Rnd.get(0,Config.ENCHANT_MAX_WEAPON));
			player.getInventory().equipItemAndRecord(item);
			player.getInventory().reloadEquippedItems();
		}
	}
	
	public static List<ClassId> getThirdClasses()
	{
		List<ClassId> classes = new ArrayList<>();
		classes.add(ClassId.SAGGITARIUS);
		classes.add(ClassId.ARCHMAGE);
		classes.add(ClassId.SOULTAKER);
		classes.add(ClassId.MYSTIC_MUSE);
		classes.add(ClassId.STORM_SCREAMER);
		classes.add(ClassId.MOONLIGHT_SENTINEL);
		classes.add(ClassId.GHOST_SENTINEL);
		classes.add(ClassId.ADVENTURER);
		classes.add(ClassId.WIND_RIDER);
		classes.add(ClassId.DOMINATOR);
		classes.add(ClassId.TITAN);
		classes.add(ClassId.CARDINAL);
		classes.add(ClassId.DUELIST);
		classes.add(ClassId.GRAND_KHAVATARI);
		classes.add(ClassId.DREADNOUGHT);
		
		return classes;
	}
	
	public static Map<ClassId, Class<? extends FakePlayerAI>> getAllAIs()
	{
		Map<ClassId, Class<? extends FakePlayerAI>> ais = new HashMap<>();
		ais.put(ClassId.STORM_SCREAMER, StormScreamerAI.class);
		ais.put(ClassId.MYSTIC_MUSE, MysticMuseAI.class);
		ais.put(ClassId.ARCHMAGE, ArchmageAI.class);
		ais.put(ClassId.SOULTAKER, SoultakerAI.class);
		ais.put(ClassId.SAGGITARIUS, SaggitariusAI.class);
		ais.put(ClassId.MOONLIGHT_SENTINEL, MoonlightSentinelAI.class);
		ais.put(ClassId.GHOST_SENTINEL, GhostSentinelAI.class);
		ais.put(ClassId.ADVENTURER, AdventurerAI.class);
		ais.put(ClassId.WIND_RIDER, WindRiderAI.class);
		ais.put(ClassId.GHOST_HUNTER, GhostHunterAI.class);
		ais.put(ClassId.DOMINATOR, DominatorAI.class);
		ais.put(ClassId.TITAN, TitanAI.class);
		ais.put(ClassId.CARDINAL, CardinalAI.class);
		ais.put(ClassId.DUELIST, DuelistAI.class);
		ais.put(ClassId.GRAND_KHAVATARI, GrandKhavatariAI.class);
		ais.put(ClassId.DREADNOUGHT, DreadnoughtAI.class);
		return ais;
	}
	
	public static PcAppearance getRandomAppearance(ClassRace race)
	{
		
		Sex randomSex = Rnd.get(1, 2) == 1 ? Sex.MALE : Sex.FEMALE;
		int hairStyle = Rnd.get(0, randomSex == Sex.MALE ? 4 : 6);
		int hairColor = Rnd.get(0, 3);
		int faceId = Rnd.get(0, 2);
		
		return new PcAppearance((byte) faceId, (byte) hairColor, (byte) hairStyle, randomSex);
	}
	
	public static void setLevel(FakePlayer player, int level)
	{
		if (level >= 1 && level <= Experience.MAX_LEVEL)
		{
			long pXp = player.getExp();
			long tXp = Experience.LEVEL[81];
			
			if (pXp > tXp)
				player.removeExpAndSp(pXp - tXp, 0);
			else if (pXp < tXp)
				player.addExpAndSp(tXp - pXp, 0);
		}
	}
	
	public static Class<? extends FakePlayerAI> getAIbyClassId(ClassId classId)
	{
		Class<? extends FakePlayerAI> ai = getAllAIs().get(classId);
		if (ai == null)
			return FallbackAI.class;
		
		return ai;
	}
	
	public static Class<? extends FakePlayerAI> getAIbyCustom(int type)
	{
		return type == 1 ? GiranWalkerAI.class : type == 2 ? EnchanterAI.class :  FallbackAI.class ;
	}
}
