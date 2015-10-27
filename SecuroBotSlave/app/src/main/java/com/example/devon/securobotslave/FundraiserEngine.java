package com.example.devon.securobotslave;

import android.util.Log;

import java.util.Calendar;
import java.util.Random;

/**
 * Created by Devon on 10/16/2015.
 */
public class FundraiserEngine {
    private static final int gameTime[] = {13, 0};
    private Random r = new Random();
    private static final String greetings[] = {
            /*
            "Hello, there.",
            "Hi, How are you today?",
            "Howdy.",
            "How's it going?",
            "Hey there.",
            "Greetings human.",
            "Hola.",
            "Hi, there.",
            "Greetings, Earthling.",
            "Hello my hommie.",
            "What is up my hommie.",
            "Hey, cutie.",
            "Yo yo yo.",
            "Damn girl, you look good!",
            "Wazz up!"
            */

            "Our students love you, Mr. Jeff Hazel.",
            "Thank you for supporting the University of New Haven.",
            "Give to Engineering. We play fair and we are cute!",
            "I'd love to have a photo taken with you!",
            "You don't think engineers are dull, boring and square, do you?",
            "Isn't President Kaplan awesome? And how about his wife?",
            "Thanks for inviting us to the presidential tent! These guys never get out of the lab.",
            "Who's ready for this football game!",
            "I love watching football, just dont ask me to play.",
            "I really hope we win the challenge. Ive been raising funds like its my job.",
            "Go blue!",
            "Haven't you ever seen a robot at a football game before?",
            "Don't mind me, I'm just a cute robot that enjoys going to football games.",
            "Go UNH",
            "Rah, rah Ree, Kick 'em in the knee. Rah, rah, rass, Kick 'em in the other knee.",
            "We hope you enjoy the Blue Out today!",
            "Check out our awesome marching band!",
            "*TIME*",
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
            " Be a winner, not a looser. Donate to " + cause + ".",
            " Donate to " + cause + ", and let's win this thing!",
            " You don't want to loose to the Lee college do you? Donate now to " + cause + ".",
            " Let's go get m tiger! Donate to " + cause + ".",
            " We aint gonna win if you don't play! Donate to " + cause + ".",
            " I don't like getting my ass kicked! Donate to " + cause + ".",
            " Don't hesitate, donate to " + cause + ".",
            " Show me the money! donate to " + cause + "."
    };
    String link = "https://makeithappen.newhaven.edu/project/844/donate";

    public String generateGreeting() {
        int rn1 = r.nextInt(greetings.length-0);
        //int rn2 = r.nextInt(followups.length-0);
        String greeting = greetings[rn1];
        Log.d("FRGreet", greeting);


        if(greeting.equalsIgnoreCase("*TIME*")) {
            Calendar c = Calendar.getInstance();
            int hrs = c.get(Calendar.HOUR_OF_DAY);
            int min = c.get(Calendar.MINUTE);

            int minsHrsTil = (60 * (gameTime[0] - hrs)) + gameTime[1] - min;
            int minTil = minsHrsTil%60;
            double div = minsHrsTil/60;
            int hrsTil = (int)Math.floor(div);


            if(hrsTil<=0 && minTil<=0) {
                greeting = "Its game time!";
            }
            else if(hrsTil<=0) {
                greeting = "Just " + minTil + " minutes until kickoff!";
            }
            else if(minTil<=0) {
                greeting = "Just " + hrsTil + " hours until kickoff!";
            }
            else {
                greeting = "Just " + hrsTil + " hours and " + minTil + " minutes until kickoff!";
            }
        }

        return greeting;
    }

    public String getLink() {
        return link;
    }
}
