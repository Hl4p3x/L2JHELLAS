package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.l2jhellas.gameserver.enums.FenceState;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.instancemanager.FenceManager;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2FenceInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;

public class AdminFence implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_fence",
		"admin_addfence",
		"admin_setfencestate",
		"admin_removefence",
		"admin_listfence",
		"admin_fence_page",
		"admin_gofence"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String cmd = st.nextToken();
		
		switch (cmd)
		{
			case "admin_fence":
			{
				MainFence(activeChar);
				break;
			}
			case "admin_addfence":
			{
				try
				{
					final String _fenceName = String.valueOf(st.nextToken());
					final int fenceTypeOrdinal = Integer.parseInt(st.nextToken());
					final FenceState state = FenceState.values()[fenceTypeOrdinal];
					
					final int width = Integer.parseInt(st.nextToken());
					final int length = Integer.parseInt(st.nextToken());
					final int height = Integer.parseInt(st.nextToken());
					
					if (_fenceName.isEmpty() || !_fenceName.isEmpty() && _fenceName.length() > 16)
					{
						activeChar.sendMessage("Something went wrong with name value");
						return false;
					}
					if ((width < 1) || (length < 1))
					{
						activeChar.sendMessage("Width and length values must be positive numbers.");
						return false;
					}
					if ((height < 1) || (height > 3))
					{
						activeChar.sendMessage("The range for height can only be 1-3.");
						return false;
					}
					
					FenceManager.getInstance().spawnFence(_fenceName, activeChar.getX(), activeChar.getY(), activeChar.getZ(), width, length, height, state);
					activeChar.sendMessage("Fence:" + _fenceName + " spawned succesfully.");
					MainFence(activeChar);
				}
				catch (NoSuchElementException | NumberFormatException e)
				{
					activeChar.sendMessage("Format must be: //addfence <name> <width> <length> <height> <state>");
				}
				
				break;
			}
			case "admin_setfencestate":
			{
				try
				{
					final int objId = Integer.parseInt(st.nextToken());
					final int fenceTypeOrdinal = Integer.parseInt(st.nextToken());
					
					if ((fenceTypeOrdinal < 0) || (fenceTypeOrdinal >= FenceState.values().length))
					{
						activeChar.sendMessage("Specified FenceType is out of range. Only 0-" + (FenceState.values().length - 1) + " are permitted.");
					}
					else
					{
						final L2Object obj = L2World.getInstance().findObject(objId);
						if (obj != null && obj instanceof L2FenceInstance)
						{
							final L2FenceInstance fence = (L2FenceInstance) obj;
							final FenceState state = FenceState.values()[fenceTypeOrdinal];
							fence.setState(state);
						}
						else
						{
							activeChar.sendMessage("Target is not a fence.");
						}
					}
				}
				catch (NoSuchElementException | NumberFormatException e)
				{
					activeChar.sendMessage("Format must be: //setfencestate <fenceObjectId> <fenceState>");
				}
				
				break;
			}
			case "admin_removefence":
			{
				try
				{
					final int objId = Integer.parseInt(st.nextToken());
					final L2Object obj = L2World.getInstance().findObject(objId);
					if (obj != null && obj instanceof L2FenceInstance)
					{
						((L2FenceInstance) obj).deleteMe();
						activeChar.sendMessage("Fence removed succesfully.");
					}
					else
						activeChar.sendMessage("Target is not a fence.");
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Invalid object ID or target was not found.");
				}
				
				ShowFenceList(activeChar);
				break;
			}
			case "admin_listfence":
			{
				ShowFenceList(activeChar);
				break;
			}
			case "admin_gofence":
			{
				try
				{
					final int objId = Integer.parseInt(st.nextToken());
					final L2Object obj = L2World.getInstance().findObject(objId);
					if (obj != null)
						activeChar.teleToLocation(obj.getX(), obj.getY(), obj.getZ());
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Invalid object ID or target was not found.");
				}
				
				break;
			}
			case "admin_fence_page":
			{
				try
				{
					final int objId = Integer.parseInt(st.nextToken());
					final L2Object obj = L2World.getInstance().findObject(objId);
					
					if (obj != null)
						FencePage(activeChar, objId);
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Invalid object ID or target was not found.");
				}
				
				break;
			}
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void FencePage(L2PcInstance player, int id)
	{
		final L2FenceInstance fence = FenceManager.getInstance().getFence(id);
		final StringBuilder sb = new StringBuilder();
		
		sb.append("<html><body>FenceName: " + fence.getFenceName() + "<br>");
		sb.append("<a action=\"bypass -h admin_gofence " + fence.getObjectId() + " 1\">TeleToFence</a><br>");
		sb.append("<a action=\"bypass -h admin_setfencestate " + fence.getObjectId() + " 0\">HideFence</a><br>");
		sb.append("<a action=\"bypass -h admin_setfencestate " + fence.getObjectId() + " 2\">UnhideFene</a><br>");
		sb.append("<a action=\"bypass -h admin_setfencestate " + fence.getObjectId() + " 1\">openFence</a><br>");
		sb.append("<a action=\"bypass -h admin_setfencestate " + fence.getObjectId() + " 2\">close</a><br>");
		sb.append("<a action=\"bypass -h admin_removefence " + fence.getObjectId() + " 1\">RemoveFence</a><br>");
		sb.append("</body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		
		player.sendPacket(html);
	}
	
	private static void ShowFenceList(L2PcInstance player)
	{
		
		final int fences = FenceManager.getInstance().getFenceCount();
		final StringBuilder sb = new StringBuilder();
		
		sb.append("<html><body>Total Fences: " + fences + "<br><br>");
		
		for (L2FenceInstance fence : FenceManager.getInstance().getFences().values())
			sb.append("<a action=\"bypass -h admin_fence_page " + fence.getObjectId() + " 1\">Fence: " + " [" + fence.getFenceName() + "]</a><br>");
		
		sb.append("</body></html>");
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		
		player.sendPacket(html);
	}
	
	public void MainFence(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(5);
		final StringBuilder sb = new StringBuilder();
		
		sb.append("<html><title>L2JHellas's Fence Builder</title><body>");
		sb.append("<table width=270>");
		sb.append("<tr></tr>");
		sb.append("<tr><td>Name:</td></tr>");
		sb.append("<tr><td><td><edit var=\"name\"></td></tr>");
		sb.append("<tr><td>Type: </td></tr>");
		sb.append("<tr><td><combobox width=75 var=type list=0;1;2></td></tr>");
		sb.append("<tr><td>Width:</td></tr>");
		sb.append("<tr><td><td><edit var=\"wid\"></td></tr>");
		sb.append("<tr><td>Lenght:</td></tr>");
		sb.append("<tr><td><td><edit var=\"len\"></td></tr>");
		sb.append("<tr><td>Hight: </td></tr>");
		sb.append("<tr><td><td><combobox width=75 var=hight list=1;2;3></td></tr>");
		sb.append("<tr>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("<br>");
		sb.append("<br>");
		sb.append("<a action=\"bypass -h admin_addfence $name $type $wid $len $hight \">AddFence</a><br>");
		sb.append("<a action=\"bypass -h admin_listfence \">FenceList</a><br>");
		sb.append("</body></html>");
		
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
}