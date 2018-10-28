package controllers.singlePlayer.multisteplookahead;

import controllers.singlePlayer.Heuristics.SimpleStateHeuristic;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

import java.util.ArrayList;
import java.util.Collections;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: group 3
 * Date: 15/09/18
 */
public class Agent extends AbstractPlayer {

    public static double epsilon = 1e-6;
    public static Random m_rnd;

    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        m_rnd = new Random();
    }

    /**
     * Very simple multi step lookahead agent.
     *
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        Types.ACTIONS bestAction = lookAhead(stateObs, elapsedTimer);
        return bestAction;
    }

    /**
     * Simple method that executes three step lookAhead
     * @param stateObs Observation of the current state
     * @param elapsedCpuTimer Timer when the action is due
     * @return Current best action found while looking ahead
     */
    private Types.ACTIONS lookAhead(StateObservation stateObs, ElapsedCpuTimer elapsedCpuTimer) {
        // The action that will be returned
        Types.ACTIONS bestAction = null;
        //Variable to store the max reward (Q) found.
        double maxQ = Double.NEGATIVE_INFINITY;
        SimpleStateHeuristic heuristic =  new SimpleStateHeuristic(stateObs);
        long remaining;
        int remainingLimit = 10;

        // Shuffle the available actions in case we dont get to visit them all
        ArrayList<Types.ACTIONS> moves = stateObs.getAvailableActions();
        Collections.shuffle(moves);
        // First step
        for (Types.ACTIONS step1 : moves) { //For all available actions.
            StateObservation stCopy = stateObs.copy();  //Copy the original state (to apply action from it)
            stCopy.advance(step1);

            // Return the best solution so far if we are at risk of going overtime
            remaining = elapsedCpuTimer.remainingTimeMillis();
            if(remaining < remainingLimit){
                return bestAction;
            }

            // Shuffle the available actions in case we dont get to visit them all
            ArrayList<Types.ACTIONS> moves2 = stCopy.getAvailableActions();
            Collections.shuffle(moves2);
            // Second step
            for(Types.ACTIONS step2 : moves2){
                StateObservation stCopy2 = stCopy.copy();
                stCopy2.advance(step2);

                // Shuffle the available actions in case we dont get to visit them all
                ArrayList<Types.ACTIONS> moves3 = stCopy2.getAvailableActions();
                Collections.shuffle(moves3);
                // third step.
                for(Types.ACTIONS step3 : moves3){
                    StateObservation stCopy3 = stCopy2.copy();
                    stCopy3.advance(step3);

                    double Q = heuristic.evaluateState(stCopy3);
                    Q = Utils.noise(Q, epsilon, m_rnd.nextDouble());

                    // Best action is still based on the first step
                    if (Q > maxQ) {
                        maxQ = Q;
                        bestAction = step1;
                    }

                    // Return the best solution so far if we are at risk of going overtime
                    remaining = elapsedCpuTimer.remainingTimeMillis();
                    if (remaining < remainingLimit) {
                        return bestAction;
                    }
                }
            }
        }
        //Return the best action found.
        return bestAction;
    }
}
