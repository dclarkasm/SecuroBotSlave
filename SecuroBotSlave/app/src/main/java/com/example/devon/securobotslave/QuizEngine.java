package com.example.devon.securobotslave;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Devon on 7/7/2015.
 */
public class QuizEngine{
    private Random r = new Random();
    private static final int queueSize = 10;
    private Queue quizes = new ArrayBlockingQueue(queueSize);   //initial array. we add content via constructor and addContent()

    public QuizEngine() {
        quizes.add("https://www.onlinequizcreator.com/cyber-quiz-v1/quiz-115974");
    }

    public String generateQuiz() {
        int rn = r.nextInt(quizes.size() - 0);
        Iterator iterator = quizes.iterator();
        String tmp = iterator.next().toString();
        while(rn > 0) {
            Log.d("Quiz", tmp);
            tmp = iterator.next().toString();
            rn--;
        }
        Log.d("Quiz", "Generated Quiz: " + tmp);
        return tmp;
    }

    public void printContent() {
        Iterator iterator = quizes.iterator();
        while(iterator.hasNext()) {
            Log.d("Quiz", iterator.next().toString());
        }
    }

    public void addContent(Queue content) {
        if(content!=null) {
            while(content.size()>0) {
                String c = content.remove().toString();
                if(!quizes.contains(c) && quizes.size()+1<queueSize) {
                    quizes.add(c);
                    Log.d("Quiz", "\nJust added:\n\n" + c + "\n\nto end of queue.\n");
                }
                else {
                    String removed = quizes.remove().toString();
                    Log.d("Quiz", "\nJust removed:\n\n" + removed
                    + "\n\nfrom front of queue.\n"); //if queue is at capacity, dequeue to add space for new content
                    if(!quizes.contains(c) && quizes.size()+1<queueSize) {
                        quizes.add(c);
                        Log.d("Quiz", "\nJust added:\n\n" + c + "\n\nto end of queue.\n");
                    }
                }
            }
        }

        Log.d("Quiz", "content:\n");
        printContent();
    }
}
