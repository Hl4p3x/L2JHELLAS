package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2Macro;
import com.l2jhellas.gameserver.model.L2Macro.L2MacroCmd;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;

public final class RequestMakeMacro extends L2GameClientPacket
{
	private static final String _C__C1_REQUESTMAKEMACRO = "[C] C1 RequestMakeMacro";
	private static final int MAX_MACRO_LENGTH = 12;
	private L2Macro _macro;
	private int _commandsLenght = 0;
	
	@Override
	protected void readImpl()
	{
		int _id = readD();
		String _name = readS();
		String _desc = readS();
		String _acronym = readS();
		int _icon = readC();
		int _count = readC();
		
		if (_count > MAX_MACRO_LENGTH)
			_count = MAX_MACRO_LENGTH;
		
		L2MacroCmd[] commands = new L2MacroCmd[_count];

		for (int i = 0; i < _count; i++)
		{
			int entry = readC();
			int type = readC(); // 1 = skill, 3 = action, 4 = shortcut
			int d1 = readD(); // skill or page number for shortcuts
			int d2 = readC();
			String command = readS();
			
			_commandsLenght += command.length();
			commands[i] = new L2MacroCmd(entry, type, d1, d2, command);			
		}
		
		_macro = new L2Macro(_id, _icon, _name, _desc, _acronym, commands);
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		if (_commandsLenght > 255)
		{
			// Invalid macro. Refer to the Help file for instructions.
			player.sendPacket(SystemMessageId.INVALID_MACRO);
			return;
		}
		if (player.getMacroses().getAllMacroses().length > 24)
		{
			// You may create up to 24 macros.
			player.sendPacket(SystemMessageId.YOU_MAY_CREATE_UP_TO_24_MACROS);
			return;
		}
		if (_macro.name.length() == 0)
		{
			// Enter the name of the macro.
			player.sendPacket(SystemMessageId.ENTER_THE_MACRO_NAME);
			return;
		}
		if (_macro.descr.length() > 32)
		{
			// Macro descriptions may contain up to 32 characters.
			player.sendPacket(SystemMessageId.MACRO_DESCRIPTION_MAX_32_CHARS);
			return;
		}
		player.registerMacro(_macro);
	}
	
	@Override
	public String getType()
	{
		return _C__C1_REQUESTMAKEMACRO;
	}
}