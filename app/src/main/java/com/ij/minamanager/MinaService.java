package com.ij.minamanager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * ┏┓　　┏┓+ +
 * 　　　　　　　┏┛┻━━━┛┻┓ + +
 * 　　　　　　　┃　　　　　　　┃
 * 　　　　　　　┃　　　━　　　┃ ++ + + +
 * 　　　　　　 ████━████ ┃+
 * 　　　　　　　┃　　　　　　　┃ +
 * 　　　　　　　┃　　　┻　　　┃
 * iBoosJie.　 ┃　　　　　　　┃ + +
 * 　　　　　　　┗━┓　　　┏━┛
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃ + + + +
 * 　　　　　　　　　┃　　　┃　　　　Code is far away from bug with the animal protecting
 * 　　　　　　　　　┃　　　┃ + 　　　　神兽保佑,代码无bug
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃　　+
 * 　　　　　　　　　┃　 　　┗━━━┓ + +
 * 　　　　　　　　　┃ 　　　　　　　┣┓
 * 　　　　　　　　　┃ 　　　　　　　┏┛
 * 　　　　　　　　　┗┓┓┏━┳┓┏┛ + + + +
 * 　　　　　　　　　　┃┫┫　┃┫┫
 * 　　　　　　　　　　┗┻┛　┗┻┛+ + + +
 * 创建人：贾杰
 * 创建日期： 2017/6/6
 * 说明：
 * 修改：
 */
public class MinaService extends Service{

    private ConnectionThread thread;

    @Override
    public void onCreate() {
        super.onCreate();
        //全局context 避免内存泄漏，不多说
        thread = new ConnectionThread("mina",getApplicationContext());
        thread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        thread.disConnection();
        thread = null;
    }


    //负责调用ConnectionManager类来完成与服务器连接
    class ConnectionThread extends HandlerThread{

        private Context context;

        boolean isConnection;

        ConnectionManager mManager;

        public ConnectionThread(String name,Context context) {
            super(name);
            this.context = context;

            ConnectionConfig config = new ConnectionConfig.Builder(context)
                    .setIp("127.0.0.1")
                    .setPort(9123)
                    .setReadBuilder(10240)
                    .setConnectionTimeout(10000).builder();
        }

        //run 开始连接我们的服务器
        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            //死循环
            for (;;){
                isConnection = mManager.connection();
                if (isConnection){
                    break;
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        //断开连接
        public void disConnection(){

            mManager.disConnect();
        }
    }
}
