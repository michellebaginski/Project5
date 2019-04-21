import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.*;

public class ReadTxtFile{

    private HashMap<String, ArrayList<String>> triviaQns = new HashMap<String, ArrayList<String>>();
    private ArrayList<String> answersArray = new ArrayList<String>();
    private Scanner x;
    private String filename = "src/questions.txt";
    private String answersFilename ="src/answers.txt";

    //return the private hashmap
    public HashMap<String,ArrayList<String>> getTriviaQnsHashMap(){
        return this.triviaQns;
    }
    //return the private ArrayList answers
    public ArrayList<String> getAnswersArray(){return this.answersArray; }
    public void openFile(){
        try{
            x = new Scanner(new File(filename));
        }
        catch (Exception e){
            System.out.println("could not find file");
            e.printStackTrace();
        }
    }

    //Create an instance of an ArrayList and add multiple choice answers to the ArrayList and return the lsit
    public ArrayList<String> getMCAnswers(){
        ArrayList<String> multiAnswers = new ArrayList<>();
        String answer;
        for (int i = 0; i < 3; i++){
            answer = x.nextLine();
            multiAnswers.add(answer);
        }
        return multiAnswers;
    }

    //Create a function to traverse the text file and extract the questions and answers and insert into a hashmap
    public void readFile(){
        while( x.hasNext()){
            String key = x.nextLine();
            ArrayList<String> multiAnswers = getMCAnswers();
            triviaQns.put(key,multiAnswers);
        }
    }

    public void getAnswersTxt(){
        try {
            Scanner scanner = new Scanner(new File(answersFilename));
            String answerInfo;
            while(scanner.hasNext()){
                answerInfo = scanner.nextLine();
                answersArray.add(answerInfo);
            }
            scanner.close();

            for(int i = 0; i < answersArray.size(); i++){
                System.out.println(answersArray.get(i));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // create a function to close the file after opening and extracting the text from the file
    public void closeFile(){
        x.close();
    }
}
