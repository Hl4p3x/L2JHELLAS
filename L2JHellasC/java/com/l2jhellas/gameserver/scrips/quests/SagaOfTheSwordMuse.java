package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfTheSwordMuse extends SagasSuperClass
{
	public static String qn1 = "Q072_SagaOfTheSwordMuse";
	public static int qnu = 72;
	public static String qna = "Saga of the Sword Muse";
	
	public SagaOfTheSwordMuse()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			30853,
			31624,
			31583,
			31537,
			31618,
			31646,
			31649,
			31652,
			31654,
			31655,
			31659,
			31281
		};
		
		Items = new int[]
		{
			7080,
			7536,
			7081,
			7487,
			7270,
			7301,
			7332,
			7363,
			7394,
			7425,
			7095,
			6482
		};
		
		Mob = new int[]
		{
			27288,
			27221,
			27280
		};
		
		qn = qn1;
		classid = 100;
		prevclass = 0x15;
		
		X = new int[]
		{
			161719,
			124355,
			124376
		};
		
		Y = new int[]
		{
			-92823,
			82155,
			82127
		};
		
		Z = new int[]
		{
			-1893,
			-2803,
			-2796
		};
		
		registerNPCs();
	}
}