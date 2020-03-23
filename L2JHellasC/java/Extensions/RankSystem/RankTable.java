package Extensions.RankSystem;

import java.util.LinkedHashMap;
import java.util.Map;

public class RankTable
{
	private static RankTable _instance = null;
	
	// [rankId, Rank] - store all Ranks as Rank objects by rank id.
	private static Map<Integer, Rank> _rankList = new LinkedHashMap<>();
	
	public static RankTable getInstance()
	{
		if (_instance == null)
			_instance = new RankTable();
		
		return _instance;
	}
	
	public Map<Integer, Rank> getRankList()
	{
		return _rankList;
	}
	
	public void setRankList(Map<Integer, Rank> rankList)
	{
		_rankList = rankList;
	}
	
	public Rank getRankById(int id)
	{
		return _rankList.get(id);
	}
}