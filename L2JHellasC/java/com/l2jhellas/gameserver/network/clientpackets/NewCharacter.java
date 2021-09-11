package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.datatables.xml.PlayerDataTemplate;
import com.l2jhellas.gameserver.enums.player.ClassId;
import com.l2jhellas.gameserver.network.serverpackets.CharTemplates;

public final class NewCharacter extends L2GameClientPacket
{
	private static final String _C__0E_NEWCHARACTER = "[C] 0E NewCharacter";
	
	@Override
	protected void readImpl()
	{
		
	}
	
	@Override
	protected void runImpl()
	{
		CharTemplates ct = new CharTemplates();
		
		ct.addChar(PlayerDataTemplate.getInstance().getTemplate(0));
		ct.addChar(PlayerDataTemplate.getInstance().getTemplate(ClassId.HUMAN_FIGHTER));
		ct.addChar(PlayerDataTemplate.getInstance().getTemplate(ClassId.HUMAN_MYSTIC));
		ct.addChar(PlayerDataTemplate.getInstance().getTemplate(ClassId.ELVEN_FIGHTER));
		ct.addChar(PlayerDataTemplate.getInstance().getTemplate(ClassId.ELVEN_MYSTIC));
		ct.addChar(PlayerDataTemplate.getInstance().getTemplate(ClassId.DARK_FIGHTER));
		ct.addChar(PlayerDataTemplate.getInstance().getTemplate(ClassId.DARK_MYSTIC));
		ct.addChar(PlayerDataTemplate.getInstance().getTemplate(ClassId.ORC_FIGHTER));
		ct.addChar(PlayerDataTemplate.getInstance().getTemplate(ClassId.ORC_MYSTIC));
		ct.addChar(PlayerDataTemplate.getInstance().getTemplate(ClassId.DWARVEN_FIGHTER));
		
		sendPacket(ct);
	}
	
	@Override
	public String getType()
	{
		return _C__0E_NEWCHARACTER;
	}
}