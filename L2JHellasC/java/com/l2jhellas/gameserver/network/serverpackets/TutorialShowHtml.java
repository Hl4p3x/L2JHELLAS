package com.l2jhellas.gameserver.network.serverpackets;

public class TutorialShowHtml extends L2GameServerPacket
{
	private static final String _S__A0_TUTORIALSHOWHTML = "[S] a0 TutorialShowHtml";
	private final String _html;
	
	public TutorialShowHtml(String html)
	{
		_html = html;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xa0);
		writeS(_html);
	}
	
	@Override
	public String getType()
	{
		return _S__A0_TUTORIALSHOWHTML;
	}
}