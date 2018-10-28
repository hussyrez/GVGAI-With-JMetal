package controllers.singlePlayer.Heuristics;

import core.game.StateObservation;
import ontology.Types;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 11/02/14
 * Time: 15:44
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class WinScoreHeuristic extends StateHeuristic {

    private static final double HUGE_NEGATIVE = -1000.0;
    private static final double HUGE_POSITIVE =  1000.0;

    double initialNpcCounter = 0;

    public WinScoreHeuristic(StateObservation stateObs) {

    }

    //The StateObservation stateObs received is the state of the game to be evaluated.
    public double evaluateState(StateObservation stateObs) {
        boolean gameOver = stateObs.isGameOver(); //true if the game has finished.
        Types.WINNER win = stateObs.getGameWinner(); //player loses, wins, or no winner yet.
        double rawScore = stateObs.getGameScore(); //Neither won or lost, let's get the score and use it as fitness.

        if(gameOver && win == Types.WINNER.PLAYER_LOSES) {
            return HUGE_NEGATIVE; //We lost, this is bad.
        }

        if(gameOver && win == Types.WINNER.PLAYER_WINS) {
            return HUGE_POSITIVE; //We won, this is good.
        }

        return rawScore;
    }


}


