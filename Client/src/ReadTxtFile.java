import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.*;

public class ReadTxtFile{

    // key: question | value: array index of 0, 1, 2
    private HashMap<String, ArrayList<String>> QtoAmap = new HashMap<String, ArrayList<String>>();
    private HashMap<String, Integer> questionNum = new HashMap<String, Integer>();
    private Scanner x;
    private String filename = "src/questions.txt";
    private ArrayList<String> answersArray = new ArrayList<String>();
    private String answersFilename ="src/answers.txt";

    //return the private ArrayList answers
    public ArrayList<String> getAnswersArray(){
        return this.answersArray;
    }

    //return the private hashmap
    public HashMap<String,ArrayList<String>> getQtoAmap(){
        return this.QtoAmap;
    }

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
        int picNum = 1;
        while( x.hasNext()){
            String key = x.nextLine();
            ArrayList<String> multiAnswers = getMCAnswers();
            QtoAmap.put(key,multiAnswers);
            questionNum.put(key, picNum);
            picNum++;
        }

        for (HashMap.Entry<String, Integer> entry : questionNum.entrySet()) {
            System.out.println("Key: "+entry.getKey());
            int val = entry.getValue();
            System.out.println("Value: " + entry.getValue());
        }
    }

    // create a function to close the file after opening and extracting the text from the file
    public void closeFile(){
        x.close();
    }

    public HashMap<String, Integer> getQuestionNum() {
        return this.questionNum;
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

}
