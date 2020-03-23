package Extensions.Balancer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.network.serverpackets.UserInfo;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class BalancerEdit
{
	private static final Logger log = Logger.getLogger(BalancerEdit.class.getSimpleName());
	
	public static void editStat(String stat, int classId, int value, boolean add)
	{
		switch (stat)
		{
			case "patk":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET patk=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT patk FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("patk") + value);
							BalanceLoad.PAtk[classId - 88] = BalanceLoad.PAtk[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("patk") - value);
							BalanceLoad.PAtk[classId - 88] = BalanceLoad.PAtk[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						if (Config.DEVELOPER)
							e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "matk":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET matk=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT matk FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("matk") + value);
							BalanceLoad.MAtk[classId - 88] = BalanceLoad.MAtk[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("matk") - value);
							BalanceLoad.MAtk[classId - 88] = BalanceLoad.MAtk[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "pdef":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET pdef=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT pdef FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("pdef") + value);
							BalanceLoad.PDef[classId - 88] = BalanceLoad.PDef[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("pdef") - value);
							BalanceLoad.PDef[classId - 88] = BalanceLoad.PDef[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "mdef":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET mdef=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT mdef FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("mdef") + value);
							BalanceLoad.MDef[classId - 88] = BalanceLoad.MDef[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("mdef") - value);
							BalanceLoad.MDef[classId - 88] = BalanceLoad.MDef[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "acc":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET acc=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT acc FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("acc") + value);
							BalanceLoad.Accuracy[classId - 88] = BalanceLoad.Accuracy[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("acc") - value);
							BalanceLoad.Accuracy[classId - 88] = BalanceLoad.Accuracy[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "ev":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET ev=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT ev FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("ev") + value);
							BalanceLoad.Evasion[classId - 88] = BalanceLoad.Evasion[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("ev") - value);
							BalanceLoad.Evasion[classId - 88] = BalanceLoad.Evasion[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "patksp":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET patksp=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT patksp FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("patksp") + value);
							BalanceLoad.PAtkSpd[classId - 88] = BalanceLoad.PAtkSpd[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("patksp") - value);
							BalanceLoad.PAtkSpd[classId - 88] = BalanceLoad.PAtkSpd[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "matksp":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET matksp=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT matksp FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("matksp") + value);
							BalanceLoad.MAtkSpd[classId - 88] = BalanceLoad.MAtkSpd[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("matksp") - value);
							BalanceLoad.MAtkSpd[classId - 88] = BalanceLoad.MAtkSpd[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "cp":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET cp=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT cp FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("cp") + value);
							BalanceLoad.CP[classId - 88] = BalanceLoad.CP[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("cp") - value);
							BalanceLoad.CP[classId - 88] = BalanceLoad.CP[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "hp":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET hp=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT hp FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("hp") + value);
							BalanceLoad.HP[classId - 88] = BalanceLoad.HP[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("hp") - value);
							BalanceLoad.HP[classId - 88] = BalanceLoad.HP[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "mp":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET mp=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT mp FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("mp") + value);
							BalanceLoad.MP[classId - 88] = BalanceLoad.MP[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("mp") - value);
							BalanceLoad.MP[classId - 88] = BalanceLoad.MP[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "walk":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET walk=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT walk FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("walk") + value);
							BalanceLoad.Speed[classId - 88] = BalanceLoad.Speed[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("walk") - value);
							BalanceLoad.Speed[classId - 88] = BalanceLoad.Speed[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "critical":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET critical=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT critical FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("critical") + value);
							BalanceLoad.Critical[classId - 88] = BalanceLoad.Critical[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("Critical") - value);
							BalanceLoad.Critical[classId - 88] = BalanceLoad.Critical[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "magiccritical":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET magiccritical=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT magiccritical FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("magiccritical") + value);
							BalanceLoad.MagicCritical[classId - 88] = BalanceLoad.MagicCritical[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("magiccritical") - value);
							BalanceLoad.MagicCritical[classId - 88] = BalanceLoad.MagicCritical[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "walkspeed":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET walkspeed=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT walkspeed FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("walkspeed") + value);
							BalanceLoad.WalkSpeed[classId - 88] = BalanceLoad.WalkSpeed[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("walkspeed") - value);
							BalanceLoad.WalkSpeed[classId - 88] = BalanceLoad.WalkSpeed[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "matkrange":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET matkrange=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT matkrange FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("matkrange") + value);
							BalanceLoad.MAtkRange[classId - 88] = BalanceLoad.MAtkRange[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("matkrange") - value);
							BalanceLoad.MAtkRange[classId - 88] = BalanceLoad.MAtkRange[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "mreuserate":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET mreuserate=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT mreuserate FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("mreuserate") + value);
							BalanceLoad.MReuseRate[classId - 88] = BalanceLoad.MReuseRate[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("mreuserate") - value);
							BalanceLoad.MReuseRate[classId - 88] = BalanceLoad.MReuseRate[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "preuserate":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET preuserate=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT preuserate FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("preuserate") + value);
							BalanceLoad.PReuseRate[classId - 88] = BalanceLoad.PReuseRate[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("preuserate") - value);
							BalanceLoad.PReuseRate[classId - 88] = BalanceLoad.PReuseRate[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "INT":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET INT_=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT INT_ FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("INT_") + value);
							BalanceLoad.INT[classId - 88] = BalanceLoad.INT[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("INT_") - value);
							BalanceLoad.INT[classId - 88] = BalanceLoad.INT[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "MEN":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET MEN=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT MEN FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("MEN") + value);
							BalanceLoad.MEN[classId - 88] = BalanceLoad.MEN[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("MEN") - value);
							BalanceLoad.MEN[classId - 88] = BalanceLoad.MEN[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "WIT":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET WIT=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT WIT FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("WIT") + value);
							BalanceLoad.WIT[classId - 88] = BalanceLoad.WIT[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("WIT") - value);
							BalanceLoad.WIT[classId - 88] = BalanceLoad.WIT[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "CON":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET CON=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT CON FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("CON") + value);
							BalanceLoad.CON[classId - 88] = BalanceLoad.CON[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("CON") - value);
							BalanceLoad.CON[classId - 88] = BalanceLoad.CON[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "STR":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET STR=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT STR FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("STR") + value);
							BalanceLoad.STR[classId - 88] = BalanceLoad.STR[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("STR") - value);
							BalanceLoad.STR[classId - 88] = BalanceLoad.STR[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
			case "DEX":
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement stm = con.prepareStatement("UPDATE balance SET DEX=? WHERE class_id=?");
					PreparedStatement stm2 = con.prepareStatement("SELECT DEX FROM balance WHERE class_id=" + classId);
					ResultSet rset = stm2.executeQuery();
					
					if (rset.next())
					{
						if (add)
						{
							stm.setInt(1, rset.getInt("DEX") + value);
							BalanceLoad.DEX[classId - 88] = BalanceLoad.DEX[classId - 88] + value;
						}
						else
						{
							stm.setInt(1, rset.getInt("DEX") - value);
							BalanceLoad.DEX[classId - 88] = BalanceLoad.DEX[classId - 88] - value;
						}
						stm.setInt(2, classId);
						stm.execute();
					}
					
					rset.close();
					stm.close();
					stm2.close();
				}
				catch (Exception e)
				{
					log.log(Level.WARNING, "Error while saving balance stats to database.");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
				for (L2PcInstance p : L2World.getInstance().getAllPlayers().values())
					if (p.getClassId().getId() == classId)
						p.sendPacket(new UserInfo(p));
				break;
			}
		}
	}
	
	public void sendBalanceWindow(int classId, L2PcInstance p)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(0);
		htm.setFile("./data/html/admin/balance/balance.htm");
		htm.replace("%classId%", classId + "");
		
		p.sendPacket(htm);
	}
}