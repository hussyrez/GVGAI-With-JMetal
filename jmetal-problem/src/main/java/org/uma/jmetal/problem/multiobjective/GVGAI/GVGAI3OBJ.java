package org.uma.jmetal.problem.multiobjective.GVGAI;

import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Class representing a GVGAI problem which aims to evolve the four parameters of sampleGA controller.
 * Min Su, 17/Oct/2018
 *
 *
 * These following default value and lower and upper bounds are from assignment2.
 *  // parameters to evolve
 *      private int simulation_depth;   //7
 *      private int population_size;    //5
 *      private double gamma;   //0.90
 *      private double recprob; //0.1
 *
 * // settings
 *     private static int SIMULATION_DEPTH_MIN = 5;
 *     private static int SIMULATION_DEPTH_MAX = 20;
 *     private static int POPULATION_SIZE_MIN = 5;
 *     private static int POPULATION_SIZE_MAX = 30;
 *     private static double GAMMA_MIN = 0.6;
 *     private static double GAMMA_MAX = 1.0;
 *     private static double RECPROB_MIN = 0.01;
 *     private static double RECPROB_MAX = 0.3;
 */

public class GVGAI3OBJ extends AbstractDoubleProblem {

    /* fixed settings */
    // TODO: this might be changed later on.
    private static Integer numberOfVariables = 4;
    private static int numberOfIntegerVariables = 2;
    private static int numberOfDoubleVariables = 2;

    /* objectives: maximise the combination of scores of a set of games. */
    private int numberOfObjectives;

    /* bound settings */
    private static int SIMULATION_DEPTH_MIN = 5;
    private static int SIMULATION_DEPTH_MAX = 20;
    private static int POPULATION_SIZE_MIN = 5;
    private static int POPULATION_SIZE_MAX = 30;
    private static double GAMMA_MIN = 0.6;
    private static double GAMMA_MAX = 1.0;
    private static double RECPROB_MIN = 0.01;
    private static double RECPROB_MAX = 0.3;

    private double[] lowerBounds = {SIMULATION_DEPTH_MIN, POPULATION_SIZE_MIN, GAMMA_MIN, RECPROB_MIN};
    private double[] upperBounds = {SIMULATION_DEPTH_MAX, POPULATION_SIZE_MAX, GAMMA_MAX, RECPROB_MAX};

    //controllers:
    private String sampleGAController = "controllers.singlePlayer.sampleGA.Agent";
    private String GAEvolutionController = "controllers.singlePlayer.GAEvolution.Agent";

    /* game and level setting */
    private String gamesPath = "gvgai/examples/gridphysics/";
    private String[] gameNames = {"aliens", "butterflies", "chase"};
    private String[] gameFiles;
    private String[] levelFiles;
    private String level;
    private int seed;
    private int[] gameIdx;

    /* problem args */

    public void setProblemArguments(int[] gameIdx, String level){
        //this.numberOfObjectives = (Integer) gameIdx.length;
        //this.numberOfVariables = 4;
        this.gameIdx = gameIdx;
        this.level = level;
        this.gameFiles = new String[3];
        this.levelFiles = new String[3];
        this.run();
    }

    public GVGAI3OBJ(){
        this(4);
    }

    /**
     * Creates a new instance of problem GVGAI2OBJ.
     */
    public GVGAI3OBJ(Integer numberOfVariables) {
        setNumberOfVariables(numberOfVariables);
        //setNumberOfObjectives(gameIdx.length);
        setNumberOfObjectives(3);
        setName("GVGAI3OBJ");

        List<Double> lowerLimit = new ArrayList<>(getNumberOfVariables()) ;
        List<Double> upperLimit = new ArrayList<>(getNumberOfVariables()) ;

        for (int i = 0; i < getNumberOfVariables(); i++) {
            lowerLimit.add(lowerBounds[i]);
            upperLimit.add(upperBounds[i]);
        }

        setLowerLimit(lowerLimit);
        setUpperLimit(upperLimit);
    }

    private void run(){
        /* load game settings */
        this.seed = new Random().nextInt();
        for (int i = 0; i < gameIdx.length; i++){
            this.gameFiles[i] = gamesPath + gameNames[gameIdx[i]] + ".txt";
            this.levelFiles[i] = gamesPath + gameNames[gameIdx[i]] + "_lvl" + level +".txt";
        }
    }

    /** Evaluate() method */
    public void evaluate(DoubleSolution solution) {
        double[] f = new double[getNumberOfObjectives()];
        for (int i = 0; i < this.gameFiles.length; i++){
            GameExecutor executor = new GameExecutor(this.gameFiles[i], this.levelFiles[i], this.seed, solution);
            System.out.println(this.gameNames[i] + ": level " + this.level + ". ");
            //f[i] = executor.getScore();
            //f[i] = executor.getFitness();
            f[i] = executor.getNegScore();
            solution.setObjective(i, f[i]);
        }
    }
/*
    // create solution(individual)
    public DoubleSolution createSolution(){
        return new DefaultDoubleSolution(this);
    }
    */
}

