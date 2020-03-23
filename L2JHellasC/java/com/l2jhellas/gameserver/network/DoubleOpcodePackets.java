package com.l2jhellas.gameserver.network;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.l2jhellas.gameserver.network.L2GameClient.GameClientState;
import com.l2jhellas.gameserver.network.clientpackets.AnswerJoinPartyRoom;
import com.l2jhellas.gameserver.network.clientpackets.RequestAskJoinPartyRoom;
import com.l2jhellas.gameserver.network.clientpackets.RequestAutoSoulShot;
import com.l2jhellas.gameserver.network.clientpackets.RequestChangePartyLeader;
import com.l2jhellas.gameserver.network.clientpackets.RequestConfirmCancelItem;
import com.l2jhellas.gameserver.network.clientpackets.RequestConfirmGemStone;
import com.l2jhellas.gameserver.network.clientpackets.RequestConfirmRefinerItem;
import com.l2jhellas.gameserver.network.clientpackets.RequestConfirmTargetItem;
import com.l2jhellas.gameserver.network.clientpackets.RequestCursedWeaponList;
import com.l2jhellas.gameserver.network.clientpackets.RequestCursedWeaponLocation;
import com.l2jhellas.gameserver.network.clientpackets.RequestDismissPartyRoom;
import com.l2jhellas.gameserver.network.clientpackets.RequestDuelAnswerStart;
import com.l2jhellas.gameserver.network.clientpackets.RequestDuelStart;
import com.l2jhellas.gameserver.network.clientpackets.RequestDuelSurrender;
import com.l2jhellas.gameserver.network.clientpackets.RequestExAcceptJoinMPCC;
import com.l2jhellas.gameserver.network.clientpackets.RequestExAskJoinMPCC;
import com.l2jhellas.gameserver.network.clientpackets.RequestExEnchantSkill;
import com.l2jhellas.gameserver.network.clientpackets.RequestExEnchantSkillInfo;
import com.l2jhellas.gameserver.network.clientpackets.RequestExFishRanking;
import com.l2jhellas.gameserver.network.clientpackets.RequestExMPCCShowPartyMembersInfo;
import com.l2jhellas.gameserver.network.clientpackets.RequestExMagicSkillUseGround;
import com.l2jhellas.gameserver.network.clientpackets.RequestExOustFromMPCC;
import com.l2jhellas.gameserver.network.clientpackets.RequestExPledgeCrestLarge;
import com.l2jhellas.gameserver.network.clientpackets.RequestExSetPledgeCrestLarge;
import com.l2jhellas.gameserver.network.clientpackets.RequestExitPartyMatchingWaitingRoom;
import com.l2jhellas.gameserver.network.clientpackets.RequestGetBossRecord;
import com.l2jhellas.gameserver.network.clientpackets.RequestListPartyMatchingWaitingRoom;
import com.l2jhellas.gameserver.network.clientpackets.RequestManorList;
import com.l2jhellas.gameserver.network.clientpackets.RequestOlympiadMatchList;
import com.l2jhellas.gameserver.network.clientpackets.RequestOlympiadObserverEnd;
import com.l2jhellas.gameserver.network.clientpackets.RequestOustFromPartyRoom;
import com.l2jhellas.gameserver.network.clientpackets.RequestPCCafeCouponUse;
import com.l2jhellas.gameserver.network.clientpackets.RequestPledgeMemberInfo;
import com.l2jhellas.gameserver.network.clientpackets.RequestPledgeMemberPowerInfo;
import com.l2jhellas.gameserver.network.clientpackets.RequestPledgePowerGradeList;
import com.l2jhellas.gameserver.network.clientpackets.RequestPledgeReorganizeMember;
import com.l2jhellas.gameserver.network.clientpackets.RequestPledgeSetAcademyMaster;
import com.l2jhellas.gameserver.network.clientpackets.RequestPledgeSetMemberPowerGrade;
import com.l2jhellas.gameserver.network.clientpackets.RequestPledgeWarList;
import com.l2jhellas.gameserver.network.clientpackets.RequestProcureCropList;
import com.l2jhellas.gameserver.network.clientpackets.RequestRefine;
import com.l2jhellas.gameserver.network.clientpackets.RequestRefineCancel;
import com.l2jhellas.gameserver.network.clientpackets.RequestSetCrop;
import com.l2jhellas.gameserver.network.clientpackets.RequestSetSeed;
import com.l2jhellas.gameserver.network.clientpackets.RequestWithdrawPartyRoom;
import com.l2jhellas.gameserver.network.clientpackets.RequestWriteHeroWords;
import com.l2jhellas.mmocore.network.ReceivablePacket;

public enum DoubleOpcodePackets implements IPacket
{
	RequestOustFromPartyRoom(1, new RequestOustFromPartyRoom(), GameClientState.IN_GAME),
	RequestDismissPartyRoom(2, new RequestDismissPartyRoom(), GameClientState.IN_GAME),
	RequestWithdrawPartyRoom(3, new RequestWithdrawPartyRoom(), GameClientState.IN_GAME),
	RequestChangePartyLeader(4, new RequestChangePartyLeader(), GameClientState.IN_GAME),
	RequestAutoSoulShot(5, new RequestAutoSoulShot(), GameClientState.IN_GAME),
	RequestExEnchantSkillInfo(6, new RequestExEnchantSkillInfo(), GameClientState.IN_GAME),
	RequestExEnchantSkill(7, new RequestExEnchantSkill(), GameClientState.IN_GAME),
	RequestManorList(8, new RequestManorList(), GameClientState.ENTERING),
	RequestProcureCropList(9, new RequestProcureCropList(), GameClientState.IN_GAME),
	RequestSetSeed(0x0a, new RequestSetSeed(), GameClientState.IN_GAME),
	RequestSetCrop(0x0b, new RequestSetCrop(), GameClientState.IN_GAME),
	RequestWriteHeroWords(0x0c, new RequestWriteHeroWords(), GameClientState.IN_GAME),
	RequestExAskJoinMPCC(0x0d, new RequestExAskJoinMPCC(), GameClientState.IN_GAME),
	RequestExAcceptJoinMPCC(0x0e, new RequestExAcceptJoinMPCC(), GameClientState.IN_GAME),
	RequestExOustFromMPCC(0x0f, new RequestExOustFromMPCC(), GameClientState.IN_GAME),
	RequestExPledgeCrestLarge(0x10, new RequestExPledgeCrestLarge(), GameClientState.IN_GAME),
	RequestExSetPledgeCrestLarge(0x11, new RequestExSetPledgeCrestLarge(), GameClientState.IN_GAME),
	RequestOlympiadObserverEnd(0x12, new RequestOlympiadObserverEnd(), GameClientState.IN_GAME),
	RequestOlympiadMatchList(0x13, new RequestOlympiadMatchList(), GameClientState.IN_GAME),
	RequestAskJoinPartyRoom(0x14, new RequestAskJoinPartyRoom(), GameClientState.IN_GAME),
	AnswerJoinPartyRoom(0x15, new AnswerJoinPartyRoom(), GameClientState.IN_GAME),
	RequestListPartyMatchingWaitingRoom(0x16, new RequestListPartyMatchingWaitingRoom(), GameClientState.IN_GAME),
	RequestExitPartyMatchingWaitingRoom(0x17, new RequestExitPartyMatchingWaitingRoom(), GameClientState.IN_GAME),
	RequestGetBossRecord(0x18, new RequestGetBossRecord(), GameClientState.IN_GAME),
	RequestPledgeSetAcademyMaster(0x19, new RequestPledgeSetAcademyMaster(), GameClientState.IN_GAME),
	RequestPledgePowerGradeList(0x1a, new RequestPledgePowerGradeList(), GameClientState.IN_GAME),
	RequestPledgeMemberPowerInfo(0x1b, new RequestPledgeMemberPowerInfo(), GameClientState.IN_GAME),
	RequestPledgeSetMemberPowerGrade(0x1c, new RequestPledgeSetMemberPowerGrade(), GameClientState.IN_GAME),
	RequestPledgeMemberInfo(0x1d, new RequestPledgeMemberInfo(), GameClientState.IN_GAME),
	RequestPledgeWarList(0x1e, new RequestPledgeWarList(), GameClientState.IN_GAME),
	RequestExFishRanking(0x1f, new RequestExFishRanking(), GameClientState.IN_GAME),
	RequestPCCafeCouponUse(0x20, new RequestPCCafeCouponUse(), GameClientState.IN_GAME),
	RequestCursedWeaponList(0x22, new RequestCursedWeaponList(), GameClientState.IN_GAME),
	RequestCursedWeaponLocation(0x23, new RequestCursedWeaponLocation(), GameClientState.IN_GAME),
	RequestPledgeReorganizeMember(0x24, new RequestPledgeReorganizeMember(), GameClientState.IN_GAME),
	RequestExMPCCShowPartyMembersInfo(0x26, new RequestExMPCCShowPartyMembersInfo(), GameClientState.IN_GAME),
	RequestDuelStart(0x27, new RequestDuelStart(), GameClientState.IN_GAME),
	RequestDuelAnswerStart(0x28, new RequestDuelAnswerStart(), GameClientState.IN_GAME),
	RequestConfirmTargetItem(0x29, new RequestConfirmTargetItem(), GameClientState.IN_GAME),
	RequestConfirmRefinerItem(0x2a, new RequestConfirmRefinerItem(), GameClientState.IN_GAME),
	RequestConfirmGemStone(0x2b, new RequestConfirmGemStone(), GameClientState.IN_GAME),
	RequestRefine(0x2c, new RequestRefine(), GameClientState.IN_GAME),
	RequestConfirmCancelItem(0x2d, new RequestConfirmCancelItem(), GameClientState.IN_GAME),
	RequestRefineCancel(0x2e, new RequestRefineCancel(), GameClientState.IN_GAME),
	RequestExMagicSkillUseGround(0x2f, new RequestExMagicSkillUseGround(), GameClientState.IN_GAME),
	RequestDuelSurrender(0x30, new RequestDuelSurrender(), GameClientState.IN_GAME);
	
	public static final DoubleOpcodePackets[] PACKET_ARRAY;
	
	static
	{
		final short maxPacketId = (short) Arrays.stream(values()).mapToInt(DoubleOpcodePackets::getPacketId).max().orElse(0);
		PACKET_ARRAY = new DoubleOpcodePackets[maxPacketId + 1];
		
		for (DoubleOpcodePackets incomingPacket : values())
		{
			PACKET_ARRAY[incomingPacket.getPacketId()] = incomingPacket;
		}
	}
	
	private short _packetId;
	private ReceivablePacket<L2GameClient> _incomingPacket;
	private Set<GameClientState> _connectionStates;
	
	DoubleOpcodePackets(int OpCode, ReceivablePacket<L2GameClient> packet, GameClientState... connectionStates)
	{
		_packetId = (short) OpCode;
		_incomingPacket = packet;
		_connectionStates = new HashSet<>(Arrays.asList(connectionStates));
	}
	
	public boolean isInRightState(GameClientState state)
	{
		for (GameClientState _state : _connectionStates)
		{
			if (_state.equals(state))
				return true;
		}
		
		return false;
	}
	
	@Override
	public ReceivablePacket<L2GameClient> getPacket()
	{
		return _incomingPacket;
	}
	
	@Override
	public int getPacketId()
	{
		return _packetId;
	}
	
	@Override
	public Set<GameClientState> getState()
	{
		return _connectionStates;
	}
}