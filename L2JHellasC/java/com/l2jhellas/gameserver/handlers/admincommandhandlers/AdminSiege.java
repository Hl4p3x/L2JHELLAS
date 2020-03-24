package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.Calendar;
import java.util.StringTokenizer;

import com.l2jhellas.gameserver.SevenSigns;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.instancemanager.AuctionManager;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.ClanHallManager;
import com.l2jhellas.gameserver.instancemanager.SiegeGuardManager;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.model.entity.ClanHall;
import com.l2jhellas.gameserver.model.zone.type.L2ClanHallZone;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.util.StringUtil;
import com.l2jhellas.util.Util;

public class AdminSiege implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_siege",
		"admin_add_attacker",
		"admin_add_defender",
		"admin_add_guard",
		"admin_list_siege_clans",
		"admin_clear_siege_list",
		"admin_move_defenders",
		"admin_spawn_doors",
		"admin_endsiege",
		"admin_startsiege",
		"admin_setsiegetime",
		"admin_setcastle",
		"admin_removecastle",
		"admin_clanhall",
		"admin_clanhallset",
		"admin_clanhalldel",
		"admin_clanhallopendoors",
		"admin_clanhallclosedoors",
		"admin_clanhallteleportself"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		command = st.nextToken();
		
		Castle castle = null;
		ClanHall clanhall = null;
		if (st.hasMoreTokens())
		{
			L2PcInstance player = null;
			
			if ((activeChar.getTarget() != null) && activeChar.getTarget().isPlayer())
				player = activeChar.getTarget().getActingPlayer();
			
			String val = st.nextToken();
			if (command.startsWith("admin_clanhall"))
			{
				if (Util.isDigit(val))
				{
					clanhall = ClanHallManager.getInstance().getClanHallById(Integer.parseInt(val));
					L2Clan clan = null;
					switch (command)
					{
						case "admin_clanhallset":
						if ((player == null) || (player.getClan() == null))
						{
							activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
							return false;
						}
							
						if (clanhall.getOwnerId() > 0)
						{
							activeChar.sendMessage("This Clan Hall is not free!");
							return false;
						}
							
						clan = player.getClan();
							
						if (clan.hasHideout() > 0)
						{
							activeChar.sendMessage("You have already a Clan Hall!");
							return false;
						}
						
						ClanHallManager.getInstance().setOwner(clanhall.getId(), clan);
						
						if (AuctionManager.getInstance().getAuction(clanhall.getId()) != null)
							AuctionManager.getInstance().getAuction(clanhall.getId()).deleteAuctionFromDB();
							
						break;
						case "admin_clanhalldel":
						if (!ClanHallManager.getInstance().isFree(clanhall.getId()))
						{
							ClanHallManager.getInstance().setFree(clanhall.getId());
							AuctionManager.getInstance().initNPC(clanhall.getId());
						}
						else
							activeChar.sendMessage("This Clan Hall is already free!");
							break;
						case "admin_clanhallopendoors":
							clanhall.openCloseDoors(true);
							break;
						case "admin_clanhallclosedoors":
							clanhall.openCloseDoors(false);
							break;
						case "admin_clanhallteleportself":
							final L2ClanHallZone zone = clanhall.getZone();
							if (zone != null)
								activeChar.teleToLocation(zone.getSpawnLoc(), true);
							break;
						default:
							showClanHallPage(activeChar, clanhall);
							break;
					}
				}
			}
			else
			{
				castle = CastleManager.getInstance().getCastle(val);
				switch (command)
				{
					case "admin_add_attacker":
						if (player == null)
							activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
						else
							castle.getSiege().registerAttacker(player, true);
						break;
					case "admin_add_defender":
						if (player == null)
							activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
						else
							castle.getSiege().registerDefender(player, true);
						break;
					case "admin_add_guard":
						if (st.hasMoreTokens())
						{
							val = st.nextToken();
							if (Util.isDigit(val))
							{
								SiegeGuardManager.getInstance().addSiegeGuard(castle,activeChar, Integer.parseInt(val));
								break;
							}
						}
						activeChar.sendMessage("Usage: //add_guard castle npcId");
						break;
					case "admin_clear_siege_list":
						castle.getSiege().clearSiegeClan();
						break;
					case "admin_endsiege":
						castle.getSiege().endSiege();
						break;
					case "admin_list_siege_clans":
						castle.getSiege().listRegisterClan(activeChar);
						break;
					case "admin_move_defenders":
						activeChar.sendMessage("Not implemented yet.");
						break;
					case "admin_setcastle":
						if ((player == null) || (player.getClan() == null))
							activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
						else
							castle.setOwner(player.getClan());
						break;
					case "admin_removecastle":
						final L2Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
						if (clan != null)
							castle.removeOwner(clan);
						else
							activeChar.sendMessage("Unable to remove castle.");
						break;
					case "admin_setsiegetime":
						if (st.hasMoreTokens())
						{
							final Calendar cal = Calendar.getInstance();
							cal.setTimeInMillis(castle.getSiegeDate().getTimeInMillis());
							
							val = st.nextToken();
							
							if ("month".equals(val))
							{
								int month = cal.get(Calendar.MONTH) + Integer.parseInt(st.nextToken());
								if ((cal.getActualMinimum(Calendar.MONTH) > month) || (cal.getActualMaximum(Calendar.MONTH) < month))
								{
									activeChar.sendMessage("Unable to change Siege Date - Incorrect month value only " + cal.getActualMinimum(Calendar.MONTH) + "-" + cal.getActualMaximum(Calendar.MONTH) + " is accepted!");
									return false;
								}
								cal.set(Calendar.MONTH, month);
							}
							else if ("day".equals(val))
							{
								int day = Integer.parseInt(st.nextToken());
								if ((cal.getActualMinimum(Calendar.DAY_OF_MONTH) > day) || (cal.getActualMaximum(Calendar.DAY_OF_MONTH) < day))
								{
									activeChar.sendMessage("Unable to change Siege Date - Incorrect day value only " + cal.getActualMinimum(Calendar.DAY_OF_MONTH) + "-" + cal.getActualMaximum(Calendar.DAY_OF_MONTH) + " is accepted!");
									return false;
								}
								cal.set(Calendar.DAY_OF_MONTH, day);
							}
							else if ("hour".equals(val))
							{
								int hour = Integer.parseInt(st.nextToken());
								if ((cal.getActualMinimum(Calendar.HOUR_OF_DAY) > hour) || (cal.getActualMaximum(Calendar.HOUR_OF_DAY) < hour))
								{
									activeChar.sendMessage("Unable to change Siege Date - Incorrect hour value only " + cal.getActualMinimum(Calendar.HOUR_OF_DAY) + "-" + cal.getActualMaximum(Calendar.HOUR_OF_DAY) + " is accepted!");
									return false;
								}
								cal.set(Calendar.HOUR_OF_DAY, hour);
							}
							else if ("min".equals(val))
							{
								int min = Integer.parseInt(st.nextToken());
								if ((cal.getActualMinimum(Calendar.MINUTE) > min) || (cal.getActualMaximum(Calendar.MINUTE) < min))
								{
									activeChar.sendMessage("Unable to change Siege Date - Incorrect minute value only " + cal.getActualMinimum(Calendar.MINUTE) + "-" + cal.getActualMaximum(Calendar.MINUTE) + " is accepted!");
									return false;
								}
								cal.set(Calendar.MINUTE, min);
							}
							
							if (cal.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
								activeChar.sendMessage("Unable to change Siege Date");
							else if (cal.getTimeInMillis() != castle.getSiegeDate().getTimeInMillis())
							{
								castle.getSiegeDate().setTimeInMillis(cal.getTimeInMillis());
								castle.getSiege().saveSiegeDate();
								activeChar.sendMessage("Castle siege time for castle " + castle.getName() + " has been changed.");
							}
						}
						showSiegeTimePage(activeChar, castle);
						break;
					case "admin_spawn_doors":
						castle.spawnDoor();
						break;
					case "admin_startsiege":
						castle.getSiege().startSiege();
						break;
					default:
						showSiegePage(activeChar, castle.getName());
						break;
				}
			}
		}
		else
			showCastleSelectPage(activeChar);
		
		return true;
	}
	
	protected void showCastleSelectPage(L2PcInstance activeChar)
	{
		int i = 0;
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/castles.htm");	
		final StringBuilder cList = new StringBuilder(500);
		
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle != null)
			{
				String name = castle.getName();
				StringUtil.append(cList, "<td fixwidth=90><a action=\"bypass -h admin_siege ", name, "\">", name, "</a></td>");
				i++;
			}
			if (i > 2)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		adminReply.replace("%castles%", cList.toString());
		cList.setLength(0);
		i = 0;
		for (ClanHall clanhall : ClanHallManager.getInstance().getClanHalls().values())
		{
			if (clanhall != null)
			{
				StringUtil.append(cList, "<td fixwidth=134><a action=\"bypass -h admin_clanhall ", String.valueOf(clanhall.getId()), "\">", clanhall.getName(), "</a></td>");
				i++;
			}
			if (i > 1)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		adminReply.replace("%clanhalls%", cList.toString());
		cList.setLength(0);
		i = 0;
		for (ClanHall clanhall : ClanHallManager.getInstance().getFreeClanHalls().values())
		{
			if (clanhall != null)
			{
				StringUtil.append(cList, "<td fixwidth=134><a action=\"bypass -h admin_clanhall ", String.valueOf(clanhall.getId()), "\">", clanhall.getName(), "</a></td>");
				i++;
			}
			if (i > 1)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		adminReply.replace("%freeclanhalls%", cList.toString());
		activeChar.sendPacket(adminReply);
	}
	
	protected void showSiegePage(L2PcInstance activeChar, String castleName)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/castle.htm");
		adminReply.replace("%castleName%", castleName);
		activeChar.sendPacket(adminReply);
	}
	
	protected void showSiegeTimePage(L2PcInstance activeChar, Castle castle)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/castlesiegetime.htm");
		adminReply.replace("%castleName%", castle.getName());
		adminReply.replace("%time%", castle.getSiegeDate().getTime().toString());
		final Calendar newDay = Calendar.getInstance();
		boolean isSunday = false;
		
		if (newDay.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
			isSunday = true;
		else
			newDay.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		
		if (!SevenSigns.getInstance().isDateInSealValidPeriod(newDay))
			newDay.add(Calendar.DAY_OF_MONTH, 7);
		
		if (isSunday)
		{
			adminReply.replace("%sundaylink%", String.valueOf(newDay.get(Calendar.DAY_OF_YEAR)));
			adminReply.replace("%sunday%", String.valueOf(newDay.get(Calendar.MONTH) + "/" + String.valueOf(newDay.get(Calendar.DAY_OF_MONTH))));
			newDay.add(Calendar.DAY_OF_MONTH, 13);
			adminReply.replace("%saturdaylink%", String.valueOf(newDay.get(Calendar.DAY_OF_YEAR)));
			adminReply.replace("%saturday%", String.valueOf(newDay.get(Calendar.MONTH) + "/" + String.valueOf(newDay.get(Calendar.DAY_OF_MONTH))));
		}
		else
		{
			adminReply.replace("%saturdaylink%", String.valueOf(newDay.get(Calendar.DAY_OF_YEAR)));
			adminReply.replace("%saturday%", String.valueOf(newDay.get(Calendar.MONTH) + "/" + String.valueOf(newDay.get(Calendar.DAY_OF_MONTH))));
			newDay.add(Calendar.DAY_OF_MONTH, 1);
			adminReply.replace("%sundaylink%", String.valueOf(newDay.get(Calendar.DAY_OF_YEAR)));
			adminReply.replace("%sunday%", String.valueOf(newDay.get(Calendar.MONTH) + "/" + String.valueOf(newDay.get(Calendar.DAY_OF_MONTH))));
		}
		activeChar.sendPacket(adminReply);
	}
	
	protected void showClanHallPage(L2PcInstance activeChar, ClanHall clanhall)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/clanhall.htm");
		
		adminReply.replace("%clanhallName%", clanhall.getName());
		adminReply.replace("%clanhallId%", String.valueOf(clanhall.getId()));
		final L2Clan owner = ClanTable.getInstance().getClan(clanhall.getOwnerId());
		adminReply.replace("%clanhallOwner%", (owner == null) ? "None" : owner.getName());
		activeChar.sendPacket(adminReply);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}