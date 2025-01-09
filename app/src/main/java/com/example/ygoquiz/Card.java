package com.example.ygoquiz;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Objects;

public class Card{

    private static final String TAG = "Card";
    private int id;
    private String name;
    private String description;
    private String attribute;
    private String password;
    private byte[] image;
    private List<Integer> linkMarkers;
    private String spellTrapType;
    private List<String> hasLanguages;
    private List<String> mentions;
    private List<String> archetype;
    private String distinction;
    private String Rarity;
    private List<String> keysNotFound;

    private boolean monsterCard = false;
    private int ATK;
    private int DEF;
    private int pendulumScale;
    private int level;
    private String race;

    private static final String DB_NAME = "YGO Quiz.db";
    private static final int DB_VERSION = 1;
    private final Context context;

    // Constructor to initialize from a Map (similar to Dictionary in C#)
    public Card(Map<String, Object> values, Context context) {
        this.context = context;
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);

        this.id = -1; // Default id value
        this.keysNotFound = new ArrayList<>();
        keysNotFound.add("BOLAN");

        if (values.containsKey("Card type")) {
            if (Objects.equals((String) values.get("Card type"), "Monster Card")) this.monsterCard = true;
        }

        // Check and set "Card.name"
        if (values.containsKey("Card.name")) {
            this.name = (String) values.get("Card.name");
        } else {
            keysNotFound.add("Card.name");
        }

        // Check and set "Card.description"
        if (values.containsKey("Card.description")) {
            this.description = (String) values.get("Card.description");
        } else {
            keysNotFound.add("Card.description");
        }

        // Check and set "Card.attribute"
        if (values.containsKey("Card.attribute")) {
            this.attribute = (String) values.get("Card.attribute");
        } else {
            keysNotFound.add("Card.attribute");

        }

        if (values.containsKey("Rarity")) {
            this.Rarity = (String) values.get("Rarity");
        } else {
            keysNotFound.add("Rarity");

        }

        // Check and set "Card.password"
        if (values.containsKey("Card.password")) {
            this.password = (String) values.get("Card.password");
        } else {
            keysNotFound.add("Card.password");
        }

        // Check and set "LinkArrow"
        if (values.containsKey("LinkArrow")) {
            this.linkMarkers = (List<Integer>) values.get("LinkArrow");
            this.linkMarkers.sort(Integer::compareTo); // Sorting the list
        } else {
            keysNotFound.add("LinkArrow");

        }

        // Check and set "SpellType"
        if (values.containsKey("SpellType")) {
            this.spellTrapType = (String) values.get("SpellType");
        } else {
            keysNotFound.add("SpellType");
        }

        // Check and set "languages"
        if (values.containsKey("languages")) {
            this.hasLanguages = (List<String>) values.get("languages");
        } else {
            keysNotFound.add("languages");
        }

        // Check and set "Mentions"
        if (values.containsKey("Mentions")) {
            this.mentions = (List<String>) values.get("Mentions");
            if (!keysNotFound.contains("Card.name")) {
                this.mentions.remove(this.name);
                if (this.mentions.isEmpty()) {
                    keysNotFound.add("Mentions");
                }
            }
        } else {
            keysNotFound.add("Mentions");
        }

        // Check and set "Archetype"
        if (values.containsKey("Archetype")) {
            this.archetype = (List<String>) values.get("Archetype");
        } else {
            keysNotFound.add("Archetype");
        }

        // Check and set "Distinction"
        if (values.containsKey("Distinction")) {
            this.distinction = (String) values.get("Distinction");
        } else {
            keysNotFound.add("Distinction");
        }

        if (monsterCard) {

            if (values.containsKey("Monster.ATK") && values.get("Monster.ATK").toString().equals("?")) {
                values.remove("Monster.ATK");
            }

            if (values.containsKey("Monster.DEF") && values.get("Monster.DEF").toString().equals("?")) {
                values.remove("Monster.DEF");
            }

            if (values.containsKey("Monster.ATK") && values.get("Monster.ATK") == null) {
                values.remove("Monster.ATK");
            }

            if (values.containsKey("Monster.DEF") && values.get("Monster.DEF") == null) {
                values.remove("Monster.DEF");
            }


            if (values.containsKey("Monster.ATK")) {
                this.ATK = Integer.parseInt(values.get("Monster.ATK").toString());
            } else {
                keysNotFound.add("Monster.ATK");
            }

            if (values.containsKey("Monster.DEF")) {
                this.DEF = Integer.parseInt(values.get("Monster.DEF").toString());
            } else {
                keysNotFound.add("Monster.DEF");
            }

            if (values.containsKey("Monster.status")) {
                this.level = Integer.parseInt(values.get("Monster.status").toString());
            } else {
                keysNotFound.add("Monster.status");
            }

            if (values.containsKey("Pendulum.scale")) {
                this.pendulumScale = Integer.parseInt(values.get("Pendulum.scale").toString());
            } else {
                keysNotFound.add("Pendulum.scale");
            }

            if (values.containsKey("Monster.type")) {
                this.race = (String) values.get("Monster.type");
            } else {
                keysNotFound.add("Monster.type");
            }
        }

        db.close();
    }

    @SuppressLint("Range")
    public Card(int id, Context context) {
        this.id = id;
        this.context = context;

        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);

        Cursor cursor = null;
        boolean foundData = false;
        this.keysNotFound = new ArrayList<>();
        keysNotFound.add("BOLAN");
        try {
            String query = "SELECT attribute FROM card WHERE id = " + this.id + ";";
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                foundData = true;
                this.attribute = enumCBSearch("attribute", cursor.getInt(cursor.getColumnIndex("attribute")));
                cursor.close();
            }
            if (!foundData) {
                keysNotFound.add("Card.attribute");
            }

            foundData = false;
            query = "SELECT card.name as name FROM card WHERE id = " + this.id + ";";
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                foundData = true;

                this.name = cursor.getString(cursor.getColumnIndex("name"));
                cursor.close();
            }
            if (!foundData) {
                keysNotFound.add("Name");
            }

            foundData = false;
            query = "SELECT rarity FROM Card_has_Rarity WHERE card = " + this.id + ";";
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                foundData = true;

                this.Rarity = enumCBSearch("Rarity", cursor.getInt(cursor.getColumnIndex("rarity")));
                cursor.close();
            }
            if (!foundData) {
                keysNotFound.add("Rarity");
            }

            foundData = false;
            query = "SELECT spellType FROM Card_has_SpellType WHERE card = " + this.id + ";";
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                foundData = true;
                this.spellTrapType = enumCBSearch("SpellType", cursor.getInt(cursor.getColumnIndex("spellType")));
                cursor.close();
            }
            if (!foundData) {
                keysNotFound.add("SpellType");
            }

            foundData = false;
            query = "SELECT linkArrow FROM Card_has_LinkArrow WHERE card = " + this.id + ";";
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                foundData = true;
                this.linkMarkers = new ArrayList<>();
                do {
                    Log.d("Link", "Link is: " + cursor.getInt(cursor.getColumnIndex("linkArrow")));
                    this.linkMarkers.add(cursor.getInt(cursor.getColumnIndex("linkArrow")));
                } while (cursor.moveToNext());
                cursor.close();
            }
            if (!foundData) {
                keysNotFound.add("LinkArrow");
            }

            query = "SELECT monster.id as data FROM monster WHERE id = " + this.id + ";";
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                this.monsterCard = true;
                cursor.close();
            }

            if (monsterCard) {
                foundData = false;
                query = "SELECT monster.ATK as data FROM monster WHERE id = " + this.id + ";";
                cursor = db.rawQuery(query, null);
                if (cursor != null && cursor.moveToFirst()) {
                    foundData = true;
                    if (!cursor.isNull(cursor.getColumnIndex("data"))) this.ATK = cursor.getInt(cursor.getColumnIndex("data"));
                    else foundData = false;
                    cursor.close();
                }
                if (!foundData) {
                    keysNotFound.add("Monster.ATK");
                }

                foundData = false;
                query = "SELECT monster.DEF as data FROM monster WHERE id = " + this.id + ";";
                cursor = db.rawQuery(query, null);
                if (cursor != null && cursor.moveToFirst()) {
                    foundData = true;
                    if (!cursor.isNull(cursor.getColumnIndex("data"))) this.DEF = cursor.getInt(cursor.getColumnIndex("data"));
                    else foundData = false;
                    cursor.close();
                }
                if (!foundData) {
                    keysNotFound.add("Monster.DEF");
                }

                foundData = false;
                query = "SELECT monster.type as data FROM monster WHERE id = " + this.id + ";";
                cursor = db.rawQuery(query, null);
                if (cursor != null && cursor.moveToFirst()) {
                    foundData = true;
                    String typeID = enumCBSearch("Type", cursor.getInt(cursor.getColumnIndex("data")));
                    this.race = typeID;
                    cursor.close();
                }
                if (!foundData) {
                    keysNotFound.add("Monster.type");
                }

                foundData = false;
                query = "SELECT monster.status as data FROM monster WHERE id = " + this.id + ";";
                cursor = db.rawQuery(query, null);
                if (cursor != null && cursor.moveToFirst()) {
                    foundData = true;
                    if (!cursor.isNull(cursor.getColumnIndex("data"))) this.level = cursor.getInt(cursor.getColumnIndex("data"));
                    else foundData = false;
                    cursor.close();
                }
                if (!foundData) {
                    keysNotFound.add("Monster.status");
                }

                foundData = false;
                query = "SELECT pendulum.scale as data FROM pendulum WHERE id = " + this.id + ";";
                cursor = db.rawQuery(query, null);
                if (cursor != null && cursor.moveToFirst()) {
                    foundData = true;
                    if (!cursor.isNull(cursor.getColumnIndex("data"))) this.pendulumScale = cursor.getInt(cursor.getColumnIndex("data"));
                    else foundData = false;
                    cursor.close();
                }
                if (!foundData) {
                    keysNotFound.add("Pendulum.scale");
                }
            }

        } catch (Exception e) {
            Log.e("Database Error", e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        db.close();
    }


    @SuppressLint({"Range", "Recycle"})
    public void addMeToTheCB(String profileName) {
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        try {
            ContentValues values2 = new ContentValues();
            values2.put("name", this.name);
            this.id = (int)db.insert("card", null, values2);
            if (monsterCard) {
                ContentValues values3 = new ContentValues();
                values3.put("id", this.id);
                db.insert("Monster", null, values3);
            }
                try {
                    // If 'Card.name' isn't in the 'keysNotFound' list, update the card name
                    if (!keysNotFound.contains("Card.name")) {
                        String query = "UPDATE Card SET name = '" + this.name.replace("'", "''") + "' WHERE ID = " + this.id + ";";
                        Cursor tempCursor = db.rawQuery(query, null);
                        tempCursor.close();
                    }
                    // If 'Card.description' isn't in the 'keysNotFound' list, update the card description
                    if (!keysNotFound.contains("Card.description")) {
                        ContentValues cv = new ContentValues();
                        cv.put("description", this.description);
                        db.update("card", cv, "id = ?", new String[]{Integer.toString(this.id)});
                    }

                    // If 'Card.attribute' isn't in the 'keysNotFound' list, update the card attribute
                    if (!keysNotFound.contains("Card.attribute")) {
                        int attributeID = enumCBSearch("attribute", this.attribute);
                        ContentValues cv = new ContentValues();
                        cv.put("attribute", attributeID);
                        db.update("card", cv, "id = ?", new String[]{Integer.toString(this.id)});
                    }

                    // If 'Card.attribute' isn't in the 'keysNotFound' list, update the card attribute
                    if (!keysNotFound.contains("Rarity")) {
                        int rarityID = enumCBSearch("rarity", this.Rarity);
                        ContentValues cv = new ContentValues();
                        cv.put("rarity", rarityID);
                        cv.put("card", this.id);
                        db.insert("Card_has_Rarity", null, cv);
                    }


                    // If 'SpellType' isn't in the 'keysNotFound' list, insert into 'Card_has_SpellType'
                    if (!keysNotFound.contains("SpellType")) {
                        int spellTypeID = enumCBSearch("SpellType", this.spellTrapType);
                        ContentValues cv = new ContentValues();
                        cv.put("spellType", spellTypeID);
                        cv.put("card", this.id);
                        db.insert("Card_has_SpellType", null, cv);
                    }

                    if (linkMarkers != null) {
                        for (int mark : this.linkMarkers) {
                            ContentValues cv = new ContentValues();
                            cv.put("linkArrow", mark);
                            cv.put("card", this.id);
                            db.insert("Card_has_LinkArrow", null, cv);
                        }
                    }

                    if (monsterCard) {
                        if (!keysNotFound.contains("Monster.ATK")) {
                            ContentValues cv = new ContentValues();
                            cv.put("ATK", this.ATK);
                            db.update("Monster", cv, "id = ?", new String[]{Integer.toString(this.id)});
                        }
                        if (!keysNotFound.contains("Monster.DEF")) {
                            ContentValues cv = new ContentValues();
                            cv.put("DEF", this.DEF);
                            db.update("Monster", cv, "id = ?", new String[]{Integer.toString(this.id)});
                        }
                        if (!keysNotFound.contains("Monster.status")) {
                            ContentValues cv = new ContentValues();
                            cv.put("status", this.level);
                            db.update("Monster", cv, "id = ?", new String[]{Integer.toString(this.id)});
                        }
                        if (!keysNotFound.contains("Monster.type")) {
                            int raceID = enumCBSearch("Type", this.race);
                            ContentValues cv = new ContentValues();
                            cv.put("type", raceID);
                            db.update("Monster", cv, "id = ?", new String[]{Integer.toString(this.id)});
                        }
                        if (!keysNotFound.contains("Pendulum.scale")) {
                            ContentValues cv = new ContentValues();
                            cv.put("id", this.id);
                            cv.put("scale", this.pendulumScale);
                            db.insert("Pendulum", null, cv);
                        }
                    }

                } catch (Exception e) {
                    Log.e("Database Error", e.getMessage());
                }

        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
        } finally {
            // Close the database when done
            db.close();
        }
    }

    public void FetchCardsImages(String pageName, int currentScore, int highScore) {
        String nameConverted = removeNonAlphanumeric(this.name) + "-MADU-EN-VG-artwork.png";
        new Thread(() -> {
            try {
                String apiUrl = "https://yugipedia.com/api.php?action=query&format=json&prop=imageinfo&iiprop=url&titles=File:"+nameConverted+"&*";;
                URL url = new URL(apiUrl);
                Log.d("FetchCardsImages", apiUrl);

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
                    Log.d("Yugipedia-Image", "API Response: " + response.toString());

                    List<String> list = new ArrayList<>(returnCardNames((response.toString())));
                    String fluff = "https://yugipedia.com/wiki/";
                    String foundUrl = "";
                    for (String str : list) {
                        foundUrl = str.substring(1);
                        Log.d("Yugipedia-Image", "Item: " + str);
                    }
                    Log.d("Yugipedia-Image", "Found URL: " + foundUrl);
                    downloadApplyAndContinue(foundUrl, currentScore, highScore);

                } else {
                    Log.e("Yugipedia-Image", "Error: " + responseCode);
                }
                connection.disconnect();
            } catch (Exception e) {
                Log.e("Yugipedia-Image", "Exception: " + e.getMessage(), e);
            }
        }).start();
    }


    public void logMyself(String when) {
        Log.d(TAG, "At: " + when);
        Log.d(TAG, "Rarity: " + Rarity);
        Log.d(TAG, "id: " + id);
        Log.d(TAG, "Name: " + name);
        Log.d(TAG, "Distinction: " + distinction);
        Log.d(TAG, "Description: " + description);
        Log.d(TAG, "Attribute: " + attribute);
        Log.d(TAG, "Spell Trap Type: " + spellTrapType);
        Log.d(TAG, "Password: " + password);

        Log.d(TAG, "Is it a Monster: " + monsterCard);

        logList("Mentions", mentions);
        logList("Archetypes", archetype);
        logList("Link Markers", linkMarkers);

        if (monsterCard) {
            if (!keysNotFound.contains("Monster.ATK")) Log.d(TAG, "ATK: " + ATK);
            if (!keysNotFound.contains("Monster.DEF")) Log.d(TAG, "DEF: " + DEF);
            if (!keysNotFound.contains("Monster.status")) Log.d(TAG, "Level: " + level);
            Log.d(TAG, "Race: " + race);
            if (!keysNotFound.contains("Pendulum.scale")) Log.d(TAG, "Scale: " + pendulumScale);
        }

        try {
            if (!hasLanguages.isEmpty()) {
                Log.d(TAG, "--Languages--");
                for (String lang : hasLanguages) {
                    Log.d(TAG, lang);
                }
            }
        }
        catch(Exception e) {
            Log.d(TAG,  "Languages is empty!");
        }

    }

    private void logList(String label, List<?> list) {
        try {
            if (list.size() == 0) {
                Log.d(TAG, label + " is empty!");
                return;
            }
            String builder = label + ": ";
            for (Object item : list) {
                builder += item.toString() + "|";
            }
            if (list.size() > 0) builder.substring(0, builder.length() - 1);  // Remove last '|'
            Log.d(TAG, builder.toString());
        }
        catch(Exception e) {
            Log.d(TAG, label + " is empty!");
            return;
        }
    }

    // First method: To search for an 'id' by 'name' in a table
    @SuppressLint({"Range", "Recycle"})
    public int enumCBSearch(String enumName, String value) {
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);

        int result = -1;

        value = value.replace("'", "''"); // Escape single quotes to prevent SQL injection
        Log.d("EnumFind", "Trying to find: " + enumName);
        Cursor cursor = null;
        try {
            // Try to find the 'id' of the given 'value' from the table
            String query = "select id from " + enumName + " where name = '"+value+"';";
            cursor = db.rawQuery(query, null);

            // If a result is found, get the 'id'
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getInt(cursor.getColumnIndex("id"));
                Log.d("EnumFind", "Enum found: " + result);
            }

            // If no result is found, insert a new record and get the last inserted row ID
            if (result == -1) {
                ContentValues cv = new ContentValues();
                cv.put("name", value);
                result = (int)db.insert(enumName, null, cv);
            }
        } catch (Exception e) {
            Log.e("Database Error", e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close(); // Ensure the cursor is closed after use
            }
            db.close(); // Close the database connection
        }

        return result;
    }

    // Second method: To search for a 'name' by 'id' in a table
    @SuppressLint("Range")
    public String enumCBSearch(String enumName, int value) {
        String result = "";
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        Log.d("EnumFind", "Trying to find: " + enumName);
        Cursor cursor = null;
        try {
            // Try to find the 'name' of the given 'id' from the table
            String query = "SELECT name FROM " + enumName + " WHERE id = " + value + ";";
            cursor = db.rawQuery(query, null);

            // If a result is found, get the 'name'
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex("name"));
                Log.d("EnumFind", "Enum Found: " + result);
            }
        } catch (Exception e) {
            Log.e("Database Error", e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close(); // Ensure the cursor is closed after use
            }
            db.close(); // Close the database connection
        }

        return result;
    }


    public String setTypeOfQuestions() {
        List<String> typeOfQuestion = new ArrayList<>();
        SqliteHandler SqliteHandler = new SqliteHandler(context);

        if (SqliteHandler.getNumberOf(true) >= 50) {
            typeOfQuestion.add("Name");
        }
        typeOfQuestion.add("Rarity");
        int attributeID = enumCBSearch("attribute", this.attribute);
        if (attributeID >= 1 && attributeID <= 7) typeOfQuestion.add("Monster Attribute");
        else typeOfQuestion.add("SpellTrap Type");
        if (!keysNotFound.contains("LinkArrow")) typeOfQuestion.add("LinkArrow");
        if (monsterCard) {
            if (!keysNotFound.contains("Monster.status")) typeOfQuestion.add("Monster.status");
            if (!keysNotFound.contains("Monster.type")) typeOfQuestion.add("Monster.type");
            if (!keysNotFound.contains("Pendulum.scale")) typeOfQuestion.add("Pendulum.scale");
            if (SqliteHandler.getNumberOf(false) >= 50) {
                typeOfQuestion.add("ATK");
                typeOfQuestion.add("DEF");
            }
        }

        Collections.shuffle(typeOfQuestion);
        String[] options = typeOfQuestion.toArray(new String[0]);

        return options[0];
    }

    public String[] generateQuestions(String typeOfQuestion) {
        Log.d("generateQuestions", "generateQuestions: " + typeOfQuestion);
        SqliteHandler SqliteHandler = new SqliteHandler(context);
        List<String> toReturn = new ArrayList<>();
        if (Objects.equals(typeOfQuestion, "Rarity")) {
            toReturn.add(this.Rarity);
            if (!Objects.equals(this.Rarity, "Common")) toReturn.add("Common");
            if (!Objects.equals(this.Rarity, "Rare")) toReturn.add("Rare");
            if (!Objects.equals(this.Rarity, "Super Rare")) toReturn.add("Super Rare");
            if (!Objects.equals(this.Rarity, "Ultra Rare")) toReturn.add("Ultra Rare");
        }
        else if (Objects.equals(typeOfQuestion, "Name")) {
            toReturn.add(this.name);
            List<String> cardNames = SqliteHandler.getCardNames();
            for (String str : cardNames) {
                if (!Objects.equals(this.name, str)) toReturn.add(str);
            }
        }
        else if (Objects.equals(typeOfQuestion, "Monster Attribute")) {
            toReturn.add(this.attribute);
            if (!Objects.equals(this.attribute, "LIGHT")) toReturn.add("LIGHT");
            if (!Objects.equals(this.attribute, "DARK")) toReturn.add("DARK");
            if (!Objects.equals(this.attribute, "FIRE")) toReturn.add("FIRE");
            if (!Objects.equals(this.attribute, "WATER")) toReturn.add("WATER");
            if (!Objects.equals(this.attribute, "WIND")) toReturn.add("WIND");
            if (!Objects.equals(this.attribute, "EARTH")) toReturn.add("EARTH");
            if (!Objects.equals(this.attribute, "DIVINE")) toReturn.add("DIVINE");
        }
        else if (Objects.equals(typeOfQuestion, "SpellTrap Type")) {
            toReturn.add(this.spellTrapType);
            if (!Objects.equals(this.spellTrapType, "Normal")) toReturn.add("Normal");
            if (!Objects.equals(this.spellTrapType, "Continuous")) toReturn.add("Continuous");
            if (!Objects.equals(this.spellTrapType, "Counter")) toReturn.add("Counter");
            if (!Objects.equals(this.spellTrapType, "Field")) toReturn.add("Field");
            if (!Objects.equals(this.spellTrapType, "Equip")) toReturn.add("Equip");
            if (!Objects.equals(this.spellTrapType, "Quick-Play")) toReturn.add("Quick-Play");
            if (!Objects.equals(this.spellTrapType, "Ritual")) toReturn.add("Ritual");
        }
        else if (Objects.equals(typeOfQuestion, "LinkArrow")) {
            String tottalLink = linkMarkers.size() + "";
            toReturn.add(tottalLink);
            if (!Objects.equals(tottalLink, "1")) toReturn.add("1");
            if (!Objects.equals(tottalLink, "2")) toReturn.add("2");
            if (!Objects.equals(tottalLink, "3")) toReturn.add("3");
            if (!Objects.equals(tottalLink, "4")) toReturn.add("4");
            if (!Objects.equals(tottalLink, "5")) toReturn.add("5");
            if (!Objects.equals(tottalLink, "6")) toReturn.add("6");
        }
        else if (Objects.equals(typeOfQuestion, "Monster.status")) {
            String tottalLink = this.level + "";
            toReturn.add(tottalLink);
            if (!Objects.equals(tottalLink, "1")) toReturn.add("1");
            if (!Objects.equals(tottalLink, "2")) toReturn.add("2");
            if (!Objects.equals(tottalLink, "3")) toReturn.add("3");
            if (!Objects.equals(tottalLink, "4")) toReturn.add("4");
            if (!Objects.equals(tottalLink, "5")) toReturn.add("5");
            if (!Objects.equals(tottalLink, "6")) toReturn.add("6");
            if (!Objects.equals(tottalLink, "7")) toReturn.add("7");
            if (!Objects.equals(tottalLink, "8")) toReturn.add("8");
            if (!Objects.equals(tottalLink, "9")) toReturn.add("9");
            if (!Objects.equals(tottalLink, "10")) toReturn.add("10");
            if (!Objects.equals(tottalLink, "11")) toReturn.add("11");
            if (!Objects.equals(tottalLink, "12")) toReturn.add("12");
        }
        else if (Objects.equals(typeOfQuestion, "Monster.type")) {
            toReturn.add(this.race);
            List<String> types = SqliteHandler.getTypes();
            types.remove("UNKNOWN");
            for (String str : types) {
                if (!Objects.equals(this.race, str)) toReturn.add(str);
            }
        }
        else if (Objects.equals(typeOfQuestion, "ATK")) {
            String newExpression = this.ATK + " ATK";
            toReturn.add(newExpression);
            List<String> attacks = SqliteHandler.getATKorDEF(true);
            for (String str : attacks) {
                if (!Objects.equals(newExpression, str)) toReturn.add(str);
            }
        }
        else if (Objects.equals(typeOfQuestion, "DEF")) {
            String newExpression = this.DEF + " DEF";
            toReturn.add(newExpression);
            List<String> attacks = SqliteHandler.getATKorDEF(false);
            for (String str : attacks) {
                if (!Objects.equals(newExpression, str)) toReturn.add(str);
            }
        }
        else if (Objects.equals(typeOfQuestion, "Pendulum.scale")) {
            String tottalLink = this.pendulumScale + "";
            toReturn.add("Scale " + tottalLink);
            if (!Objects.equals(tottalLink, "0")) toReturn.add("Scale 0");
            if (!Objects.equals(tottalLink, "1")) toReturn.add("Scale 1");
            if (!Objects.equals(tottalLink, "2")) toReturn.add("Scale 2");
            if (!Objects.equals(tottalLink, "3")) toReturn.add("Scale 3");
            if (!Objects.equals(tottalLink, "4")) toReturn.add("Scale 4");
            if (!Objects.equals(tottalLink, "5")) toReturn.add("Scale 5");
            if (!Objects.equals(tottalLink, "6")) toReturn.add("Scale 6");
            if (!Objects.equals(tottalLink, "7")) toReturn.add("Scale 7");
            if (!Objects.equals(tottalLink, "8")) toReturn.add("Scale 8");
            if (!Objects.equals(tottalLink, "9")) toReturn.add("Scale 9");
            if (!Objects.equals(tottalLink, "10")) toReturn.add("Scale 10");
            if (!Objects.equals(tottalLink, "11")) toReturn.add("Scale 11");
            if (!Objects.equals(tottalLink, "12")) toReturn.add("Scale 12");
            if (!Objects.equals(tottalLink, "13")) toReturn.add("Scale 13");
        }

        String[] array = toReturn.toArray(new String[0]);
        return array;
    }


    private List<String> returnCardNames(String response) {
        List<String> list = new ArrayList<>();
        String findIt = "\"url\":";
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


    public void downloadApplyAndContinue(String imageUrl, int currentScore, int highScore) {
        new Thread(() -> {
            try {
                URL url = new URL(imageUrl);
                Log.d("FetchImage-END", imageUrl);

                // Open a connection to the URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("ApplyingTheImage", "Before:" + this.id);
                    InputStream inputStream = connection.getInputStream();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, length);
                    }
                    inputStream.close();

                    // Convert the ByteArrayOutputStream to byte array
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();
                    Log.d("FetchImage-END", "Downloaded image size: " + imageBytes.length + " bytes");

                    String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
                    SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
                    try {
                        try {
                            ContentValues cv = new ContentValues();
                            cv.put("image", imageBytes);
                            cv.put("id", this.id);
                            db.update("card", cv, "id = ?", new String[]{Integer.toString(this.id)});
                        } catch (Exception e) {
                            Log.e("Database Error", e.getMessage());
                        }
                        Log.d("ApplyingTheImage", "After:" + this.id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        db.close();
                    }

                    String optionsToUse = setTypeOfQuestions();
                    Log.d("MakeTheQuestion", optionsToUse);
                    String[] options = generateQuestions(optionsToUse);
                    Log.d("MakeTheQuestion", options[0] + " " + options[1] + " " + options[2] + " " + options[3]);

                    SqliteHandler SqliteHandler = new SqliteHandler(context);
                    SqliteHandler.writeTheNewQuestion(currentScore, highScore, options, this.id);


                } else {
                    Log.e("FetchImage-END", "Error: " + responseCode);
                }
                connection.disconnect();
            } catch (Exception e) {
                Log.e("FetchImage-END", "Exception: " + e.getMessage(), e);
            }
        }).start();
    }

    public String removeNonAlphanumeric(String input) {
        return input.replaceAll("[^a-zA-Z0-9]", "");
    }

    public int getID() {
        return  this.id;
    }
}

