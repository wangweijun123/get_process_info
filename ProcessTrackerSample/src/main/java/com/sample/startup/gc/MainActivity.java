package com.sample.startup.gc;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends Activity {
    public static Context sContext;
    public static ProcessCpuTracker processCpuTracker = new ProcessCpuTracker(android.os.Process.myPid());

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().getViewTreeObserver().addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
            @Override
            public void onDraw() {
                // 绘制前调用，包括子view changed
                Log.i(ChoreographerMonitor.TAG, "view tree about to draw ..");
            }
        });

        // 统计帧率
        ChoreographerMonitor choreographerMonitor = new ChoreographerMonitor();
        choreographerMonitor.start();
        this.animator();


        sContext = getApplicationContext();
        final Button testGc = (Button) findViewById(R.id.test_gc);
        testGc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 *  掉帧： 系统有一个接口的回调，开始一帧绘制的时候，把当前时间带了出来，就可以计算出一秒钟
                 *  绘制了多少帧， click时间，什么都不做，还是60帧， Click 主线程sleep 160ms，立马掉下10帧，10左右
                 *  如果是在子线程操作没有任何影响
                 *  在子线程去触发gc，去调用函数，分配一个很大数组，数组长度一万，while(true) {new int[], systme.gc()}
                 *  这个时候会看到
                 *  WaitForGcToComplete blocked Alloc on HeapTrim for 90.387ms
                 * 2021-09-22 00:10:48.181 11837-12183/com.dodola.breakpad I/dodola.breakpa: Starting a blocking GC Alloc
                 *
                 *
                 * */
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        processCpuTracker.update();
                        long start = System.currentTimeMillis();
                        Log.i(ChoreographerMonitor.TAG, "gc start ...");
                        testGc();
                        Log.i(ChoreographerMonitor.TAG, "gc finished spend time="+(System.currentTimeMillis()-start));
                        processCpuTracker.update();
                        android.util.Log.e("ProcessCpuTracker",
                                processCpuTracker.printCurrentState(SystemClock.uptimeMillis()));
                    }
                }).start();
            }
        });


        final Button testIO = (Button) findViewById(R.id.test_io);
        testIO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processCpuTracker.update();
                long start = System.currentTimeMillis();
                Log.i(ChoreographerMonitor.TAG, "io start ...");
                testIO();
                Log.i(ChoreographerMonitor.TAG, "io finished spend time="+(System.currentTimeMillis()-start));
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        processCpuTracker.update();
                        android.util.Log.e("ProcessCpuTracker",
                                processCpuTracker.printCurrentState(SystemClock.uptimeMillis()));
                    }
                }, 5000);

            }
        });

        final Button processOut = (Button) findViewById(R.id.test_process);
        processOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processCpuTracker.update();
                android.util.Log.e("ProcessCpuTracker",
                        processCpuTracker.printCurrentState(SystemClock.uptimeMillis()));

            }
        });

    }

    private void testIO() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeSth();
                try {
                    Thread.sleep(100000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setName("SingleThread");
        thread.start();
    }


    private void testGc() {
        /*for (int i = 0; i < 10000; i++) {
            int[] test = new int[100000];
            System.gc();
        }*/

        while (true) {
            int[] test = new int[100000];
            System.gc();
        }
    }


    private void writeSth() {
        try {
            File f = new File(getFilesDir(), "aee.txt");

            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fos = new FileOutputStream(f);

            byte[] data = new byte[1024 * 4 * 3000];

            for (int i = 0; i < 30; i++) {
                Arrays.fill(data, (byte) i);
                fos.write(data);
                fos.flush();
            }
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void animator() {
        ImageView image = (ImageView) findViewById(R.id.image);
        Animation hyperspaceJump = AnimationUtils.loadAnimation(this, R.anim.hyperspace_jump);
        image.startAnimation(hyperspaceJump);

    }
}
