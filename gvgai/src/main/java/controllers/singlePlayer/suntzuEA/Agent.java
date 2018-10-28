package controllers.singlePlayer.suntzuEA;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import java.util.Collections;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.Random;

public class Agent extends AbstractPlayer {

  private Random random;
  private Population population;
  private int sim_depth = 12;
  private int pop_size = 15;
  private int lambda = 4;
  private double mr = 0.94;

  public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedCpuTimer) {
    population = new Population(stateObs, pop_size, sim_depth);
    random = new Random(System.nanoTime());
  }

  public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedCpuTimer) {
    long remaining = elapsedCpuTimer.remainingTimeMillis();
    long timeTaken = remaining;
    population = new Population(stateObs, pop_size, sim_depth);
    Individual best = population.getPopulation().get(0);
    while (timeTaken <= remaining) {
      Population old_pop = population;
      population = Selection.elitism(stateObs, lambda, population);
      best = population.getPopulation().get(0);

      // Offspring = recombination of best two parents
      // Possible mutation
      ArrayList<Individual> pop_temp = population.getPopulation();
      ArrayList<Individual> off_spring = new ArrayList<>();
      for (int i = 0; i < (lambda / 2); i = i + 2) {
        int r = random.nextInt(sim_depth);
        off_spring.addAll(XOver.singlePoint(r, pop_temp.get(i), pop_temp.get(i + 1)));
      }

      Collections.shuffle(off_spring);
      for (Individual ind : off_spring) {
        if (population.size() < this.pop_size) {
          double pr = random.nextDouble();
          if (pr < mr) {
            int i = random.nextInt(sim_depth);
            int j = random.nextInt(sim_depth);
            Swap.mutate(i, j, ind);
          }
          ind.updateFitness(stateObs);
          population.add(ind);
        } else {
          break;
        }
      }

      // Randomly select individuals from the old population
      pop_temp = old_pop.getPopulation();
      while (population.size() < pop_size) {
        int rand = random.nextInt(pop_size);
        population.add(pop_temp.get(rand));
      }

      // Different timing depending on the game being played
      remaining = elapsedCpuTimer.remainingTimeMillis();
      timeTaken = elapsedCpuTimer.remainingTimeMillis() - timeTaken;
    }

    // Since we are using elitism, the best individual found so far will always be in the population
    // therefore we can simply get the best individual at the end of simulation and return
    // it's first action
    return best.getGenome().get(0);
  }
}
