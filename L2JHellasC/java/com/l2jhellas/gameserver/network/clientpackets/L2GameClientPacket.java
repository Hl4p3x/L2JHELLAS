package com.l2jhellas.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.network.L2GameClient;
import com.l2jhellas.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jhellas.mmocore.network.ReceivablePacket;

public abstract class L2GameClientPacket extends ReceivablePacket<L2GameClient>
{
	protected static final Logger _log = Logger.getLogger(L2GameClientPacket.class.getName());
	
	public abstract String getType();
	
	protected int _opcode = 0;
	
	@Override
	protected boolean read()
	{
        if(getClient() == null)
        	return false;
        
		try
		{
			readImpl();
			return true;
		}
		catch (Exception t)
		{
			_log.warning(L2GameClientPacket.class.getName() + ": Client: " + getClient().toString() + " - Failed reading: " + getType() );
			t.printStackTrace();
			
			if (t instanceof BufferUnderflowException)
				getClient().onBufferUnderflow();
			
				
		}
		return false;
	}
	
	protected abstract void readImpl();
	
	@Override
	public void run()
	{
		try
		{
			runImpl();
		}
		catch (Throwable t)
		{
			_log.warning(L2GameClientPacket.class.getName() + ": Client: " + getClient().toString() + " - Failed running: " + getType());
			t.printStackTrace();
			
			if (this instanceof EnterWorld)
				getClient().closeNow();
		}
	}
	
	protected abstract void runImpl();
	
	protected final void sendPacket(L2GameServerPacket gsp)
	{
		getClient().sendPacket(gsp);
	}
}