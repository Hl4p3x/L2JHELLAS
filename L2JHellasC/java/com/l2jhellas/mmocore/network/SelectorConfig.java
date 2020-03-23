package com.l2jhellas.mmocore.network;

public final class SelectorConfig
{
	public int READ_BUFFER_SIZE = 64 * 1024;
	public int WRITE_BUFFER_SIZE = 64 * 1024;
	
	public int HELPER_BUFFER_COUNT = 20;
	public int HELPER_BUFFER_SIZE = 64 * 1024;
	
	public int MAX_SEND_PER_PASS = 10;
	
	public int MAX_READ_PER_PASS = 10;
	
	public int SLEEP_TIME = 10;
	
	public boolean TCP_NODELAY = true;
}