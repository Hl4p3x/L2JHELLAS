package com.l2jhellas.gameserver.model.actor.group.party;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.SevenSignsFestival;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.controllers.GameTimeController;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.enums.player.PartyLootType;
import com.l2jhellas.gameserver.instancemanager.DuelManager;
import com.l2jhellas.gameserver.model.BlockList;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.entity.DimensionalRift;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.network.serverpackets.ExCloseMPCC;
import com.l2jhellas.gameserver.network.serverpackets.ExOpenMPCC;
import com.l2jhellas.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jhellas.gameserver.network.serverpackets.PartyMemberPosition;
import com.l2jhellas.gameserver.network.serverpackets.PartySmallWindowAdd;
import com.l2jhellas.gameserver.network.serverpackets.PartySmallWindowAll;
import com.l2jhellas.gameserver.network.serverpackets.PartySmallWindowDelete;
import com.l2jhellas.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;

public class L2Party
{
	private static final double[] BONUS_EXP_SP =
	{
		1,
		1.30,
		1.39,
		1.50,
		1.54,
		1.58,
		1.63,
		1.67,
		1.71
	};
	
	private final List<L2PcInstance> _members;

	private boolean _pendingInvitation = false;

	private long _pendingInviteTimeout;
	private static final int PARTY_POSITION_BROADCAST = 10000;
	private int _partyLvl = 0;
	private int _itemLastLoot = 0;
	
	private volatile PartyLootType _itemDistribution = PartyLootType.FINDERS_KEEPERS;

	private L2CommandChannel _commandChannel = null;
	private DimensionalRift _dr;
	
	private Future<?> _positionBroadcastTask = null;
	protected PartyMemberPosition _positionPacket;
	
	public L2Party(L2PcInstance leader, PartyLootType itemDistribution)
	{
		_members = new ArrayList<>();
		_itemDistribution = itemDistribution;
		getPartyMembers().add(leader);
		_partyLvl = leader.getLevel();
	}
	
	public int getMemberCount()
	{
		return getPartyMembers().size();
	}

	public final List<L2PcInstance> getPartyMembers()
	{
		return _members;
	}
	
	private L2PcInstance getRandomMember(int ItemId, L2Character target)
	{
		List<L2PcInstance> availableMembers = new ArrayList<>();
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null && member.getInventory().validateCapacityByItemId(ItemId) && Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
				availableMembers.add(member);
		}

		return !availableMembers.isEmpty() ? availableMembers.get(Rnd.get(availableMembers.size())) : null;
	}
	
	private L2PcInstance getNextLooter(int ItemId, L2Character target)
	{
		for (int i = 0; i < getMemberCount(); i++)
		{
			if (++_itemLastLoot >= getMemberCount())
				_itemLastLoot = 0;
			
			L2PcInstance member = getPartyMembers().get(_itemLastLoot);
			if (member != null && member.getInventory().validateCapacityByItemId(ItemId) && Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
				return member;
		}
		
		return null;
	}
	
	private L2PcInstance getActualLooter(L2PcInstance player, int ItemId, boolean spoil, L2Character target)
	{
		L2PcInstance looter = player;
		
		switch (_itemDistribution)
		{
			case RANDOM:
				if (!spoil)
					looter = getRandomMember(ItemId, target);
				break;
			case RANDOM_INCLUDING_SPOIL:
				looter = getRandomMember(ItemId, target);
				break;
			case BY_TURN:
				if (!spoil)
					looter = getNextLooter(ItemId, target);
				break;
			case BY_TURN_INCLUDING_SPOIL:
				looter = getNextLooter(ItemId, target);
				break;
			default :
				break;
		}	
		return looter != null ? looter : player;
	}
	
	public boolean isLeader(L2PcInstance player)
	{
		return (getLeader().equals(player));
	}
	
	public int getPartyLeaderOID()
	{
		return getLeader().getObjectId();
	}
	
	public void broadcastToPartyMembers(L2GameServerPacket packet)
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null)
				member.sendPacket(packet);
		}
	}
	
	public void broadcastToPartyMembersNewLeader()
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null)
			{
				member.sendPacket(new PartySmallWindowDeleteAll());
				member.sendPacket(new PartySmallWindowAll(member, this));
				member.broadcastUserInfo();
			}
		}
	}
	
	public void broadcastCSToPartyMembers(CreatureSay msg, L2PcInstance broadcaster)
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null && !BlockList.isBlocked(member, broadcaster))
				member.sendPacket(msg);
		}
	}
	
	public void broadcastToPartyMembers(L2PcInstance player, L2GameServerPacket msg)
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null && !member.equals(player))
				member.sendPacket(msg);
		}
	}
	
	public synchronized void addPartyMember(L2PcInstance player)
	{
		if (getPartyMembers().contains(player))
			return;
		
		// Send new member party window for all members
		player.sendPacket(new PartySmallWindowAll(player, this));
		broadcastToPartyMembers(new PartySmallWindowAdd(player, this));
		
		// Send messages
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_JOINED_S1_PARTY).addPcName(getLeader()));
		broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_JOINED_PARTY).addPcName(player));
		
		// Add player to party, adjust party level
		getPartyMembers().add(player);
		if (player.getLevel() > _partyLvl)
			_partyLvl = player.getLevel();
		
		// Update partySpelled
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null)
			{
				member.updateEffectIcons(true); // update party icons only
				member.broadcastUserInfo();
			}
		}
		
		if (isInDimensionalRift())
			_dr.partyMemberInvited();
		
		// open the CCInformationwindow
		if (isInCommandChannel())
			player.sendPacket(new ExOpenMPCC());
		
		// activate position task
		if (_positionBroadcastTask == null)
			_positionBroadcastTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PositionBroadcast(), PARTY_POSITION_BROADCAST / 2, PARTY_POSITION_BROADCAST);
	}
	
	public void removePartyMember(String name)
	{
		removePartyMember(getPlayerByName(name));
	}
	
	public void removePartyMember(L2PcInstance player)
	{
		removePartyMember(player, true);
	}
	
	public synchronized void removePartyMember(L2PcInstance player, boolean sendMessage)
	{
		if (getPartyMembers().contains(player))
		{
			boolean isLeader = isLeader(player);
			getPartyMembers().remove(player);
			recalculatePartyLevel();
			
			if (player.isFestivalParticipant())
				SevenSignsFestival.getInstance().updateParticipants(player, this);
			
			DuelManager.getInstance().onPartyEdit(player);
			
			if (sendMessage)
			{
				player.sendPacket(SystemMessageId.YOU_LEFT_PARTY);
				broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_LEFT_PARTY).addPcName(player));
			}
			
			player.sendPacket(new PartySmallWindowDeleteAll());
			player.setParty(null);
			
			broadcastToPartyMembers(new PartySmallWindowDelete(player));
			
			if (isInDimensionalRift())
				_dr.partyMemberExited(player);
			
			// Close the CCInfoWindow
			if (isInCommandChannel())
				player.sendPacket(new ExCloseMPCC());
			
			if (isLeader && getPartyMembers().size() > 1)
			{
				broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER).addPcName(getLeader()));
				broadcastToPartyMembersNewLeader();
			}
			else if (getPartyMembers().size() == 1)
			{
				if (isInCommandChannel())
				{
					// delete the whole command channel when the party who opened the channel is disbanded
					if (getCommandChannel().getChannelLeader().equals(getLeader()))
						getCommandChannel().disbandChannel();
					else
						getCommandChannel().removeParty(this);
				}
				
				if (getLeader() != null)
				{
					getLeader().setParty(null);
					if (getLeader().isInDuel())
						DuelManager.getInstance().onPartyEdit(getLeader());
				}
				
				if (_positionBroadcastTask != null)
				{
					_positionBroadcastTask.cancel(false);
					_positionBroadcastTask = null;
				}
				_members.clear();
			}
		}
	}
	
	public void changePartyLeader(String name)
	{
		L2PcInstance player = getPlayerByName(name);
		
		if (player != null && !player.isInDuel())
		{
			if (getPartyMembers().contains(player))
			{
				if (isLeader(player))
					player.sendPacket(SystemMessageId.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF);
				else
				{
					// Swap party members
					L2PcInstance temp = getLeader();
					int p1 = getPartyMembers().indexOf(player);
					
					getPartyMembers().set(0, getPartyMembers().get(p1));
					getPartyMembers().set(p1, temp);
					
					broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER).addPcName(getLeader()));
					broadcastToPartyMembersNewLeader();
					
					if (isInCommandChannel() && temp.equals(_commandChannel.getChannelLeader()))
					{
						_commandChannel.setChannelLeader(getLeader());
						_commandChannel.broadcastToChannelMembers(SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_LEADER_NOW_S1).addPcName(_commandChannel.getChannelLeader()));
					}
					
					if (player.isInPartyMatchRoom())
					{
						PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
						room.changeLeader(player);
					}
				}
			}
			else
				player.sendPacket(SystemMessageId.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER);
		}
	}
	
	private L2PcInstance getPlayerByName(String name)
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member.getName().equalsIgnoreCase(name))
				return member;
		}
		return null;
	}
	
	public void distributeItem(L2PcInstance player, L2ItemInstance item)
	{
		if (item.getItemId() == 57)
		{
			distributeAdena(player, item.getCount(), player);
			ItemTable.getInstance().destroyItem("Party", item, player, null);
			return;
		}
		
		L2PcInstance target = getActualLooter(player, item.getItemId(), false, player);
		target.addItem("Party", item, player, true);
		
		// Send messages to other party members about reward
		if (item.getCount() > 1)
			broadcastToPartyMembers(target, SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S3_S2).addPcName(target).addItemName(item).addItemNumber(item.getCount()));
		else if (item.getEnchantLevel() > 0)
			broadcastToPartyMembers(target, SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S2_S3).addPcName(target).addNumber(item.getEnchantLevel()).addItemName(item));
		else
			broadcastToPartyMembers(target, SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S2).addPcName(target).addItemName(item));
	}
	
	public void distributeItem(L2PcInstance player, L2Attackable.RewardItem item, boolean spoil, L2Attackable target)
	{
		if (item == null)
			return;
		
		if (item.getItemId() == 57)
		{
			distributeAdena(player, item.getCount(), target);
			return;
		}
		
		L2PcInstance looter = getActualLooter(player, item.getItemId(), spoil, target);
		looter.addItem(spoil ? "Sweep" : "Party", item.getItemId(), item.getCount(), player, true);
		
		// Send messages to other party members about reward
		SystemMessage msg;
		if (item.getCount() > 1)
		{
			msg = spoil ? SystemMessage.getSystemMessage(SystemMessageId.S1_SWEEPED_UP_S3_S2) : SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S3_S2);
			msg.addPcName(looter);
			msg.addItemName(item.getItemId());
			msg.addItemNumber(item.getCount());
			broadcastToPartyMembers(looter, msg);
		}
		else
		{
			msg = spoil ? SystemMessage.getSystemMessage(SystemMessageId.S1_SWEEPED_UP_S2) : SystemMessage.getSystemMessage(SystemMessageId.S1_OBTAINED_S2);
			msg.addPcName(looter);
			msg.addItemName(item.getItemId());
			broadcastToPartyMembers(looter, msg);
		}
		msg = null;
	}
	
	public void distributeAdena(L2PcInstance player, int adena, L2Character target)
	{
		// Get all the party members
		List<L2PcInstance> membersList = getPartyMembers();
		
		// Check the number of party members that must be rewarded
		// (The party member must be in range to receive its reward)
		List<L2PcInstance> ToReward = new ArrayList<>();
		
		for (L2PcInstance member : membersList)
		{
			if (Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true))
				ToReward.add(member);
		}
		
		if (!ToReward.isEmpty())
		{
			int count = adena / ToReward.size();
			
			for (L2PcInstance member : ToReward)
				member.addAdena("Party", count, player, true);
		}
	}
	
	public void distributeXpAndSp(long xpReward_pr, int spReward_pr, long xpReward, int spReward, List<L2Playable> rewardedMembers, int topLvl)
	{
		L2SummonInstance summon = null;
		List<L2Playable> validMembers = getValidMembers(rewardedMembers, topLvl);
		
		float penalty;
		double sqLevel;
		double preCalculation;
		int temp_sp;
		long temp_exp;
		
		xpReward *= getExpBonus(validMembers.size());
		spReward *= getSpBonus(validMembers.size());
		xpReward_pr *= getExpBonus(validMembers.size());
		spReward_pr *= getSpBonus(validMembers.size());
		temp_exp = xpReward;
		temp_sp = spReward;
		
		double sqLevelSum = 0;
		for (L2Playable character : validMembers)
			sqLevelSum += (character.getLevel() * character.getLevel());
		
		// Go through the L2PcInstances and L2PetInstances (not L2SummonInstances) that must be rewarded
		synchronized (rewardedMembers)
		{
			for (L2Character member : rewardedMembers)
			{
				if (member.isDead())
					continue;
				
				if (member.getPremiumService() == 1)
				{
					xpReward = xpReward_pr;
					spReward = spReward_pr;
				}
				else
				{
					xpReward = temp_exp;
					spReward = temp_sp;
				}
				
				penalty = 0;
				
				// The L2SummonInstance penalty
				if (member.getPet() instanceof L2SummonInstance)
				{
					summon = (L2SummonInstance) member.getPet();
					penalty = summon.getExpPenalty();
				}
				
				// Pets that leech xp from the owner (like babypets) do not get rewarded directly
				if (member instanceof L2PetInstance)
				{
					if (((L2PetInstance) member).getPetData().getOwnerExpTaken() > 0)
						continue;
					
					penalty = (float) 0.85;
				}
				
				// Calculate and add the EXP and SP reward to the member
				if (validMembers.contains(member))
				{
					sqLevel = member.getLevel() * member.getLevel();
					preCalculation = (sqLevel / sqLevelSum) * (1 - penalty);
					
					// Add the XP/SP points to the requested party member
					if (!member.isDead())
						member.addExpAndSp(Math.round(xpReward * preCalculation), (int) (spReward * preCalculation));
				}
				else
					member.addExpAndSp(0, 0);
			}
		}
	}
	
	public void recalculatePartyLevel()
	{
		int newLevel = 0;
		for (L2PcInstance member : getPartyMembers())
		{
			if (member == null)
			{
				getPartyMembers().remove(member);
				continue;
			}
			
			if (member.getLevel() > newLevel)
				newLevel = member.getLevel();
		}
		_partyLvl = newLevel;
	}
	
	private static List<L2Playable> getValidMembers(List<L2Playable> members, int topLvl)
	{
		List<L2Playable> validMembers = new ArrayList<>();
		
		// Fixed LevelDiff cutoff point
		if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level"))
		{
			for (L2Playable member : members)
			{
				if (topLvl - member.getLevel() <= Config.PARTY_XP_CUTOFF_LEVEL)
					validMembers.add(member);
			}
		}
		// Fixed MinPercentage cutoff point
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("percentage"))
		{
			int sqLevelSum = 0;
			for (L2Playable member : members)
				sqLevelSum += (member.getLevel() * member.getLevel());
			
			for (L2Playable member : members)
			{
				int sqLevel = member.getLevel() * member.getLevel();
				if (sqLevel * 100 >= sqLevelSum * Config.PARTY_XP_CUTOFF_PERCENT)
					validMembers.add(member);
			}
		}
		// Automatic cutoff method
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("auto"))
		{
			int sqLevelSum = 0;
			for (L2Playable member : members)
				sqLevelSum += (member.getLevel() * member.getLevel());
			
			int i = members.size() - 1;
			if (i < 1)
				return members;
			
			if (i >= BONUS_EXP_SP.length)
				i = BONUS_EXP_SP.length - 1;
			
			for (L2Playable member : members)
			{
				int sqLevel = member.getLevel() * member.getLevel();
				if (sqLevel >= sqLevelSum * (1 - 1 / (1 + BONUS_EXP_SP[i] - BONUS_EXP_SP[i - 1])))
					validMembers.add(member);
			}
		}
		return validMembers;
	}
	
	private static double getBaseExpSpBonus(int membersCount)
	{
		int i = membersCount - 1;
		if (i < 1)
			return 1;
		
		if (i >= BONUS_EXP_SP.length)
			i = BONUS_EXP_SP.length - 1;
		
		return BONUS_EXP_SP[i];
	}
	
	private static double getExpBonus(int membersCount)
	{
		// Not a valid party
		if (membersCount < 2)
			return getBaseExpSpBonus(membersCount);
		
		return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_XP;
	}
	
	private static double getSpBonus(int membersCount)
	{
		// Not a valid party
		if (membersCount < 2)
			return getBaseExpSpBonus(membersCount);
		
		return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_SP;
	}
	
	public int getLevel()
	{
		return _partyLvl;
	}
	
	public PartyLootType getDistributionType()
	{
		return _itemDistribution;
	}
	
	public boolean isInCommandChannel()
	{
		return _commandChannel != null;
	}
	
	public L2CommandChannel getCommandChannel()
	{
		return _commandChannel;
	}
	
	public void setCommandChannel(L2CommandChannel channel)
	{
		_commandChannel = channel;
	}
	
	public boolean isInDimensionalRift()
	{
		return _dr != null;
	}
	
	public void setDimensionalRift(DimensionalRift dr)
	{
		_dr = dr;
	}
	
	public DimensionalRift getDimensionalRift()
	{
		return _dr;
	}
	
	public boolean getPendingInvitation()
	{
		return _pendingInvitation;
	}
	
	public void setPendingInvitation(boolean val)
	{
		_pendingInvitation = val;
		_pendingInviteTimeout = GameTimeController.getInstance().getGameTicks() + L2PcInstance.REQUEST_TIMEOUT * GameTimeController.TICKS_PER_SECOND;
	}
	
	public boolean isInvitationRequestExpired()
	{
		return !(_pendingInviteTimeout > GameTimeController.getInstance().getGameTicks());
	}
	
	public L2PcInstance getLeader()
	{
		return _members.get(0);
	}
	
	protected class PositionBroadcast implements Runnable
	{
		@Override
		public void run()
		{
			if (_positionPacket == null)
				_positionPacket = new PartyMemberPosition(L2Party.this);
			
			broadcastToPartyMembers(_positionPacket);
		}
	}
	
	double _mobdmg = 0;
	
	public void setMobDamage(double d)
	{
		_mobdmg = d;
	}
	
	public double getMobDamage()
	{
		return _mobdmg;
	}
	
	L2Attackable _mob;
	public L2PcInstance rewarded = null;
	
	public L2Attackable getLastMob()
	{
		return _mob;
	}
	
	public void setLastMob(L2Attackable m)
	{
		_mob = m;
	}
	
	public boolean containsPlayer(L2PcInstance player)
	{
		return _members.contains(player);
	}
}