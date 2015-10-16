package com.example.devon.securobotslave;

import java.util.Random;

/**
 * Created by Devon on 10/16/2015.
 */
public class FundraiserEngine {
    private Random r = new Random();
    private static final String greetings[] = {
            "Hello, there.",
            "Hi, How are you today?",
            "Howdy.",
            "How's it going?",
            "Hey there.",
            "Greetings human.",
            "Hola.",
            "Hi, there.",
            "Greetings, Earthling.",
            "What's up Dog.",
            "Hello my hommie.",
            "What is up my hommie.",
            "Hey, cutie."
    };

    private static final String cause = "The Jeffery Hayzzel Count Me In Challenge";

    private static final String followups[] = {
            " Have you donated to " + cause + " Yet?",
            " Don't be cheap, donate to " + cause + "!",
            " Come support your college by donating to " + cause + "!",
            " Come on, everyone's doing it, donate to " + cause + "!",
            " Don't be a weirdo, donate to " + cause + ".",
            " Donate to " + cause + ". All the cool kids are doing it.",
            " Come on and donate to " + cause + ". Your mom donated twice.",
            " Get on Santa's nice list by donating to " + cause + ".",
            " Donate to " + cause + " to help win money for your school to spend on you.",
            " Its not that hard, just donate to " + cause + ".",
            " Donate to " + cause + ". Its so easy, even a human could do it.",
            " Would you like to see your Dean dress up as a disco dancer? Just donate to " + cause + ".",
            " Be a winner, not a looser. Donate to " + cause + "."
    };
    String link = "https://makeithappen.newhaven.edu/project/844/donate";

    public String generateGreeting() {
        int rn1 = r.nextInt(greetings.length-0);
        int rn2 = r.nextInt(followups.length-0);
        return greetings[rn1] + followups[rn2];
    }

    public String getLink() {
        return link;
    }
}
