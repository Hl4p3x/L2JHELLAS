package com.l2jhellas.gameserver.model.actor.instance;

import java.util.Map;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.PcFreight;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.EnchantResult;
import com.l2jhellas.gameserver.network.serverpackets.PackageToList;
import com.l2jhellas.gameserver.network.serverpackets.WareHouseDepositList;
import com.l2jhellas.gameserver.network.serverpackets.WareHouseWithdrawalList;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public final class L2WarehouseInstance extends L2NpcInstance
{
	
	public L2WarehouseInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		
		return "data/html/warehouse/" + pom + ".htm";
	}
	
	private static void showRetrieveWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		
		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			return;
		}
		
		player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE));
	}
	
	private static void showDepositWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		player.tempInvetoryDisable();
		player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.PRIVATE));
	}
	
	private static void showDepositWindowClan(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		if (player.getClan() != null)
		{
			if (player.getClan().getLevel() == 0)
				player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
			else
			{
				player.setActiveWarehouse(player.getClan().getWarehouse());
				player.tempInvetoryDisable();
				WareHouseDepositList dl = new WareHouseDepositList(player, WareHouseDepositList.CLAN);
				player.sendPacket(dl);
			}
		}
	}
	
	private static void showWithdrawWindowClan(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
			return;
		}
		if (player.getClan().getLevel() == 0)
			player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
		else
		{
			player.setActiveWarehouse(player.getClan().getWarehouse());
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN));
		}
	}
	
	private void showWithdrawWindowFreight(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		PcFreight freight = player.getFreight();
		
		if (freight != null)
			if (freight.getSize() > 0)
			{
				if (Config.ALT_GAME_FREIGHTS)
					
					freight.setActiveLocation(0);
				else
					freight.setActiveLocation(getWorldRegion().hashCode());
				
				player.setActiveWarehouse(freight);
				player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.FREIGHT));
			}
			else
				player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
		
	}
	
	private static void showDepositWindowFreight(L2PcInstance player)
	{
		// No other chars in the account of this player
		if (player.getAccountChars().size() == 0)
			player.sendPacket(SystemMessageId.CHARACTER_DOES_NOT_EXIST);
		// One or more chars other than this player for this account
		else
		{
			Map<Integer, String> chars = player.getAccountChars();
			if (chars.size() < 1)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			player.sendPacket(new PackageToList(chars));
		}
	}
	
	private void showDepositWindowFreight(L2PcInstance player, int obj_Id)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		L2PcInstance destChar = L2PcInstance.load(obj_Id);
		if (destChar == null)
		{
			return;
		}
		
		PcFreight freight = destChar.getFreight();
		if (Config.ALT_GAME_FREIGHTS)
		{
			freight.setActiveLocation(0);
		}
		else
		{
			freight.setActiveLocation(getWorldRegion().hashCode());
		}
		player.setActiveWarehouse(freight);
		player.tempInvetoryDisable();
		destChar.deleteMe();
		
		player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.FREIGHT));
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player.isProcessingTransaction())
		{
			player.sendPacket(SystemMessageId.ALREADY_TRADING);
			return;
		}
		
		if (player.getActiveEnchantItem() != null)
		{
			player.setActiveEnchantItem(null);
			player.sendPacket(EnchantResult.CANCELLED);
			player.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
		}
		
		if (command.startsWith("WithdrawP"))
		{
			showRetrieveWindow(player);
		}
		else if (command.equals("DepositP"))
		{
			showDepositWindow(player);
		}
		else if (command.equals("WithdrawC"))
		{
			showWithdrawWindowClan(player);
		}
		else if (command.equals("DepositC"))
		{
			showDepositWindowClan(player);
		}
		else if (command.startsWith("WithdrawF"))
		{
			if (Config.ALLOW_FREIGHT)
				showWithdrawWindowFreight(player);
		}
		else if (command.startsWith("DepositF"))
		{
			if (Config.ALLOW_FREIGHT)
				showDepositWindowFreight(player);
		}
		else if (command.startsWith("FreightChar"))
		{
			if (Config.ALLOW_FREIGHT)
			{
				int startOfId = command.lastIndexOf("_") + 1;
				String id = command.substring(startOfId);
				showDepositWindowFreight(player, Integer.parseInt(id));
			}
		}
		else
		{
			// this class don't know any other commands, let forward the command to the parent class
			super.onBypassFeedback(player, command);
		}
	}
}