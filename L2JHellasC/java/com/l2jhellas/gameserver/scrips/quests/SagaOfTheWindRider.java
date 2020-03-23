package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfTheWindRider extends SagasSuperClass
{
	public static String qn1 = "Q080_SagaOfTheWindRider";
	public static int qnu = 80;
	public static String qna = "Saga of the Wind Rider";
	
	public SagaOfTheWindRider()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			31603,
			31624,
			31284,
			31615,
			31612,
			31646,
			31648,
			31652,
			31654,
			31655,
			31659,
			31616
		};
		
		Items = new int[]
		{
			7080,
			7517,
			7081,
			7495,
			7278,
			7309,
			7340,
			7371,
			7402,
			7433,
			7103,
			0
		};
		
		Mob = new int[]
		{
			27300,
			27229,
			27303
		};
		
		qn = qn1;
		classid = 101;
		prevclass = 0x17;
		
		X = new int[]
		{
			161719,
			124314,
			124355
		};
		
		Y = new int[]
		{
			-92823,
			82155,
			82155
		};
		
		Z = new int[]
		{
			-1893,
			-2803,
			-2803
		};
		
		registerNPCs();
	}
}