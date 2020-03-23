package Extensions.RankSystem;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class RPSBypass
{
	public static final Logger log = Logger.getLogger(RPSBypass.class.getSimpleName());
	
	public static void executeCommand(L2PcInstance activeChar, String command)
	{
		
		if (!Config.RANK_PVP_SYSTEM_ENABLED)
			return;
		
		String param = command.split("\\.", 2)[1];
		
		RPSCookie pc = activeChar.getRPSCookie();
		
		// reset death status:
		if (!activeChar.isDead())
			pc.setDeathStatus(null);
		
		if (param.equals("PS")) // PvP Status
		{
			RPSHtmlPvpStatus.sendPage(activeChar, pc.getTarget());
		}
		else if (param.equals("DS")) // Death Status
		{
			if (pc.isDeathStatusActive() && pc.getDeathStatus().isValid())
			{
				pc.getDeathStatus().sendPage(activeChar);
			}
			else if (pc.getTarget() != null)
			{
				RPSHtmlPvpStatus.sendPage(activeChar, pc.getTarget());
				activeChar.sendMessage("You can see Death Status only when you are dead!");
			}
			else
			// if !pc.isDeathStatusActive()
			{
				activeChar.sendMessage("You can see Death Status only when you are dead!");
			}
		}
		else if (param.startsWith("RPC:"))
		{
			String param2 = command.split(":", 2)[1].trim();
			int pageNo = 1;
			try
			{
				pageNo = Integer.valueOf(param2);
			}
			catch (Exception e)
			{
				log.log(Level.WARNING, e.getMessage());
			}
			
			if (pageNo < 1)
			{
				pageNo = 1;
			}
			
			RPSHtmlRPCRewardList.sendPage(activeChar, pageNo);
		}
		else if (param.startsWith("RPCReward:"))
		{
			try
			{
				int rpcRewardId = Integer.valueOf(command.split(":", 2)[1].trim().split(",", 2)[0].trim());
				int pageNo = Integer.valueOf(command.split(":", 2)[1].trim().split(",", 2)[1].trim());
				
				RPCReward rpcReward = RPCRewardTable.getInstance().getRpcRewardList().get(rpcRewardId);
				
				RPCRewardTable.getInstance().giveReward(activeChar, rpcReward);
				
				RPSHtmlRPCRewardList.sendPage(activeChar, pageNo);
			}
			catch (Exception e)
			{
				log.log(Level.WARNING, e.getMessage());
			}
		}
		else if (param.startsWith("RPCRewardConfirm:"))
		{
			try
			{
				int rpcRewardId = Integer.valueOf(command.split(":", 2)[1].trim().split(",", 2)[0].trim());
				int pageNo = Integer.valueOf(command.split(":", 2)[1].trim().split(",", 2)[1].trim());
				
				RPSHtmlRPCRewardList.getConfirmPage(activeChar, pageNo, rpcRewardId);
			}
			catch (Exception e)
			{
				log.log(Level.WARNING, e.getMessage());
			}
		}
		else if (param.startsWith("RewardList:"))
		{
			try
			{
				int rankId = Integer.valueOf(command.split(":", 2)[1].trim().split(",", 2)[0].trim());
				int pageNo = Integer.valueOf(command.split(":", 2)[1].trim().split(",", 2)[1].trim());
				
				RPCHtmlRewardList.sendPage(activeChar, pageNo, rankId);
			}
			catch (Exception e)
			{
				log.log(Level.WARNING, e.getMessage());
			}
		}
	}
}