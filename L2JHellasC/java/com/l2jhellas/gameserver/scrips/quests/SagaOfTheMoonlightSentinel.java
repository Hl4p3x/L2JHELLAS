package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfTheMoonlightSentinel extends SagasSuperClass
{
	public static String qn1 = "Q083_SagaOfTheMoonlightSentinel";
	public static int qnu = 83;
	public static String qna = "Saga of the Moonlight Sentinel";
	
	public SagaOfTheMoonlightSentinel()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			30702,
			31627,
			31604,
			31640,
			31634,
			31646,
			31648,
			31652,
			31654,
			31655,
			31658,
			31641
		};
		
		Items = new int[]
		{
			7080,
			7520,
			7081,
			7498,
			7281,
			7312,
			7343,
			7374,
			7405,
			7436,
			7106,
			0
		};
		
		Mob = new int[]
		{
			27297,
			27232,
			27306
		};
		
		qn = qn1;
		classid = 102;
		prevclass = 0x18;
		
		X = new int[]
		{
			161719,
			181227,
			181215
		};
		
		Y = new int[]
		{
			-92823,
			36703,
			36676
		};
		
		Z = new int[]
		{
			-1893,
			-4816,
			-4812
		};
		
		registerNPCs();
	}
}