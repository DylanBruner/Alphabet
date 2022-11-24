package dylanbruner;

import java.util.Hashtable;

/*
 * NOTE: I dont think this gun will be included in the virtual gun manager simply because it's quite comutationally expensive
 * and I dont want to be running it all the time. So i think we will just default to it in melee and resume normal gun switching
 * in 1v1
 *
 * This gun get's it's name because that's what everyone calls it lol
 * and the idea of the gun comes from the bot called "Shadow"
*/

public class ShadowGun {
    //Components
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("ShadowGun");
    
    //Code
    Hashtable<String, Double> computedWeights = new Hashtable<String, Double>();//Used mostly for visual debugging right now

    public void init(Alphabet alphabet){
        this.alphabet = alphabet;
    }

    public void execute() {}


    public Enemy getBestTarget(){
        if (alphabet.radar.enemies.size() == 0) {logger.log("No enemies"); return null; }
        //First lets make a hashtable of weights
        Hashtable<String, Double> weights = new Hashtable<String, Double>();


        //First lets add weight based on "swarm targeting" aka how many bots are around said bot
        //This is probably the most computationaly expensive part, turns out java is nothing like 
        //python and it takes 0ms with 5 bots
        for (Enemy enemy : alphabet.radar.enemies.values()) {
            if (enemy.name.equals(alphabet.getName())) continue; //We dont want to compare the bot to itself
            if (!enemy.alive) continue;
            double strength = 0;//Each bots distance gets translated into a "strength" which will be added to the robots weight
            
            //I really cant think of a better way to do this
            for (Enemy e2 : alphabet.radar.enemies.values()) {
                if (e2.name.equals(enemy.name)) continue; //We dont want to compare the bot to itself

                //Add strength based on distance
                double distance = enemy.location.distance(e2.location);
                strength += 1 / distance; //The closer the bots are the more strength they get
            }
            weights.put(enemy.name, strength);

            //Add weight based on energy, lower energy = higher weight
            weights.put(enemy.name, weights.get(enemy.name) + (0.1 / enemy.energy));

            //Do stuff for based on hitting walls

            //Calculate in virtual leaderboard placement also
        }

        computedWeights = weights;//This is just for visual debugging

        //Now we need to find the bot with the highest weight
        String bestBot = null;
        double bestWeight = 0;
        for (String name : weights.keySet()) {
            if (weights.get(name) > bestWeight) {
                bestBot = name;
                bestWeight = weights.get(name);
            }
        }

        //We could just return a point or something but this way it's more modular
        return alphabet.radar.enemies.get(bestBot);
    }
}