package com.bupt.pulltozoom.pulltozoomrefresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by lianghenghui on 2016/5/10.
 */
public class RoundImageView extends View{

    private int type;
    private static final int TYPE_CIRCLE = 0;
    private static final int TYPE_ROUND = 1;
    private Bitmap bitmap;
    private int mWidth;//控件宽度
    private int mHeight;//控件高度
    private int mRadius;//半径
    public RoundImageView(Context context) {
        super(context);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,R.styleable.RoundImageView,defStyleAttr,0);
        int n = a.getIndexCount();
        for (int i = 0;i< n;i++){
            int attr = a.getIndex(i);

            switch (attr){
                case R.styleable.RoundImageView_src:
                    bitmap = BitmapFactory.decodeResource(getResources(),a.getResourceId(attr,0));
                    break;
                case R.styleable.RoundImageView_type:
                    type = a.getInt(attr,0);//默认为circle
                    break;
                case R.styleable.RoundImageView_borderRadius:
                    mRadius = a.getDimensionPixelOffset(attr,(int) TypedValue
                            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f,
                                    getResources().getDisplayMetrics()));//默认为10dp
                    break;
            }
        }
        a.recycle();
    }


    //直接继承自View需要自己处理wrap_content
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //设置宽度
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);

        if (specMode == MeasureSpec.EXACTLY){
            mWidth = specSize;
        }else {

            int desireByImg = getPaddingLeft()+getPaddingRight()+bitmap.getWidth();
            if (specMode == MeasureSpec.AT_MOST){
                mWidth = Math.min(desireByImg,specSize);
            }else {
                mWidth = desireByImg;
            }
        }

        //设置高度
        specMode = MeasureSpec.getMode(heightMeasureSpec);
        specSize = MeasureSpec.getSize(heightMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY){
            mHeight = specSize;
        }else {
            int desireByImg1 = getPaddingBottom()+getPaddingTop()+bitmap.getHeight();
            if (specMode == MeasureSpec.AT_MOST){
                mHeight = Math.min(desireByImg1,specSize);
            }else {
                mHeight = desireByImg1;
            }
        }

        setMeasuredDimension(mWidth, mHeight);
    }

    /**
     * 绘制过程
     */
    @Override
    protected void onDraw(Canvas canvas) {

        switch(type){
            case TYPE_CIRCLE:
                int min = Math.min(mWidth,mHeight);
                bitmap = Bitmap.createScaledBitmap(bitmap,min,min,false);//对图片进行压缩，如果长度不一致，按小的来进行压缩
                canvas.drawBitmap(createCircleBitmap(bitmap,min),0,0,null);
                break;
            case TYPE_ROUND:
                canvas.drawBitmap(createRoundConerImage(bitmap),0,0,null);

        }
    }


    /**
     * 根据给定图片创建圆形图片
     */
    private Bitmap createCircleBitmap(Bitmap source,int min){
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = Bitmap.createBitmap(min,min, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);//创建一个这样大小的画布
        canvas.drawCircle(min/2,min/2,min/2,paint);//首先绘制一个圆形
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));//不太懂这个地方，明天看
        canvas.drawBitmap(source,0,0,paint);
        return target;
    }

    /**
     * 根据给定图片创建圆角矩形
     */
    private Bitmap createRoundConerImage(Bitmap source)
    {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        RectF rect = new RectF(0, 0, source.getWidth(), source.getHeight());//首先创建一个矩形
        canvas.drawRoundRect(rect, mRadius, mRadius, paint);//根据给定的矩形，创建圆角矩形
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);//最后将给定图片在画布上用画笔画出来
        return target;
    }
}
