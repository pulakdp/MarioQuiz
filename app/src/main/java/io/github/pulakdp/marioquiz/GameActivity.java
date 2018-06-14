package io.github.pulakdp.marioquiz;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import io.github.pulakdp.marioquiz.data.Question;
import io.github.pulakdp.marioquiz.data.QuestionsApiClient;
import io.github.pulakdp.marioquiz.data.QuestionsApiInterface;
import io.github.pulakdp.marioquiz.data.QuestionsResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameActivity extends AppCompatActivity {

    public static final String LOG_TAG = GameActivity.class.getSimpleName();
    public static final int speedX = 10;
    public static float speedY = 0f;
    public static final float gravity = 0.855f;

    private ImageView pipe1;
    private ImageView pipe2;
    private ImageView pipe3;
    private ImageView pipe4;

    private ImageView mario;

    private TextView remainingTime;
    private TextView questionNumber;
    private TextView question;
    private TextView option1;
    private TextView option2;
    private TextView option3;

    private ConstraintLayout layout;

    int initialPos1, initialPos2, initialPos3, initialPos4;
    int marioInitialPosX;
    int marioInitialPosY;

    boolean stop = false;
    boolean onPipe = true;
    boolean worldShifting = false;

    private QuestionsApiInterface apiClient;
    private List<Question> questions;
    private int numOfQuestions;
    private int timeInSeconds;
    private int totalTime;
    private int currentQuestion = 1;
    int jumpsMade = 0;
    int rightPipe = 0;
    int counter = 0;
    boolean gameOverAnimDone = false;
    boolean gameOver = false;
    boolean pauseTimer = false;

    private Button retry;
    private Button quit;
    private Group group;

    private Handler answerCheckingHandler;
    private Runnable answerCheckingRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        initViews();

        fetchQuestions();

        layout.setOnClickListener(view -> {
            userClicked();
        });

        retry.setOnClickListener(view -> {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            layout.setOnClickListener(view1 -> userClicked());
            group.setVisibility(View.GONE);
            assignDefaultValues();
            mario.setX(marioInitialPosX);
            mario.setY(getScreenHeight() - pipe1.getHeight() - mario.getHeight());

            start();
        });
        quit.setOnClickListener(view -> finish());

        initAnCheckComponents();
    }

    private void initAnCheckComponents() {
        answerCheckingHandler = new Handler();
        answerCheckingRunnable = () -> {
            if (!stop || !onPipe || worldShifting) {
                return;
            }
            if (rightPipe == -1) {
                Log.d(LOG_TAG, "Oops!");
                return;
            }
            question.setText("");
            option1.setText("");
            option2.setText("");
            option3.setText("");
            if (onPipe && (jumpsMade == rightPipe)) {
                if (currentQuestion == 10) {
                    pauseTimer = true;
                    gameOver = true;
                    Toast.makeText(getApplicationContext(), R.string.win_message, Toast.LENGTH_SHORT).show();
                    group.setVisibility(View.VISIBLE);
                    return;
                }
                stop = true;
                worldShifting = true;
                shiftWorldAndLoadNextQuestion(rightPipe);
            } else {
                pauseTimer = true;
                gameOver = true;
                startGameOverAnimation();
            }
        };
    }

    private void assignDefaultValues() {
        stop = false;
        onPipe = true;
        pauseTimer = false;
        gameOver = false;
        worldShifting = false;
        timeInSeconds = totalTime;
        currentQuestion = 1;
        jumpsMade = 0;
        rightPipe = 0;
        counter = 0;
        gameOverAnimDone = false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initialPos1 = (int) pipe1.getX();
        initialPos2 = (int) pipe2.getX();
        initialPos3 = (int) pipe3.getX();
        initialPos4 = (int) pipe4.getX();

        mario.setX(pipe1.getWidth() / 2 - mario.getWidth() / 2);
        marioInitialPosX = (int) mario.getX();
        marioInitialPosY = (int) mario.getY();
    }

    private void initViews() {
        pipe1 = findViewById(R.id.pipe1);
        pipe2 = findViewById(R.id.pipe2);
        pipe3 = findViewById(R.id.pipe3);
        pipe4 = findViewById(R.id.pipe4);
        mario = findViewById(R.id.mario);
        layout = findViewById(R.id.constraintLayout);
        group = findViewById(R.id.group);
        retry = findViewById(R.id.retry);
        quit = findViewById(R.id.quit);

        remainingTime = findViewById(R.id.time_remaining);
        questionNumber = findViewById(R.id.question_no);
        question = findViewById(R.id.question);
        option1 = findViewById(R.id.opt1);
        option2 = findViewById(R.id.opt2);
        option3 = findViewById(R.id.opt3);
    }

    private void fetchQuestions() {

        if (!hasInternetConnection(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
            finish();
        }

        apiClient = QuestionsApiClient.getClient().create(QuestionsApiInterface.class);

        Call<QuestionsResponse> call = apiClient.getQuestions();
        call.enqueue(new Callback<QuestionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<QuestionsResponse> call, @NonNull Response<QuestionsResponse> response) {
                if (!response.isSuccessful()) {
                    int responseCode = response.code();
                    Toast.makeText(getApplicationContext(), "Can't load. Response Code: " + responseCode, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                QuestionsResponse questionsResponse = response.body();
                if (questionsResponse != null) {
                    questions = questionsResponse.getQuestions();
                    numOfQuestions = questionsResponse.getNumQuestions();
                    totalTime = questionsResponse.getTotalTime();
                    timeInSeconds = totalTime;

                    start();
                }

            }

            @Override
            public void onFailure(@NonNull Call<QuestionsResponse> call, @NonNull Throwable t) {
                if (!call.isCanceled())
                    Toast.makeText(getApplicationContext(), R.string.could_not_fetch_questions, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void start() {
        remainingTime.setText(String.format(Locale.getDefault(), "%ds", timeInSeconds));

        showQuestion();
        startTimer();
    }

    private void showQuestion() {
        questionNumber.setText(getResources().getString(R.string.question_number, currentQuestion, numOfQuestions));

        Question ques = questions.get(currentQuestion - 1);
        question.setText(ques.getQuestion());
        option1.setText(ques.getOption1());
        option2.setText(ques.getOption2());
        option3.setText(ques.getOption3());

        if (ques.getAnswer().equalsIgnoreCase(ques.getOption1()))
            rightPipe = 1;
        else if (ques.getAnswer().equalsIgnoreCase(ques.getOption2()))
            rightPipe = 2;
        else if (ques.getAnswer().equalsIgnoreCase(ques.getOption3()))
            rightPipe = 3;
        else
            rightPipe = -1;
    }

    public void userClicked() {
        if (!worldShifting && !gameOver && jumpsMade < 3 && onPipe) {
            onPipe = false;
            stop = false;
            jump();
        }
    }

    private void jump() {
        onPipe = false;
        stop = false;
        speedY = -12f;
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                mario.setX(mario.getX() + speedX);
                speedY += gravity;
                mario.setY(mario.getY() + speedY);

                for (int i = 1; i < 4; i++) {
                    if (onPipe)
                        break;

                    if ((marioInitialPosX + (i * pipe1.getWidth())) == (int) mario.getX()) {
                        jumpsMade += 1;
                        Log.d(LOG_TAG, "Stared checking. Jumps = " + jumpsMade);
                        stop = true;
                        onPipe = true;
                        startDelayedAnswerCheck();
                        break;
                    }
                }

                if (!stop) {
                    onPipe = false;
                    mario.setImageResource(R.drawable.mario_jumping);
                    mario.setScaleType(ImageView.ScaleType.FIT_XY);
                    handler.postDelayed(this, 25);
                } else {
                    onPipe = true;
                    mario.setImageResource(R.drawable.mario_standing);
                    handler.removeCallbacks(this);
                }
            }
        };
        handler.post(runnable);
    }

    private void startDelayedAnswerCheck() {
        answerCheckingHandler.removeCallbacksAndMessages(null);
        answerCheckingHandler.postDelayed(answerCheckingRunnable, 1000);
    }

    private void shiftWorldAndLoadNextQuestion(int rightPipe) {
        layout.setOnClickListener(null);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mario.setX(mario.getX() - speedX);
                pipe1.setX(pipe1.getX() - speedX);
                pipe2.setX(pipe2.getX() - speedX);
                pipe3.setX(pipe3.getX() - speedX);
                pipe4.setX(pipe4.getX() - speedX);

                if ((int) pipe1.getX() == 0 ||
                        (int) pipe2.getX() == 0 ||
                        (int) pipe3.getX() == 0 ||
                        (int) pipe4.getX() == 0) {
                    counter++;
                }

                if (pipe1.getX() < 0)
                    pipe1.setX(getScreenWidth());
                if (pipe2.getX() < 0)
                    pipe2.setX(getScreenWidth());
                if (pipe3.getX() < 0)
                    pipe3.setX(getScreenWidth());
                if (pipe4.getX() < 0)
                    pipe4.setX(getScreenWidth());

                if (counter == rightPipe) {
                    handler.removeCallbacks(this);
                    worldShifting = false;
                    reset();
                    showQuestion();
                } else {
                    worldShifting = true;
                    handler.postDelayed(this, 25);
                }
            }
        };
        handler.post(runnable);
    }

    private void reset() {
        layout.setOnClickListener(view -> userClicked());
        currentQuestion++;
        jumpsMade = 0;
        counter = 0;
        stop = false;
        rightPipe = 0;
        gameOverAnimDone = false;
        mario.setY(getScreenHeight() - pipe1.getHeight() - mario.getHeight());
    }

    private void startGameOverAnimation() {
        speedY = -6f;
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                onPipe = false;
                layout.setOnClickListener(null);
                speedY += gravity;
                mario.setY(mario.getY() + speedY);
                if (mario.getY() >= getScreenHeight()) {
                    gameOverAnimDone = true;
                    Toast.makeText(getApplicationContext(), R.string.game_over, Toast.LENGTH_SHORT).show();
                    group.setVisibility(View.VISIBLE);
                }

                if (!gameOverAnimDone)
                    handler.postDelayed(this, 25);
            }
        };
        handler.post(runnable);
    }

    private void startTimer() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                timeInSeconds--;
                if (timeInSeconds >= 0)
                    remainingTime.setText(String.format(Locale.getDefault(), "%ds", timeInSeconds));
                if (!pauseTimer && timeInSeconds > 0)
                    handler.postDelayed(this, 1000);
                else if (timeInSeconds <= 0) {
                    handler.removeCallbacks(this);
                    Toast.makeText(getApplicationContext(), R.string.time_up, Toast.LENGTH_SHORT).show();
                    group.setVisibility(View.VISIBLE);
                }
            }
        };
        handler.post(runnable);
    }

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE));

        return connectivityManager != null &&
                connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}
