package com.student.learncraft;

import java.util.List;

public class ResultDataHolder {

    private static List<Integer> userAnswers;

    public static void setUserAnswers(List<Integer> answers) {
        userAnswers = answers;
    }

    public static List<Integer> getUserAnswers() {
        return userAnswers;
    }

    public static void clear() {
        userAnswers = null;
    }
}
