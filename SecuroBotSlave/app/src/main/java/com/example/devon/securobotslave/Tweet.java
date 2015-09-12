package com.example.devon.securobotslave;

import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Pattern;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

/**
 * Created by Devon on 6/13/2015.
 */
public class Tweet {
    public static final String HASHTAG_QUIZ = "securobotquiz";
    public static final String HASHTAG_TIP = "securobottip";
    public static final String HASHTAG_JOKE = "securobotjoke";
    public static final String HASHTAG_ARTICLE = "securobotarticle";
    public static final String HASHTAG_RSSFEED = "securobotrssfeed";
    public static final String TWITTER_RT = "RT";

    public static final int UNKNOWN = 0;
    public static final int SECUROBOT_QUIZ = 1;
    public static final int SECUROBOT_TIP = 2;
    public static final int SECUROBOT_JOKE = 3;
    public static final int SECUROBOT_ARTICLE = 4;
    public static final int SECUROBOT_RSSFEED = 5;
    public static final int SECUROBOT_RT = 6;

    private String tweetBy;
    private String tweet;
    private Status status;
    private String parsedContent;
    private int contentType = UNKNOWN;
    HashtagEntity hashtags [];
    private ArrayList<String> URLs = new ArrayList<String>();

    public Tweet(Status status) {
        this.status = status;
        this.tweetBy = status.getUser().getScreenName();
        this.tweet = status.getText();

        parseTweet();
        Log.d("FinalTweet", "Final Parsed " + contentType + " Content: " + getContent());
    }

    public String getTweetBy() {
        return tweetBy;
    }

    public String getTweet() {
        return tweet;
    }

    private void parseTweet() {
        /*
        Log.d("TweetParser", "Original Tweet:\n" + "Tweet By: " + tweetBy +
                "\nTweet content: " + tweet);
*/
        if(status.isRetweet()){
            Status nStatus = status.getRetweetedStatus();
            contentType = SECUROBOT_RT;
            parsedContent = nStatus.getText();
            parsedContent = removeLinks(parsedContent);
            //Log.d("TweetParser", "Found retweeted status!\n" + nStatus.getText());
        }
        else {
            hashtags = status.getHashtagEntities();
            contentType = getTweetType();
            parsedContent = removeHashtags(status.getText());
            if(contentType != SECUROBOT_ARTICLE &&
                    contentType != SECUROBOT_QUIZ &&
                    contentType != SECUROBOT_RSSFEED) {
                parsedContent = removeLinks(parsedContent);
            }
        }
    }

    private String removeHashtags(String withTags) {
        String noTags = withTags;

        //grab just the link if one of the below types
        if(contentType == SECUROBOT_QUIZ ||
                contentType == SECUROBOT_RSSFEED ||
                contentType == SECUROBOT_ARTICLE) {
            for(URLEntity l : status.getURLEntities()) {
                URLs.add(l.getURL());
                //Log.d("Parsed URL", l.getURL());
            }
            noTags = URLs.get(0);
        }
        else {
            String pattern;
            switch(contentType) {
                case SECUROBOT_TIP: pattern = "#" + HASHTAG_TIP + "\\s+"; break;
                case SECUROBOT_JOKE: pattern = "#" + HASHTAG_JOKE + "\\s+"; break;
                case SECUROBOT_RT: pattern = "#" + TWITTER_RT + "\\s+"; break;
                default: pattern=null; break;
            }

            if(pattern!=null) {
                Pattern r = Pattern.compile(pattern);

                try{
                    noTags = withTags.split(pattern)[1];
                    //Log.d("RegEx", "Parsed Status: " + noTags);
                }
                catch(Exception e) {
                    Log.d("RegEx", "Error splitting string using RegEx. Hashtags will be included in final string...");
                    noTags = status.getText();
                }
            }
            else {
                Log.d("RegEx", "Pattern is null. Hashtags will be included in final string...");
                noTags = status.getText();
            }
        }
        return noTags;
    }

    private String removeLinks(String withLinks) {
        //Log.d("RemoveLinks", "Original: " + withLinks);
        String noLinks = withLinks;
        String pattern = "http[s]?://t.co/\\w+\\S";
        Pattern r = Pattern.compile(pattern);
        try{
            noLinks = withLinks.split(pattern)[0];
            //Log.d("RemoveLinks", "W/O links: " + noLinks);
        }
        catch(Exception e) {
            Log.d("RemoveLinks", "String only consists of a link, cannot split");
            e.printStackTrace();
        }

        return noLinks;
    }

    public String getContent() {
        return parsedContent;
    }

    private int getTweetType() {
        for(HashtagEntity ht : hashtags) {
            switch(ht.getText()) {
                case HASHTAG_QUIZ: return SECUROBOT_QUIZ;
                case HASHTAG_TIP: return SECUROBOT_TIP;
                case HASHTAG_JOKE: return SECUROBOT_JOKE;
                case HASHTAG_ARTICLE: return SECUROBOT_ARTICLE;
                case HASHTAG_RSSFEED: return SECUROBOT_RSSFEED;
                default: break;
            }
        }

        return UNKNOWN;
    }

    public int getContentType() {
        return contentType;
    }
}