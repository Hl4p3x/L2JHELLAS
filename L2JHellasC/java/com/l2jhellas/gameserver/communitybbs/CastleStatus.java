package com.l2jhellas.gameserver.communitybbs;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.model.L2Clan;

public class CastleStatus
{
	protected static final Logger _log = Logger.getLogger(CastleStatus.class.getName());
	
	private final StringBuilder _playerList = new StringBuilder();
	
	public CastleStatus()
	{
		loadCastle();
	}
	
	private void loadCastle()
	{		
		CastleManager.getInstance().getCastles().forEach(castle ->
		{		
			List<L2Clan> CastleOwners = ClanTable.getInstance().getClans().stream().filter(Objects::nonNull).filter(clan -> clan.hasCastle() == castle.getCastleId()).collect(Collectors.toList());			
			
			CastleOwners.forEach(owner ->
			{		
				addCastleToList(castle.getName(),  owner.getLeaderName(), owner.getLevel(), castle.getTaxPercent(), castle.getSiegeDate().getTime().toString());
			});	
		});
	}
	
	private void addCastleToList(String name, String owner, int level, int tax, String siegeDate)
	{
		_playerList.append("<table border=0 cellspacing=0 cellpadding=2 width=610>");
		_playerList.append("<tr>");
		_playerList.append("<td FIXWIDTH=10></td>");
		_playerList.append("<td FIXWIDTH=100>" + name + "</td>");
		_playerList.append("<td FIXWIDTH=100>" + owner + "</td>");
		_playerList.append("<td FIXWIDTH=80>" + level + "</td>");
		_playerList.append("<td FIXWIDTH=40>" + tax + "</td>");
		_playerList.append("<td FIXWIDTH=180>" + siegeDate + "</td>");
		_playerList.append("<td FIXWIDTH=5></td>");
		_playerList.append("</tr>");
		_playerList.append("</table>");
		_playerList.append("<img src=\"L2UI.Squaregray\" width=\"610\" height=\"1\">");
	}
	
	public String loadCastleList()
	{
		return _playerList.toString();
	}
}