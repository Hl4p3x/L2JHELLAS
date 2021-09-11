package com.l2jhellas.gameserver.model.actor.instance;

import java.util.stream.Collectors;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.player.VoteSite;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2VoteManagerInstance extends L2Npc
{
	public L2VoteManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(getHtmlPath(getNpcId(), val));
		html.replace("%objectId%", getObjectId());
		html.replace("%topzone%", player.canVoteTo(VoteSite.TOPZONE) ? "<a action=\"bypass -h npc_" + getObjectId() + "_vote TOPZONE\">Vote for L2Topzone.com</a>" : "<font color=FF0000>" + player.nextVoteTime(VoteSite.TOPZONE) + "</font>");
		html.replace("%hopzone%", player.canVoteTo(VoteSite.HOPZONE) ? "<a action=\"bypass -h npc_" + getObjectId() + "_vote HOPZONE\">Vote for L2.Hopzone.net</a>" : "<font color=FF0000>" + player.nextVoteTime(VoteSite.HOPZONE) + "</font>");
		html.replace("%network%", player.canVoteTo(VoteSite.NETWORK) ? "<a action=\"bypass -h npc_" + getObjectId() + "_vote NETWORK\">Vote for L2Network.com</a>" : "<font color=FF0000>" + player.nextVoteTime(VoteSite.NETWORK) + "</font>");
		html.replace("%l2topco%", player.canVoteTo(VoteSite.L2TOPCO) ? "<a action=\"bypass -h npc_" + getObjectId() + "_vote L2TOPCO\">Vote for L2Top.co</a>" : "<font color=FF0000>" + player.nextVoteTime(VoteSite.L2TOPCO) + "</font>");
		html.replace("%mmo_top%", player.canVoteTo(VoteSite.L2J_TOP) ? "<a action=\"bypass -h npc_" + getObjectId() + "_vote L2J_TOP\">Vote for l2Jtop.com</a>" : "<font color=FF0000>" + player.nextVoteTime(VoteSite.L2J_TOP) + "</font>");
		html.replace("%topgame%", player.canVoteTo(VoteSite.TOPGAME) ? "<a action=\"bypass -h npc_" + getObjectId() + "_vote TOPGAME\">Vote for L2.TopGameServer.net</a>" : "<font color=FF0000>" + player.nextVoteTime(VoteSite.TOPGAME) + "</font>");
		html.replace("%topserv%", player.canVoteTo(VoteSite.L2VOTES) ? "<a action=\"bypass -h npc_" + getObjectId() + "_vote L2VOTES\">Vote for L2Votes.com</a>" : "<font color=FF0000>" + player.nextVoteTime(VoteSite.L2VOTES) + "</font>");
		html.replace("%topbraz%", player.canVoteTo(VoteSite.L2JBRAZIL) ? "<a action=\"bypass -h npc_" + getObjectId() + "_vote L2JBRAZIL\">Vote for top.l2jbrasil.com/</a>" : "<font color=FF0000>" + player.nextVoteTime(VoteSite.L2JBRAZIL) + "</font>");
		player.sendPacket(html);
	}
		
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("Chat"))
			showChatWindow(player, Integer.parseInt(command.substring(5)));
		else if (player.getLevel() < Config.LEVEL_RESTRICTED)
			player.sendMessage("Only players level " + Config.LEVEL_RESTRICTED + " and above can be rewarded.");
		else if (player.isVoting())
			player.sendMessage("You are already voting.");
		else if (L2World.getInstance().getAllPlayers().values().stream().filter(x -> x != null && x.getClient() != null && !x.getClient().isDetached() && x.isVoting()).count() >= 5)
			player.sendMessage("Many people are voting currently. Try again in a couple of seconds.");
		else if (L2World.getInstance().getAllPlayers().values().stream().filter(x -> x != null && x.getClient() != null && !x.getClient().isDetached() && x.isVoting() && x.getIP().equals(player.getIP())).collect(Collectors.toList()).size() > 0)
			player.sendMessage("Somebody else with same IP is voting right now.");
		else if (command.startsWith("vote"))
			new Thread(() -> player.requestVoteTo(VoteSite.valueOf(command.substring(5)))).start();
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		return "data/html/votemanager/" + npcId + (val == 0 ? "" : "-" + val) + ".htm";
	}
}