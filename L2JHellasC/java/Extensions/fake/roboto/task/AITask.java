package Extensions.fake.roboto.task;

import java.util.List;

import Extensions.fake.roboto.FakePlayer;
import Extensions.fake.roboto.FakePlayerManager;

public class AITask implements Runnable
{
	private final int _from;
	private int _to;
	
	public AITask(int from, int to)
	{
		_from = from;
		_to = to;
	}
	
	@Override
	public void run()
	{
		adjustPotentialIndexOutOfBounds();
		
		List<FakePlayer> fakePlayers = FakePlayerManager.getFakePlayers().subList(_from, _to);
		
		for (FakePlayer fpl : fakePlayers)
		{
			if (fpl == null)
				continue;
			
			if (fpl.getFakeAi() != null && !fpl.getFakeAi().isBusyThinking())
				fpl.getFakeAi().thinkAndAct();
		}
	}
	
	private void adjustPotentialIndexOutOfBounds()
	{
		if (_to > FakePlayerManager.getFakePlayersCount())
			_to = FakePlayerManager.getFakePlayersCount();
	}
}
