package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfTheHierophant extends SagasSuperClass
{
	public static String qn1 = "Q086_SagaOfTheHierophant";
	public static int qnu = 86;
	public static String qna = "Saga of the Hierophant";
	
	public SagaOfTheHierophant()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			30191,
			31626,
			31588,
			31280,
			31591,
			31646,
			31648,
			31652,
			31654,
			31655,
			31659,
			31280
		};
		
		Items = new int[]
		{
			7080,
			7523,
			7081,
			7501,
			7284,
			7315,
			7346,
			7377,
			7408,
			7439,
			7089,
			0
		};
		
		Mob = new int[]
		{
			27269,
			27235,
			27275
		};
		
		qn = qn1;
		classid = 98;
		prevclass = 0x11;
		
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