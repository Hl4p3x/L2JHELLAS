package com.l2jhellas.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2ProtectorInstance extends L2NpcInstance
{
	private ScheduledFuture<?> _aiTask;
	
	public L2ProtectorInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
		}
		
		_aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new ProtectorAI(this), 3000, 3000);
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
		}
		else if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
		{
			player.setLastFolkNPC(this);
			showHtmlWindow(player);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	@Override
	public void deleteMe()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
			_aiTask = null;
		}
		
		super.deleteMe();
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
	
	private static void showHtmlWindow(L2PcInstance activeChar)
	{
		StringBuilder tb = new StringBuilder();
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		
		tb.append("<html><head><title>Protector</title></head><body>");
		tb.append("<center><font color=\"a1df64\">L2jHellas Protector</font></center></body></html>");
		
		html.setHtml(tb.toString());
		activeChar.sendPacket(html);
	}
	
	private class ProtectorAI implements Runnable
	{
		private final L2ProtectorInstance _caster;
		
		protected ProtectorAI(L2ProtectorInstance caster)
		{
			_caster = caster;
		}
		
		@Override
		public void run()
		{
			
			for (L2PcInstance player : L2World.getInstance().getVisibleObjects(_caster, L2PcInstance.class, 2000))
			
			{
				if (player.getKarma() > 0 && Config.PROTECTOR_PLAYER_PK || player.getPvpFlag() != 0 && Config.PROTECTOR_PLAYER_PVP)
				{
					handleCast(player, Config.PROTECTOR_SKILLID, Config.PROTECTOR_SKILLLEVEL);
				}
			}
		}
		
		private boolean handleCast(L2PcInstance player, int skillId, int skillLevel)
		{
			if (player.isGM() || player.isDead() || !player.isVisible() || !isInsideRadius(player, Config.PROTECTOR_RADIUS_ACTION, false, false))
				return false;
			
			L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
			
			if (player.getFirstEffect(skill) == null)
			{
				int objId = _caster.getObjectId();
				skill.getEffects(_caster, player);
				broadcastPacket(new MagicSkillUse(_caster, player, skillId, skillLevel, Config.PROTECTOR_SKILLTIME, 0));
				if (Config.SEND_MESSAGE)
					broadcastPacket(new CreatureSay(objId, 0, String.valueOf(getName()), Config.PROTECTOR_MESSAGE));
				skill = null;
				return true;
			}
			
			return false;
		}
	}
}