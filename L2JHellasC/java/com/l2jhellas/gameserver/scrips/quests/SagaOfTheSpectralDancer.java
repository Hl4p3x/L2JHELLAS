package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfTheSpectralDancer extends SagasSuperClass
{
	public static String qn1 = "Q096_SagaOfTheSpectralDancer";
	public static int qnu = 96;
	public static String qna = "Saga of the Spectral Dancer";
	
	public SagaOfTheSpectralDancer()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			31582,
			31623,
			31284,
			31284,
			31611,
			31646,
			31649,
			31653,
			31654,
			31655,
			31656,
			31284
		};
		
		Items = new int[]
		{
			7080,
			7527,
			7081,
			7511,
			7294,
			7325,
			7356,
			7387,
			7418,
			7449,
			7092,
			0
		};
		
		Mob = new int[]
		{
			27272,
			27245,
			27264
		};
		
		qn = qn1;
		classid = 107;
		prevclass = 0x22;
		
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