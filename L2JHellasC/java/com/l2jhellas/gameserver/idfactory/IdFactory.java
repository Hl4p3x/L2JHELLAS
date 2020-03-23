package com.l2jhellas.gameserver.idfactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.util.PrimeFinder;
import com.l2jhellas.util.database.L2DatabaseFactory;


public class IdFactory
{	
	private static Logger _log = Logger.getLogger(IdFactory.class.getName());
	
	public static final int FIRST_OID = 0x10000000;
	public static final int LAST_OID = 0x7FFFFFFF;
	public static final int FREE_OBJECT_ID_SIZE = LAST_OID - FIRST_OID;
	
	private BitSet _freeIds;
	private AtomicInteger _freeIdCount;
	private AtomicInteger _nextFreeId;
	
	protected IdFactory()
	{
		setAllCharacterOffline();
		cleanUpDB();
		cleanUpTimeStamps();

		initialize();

		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			if (reachingBitSetCapacity())
				increaseBitSetCapacity();
			
		}, 30000, 30000);
	}

	

	private static void cleanUpDB()
	{
		int cleanCount = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (Statement stmt = con.createStatement())
			{
				// Character related
				cleanCount += stmt.executeUpdate("DELETE FROM augmentations WHERE augmentations.item_id NOT IN (SELECT object_id FROM items);");
				cleanCount += stmt.executeUpdate("DELETE FROM character_friends WHERE character_friends.char_id NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM character_friends WHERE character_friends.friend_id NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM character_hennas WHERE character_hennas.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM character_macroses WHERE character_macroses.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM character_quests WHERE character_quests.char_id NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM character_raid_points WHERE character_raid_points.charId NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM character_recipebook WHERE character_recipebook.char_id NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM character_shortcuts WHERE character_shortcuts.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM character_skills WHERE character_skills.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM character_skills_save WHERE character_skills_save.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM character_subclasses WHERE character_subclasses.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM cursed_weapons WHERE cursed_weapons.playerId NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM pets WHERE pets.item_obj_id NOT IN (SELECT object_id FROM items);");
				cleanCount += stmt.executeUpdate("DELETE FROM seven_signs WHERE seven_signs.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
				
				// Olympiads & Heroes
				cleanCount += stmt.executeUpdate("DELETE FROM heroes WHERE heroes.char_id NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM olympiad_nobles WHERE olympiad_nobles.char_id NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM olympiad_nobles_eom WHERE olympiad_nobles_eom.char_id NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM olympiad_fights WHERE olympiad_fights.charOneId NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM olympiad_fights WHERE olympiad_fights.charTwoId NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM heroes_diary WHERE heroes_diary.char_id NOT IN (SELECT obj_Id FROM characters);");
				
				// Auction
				cleanCount += stmt.executeUpdate("DELETE FROM auction_bid WHERE auctionId IN (SELECT id FROM clanhall WHERE ownerId <> 0);");
				
				// Clan related
				cleanCount += stmt.executeUpdate("DELETE FROM clan_data WHERE clan_data.leader_id NOT IN (SELECT obj_Id FROM characters);");
				cleanCount += stmt.executeUpdate("DELETE FROM auction_bid WHERE auction_bid.bidderId NOT IN (SELECT clan_id FROM clan_data);");
				cleanCount += stmt.executeUpdate("DELETE FROM clanhall_functions WHERE clanhall_functions.hall_id NOT IN (SELECT id FROM clanhall WHERE ownerId <> 0);");
				cleanCount += stmt.executeUpdate("DELETE FROM clan_privs WHERE clan_privs.clan_id NOT IN (SELECT clan_id FROM clan_data);");
				cleanCount += stmt.executeUpdate("DELETE FROM clan_skills WHERE clan_skills.clan_id NOT IN (SELECT clan_id FROM clan_data);");
				cleanCount += stmt.executeUpdate("DELETE FROM clan_subpledges WHERE clan_subpledges.clan_id NOT IN (SELECT clan_id FROM clan_data);");
				cleanCount += stmt.executeUpdate("DELETE FROM clan_wars WHERE clan_wars.clan1 NOT IN (SELECT clan_id FROM clan_data);");
				cleanCount += stmt.executeUpdate("DELETE FROM clan_wars WHERE clan_wars.clan2 NOT IN (SELECT clan_id FROM clan_data);");
				cleanCount += stmt.executeUpdate("DELETE FROM siege_clans WHERE siege_clans.clan_id NOT IN (SELECT clan_id FROM clan_data);");
				
				// Items
				cleanCount += stmt.executeUpdate("DELETE FROM items WHERE items.owner_id NOT IN (SELECT obj_Id FROM characters) AND items.owner_id NOT IN (SELECT clan_id FROM clan_data);");
				
				// Forum related
				cleanCount += stmt.executeUpdate("DELETE FROM forums WHERE forums.forum_owner_id NOT IN (SELECT clan_id FROM clan_data) AND forums.forum_parent=2;");
				cleanCount += stmt.executeUpdate("DELETE FROM topic WHERE topic.topic_forum_id NOT IN (SELECT forum_id FROM forums);");
				cleanCount += stmt.executeUpdate("DELETE FROM posts WHERE posts.post_forum_id NOT IN (SELECT forum_id FROM forums);");
				
				stmt.executeUpdate("UPDATE clan_data SET auction_bid_at = 0 WHERE auction_bid_at NOT IN (SELECT auctionId FROM auction_bid);");
				stmt.executeUpdate("UPDATE clan_data SET leader_id = 0 WHERE leader_id NOT IN (SELECT obj_Id FROM characters);");
				stmt.executeUpdate("UPDATE clan_subpledges SET clan_id=0 WHERE clan_subpledges.clan_id NOT IN (SELECT obj_Id FROM characters) AND clan_id > 0;");
				stmt.executeUpdate("UPDATE castle SET taxpercent=0 WHERE castle.id NOT IN (SELECT hasCastle FROM clan_data);");
				stmt.executeUpdate("UPDATE characters SET clanid=0 WHERE characters.clanid NOT IN (SELECT clan_id FROM clan_data);");
				stmt.executeUpdate("UPDATE clanhall SET ownerId=0, paidUntil=0, paid=0 WHERE clanhall.ownerId NOT IN (SELECT clan_id FROM clan_data);");
			}
		}
		catch (Exception e)
		{
			_log.warning(IdFactory.class.getName() + ": Could not Clean up database ");
			e.printStackTrace();
		}
		_log.info(IdFactory.class.getSimpleName() + ": Cleaned " + cleanCount + " elements from database.");
	}
	
	private void initialize()
	{
		_freeIds = new BitSet(PrimeFinder.nextPrime(100000));
		_freeIdCount = new AtomicInteger(FREE_OBJECT_ID_SIZE);
		
		final List<Integer> usedObjectIds = new ArrayList<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (Statement st = con.createStatement())
			{
				try (ResultSet rs = st.executeQuery("SELECT obj_Id FROM characters"))
				{
					while (rs.next())
						usedObjectIds.add(rs.getInt(1));
				}
				
				try (ResultSet rs = st.executeQuery("SELECT object_id FROM items"))
				{
					while (rs.next())
						usedObjectIds.add(rs.getInt(1));
				}
				
				try (ResultSet rs = st.executeQuery("SELECT clan_id FROM clan_data"))
				{
					while (rs.next())
						usedObjectIds.add(rs.getInt(1));
				}
				
				try (ResultSet rs = st.executeQuery("SELECT object_id FROM itemsonground"))
				{
					while (rs.next())
						usedObjectIds.add(rs.getInt(1));
				}
				
				try (ResultSet rs = st.executeQuery("SELECT id FROM mods_wedding"))
				{
					while (rs.next())
						usedObjectIds.add(rs.getInt(1));
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("Couldn't properly initialize objectIds.");
			e.printStackTrace();
		}
		
		for (int usedObjectId : usedObjectIds)
		{
			final int objectId = usedObjectId - FIRST_OID;
			if (objectId < 0)
			{
				_log.warning("Found invalid objectId: "+usedObjectId+". It is less than minimum of "+FIRST_OID);
				continue;
			}
			
			_freeIds.set(objectId);
			_freeIdCount.decrementAndGet();
		}
		
		_nextFreeId = new AtomicInteger(_freeIds.nextClearBit(0));
		
		_log.info("Initializing:  "+_freeIds.size()+" objectIds, with "+usedObjectIds.size()+" used ids.");
	}

	public synchronized void releaseId(int objectID)
	{
		if ((objectID - FIRST_OID) > -1)
		{
			_freeIds.clear(objectID - FIRST_OID);
			_freeIdCount.incrementAndGet();
		}
		else
		    _log.warning(IdFactory.class.getName() + ": Release objectID " + objectID + " failed (< " + FIRST_OID + ")");
	}

	public synchronized int getNextId()
	{
		int newId = _nextFreeId.get();
		
		_freeIds.set(newId);
		_freeIdCount.decrementAndGet();
		
		int nextFree = _freeIds.nextClearBit(newId);
		
		if (nextFree < 0)
			nextFree = _freeIds.nextClearBit(0);
		
		if (nextFree < 0)
		{
			if (_freeIds.size() < FREE_OBJECT_ID_SIZE)
				increaseBitSetCapacity();
			else
				throw new NullPointerException("Ran out of valid Id's.");
		}
		
		_nextFreeId.set(nextFree);
		
		return newId + FIRST_OID;
	}

	protected int usedIdCount()
	{
		return _freeIdCount.get() - FIRST_OID;
	}

	protected synchronized boolean reachingBitSetCapacity()
	{
		return PrimeFinder.nextPrime(usedIdCount() * 11 / 10) > _freeIds.size();
	}

	protected synchronized void increaseBitSetCapacity()
	{
		BitSet newBitSet = new BitSet(PrimeFinder.nextPrime(usedIdCount() * 11 / 10));
		newBitSet.or(_freeIds);
		_freeIds = newBitSet;
	}

	private static void setAllCharacterOffline()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE characters SET online = 0"))
		{
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warning(IdFactory.class.getName() + ": could not update character offline status ");
			e.printStackTrace();
		}
		_log.info(IdFactory.class.getSimpleName() + ": Updated characters online status.");
	}

	private static void cleanUpTimeStamps()
	{
		int cleanCount = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills_save WHERE restore_type = 1 AND systime <= ?"))
		{
			ps.setLong(1, System.currentTimeMillis());
			
			cleanCount += ps.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warning(IdFactory.class.getName() +"Couldn't cleanup timestamps.");
			e.printStackTrace();
		}
		_log.info("Cleaned:"+cleanCount+" expired timestamps from database.");
	}
	
	public static final IdFactory getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final IdFactory INSTANCE = new IdFactory();
	}
}