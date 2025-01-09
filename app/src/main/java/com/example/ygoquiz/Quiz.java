package com.example.ygoquiz;

import static com.example.ygoquiz.Yugipedia.isInternetAvailable;

import android.content.Context;
import android.widget.Button;
import android.widget.Toast;

public class Quiz {
    public static void showMessage(Context context, int answerNum) {
        String[] convertIDs = new String[]{"A", "B", "C", "D"};
        SqliteHandler dbHelper = new SqliteHandler(context);
        try {
            MainActivity.instance.turnAllButtons(false);
            //dbHelper.resetDatabase();
            dbHelper.copyDatabaseFromAssets();
            //dbHelper.loadCurrentQuiz();
            //dbHelper.openDatabase();
            dbHelper.correctGuess(answerNum);
        }
        catch(Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean startDowloading(Context context, boolean inProcess) {
        //If the button has already been pressed
        boolean toReturn = inProcess;
        if (toReturn) return true;

        SqliteHandler dbHelper = new SqliteHandler(context);
        int numberOFCards = dbHelper.getNumberOf(true);;
        if (numberOFCards < 100) {
            boolean isOnline = isInternetAvailable(context);
            if (isOnline) toReturn = true;
            else return false;
        }
        else {
            return false;
        }

        try {
            MainActivity.instance.turnAllButtons(false);
            dbHelper.copyDatabaseFromAssets();
            int answer = dbHelper.getTheCorrectAnswer();
            answer++;
            if (answer == 4) answer = 0;
            dbHelper.correctGuess(answer);
        }
        catch(Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return toReturn;
    }
}
