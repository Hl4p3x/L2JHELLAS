package com.l2jhellas.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
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
		if (Config.DEBUG)
			_log.config(L2GameClientPacket.class.getName() + ": packet: " + getType());
		try
		{
			readImpl();
			return true;
		}
		catch (Exception t)
		{
			_log.warning(L2GameClientPacket.class.getName() + ": Client: " + getClient().toString() + " - Failed reading: " + getType() + " packet - l2jhellas Server Version: " + Config.SERVER_VERSION);
			
			if (t instanceof BufferUnderflowException)
				getClient().onBufferUnderflow();
			
			if (Config.DEVELOPER)
				t.printStackTrace();
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
			if (this instanceof MoveBackwardToLocation || this instanceof AttackRequest || this instanceof RequestMagicSkillUse)
			{
				final L2PcInstance player = getClient().getActiveChar();
				if (player != null && player.isSpawnProtected())
					player.onActionRequest();
			}
		}
		catch (Throwable t)
		{
			_log.warning(L2GameClientPacket.class.getName() + ": Client: " + getClient().toString() + " - Failed running: " + getType() + " - l2jhellas Server Version: " + Config.SERVER_VERSION);
			if (Config.DEVELOPER)
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