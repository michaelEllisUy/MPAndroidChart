package com.github.mikephil.charting.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Triplet {
    @NonNull
    String first;
    @NonNull
    String second;
    @Nullable
    String third;

    public Triplet(@NonNull String first, @NonNull String second, @Nullable String third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public Triplet(@NonNull String first, @NonNull String second) {
        this.first = first;
        this.second = second;
    }

    public Triplet(@NonNull String first) {
        this.first = first;
    }

    @NonNull
    public String getFirst() {
        return first;
    }

    @NonNull
    public String getSecond() {
        return second;
    }

    @Nullable
    public String getThird() {
        return third;
    }
}
