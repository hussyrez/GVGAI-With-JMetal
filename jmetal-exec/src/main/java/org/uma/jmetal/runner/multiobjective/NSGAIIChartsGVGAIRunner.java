package org.uma.jmetal.runner.multiobjective;

import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIMeasures;
import org.uma.jmetal.measure.MeasureListener;
import org.uma.jmetal.measure.MeasureManager;
import org.uma.jmetal.measure.impl.BasicMeasure;
import org.uma.jmetal.measure.impl.CountingMeasure;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.NPointCrossover;
import org.uma.jmetal.operator.impl.mutation.SimpleRandomMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.GVGAI.GVGAI2OBJ;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.*;
import org.uma.jmetal.util.chartcontainer.ChartContainer;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.front.imp.ArrayFront;

import java.io.IOException;
import java.util.List;

/**
 * Class to configure and run the NSGA-II algorithm (variant with measures)
 */
public class NSGAIIChartsGVGAIRunner extends AbstractAlgorithmRunner {
  /**
   * @param args Command line arguments.
   * @throws SecurityException Invoking command: java org.uma.jmetal.runner.multiobjective.NSGAIIMeasuresRunner
   *                           problemName [referenceFront]
   */
  public static void main(String[] args) throws JMetalException, InterruptedException, IOException {
    Problem<DoubleSolution> problem;
    Algorithm<List<DoubleSolution>> algorithm;
    CrossoverOperator<DoubleSolution> crossover;
    MutationOperator<DoubleSolution> mutation;
    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;
    String referenceParetoFront = "";

    String problemName;
    if (args.length == 2) {
      problemName = args[0];
      referenceParetoFront = args[1];
    } else {
      problemName = "org.uma.jmetal.problem.multiobjective.GVGAI.GVGAI2OBJ";
      referenceParetoFront = "jmetal-problem/src/test/resources/pareto_fronts/GVGAI2OBJ.pf";
    }
    /* our problem */
    problem = ProblemUtils.<DoubleSolution> loadProblem(problemName);
    int[] gameIdxList = {0,1};
    ((GVGAI2OBJ) problem).setProblemArguments(gameIdxList, "0");

    double crossoverProbability = 0.3;
    //double crossoverDistributionIndex = 20.0;
    crossover = new NPointCrossover(crossoverProbability, 1);
    //crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

    double mutationProbability = 1.0 / problem.getNumberOfVariables();
    //double mutationDistributionIndex = 20.0;
    mutation = new SimpleRandomMutation(mutationProbability);
    //mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

    selection = new BinaryTournamentSelection<DoubleSolution>(
            new RankingAndCrowdingDistanceComparator<DoubleSolution>());

    int maxEvaluations = 1000;
    int populationSize = 10;

    algorithm = new NSGAIIBuilder<DoubleSolution>(problem, crossover, mutation)
            .setSelectionOperator(selection)
            .setMaxEvaluations(maxEvaluations)
            .setPopulationSize(populationSize)
            .setVariant(NSGAIIBuilder.NSGAIIVariant.Measures)
            .build();

    ((NSGAIIMeasures<DoubleSolution>) algorithm).setReferenceFront(new ArrayFront(referenceParetoFront)); //TODO, what if remove this?

    MeasureManager measureManager = ((NSGAIIMeasures<DoubleSolution>) algorithm).getMeasureManager();

        /* Measure management */
    BasicMeasure<List<DoubleSolution>> solutionListMeasure = (BasicMeasure<List<DoubleSolution>>) measureManager
            .<List<DoubleSolution>>getPushMeasure("currentPopulation");
    CountingMeasure iterationMeasure = (CountingMeasure) measureManager.<Long>getPushMeasure("currentEvaluation");
    BasicMeasure<Double> hypervolumeMeasure = (BasicMeasure<Double>) measureManager
            .<Double>getPushMeasure("hypervolume");

    ChartContainer chart = new ChartContainer(algorithm.getName(), 100);
    chart.setFrontChart(0, 1, referenceParetoFront);
    chart.addIndicatorChart("Hypervolume");
    chart.initChart();

    solutionListMeasure.register(new ChartListener(chart));
    iterationMeasure.register(new IterationListener(chart));
    hypervolumeMeasure.register(new IndicatorListener("Hypervolume", chart));
    /* End of measure management */

    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
    chart.saveChart("./chart", BitmapFormat.PNG);

    List<DoubleSolution> population = algorithm.getResult();
    long computingTime = algorithmRunner.getComputingTime();

    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

    printFinalSolutionSet(population);
    if (!referenceParetoFront.equals("")) {
      printQualityIndicators(population, referenceParetoFront);
    }
  }

  private static class IterationListener implements MeasureListener<Long> {
    ChartContainer chart;

    public IterationListener(ChartContainer chart) {
      this.chart = chart;
      this.chart.getChart("Hypervolume").setTitle("Iteration: " + 0);
    }

    @Override
    synchronized public void measureGenerated(Long iteration) {
      if (this.chart != null) {
        this.chart.getChart("Hypervolume").setTitle("Iteration: " + iteration);
      }
    }
  }

  private static class IndicatorListener implements MeasureListener<Double> {
    ChartContainer chart;
    String indicator;

    public IndicatorListener(String indicator, ChartContainer chart) {
      this.chart = chart;
      this.indicator = indicator;
    }

    @Override
    synchronized public void measureGenerated(Double value) {
      if (this.chart != null) {
        this.chart.updateIndicatorChart(this.indicator, value);
        this.chart.refreshCharts(0);
      }
    }
  }

  private static class ChartListener implements MeasureListener<List<DoubleSolution>> {
    private ChartContainer chart;
    private int iteration = 0;

    public ChartListener(ChartContainer chart) {
      this.chart = chart;
      this.chart.getFrontChart().setTitle("Iteration: " + this.iteration);
    }

    private void refreshChart(List<DoubleSolution> solutionList) {
      if (this.chart != null) {
        iteration++;
        this.chart.getFrontChart().setTitle("Iteration: " + this.iteration);
        this.chart.updateFrontCharts(solutionList);
        this.chart.refreshCharts();
      }
    }

    @Override
    synchronized public void measureGenerated(List<DoubleSolution> solutions) {
      refreshChart(solutions);
    }
  }

}
