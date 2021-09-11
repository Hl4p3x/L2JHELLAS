package com.l2jhellas.gameserver.enums.player;

import com.l2jhellas.Config;

public enum VoteSite
{
	TOPZONE("L2Topzone", "https://api.l2topzone.com/v1/vote?token=" + Config.VOTE_TOPZONE_API + "&ip=%s", "\"isVoted\":true"),
	HOPZONE("L2Hopzone", "https://api.hopzone.net/lineage2/vote?token=" + Config.VOTE_HOPZONE_API + "&ip_address=%s", "\"voted\":true"),		
	NETWORK("L2Network", "https://l2network.eu/index.php?a=in&u=" + Config.VOTE_NETWORK_API + "&ipc=%s", "1"),
	L2TOPCO("User-Agent", "https://l2top.co/reward/VoteCheck.php?id=" + Config.VOTE_L2TOPCO_API + "&ip=%s", "TRUE"),	
	L2J_TOP("User-Agent", "https://l2jtop.com/api/" + Config.VOTE_L2J_TOP_API + "/ip/%s/", "\"is_voted\":true"),	
	TOPGAME("User-Agent", "http://l2.topgameserver.net/lineage/VoteApi/API_KEY=" + Config.VOTE_TOPGAME_API + "/getData/%s", "\"already_voted\":true"),	
	L2VOTES("User-Agent", "https://l2votes.com/api.php?apiKey=" + Config.VOTE_L2VOTESSERV_API + "&ip=%s", "1"),		
	L2JBRAZIL("User-Agent", "https://top.l2jbrasil.com/votesystem/index.php?" + "username=" + Config.VOTE_L2JBRAZIL_USERNAME + "&ip=%s" + "&type=json" ,"\"status\":1");
				
	public static final VoteSite[] VALUES = values();
	
	private final String _userAgent;
	private final String _apiLink;
	private final String _result;
	
	private VoteSite(String userAgent, String apiLink, String result)
	{
		_userAgent = userAgent;
		_apiLink = apiLink;
		_result = result;
	}
	
	public String getUserAgent()
	{
		return _userAgent;
	}
	
	public String getApiLink()
	{
		return _apiLink;
	}
	
	public String getResult()
	{
		return _result;
	}
}