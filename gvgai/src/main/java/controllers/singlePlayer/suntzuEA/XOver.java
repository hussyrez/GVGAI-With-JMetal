package controllers.singlePlayer.suntzuEA;

import ontology.Types;

import java.util.ArrayList;

public class XOver {
    public static ArrayList<Individual> singlePoint(int p, Individual parent_1, Individual parent_2){
        // Set up of off springs
        ArrayList<Individual> off_spring = new ArrayList<>();
        Individual child_1 = new Individual();
        Individual child_2 = new Individual();
        // Set up of genomes
        ArrayList<Types.ACTIONS> genome_1 = parent_1.getGenome();
        ArrayList<Types.ACTIONS> genome_2 = parent_2.getGenome();
        ArrayList<Types.ACTIONS> mutated_genome_1 = new ArrayList<>();
        ArrayList<Types.ACTIONS> mutated_genome_2 = new ArrayList<>();

        // Performing singlePoint x over on both child genomes
        int length = genome_1.size();
        for(int i = 0; i < length; i++){
            if(i < p){
                mutated_genome_1.add(genome_1.get(i));
                mutated_genome_2.add(genome_2.get(i));
            } else {
                mutated_genome_1.add(genome_2.get(i));
                mutated_genome_2.add(genome_1.get(i));
            }
        }

        // Set genomes
        child_1.setGenome(mutated_genome_1);
        child_2.setGenome(mutated_genome_2);

        // Add off spring to array
        off_spring.add(child_1);
        off_spring.add(child_2);

        return off_spring;
    }
}
