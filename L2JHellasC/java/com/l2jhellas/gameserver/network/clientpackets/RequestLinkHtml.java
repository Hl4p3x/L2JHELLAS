package com.l2jhellas.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;

public final class RequestLinkHtml extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestLinkHtml.class.getName());
	private static final String REQUESTLINKHTML__C__20 = "[C] 20 RequestLinkHtml";
	private String _link;
	
	@Override
	protected void readImpl()
	{
		_link = readS();
	}
	
	@Override
	public void runImpl()
	{
		L2PcInstance actor = getClient().getActiveChar();
		if (actor == null)
			return;
		
		if (_link.isEmpty())
		{
			_log.warning("Player " + actor.getName() + " sent empty html link!");
			return;
		}
		
		if (_link.contains("..") || !_link.contains(".htm"))
		{
			_log.warning(RequestLinkHtml.class.getName() + ": [RequestLinkHtml] hack? link contains prohibited characters: '" + _link + "', skipped.");
			return;
		}
		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setFile(_link);
		
		sendPacket(msg);
	}
	
	@Override
	public String getType()
	{
		return REQUESTLINKHTML__C__20;
	}
}