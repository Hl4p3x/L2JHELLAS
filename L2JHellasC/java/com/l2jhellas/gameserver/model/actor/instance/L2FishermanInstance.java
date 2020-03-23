package com.l2jhellas.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.controllers.TradeController;
import com.l2jhellas.gameserver.datatables.xml.SkillTreeData;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2SkillLearn;
import com.l2jhellas.gameserver.model.L2TradeList;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.AcquireSkillList;
import com.l2jhellas.gameserver.network.serverpackets.BuyList;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.network.serverpackets.SellList;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
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
	
	public void showSkillList(L2PcInstance player)
	{
		L2SkillLearn[] skills = SkillTreeData.getInstance().getAvailableSkills(player);
		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.skillType.Fishing);
		
		int counts = 0;
		
		for (L2SkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			
			if (sk == null)
				continue;
			
			counts++;
			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getSpCost(), 1);
		}
		
		if (counts == 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			int minlevel = SkillTreeData.getInstance().getMinLevelForNewSkill(player);
			
			if (minlevel > 0)
			{
				// No more skills to learn, come back when you level.
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1);
				sm.addNumber(minlevel);
				player.sendPacket(sm);
			}
			else
			{
				StringBuilder sb = new StringBuilder();
				sb.append("<html><head><body>");
				sb.append("You've learned all skills.<br>");
				sb.append("</body></html>");
				html.setHtml(sb.toString());
				player.sendPacket(html);
			}
		}
		else
		{
			player.sendPacket(asl);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}