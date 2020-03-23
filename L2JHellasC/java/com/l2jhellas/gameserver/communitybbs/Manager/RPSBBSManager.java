package com.l2jhellas.gameserver.communitybbs.Manager;

import java.util.logging.Level;
import java.util.logging.Logger;

import Extensions.RankSystem.RPSHtmlCommunityBoard;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class RPSBBSManager extends BaseBBSManager
{
	public static final Logger log = Logger.getLogger(RPSBBSManager.class.getSimpleName());
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("_bbsrps:"))
		{
			int page = 0;
			try
			{
				page = Integer.parseInt(command.split(":", 2)[1].trim());
			}
			catch (Exception e)
			{
				log.log(Level.WARNING, e.getMessage());
				page = 0;
			}
			
			separateAndSend(RPSHtmlCommunityBoard.getPage(activeChar, page), activeChar);
		}
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		//
	}
	
	private static RPSBBSManager _instance = new RPSBBSManager();
	
	public static RPSBBSManager getInstance()
	{
		return _instance;
	}
}
