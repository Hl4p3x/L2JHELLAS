package Extensions.RankSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.util.Broadcast;

public class RPSHtmlComboKill
{
	private List<Integer> _victims = new ArrayList<>(); // list of victims id's
	private int _comboLevel = 0; // contains real combo size, do not use _victims.size() !!!
	private long _lastKillTime = 0; // the time of the last kill counted to combo, this is not standard PvP kill time !
	
	// combo shout definitions:
	public static final int SAY_TYPE = ChatType.HERO_VOICE.getClientId(); // local or global area are defined by sending methods.
	
	public boolean addVictim(int victimId, long killTime)
	{
		if (!Config.COMBO_KILL_PROTECTION_NO_REPEAT_ENABLED)
		{
			_comboLevel++;
			_lastKillTime = killTime;
			
			return true;
		}
		
		// else if Config.COMBO_KILL_PROTECTION_NO_REPEAT_ENABLED:
		if (!_victims.contains(victimId))
		{
			_victims.add(victimId);
			_comboLevel++;
			_lastKillTime = killTime;
			
			return true;
		}
		
		return false;
	}
	
	public void shoutComboKill(L2PcInstance killer, L2PcInstance victim)
	{
		String msg = null;
		
		CreatureSay cs;
		
		if (!Config.COMBO_KILL_ALT_MESSAGES_ENABLED)
		{
			if (Config.COMBO_KILL_LOCAL_AREA_MESSAGES.containsKey(getComboLevel()))
			{
				msg = Config.COMBO_KILL_LOCAL_AREA_MESSAGES.get(getComboLevel());
				msg = msg.replace("%killer%", killer.getName());
				msg = msg.replace("%victim%", victim.getName());
				msg = msg.replace("%combo_level%", Integer.toString(getComboLevel()));
				
				cs = new CreatureSay(0, SAY_TYPE, "", msg);
				
				Broadcast.toSelfAndKnownPlayers(killer, cs);
			}
			else if (Config.COMBO_KILL_GLOBAL_AREA_MESSAGES.containsKey(getComboLevel()))
			{
				msg = Config.COMBO_KILL_GLOBAL_AREA_MESSAGES.get(getComboLevel());
				msg = msg.replace("%killer%", killer.getName());
				msg = msg.replace("%victim%", victim.getName());
				msg = msg.replace("%combo_level%", Integer.toString(getComboLevel()));
				
				cs = new CreatureSay(0, SAY_TYPE, "", msg);
				
				Broadcast.toAllOnlinePlayers(cs);
			}
			else
			{
				// global have higher priority than local.
				Entry<Integer, String> last = null;
				
				for (Entry<Integer, String> value : Config.COMBO_KILL_GLOBAL_AREA_MESSAGES.entrySet())
				{
					last = value;
				}
				
				if (last != null && last.getKey() != null && getComboLevel() > last.getKey())
				{
					// if combo size greater than global max key.
					msg = last.getValue();
					msg = msg.replace("%killer%", killer.getName());
					msg = msg.replace("%victim%", victim.getName());
					msg = msg.replace("%combo_level%", Integer.toString(getComboLevel()));
					
					cs = new CreatureSay(0, SAY_TYPE, "", msg);
					
					Broadcast.toAllOnlinePlayers(cs);
				}
				else if (last != null && last.getKey() != null && getComboLevel() > last.getKey())
				{
					// if combo size greater than local max key.
					msg = last.getValue();
					msg = msg.replace("%killer%", killer.getName());
					msg = msg.replace("%victim%", victim.getName());
					msg = msg.replace("%combo_level%", Integer.toString(getComboLevel()));
					
					cs = new CreatureSay(0, SAY_TYPE, "", msg);
					
					Broadcast.toSelfAndKnownPlayers(killer, cs);
				}
			}
		}
		else
		{
			if (getComboLevel() > 1)
			{
				if (Config.COMBO_KILL_ALT_GLOBAL_MESSAGE_LVL > 0 && getComboLevel() >= Config.COMBO_KILL_ALT_GLOBAL_MESSAGE_LVL)
				{
					msg = Config.COMBO_KILL_ALT_MESSAGE;
					msg = msg.replace("%killer%", killer.getName());
					msg = msg.replace("%victim%", victim.getName());
					msg = msg.replace("%combo_level%", Integer.toString(getComboLevel()));
					
					cs = new CreatureSay(0, SAY_TYPE, "", msg);
					
					Broadcast.toAllOnlinePlayers(cs);
				}
				else
				{
					msg = Config.COMBO_KILL_ALT_MESSAGE;
					msg = msg.replace("%killer%", killer.getName());
					msg = msg.replace("%victim%", victim.getName());
					msg = msg.replace("%combo_level%", Integer.toString(getComboLevel()));
					
					cs = new CreatureSay(0, SAY_TYPE, "", msg);
					
					Broadcast.toSelfAndKnownPlayers(killer, cs);
				}
			}
		}
	}
	
	public void shoutDefeatMessage(L2PcInstance killer)
	{
		if (Config.COMBO_KILL_DEFEAT_MESSAGE_ENABLED)
		{
			if (getComboLevel() >= Config.COMBO_KILL_DEFEAT_MESSAGE_MIN_LVL)
			{
				String msg = Config.COMBO_KILL_DEFEAT_MESSAGE;
				msg = msg.replace("%killer%", killer.getName());
				msg = msg.replace("%combo_level%", Integer.toString(getComboLevel()));
				
				CreatureSay cs = new CreatureSay(0, SAY_TYPE, "", msg);
				
				Broadcast.toKnownPlayers(killer, cs);
			}
		}
	}
	
	public double getComboKillRankPointsRatio()
	{
		if (getComboLevel() > 0)
		{
			Map<Integer, Double> list = Config.COMBO_KILL_RANK_POINTS_RATIO;
			
			// checking if combo size is in combo rank points ratio table:
			if (list.containsKey(_comboLevel))
			{
				return list.get(_comboLevel);
			}
			
			// if not, then check the last element of table.
			// Reason: combo size can be greater than max table value, then killer should get max ratio:
			Entry<Integer, Double> last = null;
			
			for (Entry<Integer, Double> value : list.entrySet())
			{
				last = value;
			}
			
			if (last != null && last.getKey() < getComboLevel())
			{
				return last.getValue();
			}
		}
		
		return 1.0;
	}
	
	public List<Integer> getVictims()
	{
		return _victims;
	}
	
	public void setVictims(List<Integer> victims)
	{
		_victims = victims;
	}
	
	public int getComboLevel()
	{
		return _comboLevel;
	}
	
	public void setComboLevel(int comboLevel)
	{
		_comboLevel = comboLevel;
	}
	
	public long getLastKillTime()
	{
		return _lastKillTime;
	}
	
	public void setLastKillTime(long lastKillTime)
	{
		_lastKillTime = lastKillTime;
	}
}
