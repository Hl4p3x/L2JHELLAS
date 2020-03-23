package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfTheSpectralMaster extends SagasSuperClass
{
	public static String qn1 = "Q093_SagaOfTheSpectralMaster";
	public static int qnu = 93;
	public static String qna = "Saga of the Spectral Master";
	
	public SagaOfTheSpectralMaster()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			30175,
			31287,
			31613,
			30175,
			31632,
			31646,
			31649,
			31653,
			31654,
			31655,
			31656,
			31613
		};
		
		Items = new int[]
		{
			7080,
			7606,
			7081,
			7508,
			7291,
			7322,
			7353,
			7384,
			7415,
			7446,
			7112,
			0
		};
		
		Mob = new int[]
		{
			27315,
			27242,
			27312
		};
		
		qn = qn1;
		classid = 111;
		prevclass = 0x29;
		
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