package com.ij.minamanager;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;

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
 * 说明：连接管理类
 * 修改：
 */
public class ConnectionManager {


    public static final String BROADCAST_ACTION ="com.ij.minamanager";

    public static final String MESSAGE ="message";

    private ConnectionConfig mConfig;

    private WeakReference<Context> mContext; //避免内存泄漏

    private NioSocketConnector mConnection;

    private IoSession mSession;

    private InetSocketAddress mAddress;

    public ConnectionManager(ConnectionConfig config) {
        this.mConfig = mConfig;
        this.mContext =new WeakReference<Context>(config.getContext());
        
        init();
    }

    //通过构建者模式来进行初始化
    private void init() {


        mAddress = new InetSocketAddress(mConfig.getIp(),mConfig.getPort());

        mConnection = new NioSocketConnector();

        //设置读数据大小
        mConnection.getSessionConfig().setReadBufferSize(mConfig.getReadBufferSize());

        //添加日志过滤
        mConnection.getFilterChain().addLast("Logging",new LoggingFilter());

        //编码过滤
        mConnection.getFilterChain().addLast("codec",new ProtocolCodecFilter(
                new ObjectSerializationCodecFactory()));

        //事物处理
        mConnection.setHandler(new DeafultHandler(mContext.get()));
    }

    //连接方法（外部调用）
    public boolean connection(){
        try {
            ConnectFuture futrue = mConnection.connect();
            //一直连接，直至成功
            futrue.awaitUninterruptibly();
            mSession = futrue.getSession();
        }catch (Exception e){
            return false;
        }

        return mSession == null ? false : true;
    }

    //断开连接方法（外部调用）
    public void disConnect(){
        //关闭
        mConnection.dispose();
        //大对象置空
        mConnection = null;
        mSession = null;
        mAddress = null;
        mContext = null;

    }


    //内部类实现事物处理
    private class DeafultHandler extends IoHandlerAdapter {

        private Context mContext;

        DeafultHandler(Context context){

            this.mContext = context;

        }

        @Override
        public void sessionCreated(IoSession session) throws Exception {


        }

        @Override
        public void sessionOpened(IoSession session) throws Exception {
            //将我们的session 保存到我们sessionManager 中，从而可以发送消息到服务器
        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            super.sessionClosed(session);
        }

        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            //1,EventBus 来进行事件通知
            //2,广播
            if (mContext!=null){
                Intent  intent = new Intent(BROADCAST_ACTION);
                intent .putExtra(MESSAGE,message.toString());
                //使用局部广播，保证安全性
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        }

        @Override
        public void messageSent(IoSession session, Object message) throws Exception {
            super.messageSent(session, message);
        }
    }
}
