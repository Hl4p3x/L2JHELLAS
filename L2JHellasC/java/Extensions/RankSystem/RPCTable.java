package Extensions.RankSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jhellas.util.database.L2DatabaseFactory;

public class RPCTable
{
	public static final Logger log = Logger.getLogger(RPCTable.class.getSimpleName());
	
	private static RPCTable _instance = null;
	
	private Map<Integer, RPC> _rpcList = new HashMap<>();
	
	private RPCTable()
	{
		long startTime = Calendar.getInstance().getTimeInMillis();
		
		load();
		
		long endTime = Calendar.getInstance().getTimeInMillis();
		
		log.log(Level.INFO, " - RPCTable: Data loaded. " + (_rpcList.size()) + " objects in " + (endTime - startTime) + " ms.");
	}
	
	public static RPCTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new RPCTable();
		}
		
		return _instance;
	}
	
	public Map<Integer, RPC> getRpcList()
	{
		return _rpcList;
	}
	
	public void setRpcList(Map<Integer, RPC> rpcList)
	{
		_rpcList = rpcList;
	}
	
	public void addRpcForPlayer(int playerId, long addRpc)
	{
		RPC rpc = _rpcList.get(playerId);
		
		if (rpc != null)
		{
			rpc.increaseRpcBy(addRpc);
			return;
		}
		
		// else create new RPC for this player:
		
		rpc = new RPC(playerId);
		
		rpc.increaseRpcBy(addRpc);
		rpc.setDbStatus(DBStatus.INSERTED); // as inserted
		
		_rpcList.put(rpc.getPlayerId(), rpc);
	}
	
	public RPC getRpcByPlayerId(int playerId)
	{
		for (Map.Entry<Integer, RPC> e : _rpcList.entrySet())
		{
			if (e != null && e.getKey() == playerId)
			{
				return e.getValue();
			}
		}
		
		return null;
	}
	
	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM rank_pvp_system_rpc ORDER BY rpc_total"); // ordered for faster search.
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				RPC rpc = new RPC();
				
				rpc.setPlayerId(rset.getInt("player_id"));
				rpc.setRpcTotal(rset.getLong("rpc_total"));
				rpc.setRpcCurrent(rset.getLong("rpc_current"));
				
				_rpcList.put(rpc.getPlayerId(), rpc);
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			log.log(Level.WARNING, e.getMessage());
		}
	}
	
	public void updateDB()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			Statement statement = con.createStatement();
			// search new or updated fields in RPCTable:
			for (Map.Entry<Integer, RPC> e : _rpcList.entrySet())
			{
				RPC rpc = e.getValue();
				
				if (rpc == null)
					continue;
				
				if (rpc.getDbStatus() == DBStatus.UPDATED)
				{
					rpc.setDbStatus(DBStatus.NONE);
					statement.addBatch("UPDATE rank_pvp_system_rpc SET player_id=" + rpc.getPlayerId() + ", rpc_total=" + rpc.getRpcTotal() + ", rpc_current=" + rpc.getRpcCurrent() + " WHERE player_id=" + rpc.getPlayerId());
				}
				else if (rpc.getDbStatus() == DBStatus.INSERTED)
				{
					rpc.setDbStatus(DBStatus.NONE);
					statement.addBatch("INSERT INTO rank_pvp_system_rpc (player_id, rpc_total, rpc_current) VALUES (" + rpc.getPlayerId() + ", " + rpc.getRpcTotal() + ", " + rpc.getRpcCurrent() + ")");
				}
			}
			statement.executeBatch();
			statement.close();
		}
		catch (SQLException e)
		{
			log.log(Level.WARNING, e.getMessage());
		}
	}
}