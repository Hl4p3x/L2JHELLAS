package Extensions.fake.roboto.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Extensions.fake.roboto.FakePlayer;
import Extensions.fake.roboto.helpers.FakeHelpers;
import Extensions.fake.roboto.model.HealingSpell;
import Extensions.fake.roboto.model.OffensiveSpell;
import Extensions.fake.roboto.model.SupportSpell;

import com.l2jhellas.gameserver.enums.items.ShotType;

public class GrandKhavatariAI extends CombatAI
{
	public GrandKhavatariAI(FakePlayer character)
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
		tryTargetRandomCreatureByTypeInRadius(FakeHelpers.getTestTargetClass(), FakeHelpers.getTestTargetRange());
		tryAttackingUsingFighterOffensiveSkill();
		setBusyThinking(false);
	}
	
	@Override
	protected double changeOfUsingSkill()
	{
		return 0.5;
	}
	
	@Override
	protected ShotType getShotType()
	{
		return ShotType.SOULSHOT;
	}
	
	@Override
	protected List<OffensiveSpell> getOffensiveSpells()
	{
		List<OffensiveSpell> _offensiveSpells = new ArrayList<>();
		_offensiveSpells.add(new OffensiveSpell(284, 1));
		_offensiveSpells.add(new OffensiveSpell(281, 2));
		_offensiveSpells.add(new OffensiveSpell(280, 3));
		_offensiveSpells.add(new OffensiveSpell(54, 4));
		_offensiveSpells.add(new OffensiveSpell(346, 5));
		return _offensiveSpells;
	}
	
	@Override
	protected int[][] getBuffs()
	{
		return FakeHelpers.getFighterBuffs();
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