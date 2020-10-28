package com.l2jhellas.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.skills.NpcBufferSkills;
import com.l2jhellas.gameserver.skills.NpcBufferSkills.NpcBufferData;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class NpcBufferSkillIdsTable
{
	protected static final Logger _log = Logger.getLogger(NpcBufferSkillIdsTable.class.getName());

	private final Map<Integer, NpcBufferSkills> _buffers = new HashMap<>();

	protected NpcBufferSkillIdsTable()
	{
		NpcBufferSkills skills = null;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement("SELECT npc_id,skill_id,skill_level,skill_fee_id,skill_fee_amount FROM npc_buffer ORDER BY npc_id ASC"))
		{
			try(ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int npcId = rset.getInt("npc_id");
					int skillId = rset.getInt("skill_id");
					int skillLevel = rset.getInt("skill_level");
					int skillFeeId = rset.getInt("skill_fee_id");
					int skillFeeAmount = rset.getInt("skill_fee_amount");

					if(!_buffers.containsKey(npcId))
					{
						skills = new NpcBufferSkills(npcId);
						skills.addSkill(skillId, skillLevel, skillFeeId, skillFeeAmount);
						_buffers.put(npcId, skills);
					}
					else if(skills!=null)
						skills.addSkill(skillId, skillLevel, skillFeeId, skillFeeAmount);										
				}
			}
		}
		catch (Exception e)
		{
			_log.warning(NpcBufferSkillIdsTable.class.getName() + ": Error reading npc_buffer_skill_ids table: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}

		_log.info(NpcBufferSkillIdsTable.class.getSimpleName() + ": Loaded " + _buffers.size() + " buffers and "+ (skills !=null ? skills.getSkills().size() : 0) +" skills.");
	}
	
	public void reload()
	{
		getInstance();
	}

	public NpcBufferData getSkillInfo(int npcId, int skillId)
	{
		final NpcBufferSkills skills = _buffers.get(npcId);
		return skills.getSkillInfo(skillId);
	}
		
	public static NpcBufferSkillIdsTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final NpcBufferSkillIdsTable _instance = new NpcBufferSkillIdsTable();
	}
}