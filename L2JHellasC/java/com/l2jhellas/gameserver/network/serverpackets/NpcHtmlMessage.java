package com.l2jhellas.gameserver.network.serverpackets;

import java.util.logging.Logger;

import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.clientpackets.RequestBypassToServer;

public class NpcHtmlMessage extends L2GameServerPacket
{
	// d S
	// d is usually 0, S is the html text starting with <html> and ending with </html>
	private static final String _S__1B_NPCHTMLMESSAGE = "[S] 0f NpcHtmlMessage";
	private static Logger _log = Logger.getLogger(RequestBypassToServer.class.getName());
	private final int _npcObjId;
	private String _html;
	private int _itemId = 0;
	private boolean _validate = true;
	
	public NpcHtmlMessage(int npcObjId, String text)
	{
		_npcObjId = npcObjId;
		setHtml(text);
	}
	
	public NpcHtmlMessage(int npcObjId)
	{
		_npcObjId = npcObjId;
	}
	
	@Override
	public void runImpl()
	{
		buildBypassCache(getClient().getActiveChar());
	}
	
	public void setHtml(String text)
	{
		if (text.length() > 8192)
		{
			_log.warning(NpcHtmlMessage.class.getName() + ": Html is too long! this will crash the client!");
			_html = "<html><body>Html was too long</body></html>";
			return;
		}
		_html = text; // html code must not exceed 8192 bytes
	}
	
	public boolean setFile(String path)
	{
		String content = HtmCache.getInstance().getHtm(path);
		
		if (content == null)
		{
			setHtml("<html><body>My Text is missing:<br>" + path + "</body></html>");
			_log.warning(NpcHtmlMessage.class.getName() + ": missing html page " + path);
			return false;
		}
		
		setHtml(content);
		return true;
	}
	
	public void basicReplace(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value);
	}
	
	public void replace(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value.replaceAll("\\$", "\\\\\\$"));
	}
	
	public void replace(String pattern, int value)
	{
		_html = _html.replaceAll(pattern, Integer.toString(value));
	}
	
	public void replace(String pattern, long value)
	{
		_html = _html.replaceAll(pattern, Long.toString(value));
	}
	
	public void replace(String pattern, double value)
	{
		_html = _html.replaceAll(pattern, Double.toString(value));
	}
	
	private final void buildBypassCache(L2PcInstance activeChar)
	{
		if (!_validate)
			return;
		
		if (activeChar == null)
			return;
		
		activeChar.clearBypass();
		
		for (int i = 0; i < _html.length(); i++)
		{
			int start = _html.indexOf("\"bypass ", i);
			int finish = _html.indexOf("\"", start + 1);
			if (start < 0 || finish < 0)
				break;
			
			if (_html.substring(start + 8, start + 10).equals("-h"))
				start += 11;
			else
				start += 8;
			
			i = finish;
			int finish2 = _html.indexOf("$", start);
			if (finish2 < finish && finish2 > 0)
				activeChar.addBypass2(_html.substring(start, finish2).trim());
			else
				activeChar.addBypass(_html.substring(start, finish).trim());
		}
	}
	
	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}
	
	public void disableValidation()
	{
		_validate = false;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x0f);
		
		writeD(_npcObjId);
		writeS(_html);
		writeD(_itemId);
	}
	
	@Override
	public String getType()
	{
		return _S__1B_NPCHTMLMESSAGE;
	}
}