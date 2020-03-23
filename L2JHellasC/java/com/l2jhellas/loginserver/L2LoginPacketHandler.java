package com.l2jhellas.loginserver;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import com.l2jhellas.loginserver.L2LoginClient.LoginClientState;
import com.l2jhellas.loginserver.clientpackets.AuthGameGuard;
import com.l2jhellas.loginserver.clientpackets.RequestAuthLogin;
import com.l2jhellas.loginserver.clientpackets.RequestServerList;
import com.l2jhellas.loginserver.clientpackets.RequestServerLogin;
import com.l2jhellas.mmocore.network.IPacketHandler;
import com.l2jhellas.mmocore.network.ReceivablePacket;

public final class L2LoginPacketHandler implements IPacketHandler<L2LoginClient>
{
	private static final Logger _log = Logger.getLogger(L2LoginPacketHandler.class.getName());
	
	@Override
	public ReceivablePacket<L2LoginClient> handlePacket(ByteBuffer buf, L2LoginClient client)
	{
		int opcode = buf.get() & 0xFF;
		
		ReceivablePacket<L2LoginClient> packet = null;
		LoginClientState state = client.getState();
		
		switch (state)
		{
			case CONNECTED:
				if (opcode == 0x07)
					packet = new AuthGameGuard();
				else
					debugOpcode(opcode, state);
				break;
			case AUTHED_GG:
				if (opcode == 0x00)
					packet = new RequestAuthLogin();
				else
					debugOpcode(opcode, state);
				break;
			case AUTHED_LOGIN:
				if (opcode == 0x05)
					packet = new RequestServerList();
				else if (opcode == 0x02)
					packet = new RequestServerLogin();
				else
					debugOpcode(opcode, state);
				break;
		}
		return packet;
	}
	
	private static void debugOpcode(int opcode, LoginClientState state)
	{
		_log.warning(L2LoginPacketHandler.class.getName() + ": Unknown Opcode: " + opcode + " for state: " + state.name());
	}
}