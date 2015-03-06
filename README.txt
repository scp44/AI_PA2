Jiawei He, jxh602
Selena Pigoni, scp44

Our .zip contains the following files:
MinimaxAlphaBeta.java
GameState.java
AstarAgent.java
This README

MinimaxAlphaBeta.java Notes:
alphaBetaSearch() is essentially an entry point for the minimax() function.
* It gets the current node's children nodes & organizes them according to those node's utility values
* Loops through the children nodes, calling minimax() on each of these nodes to get the value
* Returns the node with the highest utility value after minimax is run.

minimax() is where alpha-beta pruning search actually happens
* If the depth is reached, we return the utility of the current node
* Otherwise, get the children and order with heuristics if there are no children, we return the utility of the current node
* If it's the maximizing player, we recursively call minimax() on each of the children and eventually find the alpha value
* If it's the minimizing player, we recursively call minimax() on each of the children and eventually find the beta value
* minimax() includes pruning so that if beta <= alpha, we prune the other children

orderChildrenWithHeuristics() orders a list of children according to their utility value
* Uses insertion sort

GameState.java Notes:
getUtility() returns the utility for the given node state
* distance = (min distance from footman1 to one archer) + (min distance from footman2 to one archer)
* distance is measured with an A* search using getHopDistance()
* utility based on a function of distance

getChildren() returns the children of a given state
* If maximizing player, gets possible moves for footmen
	* gets all possible moves for footman1 and (if alive) footman2
	* Combines all possible combinations of moves
	* Includes attack as a possible move if a footman is adjacent to an archer
* If minimizing player, gets possible moves for the archer(s)
	* Works in much the same way as it does for maximizing
	* includes attack if a footman is in range
	
AstarAgent.java
* Corrected version of our PA1 assignment

getHopDistance()
* Calls A* search for the given footman to the given archer
* Returns the size of the solution (how many blocks are between the footman and archer)