package Extensions.RankSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class RewardTable
{
	public static final Logger log = Logger.getLogger(RewardTable.class.getSimpleName());
	
	private static RewardTable _instance = null;
	
	private Map<Integer, RankReward> _rankRewardList = new HashMap<>();
	
	private RewardTable()
	{
		long startTime = Calendar.getInstance().getTimeInMillis();
		
		load();
		
		long endTime = Calendar.getInstance().getTimeInMillis();
		
		log.log(Level.INFO, " - RewardTable: Data loaded. " + _rankRewardList.size() + " objects in " + (endTime - startTime) + " ms.");
	}
	
	public static RewardTable getInstance()
	{
		if (_instance == null)
			_instance = new RewardTable();
		
		return _instance;
	}
	
	public Map<Integer, RankReward> getRankRewardList()
	{
		return _rankRewardList;
	}
	
	public void setRankRewardList(Map<Integer, RankReward> rankRewardList)
	{
		_rankRewardList = rankRewardList;
	}
	
	public Map<Integer, Reward> getRankPvpRewardList(L2PcInstance player)
	{
		int rankId = PvpTable.getInstance().getRankId(player.getObjectId());
		
		RankReward rr = _rankRewardList.get(rankId);
		
		if (rr != null)
			return rr.getRankPvpRewardList();
		
		return new HashMap<>();
	}
	
	public Map<Integer, Reward> getRankPvpRewardList(int rankId)
	{
		RankReward rr = _rankRewardList.get(rankId);
		
		if (rr != null)
			return rr.getRankPvpRewardList();
		
		return new HashMap<>();
	}
	
	public void giveRankLevelRewards(L2PcInstance player, int newRankId)
	{
		if (player == null)
			return;
		
		// overloads the inventory, there is no repeat method for this action. Reward is given only once (in rank level time).
		
		// add items into player's inventory:
		RankReward rr = _rankRewardList.get(newRankId);
		
		if (rr == null)
			return;
		
		for (Map.Entry<Integer, Reward> e : rr.getRankLevelRewardList().entrySet())
		{
			Reward reward = e.getValue();
			
			if (reward != null)
				player.addItem("Reward", reward.getItemId(), reward.getItemAmount(), player, true);
		}
	}
	
	public void giveRankPvpRewards(L2PcInstance player, int victimRankId)
	{
		if (player == null)
			return;
		
		// overloads inventory, there is no repeat method for this action. Reward is given only in rank level time.
		
		// add items into player's inventory:
		RankReward rr = _rankRewardList.get(victimRankId);
		
		if (rr == null)
			return;
		
		for (Map.Entry<Integer, Reward> e : rr.getRankPvpRewardList().entrySet())
		{
			Reward reward = e.getValue();
			
			if (reward != null)
				player.addItem("Reward", reward.getItemId(), reward.getItemAmount(), player, true);
		}
	}
	
	public void giveRankSkillRewards(L2PcInstance player, int newRankId)
	{
		if (player == null)
			return;
		
		// add items into player's inventory:
		RankReward rr = _rankRewardList.get(newRankId);
		
		if (rr == null)
			return;
		
		for (Map.Entry<Integer, SkillReward> e : rr.getRankSkillRewardList().entrySet())
		{
			SkillReward skillReward = e.getValue();
			
			if (skillReward != null)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(skillReward.getSkillId(), skillReward.getSkillLevel());
				if (skill != null)
					player.addSkill(skill, true);
			}
		}
		
		player.sendSkillList();
	}
	
	public void giveReward(L2PcInstance player)
	{
		if (player == null || Config.PVP_REWARD_ID <= 0 || Config.PVP_REWARD_AMOUNT <= 0)
			return;
		
		// overloads inventory, there is no repeat method for this action. Reward is given only in rank level time.
		player.addItem("Reward", Config.PVP_REWARD_ID, Config.PVP_REWARD_AMOUNT, player, true);
	}
	
	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// PvP + Level Rewards
			PreparedStatement statement = con.prepareStatement("SELECT * FROM rank_pvp_system_rank_reward ORDER BY id ASC");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				Reward r = new Reward();
				
				r.setId(rset.getInt("id"));
				r.setItemId(rset.getInt("item_id"));
				r.setItemAmount(rset.getLong("item_amount"));
				r.setRankId(rset.getInt("rank_id"));
				
				if (Config.RANK_PVP_REWARD_ENABLED && rset.getBoolean("is_pvp"))
				{
					RankReward rr = _rankRewardList.get(r.getRankId());
					
					if (rr == null)
					{
						rr = new RankReward();
						_rankRewardList.put(r.getRankId(), rr);
						rr.addRankPvpReward(r);
					}
					else
						rr.addRankPvpReward(r);
				}
				if (Config.RANK_LEVEL_REWARD_ENABLED && rset.getBoolean("is_level"))
				{
					RankReward rr = _rankRewardList.get(r.getRankId());
					
					if (rr == null)
					{
						rr = new RankReward();
						_rankRewardList.put(r.getRankId(), rr);
						rr.addRankLevelReward(r);
					}
					else
						rr.addRankLevelReward(r);
				}
			}
			
			rset.close();
			statement.close();
			
			// Skill Rewards
			if (Config.RANK_SKILL_REWARD_ENABLED)
			{
				statement = con.prepareStatement("SELECT * FROM rank_pvp_system_rank_skill ORDER BY id ASC");
				rset = statement.executeQuery();
				
				while (rset.next())
				{
					SkillReward r = new SkillReward();
					r.setId(rset.getInt("id"));
					r.setSkillId(rset.getInt("skill_id"));
					r.setSkillLevel(rset.getInt("skill_level"));
					r.setRankId(rset.getInt("rank_id"));
					
					RankReward rr = _rankRewardList.get(r.getRankId());
					
					if (rr == null)
					{
						rr = new RankReward();
						_rankRewardList.put(r.getRankId(), rr);
						rr.addRankSkillReward(r);
					}
					else
						rr.addRankSkillReward(r);
				}
				
				rset.close();
				statement.close();
			}
		}
		catch (SQLException e)
		{
			log.log(Level.WARNING, e.getMessage());
		}
	}
	
	protected class RankReward
	{
		// [RewardId, Object]
		private final Map<Integer, Reward> _rankLevelRewardList = new HashMap<>();
		private final Map<Integer, Reward> _rankPvpRewardList = new HashMap<>();
		private final Map<Integer, SkillReward> _rankSkillRewardList = new HashMap<>();
		
		public void addRankLevelReward(Reward reward)
		{
			_rankLevelRewardList.put(reward.getId(), reward);
		}
		
		public void addRankPvpReward(Reward reward)
		{
			_rankPvpRewardList.put(reward.getId(), reward);
		}
		
		public void addRankSkillReward(SkillReward skillReward)
		{
			_rankSkillRewardList.put(skillReward.getId(), skillReward);
		}
		
		public Map<Integer, Reward> getRankLevelRewardList()
		{
			return _rankLevelRewardList;
		}
		
		public Map<Integer, Reward> getRankPvpRewardList()
		{
			return _rankPvpRewardList;
		}
		
		public Map<Integer, SkillReward> getRankSkillRewardList()
		{
			return _rankSkillRewardList;
		}
	}
	
	protected class Reward
	{
		private int _id = 0; // reward id
		private int _itemId = 0; // game item id
		private long _itemAmount = 0; // amount of the game item
		private int _rankId = 0; // required rank id
		
		public int getId()
		{
			return _id;
		}
		
		public void setId(int id)
		{
			_id = id;
		}
		
		public int getItemId()
		{
			return _itemId;
		}
		
		public void setItemId(int itemId)
		{
			_itemId = itemId;
		}
		
		public long getItemAmount()
		{
			return _itemAmount;
		}
		
		public void setItemAmount(long itemAmount)
		{
			_itemAmount = itemAmount;
		}
		
		public int getRankId()
		{
			return _rankId;
		}
		
		public void setRankId(int rankId)
		{
			_rankId = rankId;
		}
	}
	
	protected class SkillReward
	{
		private int _id = 0; // reward id
		private int _skillId = 0; // skill id
		private int _skillLevel = 0; // skill level
		private int _rankId = 0; // required rank id
		
		public int getId()
		{
			return _id;
		}
		
		public void setId(int id)
		{
			_id = id;
		}
		
		public int getSkillId()
		{
			return _skillId;
		}
		
		public void setSkillId(int skillId)
		{
			_skillId = skillId;
		}
		
		public int getSkillLevel()
		{
			return _skillLevel;
		}
		
		public void setSkillLevel(int skillLevel)
		{
			_skillLevel = skillLevel;
		}
		
		public int getRankId()
		{
			return _rankId;
		}
		
		public void setRankId(int rankId)
		{
			_rankId = rankId;
		}
	}
}