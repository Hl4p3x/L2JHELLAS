package Extensions.fake.roboto.admincommands;

import Extensions.fake.roboto.FakePlayer;
import Extensions.fake.roboto.FakePlayerManager;
import Extensions.fake.roboto.FakePlayerTaskManager;

import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;

public class AdminFakePlayers implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_takecontrol",
		"admin_releasecontrol",
		"admin_fakes",
		"admin_deletefake",
		"admin_deleteallfake",
		"admin_spawnrandom",		
		"admin_spawnenchanter",
		"admin_spawnfallback",
		"admin_spawnwalker"
	};
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void showFakeDashboard(L2PcInstance activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/fakeplayers/index.htm");
		html.replace("%fakecount%", FakePlayerManager.getFakePlayersCount());
		html.replace("%taskcount%", FakePlayerTaskManager.INSTANCE.getTaskCount());
		activeChar.sendPacket(html);
	}
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_fakes"))
		{
			showFakeDashboard(activeChar);
			return true;
		}
		
		if (command.startsWith("admin_deletefake"))
		{
			if (activeChar.getTarget() != null && activeChar.getTarget() instanceof FakePlayer)
			{
				FakePlayer fakePlayer = (FakePlayer) activeChar.getTarget();
				fakePlayer.despawnPlayer();
			}
			return true;
		}
		
		if (command.startsWith("admin_deleteallfake"))
		{
			if (FakePlayerManager.getFakePlayersCount() > 0)
				FakePlayerManager.deleteAllFakes();
			return true;
		}
		
		if (command.startsWith("admin_spawnwalker"))
		{
			FakePlayer fakePlayer = FakePlayerManager.spawnPlayer(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			fakePlayer.assignCustomAI(1);
			return true;
		}
					
		if (command.startsWith("admin_spawnfallback"))
		{
			FakePlayer fakePlayer = FakePlayerManager.spawnPlayer(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			fakePlayer.assignCustomAI(0);
			return true;
		}
		
		if (command.startsWith("admin_spawnenchanter"))
		{
			FakePlayer fakePlayer = FakePlayerManager.spawnPlayer(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			fakePlayer.assignCustomAI(2);
			return true;
		}
		
		if (command.startsWith("admin_spawnrandom"))
		{
			FakePlayer fakePlayer = FakePlayerManager.spawnPlayer(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			fakePlayer.assignDefaultAI();
			if (command.contains(" "))
			{
				String arg = command.split(" ")[1];
				if (arg.equalsIgnoreCase("htm"))
				{
					showFakeDashboard(activeChar);
				}
			}
			return true;
		}
		
		return true;
	}
}