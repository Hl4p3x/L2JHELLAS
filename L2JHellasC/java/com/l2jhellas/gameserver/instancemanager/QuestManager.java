package com.l2jhellas.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.model.quest.Quest;

public class QuestManager
{
	protected static final Logger _log = Logger.getLogger(QuestManager.class.getName());
	
	private final List<Quest> _quests = new ArrayList<>();
	
	public QuestManager()
	{
		
	}
	
	public void addQuest(Quest quest)
	{
		_quests.add(quest);
	}
	
	public final Quest getQuest(String name)
	{
		return _quests.stream().filter(q -> q.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}
	
	public final Quest getQuest(int questId)
	{
		return _quests.stream().filter(q -> q.getQuestId() == questId).findFirst().orElse(null);
	}
	
	public void cleanQuests()
	{
		_quests.clear();
	}
	
	public List<Quest> getAllManagedScripts()
	{
		return _quests;
	}
	
	public final void report()
	{
		_log.info(QuestManager.class.getSimpleName() + ": Loaded: " + _quests.size() + " quests.");
	}
	
	public static final QuestManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final QuestManager _instance = new QuestManager();
	}
}