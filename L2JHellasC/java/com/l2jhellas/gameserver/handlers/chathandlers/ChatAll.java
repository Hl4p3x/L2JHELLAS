package com.l2jhellas.gameserver.handlers.chathandlers;

import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.handler.IChatHandler;
import com.l2jhellas.gameserver.handler.IVoicedCommandHandler;
import com.l2jhellas.gameserver.handler.VoicedCommandHandler;
import com.l2jhellas.gameserver.model.BlockList;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.FloodAction;

public class ChatAll implements IChatHandler
{
	final Logger _log = Logger.getLogger(ChatAll.class.getName());
	private static final ChatType[] COMMAND_IDS =
	{
		ChatType.GENERAL
	};
	
	@Override
	public void handleChat(ChatType type, L2PcInstance activeChar, String target, String text)
	{
		if (!FloodProtectors.performAction(activeChar.getClient(), FloodAction.GLOBAL_CHAT))
			return;
		
		if (text.startsWith(".") && !text.startsWith(".."))
		{
			StringTokenizer st = new StringTokenizer(text);
			IVoicedCommandHandler vch;
			String command = "";
			
			if (st.countTokens() > 1)
			{
				command = st.nextToken().substring(1);
				target = text.substring(command.length() + 2);
				vch = VoicedCommandHandler.getInstance().getHandler(command);
			}
			else
			{
				command = text.substring(1);
				vch = VoicedCommandHandler.getInstance().getHandler(command);
			}
			
			if (vch != null)
				vch.useVoicedCommand(command, activeChar, target);				
		}
		else
		{
			
			boolean vcd_used = false;
			
			if (text.startsWith("."))
			{
				StringTokenizer st = new StringTokenizer(text);
				IVoicedCommandHandler vch;
				String command = "";
				String params = "";
				
				if (st.countTokens() > 1)
				{
					command = st.nextToken().substring(1);
					params = text.substring(command.length() + 2);
					vch = VoicedCommandHandler.getInstance().getHandler(command);
				}
				else
				{
					command = text.substring(1);
					vch = VoicedCommandHandler.getInstance().getHandler(command);
				}
				
				if (vch != null)
				{
					vch.useVoicedCommand(command, activeChar, params);
					vcd_used = true;
				}
				else
				{
					vcd_used = false;
				}
			}
			
			if (!vcd_used)
			{
				CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
				Collection<L2PcInstance> plrs = L2World.getInstance().getVisibleObjects(activeChar, L2PcInstance.class, 1250);
				
				for (L2PcInstance player : plrs)
				{
					if ((player != null) && !BlockList.isBlocked(player, activeChar))
						player.sendPacket(cs);
				}
				activeChar.sendPacket(cs);
			}
		}
	}
	
	@Override
	public ChatType[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}