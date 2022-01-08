package com.l2jhellas.gameserver.model.zone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.network.serverpackets.ExServerPrimitive;
import com.l2jhellas.gameserver.network.serverpackets.L2GameServerPacket;

public abstract class L2ZoneType
{
	
	protected static final Logger _log = Logger.getLogger(L2ZoneType.class.getName());
	
	private final int _id;
	protected L2ZoneForm _zone;
	protected final Map<Integer, L2Character> _characterList = new ConcurrentHashMap<>();
	
	private Map<QuestEventType, List<Quest>> _questEvents;

	private L2Character _character;
	
	protected L2ZoneType(int id)
	{
		_id = id;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void setParameter(String name, String value)
	{
		_log.info(getClass().getSimpleName() + ": Unknown parameter - " + name + " in zone: " + getId());
	}
	
	protected boolean isAffected(L2Character character)
	{
		// Overriden in children classes.
		return true;
	}
	
	public void setZone(L2ZoneForm zone)
	{
		if (_zone != null)
			throw new IllegalStateException("Zone already set");
		_zone = zone;
	}
	
	public L2ZoneForm getZone()
	{
		return _zone;
	}
	
	public boolean isInsideZone(int x, int y)
	{
		return _zone.isInsideZone(x, y, _zone.getHighZ());
	}
	
	public boolean isInsideZone(int x, int y, int z)
	{
		return _zone.isInsideZone(x, y, z);
	}
	
	public boolean isInsideZone(L2Object object)
	{
		return isInsideZone(object.getX(), object.getY(), object.getZ());
	}
	
	public double getDistanceToZone(int x, int y)
	{
		return getZone().getDistanceToZone(x, y);
	}
	
	public double getDistanceToZone(L2Object object)
	{
		return getZone().getDistanceToZone(object.getX(), object.getY());
	}
	
	public void revalidateInZone(L2Character character)
	{
		// If the character can't be affected by this zone return
		if (!isAffected(character))
			return;
		
		// If the object is inside the zone...
		if (isInsideZone(character))
		{
			// Was the character not yet inside this zone?
			if (_characterList.putIfAbsent(character.getObjectId(), character) == null)
			{
				// Notify to scripts.
				final List<Quest> quests = getQuestByEvent(QuestEventType.ON_ENTER_ZONE);
				if (quests != null)
				{
					for (Quest quest : quests)
						quest.notifyEnterZone(character, this);
				}
				
				// Notify Zone implementation.
				onEnter(character);
			}
		}
		else
			removeCharacter(character);
	}
	
	public void removeCharacter(L2Character character)
	{
		// Was the character inside this zone?
		if (_characterList.containsKey(character.getObjectId()))
		{
			// Notify to scripts.
			final List<Quest> quests = getQuestByEvent(QuestEventType.ON_EXIT_ZONE);
			if (quests != null)
			{
				for (Quest quest : quests)
					quest.notifyExitZone(character, this);
			}
			
			// Unregister player.
			_characterList.remove(character.getObjectId());
			
			// Notify Zone implementation.
			onExit(character);
		}
	}
	
	public boolean isCharacterInZone(L2Character character)
	{
		_character = character;
		return _character != null && _characterList.containsKey(_character.getObjectId()) || isInsideZone(_character.getX(), _character.getY(),_character.getZ());
	}
	
	protected abstract void onEnter(L2Character character);
	
	protected abstract void onExit(L2Character character);
	
	public abstract void onDieInside(L2Character character);
	
	public abstract void onReviveInside(L2Character character);
	
	public Collection<L2Character> getCharactersInside()
	{
		return _characterList.values();
	}
	
	public List<L2PcInstance> getPlayersInside()
	{
		List<L2PcInstance> players = new ArrayList<>();
		for (L2Character ch : _characterList.values())
		{
			if ((ch != null) && ch.isPlayer())
				players.add(ch.getActingPlayer());
		}
		
		return players;
	}
	
	@SuppressWarnings("unchecked")
	public final <A> List<A> getKnownTypeInside(Class<A> type)
	{
		List<A> result = new ArrayList<>();
		
		for (L2Object obj : _characterList.values())
		{
			if (type.isAssignableFrom(obj.getClass()))
				result.add((A) obj);
		}
		return result;
	}
	
	public void addQuestEvent(QuestEventType QuestEventType, Quest quest)
	{
		if (_questEvents == null)
			_questEvents = new HashMap<>();
		
		List<Quest> eventList = _questEvents.get(QuestEventType);
		if (eventList == null)
		{
			eventList = new ArrayList<>();
			eventList.add(quest);
			_questEvents.put(QuestEventType, eventList);
		}
		else
		{
			eventList.remove(quest);
			eventList.add(quest);
		}
	}
	
	public List<Quest> getQuestByEvent(QuestEventType QuestEventType)
	{
		return (_questEvents == null) ? null : _questEvents.get(QuestEventType);
	}
	
	public void broadcastPacket(L2GameServerPacket packet)
	{
		if (!getKnownTypeInside(L2PcInstance.class).isEmpty())
		{
			for (L2PcInstance players : getKnownTypeInside(L2PcInstance.class))
			{
				players.sendPacket(packet);
			}
		}
	}
	
	// public void oustAllPlayers()
	// {
	// _characterList.values().stream().filter(Objects::nonNull).filter(L2Object::isPlayer).map(L2Object::getActingPlayer).filter(L2PcInstance::isbOnline).forEach(player -> player.teleToLocation(TeleportWhereType.TOWN));
	// }
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + _id + "]";
	}
	
	public void visualizeZone(ExServerPrimitive debug, int z)
	{
		getZone().visualizeZone(toString() , debug , z);
	}
}