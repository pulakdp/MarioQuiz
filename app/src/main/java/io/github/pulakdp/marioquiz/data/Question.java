package io.github.pulakdp.marioquiz.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Author: PulakDebasish
 */
public class Question {

    @SerializedName("question")
    @Expose
    public String question;
    @SerializedName("option1")
    @Expose
    public String option1;
    @SerializedName("option2")
    @Expose
    public String option2;
    @SerializedName("option3")
    @Expose
    public String option3;
    @SerializedName("answer")
    @Expose
    public String answer;

    public String getQuestion() {
        return question;
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }

    public String getOption3() {
        return option3;
    }

    public String getAnswer() {
        return answer;
    }

    @Override
    public String toString() {
        return "Question{" +
                "question='" + question + '\'' +
                ", option1='" + option1 + '\'' +
                ", option2='" + option2 + '\'' +
                ", option3='" + option3 + '\'' +
                ", answer='" + answer + '\'' +
                '}';
    }
}
