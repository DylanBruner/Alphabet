package dylanbruner.util;

public class AlphabetLogger {
    String name;

    public AlphabetLogger(String name){this.name = name;}
    public void log(String text){System.out.println("[INFO::" + name + "] " + text);}
    public void error(String text){System.out.println("[ERROR::" + name + "] " + text);}
    public void warn(String text){System.out.println("[WARN::" + name + "] " + text);}
}
