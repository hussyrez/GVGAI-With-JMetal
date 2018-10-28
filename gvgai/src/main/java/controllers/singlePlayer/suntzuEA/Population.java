package controllers.singlePlayer.suntzuEA;

import core.game.StateObservation;

import java.util.ArrayList;

public class Population {
    private ArrayList<Individual> population;

    public Population(){
        population = new ArrayList<>();
    }

    public Population(StateObservation stateObs, int size, int sim_depth){
        population = new ArrayList<>();
        // Create 'size' individuals with sim_depth
        for(int i = 0; i < size; i++){
            population.add(new Individual(stateObs, sim_depth));
        }
    }

    public ArrayList<Individual> getPopulation() {
        return population;
    }

    public void setPopulation(ArrayList<Individual> population) {
        this.population = population;
    }

    // Shallow copy of individual
    public Population copy(){
        Population copy = new Population();
        // shallow copy
        copy.setPopulation(new ArrayList<>(population));
        return copy;
    }

    public Individual getBest(StateObservation stateObs){
        Individual best = population.get(0);
        double best_fitness = best.getFitness();
        int i = 1;
        for(; i < population.size(); i++){
            double current_fitness = population.get(i).getFitness();
            if(best_fitness < current_fitness){
                best = population.get(i);
                best_fitness = current_fitness;
            }
        }
        return best;
    }

    // Adds an individual to the population
    public void add(Individual individual){
        population.add(individual);
    }

    public int size(){
        return population.size();
    }
}
