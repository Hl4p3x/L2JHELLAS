package Extensions.fake.roboto.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Extensions.fake.roboto.FakePlayer;
import Extensions.fake.roboto.ai.addon.IHealer;
import Extensions.fake.roboto.helpers.FakeHelpers;
import Extensions.fake.roboto.model.HealingSpell;
import Extensions.fake.roboto.model.OffensiveSpell;
import Extensions.fake.roboto.model.SupportSpell;

import com.l2jhellas.gameserver.enums.items.ShotType;
import com.l2jhellas.gameserver.enums.skills.L2SkillTargetType;

public class CardinalAI extends CombatAI implements IHealer
{
	public CardinalAI(FakePlayer character)
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
		tryTargetingLowestHpTargetInRadius(_fakePlayer, FakePlayer.class, FakeHelpers.getTestTargetRange());
		tryHealingTarget(_fakePlayer);
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
		return Collections.emptyList();
	}
	
	@Override
	protected List<HealingSpell> getHealingSpells()
	{
		List<HealingSpell> _healingSpells = new ArrayList<>();
		_healingSpells.add(new HealingSpell(1218, L2SkillTargetType.TARGET_ONE, 70, 1));
		_healingSpells.add(new HealingSpell(1217, L2SkillTargetType.TARGET_ONE, 80, 3));
		return _healingSpells;
	}
	
	@Override
	protected int[][] getBuffs()
	{
		return FakeHelpers.getMageBuffs();
	}
	
	@Override
	protected List<SupportSpell> getSelfSupportSpells()
	{
		return Collections.emptyList();
	}
}
