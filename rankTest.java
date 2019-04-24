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
        SCORE.add(0, 6);
        SCORE.add(1, 5);
        SCORE.add(2, 6);
        SCORE.add(3, 5);
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
            int greaterThans = 1;
            int equals = 1;
            for (int j = 0; j < n; j++) {
                if (j != i && scores.get(j) > scores.get(i)) {
                    for(int k = 0; k < gt.size(); k++){
                        if(scores.get(j) == gt.get(k)){
                            greaterThans -= 1;
                            gt.remove(scores.get(j));
                        }
                    }
                    greaterThans += 1;
                    gt.add(scores.get(j));
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


