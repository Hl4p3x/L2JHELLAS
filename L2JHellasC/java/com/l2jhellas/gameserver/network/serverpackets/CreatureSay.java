package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;

public class CreatureSay extends L2GameServerPacket
{
	// ddSS
	private static final String _S__4A_CREATURESAY = "[S] 4A CreatureSay";
	private final int _objectId;
	private final int _textType;
	private String _charName = null;
	private int _charId = 0;
	private String _text = null;
	private int _npcString = -1;
	private List<String> _parameters;
	
	public CreatureSay(int objectId, int messageType, String charName, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_charName = charName;
		_text = text;
	}
	
	public CreatureSay(int objectId, ChatType messageType, String charName, String text)
	{
		_objectId = objectId;
		_textType = messageType.getClientId();
		_charName = charName;
		_text = text;
	}
	
	public CreatureSay(int objectId, int messageType, int charId, SystemMessageId sysString)
	{
		_objectId = objectId;
		_textType = messageType;
		_charId = charId;
		_npcString = sysString.getId();
	}
	
	public void addStringParameter(String text)
	{
		if (_parameters == null)
			_parameters = new ArrayList<>();
		
		_parameters.add(text);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4a);
		writeD(_objectId);
		writeD(_textType);
		
		if (_charName != null)
			writeS(_charName);
		else
			writeD(_charId);
		writeD(_npcString); // High Five NPCString ID
		if (_text != null)
			writeS(_text);
		else
		{
			if (_parameters != null)
			{
				for (String s : _parameters)
					writeS(s);
			}
		}
		
		L2PcInstance _pci = getClient().getActiveChar();
		if (_pci != null)
		{
			_pci.broadcastSnoop(_textType, _charName, _text);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__4A_CREATURESAY;
	}
}