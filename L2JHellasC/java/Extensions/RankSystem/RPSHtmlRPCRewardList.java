package Extensions.RankSystem;

import java.util.Map;

import Extensions.RankSystem.Util.RPSUtil;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;

public class RPSHtmlRPCRewardList
{
	public static final int LIST_LENGTH = 8;
	public static int PAGE_COUNT = 1; // auto setup on startup.
	
	public static void sendPage(L2PcInstance player, int pageNo)
	{
		NpcHtmlMessage n = new NpcHtmlMessage(0);
		
		n.setHtml(preparePage(player, pageNo));
		
		player.sendPacket(n);
	}
	
	private static String preparePage(L2PcInstance player, int pageNo)
	{
		// get player RPC:
		RPC rpc = RPCTable.getInstance().getRpcByPlayerId(player.getObjectId());
		
		if (rpc == null)
		{
			rpc = new RPC(player.getObjectId()); // create dummy RPC object.
		}
		
		// define container table:
		return "<html><title>RPC Exchange</title><body><center><table border=0 cellspacing=0 cellpadding=0><tr><td><table><tr><td width=100 align=left> <font color=ae9977>RPC Total</font> </td><td align=center><font color=2080D0>" + RPSUtil.preparePrice(rpc.getRpcTotal()) + "</font></td></tr><tr><td width=100 align=left> <font color=ae9977>RPC Current</font> </td><td width=170 align=center><font color=2080D0>" + RPSUtil.preparePrice(rpc.getRpcCurrent()) + "</font></td></tr></table></td></tr><tr><td height=8>&nbsp;</td></tr><tr><td FIXWIDTH=270 HEIGHT=4><img src=\"L2UI.Squaregray\" width=\"270\" height=\"1\"></img></td></tr><tr><td height=8></td></tr><tr><td>" + getRpcRewardList(pageNo) + "</td></tr><tr><td>&nbsp;</td></tr><tr><td HEIGHT=4></td></tr><tr><td>" + getPageChanger(pageNo) + "</td></tr><tr><td FIXWIDTH=270 HEIGHT=4><img src=\"L2UI.Squaregray\" width=\"270\" height=\"1\"></img></td></tr><tr><td align=center><button value=\"Back\" action=\"bypass RPS.PS\"  width=" + Config.BUTTON_W + " height=" + Config.BUTTON_H + " back=\"" + Config.BUTTON_DOWN + "\" fore=\"" + Config.BUTTON_UP + "\"></td></tr></table></center></body></html>";
	}
	
	private static String getRpcRewardList(final int pageNo)
	{
		int i = 0;
		boolean inList = false;
		
		String list = "";
		
		Map<Integer, RPCReward> rpcRewardList = RPCRewardTable.getInstance().getRpcRewardList();
		
		for (Map.Entry<Integer, RPCReward> e : rpcRewardList.entrySet())
		{
			i++;
			
			if (i > (LIST_LENGTH * (pageNo - 1)) && i <= (LIST_LENGTH * (pageNo)))
			{
				RPCReward rpcr = e.getValue();
				
				if (rpcr == null)
					break;
				
				String itemName = ItemTable.getInstance().getTemplate(rpcr.getItemId()).getItemName();
				
				list += getRpcRewardListItem(rpcr.getId(), itemName, rpcr.getItemAmount(), rpcr.getRpc(), pageNo);
				
				inList = true;
			}
			else
			{
				if (inList)
					break;
			}
		}
		
		if (list.equals(""))
		{
			return "<table cellspacing=0 cellpadding=0><tr><td height=270>No reward defined yet.</td></tr></table>";
		}
		
		return "<table cellspacing=0 cellpadding=0><tr><td height=270><table>" + list + "</table></td></tr></table>";
	}
	
	private static String getRpcRewardListItem(int itemId, String itemName, long itemCount, long rpcCost, int pageNo)
	{
		if (Config.RPC_EXCHANGE_CONFIRM_BOX_ENABLED)
		{
			return "<tr><td height=32><table cellspacing=0 cellpadding=0><tr><td width=270 height=16><font color=FF8000>" + itemName + "</font></td></tr><tr><td><table width=270 cellspacing=0 cellpadding=0><tr><td width=150 height=16><font color=ae9977>Count:</font> <font color=808080>" + RPSUtil.preparePrice(itemCount) + "</font></td><td width=80><font color=ae9977>RPC:</font> <font color=2080D0>" + RPSUtil.preparePrice(rpcCost) + "</font></td><td align=right><button value=\"Get\" action=\"bypass RPS.RPCRewardConfirm:" + itemId + "," + pageNo + "\" width=40 height=16 back=\"" + Config.BUTTON_DOWN + "\" fore=\"" + Config.BUTTON_UP + "\"></td></tr></table></td></tr></table></td></tr><tr><td FIXWIDTH=270 HEIGHT=4><img src=\"L2UI.Squaregray\" width=\"270\" height=\"1\"></img></td></tr>";
		}
		
		return "<tr><td height=32><table cellspacing=0 cellpadding=0><tr><td width=270 height=16><font color=FF8000>" + itemName + "</font></td></tr><tr><td><table width=270 cellspacing=0 cellpadding=0><tr><td width=150 height=16><font color=ae9977>Count:</font> <font color=808080>" + RPSUtil.preparePrice(itemCount) + "</font></td><td width=80><font color=ae9977>RPC:</font> <font color=2080D0>" + RPSUtil.preparePrice(rpcCost) + "</font></td><td align=right><button value=\"Get\" action=\"bypass RPS.RPCReward:" + itemId + "," + pageNo + "\" width=40 height=16 back=\"" + Config.BUTTON_DOWN + "\" fore=\"" + Config.BUTTON_UP + "\"></td></tr></table></td></tr></table></td></tr><tr><td FIXWIDTH=270 HEIGHT=4><img src=\"L2UI.Squaregray\" width=\"270\" height=\"1\"></img></td></tr>";
	}
	
	private static String getPageChanger(int pageNo)
	{
		String backButton = "&nbsp;";
		String nextButton = "&nbsp;";
		
		if (pageNo > 1)
			backButton = "<button value=\"<<\" action=\"bypass RPS.RPC:" + (pageNo - 1) + "\" width=40 height=16 back=\"" + Config.BUTTON_DOWN + "\" fore=\"" + Config.BUTTON_UP + "\">";
		
		if (pageNo < PAGE_COUNT)
			nextButton = "<button value=\">>\" action=\"bypass RPS.RPC:" + (pageNo + 1) + "\" width=40 height=16 back=\"" + Config.BUTTON_DOWN + "\" fore=\"" + Config.BUTTON_UP + "\">";
		
		return "<table><tr><td width=90 align=right>" + backButton + "</td><td width=90 align=center>" + pageNo + " / " + PAGE_COUNT + "</td><td width=90 align=left>" + nextButton + "</td></tr></table>";
	}
	
	public static void getConfirmPage(L2PcInstance player, int prevPageNo, int rewardId)
	{
		String yesButton = "<button value=\"Yes\" action=\"bypass RPS.RPCReward:" + rewardId + "," + prevPageNo + "\"  width=" + Config.BUTTON_W + " height=" + Config.BUTTON_H + " back=\"" + Config.BUTTON_DOWN + "\" fore=\"" + Config.BUTTON_UP + "\">";
		String noButton = "<button value=\"No\" action=\"bypass RPS.RPC:" + prevPageNo + "\"  width=" + Config.BUTTON_W + " height=" + Config.BUTTON_H + " back=\"" + Config.BUTTON_DOWN + "\" fore=\"" + Config.BUTTON_UP + "\">";
		
		String page = "<html><title>RPC Exchange - Confirm</title><body><center><table><tr><td height=100>&nbsp;</td></tr><tr><td height=100 align=center>Are you sure you want to do that?</td></tr><tr><td><table><tr><td width=130 align=center>" + yesButton + "</td><td width=130 align=center>" + noButton + "</td></tr></table></td></tr></table></center></body></html>";
		
		NpcHtmlMessage n = new NpcHtmlMessage(0);
		
		n.setHtml(page);
		
		player.sendPacket(n);
	}
	
	public static void init(int rewardListSize)
	{
		if (rewardListSize % RPSHtmlRPCRewardList.LIST_LENGTH == 0)
			RPSHtmlRPCRewardList.PAGE_COUNT = (int) Math.floor(rewardListSize / RPSHtmlRPCRewardList.LIST_LENGTH);
		else
			RPSHtmlRPCRewardList.PAGE_COUNT = (int) Math.floor(rewardListSize / RPSHtmlRPCRewardList.LIST_LENGTH) + 1;
	}
	
}
