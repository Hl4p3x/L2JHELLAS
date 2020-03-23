package com.l2jhellas.gameserver.model.actor.instance;

import java.util.List;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.MoveToPawn;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Rnd;

public final class L2GuardInstance extends L2Attackable
{	
	public L2GuardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);	
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return attacker instanceof L2MonsterInstance ? true : attacker.isPlayer() ? attacker.getActingPlayer().getKarma() > 0 : false;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}
	
	@Override
	public boolean isGuard()
	{
		return true;
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = val == 0 ? "" + npcId : npcId + "-" + val;	
		return "data/html/guard/" + pom + ".htm";
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;

		if (getObjectId() != player.getTargetId())
		{
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
		}
		else
		{
			if (containsTarget(player))
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			else
			{
				if (!canInteract(player))
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				else
				{
					player.sendPacket(new MoveToPawn(player, this, L2Npc.INTERACTION_DISTANCE));
					
					switch (getNpcId())
					{
						case 30733: 
						case 31032:
						case 31033:
						case 31034:
						case 31035:
						case 31036:
						case 31671: 
						case 31672:
						case 31673:
						case 31674:
							return;
					}
					
					if (hasRandomAnimation())
						onRandomAnimation(Rnd.get(8));
					
					List<Quest> scripts = getTemplate().getEventQuests(QuestEventType.QUEST_START);
					if (scripts != null && !scripts.isEmpty())
						player.setLastQuestNpcObject(getObjectId());
					
					scripts = getTemplate().getEventQuests(QuestEventType.ON_FIRST_TALK);
					if (scripts != null && scripts.size() == 1)
						scripts.get(0).notifyFirstTalk(this, player);
					else
						showChatWindow(player);
				}
			}
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}