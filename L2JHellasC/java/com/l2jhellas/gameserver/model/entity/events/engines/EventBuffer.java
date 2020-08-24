package com.l2jhellas.gameserver.model.entity.events.engines;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.instancemanager.BufferManager;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.util.StringUtil;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class EventBuffer
{
	private Map<String, List<Integer>> buffTemplates;
	private Map<String, Boolean> changes;
	private UpdateTask updateTask;
	
	private static class SingletonHolder
	{
		protected static final EventBuffer _instance = new EventBuffer();
	}
	
	public static EventBuffer getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected class UpdateTask implements Runnable
	{
		@Override
		public void run()
		{
			updateSQL();
		}
	}
	
	public EventBuffer()
	{
		updateTask = new UpdateTask();
		changes = new ConcurrentHashMap<>();
		buffTemplates = new ConcurrentHashMap<>();
		loadSQL();
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(updateTask, 600000, 600000);
	}
	
	protected void buffPlayer(L2PcInstance player)
	{
		String playerId = "" + player.getObjectId() + player.getClassIndex();
		
		if (!buffTemplates.containsKey(playerId))
			return;
		
		for (int skillId : buffTemplates.get(playerId))
			SkillTable.getInstance().getInfo(skillId, SkillTable.getInstance().getMaxLevel(skillId,0)).getEffects(player, player);
	}
	
	public void changeList(L2PcInstance player, int buff, boolean action)
	{
		String playerId = "" + player.getObjectId() + player.getClassIndex();
		
		if (!buffTemplates.containsKey(playerId))
		{
			buffTemplates.put(playerId, new CopyOnWriteArrayList<Integer>());
			changes.put(playerId, true);
		}
		else
		{
			if (!changes.containsKey(playerId))
				changes.put(playerId, false);
			
			if (action)
				buffTemplates.get(playerId).add(buff);
			else
				buffTemplates.get(playerId).remove(buffTemplates.get(playerId).indexOf(buff));
		}
	}
	
	private void loadSQL()
	{
		if (!EventManager.getInstance().getBoolean("eventBufferEnabled"))
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM event_buffs");
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				buffTemplates.put(rset.getString("player"), new CopyOnWriteArrayList<Integer>());
				StringTokenizer st = new StringTokenizer(rset.getString("buffs"), ",");
				List<Integer> templist = new CopyOnWriteArrayList<>();
				while (st.hasMoreTokens())
					templist.add(Integer.parseInt(st.nextToken()));
				
				buffTemplates.put(rset.getString("player"), templist);
			}
		}
		catch (Exception e)
		{
			System.out.println("EventBuffs SQL catch " + e);
		}
	}
	
	protected boolean playerHaveTemplate(L2PcInstance player)
	{
		String playerId = "" + player.getObjectId() + player.getClassIndex();
		
		if (buffTemplates.containsKey(playerId))
			return true;
		
		return false;
	}

	public void showHtml(L2PcInstance player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(HtmlPath()); 
		html.replace("%plname%", player.getName());
		html.replace("%count%", EventManager.getInstance().getInt("maxBuffNum"));
		html.replace("%bufflist%", getSkillList(player));
		html.replace("%objectId%", player.getTargetId());
		player.sendPacket(html);
	}
	
	public String HtmlPath()
	{		
		return "data/html/mods/buffer/eventbuffer.htm";
	}
	
	private String getSkillList(L2PcInstance  player)
	{
		
		StringTokenizer st = new StringTokenizer(EventManager.getInstance().getString("allowedBuffsList"), ",");
		List<Integer> skillList = new CopyOnWriteArrayList<>();
		
		while (st.hasMoreTokens())
			skillList.add(Integer.parseInt(st.nextToken()));
		
		String playerId = "" + player.getObjectId() + player.getClassIndex();
		
		if (!buffTemplates.containsKey(playerId))
		{
			buffTemplates.put(playerId, new CopyOnWriteArrayList<Integer>());
			changes.put(playerId, true);
		}
		
		final StringBuilder sb = new StringBuilder(skillList.size() * 150);
		
		int row = 0;
		
		sb.append("<html><body><table width=270><tr><td width=200>Event Engine </td></tr></table><br><center><table width=270 bgcolor=5A5A5A><tr><td width=70>Edit Buffs</td><td width=80></td><td width=120>Remaining slots: " + (EventManager.getInstance().getInt("maxBuffNum") - buffTemplates.get(playerId).size()) + "</td></tr></table><br><br><table width=270 bgcolor=5A5A5A><tr><td>Added buffs:</td></tr></table><br>");


		for (int skillId : buffTemplates.get(playerId))
		{
			String icon = "";

			if(skillId == 4702 || skillId == 4703)
				icon = "icon.skill1332";
			else if(skillId == 4699 || skillId == 4700)
				icon = "icon.skill1331";
			else
				icon = (skillId < 100) ? "icon.skill00" + skillId : (skillId < 1000) ? "icon.skill0" + skillId : "icon.skill" + skillId;

			sb.append(((row % 2) == 0 ? "<table width=\"280\" bgcolor=\"000000\"><tr>" : "<table width=\"280\"><tr>"));

			StringUtil.append(sb, "<td height=40 width=40><img src=\"", icon, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferManager.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass -h eventbuffer " + skillId + " 0", "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");

			sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
			row++;
		}

		sb.append("<br><table width=270 bgcolor=5A5A5A><tr><td>Available buffs:</td></tr></table><br>");

		for (int skillId : skillList)
		{
			String icon = "";

				if(skillId == 4702 || skillId == 4703)
					icon = "icon.skill1332";
				else if(skillId == 4699 || skillId == 4700)
					icon = "icon.skill1331";
				else
					icon = (skillId < 100) ? "icon.skill00" + skillId : (skillId < 1000) ? "icon.skill0" + skillId : "icon.skill" + skillId;

				sb.append(((row % 2) == 0 ? "<table width=\"280\" bgcolor=\"000000\"><tr>" : "<table width=\"280\"><tr>"));

				if (!buffTemplates.get(playerId).contains(skillId))
				{
					if(EventManager.getInstance().getInt("maxBuffNum") - buffTemplates.get(playerId).size() != 0)
						StringUtil.append(sb, "<td height=40 width=40><img src=\"", icon, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferManager.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass -h eventbuffer " + skillId + " 1", "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
					else
						break;
				}
				
				sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
				row++;
		}
		
		for (int i = 10; i > row; i--)
			StringUtil.append(sb, "<img height=41>");
		
		sb.append("<br><img src=\"L2UI.SquareGray\" width=277 height=1><table width=\"100%\" bgcolor=000000><tr>");
		
		sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
		
		return sb.toString();
	}
	
	public void updateSQL()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			for (Map.Entry<String, Boolean> player : changes.entrySet())
			{
				StringBuilder sb = new StringBuilder();
				
				int c = 0;
				for (int buffid : buffTemplates.get(player.getKey()))
				{
					if (c == 0)
					{
						sb.append(buffid);
						c++;
					}
					else
						sb.append("," + buffid);
				}
				
				if (player.getValue())
				{
					try (PreparedStatement statement = con.prepareStatement("INSERT INTO event_buffs(player,buffs) VALUES (?,?)"))
					{
						statement.setString(1, player.getKey());
						statement.setString(2, sb.toString());
						statement.executeUpdate();
					}
				}
				else
				{
					try (PreparedStatement statement = con.prepareStatement("UPDATE event_buffs SET buffs=? WHERE player=?"))
					{
						statement.setString(1, sb.toString());
						statement.setString(2, player.getKey());
						statement.executeUpdate();
					}
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("EventBuffs SQL catch" + e);
		}
		
		changes.clear();
	}
}