package com.l2jhellas.gameserver.enums.skills;

public enum AbnormalEffect
{
	NULL("null", 0),
	BLEEDING("bleeding", 1),
	POISON("poison", 2),
	REDCIRCLE("redcircle", 4),
	ICE("ice", 8),
	WIND("wind", 16),
	FEAR("fear", 32),
	STUN("stun", 64),
	SLEEP("sleep", 128),
	MUTED("mute", 256),
	ROOT("root", 512),
	HOLD_1("hold1", 1024),
	HOLD_2("hold2", 2048),
	UNKNOWN_13("unknown13", 4096),
	BIG_HEAD("bighead", 8192),
	FLAME("flame", 16384),
	CHANGE_TEXTURE("changetexture", 32768),
	GROW("grow", 65536),
	FLOATING_ROOT("floatroot", 131072),
	DANCE_STUNNED("dancestun", 262144),
	FIREROOT_STUN("firerootstun", 524288),
	STEALTH("stealth", 1048576),
	IMPRISIONING_1("imprison1", 2097152),
	IMPRISIONING_2("imprison2", 4194304),
	MAGIC_CIRCLE("magiccircle", 8388608);
	
	private final int _mask;
	private final String _name;
	
	private AbnormalEffect(String name, int mask)
	{
		_name = name;
		_mask = mask;
	}
	
	public final int getMask()
	{
		return _mask;
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public static AbnormalEffect FindById(int mask)
	{
		for (AbnormalEffect eff : AbnormalEffect.values())
		{
			if (eff.getMask() == mask)
				return eff;
		}
		return null;
	}
}