package com.l2jhellas.gameserver.model.actor.instance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.StringTokenizer;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2DonateInstance extends L2NpcInstance
{
	Date dateInfo = new Date();
	SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh");
	String dayInfo = df.format(dateInfo);
	
	public L2DonateInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, String command)
	{
		if (player == null)
			return;
		
		if (command.startsWith("dlist"))
			info(player);
		if (command.startsWith("donate"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			String amount = null;
			String pin1 = null;
			String pin2 = null;
			String pin3 = null;
			String pin4 = null;
			String message = "";
			
			try
			{
				if (st.countTokens() < 5)
				{
					player.sendMessage("Complete all the fields before pressing donate");
					return;
				}
				amount = st.nextToken();
				pin1 = st.nextToken();
				pin2 = st.nextToken();
				pin3 = st.nextToken();
				pin4 = st.nextToken();
				while (st.hasMoreTokens())
					message = message + st.nextToken() + " ";
				if (pin1.length() != 4 || pin2.length() != 4 || pin3.length() != 4 || pin4.length() != 4)
				{
					player.sendMessage("Invalid Pin code.Please check your pin code and try again.");
					return;
				}
				String fname = "data/donates/" + dayInfo + " " + player.getName() + ".txt";
				File file = new File(fname);
				boolean exist = file.createNewFile();
				if (!exist)
				{
					player.sendMessage("Please wait until your last donation is checked!");
					return;
				}
				FileWriter fstream = new FileWriter(fname);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write("Character Info:");
				out.write("Character: " + player.getName() + "[" + player.getObjectId() + "]\n");
				out.write("Account: " + player.getAccountName() + "\n");
				out.write("IP: " + player.getClient().getConnection().getInetAddress().getHostAddress() + "\n");
				out.write("Message : " + message + "\n");
				out.write("Donate : " + amount + "\n");
				out.write("PIN : " + pin1 + " " + pin2 + " " + pin3 + " " + pin4 + "\n");
				out.write("Delete this file so player can make a new donation :)");
				out.close();
				player.sendMessage("Donation sent, you can donate again after 1 hour!");
				
				Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers().values();
				for (L2PcInstance gms : pls)
				{
					if (gms.isGM())
						gms.sendMessage(player.getName() + " sent a donation.");
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
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
			showHtmlWindow(player);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void showHtmlWindow(L2PcInstance activeChar)
	{
		StringBuilder tb = new StringBuilder();
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		
		tb.append("<html><head><title>Donation Manager</title></head><body><center><table width=\"250\" bgcolor=\"000000\"><tr><td align=center><font color=\"6fd3d1\">Easy Donation With Paysafe Card</font></td></tr></table>_______________________________________<br><br><table width=\"250\"><tr><td><font color=\"ddc16d\">Select Donation amount:</font></td><td><combobox width=80 height=17 var=amount list=10-Euro;25-Euro;50-Euro;100-Euro;></td></tr></table><br><br><font color=\"ddc16d\">Paysafe Card Pin:</font><table width=\"250\"><tr><td><edit var=\"pin1\" width=50 height=12 type=number></td><td><edit var=\"pin2\" width=50 height=12 type=number></td><td><edit var=\"pin3\" width=50 height=12 type=number></td><td><edit var=\"pin4\" width=50 height=12 type=number></td></table><br><br><multiedit var=\"message\" width=240 height=40><br><br><button value=\"Donate!\" action=\"bypass -h npc_" + getObjectId() + "_donate $amount $pin1 $pin2 $pin3 $pin4 $message\" width=95 height=21 back=\"bigbutton_over\" fore=\"bigbutton\"><br><button value=\"Donation List\" action=\"bypass -h npc_" + getObjectId() + "_dlist\" width=95 height=21 back=\"bigbutton_over\" fore=\"bigbutton\"><br><font color=\"a1df64\">L2jHellas Donation Manager</font></center></body></html>");
		
		html.setHtml(tb.toString());
		activeChar.sendPacket(html);
	}
	
	private static void info(L2PcInstance activeChar)
	{
		StringBuilder tb = new StringBuilder();
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		
		tb.append("<html><head><title>Donation Manager</title></head><body><center>Info goes here</center></body></html>");
		html.setHtml(tb.toString());
		activeChar.sendPacket(html);
	}
}