package com.l2jhellas.gameserver.network.clientpackets;

import java.util.Collection;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.handler.ChatHandler;
import com.l2jhellas.gameserver.handler.IChatHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.util.Util;

public final class Say2 extends L2GameClientPacket
{
	private static final String _C__38_SAY2 = "[C] 38 Say2";

	private int _type;
	
	private String _text;
	private String _target;
	
	@Override
	protected void readImpl()
	{
		_text = readS();
		_type = readD();
		_target = (_type == ChatType.WHISPER.getClientId()) ? readS() : null;
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		if (!activeChar.getAppearance().isVisible())
		{		
			activeChar.sendPacket(SystemMessageId.NOT_CHAT_WHILE_INVISIBLE);
			return;
		}
		
		if (_text.isEmpty() || _text.length() > 100)
			return;
		
		if (_type < 0 || _type > ChatType.values().length)
			return;
		
		ChatType chatType = ChatType.findByClientId(_type);

		if (chatType == null)
			return;

		if (!activeChar.isGM() && (chatType == ChatType.ANNOUNCEMENT || chatType == ChatType.CRITICAL_ANNOUNCE))
			return;
		
		if (chatType == ChatType.WHISPER && checkBot(_text))
		{
			Util.handleIllegalPlayerAction(activeChar, "Client Emulator Detect: " + activeChar.getName() + " is using L2Walker.", Config.DEFAULT_PUNISH);
			return;
		}

		if (activeChar.isChatBanned() || (activeChar.isInJail() && Config.JAIL_DISABLE_CHAT && !activeChar.isGM()))
		{
			activeChar.sendPacket(SystemMessageId.CHATTING_PROHIBITED);
			return;
		}
		
		if (activeChar.isCursedWeaponEquiped() && (chatType == ChatType.TRADE || chatType == ChatType.SHOUT))
		{
			activeChar.sendMessage("Shout and trade chatting cannot be used while possessing a cursed weapon.");
			return;
		}
				
		// Say Filter implementation
		if (Config.USE_SAY_FILTER)
			checkText(activeChar);
		
		if (chatType == ChatType.PETITION_PLAYER && activeChar.isGM())
			chatType = ChatType.PETITION_GM;
		
		L2Object saymode = activeChar.getSayMode();
		if (saymode != null)
		{
			String name = saymode.getName();
			int actor = saymode.getObjectId();
			chatType = ChatType.GENERAL;
			
			final Collection<L2PcInstance> list = L2World.getInstance().getVisibleObjects(saymode, L2PcInstance.class);
			
			CreatureSay cs = new CreatureSay(actor, chatType, name, _text);
			for (L2Object obj : list)
			{
				if ((obj == null))
					continue;
				
				L2Character chara = (L2Character) obj;
				chara.sendPacket(cs);
			}
			return;
		}
		
		IChatHandler handler = ChatHandler.getInstance().getHandler(chatType);
		if (handler != null)
			handler.handleChat(chatType, activeChar, _target, _text);
	}
	
	private void checkText(L2PcInstance activeChar)
	{
		String filteredText = _text;
		filteredText.toLowerCase();
		
		for (String pattern : Config.FILTER_LIST)
		{
			filteredText = filteredText.replaceAll("(?i)" + pattern, Config.CHAT_FILTER_CHARS);
		}
		
		_text = filteredText;
	}
	
	@Override
	public String getType()
	{
		return _C__38_SAY2;
	}
	
	private static final String[] WALKER_COMMAND_LIST =
	{
		"USESKILL",
		"USEITEM",
		"BUYITEM",
		"SELLITEM",
		"SAVEITEM",
		"LOADITEM",
		"MSG",
		"DELAY",
		"LABEL",
		"JMP",
		"CALL",
		"RETURN",
		"MOVETO",
		"NPCSEL",
		"NPCDLG",
		"DLGSEL",
		"CHARSTATUS",
		"POSOUTRANGE",
		"POSINRANGE",
		"GOHOME",
		"SAY",
		"EXIT",
		"PAUSE",
		"STRINDLG",
		"STRNOTINDLG",
		"CHANGEWAITTYPE",
		"FORCEATTACK",
		"ISMEMBER",
		"REQUESTJOINPARTY",
		"REQUESTOUTPARTY",
		"QUITPARTY",
		"MEMBERSTATUS",
		"CHARBUFFS",
		"ITEMCOUNT",
		"FOLLOWTELEPORT"
	};
	
	private static boolean checkBot(String text)
	{
		for (String botCommand : WALKER_COMMAND_LIST)
		{
			if (text.startsWith(botCommand))
				return true;
		}
		return false;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}