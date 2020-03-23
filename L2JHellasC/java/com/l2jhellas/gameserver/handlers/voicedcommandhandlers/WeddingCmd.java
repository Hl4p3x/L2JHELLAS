package com.l2jhellas.gameserver.handlers.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.controllers.GameTimeController;
import com.l2jhellas.gameserver.handler.IVoicedCommandHandler;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.CoupleManager;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ConfirmDlg;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.SetupGauge;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.util.Broadcast;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class WeddingCmd implements IVoicedCommandHandler
{
	protected static final Logger _log = Logger.getLogger(WeddingCmd.class.getName());
	private static String[] VOICED_COMMANDS =
	{
		"divorce",
		"engage",
		"gotolove"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith(VOICED_COMMANDS[1]))
			return Engage(activeChar);
		else if (command.startsWith(VOICED_COMMANDS[0]))
			return Divorce(activeChar);
		else if (command.startsWith(VOICED_COMMANDS[2]))
			return GoToLove(activeChar);
		return false;
	}
	
	public boolean Divorce(L2PcInstance activeChar)
	{
		if (activeChar.getPartnerId() == 0)
			return false;
		
		int _partnerId = activeChar.getPartnerId();
		int _coupleId = activeChar.getCoupleId();
		int AdenaAmount = 0;
		
		if (activeChar.isMarried())
		{
			activeChar.sendMessage("You are now divorced.");
			AdenaAmount = (activeChar.getAdena() / 100) * Config.MOD_WEDDING_DIVORCE_COSTS;
			activeChar.getInventory().reduceAdena("Wedding", AdenaAmount, activeChar, null);
			activeChar.getInventory().destroyItemByItemId("Divorced", 9140, 1, activeChar, activeChar);
			ItemList il = new ItemList(activeChar.getClient().getActiveChar(), true);
			activeChar.sendPacket(il);
		}
		else
			activeChar.sendMessage("You have broken up as a couple.");
		
		final L2PcInstance partner = L2World.getInstance().getPlayer(_partnerId);
		
		if (partner != null)
		{
			partner.setPartnerId(0);
			if (partner.isMarried())
			{
				partner.sendMessage("Your spouse has decided to divorce you.");
				partner.getInventory().destroyItemByItemId("Divorced", 9140, 1, partner, partner);
				ItemList il = new ItemList(partner.getClient().getActiveChar(), true);
				partner.sendPacket(il);
			}
			else
				partner.sendMessage("Your fiance has decided to break the engagement with you.");
			// give adena
			if (AdenaAmount > 0)
				partner.addAdena("WEDDING", AdenaAmount, null, false);
		}
		
		CoupleManager.getInstance().deleteCouple(_coupleId);
		return true;
	}
	
	public boolean Engage(L2PcInstance activeChar)
	{
		// check target
		if (activeChar.getTarget() == null)
		{
			activeChar.sendMessage("You have no one targeted.");
			return false;
		}
		
		// check if target is a l2pcinstance
		if (!(activeChar.getTarget() instanceof L2PcInstance))
		{
			activeChar.sendMessage("You can only ask another player to engage you.");
			return false;
		}
		// check if player is already engaged
		if (activeChar.getPartnerId() != 0)
		{
			activeChar.sendMessage("You are already engaged.");
			if (Config.MOD_WEDDING_PUNISH_INFIDELITY)
			{
				activeChar.startAbnormalEffect((short) 0x2000); // give player a
				// Big Head
				// lets recycle the sevensigns debuffs
				int skillId;
				
				int skillLevel = 1;
				
				if (activeChar.getLevel() > 40)
					skillLevel = 2;
				
				if (activeChar.isMageClass())
					skillId = 4361;
				else
					skillId = 4362;
				
				L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
				
				if (activeChar.getFirstEffect(skill) == null)
				{
					skill.getEffects(activeChar, activeChar);
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
					sm.addSkillName(skillId);
					activeChar.sendPacket(sm);
				}
			}
			return false;
		}
		
		L2PcInstance ptarget = (L2PcInstance) activeChar.getTarget();
		
		// check if player target himself
		if (ptarget.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendMessage("Is there something wrong with you, are you trying to go out with youself?");
			return false;
		}
		
		if (ptarget.isMarried())
		{
			activeChar.sendMessage("Player already married.");
			return false;
		}
		
		if (ptarget.isEngageRequest())
		{
			activeChar.sendMessage("Player already asked by someone else.");
			return false;
		}
		
		if (ptarget.getPartnerId() != 0)
		{
			activeChar.sendMessage("Player already engaged with someone else.");
			return false;
		}
		
		if (ptarget.getAppearance().getSex() == activeChar.getAppearance().getSex() && !Config.MOD_WEDDING_SAMESEX)
		{
			activeChar.sendMessage("Gay marriage is not allowed on this server!");
			return false;
		}
		
		// check if target has player on friendlist
		boolean FoundOnFriendList = false;
		int objectId = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id=?"))
		{
			
			statement.setInt(1, ptarget.getObjectId());
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					objectId = rset.getInt("friend_id");
					if (objectId == activeChar.getObjectId())
						FoundOnFriendList = true;
				}
			}
		}
		catch (SQLException e)
		{
			_log.warning(WeddingCmd.class.getName() + ": could not read friend data:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		if (!FoundOnFriendList)
		{
			activeChar.sendMessage("The player you want to ask is not on your friends list, you must first be on each others friends list before you choose to engage.");
			return false;
		}
		
		ptarget.setEngageRequest(true, activeChar.getObjectId());
		// ptarget.sendMessage("Player "+activeChar.getName()+" wants to engage with you.");
		activeChar.awaitingAnswer = true;
		ConfirmDlg dlg = new ConfirmDlg(614);
		dlg.addString(activeChar.getName() + " asking you to engage. Do you want to start a new relationship?");
		ptarget.sendPacket(dlg);
		dlg = null;
		ptarget = null;
		return true;
	}
	
	public boolean GoToLove(L2PcInstance activeChar)
	{
		if (!activeChar.isMarried())
		{
			activeChar.sendMessage("You're not married.");
			return false;
		}
		
		if (activeChar.getPartnerId() == 0)
		{
			activeChar.sendMessage("Couldn't find your fiance in the Database - Inform a Gamemaster.");
			_log.warning(WeddingCmd.class.getName() + ": Married but couldn't find parter for " + activeChar.getName());
			return false;
		}
		
		final L2PcInstance partner = L2World.getInstance().getPlayer(activeChar.getPartnerId());
		if (partner == null)
		{
			activeChar.sendMessage("Your partner is not online.");
			return false;
		}
		else if (partner.isInJail())
		{
			activeChar.sendMessage("Your partner is in Jail.");
			return false;
		}
		else if (partner.isInOlympiadMode())
		{
			activeChar.sendMessage("Your partner is in the Olympiad now.");
			return false;
		}
		else if (partner.atEvent)
		{
			activeChar.sendMessage("Your partner is in an event.");
			return false;
		}
		else if (partner.isInDuel())
		{
			activeChar.sendMessage("Your partner is in a duel.");
			return false;
		}
		else if (partner.isFestivalParticipant())
		{
			activeChar.sendMessage("Your partner is in a festival.");
			return false;
		}
		else if (partner.isInParty() && partner.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage("Your partner is in dimensional rift.");
			return false;
		}
		else if (partner.inObserverMode())
		{
			activeChar.sendMessage("Your partner is in the observation.");
			return false;
		}
		else if (partner.getClan() != null && CastleManager.getInstance().getCastleByOwner(partner.getClan()) != null && CastleManager.getInstance().getCastleByOwner(partner.getClan()).getSiege().getIsInProgress())
		{
			activeChar.sendMessage("Your partner is in siege, you can't go to your partner.");
			return false;
		}
		
		else if (activeChar.isInJail())
		{
			activeChar.sendMessage("You are in Jail!");
			return false;
		}
		else if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("You are in the Olympiad now.");
			return false;
		}
		else if (activeChar.atEvent)
		{
			activeChar.sendMessage("You are in an event.");
			return false;
		}
		else if (activeChar.isInDuel())
		{
			activeChar.sendMessage("You are in a duel!");
			return false;
		}
		else if (activeChar.inObserverMode())
		{
			activeChar.sendMessage("You are in the observation.");
			return false;
		}
		else if (activeChar.getClan() != null && CastleManager.getInstance().getCastleByOwner(activeChar.getClan()) != null && CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).getSiege().getIsInProgress())
		{
			activeChar.sendMessage("You are in siege, you can't go to your partner.");
			return false;
		}
		else if (activeChar.isFestivalParticipant())
		{
			activeChar.sendMessage("You are in a festival.");
			return false;
		}
		else if (activeChar.isInParty() && activeChar.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage("You are in the dimensional rift.");
			return false;
		}
		if (activeChar.isCursedWeaponEquiped())
		{
			activeChar.sendMessage("You cannot do this while wielding a cursed weapon.");
			return false;
		}
		// Thanks nbd
		if (activeChar._inEventTvT)
		{
			activeChar.sendMessage("You may not use an escape skill in a Event.");
			return false;
		}
		int teleportTimer = Config.MOD_WEDDING_TELEPORT_DURATION * 1000;
		
		activeChar.sendMessage("After " + teleportTimer / 60000 + " min. you will be teleported to your fiance.");
		activeChar.getInventory().reduceAdena("Wedding", Config.MOD_WEDDING_TELEPORT_PRICE, activeChar, null);
		
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		// SoE Animation section
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills();
		
		MagicSkillUse msk = new MagicSkillUse(activeChar, 1050, 1, teleportTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 810000);
		SetupGauge sg = new SetupGauge(0, teleportTimer);
		activeChar.sendPacket(sg);
		// End SoE Animation section
		
		EscapeFinalizer ef = new EscapeFinalizer(activeChar, partner.getX(), partner.getY(), partner.getZ(), partner.isIn7sDungeon());
		// continue execution later
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, teleportTimer));
		activeChar.setSkillCastEndTime(10 + GameTimeController.getInstance().getGameTicks() + teleportTimer / GameTimeController.MILLIS_IN_TICK);
		
		return true;
	}
	
	static class EscapeFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final int _partnerx;
		private final int _partnery;
		private final int _partnerz;
		private final boolean _to7sDungeon;
		
		EscapeFinalizer(L2PcInstance activeChar, int x, int y, int z, boolean to7sDungeon)
		{
			_activeChar = activeChar;
			_partnerx = x;
			_partnery = y;
			_partnerz = z;
			_to7sDungeon = to7sDungeon;
		}
		
		@Override
		public void run()
		{
			if (_activeChar.isDead())
				return;
			
			_activeChar.setIsIn7sDungeon(_to7sDungeon);
			
			_activeChar.enableAllSkills();
			
			try
			{
				_activeChar.teleToLocation(_partnerx, _partnery, _partnerz);
			}
			catch (Throwable e)
			{
				_log.warning(WeddingCmd.class.getName() + "error RUN");
			}
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}