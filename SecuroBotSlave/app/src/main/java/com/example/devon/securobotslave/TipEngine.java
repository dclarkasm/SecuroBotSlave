package com.example.devon.securobotslave;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Devon on 7/6/2015.
 */
public class TipEngine {
    private Random r = new Random();
    private static final int queueSize = 10;
    private Queue tips = new ArrayBlockingQueue(queueSize);        //a queue of size 10

    public TipEngine() {
        tips.add("Keep your operating system, browser, anti-virus and other critical software up to date. " +
                "Security updates and patches are available for free from major companies.");
        tips.add("Firewalls create a protective wall between your computer and the outside world. They ensure that unauthorized " +
                "persons cant gain access to your computer while your connected to the Internet. ");
        tips.add("Disconnect your computer from the Internet when not in use. Disconnecting from the " +
                "Internet when your not online lessens the chance that someone will be able to access " +
                "your computer.");
/*
        //*************
        tips.add("Keep your computer safe by choosing a password that includes letters, " +
                "numbers, and special characters.");
        tips.add("Try not to include birthdays, common names, or places in your passwords");
        tips.add("When creating passwords, using obscure words mixed with an assortment of " +
                "random numbers or symbols can better protect you against password hacking");

        //from http://www.dhs.gov/cybersecurity-tips
        //tips.add("Verify the authenticity of requests from companies or individuals by contacting them directly.");
        tips.add("If you are being asked to provide personal information via email, you can independently contact " +
                "the company directly to verify this request.");
        tips.add("Malicious websites sometimes use a variation in common spelling or a different domain " +
        "to deceive unsuspecting computer users.");

        //from http://www.protectmyid.com/cyber-security
        tips.add("Before submitting credit card information online, look at the URL to ensure you're on a HTTPS site.");
        tips.add("Be wary if a site requires information that isnt necessary for a transaction.");
        tips.add("With the proper software installed, stolen laptops can be tracked to a physical location if they " +
        "are connected to the Internet.");

        //from http://www.ncpc.org/resources/files/pdf/fraud/10-tips-to-secure-brochure-pdf.pdf
*/
    }

    public String generateTip() {
        int rn = r.nextInt(tips.size() - 0);
        Iterator iterator = tips.iterator();
        String tmp = iterator.next().toString();
        while(rn > 0) {
            Log.d("Tip", tmp);
            tmp = iterator.next().toString();
            rn--;
        }

        return "Here's a tip. " + tmp;
    }

    public void printContent() {
        Iterator iterator = tips.iterator();
        while(iterator.hasNext()) {
            Log.d("Tip", iterator.next().toString());
        }
    }

    public void addContent(Queue content) {
        if(content!=null) {
            while(content.size()>0) {
                String c = content.remove().toString();
                if(!tips.contains(c) && tips.size()+1<queueSize) {
                    tips.add(c);
                    Log.d("Tip", "\nJust added:\n\n" + c + "\n\nto end of queue.\n");
                }
                else {
                    String removed = tips.remove().toString();
                    Log.d("Tip", "\nJust removed:\n\n" + removed
                            + "\n\nfrom front of queue.\n"); //if queue is at capacity, dequeue to add space for new content
                    if(!tips.contains(c) && tips.size()+1<queueSize) {
                        tips.add(c);
                        Log.d("Tip", "\nJust added:\n\n" + c + "\n\nto end of queue.\n");
                    }
                }
            }
        }

        Log.d("Tip", "content:\n");
        printContent();
    }
}
