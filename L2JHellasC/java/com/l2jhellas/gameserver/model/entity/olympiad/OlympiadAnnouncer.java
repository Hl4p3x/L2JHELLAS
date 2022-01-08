package com.l2jhellas.gameserver.model.entity.olympiad;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.spawn.L2Spawn;
import com.l2jhellas.gameserver.model.spawn.SpawnData;
import com.l2jhellas.gameserver.network.serverpackets.NpcSay;

public final class OlympiadAnnouncer implements Runnable
{
	private static final int OLY_MANAGER = 31688;
	
	private final List<L2Spawn> _managers = new ArrayList<>();
	private int _currentStadium = 0;
	
	public OlympiadAnnouncer()
	{	
		SpawnData.getInstance().forEachSpawn(sp ->
		{
			if (sp != null && sp.getId() == OLY_MANAGER)
				_managers.add(sp);
			return true;
		});
	}
	
	@Override
	public void run()
	{
		OlympiadGameTask task;
		for (int i = OlympiadGameManager.getInstance().getNumberOfStadiums(); --i >= 0; _currentStadium++)
		{
			if (_currentStadium >= OlympiadGameManager.getInstance().getNumberOfStadiums())
				_currentStadium = 0;
			
			task = OlympiadGameManager.getInstance().getOlympiadTask(_currentStadium);
			if (task != null && task.getGame() != null && task.needAnnounce())
			{
				String npcString;
				final String arenaId = String.valueOf(task.getGame().getStadiumId() + 1);
				switch (task.getGame().getType())
				{
					case NON_CLASSED:
						npcString = "Olympiad class-free individual match is going to begin in Arena " + arenaId + " in a moment.";
						break;
					
					case CLASSED:
						npcString = "Olympiad class individual match is going to begin in Arena " + arenaId + " in a moment.";
						break;
					
					default:
						continue;
				}
				
				L2Npc manager;
				for (L2Spawn spawn : _managers)
				{
					manager = spawn.getLastSpawn();
					if (manager != null)
						manager.broadcastPacket(new NpcSay(manager.getObjectId(), ChatType.SHOUT.getClientId(), manager.getNpcId(), npcString));
				}
				break;
			}
		}
	}
}