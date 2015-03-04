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
	
	
	class UnitState {
		private int xPosition, yPosition, unitHP, ID, basicAtt, range;
		private boolean isDead;
		private String type;
		public UnitState(int x, int y, int hp, int id, boolean dead, int basicAttack, int range, String type) {
			this.xPosition = x;
			this.yPosition = y;
			this.unitHP = hp;
			this.ID = id;
			this.isDead = dead;
			this.basicAtt = basicAttack;
			this.range = range;
			this.type = type;
		}
	}

	private List<Integer> friendlyUnitIDs = new ArrayList<Integer>();
	private List<Integer> enemyUnitIDs = new ArrayList<Integer>();
	private HashSet<MapLocation> resourceLocations = new HashSet<MapLocation>();
	private int mapXExtent;
	private int mapYExtent;
	//units[0] and units[1] are footmen; units[2] and units [3] are archers
	private UnitState[] units = new UnitState[4];
	private int numArchers; //We always have 2 footmen, only the number of archers will change, so store it 

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
		numArchers = enemyUnitIDs.size();
		// Fill the units array with UnitState objects that tracks the stats of each unit (archers)
		int index = 0;
		for (Integer unitID : friendlyUnitIDs) {
			units[index++] = new UnitState(state.getUnit(unitID).getXPosition(), state.getUnit(unitID).getYPosition(),
					state.getUnit(unitID).getHP(), state.getUnit(unitID).getID(), state.getUnit(unitID).getHP() <= 0,
					state.getUnit(unitID).getTemplateView().getBasicAttack(), 
					state.getUnit(unitID).getTemplateView().getRange(), "footman");
		}
		// Fill the units array with UnitState objects that tracks the stats of each unit (footmen)
		for (Integer unitID : enemyUnitIDs) {
			units[index++] = new UnitState(state.getUnit(unitID).getXPosition(), state.getUnit(unitID).getYPosition(),
					state.getUnit(unitID).getHP(), state.getUnit(unitID).getID(), state.getUnit(unitID).getHP() <= 0, 
					state.getUnit(unitID).getTemplateView().getBasicAttack(), 
					state.getUnit(unitID).getTemplateView().getRange(), "archers");
		}

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

	}
	
	public GameState(GameState newState) {
		//Initializes the new GameState object with the same fields as the one passed in
		//Basically making a copy of a GameState object
		this.friendlyUnitIDs = newState.friendlyUnitIDs;
		this.enemyUnitIDs = newState.enemyUnitIDs;

		this.resourceLocations = newState.resourceLocations;
		this.mapXExtent = newState.mapXExtent;
		this.mapYExtent = newState.mapYExtent;
		this.units = newState.units;
		this.numArchers = newState.numArchers;
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
		int distanceMetric = 0;
		// Calculate the minimum distance between pairs of archers/footmen, and
		// take the sum of
		// those minimum values
		for (int i = 0; i < 2; i++) {
			for (int j = 2; j < 2 + numArchers; j++) {
				tempMin = tempMin > getDistance(units[i], units[j]) ? getDistance(
						units[i], units[j]) : tempMin;
			}
			distanceMetric += tempMin;
		}
		
		//10 * (-dist) + (footmenHP - archerHP)
		
		// Return the utility of the state as the inverse of the sum of mins;
		// ie: when archers and footmen
		// are far apart, the utility is low, and vice versa
		int hpMetric = 0;
		for (int j = 0; j < 2 + numArchers; j++) {
			if (units[j].type.equals("footmen")) {
				hpMetric += units[j].unitHP;
			}
			else
				hpMetric -= units[j].unitHP;
		}
		return hpMetric - distanceMetric;
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
		GameState childState = new GameState(this);
		//If it's the player's turn (our turn), we look at all the possible footmen moves
		if (playerTurn) {
			int firstFootmenID = friendlyUnitIDs.get(0);
			int secondFootmenID = friendlyUnitIDs.get(1);
			//Get the coordinates of the 2 footmen
			int footmen1X = units[0].xPosition;
			int footmen1Y = units[0].yPosition;
			int footmen2X = units[1].xPosition;
			int footmen2Y = units[1].yPosition;
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
					Action footAct;
					//First check if the footmen is next to an archer, if it is, then always attack
					if (isAdjacent(this.units[0], this.units[2]) || numArchers == 2 && isAdjacent(this.units[0], this.units[3])) {
						//If adjacent to the first enemy archer
						if (isAdjacent(this.units[0], this.units[2])) {
							footAct = Action.createPrimitiveAttack(firstFootmenID, enemyUnitIDs.get(0));
							//Subtract from the archer's hp the basic attack damage of the footmen
							childState.units[2].unitHP -= childState.units[0].basicAtt;
						}
						//Otherwise adjacent to the second enemy archer; Note, if adjacent to both archers,
						//then the footman will still attack the first one
						else 
							footAct = Action.createPrimitiveAttack(firstFootmenID, enemyUnitIDs.get(1));
							childState.units[3].unitHP -= childState.units[0].basicAtt;
					}
					//If not close enough to attack, then create the action for footmen1 to move towards direction
					else {
						footAct = Action.createCompoundMove(firstFootmenID,
								footmen1X + dirX, footmen1Y + dirY);
					}

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
							Action footAct2;
							//First check if the footmen is next to an archer, if it is, then always attack
							if (isAdjacent(this.units[1], this.units[2]) || numArchers == 2 && isAdjacent(this.units[1], this.units[3])) {
								//If adjacent to the first enemy archer
								if (isAdjacent(this.units[1], this.units[2])) {
									footAct2 = Action.createPrimitiveAttack(secondFootmenID, enemyUnitIDs.get(0));
									childState.units[2].unitHP -= childState.units[1].basicAtt;
								}
								//Otherwise adjacent to the second enemy archer; Note, if adjacent to both archers,
								//then the footman will still attack the first one
								else 
									footAct2 = Action.createPrimitiveAttack(secondFootmenID, enemyUnitIDs.get(1));
									childState.units[3].unitHP -= childState.units[1].basicAtt;
							}
							//If not close enough to attack, then create the action for footmen1 to move towards direction
							else {
								footAct2 = Action.createCompoundMove(secondFootmenID,
										footmen2X + dir2X, footmen2Y + dir2Y);
							}
							unitActions.put(firstFootmenID, footAct);
							unitActions.put(secondFootmenID, footAct2);
		
							
							//Manually change the positions of the footmen via the UnitState object
							childState.units[0].xPosition = footmen1X + dirX;
							childState.units[0].yPosition = footmen1Y + dirY;
							childState.units[1].xPosition = footmen2X + dir2X;
							childState.units[1].yPosition = footmen2Y + dir2Y;
							childNodes.add(new GameStateChild(unitActions, childState));
						}
					}
				}
			}
			return childNodes;
		}

		//Otherwise, we are getting the child nodes of an archer
		else {
			int firstArcherID = enemyUnitIDs.get(0);
			//Get the coordinates of the 2 archers
			int archer1X = units[2].xPosition;
			int archer1Y = units[2].yPosition;
			//Iterate over all the possible directions starting with the first archer
			for (Direction direction : Direction.values()) {
				int dirX = direction.xComponent();
				int dirY = direction.yComponent();
				// A big conditional to check that the direction of the move is
				// not colliding with an obstacle,
				// is not a diagonal move (not allowed), and is within the
				// boundaries of the map
				if ((dirX == 0 || dirY == 0)
						&& !resourceLocations.contains(new MapLocation(archer1X
								+ dirX, archer1Y + dirY))
								&& archer1Y + dirY >= 0 && archer1X + dirX >= 0
								&& archer1Y + dirY <= mapYExtent
								&& archer1X + dirX <= mapXExtent) {
					Action archerAct;
					if (isAdjacent(this.units[0], this.units[2]) || isAdjacent(this.units[1], this.units[2])) {
						//If adjacent to the first enemy archer
						if (isAdjacent(this.units[0], this.units[2])) {
							archerAct = Action.createPrimitiveAttack(firstArcherID, friendlyUnitIDs.get(0));
							childState.units[0].unitHP -= childState.units[2].basicAtt;
						}
						//Otherwise adjacent to the second enemy archer; Note, if adjacent to both archers,
						//then the footman will still attack the first one
						else 
							archerAct = Action.createPrimitiveAttack(firstArcherID, friendlyUnitIDs.get(1));
							childState.units[1].unitHP -= childState.units[3].basicAtt;
					}
					//Create the action for footmen1 to move towards direction
					else {
						archerAct = Action.createCompoundMove(firstArcherID,
								archer1X + dirX, archer1Y + dirY);
					}
							

					//An inner loop that will iterate over the moves of the second archer (if it exists)
					if (numArchers > 1) {
						int secondArcherID = friendlyUnitIDs.get(1);
						int archer2X = units[3].xPosition;
						int archer2Y = units[3].yPosition;
						for (Direction direction2 : Direction.values()) {
							int dir2X = direction2.xComponent();
							int dir2Y = direction2.yComponent();
							//Same as the first footmen; check that the next move is legal
							if ((dir2X == 0 || dir2Y == 0)
									&& !resourceLocations.contains(new MapLocation(archer2X
											+ dir2X, archer2Y + dir2Y))
											&& archer2Y + dir2Y >= 0 && archer2X + dir2X >= 0
											&& archer2Y + dir2Y <= mapYExtent
											&& archer2X + dir2X <= mapXExtent) {
								Action archerAct2;
								if (isAdjacent(this.units[0], this.units[3]) || isAdjacent(this.units[1], this.units[3])) {
									//If adjacent to the first enemy archer
									if (isAdjacent(this.units[0], this.units[3])) {
										archerAct2 = Action.createPrimitiveAttack(secondArcherID, friendlyUnitIDs.get(0));
									}
									//Otherwise adjacent to the second enemy archer; Note, if adjacent to both archers,
									//then the footman will still attack the first one
									else 
										archerAct2 = Action.createPrimitiveAttack(secondArcherID, friendlyUnitIDs.get(1));
								}
								//Create the action for footmen1 to move towards direction
								else {
									archerAct2 = Action.createCompoundMove(firstArcherID,
											archer1X + dirX, archer1Y + dirY);
								}
								unitActions.put(firstArcherID, archerAct);
								unitActions.put(secondArcherID, archerAct2);

								//Manually change the positions of the footmen via the UnitState object
								childState.units[0].xPosition = archer1X + dirX;
								childState.units[0].yPosition = archer1Y + dirY;
								childState.units[1].xPosition = archer2X + dir2X;
								childState.units[1].yPosition = archer2Y + dir2Y;
								childNodes.add(new GameStateChild(unitActions, childState));
							}
						}
					}
				}
			}
			return childNodes;
		}

		// GameStateChild child = new GameStateChild()

		
	}

	public int getDistance(UnitState unit1, UnitState unit2) {
		int distance = 0;
		distance = ((unit2.xPosition - unit1.xPosition) ^ 2
				+ (unit2.yPosition - unit1.yPosition) ^ 2)
				^ (1 / 2);
		return distance;
	}
	
	public boolean isAdjacent(UnitState unit1, UnitState unit2) {
		return (Math.abs(unit1.xPosition - unit2.xPosition) == 1 || Math.abs(unit1.yPosition - unit2.yPosition) == 1);
	}
}
