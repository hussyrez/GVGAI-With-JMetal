import core.ArcadeMachine;

/**
 * Driver class for exercise 1
 */
public class ExerciseOneDriver {

    public static void main(String [] args){
        // sample controllers for exercise 1
        String sampleRandomController = "controllers.singlePlayer.sampleRandom.Agent";
        String sampleOneStepController = "controllers.singlePlayer.sampleonesteplookahead.Agent";
        String sampleGAController = "controllers.singlePlayer.sampleGA.Agent";
        String twoStepController = "controllers.singlePlayer.twosteplookahead.Agent";

        // Games for exercise 1
        String gamesPath = "examples/gridphysics/";
        String games[] = new String[]{"aliens", "boulderdash", "butterflies", "chase"};
        String controllers[] = new String[]{sampleRandomController, sampleOneStepController,
                                            sampleGAController, twoStepController};
        //Game and level to play
        int levelIdx = 0;

        // Plays all games 5 times each, on level1
        for(int gameIdx = 0; gameIdx < 4; gameIdx++) {
            String game = gamesPath + games[gameIdx] + ".txt";
            String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx +".txt";
            // Run each controller 5 times on each game
            for(String controller : controllers) {
                ArcadeMachine.runGames(game, new String[]{level1}, 5, controller, null, 1);
            }
        }
    }
}
