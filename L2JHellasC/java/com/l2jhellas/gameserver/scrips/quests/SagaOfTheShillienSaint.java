package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfTheShillienSaint extends SagasSuperClass
{
	public static String qn1 = "Q098_SagaOfTheShillienSaint";
	public static int qnu = 98;
	public static String qna = "Saga of the Shillien Saint";
	
	public SagaOfTheShillienSaint()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			31581,
			31626,
			31588,
			31287,
			31621,
			31646,
			31647,
			31651,
			31654,
			31655,
			31658,
			31287
		};
		
		Items = new int[]
		{
			7080,
			7525,
			7081,
			7513,
			7296,
			7327,
			7358,
			7389,
			7420,
			7451,
			7090,
			0
		};
		
		Mob = new int[]
		{
			27270,
			27247,
			27277
		};
		
		qn = qn1;
		classid = 112;
		prevclass = 0x2b;
		
		X = new int[]
		{
			119518,
			181215,
			181227
		};
		
		Y = new int[]
		{
			-28658,
			36676,
			36703
		};
		
		Z = new int[]
		{
			-3811,
			-4812,
			-4816
		};
		
		registerNPCs();
	}
}