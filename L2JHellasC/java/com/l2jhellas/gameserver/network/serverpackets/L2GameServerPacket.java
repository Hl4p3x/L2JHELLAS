package com.l2jhellas.gameserver.network.serverpackets;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.network.L2GameClient;
import com.l2jhellas.mmocore.network.SendablePacket;

public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{
	protected static final Logger _log = Logger.getLogger(L2GameServerPacket.class.getName());
	
	@Override
	protected void write()
	{
		if (Config.DEBUG)
			_log.info(getType());
		try
		{
			writeImpl();
		}
		catch (Throwable t)
		{
			_log.severe("Client: " + getClient().toString() + " - Failed writing: " + getType());
			t.printStackTrace();
		}
	}
	
	public void runImpl()
	{
		
	}

	protected abstract void writeImpl();
	
	public String getType()
	{
		return "[S] " + getClass().getSimpleName();
	}
}