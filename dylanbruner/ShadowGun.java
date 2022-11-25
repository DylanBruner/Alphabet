package dylanbruner;

import java.util.Hashtable;

/*
 * This gun get's it's name because that's what everyone calls it lol
 * and the idea of the gun comes from the bot called "Shadow"
*/

public class ShadowGun extends Component {
    //Components
    AlphabetLogger logger = new AlphabetLogger("ShadowGun");
    
    //Code
    Hashtable<String, Double> computedWeights = new Hashtable<String, Double>();//Used mostly for visual debugging right now

    public void execute(){ }//This might be used

    public int getRobotPlacement(String name){
        if (!computedWeights.containsKey(name)) return -1;
        //Use computed weights to determine where the robot is in the leaderboard
        
        //First store computed weights into a sorted array
        double[] sortedWeights = new double[computedWeights.size()];
        int i = 0;
        for(String key : computedWeights.keySet()){
            sortedWeights[i] = computedWeights.get(key);
            i++;
        }
        //Sort the array
        for(int j = 0; j < sortedWeights.length; j++){
            for(int k = 0; k < sortedWeights.length; k++){
                if(sortedWeights[j] > sortedWeights[k]){
                    double temp = sortedWeights[j];
                    sortedWeights[j] = sortedWeights[k];
                    sortedWeights[k] = temp;
                }
            }
        }

        //Now we have a sorted array of weights, we can use this to determine the placement of the robot
        for(int j = 0; j < sortedWeights.length; j++){
            if(computedWeights.get(name) == sortedWeights[j]){
                return j;
            }
        }
        return -1;
    }

    public Enemy getBestTarget(){
        if (alphabet.radar.enemies.size() == 0) {logger.log("No enemies"); return null; }
        //First lets make a hashtable of weights
        Hashtable<String, Double> weights = new Hashtable<String, Double>();


        //First lets add weight based on "swarm targeting" aka how many bots are around said bot
        //This is probably the most computationaly expensive part, turns out java is nothing like 
        //python and it takes 0ms with 5 bots
        for (Enemy enemy : alphabet.radar.enemies.values()) {
            if (enemy == null || !enemy.initialized) continue;
            if (enemy.name.equals(alphabet.getName())) continue; //We dont want to compare the bot to itself
            if (!enemy.alive) continue;
            double strength = 0;//Each bots distance gets translated into a "strength" which will be added to the robots weight
            
            //I really cant think of a better way to do this
            for (Enemy e2 : alphabet.radar.enemies.values()) {
                if (e2.name.equals(enemy.name)) continue; //We dont want to compare the bot to itself
                if (!enemy.alive) continue;
                if (enemy == null || !enemy.initialized) continue;

                //Add strength based on distance
                double distance = enemy.location.distance(e2.location);
                strength += 1 / distance; //The closer the bots are the more strength they get
            }
            weights.put(enemy.name, strength);

            //Add weight based on energy, lower energy = higher weight
            weights.put(enemy.name, weights.get(enemy.name) + (0.1 / enemy.energy));

            //Do stuff for based on hitting walls

            //Calculate in virtual leaderboard placement also (maybe)
        }

        computedWeights = weights;//This is just for visual debugging

        //Now we need to find the bot with the highest weight
        String bestBot = null;
        double bestWeight = 0;
        for (String name : weights.keySet()) {
            if (weights.get(name) > bestWeight) {
                //Check if any robots are between us and the target
                bestBot = name;
                bestWeight = weights.get(name);
            }
        }

        if (bestBot == null) return null;

        //We could just return a point or something but this way it's more modular
        return alphabet.radar.enemies.get(bestBot);
    }
}