package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2ShortCut;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class ShortCutRegister extends L2GameServerPacket
{
	private static final String _S__56_SHORTCUTREGISTER = "[S] 44 ShortCutRegister";
	
	private final L2ShortCut _shortcut;
	private final L2PcInstance _activeChar;
	
	public ShortCutRegister(L2PcInstance activeChar, L2ShortCut sc)
	{
		_shortcut = sc;
		_activeChar = activeChar;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x44);
		
		writeD(_shortcut.getType());
		writeD(_shortcut.getSlot() + _shortcut.getPage() * 12); // C4 Client

		switch (_shortcut.getType())
		{
			case L2ShortCut.TYPE_ITEM:
				writeD(_shortcut.getId());
				writeD(_shortcut.getCharacterType());
				writeD(0x00); // SharedReuseGroup
				writeD(0x00); // Remaining time
				writeD(0x00); // Cooldown time			
				writeD(_activeChar.WriteAugmentation(_shortcut));
				break;			
			case  L2ShortCut.TYPE_SKILL:
				writeD(_shortcut.getId());
				writeD(_shortcut.getLevel());
				writeC(0x00);
				writeD(_shortcut.getCharacterType());
				break;			
			default:
				writeD(_shortcut.getId());
				writeD(_shortcut.getCharacterType());
				break;
		}
	}
	
	@Override
	public String getType()
	{
		return _S__56_SHORTCUTREGISTER;
	}
}