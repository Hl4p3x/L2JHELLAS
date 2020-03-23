package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfTheTitan extends SagasSuperClass
{
	public static String qn1 = "Q075_SagaOfTheTitan";
	public static int qnu = 75;
	public static String qna = "Saga of the Titan";
	
	public SagaOfTheTitan()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			31327,
			31624,
			31289,
			31290,
			31607,
			31646,
			31649,
			31651,
			31654,
			31655,
			31658,
			31290
		};
		
		Items = new int[]
		{
			7080,
			7539,
			7081,
			7490,
			7273,
			7304,
			7335,
			7366,
			7397,
			7428,
			7098,
			0
		};
		
		Mob = new int[]
		{
			27292,
			27224,
			27283
		};
		
		qn = qn1;
		classid = 113;
		prevclass = 0x2e;
		
		X = new int[]
		{
			119518,
			181215,
			181227
		};
		
		Y = new int[]
		{
			-28658,
			36676,
			36703
		};
		
		Z = new int[]
		{
			-3811,
			-4812,
			-4816
		};
		
		registerNPCs();
	}
}