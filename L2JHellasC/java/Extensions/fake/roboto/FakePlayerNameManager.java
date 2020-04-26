package Extensions.fake.roboto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.datatables.sql.CharNameTable;
import com.l2jhellas.util.Rnd;

public enum FakePlayerNameManager
{
	INSTANCE;
	
	public static final Logger _log = Logger.getLogger(FakePlayerNameManager.class.getName());
	private List<String> _fakePlayerNames = new ArrayList<>();
	int id = 1;
	
	public void initialise()
	{
		loadWordlist();
	}

	public String getRandomAvailableName()
	{
		id ++;	
		String name = getRandomNameFromWordlist();
		
		if(nameAlreadyExists(name))
		   return name+id;
		
		return name;
	}
	
	private String getRandomNameFromWordlist()
	{
		return _fakePlayerNames.get(Rnd.get(0, _fakePlayerNames.size() - 1));
	}
	
	public List<String> getFakePlayerNames()
	{
		return _fakePlayerNames;
	}
	
	private void loadWordlist()
	{
		try (LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(new File("./data/fakenamewordlist.txt"))));)
		{
			String line;
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().length() == 0 || line.startsWith("#"))
					continue;
				_fakePlayerNames.add(line);
			}
			_log.log(Level.INFO, String.format("Loaded %s fake player names.", _fakePlayerNames.size()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static boolean nameAlreadyExists(String name)
	{
		return CharNameTable.getInstance().doesCharNameExist(name);
	}
}
