package com.l2jhellas.gameserver.model;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class L2Request
{
	private static final int REQUEST_TIMEOUT = 15; // in secs
	
	protected L2PcInstance _player;
	protected L2PcInstance _partner;
	protected boolean _isRequestor;
	protected boolean _isAnswerer;
	protected L2GameClientPacket _requestPacket;
	
	public L2Request(L2PcInstance player)
	{
		_player = player;
	}
	
	protected void clear()
	{
		_partner = null;
		_requestPacket = null;
		_isRequestor = false;
		_isAnswerer = false;
	}
	
	private synchronized void setPartner(L2PcInstance partner)
	{
		_partner = partner;
	}
	
	public L2PcInstance getPartner()
	{
		return _partner;
	}
	
	private synchronized void setRequestPacket(L2GameClientPacket packet)
	{
		_requestPacket = packet;
	}
	
	public L2GameClientPacket getRequestPacket()
	{
		return _requestPacket;
	}
	
	public synchronized boolean setRequest(L2PcInstance partner, L2GameClientPacket packet)
	{
		if (partner == null)
		{
			_player.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return false;
		}
		if (partner.getRequest().isProcessingRequest())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
			sm.addString(partner.getName());
			_player.sendPacket(sm);
			sm = null;
			return false;
		}
		if (isProcessingRequest())
		{
			_player.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
			return false;
		}
		
		_partner = partner;
		_requestPacket = packet;
		setOnRequestTimer(true);
		_partner.getRequest().setPartner(_player);
		_partner.getRequest().setRequestPacket(packet);
		_partner.getRequest().setOnRequestTimer(false);
		return true;
	}
	
	private void setOnRequestTimer(boolean isRequestor)
	{
		_isRequestor = isRequestor ? true : false;
		_isAnswerer = isRequestor ? false : true;
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				clear();
			}
		}, REQUEST_TIMEOUT * 1000);
	}
	
	public void onRequestResponse()
	{
		if (_partner != null)
		{
			_partner.getRequest().clear();
		}
		clear();
	}
	
	public boolean isProcessingRequest()
	{
		return _partner != null;
	}
}