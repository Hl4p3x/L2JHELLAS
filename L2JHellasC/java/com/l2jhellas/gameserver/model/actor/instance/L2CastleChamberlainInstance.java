package com.l2jhellas.gameserver.model.actor.instance;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.SevenSigns;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.controllers.TradeController;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.CastleManorManager;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2Effect.EffectType;
import com.l2jhellas.gameserver.model.L2TradeList;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.PcInventory;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.BuyList;
import com.l2jhellas.gameserver.network.serverpackets.ExShowCropInfo;
import com.l2jhellas.gameserver.network.serverpackets.ExShowCropSetting;
import com.l2jhellas.gameserver.network.serverpackets.ExShowManorDefaultInfo;
import com.l2jhellas.gameserver.network.serverpackets.ExShowSeedInfo;
import com.l2jhellas.gameserver.network.serverpackets.ExShowSeedSetting;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Util;

public class L2CastleChamberlainInstance extends L2NpcInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;
	
	public L2CastleChamberlainInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;
		
		player.setLastFolkNPC(this);
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showMessageWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// BypassValidation Exploit plug.
		if (player.getLastFolkNPC().getObjectId() != getObjectId())
			return;
		
		int condition = validateCondition(player);
		if (condition <= COND_ALL_FALSE)
			return;
		
		if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			return;
		else if (condition == COND_OWNER)
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String actualCommand = st.nextToken(); // Get actual command
			
			String val = "";
			if (st.countTokens() >= 1)
			{
				val = st.nextToken();
			}
			
			if (actualCommand.equalsIgnoreCase("banish_foreigner"))
			{
				getCastle().banishForeigners(); // Move non-clan members off castle area
				return;
			}
			else if (actualCommand.equalsIgnoreCase("list_siege_clans"))
			{
				getCastle().getSiege().listRegisterClan(player); // List current register clan
				return;
			}
			else if (actualCommand.equalsIgnoreCase("receive_report"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/chamberlain/chamberlain-report.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
				html.replace("%clanname%", clan.getName());
				html.replace("%clanleadername%", clan.getLeaderName());
				html.replace("%castlename%", getCastle().getName());
				
				int currentPeriod = SevenSigns.getInstance().getCurrentPeriod();
				switch (currentPeriod)
				{
					case SevenSigns.PERIOD_COMP_RECRUITING:
						html.replace("%ss_event%", "Quest Event Initialization");
						break;
					case SevenSigns.PERIOD_COMPETITION:
						html.replace("%ss_event%", "Competition (Quest Event)");
						break;
					case SevenSigns.PERIOD_COMP_RESULTS:
						html.replace("%ss_event%", "Quest Event Results");
						break;
					case SevenSigns.PERIOD_SEAL_VALIDATION:
						html.replace("%ss_event%", "Seal Validation");
						break;
				}
				int sealOwner1 = SevenSigns.getInstance().getSealOwner(1);
				switch (sealOwner1)
				{
					case SevenSigns.CABAL_NULL:
						html.replace("%ss_avarice%", "Not in Possession");
						break;
					case SevenSigns.CABAL_DAWN:
						html.replace("%ss_avarice%", "Lords of Dawn");
						break;
					case SevenSigns.CABAL_DUSK:
						html.replace("%ss_avarice%", "Revolutionaries of Dusk");
						break;
				}
				int sealOwner2 = SevenSigns.getInstance().getSealOwner(2);
				switch (sealOwner2)
				{
					case SevenSigns.CABAL_NULL:
						html.replace("%ss_gnosis%", "Not in Possession");
						break;
					case SevenSigns.CABAL_DAWN:
						html.replace("%ss_gnosis%", "Lords of Dawn");
						break;
					case SevenSigns.CABAL_DUSK:
						html.replace("%ss_gnosis%", "Revolutionaries of Dusk");
						break;
				}
				int sealOwner3 = SevenSigns.getInstance().getSealOwner(3);
				switch (sealOwner3)
				{
					case SevenSigns.CABAL_NULL:
						html.replace("%ss_strife%", "Not in Possession");
						break;
					case SevenSigns.CABAL_DAWN:
						html.replace("%ss_strife%", "Lords of Dawn");
						break;
					case SevenSigns.CABAL_DUSK:
						html.replace("%ss_strife%", "Revolutionaries of Dusk");
						break;
				}
				
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("items"))
			{
				if (val == "")
					return;
				player.tempInvetoryDisable();
				
				if (Config.DEBUG)
					_log.fine("Showing chamberlain buylist");
				
				int buy;
				{
					int castleId = getCastle().getCastleId();
					int circlet = CastleManager.getInstance().getCircletByCastleId(castleId);
					PcInventory s = player.getInventory();
					if (s.getItemByItemId(circlet) == null)
					{
						buy = (Integer.parseInt(val + "1"));
					}
					else
					{
						buy = (Integer.parseInt(val + "2"));
					}
				}
				L2TradeList list = TradeController.getInstance().getBuyList(buy);
				if (list != null && list.getNpcId().equals(String.valueOf(getNpcId())))
				{
					BuyList bl = new BuyList(list, player.getAdena(), 0);
					player.sendPacket(bl);
				}
				else
				{
					_log.warning(L2CastleChamberlainInstance.class.getName() + ": player: " + player.getName() + " attempting to buy from chamberlain that don't have buylist!");
					_log.warning(L2CastleChamberlainInstance.class.getName() + ": buylist id:" + buy);
				}
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (actualCommand.equalsIgnoreCase("manage_siege_defender"))
			{
				getCastle().getSiege().listRegisterClan(player);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("manage_vault"))
			{
				String filename = "data/html/chamberlain/chamberlain-vault.htm";
				int amount = 0;
				if (val.equalsIgnoreCase("deposit"))
				{
					try
					{
						amount = Integer.parseInt(st.nextToken());
					}
					catch (NoSuchElementException e)
					{
					}
					if (amount > 0 && (long) getCastle().getTreasury() + amount < Integer.MAX_VALUE)
					{
						if (player.reduceAdena("Castle", amount, this, true))
						{
							getCastle().addToTreasuryNoTax(amount);
						}
						else
						{
							sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
						}
					}
				}
				else if (val.equalsIgnoreCase("withdraw"))
				{
					try
					{
						amount = Integer.parseInt(st.nextToken());
					}
					catch (NoSuchElementException e)
					{
					}
					if (amount > 0)
					{
						if (getCastle().getTreasury() < amount)
						{
							filename = "data/html/chamberlain/chamberlain-vault-no.htm";
						}
						else
						{
							if (getCastle().addToTreasuryNoTax((-1) * amount))
								player.addAdena("Castle", amount, this, true);
							
						}
					}
				}
				
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcId%", String.valueOf(getNpcId()));
				html.replace("%npcname%", getName());
				html.replace("%tax_income%", Util.formatAdena(getCastle().getTreasury()));
				html.replace("%withdraw_amount%", Util.formatAdena(amount));
				player.sendPacket(html);
				
				return;
			}
			else if (actualCommand.equalsIgnoreCase("Clan_Gate"))
			{
				L2PcInstance leader = L2World.getInstance().getPlayer(player.getClan().getLeaderId());
				if (leader == null)
				{
					player.sendMessage("Your Leader is not online.");
				}
				
				else if (leader.atEvent || leader.isinZodiac || leader.isInFunEvent())
				{
					player.sendMessage("Your leader is in an event.");
					return;
				}
				
				else if (leader.isInJail())
				{
					player.sendMessage("Your leader is in Jail.");
					return;
				}
				else if (player.isClanLeader())
				{
					player.sendMessage("Your are the leader.");
					return;
				}
				
				else if (leader.isInOlympiadMode())
				{
					player.sendMessage("Your leader is in the Olympiad now.");
					return;
				}
				
				else if (leader.inObserverMode())
				{
					player.sendMessage("Your leader is in Observ Mode.");
					return;
				}
				
				else if (leader.isInDuel())
				{
					player.sendMessage("Your leader is in a duel.");
					return;
				}
				
				else if (leader.isFestivalParticipant())
				{
					player.sendMessage("Your leader is in a festival.");
					
					return;
				}
				
				else if (leader.isInParty() && leader.getParty().isInDimensionalRift())
				{
					player.sendMessage("Your leader is in dimensional rift.");
					return;
				}
				
				if (player.getClan() != null)
				{
					if (leader == null)
						return;
					
					if (leader.getFirstEffect(EffectType.CLAN_GATE) != null)
					{
						if (!validateGateCondition(leader, player))
							return;
						
						player.teleToLocation(leader.getX(), leader.getY(), leader.getZ());
						return;
					}
					String filename = "data/html/chamberlain/chamberlain-nogate.htm";
					showChatWindow(player, filename);
				}
				else
					player.sendMessage("Your leader is unavailable.");
			}
			else if (actualCommand.equalsIgnoreCase("manor"))
			{
				String filename = "";
				if (CastleManorManager.getInstance().isDisabled())
				{
					filename = "data/html/npcdefault.htm";
				}
				else
				{
					int cmd = Integer.parseInt(val);
					switch (cmd)
					{
						case 0:
							filename = "data/html/chamberlain/manor/manor.htm";
							break;
						// TODO: correct in html's to 1
						case 4:
							filename = "data/html/chamberlain/manor/manor_help00" + st.nextToken() + ".htm";
							break;
						default:
							filename = "data/html/chamberlain/chamberlain-no.htm";
							break;
					}
				}
				
				if (filename.length() != 0)
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(filename);
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", getName());
					player.sendPacket(html);
				}
				return;
			}
			else if (command.startsWith("manor_menu_select"))
			{// input string format:
				// manor_menu_select?ask=X&state=Y&time=X
				if (CastleManorManager.getInstance().isUnderMaintenance())
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					player.sendPacket(SystemMessageId.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
					return;
				}
				
				String params = command.substring(command.indexOf("?") + 1);
				StringTokenizer str = new StringTokenizer(params, "&");
				int ask = Integer.parseInt(str.nextToken().split("=")[1]);
				int state = Integer.parseInt(str.nextToken().split("=")[1]);
				int time = Integer.parseInt(str.nextToken().split("=")[1]);
				
				int castleId;
				if (state == -1) // info for current manor
					castleId = getCastle().getCastleId();
				else
					// info for requested manor
					castleId = state;
				
				switch (ask)
				{ // Main action
					case 3: // Current seeds (Manor info)
						if (time == 1 && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
							player.sendPacket(new ExShowSeedInfo(castleId, null));
						else
							player.sendPacket(new ExShowSeedInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getSeedProduction(time)));
						break;
					case 4: // Current crops (Manor info)
						if (time == 1 && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
							player.sendPacket(new ExShowCropInfo(castleId, null));
						else
							player.sendPacket(new ExShowCropInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getCropProcure(time)));
						break;
					case 5: // Basic info (Manor info)
						player.sendPacket(new ExShowManorDefaultInfo());
						break;
					case 7: // Edit seed setup
						if (getCastle().isNextPeriodApproved())
						{
							player.sendPacket(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM);
						}
						else
						{
							player.sendPacket(new ExShowSeedSetting(getCastle().getCastleId()));
						}
						break;
					case 8: // Edit crop setup
						if (getCastle().isNextPeriodApproved())
						{
							player.sendPacket(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM);
						}
						else
						{
							player.sendPacket(new ExShowCropSetting(getCastle().getCastleId()));
						}
						break;
				}
			}
			else if (actualCommand.equalsIgnoreCase("operate_door")) // door control
			{
				if (val != "")
				{
					boolean open = (Integer.parseInt(val) == 1);
					while (st.hasMoreTokens())
					{
						getCastle().openCloseDoor(player, Integer.parseInt(st.nextToken()), open);
					}
				}
				
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/chamberlain/" + getTemplate().npcId + "-d.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("tax_set")) // tax rates control
			{
				if (val != "")
					getCastle().setTaxPercent(player, Integer.parseInt(val));
				
				StringBuilder msg = new StringBuilder("<html><body>");
				msg.append(getName() + ":<br>");
				msg.append("Current tax rate: " + getCastle().getTaxPercent() + "%<br>");
				msg.append("<table>");
				msg.append("<tr>");
				msg.append("<td>Change tax rate to:</td>");
				msg.append("<td><edit var=\"value\" width=40><br>");
				msg.append("<button value=\"Adjust\" action=\"bypass -h npc_%objectId%_tax_set $value\" width=80 height=15></td>");
				msg.append("</tr>");
				msg.append("</table>");
				msg.append("</center>");
				msg.append("</body></html>");
				
				sendHtmlMessage(player, msg.toString());
				return;
			}
		}
		
		super.onBypassFeedback(player, command);
	}
	
	private void sendHtmlMessage(L2PcInstance player, String htmlMessage)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setHtml(htmlMessage);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	private void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/chamberlain/chamberlain-no.htm";
		
		int condition = validateCondition(player);
		if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
				filename = "data/html/chamberlain/chamberlain-busy.htm"; // Busy because of siege
			else if (condition == COND_OWNER) // Clan owns castle
				filename = "data/html/chamberlain/chamberlain.htm"; // Owner message window
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	protected int validateCondition(L2PcInstance player)
	{
		if (getCastle() != null && player.getClan() != null)
		{
			if (getCastle().getSiege().getIsInProgress())
				return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
				
			if (getCastle().getOwnerId() == player.getClanId()) // Clan owns castle
				return COND_OWNER; // Owner
		}
		
		return COND_ALL_FALSE;
	}
	
	private static final boolean validateGateCondition(L2PcInstance clanLeader, L2PcInstance player)
	{
		if (clanLeader.isAlikeDead() || clanLeader.isInStoreMode() || clanLeader.isRooted() || clanLeader.isInCombat() || clanLeader.isInOlympiadMode() || clanLeader.isFestivalParticipant() || clanLeader.inObserverMode() || clanLeader.isInsideZone(ZoneId.NO_SUMMON_FRIEND))
		{
			player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
			return false;
		}
		
		if (player.isIn7sDungeon())
		{
			final int targetCabal = SevenSigns.getInstance().getPlayerCabal(clanLeader);
			if (SevenSigns.getInstance().isSealValidationPeriod())
			{
				if (targetCabal != SevenSigns.getInstance().getCabalHighestScore())
				{
					player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
					return false;
				}
			}
			else
			{
				if (targetCabal == SevenSigns.CABAL_NULL)
				{
					player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.");
					return false;
				}
			}
		}
		
		return true;
	}
}