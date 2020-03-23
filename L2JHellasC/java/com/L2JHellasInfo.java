package com;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.GameServer;

public class L2JHellasInfo
{
	
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());
	
	public static final void showInfo()
	{
		_log.info("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
		_log.info("                              Interlude Project                                 ");
		_log.info("                                     Owner                                      ");
		_log.info("                            Boorinio,AbsolutePower                              ");
		_log.info("                                                                                ");
		_log.info("                 ##    ##            ##   ##            ######                  ");
		_log.info("                 ##    ##            ##   ##           ##    ##                 ");
		_log.info("                 ##    ##            ##   ##           ##    ##                 ");
		_log.info("                 ##    ##    ###     ##   ##    ###     ##                      ");
		_log.info("                 ########   ## ##    ##   ##   ## ##      ##                    ");
		_log.info("                 ##    ##   #####    ##   ##      ##        ##                  ");
		_log.info("                 ##    ##   ##       ##   ##    ####   ##    ##                 ");
		_log.info("                 ##    ##   ## ##    ##   ##   ## ##   ##    ##                 ");
		_log.info("                 ##    ##    ###     ##   ##    ####    ######                  ");
		_log.info("                                                                                ");
		_log.info("                           Contact: nikos.nikosnikos1                           ");
		_log.info("                          Forum: http://l2jhellas.com/                          ");
		_log.info(" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
		_log.info("             Server Version: " + Config.SERVER_VERSION + " Builded: " + Config.SERVER_BUILD_DATE);
		_log.info("");
	}
}