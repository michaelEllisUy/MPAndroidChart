package com.github.mikephil.charting.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

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
import com.github.mikephil.charting.model.GradientColor;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BarChartRenderer extends BarLineScatterCandleBubbleRenderer {
    private static final int DIVIDER_WIDTH = 2;
    private static final int BAR_RADIUS = 35;
    private static final int DESCRIPTION_PADDING = 25;
    protected BarDataProvider mChart;
    private TextPaint descriptionTextPaint;
    private TextPaint descriptionTextPaintBold;
    private int textSize = 35;

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

            if (mChart.areBarsRectangles()) {
                c.drawRect(buffer.buffer[j] + 4, buffer.buffer[j + 1], buffer.buffer[j + 2] - 4,
                        buffer.buffer[j + 3], mRenderPaint);
            } else {
                float top = buffer.buffer[j + 1];
                float bottom = buffer.buffer[j + 3];
                if (top != bottom) {
                    c.drawRoundRect(buffer.buffer[j] + 4, top, buffer.buffer[j + 2] - 4,
                            bottom, BAR_RADIUS, BAR_RADIUS, mRenderPaint);
                } else if (top != 0) {
                    float left = buffer.buffer[j];
                    float right = buffer.buffer[j + 2];
                    c.drawCircle(left + (right - left) / 2, top, (right - left) / 2 - 2, mRenderPaint);
                }
            }

            if (drawBorder) {
                c.drawRoundRect(buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                        buffer.buffer[j + 3], BAR_RADIUS, BAR_RADIUS, mBarBorderPaint);
            }
        }
    }

    protected void prepareBarHighlight(float x, float y1, float y2, float barWidthHalf, Transformer trans) {

        float left = x - barWidthHalf;
        float right = x + barWidthHalf;
        float top = y1;
        float bottom = y2;

        if (top == bottom && top != 0) {
            top -= barWidthHalf * 2;
            bottom += barWidthHalf * 2;
        }

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

            int highlightColor = high.getStackIndex() == -1
                    ? set.getHighLightColor() : barData.getColors()[high.getStackIndex()];

            mHighlightPaint.setColor(highlightColor);

            int stackIndex = high.getStackIndex() == -1 ? 0 : high.getStackIndex();
            Range range = e.getRanges()[stackIndex];

            float y1 = range.from;
            float y2 = range.to;

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
                    currentBounds.right, currentBounds.bottom + Utils.convertDpToPixel(4));

            drawVerticalselectorHighlight(c, e, trans);

            drawTopData(c, e, trans, stackIndex);
            c.restore();

            // Un-comment to draw shadow
            // c.drawRoundRect(mBarRect, BAR_RADIUS, BAR_RADIUS, mHighlightPaint);
        }
    }

    private void drawTopData(Canvas c, BarEntry e, Transformer trans, int stackIndex) {
        MPPointD pix = trans.getPixelForValues(e.getX(), 0);
        if (!(mBarRect.top - mBarRect.bottom == 0)) {

            if (e.getData() instanceof ArrayList) {
                List dataObjects = (List) e.getData();
                Object o = dataObjects.get(stackIndex);
                if (o instanceof Triplet) {
                    drawDescription(c, pix, (Triplet) o);
                }
            } else if (e.getData() instanceof Triplet) {
                drawDescription(c, pix, (Triplet) e.getData());
            }
        }
    }

    private void drawDescription(Canvas c, MPPointD pix, Triplet o) {
        String first = o.getFirst();
        String second = o.getSecond();
        String third = o.getThird();

        float firstTextMeasure = descriptionTextPaint.measureText(first);
        float secondTextMeasure = descriptionTextPaintBold.measureText(second);
        float thirdTextMeasure = third == null ? 0 : descriptionTextPaintBold.measureText(third);

        float textWidth = firstTextMeasure + secondTextMeasure + thirdTextMeasure;

        float totalPadding = third == null ? DESCRIPTION_PADDING * 5 + 4 : DESCRIPTION_PADDING * 7 + 2;

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

        float containerStartY = mViewPortHandler.contentTop() / 4;
        float containerEndY = mViewPortHandler.contentTop() * 3 / 4;

        deleteTitle(c, containerEndY);

        c.drawRoundRect(left,
                containerStartY,
                right,
                containerEndY,
                BAR_RADIUS,
                BAR_RADIUS,
                mRenderPaint);

        drawDescription(c, first, second, third, left, containerStartY, containerEndY);
    }

    private void deleteTitle(Canvas c, float containerEndY) {
        c.drawRect(mViewPortHandler.contentLeft(), 0, mViewPortHandler.contentRight(),
                containerEndY, mBlankPaint);
    }

    private void drawDescription(Canvas c, @NonNull String first, @NonNull String second, @Nullable String third, float left,
                                 float containerStartY, float containerEndY) {
        //Drawing first
        float firstTextLeft = left + DESCRIPTION_PADDING / 2;
        float textY = (containerEndY - containerStartY - textSize) / 2 + containerStartY + textSize;
        drawSingleText(c, first, firstTextLeft, true, false, textY);
        float firstDividerBarLeft = firstTextLeft + DESCRIPTION_PADDING + DIVIDER_WIDTH
                + descriptionTextPaint.measureText(first);
        drawSingleText(c, second, firstDividerBarLeft + DESCRIPTION_PADDING, third != null,
                true, textY);
        if (third != null) {
            float secondDividerBarLeft = firstDividerBarLeft + DESCRIPTION_PADDING * 2 + DIVIDER_WIDTH
                    + descriptionTextPaintBold.measureText(second);
            drawSingleText(c, third, secondDividerBarLeft + DESCRIPTION_PADDING, false,
                    true, textY);
        }
    }

    private void drawSingleText(Canvas c, String text, float left, boolean drawEndDivider,
                                boolean isBoldText, float textY) {
        TextPaint paint = isBoldText ? descriptionTextPaintBold : descriptionTextPaint;
        c.drawText(text,
                left + DESCRIPTION_PADDING,
                textY - paint.descent() / 2,
                paint);

        if (drawEndDivider) {
            float firstDividerBarLeft = left + DESCRIPTION_PADDING * 2 + paint.measureText(text);
            c.drawRect(firstDividerBarLeft,
                    textY - textSize - 5,
                    firstDividerBarLeft + DIVIDER_WIDTH,
                    textY + 5,
                    descriptionTextPaint);
        }
    }

    private void drawVerticalselectorHighlight(Canvas c, BarEntry e, Transformer trans) {
        // create vertical path
        MPPointD pix = trans.getPixelForValues(e.getX(), 0);
        if (!(mBarRect.top - mBarRect.bottom == 0)) {
            c.drawRect((float) pix.x - 2, mViewPortHandler.contentTop() - textSize, (float) pix.x + 2,
                    mBarRect.top - Utils.convertDpToPixel(2), mRenderPaint);

            c.drawRect((float) pix.x - 2, mBarRect.bottom + Utils.convertDpToPixel(2), (float) pix.x + 2,
                    mViewPortHandler.contentBottom() + Utils.convertDpToPixel(4), mRenderPaint);
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
