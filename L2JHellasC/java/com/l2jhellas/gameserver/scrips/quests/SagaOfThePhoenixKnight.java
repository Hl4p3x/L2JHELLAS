package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfThePhoenixKnight extends SagasSuperClass
{
	public static String qn1 = "Q070_SagaOfThePhoenixKnight";
	public static int qnu = 70;
	public static String qna = "Saga of the Phoenix Knight";
	
	public SagaOfThePhoenixKnight()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			30849,
			31624,
			31277,
			30849,
			31631,
			31646,
			31647,
			31650,
			31654,
			31655,
			31657,
			31277
		};
		
		Items = new int[]
		{
			7080,
			7534,
			7081,
			7485,
			7268,
			7299,
			7330,
			7361,
			7392,
			7423,
			7093,
			6482
		};
		
		Mob = new int[]
		{
			27286,
			27219,
			27278
		};
		
		qn = qn1;
		classid = 90;
		prevclass = 0x05;
		
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