package com.example.ygoquiz;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class Request {


    public static Map<String, Object> loadJSON(String json, List<String> languages, Context context) {
        String info = "";
        Map<String, Object> result = new HashMap<>();
        Map<String, String> matchDB = loadKeyPairs(context);
        List<String> getvalues = Arrays.asList("Misc", "English name", "Lore", "Attribute", "Password", "Type", "Types", "Card type", "Property", "Stars string", "ATK string", "MAXIMUM ATK", "DEF string", "Link Rating", "Link Arrows", "Pendulum Scale string", "Pendulum Effect", "OCG status", "TCG status", "Card image", "In Deck", "Archseries", "Mentions", "Rarity");

        int numberBeforeLang = getvalues.size();
        for (String lang : languages) {
            getvalues.add(lang + " name");
            getvalues.add(lang + " lore");
            getvalues.add(lang + " Pendulum Effect");
        }

        int languageFindCount = -2;

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject queryDict = jsonObject.getJSONObject("query");
            JSONObject resultsDict = queryDict.getJSONObject("results");

            Map<String, Integer> linkMarker = new HashMap<>();
            linkMarker.put("Top-Left", 1);
            linkMarker.put("Top-Center", 2);
            linkMarker.put("Top-Right", 3);
            linkMarker.put("Middle-Left", 4);
            linkMarker.put("Middle-Right", 5);
            linkMarker.put("Bottom-Left", 6);
            linkMarker.put("Bottom-Center", 7);
            linkMarker.put("Bottom-Right", 8);

            List<String> misc = new ArrayList<>();
            List<String> mentions = new ArrayList<>();
            List<String> series = new ArrayList<>();
            String typeField = "";
            String spellTrapType = "";
            List<Integer> markers = new ArrayList<>();
            int hasEffect = -1;
            List<String> types = new ArrayList<>();
            String displayMarker = "Markers are:";
            String Rarity = "";

            for (Iterator<String> it = resultsDict.keys(); it.hasNext();) {
                String cardName = it.next();
                JSONObject cardDict = resultsDict.getJSONObject(cardName);
                result.put("name", cardName);

                int bracketPos2 = cardName.indexOf('(');
                if (bracketPos2 > 0) {
                    bracketPos2++;
                    cardName = cardName.substring(bracketPos2, cardName.indexOf(')'));
                    result.put(matchDB.get("Distinction"), cardName.trim());
                }

                JSONObject printoutsDict = cardDict.getJSONObject("printouts");

                boolean complexObject = false;

                for (String getval : getvalues) {
                    info = getval;
                    numberBeforeLang--;

                    if (numberBeforeLang <= 0) {
                        languageFindCount++;
                        if (languageFindCount == 3) {
                            languageFindCount = 0;
                        }
                    }

                    if (printoutsDict.has(getval)) {
                        JSONArray valList = printoutsDict.getJSONArray(getval);
                        if (valList.length() > 0) {
                            String val = "";
                            JSONObject innerObject = new JSONObject();

                            if (valList.get(0) instanceof String || valList.get(0) instanceof Integer) {
                                int elementCount = valList.length();
                                val = valList.get(0).toString();

                                if (elementCount > 1) {
                                    for (int i = 0; i < elementCount; i++) {
                                        if (getval.equals("Link Arrows")) {
                                            markers.add(linkMarker.get(valList.getString(i)));
                                            displayMarker += linkMarker.get(valList.getString(i)) + ",";
                                        }
                                    }
                                }
                                complexObject = false;
                            } else if (innerObject.length() == 0) {
                                innerObject = valList.getJSONObject(0);
                                complexObject = true;
                            }

                            if (getval.equals("Misc")) {
                                for (int i = 0; i < valList.length(); i++) {
                                    innerObject = valList.getJSONObject(i);
                                    misc.add(innerObject.getString("fulltext"));
                                }
                            }
                            else if (getval.equals("Rarity")) {
                                Rarity = innerObject.getString("fulltext");
                            }
                            else if (getval.equals("Mentions")) {
                                for (int i = 0; i < valList.length(); i++) {
                                    innerObject = valList.getJSONObject(i);
                                    String toAddToMen = innerObject.getString("fulltext");
                                    int bracketPos = toAddToMen.indexOf('(');
                                    if (bracketPos > 0) {
                                        toAddToMen = toAddToMen.substring(0, bracketPos).trim();
                                    }
                                    mentions.add(toAddToMen);
                                }
                            } else if (getval.equals("Archseries")) {
                                for (int i = 0; i < valList.length(); i++) {
                                    innerObject = valList.getJSONObject(i);
                                    String reformed = innerObject.getString("fulltext");
                                    int firstPos = reformed.indexOf('(');
                                    if (firstPos > 0) {
                                        reformed = reformed.substring(0, firstPos).trim();
                                    }
                                    series.add(reformed);
                                }
                            }

                            if (getval.equals("Type")) {
                                typeField = innerObject.getString("fulltext");
                            }

                            if (getval.equals("Types")) {
                                // Create a map to hold the possible effect displays
                                Map<Integer, String> hasEffectDisplay = new HashMap<>();
                                hasEffectDisplay.put(-1, "None");
                                hasEffectDisplay.put(1, "Effect");
                                hasEffectDisplay.put(0, "Normal");

                                // Remove whitespace and split the types string by "/"
                                String[] splitTypes = val.replaceAll("\\s+", "").split("/");

                                // Convert the array to a List for easier manipulation
                                types = new ArrayList<>(Arrays.asList(splitTypes));

                                // Remove the 'typeField' from the types list
                                types.remove(typeField);

                                // Check for and remove "Effect" or "Normal" in the types list, setting 'hasEffect' accordingly
                                if (types.remove("Effect")) {
                                    hasEffect = 1;
                                } else if (types.remove("Normal")) {
                                    hasEffect = 0;
                                }

                                // You can optionally log the remaining types (for debugging purposes)
                                for (String type : types) {
                                    // Log or process the remaining types as needed
                                    // Log.d("Type", type); // Uncomment for logging
                                }

                                // Optionally use 'hasEffectDisplay' for some action, like logging
                                // Log.d("Effect Type", hasEffectDisplay.get(hasEffect)); // Uncomment for logging
                            }


                            if (getval.equals("Lore") || getval.equals("Pendulum Effect")) {
                                val = val.replace("<br/>", "\n").replace("<br />", "\n");
                                val = fixBrackets(val);
                            }

                            try {
                                if (getval.equals("Mentions")) {
                                    result.put(matchDB.get(getval), mentions);
                                }
                                else if (getval.equals("Rarity")) {
                                    result.put(matchDB.get(getval), Rarity);
                                }
                                else if (getval.equals("In Deck")) {
                                    // Create a list to hold the characters
                                    List<String> allTheCharacters = new ArrayList<>();

                                    // Iterate through the valList (which is a List of objects in Java)
                                    for (int i = 0; i < valList.length(); i++) {
                                        // Get the current item from the list as a Map (equivalent to Dictionary in C#)
                                        // Add the value of "fullurl" to the allTheCharacters list
                                        allTheCharacters.add((String) innerObject.get("fullurl"));

                                        // Optionally log the fullurl for debugging purposes
                                        // Log.e("Full URL", (String) innerObject.get("fullurl"));
                                    }

                                    // Optionally, do something with 'allTheCharacters' list after the loop
                                }
                                else if (getval.equals("Archseries")) {
                                    result.put(matchDB.get(getval), series);
                                } else if (numberBeforeLang <= 0) {
                                    List<String> hasLanguages = new ArrayList<>();
                                    if (result.containsKey("languages")) {
                                        hasLanguages.addAll((List<String>) result.get("languages"));
                                    }
                                    String[] languageFind = {"name", "lore", "Pendulum Effect"};
                                    int atLang = numberBeforeLang * -1;
                                    int langCount = -1;
                                    while (atLang > 0) {
                                        atLang -= 3;
                                        langCount++;
                                    }
                                    val = val.replace("<br/>", "\n").replace("<br />", "\n");
                                    hasLanguages.add(languageFind[languageFindCount] + "|" + languages.get(langCount) + "|" + fixBrackets(val));
                                    result.put("languages", hasLanguages);
                                } else if (getval.equals("Link Arrows")) {
                                    result.put(matchDB.get(getval), markers);
                                } else if (getval.equals("Types")) {
                                    result.put(matchDB.get(getval), types);
                                    result.put(matchDB.get("hasEffect"), hasEffect);
                                } else if (getval.equals("Misc")) {
                                    result.put(matchDB.get(getval), misc);
                                } else if (!complexObject) {
                                    result.put(matchDB.get(getval), val);
                                } else {
                                    result.put(matchDB.get(getval), innerObject.getString("fulltext"));
                                }
                            } catch (Exception e) {
                                System.err.println(info + " Error at saving read JSON to an object: " + e.getMessage());
                            }

                            if (getval.equals("Card type")) {
                                // Get the card type string from the innerObject
                                String cardType = (String) innerObject.get("fulltext");

                                // Remove "Card" from the card type string
                                cardType = cardType.replace("Card", "");

                                // Remove all whitespace from the card type using regex
                                cardType = cardType.replaceAll("\\s+", "");

                                // Convert the card type to uppercase
                                cardType = cardType.toUpperCase();

                                // If the card type is "TRAPSPELL", change it to "SPELLTRAP"
                                if (cardType.equals("TRAPSPELL")) {
                                    cardType = "SPELLTRAP";
                                }

                                // If the card type is not "MONSTER", update the result map
                                if (!cardType.equals("MONSTER")) {
                                    result.put(matchDB.get("Attribute"), cardType);
                                    spellTrapType = "Is"; // You can set this as required for your logic
                                    // Optionally, log the corrected attribute for debugging
                                    // Log.d("CardType", "Corrected Attribute: " + cardType);
                                }
                            }


                            else if (getval.equals("Property") && spellTrapType.equals("Is")) {
                                // Start with the 'val' string for the spell type
                                String spellType = val;

                                // Remove specific substrings from the spell type
                                spellType = spellType.replace("Card", "");
                                spellType = spellType.replace("Spell", "");
                                spellType = spellType.replace("Trap", "");
                                spellType = spellType.replace("Speed", "");

                                // Check if any string in 'misc' matches "Action Card" and change spellType to "Action"
                                for (String str : misc) {
                                    if (str.equals("Action Card")) {
                                        spellType = "Action";
                                        // Log or do other actions if needed
                                        // Log.d("Property", "Found Action Card");
                                        break;
                                    }
                                }

                                // Remove whitespace characters using regex
                                spellType = spellType.replaceAll("\\s+", "");

                                // Log the spell type if needed for debugging
                                // Log.d("Property", "Spell Type is: " + spellType);

                                // Update the spellTrapType and result map
                                spellTrapType = spellType;
                                result.put(matchDB.get(getval), spellType);
                            }

                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fixJSON(result, matchDB);
    }

    public static Map<String, Object> fixJSON(Map<String, Object> values, Map<String, String> matchDB) {
        // Create a list of strings to compare, using the values of matchDB
        List<String> toCompare = new ArrayList<>(matchDB.values());

        // Iterate through the 'toCompare' list and remove entries from 'values' that are equal to "???"
        for (Iterator<String> iterator = toCompare.iterator(); iterator.hasNext(); ) {
            String str = iterator.next();
            if (values.containsKey(str) && values.get(str).toString().equals("???")) {
                values.remove(str);
            }
        }

        // If "Monster.ATK" exists and its value is "X000", change it to "?" and also modify "Monster.DEF"
        if (values.containsKey("Monster.ATK") && values.get("Monster.ATK").toString().equals("X000")) {
            values.put("Monster.ATK", "?");
            values.put("Monster.DEF", "?");
        }

        // Remove "Monster.ATK" and "Monster.DEF" from 'toCompare'
        toCompare.remove("Monster.ATK");
        toCompare.remove("Monster.DEF");

        // Iterate through the 'toCompare' list and remove entries from 'values' that are equal to "?"
        for (Iterator<String> iterator = toCompare.iterator(); iterator.hasNext(); ) {
            String str = iterator.next();
            if (values.containsKey(str) && values.get(str).toString().equals("?")) {
                values.remove(str);
            }
        }

        return values;
    }

    public static Map<String, String> loadKeyPairs(Context context) {
        Map<String, String> result = new HashMap<>();

        // Open the "keyPairs.txt" file from the assets folder
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("keyPairs.txt")));
            String line;

            // Read the file line by line
            while ((line = reader.readLine()) != null) {
                // Ensure the line contains '=' to avoid errors
                if (line.contains("=")) {
                    String[] parts = line.split("=", 2); // Split into key and value
                    String key = parts[0].trim();  // Key part
                    String value = parts[1].trim();  // Value part
                    result.put(key, value);
                }
            }

            reader.close();
        } catch (IOException e) {
            // Log error if file is not found or there's an issue reading the file
            Log.e("loadKeyPairs", "Error reading keyPairs.txt from assets", e);
        }

        return result;
    }

    public static String fixBrackets(String text) {
        // Find the position of "[[" in the text
        int position = text.indexOf("[[");

        // If "[[" is not found, return the original text
        if (position < 0)
            return text;

        // Find the position of "]]" after "[["
        int lastPosition = text.indexOf("]]");
        position += 2; // Move the position past "[["

        // Extract the middle part of the text between "[[" and "]]"
        String middleText = text.substring(position, lastPosition);

        // Find the position of '|' in the middle text, if it exists
        int slashPosition = middleText.indexOf('|');
        if (slashPosition > 0) {
            slashPosition++;
            middleText = middleText.substring(slashPosition);
        }

        // Get the text before "[[" and after "]]"
        String firstHalf = text.substring(0, position - 2);
        String otherHalf = text.substring(lastPosition + 2);

        // Reconstruct the text with the modified middle part
        text = firstHalf + middleText + otherHalf;

        // Recursively call fixBrackets() to handle any nested occurrences
        return fixBrackets(text);
    }
}
