package com.l2jhellas.gameserver.model.actor.instance;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.instancemanager.BufferManager;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.MathUtil;
import com.l2jhellas.util.StringUtil;

public class L2SchemeBufferInstance extends L2Npc
{
	private static final int PAGE_LIMIT = 6;
	
	public L2SchemeBufferInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

    private void ShowMain(L2PcInstance player)
    {
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(getHtmlPath(getNpcId(), 0));
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
    }
    
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		filename = val == 0 ? "" + npcId : npcId + "-" + val;		
		return "data/html/mods/buffer/" + filename + ".htm";
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance  player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String currentCommand = st.nextToken();

		if(player.isAlikeDead() || player.isSitting() || player.isInCombat())
		{
			player.sendMessage("You cannot use the buffer now due to inappropriate conditions");
			return;
		}
				
		if (currentCommand.startsWith("menu"))
			ShowMain(player);
		else if (currentCommand.startsWith("autosupport"))
		     autoBuff(player);
		else if (currentCommand.startsWith("support"))
			showGiveBuffsWindow(player);
		else if (currentCommand.startsWith("noble"))
		    blessMe(player);
		else if (currentCommand.startsWith("editschemes"))
			showEditSchemeWindow(player, st.nextToken(), st.nextToken(), Integer.parseInt(st.nextToken()));
		else if (currentCommand.startsWith("givebuffs"))
		{
			final String schemeName = st.nextToken();
			final int cost = Integer.parseInt(st.nextToken());
			
			L2Character target = player;
			
			if (st.hasMoreTokens())
			{
				final String PetTarge = st.nextToken();
				if (PetTarge != null && PetTarge.equalsIgnoreCase("pet"))
					target = player.getPet();
			}

			if (target == null)
				player.sendMessage("In order to use this action you must have a pet.");
			else
			{
				List<Integer> buffs = BufferManager.getInstance().getScheme(player.getObjectId(), schemeName);
				if (buffs == null || buffs.isEmpty())
					player.sendMessage("Your buff list is empty,in order to use this action you must fill your buff list.");
				else if (cost == 0|| player.reduceAdena("NPC Buffer", cost, this, true)) 
				{
					for (int skillId : buffs)
						SkillTable.getInstance().getInfo(skillId,SkillTable.getInstance().getMaxLevel(skillId, 0)).getEffects(target, target);
				}
			}
		}
		else if (currentCommand.startsWith("petcleanup"))
		{			
			final L2Summon summon = player.getPet();
			if (summon != null)
				summon.stopAllEffects();
			else
				player.sendMessage("In order to use this action you must have a pet.");
			
			ShowMain(player);
		}
		else if (currentCommand.startsWith("cleanup"))
		{
			player.stopAllEffects();
			ShowMain(player);
		}
		else if (currentCommand.startsWith("heal"))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
			
			final L2Summon summon = player.getPet();
			if (summon != null)
				summon.setCurrentHpMp(summon.getMaxHp(), summon.getMaxMp());

			ShowMain(player);
		}
		else if (currentCommand.startsWith("skill"))
		{
			final String groupType = st.nextToken();
			final String schemeName = st.nextToken();
			
			final int skillId = Integer.parseInt(st.nextToken());
			final int page = Integer.parseInt(st.nextToken());
			
			final List<Integer> skills = BufferManager.getInstance().getScheme(player.getObjectId(), schemeName);
			
			if (currentCommand.startsWith("skillselect") && !schemeName.equalsIgnoreCase("none"))
			{
				if (skills.size() < player.getMaxBuffCount())
					skills.add(skillId);
				else
					player.sendMessage("You have reached the maximum amount of buffs.");
			}
			else if (currentCommand.startsWith("skillunselect"))
				skills.remove(Integer.valueOf(skillId));
			
			showEditSchemeWindow(player, groupType, schemeName, page);
		}
		else if (currentCommand.startsWith("createscheme"))
		{
			try
			{
				final String schemeName = st.nextToken();
				if (schemeName.length() > 14)
				{
					player.sendMessage("Scheme's name must be between 1-14 characters.");
					return;
				}
				
				final Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
				if (schemes != null)
				{
					if (schemes.size() == Config.BUFFER_MAX_SCHEMES)
					{
						player.sendMessage("Maximum schemes amount is already reached.");
						return;
					}
					
					if (schemes.containsKey(schemeName))
					{
						player.sendMessage("The scheme name already exists.");
						return;
					}
				}
				
				BufferManager.getInstance().setScheme(player.getObjectId(), schemeName.trim(), new ArrayList<Integer>());
				showGiveBuffsWindow(player);
			}
			catch (Exception e)
			{
				player.sendMessage("Scheme's name must be between 1-14 characters.");
			}
		}
		else if (currentCommand.startsWith("deletescheme"))
		{
			try
			{
				final String schemeName = st.nextToken();
				final Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
				
				if (schemes != null && schemes.containsKey(schemeName))
					schemes.remove(schemeName);
			}
			catch (Exception e)
			{
				player.sendMessage("This scheme name is invalid.");
			}
			showGiveBuffsWindow(player);
		}
		
		super.onBypassFeedback(player, command);
	}

	private void showGiveBuffsWindow(L2PcInstance player)
	{
		final StringBuilder sb = new StringBuilder(200);
		
		final Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
		if (schemes == null || schemes.isEmpty())
			sb.append("<font color=\"LEVEL\">You haven't defined any scheme.</font>");
		else
		{
			for (Map.Entry<String, ArrayList<Integer>> scheme : schemes.entrySet())
			{
				final int cost = getCost(scheme.getValue());
				StringUtil.append(sb, "<font color=\"LEVEL\">", scheme.getKey(), " [", scheme.getValue().size(), " / ", player.getMaxBuffCount(), "]", ((cost > 0) ? " - cost: " + NumberFormat.getInstance(Locale.ENGLISH).format(cost) : ""), "</font><br1>");
				StringUtil.append(sb, "<a action=\"bypass npc_%objectId%_givebuffs ", scheme.getKey(), " ", cost, "\">Use on Me</a>&nbsp;|&nbsp;");
				StringUtil.append(sb, "<a action=\"bypass npc_%objectId%_givebuffs ", scheme.getKey(), " ", cost, " pet\">Use on Pet</a>&nbsp;|&nbsp;");
				StringUtil.append(sb, "<a action=\"bypass npc_%objectId%_editschemes Buffs ", scheme.getKey(), " 1\">Edit</a>&nbsp;|&nbsp;");
				StringUtil.append(sb, "<a action=\"bypass npc_%objectId%_deletescheme ", scheme.getKey(), "\">Delete</a><br>");
			}
		}	
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(getHtmlPath(getNpcId(), 1));
		html.replace("%schemes%", sb.toString());
		html.replace("%max_schemes%", Config.BUFFER_MAX_SCHEMES);
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
	}

	private void showEditSchemeWindow(L2PcInstance  player, String groupType, String schemeName, int page)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		final List<Integer> schemeSkills = BufferManager.getInstance().getScheme(player.getObjectId(), schemeName);		
		html.setFile(getHtmlPath(getNpcId(), 2));
		html.replace("%schemename%", schemeName);
		html.replace("%count%", schemeSkills.size() + " / " + player.getMaxBuffCount());
		html.replace("%typesframe%", getTypesFrame(groupType, schemeName));
		html.replace("%skilllistframe%", getGroupSkillList(player, groupType, schemeName, page));
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
	}

	private String getGroupSkillList(L2PcInstance  player, String groupType, String schemeName, int page)
	{
		List<Integer> skills = BufferManager.getInstance().getSkillsIdsByType(groupType);
		if (skills.isEmpty())
			return "That group doesn't contain any skills.";
		
		final int max = MathUtil.countPagesNumber(skills.size(), PAGE_LIMIT);
		if (page > max)
			page = max;
		
		skills = skills.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, skills.size()));
		
		final List<Integer> schemeSkills = BufferManager.getInstance().getScheme(player.getObjectId(), schemeName);
		final StringBuilder sb = new StringBuilder(skills.size() * 150);
		
		int row = 0;
		for (int skillId : skills)
		{
			String icon = "";

			if(skillId == 4702 || skillId == 4703)
				icon = "icon.skill1332";
			else if(skillId == 4699 || skillId == 4700)
				icon = "icon.skill1331";
			else
			    icon = (skillId < 100) ? "icon.skill00" + skillId : (skillId < 1000) ? "icon.skill0" + skillId : "icon.skill" + skillId;
			
			sb.append(((row % 2) == 0 ? "<table width=\"280\" bgcolor=\"000000\"><tr>" : "<table width=\"280\"><tr>"));
			
			if (schemeSkills.contains(skillId))
				StringUtil.append(sb, "<td height=40 width=40><img src=\"", icon, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferManager.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass npc_%objectId%_skillunselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");
			else
				StringUtil.append(sb, "<td height=40 width=40><img src=\"", icon, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferManager.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass npc_%objectId%_skillselect ", groupType, " ", schemeName, " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
			
			sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
			row++;
		}
		
		for (int i = PAGE_LIMIT; i > row; i--)
			StringUtil.append(sb, "<img height=41>");
		
		sb.append("<br><img src=\"L2UI.SquareGray\" width=277 height=1><table width=\"100%\" bgcolor=000000><tr>");
		
		if (page > 1)
			StringUtil.append(sb, "<td align=left width=70><a action=\"bypass npc_" + getObjectId() + "_editschemes ", groupType, " ", schemeName, " ", page - 1, "\">Previous</a></td>");
		else
			StringUtil.append(sb, "<td align=left width=70>Previous</td>");
		
		StringUtil.append(sb, "<td align=center width=100>Page ", page, "</td>");
		
		if (page < max)
			StringUtil.append(sb, "<td align=right width=70><a action=\"bypass npc_" + getObjectId() + "_editschemes ", groupType, " ", schemeName, " ", page + 1, "\">Next</a></td>");
		else
			StringUtil.append(sb, "<td align=right width=70>Next</td>");
		
		sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
		
		return sb.toString();
	}

	private static String getTypesFrame(String groupType, String schemeName)
	{
		final StringBuilder sb = new StringBuilder(500);
		sb.append("<table>");
		
		int count = 0;
		for (String type : BufferManager.getInstance().getSkillTypes())
		{
			if (count == 0)
				sb.append("<tr>");
			
			if (groupType.equalsIgnoreCase(type))
				StringUtil.append(sb, "<td width=65>", type, "</td>");
			else
				StringUtil.append(sb, "<td width=65><a action=\"bypass npc_%objectId%_editschemes ", type, " ", schemeName, " 1\">", type, "</a></td>");
			
			count++;
			if (count == 4)
			{
				sb.append("</tr>");
				count = 0;
			}
		}
		
		if (!sb.toString().endsWith("</tr>"))
			sb.append("</tr>");
		
		sb.append("</table>");
		
		return sb.toString();
	}

	private void autoBuff(L2PcInstance player) 
	{
		if(player == null)
			return;
		
		List<Integer> Bufflist = player.isMageClass() ? Config.AUTO_MAGE_SKILL_LIST : Config.AUTO_FIGHTER_SKILL_LIST;
		if (Bufflist != null && Bufflist.size() > 0)
		{
			for (int skillId : Bufflist)
				SkillTable.getInstance().getInfo(skillId,SkillTable.getInstance().getMaxLevel(skillId, 0)).getEffects(player, player);			
		}
		ShowMain(player);
	}
	
	private void blessMe(L2PcInstance player) 
	{
		if(player != null)
		{
		   SkillTable.getInstance().getInfo(1323,1).getEffects(player, player);
		   ShowMain(player);
		}
	}
	
	private static int getCost(ArrayList<Integer> list)
	{
		if (Config.BUFFER_STATIC_BUFF_COST > 0)
			return list.size() * Config.BUFFER_STATIC_BUFF_COST;		
		int fee = 0;
		for (int sk : list)
			fee += BufferManager.getInstance().getAvailableBuff(sk).getValue();
		
		return fee;
	}
}