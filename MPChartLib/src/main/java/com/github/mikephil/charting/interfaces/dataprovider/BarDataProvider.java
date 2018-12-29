package com.github.mikephil.charting.interfaces.dataprovider;

import com.github.mikephil.charting.data.BarData;

public interface BarDataProvider extends BarLineScatterCandleBubbleDataProvider {

    BarData getBarData();

    boolean isDrawBarShadowEnabled();

    boolean areBarsRectangles();

    boolean isDrawValueAboveBarEnabled();

    boolean isHighlightFullBarEnabled();
}
