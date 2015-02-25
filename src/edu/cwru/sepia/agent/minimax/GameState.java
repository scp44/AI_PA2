package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.util.*;


/**
 * This class stores all of the information the agent
 * needs to know about the state of the game. For example this
 * might include things like footmen HP and positions.
 *
 * Add any information or methods you would like to this class,
 * but do not delete or change the signatures of the provided methods.
 */
public class GameState {

	class MapLocation
    {
        public int x, y;


        public MapLocation(int x, int y)
        {
            this.x = x;
            this.y = y;
        }
    }
	
	
	
	private List<Integer> friendlyUnitIDs = new ArrayList<Integer>();
	private List<Integer> enemyUnitIDs = new ArrayList<Integer>();
	private Unit.UnitView[] friendlyArr;
	private Unit.UnitView[] enemyArr;
	private HashSet<MapLocation> resourceLocations = new HashSet<MapLocation>();
	private int mapXExtent;
	private int mapYExtent;
    /**
     * You will implement this constructor. It will
     * extract all of the needed state information from the built in
     * SEPIA state view.
     *
     * You may find the following state methods useful:
     *
     * state.getXExtent() and state.getYExtent(): get the map dimensions
     * state.getAllResourceIDs(): returns all of the obstacles in the map
     * state.getResourceNode(Integer resourceID): Return a ResourceView for the given ID
     *
     * For a given ResourceView you can query the position using
     * resource.getXPosition() and resource.getYPosition()
     *
     * For a given unit you will need to find the attack damage, range and max HP
     * unitView.getTemplateView().getRange(): This gives you the attack range
     * unitView.getTemplateView().getBasicAttack(): The amount of damage this unit deals
     * unitView.getTemplateView().getBaseHealth(): The maximum amount of health of this unit
     *
     * @param state Current state of the episode
     */
    public GameState(State.StateView state) {
    	friendlyUnitIDs = state.getUnitIds(0);
    	enemyUnitIDs = state.getUnitIds(1);
    	friendlyArr = new Unit.UnitView[friendlyUnitIDs.size()];
    	enemyArr = new Unit.UnitView[enemyUnitIDs.size()];
    	int index = 0;
    	for(Integer unitID : enemyUnitIDs)
        {
            enemyArr[index] = state.getUnit(unitID);
        }
    	index = 0;
    	for(Integer unitID : friendlyUnitIDs)
        {
            friendlyArr[index] = state.getUnit(unitID);
        }

    	List<Integer> resourceIDs = state.getAllResourceIds();
        
        for(Integer resourceID : resourceIDs)
        {
            ResourceNode.ResourceView resource = state.getResourceNode(resourceID);
            resourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition()));
        }
        
        mapXExtent = state.getXExtent();
        mapYExtent = state.getYExtent();
    }

    /**
     * You will implement this function.
     *
     * You should use weighted linear combination of features.
     * The features may be primitives from the state (such as hp of a unit)
     * or they may be higher level summaries of information from the state such
     * as distance to a specific location. Come up with whatever features you think
     * are useful and weight them appropriately.
     *
     * It is recommended that you start simple until you have your algorithm working. Then watch
     * your agent play and try to add features that correct mistakes it makes. However, remember that
     * your features should be as fast as possible to compute. If the features are slow then you will be
     * able to do less plys in a turn.
     *
     * Add a good comment about what is in your utility and why you chose those features.
     *
     * @return The weighted linear combination of the features
     */
    public double getUtility() {
    	int tempMin = Integer.MAX_VALUE;
    	int runningCount = 0;
    	for (int i = 0; i < friendlyArr.length; i++) {
    		for (int j = 0; j < enemyArr.length; j++) {
    			tempMin = tempMin > getDistance(friendlyArr[i], enemyArr[j]) ? 
    					getDistance(friendlyArr[i], enemyArr[j]) : tempMin;
    		}
    		runningCount += tempMin;
    	}
        return (double) 1.0 / runningCount;
    }

    /**
     * You will implement this function.
     *
     * This will return a list of GameStateChild objects. You will generate all of the possible
     * actions in a step and then determine the resulting game state from that action. These are your GameStateChildren.
     *
     * You may find it useful to iterate over all the different directions in SEPIA.
     *
     * for(Direction direction : Directions.values())
     *
     * To get the resulting position from a move in that direction you can do the following
     * x += direction.xComponent()
     * y += direction.yComponent()
     *
     * @return All possible actions and their associated resulting game state
     */
    public List<GameStateChild> getChildren() {
    	List<GameStateChild> childNodes = new ArrayList<GameStateChild>();
    	for (Integer i : friendlyUnitIDs) {
    		Action myAct = Action.createCompoundMove(i, friendlyArr[i].getXPosition() + 1, 
    				friendlyArr[i].getXPosition());
    		//Action myAction = new Action(i, ActionType.PRIMITIVEMOVE);
    	}
    	
    	//GameStateChild child = new GameStateChild()

    	
        return null;
    }
    
    public double getXExtent()
    {	
    	return 0;
    }
    
    public double getYExtent()
    {	
    	return 0;
    }
    
    public int getDistance(Unit.UnitView unit1, Unit.UnitView unit2)
    {
    	int distance = 0;
    	distance = ((unit2.getXPosition()-unit1.getXPosition())^2 + (unit2.getYPosition()-unit1.getYPosition())^2)^(1/2);
    	return distance;
    }
}
