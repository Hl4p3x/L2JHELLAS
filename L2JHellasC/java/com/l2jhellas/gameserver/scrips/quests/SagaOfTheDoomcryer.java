package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfTheDoomcryer extends SagasSuperClass
{
	public static String qn1 = "Q078_SagaOfTheDoomcryer";
	public static int qnu = 78;
	public static String qna = "Saga of the Doomcryer";
	
	public SagaOfTheDoomcryer()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			31336,
			31624,
			31589,
			31290,
			31642,
			31646,
			31649,
			31650,
			31654,
			31655,
			31657,
			31290
		};
		
		Items = new int[]
		{
			7080,
			7539,
			7081,
			7493,
			7276,
			7307,
			7338,
			7369,
			7400,
			7431,
			7101,
			0
		};
		
		Mob = new int[]
		{
			27295,
			27227,
			27285
		};
		
		qn = qn1;
		classid = 116;
		prevclass = 0x34;
		
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