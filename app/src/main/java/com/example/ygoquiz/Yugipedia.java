package com.example.ygoquiz;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Yugipedia {

    private final int limitRule = 100;
    private final Context context;

    public Yugipedia(Context context) {
        this.context = context;
    }

    public void getCardName(int currentScore, int highScore) {
        if (isInternetAvailable(this.context)) {
            new Thread(() -> {
                try {
                    String apiUrl = "https://yugipedia.com/api.php?action=ask&format=json&query=[[Release::Yu-Gi-Oh!%20Master%20Duel]]|sort=random|limit=" + caculateTheLimit();
                    URL url = new URL(apiUrl);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        Log.d("Yugipedia", "API Response: " + response.toString());

                        List<String> list = new ArrayList<>(returnCardNames((response.toString())));
                        String fluff = "https://yugipedia.com/wiki/";
                        Collections.shuffle(list);
                        for (String str : list) {
                            Log.d("Yugipedia", "API Response - Item: " + str);
                            Log.d("Yugipedia", "API Response - Real Item: " + URLDecoder.decode(str.substring(fluff.length()), "UTF-8").replace("_", " "));
                        }
                        String str = list.get(0);
                        //str = "https://yugipedia.com/wiki/Blue-Eyes_Alternative_White_Dragon_(Master_Duel)";
                        addCardInfo(URLDecoder.decode(str.substring(fluff.length()), "UTF-8").replace("_", " "), str, currentScore, highScore);

                    } else {
                        Log.e("Yugipedia", "Error: " + responseCode);
                    }
                    connection.disconnect();
                } catch (Exception e) {
                    Log.e("Yugipedia", "Exception: " + e.getMessage(), e);
                }
            }).start();
        }
        else  {
            SqliteHandler sq = new SqliteHandler(this.context);
            int cardID = sq.getRandomCardFromDB();
            catchedCardUse(currentScore, highScore, cardID);
        }
    }

    private List<String> returnCardNames(String response) {
        List<String> list = new ArrayList<>();
        String findIt = "\"fullurl\":\"";
        int foundAt = response.indexOf(findIt);
        while (foundAt > 0) {
            foundAt += findIt.length();
            //Log.d("Convert to List", "FoundAt:" + foundAt);
            int endOfTheLink = response.indexOf('"', foundAt + 1);
            //Log.d("Convert to List", "Found End At:" + endOfTheLink);
            list.add(response.substring(foundAt, endOfTheLink));
            //Log.d("Convert to List", "Here is the URL:" + response.substring(foundAt, endOfTheLink));
            response = response.substring(endOfTheLink + 1);
            //Log.d("Convert to List", "Response is now:" + response);
            foundAt = response.indexOf(findIt);
        }
        return list;
    }

    public int caculateTheLimit() {
        int amountOfCards = -1;
        try {
            SqliteHandler SqliteHandler = new SqliteHandler(context);
            amountOfCards = SqliteHandler.getNumberOfCards();
            if (amountOfCards <= 0) amountOfCards = 1;
            amountOfCards = amountOfCards % limitRule;
        }
        catch(Exception e) {
            Log.e("SqliteHandler", "caculateTheLimit: " + e.getMessage());
        }
        if (amountOfCards <= 0) amountOfCards = 1;
        return amountOfCards;
    }

    public void addCardInfo(String cardPageDecoded, String cardPage, int currentScore, int highScore) {
        String refinedCardPage = cardPageDecoded.replace("(Master Duel)", "").trim();
        int cardID = -1;
        try {
            SqliteHandler SqliteHandler = new SqliteHandler(context);
            cardID = SqliteHandler.doesItExist(refinedCardPage);
        }
        catch(Exception e) {
            Log.e("SqliteHandler", "AddCardInfo: " + e.getMessage());
        }
        //if the card isn't cached
        if (cardID < 0) {
            downloadCardData(cardPage, currentScore, highScore);
        }
        //if the card IS cached
        else {
            catchedCardUse(currentScore, highScore, cardID);
        }
    }

    public void downloadCardData(String cardPage, int currentScore, int highScore) {
        new Thread(() -> {
            try {

                String[] properties = {
                        "?English%20name", "?ATK%20string", "?DEF%20string", "?Attribute", "?Type", "?Types", "?Password", "?Stars%20string",
                        "?Lore", "?Link%20Arrows", "?Link%20Rating", "?Pendulum%20Scale%20string", "?Pendulum%20Effect", "?MAXIMUM%20ATK",
                        "?OCG%20status", "?TCG%20status", "?Card%20image", "?Card%20type", "?Property", "?Misc", "?Archseries", "?Mentions",
                        "?In%20Deck", "?Rarity"
                };
                String fluff = "https://yugipedia.com/wiki/";
                String apiUrl = "https://yugipedia.com/api.php?action=ask&format=json&query=[["+cardPage.substring(fluff.length())+"]]|" + String.join("|", properties);
                URL url = new URL(apiUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    Log.d("Yugipedia", "API Query: " + apiUrl);
                    Log.d("Yugipedia", "API THE REAL !!Response!!: " + response.toString());

                    try {
                        JSONObject jsonObject = new JSONObject(response.toString());
                        JSONObject queryDict = jsonObject.getJSONObject("query");
                        JSONObject results = queryDict.getJSONObject("results");
                        Iterator<String> keysIterator = results.keys();
                        Map<String, Object> map = new HashMap<>();
                        while (keysIterator.hasNext()) {
                            String key = keysIterator.next();
                            Log.d("JSON", "Card Name: " + key.replace("(Master Duel)", "").trim());
                        }

                        List<String> emptyList = new ArrayList<>();
                        Map<String, Object> endResult = Request.loadJSON(response.toString(), emptyList , context);

                        Card c = new Card(endResult, context);
                        c.logMyself("Test");
                        c.addMeToTheCB("");
                        c.FetchCardsImages(cardPage.substring(fluff.length()), currentScore, highScore);

                        /*
                        while (keysIterator.hasNext()) {
                            String cardName = keysIterator.next();
                            JSONObject cardInfo = results.getJSONObject(cardName);
                            JSONObject printouts = cardInfo.getJSONObject("printouts");
                            String[] keys = {
                                    "English name", "ATK string", "DEF string", "Attribute", "Type", "Types", "Password", "Stars string",
                                    "Lore", "Link Arrows", "Link Rating", "Pendulum Scale string", "Pendulum Effect", "MAXIMUM ATK",
                                    "OCG status", "TCG status", "Card image", "Card type", "Property", "Misc", "Archseries", "Mentions", "In Deck"
                            };
                            List<String> keysNotFound = new ArrayList<>();
                            Log.d("JSON", "Card Name: " + cardName);
                            for (String key : keys) {
                                if (printouts.has(key)) {
                                    JSONArray valueArray = printouts.getJSONArray(key);
                                    Log.d("JSON", key + ": " + valueArray.toString());
                                } else {
                                    keysNotFound.add(key);
                                    Log.d("JSON", key + ": Not available");
                                }
                            }
                        }
                        */
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e("Yugipedia", "Error: " + responseCode);
                }
                connection.disconnect();
            } catch (Exception e) {
                Log.e("Yugipedia", "Exception: " + e.getMessage(), e);
            }
        }).start();
    }

    public static boolean isInternetAvailable(Context context) {
        // Get the ConnectivityManager system service
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get the current network info
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        // Check if the active network is connected and if it is either mobile or WiFi
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public void catchedCardUse(int currentScore, int highScore, int cardID) {
        try {
            Log.d("AddCardInfo", "The card is already in the DB with the Id of " + cardID);
            Card c = new Card(cardID, context);
            c.logMyself("Test Data from DB");
            String optionsToUse = c.setTypeOfQuestions();
            Log.d("MakeTheQuestion", optionsToUse);
            String[] options = c.generateQuestions(optionsToUse);
            Log.d("MakeTheQuestion", options[0] + " " + options[1] + " " + options[2] + " " + options[3]);
            SqliteHandler SqliteHandler = new SqliteHandler(context);
            SqliteHandler.writeTheNewQuestion(currentScore, highScore, options, c.getID());
        } catch (Exception e) {
            Log.e("catchedCardUse", "Exception: " + e.getMessage(), e);
        }
    }
}

