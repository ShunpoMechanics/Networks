/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 */
public final class Flags {
    
    /**
     * Default level used for debugging print statements.
     */
    public static final Debug DEFAULT_DEBUG_LEVEL = Debug.INFO;

    // Debug level INFO: Print all messages.
    // Debug level ERROR: Print error messages only.
    public static enum Debug  {
        INFO,
        ERROR,
        NONE
    };
    

    public static void print(String msg, Debug debug) {
        if (debug == Debug.INFO && DEFAULT_DEBUG_LEVEL == Debug.INFO) {
            System.out.println(msg);
        } else if (debug == Debug.ERROR && DEFAULT_DEBUG_LEVEL == Debug.ERROR) {
            System.err.println(msg);
        }
    }

}
