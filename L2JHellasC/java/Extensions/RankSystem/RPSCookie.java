package Extensions.RankSystem;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class RPSCookie
{
	private RPSHtmlDeathStatus _deathStatus = null;
	private RPSHtmlComboKill _comboKill = null;
	
	private L2PcInstance _target = null;
	
	public void runPvpTask(L2PcInstance player, L2Character target)
	{
		if (Config.RANK_PVP_SYSTEM_ENABLED)
		{
			if (player != null && target != null && target.isPlayer())
			{
				((L2PcInstance) target).getRPSCookie().setTarget(player);			
				ThreadPoolManager.getInstance().executeTask(new RankPvpSystemPvpTask(player, (L2PcInstance) target));
			}
		}
	}
	
	public class RankPvpSystemPvpTask implements Runnable
	{
		private L2PcInstance _killer = null;
		private L2PcInstance _victim = null;
		
		public RankPvpSystemPvpTask(L2PcInstance killer, L2PcInstance victim)
		{
			_killer = killer;
			_victim = victim;
		}
		
		@Override
		public void run()
		{
			RankPvpSystem rps = new RankPvpSystem(_killer, _victim);
			
			rps.doPvp();
		}
	}
	
	public RPSHtmlDeathStatus getDeathStatus()
	{
		return _deathStatus;
	}
	
	public boolean isDeathStatusActive()
	{
		if (_deathStatus == null)
			return false;
		
		return true;
	}
	
	public void setDeathStatus(RPSHtmlDeathStatus deathStatus)
	{
		_deathStatus = deathStatus;
	}
	
	public RPSHtmlComboKill getComboKill()
	{
		return _comboKill;
	}
	
	public boolean isComboKillActive()
	{
		if (_comboKill == null)
			return false;
		
		return true;
	}
	
	public void setComboKill(RPSHtmlComboKill comboKill)
	{
		_comboKill = comboKill;
	}
	
	public L2PcInstance getTarget()
	{
		return _target;
	}
	
	public void setTarget(L2PcInstance target)
	{
		_target = target;
	}
}
