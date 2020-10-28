package com.l2jhellas.gameserver.communitybbs;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.entity.Castle;

public class ClanList
{
	protected static final Logger _log = Logger.getLogger(ClanList.class.getName());

	private final StringBuilder _clanList = new StringBuilder();
	
	public ClanList(int type)
	{
		loadClanList(type);
	}
	
	private void loadClanList(int type)
	{
		List<L2Clan> clans = ClanTable.getInstance().getClans().stream().filter(Objects::nonNull).sorted((x1, x2) -> Integer.compare(x2.getLevel(),x1.getLevel())).collect(Collectors.toList());
		clans = clans.subList((1 - 1) * 15, Math.min(1 * 15, clans.size()));

	    AtomicInteger counter = new AtomicInteger(0);

		clans.forEach(clan ->
		{		
			final Castle castle = CastleManager.getInstance().getCastleById(clan.hasCastle());
			boolean hasAlly = clan.getAllyId() > 0;			
			String AllyStatus = !hasAlly ? "-" : clan.getAllyId() == clan.getAllyId() ? "Alliance Leader" : "Affiliated Clan";
			
			addClanToList(counter.incrementAndGet(), clan.getName(), hasAlly ? clan.getAllyName() : "-",clan.getLeaderName(), clan.getLevel(), clan.getReputationScore(), castle == null ? "-" : castle.getName(), AllyStatus);
		});
	}
	
	private void addClanToList(int pos, String clan, String ally, String leadername, int clanlevel, int reputation, String castlename, String allystatus)
	{
		_clanList.append("<table border=0 cellspacing=0 cellpadding=2 width=610>");
		_clanList.append("<tr>");
		_clanList.append("<td FIXWIDTH=5></td>");
		_clanList.append("<td FIXWIDTH=20>" + pos + "</td>");
		_clanList.append("<td FIXWIDTH=90>" + clan + "</td>");
		_clanList.append("<td FIXWIDTH=90>" + ally + "</td>");
		_clanList.append("<td FIXWIDTH=85>" + leadername + "</td>");
		_clanList.append("<td FIXWIDTH=45 align=center>" + clanlevel + "</td>");
		_clanList.append("<td FIXWIDTH=70 align=center>" + reputation + "</td>");
		_clanList.append("<td FIXWIDTH=50 align=center>" + castlename + "</td>");
		_clanList.append("<td FIXWIDTH=70 align=center>" + allystatus + "</td>");
		_clanList.append("<td FIXWIDTH=5></td>");
		_clanList.append("</tr>");
		_clanList.append("</table>");
		_clanList.append("<img src=\"L2UI.Squaregray\" width=\"610\" height=\"1\">");
	}
	
	public String loadClanList()
	{
		return _clanList.toString();
	}
}