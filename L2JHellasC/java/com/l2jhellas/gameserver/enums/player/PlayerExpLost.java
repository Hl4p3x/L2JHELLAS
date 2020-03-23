package com.l2jhellas.gameserver.enums.player;

/**
 * @author AbsolutePower
 */
public enum PlayerExpLost
{
	s9(9,9.1),
	s10(10,8.875),
	s11(11,8.74),
	s12(12,8.624),
	s13(13,8.5),
	s14(14,8.373),
	s15(15,8.23),
	s16(16,8.123),
	s17(17,8.0),
	s18(18,7.872),
	s19(19,7.72),
	s20(20,7.622),
	s21(21,7.5),
	s22(22,7.372),
	s23(23,7.22),
	s24(24,7.122),
	s25(25,7.0),
	s26(26,6.872),
	s27(27,6.72),
	s28(28,6.622),
	s29(29,6.5),
	s30(30,6.372),
	s31(31,6.22),
	s32(32,6.122),
	s33(33,6.0),
	s34(34,5.872),
	s35(35,5.75),
	s36(36,5.622),
	s37(37,5.5),
	s38(38,5.372),
	s39(39,5.22),
	s40(40,5.122),
	s41(41,5.0),
	s42(42,4.872),
	s43(43,4.72),
	s44(44,4.622),
	s45(45,4.2),
	s46(46,4.372),
	s47(47,4.22),
	s48(48,4.122),
	s49(49,4.0),
	s50(50,4.0),
	s51(51,4.0),
	s52(52,4.0),
	s53(53,4.0),
	s54(54,4.0),
	s55(55,4.0),
	s56(56,4.0),
	s57(57,4.0),
	s58(58,4.0),
	s59(59,4.0),
	s60(60,4.0),
	s61(61,4.0),
	s62(62,4.0),
	s63(63,4.0),
	s64(64,4.0),
	s65(65,4.0),
	s66(66,4.0),
	s67(67,4.0),
	s68(68,4.0),
	s69(69,4.0),
	s70(70,4.0),
	s71(71,4.0),
	s72(72,4.0),
	s73(73,4.0),
	s74(74,4.0),
	s75(75,4.0),
	s76(76,2.5),
	s77(77,2.0),
	s78(78,1.5),
	s79(79,1.0),
	s80(80,1.0),
	s81(81,1.0),
	s82(82,1.0);
	
	private final int _level;
	private final double _val;
	
	private PlayerExpLost(int level,double val)
	{
		_level = level;
		_val = val;
	}

	public int getLevel()
	{
		return _level;
	}

	public double getVal()
	{
		return _val;
	}
	
	public static double getExpLost(int level)
	{
		for (PlayerExpLost current : values())
		{
			if (current.getLevel() == level)
				return current.getVal();
		}		
		return 0;		
	}
}