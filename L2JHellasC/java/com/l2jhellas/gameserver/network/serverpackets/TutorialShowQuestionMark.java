package com.l2jhellas.gameserver.network.serverpackets;

public class TutorialShowQuestionMark extends L2GameServerPacket
{
	private static final String _S__A1_TUTORIALSHOWQUESTIONMARK = "[S] a1 TutorialShowQuestionMark";
	private final int _blink;
	
	public TutorialShowQuestionMark(int blink)
	{
		_blink = blink; // this influences the blinking frequency :S
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xa1);
		writeD(_blink);
	}
	
	@Override
	public String getType()
	{
		return _S__A1_TUTORIALSHOWQUESTIONMARK;
	}
}