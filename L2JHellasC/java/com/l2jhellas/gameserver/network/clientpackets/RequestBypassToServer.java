package com.l2jhellas.gameserver.network.clientpackets;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import Extensions.Balancer.Balancer;
import Extensions.Balancer.BalancerEdit;
import Extensions.RankSystem.RPSBypass;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.communitybbs.CommunityBoard;
import com.l2jhellas.gameserver.datatables.xml.AdminData;
import com.l2jhellas.gameserver.handler.AdminCommandHandler;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2ClassMasterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2OlympiadManagerInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.entity.Hero;
import com.l2jhellas.gameserver.model.entity.events.CTF;
import com.l2jhellas.gameserver.model.entity.events.DM;
import com.l2jhellas.gameserver.model.entity.events.TvT;
import com.l2jhellas.gameserver.model.entity.events.engines.L2Event;
import com.l2jhellas.gameserver.model.entity.events.engines.ZodiacMain;
import com.l2jhellas.gameserver.model.entity.olympiad.OlympiadGameManager;
import com.l2jhellas.gameserver.model.entity.olympiad.OlympiadGameTask;
import com.l2jhellas.gameserver.model.entity.olympiad.OlympiadManager;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.Action;

public final class RequestBypassToServer extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestBypassToServer.class.getName());
	private static final String _C__21_REQUESTBYPASSTOSERVER = "[C] 21 RequestBypassToServer";
	
	private String _command;
	private L2Object object;
	
	@Override
	protected void readImpl()
	{
		_command = readS();
	}
	
	@Override
	protected void runImpl()
	{
		if (_command.isEmpty())
			return;
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		if (!FloodProtectors.performAction(getClient(), Action.SERVER_BYPASS))
			return;
		
		if (_command.startsWith("admin_") && activeChar.isGM())
		{
			final String command = _command.split(" ")[0];
			
			final IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);
			
			if (ach == null)
			{
				if (activeChar.isGM())
				{
					activeChar.sendMessage("The command " + command.substring(6) + " doesn't exist.");
					_log.warning(RequestBypassToServer.class.getName() + ": No handler registered for admin command '" + command + "'");
				}
				return;
			}
			
			if (!AdminData.getInstance().hasAccess(command, activeChar.getAccessLevel()))
			{
				activeChar.sendMessage("You don't have the access rights to use this command.");
				_log.warning(RequestBypassToServer.class.getName() + ": " + activeChar.getName() + " tried to use admin command " + command + " without proper Access Level.");
				return;
			}
			
			ThreadPoolManager.getInstance().executeTask(() ->
			{
				try
				{
					ach.useAdminCommand(_command, activeChar);
				}
				catch (final RuntimeException e)
				{
				}
			});
		}
		else if (_command.equals("come_here") && activeChar.isGM())
			comeHere(activeChar);
		else if (_command.startsWith("SecondAnswer"))
		{
			activeChar.sendMessage("You passed our vertification system");
			activeChar.PassedProt = true;
		}
		else if (_command.startsWith("FirstAnswer"))
		{
			activeChar.sendMessage("You didn't pass our vertification system you will be kicked within 2 minutes!");
			activeChar.PassedProt = false;
		}
		else if (_command.startsWith("player_help "))
			playerHelp(activeChar, _command.substring(12));
		else if (ZodiacMain.voting && _command.startsWith("PeloponnesianWar") && !ZodiacMain.HasVoted(activeChar))
		{
			activeChar.sendMessage("You have voted for PeloponnesianWar!");
			ZodiacMain.count[0]++;
			ZodiacMain.showFinalWindow(activeChar);
			ZodiacMain.AddVotedPlayer(activeChar);
		}
		else if (ZodiacMain.voting && _command.startsWith("CaptureThem") && !ZodiacMain.HasVoted(activeChar))
		{
			activeChar.sendMessage("You have voted for CaptureThem!");
			ZodiacMain.count[1]++;
			ZodiacMain.showFinalWindow(activeChar);
			ZodiacMain.AddVotedPlayer(activeChar);
		}
		else if (ZodiacMain.voting && _command.startsWith("CastleWars") && !ZodiacMain.HasVoted(activeChar))
		{
			activeChar.sendMessage("You have voted for CastleWars!");
			ZodiacMain.count[2]++;
			ZodiacMain.showFinalWindow(activeChar);
			ZodiacMain.AddVotedPlayer(activeChar);
		}
		else if (ZodiacMain.voting && _command.startsWith("ProtectTheLdr") && !ZodiacMain.HasVoted(activeChar))
		{
			activeChar.sendMessage("You have voted for ProtectTheLeader!");
			ZodiacMain.count[3]++;
			ZodiacMain.showFinalWindow(activeChar);
			ZodiacMain.AddVotedPlayer(activeChar);
		}
		else if (ZodiacMain.voting && _command.startsWith("TreasureChest") && !ZodiacMain.HasVoted(activeChar))
		{
			activeChar.sendMessage("You have voted for TreasureChest!");
			ZodiacMain.count[4]++;
			ZodiacMain.showFinalWindow(activeChar);
			ZodiacMain.AddVotedPlayer(activeChar);
		}
		else if (ZodiacMain.voting && _command.startsWith("ChaosEvent") && !ZodiacMain.HasVoted(activeChar))
		{
			activeChar.sendMessage("You have voted for Chaos Event!");
			ZodiacMain.count[5]++;
			ZodiacMain.showFinalWindow(activeChar);
			ZodiacMain.AddVotedPlayer(activeChar);
		}
		else if (ZodiacMain.voting && _command.startsWith("Challenge") && !ZodiacMain.HasVoted(activeChar))
		{
			activeChar.sendMessage("You have voted for Challenge Event!");
			ZodiacMain.count[6]++;
			ZodiacMain.showFinalWindow(activeChar);
			ZodiacMain.AddVotedPlayer(activeChar);
		}
		else if (_command.startsWith("npc_"))
		{
			if (!activeChar.validateBypass(_command))
				return;
						
			int endOfId = _command.indexOf('_', 5);
			String id;
			if (endOfId > 0)
				id = _command.substring(4, endOfId);
			else
				id = _command.substring(4);
			
			
			try
			{
				if (id.matches("[0-9]+"))
				{
					if(activeChar.getTargetId() == Integer.parseInt(id))
						object =  activeChar.getTarget();
					else
					    object = L2World.getInstance().findObject(Integer.parseInt(id));
				}
				else
				{
					final L2Object target = activeChar.getTarget();
					if(target != null && target.isNpc() && target.getName() == id)
						object = target;
				}

				if (object != null && object instanceof L2Npc && endOfId > 0 && activeChar.isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false) || ((Config.ALLOW_REMOTE_CLASS_MASTER) && (object instanceof L2ClassMasterInstance)))
					((L2Npc) object).onBypassFeedback(activeChar, _command.substring(endOfId + 1));
				
				if (_command.substring(endOfId + 1).startsWith("event_participate"))
					L2Event.inscribePlayer(activeChar);
				else if (_command.substring(endOfId + 1).startsWith("tvt_player_join "))
				{
					String teamName = _command.substring(endOfId + 1).substring(16);
					
					if (TvT._joining)
						TvT.addPlayer(activeChar, teamName);
					else
						activeChar.sendMessage("The event is already started. You can not join now!");
				}
				else if (_command.substring(endOfId + 1).startsWith("tvt_player_leave"))
				{
					if (TvT._joining)
						TvT.removePlayer(activeChar);
					else
						activeChar.sendMessage("The event is already started. You can not leave now!");
				}
				else if (_command.substring(endOfId + 1).startsWith("dmevent_player_join"))
				{
					if (DM._joining)
						DM.addPlayer(activeChar);
					else
						activeChar.sendMessage("The event is already started. You can not join now!");
				}
				else if (_command.substring(endOfId + 1).startsWith("dmevent_player_leave"))
				{
					if (DM._joining)
						DM.removePlayer(activeChar);
					else
						activeChar.sendMessage("The event is already started. You can not leave now!");
				}
				else if (_command.substring(endOfId + 1).startsWith("ctf_player_join "))
				{
					String teamName = _command.substring(endOfId + 1).substring(16);
					
					if (CTF._joining)
						CTF.addPlayer(activeChar, teamName);
					else
						activeChar.sendMessage("The event is already started. You can not join now!");
				}
				else if (_command.substring(endOfId + 1).startsWith("ctf_player_leave"))
				{
					if (CTF._joining)
						CTF.removePlayer(activeChar);
					else
						activeChar.sendMessage("The event is already started. You can not leave now!");
				}
				
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			catch (NumberFormatException nfe)
			{
			}
		}
		// Draw a Symbol
		else if (_command.equals("menu_select?ask=-16&reply=1"))
		{
			L2Object object = activeChar.getTarget();
			if (object instanceof L2Npc)
				((L2Npc) object).onBypassFeedback(activeChar, _command);
		}
		else if (_command.equals("menu_select?ask=-16&reply=2"))
		{
			L2Object object = activeChar.getTarget();
			if (object instanceof L2Npc)
				((L2Npc) object).onBypassFeedback(activeChar, _command);
		}
		// Navigate through Manor windows
		else if (_command.startsWith("manor_menu_select?"))
		{
			L2Object object = activeChar.getTarget();
			if (object instanceof L2Npc)
				((L2Npc) object).onBypassFeedback(activeChar, _command);
		}
		else if (_command.startsWith("bbs_"))
			CommunityBoard.getInstance().handleCommands(getClient(), _command);
		else if (_command.startsWith("_bbs"))
			CommunityBoard.getInstance().handleCommands(getClient(), _command);
		else if (_command.startsWith("Quest "))
		{
			if (!activeChar.validateBypass(_command))
				return;
			
			String[] str = _command.substring(6).trim().split(" ", 2);
			if (str.length == 1)
				activeChar.processQuestEvent(str[0], "");
			else
				activeChar.processQuestEvent(str[0], str[1]);
		}
		// Balancer: ->
		// -------------------------------------------------------------------------------
		else if (_command.startsWith("bp_balance") && activeChar.isGM())
		{
			String bp = _command.substring(11);
			StringTokenizer st = new StringTokenizer(bp);
			
			if (st.countTokens() != 1)
				return;
			
			int classId = Integer.parseInt(st.nextToken());
			
			Balancer.sendBalanceWindow(classId, activeChar);
		}
		
		else if (_command.startsWith("bp_add") && activeChar.isGM())
		{
			String bp = _command.substring(7);
			StringTokenizer st = new StringTokenizer(bp);
			
			if (st.countTokens() != 3)
				return;
			
			String stat = st.nextToken();
			int classId = Integer.parseInt(st.nextToken()), value = Integer.parseInt(st.nextToken());
			
			BalancerEdit.editStat(stat, classId, value, true);
			
			Balancer.sendBalanceWindow(classId, activeChar);
		}
		
		else if (_command.startsWith("bp_rem") && activeChar.isGM())
		{
			String bp = _command.substring(7);
			StringTokenizer st = new StringTokenizer(bp);
			
			if (st.countTokens() != 3)
				return;
			
			String stat = st.nextToken();
			int classId = Integer.parseInt(st.nextToken()), value = Integer.parseInt(st.nextToken());
			
			BalancerEdit.editStat(stat, classId, value, false);
			
			Balancer.sendBalanceWindow(classId, activeChar);
		}
		// Rank PvP System by Masterio --------------------------------------------
		else if (_command.startsWith("RPS."))
			RPSBypass.executeCommand(activeChar, _command);
		// ------------------------------------------------------------------------
		else if (_command.startsWith("_match"))
		{
			String params = _command.substring(_command.indexOf("?") + 1);
			StringTokenizer st = new StringTokenizer(params, "&");
			int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
			int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
			int heroid = Hero.getInstance().getHeroByClass(heroclass);
			if (heroid > 0)
				Hero.getInstance().showHeroFights(activeChar, heroclass, heroid, heropage);
		}
		else if (_command.startsWith("_diary"))
		{
			String params = _command.substring(_command.indexOf("?") + 1);
			StringTokenizer st = new StringTokenizer(params, "&");
			int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
			int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
			int heroid = Hero.getInstance().getHeroByClass(heroclass);
			if (heroid > 0)
				Hero.getInstance().showHeroDiary(activeChar, heroclass, heroid, heropage);
		}
		else if (_command.startsWith("arenachange")) // change
		{
			
			final boolean isManager = activeChar.getTarget() instanceof L2OlympiadManagerInstance;
			if (!isManager)
			{
				// Without npc, command can be used only in observer mode on arena
				if (!activeChar.inObserverMode() || activeChar.isInOlympiadMode() || activeChar.getOlympiadGameId() < 0)
					return;
			}
			
			if (OlympiadManager.getInstance().isRegisteredInComp(activeChar))
			{
				activeChar.sendPacket(SystemMessageId.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME);
				return;
			}
			
			final int arenaId = Integer.parseInt(_command.substring(12).trim());
			final OlympiadGameTask nextArena = OlympiadGameManager.getInstance().getOlympiadTask(arenaId);
			if (nextArena != null)
			{
				nextArena.getZone().addSpectator(arenaId, activeChar);
				return;
			}
		}
	}
	
	private static void comeHere(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if (obj == null)
			return;
		if (obj instanceof L2Npc)
		{
			L2Npc temp = (L2Npc) obj;
			temp.setTarget(activeChar);
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 0));
		}
	}
	
	private static void playerHelp(L2PcInstance activeChar, String path)
	{
		if (path.indexOf("..") != -1)
			return;
		
		String filename = "data/html/help/" + path;
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		activeChar.sendPacket(html);
	}
	
	@Override
	public String getType()
	{
		return _C__21_REQUESTBYPASSTOSERVER;
	}
}