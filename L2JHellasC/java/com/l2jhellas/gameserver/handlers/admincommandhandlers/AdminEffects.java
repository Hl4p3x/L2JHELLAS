package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.Team;
import com.l2jhellas.gameserver.enums.skills.AbnormalEffect;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2ChestInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.Earthquake;
import com.l2jhellas.gameserver.network.serverpackets.ExRedSky;
import com.l2jhellas.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.PlaySound;
import com.l2jhellas.gameserver.network.serverpackets.SignsSky;
import com.l2jhellas.gameserver.network.serverpackets.SocialAction;
import com.l2jhellas.gameserver.network.serverpackets.SunRise;
import com.l2jhellas.gameserver.network.serverpackets.SunSet;
import com.l2jhellas.gameserver.skills.SkillTable;

public class AdminEffects implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_earthquake",
		"admin_earthquake_menu",
		"admin_bighead",
		"admin_shrinkhead",
		"admin_gmspeed",
		"admin_gmspeed_menu",
		"admin_unpara_all",
		"admin_para_all",
		"admin_unpara",
		"admin_para",
		"admin_unpara_all_menu",
		"admin_para_all_menu",
		"admin_unpara_menu",
		"admin_para_menu",
		"admin_polyself_menu",
		"admin_unpolyself_menu",
		"admin_clearteams",
		"admin_setteam_close",
		"admin_setteam",
		"admin_social",
		"admin_effect",
		"admin_social_menu",
		"admin_effect_menu",
		"admin_abnormal",
		"admin_abnormal_menu",
		"admin_play_sounds",
		"admin_play_sound",
		"admin_atmosphere",
		"admin_atmosphere_menu"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		if (command.startsWith("admin_earthquake"))
		{
			try
			{
				String val1 = st.nextToken();
				int intensity = Integer.parseInt(val1);
				String val2 = st.nextToken();
				int duration = Integer.parseInt(val2);
				Earthquake eq = new Earthquake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), intensity, duration);
				activeChar.broadcastPacket(eq);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Use: //earthquake <intensity> <duration>");
			}
		}
		else if (command.startsWith("admin_atmosphere"))
		{
			try
			{
				String type = st.nextToken();
				String state = st.nextToken();
				adminAtmosphere(type, state, activeChar);
			}
			catch (Exception ex)
			{
			}
		}
		else if (command.equals("admin_play_sounds"))
			AdminHelpPage.showHelpPage(activeChar, "songs/songs.htm");
		else if (command.startsWith("admin_play_sounds"))
		{
			try
			{
				AdminHelpPage.showHelpPage(activeChar, "songs/songs" + command.substring(17) + ".htm");
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_play_sound"))
		{
			try
			{
				playAdminSound(activeChar, command.substring(17));
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_para_all"))
		{
			try
			{
				L2World.getInstance().forEachVisibleObject(activeChar, L2PcInstance.class, player ->
				{
					player.startAbnormalEffect(AbnormalEffect.HOLD_2);
					player.setIsParalyzed(true);
					player.abortAllAttacks();
					player.stopMove(null);
					player.sendMessage("You are Paralyzed by " + activeChar.getName());
				});
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_unpara_all"))
		{
			try
			{
				L2World.getInstance().forEachVisibleObject(activeChar, L2PcInstance.class, player ->
				{
					player.stopAbnormalEffect(AbnormalEffect.HOLD_2);
					player.setIsParalyzed(false);
				});
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_para"))
		{
			final L2Object target = activeChar.getTarget();
			
			if (!(target instanceof L2Character))
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			final L2Character player = (L2Character) target;
			
			player.startAbnormalEffect(AbnormalEffect.HOLD_2);
			player.setIsParalyzed(true);
			player.abortAllAttacks();
			player.stopMove(null);
		}
		else if (command.startsWith("admin_unpara"))
		{
			final L2Object target = activeChar.getTarget();
			
			if (!(target instanceof L2Character))
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			final L2Character player = (L2Character) target;
			
			player.stopAbnormalEffect(AbnormalEffect.HOLD_2);
			player.setIsParalyzed(false);
		}
		else if (command.startsWith("admin_bighead"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				if (target instanceof L2Character)
				{
					player = (L2Character) target;
					player.startAbnormalEffect(0x2000);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_shrinkhead"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				if (target instanceof L2Character)
				{
					player = (L2Character) target;
					player.stopAbnormalEffect((short) 0x2000);
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_gmspeed"))
		{
			try
			{
				int val = Integer.parseInt(st.nextToken());
				boolean sendMessage = activeChar.getFirstEffect(7029) != null;
				activeChar.stopSkillEffects(7029);
				if (val == 0 && sendMessage)
				{
					activeChar.doCast(SkillTable.getInstance().getInfo(7029, val));
				}
				else if ((val >= 1) && (val <= 4))
				{
					L2Skill gmSpeedSkill = SkillTable.getInstance().getInfo(7029, val);
					activeChar.doCast(gmSpeedSkill);
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Use //gmspeed value (0=off...4=max).");
			}
			finally
			{
				activeChar.updateEffectIcons();
			}
		}
		else if (command.equals("admin_clear_teams"))
		{
			try
			{
				L2World.getInstance().forEachVisibleObject(activeChar, L2PcInstance.class, player ->
				{
					if (player != null && !player.isInFunEvent())
					{
						player.setTeam(Team.NONE);
						player.broadcastUserInfo();
					}
				});
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_setteam_close"))
		{
			try
			{
				String val = st.nextToken();
				
				int radius = 400;
				if (st.hasMoreTokens())
				{
					radius = Integer.parseInt(st.nextToken());
				}
				
				L2World.getInstance().forEachVisibleObjectInRange(activeChar, L2PcInstance.class, radius, player ->
				{
					if (player != null)
					{
						Team team = Team.valueOf(val.toUpperCase());
						player.setTeam(team);
						
						if (team.getId() > 0)
							player.sendMessage("You have joined " + team + " team");
						else
							player.sendMessage("you have left from the team");
						
						player.broadcastUserInfo();
					}
				});
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //setteam_close <none|blue|red> [radius]");
			}
		}
		else if (command.startsWith("admin_setteam"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				
				if (target instanceof L2PcInstance)
					player = (L2PcInstance) target;
				else
					return false;
				
				Team team = Team.valueOf(st.nextToken().toUpperCase());
				player.setTeam(team);
				
				if (team.getId() > 0)
					player.sendMessage("You have joined " + team + " team");
				else
					player.sendMessage("you have left from the team");
				
				player.broadcastUserInfo();
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //setteam <none|blue|red>");
			}
		}
		else if (command.startsWith("admin_social"))
		{
			try
			{
				String target = null;
				L2Object obj = activeChar.getTarget();
				if (st.countTokens() == 2)
				{
					int social = Integer.parseInt(st.nextToken());
					target = st.nextToken();
					if (target != null)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(target);
						if (player != null)
						{
							if (performSocial(social, player, activeChar))
								activeChar.sendMessage(player.getName() + " was affected by your request.");
						}
						else
						{
							try
							{
								int radius = Integer.parseInt(target);
								
								L2World.getInstance().forEachVisibleObject(activeChar, L2PcInstance.class, object ->
								{
									if (activeChar.isInsideRadius(player, 400, false, true))
									{
										performSocial(social, object, activeChar);
									}
								});
								
								activeChar.sendMessage(radius + " units radius affected by your request.");
							}
							catch (NumberFormatException nbe)
							{
								activeChar.sendMessage("Incorrect parameter");
							}
						}
					}
				}
				else if (st.countTokens() == 1)
				{
					int social = Integer.parseInt(st.nextToken());
					if (obj == null)
						obj = activeChar;
					
					if (performSocial(social, obj, activeChar))
						activeChar.sendMessage(obj.getName() + " was affected by your request.");
					else
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				}
				else if (!command.contains("menu"))
					activeChar.sendMessage("Usage: //social <social_id> [player_name|radius]");
			}
			catch (Exception e)
			{
				if (Config.DEVELOPER)
				{
					e.printStackTrace();
				}
			}
		}
		else if (command.startsWith("admin_abnormal"))
		{
			try
			{
				String target = null;
				L2Object obj = activeChar.getTarget();
				if (st.countTokens() == 2)
				{
					String parm = st.nextToken();
					int abnormal = Integer.decode("0x" + parm);
					target = st.nextToken();
					if (target != null)
					{
						L2PcInstance player = L2World.getInstance().getPlayer(target);
						if (player != null)
						{
							if (performAbnormal(abnormal, player))
								activeChar.sendMessage(player.getName() + "'s abnormal status was affected by your request.");
							else
								activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
						}
						else
						{
							try
							{
								int radius = Integer.parseInt(target);
								
								L2World.getInstance().forEachVisibleObject(activeChar, L2PcInstance.class, object ->
								{
									if (activeChar.isInsideRadius(player, 400, false, true))
									{
										performAbnormal(abnormal, object);
									}
								});
								
								activeChar.sendMessage(radius + " units radius affected by your request.");
							}
							catch (NumberFormatException nbe)
							{
								activeChar.sendMessage("Usage: //abnormal <hex_abnormal_mask> [player|radius]");
							}
						}
					}
				}
				else if (st.countTokens() == 1)
				{
					int abnormal = Integer.decode("0x" + st.nextToken());
					if (obj == null)
						obj = activeChar;

					if (performAbnormal(abnormal, obj))
						activeChar.sendMessage(obj.getName() + "'s abnormal status was affected by your request.");
					else
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				}
				else if (!command.contains("menu"))
					activeChar.sendMessage("Usage: //abnormal <abnormal_mask> [player_name|radius]");
			}
			catch (Exception e)
			{
				if (Config.DEVELOPER)
				{
					e.printStackTrace();
				}
			}
		}
		else if (command.startsWith("admin_effect"))
		{
			try
			{
				L2Object obj = activeChar.getTarget();
				int level = 1, hittime = 1;
				int skill = Integer.parseInt(st.nextToken());
				if (st.hasMoreTokens())
					level = Integer.parseInt(st.nextToken());
				if (st.hasMoreTokens())
					hittime = Integer.parseInt(st.nextToken());
				if (obj == null)
					obj = activeChar;
				
				if (!(obj instanceof L2Character))
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				else
				{
					L2Character target = (L2Character) obj;
					target.broadcastPacket(new MagicSkillUse(target, activeChar, skill, level, hittime, 0));
					activeChar.sendMessage(obj.getName() + " performs MSU " + skill + "/" + level + " by your request.");
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //effect skill [level | level hittime]");
			}
		}
		if (command.contains("menu"))
			showMainPage(activeChar, command);
		return true;
	}
	
	private static boolean performAbnormal(int action, L2Object target)
	{
		if (target instanceof L2Character)
		{
			L2Character character = (L2Character) target;
			if ((character.getAbnormalEffect() & action) == action)
				character.stopAbnormalEffect(action);
			else
				character.startAbnormalEffect(action);
			return true;
		}
		return false;
	}
	
	private static boolean performSocial(int action, L2Object target, L2PcInstance activeChar)
	{
		try
		{
			if (target instanceof L2Character)
			{
				if ((target instanceof L2Summon) || (target instanceof L2ChestInstance))
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return false;
				}
				if ((target instanceof L2Npc) && (action < 1 || action > 3))
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return false;
				}
				if ((target instanceof L2PcInstance) && (action < 2 || action > 16))
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return false;
				}
				L2Character character = (L2Character) target;
				character.broadcastPacket(new SocialAction(target.getObjectId(), action));
			}
			else
				return false;
		}
		catch (Exception e)
		{
		}
		return true;
	}
	
	private static void adminAtmosphere(String type, String state, L2PcInstance activeChar)
	{
		L2GameServerPacket packet = null;
		
		if (type.equals("signsky"))
		{
			if (state.equals("dawn"))
				packet = new SignsSky(2);
			else if (state.equals("dusk"))
				packet = new SignsSky(1);
		}
		else if (type.equals("sky"))
		{
			if (state.equals("night"))
				packet = new SunSet();
			else if (state.equals("day"))
				packet = new SunRise();
			else if (state.equals("red"))
				packet = new ExRedSky(10);
		}
		else
			activeChar.sendMessage("Usage: //atmosphere <signsky dawn|dusk>|<sky day|night|red>");
		if (packet != null)
			for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
				player.sendPacket(packet);
	}
	
	private static void playAdminSound(L2PcInstance activeChar, String sound)
	{
		final PlaySound _snd = (sound.contains(".")) ? PlaySound.createSound(sound) : new PlaySound(1, sound, 0);
		activeChar.broadcastPacket(_snd);
		activeChar.sendMessage("Playing " + sound + ".");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void showMainPage(L2PcInstance activeChar, String command)
	{
		String filename = "effects_menu";
		if (command.contains("abnormal"))
			filename = "abnormal";
		else if (command.contains("social"))
			filename = "social";
		AdminHelpPage.showHelpPage(activeChar, filename + ".htm");
	}
}