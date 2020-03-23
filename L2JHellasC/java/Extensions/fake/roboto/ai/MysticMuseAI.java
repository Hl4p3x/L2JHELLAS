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

public class MysticMuseAI extends CombatAI
{
	public MysticMuseAI(FakePlayer character)
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
		tryAttackingUsingMageOffensiveSkill();
		setBusyThinking(false);
	}
	
	@Override
	protected ShotType getShotType()
	{
		return ShotType.BLESSED_SPIRITSHOT;
	}
	
	@Override
	protected List<OffensiveSpell> getOffensiveSpells()
	{
		List<OffensiveSpell> _offensiveSpells = new ArrayList<>();
		_offensiveSpells.add(new OffensiveSpell(1235, 4));
		_offensiveSpells.add(new OffensiveSpell(1340, 3));
		_offensiveSpells.add(new OffensiveSpell(1342, 2));
		_offensiveSpells.add(new OffensiveSpell(1265, 1));
		return _offensiveSpells;
	}
	
	@Override
	protected int[][] getBuffs()
	{
		return FakeHelpers.getMageBuffs();
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