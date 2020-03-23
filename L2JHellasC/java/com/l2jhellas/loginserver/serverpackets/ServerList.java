package com.l2jhellas.loginserver.serverpackets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.loginserver.GameServerTable;
import com.l2jhellas.loginserver.GameServerTable.GameServerInfo;
import com.l2jhellas.loginserver.L2LoginClient;
import com.l2jhellas.loginserver.gameserverpackets.ServerStatus;

public final class ServerList extends L2LoginServerPacket
{
	private final List<ServerData> _servers;
	private final int _lastServer;
	
	class ServerData
	{
		protected String _ip;
		protected int _port;
		protected boolean _pvp;
		protected int _currentPlayers;
		protected int _maxPlayers;
		protected boolean _testServer;
		protected boolean _brackets;
		protected boolean _clock;
		protected int _status;
		protected int _serverId;
		protected final int _ageLimit;
		
		ServerData(String pIp, int pPort, boolean pPvp, boolean pTestServer, int pCurrentPlayers, int pMaxPlayers, boolean pBrackets, boolean pClock, int pStatus, int pServer_id, int ageLimit)
		{
			_ip = pIp;
			_port = pPort;
			_pvp = pPvp;
			_testServer = pTestServer;
			_currentPlayers = pCurrentPlayers;
			_maxPlayers = pMaxPlayers;
			_brackets = pBrackets;
			_clock = pClock;
			_status = pStatus;
			_serverId = pServer_id;
			_ageLimit = ageLimit;
		}
	}
	
	public ServerList(L2LoginClient client)
	{
		_servers = new ArrayList<>();
		_lastServer = client.getLastServer();
		for (GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values())
		{
			if (gsi.getStatus() == ServerStatus.STATUS_GM_ONLY && client.getAccessLevel() > 0)
			{
				// Server is GM-Only but you've got GM Status
				addServer(client.usesInternalIP() ? gsi.getInternalHost() : gsi.getExternalHost(), gsi.getPort(), gsi.isPvp(), gsi.isTestServer(), gsi.getCurrentPlayerCount(), gsi.getMaxPlayers(), gsi.isShowingBrackets(), gsi.isShowingClock(), gsi.getStatus(), gsi.getId(), gsi.getAgeLimit());
			}
			else if (gsi.getStatus() != ServerStatus.STATUS_GM_ONLY)
			{
				// Server is not GM-Only
				addServer(client.usesInternalIP() ? gsi.getInternalHost() : gsi.getExternalHost(), gsi.getPort(), gsi.isPvp(), gsi.isTestServer(), gsi.getCurrentPlayerCount(), gsi.getMaxPlayers(), gsi.isShowingBrackets(), gsi.isShowingClock(), gsi.getStatus(), gsi.getId(), gsi.getAgeLimit());
			}
			else
			{
				// Server's GM-Only and you've got no GM-Status
				addServer(client.usesInternalIP() ? gsi.getInternalHost() : gsi.getExternalHost(), gsi.getPort(), gsi.isPvp(), gsi.isTestServer(), gsi.getCurrentPlayerCount(), gsi.getMaxPlayers(), gsi.isShowingBrackets(), gsi.isShowingClock(), ServerStatus.STATUS_DOWN, gsi.getId(), gsi.getAgeLimit());
			}
		}
	}
	
	public void addServer(String ip, int port, boolean pvp, boolean testServer, int currentPlayer, int maxPlayer, boolean brackets, boolean clock, int status, int server_id, int agelimit)
	{
		_servers.add(new ServerData(ip, port, pvp, testServer, currentPlayer, maxPlayer, brackets, clock, status, server_id, agelimit));
	}
	
	@Override
	public void write()
	{
		writeC(0x04);
		writeC(_servers.size());
		writeC(_lastServer);
		
		for (ServerData server : _servers)
		{
			writeC(server._serverId);
			
			try
			{
				final byte[] raw = InetAddress.getByName(server._ip).getAddress();
				writeC(raw[0] & 0xff);
				writeC(raw[1] & 0xff);
				writeC(raw[2] & 0xff);
				writeC(raw[3] & 0xff);
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
				writeC(127);
				writeC(0);
				writeC(0);
				writeC(1);
			}
			
			writeD(server._port);
			writeC(server._ageLimit);
			writeC(server._pvp ? 0x01 : 0x00);
			writeH(server._currentPlayers);
			writeH(server._maxPlayers);
			writeC(server._status == ServerStatus.STATUS_DOWN ? 0x00 : 0x01);
			
			int bits = 0;
			if (server._testServer)
				bits |= 0x04;
			
			if (server._clock)
				bits |= 0x02;
			
			writeD(bits);
			writeC(server._brackets ? 0x01 : 0x00);
		}
	}
}