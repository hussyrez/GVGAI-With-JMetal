package controllers.singlePlayer.GAEvolution;


import controllers.singlePlayer.Heuristics.StateHeuristic;
import controllers.singlePlayer.Heuristics.WinScoreHeuristic;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

import java.awt.*;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeoutException;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 26/02/14
 * Time: 15:17
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 *
 * The sample GA controller implements an online (rolling horizon) genetic algorithm to decide the next move to make.
 * At every game step, a (new) small population of individuals is evolved during the 40 ms given. Each individual
 * represents a sequence of actions, and its fitness is calculated with an heuristic that evaluates the state reached
 * after applying all these actions. The move returned is the first action of the best individual found.
 */
public class Agent extends AbstractPlayer {

    /* Default parameters if not set */
    private static double GAMMA = 0.90;
    private static long BREAK_MS = 5;
    private static int SIMULATION_DEPTH = 7;
    private static int POPULATION_SIZE = 5;

    private static double RECPROB = 0.1;
    private double MUT = (1.0 / SIMULATION_DEPTH);
    private final int N_ACTIONS;

    private ElapsedCpuTimer timer;

    private int genome[][][];
    private final HashMap<Integer, Types.ACTIONS> action_mapping;
    private final HashMap<Types.ACTIONS, Integer> r_action_mapping;
    protected Random randomGenerator;

    private int numSimulations;

    /**
     * Min: assignment 2
     * Get/Set methods for evolving parameters
     */
    public static double getGAMMA() {
        return GAMMA;
    }

    public static double getRECPROB() {
        return RECPROB;
    }

    public static int getPopulationSize() {
        return POPULATION_SIZE;
    }

    public static int getSimulationDepth() {
        return SIMULATION_DEPTH;
    }

    public static void setGAMMA(double newGAMMA){
        GAMMA = newGAMMA;
    }

    public static void setRECPROB(double newRECPROB){
        RECPROB = newRECPROB;
    }

    public static void setPopulationSize(int newPOPULATION_SIZE){
        POPULATION_SIZE = newPOPULATION_SIZE;
    }

    public void setSimulationDepth(int newSIMULATION_DEPTH){    //TODO, static method/static arg
        SIMULATION_DEPTH = newSIMULATION_DEPTH;
        this.MUT = (1.0 / SIMULATION_DEPTH);
    }


    /**
     * initialise parameters, this will be called before running a game.
     * Program can not be parallelled due to using static values.
     */
    public static void parameterInit(double simulation_depth, double population_size, double gamma, double recprob){
        Agent.SIMULATION_DEPTH = (int) Math.ceil(simulation_depth); //TODO: this may not be accurate enough?
        Agent.POPULATION_SIZE = (int) Math.ceil(population_size);
        Agent.GAMMA = gamma;
        Agent.RECPROB = recprob;
    }

    /**
     * Public constructor with state observation and time due.
     *
     * @param stateObs     state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        randomGenerator = new Random();

        action_mapping = new HashMap<Integer, Types.ACTIONS>();
        r_action_mapping = new HashMap<Types.ACTIONS, Integer>();
        int i = 0;
        for (Types.ACTIONS action : stateObs.getAvailableActions()) {
            action_mapping.put(i, action);
            r_action_mapping.put(action, i);
            i++;
        }

        N_ACTIONS = stateObs.getAvailableActions().size();
        initGenome(stateObs);
    }


    double microbial_tournament(int[][] actionGenome, StateObservation stateObs, StateHeuristic heuristic) throws TimeoutException {
        int a, b, c, winner, loser;
        int i;

        // Select two random parents that aren't the same
        a = (int) ((POPULATION_SIZE - 1) * randomGenerator.nextDouble());
        do {
            b = (int) ((POPULATION_SIZE - 1) * randomGenerator.nextDouble());
        } while (a == b);

        // Simulate the individual to get the score
        double score_a = simulate(stateObs, heuristic, actionGenome[a]);
        double score_b = simulate(stateObs, heuristic, actionGenome[b]);

        // Select the winning parent
        if (score_a > score_b) {
            winner = a;
            loser = b;
        } else {
            winner = b;
            loser = a;
        }

        // Set the losing parent to be the same as the winning parent
        // with probability RECPROB
        int LEN = actionGenome[0].length;
        for (i = 0; i < LEN; i++) {
            if (randomGenerator.nextDouble() < RECPROB) {
                actionGenome[loser][i] = actionGenome[winner][i];
            }
        }

        // Mutate the loser with probability MUT
        for (i = 0; i < LEN; i++) {
            if (randomGenerator.nextDouble() < MUT) actionGenome[loser][i] = randomGenerator.nextInt(N_ACTIONS);
        }

        // Return the maximum score of the parent with highest score
        return Math.max(score_a, score_b);
    }

    private void initGenome(StateObservation stateObs) {
        genome = new int[N_ACTIONS][POPULATION_SIZE][SIMULATION_DEPTH];
        // Randomize initial genome
        for (int i = 0; i < genome.length; i++) {
            for (int j = 0; j < genome[i].length; j++) {
                for (int k = 0; k < genome[i][j].length; k++) {
                    genome[i][j][k] = randomGenerator.nextInt(N_ACTIONS);
                }
            }
        }
    }

    /**
     * StateObservation stateObs: state of the game that can be copied multiple times and advanced with a given action.
     * WinStateHeuristic heuristic: heuristic used to evaluate a given state of the game.
     * int[] policy: genome of an individual of the GA, each value is an action.
     */
    private double simulate(StateObservation state, StateHeuristic heuristic, int[] policy) throws TimeoutException {

        //First, check that we have not run out of time. We set 35 ms as maximum time, to avoid overtiming and being
        //disqualified. The exception is caught in the caller of this function, to deal with the end of the algorithm.
        long remaining = timer.remainingTimeMillis();
        if (remaining < 35)
            throw new TimeoutException("Timeout");

        //Create a copy of the current state to simulate actions on it.
        state = state.copy();
        int depth = 0;
        for (; depth < policy.length; depth++) { //Go through every action in the individual.

            //Get the action according to the value in the genome
            //action_mapping is a class variable that maps integers to values of type Types.ACTIONS.
            Types.ACTIONS action = action_mapping.get(policy[depth]);

            //Advance the state with the next action.
            state.advance(action);

            if (state.isGameOver()) break; //If the game is over, no need to keep applying actions of this individual.
        }

        //Calculate the score of the state reached after applying all actions of the individual, using the heuristic.
        double score = Math.pow(GAMMA, depth) * heuristic.evaluateState(state);
        return score;
    }

    private Types.ACTIONS microbial(StateObservation stateObs, int maxdepth, StateHeuristic heuristic, int iterations) {
        // Maximum score for each possible action that we can take at the current state
        double[] maxScores = new double[stateObs.getAvailableActions().size()];

        // Set all max scores to negative_infinity for obvious reasons
        for (int i = 0; i < maxScores.length; i++) {
            maxScores[i] = Double.NEGATIVE_INFINITY;
        }

        outerloop:
        // Loop for the number of iterations, usually set to 100
        for (int i = 0; i < iterations; i++) {
            // Loop through each possible action that we can take in the current state
            for (Types.ACTIONS action : stateObs.getAvailableActions()) {
                // Copy the current state
                StateObservation stCopy = stateObs.copy();
                stCopy.advance(action);

                double score = 0;

                try {
                    // Here we compare the population of the given action
                    // E.g  In aliens there are three possible actions (LEFT, RIGHT, FIRE)
                    //      therefore we will have 3 populations for each action
                    //      the score is the best individual within that population
                    score = microbial_tournament(genome[r_action_mapping.get(action)], stCopy, heuristic) + randomGenerator.nextDouble()*0.00001;
                } catch (TimeoutException e) {
                    break outerloop;
                }

                // Update the maximum score for the current action if the score is better
                // If we don't run out of time, there will be 100 iterations which is why
                // we need to compare to what is currently there
                int int_act = this.r_action_mapping.get(action);
                if (score > maxScores[int_act]) {
                    maxScores[int_act] = score;
                }
            }
        }

        // Once 100 iterations or TIMEOUT we select the action that resulted in the highest score
        Types.ACTIONS maxAction = this.action_mapping.get(Utils.argmax(maxScores));
        return maxAction;
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     *
     * @param stateObs     Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        this.timer = elapsedTimer;
        numSimulations = 0;

        Types.ACTIONS lastGoodAction = microbial(stateObs, SIMULATION_DEPTH, new WinScoreHeuristic(stateObs), 100);
        return lastGoodAction;
    }


    @Override
    public void draw(Graphics2D g)
    {
        //g.drawString("Num Simulations: " + numSimulations, 10, 20);
    }
}
