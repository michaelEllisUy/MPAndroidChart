package com.github.mikephil.charting.data;

import androidx.annotation.Nullable;

public class Triplet {
    @Nullable
    String first;
    @Nullable
    String second;
    @Nullable
    String third;

    public Triplet(@Nullable String first, @Nullable String second, @Nullable String third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public Triplet(@Nullable String first, @Nullable String second) {
        this.first = first;
        this.second = second;
    }

    public Triplet(@Nullable String first) {
        this.first = first;
    }

    @Nullable
    public String getFirst() {
        return first;
    }

    @Nullable
    public String getSecond() {
        return second;
    }

    @Nullable
    public String getThird() {
        return third;
    }
}
