package controllers.singlePlayer.suntzuEA;

import controllers.singlePlayer.Heuristics.StateHeuristic;
import controllers.singlePlayer.Heuristics.WinScoreHeuristic;
import core.game.StateObservation;
import ontology.Types;

import java.util.ArrayList;
import java.util.Random;

public class Individual {
    // genome == sequence of actions
    private ArrayList<Types.ACTIONS> genome;
    private double fitness;

    // creates empty individual
    public Individual(){
        genome = new ArrayList<>();
    }

    /**
     * Create an individual from state obs and current depth
     * @param stateObs contains current state observation
     * @param depth how deep we want the simulation to be
     */
    public Individual(StateObservation stateObs, int depth){
        genome = new ArrayList<>();
        ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
        int lenActions = actions.size();

        // Randomly select actions and add to genome
        Random random = new Random(System.nanoTime());
        for(int i = 0; i < depth; i++){
            int action = random.nextInt(lenActions);
            genome.add(actions.get(action));
        }

        this.updateFitness(stateObs);
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getFitness() {
        return fitness;
    }

    /**
     * Creates and returns a copy of an individual
     * @return copy of individual
     */
    public Individual copy(){
        Individual copy = new Individual();
        // Shallow copy of this.genome
        copy.setGenome(new ArrayList<>(genome));
        copy.setFitness(fitness);
        return copy;
    }

    /**
     * Sets the genome of an individual
     * @param actions new genome
     */
    public void setGenome(ArrayList<Types.ACTIONS> actions){
        genome = actions;
    }

    /**
     * Returns the phenotype of individual
     * @param stateObs current state of game
     * @return return the state reached after executing genome
     */
    public StateObservation phenotype(StateObservation stateObs){
        StateObservation stCopy = stateObs.copy();
        // Execute all events in sequence
        for(Types.ACTIONS gene : genome){
            stCopy.advance(gene);
        }
        return stCopy;
    }

    /**
     * Calculates and returns fitness of individual
     */
    public void updateFitness(StateObservation stateObs){
        StateHeuristic heuristic = new WinScoreHeuristic(stateObs);
        StateObservation phenotype = this.phenotype(stateObs);
        this.fitness = heuristic.evaluateState(phenotype);
    }

    /**
     * Returns the genome of individual
     * @return the genome of the individual
     */
    public ArrayList<Types.ACTIONS> getGenome() {
        return genome;
    }

}
