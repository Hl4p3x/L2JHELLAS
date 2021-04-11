package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.NpcSay;
import com.l2jhellas.util.MathUtil;
import com.l2jhellas.util.Rnd;

public final class RequestActionUse extends L2GameClientPacket
{
	private static final String _C__45_REQUESTACTIONUSE = "[C] 45 RequestActionUse";
	
	private static final int SIN_EATER_ID = 12564;
	
	private int _actionId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	@Override
	protected void readImpl()
	{
		_actionId = readD();
		_ctrlPressed = (readD() == 1);
		_shiftPressed = (readC() == 1);
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
			return;
		
		if ((player.isFakeDeath() && _actionId != 0) || player.isDead() || player.isOutOfControl())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2Summon summon = player.getPet();
		final L2Object target = player.getTarget();
		
		switch (_actionId)
		{
			case 0:
				if (player.getMountType() == 0)
				player.SitStand(target, player.isSitting());
				break;
			case 1:
				if (player.isMounted())
					return;
				
				if (player.isRunning())
					player.setWalking();
				else
					player.setRunning();
				break;		
			case 10: 
				player.openPrivateSellStore(false);
				break;			
			case 28: 
				player.openPrivateBuyStore();
				break;			
			case 15:
			case 21: // Change Movement Mode (pet follow/stop)
				if (summon == null)
					return;
				
				if (summon.getFollowStatus() && MathUtil.calculateDistance(player, summon, true) > 2000)
					return;
				
				if (summon.isOutOfControl())
				{
					player.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
					return;
				}				
				summon.setFollowStatus(!summon.getFollowStatus());
				break;
			
			case 16:
			case 22: // pet attack
				if (!(target instanceof L2Character) || summon == null || summon == target || player == target)
					return;
				
				if (checkforsummons(NOATTACKACTION_SUMMONS, summon.getNpcId()))
					return;
				
				if (summon.isOutOfControl())
				{
					player.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
					return;
				}
				
				if (summon.isAttackingDisabled())
				{
					if (summon.getAttackEndTime() <= System.currentTimeMillis())
						return;
					
					summon.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
				
				if (summon instanceof L2PetInstance && (summon.getLevel() - player.getLevel() > 20))
				{
					player.sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL);
					return;
				}
				
				if (player.isInOlympiadMode() && !player.isOlympiadStart())
					return;
				
				summon.setTarget(target);
				
				if (!target.isAutoAttackable(player) && !_ctrlPressed)
				{
					summon.setFollowStatus(false);
					summon.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
					player.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return;
				}
				
				if (target instanceof L2DoorInstance)
				{
					if (((L2DoorInstance) target).isAutoAttackable(player) && summon.getNpcId() != L2SiegeSummonInstance.SWOOP_CANNON_ID)
						summon.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
				else if (summon.getNpcId() != L2SiegeSummonInstance.SIEGE_GOLEM_ID)
				{
					if (summon.isInsidePeaceZone(summon, target))
					{
						summon.setFollowStatus(false);
						summon.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
					}
					else
						summon.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
				break;
			
			case 17:
			case 23: // Stop (pet - cancel action)
				if (summon == null)
					return;
				
				if (summon.isOutOfControl())
				{
					player.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
					return;
				}
				
				summon.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				break;
			
			case 19: // Returns pet to control item
				if (summon == null || !(summon instanceof L2PetInstance))
					return;
				
				if (summon.isDead())
					player.sendPacket(SystemMessageId.DEAD_PET_CANNOT_BE_RETURNED);
				if (summon.isOutOfControl())
					player.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
				if (summon.isAttackingNow() || summon.isInCombat())
					player.sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);				
				if ((((L2PetInstance) summon).getCurrentFed() > ((((L2PetInstance) summon).getMaxFed() * 0.40))))
					summon.unSummon(player);				
				else 
					player.sendPacket(SystemMessageId.YOU_CANNOT_RESTORE_HUNGRY_PETS);				
				break;
			
			case 38: // pet mount/dismount
				player.mountPlayer(summon);
				break;
			
			case 32: // Wild Hog Cannon - Mode Change
				// useSkill(4230);
				break;
			
			case 36: // Soulless - Toxic Smoke
				useSkill(4259, target);
				break;
			
			case 37: // Dwarven Manufacture
				player.openWorkshop(true);
				break;
			
			case 39: // Soulless - Parasite Burst
				useSkill(4138, target);
				break;
			
			case 41: // Wild Hog Cannon - Attack
				if (!(target instanceof L2DoorInstance))
				{
					player.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return;
				}
				
				useSkill(4230, target);
				break;
			
			case 42: // Kai the Cat - Self Damage Shield
				useSkill(4378, player);
				break;
			
			case 43: // Unicorn Merrow - Hydro Screw
				useSkill(4137, target);
				break;
			
			case 44: // Big Boom - Boom Attack
				useSkill(4139, target);
				break;
			
			case 45: // Unicorn Boxer - Master Recharge
				useSkill(4025, player);
				break;
			
			case 46: // Mew the Cat - Mega Storm Strike
				useSkill(4261, target);
				break;
			
			case 47: // Silhouette - Steal Blood
				useSkill(4260, target);
				break;
			
			case 48: // Mechanic Golem - Mech. Cannon
				useSkill(4068, target);
				break;
			
			case 51: // General Manufacture
				player.openWorkshop(false);
				break;
			
			case 52: // Unsummon a servitor
				if (summon == null || !(summon instanceof L2SummonInstance))
					return;
				
				if (summon.isDead())
					player.sendPacket(SystemMessageId.DEAD_PET_CANNOT_BE_RETURNED);
				else if (summon.isOutOfControl())
					player.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
				else if (summon.isAttackingNow() || summon.isInCombat())
					player.sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
				else
					summon.unSummon(player);
				break;
				
			// move to target
			case 53: 
			case 54:
				if (target == null || summon == null || summon == target)
					return;
				
				if (summon.isOutOfControl())
				{
					player.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
					return;
				}
				
				summon.setFollowStatus(false);
				summon.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(target.getX(), target.getY(), target.getZ(),0));
				break;
			
			case 61: // Private Store Package Sell
				player.openPrivateSellStore(true);
				break;
			
			case 96: // Quit Party Command Channel
				_log.info("98 Accessed");
				break;
			case 97: // Request Party Command Channel Info //TODO cleanup this
				// if (!PartyCommandManager.getInstance().isPlayerInChannel(activeChar))
				// return;
				_log.info("97 Accessed");
				// PartyCommandManager.getInstance().getActiveChannelInfo(activeChar);
				break;
			case 1000: // Siege Golem - Siege Hammer
				if (!(target instanceof L2DoorInstance))
				{
					player.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return;
				}
				
				useSkill(4079, target);
				break;
			
			case 1001: // Sin Eater - Ultimate Bombastic Buster
				if (useSkill(4139, summon) && summon.getNpcId() == SIN_EATER_ID && Rnd.get(100) < 10)
					summon.broadcastPacket(new NpcSay(summon.getObjectId(), ChatType.GENERAL.getClientId(), summon.getNpcId(), Rnd.get(SIN_EATER_ACTIONS_STRINGS)));
				break;
			
			case 1003: // Wind Hatchling/Strider - Wild Stun
				useSkill(4710, target);
				break;
			
			case 1004: // Wind Hatchling/Strider - Wild Defense
				useSkill(4711, player);
				break;
			
			case 1005: // Star Hatchling/Strider - Bright Burst
				useSkill(4712, target);
				break;
			
			case 1006: // Star Hatchling/Strider - Bright Heal
				useSkill(4713, player);
				break;
			
			case 1007: // Cat Queen - Blessing of Queen
				useSkill(4699, player);
				break;
			
			case 1008: // Cat Queen - Gift of Queen
				useSkill(4700, player);
				break;
			
			case 1009: // Cat Queen - Cure of Queen
				useSkill(4701, target);
				break;
			
			case 1010: // Unicorn Seraphim - Blessing of Seraphim
				useSkill(4702, player);
				break;
			
			case 1011: // Unicorn Seraphim - Gift of Seraphim
				useSkill(4703, player);
				break;
			
			case 1012: // Unicorn Seraphim - Cure of Seraphim
				useSkill(4704, target);
				break;
			
			case 1013: // Nightshade - Curse of Shade
				useSkill(4705, target);
				break;
			
			case 1014: // Nightshade - Mass Curse of Shade
				useSkill(4706, player);
				break;
			
			case 1015: // Nightshade - Shade Sacrifice
				useSkill(4707, target);
				break;
			
			case 1016: // Cursed Man - Cursed Blow
				useSkill(4709, target);
				break;
			
			case 1017: // Cursed Man - Cursed Strike/Stun
				useSkill(4708, target);
				break;
			
			case 1031: // Feline King - Slash
				useSkill(5135, target);
				break;
			
			case 1032: // Feline King - Spinning Slash
				useSkill(5136, target);
				break;
			
			case 1033: // Feline King - Grip of the Cat
				useSkill(5137, target);
				break;
			
			case 1034: // Magnus the Unicorn - Whiplash
				useSkill(5138, target);
				break;
			
			case 1035: // Magnus the Unicorn - Tridal Wave
				useSkill(5139, target);
				break;
			
			case 1036: // Spectral Lord - Corpse Kaboom
				useSkill(5142, target);
				break;
			
			case 1037: // Spectral Lord - Dicing Death
				useSkill(5141, target);
				break;
			
			case 1038: // Spectral Lord - Force Curse
				useSkill(5140, target);
				break;
			
			case 1039: // Swoop Cannon - Cannon Fodder
				if (target instanceof L2DoorInstance)
				{
					player.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return;
				}
				
				useSkill(5110, target);
				break;
			
			case 1040: // Swoop Cannon - Big Bang
				if (target instanceof L2DoorInstance)
				{
					player.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return;
				}
				
				useSkill(5111, target);
				break;
			
			default:
				_log.warning(RequestActionUse.class.getName() + ": " + player.getName() + ": unhandled action type " + _actionId);
		}
	}

	private boolean useSkill(int skillId, L2Object target)
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		// No owner, or owner in shop mode.
		if (player == null || player.isInStoreMode())
			return false;
		
		final L2Summon summon = player.getPet();
		if (summon == null || target == null)
			return false;
		
		// Pet which is 20 levels higher than owner.
		if (summon instanceof L2PetInstance && summon.getLevel() - player.getLevel() > 20)
		{
			player.sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL);
			return false;
		}
		
		// Out of control pet.
		if (summon.isOutOfControl())
		{
			player.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
			return false;
		}
		
		if(summon.getTemplate().getSkills() == null || skillId <= 0)
			return false;
		
		// Verify if the launched skill is mastered by the summon.
		final L2Skill skill = summon.getTemplate().getSkills().get(skillId);

		if (skill == null)
			return false;
		
		// Can't launch offensive skills on owner.
		if (skill.isOffensive() && player == target)
			return false;
		
		summon.setTarget(target);
		summon.useMagic(skill, _ctrlPressed, _shiftPressed);
		return true;
	}

	public static boolean checkforsummons(int[] array, int obj)
	{
		if (array == null || array.length == 0)
			return false;
		
		for (int element : array)
			if (element == obj)
				return true;
		
		return false;
	}
	
	private static final String[] SIN_EATER_ACTIONS_STRINGS =
	{
		"special skill? Abuses in this kind of place, can turn blood Knots...!",
		"Hey! Brother! What do you anticipate to me?",
		"shouts ha! Flap! Flap! Response?",
		", has not hit...!"
	};
	
	private static final int[] NOATTACKACTION_SUMMONS =
	{
		12564,
		12621,
		14702,
		14703,
		14704,
		14705,
		14706,
		14707,
		14708,
		14709,
		14710,
		14711,
		14712,
		14713,
		14714,
		14715,
		14716,
		14717,
		14718,
		14719,
		14720,
		14721,
		14722,
		14723,
		14724,
		14725,
		14726,
		14727,
		14728,
		14729,
		14730,
		14731,
		14732,
		14733,
		14734,
		14735,
		14736
	};

	@Override
	public String getType()
	{
		return _C__45_REQUESTACTIONUSE;
	}
}