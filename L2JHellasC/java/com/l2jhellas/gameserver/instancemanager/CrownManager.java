package com.l2jhellas.gameserver.instancemanager;

import java.util.logging.Logger;

import com.l2jhellas.gameserver.enums.items.CrownList;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2ClanMember;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.entity.Castle;

public class CrownManager
{
	protected static final Logger _log = Logger.getLogger(CrownManager.class.getName());
	private static CrownManager _instance;
	
	public static final CrownManager getInstance()
	{
		if (_instance == null)
			_instance = new CrownManager();
		return _instance;
	}
	
	public CrownManager()
	{
		_log.info(CrownManager.class.getSimpleName() + ": initialized.");
	}
	
	public void checkCrowns(L2Clan clan)
	{
		if (clan == null)
			return;
		
		for (L2ClanMember member : clan.getMembers())
		{
			if (member != null && member.isOnline() && member.getPlayerInstance() != null)
				checkCrowns(member.getPlayerInstance());
		}
	}
	
	public void checkCrowns(L2PcInstance activeChar)
	{
		if (activeChar == null)
			return;
		
		boolean isLeader = false;
		int crownId = -1;
		
		L2Clan activeCharClan = activeChar.getClan();
		L2ClanMember activeCharClanLeader;
		if (activeCharClan != null)
			activeCharClanLeader = activeChar.getClan().getLeader();
		else
			activeCharClanLeader = null;
		if (activeCharClan != null)
		{
			Castle activeCharCastle = CastleManager.getInstance().getCastleByOwner(activeCharClan);
			
			if (activeCharCastle != null)		
				crownId = CrownList.findCrownByCastle(activeCharCastle.getCastleId());
			
			if (activeCharClanLeader != null && activeCharClanLeader.getObjectId() == activeChar.getObjectId())
				isLeader = true;
		}
		
		if (crownId > 0)
		{
			if (isLeader && activeChar.getInventory().getItemByItemId(6841) == null)
			{
				activeChar.getInventory().addItem("Crown", 6841, 1, activeChar, null);
				activeChar.getInventory().updateDatabase();
			}
			
			if (activeChar.getInventory().getItemByItemId(crownId) == null)
			{
				activeChar.getInventory().addItem("Crown", crownId, 1, activeChar, null);
				activeChar.getInventory().updateDatabase();
			}
		}
		
		boolean alreadyFoundCirclet = false;
		boolean alreadyFoundCrown = false;
		for (L2ItemInstance item : activeChar.getInventory().getItems())
		{
			if(CrownList.findCrownByItemId(item.getItemId()) > 0)
			{
				if (crownId > 0)
				{
					if (item.getItemId() == crownId)
					{
						if (!alreadyFoundCirclet)
						{
							alreadyFoundCirclet = true;
							continue;
						}
					}
					else if (item.getItemId() == 6841 && isLeader)
					{
						if (!alreadyFoundCrown)
						{
							alreadyFoundCrown = true;
							continue;
						}
					}
				}
				
				activeChar.destroyItem("Removing Crown", item, activeChar, true);
				activeChar.getInventory().updateDatabase();
			}
		}
	}
}