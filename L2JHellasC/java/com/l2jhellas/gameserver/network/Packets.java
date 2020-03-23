package com.l2jhellas.gameserver.network;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.l2jhellas.gameserver.network.L2GameClient.GameClientState;
import com.l2jhellas.gameserver.network.clientpackets.*;
import com.l2jhellas.mmocore.network.ReceivablePacket;

public enum Packets implements IPacket
{
	ProtocolVersion(0x00, new ProtocolVersion(), GameClientState.CONNECTED),
	AuthLogin(0x08, new AuthLogin(), GameClientState.CONNECTED),
	
	Logout(0x09, new Logout(), GameClientState.AUTHED, GameClientState.IN_GAME),
	
	CharacterCreate(0x0b, new CharacterCreate(), GameClientState.AUTHED),
	CharacterDelete(0x0c, new CharacterDelete(), GameClientState.AUTHED),
	CharacterSelected(0x0d, new CharacterSelected(), GameClientState.AUTHED),
	NewCharacter(0x0e, new NewCharacter(), GameClientState.AUTHED),
	CharacterRestore(0x62, new CharacterRestore(), GameClientState.AUTHED),
	
	RequestPledgeCrest(0x68, new RequestPledgeCrest(), GameClientState.AUTHED, GameClientState.IN_GAME),
	MoveBackwardToLocation(0x01, new MoveBackwardToLocation(), GameClientState.IN_GAME),
	EnterWorld(0x03, new EnterWorld(), GameClientState.ENTERING),
	Action(0x04, new Action(), GameClientState.IN_GAME),
	AttackRequest(0x0a, new AttackRequest(), GameClientState.IN_GAME),
	RequestItemList(0x0f, new RequestItemList(), GameClientState.IN_GAME),
	RequestUnEquipItem(0x11, new RequestUnEquipItem(), GameClientState.IN_GAME),
	RequestDropItem(0x12, new RequestDropItem(), GameClientState.IN_GAME),
	UseItem(0x14, new UseItem(), GameClientState.IN_GAME),
	TradeRequest(0x15, new TradeRequest(), GameClientState.IN_GAME),
	AddTradeItem(0x16, new AddTradeItem(), GameClientState.IN_GAME),
	TradeDone(0x17, new TradeDone(), GameClientState.IN_GAME),
	DummyPacket1a(0x1a, new DummyPacket(), GameClientState.IN_GAME),
	RequestSocialAction(0x1b, new RequestSocialAction(), GameClientState.IN_GAME),
	ChangeMoveType2(0x1c, new ChangeMoveType2(), GameClientState.IN_GAME),
	ChangeWaitType2(0x1d, new ChangeWaitType2(), GameClientState.IN_GAME),
	RequestSellItem(0x1e, new RequestSellItem(), GameClientState.IN_GAME),
	RequestBuyItem(0x1f, new RequestBuyItem(), GameClientState.IN_GAME),
	RequestLinkHtml(0x20, new RequestLinkHtml(), GameClientState.IN_GAME),
	RequestBypassToServer(0x21, new RequestBypassToServer(), GameClientState.IN_GAME),
	RequestBBSwrite(0x22, new RequestBBSwrite(), GameClientState.IN_GAME),
	DummyPacket23(0x23, new DummyPacket(), GameClientState.IN_GAME),
	RequestJoinPledge(0x24, new RequestJoinPledge(), GameClientState.IN_GAME),
	RequestAnswerJoinPledge(0x25, new RequestAnswerJoinPledge(), GameClientState.IN_GAME),
	RequestWithdrawalPledge(0x26, new RequestWithdrawalPledge(), GameClientState.IN_GAME),
	RequestOustPledgeMember(0x27, new RequestOustPledgeMember(), GameClientState.IN_GAME),
	RequestJoinParty(0x29, new RequestJoinParty(), GameClientState.IN_GAME),
	RequestAnswerJoinParty(0x2a, new RequestAnswerJoinParty(), GameClientState.IN_GAME),
	RequestWithDrawalParty(0x2b, new RequestWithDrawalParty(), GameClientState.IN_GAME),
	RequestOustPartyMember(0x2c, new RequestOustPartyMember(), GameClientState.IN_GAME),
	DummyPacket(0x2e, new DummyPacket(), GameClientState.IN_GAME),
	RequestMagicSkillUse(0x2f, new RequestMagicSkillUse(), GameClientState.IN_GAME),
	Appearing(0x30, new Appearing(), GameClientState.IN_GAME),
	SendWareHouseDepositList(0x31, new SendWareHouseDepositList(), GameClientState.IN_GAME),
	SendWareHouseWithDrawList(0x32, new SendWareHouseWithDrawList(), GameClientState.IN_GAME),
	RequestShortCutReg(0x33, new RequestShortCutReg(), GameClientState.IN_GAME),
	DummyPacket2(0x34, new DummyPacket(), GameClientState.IN_GAME),
	RequestShortCutDel(0x35, new RequestShortCutDel(), GameClientState.IN_GAME),
	CannotMoveAnymore(0x36, new CannotMoveAnymore(), GameClientState.IN_GAME),
	RequestTargetCanceld(0x37, new RequestTargetCanceld(), GameClientState.IN_GAME),
	Say2(0x38, new Say2(), GameClientState.IN_GAME),
	RequestPledgeMemberList(0x3c, new RequestPledgeMemberList(), GameClientState.IN_GAME),
	DummyPacket3(0x3e, new DummyPacket(), GameClientState.IN_GAME),
	RequestSkillList(0x3f, new RequestSkillList(), GameClientState.IN_GAME),
	RequestGetOnVehicle(0x42, new RequestGetOnVehicle(), GameClientState.IN_GAME),
	RequestGetOffVehicle(0x43, new RequestGetOffVehicle(), GameClientState.IN_GAME),
	AnswerTradeRequest(0x44, new AnswerTradeRequest(), GameClientState.IN_GAME),
	RequestActionUse(0x45, new RequestActionUse(), GameClientState.IN_GAME),
	RequestRestart(0x46, new RequestRestart(), GameClientState.IN_GAME),
	ValidatePosition(0x48, new ValidatePosition(), GameClientState.IN_GAME),
	StartRotating(0x4a, new StartRotating(), GameClientState.IN_GAME),
	FinishRotating(0x4b, new FinishRotating(), GameClientState.IN_GAME),
	RequestStartPledgeWar(0x4d, new RequestStartPledgeWar(), GameClientState.IN_GAME),
	RequestReplyStartPledgeWar(0x4e, new RequestReplyStartPledgeWar(), GameClientState.IN_GAME),
	RequestStopPledgeWar(0x4f, new RequestStopPledgeWar(), GameClientState.IN_GAME),
	RequestReplyStopPledgeWar(0x50, new RequestReplyStopPledgeWar(), GameClientState.IN_GAME),
	RequestSurrenderPledgeWar(0x51, new RequestSurrenderPledgeWar(), GameClientState.IN_GAME),
	RequestReplySurrenderPledgeWar(0x52, new RequestReplySurrenderPledgeWar(), GameClientState.IN_GAME),
	RequestSetPledgeCrest(0x53, new RequestSetPledgeCrest(), GameClientState.IN_GAME),
	RequestGiveNickName(0x55, new RequestGiveNickName(), GameClientState.IN_GAME),
	RequestShowBoard(0x57, new RequestShowBoard(), GameClientState.IN_GAME),
	RequestEnchantItem(0x58, new RequestEnchantItem(), GameClientState.IN_GAME),
	RequestDestroyItem(0x59, new RequestDestroyItem(), GameClientState.IN_GAME),
	SendBypassBuildCmd(0x5b, new SendBypassBuildCmd(), GameClientState.IN_GAME),
	RequestMoveToLocationInVehicle(0x5c, new RequestMoveToLocationInVehicle(), GameClientState.IN_GAME),
	CannotMoveAnymoreInVehicle(0x5d, new CannotMoveAnymoreInVehicle(), GameClientState.IN_GAME),
	RequestFriendInvite(0x5e, new RequestFriendInvite(), GameClientState.IN_GAME),
	RequestAnswerFriendInvite(0x5f, new RequestAnswerFriendInvite(), GameClientState.IN_GAME),
	RequestFriendList(0x60, new RequestFriendList(), GameClientState.IN_GAME),
	RequestFriendDel(0x61, new RequestFriendDel(), GameClientState.IN_GAME),
	RequestQuestList(0x63, new RequestQuestList(), GameClientState.IN_GAME),
	RequestQuestAbort(0x64, new RequestQuestAbort(), GameClientState.IN_GAME),
	RequestPledgeInfo(0x66, new RequestPledgeInfo(), GameClientState.IN_GAME),
	RequestSurrenderPersonally(0x69, new RequestSurrenderPersonally(), GameClientState.IN_GAME),
	RequestAquireSkillInfo(0x6b, new RequestAquireSkillInfo(), GameClientState.IN_GAME),
	RequestAquireSkill(0x6c, new RequestAquireSkill(), GameClientState.IN_GAME),
	RequestRestartPoint(0x6d, new RequestRestartPoint(), GameClientState.IN_GAME),
	RequestGMCommand(0x6e, new RequestGMCommand(), GameClientState.IN_GAME),
	RequestPartyMatchConfig(0x6f, new RequestPartyMatchConfig(), GameClientState.IN_GAME),
	RequestPartyMatchList(0x70, new RequestPartyMatchList(), GameClientState.IN_GAME),
	RequestPartyMatchDetail(0x71, new RequestPartyMatchDetail(), GameClientState.IN_GAME),
	RequestCrystallizeItem(0x72, new RequestCrystallizeItem(), GameClientState.IN_GAME),
	RequestPrivateStoreManageSell(0x73, new RequestPrivateStoreManageSell(), GameClientState.IN_GAME),
	SetPrivateStoreListSell(0x74, new SetPrivateStoreListSell(), GameClientState.IN_GAME),
	RequestPrivateStoreQuitSell(0x76, new RequestPrivateStoreQuitSell(), GameClientState.IN_GAME),
	SetPrivateStoreMsgSell(0x77, new SetPrivateStoreMsgSell(), GameClientState.IN_GAME),
	RequestPrivateStoreBuy(0x79, new RequestPrivateStoreBuy(), GameClientState.IN_GAME),
	RequestTutorialLinkHtml(0x7b, new RequestTutorialLinkHtml(), GameClientState.IN_GAME),
	RequestTutorialPassCmdToServer(0x7c, new RequestTutorialPassCmdToServer(), GameClientState.IN_GAME),
	RequestTutorialQuestionMark(0x7d, new RequestTutorialQuestionMark(), GameClientState.IN_GAME),
	RequestTutorialClientEvent(0x7e, new RequestTutorialClientEvent(), GameClientState.IN_GAME),
	RequestPetition(0x7f, new RequestPetition(), GameClientState.IN_GAME),
	RequestPetitionCancel(0x80, new RequestPetitionCancel(), GameClientState.IN_GAME),
	RequestGmList(0x81, new RequestGmList(), GameClientState.IN_GAME),
	RequestJoinAlly(0x82, new RequestJoinAlly(), GameClientState.IN_GAME),
	RequestAnswerJoinAlly(0x83, new RequestAnswerJoinAlly(), GameClientState.IN_GAME),
	AllyLeave(0x84, new AllyLeave(), GameClientState.IN_GAME),
	AllyDismiss(0x85, new AllyDismiss(), GameClientState.IN_GAME),
	RequestDismissAlly(0x86, new RequestDismissAlly(), GameClientState.IN_GAME),
	RequestSetAllyCrest(0x87, new RequestSetAllyCrest(), GameClientState.IN_GAME),
	RequestAllyCrest(0x88, new RequestAllyCrest(), GameClientState.IN_GAME),
	RequestChangePetName(0x89, new RequestChangePetName(), GameClientState.IN_GAME),
	RequestPetUseItem(0x8a, new RequestPetUseItem(), GameClientState.IN_GAME),
	RequestGiveItemToPet(0x8b, new RequestGiveItemToPet(), GameClientState.IN_GAME),
	RequestGetItemFromPet(0x8c, new RequestGetItemFromPet(), GameClientState.IN_GAME),
	RequestAllyInfo(0x8e, new RequestAllyInfo(), GameClientState.IN_GAME),
	RequestPetGetItem(0x8f, new RequestPetGetItem(), GameClientState.IN_GAME),
	RequestPrivateStoreManageBuy(0x90, new RequestPrivateStoreManageBuy(), GameClientState.IN_GAME),
	SetPrivateStoreListBuy(0x91, new SetPrivateStoreListBuy(), GameClientState.IN_GAME),
	RequestPrivateStoreQuitBuy(0x93, new RequestPrivateStoreQuitBuy(), GameClientState.IN_GAME),
	SetPrivateStoreMsgBuy(0x94, new SetPrivateStoreMsgBuy(), GameClientState.IN_GAME),
	RequestPrivateStoreSell(0x96, new RequestPrivateStoreSell(), GameClientState.IN_GAME),
	RequestPackageSendableItemList(0x9e, new RequestPackageSendableItemList(), GameClientState.IN_GAME),
	RequestPackageSend(0x9f, new RequestPackageSend(), GameClientState.IN_GAME),
	RequestBlock(0xa0, new RequestBlock(), GameClientState.IN_GAME),
	RequestSiegeAttackerList(0xa2, new RequestSiegeAttackerList(), GameClientState.IN_GAME),
	RequestSiegeDefenderList(0xa3, new RequestSiegeDefenderList(), GameClientState.IN_GAME),
	RequestJoinSiege(0xa4, new RequestJoinSiege(), GameClientState.IN_GAME),
	RequestConfirmSiegeWaitingList(0xa5, new RequestConfirmSiegeWaitingList(), GameClientState.IN_GAME),
	MultiSellChoose(0xa7, new MultiSellChoose(), GameClientState.IN_GAME),
	RequestUserCommand(0xaa, new RequestUserCommand(), GameClientState.IN_GAME),
	SnoopQuit(0xab, new SnoopQuit(), GameClientState.IN_GAME),
	RequestRecipeBookOpen(0xac, new RequestRecipeBookOpen(), GameClientState.IN_GAME),
	RequestRecipeBookDestroy(0xad, new RequestRecipeBookDestroy(), GameClientState.IN_GAME),
	RequestRecipeItemMakeInfo(0xae, new RequestRecipeItemMakeInfo(), GameClientState.IN_GAME),
	RequestRecipeItemMakeSelf(0xaf, new RequestRecipeItemMakeSelf(), GameClientState.IN_GAME),
	RequestRecipeShopMessageSet(0xb1, new RequestRecipeShopMessageSet(), GameClientState.IN_GAME),
	RequestRecipeShopListSet(0xb2, new RequestRecipeShopListSet(), GameClientState.IN_GAME),
	RequestRecipeShopManageQuit(0xb3, new RequestRecipeShopManageQuit(), GameClientState.IN_GAME),
	RequestRecipeShopMakeInfo(0xb5, new RequestRecipeShopMakeInfo(), GameClientState.IN_GAME),
	RequestRecipeShopMakeItem(0xb6, new RequestRecipeShopMakeItem(), GameClientState.IN_GAME),
	RequestRecipeShopManagePrev(0xb7, new RequestRecipeShopManagePrev(), GameClientState.IN_GAME),
	ObserverReturn(0xb8, new ObserverReturn(), GameClientState.IN_GAME),
	RequestEvaluate(0xb9, new RequestEvaluate(), GameClientState.IN_GAME),
	RequestHennaList(0xba, new RequestHennaList(), GameClientState.IN_GAME),
	RequestHennaItemInfo(0xbb, new RequestHennaItemInfo(), GameClientState.IN_GAME),
	RequestHennaEquip(0xbc, new RequestHennaEquip(), GameClientState.IN_GAME),
	RequestHennaRemoveList(0xbd, new RequestHennaRemoveList(), GameClientState.IN_GAME),
	RequestHennaItemRemoveInfo(0xbe, new RequestHennaItemRemoveInfo(), GameClientState.IN_GAME),
	RequestHennaRemove(0xbf, new RequestHennaRemove(), GameClientState.IN_GAME),
	RequestPledgePower(0xc0, new RequestPledgePower(), GameClientState.IN_GAME),
	RequestMakeMacro(0xc1, new RequestMakeMacro(), GameClientState.IN_GAME),
	RequestDeleteMacro(0xc2, new RequestDeleteMacro(), GameClientState.IN_GAME),
	RequestBuyProcure(0xc3, new RequestBuyProcure(), GameClientState.IN_GAME),
	RequestBuySeed(0xc4, new RequestBuySeed(), GameClientState.IN_GAME),
	DlgAnswer(0xc5, new DlgAnswer(), GameClientState.IN_GAME),
	RequestSSQStatus(0xc7, new RequestSSQStatus(), GameClientState.IN_GAME),
	GameGuardReply(0xCA, new GameGuardReply(), GameClientState.IN_GAME),
	RequestSendFriendMsg(0xcc, new RequestSendFriendMsg(), GameClientState.IN_GAME),
	RequestShowMiniMap(0xcd, new RequestShowMiniMap(), GameClientState.IN_GAME),
	RequestRecordInfo(0xcf, new RequestRecordInfo(), GameClientState.IN_GAME);
	
	public static final Packets[] PACKET_ARRAY;
	
	static
	{
		final short maxPacketId = (short) Arrays.stream(values()).mapToInt(Packets::getPacketId).max().orElse(0);
		PACKET_ARRAY = new Packets[maxPacketId + 1];
		
		for (Packets incomingPacket : values())
		{
			PACKET_ARRAY[incomingPacket.getPacketId()] = incomingPacket;
		}
	}
	
	private short _packetId;
	private ReceivablePacket<L2GameClient> _incomingPacket;
	private Set<GameClientState> _connectionStates;
	
	Packets(int OpCode, ReceivablePacket<L2GameClient> packet, GameClientState... connectionStates)
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