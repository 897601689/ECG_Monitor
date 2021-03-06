package widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 2016/10/27. 画曲线控件
 */
public class MySurfaceView1 extends SurfaceView implements SurfaceHolder.Callback, Runnable {


    //SurfaceHolder
    private SurfaceHolder mSurfaceHolder;
    // 用于绘图的Canvas
    private Canvas mCanvas;

    public void setmIsDrawing(boolean mIsDrawing) {
        this.mIsDrawing = mIsDrawing;
    }

    // 子线程标志位
    private boolean mIsDrawing;

    //private
    private int x = -2;
    private float y = 128;
    private Path mPath;
    private Paint mPaint;

    List<Integer> curves = new ArrayList<>();

    private int curve = -1;//数据
    private String info = "";//导联
    private int backColor = Color.BLACK;;//背景颜色
    private int pen = Color.rgb(46, 139, 87);//画笔颜色
    private int textSize = 28;//字体大小
    private int max = 150; //曲线最大值
    private float amplitude = 0;//幅值长度
    private int time = 0;//时间刻度 总时长默认7秒

    public void setTime(int time) {
        this.time = time;
    }

    public void setCurve(int curve) {
        this.curve = curve;
        curves.add(curve);
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setBackColor(int backColor) {
        this.backColor = backColor;
    }

    public void setPen(int pen) {
        this.pen = pen;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    public void setMax(int max) {
        this.max = max;
    }
    //demo使用
    String[] data = new String[]{"128", "128", "128", "130", "132", "134", "135", "135", "135", "135", "135", "135", "135", "135", "135", "135", "137", "138", "138", "138", "137", "136", "136", "136", "136", "134", "131", "129", "129", "129", "129", "129", "129", "129", "129", "130", "130", "130", "130", "130", "130", "129", "129", "131", "137", "147", "159", "174", "190", "204", "212", "212", "203", "182", "156", "130", "109", "96", "90", "90", "95", "102", "111", "117", "122", "126", "127", "128", "128", "128", "128", "128", "127", "127", "127", "129", "132", "134", "135", "135", "135", "135", "135", "135", "135", "135", "135", "135", "135", "135", "135", "135", "137", "139", "141", "141", "141", "141", "141", "141", "141", "141", "142", "144", "147", "148", "148", "148", "148", "148", "148", "147", "144", "142", "142", "142", "142", "142", "140", "137", "136", "135", "134", "133", "130", "129"};

    public MySurfaceView1(Context context) {
        super(context);
        initView();
    }

    public MySurfaceView1(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MySurfaceView1(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    /**
     * 初始化，在构造函数中调用
     */
    private void initView() {


        //this.setZOrderOnTop(true);
        //this.getHolder().setFormat(PixelFormat.TRANSLUCENT);//使窗口支持透明度

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        //this.setZOrderMediaOverlay(true);//配合this.setZOrderOnTop(true);使用

        this.setKeepScreenOn(true);


        //setFocusable(true);//用键盘是否能获得焦点
        //setFocusableInTouchMode(true);//触摸是否能获得焦点

        mPath = new Path();
        //mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint = new Paint();
        mPaint.setColor(pen);
        mPaint.setAntiAlias(true);          //防锯齿
        mPaint.setDither(true);            //防抖动
        mPaint.setStyle(Paint.Style.FILL);//画笔类型 STROKE空心 FILL 实心
        mPaint.setStrokeWidth(2); // 设置画笔宽度

        //mPaint.setStrokeCap(Paint.Cap.ROUND); // 设置转弯处为圆角
        //mPaint.setStrokeJoin(Paint.Join.ROUND);//结合处为圆角
    }

    /**
     * SurfaceView创建
     *
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        new Thread(this).start();
    }

    /**
     * SurfaceView改变
     *
     * @param holder
     * @param format
     * @param width
     * @param height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * SurfaceView销毁
     *
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
    }

    @Override
    public void run() {
        try {
            mPaint.setColor(pen);
            drawCurve();

            Thread.sleep(1000);
            while (mIsDrawing) {
                //Thread.sleep(2);
                drawCurve();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    int index = 0;
 int num = 0;
    /**
     * 画曲线
     */
    public void drawCurve() {
        try {
            mCanvas = mSurfaceHolder.lockCanvas(new Rect(x, 0, x + 3 + 15, getHeight()));//
           // mCanvas.drawColor(backColor, PorterDuff.Mode.CLEAR);
            mCanvas.drawColor(backColor);
            num = index+10;
            while (curves.size() >= 2) {
                if(index>num)
                    break;
                Thread.sleep(5);
                //curve为-1时数据无效
                if (curves != null) {

                    //Log.e("SIZE", curves.size() + "");
                    float y2 = curves.get(index);

                    float yy1 = getHeight() - (getHeight() * y / max);
                    float yy2 = getHeight() - (getHeight() * y2 / max);
                    mPaint.setTextSize(textSize);
                    mCanvas.drawLine(x, yy1, x + 1, yy2, mPaint);
                    //画字
                    mCanvas.drawText(info, 20, 30, mPaint);
                    //画幅值长度
                    mCanvas.drawLine(getWidth() - 30, ((getHeight() - amplitude) / 2), getWidth() - 30, ((getHeight() - amplitude) / 2) + amplitude, mPaint);

                    //Log.e(y+" "+yy1,y2+" "+yy2);
//                int num = 7;
//                mCanvas.drawLine(0, ((getHeight() - amplitude) / 2 - num), getWidth(), ((getHeight() - amplitude) / 2 - num), mPaint);
//                mCanvas.drawLine(0, ((getHeight() - amplitude) / 2 + amplitude - num), getWidth(), ((getHeight() - amplitude) / 2 + amplitude - num), mPaint);

                    //画时间刻度
                    for (int i = 1; i <= time; i++) {
                        mCanvas.drawLine((getWidth() / time) * i, 0, (getWidth() / time) * i, 5, mPaint);
                    }
                    y = y2;
                    if (x > getWidth()) {
                        x = 0;
                        curves.clear();
                        index=0;
                    }
                    else
                        x += 1;
                    index++;

                } else {
                    //mCanvas = mSurfaceHolder.lockCanvas();
                    //mCanvas.drawColor(backColor);
                    mPaint.setTextSize(textSize);
                    mCanvas.drawText(info, 20, 30, mPaint);
                    mCanvas.drawLine(getWidth() - 30, ((getHeight() - amplitude) / 2), getWidth() - 30, ((getHeight() - amplitude) / 2) + amplitude, mPaint);
                }
            }
        } catch (Exception e) {
            //Log.e("error", "" + e.getMessage());
        } finally {
            if (mCanvas != null) {//确保每次都能提交
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }


    }

    @Override
    public void draw(Canvas canvas) {
        Path path = new Path();  //用矩形表示SurfaceView宽高
        RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());//15.0f即是圆角半径
        path.addRoundRect(rect, 5.0f, 5.0f, Path.Direction.CCW);     //裁剪画布，并设置其填充方式
        canvas.clipPath(path, Region.Op.REPLACE);
        super.draw(canvas);
    }


}
