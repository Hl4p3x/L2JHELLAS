package com.l2jhellas.loginserver;

import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.l2jhellas.loginserver.serverpackets.Init;
import com.l2jhellas.mmocore.network.IAcceptFilter;
import com.l2jhellas.mmocore.network.IClientFactory;
import com.l2jhellas.mmocore.network.IMMOExecutor;
import com.l2jhellas.mmocore.network.MMOConnection;
import com.l2jhellas.mmocore.network.ReceivablePacket;

public class SelectorHelper implements IMMOExecutor<L2LoginClient>, IClientFactory<L2LoginClient>, IAcceptFilter
{
	private final ThreadPoolExecutor _generalPacketsThreadPool;
	
	public SelectorHelper()
	{
		_generalPacketsThreadPool = new ThreadPoolExecutor(4, 6, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}
	
	@Override
	public void execute(ReceivablePacket<L2LoginClient> packet)
	{
		_generalPacketsThreadPool.execute(packet);
	}
	
	@Override
	public L2LoginClient create(MMOConnection<L2LoginClient> con)
	{
		L2LoginClient client = new L2LoginClient(con);
		client.sendPacket(new Init(client));
		return client;
	}
	
	@Override
	public boolean accept(SocketChannel sc)
	{
		return !LoginController.getInstance().isBannedAddress(sc.socket().getInetAddress());
	}
}