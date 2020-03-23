package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfTheMysticMuse extends SagasSuperClass
{
	public static String qn1 = "Q089_SagaOfTheMysticMuse";
	public static int qnu = 89;
	public static String qna = "Saga of the Mystic Muse";
	
	public SagaOfTheMysticMuse()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			30174,
			31627,
			31283,
			31283,
			31643,
			31646,
			31648,
			31651,
			31654,
			31655,
			31658,
			31283
		};
		
		Items = new int[]
		{
			7080,
			7530,
			7081,
			7504,
			7287,
			7318,
			7349,
			7380,
			7411,
			7442,
			7083,
			0
		};
		
		Mob = new int[]
		{
			27251,
			27238,
			27255
		};
		
		qn = qn1;
		classid = 103;
		prevclass = 0x1b;
		
		X = new int[]
		{
			119518,
			181227,
			181215
		};
		
		Y = new int[]
		{
			-28658,
			36703,
			36676
		};
		
		Z = new int[]
		{
			-3811,
			-4816,
			-4812
		};
		
		registerNPCs();
	}
}