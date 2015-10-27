package com.example.devon.securobotslave;

import java.util.Calendar;
import java.util.Random;

/**
 * Created by Devon on 7/6/2015.
 */
public class GreetingEngine {
    private Random r = new Random();
    private static final String greetings[] = {
            //"Hello, student.",
            "Hi, How are you today?",
            "Hello, would you like to learn something about cyber security?",
            "How's it going?",
            "Hey there.",
            "Come and learn something about cyber security.",
            "Hello, there.",
            "Howdy.",
            "Greetings human.",
            "Hola.",
            "Hi, there.",
            "Greetings, Earthling.",
    };

    public String generateGreeting() {
        int rn = r.nextInt(greetings.length-0);
        return greetings[rn];
    }

    public String generateInstruction() {
        return "Select an activity from my menu.";
    }
}
