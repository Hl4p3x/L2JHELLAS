package Extensions.RankSystem;

public class TopField
{
	private int _characterId = 0;
	private String _characterName = null;
	private int _characterLevel = 0;
	private int _characterBaseClassId = 0;
	private long _value = 0; // rank points or total kills
	private int _topPosition = 0;
	
	public int getCharacterId()
	{
		return _characterId;
	}
	
	public void setCharacterId(int characterId)
	{
		_characterId = characterId;
	}
	
	public long getValue()
	{
		return _value;
	}
	
	public void setValue(long value)
	{
		_value = value;
	}
	
	public String getCharacterName()
	{
		return _characterName;
	}
	
	public void setCharacterName(String characterName)
	{
		_characterName = characterName;
	}
	
	public int getCharacterLevel()
	{
		return _characterLevel;
	}
	
	public void setCharacterLevel(int characterLevel)
	{
		_characterLevel = characterLevel;
	}
	
	public int getCharacterBaseClassId()
	{
		return _characterBaseClassId;
	}
	
	public void setCharacterBaseClassId(int characterBaseClassId)
	{
		_characterBaseClassId = characterBaseClassId;
	}
	
	public int getTopPosition()
	{
		return _topPosition;
	}
	
	public void setTopPosition(int topPosition)
	{
		_topPosition = topPosition;
	}
	
}
