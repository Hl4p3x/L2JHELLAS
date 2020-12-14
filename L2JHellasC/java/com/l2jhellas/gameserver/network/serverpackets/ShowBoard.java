package com.l2jhellas.gameserver.network.serverpackets;

import java.util.List;

import com.l2jhellas.util.StringUtil;

public class ShowBoard extends L2GameServerPacket
{
	public static final ShowBoard STATIC_SHOWBOARD_102 = new ShowBoard(null, "102");
	public static final ShowBoard STATIC_SHOWBOARD_103 = new ShowBoard(null, "103");
		
	private final StringBuilder _htmlCode = new StringBuilder();
	
	public ShowBoard(String htmlCode, String id)
	{
		StringUtil.append(_htmlCode, id, "\u0008", htmlCode);
	}
	
	public ShowBoard(List<String> arg)
	{
		_htmlCode.append("1002\u0008");
		for (String str : arg)
			StringUtil.append(_htmlCode, str, " \u0008");
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x6e);
		writeC(0x01);
		writeS("bypass _bbshome");
		writeS("bypass _bbsgetfav");
		writeS("bypass _bbsloc");
		writeS("bypass _bbsclan");
		writeS("bypass _bbsmemo");
		writeS("bypass _bbsmail");
		writeS("bypass _bbsfriends");
		writeS("bypass bbs_add_fav");
		writeS(_htmlCode.toString());
	}
}