package com.l2jhellas.gameserver.model.actor.instance;

import java.util.logging.Logger;

import com.l2jhellas.gameserver.datatables.sql.NpcBufferSkillIdsTable;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.NpcBufferSkills.NpcBufferData;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2NpcBufferInstance extends L2Npc
{
	final Logger _log = Logger.getLogger(L2NpcBufferInstance.class.getName());
	
	public L2NpcBufferInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		
		return "data/html/mods/buffer/" + pom + ".htm";
	}
	
	@Override
	public void deleteMe()
	{
		super.deleteMe();
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance playerInstance, String command)
	{
		if (playerInstance == null)
			return;
		int npcId = getNpcId();
		int val = 0;
		if (command.startsWith("Chat"))
		{
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}
			catch (NumberFormatException nfe)
			{
			}
		}
		if (command.startsWith("npc_buffer_heal"))
		{
			if (playerInstance.getCurrentHp() == 0 || playerInstance.getPvpFlag() > 0)
				playerInstance.sendMessage("You can't do that in combat!!!");
			else
			{
				playerInstance.setCurrentCp(playerInstance.getMaxCp());
				playerInstance.setCurrentHp(playerInstance.getMaxHp());
				playerInstance.setCurrentMp(playerInstance.getMaxMp());
			}
		}
		if (command.startsWith("npc_buffer_cancel"))
		{
			if (playerInstance.getCurrentHp() == 0 || playerInstance.getPvpFlag() > 0)
				playerInstance.sendMessage("You can't do that!!!");
			else
				removeAllBuffs(playerInstance);
		}
		if (command.startsWith("npc_buffer_buff"))
		{
			String[] params = command.split(" ");
			int skillId = Integer.parseInt(params[1]);
			val = Integer.parseInt(params[2]);
			NpcBufferData skillInfos = NpcBufferSkillIdsTable.getInstance().getSkillInfo(npcId,skillId);

			if (skillInfos == null)
			{
				_log.warning(L2NpcBufferInstance.class.getName() + ": NpcBuffer warning(" + npcId + " at " + getX() + ", " + getY() + ", " + getZ() + "): Player " + playerInstance.getName() + " tried to use skill(" + skillId + ") not assigned to npc buffer!");
				return;
			}

			if (skillInfos.getFee().getId() != 0)
			{
				L2ItemInstance itemInstance = playerInstance.getInventory().getItemByItemId(skillInfos.getFee().getId());
				if ((itemInstance == null) || (!itemInstance.isStackable() && (playerInstance.getInventory().getInventoryItemCount(skillInfos.getFee().getId(), -1) < skillInfos.getFee().getCount())))
				{
					playerInstance.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
				
				if (itemInstance.isStackable()) 
				{
					if (!playerInstance.destroyItemByItemId("Npc Buffer", skillInfos.getFee().getId(), skillInfos.getFee().getCount(), playerInstance.getTarget(), true)) 
					{
						playerInstance.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
						return;
					}
				} 
				else
				{
					for (int i = 0; i < skillInfos.getFee().getCount(); ++i)
						playerInstance.destroyItemByItemId("Npc Buffer", skillInfos.getFee().getId(), 1, playerInstance.getTarget(), true);
				}
			}
			
			final L2Skill skill = SkillTable.getInstance().getInfo(skillInfos.getSkill().getSkillId(), skillInfos.getSkill().getSkillLvl());
			
			if (skill != null)			
			    skill.getEffects(playerInstance, playerInstance);
		}
		showChatWindow(playerInstance, val);
	}
	
	protected void removeAllBuffs(L2PcInstance player)
	{
		if (player != null)
		{
			player.stopAllEffects();
			player.sendMessage("Your buffs has been removed.");
		}
	}
}