package Extensions.Balancer;

import com.PackRoot;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;

public class Balancer implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_balance"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_balance") && activeChar.isGM())
		{
			NpcHtmlMessage htm = new NpcHtmlMessage(0);
			htm.setFile(PackRoot.DATAPACK_ROOT + "/data/html/admin/balance/main.htm");
			activeChar.sendPacket(htm);
			return true;
		}
		return false;
	}
	
	public static void sendBalanceWindow(int classId, L2PcInstance p)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(0);
		htm.setFile("./data/html/admin/balance/balance.htm");
		
		htm.replace("%classId%", classId + "");
		htm.replace("%Patk%", BalanceLoad.loadPAtk(classId) + "");
		htm.replace("%Matk%", BalanceLoad.loadMAtk(classId) + "");
		htm.replace("%Pdef%", BalanceLoad.loadPDef(classId) + "");
		htm.replace("%Mdef%", BalanceLoad.loadMDef(classId) + "");
		htm.replace("%Acc%", BalanceLoad.loadAccuracy(classId) + "");
		htm.replace("%Eva%", BalanceLoad.loadEvasion(classId) + "");
		htm.replace("%AtkSp%", BalanceLoad.loadPAtkSpd(classId) + "");
		htm.replace("%CastSp%", BalanceLoad.loadMAtkSpd(classId) + "");
		htm.replace("%Cp%", BalanceLoad.loadCP(classId) + "");
		htm.replace("%Hp%", BalanceLoad.loadHP(classId) + "");
		htm.replace("%Mp%", BalanceLoad.loadMP(classId) + "");
		htm.replace("%Speed%", BalanceLoad.loadSpeed(classId) + "");
		htm.replace("%Critical%", BalanceLoad.loadCritical(classId) + "");
		htm.replace("%MagicCritical%", BalanceLoad.loadMagicCritical(classId) + "");
		htm.replace("%WalkSpeed%", BalanceLoad.loadWalkSpeed(classId) + "");
		htm.replace("%MAtkRange%", BalanceLoad.loadMAtkRange(classId) + "");
		htm.replace("%MReuseRate%", BalanceLoad.loadMReuseRate(classId) + "");
		htm.replace("%PReuseRate%", BalanceLoad.loadPReuseRate(classId) + "");
		htm.replace("%INT%", BalanceLoad.loadINT(classId) + "");
		htm.replace("%WIT%", BalanceLoad.loadWIT(classId) + "");
		htm.replace("%MEN%", BalanceLoad.loadMEN(classId) + "");
		htm.replace("%CON%", BalanceLoad.loadCON(classId) + "");
		htm.replace("%STR%", BalanceLoad.loadSTR(classId) + "");
		htm.replace("%DEX%", BalanceLoad.loadDEX(classId) + "");
		
		p.sendPacket(htm);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}