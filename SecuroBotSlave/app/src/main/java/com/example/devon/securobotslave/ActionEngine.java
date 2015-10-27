package com.example.devon.securobotslave;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Random;
import java.util.TimerTask;
import android.os.Handler;
import java.util.logging.LogRecord;

/**
 * Created by Devon on 7/5/2015.
 */
public class ActionEngine{
    public TTSEngine TTSE;
    private GreetingEngine greetingE = new GreetingEngine();
    private RSSEngine RSSFeed = new RSSEngine();
    private QuizEngine quizE = new QuizEngine();
    private JokeEngine jokeE = new JokeEngine();
    private TipEngine tipE = new TipEngine();
    protected TwitterEngine twitE = new TwitterEngine();
    private FundraiserEngine fundE = new FundraiserEngine();
    private Random r = new Random();

    public static final int ACTION_TWEET = 0;
    public static final int ACTION_RSS = 1;
    public static final int ACTION_JOKE = 2;
    public static final int ACTION_QUIZ = 3;
    public static final int ACTION_PAGE = 4;
    public static final int ACTION_TIP = 5;
    public static final int ACTION_PICTURE = 6;
    public static final int ACTION_HACKED = 7;

    public ActionEngine(TTSEngine e) {
        setTTSEngine(e);
        //executeTweetSearch(false);
        executeRSS(false);
    }

    public void setTTSEngine(TTSEngine e){
        TTSE = e;
        twitE.setTTSEngine(e);
    }

    public void executeSpeech(String speech) {
        TTSE.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void executeGreeting() {
        executeSpeech(greetingE.generateGreeting());
    }

    public void executeInstruction() { executeSpeech(greetingE.generateInstruction());}

    public void executeFRGreeting() {
        executeSpeech(fundE.generateGreeting());
    }

    public void executeRandActivity() {
        int rn = r.nextInt(7-0);

        switch(rn) {
            case ACTION_TWEET: executeTimelineSearch(true); break;
            case ACTION_RSS: executeRSS(true); break;
            case ACTION_JOKE: executeJoke(); break;
            case ACTION_QUIZ: executeQuiz(); break;
            case ACTION_PAGE: executePage(); break;
            case ACTION_TIP: executeTip(); break;
            case ACTION_HACKED: executeHacked(); break;
            default: executeTimelineSearch(true); break;
        }
    }

    public void executeActivity(int ActivityType) {
        switch(ActivityType) {
            case ACTION_TWEET: executeTimelineSearch(true); break;
            case ACTION_RSS: executeRSS(true); break;
            case ACTION_JOKE: executeJoke(); break;
            case ACTION_QUIZ: executeQuiz(); break;
            case ACTION_PAGE: executePage(); break;
            case ACTION_TIP: executeTip(); break;
            case ACTION_HACKED: executeHacked(); break;
            default: Log.d("ActionEngine", "Unknown Action"); return;
        }
    }

    public void executeTweetSearch(boolean speak) {
        executeTweetSearch("cyber security", speak);
    }

    public void executeTweetSearch(String search, boolean speak) {
        try{
            twitE.searchOnTwitter(search);
            if(speak) {
                executeSpeech("I just red this on twitter.");
                twitE.speakLatestTweet();
            }
        }
        catch(Exception e) {
            Log.d("TWITTER", "Caught an exception! - execute tweet search");
            e.printStackTrace();
        }
    }

    public void executeTimelineSearch(boolean speak) {
        if(speak) {
            int rn = r.nextInt(3-0);
            if(rn == 0) {
                if(twitE.getStatusSize()>0) {
                    executeSpeech("Check out my latest status update. ");
                    Log.d("Twitter", "Status size: " + twitE.getStatusSize());
                    Log.d("Twitter", "Status: " + twitE.getLatestStatus());
                    twitE.speakLatestStatus();
                    return;
                }
                executeTimelineSearch(speak);
            }
            else if(rn == 1) {
                if(twitE.getReTweetSize()>0) {
                    executeSpeech("I just red this on twitter.");
                    twitE.speakLatestTweet();
                    return;
                }
                executeTimelineSearch(speak);
            }
            else if(rn == 2) {
                if(twitE.getRandTweetSize()>0) {
                    executeSpeech("I just saw this on twitter.");
                    twitE.speakLatestRandTweet();
                    return;
                }
                executeTimelineSearch(speak);
            }
            else {
                executeSpeech("Sorry, I didn't see anything on Twitter");
            }
        }
    }

    public void executeMakeTweet(String text) {
        //twitE.updateStatus(text);
    }

    public void executeRSS(boolean speak) {
        RSSFeed.fetchXML();

        while(RSSFeed.processing);
        if(!RSSFeed.isProcessingFailure()) {
            executeSpeech("According to this RSS feed I just red from " + RSSFeed.getAuthor() +
                    ", " + RSSFeed.getTitle());
            Log.d("RSS FEED", "Author: " + RSSFeed.getAuthor() + "\nTitle: " + RSSFeed.getTitle()
                    + "\nDescription: " + RSSFeed.getDescription());
        }
        else Log.d("RSS FEED", "RSS Failure");
    }

    public void executeJoke() {
        executeSpeech(jokeE.generateJoke());
    }

    public void executeQuiz() {
        executeSpeech("Would you like to take a quiz?");
    }

    public void executePage() {
        RSSFeed.fetchXML();

        while(RSSFeed.processing);
        if(!RSSFeed.isProcessingFailure()) {
            executeSpeech("Check out this article eye just red from " + RSSFeed.getAuthor());    //read is spelled red for phonetics same with I (eye)
            while(TTSE.t1.isSpeaking());
            Log.d("WEB PAGE", "Author: " + RSSFeed.getAuthor() + "\nLink: " + RSSFeed.getLink());
        }
    }

    public String getWebPage() {
        return RSSFeed.getLink();
    }

    public String getFRWebPage() {
        return fundE.getLink();
    }

    public String getQuiz() {
        return quizE.generateQuiz();
    }

    public void executeTip() {
        executeSpeech(tipE.generateTip());
    }

    public void fetchContent(){
        twitE.getTimeline();
        executeTweetSearch(false);
    }

    public void populateContent() {
        //twitter adds all tweet content to itself automatically
        quizE.addContent(twitE.getContent(Tweet.SECUROBOT_QUIZ));
        RSSFeed.addContent(twitE.getContent(Tweet.SECUROBOT_RSSFEED));
        jokeE.addContent(twitE.getContent(Tweet.SECUROBOT_JOKE));
        tipE.addContent(twitE.getContent(Tweet.SECUROBOT_TIP));
    }

    public void executeHacked() {
    }
}
