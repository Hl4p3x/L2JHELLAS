package Extensions.fake.roboto.ai.walker;

import Extensions.fake.roboto.FakePlayer;
import Extensions.fake.roboto.model.WalkNode;
import Extensions.fake.roboto.model.WalkerType;

import com.l2jhellas.util.Rnd;

public class GiranWalkerAI extends WalkerAI
{
	
	public GiranWalkerAI(FakePlayer character)
	{
		super(character);
	}
	
	@Override
	protected WalkerType getWalkerType()
	{
		return WalkerType.RANDOM;
	}
	
	@Override
	protected void setWalkNodes()
	{
		_walkNodes.add(new WalkNode(82248, 148600, -3464, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(82072, 147560, -3464, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(82792, 147832, -3464, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(83320, 147976, -3400, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(84584, 148536, -3400, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(83384, 149256, -3400, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(83064, 148392, -3464, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(87016, 148632, -3400, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(85816, 148872, -3400, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(85832, 153208, -3496, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(81384, 150040, -3528, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(79656, 150728, -3512, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(79272, 149544, -3528, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(80744, 146424, -3528, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(81894, 148284, -3493, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(82778, 148505, -3495, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(83643, 148007, -3431, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(83633, 148866, -3431, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(82597, 148472, -3495, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(82458, 148817, -3495, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(83092, 147989, -3495, Rnd.get(10, 100)));		
		_walkNodes.add(new WalkNode(82244, 148535, -3493, Rnd.get(10, 100)));
		_walkNodes.add(new WalkNode(82460, 148705, -3495, Rnd.get(10, 100)));
	}
}
