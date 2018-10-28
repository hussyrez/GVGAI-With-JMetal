package controllers.singlePlayer.suntzuEA;

import ontology.Types;
import java.util.ArrayList;

public class Swap {
    /**
     * Mutates the genome via swap(i,j)
     * @param i index to swap
     * @param j index to swap
     * @param individual to mutate
     */
    public static void mutate(int i, int j, Individual individual) {
        ArrayList<Types.ACTIONS> genome = individual.getGenome();
        Types.ACTIONS temp = genome.get(i);
        genome.set(i, genome.get(j));
        genome.set(j, temp);
        individual.setGenome(genome);
    }
}
