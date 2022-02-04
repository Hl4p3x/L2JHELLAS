package com.l2jhellas.gameserver.scrips.siegable;

import java.util.Calendar;
import java.util.List;

import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2SiegeClan;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public interface Siegable 
{	
	List<L2SiegeClan> getAttackerClans();	
	List<L2PcInstance> getAttackersInZone();
	List<L2SiegeClan> getDefenderClans();
	List<L2Npc> getFlag(L2Clan clan);
	
	L2SiegeClan getAttackerClan(int clanId);	
	L2SiegeClan getAttackerClan(L2Clan clan);
	L2SiegeClan getDefenderClan(int clanId);	
	L2SiegeClan getDefenderClan(L2Clan clan);
	
	Calendar getSiegeDate();

	boolean checkIsAttacker(L2Clan clan);
	boolean checkIsDefender(L2Clan clan);
	
	void startSiege();
	void updateSiege();
	void endSiege();
}
