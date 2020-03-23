package com.l2jhellas.gameserver.handlers.voicedcommandhandlers;

import java.io.File;
import java.util.logging.Logger;

import com.PackRoot;
import com.l2jhellas.gameserver.handler.IVoicedCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.events.CaptureThem;
import com.l2jhellas.gameserver.model.entity.events.CastleWars;
import com.l2jhellas.gameserver.model.entity.events.ChaosEvent;
import com.l2jhellas.gameserver.model.entity.events.PeloponnesianWar;
import com.l2jhellas.gameserver.model.entity.events.ProtectTheLdr;
import com.l2jhellas.gameserver.model.entity.events.TreasureChest;
import com.l2jhellas.gameserver.model.entity.events.engines.ZodiacMain;
import com.l2jhellas.gameserver.model.entity.olympiad.OlympiadManager;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;

public class ZodiacRegistrationCmd implements IVoicedCommandHandler
{
	private static Logger _log = Logger.getLogger(ZodiacRegistrationCmd.class.getName());
	private static final String[] VOICED_COMMANDS =
	{
		"join",
		"leave"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		final String Ip = activeChar.getClient().getConnection().getInetAddress().getHostAddress();
		
		if (OlympiadManager.getInstance().isRegisteredInComp(activeChar) || activeChar.isInOlympiadMode() || activeChar.getOlympiadGameId() > 0)
		{
			activeChar.sendMessage("You can't register while you are in olympiad!");
			return false;
		}
		else if (command.startsWith(VOICED_COMMANDS[0]) && ZodiacMain.isEligible(activeChar, Ip))
		{
			activeChar.isinZodiac = true;
			activeChar.sendMessage("You are now registered!");
			ZodiacMain.Ips.add(Ip);
			if (CastleWars.CastleWarsRunning)
			{
				String Castle_Path = "data/html/zodiac/CastleTutorial.htm";
				File mainText = new File(PackRoot.DATAPACK_ROOT, Castle_Path);
				if (!mainText.exists())
				{
					_log.warning(ZodiacRegistrationCmd.class.getName() + ": cant find " + PackRoot.DATAPACK_ROOT + Castle_Path + " check your files.");
				}
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(Castle_Path);
				activeChar.sendPacket(html);
			}
			else if (CaptureThem.CaptureThemRunning)
			{
				String Capture_Path = "data/html/zodiac/Tutorial.htm";
				File mainText = new File(PackRoot.DATAPACK_ROOT, Capture_Path);
				if (!mainText.exists())
				{
					_log.warning(ZodiacRegistrationCmd.class.getName() + ": cant find " + PackRoot.DATAPACK_ROOT + Capture_Path + " check your files.");
				}
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(Capture_Path);
				activeChar.sendPacket(html);
			}
			else if (PeloponnesianWar.PeloRunning)
			{
				String Pelo_Path = "data/html/zodiac/TutorialPelo.htm";
				File mainText = new File(PackRoot.DATAPACK_ROOT, Pelo_Path);
				if (!mainText.exists())
				{
					_log.warning(ZodiacRegistrationCmd.class.getName() + ": cant find " + PackRoot.DATAPACK_ROOT + Pelo_Path + " check your files.");
				}
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(Pelo_Path);
				activeChar.sendPacket(html);
			}
			else if (ProtectTheLdr.ProtectisRunning)
			{
				String Protect_Path = "data/html/zodiac/ProtectTuto.htm";
				File mainText = new File(PackRoot.DATAPACK_ROOT, Protect_Path);
				if (!mainText.exists())
				{
					_log.warning(ZodiacRegistrationCmd.class.getName() + ": cant find " + PackRoot.DATAPACK_ROOT + Protect_Path + " check your files.");
				}
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(Protect_Path);
				activeChar.sendPacket(html);
			}
			else if (TreasureChest.TreasureRunning)
			{
				String Capture_Path = "data/html/zodiac/Treasure.htm";
				File mainText = new File(PackRoot.DATAPACK_ROOT, Capture_Path);
				if (!mainText.exists())
				{
					_log.warning(ZodiacRegistrationCmd.class.getName() + ": cant find " + PackRoot.DATAPACK_ROOT + Capture_Path + " check your files.");
				}
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(Capture_Path);
				activeChar.sendPacket(html);
			}
			else if (ChaosEvent._isChaosActive)
			{
				ChaosEvent.registerToChaos(activeChar);
				return true;
			}
		}
		if ((command.startsWith(VOICED_COMMANDS[1]) && activeChar.isinZodiac))
		{
			if (!ZodiacMain.ZodiacRegisterActive)
			{
				activeChar.sendMessage("You can't unregister while an event is running");
				return true;
			}
			activeChar.isinZodiac = false;
			activeChar.sendMessage("You are now unregistered!");
			ZodiacMain.Ips.remove(Ip);
			if (ChaosEvent._isChaosActive)
				ChaosEvent.removeFromChaos(activeChar);
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}