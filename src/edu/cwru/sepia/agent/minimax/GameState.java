package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.state.PlayerState;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.StateCreator;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

import java.io.IOException;
import java.util.*;

/**
 * This class stores all of the information the agent needs to know about the
 * state of the game. For example this might include things like footmen HP and
 * positions.
 *
 * Add any information or methods you would like to this class, but do not
 * delete or change the signatures of the provided methods.
 */
public class GameState {

	class MapLocation {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof MapLocation)) {
				return false;
			}
			MapLocation other = (MapLocation) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (x != other.x) {
				return false;
			}
			if (y != other.y) {
				return false;
			}
			return true;
		}

		public int x, y;

		public MapLocation(int x, int y) {
			this.x = x;
			this.y = y;
		}

		private GameState getOuterType() {
			return GameState.this;
		}
	}

	private List<Integer> friendlyUnitIDs = new ArrayList<Integer>();
	private List<Integer> enemyUnitIDs = new ArrayList<Integer>();
	private Unit.UnitView[] friendlyArr;
	private Unit.UnitView[] enemyArr;
	private HashSet<MapLocation> resourceLocations = new HashSet<MapLocation>();
	private int mapXExtent;
	private int mapYExtent;
	private State.StateView thisState;
	private StateCreator creator;

	/**
	 * You will implement this constructor. It will extract all of the needed
	 * state information from the built in SEPIA state view.
	 *
	 * You may find the following state methods useful:
	 *
	 * state.getXExtent() and state.getYExtent(): get the map dimensions
	 * state.getAllResourceIDs(): returns all of the obstacles in the map
	 * state.getResourceNode(Integer resourceID): Return a ResourceView for the
	 * given ID
	 *
	 * For a given ResourceView you can query the position using
	 * resource.getXPosition() and resource.getYPosition()
	 *
	 * For a given unit you will need to find the attack damage, range and max
	 * HP unitView.getTemplateView().getRange(): This gives you the attack range
	 * unitView.getTemplateView().getBasicAttack(): The amount of damage this
	 * unit deals unitView.getTemplateView().getBaseHealth(): The maximum amount
	 * of health of this unit
	 *
	 * @param state
	 *            Current state of the episode
	 */
	public GameState(State.StateView state) {

		// Initialize the archers and footmen arrays here
		friendlyUnitIDs = state.getUnitIds(0);
		enemyUnitIDs = state.getUnitIds(1);
		friendlyArr = new Unit.UnitView[friendlyUnitIDs.size()];
		enemyArr = new Unit.UnitView[enemyUnitIDs.size()];
		// Fill in the enemyArr with enemy UnitView objects
		int index = 0;
		for (Integer unitID : enemyUnitIDs) {
			enemyArr[index++] = state.getUnit(unitID);
		}
		// Fill in the enemyArr with friendly UnitView objects
		index = 0;
		for (Integer unitID : friendlyUnitIDs) {
			friendlyArr[index++] = state.getUnit(unitID);
		}
		/*for (int i = 0; i < 2; i++) {
			System.out.println("A footmen unit at: (" + friendlyArr[i].getXPosition() + ", " 
					+ friendlyArr[i].getYPosition() + ")");
		}
		for (int i = 0; i < 2; i++) {
			System.out.println("A archer unit at: (" + enemyArr[i].getXPosition() + ", " 
					+ enemyArr[i].getYPosition() + ")");
		}*/
		// Get the resource IDs
		List<Integer> resourceIDs = state.getAllResourceIds();
		// Store the resource locations into a HashSet (resourceLocations),
		// which encapsulates each resource
		// as a MapLocation object (with only x and y coordinates)
		for (Integer resourceID : resourceIDs) {
			ResourceNode.ResourceView resource = state
					.getResourceNode(resourceID);
			//System.out.println("Resource location at: (" + resource.getXPosition() + ", " + 
				//	resource.getYPosition() + ")");
			resourceLocations.add(new MapLocation(resource.getXPosition(),
					resource.getYPosition()));
		}
		// Get the boundaries (size) of the entire map s
		mapXExtent = state.getXExtent();
		mapYExtent = state.getYExtent();
		try {
			creator = state.getStateCreator();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * You will implement this function.
	 *
	 * You should use weighted linear combination of features. The features may
	 * be primitives from the state (such as hp of a unit) or they may be higher
	 * level summaries of information from the state such as distance to a
	 * specific location. Come up with whatever features you think are useful
	 * and weight them appropriately.
	 *
	 * It is recommended that you start simple until you have your algorithm
	 * working. Then watch your agent play and try to add features that correct
	 * mistakes it makes. However, remember that your features should be as fast
	 * as possible to compute. If the features are slow then you will be able to
	 * do less plys in a turn.
	 *
	 * Add a good comment about what is in your utility and why you chose those
	 * features.
	 *
	 * @return The weighted linear combination of the features
	 */
	public double getUtility() {
		int tempMin = Integer.MAX_VALUE;
		int runningCount = 0;
		// Calculate the minimum distance between pairs of archers/footment, and
		// take the sum of
		// those minimum values
		for (int i = 0; i < friendlyArr.length; i++) {
			for (int j = 0; j < enemyArr.length; j++) {
				tempMin = tempMin > getDistance(friendlyArr[i], enemyArr[j]) ? getDistance(
						friendlyArr[i], enemyArr[j]) : tempMin;
			}
			runningCount += tempMin;
		}
		// Return the utility of the state as the inverse of the sum of mins;
		// ie: when archers and footmen
		// are far apart, the utility is low, and vice versa
		return (double) 1.0 / runningCount;
	}

	/**
	 * You will implement this function.
	 *
	 * This will return a list of GameStateChild objects. You will generate all
	 * of the possible actions in a step and then determine the resulting game
	 * state from that action. These are your GameStateChildren.
	 *
	 * You may find it useful to iterate over all the different directions in
	 * SEPIA.
	 *
	 * for(Direction direction : Directions.values())
	 *
	 * To get the resulting position from a move in that direction you can do
	 * the following x += direction.xComponent() y += direction.yComponent()
	 *
	 * @return All possible actions and their associated resulting game state
	 */
	public List<GameStateChild> getChildren(boolean playerTurn) {
		List<GameStateChild> childNodes = new ArrayList<GameStateChild>();
		Map<Integer, Action> unitActions = new HashMap<Integer, Action>();
		if (playerTurn) {
			int firstFootmenID = friendlyUnitIDs.get(0);
			int secondFootmenID = friendlyUnitIDs.get(1);
			int footmen1X = friendlyArr[0].getXPosition();
			int footmen1Y = friendlyArr[0].getYPosition();
			int footmen2X = friendlyArr[1].getXPosition();
			int footmen2Y = friendlyArr[1].getYPosition();
			//Iterate over all the possible directions starting with the first footmen
			for (Direction direction : Direction.values()) {
				int dirX = direction.xComponent();
				int dirY = direction.yComponent();
				// A big conditional to check that the direction of the move is
				// not colliding with an obstacle,
				// is not a diagonal move (not allowed), and is within the
				// boundaries of the map
				if ((dirX == 0 || dirY == 0)
						&& !resourceLocations.contains(new MapLocation(footmen1X
								+ dirX, footmen1Y + dirY))
								&& footmen1Y + dirY >= 0 && footmen1X + dirX >= 0
								&& footmen1Y + dirY <= mapYExtent
								&& footmen1X + dirX <= mapXExtent) {
					//Create the action for footmen1 to move towards direction
					Action footAct = Action.createCompoundMove(firstFootmenID,
							footmen1X + dirX, footmen1Y + dirY);

					//An inner loop that will iterate over the moves of the second footmen
					for (Direction direction2 : Direction.values()) {
						int dir2X = direction2.xComponent();
						int dir2Y = direction2.yComponent();
						//Same as the first footmen; check that the next move is legal
						if ((dir2X == 0 || dir2Y == 0)
								&& !resourceLocations.contains(new MapLocation(footmen2X
										+ dir2X, footmen2Y + dir2Y))
										&& footmen2Y + dir2Y >= 0 && footmen2X + dir2X >= 0
										&& footmen2Y + dir2Y <= mapYExtent
										&& footmen2X + dir2X <= mapXExtent) {
							//Create the action for the second footmen to move towards direction2
							Action footAct2 = Action.createCompoundMove(secondFootmenID,
									footmen2X + dir2X, footmen2Y + dir2Y);
							unitActions.put(firstFootmenID, footAct);
							unitActions.put(secondFootmenID, footAct2);
							State childState = creator.createState();
							System.out.println("Footmen's initial position is: (" + 
									childState.getUnit(firstFootmenID).getxPosition() + ", " + 
									childState.getUnit(firstFootmenID).getyPosition() + ")");
							childState.moveUnit(childState.getUnit(firstFootmenID), direction);
							childState.moveUnit(childState.getUnit(secondFootmenID), direction2);
							
							GameState dummy = new GameState(childState.getView(0));
							System.out.println("New state's 1st footmen location: (" + 
									dummy.friendlyArr[0].getXPosition() + ", " + 
									dummy.friendlyArr[0].getYPosition() + ")");
							System.out.println("New state's 2nd footmen location: (" + 
									dummy.friendlyArr[1].getXPosition() + ", " + 
									dummy.friendlyArr[1].getYPosition() + ")");
							childNodes.add(new GameStateChild(unitActions, new GameState(childState.getView(0))));
							
							/*unitActions.put(firstFootmenID, footAct);
							unitActions.put(secondFootmenID, footAct2);
							State builder = new State();
							//footmenPlayer.addUnit();
							builder.addPlayer(0);
							builder.addPlayer(1);
							Unit footmen1 = new Unit(null, 0);*/
						}
					}
				}
			}

		}

		// GameStateChild child = new GameStateChild()

		return null;
	}

	public int getDistance(Unit.UnitView unit1, Unit.UnitView unit2) {
		int distance = 0;
		distance = ((unit2.getXPosition() - unit1.getXPosition()) ^ 2
				+ (unit2.getYPosition() - unit1.getYPosition()) ^ 2)
				^ (1 / 2);
		return distance;
	}
}
