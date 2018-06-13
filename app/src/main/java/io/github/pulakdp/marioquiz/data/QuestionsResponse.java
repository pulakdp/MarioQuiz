package io.github.pulakdp.marioquiz.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Author: PulakDebasish
 */
public class QuestionsResponse {

    @SerializedName("num_questions")
    @Expose
    public Integer numQuestions;
    @SerializedName("total_time")
    @Expose
    public Integer totalTime;
    @SerializedName("questions")
    @Expose
    public List<Question> questions = null;

    public Integer getNumQuestions() {
        return numQuestions;
    }

    public Integer getTotalTime() {
        return totalTime;
    }

    public List<Question> getQuestions() {
        return questions;
    }
}
