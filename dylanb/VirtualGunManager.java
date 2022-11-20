package dylanb;

public class VirtualGunManager {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("VirtualGunManager");
    
    public void init(Alphabet alphabet){
        this.alphabet = alphabet;
        logger.log("VirtualGunManager initialized");
    }
}
