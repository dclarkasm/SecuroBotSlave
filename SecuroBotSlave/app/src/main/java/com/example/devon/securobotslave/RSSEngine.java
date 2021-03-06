package com.example.devon.securobotslave;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Devon on 7/6/2015.
 * source code adapted from: http://www.tutorialspoint.com/android/android_rss_reader.htm
 */
public class RSSEngine {
    private String author = null;   //RSS feed channel name
    private String title = "title";     //feed title (title of article
    private String link = "link";       //associated link to article
    private String description = "description"; //article preface (String HTML format)
    private String urlString = null;
    private XmlPullParserFactory xmlFactoryObject;
    public volatile boolean processing = false;
    private boolean processingFailure = false;
    //private ArrayList<String> URLList = new ArrayList<String>();
    private static final int queueSize = 10;
    private Queue URLList = new ArrayBlockingQueue(queueSize);
    private Random r = new Random();

    public RSSEngine() {
        URLList.add("https://nakedsecurity.sophos.com/feed/");
        //URLList.add("https://www.sans.org/webcasts/rss/");
        //URLList.add("http://www.theregister.co.uk/headlines.rss");
    }

    public String getTitle(){
        return title;
    }

    public String getLink(){
        return link;
    }

    public String getAuthor(){
        return author;
    }

    public String getDescription(){
        return description;
    }

    public boolean isProcessingFailure() {
        return processingFailure;
    }

    public void parseXMLAndStoreIt(XmlPullParser myParser) {
        int event;
        String text=null;
        boolean foundChannel=false, foundAuthor=false, foundItem=false,
                foundTitle=false, foundDescription=false, foundLink=false;

        try {
            event = myParser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {
                String name=myParser.getName();

                switch (event){
                    case XmlPullParser.START_TAG: {
                        if(name.equals("channel")) {
                            foundChannel=true;
                            Log.d("RSS", "found channel");
                        }
                        else if(name.equals("item") && foundChannel && foundAuthor && !foundItem) {
                            foundItem = true;
                            Log.d("RSS", "found item");
                        }
                        break;
                    }
                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                    {
                        if(name.equals("title") && foundChannel && !foundAuthor) {
                            author = text; //if the author was not set on initialization
                            foundAuthor = true;
                            Log.d("RSS", "found RSSFeed Author: " + author);
                        }
                        else if(name.equals("title") && foundChannel && foundAuthor &&
                                foundItem && !foundTitle) {
                            title = text;
                            foundTitle = true;
                            Log.d("RSS", "found title");
                        }
                        else if(name.equals("link") && foundChannel && foundAuthor &&
                                foundItem && foundTitle && !foundDescription) {
                            link = text;
                            foundLink = true;
                        }
                        else if(name.equals("description") && foundChannel && foundAuthor &&
                                foundItem && foundTitle && !foundDescription) {
                            description = text;
                            foundDescription = true;
                            Log.d("RSS", "found RSSFeed description: " + description);
                            //TODO: add code here to process the string description
                        }
                        break;
                    }
                }
                if(foundAuthor && foundChannel && foundDescription && foundItem && foundTitle && foundLink) break;   //break if we found all the components to the latest RSS feed
                event = myParser.next();
            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchXML(){
        processing = true;

        //set a random url for the search if no url specified
        Log.d("RSS", "URL list size: " + URLList.size());
        int rn = r.nextInt(URLList.size() - 0);
        Iterator iterator = URLList.iterator();
        String tmp = iterator.next().toString();
        while(rn > 0) {
            Log.d("RSS", tmp);
            tmp = iterator.next().toString();
            rn--;
        }
        urlString = tmp;

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {

                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);

                    // Starts the query
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser myparser = xmlFactoryObject.newPullParser();

                    myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myparser.setInput(stream, null);

                    parseXMLAndStoreIt(myparser);
                    stream.close();
                    processing = false;
                }

                catch (Exception e) {
                    Log.d("RSS", "Processing error for url: " + urlString);
                    processing = false;
                    processingFailure = false;
                }
                processing = false;
            }
        });
        thread.start();
    }

    public void printContent() {
        Iterator iterator = URLList.iterator();
        while(iterator.hasNext()) {
            Log.d("RSS", iterator.next().toString());
        }
    }

    public void addContent(Queue content) {
        if(content!=null) {
            while(content.size()>0) {
                String c = content.remove().toString();
                if(!URLList.contains(c) && URLList.size()+1<queueSize) {
                    URLList.add(c);
                    Log.d("RSS", "\nJust added:\n\n" + c + "\n\nto end of queue.\n");
                }
                else {
                    URLList.remove();  //if queue is at capacity, dequeue to add space for new content
                    if(!URLList.contains(c) && URLList.size()+1<queueSize) {
                        URLList.add(c);
                        Log.d("RSS", "\nJust added:\n\n" + c + "\n\nto end of queue.\n");
                    }
                }
            }
        }

        Log.d("RSS", "content:\n");
        printContent();
    }
}
