package com.example.devon.securobotslave;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Devon on 7/6/2015.
 */
public class JokeEngine {
    private Random r = new Random();
    private static final String intros[] = {
            "I heard this great joke the other day. ",
            "A computer friend of mine told me this funny joke. "
    };

    private static final int queueSize = 10;
    private Queue jokes = new ArrayBlockingQueue(queueSize);

    public JokeEngine() {
        jokes.add("A guy walks into a bar. Ouch.");
        /*
        jokes.add("Your computer is a joke without virus protection. You should get some virus protection");
        //from http://www.ducksters.com/jokesforkids/computer.php
        jokes.add("What did the computer do at lunchtime? Had a byte");
        jokes.add("What does a baby computer call his father? Data.");
        jokes.add("Why did the computer keep sneezing? It had a virus.");
        jokes.add("What is a computer virus? A terminal illness");
        jokes.add("Why did the computer squeak? Because someone stepped on it's mouse.");
        //http://www.ajokeaday.com/Clasificacion.asp?ID=18
        jokes.add("Computers are like air conditioners. They work fine until you start opening windows");
        jokes.add("How many programmers does it take to change a lightbulb? " +
                "None, thats a hardware problem.");
        jokes.add("Why did the spider cross the computer keyboard? To get to the World Wide Web.");*/
    }

    public String generateJoke() {
        int rn1 = r.nextInt(intros.length-0);
        int rn2 = r.nextInt(jokes.size()-0);
        Iterator iterator = jokes.iterator();
        String tmp = iterator.next().toString();
        while(rn2 > 0) {
            Log.d("Joke", tmp);
            tmp = iterator.next().toString();
            rn2--;
        }
        return intros[rn1] + tmp;
    }

    public void printContent() {
        Iterator iterator = jokes.iterator();
        while(iterator.hasNext()) {
            Log.d("Joke", iterator.next().toString());
        }
    }

    public void addContent(Queue content) {
        if(content!=null) {
            while(content.size()>0) {
                String c = content.remove().toString();
                if(!jokes.contains(c) && jokes.size()+1<queueSize) {
                    jokes.add(c);
                    Log.d("Joke", "\nJust added:\n\n" + c + "\n\nto end of queue.\n");
                }
                else {
                    String removed = jokes.remove().toString();
                    Log.d("Joke", "\nJust removed:\n\n" + removed
                            + "\n\nfrom front of queue.\n"); //if queue is at capacity, dequeue to add space for new content
                    if(!jokes.contains(c) && jokes.size()+1<queueSize) {
                        jokes.add(c);
                        Log.d("Joke", "\nJust added:\n\n" + c + "\n\nto end of queue.\n");
                    }
                }
            }
        }

        Log.d("Joke", "content:\n");
        printContent();
    }
}
