package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfTheSagittarius extends SagasSuperClass
{
	public static String qn1 = "Q082_SagaOfTheSagittarius";
	public static int qnu = 82;
	public static String qna = "Saga of the Sagittarius";
	
	public SagaOfTheSagittarius()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			30702,
			31627,
			31604,
			31640,
			31633,
			31646,
			31647,
			31650,
			31654,
			31655,
			31657,
			31641
		};
		
		Items = new int[]
		{
			7080,
			7519,
			7081,
			7497,
			7280,
			7311,
			7342,
			7373,
			7404,
			7435,
			7105,
			0
		};
		
		Mob = new int[]
		{
			27296,
			27231,
			27305
		};
		
		qn = qn1;
		classid = 92;
		prevclass = 0x09;
		
		X = new int[]
		{
			191046,
			46066,
			46066
		};
		
		Y = new int[]
		{
			-40640,
			-36396,
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