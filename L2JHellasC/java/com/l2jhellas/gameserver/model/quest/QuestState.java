package com.l2jhellas.gameserver.model.quest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.model.L2DropData;
import com.l2jhellas.gameserver.model.PcInventory;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ExShowQuestMark;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.PlaySound;
import com.l2jhellas.gameserver.network.serverpackets.QuestList;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.network.serverpackets.TutorialCloseHtml;
import com.l2jhellas.gameserver.network.serverpackets.TutorialEnableClientEvent;
import com.l2jhellas.gameserver.network.serverpackets.TutorialShowHtml;
import com.l2jhellas.gameserver.network.serverpackets.TutorialShowQuestionMark;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.database.L2DatabaseFactory;

public final class QuestState
{
	protected static final Logger _log = Logger.getLogger(Quest.class.getName());
	
	public static final String SOUND_ACCEPT = "ItemSound.quest_accept";
	public static final String SOUND_ITEMGET = "ItemSound.quest_itemget";
	public static final String SOUND_MIDDLE = "ItemSound.quest_middle";
	public static final String SOUND_FINISH = "ItemSound.quest_finish";
	public static final String SOUND_GIVEUP = "ItemSound.quest_giveup";
	public static final String SOUND_JACKPOT = "ItemSound.quest_jackpot";
	public static final String SOUND_FANFARE = "ItemSound.quest_fanfare_2";
	public static final String SOUND_BEFORE_BATTLE = "Itemsound.quest_before_battle";
	public static final String AMDSOUND_HORROR_02 = "AmdSound.dd_horror_02";
	
	private static final String QUEST_SET_VAR = "REPLACE INTO character_quests (char_id,name,var,value) VALUES (?,?,?,?)";
	private static final String QUEST_DEL_VAR = "DELETE FROM character_quests WHERE char_id=? AND name=? AND var=?";
	private static final String QUEST_DELETE = "DELETE FROM character_quests WHERE char_id=? AND name=?";
	private static final String QUEST_COMPLETE = "DELETE FROM character_quests WHERE char_id=? AND name=? AND var<>'<state>'";
	
	public static final byte DROP_DIVMOD = 0;
	public static final byte DROP_FIXED_RATE = 1;
	public static final byte DROP_FIXED_COUNT = 2;
	public static final byte DROP_FIXED_BOTH = 3;
	
	private final L2PcInstance _player;
	private final Quest _quest;
	private byte _state;
	private final Map<String, String> _vars = new HashMap<>();
	
	QuestState(L2PcInstance player, Quest quest, byte state)
	{
		_player = player;
		_quest = quest;
		_state = state;
		
		_player.setQuestState(this);
	}
	
	public L2PcInstance getPlayer()
	{
		return _player;
	}
	
	public Quest getQuest()
	{
		return _quest;
	}
	
	public byte getState()
	{
		return _state;
	}
	
	public boolean isCreated()
	{
		return (_state == Quest.STATE_CREATED);
	}
	
	public boolean isCompleted()
	{
		return (_state == Quest.STATE_COMPLETED);
	}
	
	public boolean isStarted()
	{
		return (_state == Quest.STATE_STARTED);
	}
	
	public void setState(byte state)
	{
		if (_state != state)
		{
			_state = state;
			
			setQuestVarInDb("<state>", String.valueOf(_state));
			
			_player.sendPacket(new QuestList(_player));
		}
	}
	
	public void exitQuest(boolean repeatable)
	{
		if (!isStarted())
			return;
		
		// Remove quest from player's notifyDeath list.
		_player.removeNotifyQuestOfDeath(this);
		
		// Remove/Complete quest.
		if (repeatable)
		{
			_player.delQuestState(this);
			_player.sendPacket(new QuestList(_player));
		}
		else
		{
			setState(Quest.STATE_COMPLETED);
			playSound(QuestState.QUEST_COMPLETE);
		}
		
		// Remove quest variables.
		_vars.clear();
		
		// Remove registered quest items.
		int[] itemIdList = _quest.getItemsIds();
		if (itemIdList != null)
		{
			for (int itemId : itemIdList)
				takeItems(itemId, -1);
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement;
			if (repeatable)
				statement = con.prepareStatement(QUEST_DELETE);
			else
				statement = con.prepareStatement(QUEST_COMPLETE);
			
			statement.setInt(1, _player.getObjectId());
			statement.setString(2, _quest.getName());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(QuestState.class.getSimpleName() + ": could not delete char quest:");
		}
	}
	
	public void addNotifyOfDeath()
	{
		if (_player != null)
			_player.addNotifyQuestOfDeath(this);
	}
	
	public void set(String var, String value)
	{
		if (var == null || var.isEmpty() || value == null || value.isEmpty())
			return;
		
		// HashMap.put() returns previous value associated with specified key, or null if there was no mapping for key.
		String old = _vars.put(var, value);
		
		setQuestVarInDb(var, value);
		
		if ("cond".equals(var))
		{
			try
			{
				int previousVal = 0;
				try
				{
					previousVal = Integer.parseInt(old);
				}
				catch (Exception ex)
				{
					previousVal = 0;
				}
				setCond(Integer.parseInt(value), previousVal);
			}
			catch (Exception e)
			{
				_log.warning(QuestState.class.getName() + ": " + _player.getName() + ", " + _quest.getName() + " cond [" + value + "] is not an integer. Value stored, but no packet was sent: " + e.getMessage());
			}
		}
	}
	
	public QuestState setMemoState(int value) 
	{
		set("memoState", String.valueOf(value));
		return this;
	}

	public int getMemoState() 
	{
		if (isStarted())
			return getInt("memoState");
		return -1;
	}
	
	public boolean hasMemoState()
	{
		return getMemoState() > 0;
	}
	
	public boolean isMemoState(int memoState)
	{
		return (getInt("memoState") == memoState);
	}

	public int getMemoStateEx(int slot)
	{
		if (isStarted())
			return getInt("memoStateEx" + slot);
		return 0;
	}

	public QuestState setMemoStateEx(int slot, int value)
	{
		set("memoStateEx" + slot, String.valueOf(value));
		return this;
	}

	public boolean isMemoStateEx(int slot, int memoStateEx) 
	{
		return (getMemoStateEx(slot) == memoStateEx);
	}
	
	public int getCond()
	{
		return isStarted() ? getInt("cond") : 0;
	}
	
	public boolean isCond(int cond)
	{
		return getInt("cond") == cond;
	}
	
	public boolean isSet(String var)
	{
		return get(var) != null;
	}
	
	public void setInternal(String var, String value)
	{
		if (var == null || var.isEmpty() || value == null || value.isEmpty())
			return;
		
		_vars.put(var, value);
	}
	
	private void setCond(int cond, int old)
	{
		// if there is no change since last setting, there is nothing to do here
		if (cond == old)
			return;
		
		int completedStateFlags = 0;
		
		// cond 0 and 1 do not need completedStateFlags. Also, if cond > 1, the 1st step must
		// always exist (i.e. it can never be skipped). So if cond is 2, we can still safely
		// assume no steps have been skipped.
		// Finally, more than 31 steps CANNOT be supported in any way with skipping.
		if (cond < 3 || cond > 31)
			unset("__compltdStateFlags");
		else
			completedStateFlags = getInt("__compltdStateFlags");
		
		// case 1: No steps have been skipped so far...
		if (completedStateFlags == 0)
		{
			// check if this step also doesn't skip anything. If so, no further work is needed
			// also, in this case, no work is needed if the state is being reset to a smaller value
			// in those cases, skip forward to informing the client about the change...
			
			// ELSE, if we just now skipped for the first time...prepare the flags!!!
			if (cond > (old + 1))
			{
				// set the most significant bit to 1 (indicates that there exist skipped states)
				// also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter
				// what the cond says)
				completedStateFlags = 0x80000001;
				
				// since no flag had been skipped until now, the least significant bits must all
				// be set to 1, up until "old" number of bits.
				completedStateFlags |= ((1 << old) - 1);
				
				// now, just set the bit corresponding to the passed cond to 1 (current step)
				completedStateFlags |= (1 << (cond - 1));
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		// case 2: There were exist previously skipped steps
		else
		{
			// if this is a push back to a previous step, clear all completion flags ahead
			if (cond < old)
			{
				completedStateFlags &= ((1 << cond) - 1); // note, this also unsets the flag indicating that there exist skips
				
				// now, check if this resulted in no steps being skipped any more
				if (completedStateFlags == ((1 << cond) - 1))
					unset("__compltdStateFlags");
				else
				{
					// set the most significant bit back to 1 again, to correctly indicate that this skips states.
					// also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter
					// what the cond says)
					completedStateFlags |= 0x80000001;
					set("__compltdStateFlags", String.valueOf(completedStateFlags));
				}
			}
			// if this moves forward, it changes nothing on previously skipped steps...so just mark this
			// state and we are done
			else
			{
				completedStateFlags |= (1 << (cond - 1));
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		
		// send a packet to the client to inform it of the quest progress (step change)
		_player.sendPacket(new QuestList(_player));
		
		if (_quest.isRealQuest() && cond > 0)
			_player.sendPacket(new ExShowQuestMark(_quest.getQuestId()));
	}
	
	public void unset(String var)
	{
		if (_vars.remove(var) != null)
			removeQuestVarInDb(var);
	}
	
	public String get(String var)
	{
		return _vars.get(var);
	}
		
	public int getInt(String var)
	{
		final String variable = _vars.get(var);
		if (variable == null || variable.isEmpty())
			return 0;
		
		int value = 0;
		try
		{
			value = Integer.parseInt(variable);
		}
		catch (Exception e)
		{
			_log.log(Level.FINER, _player.getName() + ": variable " + var + " isn't an integer: " + value + " ! " + e.getMessage(), e);
		}
		
		return value;
	}
	
	private void setQuestVarInDb(String var, String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(QUEST_SET_VAR);
			statement.setInt(1, _player.getObjectId());
			statement.setString(2, _quest.getName());
			statement.setString(3, var);
			statement.setString(4, value);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(QuestState.class.getSimpleName() + ": could not insert char quest:");
		}
	}
	
	private void removeQuestVarInDb(String var)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(QUEST_DEL_VAR);
			statement.setInt(1, _player.getObjectId());
			statement.setString(2, _quest.getName());
			statement.setString(3, var);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(QuestState.class.getSimpleName() + ": could not delete char quest:");
		}
	}
	
	public boolean hasQuestItems(int itemId)
	{
		return _player.getInventory().getItemByItemId(itemId) != null;
	}
	
	public boolean hasQuestItems(int... itemIds)
	{
		final PcInventory inv = _player.getInventory();
		for (int itemId : itemIds)
		{
			if (inv.getItemByItemId(itemId) == null)
				return false;
		}
		return true;
	}
	
	public boolean hasAtLeastOneQuestItem(int... itemIds)
	{
		return _player.getInventory().hasAtLeastOneItem(itemIds);
	}
	
	public int getQuestItemsCount(int itemId)
	{
		int count = 0;
		
		for (L2ItemInstance item : _player.getInventory().getItems())
			if (item != null && item.getItemId() == itemId)
				count += item.getCount();
		
		return count;
	}
	
	public int getItemEquipped(int loc)
	{
		return _player.getInventory().getPaperdollItemId(loc);
	}
	
	public int getEnchantLevel(int itemId)
	{
		final L2ItemInstance enchanteditem = _player.getInventory().getItemByItemId(itemId);
		if (enchanteditem == null)
			return 0;
		
		return enchanteditem.getEnchantLevel();
	}
	
	public void giveItems(int itemId, int itemCount)
	{
		giveItems(itemId, itemCount, 0);
	}
	
	public void giveItems(int itemId, int itemCount, int enchantLevel)
	{
		// Incorrect amount.
		if (itemCount <= 0)
			return;
		
		// Add items to player's inventory.
		final L2ItemInstance item = _player.getInventory().addItem("Quest", itemId, itemCount, _player, _player);
		if (item == null)
			return;
		
		// Set enchant level for the item.
		if (enchantLevel > 0)
			item.setEnchantLevel(enchantLevel);
		
		// Send message to the client.
		if (itemId == 57)
			_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA).addItemNumber(itemCount));
		else
		{
			if (itemCount > 1)
				_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(itemId).addItemNumber(itemCount));
			else
				_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(itemId));
		}
		
		// Send status update packet.
		StatusUpdate su = new StatusUpdate(_player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, _player.getCurrentLoad());
		_player.sendPacket(su);
	}
	
	public void takeItems(int itemId, int itemCount)
	{
		// Find item in player's inventory.
		final L2ItemInstance item = _player.getInventory().getItemByItemId(itemId);
		if (item == null)
			return;
		
		// Tests on count value and set correct value if necessary.
		if (itemCount < 0 || itemCount > item.getCount())
			itemCount = item.getCount();
		
		// Disarm item, if equipped.
		if (item.isEquipped())
		{
			L2ItemInstance[] unequiped = _player.getInventory().unEquipItemInBodySlotAndRecord(item);
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance itm : unequiped)
				iu.addModifiedItem(itm);
			
			_player.sendPacket(iu);
			_player.broadcastUserInfo();
		}
		
		// Destroy the quantity of items wanted.
		_player.destroyItemByItemId("Quest", itemId, itemCount, _player, true);
	}
	
	public boolean dropItemsAlways(int itemId, int count, int neededCount)
	{
		return dropItems(itemId, count, neededCount, L2DropData.MAX_CHANCE, DROP_FIXED_RATE);
	}
	
	public boolean dropItems(int itemId, int count, int neededCount, int dropChance)
	{
		return dropItems(itemId, count, neededCount, dropChance, DROP_DIVMOD);
	}
	
	public boolean dropItems(int itemId, int count, int neededCount, int dropChance, byte type)
	{
		// Get current amount of item.
		final int currentCount = getQuestItemsCount(itemId);
		
		// Required amount reached already?
		if (neededCount > 0 && currentCount >= neededCount)
			return true;
		
		int amount = 0;
		switch (type)
		{
			case DROP_DIVMOD:
				dropChance *= Config.RATE_QUEST_DROP;
				amount = count * (dropChance / L2DropData.MAX_CHANCE);
				if (Rnd.get(L2DropData.MAX_CHANCE) < dropChance % L2DropData.MAX_CHANCE)
					amount += count;
				break;
			
			case DROP_FIXED_RATE:
				if (Rnd.get(L2DropData.MAX_CHANCE) < dropChance)
					amount = (int) (count * Config.RATE_QUEST_DROP);
				break;
			
			case DROP_FIXED_COUNT:
				if (Rnd.get(L2DropData.MAX_CHANCE) < dropChance * Config.RATE_QUEST_DROP)
					amount = count;
				break;
			
			case DROP_FIXED_BOTH:
				if (Rnd.get(L2DropData.MAX_CHANCE) < dropChance)
					amount = count;
				break;
		}
		
		boolean reached = false;
		if (amount > 0)
		{
			// Limit count to reach required amount.
			if (neededCount > 0)
			{
				reached = (currentCount + amount) >= neededCount;
				amount = (reached) ? neededCount - currentCount : amount;
			}
			
			// Inventory slot check.
			if (!_player.getInventory().validateCapacityByItemId(itemId))
				return false;
			
			// Give items to the player.
			giveItems(itemId, amount, 0);
			
			// Play the sound.
			playSound(reached ? SOUND_MIDDLE : SOUND_ITEMGET);
		}
		
		return neededCount > 0 && reached;
	}
	
	public boolean dropMultipleItems(int[][] rewardsInfos)
	{
		return dropMultipleItems(rewardsInfos, DROP_DIVMOD);
	}
	
	public boolean dropMultipleItems(int[][] rewardsInfos, byte type)
	{
		// Used for the sound.
		boolean sendSound = false;
		
		// Used for the reached state.
		boolean reached = true;
		
		// For each reward type, calculate the probability of drop.
		for (int[] info : rewardsInfos)
		{
			final int itemId = info[0];
			final int currentCount = getQuestItemsCount(itemId);
			final int neededCount = info[2];
			
			// Required amount reached already?
			if (neededCount > 0 && currentCount >= neededCount)
				continue;
			
			final int count = info[1];
			
			int dropChance = info[3];
			int amount = 0;
			
			switch (type)
			{
				case DROP_DIVMOD:
					dropChance *= Config.RATE_QUEST_DROP;
					amount = count * (dropChance / L2DropData.MAX_CHANCE);
					if (Rnd.get(L2DropData.MAX_CHANCE) < dropChance % L2DropData.MAX_CHANCE)
						amount += count;
					break;
				
				case DROP_FIXED_RATE:
					if (Rnd.get(L2DropData.MAX_CHANCE) < dropChance)
						amount = (int) (count * Config.RATE_QUEST_DROP);
					break;
				
				case DROP_FIXED_COUNT:
					if (Rnd.get(L2DropData.MAX_CHANCE) < dropChance * Config.RATE_QUEST_DROP)
						amount = count;
					break;
				
				case DROP_FIXED_BOTH:
					if (Rnd.get(L2DropData.MAX_CHANCE) < dropChance)
						amount = count;
					break;
			}
			
			if (amount > 0)
			{
				// Limit count to reach required amount.
				if (neededCount > 0)
					amount = ((currentCount + amount) >= neededCount) ? neededCount - currentCount : amount;
				
				// Inventory slot check.
				if (!_player.getInventory().validateCapacityByItemId(itemId))
					continue;
				
				// Give items to the player.
				giveItems(itemId, amount, 0);
				
				// Send sound.
				sendSound = true;
				
				// Illimited needed count or current count being inferior to needed count means the state isn't reached.
				if (neededCount <= 0 || ((currentCount + amount) < neededCount))
					reached = false;
			}
		}
		
		// Play the sound.
		if (sendSound)
			playSound((reached) ? SOUND_MIDDLE : SOUND_ITEMGET);
		
		return reached;
	}
	
	public void rewardItems(int itemId, int itemCount)
	{
		giveItems(itemId, (int) (itemCount * Config.RATE_QUEST_REWARD), 0);
	}
	
	public void rewardExpAndSp(long exp, int sp)
	{
		_player.addExpAndSp((long) getPlayer().calcStat(Stats.EXPSP_RATE, exp * Config.RATE_QUEST_REWARD_XP, null, null), (int) getPlayer().calcStat(Stats.EXPSP_RATE, sp * Config.RATE_QUEST_REWARD_SP, null, null));
	}
	
	// TODO: More radar functions need to be added when the radar class is complete.
	// BEGIN STUFF THAT WILL PROBABLY BE CHANGED
	
	public void addRadar(int x, int y, int z)
	{
		_player.getRadar().addMarker(x, y, z);
	}
	
	public void removeRadar(int x, int y, int z)
	{
		_player.getRadar().removeMarker(x, y, z);
	}
	
	public void clearRadar()
	{
		_player.getRadar().removeAllMarkers();
	}
	
	// END STUFF THAT WILL PROBABLY BE CHANGED
	
	public void playSound(String sound)
	{
		PlaySound _snd = PlaySound.createSound(sound);
		_player.sendPacket(_snd);
	}

	public void showQuestionMark(int number)
	{
		_player.sendPacket(new TutorialShowQuestionMark(number));
	}
	
	public void playTutorialVoice(String voice)
	{
		PlaySound _snd = PlaySound.createVoice(voice);
		_player.sendPacket(_snd);
	}
	
	public void showTutorialHTML(String html)
	{
		_player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/scripts/quests/Q255_Tutorial/" + html)));
	}
	
	public void closeTutorialHtml()
	{
		_player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
	}
	
	public void onTutorialClientEvent(int number)
	{
		_player.sendPacket(new TutorialEnableClientEvent(number));
	}

	// discover the string representation of the state, for readable DB storage
	public static String getStateName(byte state)
	{
		switch (state)
		{
			case 1:
				return "Started";
			case 2:
				return "Completed";
			default:
				return "Start";
		}
	}
}
