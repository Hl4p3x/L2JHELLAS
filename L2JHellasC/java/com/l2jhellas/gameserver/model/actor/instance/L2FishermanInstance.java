package com.l2jhellas.gameserver.model.actor.instance;

import java.util.List;
import java.util.StringTokenizer;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.controllers.TradeController;
import com.l2jhellas.gameserver.datatables.xml.SkillTreeData;
import com.l2jhellas.gameserver.holder.FishingSkillNode;
import com.l2jhellas.gameserver.model.L2TradeList;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.AcquireSkillDone;
import com.l2jhellas.gameserver.network.serverpackets.AcquireSkillList;
import com.l2jhellas.gameserver.network.serverpackets.BuyList;
import com.l2jhellas.gameserver.network.serverpackets.SellList;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2FishermanInstance extends L2NpcInstance
{
	
	public L2FishermanInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		
		if (val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		
		return "data/html/fisherman/" + pom + ".htm";
	}
	
	private void showBuyWindow(L2PcInstance player, int val)
	{
		double taxRate = 0;
		if (getIsInTown())
			taxRate = getCastle().getTaxRate();
		player.tempInvetoryDisable();
		if (Config.DEBUG)
			_log.fine("Showing buylist");
		L2TradeList list = TradeController.getInstance().getBuyList(val);
		
		if (list != null && list.getNpcId().equals(String.valueOf(getNpcId())))
		{
			BuyList bl = new BuyList(list, player.getAdena(), taxRate);
			player.sendPacket(bl);
		}
		else
		{
			_log.warning(L2FishermanInstance.class.getName() + ": possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
			_log.warning(L2FishermanInstance.class.getName() + ": buylist id:" + val);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private static void showSellWindow(L2PcInstance player)
	{
		if (Config.DEBUG)
			_log.fine("Showing selllist");
		
		player.sendPacket(new SellList(player));
		
		if (Config.DEBUG)
			_log.fine("Showing sell window");
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("FishSkillList"))
		{
			player.setSkillLearningClassId(player.getClassId());
			showSkillList(player);
		}
		
		StringTokenizer st = new StringTokenizer(command, " ");
		String command2 = st.nextToken();
		
		if (command2.equalsIgnoreCase("Buy"))
		{
			if (st.countTokens() < 1)
				return;
			int val = Integer.parseInt(st.nextToken());
			showBuyWindow(player, val);
		}
		else if (command2.equalsIgnoreCase("Sell"))
		{
			showSellWindow(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	
	public static void showSkillList(L2PcInstance player)
	{
		final List<FishingSkillNode> skills = SkillTreeData.getInstance().getFishingSkillsFor(player);
		if (skills.isEmpty())
		{
			final int minlevel = SkillTreeData.getInstance().getRequiredLevelForNextFishingSkill(player);
			if (minlevel > 0)
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1).addNumber(minlevel));
			else
				player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
			
			player.sendPacket(AcquireSkillDone.STATIC_PACKET);
		}
		else
			player.sendPacket(new AcquireSkillList(AcquireSkillList.skillType.Fishing, skills));
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}