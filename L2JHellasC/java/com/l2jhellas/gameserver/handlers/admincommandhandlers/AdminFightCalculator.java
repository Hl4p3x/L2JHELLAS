package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Rnd;

public class AdminFightCalculator implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_fight_calculator",
		"admin_fight_calculator_show",
		"admin_fcs"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		try
		{
			if (command.startsWith("admin_fight_calculator_show"))
				handleShow(command.substring("admin_fight_calculator_show".length()), activeChar);
			else if (command.startsWith("admin_fcs"))
				handleShow(command.substring("admin_fcs".length()), activeChar);
			else if (command.startsWith("admin_fight_calculator"))
				handleStart(command.substring("admin_fight_calculator".length()), activeChar);
		}
		catch (StringIndexOutOfBoundsException e)
		{
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void handleStart(String params, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(params);
		int lvl1 = 0;
		int lvl2 = 0;
		int mid1 = 0;
		int mid2 = 0;
		while (st.hasMoreTokens())
		{
			String s = st.nextToken();
			if (s.equals("lvl1"))
			{
				lvl1 = Integer.parseInt(st.nextToken());
				continue;
			}
			if (s.equals("lvl2"))
			{
				lvl2 = Integer.parseInt(st.nextToken());
				continue;
			}
			if (s.equals("mid1"))
			{
				mid1 = Integer.parseInt(st.nextToken());
				continue;
			}
			if (s.equals("mid2"))
			{
				mid2 = Integer.parseInt(st.nextToken());
				continue;
			}
		}
		
		L2NpcTemplate npc1 = null;
		if (mid1 != 0)
			npc1 = NpcData.getInstance().getTemplate(mid1);
		L2NpcTemplate npc2 = null;
		if (mid2 != 0)
			npc2 = NpcData.getInstance().getTemplate(mid2);
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		StringBuilder replyMSG = new StringBuilder();
		if (npc1 != null && npc2 != null)
		{
			replyMSG.append("<html><title>Selected mobs to fight</title>");
			replyMSG.append("<body>");
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td><button value=\"Main\" action=\"bypass -h admin_admin\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			replyMSG.append("<td><button value=\"Game\" action=\"bypass -h admin_admin2\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			replyMSG.append("<td><button value=\"Effects\" action=\"bypass -h admin_admin3\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			replyMSG.append("<td><button value=\"Server\" action=\"bypass -h admin_admin4\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			replyMSG.append("<td><button value=\"Mods\" action=\"bypass -h admin_admin5\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			replyMSG.append("</tr>");
			replyMSG.append("</table><br1>");
			replyMSG.append("<table>");
			replyMSG.append("<tr><td>First</td><td>Second</td></tr>");
			replyMSG.append("<tr><td>level " + lvl1 + "</td><td>level " + lvl2 + "</td></tr>");
			replyMSG.append("<tr><td>id " + npc1.npcId + "</td><td>id " + npc2.npcId + "</td></tr>");
			replyMSG.append("<tr><td>" + npc1.name + "</td><td>" + npc2.name + "</td></tr>");
			replyMSG.append("</table>");
			replyMSG.append("<center><br><br><br>");
			replyMSG.append("<button value=\"OK\" action=\"bypass -h admin_fight_calculator_show " + npc1.npcId + " " + npc2.npcId + "\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
			replyMSG.append("</center>");
			replyMSG.append("</body></html>");
		}
		else if (lvl1 != 0 && npc1 == null)
		{
			replyMSG.append("<html><title>Select first mob to fight</title>");
			replyMSG.append("<body><table>");
			L2NpcTemplate[] npcs = NpcData.getInstance().getAllOfLevel(lvl1);
			for (L2NpcTemplate n : npcs)
			{
				replyMSG.append("<tr><td><a action=\"bypass -h admin_fight_calculator lvl1 " + lvl1 + " lvl2 " + lvl2 + " mid1 " + n.npcId + " mid2 " + mid2 + "\">" + n.name + "</a></td></tr>");
			}
			replyMSG.append("</table></body></html>");
		}
		else if (lvl2 != 0 && npc2 == null)
		{
			replyMSG.append("<html><title>Select second mob to fight</title>");
			replyMSG.append("<body>");
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td><button value=\"Main\" action=\"bypass -h admin_admin\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			replyMSG.append("<td><button value=\"Game\" action=\"bypass -h admin_admin2\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			replyMSG.append("<td><button value=\"Effects\" action=\"bypass -h admin_admin3\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			replyMSG.append("<td><button value=\"Server\" action=\"bypass -h admin_admin4\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			replyMSG.append("<td><button value=\"Mods\" action=\"bypass -h admin_admin5\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			replyMSG.append("</tr>");
			replyMSG.append("</table><br1>");
			replyMSG.append("<table>");
			L2NpcTemplate[] npcs = NpcData.getInstance().getAllOfLevel(lvl2);
			for (L2NpcTemplate n : npcs)
			{
				replyMSG.append("<tr><td><a action=\"bypass -h admin_fight_calculator lvl1 " + lvl1 + " lvl2 " + lvl2 + " mid1 " + mid1 + " mid2 " + n.npcId + "\">" + n.name + "</a></td></tr>");
			}
			replyMSG.append("</table></body></html>");
		}
		else
		{
			replyMSG.append("<html><title>Select mobs to fight</title>");
			replyMSG.append("<body>");
			replyMSG.append("<table width=260><tr>");
			replyMSG.append("<td><button value=\"Main\" action=\"bypass -h admin_admin\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			replyMSG.append("<td><button value=\"Game\" action=\"bypass -h admin_admin2\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			replyMSG.append("<td><button value=\"Effects\" action=\"bypass -h admin_admin3\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			replyMSG.append("<td><button value=\"Server\" action=\"bypass -h admin_admin4\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			replyMSG.append("<td><button value=\"Mods\" action=\"bypass -h admin_admin5\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			replyMSG.append("</tr>");
			replyMSG.append("</table><br1>");
			replyMSG.append("<table>");
			replyMSG.append("<tr><td>First</td><td>Second</td></tr>");
			replyMSG.append("<tr><td><edit var=\"lvl1\" width=80></td><td><edit var=\"lvl2\" width=80></td></tr>");
			replyMSG.append("</table>");
			replyMSG.append("<center><br><br><br>");
			replyMSG.append("<button value=\"OK\" action=\"bypass -h admin_fight_calculator lvl1 $lvl1 lvl2 $lvl2\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
			replyMSG.append("</center>");
			replyMSG.append("</body></html>");
		}
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private static void handleShow(String params, L2PcInstance activeChar)
	{
		params = params.trim();
		
		L2Character npc1 = null;
		L2Character npc2 = null;
		if (params.length() == 0)
		{
			npc1 = activeChar;
			npc2 = (L2Character) activeChar.getTarget();
			if (npc2 == null)
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return;
			}
		}
		else
		{
			int mid1 = 0;
			int mid2 = 0;
			StringTokenizer st = new StringTokenizer(params);
			mid1 = Integer.parseInt(st.nextToken());
			mid2 = Integer.parseInt(st.nextToken());
			
			npc1 = new L2MonsterInstance(IdFactory.getInstance().getNextId(), NpcData.getInstance().getTemplate(mid1));
			npc2 = new L2MonsterInstance(IdFactory.getInstance().getNextId(), NpcData.getInstance().getTemplate(mid2));
		}
		
		int miss1 = 0;
		int miss2 = 0;
		int shld1 = 0;
		int shld2 = 0;
		int crit1 = 0;
		int crit2 = 0;
		double patk1 = 0;
		double patk2 = 0;
		double pdef1 = 0;
		double pdef2 = 0;
		double dmg1 = 0;
		double dmg2 = 0;
		
		// ATTACK speed in milliseconds
		int sAtk1 = Formulas.calculateTimeBetweenAttacks(npc2);
		int sAtk2 = Formulas.calculateTimeBetweenAttacks(npc1);
		// number of ATTACK per 100 seconds
		sAtk1 = 100000 / sAtk1;
		sAtk2 = 100000 / sAtk2;
		
		for (int i = 0; i < 10000; i++)
		{
			boolean _miss1 = Formulas.calcHitMiss(npc1, npc2);
			if (_miss1)
				miss1++;
			byte _shld1 = Formulas.calcShldUse(npc1, npc2, false);
			if (_shld1 > 0)
				shld1++;
			boolean _crit1 = Formulas.calcCrit(npc1.getCriticalHit(npc2, null));
			if (_crit1)
				crit1++;
			
			double _patk1 = npc1.getPAtk(npc2);
			_patk1 += Rnd.nextDouble() * npc1.getRandomDamage();
			patk1 += _patk1;
			
			double _pdef1 = npc1.getPDef(npc2);
			pdef1 += _pdef1;
			
			if (!_miss1)
			{
				npc1.setAttackingBodypart();
				double _dmg1 = Formulas.calcPhysDam(npc1, npc2, null, _shld1, _crit1, false, false);
				dmg1 += _dmg1;
				npc1.abortAttack();
			}
		}
		
		for (int i = 0; i < 10000; i++)
		{
			boolean _miss2 = Formulas.calcHitMiss(npc2, npc1);
			if (_miss2)
				miss2++;
			byte _shld2 = Formulas.calcShldUse(npc2, npc1, false);
			if (_shld2 > 0)
				shld2++;
			boolean _crit2 = Formulas.calcCrit(npc2.getCriticalHit(npc1, null));
			if (_crit2)
				crit2++;
			
			double _patk2 = npc2.getPAtk(npc1);
			_patk2 += Rnd.nextDouble() * npc2.getRandomDamage();
			patk2 += _patk2;
			
			double _pdef2 = npc2.getPDef(npc1);
			pdef2 += _pdef2;
			
			if (!_miss2)
			{
				npc2.setAttackingBodypart();
				double _dmg2 = Formulas.calcPhysDam(npc2, npc1, null, _shld2, _crit2, false, false);
				dmg2 += _dmg2;
				npc2.abortAttack();
			}
		}
		
		miss1 /= 100;
		miss2 /= 100;
		shld1 /= 100;
		shld2 /= 100;
		crit1 /= 100;
		crit2 /= 100;
		patk1 /= 10000;
		patk2 /= 10000;
		pdef1 /= 10000;
		pdef2 /= 10000;
		dmg1 /= 10000;
		dmg2 /= 10000;
		
		// total damage per 100 seconds
		int tdmg1 = (int) (sAtk1 * dmg1);
		int tdmg2 = (int) (sAtk2 * dmg2);
		// HP restored per 100 seconds
		double maxHp1 = npc1.getMaxHp();
		int hp1 = (int) (Formulas.calcHpRegen(npc1) * 100000 / Formulas.getRegeneratePeriod(npc1));
		
		double maxHp2 = npc2.getMaxHp();
		int hp2 = (int) (Formulas.calcHpRegen(npc2) * 100000 / Formulas.getRegeneratePeriod(npc2));
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		StringBuilder replyMSG = new StringBuilder();
		replyMSG.append("<html><title>Selected mobs to fight</title>");
		replyMSG.append("<body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td><button value=\"Main\" action=\"bypass -h admin_admin\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"Game\" action=\"bypass -h admin_admin2\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"Effects\" action=\"bypass -h admin_admin3\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"Server\" action=\"bypass -h admin_admin4\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"Mods\" action=\"bypass -h admin_admin5\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr>");
		replyMSG.append("</table><br1>");
		replyMSG.append("<table>");
		if (params.length() == 0)
		{
			replyMSG.append("<tr><td width=140>Parameter</td><td width=70>Me</td><td width=70>Target</td></tr>");
		}
		else
		{
			replyMSG.append("<tr><td width=140>Parameter</td><td width=70>" + ((L2NpcTemplate) npc1.getTemplate()).name + "</td><td width=70>" + ((L2NpcTemplate) npc2.getTemplate()).name + "</td></tr>");
		}
		replyMSG.append("<tr><td>Miss</td><td>" + miss1 + "%</td><td>" + miss2 + "%</td></tr>");
		replyMSG.append("<tr><td>Shield</td><td>" + shld2 + "%</td><td>" + shld1 + "%</td></tr>");
		replyMSG.append("<tr><td>Crit</td><td>" + crit1 + "%</td><td>" + crit2 + "%</td></tr>");
		replyMSG.append("<tr><td>pAtk / pDef</td><td>" + ((int) patk1) + " / " + ((int) pdef1) + "</td><td>" + ((int) patk2) + " / " + ((int) pdef2) + "</td></tr>");
		replyMSG.append("<tr><td>Landing hits</td><td>" + sAtk1 + "</td><td>" + sAtk2 + "</td></tr>");
		replyMSG.append("<tr><td>Dmg per hit</td><td>" + ((int) dmg1) + "</td><td>" + ((int) dmg2) + "</td></tr>");
		replyMSG.append("<tr><td>Dmg get</td><td>" + tdmg2 + "</td><td>" + tdmg1 + "</td></tr>");
		replyMSG.append("<tr><td>Regenerate</td><td>" + hp1 + "</td><td>" + hp2 + "</td></tr>");
		replyMSG.append("<tr><td>HP Left</td><td>" + (int) maxHp1 + "</td><td>" + (int) maxHp2 + "</td></tr>");
		replyMSG.append("<tr><td>Die</td>");
		if (tdmg2 - hp1 > 1)
			replyMSG.append("<td>" + (int) (100 * maxHp1 / (tdmg2 - hp1)) + " sec</td>");
		else
			replyMSG.append("<td>never</td>");
		if (tdmg1 - hp2 > 1)
			replyMSG.append("<td>" + (int) (100 * maxHp2 / (tdmg1 - hp2)) + " sec</td>");
		else
			replyMSG.append("<td>never</td>");
		replyMSG.append("</tr>");
		replyMSG.append("</table>");
		replyMSG.append("<center><br>");
		if (params.length() == 0)
		{
			replyMSG.append("<button value=\"Retry\" action=\"bypass -h admin_fight_calculator_show\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		}
		else
		{
			replyMSG.append("<button value=\"Retry\" action=\"bypass -h admin_fight_calculator_show " + ((L2NpcTemplate) npc1.getTemplate()).npcId + " " + ((L2NpcTemplate) npc2.getTemplate()).npcId + "\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		}
		replyMSG.append("</center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
		
		if (params.length() != 0)
		{
			((L2MonsterInstance) npc1).deleteMe();
			((L2MonsterInstance) npc2).deleteMe();
		}
	}
}