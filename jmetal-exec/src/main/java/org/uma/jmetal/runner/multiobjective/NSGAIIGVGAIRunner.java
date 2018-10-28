package org.uma.jmetal.runner.multiobjective;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.NPointCrossover;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.mutation.SimpleRandomMutation;
import org.uma.jmetal.operator.impl.mutation.UniformMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.GVGAI.GVGAI2OBJ;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.*;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Class to configure and run the NSGA-II algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class NSGAIIGVGAIRunner extends AbstractAlgorithmRunner {
  /**
   * @param args Command line arguments.
   * @throws JMetalException
   * @throws FileNotFoundException
   * Invoking command:
    java org.uma.jmetal.runner.multiobjective.NSGAIIRunnerExercise3 problemName [referenceFront]
   */
  public static void main(String[] args) throws JMetalException, FileNotFoundException {
    Problem<DoubleSolution> problem;
    Algorithm<List<DoubleSolution>> algorithm;
    CrossoverOperator<DoubleSolution> crossover;
    MutationOperator<DoubleSolution> mutation;
    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;
    String referenceParetoFront = "" ;

    String problemName ;

    problemName = "org.uma.jmetal.problem.multiobjective.GVGAI.GVGAI2OBJ";
    referenceParetoFront = "jmetal-problem/src/test/resources/pareto_fronts/GVGAI2OBJ.pf";


    String level = "";
    int[] gameIdxList = new int[args.length-2];
    int MaxEvaluations = 1500;


    if (args.length == 4) {
      level = args[0];
      gameIdxList[0] = Integer.parseInt(args[1]);
      gameIdxList[1] = Integer.parseInt(args[2]);
      MaxEvaluations = Integer.parseInt(args[3]);


    } else if (args.length == 5) {
      level = args[0];
      gameIdxList[0] = Integer.parseInt(args[1]);
      gameIdxList[1] = Integer.parseInt(args[2]);
      gameIdxList[2] = Integer.parseInt(args[3]);
      MaxEvaluations = Integer.parseInt(args[4]);
    }else{
        // refuse others.
    }

    problem = ProblemUtils.<DoubleSolution> loadProblem(problemName);

    /* our problem */
    ((GVGAI2OBJ) problem).setProblemArguments(gameIdxList, level);

    double crossoverProbability = 0.3;
    crossover = new NPointCrossover(crossoverProbability, 1);
//    Uncomment the following 2 lines when using SBXCrossover.
//    double crossoverDistributionIndex = 20.0;
//    crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.getNumberOfVariables();
    double mutationDistributionIndex = 20.0;
    mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);
//    Uncomment the following line when using SimpleRondomMutation.
//    mutation = new SimpleRandomMutation(mutationProbability);

    selection = new BinaryTournamentSelection<DoubleSolution>(
            new RankingAndCrowdingDistanceComparator<DoubleSolution>());

    algorithm = new NSGAIIBuilder<DoubleSolution>(problem, crossover, mutation)
        .setSelectionOperator(selection)
        .setMaxEvaluations(MaxEvaluations)
        .setPopulationSize(10)
        .build() ;

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
        .execute() ;

    List<DoubleSolution> population = algorithm.getResult() ;
    long computingTime = algorithmRunner.getComputingTime() ;

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
    if (!referenceParetoFront.equals("")) {
      printQualityIndicators(population, referenceParetoFront) ;
    }

    String funName = "FUN_" + level;
    String varName = "VAR_" + level;
    for (int i = 0; i< gameIdxList.length; i++){
      funName = funName + String.valueOf(gameIdxList[i]);
      varName = varName + String.valueOf(gameIdxList[i]);
    }
//    This relative path works well on phoenix under "/fast/users/a1708302/ec/jMetal/jmetal-exec/target/classes",
//    change the path as needed in your deploying if something goes wrong with finding the files.
    try {
      copy("./FUN.tsv", "./"+funName+".tsv");
      copy("./VAR.tsv", "./"+varName+".tsv");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void copy(String sourcePath, String destinationPath) throws IOException {
    Files.copy(Paths.get(sourcePath), new FileOutputStream(destinationPath));
  }
}
