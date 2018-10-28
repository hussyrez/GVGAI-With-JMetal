package controllers.singlePlayer.suntzuEA;

import core.game.StateObservation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Selection {
    /**
     * Selects the best individuals from a population
     * @param stateObs current state observation of game, used for fitness evaluation
     * @param lambda number of individuals to select
     * @param population population to select from
     * @return elite population
     */
    public static Population elitism(StateObservation stateObs, int lambda, Population population){
        Population elite = new Population();
        ArrayList<Individual> pop = population.getPopulation();
        pop.sort(new Comparator<Individual>() {
            @Override
            public int compare(Individual o1, Individual o2) {
                double f1 = o1.getFitness();
                double f2 = o2.getFitness();
                if(f1 - f2 < 0){
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        // Read from end, since it is in ascending order
        for(int i = 1; i <= lambda; i++){
            elite.add(pop.get(pop.size() - i));
        }

        return elite;
    }
}
