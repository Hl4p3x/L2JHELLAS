package com.l2jhellas.gameserver.instancemanager.games;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.Announcements;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class Lottery
{
	protected static final Logger _log = Logger.getLogger(Lottery.class.getName());
	
	public static final long SECOND = 1000;
	public static final long MINUTE = 60000;
	
	private static Lottery _instance;
	
	private static final String INSERT_LOTTERY = "INSERT INTO games(id, idnr, enddate, prize, newprize) VALUES (?, ?, ?, ?, ?)";
	private static final String UPDATE_PRICE = "UPDATE games SET prize=?, newprize=? WHERE id = 1 AND idnr = ?";
	private static final String UPDATE_LOTTERY = "UPDATE games SET finished=1, prize=?, newprize=?, number1=?, number2=?, prize1=?, prize2=?, prize3=? WHERE id=1 AND idnr=?";
	private static final String SELECT_LAST_LOTTERY = "SELECT idnr, prize, newprize, enddate, finished FROM games WHERE id = 1 ORDER BY idnr DESC LIMIT 1";
	private static final String SELECT_LOTTERY_ITEM = "SELECT enchant_level, custom_type2 FROM items WHERE item_id = 4442 AND custom_type1 = ?";
	private static final String SELECT_LOTTERY_TICKET = "SELECT number1, number2, prize1, prize2, prize3 FROM games WHERE id = 1 AND idnr = ?";
	
	protected int _number;
	protected int _prize;
	protected boolean _isSellingTickets;
	protected boolean _isStarted;
	protected long _enddate;
	
	private Lottery()
	{
		_number = 1;
		_prize = Config.ALT_LOTTERY_PRIZE;
		_isSellingTickets = false;
		_isStarted = false;
		_enddate = System.currentTimeMillis();
		
		if (Config.ALLOW_LOTTERY)
			(new startLottery()).run();
	}
	
	public static Lottery getInstance()
	{
		if (_instance == null)
			_instance = new Lottery();
		
		return _instance;
	}
	
	public int getId()
	{
		return _number;
	}
	
	public int getPrize()
	{
		return _prize;
	}
	
	public long getEndDate()
	{
		return _enddate;
	}
	
	public void increasePrize(int count)
	{
		_prize += count;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement(UPDATE_PRICE))
		{
			statement.setInt(1, getPrize());
			statement.setInt(2, getPrize());
			statement.setInt(3, getId());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(Lottery.class.getName() + ": Could not increase current lottery prize: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public boolean isSellableTickets()
	{
		return _isSellingTickets;
	}
	
	public boolean isStarted()
	{
		return _isStarted;
	}
	
	private class startLottery implements Runnable
	{
		protected startLottery()
		{
			// Do nothing
		}
		
		@Override
		public void run()
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_LAST_LOTTERY))
			{
				try(ResultSet rset = statement.executeQuery())
				{
					if (rset.next())
					{
						_number = rset.getInt("idnr");

						if (rset.getInt("finished") == 1)
						{
							_number++;
							_prize = rset.getInt("newprize");
						}
						else
						{
							_prize = rset.getInt("prize");
							_enddate = rset.getLong("enddate");

							if (_enddate <= System.currentTimeMillis() + 2 * MINUTE)
							{
								(new finishLottery()).run();
								rset.close();
								statement.close();
								return;
							}

							if (_enddate > System.currentTimeMillis())
							{
								_isStarted = true;
								ThreadPoolManager.getInstance().scheduleGeneral(new finishLottery(), _enddate - System.currentTimeMillis());

								if (_enddate > System.currentTimeMillis() + 12 * MINUTE)
								{
									_isSellingTickets = true;
									ThreadPoolManager.getInstance().scheduleGeneral(new stopSellingTickets(), _enddate - System.currentTimeMillis() - 10 * MINUTE);
								}
								rset.close();
								statement.close();
								return;
							}
						}
					}
				}
			}
			catch (SQLException e)
			{
				_log.warning(Lottery.class.getName() + ": Could not restore lottery data: ");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
			
			if (Config.DEBUG)
				_log.config(Lottery.class.getName() + ": Starting ticket sell for lottery #" + getId() + ".");
			
			_isSellingTickets = true;
			_isStarted = true;
			
			Announcements.getInstance().announceToAll("Lottery tickets are now available for Lucky Lottery #" + getId() + ".");
			Calendar finishtime = Calendar.getInstance();
			finishtime.setTimeInMillis(_enddate);
			finishtime.set(Calendar.MINUTE, 0);
			finishtime.set(Calendar.SECOND, 0);
			
			if (finishtime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
			{
				finishtime.set(Calendar.HOUR_OF_DAY, 19);
				_enddate = finishtime.getTimeInMillis();
				_enddate += 604800000;
			}
			else
			{
				finishtime.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
				finishtime.set(Calendar.HOUR_OF_DAY, 19);
				_enddate = finishtime.getTimeInMillis();
			}
			
			ThreadPoolManager.getInstance().scheduleGeneral(new stopSellingTickets(), _enddate - System.currentTimeMillis() - 10 * MINUTE);
			ThreadPoolManager.getInstance().scheduleGeneral(new finishLottery(), _enddate - System.currentTimeMillis());
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_LOTTERY))
			{
				statement.setInt(1, 1);
				statement.setInt(2, getId());
				statement.setLong(3, getEndDate());
				statement.setInt(4, getPrize());
				statement.setInt(5, getPrize());
				statement.execute();
			}
			catch (SQLException e)
			{
				_log.warning(Lottery.class.getName() + ": Could not store new lottery data: ");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
		}
	}
	
	private class stopSellingTickets implements Runnable
	{
		protected stopSellingTickets()
		{
			// Do nothing
		}
		
		@Override
		public void run()
		{
			if (Config.DEBUG)
				_log.config(Lottery.class.getName() + ": Stopping ticket sell for lottery #" + getId() + ".");
			
			_isSellingTickets = false;
			
			Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.LOTTERY_TICKET_SALES_TEMP_SUSPENDED));
		}
	}
	
	private class finishLottery implements Runnable
	{
		protected finishLottery()
		{
			// Do nothing
		}
		
		@Override
		public void run()
		{
			if (Config.DEBUG)
				_log.config(Lottery.class.getName() + ": Ending lottery #" + getId() + ".");
			
			int[] luckynums = new int[5];
			int luckynum = 0;
			
			for (int i = 0; i < 5; i++)
			{
				boolean found = true;
				
				while (found)
				{
					luckynum = Rnd.get(20) + 1;
					found = false;
					
					for (int j = 0; j < i; j++)
						if (luckynums[j] == luckynum)
							found = true;
				}
				
				luckynums[i] = luckynum;
			}
			
			if (Config.DEBUG)
				_log.config(Lottery.class.getName() + ": The lucky numbers are " + luckynums[0] + ", " + luckynums[1] + ", " + luckynums[2] + ", " + luckynums[3] + ", " + luckynums[4] + ".");
			
			int enchant = 0;
			int type2 = 0;
			
			for (int i = 0; i < 5; i++)
			{
				if (luckynums[i] < 17)
					enchant += Math.pow(2, luckynums[i] - 1);
				else
					type2 += Math.pow(2, luckynums[i] - 17);
			}
			
			if (Config.DEBUG)
				_log.config(Lottery.class.getName() + ": Encoded lucky numbers are " + enchant + ", " + type2);
			
			int count1 = 0;
			int count2 = 0;
			int count3 = 0;
			int count4 = 0;
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_LOTTERY_ITEM))
			{
				statement.setInt(1, getId());
				try(ResultSet rset = statement.executeQuery())
				{
					while (rset.next())
					{
						int curenchant = rset.getInt("enchant_level") & enchant;
						int curtype2 = rset.getInt("custom_type2") & type2;

						if (curenchant == 0 && curtype2 == 0)
							continue;

						int count = 0;

						for (int i = 1; i <= 16; i++)
						{
							int val = curenchant / 2;

							if (val != (double) curenchant / 2)
								count++;

							int val2 = curtype2 / 2;

							if (val2 != (double) curtype2 / 2)
								count++;

							curenchant = val;
							curtype2 = val2;
						}

						if (count == 5)
							count1++;
						else if (count == 4)
							count2++;
						else if (count == 3)
							count3++;
						else if (count > 0)
							count4++;
					}
				}			
			}
			catch (SQLException e)
			{
				_log.warning(Lottery.class.getName() + ": Could restore lottery data: ");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
			
			int prize4 = count4 * Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
			int prize1 = 0;
			int prize2 = 0;
			int prize3 = 0;
			
			if (count1 > 0)
				prize1 = (int) ((getPrize() - prize4) * Config.ALT_LOTTERY_5_NUMBER_RATE / count1);
			
			if (count2 > 0)
				prize2 = (int) ((getPrize() - prize4) * Config.ALT_LOTTERY_4_NUMBER_RATE / count2);
			
			if (count3 > 0)
				prize3 = (int) ((getPrize() - prize4) * Config.ALT_LOTTERY_3_NUMBER_RATE / count3);
			
			_log.info(Lottery.class.getSimpleName() + ": " + count1 + " players with all FIVE numbers each win " + prize1 + ".");
			_log.info(Lottery.class.getSimpleName() + ": " + count2 + " players with FOUR numbers each win " + prize2 + ".");
			_log.info(Lottery.class.getSimpleName() + ": " + count3 + " players with THREE numbers each win " + prize3 + ".");
			_log.info(Lottery.class.getSimpleName() + ": " + count4 + " players with ONE or TWO numbers each win " + prize4 + ".");
			
			int newprize = getPrize() - (prize1 + prize2 + prize3 + prize4);
			
			_log.info(Lottery.class.getSimpleName() + ": Jackpot for next lottery is " + newprize + ".");
			
			SystemMessage sm;
			if (count1 > 0)
			{
				// There are winners.
				sm = SystemMessage.getSystemMessage(SystemMessageId.AMOUNT_FOR_WINNER_S1_IS_S2_ADENA_WE_HAVE_S3_PRIZE_WINNER);
				sm.addNumber(getId());
				sm.addNumber(getPrize());
				sm.addNumber(count1);
				Announcements.getInstance().announceToAll(sm);
			}
			else
			{
				// There are no winners.
				sm = SystemMessage.getSystemMessage(SystemMessageId.AMOUNT_FOR_LOTTERY_S1_IS_S2_ADENA_NO_WINNER);
				sm.addNumber(getId());
				sm.addNumber(getPrize());
				Announcements.getInstance().announceToAll(sm);
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_LOTTERY))
			{
				statement.setInt(1, getPrize());
				statement.setInt(2, newprize);
				statement.setInt(3, enchant);
				statement.setInt(4, type2);
				statement.setInt(5, prize1);
				statement.setInt(6, prize2);
				statement.setInt(7, prize3);
				statement.setInt(8, getId());
				statement.execute();
			}
			catch (SQLException e)
			{
				_log.warning(Lottery.class.getName() + ": Could not store finished lottery data: ");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
			
			ThreadPoolManager.getInstance().scheduleGeneral(new startLottery(), MINUTE);
			_number++;
			
			_isStarted = false;
		}
	}
	
	public int[] decodeNumbers(int enchant, int type2)
	{
		int res[] = new int[5];
		int id = 0;
		int nr = 1;
		
		while (enchant > 0)
		{
			int val = enchant / 2;
			if (val != (double) enchant / 2)
			{
				res[id] = nr;
				id++;
			}
			enchant /= 2;
			nr++;
		}
		
		nr = 17;
		
		while (type2 > 0)
		{
			int val = type2 / 2;
			if (val != (double) type2 / 2)
			{
				res[id] = nr;
				id++;
			}
			type2 /= 2;
			nr++;
		}
		
		return res;
	}
	
	public int[] checkTicket(L2ItemInstance item)
	{
		return checkTicket(item.getCustomType1(), item.getEnchantLevel(), item.getCustomType2());
	}
	
	public int[] checkTicket(int id, int enchant, int type2)
	{
		int res[] =
		{
			0,
			0
		};
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement(SELECT_LOTTERY_TICKET))
		{
			statement.setInt(1, id);
			try(ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					int curenchant = rset.getInt("number1") & enchant;
					int curtype2 = rset.getInt("number2") & type2;

					if (curenchant == 0 && curtype2 == 0)
					{
						rset.close();
						statement.close();
						return res;
					}

					int count = 0;

					for (int i = 1; i <= 16; i++)
					{
						int val = curenchant / 2;
						if (val != (double) curenchant / 2)
							count++;
						int val2 = curtype2 / 2;
						if (val2 != (double) curtype2 / 2)
							count++;
						curenchant = val;
						curtype2 = val2;
					}

					switch (count)
					{
						case 0:
							break;
						case 5:
							res[0] = 1;
							res[1] = rset.getInt("prize1");
							break;
						case 4:
							res[0] = 2;
							res[1] = rset.getInt("prize2");
							break;
						case 3:
							res[0] = 3;
							res[1] = rset.getInt("prize3");
							break;
						default:
							res[0] = 4;
							res[1] = 200;
					}

					if (Config.DEBUG)
						_log.warning(Lottery.class.getName() + ": count: " + count + ", id: " + id + ", enchant: " + enchant + ", type2: " + type2);
				}
			}
		}
		catch (SQLException e)
		{
			_log.warning(Lottery.class.getName() + ": Could not check lottery ticket #" + id + ": ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		return res;
	}
}