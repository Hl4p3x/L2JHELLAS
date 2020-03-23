package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfTheFortuneSeeker extends SagasSuperClass
{
	public static String qn1 = "Q099_SagaOfTheFortuneSeeker";
	public static int qnu = 99;
	public static String qna = "Saga of the Fortune Seeker";
	
	public SagaOfTheFortuneSeeker()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			31594,
			31623,
			31600,
			31600,
			31601,
			31646,
			31649,
			31650,
			31654,
			31655,
			31657,
			31600
		};
		
		Items = new int[]
		{
			7080,
			7608,
			7081,
			7514,
			7297,
			7328,
			7359,
			7390,
			7421,
			7452,
			7109,
			0
		};
		
		Mob = new int[]
		{
			27259,
			27248,
			27309
		};
		
		qn = qn1;
		classid = 117;
		prevclass = 0x37;
		
		X = new int[]
		{
			191046,
			46066,
			46087
		};
		
		Y = new int[]
		{
			-40640,
			-36396,
			-36372
		};
		
		Z = new int[]
		{
			-3042,
			-1685,
			-1685
		};
		
		registerNPCs();
	}
}