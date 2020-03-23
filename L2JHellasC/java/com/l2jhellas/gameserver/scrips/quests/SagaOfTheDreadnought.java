package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfTheDreadnought extends SagasSuperClass
{
	public static String qn1 = "Q074_SagaOfTheDreadnought";
	public static int qnu = 74;
	public static String qna = "Saga of the Dreadnought";
	
	public SagaOfTheDreadnought()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			30850,
			31624,
			31298,
			31276,
			31595,
			31646,
			31648,
			31650,
			31654,
			31655,
			31657,
			31522
		};
		
		Items = new int[]
		{
			7080,
			7538,
			7081,
			7489,
			7272,
			7303,
			7334,
			7365,
			7396,
			7427,
			7097,
			6480
		};
		
		Mob = new int[]
		{
			27290,
			27223,
			27282
		};
		
		qn = qn1;
		classid = 89;
		prevclass = 0x03;
		
		X = new int[]
		{
			191046,
			46087,
			46066
		};
		
		Y = new int[]
		{
			-40640,
			-36372,
			-36396
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