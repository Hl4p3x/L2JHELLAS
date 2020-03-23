package com.l2jhellas.gameserver.scrips.quests;

public class SagaOfTheAdventurer extends SagasSuperClass
{
	public static String qn1 = "Q079_SagaOfTheAdventurer";
	public static int qnu = 79;
	public static String qna = "Saga of the Adventurer";
	
	public SagaOfTheAdventurer()
	{
		super(qnu, qn1, qna);
		
		NPC = new int[]
		{
			31603,
			31584,
			31579,
			31615,
			31619,
			31646,
			31647,
			31651,
			31654,
			31655,
			31658,
			31616
		};
		
		Items = new int[]
		{
			7080,
			7516,
			7081,
			7494,
			7277,
			7308,
			7339,
			7370,
			7401,
			7432,
			7102,
			0
		};
		
		Mob = new int[]
		{
			27299,
			27228,
			27302
		};
		
		qn = qn1;
		classid = 93;
		prevclass = 0x08;
		
		X = new int[]
		{
			119518,
			181205,
			181215
		};
		
		Y = new int[]
		{
			-28658,
			36676,
			36676
		};
		
		Z = new int[]
		{
			-3811,
			-4816,
			-4812
		};
		
		registerNPCs();
	}
}