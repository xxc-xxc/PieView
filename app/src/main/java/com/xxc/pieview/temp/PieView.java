package com.xxc.pieview.temp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.xxc.pieview.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PieView extends View {
    private final int[] colors = {0xFFCCFF00, 0xFF6495ED, 0xFFE32636, 0xFF800000, 0xFF808000, 0xFFFF8C69, 0xFF808080,
            0xFFE6B800, 0xFF7CFC00};
    //饼图初始绘制角度
    private float startAngle = -90;
    //延长点和饼图边缘的间距
    private float distance = 0F;
    //延长线转折点的横向偏移
    private float xOffset = 40F;
    //延长线转折点的纵向偏移
    private float yOffset = 24F;
    //延长线最长段部分的长度
    private float extend = 240F;
    private float circleRadius = 10F;
    // 延长点上的同心圆环的大小
    private float bigCircleRadius = 0F;
    // 是否显示中间的空洞
    private boolean mShowHole = false;
    // 空洞的颜色
    private int holeColor = Color.WHITE;
    // 饼图中间的空洞占据的比例
    private float holeRadiusProportion = 59;
    private Paint mPaint;
    private Paint mlinetextPaint;
    private Paint mLinePaint;
    private Paint mcirclePaint;
    private RectF mRectF;
    /**
     * 文字画笔
     */
    private Paint mTextPaint;
    /**
     * 版块上的字号
     */
    public int blockTextSize = 20;
    /**
     * 版块上文字的颜色
     */
    public int blockTextColor = Color.WHITE;
    /**
     * decimal format
     */
    private final DecimalFormat mFormat = new DecimalFormat("#0.00%");
    /**
     * 是否展示百分比数
     */
    public boolean disPlayPercent = true;
    //View的可用总宽高
    private float mTotalWidth;
    private float mTotalHeight;
    //饼图+延长线+文字 所占用长方形总空间的长宽比
    private float mScale;
    //饼图的半径
    private float mRadius;
    private ArrayList<Integer> mColorLists;//饼图颜色
    private ArrayList<PieEntry1> mPieLists;
    private ArrayList<Integer> mColorCountLists;//饼图颜色
    private ArrayList<PieEntry1> mPieCountLists;
    private ArrayList<Integer> mCircleColorLists;//圆点颜色

    public PieView(Context context) {
        super(context);
    }

    public PieView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public PieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //高度为WrapContent时，设置默认高度
        if (mScale != 0 && MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            int height = (int) (mTotalWidth / mScale);
            setMeasuredDimension(widthMeasureSpec, height);
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //获取实际View的宽高
        mTotalWidth = w - getPaddingStart() - getPaddingEnd();
        mTotalHeight = h - getPaddingTop() - getPaddingBottom();
        //绘制饼图所处的正方形RectF
        initRectF();

    }

    //绘制饼图所处的正方形RectF
    private void initRectF() {

        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        //文字的高度
        float textHeight = fontMetrics.bottom - fontMetrics.top + fontMetrics.leading;
        //延长线的纵向长度
        float lineHeight = distance + bigCircleRadius + yOffset;
        //延长线的横向长度
        float lineWidth = distance + bigCircleRadius + xOffset + extend;
        //求出饼状图加延长线和文字 所有内容需要的长方形空间的长宽比
        mScale = mTotalWidth / (mTotalWidth + lineHeight * 2 + textHeight * 2 - lineWidth * 2);


        //长方形空间其短边的长度
        float shortSideLength;
        //通过宽高比选择短边
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //将坐标中心设到View的中心
        canvas.translate(mTotalWidth / 2, mTotalHeight / 2);

        if (isPieListsNull()) {
            mPaint.setColor(Color.BLACK);
            canvas.drawText("请通过setData添加数据", -120, 0, mPaint);
        } else {
            //绘制饼状图
            drawPie(canvas);
            //绘制中心空洞
            drawHole(canvas);
            //绘制延长线和文字
            drawLineAndText(canvas);
            //绘制各板块文字信息
            drawTextInner(canvas);
        }

    }

    /**
     * 绘制各板块文字信息
     *主要绘制饼图中的百分比
     * @param canvas 画布
     */
    private void drawTextInner(Canvas canvas) {
        mTextPaint.setTextSize(blockTextSize);
        mTextPaint.setColor(blockTextColor);
        float currentDegree = startAngle;
        for (PieEntry1 pie : mPieLists) {
            canvas.save();
            //当前模块角平分线的sin和cos值
            float mathCos = (float) (Math.cos((pie.sweepAngle / 2 + currentDegree) / 180f * Math.PI));
            float mathSin = (float) (Math.sin((pie.sweepAngle / 2 + currentDegree) / 180f * Math.PI));
            String msg = pie.getLabel();
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

    //绘制饼图
    private void drawPie(Canvas canvas) {
        //当前起始角度
        for (PieEntry1 pie : mPieLists) {
            mPaint.setColor(pie.getColor());//设置饼图的颜色
            canvas.drawArc(mRectF,
                    pie.getCurrentStartAngle(),
                    pie.getSweepAngle(),
                    true, mPaint);
        }
    }

    //绘制中心空洞
    private void drawHole(Canvas canvas) {
        if (mShowHole) {
            mPaint.setColor(holeColor);
            canvas.drawCircle(0, 0, mRadius * holeRadiusProportion / 100, mPaint);
        }
    }

    //延长线分为 延长点、同心圆环和线三个部分
    private void drawLineAndText(Canvas canvas) {

        //算出延长线转折点相对起点的正余弦值
        double offsetRadians = Math.atan(yOffset / xOffset);
        float cosOffset = (float) Math.cos(offsetRadians);
        float sinOffset = (float) Math.sin(offsetRadians);

        for (PieEntry1 pie : mPieLists) {

            //延长点的位置处于扇形的中间
            float halfAngle = pie.getCurrentStartAngle() + pie.getSweepAngle() / 2;
            float cos = (float) Math.cos(Math.toRadians(halfAngle));
            float sin = (float) Math.sin(Math.toRadians(halfAngle));
            //通过正余弦算出延长点的位置
            float xCirclePoint = (mRadius + distance) * cos;
            float yCirclePoint = (mRadius + distance) * sin;
            mPaint.setColor(pie.getColor());
            //将饼图分为4个象限，从右上角开始顺时针，每90度分为一个象限
            int quadrant = (int) (halfAngle + 90) / 90;
            //初始化 延长线的起点、转折点、终点
            float xLineStartPoint = 0;
            float yLineStartPoint = 0;
            float xLineTurningPoint = 0;
            float yLineTurningPoint = 0;
            float xLineEndPoint = 0;
            float yLineEndPoint = 0;

            String text = pie.getLabel();
            String abovelabel = pie.getAbovelabel();

            //延长点、起点、转折点在同一条线上
            //不同象限转折的方向不同
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
                    mlinetextPaint.setTextAlign(Paint.Align.RIGHT);
                    mcirclePaint.setColor(getResources().getColor(R.color.color_F291C2));
                    canvas.drawCircle(xLineEndPoint-80, yLineEndPoint - 60,circleRadius,mcirclePaint);
                    canvas.drawText(text, xLineEndPoint, yLineEndPoint - 50, mlinetextPaint);
                    mLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
                    mLinePaint.setTextSize(sp2px(12));
                    canvas.drawText(abovelabel, xLineEndPoint-60, yLineEndPoint-10, mLinePaint);

                    break;
                case 1:
                    xLineStartPoint = xCirclePoint + cosLength;
                    yLineStartPoint = yCirclePoint + sinLength;
                    xLineTurningPoint = xLineStartPoint + xOffset;
                    yLineTurningPoint = yLineStartPoint + yOffset;
                    xLineEndPoint = xLineTurningPoint + extend;
                    yLineEndPoint = yLineTurningPoint;
                    mlinetextPaint.setTextAlign(Paint.Align.RIGHT);
                    mcirclePaint.setColor(getResources().getColor(R.color.color_FFCC66));
                    canvas.drawCircle(xLineEndPoint-110, yLineEndPoint - 60,circleRadius,mcirclePaint);
                    canvas.drawText(text, xLineEndPoint, yLineEndPoint - 50, mlinetextPaint);
                    mLinePaint.setTextSize(sp2px(12));
                    canvas.drawText(abovelabel, xLineEndPoint-90, yLineEndPoint-10, mLinePaint);
                    break;
                case 2:
                    xLineStartPoint = xCirclePoint - cosLength;
                    yLineStartPoint = yCirclePoint + sinLength;
                    xLineTurningPoint = xLineStartPoint - xOffset;
                    yLineTurningPoint = yLineStartPoint + yOffset;
                    xLineEndPoint = xLineTurningPoint - extend;
                    yLineEndPoint = yLineTurningPoint;
                    mlinetextPaint.setTextAlign(Paint.Align.LEFT);
                    mcirclePaint.setColor(getResources().getColor(R.color.color_00CCCC));
                    canvas.drawCircle(xLineEndPoint-20, yLineEndPoint - 60,circleRadius,mcirclePaint);
                    canvas.drawText(text, xLineEndPoint, yLineEndPoint - 50, mlinetextPaint);
                    mLinePaint.setTextSize(sp2px(12));
                    canvas.drawText(abovelabel, xLineEndPoint, yLineEndPoint-10, mLinePaint);
                    break;
                case 3:
                    xLineStartPoint = xCirclePoint - cosLength;
                    yLineStartPoint = yCirclePoint - sinLength;
                    xLineTurningPoint = xLineStartPoint - xOffset;
                    yLineTurningPoint = yLineStartPoint - yOffset;
                    xLineEndPoint = xLineTurningPoint - extend;
                    yLineEndPoint = yLineTurningPoint;
                    mlinetextPaint.setTextAlign(Paint.Align.LEFT);
                    mcirclePaint.setColor(getResources().getColor(R.color.color_4D88FF));
                    canvas.drawCircle(xLineEndPoint-20, yLineEndPoint - 60,circleRadius,mcirclePaint);
                    canvas.drawText(text, xLineEndPoint, yLineEndPoint - 50, mlinetextPaint);
                    mLinePaint.setTextSize(sp2px(12));
                    canvas.drawText(abovelabel, xLineEndPoint, yLineEndPoint-10, mLinePaint);
                    break;
                default:
            }
            //绘制延长线
            canvas.drawLine(xLineStartPoint, yLineStartPoint, xLineTurningPoint, yLineTurningPoint, mLinePaint);
            canvas.drawLine(xLineTurningPoint, yLineTurningPoint, xLineEndPoint, yLineEndPoint, mLinePaint);
        }
    }


    //初始化画笔
    private void initPaint() {
        //绘制饼图的画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(sp2px(12));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(1f);
        //绘制延长线的上面说明文字
        mlinetextPaint = new Paint();
        mlinetextPaint.setAntiAlias(true);
        mlinetextPaint.setColor(getResources().getColor(R.color.color_half_transparent));
        mlinetextPaint.setTextSize(sp2px(12));
        mlinetextPaint.setStyle(Paint.Style.FILL);
        mlinetextPaint.setStrokeWidth(1f);
        //绘制延长线的颜色
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(getResources().getColor(R.color.color_9e));
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeWidth(2f);
        //绘制圆点
        mcirclePaint = new Paint();
        mcirclePaint.setColor(getResources().getColor(R.color.color_16CE12));
        mcirclePaint.setStyle(Paint.Style.FILL);
        mcirclePaint.setStrokeWidth(1f);

        //绘制中间百分比文字
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStrokeWidth(1f);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    //初始化数据
    private void initData() {
        if (isPieListsNull()) {
            return;
        }
        //当前起始角度
        float currentStartAngle = startAngle;
        for (int i = 0; i < mPieLists.size(); i++) {
            PieEntry1 pie = mPieLists.get(i);
            pie.setCurrentStartAngle(currentStartAngle);
            //每个数据百分比对应的角度
            float sweepAngle = pie.getPercentage() / 100 * 360;
            pie.setSweepAngle(sweepAngle);
            //起始角度不断增加
            currentStartAngle += sweepAngle;

            //未传入颜色时 以默认的颜色表作为颜色
            if (mColorLists == null || mColorLists.size() == 0) {
                int j = i % colors.length;
                pie.setColor(colors[j]);
            } else {
                pie.setColor(mColorLists.get(i));
            }
        }
    }
    //初始化数据
    private void initCountData() {
        if (isPieListsNull()) {
            return;
        }
        //当前起始角度
        float currentStartAngle = startAngle;
        for (int i = 0; i < mPieCountLists.size(); i++) {
            PieEntry1 pie = mPieCountLists.get(i);
            pie.setCurrentStartAngle(currentStartAngle);
            //每个数据百分比对应的角度
            float sweepAngle = pie.getPercentage() / 100 * 360;
            pie.setSweepAngle(sweepAngle);
            //起始角度不断增加
            currentStartAngle += sweepAngle;

            //未传入颜色时 以默认的颜色表作为颜色
            if (mColorCountLists == null || mColorCountLists.size() == 0) {
                int j = i % colors.length;
                pie.setColor(colors[j]);
            } else {
                pie.setColor(mColorCountLists.get(i));
            }
        }
    }
    //初始化颜色
    private void initColors() {
        if (isPieListsNull()) {
            return;
        }
        for (int i = 0; i < mPieLists.size(); i++) {
            mPieLists.get(i).setColor(mColorLists.get(i));
        }
    }

    //初始化颜色
    private void initCountColors() {
        if (isPieListsNull()) {
            return;
        }
        for (int i = 0; i < mPieCountLists.size(); i++) {
            mPieCountLists.get(i).setColor(mColorCountLists.get(i));
        }
    }

    //判断饼图颜色数据是否为空
    private boolean isPieListsNull() {
        return mPieLists == null || mPieLists.size() == 0;
    }

    //添加数据
    public void setData(ArrayList<PieEntry1> pieLists) {
        this.mPieLists = pieLists;
        initData();
        invalidate();
    }
    //添加数据
    public void setCountData(ArrayList<PieEntry1> mPieCountLists) {
        this.mPieCountLists = mPieCountLists;
        initCountData();
        invalidate();
    }

    //添加颜色
    public void setColors(ArrayList<Integer> colorLists) {
        this.mColorLists = colorLists;
        initColors();
        invalidate();
    }

    //添加颜色
    public void setCountColors(ArrayList<Integer> mColorCountLists) {
        this.mColorCountLists = mColorCountLists;
        initCountColors();
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

    public static class PieEntry1 {
        //颜色
        private int color;
        private int circleColor;
        //比分比
        private float percentage;
        //条目名
        private String label;
        private String abovelabel;
        //扇区起始角度
        private float currentStartAngle;
        //扇区总角度
        private float sweepAngle;

        public String getAbovelabel() {
            return abovelabel;
        }

        public void setAbovelabel(String abovelabel) {
            this.abovelabel = abovelabel;
        }

        public int getCircleColor() {
            return circleColor;
        }

        public void setCircleColor(int circleColor) {
            this.circleColor = circleColor;
        }

        public PieEntry1(float percentage, String label, String abovelabel) {
            this.percentage = percentage;
            this.label = label;
            this.abovelabel = abovelabel;
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