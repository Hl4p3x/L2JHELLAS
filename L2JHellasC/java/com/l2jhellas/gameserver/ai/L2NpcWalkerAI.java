package com.l2jhellas.gameserver.ai;

import java.util.List;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.datatables.xml.NpcWalkerRoutesData;
import com.l2jhellas.gameserver.model.L2NpcWalkerNode;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2NpcWalkerInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;

public class L2NpcWalkerAI extends L2CharacterAI implements Runnable
{
	// The route used by this NPC, consisting of multiple nodes
	private final List<L2NpcWalkerNode> _route;
	
	// Flag allowing NPC to go to next point or no (allow to delay).
	private boolean _walkingToNextPoint = false;
	private long _nextMoveTime;
	
	// The currents node and position where the NPC is situated.
	private L2NpcWalkerNode _currentNode;
	private int _currentPos;
	
	public L2NpcWalkerAI(L2Character accessor)
	{
		super(accessor);
		
		_route = NpcWalkerRoutesData.getInstance().getRouteForNpc(getActor().getNpcId());
		
		if (_route != null)
			ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 1000);
		else
			_log.warning(getClass().getSimpleName() + ": Missing route data for NpcID: " + _actor);
	}
	
	@Override
	public void run()
	{
		onEvtThink();
	}
	
	@Override
	protected void onEvtThink()
	{
		if (_walkingToNextPoint)
			return;
		
		if (_nextMoveTime < System.currentTimeMillis())
			walkToLocation();
	}
	
	@Override
	protected void onEvtArrived()
	{
		String chat = _currentNode.getChatText();
		
		if (chat != null && !chat.isEmpty())
			getActor().broadcastNpcSay(chat);
		
		_nextMoveTime = System.currentTimeMillis() + Math.max(0, _currentNode.getDelay() * 1000);
		_walkingToNextPoint = false;
	}
	
	@Override
	protected void onEvtArrivedBlocked(Location blocked_at_pos)
	{
		_log.warning("NpcWalker ID: " + getActor().getNpcId() + ": Blocked at coords: " + blocked_at_pos.toString() + ". Teleporting to next point.");
		
		getActor().teleToLocation(_currentNode.getMoveX(), _currentNode.getMoveY(), _currentNode.getMoveZ(), false);
		super.onEvtArrivedBlocked(blocked_at_pos);
	}
	
	private void walkToLocation()
	{
		if (_currentPos < (_route.size() - 1))
			_currentPos++;
		else
			_currentPos = 0;
		
		_currentNode = _route.get(_currentPos);
		
		getActor().setIsRunning(_currentNode.getRunning());
		
		_walkingToNextPoint = true;

		setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(_currentNode.getMoveX(), _currentNode.getMoveY(), _currentNode.getMoveZ(), 0));		
	}
	
	@Override
	public L2NpcWalkerInstance getActor()
	{
		return (L2NpcWalkerInstance) super.getActor();
	}
}