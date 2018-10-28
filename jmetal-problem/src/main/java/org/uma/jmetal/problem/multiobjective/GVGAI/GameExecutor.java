package org.uma.jmetal.problem.multiobjective.GVGAI;

import controllers.singlePlayer.GAEvolution.Agent;
import core.ArcadeMachine;
import org.uma.jmetal.solution.DoubleSolution;

import java.util.Random;

public class GameExecutor {

    /* Default controller:  GAEvolution based on sampleGA */
    private String controller = "controllers.singlePlayer.GAEvolution.Agent";

    /* parameters */
    private String gameName;
    private String level;
    private int seed;

    /* Full results: winState, score, gameTick */
    private double[] fullResults;

    /* getters */

    public double[] getFullResults() {
        return fullResults;
    }

    public double getScore(){
        return fullResults[1];
    }

    public double getNegScore() {return -fullResults[1];}

    public double getFitness(){
        return 1/(fullResults[0] * 100000 + fullResults[1] * 1000 - fullResults[2] * 0.1);
    }

    /**
     * Constructor, game name and level are needed.
     */
    public GameExecutor(String gameName, String levelFile, int seed, DoubleSolution solution){

        this.gameName = gameName;
        this.level = levelFile;
//        TODO: just a note here, this seed was set 0 in last assignment.
        this.seed = seed;

        //the first two are ceiled in Agent.
        Agent.parameterInit(solution.getVariableValue(0),
                            solution.getVariableValue(1),
                            solution.getVariableValue(2),
                            solution.getVariableValue(3));

        this.fullResults = ArcadeMachine.runOneGame(this.gameName, this.level, false, this.controller,
                                            null, this.seed, 0);
    }

}
