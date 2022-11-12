package me.raducapatina.server.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Simple logger class because I don't have time for better implementation.
 * @author Radu 21.05.2020
 */
public class Log {

    public static void message(String message, String sender){
        System.out.println(getDate() + getThread("MESSAGE") + ": " + sender + " says " + message);
    }
    public static void info(String message){
        System.out.println(getDate() + getThread("INFO") + ": " + message);
    }
    public static void warn(String message){
        System.out.println(getDate() + getThread("WARN") + ": " + message);
    }
    public static void error(String message){
        System.err.println(getDate() + getThread("ERROR") + ": " + message);
    }
    public static void fatalError(String message){
        System.err.println(getDate() + getThread("FATAL") + ": " +  message);
        System.exit(1);
    }
    private static String getDate(){

        SimpleDateFormat dt = new SimpleDateFormat("hh:mm:ss");
        return "[" + dt.format(new Date()) + "]";
    }
    private static String getThread(String Type){

        return "[" + Thread.currentThread().getName() + "/" + Type + "]";
    }

}