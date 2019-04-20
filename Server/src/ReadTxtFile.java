import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.*;

public class ReadTxtFile{

    private HashMap<Integer, String> triviaQns = new HashMap<Integer, String>();
    private Scanner x;
    private String filename = "src/questions.txt";

    //return the private hashmap
    public HashMap<Integer,String> getTriviaQnsHashMap(){
        return this.triviaQns;
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


    //Create a function to traverse the text file and extract the questions and answers and insert into a hashmap
    public void readFile(){
        int i = 1;
        while( x.hasNext()){
            String key = x.nextLine();
            triviaQns.put(i,key);
            i++;
            //Skip the answers multiple choice answers
            for(int j = 0; j < 3; j++){
                x.nextLine();
            }
        }
    }

    // create a function to close the file after opening and extracting the text from the file
    public void closeFile(){
        x.close();
    }
}
