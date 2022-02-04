package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.Calendar;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.instancemanager.ClanHallSiegeManager;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.network.serverpackets.SiegeInfo;
import com.l2jhellas.gameserver.scrips.siegable.ClanHallSiegeEngine;
import com.l2jhellas.gameserver.scrips.siegable.SiegableHall;

public final class AdminCHSiege implements IAdminCommandHandler
{
	private static final String[] COMMANDS =
	{
		"admin_chsiege_siegablehall", "admin_chsiege_startSiege",
		"admin_chsiege_endsSiege", "admin_chsiege_setSiegeDate",
		"admin_chsiege_addAttacker", "admin_chsiege_removeAttacker",
		"admin_chsiege_clearAttackers", "admin_chsiege_listAttackers",
		"admin_chsiege_forwardSiege"
	};

	@Override
	public String[] getAdminCommandList()
	{
		return COMMANDS;
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		final String[] split = command.split(" ");
		SiegableHall hall = null;
		if (Config.ALT_DEV_NO_SCRIPT)
		{
			activeChar.sendMessage("Clan Hall Sieges are disabled!");
			return false;
		}
		
		if (split.length < 2)
		{
			activeChar.sendMessage("You have to specify the hall id.");
			return false;
		}
		if ((hall = getHall(split[1], activeChar)) == null)
		{
			activeChar.sendMessage("Couldnt find he desired siegable hall (" + split[1] + ")");
			return false;
		}
		if (hall.getSiege() == null)
		{
			activeChar.sendMessage("The given hall dont have any attached siege!");
			return false;
		}

		switch (split[0])
		{
			case "admin_chsiege_startSiege" :
				if (hall.isInSiege())
					activeChar.sendMessage("The requested clan hall is alredy in siege!");
				else
				{
					L2Clan owner = ClanTable.getInstance().getClan(hall.getOwnerId());
					if (owner != null)
					{
						hall.free();
						owner.setHasHideout(0);
						hall.addAttacker(owner);
					}
					hall.getSiege().startSiege();
				}
				break;
			case "admin_chsiege_endsSiege":
				if (!hall.isInSiege())
					activeChar.sendMessage("The requested clan hall isnt in siege!");
				else
					hall.getSiege().endSiege();
				break;
			case "admin_chsiege_setSiegeDate":
				if (!hall.isRegistering())
					activeChar.sendMessage("Cannot change siege date while hall is in siege");
				else if (split.length < 3)
					activeChar.sendMessage("The date format is incorrect. Try again.");
				else
				{
					String[] rawDate = split[2].split(";");
					if (rawDate.length < 2)
						activeChar.sendMessage("You have to specify this format DD-MM-YYYY;HH:MM");
					else
					{
						String[] day = rawDate[0].split("-");
						String[] hour = rawDate[1].split(":");
						if ((day.length < 3) || (hour.length < 2))
							activeChar.sendMessage("Incomplete day, hour or both!");
						else
						{
							int d = parseInt(day[0]);
							int month = parseInt(day[1]) - 1;
							int year = parseInt(day[2]);
							int h = parseInt(hour[0]);
							int min = parseInt(hour[1]);
							if (((month == 2) && (d > 28)) || (d > 31) || (d <= 0) || (month <= 0) || (month > 12)
							|| (year < Calendar.getInstance().get(Calendar.YEAR)))
								activeChar.sendMessage("Wrong day/month/year");
							else if ((h <= 0) || (h > 24) || (min < 0)|| (min >= 60))
								activeChar.sendMessage("Wrong hour/minutes");
							else
							{
								Calendar c = Calendar.getInstance();
								c.set(Calendar.YEAR, year);
								c.set(Calendar.MONTH, month);
								c.set(Calendar.DAY_OF_MONTH, d);
								c.set(Calendar.HOUR_OF_DAY, h);
								c.set(Calendar.MINUTE, min);
								c.set(Calendar.SECOND, 0);

								if (c.getTimeInMillis() > System.currentTimeMillis())
								{
									activeChar.sendMessage(hall.getName()+ " siege: " + c.getTime().toString());
									hall.setNextSiegeDate(c.getTimeInMillis());
									hall.getSiege().updateSiege();
									hall.updateDb();
								}
								else
									activeChar.sendMessage("The given time is in the past!");
							}
						}

					}
				}
				break;
				
			case "admin_chsiege_addAttacker":
				if (hall.isInSiege())
				{
					activeChar.sendMessage("The clan hall is in siege, cannot add attackers now.");
					return false;
				}

				L2Clan attackerclan = null;
				if (split.length < 3)
				{
					L2PcInstance target = activeChar.getTarget().getActingPlayer();
					if (target == null)
					{
						activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
						return false;
					}
					final L2Clan targetclan = target.getClan();
					if (targetclan == null)
					{
						activeChar.sendMessage("Your target does not have any clan!");
						return false;
					}
					if (hall.getSiege().checkIsAttacker(targetclan))
					{
						activeChar.sendMessage("Your target's clan is alredy participating!");
						return false;
					}
					attackerclan = targetclan;
				}
				else
				{
					L2Clan dbClan = ClanTable.getInstance().getClanByName(split[2]);
					if (dbClan == null)
						activeChar.sendMessage("The given clan does not exist!");
					else if (hall.getSiege().checkIsAttacker(dbClan))
						activeChar.sendMessage("The given clan is alredy participating!");
					else
						attackerclan = dbClan;
				}
				if (attackerclan != null)
					hall.addAttacker(attackerclan);
				break;
			case "admin_chsiege_removeAttacker":
				if (hall.isInSiege())
				{
					activeChar.sendMessage("The clan hall is in siege, cannot remove attackers now.");
					return false;
				}

				if (split.length < 3)
				{
					L2PcInstance target = activeChar.getTarget().getActingPlayer();
					if (target == null)
					{
						activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
						return false;
					}
					
					final L2Clan targetclan = target.getClan();
					if (targetclan == null)
					{
						activeChar.sendMessage("Your target does not have any clan!");
						return false;
					}

					if (!hall.getSiege().checkIsAttacker(targetclan))
					{
						activeChar.sendMessage("Your target's clan is not participating!");
						return false;
					}

					hall.removeAttacker(target.getClan());
				}
				else
				{
					L2Clan dbClan = ClanTable.getInstance().getClanByName(split[2]);
					if (dbClan == null)
						activeChar.sendMessage("The given clan does not exist!");
					else if (!hall.getSiege().checkIsAttacker(dbClan))
						activeChar.sendMessage("The given clan is not participating!");
					else
						hall.removeAttacker(dbClan);
				}
				break;
				
			case "admin_chsiege_clearAttackers":
				if (hall.isInSiege())
					activeChar.sendMessage("The requested hall is in siege right now, cannot clear attacker list!");
				else
					hall.getSiege().getAttackers().clear();
				break;
			case "admin_chsiege_listAttackers":
				activeChar.sendPacket(new SiegeInfo(hall));
               break;
			case "admin_chsiege_forwardSiege":
				ClanHallSiegeEngine siegable = hall.getSiege();
				siegable.cancelSiegeTask();
				switch (hall.getSiegeStatus())
				{
					case REGISTERING :
						siegable.prepareOwner();
						break;
					case WAITING_BATTLE :
						siegable.startSiege();
						break;
					case RUNNING :
						siegable.endSiege();
						break;
				}
				break;								
		}
		sendSiegableHallPage(activeChar, split[1], hall);
		return false;
	}

	private SiegableHall getHall(String id, L2PcInstance gm)
	{
		int ch = parseInt(id);
		if (ch == 0)
		{
			gm.sendMessage("Wrong clan hall id, unparseable id!");
			return null;
		}

		SiegableHall hall = ClanHallSiegeManager.getInstance().getSiegableHall(ch);

		if (hall == null)
			gm.sendMessage("Couldnt find the clan hall.");

		return hall;
	}

	private int parseInt(String st)
	{
		int val = 0;
		try
		{
			val = Integer.parseInt(st);
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		return val;
	}
	
	private void sendSiegableHallPage(L2PcInstance activeChar, String hallId,SiegableHall hall)
	{
		final NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setFile("data/html/admin/siegablehall.htm");
		msg.replace("%clanhallId%", hallId);
		msg.replace("%clanhallName%", hall.getName());
		if (hall.getOwnerId() > 0)
		{
			L2Clan owner = ClanTable.getInstance().getClan(hall.getOwnerId());
			if (owner != null)
				msg.replace("%clanhallOwner%", owner.getName());
			else
				msg.replace("%clanhallOwner%", "No Owner");
		}
		else
			msg.replace("%clanhallOwner%", "No Owner");
		activeChar.sendPacket(msg);
	}
}
