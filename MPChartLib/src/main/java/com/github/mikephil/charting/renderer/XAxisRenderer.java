package com.github.mikephil.charting.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.TextPaint;

import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.utils.FSize;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.List;

import androidx.annotation.NonNull;

public class XAxisRenderer extends AxisRenderer {

    protected XAxis mXAxis;
    protected Paint mAxisDistanceLabelPaint;

    public XAxisRenderer(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans) {
        super(viewPortHandler, trans, xAxis);

        this.mXAxis = xAxis;

        mAxisLabelPaint.setColor(Color.BLACK);
        mAxisLabelPaint.setTextAlign(Align.CENTER);
        mAxisLabelPaint.setTextSize(Utils.convertDpToPixel(10f));
        mAxisDistanceLabelPaint = new TextPaint();
        mAxisDistanceLabelPaint.setColor(mAxisLabelPaint.getColor());
        mAxisDistanceLabelPaint.setStyle(mAxisLabelPaint.getStyle());
        mAxisDistanceLabelPaint.setTextSize(mAxisLabelPaint.getTextSize());
        mAxisDistanceLabelPaint.setTextAlign(Align.LEFT);
    }

    protected void setupGridPaint() {
        mGridPaint.setColor(mXAxis.getGridColor());
        mGridPaint.setStrokeWidth(mXAxis.getGridLineWidth());
        mGridPaint.setPathEffect(mXAxis.getGridDashPathEffect());
    }

    protected void setupGridDelimeterPaint() {
        mGridDelimiterPaint.setColor(mXAxis.getGridColor());
        mGridDelimiterPaint.setStrokeWidth(mXAxis.getGridLineWidth());
    }

    @Override
    public void computeAxis(float min, float max, boolean inverted) {

        // calculate the starting and entry point of the y-labels (depending on
        // zoom / contentrect bounds)
        if (mViewPortHandler.contentWidth() > 10 && !mViewPortHandler.isFullyZoomedOutX()) {

            MPPointD p1 = mTrans.getValuesByTouchPoint(mViewPortHandler.contentLeft(), mViewPortHandler.contentTop());
            MPPointD p2 = mTrans.getValuesByTouchPoint(mViewPortHandler.contentRight(), mViewPortHandler.contentTop());

            if (inverted) {

                min = (float) p2.x;
                max = (float) p1.x;
            } else {

                min = (float) p1.x;
                max = (float) p2.x;
            }

            MPPointD.recycleInstance(p1);
            MPPointD.recycleInstance(p2);
        }

        computeAxisValues(min, max);
    }

    @Override
    protected void computeAxisValues(float min, float max) {
        super.computeAxisValues(min, max);

        computeSize();
    }

    protected void computeSize() {

        String longest = mXAxis.getLongestLabel();

        mAxisLabelPaint.setTypeface(mXAxis.getTypeface());
        mAxisLabelPaint.setTextSize(mXAxis.getTextSize());

        final FSize labelSize = Utils.calcTextSize(mAxisLabelPaint, longest);

        final float labelWidth = labelSize.width;
        final float labelHeight = Utils.calcTextHeight(mAxisLabelPaint, "Q");

        final FSize labelRotatedSize = Utils.getSizeOfRotatedRectangleByDegrees(
                labelWidth,
                labelHeight,
                mXAxis.getLabelRotationAngle());


        mXAxis.mLabelWidth = Math.round(labelWidth);
        mXAxis.mLabelHeight = Math.round(labelHeight);
        mXAxis.mLabelRotatedWidth = Math.round(labelRotatedSize.width);
        mXAxis.mLabelRotatedHeight = Math.round(labelRotatedSize.height);

        FSize.recycleInstance(labelRotatedSize);
        FSize.recycleInstance(labelSize);
    }

    @Override
    public void renderAxisLabels(Canvas c) {

        if (!mXAxis.isEnabled() || !mXAxis.isDrawLabelsEnabled())
            return;

        float yoffset = mXAxis.getYOffset();

        mAxisLabelPaint.setTypeface(mXAxis.getTypeface());
        mAxisLabelPaint.setTextSize(mXAxis.getTextSize());
        mAxisLabelPaint.setColor(mXAxis.getTextColor());

        MPPointF pointF = MPPointF.getInstance(0, 0);
        if (mXAxis.getPosition() == XAxisPosition.TOP) {
            pointF.x = 0.5f;
            pointF.y = 1.0f;
            drawLabels(c, mViewPortHandler.contentTop() - yoffset, pointF);

        } else if (mXAxis.getPosition() == XAxisPosition.TOP_INSIDE) {
            pointF.x = 0.5f;
            pointF.y = 1.0f;
            drawLabels(c, mViewPortHandler.contentTop() + yoffset + mXAxis.mLabelRotatedHeight, pointF);

        } else if (mXAxis.getPosition() == XAxisPosition.BOTTOM) {
            pointF.x = 0.5f;
            pointF.y = 0.0f;
            drawLabels(c, mViewPortHandler.contentBottom() + yoffset, pointF);

        } else if (mXAxis.getPosition() == XAxisPosition.BOTTOM_INSIDE) {
            pointF.x = 0.5f;
            pointF.y = 0.0f;
            drawLabels(c, mViewPortHandler.contentBottom() - yoffset - mXAxis.mLabelRotatedHeight, pointF);

        } else { // BOTH SIDED
            pointF.x = 0.5f;
            pointF.y = 1.0f;
            drawLabels(c, mViewPortHandler.contentTop() - yoffset, pointF);
            pointF.x = 0.5f;
            pointF.y = 0.0f;
            drawLabels(c, mViewPortHandler.contentBottom() + yoffset, pointF);
        }
        MPPointF.recycleInstance(pointF);
    }

    @Override
    public void renderAxisLine(Canvas c) {

        if (!mXAxis.isDrawAxisLineEnabled() || !mXAxis.isEnabled())
            return;

        mAxisLinePaint.setColor(mXAxis.getAxisLineColor());
        mAxisLinePaint.setStrokeWidth(mXAxis.getAxisLineWidth());
        mAxisLinePaint.setPathEffect(mXAxis.getAxisLineDashPathEffect());

        if (mXAxis.getPosition() == XAxisPosition.TOP
                || mXAxis.getPosition() == XAxisPosition.TOP_INSIDE
                || mXAxis.getPosition() == XAxisPosition.BOTH_SIDED) {
            c.drawLine(mViewPortHandler.contentLeft(),
                    mViewPortHandler.contentTop(), mViewPortHandler.contentRight(),
                    mViewPortHandler.contentTop(), mAxisLinePaint);
        }

        if (mXAxis.getPosition() == XAxisPosition.BOTTOM
                || mXAxis.getPosition() == XAxisPosition.BOTTOM_INSIDE
                || mXAxis.getPosition() == XAxisPosition.BOTH_SIDED) {
            c.drawLine(mViewPortHandler.contentLeft(),
                    mViewPortHandler.contentBottom(), mViewPortHandler.contentRight(),
                    mViewPortHandler.contentBottom(), mAxisLinePaint);
        }
    }

    /**
     * draws the x-labels on the specified y-position
     *
     * @param pos
     */
    protected void drawLabels(Canvas c, float pos, MPPointF anchor) {
        if (mAxis.getLabelDistance() == -1) {
            final float labelRotationAngleDegrees = mXAxis.getLabelRotationAngle();
            boolean centeringEnabled = mXAxis.isCenterAxisLabelsEnabled();

            float[] positions = new float[mXAxis.mEntryCount * 2];

            for (int i = 0; i < positions.length; i += 2) {

                // only fill x values
                if (centeringEnabled) {
                    positions[i] = mXAxis.mCenteredEntries[i / 2];
                } else {
                    positions[i] = mXAxis.mEntries[i / 2];
                }
            }

            mTrans.pointValuesToPixel(positions);

            for (int i = 0; i < positions.length; i += 2) {

                float x = positions[i];

                if (mViewPortHandler.isInBoundsX(x)) {

                    String label = mXAxis.getValueFormatter().getAxisLabel(mXAxis.mEntries[i / 2], mXAxis);

                    if (mXAxis.isAvoidFirstLastClippingEnabled()) {

                        // avoid clipping of the last
                        if (i / 2 == mXAxis.mEntryCount - 1 && mXAxis.mEntryCount > 1) {
                            float width = Utils.calcTextWidth(mAxisLabelPaint, label);

                            if (width > mViewPortHandler.offsetRight() * 2
                                    && x + width > mViewPortHandler.getChartWidth())
                                x -= width / 2;

                            // avoid clipping of the first
                        } else if (i == 0) {

                            float width = Utils.calcTextWidth(mAxisLabelPaint, label);
                            x += width / 2;
                        }
                    }

                    drawLabel(c, label, x, pos, anchor, labelRotationAngleDegrees);
                }
            }
        }
    }

    protected void drawLabel(Canvas c, String formattedLabel, float x, float y, MPPointF anchor, float angleDegrees) {
        Utils.drawXAxisValue(c, formattedLabel, x, y, mAxisLabelPaint, anchor, angleDegrees);
    }

    protected Path mRenderGridLinesPath = new Path();
    protected float[] mRenderGridLinesBuffer = new float[2];

    @Override
    public void renderGridLines(Canvas c) {

        if (!mXAxis.isDrawGridLinesEnabled() || !mXAxis.isEnabled())
            return;

        int clipRestoreCount = c.save();
        c.clipRect(getGridClippingRect());

        if (mRenderGridLinesBuffer.length != mAxis.mEntryCount * 2) {
            mRenderGridLinesBuffer = new float[mXAxis.mEntryCount * 2];
        }
        float[] positions = mRenderGridLinesBuffer;

        if (mXAxis.shouldDrawOneGridLinePerUnit()) {
            positions = new float[((int) mXAxis.mAxisMaximum - (int) mXAxis.mAxisMinimum + 2) * 2];
            int min = (int) mXAxis.mAxisMinimum;
            for (int i = min, index = 0; index < positions.length; i++, index += 2) {
                float linePosition = (float) i - 0.5f;
                positions[index] = linePosition;
                positions[index + 1] = linePosition;
            }
        } else {
            for (int i = 0; i < positions.length; i += 2) {
                positions[i] = mXAxis.mEntries[i / 2];
                positions[i + 1] = mXAxis.mEntries[i / 2];
            }
        }

        mTrans.pointValuesToPixel(positions);

        setupGridPaint();
        setupGridDelimeterPaint();

        Path gridLinePath = mRenderGridLinesPath;
        gridLinePath.reset();
        int lineNumber = 0;
        int labelNumber = 0;
        int labelDistance = mAxis.getLabelDistance();
        boolean drawTillEnd = false;
        for (int i = 0; i < positions.length; i += 2) {
            if (lineNumber == mAxis.getLabelStart() || lineNumber == positions.length / 2 - 1) {
                drawContainerLine(c, gridLinePath, positions[i]);
                if (labelNumber < mXAxis.mEntries.length) {
                    String label = getLabelForEntry(mXAxis.mEntries[labelNumber]);
                    float startingX = positions[i] + Utils.convertDpToPixel(2);
                    drawLabel(c, startingX, label);
                    labelNumber++;
                }
            } else {
                if (labelDistance != -1 && (lineNumber % labelDistance) == 0) {
                    drawTillEnd = true;
                    if (labelNumber < mXAxis.mEntries.length && lineNumber > mAxis.getLabelStart()) {
                        String label = getLabelForEntry(mXAxis.mEntries[labelNumber]);
                        float startingX = positions[i] + Utils.convertDpToPixel(2);
                        if (labelNumber != mXAxis.mEntries.length - 1
                                || i + 4 != positions.length
                                || startingX + mAxisDistanceLabelPaint.measureText(label) < positions[i + 2]
                                ) {
                            drawLabel(c, startingX, label);
                        }
                        labelNumber++;
                    }
                }
                drawGridLine(c, positions[i], gridLinePath, drawTillEnd);
                drawTillEnd = false;
            }
            lineNumber++;
        }

        c.restoreToCount(clipRestoreCount);
    }

    private String getLabelForEntry(float mEntry) {
        return mXAxis.getValueFormatter().getAxisLabel(mEntry, mXAxis);
    }

    private void drawLabel(Canvas c, float startingX, @NonNull String label) {
        if (mAxisDistanceLabelPaint.measureText(label) + startingX < mViewPortHandler.contentRight()) {
            c.drawText(label, startingX,
                    mViewPortHandler.contentBottom() + mViewPortHandler.offsetBottom() - Utils.convertDpToPixel(2),
                    mAxisDistanceLabelPaint);
        }
    }

    protected RectF mGridClippingRect = new RectF();

    public RectF getGridClippingRect() {
        mGridClippingRect.set(mViewPortHandler.getContentRect());
        mGridClippingRect.inset(-mAxis.getGridLineWidth(), 0.f);
        mGridClippingRect.bottom += mViewPortHandler.offsetBottom();
        return mGridClippingRect;
    }

    /**
     * Draws the grid line at the specified position using the provided path.
     *
     * @param c
     * @param x
     * @param gridLinePath
     */
    protected void drawGridLine(Canvas c, float x, Path gridLinePath, boolean drawTillEnd) {
        float lineEnd;
        if (drawTillEnd) {
            lineEnd = mViewPortHandler.contentBottom() + mViewPortHandler.offsetBottom();
        } else {
            lineEnd = mViewPortHandler.contentBottom() + Utils.convertDpToPixel(4);
        }

        gridLinePath.moveTo(x, lineEnd);
        gridLinePath.lineTo(x, mViewPortHandler.contentTop());

        // draw a path because lines don't support dashing on lower android versions
        c.drawPath(gridLinePath, mGridPaint);

        gridLinePath.reset();
    }

    private void drawContainerLine(Canvas c, Path gridLinePath, float lineX) {
        float lineEnd = mViewPortHandler.contentBottom() + mViewPortHandler.offsetBottom();
        gridLinePath.moveTo(lineX, lineEnd);
        gridLinePath.lineTo(lineX, mViewPortHandler.contentTop());

        // draw a path because lines don't support dashing on lower android versions
        c.drawPath(gridLinePath, mGridDelimiterPaint);

        gridLinePath.reset();
    }

    protected float[] mRenderLimitLinesBuffer = new float[2];
    protected RectF mLimitLineClippingRect = new RectF();

    /**
     * Draws the LimitLines associated with this axis to the screen.
     *
     * @param c
     */
    @Override
    public void renderLimitLines(Canvas c) {

        List<LimitLine> limitLines = mXAxis.getLimitLines();

        if (limitLines == null || limitLines.size() <= 0)
            return;

        float[] position = mRenderLimitLinesBuffer;
        position[0] = 0;
        position[1] = 0;

        for (int i = 0; i < limitLines.size(); i++) {

            LimitLine l = limitLines.get(i);

            if (!l.isEnabled())
                continue;

            int clipRestoreCount = c.save();
            mLimitLineClippingRect.set(mViewPortHandler.getContentRect());
            mLimitLineClippingRect.inset(-l.getLineWidth(), 0.f);
            c.clipRect(mLimitLineClippingRect);

            position[0] = l.getLimit();
            position[1] = 0.f;

            mTrans.pointValuesToPixel(position);

            renderLimitLineLine(c, l, position);
            renderLimitLineLabel(c, l, position, 2.f + l.getYOffset());

            c.restoreToCount(clipRestoreCount);
        }
    }

    float[] mLimitLineSegmentsBuffer = new float[4];
    private Path mLimitLinePath = new Path();

    public void renderLimitLineLine(Canvas c, LimitLine limitLine, float[] position) {
        mLimitLineSegmentsBuffer[0] = position[0];
        mLimitLineSegmentsBuffer[1] = mViewPortHandler.contentTop();
        mLimitLineSegmentsBuffer[2] = position[0];
        mLimitLineSegmentsBuffer[3] = mViewPortHandler.contentBottom();

        mLimitLinePath.reset();
        mLimitLinePath.moveTo(mLimitLineSegmentsBuffer[0], mLimitLineSegmentsBuffer[1]);
        mLimitLinePath.lineTo(mLimitLineSegmentsBuffer[2], mLimitLineSegmentsBuffer[3]);

        mLimitLinePaint.setStyle(Paint.Style.STROKE);
        mLimitLinePaint.setColor(limitLine.getLineColor());
        mLimitLinePaint.setStrokeWidth(limitLine.getLineWidth());
        mLimitLinePaint.setPathEffect(limitLine.getDashPathEffect());

        c.drawPath(mLimitLinePath, mLimitLinePaint);
    }

    public void renderLimitLineLabel(Canvas c, LimitLine limitLine, float[] position, float yOffset) {
        String label = limitLine.getLabel();

        // if drawing the limit-value label is enabled
        if (label != null && !label.equals("")) {

            mLimitLinePaint.setStyle(limitLine.getTextStyle());
            mLimitLinePaint.setPathEffect(null);
            mLimitLinePaint.setColor(limitLine.getTextColor());
            mLimitLinePaint.setStrokeWidth(0.5f);
            mLimitLinePaint.setTextSize(limitLine.getTextSize());


            float xOffset = limitLine.getLineWidth() + limitLine.getXOffset();

            final LimitLine.LimitLabelPosition labelPosition = limitLine.getLabelPosition();

            if (labelPosition == LimitLine.LimitLabelPosition.RIGHT_TOP) {

                final float labelLineHeight = Utils.calcTextHeight(mLimitLinePaint, label);
                mLimitLinePaint.setTextAlign(Align.LEFT);
                c.drawText(label, position[0] + xOffset, mViewPortHandler.contentTop() + yOffset + labelLineHeight,
                        mLimitLinePaint);
            } else if (labelPosition == LimitLine.LimitLabelPosition.RIGHT_BOTTOM) {

                mLimitLinePaint.setTextAlign(Align.LEFT);
                c.drawText(label, position[0] + xOffset, mViewPortHandler.contentBottom() - yOffset, mLimitLinePaint);
            } else if (labelPosition == LimitLine.LimitLabelPosition.LEFT_TOP) {

                mLimitLinePaint.setTextAlign(Align.RIGHT);
                final float labelLineHeight = Utils.calcTextHeight(mLimitLinePaint, label);
                c.drawText(label, position[0] - xOffset, mViewPortHandler.contentTop() + yOffset + labelLineHeight,
                        mLimitLinePaint);
            } else {

                mLimitLinePaint.setTextAlign(Align.RIGHT);
                c.drawText(label, position[0] - xOffset, mViewPortHandler.contentBottom() - yOffset, mLimitLinePaint);
            }
        }
    }
}
