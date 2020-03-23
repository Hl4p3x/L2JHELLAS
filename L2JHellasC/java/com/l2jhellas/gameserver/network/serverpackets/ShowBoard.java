package com.l2jhellas.gameserver.network.serverpackets;

import java.util.List;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class ShowBoard extends L2GameServerPacket
{
	private static final String _S__6E_SHOWBOARD = "[S] 6e ShowBoard";
	
	private final String _htmlCode;
	private final String _id;
	private List<String> _arg;

	private byte htmlBytes[];
	
	public ShowBoard(String htmlCode, String id)
	{
		_id = id;
		_htmlCode = htmlCode; // html code must not exceed 8192 bytes
	}
	
	public ShowBoard(List<String> arg)
	{
		_id = "1002";
		_htmlCode = null;
		_arg = arg;
		
	}
	
	private byte[] get1002()
	{
		int len = _id.getBytes().length * 2 + 2;
		for (String arg : _arg)
		{
			len += (arg.getBytes().length + 4) * 2;
		}
		byte data[] = new byte[len];
		int i = 0;
		for (int j = 0; j < _id.getBytes().length; j++, i += 2)
		{
			data[i] = _id.getBytes()[j];
			data[i + 1] = 0;
		}
		data[i] = 8;
		i++;
		data[i] = 0;
		i++;
		for (String arg : _arg)
		{
			for (int j = 0; j < arg.getBytes().length; j++, i += 2)
			{
				data[i] = arg.getBytes()[j];
				data[i + 1] = 0;
			}
			data[i] = 0x20;
			i++;
			data[i] = 0x0;
			i++;
			data[i] = 0x8;
			i++;
			data[i] = 0x0;
			i++;
		}
		return data;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x6e);
		writeC(0x01); // c4 1 to show community 00 to hide
		writeS("bypass _bbshome"); // top
		writeS("bypass _bbsgetfav"); // favorite
		writeS("bypass _bbsloc"); // region
		writeS("bypass _bbsclan"); // clan
		writeS("bypass _bbsmemo"); // memo
		writeS("bypass _bbsmail"); // mail
		writeS("bypass _bbsfriends"); // friends
		writeS("bypass bbs_add_fav"); // add fav.
		if (!_id.equals("1002"))
		{
			htmlBytes = null;
			if (_htmlCode != null)
				htmlBytes = _htmlCode.getBytes();
			byte data[] = new byte[2 + 2 + 2 + _id.getBytes().length * 2 + 2 * ((_htmlCode != null) ? htmlBytes.length : 0)];
			int i = 0;
			for (int j = 0; j < _id.getBytes().length; j++, i += 2)
			{
				data[i] = _id.getBytes()[j];
				data[i + 1] = 0;
			}
			data[i] = 8;
			i++;
			data[i] = 0;
			i++;
			if (_htmlCode == null)
			{
				
			}
			else
			{
				for (int j = 0; j < htmlBytes.length; i += 2, j++)
				{
					data[i] = htmlBytes[j];
					data[i + 1] = 0;
				}
			}
			data[i] = 0;
			i++;
			data[i] = 0;
			// writeS(_htmlCode); // current page
			writeB(data);
		}
		else
		{
			writeB(get1002());
		}
	}
	
	@Override
	public String getType()
	{
		return _S__6E_SHOWBOARD;
	}
	
	public static void separateAndSend(String html, L2PcInstance player)
	{
		if (html.length() < 4090)
		{
			player.sendPacket(new ShowBoard(html, "101"));
			player.sendPacket(new ShowBoard(null, "102"));
			player.sendPacket(new ShowBoard(null, "103"));
		}
		else if (html.length() < 8180)
		{
			player.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
			player.sendPacket(new ShowBoard(html.substring(4090, html.length()), "102"));
			player.sendPacket(new ShowBoard(null, "103"));
		}
		else if (html.length() < 12270)
		{
			player.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
			player.sendPacket(new ShowBoard(html.substring(4090, 8180), "102"));
			player.sendPacket(new ShowBoard(html.substring(8180, html.length()), "103"));
		}
	}
}