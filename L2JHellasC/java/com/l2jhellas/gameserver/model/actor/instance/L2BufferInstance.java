package com.l2jhellas.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.StringUtil;

public final class L2BufferInstance extends L2NpcInstance
{
	public L2BufferInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		
		int buffid = 0;
		int bufflevel = 1;
		if (st.countTokens() == 2)
		{
			buffid = Integer.valueOf(st.nextToken());
			bufflevel = Integer.valueOf(st.nextToken());
		}
		else if (st.countTokens() == 1)
			buffid = Integer.valueOf(st.nextToken());
		
		if (actualCommand.equalsIgnoreCase("getbuff"))
		{
			if (buffid != 0)
			{
				final L2Skill skill = SkillTable.getInstance().getInfo(buffid, bufflevel);
				skill.getEffects(player, player);
				showMessageWindow(player);
				player.broadcastPacket(new MagicSkillUse(this, player, buffid, bufflevel, 5, 0));
			}
		}
		else if (actualCommand.equalsIgnoreCase("restore"))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
			showMessageWindow(player);
		}
		else if (actualCommand.equalsIgnoreCase("cancel"))
		{
			player.stopAllEffects();
			showMessageWindow(player);
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
		}
		else if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
		{
			player.setLastFolkNPC(this);
			showMessageWindow(player);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void showMessageWindow(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		final StringBuilder strBuffer = StringUtil.startAppend(3500, "<html><title>CLAW Magician</title><body><center>");
		
		if (player.isSitting())
		{
			player.sendMessage("You can't use buffer while you're sitting.");
			strBuffer.append("Stand up, <font color=\"LEVEL\">%charname%</font>!<br>");
			strBuffer.append("How dare you to talk with me while you're sitting?!<br>");
		}
		else if (player.isAlikeDead() || player.isDead())
		{
			player.sendMessage("You can't use buffer while you're dead or using fake death.");
			strBuffer.append("Sadly, <font color=\"LEVEL\">%charname%</font>, you're dead.<br>");
			strBuffer.append("I can't offer any support effect for dead people...<br>");
		}
		else if (player.isInCombat())
		{
			player.sendMessage("You can't use buffer while you're in combat.");
			strBuffer.append("Sadly, <font color=\"LEVEL\">%charname%</font>, I can't serve you.<br>");
			strBuffer.append("Came back when you will not be in a combat.<br>");
		}
		else
		{
			strBuffer.append("Welcome, <font color=\"LEVEL\">%charname%</font>!<br>");
			strBuffer.append("Here is the list of all available effects:<br>");
			strBuffer.append("<img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32 align=left>");
			strBuffer.append("<table width=300>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1204 2\">Wind Walk</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1040 3\">Shield</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1243 6\">Bless Shield</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1068 3\">Might</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1036 2\">Magic Barrier</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1259 4\">Resist Shock</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1035 4\">Mental Shield</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1045 6\">Blessed Body</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1304 3\">Advanced Block</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1048 6\">Blessed Soul</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1062 2\">Berserker Spirit</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1189 3\">Resist Wind</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1086 2\">Haste</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1240 3\">Guidance</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1393 3\">Unholy Resistance</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1242 3\">Death Whisper</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1077 3\">Focus</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1033 3\">Resist Poison</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1268 4\">Vampiric Rage</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1087 3\">Agility</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1191 3\">Resist Fire</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1085 3\">Acumen</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1059 3\">Empower</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1182 3\">Resist Aqua</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1303 2\">Wild Magic</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 1078 6\">Concentration</a></td><td><a action=\"bypass -h npc_%objectId%_getbuff 1352 1\">Elemental Protection</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1353 1\">Divine Protection</a></td><td><a action=\"bypass -h npc_%objectId%_getbuff 1397 3\">Clarity</a></td><td><a action=\"bypass -h npc_%objectId%_getbuff 1392 3\">Holy Resistance</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 1043 1\">Holy Weapon</a></td><td><a action=\"bypass -h npc_%objectId%_getbuff 1032 3\">Invigor</a></td><td><a action=\"bypass -h npc_%objectId%_getbuff 1044 3\">Regeneration</a></td></tr>");
			strBuffer.append("<tr><td></td></tr>");
			strBuffer.append("<tr><td><font color=\"ff9900\">Dances:</font></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 275 1\">Fury</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 273 1\">Mystic</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 365 1\">Siren's</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 274 1\">Fire</a></td>  <td><a action=\"bypass -h npc_%objectId%_getbuff 276 1\">Concentration</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 310 1\">Vampire</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 271 1\">Warrior</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 277 1\">Light</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 272 1\">Inspiration</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 311 1\">Protection</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 309 1\">Earth Guard</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 307 1\">Aqua Guard</a></td></tr>");
			strBuffer.append("<tr><td></td></tr>");
			strBuffer.append("<tr><td><font color=\"ff9900\">Songs:</font></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 264 1\">Earth</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 269 1\">Hunter</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 270 1\">Invocation</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 266 1\">Water</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 267 1\">Warding</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 304 1\">Vitality</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 268 1\">Wind</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 364 1\">Champion</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 349 1\">Renewal</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 265 1\">Life</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 363 1\">Meditation</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 305 1\">Vengeance</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_getbuff 308 1\">Storm Guard</a></td> <td><a action=\"bypass -h npc_%objectId%_getbuff 306 1\">Flame Guard</a></td></tr>");
			strBuffer.append("<tr><td></td></tr>");
			strBuffer.append("<tr><td><font color=\"ff9900\">Other:</font></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_cancel\"><font color=\"ffffff\">Cancel</font></a></td><td><a action=\"bypass -h npc_%objectId%_getbuff 1388 3\">Greater Might</a></td><td><a action=\"bypass -h npc_%objectId%_getbuff 1389 3\">Greater Shield</a></td></tr>");
			strBuffer.append("<tr><td><a action=\"bypass -h npc_%objectId%_restore\"><font color=\"ffffff\">Restore</font></a></td><td><a action=\"bypass -h npc_%objectId%_getbuff 1363 1\">Chant of Victory</a></td><td><a action=\"bypass -h npc_%objectId%_getbuff 1356 1\">Prophecy of Fire</a></td></tr>");
			strBuffer.append("<tr><td></td><td><a action=\"bypass -h npc_%objectId%_getbuff 1355 1\">Prophecy of Water</a></td><td><a action=\"bypass -h npc_%objectId%_getbuff 1357 1\">Prophecy of Wind</a></td></tr>");
			strBuffer.append("<tr><td></td><td><a action=\"bypass -h npc_%objectId%_getbuff 1413 1\">Magnus' Chant</a></td><td></td></tr>");
			strBuffer.append("</table><img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32 align=left>");
		}
		strBuffer.append("</center></body></html>");
		html.setHtml(strBuffer.toString());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%charname%", player.getName());
		player.sendPacket(html);
	}
}