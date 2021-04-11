package com.l2jhellas.gameserver.network;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.LoginServerThread;
import com.l2jhellas.gameserver.LoginServerThread.SessionKey;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.model.CharSelectInfoPackage;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.events.engines.EventManager;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jhellas.gameserver.network.serverpackets.ServerClose;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.mmocore.network.MMOClient;
import com.l2jhellas.mmocore.network.MMOConnection;
import com.l2jhellas.mmocore.network.ReceivablePacket;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.util.database.L2DatabaseFactory;

public final class L2GameClient extends MMOClient<MMOConnection<L2GameClient>> implements Runnable
{
	protected static final Logger _log = Logger.getLogger(L2GameClient.class.getName());
	
	public static enum GameClientState
	{
		CONNECTED,
		AUTHED,
		ENTERING, // client is currently loading his Player instance, but didn't end
		IN_GAME
	}
	
	public GameClientState _state;
	
	private String _accountName;
	private SessionKey _sessionId;
	private L2PcInstance _activeChar;
	private final ReentrantLock _activeCharLock = new ReentrantLock();
	
	private boolean _isAuthedGG;
	private final long _connectionStartTime;
	private CharSelectInfoPackage[] _slots;
	
	protected final ScheduledFuture<?> _autoSaveInDB;
	protected ScheduledFuture<?> _cleanupTask = null;
	
	public GameCrypt _crypt;
	private final ClientStats _stats;
	
	private boolean _isDetached = false;
	
	private final ArrayBlockingQueue<ReceivablePacket<L2GameClient>> _packetQueue;
	private final ReentrantLock _queueLock = new ReentrantLock();
	private final long[] _floodProtectors = new long[FloodProtectors.FloodAction.VALUES_LENGTH];

	private static final String DELETE_CHAR_FRIENDS = "DELETE FROM character_friends WHERE char_id=? OR friend_id=?";
	private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE char_obj_id=?";
	private static final String DELETE_CHAR_MACROS = "DELETE FROM character_macroses WHERE char_obj_id=?";
	private static final String DELETE_CHAR_QUESTS = "DELETE FROM character_quests WHERE char_id=?";
	private static final String DELETE_CHAR_RECIPES = "DELETE FROM character_recipebook WHERE char_id=?";
	private static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE char_obj_id=?";
	private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE char_obj_id=?";
	private static final String DELETE_CHAR_SKILLS_SAVE = "DELETE FROM character_skills_save WHERE char_obj_id=?";
	private static final String DELETE_CHAR_SUBCLASSES = "DELETE FROM character_subclasses WHERE char_obj_id=?";
	private static final String DELETE_CHAR_HERO = "DELETE FROM heroes WHERE char_id=?";
	private static final String DELETE_CHAR_NOBLE = "DELETE FROM olympiad_nobles WHERE char_id=?";
	private static final String DELETE_CHAR_SEVEN_SIGNS = "DELETE FROM seven_signs WHERE char_obj_id=?";
	private static final String DELETE_CHAR_PETS = "DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)";
	private static final String DELETE_CHAR_AUGMENTS = "DELETE FROM augmentations WHERE item_id IN (SELECT object_id FROM items WHERE items.owner_id=?)";
	private static final String DELETE_CHAR_ITEMS = "DELETE FROM items WHERE owner_id=?";
	private static final String DELETE_CHAR_RBP = "DELETE FROM character_raid_points WHERE charId=?";
	private static final String DELETE_CHAR = "DELETE FROM characters WHERE obj_Id=?";

	public L2GameClient(MMOConnection<L2GameClient> con)
	{
		super(con);
		_state = GameClientState.CONNECTED;
		_connectionStartTime = System.currentTimeMillis();
		_crypt = new GameCrypt();
		_stats = new ClientStats();
		_packetQueue = new ArrayBlockingQueue<>(Config.CLIENT_PACKET_QUEUE_SIZE);
		
		_autoSaveInDB = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoSaveTask(), 300000L, 900000L);
	}
	
	public byte[] enableCrypt()
	{
		byte[] key = BlowFishKeygen.getRandomKey();
		_crypt.setKey(key);
		return key;
	}
	
	public GameClientState getState()
	{
		return _state;
	}
	
	public void setState(GameClientState pState)
	{
		if (_state != pState)
		{
			_state = pState;
			_packetQueue.clear();
		}
	}
	
	public ClientStats getStats()
	{
		return _stats;
	}
	
	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}
	
	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		_crypt.decrypt(buf.array(), buf.position(), size);
		return true;
	}
	
	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		_crypt.encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}
	
	public L2PcInstance getActiveChar()
	{
		return _activeChar;
	}
	
	public void setActiveChar(L2PcInstance pActiveChar)
	{
		_activeChar = pActiveChar;
	}
	
	public ReentrantLock getActiveCharLock()
	{
		return _activeCharLock;
	}
	
	public void setGameGuardOk(boolean val)
	{
		_isAuthedGG = val;
	}
	
	public boolean isAuthedGG()
	{
		return _isAuthedGG;
	}
	
	public void setAccountName(String pAccountName)
	{
		_accountName = pAccountName;
	}
	
	public String getAccountName()
	{
		return _accountName;
	}
	
	public void setSessionId(SessionKey sk)
	{
		_sessionId = sk;
	}
	
	public SessionKey getSessionId()
	{
		return _sessionId;
	}
	
	public void sendPacket(L2GameServerPacket gsp)
	{
		if (_isDetached || (gsp == null))
			return;
		
		getConnection().sendPacket(gsp);
		gsp.runImpl();
	}
	
	public boolean isDetached()
	{
		return _isDetached;
	}
	
	public void setDetached(boolean b)
	{
		_isDetached = b;
	}
	
	public byte markToDeleteChar(int charslot)
	{
		int objid = getObjectIdForSlot(charslot);
		
		if (objid < 0)
			return -1;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT clanId FROM characters WHERE obj_id=?");
			statement.setInt(1, objid);
			ResultSet rs = statement.executeQuery();
			
			rs.next();
			
			int clanId = rs.getInt(1);
			byte answer = 0;
			if (clanId != 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(clanId);
				
				if (clan == null)
					answer = 0;
				else if (clan.getLeaderId() == objid)
					answer = 2;
				else
					answer = 1;
			}
			
			rs.close();
			statement.close();
			
			if (answer == 0)
			{
				if (Config.DELETE_DAYS == 0)
					deleteCharByObjId(objid);
				else
				{
					statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_id=?");
					statement.setLong(1, System.currentTimeMillis() + Config.DELETE_DAYS * 86400000L);
					statement.setInt(2, objid);
					statement.execute();
					statement.close();
				}
			}
			
			return answer;
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error updating delete time of character.", e);
			return -1;
		}
	}
	
	public void markRestoredChar(int charslot)
	{
		final int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error restoring character.", e);
		}
	}
	
	public static void deleteCharByObjId(int objectId)
	{
		if (objectId < 0)
			return;
				
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_FRIENDS))
			{
				ps.setInt(1, objectId);
				ps.setInt(2, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_HENNAS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_MACROS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}

			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_QUESTS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_RECIPES))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_SHORTCUTS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_SKILLS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_SKILLS_SAVE))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_SUBCLASSES))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_HERO))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_NOBLE))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_SEVEN_SIGNS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_PETS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_AUGMENTS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_ITEMS))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR_RBP))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_CHAR))
			{
				ps.setInt(1, objectId);
				ps.execute();
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error deleting player.", e);
		}
	}
	
	public L2PcInstance loadCharFromDisk(int slot)
	{
		final int objectId = getObjectIdForSlot(slot);
		
		if (objectId < 0)
			return null;
		
		L2PcInstance player = L2World.getInstance().getPlayer(objectId);
		if (player != null)
		{			
			if (player.getClient() != null)
				player.getClient().closeNow();
			else
				player.deleteMe();
			
			return null;
		}
		
		player = L2PcInstance.restore(objectId);
		
		if (player != null)
		{
			player.setRunning();
			player.standUp();
			
			player.setOnlineStatus(true);
			
			player.refreshOverloaded();
			player.refreshExpertisePenalty();
			player.broadcastKarma();
		}
		else
			_log.severe("L2GameClient: could not restore in slot: " + slot);
		
		return player;
	}
	
	public CharSelectInfoPackage getCharSelectSlot(int id)
	{
		if (_slots == null || id < 0 || id >= _slots.length)
			return null;
		
		return _slots[id];
	}
	
	public void setCharSelectSlot(CharSelectInfoPackage[] list)
	{
		_slots = list;
	}
	
	public void close(L2GameServerPacket gsp)
	{
		getConnection().close(gsp);
	}
	
	private int getObjectIdForSlot(int charslot)
	{
		final CharSelectInfoPackage info = getCharSelectSlot(charslot);
		if (info == null)
		{
			_log.warning(toString() + " tried to delete Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}
		return info.getObjectId();
	}
	
	@Override
	protected void onForcedDisconnection()
	{
		_log.fine("Client " + toString() + " disconnected abnormally.");
	}
	
	@Override
	protected void onDisconnection()
	{
		try
		{
			ThreadPoolManager.getInstance().executeTask(new DisconnectTask());
		}
		catch (RejectedExecutionException e)
		{
		}
	}
	
	public void closeNow()
	{
		_isDetached = true;
		close(ServerClose.STATIC_PACKET);
		synchronized (this)
		{
			if (_cleanupTask != null)
				cancelCleanup();
			
			_cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), 0); // instant
		}
	}
	
	public void closeNow(boolean wrongProtocol)
	{
		_isDetached = true;
		close(wrongProtocol ? SystemMessage.getSystemMessage(SystemMessageId.WRONG_PROTOCOL_CONTINUE) : ServerClose.STATIC_PACKET);
		synchronized (this)
		{
			if (_cleanupTask != null)
				cancelCleanup();
			
			_cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), 0); // instant
		}
	}
	
	@Override
	public String toString()
	{
		try
		{
			final InetAddress address = getConnection().getInetAddress();
			switch (getState())
			{
				case CONNECTED:
					return "[IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				case AUTHED:
					return "[Account: " + getAccountName() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				case ENTERING:
				case IN_GAME:
					return "[Character: " + (getActiveChar() == null ? "disconnected" : getActiveChar().getName()) + " - Account: " + getAccountName() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
				default:
					throw new IllegalStateException("Missing state on switch");
			}
		}
		catch (NullPointerException e)
		{
			return "[Character read failed due to disconnect]";
		}
	}
	
	protected class DisconnectTask implements Runnable
	{
		
		@Override
		public void run()
		{
			boolean fast = true;
			
			try
			{
				if (getActiveChar() != null && !isDetached())
				{
					setDetached(true);
					fast = !getActiveChar().isInCombat() && !getActiveChar().isLocked();
				}
				cleanMe(fast);
			}
			catch (Exception e1)
			{
				_log.log(Level.WARNING, "error while disconnecting client", e1);
			}
		}
	}
	
	public void cleanMe(boolean fast)
	{
		try
		{
			synchronized (this)
			{
				if (_cleanupTask == null)
					_cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), fast ? 5 : 15000L);
			}
		}
		catch (Exception e1)
		{
			_log.log(Level.WARNING, "Error during cleanup.", e1);
		}
	}
	
	protected class CleanupTask implements Runnable
	{
		
		@Override
		public void run()
		{
			try
			{
				if (_autoSaveInDB != null)
					_autoSaveInDB.cancel(true);
				
				if (getActiveChar() != null)
				{
					if (getActiveChar().isRegisteredInFunEvent())
					{
						if(getActiveChar().isInFunEvent())
						   EventManager.getInstance().getCurrentEvent().onLogout(getActiveChar());
						else
						   EventManager.getInstance().onLogout(getActiveChar());
					}
					
					if (getActiveChar().isFlying())
						getActiveChar().removeSkill(SkillTable.getInstance().getInfo(4289, 1));
					
					getActiveChar().setClient(null);
					
					if (getActiveChar().isOnline() == 1)
						getActiveChar().deleteMe();
				}
				setActiveChar(null);
			}
			catch (Exception e1)
			{
				_log.log(Level.WARNING, "Error while cleanup client.", e1);
			}
			finally
			{
				LoginServerThread.getInstance().sendLogout(getAccountName());
			}
		}
	}
	
	protected class AutoSaveTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (getActiveChar() != null && getActiveChar().isOnline() == 1)
				{
					getActiveChar().store();
					
					if (getActiveChar().getPet() != null)
						getActiveChar().getPet().store();
				}
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Error on AutoSaveTask.", e);
			}
		}
	}
	
	public boolean dropPacket()
	{
		if (_isDetached)
			return true;
		
		if (getStats().countPacket(_packetQueue.size()))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		
		return getStats().dropPacket();
	}
	
	public void onBufferUnderflow()
	{
		if (getStats().countUnderflowException())
		{
			_log.severe("Client " + toString() + " - Disconnected: Too many buffer underflow exceptions.");
			closeNow();
			return;
		}
		
		if (_state == GameClientState.CONNECTED)
			closeNow();		
	}
	
	public void onUnknownPacket()
	{
		if (getStats().countUnknownPacket())
		{
			_log.severe("Client " + toString() + " - Disconnected: Too many unknown packets.");
			closeNow();
			return;
		}
		if (_state == GameClientState.CONNECTED)
		{
			_log.severe("Client " + toString() + " - Disconnected, too many unknown packets in non-authed state.");
			closeNow();
		}
	}
	
	public void execute(ReceivablePacket<L2GameClient> packet)
	{
		if (getStats().countFloods())
		{
			_log.severe("Client " + toString() + " - Disconnected, too many floods:" + getStats().longFloods + " long and " + getStats().shortFloods + " short.");
			closeNow();
			return;
		}
		
		if (!_packetQueue.offer(packet))
		{
			if (getStats().countQueueOverflow())
			{
				_log.severe("Client " + toString() + " - Disconnected, too many queue overflows.");
				closeNow();
			}
			else
				sendPacket(ActionFailed.STATIC_PACKET);
			
			return;
		}
		
		if (_queueLock.isLocked())
			return;
		
		try
		{
			if (_state == GameClientState.CONNECTED && getStats().processedPackets > 3)
			{
				_log.severe("Client " + toString() + " - Disconnected, too many packets in non-authed state.");
				
				closeNow();
				return;
			}
			
			ThreadPoolManager.getInstance().executeTask(this);
		}
		catch (RejectedExecutionException e)
		{
		}
	}
	
	@Override
	public void run()
	{
		if (!_queueLock.tryLock())
			return;
		
		try
		{
			int count = 0;
			ReceivablePacket<L2GameClient> packet;
			while (true)
			{
				packet = _packetQueue.poll();
				
				if (packet == null)
					return;
				
				if (_isDetached)
				{
					_packetQueue.clear();
					return;
				}
				
				try
				{
					packet.run();
				}
				catch (Exception e)
				{
					_log.severe("Exception during execution " + packet.getClass().getSimpleName() + ", client: " + toString() + "," + e.getMessage());
				}
				
				count++;
				if (getStats().countBurst(count))
					return;
			}
		}
		finally
		{
			_queueLock.unlock();
		}
	}
	
	private boolean cancelCleanup()
	{
		final Future<?> task = _cleanupTask;
		if (task != null)
		{
			_cleanupTask = null;
			return task.cancel(true);
		}
		return false;
	}
	
	public long[] getFloodProtectors()
	{
		return _floodProtectors;
	}
}