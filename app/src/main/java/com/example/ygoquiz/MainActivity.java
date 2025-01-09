    package com.example.ygoquiz;

import static com.example.ygoquiz.Yugipedia.isInternetAvailable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

    public class MainActivity extends AppCompatActivity {

    public static MainActivity instance;

    private Button option1;
    private Button option2;
    private Button option3;
    private Button option4;
    private TextView scoreText;
    private ImageView cardImage;

    private int scoreValue;
    private int highScoreValue;

    private boolean downloadImagesON = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Adding click events to buttons from A to D

        Button answerButton = findViewById(R.id.answer_a);
        this.option1 = answerButton;
        answerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Quiz.showMessage(MainActivity.this, 0);
            }
        });

        Button answerButtonB = findViewById(R.id.answer_b);
        this.option2 = answerButtonB;
        answerButtonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Quiz.showMessage(MainActivity.this, 1);
            }
        });

        Button answerButtonC = findViewById(R.id.answer_c);
        this.option3 = answerButtonC;
        answerButtonC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Quiz.showMessage(MainActivity.this, 2);
            }
        });

        Button answerButtonD = findViewById(R.id.answer_d);
        this.option4 = answerButtonD;
        answerButtonD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Quiz.showMessage(MainActivity.this, 3);
            }
        });

        Button dowloadButton = findViewById(R.id.download_img);
        dowloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadImagesON = Quiz.startDowloading(MainActivity.this, downloadImagesON);
            }
        });

        CheckBox hideButtons = findViewById(R.id.button_hider);
        hideButtons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    answerButton.setVisibility(View.GONE);
                    answerButtonB.setVisibility(View.GONE);
                    answerButtonC.setVisibility(View.GONE);
                    answerButtonD.setVisibility(View.GONE);
                }
                else {
                    answerButton.setVisibility(View.VISIBLE);
                    answerButtonB.setVisibility(View.VISIBLE);
                    answerButtonC.setVisibility(View.VISIBLE);
                    answerButtonD.setVisibility(View.VISIBLE);
                }

            }
        });

        this.scoreText = findViewById(R.id.score);
        this.cardImage = findViewById(R.id.card_image);

        instance = this;

        SqliteHandler dbHelper = new SqliteHandler(this);
        try {
            dbHelper.copyDatabaseFromAssets();
        } catch (IOException e) {
            Log.e("CreatingDB", "onCreate: " + e.getMessage());
        }


        updateDowloadButtonInfo();

        dbHelper.loadCurrentQuiz();
    }

    public void setOptionText(String[] options, int correctAnswer, int score, int highScore, byte[] image){
        runOnUiThread(() -> {
            option1.setText(options[0]);
            option2.setText(options[1]);
            option3.setText(options[2]);
            option4.setText(options[3]);
            if (score == 0) scoreText.setText("High Score:" + highScore);
            else scoreText.setText("Answered:" + score);
            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            cardImage.setImageBitmap(bitmap);
            scoreValue = score;
            highScoreValue = highScore;
        });
        turnAllButtons(true);
    }

    public void guess(boolean correct, String theAnswerWas) {
        if (correct) {
            scoreValue++;
            if (highScoreValue < scoreValue) highScoreValue = scoreValue;
        }
        else {
            scoreValue = 0;
        }

        SqliteHandler dbHelper = new SqliteHandler(this);
        dbHelper.generateNewQuestion(scoreValue, highScoreValue);

        if (!correct && !downloadImagesON) Toast.makeText(this, "The answer was " + theAnswerWas, Toast.LENGTH_LONG).show();
    }

    public void turnAllButtons(boolean onOff) {
        Log.d("Buttons!!!!", "Are going to be: " + onOff);
        
        runOnUiThread(() -> {
            Button answerButtonA = findViewById(R.id.answer_a);
            Button answerButtonB = findViewById(R.id.answer_b);
            Button answerButtonC = findViewById(R.id.answer_c);
            Button answerButtonD = findViewById(R.id.answer_d);

            answerButtonA.setEnabled(onOff);
            answerButtonB.setEnabled(onOff);
            answerButtonC.setEnabled(onOff);
            answerButtonD.setEnabled(onOff);

            if (onOff) {
                updateDowloadButtonInfo();
                if (downloadImagesON) Quiz.startDowloading(MainActivity.this, false);
            }
        });

    }

    public void updateDowloadButtonInfo() {
        runOnUiThread(() -> {
            Button downloadButton = findViewById(R.id.download_img);
            SqliteHandler dbHelper = new SqliteHandler(this);
            int numberOFCards = dbHelper.getNumberOf(true);
            if (numberOFCards < 100) {
                boolean isOnline = isInternetAvailable(this);
                if (isOnline) {
                    downloadButton.setText("Download " + (100 - numberOFCards));
                }
                else {
                    downloadButton.setVisibility(View.GONE);
                }
            } else {
                downloadButton.setVisibility(View.GONE);
            }
        });
    }
}