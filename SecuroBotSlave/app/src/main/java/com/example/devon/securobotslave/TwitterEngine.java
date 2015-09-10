package com.example.devon.securobotslave;

import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Devon on 7/5/2015.
 */
public class TwitterEngine {
    public TTSEngine engine;
    /*
    private ArrayList<String> parsedReTweets = new ArrayList<String>(); //list of the most recent retweets for speaking (parsed)
    private ArrayList<String> parsedTips = new ArrayList<String>();
    private ArrayList<String> parsedQuizLinks = new ArrayList<String>();
    private ArrayList<String> parsedRSSLinks = new ArrayList<String>();
    private ArrayList<String> parsedJokes = new ArrayList<String>();
    private ArrayList<String> parsedArticleLinks = new ArrayList<String>();
    private ArrayList<Tweet> parsedRandTweets = new ArrayList<Tweet>(); //list of the most recent random tweets for speaking (parsed)
    private ArrayList<Tweet> parsedStatuses = new ArrayList<Tweet>(); //list of the most recent status updates for speaking (parsed)
    */
    private Queue parsedTips = new LinkedList();
    private Queue parsedQuizLinks = new LinkedList();
    private Queue parsedRSSLinks = new LinkedList();
    private Queue parsedJokes = new LinkedList();
    private Queue parsedArticleLinks = new LinkedList();
    private Queue parsedReTweets = new LinkedList(); //list of the most recent retweets for speaking (parsed)
    private Queue parsedRandTweets = new LinkedList(); //list of the most recent random tweets for speaking (parsed)
    private Queue parsedStatuses = new LinkedList(); //list of the most recent status updates for speaking (parsed)
    private String latestStatus;
    public Twitter twitter;
    private static final String TWITTER_KEY = "JlxXwwVxSH8KuiqIktrNE2VQp";
    private static final String TWITTER_SECRET = "4m1kuoWKOrHDLX7CulAs6uAzEKpjFUWUkweWFunQCXlZCVpGXm";
    private static final String TWITTER_TOKEN = "3364737443-ilf4qCoDyaKcsD5fZME80qGpwmfMiv1yDgMaoJM";
    private static final String TOKEN_SECRET = "YLZbvOwFOSa6akO50Pur3aTS059QTl5qUL4c8BScwHKA6";
    private boolean contentFetched = false;

    public TwitterEngine() {
        Log.d("Twitter", "Initializing Twitter...");
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(TWITTER_KEY)
                .setOAuthConsumerSecret(TWITTER_SECRET)
                .setOAuthAccessToken(TWITTER_TOKEN)
                .setOAuthAccessTokenSecret(TOKEN_SECRET);
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
        Log.d("Twitter", "Twitter initialized");
    }

    public void searchOnTwitter(String text) {  //for searching for random things on twitter
        new FetchRandomTweets().execute(text);
    }

    public boolean getContentIsFetched() {
        return contentFetched;
    }

    public void setContentFetched(boolean status) {
        contentFetched = status;
    }

    public void getTimeline() {
        new FetchTimeline().execute("");
    }

    private class FetchTimeline extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                User user = twitter.verifyCredentials();
                List<twitter4j.Status> statuses = twitter.getHomeTimeline();
                Log.d("Twitter", "Showing @" + user.getScreenName() + "'s home timeline.");
                for (twitter4j.Status status : statuses) {
                    Log.d("Twitter", "@" + status.getUser().getScreenName() + " - " + status.getText());
                    Tweet newTweet = new Tweet(status);
                    String tweetContent = newTweet.getContent();
                    if(tweetContent!=null) {
                        parsedStatuses.add(tweetContent);
                        Log.d("Statuses", "newTweetAdded: " + tweetContent);
                        switch(newTweet.getContentType()) {
                            case Tweet.SECUROBOT_ARTICLE:
                                if(!parsedArticleLinks.contains(tweetContent)) {
                                    parsedArticleLinks.add(tweetContent);
                                }
                                break;
                            case Tweet.SECUROBOT_JOKE:
                                if(!parsedJokes.contains(tweetContent)){
                                    parsedJokes.add(tweetContent);
                                }
                                break;
                            case Tweet.SECUROBOT_QUIZ:
                                if(!parsedQuizLinks.contains(tweetContent)) {
                                    parsedQuizLinks.add(tweetContent);
                                }
                                break;
                            case Tweet.SECUROBOT_RSSFEED:
                                if(!parsedRSSLinks.contains(tweetContent)) {
                                    parsedRSSLinks.add(tweetContent);
                                }
                                break;
                            case Tweet.SECUROBOT_TIP:
                                if(!parsedTips.contains(tweetContent)) {
                                    parsedTips.add(tweetContent);
                                }
                                break;
                            case Tweet.SECUROBOT_RT:
                                if(!parsedReTweets.contains(newTweet.getTweetBy() + " says " + tweetContent)) {
                                    parsedReTweets.add(newTweet.getTweetBy() + " says " + tweetContent);
                                }
                                break;
                            default: break;
                        }
                        contentFetched = true;
                    }
                }
            } catch (TwitterException te) {
                te.printStackTrace();
                Log.d("Twitter", "Failed to get timeline: " + te.getMessage());
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            contentFetched = true;
        }

        @Override
        protected void onPreExecute() {
            contentFetched = false;
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private class FetchRandomTweets extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            for(String p : params) {
                try {
                    Query query = new Query(p);
                    QueryResult result;
                    result = twitter.search(query);
                    List<twitter4j.Status> tweets = result.getTweets();
                    //The latest tweet is in the first spot in the list
                    Log.d("Twitter", "@" + tweets.get(0).getUser().getScreenName() + " - " + tweets.get(0).getText());
                    for(int i=0; i<10 && i<tweets.size(); i++) {
                        parsedRandTweets.add(new Tweet(tweets.get(i)));
                    }
                } catch (TwitterException te) {
                    te.printStackTrace();
                    Log.d("Twitter", "Failed to search tweets: " + te.getMessage());
                }
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {}

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    public Queue getContent(final int contentType) {
        Queue tmp = new LinkedList();
        switch(contentType) {
            case Tweet.SECUROBOT_ARTICLE:
                tmp.addAll(parsedArticleLinks);
                parsedArticleLinks.clear();
                break;
            case Tweet.SECUROBOT_JOKE:
                tmp.addAll(parsedJokes);
                parsedJokes.clear();
                break;
            case Tweet.SECUROBOT_QUIZ:
                tmp.addAll(parsedQuizLinks);
                parsedQuizLinks.clear();
                break;
            case Tweet.SECUROBOT_RSSFEED:
                tmp.addAll(parsedRSSLinks);
                parsedRSSLinks.clear();
                break;
            case Tweet.SECUROBOT_TIP:
                tmp.addAll(parsedTips);
                parsedTips.clear();
                break;
            default: break;
        }
        return tmp;
    }

    public void setTTSEngine(TTSEngine e) {
        engine = e;
    }

    public void speakLatestTweet(){
        engine.speak(parsedReTweets.element().toString(), TextToSpeech.QUEUE_FLUSH, null);
    }

    public void speakLatestStatus() {
        //engine.speak(parsedStatuses.element().toString(), TextToSpeech.QUEUE_FLUSH, null);
        engine.speak(parsedStatuses.element().toString(), TextToSpeech.QUEUE_FLUSH, null);
    }

    public String getLatestStatus() {
        return latestStatus;
    }

    public void speakLatestRandTweet() {
        engine.speak(parsedRandTweets.element().toString(), TextToSpeech.QUEUE_FLUSH, null);
    }

    public void updateStatus(String text, File file) {
        try {
            try {
                // get request token.
                // this will throw IllegalStateException if access token is already available
                RequestToken requestToken = twitter.getOAuthRequestToken();
                Log.d("Twitter", "Got request token.");
                Log.d("Twitter", "Request token: " + requestToken.getToken());
                Log.d("Twitter", "Request token secret: " + requestToken.getTokenSecret());
                AccessToken accessToken = null;

                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                while (null == accessToken) {
                    Log.d("Twitter", "Open the following URL and grant access to your account:");
                    Log.d("Twitter", requestToken.getAuthorizationURL());
                    Log.d("Twitter", "Enter the PIN(if available) and hit enter after you granted access.[PIN]:");
                    String pin = br.readLine();
                    try {
                        if (pin.length() > 0) {
                            accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                        } else {
                            accessToken = twitter.getOAuthAccessToken(requestToken);
                        }
                    } catch (TwitterException te) {
                        if (401 == te.getStatusCode()) {
                            Log.d("Twitter", "Unable to get the access token.");
                        } else {
                            te.printStackTrace();
                        }
                    }
                }
                Log.d("Twitter", "Got access token.");
                Log.d("Twitter", "Access token: " + accessToken.getToken());
                Log.d("Twitter", "Access token secret: " + accessToken.getTokenSecret());
            } catch (IllegalStateException ie) {
                // access token is already available, or consumer key/secret is not set.
                if (!twitter.getAuthorization().isEnabled()) {
                    Log.d("Twitter", "OAuth consumer key/secret is not set.");
                    return;
                }
            }

            StatusUpdate status = new StatusUpdate(text);
            if(file!=null) status.setMedia(file);
            new twitterUploadTask().execute(status);
            return;
        } catch (TwitterException te) {
            te.printStackTrace();
            Log.d("Twitter", "Failed to get timeline: " + te.getMessage());
            return;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            Log.d("Twitter", "Failed to read the system input.");
            return;
        }
    }

    private class twitterUploadTask extends AsyncTask<StatusUpdate, Void, String> {
        @Override
        protected String doInBackground(StatusUpdate... status) {
            try {
                Log.d("Twitter", "Uploading status...");
                twitter.updateStatus(status[0]);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            /*
            Toast.makeText(getApplicationContext(),
                    "Finished uploading image to twitter.", Toast.LENGTH_LONG).show();
                    */
            Log.d("Twitter", "Successfully updated the status.");
            super.onPostExecute(s);
        }
    }

    public int getReTweetSize() {
        return parsedReTweets.size();
    }

    public int getRandTweetSize() {
        return parsedRandTweets.size();
    }

    public int getStatusSize() {
        return parsedStatuses.size();
    }
}

