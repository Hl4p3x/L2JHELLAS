package com.l2jhellas.gameserver.network.serverpackets;

public class ExEventMatchMessage extends L2GameServerPacket
{
	private static final String _S__FE_04_EXEVENTMATCHMESSAGE = "[S] FE:04 ExEventMatchMessage";

	enum MessageType
	{
		STRING,
		EVENT_FINISH,
		EVENT_START,
		EVENT_GAME_OVER,
		EVENT_1,
		EVENT_2,
		EVENT_3,
		EVENT_4,
		EVENT_5
	}

	public static final ExEventMatchMessage EVENT_FINISH = new ExEventMatchMessage(MessageType.EVENT_FINISH);
	public static final ExEventMatchMessage EVENT_STARTT = new ExEventMatchMessage(MessageType.EVENT_START);
	public static final ExEventMatchMessage EVENT_GAME_OVER = new ExEventMatchMessage(MessageType.EVENT_GAME_OVER);
	public static final ExEventMatchMessage COUNTDOWN_1 = new ExEventMatchMessage(MessageType.EVENT_1);
	public static final ExEventMatchMessage COUNTDOWN_2 = new ExEventMatchMessage(MessageType.EVENT_2);
	public static final ExEventMatchMessage COUNTDOWN_3 = new ExEventMatchMessage(MessageType.EVENT_3);
	public static final ExEventMatchMessage COUNTDOWN_4 = new ExEventMatchMessage(MessageType.EVENT_4);
	public static final ExEventMatchMessage COUNTDOWN_5 = new ExEventMatchMessage(MessageType.EVENT_5);

	private final MessageType _type;
	private final String _message;

	public ExEventMatchMessage(String message)
	{
		_type = MessageType.STRING;
		_message = message;
	}
	
	private ExEventMatchMessage(MessageType type)
	{
		_type = type;
		_message = null;
	}
	
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x04);
		writeC(_type.ordinal());
		
		if (_type == MessageType.STRING) 
			writeS(_message);		
	}
	
	@Override
	public String getType()
	{
		return _S__FE_04_EXEVENTMATCHMESSAGE;
	}
}
