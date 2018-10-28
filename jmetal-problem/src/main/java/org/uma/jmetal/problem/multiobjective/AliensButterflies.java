package org.uma.jmetal.problem.multiobjective;

import core.ArcadeMachine;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

public class AliensButterflies extends AbstractDoubleProblem {

  private static final String CONFIG_PATH = "gvgai/config/config.txt";

  public AliensButterflies() {
    this(4);
  }

  /**
   * Constructor. Creates a new ZDT2 problem instance.
   *
   * @param numberOfVariables Number of variables
   */
  public AliensButterflies(Integer numberOfVariables) {
    setNumberOfVariables(numberOfVariables);
    setNumberOfObjectives(2);
    setName("AliensButterflies");

    List<Double> lowerLimit = new ArrayList<>(getNumberOfVariables());
    List<Double> upperLimit = new ArrayList<>(getNumberOfVariables());

    // SIM_DEPTH (1,15)
    lowerLimit.add(0.01);
    upperLimit.add(0.15);
    // POP_SIZE (1,15)
    lowerLimit.add(0.02);
    upperLimit.add(0.15);
    // LAMBDA (1,5)
    // MUST BE LESS THAN THE POPULATION AND GREATER THAN 1
    lowerLimit.add(0.02);
    upperLimit.add(0.02);
    // MR (1,15)
    lowerLimit.add(0.0);
    upperLimit.add(1.0);

    setLowerLimit(lowerLimit);
    setUpperLimit(upperLimit);
  }

  public void evaluate(DoubleSolution solution) {
    double f[] = new double[getNumberOfObjectives()];
    String suntzuController = "controllers.singlePlayer.suntzuEA.Agent";
    String gamesPath = "gvgai/examples/gridphysics/";
    String games[] = new String[]{"aliens", "butterflies", "chase"};
    int gameIdx = 0;
    int levelIdx = 4; //level names from 0 to 4 (game_lvlN.txt).
    String game = gamesPath + games[gameIdx] + ".txt";
    String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx + ".txt";

    //Other settings
    int seed = new Random().nextInt();

    // Get parameter settings for controller
    Integer sim_depth = new Double(solution.getVariableValue(0) * 100).intValue();
    Integer pop_size = new Double(solution.getVariableValue(1) * 100).intValue();
    Integer lambda = new Double(solution.getVariableValue(2) * 100).intValue();
    Double mr = solution.getVariableValue(3);
    BufferedWriter bw;
    try {
      bw = new BufferedWriter(new FileWriter(CONFIG_PATH));
      bw.write(sim_depth.toString() + "\n");
      bw.write(pop_size.toString() + "\n");
      bw.write(lambda.toString() + "\n");
      bw.write(mr.toString() + "\n");
      bw.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Score is stored in return[1]
    f[0] = 1.0 / ArcadeMachine.runOneGame(game, level1, false, suntzuController, null, seed, 0)[1];
    f[0] = f[0] < 0 ? 1000.0 : f[0];
    // set up controller and game2
    game = gamesPath + games[gameIdx + 1] + ".txt";
    level1 = gamesPath + games[gameIdx + 1] + "_lvl" + levelIdx + ".txt";
    f[1] = 1.0 / ArcadeMachine.runOneGame(game, level1, false, suntzuController, null, seed, 0)[1];
    f[1] = f[1] < 0 ? 1000.0 : f[1];

    solution.setObjective(0, f[0]);
    solution.setObjective(1, f[1]);
  }
}
