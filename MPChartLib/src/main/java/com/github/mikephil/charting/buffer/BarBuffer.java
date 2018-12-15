
package com.github.mikephil.charting.buffer;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

public class BarBuffer extends AbstractBuffer<IBarDataSet> {

    protected int mDataSetIndex = 0;
    protected int mDataSetCount = 1;
    protected boolean mContainsStacks = false;
    protected boolean mInverted = false;

    /**
     * width of the bar on the x-axis, in values (not pixels)
     */
    protected float mBarWidth = 1f;

    public BarBuffer(int size, int dataSetCount, boolean containsStacks) {
        super(size);
        this.mDataSetCount = dataSetCount;
        this.mContainsStacks = containsStacks;
    }

    public void setBarWidth(float barWidth) {
        this.mBarWidth = barWidth;
    }

    public void setDataSet(int index) {
        this.mDataSetIndex = index;
    }

    public void setInverted(boolean inverted) {
        this.mInverted = inverted;
    }

    protected void addBar(float left, float top, float right, float bottom) {
        buffer[index++] = left;
        buffer[index++] = top;
        buffer[index++] = right;
        buffer[index++] = bottom;
    }

    @Override
    public void feed(IBarDataSet data) {

        float size = data.getEntryCount() * phaseX;
        float barWidthHalf = mBarWidth / 2f;

        for (int i = 0; i < size; i++) {

            BarEntry e = data.getEntryForIndex(i);

            if (e == null)
                continue;

            float x = e.getX();
            float y = e.getY();
            float[] vals = e.getYVals();

            if (!mContainsStacks || vals == null) {
                float left = x - barWidthHalf;
                float right = x + barWidthHalf;
                float bottom, top;
                if (vals != null && vals.length > 1) {
                    bottom = vals[0];
                    top = vals[1];

                    // multiply the height of the rect with the phase
                    if (top > 0)
                        top *= phaseY;
                    else
                        bottom *= phaseY;

                    addBar(left, top, right, bottom);
                }
            } else {
                for (int k = 0; k < vals.length / 2; k++) {

                    float left = x - barWidthHalf;
                    float right = x + barWidthHalf;
                    float bottom, top;
                    if (vals.length > 1) {
                        bottom = vals[k * 2];
                        top = vals[k * 2 + 1];

                        // multiply the height of the rect with the phase
                        top *= phaseY;
                        bottom *= phaseY;

                        addBar(left, top, right, bottom);
                    }
                }
            }
        }

        reset();
    }
}
