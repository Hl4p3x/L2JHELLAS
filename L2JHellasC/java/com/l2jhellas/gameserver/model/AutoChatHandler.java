package com.l2jhellas.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.SevenSigns;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class AutoChatHandler
{
	protected static final Logger _log = Logger.getLogger(AutoChatHandler.class.getName());
	private static AutoChatHandler _instance;
	
	private static final long DEFAULT_CHAT_DELAY = 30000; // 30 secs by default
	
	protected Map<Integer, AutoChatInstance> _registeredChats;
	
	protected AutoChatHandler()
	{
		_registeredChats = new HashMap<>();
		restoreChatData();
	}
	
	private void restoreChatData()
	{
		int numLoaded = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM auto_chat ORDER BY groupId ASC");
			ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				numLoaded++;
				
				PreparedStatement statement2 = con.prepareStatement("SELECT * FROM auto_chat_text WHERE groupId=?");
				statement2.setInt(1, rs.getInt("groupId"));
				ResultSet rs2 = statement2.executeQuery();
				
				rs2.last();
				String[] chatTexts = new String[rs2.getRow()];
				int i = 0;
				rs2.first();
				
				while (rs2.next())
				{
					chatTexts[i] = rs2.getString("chatText");
					i++;
				}
				
				registerGlobalChat(rs.getInt("npcId"), chatTexts, rs.getLong("chatDelay"));
				
				rs2.close();
				statement2.close();
			}
			
			rs.close();
			statement.close();
			
			_log.info(AutoChatHandler.class.getSimpleName() + ":  Loaded " + numLoaded + " chat group(s) from the database.");
		}
		catch (Exception e)
		{
			_log.warning(AutoChatHandler.class.getName() + ":  Could not restore chat data: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public static AutoChatHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new AutoChatHandler();
		}
		
		return _instance;
	}
	
	public int size()
	{
		return _registeredChats.size();
	}
	
	public AutoChatInstance registerGlobalChat(int npcId, String[] chatTexts, long chatDelay)
	{
		return registerChat(npcId, null, chatTexts, chatDelay);
	}
	
	public AutoChatInstance registerChat(L2Npc npcInst, String[] chatTexts, long chatDelay)
	{
		return registerChat(npcInst.getNpcId(), npcInst, chatTexts, chatDelay);
	}
	
	private final AutoChatInstance registerChat(int npcId, L2Npc npcInst, String[] chatTexts, long chatDelay)
	{
		AutoChatInstance chatInst = null;
		
		if (chatDelay < 0)
		{
			chatDelay = DEFAULT_CHAT_DELAY;
		}
		
		if (_registeredChats.containsKey(npcId))
		{
			chatInst = _registeredChats.get(npcId);
		}
		else
		{
			chatInst = new AutoChatInstance(npcId, chatTexts, chatDelay, (npcInst == null));
		}
		
		if (npcInst != null)
		{
			chatInst.addChatDefinition(npcInst);
		}
		
		_registeredChats.put(npcId, chatInst);
		
		return chatInst;
	}
	
	public boolean removeChat(int npcId)
	{
		AutoChatInstance chatInst = _registeredChats.get(npcId);
		
		return removeChat(chatInst);
	}
	
	public boolean removeChat(AutoChatInstance chatInst)
	{
		if (chatInst == null)
			return false;
		
		_registeredChats.values().remove(chatInst);
		chatInst.setActive(false);
		
		if (Config.DEBUG)
		{
			_log.config(AutoChatHandler.class.getName() + ": Removed auto chat for NPC ID " + chatInst.getNPCId());
		}
		
		return true;
	}
	
	public AutoChatInstance getAutoChatInstance(int id, boolean byObjectId)
	{
		if (!byObjectId)
			return _registeredChats.get(id);
		
		for (AutoChatInstance chatInst : _registeredChats.values())
			if (chatInst.getChatDefinition(id) != null)
				return chatInst;
		
		return null;
	}
	
	public void setAutoChatActive(boolean isActive)
	{
		for (AutoChatInstance chatInst : _registeredChats.values())
		{
			chatInst.setActive(isActive);
		}
	}
	
	public void npcSpawned(L2Npc npc)
	{
		if (npc == null)
			return;
			
		int npcId = npc.getNpcId();
		
		if (_registeredChats.containsKey(npcId))
		{
			AutoChatInstance chatInst = _registeredChats.get(npcId);
				
			if (chatInst != null && chatInst.isGlobal())
					chatInst.addChatDefinition(npc);
		}
	}
	
	public class AutoChatInstance
	{
		protected int _npcId;
		private long _defaultDelay = DEFAULT_CHAT_DELAY;
		private String[] _defaultTexts;
		private boolean _defaultRandom = false;
		
		private boolean _globalChat = false;
		private boolean _isActive;
		
		private final Map<Integer, AutoChatDefinition> _chatDefinitions = new HashMap<>();
		protected ScheduledFuture<?> _chatTask;
		
		protected AutoChatInstance(int npcId, String[] chatTexts, long chatDelay, boolean isGlobal)
		{
			_defaultTexts = chatTexts;
			_npcId = npcId;
			_defaultDelay = chatDelay;
			_globalChat = isGlobal;
			
			if (Config.DEBUG)
			{
				_log.config(AutoChatHandler.class.getName() + ": Registered auto chat for NPC ID " + _npcId + " (Global Chat = " + _globalChat + ").");
			}
			
			setActive(true);
		}
		
		protected AutoChatDefinition getChatDefinition(int objectId)
		{
			return _chatDefinitions.get(objectId);
		}
		
		protected AutoChatDefinition[] getChatDefinitions()
		{
			return _chatDefinitions.values().toArray(new AutoChatDefinition[_chatDefinitions.values().size()]);
		}
		
		public int addChatDefinition(L2Npc npcInst)
		{
			return addChatDefinition(npcInst, null, 0);
		}
		
		public int addChatDefinition(L2Npc npcInst, String[] chatTexts, long chatDelay)
		{
			int objectId = npcInst.getObjectId();
			AutoChatDefinition chatDef = new AutoChatDefinition(this, npcInst, chatTexts, chatDelay);
			if (npcInst instanceof L2SiegeGuardInstance)
			{
				chatDef.setRandomChat(true);
			}
			_chatDefinitions.put(objectId, chatDef);
			return objectId;
		}
		
		public boolean removeChatDefinition(int objectId)
		{
			if (!_chatDefinitions.containsKey(objectId))
				return false;
			
			AutoChatDefinition chatDefinition = _chatDefinitions.get(objectId);
			chatDefinition.setActive(false);
			
			_chatDefinitions.remove(objectId);
			
			return true;
		}
		
		public boolean isActive()
		{
			return _isActive;
		}
		
		public boolean isGlobal()
		{
			return _globalChat;
		}
		
		public boolean isDefaultRandom()
		{
			return _defaultRandom;
		}
		
		public boolean isRandomChat(int objectId)
		{
			if (!_chatDefinitions.containsKey(objectId))
				return false;
			
			return _chatDefinitions.get(objectId).isRandomChat();
		}
		
		public int getNPCId()
		{
			return _npcId;
		}
		
		public int getDefinitionCount()
		{
			return _chatDefinitions.size();
		}
		
		public L2Npc[] getNPCInstanceList()
		{
			List<L2Npc> npcInsts = new ArrayList<>();
			
			for (AutoChatDefinition chatDefinition : _chatDefinitions.values())
			{
				npcInsts.add(chatDefinition._npcInstance);
			}
			
			return npcInsts.toArray(new L2Npc[npcInsts.size()]);
		}
		
		public long getDefaultDelay()
		{
			return _defaultDelay;
		}
		
		public String[] getDefaultTexts()
		{
			return _defaultTexts;
		}
		
		public void setDefaultChatDelay(long delayValue)
		{
			_defaultDelay = delayValue;
		}
		
		public void setDefaultChatTexts(String[] textsValue)
		{
			_defaultTexts = textsValue;
		}
		
		public void setDefaultRandom(boolean randValue)
		{
			_defaultRandom = randValue;
		}
		
		public void setChatDelay(int objectId, long delayValue)
		{
			AutoChatDefinition chatDef = getChatDefinition(objectId);
			
			if (chatDef != null)
			{
				chatDef.setChatDelay(delayValue);
			}
		}
		
		public void setChatTexts(int objectId, String[] textsValue)
		{
			AutoChatDefinition chatDef = getChatDefinition(objectId);
			
			if (chatDef != null)
			{
				chatDef.setChatTexts(textsValue);
			}
		}
		
		public void setRandomChat(int objectId, boolean randValue)
		{
			AutoChatDefinition chatDef = getChatDefinition(objectId);
			
			if (chatDef != null)
			{
				chatDef.setRandomChat(randValue);
			}
		}
		
		public void setActive(boolean activeValue)
		{
			if (_isActive == activeValue)
				return;
			
			_isActive = activeValue;
			
			if (!isGlobal())
			{
				for (AutoChatDefinition chatDefinition : _chatDefinitions.values())
				{
					chatDefinition.setActive(activeValue);
				}
				
				return;
			}
			
			if (isActive())
			{
				AutoChatRunner acr = new AutoChatRunner(_npcId, -1);
				_chatTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(acr, _defaultDelay, _defaultDelay);
			}
			else
			{
				_chatTask.cancel(false);
			}
		}
		
		private class AutoChatDefinition
		{
			protected int _chatIndex = 0;
			protected L2Npc _npcInstance;
			
			protected AutoChatInstance _chatInstance;
			
			private long _chatDelay = 0;
			private String[] _chatTexts = null;
			private boolean _isActiveDefinition;
			private boolean _randomChat;
			
			protected AutoChatDefinition(AutoChatInstance chatInst, L2Npc npcInst, String[] chatTexts, long chatDelay)
			{
				_npcInstance = npcInst;
				
				_chatInstance = chatInst;
				_randomChat = chatInst.isDefaultRandom();
				
				_chatDelay = chatDelay;
				_chatTexts = chatTexts;
				
				if (Config.DEBUG)
				{
					_log.info(AutoChatHandler.class.getName() + ":  Chat definition added for NPC ID " + _npcInstance.getNpcId() + " (Object ID = " + _npcInstance.getObjectId() + ").");
				}
				
				// If global chat isn't enabled for the parent instance,
				// then handle the chat task locally.
				if (!chatInst.isGlobal())
				{
					setActive(true);
				}
			}
			
			protected String[] getChatTexts()
			{
				return _chatTexts != null ? _chatTexts: _chatInstance.getDefaultTexts();
			}
			
			private long getChatDelay()
			{
				return _chatDelay > 0 ? _chatDelay : _chatInstance.getDefaultDelay();
			}
			
			private boolean isActive()
			{
				return _isActiveDefinition;
			}
			
			boolean isRandomChat()
			{
				return _randomChat;
			}
			
			void setRandomChat(boolean randValue)
			{
				_randomChat = randValue;
			}
			
			void setChatDelay(long delayValue)
			{
				_chatDelay = delayValue;
			}
			
			void setChatTexts(String[] textsValue)
			{
				_chatTexts = textsValue;
			}
			
			void setActive(boolean activeValue)
			{
				if (isActive() == activeValue)
					return;
				
				if (activeValue)
				{
					AutoChatRunner acr = new AutoChatRunner(_npcId, _npcInstance.getObjectId());
					if (getChatDelay() == 0)
					{
						// Schedule it set to 5Ms, isn't error, if use 0 sometine
						// chatDefinition return null in AutoChatRunner
						_chatTask = ThreadPoolManager.getInstance().scheduleGeneral(acr, 5);
					}
					else
					{
						_chatTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(acr, getChatDelay(), getChatDelay());
					}
				}
				else
				{
					_chatTask.cancel(false);
				}
				
				_isActiveDefinition = activeValue;
			}
		}
		
		private class AutoChatRunner implements Runnable
		{
			private final int _runnerNpcId;
			private final int _objectId;
			
			protected AutoChatRunner(int pNpcId, int pObjectId)
			{
				_runnerNpcId = pNpcId;
				_objectId = pObjectId;
			}
			
			@Override
			public synchronized void run()
			{
				AutoChatInstance chatInst = _registeredChats.get(_runnerNpcId);
				AutoChatDefinition[] chatDefinitions;
				
				if (chatInst.isGlobal())
				{
					chatDefinitions = chatInst.getChatDefinitions();
				}
				else
				{
					AutoChatDefinition chatDef = chatInst.getChatDefinition(_objectId);
					
					if (chatDef == null)
					{
						_log.warning(AutoChatHandler.class.getName() + ": Auto chat definition is NULL for NPC ID " + _npcId + ".");
						return;
					}
					
					chatDefinitions = new AutoChatDefinition[]
					{
						chatDef
					};
				}
				
				if (Config.DEBUG)
				{
					_log.info(AutoChatHandler.class.getName() + ": Running auto chat for " + chatDefinitions.length + " instances of NPC ID " + _npcId + "." + " (Global Chat = " + chatInst.isGlobal() + ")");
				}
				
				for (AutoChatDefinition chatDef : chatDefinitions)
				{
					try
					{
						L2Npc chatNpc = chatDef._npcInstance;
						List<L2PcInstance> nearbyPlayers = new ArrayList<>();
						List<L2PcInstance> nearbyGMs = new ArrayList<>();
						
						for (L2PcInstance player : L2World.getInstance().getVisibleObjects(chatNpc, L2PcInstance.class, 1500))
						{
							if (player.isGM())
							{
								nearbyGMs.add(player);
							}
							else
							{
								nearbyPlayers.add(player);
							}
						}
						
						int maxIndex = chatDef.getChatTexts().length;
						int lastIndex = Rnd.nextInt(maxIndex);
						
						String creatureName = chatNpc.getName();
						String text;
						
						if (!chatDef.isRandomChat())
						{
							lastIndex = chatDef._chatIndex;
							lastIndex++;
							
							if (lastIndex == maxIndex)
							{
								lastIndex = 0;
							}
							
							chatDef._chatIndex = lastIndex;
						}
						
						text = chatDef.getChatTexts()[lastIndex];
						
						if (text == null)
							return;
						
						if (!nearbyPlayers.isEmpty())
						{
							int randomPlayerIndex = Rnd.nextInt(nearbyPlayers.size());
							
							L2PcInstance randomPlayer = nearbyPlayers.get(randomPlayerIndex);
							
							final int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
							int losingCabal = SevenSigns.CABAL_NULL;
							
							if (winningCabal == SevenSigns.CABAL_DAWN)
							{
								losingCabal = SevenSigns.CABAL_DUSK;
							}
							else if (winningCabal == SevenSigns.CABAL_DUSK)
							{
								losingCabal = SevenSigns.CABAL_DAWN;
							}
							
							if (text.indexOf("%player_random%") > -1)
							{
								text = text.replaceAll("%player_random%", randomPlayer.getName());
							}
							
							if (text.indexOf("%player_cabal_winner%") > -1)
							{
								for (L2PcInstance nearbyPlayer : nearbyPlayers)
								{
									if (SevenSigns.getInstance().getPlayerCabal(nearbyPlayer) == winningCabal)
									{
										text = text.replaceAll("%player_cabal_winner%", nearbyPlayer.getName());
										break;
									}
								}
							}
							
							if (text.indexOf("%player_cabal_loser%") > -1)
							{
								for (L2PcInstance nearbyPlayer : nearbyPlayers)
								{
									if (SevenSigns.getInstance().getPlayerCabal(nearbyPlayer) == losingCabal)
									{
										text = text.replaceAll("%player_cabal_loser%", nearbyPlayer.getName());
										break;
									}
								}
							}
						}
						
						if (text == null)
							return;
						
						if (text.contains("%player_cabal_loser%") || text.contains("%player_cabal_winner%") || text.contains("%player_random%"))
							return;
						
						CreatureSay cs = new CreatureSay(chatNpc.getObjectId(), 0, creatureName, text);
						
						for (L2PcInstance nearbyPlayer : nearbyPlayers)
						{
							nearbyPlayer.sendPacket(cs);
						}
						for (L2PcInstance nearbyGM : nearbyGMs)
						{
							nearbyGM.sendPacket(cs);
						}
						
						if (Config.DEBUG)
						{
							_log.log(Level.FINE, getClass().getCanonicalName() + ": Chat propogation for object ID " + chatNpc.getObjectId() + " (" + creatureName + ") with text '" + text + "' sent to " + nearbyPlayers.size() + " nearby players.");
						}
					}
					catch (Exception e)
					{
						_log.warning(AutoChatHandler.class.getName() + ": Something did wrong.");
						if (Config.DEVELOPER)
							e.printStackTrace();
						return;
					}
				}
			}
		}
	}
}