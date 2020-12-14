package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jhellas.gameserver.datatables.sql.CharNameTable;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.enums.Sex;
import com.l2jhellas.gameserver.enums.player.ClassId;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.network.L2GameClient;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.network.serverpackets.PartySmallWindowAll;
import com.l2jhellas.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import com.l2jhellas.gameserver.network.serverpackets.SetSummonRemainTime;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.util.StringUtil;
import com.l2jhellas.util.Util;

public class AdminEditChar implements IAdminCommandHandler
{
	protected static final Logger _log = Logger.getLogger(AdminEditChar.class.getName());
	
	private static String[] ADMIN_COMMANDS =
	{
		"admin_setname", // changes char name
		"admin_edit_character",
		"admin_current_player",
		"admin_nokarma",
		"admin_setkarma",
		"admin_character_list", // same as character_info, kept for compatibility purposes
		"admin_character_info", // given a player name, displays an information window
		"admin_show_characters",
		"admin_find_character",
		"admin_find_ip", // find all the player connections from a given IPv4 number
		"admin_find_account", // list all the characters from an account (useful for GMs w/o DB access)
		"admin_find_dualbox", // list all the IPs with more than 1 char logged in (dualbox)
		"admin_save_modifications", // consider it deprecated...
		"admin_rec",
		"admin_setclass",
		"admin_settitle",
		"admin_setsex",
		"admin_setcolor",
		"admin_fullfood",
		"admin_remclanwait",
		"admin_setcp",
		"admin_sethp",
		"admin_setmp",
		"admin_setchar_cp",
		"admin_setchar_hp",
		"admin_character_disconnect",
		"admin_setchar_mp"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		
		if (command.equals("admin_character_disconnect"))
			disconnectCharacter(activeChar);
		
		if (command.equals("admin_current_player"))
			showCharacterInfo(activeChar, null);
		else if ((command.startsWith("admin_character_list")) || (command.startsWith("admin_character_info")))
		{
			try
			{
				String val = command.substring(21);
				L2PcInstance target = L2World.getInstance().getPlayer(val);
				if (target != null)
					showCharacterInfo(activeChar, target);
				else
					activeChar.sendPacket(SystemMessageId.CHARACTER_DOES_NOT_EXIST);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //character_info <player_name>");
			}
		}
		else if (command.startsWith("admin_show_characters"))
		{
			try
			{
				String val = command.substring(22);
				int page = Integer.parseInt(val);
				listCharacters(activeChar, page);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// Case of empty page number
				activeChar.sendMessage("Usage: //show_characters <page_number>");
			}
		}
		else if (command.startsWith("admin_find_character"))
		{
			try
			{
				String val = command.substring(21);
				findCharacter(activeChar, val);
			}
			catch (Exception e)
			{ // Case of empty character name
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.GM_S1);
				sm.addString("You didnt enter a character name to find.");
				activeChar.sendPacket(sm);
				
				listCharacters(activeChar, 0);
			}
		}
		else if (command.startsWith("admin_find_ip"))
		{
			try
			{
				String val = command.substring(14);
				findCharactersPerIp(activeChar, val);
			}
			catch (Exception e)
			{ // Case of empty or malformed IP number
				activeChar.sendMessage("Usage: //find_ip <www.xxx.yyy.zzz>");
				listCharacters(activeChar, 0);
			}
		}
		else if (command.startsWith("admin_find_account"))
		{
			try
			{
				String val = command.substring(19);
				findCharactersPerAccount(activeChar, val);
			}
			catch (Exception e)
			{ // Case of empty or malformed player name
				activeChar.sendMessage("Usage: //find_account <player_name>");
				listCharacters(activeChar, 0);
			}
		}
		else if (command.startsWith("admin_find_dualbox"))
		{
			int multibox = 2;
			try
			{
				String val = command.substring(19);
				multibox = Integer.parseInt(val);
			}
			catch (Exception e)
			{
			}
			findDualbox(activeChar, multibox);
		}
		
		else if (command.equals("admin_edit_character"))
			editCharacter(activeChar);
		
		// Karma control commands
		else if (command.equals("admin_nokarma"))
		{
			setTargetKarma(activeChar, 0);
		}
		else if (command.startsWith("admin_setkarma"))
		{
			try
			{
				String val = command.substring(15);
				int karma = Integer.parseInt(val);
				// if (activeChar == activeChar.getTarget()) L2EMU_DISABLE
				// Visor123 fix
				setTargetKarma(activeChar, karma);
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_save_modifications"))
		{
			try
			{
				String val = command.substring(24);
				adminModifyCharacter(activeChar, val);
			}
			catch (Exception e)
			{ // Case of empty character name
				activeChar.sendMessage("Error while modifying character.");
				listCharacters(activeChar, 0);
			}
		}
		else if (command.equals("admin_rec"))
		{
			final L2PcInstance  player = activeChar.getTarget() != null ? activeChar.getTarget().getActingPlayer() : activeChar;
			
			if (player == null)
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			player.setRecomHave(player.getRecomHave() + 1);
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.GM_S1);
			sm.addString("You have been recommended by a GM");
			player.sendPacket(sm);
			player.broadcastUserInfo();
		}
		else if (command.startsWith("admin_rec"))
		{
			try
			{
				String val = command.substring(10);
				int recVal = Integer.parseInt(val);
				
				final L2PcInstance  player = activeChar.getTarget() != null ? activeChar.getTarget().getActingPlayer() : activeChar;
				
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}
				
				player.setRecomHave(player.getRecomHave() + recVal);
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.GM_S1);
				sm.addString("You have been recommended by a GM");
				player.sendPacket(sm);
				player.broadcastUserInfo();
			}
			catch (NumberFormatException nfe)
			{
				activeChar.sendMessage("You must specify the number of recommendations to add.");
			}
		}
		else if (command.startsWith("admin_setclass"))
		{
			try
			{
				String val = command.substring(15).trim();
				int classidval = Integer.parseInt(val);

				final L2PcInstance  player = activeChar.getTarget() != null ? activeChar.getTarget().getActingPlayer() : activeChar;
				
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}
								
				if ((ClassId.getClassId(classidval) != null) && (player.getClassId().getId() != classidval))
				{		
					player.setClassId(classidval);
					
					if (player.isSubClassActive())
						player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClass());
					else
						player.setBaseClass(player.getActiveClass());
					
					final String newclass = player.getTemplate().className;
					player.store();
					
					if (player != activeChar)
						player.sendMessage("A GM changed your class to " + newclass);
					
					player.broadcastUserInfo();
					player.sendSkillList();
					activeChar.sendMessage(player.getName() + " is a " + newclass + ".");
				}
				else
					activeChar.sendMessage("Usage: //setclass <valid_new_classid>");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				AdminHelpPage.showHelpPage(activeChar, "charclasses.htm");
			}
			catch (NumberFormatException e)
			{
				activeChar.sendMessage("Usage: //setclass <valid_new_classid>");
			}
		}
		else if (command.startsWith("admin_settitle"))
		{
			String val = "";
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			L2Npc npc = null;
			
			if (target == null)
				player = activeChar;
			else if (target instanceof L2PcInstance)
				player = (L2PcInstance) target;
			else if (target instanceof L2Npc)
				npc = (L2Npc) target;
			else
				return false;
			
			if (st.hasMoreTokens())
				val = st.nextToken();
			while (st.hasMoreTokens())
				val += " " + st.nextToken();
			
			if (player != null)
			{
				player.setTitle(val);
				if (player != activeChar)
					player.sendMessage("Your title has been changed by a GM");
				player.broadcastTitleInfo();
			}
			else if (npc != null)
			{
				npc.setTitle(val);
				npc.broadcastInfo();
			}
		}
		else if (command.startsWith("admin_setname"))
		{
			try
			{
				final L2Object target = activeChar.getTarget();
				final String _newName = command.substring(14);
				
				if (_newName.isEmpty())
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
					return false;
				}
				
				if (target instanceof L2PcInstance)
				{
					L2PcInstance player = (L2PcInstance) target;
					
					if (CharNameTable.getInstance().getIdByName(_newName) > 0)
					{
						activeChar.sendMessage("Warning, player name " + _newName + " already exists.");
						return false;
					}
					
					if (NpcData.getInstance().getTemplateByName(_newName) != null)
					{
						activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
						return false;
					}
					
					L2Clan temp = player.getClan();
					boolean wasLeader = false;
					
					if (temp != null)
					{
						
						if (player.getClan().getLeader().getObjectId() == target.getObjectId())
							wasLeader = true;
						
						temp.removeClanMember(player.getName(), 0);
					}
					L2World.getInstance().removeObject(player);
					CharNameTable.getInstance().removeName(player.getObjectId());
					
					player.setName(_newName);
					L2World.getInstance().storeObject(player);
					player.store();
					CharNameTable.getInstance().addName(player);
					player.broadcastUserInfo();
					player.sendMessage("Your name has been changed by a GM.");

					if (player.isInParty())
					{
						player.getParty().broadcastToPartyMembers(player, new PartySmallWindowDeleteAll());
						for (L2PcInstance member : player.getParty().getPartyMembers())
						{
							if (member != player)
								member.sendPacket(new PartySmallWindowAll(member, player.getParty()));
						}
					}
					
					if (temp != null)
					{
						temp.addClanMember(player);
						
						if (wasLeader)
							temp.setNewLeader(temp.getClanMember(player.getObjectId()), player, true);
						
						temp.updateClanInDB();
						player.getClan().updateClanMember(player);
						player.getClan().broadcastClanStatus();
					}
					
					temp = null;
					wasLeader = false;
					RegionBBSManager.getInstance().changeCommunityBoard();
				}
				else if (target instanceof L2Npc)
				{
					L2Npc npc = (L2Npc) target;
					npc.setName(_newName);
					npc.broadcastInfo();
				}
				else
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //setname name");
			}
			
		}
		else if (command.startsWith("admin_setsex"))
		{	
			final L2PcInstance  player = activeChar.getTarget() != null ? activeChar.getTarget().getActingPlayer() : activeChar;

			if (player == null)
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			if (player.getAppearance().getSex() != Sex.MALE)
			{
				player.getAppearance().setSex(Sex.MALE);
				player.sendMessage("Your gender has been changed by a GM.");
				player.decayMe();
				player.spawnMe(player.getX(), player.getY(), player.getZ());
				player.broadcastUserInfo();
			}
			else
			{
				player.getAppearance().setSex(Sex.FEMALE);
				player.sendMessage("Your gender has been changed by a GM.");
				player.decayMe();
				player.spawnMe(player.getX(), player.getY(), player.getZ());
				player.broadcastUserInfo();
			}
		}
		else if (command.startsWith("admin_setcolor"))
		{		
			final L2PcInstance  player = activeChar.getTarget() != null ? activeChar.getTarget().getActingPlayer() : activeChar;
			
			if (!activeChar.isGM())
			{
				activeChar.sendMessage("Your can't perform that action without access!");
				return false;
			}
			
			if (player == null)
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
		
			player.getAppearance().setNameColor(Integer.decode("0x" + command.substring(15)));
			player.sendMessage("Your name color has been changed by a GM.");
			player.broadcastUserInfo();
		}
		else if (command.startsWith("admin_fullfood"))
		{
			L2Object target = activeChar.getTarget();
			if (target instanceof L2PetInstance)
			{
				L2PetInstance targetPet = (L2PetInstance) target;
				targetPet.setCurrentFed(targetPet.getMaxFed());
				targetPet.getOwner().sendPacket(new SetSummonRemainTime(targetPet.getMaxFed(), targetPet.getCurrentFed()));
			}
			else
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
		else if (command.equals("admin_remclanwait"))
		{
			final L2PcInstance  player = activeChar.getTarget() != null ? activeChar.getTarget().getActingPlayer() : activeChar;
			
			if (player == null)
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
					
			if (player.getClan() == null)
			{
				player.setClanJoinExpiryTime(0);
				player.sendMessage("A GM Has reset your clan wait time, You may now join another clan.");
				activeChar.sendMessage("You have reset " + player.getName() + "'s wait time to join another clan.");
			}
			else
			{
				activeChar.sendMessage("Sorry, but " + player.getName() + " must not be in a clan. Player must leave clan before the wait limit can be reset.");
			}
		}
		StringTokenizer st2 = new StringTokenizer(command, " ");
		String cmand = st2.nextToken(); // get command
		if (cmand.equals("admin_setcp"))
		{
			int cp = 0;
			try
			{
				cp = Integer.parseInt(st2.nextToken());
				
			}
			catch (Exception e)
			{
				_log.warning(AdminEditChar.class.getName() + ": invalid command ");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
			activeChar.getStatus().setCurrentCp(cp);
		}
		if (cmand.equals("admin_sethp"))
		{
			int hp = 0;
			try
			{
				hp = Integer.parseInt(st2.nextToken());
				
			}
			catch (Exception e)
			{
				_log.warning(AdminEditChar.class.getName() + ": invalid command ");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
			activeChar.getStatus().setCurrentHp(hp);
		}
		if (cmand.equals("admin_setmp"))
		{
			int mp = 0;
			try
			{
				mp = Integer.parseInt(st2.nextToken());
				
			}
			catch (Exception e)
			{
				_log.warning(AdminEditChar.class.getName() + ": invalid command ");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
			activeChar.getStatus().setCurrentMp(mp);
		}
		if (cmand.equals("admin_setchar_cp"))
		{
			int cp = 0;
			try
			{
				cp = Integer.parseInt(st2.nextToken());
				
			}
			catch (Exception e)
			{
				_log.warning(AdminEditChar.class.getName() + ": invalid command ");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
			if (activeChar.getTarget() instanceof L2PcInstance)
			{
				L2PcInstance pc = (L2PcInstance) activeChar.getTarget();
				pc.getStatus().setCurrentCp(cp);
			}
			else if (activeChar.getTarget() instanceof L2PetInstance)
			{
				L2PetInstance pet = (L2PetInstance) activeChar.getTarget();
				pet.getStatus().setCurrentCp(cp);
			}
			else
				activeChar.getStatus().setCurrentCp(cp);
		}
		if (cmand.equals("admin_setchar_hp"))
		{
			int hp = 0;
			try
			{
				hp = Integer.parseInt(st2.nextToken());
				
			}
			catch (Exception e)
			{
				_log.warning(AdminEditChar.class.getName() + ": invalid command ");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
			if (activeChar.getTarget() instanceof L2PcInstance)
			{
				L2PcInstance pc = (L2PcInstance) activeChar.getTarget();
				pc.getStatus().setCurrentHp(hp);
			}
			else if (activeChar.getTarget() instanceof L2PetInstance)
			{
				L2PetInstance pet = (L2PetInstance) activeChar.getTarget();
				pet.getStatus().setCurrentHp(hp);
			}
			else
				activeChar.getStatus().setCurrentHp(hp);
		}
		if (cmand.equals("admin_setchar_mp"))
		{
			int mp = 0;
			try
			{
				mp = Integer.parseInt(st2.nextToken());
				
			}
			catch (Exception e)
			{
				_log.warning(AdminEditChar.class.getName() + ": invalid command ");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
			if (activeChar.getTarget() instanceof L2PcInstance)
			{
				L2PcInstance pc = (L2PcInstance) activeChar.getTarget();
				pc.getStatus().setCurrentMp(mp);
			}
			else if (activeChar.getTarget() instanceof L2PetInstance)
			{
				L2PetInstance pet = (L2PetInstance) activeChar.getTarget();
				pet.getStatus().setCurrentMp(mp);
			}
			else
				activeChar.getStatus().setCurrentMp(mp);
		}
		return true;
	}
	
	private static void listCharacters(L2PcInstance activeChar, int page)
	{
		Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers().values();
		L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);
		
		int MaxCharactersPerPage = 20;
		int MaxPages = players.length / MaxCharactersPerPage;
		
		if (players.length > MaxCharactersPerPage * MaxPages)
			MaxPages++;
		
		// Check if number of users changed
		if (page > MaxPages)
			page = MaxPages;
		
		int CharactersStart = MaxCharactersPerPage * page;
		int CharactersEnd = players.length;
		if (CharactersEnd - CharactersStart > MaxCharactersPerPage)
			CharactersEnd = CharactersStart + MaxCharactersPerPage;
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/charlist.htm");
		StringBuilder replyMSG = new StringBuilder();
		for (int x = 0; x < MaxPages; x++)
		{
			int pagenr = x + 1;
			replyMSG.append("<center><a action=\"bypass -h admin_show_characters " + x + "\">Page " + pagenr + "</a></center>");
		}
		adminReply.replace("%pages%", replyMSG.toString());
		StringBuilder replyMSG2 = new StringBuilder();
		for (int i = CharactersStart; i < CharactersEnd; i++)
		{ // Add player info into new Table row
			replyMSG2.append("<tr><td width=80><a action=\"bypass -h admin_character_info " + players[i].getName() + "\">" + players[i].getName() + "</a></td><td width=110>" + players[i].getTemplate().className + "</td><td width=40>" + players[i].getLevel() + "</td></tr>");
		}
		adminReply.replace("%players%", replyMSG2.toString());
		activeChar.sendPacket(adminReply);
	}
	
	public static void gatherCharacterInfo(L2PcInstance activeChar, L2PcInstance player, String filename)
	{
		String ip = "N/A";
		String account = "N/A";
		try
		{
			String clientInfo = player.getClient().toString();
			account = clientInfo.substring(clientInfo.indexOf("Account: ") + 9, clientInfo.indexOf(" - IP: "));
			ip = clientInfo.substring(clientInfo.indexOf(" - IP: ") + 7, clientInfo.lastIndexOf("]"));
		}
		catch (Exception e)
		{
		}
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/" + filename);
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%level%", String.valueOf(player.getLevel()));
		adminReply.replace("%clan%", String.valueOf(ClanTable.getInstance().getClan(player.getClanId())));
		adminReply.replace("%xp%", String.valueOf(player.getExp()));
		adminReply.replace("%sp%", String.valueOf(player.getSp()));
		adminReply.replace("%class%", player.getTemplate().className);
		adminReply.replace("%ordinal%", String.valueOf(player.getClassId().ordinal()));
		adminReply.replace("%classid%", String.valueOf(player.getClassId()));
		adminReply.replace("%x%", String.valueOf(player.getX()));
		adminReply.replace("%y%", String.valueOf(player.getY()));
		adminReply.replace("%z%", String.valueOf(player.getZ()));
		adminReply.replace("%currenthp%", String.valueOf((int) player.getCurrentHp()));
		adminReply.replace("%maxhp%", String.valueOf(player.getMaxHp()));
		adminReply.replace("%karma%", String.valueOf(player.getKarma()));
		adminReply.replace("%currentmp%", String.valueOf((int) player.getCurrentMp()));
		adminReply.replace("%maxmp%", String.valueOf(player.getMaxMp()));
		adminReply.replace("%pvpflag%", String.valueOf(player.getPvpFlag()));
		adminReply.replace("%currentcp%", String.valueOf((int) player.getCurrentCp()));
		adminReply.replace("%maxcp%", String.valueOf(player.getMaxCp()));
		adminReply.replace("%pvpkills%", String.valueOf(player.getPvpKills()));
		adminReply.replace("%pkkills%", String.valueOf(player.getPkKills()));
		adminReply.replace("%currentload%", String.valueOf(player.getCurrentLoad()));
		adminReply.replace("%maxload%", String.valueOf(player.getMaxLoad()));
		adminReply.replace("%percent%", String.valueOf(Util.roundTo(((float) player.getCurrentLoad() / (float) player.getMaxLoad()) * 100, 2)));
		adminReply.replace("%patk%", String.valueOf(player.getPAtk(null)));
		adminReply.replace("%matk%", String.valueOf(player.getMAtk(null, null)));
		adminReply.replace("%pdef%", String.valueOf(player.getPDef(null)));
		adminReply.replace("%mdef%", String.valueOf(player.getMDef(null, null)));
		adminReply.replace("%accuracy%", String.valueOf(player.getAccuracy()));
		adminReply.replace("%evasion%", String.valueOf(player.getEvasionRate(null)));
		adminReply.replace("%critical%", String.valueOf(player.getCriticalHit(null, null)));
		adminReply.replace("%runspeed%", String.valueOf(player.getRunSpeed()));
		adminReply.replace("%patkspd%", String.valueOf(player.getPAtkSpd()));
		adminReply.replace("%matkspd%", String.valueOf(player.getMAtkSpd()));
		adminReply.replace("%access%", String.valueOf(player.getAccessLevel().getLevel()));
		adminReply.replace("%account%", account);
		adminReply.replace("%ip%", ip);
		activeChar.sendPacket(adminReply);
	}
	
	private static void setTargetKarma(L2PcInstance activeChar, int newKarma)
	{
		final L2PcInstance  player = activeChar.getTarget() != null ? activeChar.getTarget().getActingPlayer() : activeChar;
		
		if (player == null)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
				
		if (newKarma >= 0)
		{
			// for display
			int oldKarma = player.getKarma();
			
			// update karma
			player.setKarma(newKarma);
			
			StatusUpdate su = new StatusUpdate(player.getObjectId());
			su.addAttribute(StatusUpdate.KARMA, newKarma);
			player.sendPacket(su);
			
			// Common character information
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.GM_S1);
			sm.addString("Admin has changed your karma from " + oldKarma + " to " + newKarma + ".");
			player.sendPacket(sm);
			
			// Admin information
			if (player != activeChar)
				activeChar.sendMessage("Successfully Changed karma for " + player.getName() + " from (" + oldKarma + ") to (" + newKarma + ").");
		}
		else
			activeChar.sendMessage("You must enter a value for karma greater than or equal to 0.");
	}
	
	private static void adminModifyCharacter(L2PcInstance activeChar, String modifications)
	{
		final L2PcInstance  player = activeChar.getTarget() != null ? activeChar.getTarget().getActingPlayer() : activeChar;
		
		if (player == null)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		StringTokenizer st = new StringTokenizer(modifications);
		
		if (st.countTokens() != 6)
		{
			editCharacter(player);
			return;
		}
		
		String hp = st.nextToken();
		String mp = st.nextToken();
		String cp = st.nextToken();
		String pvpflag = st.nextToken();
		String pvpkills = st.nextToken();
		String pkkills = st.nextToken();
		
		int hpval = Integer.parseInt(hp);
		int mpval = Integer.parseInt(mp);
		int cpval = Integer.parseInt(cp);
		int pvpflagval = Integer.parseInt(pvpflag);
		int pvpkillsval = Integer.parseInt(pvpkills);
		int pkkillsval = Integer.parseInt(pkkills);
		
		// Common character information
		player.sendMessage("Admin has changed your stats." + "  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + "  PvP Flag: " + pvpflagval + " PvP/PK " + pvpkillsval + "/" + pkkillsval);
		player.getStatus().setCurrentHp(hpval);
		player.getStatus().setCurrentMp(mpval);
		player.getStatus().setCurrentCp(cpval);
		player.setPvpFlag(pvpflagval);
		player.setPvpKills(pvpkillsval);
		player.setPkKills(pkkillsval);
		
		// Save the changed parameters to the database.
		player.store();
		
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, hpval);
		su.addAttribute(StatusUpdate.MAX_HP, player.getMaxHp());
		su.addAttribute(StatusUpdate.CUR_MP, mpval);
		su.addAttribute(StatusUpdate.MAX_MP, player.getMaxMp());
		su.addAttribute(StatusUpdate.CUR_CP, cpval);
		su.addAttribute(StatusUpdate.MAX_CP, player.getMaxCp());
		player.sendPacket(su);
		
		// Admin information
		player.sendMessage("Changed stats of " + player.getName() + "." + "  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + "  PvP: " + pvpflagval + " / " + pvpkillsval);
		
		if (Config.DEVELOPER || Config.DEBUG)
			_log.config(AdminEditChar.class.getName() + ": [GM]" + activeChar.getName() + " changed stats of " + player.getName() + ". " + " HP: " + hpval + " MP: " + mpval + " CP: " + cpval + " PvP: " + pvpflagval + " / " + pvpkillsval);
		
		showCharacterInfo(activeChar, null); // Back to start
		
		player.broadcastUserInfo();
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.decayMe();
		player.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}
	
	private static void editCharacter(L2PcInstance activeChar)
	{
		final L2PcInstance  player = activeChar.getTarget() != null ? activeChar.getTarget().getActingPlayer() : activeChar;		
		if (player == null)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}		
		gatherCharacterInfo(activeChar, player, "charedit.htm");
	}
	
	private static void findCharacter(L2PcInstance activeChar, String CharacterToFind)
	{
		int CharactersFound = 0;
		String name;
		Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers().values();
		L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/charfind.htm");
		StringBuilder replyMSG = new StringBuilder(1000);
		for (L2PcInstance player : players)
		{ // Add player info into new Table row
			name = player.getName();
			if (name.toLowerCase().contains(CharacterToFind.toLowerCase()))
			{
				CharactersFound = CharactersFound + 1;
				replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list " + name + "\">" + name + "</a></td><td width=110>" + player.getTemplate().className + "</td><td width=40>" + player.getLevel() + "</td></tr>");
			}
			if (CharactersFound > 20)
				break;
		}
		adminReply.replace("%results%", replyMSG.toString());
		final String replyMSG2;
		if (CharactersFound == 0)
			replyMSG2 = "s. Please try again.";
		else if (CharactersFound > 20)
		{
			adminReply.replace("%number%", " more than 20");
			replyMSG2 = "s.<br>Please refine your search to see all of the results.";
		}
		else if (CharactersFound == 1)
			replyMSG2 = ".";
		else
			replyMSG2 = "s.";
		
		adminReply.replace("%number%", String.valueOf(CharactersFound));
		adminReply.replace("%end%", replyMSG2);
		activeChar.sendPacket(adminReply);
	}
	
	private static void findCharactersPerIp(L2PcInstance activeChar, String IpAdress)
	{
		if (!IpAdress.matches("^(?:(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))$"))
			_log.warning(AdminEditChar.class.getName() + ": Malformed IPv4 number");
		
		int CharactersFound = 0;
		String name, ip = "0.0.0.0";
		final StringBuilder replyMSG = new StringBuilder(1000);
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(CharactersFound);
		adminReply.setFile("data/html/admin/ipfind.htm");
		
		Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers().values();
		L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);
		for (L2PcInstance player : players)
		{
			
			ip = player.getClient().getConnection().getInetAddress().getHostAddress();
			if (ip.equals(IpAdress))
			{
				name = player.getName();
				CharactersFound = CharactersFound + 1;
				replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list " + name + "\">" + name + "</a></td><td width=110>" + player.getTemplate().className + "</td><td width=40>" + player.getLevel() + "</td></tr>");
			}
			if (CharactersFound > 20)
				break;
		}
		adminReply.replace("%results%", replyMSG.toString());
		final String replyMSG2;
		if (CharactersFound == 0)
		{
			replyMSG2 = "s. Maybe they got d/c? :)";
		}
		else if (CharactersFound > 20)
		{
			adminReply.replace("%number%", " more than " + String.valueOf(CharactersFound));
			replyMSG2 = "s.<br>In order to avoid you a client crash I won't <br1>display results beyond the 20th character.";
		}
		else if (CharactersFound == 1)
		{
			replyMSG2 = ".";
		}
		else
		{
			replyMSG2 = "s.";
		}
		adminReply.replace("%ip%", IpAdress);
		adminReply.replace("%number%", String.valueOf(CharactersFound));
		adminReply.replace("%end%", replyMSG2);
		activeChar.sendPacket(adminReply);
	}
	
	private static void findCharactersPerAccount(L2PcInstance activeChar, String characterName)
	{
		if (characterName.matches(Config.CNAME_TEMPLATE))
		{
			String account = null;
			Map<Integer, String> chars;
			L2PcInstance player = L2World.getInstance().getPlayer(characterName);
			if (player == null)
			{
				throw new IllegalArgumentException("Player doesn't exist");
			}
			chars = player.getAccountChars();
			account = player.getAccountName();
			final StringBuilder replyMSG = new StringBuilder(chars.size() * 20);
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(0);
			adminReply.setFile("data/html/admin/accountinfo.htm");
			for (String charname : chars.values())
				StringUtil.append(replyMSG, charname, "<br1>");
			
			adminReply.replace("%characters%", replyMSG.toString());
			adminReply.replace("%account%", account);
			adminReply.replace("%player%", characterName);
			activeChar.sendPacket(adminReply);
		}
		else
			_log.warning(AdminEditChar.class.getName() + ": Malformed character name");
	}
	
	private static void findDualbox(L2PcInstance activeChar, int multibox) throws IllegalArgumentException
	{
		Map<String, List<L2PcInstance>> ipMap = new HashMap<>();
		String ip = "0.0.0.0";
		L2GameClient client;
		final Map<String, Integer> dualboxIPs = new HashMap<>();
		
		for (L2PcInstance player : L2World.getInstance().getPlayersSortedBy(Comparator.comparingLong(L2PcInstance::getUptime)))
		{
			client = player.getClient();
			if ((client == null) || client.isDetached())
				continue;
			
			ip = client.getConnection().getInetAddress().getHostAddress();
			
			if (ipMap.get(ip) == null)
				ipMap.put(ip, new ArrayList<L2PcInstance>());

			ipMap.get(ip).add(player);
			
			if (ipMap.get(ip).size() >= multibox)
			{
				Integer count = dualboxIPs.get(ip);
				if (count == null)
					dualboxIPs.put(ip, multibox);
				else
					dualboxIPs.put(ip, count + 1);
			}
		}
		
		List<String> keys = new ArrayList<>(dualboxIPs.keySet());
		keys.sort(Comparator.comparing(s -> dualboxIPs.get(s)).reversed());
		
		final StringBuilder results = new StringBuilder();
		for (String dualboxIP : keys)
		{
			StringUtil.append(results, "<a action=\"bypass -h admin_find_ip " + dualboxIP + "\">" + dualboxIP + " (" + dualboxIPs.get(dualboxIP) + ")</a><br1>");
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(multibox);
		adminReply.setFile("data/html/admin/dualbox.htm");
		adminReply.replace("%multibox%", String.valueOf(multibox));
		adminReply.replace("%results%", results.toString());
		adminReply.replace("%strict%", "");
		activeChar.sendPacket(adminReply);
	}
	
	public static void showCharacterInfo(L2PcInstance activeChar, L2PcInstance player)
	{
		if (player == null)
		{
			L2Object target = activeChar.getTarget();
			if (target instanceof L2PcInstance)
				player = (L2PcInstance) target;
			else
				return;
		}
		else
			activeChar.setTarget(player);
		gatherCharacterInfo(activeChar, player, "charinfo.htm");
	}

	private static void disconnectCharacter(L2PcInstance activeChar)
	{	
		final L2PcInstance player = activeChar.getTarget().getActingPlayer();	
		if (player == null)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		if (player.getObjectId() == activeChar.getObjectId())
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		else
		{						
			player.closeNetConnection(true);		
			RegionBBSManager.getInstance().changeCommunityBoard();
			activeChar.sendMessage("Character " + player.getName() + " disconnected from server.");
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}