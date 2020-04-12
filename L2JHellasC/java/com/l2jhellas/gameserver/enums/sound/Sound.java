package com.l2jhellas.gameserver.enums.sound;

import com.l2jhellas.gameserver.audio.IAudio;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.network.serverpackets.PlaySound;

public enum Sound implements IAudio
{
	ITEMSOUND_QUEST_ACCEPT("ItemSound.quest_accept"),
	ITEMSOUND_QUEST_MIDDLE("ItemSound.quest_middle"),
	ITEMSOUND_QUEST_FINISH("ItemSound.quest_finish"),
	ITEMSOUND_QUEST_ITEMGET("ItemSound.quest_itemget"),
	// Newbie Guide tutorial (incl. some quests), Mutated Kaneus quests, Quest 192
	ITEMSOUND_QUEST_TUTORIAL("ItemSound.quest_tutorial"),
	ITEMSOUND_QUEST_GIVEUP("ItemSound.quest_giveup"),
	ITEMSOUND_QUEST_BEFORE_BATTLE("ItemSound.quest_before_battle"),
	ITEMSOUND_QUEST_JACKPOT("ItemSound.quest_jackpot"),
	ITEMSOUND_QUEST_FANFARE_1("ItemSound.quest_fanfare_1"),
	// Played only after class transfer(ID 31756 and 31757)
	ITEMSOUND_QUEST_FANFARE_2("ItemSound.quest_fanfare_2"),
	ITEMSOUND_QUEST_FANFARE_MIDDLE("ItemSound.quest_fanfare_middle"),
	ITEMSOUND_ARMOR_WOOD("ItemSound.armor_wood_3"),
	ITEMSOUND_ARMOR_CLOTH("ItemSound.item_drop_equip_armor_cloth"),
	AMDSOUND_ED_CHIMES("AmdSound.ed_chimes_05"),
	HORROR_01("horror_01"), // played when spawned monster sees player
	AMBSOUND_HORROR_01("AmbSound.dd_horror_01"),
	AMBSOUND_HORROR_03("AmbSound.d_horror_03"),
	AMBSOUND_HORROR_15("AmbSound.d_horror_15"),
	ITEMSOUND_ARMOR_LEATHER("ItemSound.itemdrop_armor_leather"),
	ITEMSOUND_WEAPON_SPEAR("ItemSound.itemdrop_weapon_spear"),
	AMBSOUND_MT_CREAK("AmbSound.mt_creak01"),
	AMBSOUND_EG_DRON("AmbSound.eg_dron_02"),
	SKILLSOUND_HORROR_02("SkillSound5.horror_02"),
	CHRSOUND_MHFIGHTER_CRY("ChrSound.MHFighter_cry"),
	AMDSOUND_WIND_LOOT("AmdSound.d_wind_loot_02"),
	INTERFACESOUND_CHARSTAT_OPEN("InterfaceSound.charstat_open_01"),
	INTERFACESOUND_CHARSTAT_CLOSE("interfacesound.system_close_01"),
	AMDSOUND_HORROR_02("AmdSound.dd_horror_02"),
	CHRSOUND_FDELF_CRY("ChrSound.FDElf_Cry"),
	AMBSOUND_WINGFLAP("AmbSound.t_wingflap_04"),
	AMBSOUND_THUNDER("AmbSound.thunder_02"),
	AMBSOUND_DRONE("AmbSound.ed_drone_02"),
	AMBSOUND_CRYSTAL_LOOP("AmbSound.cd_crystal_loop"),
	AMBSOUND_PERCUSSION_01("AmbSound.dt_percussion_01"),
	AMBSOUND_PERCUSSION_02("AmbSound.ac_percussion_02"),
	//treasure chests
	ITEMSOUND_BROKEN_KEY("ItemSound2.broken_key"),
	ITEMSOUND_SIREN("ItemSound3.sys_siren"),
	ITEMSOUND_ENCHANT_SUCCESS("ItemSound3.sys_enchant_success"),
	ITEMSOUND_ENCHANT_FAILED("ItemSound3.sys_enchant_failed"),
	// Best farm mobs
	ITEMSOUND_SOW_SUCCESS("ItemSound3.sys_sow_success"),
	SKILLSOUND_HORROR_1("SkillSound5.horror_01"),
	SKILLSOUND_HORROR_2("SkillSound5.horror_02"),
	SKILLSOUND_ANTARAS_FEAR("SkillSound3.antaras_fear"),

	SKILLSOUND_JEWEL_CELEBRATE("SkillSound2.jewel.celebrate"),

	SKILLSOUND_LIQUID_MIX("SkillSound5.liquid_mix_01"),
	SKILLSOUND_LIQUID_SUCCESS("SkillSound5.liquid_success_01"),
	SKILLSOUND_LIQUID_FAIL("SkillSound5.liquid_fail_01"),
	
	SKILLSOUND_CRITICAL("SkillSound.critical_hit_02"),

	ETCSOUND_ELROKI_SONG_FULL("EtcSound.elcroki_song_full"),
	ETCSOUND_ELROKI_SONG_1ST("EtcSound.elcroki_song_1st"),
	ETCSOUND_ELROKI_SONG_2ND("EtcSound.elcroki_song_2nd"),
	ETCSOUND_ELROKI_SONG_3RD("EtcSound.elcroki_song_3rd"),
	
	ITEMSOUND2_RACE_START("ItemSound2.race_start"),
	
	// Ships
	ITEMSOUND_SHIP_ARRIVAL_DEPARTURE("itemsound.ship_arrival_departure"),
	ITEMSOUND_SHIP_5MIN("itemsound.ship_5min"),
	
	SIEGE_SOUND_START("systemmsg_e.17"),
	SIEGE_SOUND_END("systemmsg_e.18"),
	
	ITEMSOUND_SHIP_1MIN("itemsound.ship_1min");
	

	
	private final PlaySound _playSound;
	
	private Sound(String soundName)
	{
		_playSound = PlaySound.createSound(soundName);
	}
	
	public PlaySound withObject(L2Object obj)
	{
		return PlaySound.createSound(getSoundName(), obj);
	}
	
	@Override
	public String getSoundName()
	{
		return _playSound.getSoundName();
	}
	
	@Override
	public PlaySound getPacket()
	{
		return _playSound;
	}
}