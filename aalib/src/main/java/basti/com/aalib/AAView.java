package basti.com.aalib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 游戏控件，找到的Demo游戏名称叫aa，所以直接叫AAView了
 *
 * Created by Bowen on 2016-02-16.
 */
public class AAView extends View {
    //属性
    private int centerColor;//中间的大圆颜色
    private float center_radius;//中间大圆的半径
    private int pointColor;//周围小圆的颜色
    private float point_radius;//周围小圆的半径
    private int textColor;//数字的颜色
    private float centerTextsize;//中间数字的字体大小
    private float pointTextsize;//周围数字的字体大小
    private int lineColor;//线的颜色
    private float lineWidth;//线的宽度

    //画笔
    private Paint centerPaint,pointPaint,linePaint,textPaint;

    //游戏配置参数
    private int level = 1;//旋转速度和level有关，level越高，速度越快
    private int initCount;//游戏开始时周围圆的数量
    private int restCount;//需要添加的圆的数量
    private int rotateSpeed = 1;//旋转速度
    private int mStartAngle = 0;//旋转角度
    private int biuSpeed = 5;//发射速度

    //其他参数
    private float line_length;//线的长度.两个圆心之间的距离
    private float width,height;//控件宽度
    private float actualWidth;//实际宽度，即去掉两边空隙的宽度
    private float offset;//待发射圆之间的距离，不包括半径

    //圆的集合
    private List<Point> initPoints;//初始圆集合
    private List<Point> restPoints;//游戏中需要添加的圆集合
    private List<Point> addingPoints;//添加过程中的圆集合
    private List<Point> addedPoints;//添加完成的圆集合
    public AAView(Context context) {
        this(context, null);
    }

    public AAView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AAView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //自定义属性
        initAttr(context, attrs);

        //初始化一些paint
        initPaints();
        
        //初始化数据
        initData();

        //设置时间
        initEvents();
    }

    private void initEvents() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //发射函数
                biu();
            }
        });

    }

    private void biu() {
        if (restCount > 0){

            final Point point = restPoints.get(restCount-1);

            float destY =(float)(width*0.9-point_radius);
            int duration = (int) ((point.getCy()-destY)/biuSpeed);
            //移除
            restPoints.remove(restCount-1);
            restCount--;
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(point.getCy(),destY);
            valueAnimator.setDuration(duration);
            valueAnimator.setRepeatCount(0);
            //添加到待完成圆的队列中
            addingPoints.add(point);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    point.setCy((Float) animation.getAnimatedValue());
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    if (checkCrash(point)) {

                    } else {
                        addingPoints.remove(point);
                        addedPoints.add(point);
                    }
                }
            });
            valueAnimator.start();
        }
    }

    //碰撞检测
    private boolean checkCrash(Point point) {
        return false;
    }

    private void initData() {
        initPoints = new ArrayList<>();
        restPoints = new ArrayList<>();
        addingPoints = new ArrayList<>();
        addedPoints = new ArrayList<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;
        height = h;
        actualWidth = (float) (width*0.8);
        line_length = actualWidth/2 - point_radius;
        offset = (height - width - center_radius - 3*2*point_radius) / 3;

        initRestPoint();
    }

    private void initRestPoint() {

        for (int i = 0;i < restCount;i++){
            Point point = new Point(width/2,width+center_radius+point_radius*(2*(restCount-1-i)+1)+(restCount-1-i)*offset, 90,(restCount-1-i)+1);
            restPoints.add(point);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制中间的圆
        drawCenter(canvas);

        //绘制开始时的圆
        if(initCount > 0){
            drawInitPoint(canvas, initCount);
        }

        //绘制需要添加的圆
        if (restCount > 0){
            drawRoundPoint(canvas,restCount);
        }

        //绘制添加过程中的圆
        if (addingPoints.size() > 0){
            drawAddingPoint(canvas);
        }

        //绘制已经添加完成的圆
        if (addedPoints.size() > 0){
            drawAddedPoint(canvas);
        }
        invalidate();
    }

    private void drawAddedPoint(Canvas canvas) {

        int size = addedPoints.size();



        for (int i = 0;i<size;i++){

            Point point = addedPoints.get(i);

            point.setAngle(point.getAngle() + rotateSpeed);

            float myAngle = point.getAngle();

            float cx = (float) (width/2 + Math.cos(myAngle*Math.PI/180) * line_length);
            float cy = (float) (width/2 + Math.sin(myAngle*Math.PI/180)*line_length);

            //绘制圆
            canvas.drawCircle(cx,cy,point_radius,pointPaint);
            //绘制线
            canvas.drawLine(width/2,width/2,cx,cy,linePaint);
        }

    }

    private void drawAddingPoint(Canvas canvas) {

        int size = addingPoints.size();

        for (int i = 0;i<size;i++){
            Point point = addingPoints.get(i);
            canvas.drawCircle(point.getCx(), point.getCy(), point_radius, pointPaint);
        }

    }

    private void drawRoundPoint(Canvas canvas, int restCount) {

        for (int i = restCount-1;i>=0;i--){
            Point point = restPoints.get(i);
            canvas.drawCircle(point.getCx(), point.getCy(), point_radius, pointPaint);
            if (i == restCount - 3){
                break;
            }

        }
    }

    private void drawInitPoint(Canvas canvas, int initCount) {

        int angle = 360/initCount;

        mStartAngle += rotateSpeed;

        for (int i = 0;i<initCount;i++){

            int myAngle = angle*i+mStartAngle;

            float cx = (float) (width/2 + Math.cos(myAngle*Math.PI/180) * line_length);
            float cy = (float) (width/2 + Math.sin(myAngle*Math.PI/180)*line_length);

            Point point = new Point(cx,cy,myAngle,i+1);

            //绘制圆
            canvas.drawCircle(cx,cy,point_radius,pointPaint);
            //绘制线
            canvas.drawLine(width/2,width/2,cx,cy,linePaint);

            //这里有问题
            //===========
            //initPoints.add(point);
        }
    }


    private void drawCenter(Canvas canvas) {

        canvas.drawCircle(width / 2, width / 2, center_radius, centerPaint);

    }

    private void initPaints() {
        centerPaint = new Paint();
        centerPaint.setAntiAlias(true);
        centerPaint.setStyle(Paint.Style.FILL);
        centerPaint.setColor(centerColor);

        pointPaint = new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(pointColor);

        linePaint= new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(lineColor);
        linePaint.setStrokeWidth(lineWidth);

        textPaint = new Paint();
    }


    //自定义属性
    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.AAView);

        centerColor = ta.getColor(R.styleable.AAView_center_color,getResources().getColor(R.color.black));
        center_radius = ta.getFloat(R.styleable.AAView_center_radius, DensityUtils.dp2px(context, Config.CENTER_RADIUS));
        pointColor = ta.getColor(R.styleable.AAView_point_color, getResources().getColor(R.color.black));
        point_radius = ta.getFloat(R.styleable.AAView_point_radius, DensityUtils.dp2px(context, Config.POINT_RADIUS));
        textColor = ta.getColor(R.styleable.AAView_text_color, getResources().getColor(R.color.white));
        centerTextsize = ta.getFloat(R.styleable.AAView_center_text_size, DensityUtils.sp2px(context, Config.CENTER_TEXTSIZE));
        pointTextsize = ta.getFloat(R.styleable.AAView_point_text_size, DensityUtils.sp2px(context, Config.POINT_TEXTSIZE));
        lineColor = ta.getColor(R.styleable.AAView_line_color, getResources().getColor(R.color.black));
        lineWidth = ta.getFloat(R.styleable.AAView_line_width, DensityUtils.dp2px(context, Config.LINEWIDTH));

        ta.recycle();
    }

    public void setInitCount(int initCount) {
        this.initCount = initCount;
        invalidate();
    }

    public void setRestCount(int restCount){
        this.restCount = restCount;
    }
}