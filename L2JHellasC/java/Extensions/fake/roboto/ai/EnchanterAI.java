package Extensions.fake.roboto.ai;

import Extensions.fake.roboto.FakePlayer;
import Extensions.fake.roboto.helpers.FakeHelpers;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.util.Rnd;

public class EnchanterAI extends FakePlayerAI
{
	
	private final int _maxEnchant = Config.ENCHANT_MAX_WEAPON;
	
	public EnchanterAI(FakePlayer character)
	{
		super(character);
	}
	
	@Override
	public void setup()
	{
		super.setup();
		L2ItemInstance weapon = _fakePlayer.getActiveWeaponInstance();
		weapon = checkIfWeaponIsExistsEquipped(weapon);
		_fakePlayer.broadcastUserInfo();
	}
	
	@Override
	public void thinkAndAct()
	{
		handleDeath();
		setBusyThinking(true);
		
		if (Rnd.get(1000) <= 50)
		{
			L2ItemInstance weapon = _fakePlayer.getActiveWeaponInstance();
			weapon = checkIfWeaponIsExistsEquipped(weapon);
			double chance = getSuccessChance(weapon);
			
			int currentEnchantLevel = weapon.getEnchantLevel();
			if (currentEnchantLevel < _maxEnchant || serverHasUnlimitedMax())
			{
				if (Rnd.nextDouble() < chance || weapon.getEnchantLevel() < 4 && weapon.getEnchantLevel() <= Config.ENCHANT_MAX_WEAPON)
				{
					weapon.setEnchantLevel(currentEnchantLevel + 1);
					_fakePlayer.broadcastUserInfo();
				}
				else
				{
					destroyFailedItem(weapon);
				}
			}
		}
		setBusyThinking(false);
	}
	
	private void destroyFailedItem(L2ItemInstance weapon)
	{
		_fakePlayer.getInventory().destroyItem("Enchant", weapon, _fakePlayer, null);
		_fakePlayer.broadcastUserInfo();
		_fakePlayer.setActiveEnchantItem(null);
	}
	
	private static double getSuccessChance(L2ItemInstance weapon)
	{
		return (weapon.getEnchantLevel() > 14) ? 75 : 80;
	}
	
	private boolean serverHasUnlimitedMax()
	{
		return _maxEnchant == 0;
	}
	
	private L2ItemInstance checkIfWeaponIsExistsEquipped(L2ItemInstance weapon)
	{
		if (weapon == null)
		{
			FakeHelpers.giveWeaponsByClass(_fakePlayer, false);
			weapon = _fakePlayer.getActiveWeaponInstance();
		}
		return weapon;
	}
	
	@Override
	protected int[][] getBuffs()
	{
		return new int[0][0];
	}
}
