package com.sample.startup.gc;

import android.util.Log;
import android.view.Choreographer;

public class ChoreographerMonitor {
    public static final String TAG = "ChoreographerMonitor";
    private long nowTime = 1;
    private int sm = 1;
    private int smResult = 60;

    public void start() {
        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                // frame 主线程绘制，callback在main thread调用
                // 绘制每一帧的开始时间(纳秒),只会执行一次，所以需要继续添加
//                Log.i(TAG, "frameTimeNanos: " +frameTimeNanos);
                Choreographer.getInstance().postFrameCallback(this);
                // 当前的帧率,如果有掉帧堆栈信息怎么拿？怎么解决掉帧问题
                // 这次课先不讲
                plusSM();
            }
        });
    }

    /**
     * 怎么计算当前的帧率
     */
    private void plusSM() {
        // 查考源码
        // 没超过一秒是不断 ++
        long t = System.currentTimeMillis();
        if (nowTime == 1) {
            nowTime = t;
        }
        // 统计一秒内有多少帧
        if (nowTime / 1000 == t / 1000) {
            sm++;
        } else if (t / 1000 - nowTime / 1000 >= 1) {
            smResult = sm;
            Log.e(TAG,"smResult -> "+smResult + ", thread name:"+ Thread.currentThread().getName());
            sm = 1;
            nowTime = t;
        }
    }
}
