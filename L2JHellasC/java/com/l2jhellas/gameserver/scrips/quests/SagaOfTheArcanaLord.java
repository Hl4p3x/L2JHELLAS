package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfTheArcanaLord extends SagasSuperClass
{
	public static String qn1 = "Q091_SagaOfTheArcanaLord";
	public static int qnu = 91;
	public static String qna = "Saga of the Arcana Lord";
	
	public SagaOfTheArcanaLord()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			31605,
			31622,
			31585,
			31608,
			31586,
			31646,
			31647,
			31651,
			31654,
			31655,
			31658,
			31608
		};
		
		Items = new int[]
		{
			7080,
			7604,
			7081,
			7506,
			7289,
			7320,
			7351,
			7382,
			7413,
			7444,
			7110,
			0
		};
		
		Mob = new int[]
		{
			27313,
			27240,
			27310
		};
		
		qn = qn1;
		classid = 96;
		prevclass = 0x0e;
		
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