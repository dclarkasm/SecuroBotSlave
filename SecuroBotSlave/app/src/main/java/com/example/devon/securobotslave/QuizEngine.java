package com.example.devon.securobotslave;

import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Devon on 7/7/2015.
 */
public class QuizEngine{
    private Random r = new Random();
    private ArrayList<String> quizes = new ArrayList<String>();   //initial array. we add content via constructor and addContent()

    public QuizEngine() {
        quizes.add("https://www.onlineassessmenttool.com/test/assessment-26343");
    }

    public String generateQuiz() {
        int rn = r.nextInt(quizes.size() - 0);
        return quizes.get(rn);
    }

    public void printContent() {
        for(String q : quizes) {
            Log.d("Quiz", q);
        }
    }

    public void addContent(ArrayList<String> content) {
        if(content!=null) {
            for(String c : content) {
                if(!quizes.contains(c)) quizes.add(c);
            }
        }

        Log.d("Quiz", "content:\n");
        printContent();
    }
}
