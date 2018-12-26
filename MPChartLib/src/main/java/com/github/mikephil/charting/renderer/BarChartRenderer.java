package com.github.mikephil.charting.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Triplet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.highlight.Range;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import android.graphics.LinearGradient;
import android.text.TextPaint;

import com.github.mikephil.charting.model.GradientColor;

import java.util.ArrayList;
import java.util.List;

public class BarChartRenderer extends BarLineScatterCandleBubbleRenderer {

    protected BarDataProvider mChart;
    private int barRadius = 25;
    private TextPaint descriptionTextPaint;
    private TextPaint descriptionTextPaintBold;
    private int textSize = 30;
    private int descriptionPadding = 15;

    /**
     * the rect object that is used for drawing the bars
     */
    protected RectF mBarRect = new RectF();

    protected BarBuffer[] mBarBuffers;

    protected Paint mShadowPaint;
    protected Paint mBarBorderPaint;

    public BarChartRenderer(BarDataProvider chart, ChartAnimator animator,
                            ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
        this.mChart = chart;

        mHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHighlightPaint.setStyle(Paint.Style.FILL);
        mHighlightPaint.setColor(Color.rgb(0, 0, 0));
        // set alpha after color
        mHighlightPaint.setAlpha(120);

        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setStyle(Paint.Style.FILL);

        mBarBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarBorderPaint.setStyle(Paint.Style.STROKE);

        descriptionTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        descriptionTextPaint.setTextAlign(Paint.Align.LEFT);
        descriptionTextPaint.setTextSize(textSize);
        descriptionTextPaint.setColor(Color.WHITE);

        descriptionTextPaintBold = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        descriptionTextPaintBold.setTextAlign(Paint.Align.LEFT);
        descriptionTextPaintBold.setTextSize(textSize);
        descriptionTextPaintBold.setColor(Color.WHITE);
        descriptionTextPaintBold.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    }

    @Override
    public void initBuffers() {

        BarData barData = mChart.getBarData();
        mBarBuffers = new BarBuffer[barData.getDataSetCount()];

        for (int i = 0; i < mBarBuffers.length; i++) {
            IBarDataSet set = barData.getDataSetByIndex(i);
            mBarBuffers[i] = new BarBuffer(set.getEntryCount() * 4 * (set.isStacked() ? set.getStackSize() : 1),
                    barData.getDataSetCount(), set.isStacked());
        }
    }

    @Override
    public void drawData(Canvas c) {

        BarData barData = mChart.getBarData();

        for (int i = 0; i < barData.getDataSetCount(); i++) {

            IBarDataSet set = barData.getDataSetByIndex(i);

            if (set.isVisible()) {
                drawDataSet(c, set, i);
            }
        }
    }

    private RectF mBarShadowRectBuffer = new RectF();

    protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {

        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        mBarBorderPaint.setColor(dataSet.getBarBorderColor());
        mBarBorderPaint.setStrokeWidth(Utils.convertDpToPixel(dataSet.getBarBorderWidth()));

        final boolean drawBorder = dataSet.getBarBorderWidth() > 0.f;

        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();

        // draw the bar shadow before the values
        if (mChart.isDrawBarShadowEnabled()) {
            mShadowPaint.setColor(dataSet.getBarShadowColor());

            BarData barData = mChart.getBarData();

            final float barWidth = barData.getBarWidth();
            final float barWidthHalf = barWidth / 2.0f;
            float x;

            for (int i = 0, count = Math.min((int) (Math.ceil((float) (dataSet.getEntryCount()) * phaseX)), dataSet.getEntryCount());
                 i < count;
                 i++) {

                BarEntry e = dataSet.getEntryForIndex(i);

                x = e.getX();

                mBarShadowRectBuffer.left = x - barWidthHalf;
                mBarShadowRectBuffer.right = x + barWidthHalf;

                trans.rectValueToPixel(mBarShadowRectBuffer);

                if (!mViewPortHandler.isInBoundsLeft(mBarShadowRectBuffer.right))
                    continue;

                if (!mViewPortHandler.isInBoundsRight(mBarShadowRectBuffer.left))
                    break;

                mBarShadowRectBuffer.top = mViewPortHandler.contentTop();
                mBarShadowRectBuffer.bottom = mViewPortHandler.contentBottom();

                c.drawRect(mBarShadowRectBuffer, mShadowPaint);
            }
        }

        // initialize the buffer
        BarBuffer buffer = mBarBuffers[index];
        buffer.setPhases(phaseX, phaseY);
        buffer.setDataSet(index);
        buffer.setInverted(mChart.isInverted(dataSet.getAxisDependency()));
        buffer.setBarWidth(mChart.getBarData().getBarWidth());

        buffer.feed(dataSet);

        trans.pointValuesToPixel(buffer.buffer);

        final boolean isSingleColor = dataSet.getColors().size() == 1;

        if (isSingleColor) {
            mRenderPaint.setColor(dataSet.getColor());
        }

        for (int j = 0; j < buffer.size(); j += 4) {

            if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2]))
                continue;

            if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
                break;

            if (!isSingleColor) {
                // Set the color for the currently drawn value. If the index
                // is out of bounds, reuse colors.
                mRenderPaint.setColor(dataSet.getColor(j / 4));
            }

            if (dataSet.getGradientColor() != null) {
                GradientColor gradientColor = dataSet.getGradientColor();
                mRenderPaint.setShader(
                        new LinearGradient(
                                buffer.buffer[j],
                                buffer.buffer[j + 3],
                                buffer.buffer[j],
                                buffer.buffer[j + 1],
                                gradientColor.getStartColor(),
                                gradientColor.getEndColor(),
                                android.graphics.Shader.TileMode.MIRROR));
            }

            if (dataSet.getGradientColors() != null) {
                mRenderPaint.setShader(
                        new LinearGradient(
                                buffer.buffer[j],
                                buffer.buffer[j + 3],
                                buffer.buffer[j],
                                buffer.buffer[j + 1],
                                dataSet.getGradientColor(j / 4).getStartColor(),
                                dataSet.getGradientColor(j / 4).getEndColor(),
                                android.graphics.Shader.TileMode.MIRROR));
            }

            c.drawRoundRect(buffer.buffer[j] + 4, buffer.buffer[j + 1], buffer.buffer[j + 2] - 4,
                    buffer.buffer[j + 3], barRadius, barRadius, mRenderPaint);

            if (drawBorder) {
                c.drawRoundRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                        buffer.buffer[j + 3], barRadius, barRadius, mBarBorderPaint);
            }
        }
    }

    protected void prepareBarHighlight(float x, float y1, float y2, float barWidthHalf, Transformer trans) {

        float left = x - barWidthHalf;
        float right = x + barWidthHalf;
        float top = y1;
        float bottom = y2;

        mBarRect.set(left, top, right, bottom);

        trans.rectToPixelPhase(mBarRect, mAnimator.getPhaseY());
    }

    @Override
    public void drawValues(Canvas c) {

        // if values are drawn
        if (isDrawingValuesAllowed(mChart)) {

            List<IBarDataSet> dataSets = mChart.getBarData().getDataSets();

            final float valueOffsetPlus = Utils.convertDpToPixel(4.5f);
            float posOffset;
            float negOffset;
            boolean drawValueAboveBar = mChart.isDrawValueAboveBarEnabled();

            for (int i = 0; i < mChart.getBarData().getDataSetCount(); i++) {

                IBarDataSet dataSet = dataSets.get(i);

                if (!shouldDrawValues(dataSet))
                    continue;

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet);

                boolean isInverted = mChart.isInverted(dataSet.getAxisDependency());

                // calculate the correct offset depending on the draw position of
                // the value
                float valueTextHeight = Utils.calcTextHeight(mValuePaint, "8");
                posOffset = (drawValueAboveBar ? -valueOffsetPlus : valueTextHeight + valueOffsetPlus);
                negOffset = (drawValueAboveBar ? valueTextHeight + valueOffsetPlus : -valueOffsetPlus);

                if (isInverted) {
                    posOffset = -posOffset - valueTextHeight;
                    negOffset = -negOffset - valueTextHeight;
                }

                // get the buffer
                BarBuffer buffer = mBarBuffers[i];

                final float phaseY = mAnimator.getPhaseY();

                ValueFormatter formatter = dataSet.getValueFormatter();

                MPPointF iconsOffset = MPPointF.getInstance(dataSet.getIconsOffset());
                iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x);
                iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y);

                Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

                int bufferIndex = 0;
                int index = 0;

                while (index < dataSet.getEntryCount() * mAnimator.getPhaseX()) {

                    BarEntry entry = dataSet.getEntryForIndex(index);

                    float[] vals = entry.getYVals();
                    float x = (buffer.buffer[bufferIndex] + buffer.buffer[bufferIndex + 2]) / 2f;

                    int color = dataSet.getValueTextColor(index);

                    // we still draw stacked bars, but there is one
                    // non-stacked
                    // in between
                    if (vals == null) {

                        if (!mViewPortHandler.isInBoundsRight(x))
                            break;

                        if (!mViewPortHandler.isInBoundsY(buffer.buffer[bufferIndex + 1])
                                || !mViewPortHandler.isInBoundsLeft(x))
                            continue;

                        if (dataSet.isDrawValuesEnabled()) {
                            drawValue(c, formatter.getBarLabel(entry), x, buffer.buffer[bufferIndex + 1] +
                                            (entry.getY() >= 0 ? posOffset : negOffset),
                                    color);
                        }

                        if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {

                            Drawable icon = entry.getIcon();

                            float px = x;
                            float py = buffer.buffer[bufferIndex + 1] +
                                    (entry.getY() >= 0 ? posOffset : negOffset);

                            px += iconsOffset.x;
                            py += iconsOffset.y;

                            Utils.drawImage(
                                    c,
                                    icon,
                                    (int) px,
                                    (int) py,
                                    icon.getIntrinsicWidth(),
                                    icon.getIntrinsicHeight());
                        }

                        // draw stack values
                    } else {

                        float[] transformed = new float[vals.length * 2];

                        for (int k = 0, idx = 0; k < transformed.length; k += 2, idx++) {
                            float y = vals[idx];

                            transformed[k] = y;
                            transformed[k + 1] = y * phaseY;
                        }

                        trans.pointValuesToPixel(transformed);
                    }

                    bufferIndex = vals == null ? bufferIndex + 4 : bufferIndex + 4 * vals.length / 2;
                    index++;
                }

                MPPointF.recycleInstance(iconsOffset);
            }
        }
    }

    @Override
    public void drawValue(Canvas c, String valueText, float x, float y, int color) {
        mValuePaint.setColor(color);
        c.drawText(valueText, x, y, mValuePaint);
    }

    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {

        BarData barData = mChart.getBarData();

        for (Highlight high : indices) {

            IBarDataSet set = barData.getDataSetByIndex(high.getDataSetIndex());

            if (set == null || !set.isHighlightEnabled())
                continue;

            BarEntry e = set.getEntryForXValue(high.getX(), high.getY());

            if (!isInBoundsX(e, set))
                continue;

            Transformer trans = mChart.getTransformer(set.getAxisDependency());

            mHighlightPaint.setColor(set.getHighLightColor());
            mHighlightPaint.setAlpha(set.getHighLightAlpha());

            int stackIndex = high.getStackIndex() == -1 ? 0 : high.getStackIndex();
            Range range = e.getRanges()[stackIndex];


            final float y1 = range.from;
            final float y2 = range.to;

            prepareBarHighlight(e.getX(), y1, y2, barData.getBarWidth() / 2f, trans);

            setHighlightDrawPos(high, mBarRect);

            mBarRect.left += 4;
            mBarRect.right -= 4;

            mRenderPaint.setColor(set.getColor(stackIndex));
            descriptionTextPaint.setColor(set.getContrastColor(stackIndex));
            descriptionTextPaintBold.setColor(set.getContrastColor(stackIndex));

            Rect currentBounds = c.getClipBounds();

            c.restore();
            c.save();
            c.clipRect(currentBounds.left, currentBounds.top - textSize * 3,
                    currentBounds.right, currentBounds.bottom + 10);

            drawVerticalselectorHighlight(c, e, trans);

            drawTopData(c, e, trans, stackIndex);
            c.restore();

            // Un-comment to draw shadow
            // c.drawRoundRect(mBarRect, barRadius, barRadius, mHighlightPaint);
        }
    }

    private void drawTopData(Canvas c, BarEntry e, Transformer trans, int stackIndex) {
        MPPointD pix = trans.getPixelForValues(e.getX(), 0);
        if (!(mBarRect.top - mBarRect.bottom == 0)) {

            if (e.getData() instanceof ArrayList) {
                List dataObjects = (List) e.getData();
                Object o = dataObjects.get(stackIndex);
                if (o instanceof Triplet) {
                    Triplet triplet = (Triplet) o;
                    String first = triplet.getFirst();
                    String second = triplet.getSecond();
                    String third = triplet.getThird();

                    float firstTextMeasure = descriptionTextPaint.measureText(first);
                    float secondTextMeasure = descriptionTextPaintBold.measureText(second);
                    float thirdTextMeasure = third == null ? 0 : descriptionTextPaintBold.measureText(third);

                    float textWidth = firstTextMeasure + secondTextMeasure + thirdTextMeasure;

                    float totalPadding = third == null ? descriptionPadding * 6 + 4 : descriptionPadding * 8 + 2;

                    textWidth += totalPadding;

                    //Drawing text container
                    float left = (float) pix.x - textWidth / 2;
                    float right = (float) pix.x + textWidth / 2;
                    if (left < c.getClipBounds().left) {
                        right = right - left + c.getClipBounds().left;
                        left = c.getClipBounds().left;
                    } else if (right > c.getClipBounds().right) {
                        left = c.getClipBounds().right - (right - left);
                        right = c.getClipBounds().right;
                    }

                    c.drawRoundRect(left,
                            mViewPortHandler.contentTop() - textSize * 3,
                            right,
                            mViewPortHandler.contentTop() - textSize,
                            barRadius,
                            barRadius,
                            mRenderPaint);

                    drawDescription(c, first, second, third, left);
                }
            }
        }
    }

    private void drawDescription(Canvas c, String first, String second, String third, float left) {
        //Drawing first
        drawSingleText(c, first, left + descriptionPadding, true, false);
        float firstDividerBarLeft = left + descriptionPadding * 2 + descriptionTextPaint.measureText(first);
        drawSingleText(c, second, firstDividerBarLeft + descriptionPadding, third != null, true);
        float secondDividerBarLeft = firstDividerBarLeft + descriptionPadding * 2
                + descriptionTextPaintBold.measureText(second);
        drawSingleText(c, third, secondDividerBarLeft + descriptionPadding, false, true);
    }

    private void drawSingleText(Canvas c, String text, float left, boolean drawEndDivider,
                                boolean isBoldText) {
        c.drawText(text,
                left + descriptionPadding,
                mViewPortHandler.contentTop() - textSize * 5 / 3,
                isBoldText ? descriptionTextPaintBold : descriptionTextPaint);

        if (drawEndDivider) {
            float firstDividerBarLeft = left + descriptionPadding * 2 + descriptionTextPaint.measureText(text);
            c.drawRect(firstDividerBarLeft,
                    mViewPortHandler.contentTop() - textSize * 3 + 10,
                    firstDividerBarLeft + 2,
                    mViewPortHandler.contentTop() - textSize  - 10,
                    descriptionTextPaint);
        }
    }

    private void drawVerticalselectorHighlight(Canvas c, BarEntry e, Transformer trans) {
        // create vertical path
        MPPointD pix = trans.getPixelForValues(e.getX(), 0);
        if (!(mBarRect.top - mBarRect.bottom == 0)) {
            c.drawRect((float) pix.x - 2, mViewPortHandler.contentTop() - textSize, (float) pix.x + 2,
                    mBarRect.top - 4, mRenderPaint);

            c.drawRect((float) pix.x - 2, mBarRect.bottom + 4, (float) pix.x + 2,
                    mViewPortHandler.contentBottom() + 10, mRenderPaint);
        }
    }

    /**
     * Sets the drawing position of the highlight object based on the riven bar-rect.
     *
     * @param high
     */
    protected void setHighlightDrawPos(Highlight high, RectF bar) {
        high.setDraw(bar.centerX(), bar.top);
    }

    @Override
    public void drawExtras(Canvas c) {
    }
}
