package Extensions.fake.roboto.task;

import java.util.List;

import Extensions.fake.roboto.FakePlayerTaskManager;

import com.l2jhellas.gameserver.ThreadPoolManager;

public class AITaskRunner implements Runnable
{
	@Override
	public void run()
	{
		FakePlayerTaskManager.INSTANCE.adjustTaskSize();
		List<AITask> aiTasks = FakePlayerTaskManager.INSTANCE.getAITasks();
		aiTasks.forEach(aiTask -> ThreadPoolManager.getInstance().executeAi(aiTask));
	}
}