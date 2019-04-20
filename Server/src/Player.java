// class to store a player's information
public class Player {

    private String choice;
    private boolean isGameWinner;

    // constructor takes an int ID of the player
    public Player(){
        choice = null;
        isGameWinner = false;
    }

    // method to return the game winner ID
    public boolean getIsGameWinner(){
        return this.isGameWinner;
    }

    // method to set the game winner ID
    public void setIsGameWinner(boolean b){
        this.isGameWinner = b;
    }

    // method to get player's choice from RPSLS
    public String getChoice() {
        return choice;
    }

    // method to set player's choice
    public synchronized void setChoice(String choice){
        this.choice = choice;
    }

}
