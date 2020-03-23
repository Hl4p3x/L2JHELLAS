package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfTheDominator extends SagasSuperClass
{
	public static String qn1 = "Q077_SagaOfTheDominator";
	public static int qnu = 77;
	public static String qna = "Saga of the Dominator";
	
	public SagaOfTheDominator()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			31336,
			31624,
			31371,
			31290,
			31636,
			31646,
			31648,
			31653,
			31654,
			31655,
			31656,
			31290
		};
		
		Items = new int[]
		{
			7080,
			7539,
			7081,
			7492,
			7275,
			7306,
			7337,
			7368,
			7399,
			7430,
			7100,
			0
		};
		
		Mob = new int[]
		{
			27294,
			27226,
			27262
		};
		
		qn = qn1;
		classid = 115;
		prevclass = 0x33;
		
		X = new int[]
		{
			164650,
			47429,
			47391
		};
		
		Y = new int[]
		{
			-74121,
			-56923,
			-56929
		};
		
		Z = new int[]
		{
			-2871,
			-2383,
			-2370
		};
		
		registerNPCs();
	}
}