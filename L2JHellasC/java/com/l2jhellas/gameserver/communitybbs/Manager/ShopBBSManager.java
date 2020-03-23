package com.l2jhellas.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;

import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ShowBoard;

public class ShopBBSManager extends BaseBBSManager
{
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equals("_bbsshop"))
		{
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/shop.htm");
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith("_bbsshop;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
			
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return;
		}
	}
	
	public static ShopBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ShopBBSManager _instance = new ShopBBSManager();
	}
}