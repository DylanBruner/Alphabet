package dylanb;

import java.awt.Color;

//The most unnecessary class in this entire robot

public class Themer {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("Themer");

    Color mintGreen = new Color(152, 251, 152);
    Color bloodRed  = new Color(120, 0, 0);
    Color tomatoRed = new Color(255, 99, 71);

    public final Color[][] BASE_THEMES = {
        {Color.black, Color.blue, Color.red},
        //{Color.pink, Color.pink, Color.white},//Stolen from SandboxDT
        //{Color.black, Color.black, new Color(255, 255, 170)}//Stolen from Diamond
    }; 

    public void init(Alphabet alphabet){
        this.alphabet = alphabet;

        //Set random theme each round
        int theme = (int)(Math.random() * BASE_THEMES.length);
        alphabet.setColors(BASE_THEMES[theme][0], BASE_THEMES[theme][1], BASE_THEMES[theme][2]);
    }

    //setColors(Color, Color, Color)
    // setColors(Color, Color, Color, Color, Color)
    // setAllColors(Color)
    // setBodyColor(Color)
    // setGunColor(Color)
    // setBulletColor(Color)
    // setScanColor(Color)

    public void execute(){
        if (alphabet.getOthers() > 1){
            if (alphabet.getTime() % 10 == 0 && alphabet.getRoundNum() != 0){
                int myPlacement = Alphabet.vLeaderboard.getMyPlacement();
                if (myPlacement == 1){
                    //Change base color to mint-green
                    alphabet.setBodyColor(mintGreen);
                } else {
                    alphabet.setBodyColor(bloodRed);
                }
            }
        } else {
            alphabet.setColors(tomatoRed, tomatoRed, Color.white);
        }
    }
}
