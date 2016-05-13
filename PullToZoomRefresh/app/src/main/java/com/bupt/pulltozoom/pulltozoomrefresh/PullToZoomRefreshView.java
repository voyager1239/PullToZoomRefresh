package com.bupt.pulltozoom.pulltozoomrefresh;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * Created by lianghenghui on 2016/5/9.
 */
public class PullToZoomRefreshView extends LinearLayout implements View.OnTouchListener{


    /**
     * 下拉状态
     */
    public static final int STATUS_PULL_TO_REFRESH = 0;

    /**
     * 释放刷新状态
     */
    public static final int STATUS_RELEASE_TO_REFRESH = 1;

    /**
     * 正在刷新状态
     */
    public static final int STATUS_REFRESHING = 2;


    /**
     * 刷新完成或者未刷新状态
     */
    public static final int STATUS_REFRESH_FINISHED = 3;

    /**
     * 当前状态
     */
    private int currentStatus = STATUS_REFRESH_FINISHED;

    /**
     * 上一个状态
     */
    private int lastStatus = currentStatus;

    /**
     * 判定为滑动之前的最大移动距离
     */
    private int touchSlop;

    /**
     * 记录当前手指按下时的屏幕纵坐标
     */
    private float yDown;

    /**
     * 判断当前是否可以下拉
     */
    private boolean ableToPull;

    /**
     * ListView实例
     */
    private ListView listView;
    /**
     * 下拉头的高度
     */
    private int mHeaderHeight;


    /**
     * 整个屏幕的高度
     */
    private int mScreenHeight;

    /**
     * 头部view的布局参数
     */
    private MarginLayoutParams headerLayoutParams;

    /**
     * 是否加载过一次，这里onLayout中的加载只需要加载一次
     */
    public boolean loadOnce;

    /**
     *ListView顶部的View
     */
    private View header;

    /**
     *实现释放时回弹效果的对象动画
     */
    private ObjectAnimator objectAnimator;

    /**
     *滑动距离
     */
    private int distance;

    /**
     *刷新监听
     */
    private PullToRefreshListener mListener;

    /**
     * 刷新界面的ID
     */
    private int mId = -1;

    /**
     * 用于保存刷新界面Id和上次刷新时间的SharedPreference,预留已做以后拓展
     */
    SharedPreferences preferences;

    public PullToZoomRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        header = LayoutInflater.from(context).inflate(R.layout.pulltorefreshheader,null,true);
        WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dpm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dpm);
        mScreenHeight = dpm.heightPixels;
        mHeaderHeight = mScreenHeight/2-(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,90,context.getResources().getDisplayMetrics());
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setOrientation(VERTICAL);
        addView(header, 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed&&!loadOnce){
            headerLayoutParams = (MarginLayoutParams) header.getLayoutParams();
            headerLayoutParams.height = mHeaderHeight;
            listView = (ListView) getChildAt(1);
            listView.setOnTouchListener(this);
            loadOnce = true;
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        setIsAbleToPull(event);
        if (ableToPull){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    yDown = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float yMove = event.getRawY();
                    distance = (int)(yMove-yDown);

                    if (distance<=0&&headerLayoutParams.height<=mHeaderHeight){
                        return false;
                    }

                    if (distance < touchSlop){
                        return false;
                    }

                    if (currentStatus != STATUS_REFRESHING){
                        if (headerLayoutParams.height>mHeaderHeight){
                            currentStatus = STATUS_RELEASE_TO_REFRESH;
                        }else {
                            currentStatus = STATUS_PULL_TO_REFRESH;
                        }

                        if (distance>0){
                            setHeaderHeight(-distance/6);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                default:
                    if (currentStatus == STATUS_RELEASE_TO_REFRESH){
                        new RefreshingTask().execute();
                        resetHeaderHeight();
                    }else if(currentStatus == STATUS_PULL_TO_REFRESH){
                        resetHeaderHeight();
                    }
                    break;
            }

            if (currentStatus == STATUS_PULL_TO_REFRESH||currentStatus == STATUS_RELEASE_TO_REFRESH){
                // 当前正处于下拉或释放状态，要让ListView失去焦点，否则被点击的那一项会一直处于选中状态
                listView.setPressed(false);//设置listView的点击状态
                listView.setFocusable(false);//使listView失去焦点
                listView.setFocusableInTouchMode(false);
                lastStatus = currentStatus;

                //当前正处于下拉或释放状态,应该通过返回true来屏蔽掉listView的滚动事件
                return true;
            }
        }
        return false;
    }

    public void setHeaderHeight(int offset){
        if (offset<0){
            headerLayoutParams.height = mHeaderHeight-offset;
            header.requestLayout();//当view发生改变时调用，这个方法的调用会导致view的Layout重绘
        }
    }

    public void resetHeaderHeight(){
        if (objectAnimator != null && objectAnimator.isRunning()) {
            return;
        }

        objectAnimator = ObjectAnimator.ofInt(this,"t",-distance/4,0);
        objectAnimator.setDuration(150);
        objectAnimator.start();
    }

    public void setIsAbleToPull(MotionEvent event){
        View firstChild = getChildAt(0);
        if (firstChild != null){
            int firstVisiblePos = listView.getFirstVisiblePosition();
            if (firstVisiblePos == 0&&firstChild.getTop() == 0){
                if (!ableToPull){
                    yDown = event.getRawY();
                }

                ableToPull = true;
            }else {
                ableToPull = false;
            }
        }else {
            //如果ListView为空，应该允许刷新
            ableToPull = true;
        }
    }


    /**
     * 当所有的刷新逻辑完成后，记录调用一下，否则你的ListView将一直处于刷新状态
     */
    public void finishRefreshing(){
        currentStatus = STATUS_REFRESH_FINISHED;
//        resetHeaderHeight();
        preferences.edit().putLong("update_at"+mId,System.currentTimeMillis()).commit();
    }

    /**
     * 给下拉刷新控件注册一个监听器
     *
     * 为了防止不同界面的下拉刷新在上次更新时间上互相有冲突，请不同界面在注册下拉刷新监听器时，一定要传入不同的id
     */
    public void setOnRefreshListener(PullToRefreshListener listener,int id){
        mListener = listener;
        mId = id;
    }

    class RefreshingTask extends AsyncTask<Void,Integer,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            int height = headerLayoutParams.height;

            while(true){
                height = height-20;
                if (height <= mHeaderHeight){
                    height = mHeaderHeight;
                    break;
                }
                publishProgress(height);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            currentStatus = STATUS_REFRESHING;
            publishProgress(height);
            if (mListener != null){
                mListener.onRefresh();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            headerLayoutParams.height = values[0];
            header.setLayoutParams(headerLayoutParams);
        }
    }


    class ResetHeaderHightTask extends AsyncTask<Void,Integer,Integer>{
        @Override
        protected Integer doInBackground(Void... params) {
            int height = headerLayoutParams.height;

            while(true){
                height = height-20;
                if (height <= mHeaderHeight){
                    height = mHeaderHeight;
                    break;
                }
                publishProgress(height);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            currentStatus = STATUS_REFRESHING;
            publishProgress(height);
            if (mListener != null){
                mListener.onRefresh();
            }
            return height;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            headerLayoutParams.height = values[0];
            header.setLayoutParams(headerLayoutParams);
        }

        @Override
        protected void onPostExecute(Integer height) {
            headerLayoutParams.height = height;
            header.setLayoutParams(headerLayoutParams);
            currentStatus = STATUS_REFRESH_FINISHED;
        }
    }


    /**
     * 下拉刷新的监听器，使用下拉刷新的地方应该注册此监听器来获取刷新回调
     */
    public interface PullToRefreshListener{
        /**
         * 刷新时会去回调此方法，在方法内编写具体的刷新逻辑。注意此方法是在子线程中调用的， 你可以不必另开线程来进行耗时操作。
         */
        void onRefresh();
    }
}
