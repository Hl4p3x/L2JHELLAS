package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;

public final class DlgAnswer extends L2GameClientPacket
{
	private static final String _C__C5_DLGANSWER = "[C] C5 DlgAnswer";
	
	private int _messageId;
	private int _answer;
	private int _reqId;
	
	@Override
	protected void readImpl()
	{
		_messageId = readD();
		_answer = readD();
		_reqId = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player != null)
		{
			switch (_messageId)
			{
			    // RESSURECTION
				case 1510:
				case 332:
					player.reviveAnswer(_answer);
					break;
				// Summon tp request
				case 1842:
					player.teleportAnswer(_answer, _reqId);
					break;
				// OPEN_GATE
				case 1140:
					player.gatesAnswer(_answer, 1);
					break;
				// CLOSE_GATE
				case 1141:
					player.gatesAnswer(_answer, 0);
					break;
				// WEDDING
				case 1983:
				case 614:
					if (player.awaitingAnswer && Config.MOD_ALLOW_WEDDING)
					{
						player.EngageAnswer(_answer);
						player.awaitingAnswer = false;
					}
					break;
				default:
					player.sendPacket(ActionFailed.STATIC_PACKET);
					break;
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__C5_DLGANSWER;
	}
}