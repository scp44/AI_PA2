package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class MinimaxAlphaBeta extends Agent {

    private final int numPlys;
    private static int infinity = 999999999;

    public MinimaxAlphaBeta(int playernum, String[] args)
    {
        super(playernum);

        if(args.length < 1)
        {
            System.err.println("You must specify the number of plys");
            System.exit(1);
        }

        numPlys = Integer.parseInt(args[0]);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
        return middleStep(newstate, statehistory);
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {
        GameStateChild bestChild = alphaBetaSearch(new GameStateChild(newstate),
                numPlys,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);

        return bestChild.action;
    }

    @Override
    public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {

    }

    @Override
    public void savePlayerData(OutputStream os) {

    }

    @Override
    public void loadPlayerData(InputStream is) {

    }

    /**
     * You will implement this.
     *
     * This is the main entry point to the alpha beta search. Refer to the slides, assignment description
     * and book for more information.
     *
     * Try to keep the logic in this function as abstract as possible (i.e. move as much SEPIA specific
     * code into other functions and methods)
     *
     * @param node The action and state to search from
     * @param depth The remaining number of plys under this node
     * @param alpha The current best value for the maximizing node from this node to the root
     * @param beta The current best value for the minimizing node from this node to the root
     * @return The best child of this node with updated values
     */
    public GameStateChild alphaBetaSearch(GameStateChild node, int depth, double alpha, double beta)
    {
    	int maxval = -infinity;
    	int childval;
    	List<GameStateChild> children;
    	//List<GameStateChild> children = orderChildrenWithHeuristics(node's children...?);

    	for(int i = 0; i < children.size(); i++)
    	{
    		childval = alphabeta(children.get(i), depth, alpha, beta, true);
    		if (childval > maxval)
    		{
    			maxval = childval;
    			node = children.get(i);
    		}
    	}
        return node;
    }
    
    public int alphabeta(GameStateChild node, int depth, double alpha, double beta, boolean maximizingPlayer)
    {
    	int v = 0;
    	if(depth == 0 /*node is terminal*/)
    	{
    		
    		//return heuristic value of node
    	}
    	if (maximizingPlayer)
    	{
    		v = -infinity;
    		//for each child of node {
    			//v = max (v, alphabeta(child, depth - 1, alpha, beta, FALSE))
    			alpha = Math.max(alpha, v);
    			//if (beta <= alpha)
    				//break
    		//}
    	}
    		
    	//else
    	else
    	{
    		v = infinity;
    		//for each child of node {
    			//v = min (v, alphaBetaSearch(child, depth - 1, alpha, beta, TRUE))
    			beta = Math.min(beta, v);
    			//if beta <= alpha
    				//break
    		//}
    	}
    	return v;
    }

    /**
     * You will implement this.
     *
     * Given a list of children you will order them according to heuristics you make up.
     * See the assignment description for suggestions on heuristics to use when sorting.
     *
     * Use this function inside of your alphaBetaSearch method.
     *
     * Include a good comment about what your heuristics are and why you chose them.
     *
     * @param children
     * @return The list of children sorted by your heuristic.
     */
    public List<GameStateChild> orderChildrenWithHeuristics(List<GameStateChild> children)
    {
        return children;
    }
}
