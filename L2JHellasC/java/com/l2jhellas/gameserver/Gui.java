package com.l2jhellas.gameserver;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;

public class Gui extends JFrame
{
	private static final long serialVersionUID = 1L;
	static L2PcInstance pc;
	static JPanel panel = new JPanel();
	static JPanel panel2 = new JPanel();
	static JPanel panel3 = new JPanel();
	static JPanel panel4 = new JPanel();
	static JTextField announceTa = new JTextField("Write a Text To Announce", 15);
	static JTextField pmPlayerName = new JTextField("PlayerName", 7);
	static JTextField pmPlayerMessage = new JTextField("Your Message", 8);
	static JTextField rewardName = new JTextField("PlayerName", 7);
	static JTextField id = new JTextField("RewardId", 5);
	static JTextField count = new JTextField("Count", 3);
	static JLabel onlinePlayers = new JLabel("Online Players: ");
	public static JLabel hopzone = new JLabel("HopZone Votes: ");
	public static JLabel topzone = new JLabel("TopZone Votes: ");
	static JLabel ramUsage = new JLabel("Ram Usage: ");
	static JLabel generalMessage = new JLabel("Restart and shutdown have 60 seconds delay.");
	static JLabel serverStatus = new JLabel("Server Status: ");
	static JButton restart = new JButton("Restart");
	static JButton shutdown = new JButton("Shutdown");
	static JButton pmPlayer = new JButton("Send Pm");
	static JButton announce = new JButton("Announce");
	static JButton abort = new JButton("Abort");
	static JButton instaDown = new JButton("Instant Shutdown");
	static JButton reward = new JButton("Reward");
	
	public static void main(String[] args)
	{
		new Gui();
	}
	
	public void initListeners()
	{
		abort.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Shutdown.getInstance().abort(null);
				generalMessage.setText("ShutDown Aborted!");
			}
		});
		shutdown.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Shutdown.getInstance().startShutdown(null, "Control Panel", 60, false);
				generalMessage.setText("Shutdown process will start in 60 seconds");
			}
		});
		restart.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Shutdown.getInstance().startShutdown(null, "Control Panel", 60, true);
				generalMessage.setText("Restart process will start in 60 seconds");
			}
		});
		pmPlayer.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				pc = L2World.getInstance().getPlayer(pmPlayerName.getText().toString().trim());
				if (pc == null || !pc.isOnline())
				{
					generalMessage.setText("The player is offfline!");
					pmPlayerName.setText("PlayerName");
					pmPlayerMessage.setText("Your Message");
				}
				else
				{
					pc.sendPacket(new CreatureSay(2, 2, "ControlPanel", pmPlayerMessage.getText()));
					generalMessage.setText("Your Message has been sent!");
					pmPlayerName.setText("PlayerName");
					pmPlayerMessage.setText("Your Message");
				}
				
			}
			
		});
		reward.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				pc = L2World.getInstance().getPlayer(rewardName.getText().toString().trim());
				
				if (pc == null || !pc.isOnline())
				{
					generalMessage.setText("Player " + rewardName.getText().toString() + " is offline.");
					rewardName.setText("PlayerName");
					id.setText("RewardId");
					count.setText("Count");
				}
				else
				{
					pc.addItem("ControlPanel", Integer.parseInt(id.getText().toString().trim()), Integer.parseInt(count.getText().toString().trim()), pc, true);
					generalMessage.setText("Player " + rewardName.getText() + " has been rewarded!");
					id.setText("RewardId");
					count.setText("Count");
					rewardName.setText("PlayerName");
				}
				
			}
		});
		announce.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Announcements.getInstance().announceToAll(announceTa.getText());
				announceTa.setText("Write a Text To Announce");
				generalMessage.setText("Announcement Sent!");
			}
		});
		instaDown.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});
		pmPlayerName.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				pmPlayerName.setText(" ");
			}
		});
		announceTa.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				announceTa.setText(" ");
			}
		});
		pmPlayerMessage.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				pmPlayerMessage.setText(" ");
			}
		});
		id.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				id.setText(" ");
			}
		});
		count.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				count.setText(" ");
			}
		});
		rewardName.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				rewardName.setText(" ");
			}
		});
	}
	
	public void initStats()
	{
		ramUsage.setText("Ram Usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576 + "MB");
		onlinePlayers.setText("Online Players: " + L2World.getInstance().getAllPlayersCount());
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				initStats();
			}
		}, 60 * 1000);
	}
	
	public Gui()
	{
		setTitle("L2jHellas Control Panel");
		setSize(400, 200);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		panel.add(onlinePlayers);
		panel.add(hopzone);
		panel.add(topzone);
		panel.add(ramUsage);
		panel.add(serverStatus);
		panel.setPreferredSize(new Dimension(110, 70));
		panel4.add(generalMessage);
		panel4.setPreferredSize(new Dimension(50, 20));
		panel2.add(abort);
		panel2.add(restart);
		panel2.add(shutdown);
		panel2.add(instaDown);
		panel3.add(announceTa);
		panel3.add(announce);
		panel3.add(pmPlayerName);
		panel3.add(pmPlayerMessage);
		panel3.add(pmPlayer);
		panel3.add(rewardName);
		panel3.add(id);
		panel3.add(count);
		panel3.add(reward);
		panel3.setPreferredSize(new Dimension(275, 45));
		add(panel, BorderLayout.WEST);
		add(panel2, BorderLayout.SOUTH);
		add(panel3, BorderLayout.EAST);
		add(panel4, BorderLayout.NORTH);
		initStats();
		initListeners();
		ImageIcon img = new ImageIcon("data/images/GuiIcon.jpg");
		setIconImage(img.getImage());
		setVisible(true);
	}
	
}