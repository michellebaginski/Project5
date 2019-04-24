// This code tests determineRanks() method I (saema) wrote in server/src/FXNet without actually having to play the game
// Just update the score in the SCORE arrayList and check the console output to see if ranks are correct
// Ranks will be store in RANK arrayList

import javafx.application.Application;
import javafx.stage.Stage;
import java.util.ArrayList;
import static javafx.application.Application.launch;

public class rankTest extends Application {
    private ArrayList<Integer> RANK = new ArrayList<>();
    private ArrayList<Integer> SCORE = new ArrayList<>();
    private int n = 4;  // numClients

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage){
        // CHANGE THE ELEMENTS TO TEST THE RANKING
        // ELEMENTS ARE SCORES
        SCORE.add(0, 0);
        SCORE.add(1, 9);
        SCORE.add(2, 1);
        SCORE.add(3, 0);
        determineRank(SCORE);
    }

    void determineRank(ArrayList<Integer> SCORE) {
        // Score Array
        ArrayList<Integer> scores = new ArrayList<>();
        // Rank Array
        ArrayList<Double> ranks = new ArrayList<Double>();

        for(int i = 0; i < n; i++){
            scores.add(i, SCORE.get(i));
        }

        for (int i = 0; i < n; i++) {
            ArrayList<Integer> gt = new ArrayList<>();
            ArrayList<Integer> e = new ArrayList<>();
            int greaterThans = 1;
            int equals = 1;
            int size = 0;
            for (int j = 0; j < n; j++) {
                if (j != i && scores.get(j) > scores.get(i)) {
                    for(int k = 0; k < size; k++){
                        if(scores.get(j) == gt.get(k)){
                            greaterThans -= 1;
                            gt.remove(scores.get(j));
                            size--;
                        }
                    }
                    greaterThans += 1;
                    gt.add(scores.get(j));
                    size++;
                }

                if (j != i && scores.get(j) == scores.get(i)) {
                    equals += 1;
                }
            }
            // Use formula to obtain rank
            ranks.add(i, greaterThans + Double.valueOf(equals - 1) / Double.valueOf(equals));
        }

        // CHECK CONSOLE TO CONFIRM RANKS ARE CORRECTLY CALCULATED
        for (int i = 0; i < n; i++) {
            double tmp = ranks.get(i);
            RANK.add(i, (int)tmp);
            System.out.println("SCORE[" + i + "] = " + SCORE.get(i) + " RANK[" + i + "] = " + RANK.get(i));
        }

    }
}


