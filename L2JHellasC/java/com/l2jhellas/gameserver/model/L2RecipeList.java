package com.l2jhellas.gameserver.model;

public class L2RecipeList
{
	
	private L2RecipeInstance[] _recipes;
	
	private final int _id;
	
	private final int _level;
	
	private final int _recipeId;
	
	private final String _recipeName;
	
	private final int _successRate;
	
	private final int _mpCost;
	
	private final int _itemId;
	
	private final int _count;
	
	private final boolean _isDwarvenRecipe;
	
	public L2RecipeList(int id, int level, int recipeId, String recipeName, int successRate, int mpCost, int itemId, int count, boolean isDwarvenRecipe)
	{
		_id = id;
		_recipes = new L2RecipeInstance[0];
		_level = level;
		_recipeId = recipeId;
		_recipeName = recipeName;
		_successRate = successRate;
		_mpCost = mpCost;
		_itemId = itemId;
		_count = count;
		_isDwarvenRecipe = isDwarvenRecipe;
	}
	
	public void addRecipe(L2RecipeInstance recipe)
	{
		int len = _recipes.length;
		L2RecipeInstance[] tmp = new L2RecipeInstance[len + 1];
		System.arraycopy(_recipes, 0, tmp, 0, len);
		tmp[len] = recipe;
		_recipes = tmp;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getRecipeId()
	{
		return _recipeId;
	}
	
	public String getRecipeName()
	{
		return _recipeName;
	}
	
	public int getSuccessRate()
	{
		return _successRate;
	}
	
	public int getMpCost()
	{
		return _mpCost;
	}
	
	public boolean isConsumable()
	{
		return ((_itemId >= 1463 && _itemId <= 1467) // Soulshots
			|| (_itemId >= 2509 && _itemId <= 2514) // Spiritshots
			|| (_itemId >= 3947 && _itemId <= 3952) // Blessed Spiritshots
		|| (_itemId >= 1341 && _itemId <= 1345) // Arrows
		);
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getCount()
	{
		return _count;
	}
	
	public boolean isDwarvenRecipe()
	{
		return _isDwarvenRecipe;
	}
	
	public L2RecipeInstance[] getRecipes()
	{
		return _recipes;
	}
}