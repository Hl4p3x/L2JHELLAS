package com.l2jhellas.loginserver.clientpackets;

import java.util.logging.Logger;

import com.l2jhellas.loginserver.L2LoginClient;
import com.l2jhellas.mmocore.network.ReceivablePacket;

public abstract class L2LoginClientPacket extends ReceivablePacket<L2LoginClient>
{
	private static Logger _log = Logger.getLogger(L2LoginClientPacket.class.getName());
	
	@Override
	protected final boolean read()
	{
		try
		{
			return readImpl();
		}
		catch (Exception e)
		{
			_log.severe("ERROR READING: " + this.getClass().getSimpleName());
			e.printStackTrace();
			return false;
		}
	}
	
	protected abstract boolean readImpl();
}