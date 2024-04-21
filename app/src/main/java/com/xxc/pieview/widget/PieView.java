package com.xxc.pieview.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.xxc.pieview.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * @ClassName PieView
 * @Description 自定义扇形图
 * @Author xxc
 * @Date 2024/4/20 19:53
 * @Version 1.0
 */
public class PieView extends View {

    private Context mContext;
    // 默认颜色值
    private final int[] colors = {0xFFCCFF00, 0xFF6495ED, 0xFFE32636, 0xFF800000, 0xFF808000, 0xFFFF8C69, 0xFF808080,
            0xFFE6B800, 0xFF7CFC00};
    // View的可用总宽高
    private float mTotalWidth;
    private float mTotalHeight;
    // 绘制扇形图的画笔
    private Paint mPaint;
    // 饼图半径
    private float mRadius;
    // 饼图所在的正方形
    private RectF mRectF;
    // 饼图颜色
    private ArrayList<Integer> mColorLists;
    // 饼图数据
    private ArrayList<PieEntry> mPieLists;
    // 饼图初始绘制角度
    private float startAngle = -90;

    // 是否显示中间的空洞
    private boolean isShowHole = false;
    // 空洞的颜色
    private int holeColor = Color.WHITE;
    // 饼图中间的空洞占据的比例
    private float holeRadiusProportion = 50;
    // 延长点和饼图边缘的间距
    private float distance = 0F;
    // 延长线转折点的横向偏移
    private float xOffset = 40F;
    // 延长线转折点的纵向偏移
    private float yOffset = 24F;
    // 延长线最长段部分的长度
    private float extend = 240F;
    // 延长点上的同心圆环的半径
    private float bigCircleRadius = 0F;
    // 图例小圆点半径
    private float mCircleRadius = 10F;
    // 饼图+延长线+文字 所占用长方形总空间的长宽比
    private float mScale;
    // 小圆点图例画笔
    private Paint mCirclePaint;
    // 小圆点图例旁边上的文本画笔
    private Paint mCircleTextPaint;
    // 延长线和延长线上文本的画笔
    private Paint mLinePaint;
    // 中间百分比文本画笔
    private Paint mTextPaint;
    // 饼图中文本的字号
    private int blockTextSize = 20;
    // 饼图中文本颜色
    public int blockTextColor = Color.WHITE;
    // 是否展示百分比数
    public boolean disPlayPercent = true;
    // 数字格式化
    private final DecimalFormat mFormat = new DecimalFormat("#0.00%");

    public PieView(Context context) {
        this(context, null);
    }

    public PieView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initPaint();
    }

    public PieView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 高度为WrapContent时，设置默认高度
        if (mScale != 0 && MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            int height = (int) (mTotalWidth / mScale);
            setMeasuredDimension(widthMeasureSpec, height);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 获取实际View的宽高
        mTotalWidth = w - getPaddingStart() - getPaddingEnd();
        mTotalHeight = h - getPaddingTop() - getPaddingBottom();
        // 绘制饼图所处的正方形RectF
        initRectF();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        // 将坐标中心设到View的中心
        canvas.translate(mTotalWidth / 2, mTotalHeight / 2);
        if (isPieListsNull()) {
            mPaint.setColor(Color.BLACK);
            canvas.drawText("请通过setData添加数据", -120, 0, mPaint);
        } else {
            // 绘制饼状图
            drawPie(canvas);
            // 绘制中心空洞
            drawHole(canvas);
            // 绘制延长线和文字
            drawLineAndText(canvas);
            // 绘制各板块文字信息
            drawTextInner(canvas);
        }
    }

    /**
     * 绘制饼图
     * @param canvas
     */
    private void drawPie(Canvas canvas) {
        for (PieEntry pie : mPieLists) {
            mPaint.setColor(pie.getColor());
            canvas.drawArc(mRectF, pie.getCurrentStartAngle(), pie.getSweepAngle(), true, mPaint);
        }
    }

    /**
     * 绘制饼图中心空洞
     * @param canvas
     */
    private void drawHole(Canvas canvas) {
        if (isShowHole) {
            mPaint.setColor(holeColor);
            canvas.drawCircle(0, 0, mRadius * holeRadiusProportion / 100, mPaint);
        }
    }

    /**
     * 绘制延长线和文本
     * @param canvas
     */
    private void drawLineAndText(Canvas canvas) {
        // 算出延长线转折点相对起点的正余弦值
        double offsetRadians = Math.atan(yOffset / xOffset);
        float cosOffset = (float) Math.cos(offsetRadians);
        float sinOffset = (float) Math.sin(offsetRadians);

        for (PieEntry pie : mPieLists) {
            // 延长点的位置处于扇形的中间
            float halfAngle = pie.getCurrentStartAngle() + pie.getSweepAngle() / 2;
            float cos = (float) Math.cos(Math.toRadians(halfAngle));
            float sin = (float) Math.sin(Math.toRadians(halfAngle));
            // 通过正余弦算出延长点的位置
            float xCirclePoint = (mRadius + distance) * cos;
            float yCirclePoint = (mRadius + distance) * sin;
            // 将饼图分为4个象限，从右上角开始顺时针，每90度分为一个象限
            // 这里是人为划分的四个象限，非数学定义中的四个象限
            int quadrant = (int) (halfAngle + 90) / 90;
            // 初始化 延长线的起点、转折点、终点
            float xLineStartPoint = 0;
            float yLineStartPoint = 0;
            float xLineTurningPoint = 0;
            float yLineTurningPoint = 0;
            float xLineEndPoint = 0;
            float yLineEndPoint = 0;

            String text = pie.getLabel();
            String aboveLabel = pie.getAboveLabel();
            float textWidth = mCircleTextPaint.measureText(text);
            // 延长点、起点、转折点在同一条线上
            // 不同象限转折的方向不同
            float cosLength = bigCircleRadius * cosOffset;
            float sinLength = bigCircleRadius * sinOffset;
            switch (quadrant) {
                case 0:
                    xLineStartPoint = xCirclePoint + cosLength;
                    yLineStartPoint = yCirclePoint - sinLength;
                    xLineTurningPoint = xLineStartPoint + xOffset;
                    yLineTurningPoint = yLineStartPoint - yOffset;
                    xLineEndPoint = xLineTurningPoint + extend;
                    yLineEndPoint = yLineTurningPoint;
                    // 绘制图例
                    mCirclePaint.setColor(pie.getColor());
                    canvas.drawCircle(xLineEndPoint - textWidth - 20, yLineEndPoint - 50, mCircleRadius, mCirclePaint);
                    // 绘制图例文本
                    mCircleTextPaint.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText(text, xLineEndPoint, yLineEndPoint - 45, mCircleTextPaint);
                    // 绘制延长线上的文本
                    mLinePaint.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText(aboveLabel, xLineEndPoint, yLineEndPoint - 10, mLinePaint);
                    break;
                case 1:
                    xLineStartPoint = xCirclePoint + cosLength;
                    yLineStartPoint = yCirclePoint + sinLength;
                    xLineTurningPoint = xLineStartPoint + xOffset;
                    yLineTurningPoint = yLineStartPoint + yOffset;
                    xLineEndPoint = xLineTurningPoint + extend;
                    yLineEndPoint = yLineTurningPoint;
                    // 绘制图例
                    mCirclePaint.setColor(pie.getColor());
                    canvas.drawCircle(xLineEndPoint - textWidth - 20, yLineEndPoint - 50, mCircleRadius, mCirclePaint);
                    // 绘制图例文本
                    mCircleTextPaint.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText(text, xLineEndPoint, yLineEndPoint - 45, mCircleTextPaint);
                    // 绘制延长线上的文本
                    mLinePaint.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText(aboveLabel, xLineEndPoint, yLineEndPoint - 10, mLinePaint);
                    break;
                case 2:
                    xLineStartPoint = xCirclePoint - cosLength;
                    yLineStartPoint = yCirclePoint + sinLength;
                    xLineTurningPoint = xLineStartPoint - xOffset;
                    yLineTurningPoint = yLineStartPoint + yOffset;
                    xLineEndPoint = xLineTurningPoint - extend;
                    yLineEndPoint = yLineTurningPoint;
                    // 绘制图例
                    mCirclePaint.setColor(pie.getColor());
                    canvas.drawCircle(xLineEndPoint - 20, yLineEndPoint - 50, mCircleRadius, mCirclePaint);
                    // 绘制图例文本
                    mCircleTextPaint.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText(text, xLineEndPoint, yLineEndPoint - 45, mCircleTextPaint);
                    // 绘制延长线上的文本
                    mLinePaint.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText(aboveLabel, xLineEndPoint, yLineEndPoint - 10, mLinePaint);
                    break;
                case 3:
                    xLineStartPoint = xCirclePoint - cosLength;
                    yLineStartPoint = yCirclePoint - sinLength;
                    xLineTurningPoint = xLineStartPoint - xOffset;
                    yLineTurningPoint = yLineStartPoint - yOffset;
                    xLineEndPoint = xLineTurningPoint - extend;
                    yLineEndPoint = yLineTurningPoint;
                    // 绘制图例
                    mCirclePaint.setColor(pie.getColor());
                    canvas.drawCircle(xLineEndPoint - 20, yLineEndPoint - 50, mCircleRadius, mCirclePaint);
                    // 绘制图例文本
                    mCircleTextPaint.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText(text, xLineEndPoint, yLineEndPoint - 45, mCircleTextPaint);
                    // 绘制延长线上的文本
                    mLinePaint.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText(aboveLabel, xLineEndPoint, yLineEndPoint - 10, mLinePaint);
                    break;
                default:
            }
            // 绘制延长线
            canvas.drawLine(xLineStartPoint, yLineStartPoint, xLineTurningPoint, yLineTurningPoint, mLinePaint);
            canvas.drawLine(xLineTurningPoint, yLineTurningPoint, xLineEndPoint, yLineEndPoint, mLinePaint);
        }
    }

    /**
     * 在饼图中绘制文本信息
     * @param canvas
     */
    private void drawTextInner(Canvas canvas) {
        mTextPaint.setTextSize(blockTextSize);
        mTextPaint.setColor(blockTextColor);
        float currentDegree = startAngle;
        for (PieEntry pie : mPieLists) {
            canvas.save();
            // 当前模块角平分线的sin和cos值
            float mathCos = (float) (Math.cos((pie.sweepAngle / 2 + currentDegree) / 180f * Math.PI));
            float mathSin = (float) (Math.sin((pie.sweepAngle / 2 + currentDegree) / 180f * Math.PI));
            Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
            float textRadius = mRadius / 2;

            if (disPlayPercent) {
                //获取文字高度，因水平已居中
                //设置文字格式
                String text = mFormat.format(pie.sweepAngle / 360f);
                //绘制模块百分比
                canvas.drawText(text, textRadius * mathCos,
                        textRadius * mathSin - metrics.ascent, mTextPaint);
            } else {
                String msg = pie.getLabel();
                //获取文字高度，因水平已居中
                float textHeight = metrics.descent - metrics.ascent;
                //绘制模块文字
                if (!TextUtils.isEmpty(msg)) {
                    canvas.drawText(msg, textRadius * mathCos,
                            textRadius * mathSin + textHeight / 2 - metrics.descent, mTextPaint);
                }
            }
            canvas.restore();
            currentDegree += pie.sweepAngle;
        }
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        // 绘制饼图的画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(sp2px(12));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(1f);

        // 小圆点图例画笔
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setStrokeWidth(1f);

        // 绘制小圆点图例旁边的文本的画笔
        mCircleTextPaint = new Paint();
        mCircleTextPaint.setAntiAlias(true);
        mCircleTextPaint.setColor(ContextCompat.getColor(mContext, R.color.color_half_transparent));
        mCircleTextPaint.setTextSize(sp2px(12));
        mCircleTextPaint.setStyle(Paint.Style.FILL);
        mCircleTextPaint.setStrokeWidth(1f);

        // 延长线上文本的画笔
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(ContextCompat.getColor(mContext, R.color.color_9e));
        mLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mLinePaint.setTextSize(sp2px(12));
        mLinePaint.setStrokeWidth(2f);

        // 扇形图上文本的画笔
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setStrokeWidth(1f);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * 初始化饼图所在的正方形
     */
    private void initRectF() {
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        // 文字的高度
        float textHeight = fontMetrics.bottom - fontMetrics.top + fontMetrics.leading;
        // 延长线的纵向长度
        float lineHeight = distance + bigCircleRadius + yOffset;
        // 延长线的横向长度
        float lineWidth = distance + bigCircleRadius + xOffset + extend;
        // 求出饼状图加延长线和文字 所有内容需要的长方形空间的长宽比
        mScale = mTotalWidth / (mTotalWidth + lineHeight * 2 + textHeight * 2 - lineWidth * 2);

        // 长方形空间其短边的长度
        float shortSideLength;
        // 通过宽高比选择短边
        if (mTotalWidth / mTotalHeight >= mScale) {
            shortSideLength = mTotalHeight;
        } else {
            shortSideLength = mTotalWidth / mScale;
        }
        //饼图所在的区域为正方形，处于长方形空间的中心
        //空间的高度减去上下两部分文字显示需要的高度，除以2即为饼图的半径
        mRadius = shortSideLength / 2 - lineHeight - textHeight;
        //设置RectF的坐标
        mRectF = new RectF(-mRadius, -mRadius, mRadius, mRadius);
    }

    /**
     * 初始化饼图颜色
     */
    private void initColors() {
        if (isPieListsNull()) {
            return;
        }
        for (int i = 0; i < mPieLists.size(); i++) {
            mPieLists.get(i).setColor(mColorLists.get(i));
        }
    }

    /**
     * 初始化饼图数据
     */
    private void initData() {
        if (isPieListsNull()) {
            return;
        }
        // 绘制饼图的起始角度
        float currentStartAngle = startAngle;
        for (int i = 0; i < mPieLists.size(); i++) {
            PieEntry pie = mPieLists.get(i);
            pie.setCurrentStartAngle(currentStartAngle);
            // 每个数据百分比对应的角度
            float sweepAngle = pie.getPercentage() / 100 * 360;
            pie.setSweepAngle(sweepAngle);
            // 起始角度不断增加
            currentStartAngle += sweepAngle;

            // 未传入颜色时 以默认的颜色表作为颜色
            if (mColorLists == null || mColorLists.isEmpty()) {
                int j = i % colors.length;
                pie.setColor(colors[j]);
            } else {
                pie.setColor(mColorLists.get(i));
            }
        }
    }

    //判断饼图颜色数据是否为空
    private boolean isPieListsNull() {
        return mPieLists == null || mPieLists.isEmpty();
    }

    /*****************以下是提供外部交互*****************/
    /**
     * 在外部设置饼图颜色
     * @param colorLists
     */
    public void setColors(ArrayList<Integer> colorLists) {
        this.mColorLists = colorLists;
        initColors();
        invalidate();
    }

    /**
     * 在外部设置饼图数据
     * @param pieLists
     */
    public void setData(ArrayList<PieEntry> pieLists) {
        this.mPieLists = pieLists;
        initData();
        invalidate();
    }

    /**
     * 是否显示空洞
     * @param showHole
     */
    public void setShowHole(boolean showHole) {
        isShowHole = showHole;
        invalidate();
    }

    /**
     * Value of sp to value of px.
     *
     * @param spValue The value of sp.
     * @return value of px
     */
    public static int sp2px(final float spValue) {
        final float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static class PieEntry {
        //颜色
        private int color;
        private int circleColor;
        //比分比
        private float percentage;
        //条目名
        private String label;
        private String aboveLabel;
        //扇区起始角度
        private float currentStartAngle;
        //扇区总角度
        private float sweepAngle;

        public PieEntry() {
        }

        public PieEntry(float percentage, String label, String aboveLabel) {
            this.percentage = percentage;
            this.label = label;
            this.aboveLabel = aboveLabel;
        }

        public String getAboveLabel() {
            return aboveLabel;
        }

        public void setAboveLabel(String aboveLabel) {
            this.aboveLabel = aboveLabel;
        }

        public int getCircleColor() {
            return circleColor;
        }

        public void setCircleColor(int circleColor) {
            this.circleColor = circleColor;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public float getPercentage() {
            return percentage;
        }

        public void setPercentage(float percentage) {
            this.percentage = percentage;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public float getSweepAngle() {
            return sweepAngle;
        }

        public void setSweepAngle(float sweepAngle) {
            this.sweepAngle = sweepAngle;
        }

        public float getCurrentStartAngle() {
            return currentStartAngle;
        }

        public void setCurrentStartAngle(float currentStartAngle) {
            this.currentStartAngle = currentStartAngle;
        }
    }
}
