package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.handler.IUserCommandHandler;
import com.l2jhellas.gameserver.handler.UserCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class RequestUserCommand extends L2GameClientPacket
{
	private static final String _C__AA_REQUESTUSERCOMMAND = "[C] aa RequestUserCommand";
	
	private int _command;
	
	@Override
	protected void readImpl()
	{
		_command = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		final IUserCommandHandler handler = UserCommandHandler.getInstance().getHandler(_command);	
		if (handler != null)
			handler.useUserCommand(_command, getClient().getActiveChar());
	}
	
	@Override
	public String getType()
	{
		return _C__AA_REQUESTUSERCOMMAND;
	}
}