package Extensions.RankSystem;

import java.util.Calendar;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.UserInfo;

public class RankPvpSystem
{
	private L2PcInstance _killer = null;
	private L2PcInstance _victim = null;
	
	private final long _protectionTime = Config.PROTECTION_TIME_RESET;
	private boolean _protectionTimeEnabled = false;
	
	public RankPvpSystem(L2PcInstance killer, L2PcInstance victim)
	{
		_victim = victim;
		_killer = killer;
	}
	
	public synchronized void doPvp()
	{
		if (checkBasicConditions(_killer, _victim))
		{
			// set pvp times:
			Calendar c = Calendar.getInstance();
			long systemTime = c.getTimeInMillis(); // date & time
			
			c.set(Calendar.MILLISECOND, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.HOUR, 0);
			long systemDay = c.getTimeInMillis(); // date
			
			// get killer - victim pvp:
			Pvp pvp = PvpTable.getInstance().getPvp(_killer.getObjectId(), _victim.getObjectId(), systemDay, false);
			
			// check time protection:
			_protectionTimeEnabled = checkProtectionTime(pvp, systemTime);
			
			String nextRewardTime = "";
			if (_protectionTimeEnabled)
			{
				nextRewardTime = calculateTimeToString(systemTime, pvp.getKillTime());
			}
			
			// get Killer and Victim pvp stats:
			PvpSummary killerPvpSummary = PvpTable.getInstance().getKillerPvpSummary(_killer.getObjectId(), systemDay, false, false);
			PvpSummary victimPvpSummary = PvpTable.getInstance().getKillerPvpSummary(_victim.getObjectId(), systemDay, true, false);
			
			// update pvp:
			increasePvp(pvp, killerPvpSummary, systemTime, systemDay);
			
			// update killer Alt+T info.
			if (Config.PVP_COUNTER_FOR_ALTT_ENABLED)
			{
				if (Config.PVP_COUNTER_FOR_ALTT_LEGAL_KILLS_ONLY)
					_killer.setPvpKills(killerPvpSummary.getTotalKillsLegal());
				else
					_killer.setPvpKills(killerPvpSummary.getTotalKills());
				
				_killer.sendPacket(new UserInfo(_killer));
				_killer.broadcastUserInfo();
			}
			
			// start message separator:
			_killer.sendMessage("----------------------------------------------------------------");
			_victim.sendMessage("----------------------------------------------------------------");
			
			int[] pvpPointsTable = null; // used for check if points are added for decrease pvp exp system.
			
			// PvP Reward || Rank PvP Reward || RPC || RP
			if (checkRewardProtections(pvp))
			{
				// give PvP Reward:
				if (checkPvpRewardConditions(pvp))
					RewardTable.getInstance().giveReward(_killer);
				
				// give Rank PvP Reward:
				if (checkRankPvpRewardConditions(pvp))
					RewardTable.getInstance().giveRankPvpRewards(_killer, victimPvpSummary.getRankId());
				
				// add RPC:
				if (checkRpcConditions(pvp))
					RPCTable.getInstance().addRpcForPlayer(_killer.getObjectId(), Config.RPC_REWARD_AMOUNT);
				
				// add RP:
				if (checkRankPointsConditions(pvp))
					pvpPointsTable = addRankPointsForKiller(pvp, killerPvpSummary, victimPvpSummary);
			}
			
			// decrease PvP Exp (if not decreased before):
			if (Config.PVP_EXP_DECREASE_ENABLED && !Config.PVP_EXP_DECREASE_ON_LEGAL_KILL_ENABLED && pvpPointsTable == null)
			{
				pvpPointsTable = getPointsForKill(pvp, killerPvpSummary, victimPvpSummary, _killer, _victim);
				int loseExp = victimPvpSummary.decreasePvpExpBy(pvpPointsTable);
				
				if (loseExp > 0)
					_victim.sendMessage("You lose " + loseExp + " PvP exp.");
			}
			
			if (_protectionTimeEnabled)
				_killer.sendMessage("Protection Time is activated for: " + nextRewardTime);
			
			// update nick and title colors:
			updateNickAndTitleColor(_killer, killerPvpSummary);
			
			// show message:
			shoutPvpMessage(pvp);
			
			// end message separator:
			_killer.sendMessage("----------------------------------------------------------------");
			_victim.sendMessage("----------------------------------------------------------------");
			
			if (Config.DEATH_MANAGER_DETAILS_ENABLED)
				_victim.getRPSCookie().setDeathStatus(new RPSHtmlDeathStatus(_killer));
			
			if (Config.PVP_INFO_COMMAND_ON_DEATH_ENABLED)
			{
				if (!RPSProtection.isInDMRestrictedZone(_killer))
					RPSHtmlPvpStatus.sendPage(_victim, _killer);
			}
			
			// shout defeat message, if victim have combo level > 0
			if (_victim.getRPSCookie().isComboKillActive())
			{
				_victim.getRPSCookie().getComboKill().shoutDefeatMessage(_victim);
				_victim.getRPSCookie().setComboKill(null); // reset current combo for victim.
			}
		}
	}
	
	private void increasePvp(Pvp pvp, PvpSummary killerPvpSummary, long systemTime, long systemDay)
	{
		// killerPvpSummary today fields are updated on the end.
		
		// add normal kills, checking is outside this method:
		pvp.increaseKills();
		killerPvpSummary.addTotalKills(1);
		
		if (pvp.getKillDay() == systemDay) // daily
			pvp.increaseKillsToday();
		else
			pvp.setKillsToday(1);
		
		if (RPSProtection.checkWar(_killer, _victim))
			killerPvpSummary.addTotalWarKills(1);
		
		// shout combo kill, if legal kill protection is disabled:
		if (Config.COMBO_KILL_ENABLED && !Config.COMBO_KILL_PROTECTION_WITH_LEGAL_KILL_ENABLED)
			shoutComboKill(systemTime);
		
		if (checkLegalKillConditions(_killer, _victim, pvp))
		{
			if (!_protectionTimeEnabled)
			{
				pvp.increaseKillsLegal();
				killerPvpSummary.addTotalKillsLegal(1);
				
				if (RPSProtection.checkWar(_killer, _victim))
					killerPvpSummary.addTotalWarKillsLegal(1);
				
				if (pvp.getKillDay() == systemDay)
					pvp.increaseKillsLegalToday(); // daily
				else
					pvp.setKillsLegalToday(1); // daily
					
				// shout combo kill, if legal kill protection is enabled:
				if (Config.COMBO_KILL_ENABLED && Config.COMBO_KILL_PROTECTION_WITH_LEGAL_KILL_ENABLED)
					shoutComboKill(systemTime);
				
				// if protection is OFF set the current kill time.
				pvp.setKillTime(systemTime);
			}
		}
		
		if (pvp.getKillTime() == 0) // set last kill time if it is initial kill.
			pvp.setKillTime(systemTime);
		
		pvp.setKillDay(systemDay);
		
		// update daily fields for killerPvpSummary:
		killerPvpSummary.updateDailyStats(systemDay);
		
		// used for check not active killers on top list filter:
		killerPvpSummary.setLastKillTime(systemTime);
	}
	
	private int[] addRankPointsForKiller(Pvp pvp, PvpSummary killerPvpSummary, PvpSummary victimPvpSummary)
	{
		int[] points_table = getPointsForKill(pvp, killerPvpSummary, victimPvpSummary, _killer, _victim);
		
		// old rank id:
		int oldRankId = killerPvpSummary.getRankId();
		int oldMaxRankId = killerPvpSummary.getMaxRankId();
		
		// increase rank points:
		pvp.increaseRankPointsBy(points_table[0]);
		pvp.increaseRankPointsTodayBy(points_table[0]);
		
		// required update this object for increasePvp() (only in this method):
		killerPvpSummary.addTotalRankPoints(points_table[0]);
		killerPvpSummary.addTotalRankPointsToday(points_table[0]); // required for show in chat below.
		killerPvpSummary.increasePvpExp(points_table[0]); // add pvp exp, and update rankId and maxRankId.
		
		// decrease victim PvP experience:
		int loseExp = 0;
		if (Config.PVP_EXP_DECREASE_ENABLED && Config.PVP_EXP_DECREASE_ON_LEGAL_KILL_ENABLED)
			loseExp = victimPvpSummary.decreasePvpExpBy(points_table);
		
		// add rank RPC for killer:
		if (Config.RANK_RPC_ENABLED)
		{
			// cut RPC if enabled:
			if (Config.RANK_POINTS_CUT_ENABLED && killerPvpSummary.getRank().getRpc() < victimPvpSummary.getRank().getRpc())
				RPCTable.getInstance().addRpcForPlayer(_killer.getObjectId(), killerPvpSummary.getRank().getRpc());
			else
				RPCTable.getInstance().addRpcForPlayer(_killer.getObjectId(), victimPvpSummary.getRank().getRpc());
		}
		
		// new rank shout (include deleveled ranks):
		if (oldRankId < killerPvpSummary.getRankId())
		{
			_killer.sendMessage("You have reached a new rank: " + RankTable.getInstance().getRankById(killerPvpSummary.getRankId()).getName());
			
			// give rank rewards and skill rewards for new ranks (exclude deleveled ranks):
			if (Config.RANK_LEVEL_REWARD_ENABLED || Config.RANK_SKILL_REWARD_ENABLED)
			{
				// if player reached 1 or more new ranks.
				// oldRankId+1 because we want get reward for new rank.
				for (int i = oldRankId + 1; i <= killerPvpSummary.getRankId(); i++)
				{
					if (i > oldMaxRankId)
					{
						if (Config.RANK_LEVEL_REWARD_ENABLED)
							RewardTable.getInstance().giveRankLevelRewards(_killer, i);
						
						if (Config.RANK_SKILL_REWARD_ENABLED)
							RewardTable.getInstance().giveRankSkillRewards(_killer, i);
					}
				}
			}
		}
		
		// shout current PvP informations:
		if (Config.RANK_SHOUT_INFO_ON_KILL_ENABLED)
		{
			_killer.sendMessage("You have obtained " + points_table[0] + " Rank Points for kill " + _victim.getName());
			
			showBonusDataPointsForKiller(points_table);
			
			_killer.sendMessage("Your Rank Points: " + killerPvpSummary.getTotalRankPoints() + " (" + killerPvpSummary.getTotalRankPointsToday() + " today)");
			_victim.sendMessage("You have been killed by " + _killer.getName() + " (" + killerPvpSummary.getRank().getName() + ")");
		}
		
		if (loseExp > 0)
			_victim.sendMessage("You lose " + loseExp + " PvP exp.");
		
		return points_table;
	}
	
	private void shoutPvpMessage(Pvp pvp)
	{
		
		if (Config.TOTAL_KILLS_IN_SHOUT_ENABLED)
		{
			if (pvp.getKills() > 1)
			{
				String timeStr1 = " times";
				if (pvp.getKillsToday() == 1)
				{
					timeStr1 = "st time";
				}
				
				if (Config.PROTECTION_TIME_RESET == 0)
				{
					_victim.sendMessage(_killer.getName() + " killed you " + pvp.getKills() + " times");
					_killer.sendMessage("You have killed " + _victim.getName() + " " + pvp.getKills() + " times");
				}
				else
				{
					_victim.sendMessage(_killer.getName() + " killed you " + pvp.getKills() + " times (" + pvp.getKillsToday() + "" + timeStr1 + " today)");
					_killer.sendMessage("You have killed " + _victim.getName() + " " + pvp.getKills() + " times (" + pvp.getKillsToday() + "" + timeStr1 + " today)");
				}
			}
			else
			{
				_victim.sendMessage("This is the first time you have been killed by " + _killer.getName());
				_killer.sendMessage("You have killed " + _victim.getName() + " for the first time");
			}
		}
		else
		{
			if (pvp.getKillsLegal() > 1)
			{
				String timeStr1 = " times";
				if (pvp.getKillsLegalToday() == 1)
				{
					timeStr1 = "st time";
				}
				
				if (Config.PROTECTION_TIME_RESET == 0)
				{
					_victim.sendMessage(_killer.getName() + " killed you " + pvp.getKillsLegal() + " times legally");
					_killer.sendMessage("You have killed " + _victim.getName() + " " + pvp.getKillsLegal() + " times legally");
				}
				else
				{
					_victim.sendMessage(_killer.getName() + " killed you " + pvp.getKillsLegal() + " times (" + pvp.getKillsLegalToday() + "" + timeStr1 + " today) legally");
					_killer.sendMessage("You have killed " + _victim.getName() + " " + pvp.getKillsLegal() + " times (" + pvp.getKillsLegalToday() + "" + timeStr1 + " today) legally");
				}
			}
			else
			{
				_victim.sendMessage("This is the first time you have been killed by " + _killer.getName() + " legally.");
				_killer.sendMessage("You have killed " + _victim.getName() + " for the first time legally.");
			}
		}
		
	}
	
	private void showBonusDataPointsForKiller(int[] points_table)
	{
		// show bonus points data for killer:
		String war = "";
		String area = "";
		String combo = "";
		
		if (points_table[1] > 0)
			war = "war: " + points_table[1] + ", ";
		
		if (points_table[2] > 0)
			area = "area: " + points_table[2] + ", ";
		
		if (points_table[3] > 0)
			combo = "combo: " + points_table[3] + ", ";
		
		if (points_table[1] > 0 || points_table[2] > 0 || points_table[3] > 0)
		{
			String msg = war + area + combo;
			msg = msg.substring(0, msg.length() - 2);
			
			_killer.sendMessage("Bonus RP (" + msg + ")");
		}
	}
	
	public static void updateNickAndTitleColor(L2PcInstance killer, PvpSummary killerPvpSummary)
	{
		if (killer == null)
			return;
		
		if (!Config.GM_IGNORE_ENABLED && killer.isGM())
			return;
		
		PvpSummary PvpSummary = killerPvpSummary;
		
		if (PvpSummary == null)
		{
			PvpSummary = PvpTable.getInstance().getKillerPvpSummary(killer.getObjectId(), true, false);
			if (PvpSummary == null)
				return;
		}
		
		Rank rank = PvpSummary.getRank();
		
		if (rank == null)
			return;
		
		if (Config.NICK_COLOR_ENABLED && killer.getAppearance().getNameColor() != rank.getNickColor() && rank.getNickColor() > -1)
		{
			killer.getAppearance().setNameColor(rank.getNickColor());
			killer.sendPacket(new UserInfo(killer));
			killer.broadcastUserInfo();
		}
		
		if (Config.TITLE_COLOR_ENABLED && killer.getAppearance().getTitleColor() != rank.getTitleColor() && rank.getTitleColor() > -1)
		{
			killer.getAppearance().setTitleColor(rank.getTitleColor());
			killer.broadcastTitleInfo();
		}
	}
	
	public static final String calculateTimeToString(long sys_time, long kill_time)
	{
		long TimeToRewardInMilli = ((kill_time + (Config.PROTECTION_TIME_RESET)) - sys_time);
		long TimeToRewardHours = TimeToRewardInMilli / 3600000;
		long TimeToRewardMinutes = (TimeToRewardInMilli % 3600000) / 60000;
		long TimeToRewardSeconds = (TimeToRewardInMilli % 60000) / 1000;
		
		String H = Long.toString(TimeToRewardHours);
		String M = Long.toString(TimeToRewardMinutes);
		String S = Long.toString(TimeToRewardSeconds);
		
		if (TimeToRewardHours <= 9)
			H = "0" + H;
		if (TimeToRewardMinutes <= 9)
			M = "0" + M;
		if (TimeToRewardSeconds <= 9)
			S = "0" + S;
		
		return H + ":" + M + ":" + S;
	}
	
	private static int[] getPointsForKill(Pvp pvp, PvpSummary killerPvpSummary, PvpSummary victimPvpSummary, L2PcInstance killer, L2PcInstance victim)
	{
		
		int points = 0;
		int points_war = 0;
		int points_bonus_zone = 0;
		int points_combo = 0;
		
		// add basic points:
		if (Config.RANK_POINTS_DOWN_COUNT_ENABLED)
		{
			int i = 1;
			
			for (Integer value : Config.RANK_POINTS_DOWN_AMOUNTS)
			{
				if (pvp.getKillsLegalToday() == i)
				{
					points = value;
					break;
				}
				i++;
			}
		}
		else
		{
			points = victimPvpSummary.getRank().getPointsForKill();
		}
		
		// cut points if enabled:
		if (Config.RANK_POINTS_CUT_ENABLED && killerPvpSummary.getRank().getPointsForKill() < points)
			points = killerPvpSummary.getRank().getPointsForKill();
		
		// add war points, if Killer's clan and Victim's clan at war:
		if (Config.WAR_KILLS_ENABLED && points > 0 && Config.WAR_RANK_POINTS_RATIO > 1 && RPSProtection.checkWar(killer, victim))
			points_war = (int) Math.floor((points * Config.WAR_RANK_POINTS_RATIO) - points);
		
		// add bonus zone points, if Killer is inside bonus zone:
		if (points > 0)
		{
			double zone_ratio_killer = RPSProtection.getZoneBonusRatio(killer);
			if (zone_ratio_killer > 1)
				points_bonus_zone = (int) Math.floor((points * zone_ratio_killer) - points);
		}
		
		// add combo points:
		if (Config.COMBO_KILL_RANK_POINTS_RATIO_ENABLED && killer.getRPSCookie().getComboKill() != null)
		{
			double combo_ratio = killer.getRPSCookie().getComboKill().getComboKillRankPointsRatio();
			if (combo_ratio > 1)
				points_combo = (int) Math.floor((points * combo_ratio) - points);
		}
		
		points = points + points_war + points_bonus_zone + points_combo;
		
		int[] points_table = new int[4];
		points_table[0] = points;
		points_table[1] = points_war;
		points_table[2] = points_bonus_zone;
		points_table[3] = points_combo;
		
		return points_table;
	}
	
	private void shoutComboKill(long killTime)
	{
		// create new combo instance if not exists:
		if (_killer.getRPSCookie().getComboKill() == null)
		{
			_killer.getRPSCookie().setComboKill(new RPSHtmlComboKill());
		}
		// reset old combo if kill reseter enabled
		else if (Config.COMBO_KILL_RESETER > 0 && (killTime - _killer.getRPSCookie().getComboKill().getLastKillTime()) > Config.COMBO_KILL_RESETER * 1000)
		{
			_killer.getRPSCookie().setComboKill(new RPSHtmlComboKill());
		}
		
		// rise combo level and shout message:
		if (_killer.getRPSCookie().getComboKill().addVictim(_victim.getObjectId(), killTime))
		{
			_killer.getRPSCookie().getComboKill().shoutComboKill(_killer, _victim);
		}
	}
	
	private boolean checkProtectionTime(Pvp pvp, long systemTime)
	{
		if (Config.PROTECTION_TIME_RESET > 0 && pvp.getKillTime() + _protectionTime > systemTime)
			return true;
		
		return false;
	}
	
	private static boolean checkBasicConditions(final L2PcInstance killer, final L2PcInstance victim)
	{
		
		if (killer == null || victim == null)
			return false;
		
		if (killer.isDead() || killer.isAlikeDead())
			return false;
		
		if (RPSProtection.checkEvent(killer))
			return false;
		
		if (Config.GM_IGNORE_ENABLED && (killer.isGM() || victim.isGM()))
		{
			killer.sendMessage("Rank PvP System ignore GM characters!");
			return false;
		}
		
		// check if killer is in allowed zone & not in restricted zone:
		if (!RPSProtection.isInPvpAllowedZone(killer) || RPSProtection.isInPvpRestrictedZone(killer))
		{
			if ((Config.RPC_REWARD_ENABLED || Config.PVP_REWARD_ENABLED || Config.RANK_PVP_REWARD_ENABLED) && Config.RANKS_ENABLED)
			{
				killer.sendMessage("You can't earn Reward or Rank Points in restricted zone");
				return false;
			}
			return false;
		}
		
		if (!RPSProtection.antiFarmCheck(killer, victim))
			return false;
		return true;
	}
	
	private boolean checkLegalKillProtection(Pvp pvp)
	{
		// 1: check total legal kills:
		if (Config.LEGAL_KILL_PROTECTION > 0 && pvp.getKillsLegal() > Config.LEGAL_KILL_PROTECTION)
			return false;
		
		// 2: check total legal kills today:
		if (Config.DAILY_LEGAL_KILL_PROTECTION > 0 && pvp.getKillsLegalToday() > Config.DAILY_LEGAL_KILL_PROTECTION)
			return false;
		// 3. check protectionTimeEnabled
		if (_protectionTimeEnabled)
			return false;
		return true;
	}
	
	private boolean checkRewardProtections(Pvp pvp)
	{
		// if PK mode is disabled:
		if (!Config.REWARD_FOR_INNOCENT_KILL_ENABLED && _victim.getPvpFlag() == 0 && _victim.getKarma() == 0)
		{
			_killer.sendMessage("You can't earn reward on innocent players!");
			return false;
		}
		
		// if reward for PK kill is disabled:
		if (!Config.REWARD_FOR_PK_KILLER_ENABLED && _victim.getKarma() > 0)
		{
			_killer.sendMessage("No reward for kill player with Karma!");
			return false;
		}
		
		if (Config.REWARD_LEGAL_KILL_ENABLED && !checkLegalKillProtection(pvp))
			return false;
		
		return true;
	}
	
	private boolean checkLegalKillConditions(L2PcInstance killer, L2PcInstance victim, Pvp pvp)
	{
		if ((Config.LEGAL_KILL_MIN_LVL > victim.getLevel()) || (Config.LEGAL_KILL_MIN_LVL > killer.getLevel()))
			return false;
		
		if (!Config.LEGAL_KILL_FOR_INNOCENT_KILL_ENABLED && victim.getKarma() == 0 && victim.getPvpFlag() == 0)
			return false;
		
		if (!Config.LEGAL_KILL_FOR_PK_KILLER_ENABLED && victim.getKarma() > 0)
			return false;
		
		if (!checkLegalKillProtection(pvp))
			return false;
		return true;
	}
	
	private boolean checkRpcConditions(Pvp pvp)
	{
		if (!Config.RPC_REWARD_ENABLED)
			return false;
		
		if ((Config.RPC_REWARD_MIN_LVL > _victim.getLevel()) || (Config.RPC_REWARD_MIN_LVL > _killer.getLevel()))
		{
			_killer.sendMessage("You or your target have not required level!");
			return false;
		}
		return true;
	}
	
	private boolean checkPvpRewardConditions(Pvp pvp)
	{
		
		if (!Config.PVP_REWARD_ENABLED)
			return false;
		if ((Config.PVP_REWARD_MIN_LVL > _victim.getLevel()) || (Config.PVP_REWARD_MIN_LVL > _killer.getLevel()))
		{
			_killer.sendMessage("You or your target have not required level!");
			return false;
		}
		return true;
	}
	
	private boolean checkRankPvpRewardConditions(Pvp pvp)
	{
		
		if (!Config.RANK_PVP_REWARD_ENABLED)
			return false;
		if ((Config.RANK_PVP_REWARD_MIN_LVL > _victim.getLevel()) || (Config.RANK_PVP_REWARD_MIN_LVL > _killer.getLevel()))
		{
			_killer.sendMessage("You or your target have not required level!");
			return false;
		}
		return true;
	}
	
	private boolean checkRankPointsConditions(Pvp pvp)
	{
		if (!Config.RANKS_ENABLED)
			return false;
		
		if ((Config.RANK_POINTS_MIN_LVL > _victim.getLevel()) || (Config.RANK_POINTS_MIN_LVL > _killer.getLevel()))
		{
			_killer.sendMessage("You or your target have not required level!");
			return false;
		}
		return true;
	}
	
	public L2PcInstance getKiller()
	{
		return _killer;
	}
	
	public void setKiller(L2PcInstance killer)
	{
		_killer = killer;
	}
	
	public L2PcInstance getVictim()
	{
		return _victim;
	}
	
	public void setVictim(L2PcInstance victim)
	{
		_victim = victim;
	}
}