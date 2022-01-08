package com.l2jhellas.gameserver.model.actor.instance;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.l2jhellas.gameserver.instancemanager.GrandBossManager;
import com.l2jhellas.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.gameserver.templates.StatsSet;

public class L2BossSpawnInstance extends L2Npc
{
	private static final SimpleDateFormat Time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	String aliveColor = "9CC300";
	String deadColor = "FF0000";
	
	String aliveStatus = "Alive";
	String deadStatus = "Deal";
	
	public L2BossSpawnInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		showHtml(player);
	}
				
	public void showHtml(L2PcInstance activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/" + "70009.htm"); 
			
		long isOrfenAlive = isGrandBossAlive(29014);
		long isCoreAlive = isGrandBossAlive(29006);
		long isZakenAlive = isGrandBossAlive(29022);
		long isQueenAlive = isGrandBossAlive(29001);
		long isBaiumAlive = isGrandBossAlive(29020);
		long isAntharasAlive = isGrandBossAlive(29019);
		long isValakasAlive = isGrandBossAlive(29028);
		long isTezzaAlive = isGrandBossAlive(29045);
		long isSailrenAlive = isGrandBossAlive(29065);

		long isShadithAlive = isRaidBossAlive(25309);
		long isHekatonAlive = isRaidBossAlive(25299);
		long isBarakielAlive = isRaidBossAlive(25325);
		long isEmberAlive = isRaidBossAlive(25319);
		long isShyeedAlive = isRaidBossAlive(25514);
		long isKorimAlive = isRaidBossAlive(25092);
		long isGalaxiaAlive = isRaidBossAlive(25450);
		
		//color twn grandboss
		html.replace("%Orfencolor%", isOrfenAlive == 1 ? aliveColor : deadColor);
		html.replace("%Corecolor%",  isCoreAlive == 1 ? aliveColor : deadColor);
		html.replace("%Zakencolor%", isZakenAlive == 1 ? aliveColor : deadColor); 
		html.replace("%Queencolor%", isQueenAlive == 1 ? aliveColor : deadColor); 
		html.replace("%Baiumcolor%", isBaiumAlive == 1 ? aliveColor : deadColor);    
		html.replace("%Antharascolor%", isAntharasAlive == 1 ? aliveColor : deadColor);		
		html.replace("%Valakascolor%", isValakasAlive == 1 ? aliveColor : deadColor);
		html.replace("%Tezzacolor%", isTezzaAlive == 1 ? aliveColor : deadColor);
		html.replace("%Sailrencolor%", isSailrenAlive == 1 ? aliveColor : deadColor);

		//color twn raid boss
		html.replace("%Shadithcolor%", isShadithAlive == 1 ? aliveColor : deadColor);
		html.replace("%Hekatoncolor%", isHekatonAlive == 1 ? aliveColor : deadColor);
		html.replace("%Barakielcolor%",isBarakielAlive == 1 ? aliveColor : deadColor);		
		html.replace("%Embercolor%",  isEmberAlive == 1 ? aliveColor : deadColor);
		html.replace("%Shyeedcolor%", isShyeedAlive == 1 ? aliveColor : deadColor);
		html.replace("%Korimcolor%", isKorimAlive == 1 ? aliveColor : deadColor);
		html.replace("%Galaxiacolor%",isGalaxiaAlive == 1 ? aliveColor : deadColor);
			
		//status twn grandboss
		html.replace("%Orfenstatus%", isOrfenAlive == 1 ? aliveStatus : Time.format(new Date(isOrfenAlive)));
		html.replace("%Corestatus%",  isCoreAlive == 1 ? aliveStatus : Time.format(new Date(isCoreAlive)));
		html.replace("%Zakenstatus%", isZakenAlive == 1 ? aliveStatus : Time.format(new Date(isZakenAlive))); 
		html.replace("%Queenstatus%", isQueenAlive == 1 ? aliveStatus : Time.format(new Date(isQueenAlive))); 
		html.replace("%Baiumstatus%", isBaiumAlive == 1 ? aliveStatus : Time.format(new Date(isBaiumAlive)));    
		html.replace("%Antharasstatus%", isAntharasAlive == 1 ? aliveStatus : Time.format(new Date(isAntharasAlive)));		
		html.replace("%Valakasstatus%", isValakasAlive == 1 ? aliveStatus : Time.format(new Date(isValakasAlive)));
		html.replace("%Tezzastatus%", isTezzaAlive == 1 ? aliveStatus : Time.format(new Date(isTezzaAlive)));
		html.replace("%Sailrenstatus%", isSailrenAlive == 1 ? aliveStatus : Time.format(new Date(isSailrenAlive)));

		//status twn raid boss
		html.replace("%Shadithstatus%", isShadithAlive == 1 ? aliveStatus : Time.format(new Date(isShadithAlive)));
		html.replace("%Hekatonstatus%", isHekatonAlive == 1 ? aliveStatus : Time.format(new Date(isHekatonAlive)));
		html.replace("%Barakielstatus%", isBarakielAlive == 1 ? aliveStatus : Time.format(new Date(isBarakielAlive)));		
		html.replace("%Emberstatus%", isEmberAlive == 1 ? aliveStatus : Time.format(new Date(isEmberAlive)));
		html.replace("%Shyeedstatus%", isShyeedAlive == 1 ? aliveStatus : Time.format(new Date(isShyeedAlive)));
		html.replace("%Korimstatus%", isKorimAlive == 1 ? aliveStatus : Time.format(new Date(isKorimAlive)));
		html.replace("%Galaxiastatus%",isGalaxiaAlive == 1 ? aliveStatus : Time.format(new Date(isGalaxiaAlive)));	
		activeChar.sendPacket(html);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		return "data/html/mods/" + "70009.htm";
	}
	
	private long isRaidBossAlive(int id)
	{
		final StatsSet stats = RaidBossSpawnManager.getStatsSet(id);	
		if (stats == null)
		{
			ScheduledFuture<?> _feat = RaidBossSpawnManager.getInstance().getSchedule(id);
			return _feat != null ? _feat.getDelay(TimeUnit.MILLISECONDS) : 0;
		}
			
		return  stats.getLong("respawnTime") <= System.currentTimeMillis() ? 1 : stats.getLong("respawnTime");		
	}
	
	private long isGrandBossAlive(int id)
	{
		final StatsSet stats = GrandBossManager.getStatsSet(id);
		if (stats == null)
			return 0;
		return  stats.getLong("respawn_time") <= System.currentTimeMillis() ? 1 : stats.getLong("respawn_time");		
	}
}