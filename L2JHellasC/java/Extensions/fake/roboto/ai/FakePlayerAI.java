package Extensions.fake.roboto.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import Extensions.fake.roboto.FakePlayer;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable.TeleportWhereType;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.enums.skills.AbnormalEffect;
import com.l2jhellas.gameserver.enums.skills.L2SkillTargetType;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.geometry.Point3D;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Effect.EffectType;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.network.serverpackets.MoveToLocation;
import com.l2jhellas.gameserver.network.serverpackets.MoveToPawn;
import com.l2jhellas.gameserver.network.serverpackets.StopMove;
import com.l2jhellas.gameserver.network.serverpackets.StopRotation;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.util.Rnd;

public abstract class FakePlayerAI
{
	protected final FakePlayer _fakePlayer;
	FakePlayer _target;
	protected volatile boolean _clientMoving;
	protected volatile boolean _clientAutoAttacking;
	private long _moveToPawnTimeout;
	protected int _clientMovingToPawnOffset;
	protected boolean _isBusyThinking = false;
	protected int iterationsOnDeath = 0;
	private final int toVillageIterationsOnDeath = 22;
	boolean usingress = false;
	
	public FakePlayerAI(FakePlayer character)
	{
		_fakePlayer = character;
		setup();
		applyDefaultBuffs();
	}
	
	public void setup()
	{
		_fakePlayer.setIsRunning(true);
	}
	
	protected void applyDefaultBuffs()
	{
		for (int[] buff : getBuffs())
		{
			try
			{
				Map<Integer, L2Effect> activeEffects = Arrays.stream(_fakePlayer.getAllEffects()).filter(x -> x.getEffectType() == EffectType.BUFF).collect(Collectors.toMap(x -> x.getSkill().getId(), x -> x));
				
				if (!activeEffects.containsKey(buff[0]))
					SkillTable.getInstance().getInfo(buff[0], buff[1]).getEffects(_fakePlayer, _fakePlayer);
				else
				{
					if ((activeEffects.get(buff[0]).getPeriod() - activeEffects.get(buff[0]).getTime()) <= 20)
					{
						SkillTable.getInstance().getInfo(buff[0], buff[1]).getEffects(_fakePlayer, _fakePlayer);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	protected void handleDeath()
	{
		if (_fakePlayer.isDead())
			tryRandomDeathAction();
	}
	
	public void setBusyThinking(boolean thinking)
	{
		_isBusyThinking = thinking;
	}
	
	public boolean isBusyThinking()
	{
		return _isBusyThinking;
	}
	
	protected void tryRandomDeathAction()
	{
		List<FakePlayer> targets = new ArrayList<>();
		
		if (!usingress)
		{
			L2World.getInstance().forEachVisibleObjectInRange(_fakePlayer, FakePlayer.class, 400, target ->
			{
				if (!target.isDead())
					targets.add(target);
			});
			
			if (targets.isEmpty())
			{
				toVillageOnDeath();
				iterationsOnDeath = 0;
				usingress = false;
				
				if (_target != null)
				{
					_target.getFakeAi().setBusyThinking(false);
					_target.setIsInvul(false);
				}
				
				return;
			}
			
			_target = targets.get(Rnd.get(0, targets.size() - 1));
			_target.getFakeAi().setBusyThinking(true);
			_target.setIsInvul(true);
			_target.abortAllAttacks();
			_target.setTarget(_fakePlayer);
			_target.moveToLocation(_fakePlayer.getX(), _fakePlayer.getY(), _fakePlayer.getZ(), Rnd.get(25, 45));
			_target.broadcastPacket(new MoveToLocation(_target));
			
			L2Skill skill = SkillTable.getInstance().getInfo(2014, 1);
			_target.getFakeAi().castSpell(skill);
			usingress = true;
		}
		else if (!_target.isCastingNow() && _target.isInsideRadius(_fakePlayer, 80, true, false))
		{
			_target.getFakeAi().setBusyThinking(false);
			_target.setIsInvul(false);
			ThreadPoolManager.getInstance().scheduleGeneral(new ReviveTask(_fakePlayer), 2500);
		}
		else
		{
			iterationsOnDeath++;
			
			if (iterationsOnDeath >= toVillageIterationsOnDeath)
			{
				toVillageOnDeath();
				iterationsOnDeath = 0;
				usingress = false;
				
				if (_target != null)
				{
					_target.getFakeAi().setBusyThinking(false);
					_target.setIsInvul(false);
				}
			}
		}
	}
	
	class ReviveTask implements Runnable
	{
		FakePlayer _player;
		
		ReviveTask(FakePlayer player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			_player.doRevive();
			usingress = false;
		}
	}
	
	public void tryTargetRandomCreatureByTypeInRadius(Class<? extends L2Character> L2CharacterClass, int radius)
	{
		if (_fakePlayer.getTarget() == null)
		{
			List<L2Character> targets = new ArrayList<>();
			
			L2World.getInstance().forEachVisibleObjectInRange(_fakePlayer, L2CharacterClass, radius, target ->
			{
				if (target.isDead() || target.isInsideZone(ZoneId.PEACE) || target.isPlayer() && !target.getActingPlayer().getAppearance().isVisible() || !target.isVisible())
					return;
								
				targets.add(target);
			});
			
			if (!targets.isEmpty())
			{
				L2Character target = targets.get(Rnd.get(0, targets.size() - 1));
				_fakePlayer.setTarget(target);
			}
		}
		else
		{
			if (((L2Character) _fakePlayer.getTarget()).isDead() || !_fakePlayer.isInsideRadius(_fakePlayer.getTarget(), radius, false, false))
				_fakePlayer.setTarget(null);
		}
	}
	
	public void castSpell(L2Skill skill)
	{
		if (!_fakePlayer.isCastingNow())
		{
			
			if (skill.getTargetType() == L2SkillTargetType.TARGET_GROUND)
			{
				if (maybeMoveToPosition((_fakePlayer).getCurrentSkillWorldPosition(), skill.getCastRange()))
				{
					return;
				}
			}
			else
			{
				if (checkTargetLost(_fakePlayer.getTarget()))
				{
					if (skill.isOffensive() && _fakePlayer.getTarget() != null)
						_fakePlayer.setTarget(null);
					
					return;
				}
				
				if (_fakePlayer.getTarget() != null)
				{
					if (maybeMoveToPawn(_fakePlayer.getTarget(), skill.getCastRange()))
					{
						return;
					}
				}
			}
			
			if (skill.getHitTime() > 50)
				clientStopMoving(null);
			
			_fakePlayer.doCast(skill);
		}
	}
	
	protected void castSelfSpell(L2Skill skill)
	{
		if (!_fakePlayer.isCastingNow() && !_fakePlayer.isSkillDisabled(skill.getId()))
		{
			
			if (skill.getHitTime() > 50)
				clientStopMoving(null);
			
			_fakePlayer.doCast(skill);
		}
	}
	
	protected void toVillageOnDeath()
	{
		final Location location = MapRegionTable.getInstance().getTeleToLocation(_fakePlayer, TeleportWhereType.TOWN);
		_fakePlayer.setTarget(null);
		
		_fakePlayer.getFakeAi().setBusyThinking(true);
		
		if (_fakePlayer.isDead())
			_fakePlayer.doRevive();
		
		if (Rnd.get(1, 2) == 1)
		    _fakePlayer.assignCustomAI(2);
		else
			_fakePlayer.assignCustomAI(0);
		
		_fakePlayer.teleToLocation(location,true);
				
		_fakePlayer.broadcastUserInfo();
			
		_fakePlayer.getFakeAi().setBusyThinking(false);
	}
	
	protected void clientStopMoving(Location loc)
	{
		if (_fakePlayer.isMoving())
			_fakePlayer.stopMove(loc);
		
		_clientMovingToPawnOffset = 0;
		
		if (_clientMoving || loc != null)
		{
			_clientMoving = false;
			
			_fakePlayer.broadcastPacket(new StopMove(_fakePlayer));
			
			if (loc != null)
				_fakePlayer.broadcastPacket(new StopRotation(_fakePlayer.getObjectId(), _fakePlayer.getHeading(), 0));
		}
	}
	
	protected boolean checkTargetLost(L2Object target)
	{
		if (target instanceof L2PcInstance)
		{
			final L2PcInstance victim = (L2PcInstance) target;
			if (victim.isFakeDeath())
			{
				victim.stopFakeDeath(null);
				return false;
			}
			
			if(!victim.getAppearance().isVisible() || !victim.isVisible())
			{
			    _fakePlayer.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				return true;
			}
		}
		
		if (target == null)
		{
			_fakePlayer.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			return true;
		}
		return false;
	}
	
	protected boolean maybeMoveToPosition(Point3D point3d, int offset)
	{
		if (point3d == null)
		{
			return false;
		}
		
		if (offset < 10)
			offset = 10;
		
		if (!_fakePlayer.isInsideRadius(point3d.getX(), point3d.getY(), offset + (int)_fakePlayer.getTemplate().getCollisionRadius(), false))
		{
			if (_fakePlayer.isMovementDisabled())
				return true;
			
			int x = _fakePlayer.getX();
			int y = _fakePlayer.getY();
			
			double dx = point3d.getX() - x;
			double dy = point3d.getY() - y;
			
			double dist = Math.sqrt(dx * dx + dy * dy);
			
			double sin = dy / dist;
			double cos = dx / dist;
			
			dist -= offset - 5;
			
			x += (int) (dist * cos);
			y += (int) (dist * sin);
			
			if (!_fakePlayer.isMoving())
				moveTo(x, y, point3d.getZ());
			
			return true;
		}
		
		return false;
	}
	
	protected void moveToPawn(L2Object pawn, int offset)
	{
		if (!_fakePlayer.isMovementDisabled())
		{
			if (offset < 10)
				offset = 10;
			
			boolean sendPacket = true;
			if (_clientMoving && (_fakePlayer.getTarget() == pawn))
			{
				if (_clientMovingToPawnOffset == offset)
				{
					if (System.currentTimeMillis() < _moveToPawnTimeout)
						return;
					
					sendPacket = false;
				}
				else if (_fakePlayer.isOnGeodataPath())
				{
					if (System.currentTimeMillis() < _moveToPawnTimeout + 1000)
						return;
				}
			}
			
			_clientMoving = true;
			_clientMovingToPawnOffset = offset;
			_fakePlayer.setTarget(pawn);
			_moveToPawnTimeout = System.currentTimeMillis() + 1000;
			
			if (pawn == null)
				return;
			
			_fakePlayer.moveToLocation(pawn.getX(), pawn.getY(), pawn.getZ(), offset);
			
			if (!_fakePlayer.isMoving())
			{
				return;
			}
			
			if (pawn instanceof L2Character)
			{
				if (_fakePlayer.isOnGeodataPath())
				{
					_fakePlayer.broadcastPacket(new MoveToLocation(_fakePlayer));
					_clientMovingToPawnOffset = 0;
				}
				else if (sendPacket)
					_fakePlayer.broadcastPacket(new MoveToPawn(_fakePlayer, pawn, offset));
			}
			else
				_fakePlayer.broadcastPacket(new MoveToLocation(_fakePlayer));
		}
	}
	
	public void moveTo(int x, int y, int z)
	{
		
		if (!_fakePlayer.isMovementDisabled())
		{
			_clientMoving = true;
			_clientMovingToPawnOffset = 0;
			_fakePlayer.moveToLocation(x, y, z, 0);
			
			if (_fakePlayer.isSpawnProtected())
			{
				_fakePlayer.stopAbnormalEffect(AbnormalEffect.IMPRISIONING_1);
				_fakePlayer.setProtection(false);
			}
			
			if (!_fakePlayer.isMoving())
				return;
			
			_fakePlayer.broadcastPacket(new MoveToLocation(_fakePlayer));
			
		}
	}
	
	protected boolean maybeMoveToPawn(L2Object target, int offset)
	{
		
		if (target == null || offset < 0)
			return false;
		
		offset += _fakePlayer.getTemplate().getCollisionRadius();
		if (target instanceof L2Character)
			offset += ((L2Character) target).getTemplate().getCollisionRadius();
		
		if (!_fakePlayer.isInsideRadius(target, offset, false, false))
		{
			if (_fakePlayer.isMovementDisabled())
			{
				if (_fakePlayer.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
					_fakePlayer.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				return true;
			}
			
			if (target instanceof L2Character && !(target instanceof L2DoorInstance))
			{
				if (((L2Character) target).isMoving())
					offset -= 30;
				
				if (offset < 5)
					offset = 5;
			}
			
			moveToPawn(target, offset);
			
			return true;
		}
		
		if (!GeoEngine.canSeeTarget(_fakePlayer, _fakePlayer.getTarget()))
		{
			moveToPawn(target, 50);
			return true;
		}
		
		return false;
	}
	
	public abstract void thinkAndAct();
	
	protected abstract int[][] getBuffs();
}
