package com.l2jhellas.gameserver.scrips.quests.ai.custom;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.gameserver.scrips.quests.ai.vilagemaster.FirstClassChange;

public class ShadowWeapon extends Quest
{
	private static final String qn = "ShadowWeapon";
	
	// itemId for shadow weapon coupons, it's not used more than once but increases readability
	private static final int D_COUPON = 8869;
	private static final int C_COUPON = 8870;
	
	// TODO: That list will be moved into SecondClassChange once it's made.
	public static final int[] SECONDCLASSNPCS =
	{
		30109,
		30115,
		30120,
		30174,
		30176,
		30187,
		30191,
		30195,
		30474,
		30511,
		30512,
		30513,
		30676,
		30677,
		30681,
		30685,
		30687,
		30689,
		30694,
		30699,
		30704,
		30845,
		30847,
		30849,
		30854,
		30857,
		30862,
		30865,
		30894,
		30897,
		30900,
		30905,
		30910,
		30913,
		31269,
		31272,
		31276,
		31279,
		31285,
		31288,
		31314,
		31317,
		31321,
		31324,
		31326,
		31328,
		31331,
		31334,
		31336,
		31755,
		31958,
		31961,
		31965,
		31968,
		31974,
		31977,
		31996,
		32094,
		32095,
		32096
	};
	
	public ShadowWeapon()
	{
		super(-1, qn, "custom");
		
		addStartNpc(FirstClassChange.FIRSTCLASSNPCS);
		addTalkId(FirstClassChange.FIRSTCLASSNPCS);
		
		addStartNpc(SECONDCLASSNPCS);
		addTalkId(SECONDCLASSNPCS);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		boolean hasD = st.hasQuestItems(D_COUPON);
		boolean hasC = st.hasQuestItems(C_COUPON);
		
		if (hasD || hasC)
		{
			// let's assume character had both c & d-grade coupons, we'll confirm later
			String multisell = "306893003";
			if (!hasD) // if s/he had c-grade only...
				multisell = "306893002";
			else if (!hasC) // or d-grade only.
				multisell = "306893001";
			
			// finally, return htm with proper multisell value in it.
			htmltext = getHtmlText("exchange.htm").replace("%msid%", multisell);
		}
		else
			htmltext = "exchange-no.htm";
		
		st.exitQuest(true);
		return htmltext;
	}
	
	public static void main(String args[])
	{
		new ShadowWeapon();
	}
}