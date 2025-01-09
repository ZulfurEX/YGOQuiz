package com.example.ygoquiz;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SqliteHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "YGO Quiz.db";
    private static final int DB_VERSION = 1;
    private final Context context;

    public SqliteHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    //From ChatGPT to delete the DataBase - I was too lazy to write it myself
    //I had trouble with this whole thing because I had an empty DB
    //So my if wouldn't repopulate it.
    public void resetDatabase() {
        // Get the path to the current database in the app's internal storage
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        File dbFile = new File(dbPath);

        // Check if the database exists and delete it if it does
        if (dbFile.exists()) {
            if (dbFile.delete()) {
                Log.d("SQLite", "Existing database deleted.");
            } else {
                Log.e("SQLite", "Failed to delete the existing database.");
            }
        }

        // Now copy the populated database from the assets folder
        try {
            copyDatabaseFromAssets();
        } catch (IOException e) {
            Log.e("SQLite", "Error copying database from assets: " + e.getMessage());
        }
    }

    //Copy the DB that is found in main/assets
    public void copyDatabaseFromAssets() throws IOException {
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();

        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            InputStream inputStream = context.getAssets().open(DB_NAME);
            OutputStream outputStream = new FileOutputStream(dbFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        }
    }


    //A simple test
    public void openDatabase() {
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        String query = "SELECT * FROM QuizInfo";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String data = cursor.getString(cursor.getColumnIndex("option1")); // Replace with actual column name
                    Toast.makeText(context, "Data: " + data, Toast.LENGTH_SHORT).show();
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
    }

    public void loadCurrentQuiz(){
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        String query = "SELECT QuizInfo.id, QuizInfo.option1, QuizInfo.option2, QuizInfo.option3, QuizInfo.option4, QuizInfo.answerIs, QuizInfo.currentScore, QuizInfo.highScore, card.image FROM QuizInfo INNER JOIN card on card.id = QuizInfo.image ORDER BY QuizInfo.id DESC LIMIT 1;";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String option1 = cursor.getString(cursor.getColumnIndex("option1"));
                    @SuppressLint("Range") String option2 = cursor.getString(cursor.getColumnIndex("option2"));
                    @SuppressLint("Range") String option3 = cursor.getString(cursor.getColumnIndex("option3"));
                    @SuppressLint("Range") String option4 = cursor.getString(cursor.getColumnIndex("option4"));
                    @SuppressLint("Range") int answer = cursor.getInt(cursor.getColumnIndex("answerIs"));
                    @SuppressLint("Range") int score = cursor.getInt(cursor.getColumnIndex("currentScore"));
                    @SuppressLint("Range") int highScore = cursor.getInt(cursor.getColumnIndex("highScore"));
                    @SuppressLint("Range") byte[] imageBytes = cursor.getBlob(cursor.getColumnIndex("image"));
                    String[] options = new String[]{option1, option2, option3, option4};
                    if (MainActivity.instance != null) {
                        MainActivity.instance.setOptionText(options, answer, score, highScore, imageBytes);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();

    }


    public void correctGuess(int buttonId) {
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        String query = "SELECT QuizInfo.id, QuizInfo.option1, QuizInfo.option2, QuizInfo.option3, QuizInfo.option4, QuizInfo.answerIs, QuizInfo.currentScore, QuizInfo.highScore, card.image FROM QuizInfo INNER JOIN card on card.id = QuizInfo.image ORDER BY QuizInfo.id DESC LIMIT 1;";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String option1 = cursor.getString(cursor.getColumnIndex("option1"));
                    @SuppressLint("Range") String option2 = cursor.getString(cursor.getColumnIndex("option2"));
                    @SuppressLint("Range") String option3 = cursor.getString(cursor.getColumnIndex("option3"));
                    @SuppressLint("Range") String option4 = cursor.getString(cursor.getColumnIndex("option4"));
                    @SuppressLint("Range") int answer = cursor.getInt(cursor.getColumnIndex("answerIs"));
                    String theAnswerWas = option1;
                    if (answer == 1) theAnswerWas = option2;
                    else if (answer == 2) theAnswerWas = option3;
                    else if (answer == 3) theAnswerWas = option4;
                    if (MainActivity.instance != null) {
                        if (answer == buttonId) MainActivity.instance.guess(true, theAnswerWas);
                        else MainActivity.instance.guess(false, theAnswerWas);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

    }

    public void generateNewQuestion(int currentScore, int highScore) {
        Yugipedia yugipedia = new Yugipedia(context);
        yugipedia.getCardName(currentScore, highScore);
    }


    @SuppressLint("Range")
    public int getNumberOfCards() {
        int numberToReturn = -1;
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        String query = "SELECT SendTime.Count as amount FROM SendTime;";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    numberToReturn = cursor.getInt(cursor.getColumnIndex("amount"));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        ContentValues cv = new ContentValues();
        cv.put("Count", numberToReturn + 1);
        db.update("SendTime", cv, "count = ?", new String[]{Integer.toString(numberToReturn)});

        db.close();
        return  numberToReturn;
    }

    @SuppressLint("Range")
    public int getRandomCardFromDB() {
        int numberToReturn = -1;
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        String query = "SELECT card.id as id FROM card ORDER BY RANDOM() LIMIT 1;";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    numberToReturn = cursor.getInt(cursor.getColumnIndex("id"));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
        return  numberToReturn;
    }

    public void writeTheNewQuestion(int currentScore, int highScore, String[] options, int imageData) {
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);

        String answerString = options[0];
        String[] finalOptions = new String[4];
        finalOptions[0] = answerString;
        List<String> list = new ArrayList<>(List.of(options));
        list.remove(answerString);
        Collections.shuffle(list);
        for (int i = 1; i < 4; i++) {
            String removedString = list.remove(0);
            finalOptions[i] = removedString;
        }
        List<String> list2 = new ArrayList<>(List.of(finalOptions));
        Collections.shuffle(list2);
        int answerIs = list2.indexOf(answerString);
        options = list2.toArray(new String[0]);

        ContentValues values = new ContentValues();
        values.put("image", imageData);
        values.put("option1", options[0]);
        values.put("option2", options[1]);
        values.put("option3", options[2]);
        values.put("option4", options[3]);
        values.put("answerIs", answerIs);
        values.put("currentScore", currentScore);
        values.put("highScore", highScore);

        db.insert("QuizInfo", null, values);
        db.execSQL("DELETE FROM QuizInfo WHERE id != (SELECT MAX(id) FROM QuizInfo);");
        db.close();

        loadCurrentQuiz();
    }

    @SuppressLint("Range")
    public int doesItExist(String cardPage){
        int numberToReturn = -1;
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        String query = "SELECT id FROM card WHERE card.name = '" + cardPage + "';";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    numberToReturn = cursor.getInt(cursor.getColumnIndex("id"));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        Log.d("doesItExist", "It says: " + numberToReturn + " for " + cardPage);
        db.close();
        return numberToReturn;
    }

    @SuppressLint("Range")
    public int getNumberOf(boolean cardMonster) {
        int numberToReturn = -1;
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        String query = "SELECT COUNT(*) as amount FROM card;";
        if (!cardMonster) query = "SELECT COUNT(*) as amount FROM monster;";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    numberToReturn = cursor.getInt(cursor.getColumnIndex("amount"));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
        return numberToReturn;
    }

    @SuppressLint("Range")
    public List<String> getCardNames() {
        List<String> list = new ArrayList<>();
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        String query = "SELECT card.name as amount FROM card;";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursor.getString(cursor.getColumnIndex("amount")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
        return list;
    }

    @SuppressLint("Range")
    public List<String> getTypes() {
        List<String> list = new ArrayList<>();
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        String query = "SELECT name as amount FROM Type;";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursor.getString(cursor.getColumnIndex("amount")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
        return list;
    }

    @SuppressLint("Range")
    public List<String> getATKorDEF(boolean atkDEF) {
        List<String> list = new ArrayList<>();
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        String query = "SELECT DISTINCT ATK as amount FROM Monster;";
        if (!atkDEF) query = "SELECT DISTINCT DEF as amount FROM Monster;";
        Cursor cursor = db.rawQuery(query, null);
        String toAddto = " ATK";
        if (!atkDEF) toAddto = " DEF";

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(cursor.getInt(cursor.getColumnIndex("amount")) + toAddto);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
        return list;
    }

    @SuppressLint("Range")
    public int getTheCorrectAnswer() {
        int numberToReturn = -1;
        String dbPath = context.getDatabasePath(DB_NAME).getAbsolutePath();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        String query = "SELECT QuizInfo.answerIs as answer FROM QuizInfo ORDER BY QuizInfo.id DESC LIMIT 1;";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    numberToReturn = cursor.getInt(cursor.getColumnIndex("answer"));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
        return  numberToReturn;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
