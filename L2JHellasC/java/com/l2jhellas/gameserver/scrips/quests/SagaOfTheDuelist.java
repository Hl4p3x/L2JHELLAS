package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfTheDuelist extends SagasSuperClass
{
	public static String qn1 = "Q073_SagaOfTheDuelist";
	public static int qnu = 73;
	public static String qna = "Saga of the Duelist";
	
	public SagaOfTheDuelist()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			30849,
			31624,
			31226,
			31331,
			31639,
			31646,
			31647,
			31653,
			31654,
			31655,
			31656,
			31277
		};
		
		Items = new int[]
		{
			7080,
			7537,
			7081,
			7488,
			7271,
			7302,
			7333,
			7364,
			7395,
			7426,
			7096,
			7546
		};
		
		Mob = new int[]
		{
			27289,
			27222,
			27281
		};
		
		qn = qn1;
		classid = 88;
		prevclass = 0x02;
		
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