package Extensions.RankSystem;

import java.util.logging.Level;
import java.util.logging.Logger;

import Extensions.RankSystem.Util.ServerSideImage;

import com.l2jhellas.Config;

public final class RankLoader
{
	private static final Logger log = Logger.getLogger(RankLoader.class.getSimpleName());
	public static final String RANK_PVP_SYSTEM_VERSION = "3.8.9";
	public static final String CHAR_ID_COLUMN_NAME = "obj_Id";
	
	public static void load()
	{
		log.log(Level.INFO, "> Initializing Rank PvP System (" + RANK_PVP_SYSTEM_VERSION + "):");
		
		// initializing system
		PvpTable.getInstance();
		
		if (Config.RPC_REWARD_ENABLED || Config.RANK_RPC_ENABLED || Config.RPC_TABLE_FORCE_UPDATE_ENABLED)
			RPCTable.getInstance();
		else
			log.log(Level.INFO, " - RPCTable: Disabled, players RPC will be not updated!");
		
		if (Config.RPC_REWARD_ENABLED || Config.RPC_EXCHANGE_ENABLED)
			RPCRewardTable.getInstance();
		else
			log.log(Level.INFO, " - RPCRewardTable: Disabled.");
		
		if (Config.PVP_REWARD_ENABLED || Config.RANK_PVP_REWARD_ENABLED)
			RewardTable.getInstance();
		else
			log.log(Level.INFO, " - RewardTable: Disabled.");
		
		if (Config.TOP_LIST_ENABLED)
			TopTable.getInstance();
		else
			log.log(Level.INFO, " - TopTable: Disabled.");
		
		ServerSideImage.getInstance();
		log.log(Level.INFO, " - Rank Pvp System initialization complete.");
	}
}
