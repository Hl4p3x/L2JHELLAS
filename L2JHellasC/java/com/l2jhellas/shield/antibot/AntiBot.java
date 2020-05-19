package com.l2jhellas.shield.antibot;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.network.serverpackets.PledgeCrest;
import com.l2jhellas.util.Rnd;

import Extensions.RankSystem.Util.DDSConverter;

public class AntiBot
{
	private static final char[] CAPTCHA_POSSIBILITIES = {'A','B','C','D','E','F','G','H','K','L','M','P','R','S','T','U','W','X','Y','Z'};
	private static final int CAPTCHA_LENGTH = 5;
	private static final int CAPTCHA_MIN_ID = 1900000000;
	private static final int CAPTCHA_MAX_ID = 2000000000;

	public static boolean isvoting;

	public static void getInstance()
	{
		startAntibot();
	}
	
	public static void showWindow()
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
			if(!player.isGM())
			   sendCaptcha(player);

		isvoting = true;
		
		startCheck();		
	}
	
	public static void startAntibot()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				showWindow();
			}
		}, 60 * 1000 * Config.SECURITY_QUE_TIME);
	}
	
	public static void startCheck()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				isvoting = false;

				for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
				{
					if(player == null || !player.isbOnline() || player.isGM())
						continue;
							
					if (!player.PassedProt && !player.isGM())
					{
						if(player.isInCombat() || player.getPvpFlag() > 0 || player.getKarma() > 0)
						{
							player.store();
							player.closeNetConnection(false);
						}
						else
						{
							player.stopMove(null);
							player.teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
							player.sendMessage("You have been teleported because you did not passed the protection system.");
						}
					}
				}
								
				for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
				{
					if(player == null || !player.isbOnline() || player.isGM())
						continue;
					
					player.PassedProt = false;
					player.clearCaptcha();
				}
				
				startAntibot();				
			}
		}, 60 * 1000 * 2);
	}

	public static void CheckBypass(L2PcInstance player, String answer)
	{
		if(!isvoting || answer.isEmpty())
			return;
		
		if (answer == null || !player.getCorrectCaptcha().equals(answer))
		{
			player.PassedProt = false;
			player.clearCaptcha();
			player.sendMessage("Wrong answer,to avoid kick dont fight and you will be teleported in nearest town.");
		}
		else
		{
			player.PassedProt = true;
			player.clearCaptcha();
			player.sendMessage("Correct!");
		}
	}
	
	public static String sendCaptcha(L2PcInstance player)
	{
		int captchaId = generateRandomCaptchaId();
		char[] captchaText = generateCaptcha();

		BufferedImage image = generateCaptcha(captchaText);
		PledgeCrest packet = new PledgeCrest(captchaId, DDSConverter.convertToDDS(image).array());
		player.sendPacket(packet);

		sendCaptchaWindow(player, captchaId);

		player.clearCaptcha();
		player.setCorrectCaptcha(String.valueOf(captchaText));
		return String.valueOf(captchaText);
	}
	
	private static void sendCaptchaWindow(L2PcInstance player, int captchaId)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/captcha.htm");	
		html.replace("%servId%", String.valueOf(Config.SERVER_ID));
		html.replace("%captchaId%", String.valueOf(captchaId));
		player.sendPacket(html);
	}

	private static char[] generateCaptcha()
	{
		char[] text = new char[5];
		for (int i = 0; i < CAPTCHA_LENGTH; i++)
			text[i] = CAPTCHA_POSSIBILITIES[Rnd.get(CAPTCHA_POSSIBILITIES.length)];
		return text;
	}

	private static int generateRandomCaptchaId()
	{
		return Rnd.get(CAPTCHA_MIN_ID, CAPTCHA_MAX_ID);
	}

	private static BufferedImage generateCaptcha(char[] text)
	{
		Color textColor = new Color(38, 213, 30);
		Color circleColor = new Color(73, 100, 151);
		Font textFont = new Font("comic sans ms", Font.BOLD, 24);
		int charsToPrint = 5;
		int width = 256;
		int height = 64;
		int circlesToDraw = 8;
		float horizMargin = 20.0f;
		double rotationRange = 0.7;
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics2D g = (Graphics2D) bufferedImage.getGraphics();

		g.setColor(new Color(30,31,31));
		g.fillRect(0, 0, width, height);

		g.setColor(circleColor);
		for ( int i = 0; i < circlesToDraw; i++ ) {
			int circleRadius = (int) (Math.random() * height / 2.0);
			int circleX = (int) (Math.random() * width - circleRadius);
			int circleY = (int) (Math.random() * height - circleRadius);
			g.drawOval(circleX, circleY, circleRadius * 2, circleRadius * 2);
		}

		g.setColor(textColor);
		g.setFont(textFont);

		FontMetrics fontMetrics = g.getFontMetrics();
		int maxAdvance = fontMetrics.getMaxAdvance();
		int fontHeight = fontMetrics.getHeight();

		float spaceForLetters = -horizMargin * 2.0F + width;
		float spacePerChar = spaceForLetters / (charsToPrint - 1.0f);

		for ( int i = 0; i < charsToPrint; i++ )
		{
			char characterToShow = text[i];

			int charWidth = fontMetrics.charWidth(characterToShow);
			int charDim = Math.max(maxAdvance, fontHeight);
			int halfCharDim = charDim / 2;

			BufferedImage charImage = new BufferedImage(charDim, charDim, BufferedImage.TYPE_INT_ARGB);
			Graphics2D charGraphics = charImage.createGraphics();
			charGraphics.translate(halfCharDim, halfCharDim);
			double angle = (Math.random() - 0.5) * rotationRange;
			charGraphics.transform(AffineTransform.getRotateInstance(angle));
			charGraphics.translate(-halfCharDim,-halfCharDim);
			charGraphics.setColor(textColor);
			charGraphics.setFont(textFont);

			int charX = (int) (0.5 * charDim - 0.5 * charWidth);
			charGraphics.drawString(String.valueOf(characterToShow), charX, (charDim - fontMetrics.getAscent()) / 2 + fontMetrics.getAscent());

			float x = horizMargin + spacePerChar * i - charDim / 2.0f;
			int y = (height - charDim) / 2;
			g.drawImage(charImage, (int) x, y, charDim, charDim, null, null);

			charGraphics.dispose();
		}

		g.dispose();
		return bufferedImage;
	}
}