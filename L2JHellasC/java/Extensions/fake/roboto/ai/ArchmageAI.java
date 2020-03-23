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

public class ArchmageAI extends CombatAI
{
	public ArchmageAI(FakePlayer character)
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
		_offensiveSpells.add(new OffensiveSpell(1230, 1));
		_offensiveSpells.add(new OffensiveSpell(1339, 1));
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