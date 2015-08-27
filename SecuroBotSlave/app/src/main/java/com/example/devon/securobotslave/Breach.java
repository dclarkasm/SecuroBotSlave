package com.example.devon.securobotslave;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by devonclark on 8/27/15.
 */
public class Breach {
    //Tags
    public static final String Title_Tag = "Title";
    public static final String Name_Tag = "Name";
    public static final String Domain_Tag = "Domain";
    public static final String BreachDate_Tag = "BreachDate";
    public static final String AddedDate_Tag = "AddedDate";
    public static final String PwnCount_Tag = "PwnCount";
    public static final String Description_Tag = "Description";
    public static final String DataClasses_Tag = "DataClasses";
    public static final String IsVerified_Tag = "IsVerified";
    public static final String IsSensitive_Tag = "IsSensitive";
    public static final String LogoType_Tag = "LogoType";

    private String title;
    private String name;
    private String domain;
    private String breachDate;
    private String addedDate;
    private String pwnCount;
    private String description;
    private String dataClasses[];
    private String isVerified;
    private String isSensitive;
    private String logoType;

    public Breach(JSONObject object) {
        try {
            title = object.getString(Title_Tag);
            name = object.getString(Name_Tag);
            domain = object.getString(Domain_Tag);
            breachDate = object.getString(BreachDate_Tag);
            addedDate = object.getString(AddedDate_Tag);
            pwnCount = object.getString(PwnCount_Tag);
            description = object.getString(Description_Tag);

            JSONArray dataClassesArray = object.getJSONArray(DataClasses_Tag);
            dataClasses = new String[dataClassesArray.length()];

            for(int i=0; i<dataClassesArray.length(); i++) {
                dataClasses[i] = dataClassesArray.get(i).toString();
            }
            isVerified = object.getString(IsVerified_Tag);
            isSensitive = object.getString(IsSensitive_Tag);
            logoType = object.getString(LogoType_Tag);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void printBreach() {
        Log.d("Breach", "\n\n" + Title_Tag + ": " + title + "\n" +
                Name_Tag + ": " + name + "\n" +
                Domain_Tag + ": " + domain + "\n" +
                BreachDate_Tag + ": " + breachDate + "\n" +
                AddedDate_Tag + ": " + addedDate + "\n" +
                PwnCount_Tag + ": " + pwnCount + "\n" +
                Description_Tag + ": " + description + "\n" +
                IsVerified_Tag + ": " + isVerified + "\n" +
                IsSensitive_Tag + ": " + isSensitive + "\n" +
                LogoType_Tag + ": " + logoType + "\n");
    }
}
