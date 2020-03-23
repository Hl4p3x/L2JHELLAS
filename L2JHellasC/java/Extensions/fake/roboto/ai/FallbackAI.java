package Extensions.fake.roboto.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Extensions.fake.roboto.FakePlayer;
import Extensions.fake.roboto.model.HealingSpell;
import Extensions.fake.roboto.model.OffensiveSpell;
import Extensions.fake.roboto.model.SupportSpell;

import com.l2jhellas.gameserver.enums.items.ShotType;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.util.Rnd;

public class FallbackAI extends CombatAI
{
	
	public FallbackAI(FakePlayer character)
	{
		super(character);
	}
	
	@Override
	public void thinkAndAct()
	{
		super.thinkAndAct();
		setBusyThinking(true);
		applyDefaultBuffs();
		handleShots();
		getCloserTarget(L2PcInstance.class, 70);
		avoid((L2Character) _fakePlayer.getTarget());
		setBusyThinking(false);
	}
	
	protected void getCloserTarget(Class<? extends L2Character> L2CharacterClass, int radius)
	{
		if (_fakePlayer.getTarget() == null)
		{
			L2World.getInstance().forEachVisibleObjectInRange(_fakePlayer, L2CharacterClass, radius, target ->
			{
				if (!target.isDead())
				{
					_fakePlayer.setTarget(target);
					return;
				}
				else if (Rnd.get(600) < 50)
					_fakePlayer.broadcastPacket(new CreatureSay(_fakePlayer.getObjectId(), 0, _fakePlayer.getName(), Rnd.get(getDeadComments())));
				
			});
		}
		else
		{
			if (((L2Character) _fakePlayer.getTarget()).isDead())
			{
				if (Rnd.get(600) < 50)
					_fakePlayer.broadcastPacket(new CreatureSay(_fakePlayer.getObjectId(), 0, _fakePlayer.getName(), Rnd.get(getDeadComments())));
				
				L2World.getInstance().forEachVisibleObjectInRange(_fakePlayer, L2CharacterClass, radius, target ->
				{
					if (!target.isDead())
					{
						_fakePlayer.setTarget(target);
						return;
					}
				});
			}
		}
	}
	
	private void avoid(L2Character avoid)
	{
		if (avoid == null || _fakePlayer == null || _fakePlayer == avoid)
			return;
		
		if (_fakePlayer.isDead() || _fakePlayer.isMovementDisabled())
			return;
		
		if (!_fakePlayer.isMoving() && Rnd.get(400) < 50)
		{
			final int X = _fakePlayer.getX();
			final int Y = _fakePlayer.getY();
			
			final double angle = Math.toRadians(Rnd.get(-200, 200)) + Math.atan2(Y - avoid.getY(), X - avoid.getX());
			
			final int targetX = Rnd.get(400) < 50 ? avoid.getX() : X + (int) (150 * Math.cos(angle));
			final int targetY = Rnd.get(400) < 50 ? avoid.getY() : Y + (int) (150 * Math.sin(angle));
			
			moveTo(targetX, targetY, avoid.getZ());
			
			_fakePlayer.setTarget(null);
			
			if (Rnd.get(400) < 50)
				_fakePlayer.broadcastPacket(new CreatureSay(_fakePlayer.getObjectId(), 0, _fakePlayer.getName(), Rnd.get(getAvoidComments())));
		}
	}
	
	public List<String> getAvoidComments()
	{
		List<String> worlds = new ArrayList<>();
		worlds.add("....");
		worlds.add("...");
		worlds.add("wtf");
		worlds.add("pff");
		return worlds;
	}
	
	public List<String> getDeadComments()
	{
		List<String> worlds = new ArrayList<>();
		worlds.add("dn exw res");
		worlds.add("tovilage re noobaki");
		worlds.add("???");
		worlds.add("??");
		return worlds;
	}
	
	@Override
	protected ShotType getShotType()
	{
		return ShotType.SOULSHOT;
	}
	
	@Override
	protected List<OffensiveSpell> getOffensiveSpells()
	{
		return Collections.emptyList();
	}
	
	@Override
	protected int[][] getBuffs()
	{
		return new int[0][0];
	}
	
	@Override
	protected List<HealingSpell> getHealingSpells()
	{
		return Collections.emptyList();
	}
	
	@Override
	protected List<SupportSpell> getSelfSupportSpells()
	{
		return Collections.emptyList();
	}
}
