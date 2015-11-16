package cn.bmob.im;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import cn.bmob.im.bean.BmobChatInstallation;
import cn.bmob.im.poll.BmobPollService;
import cn.bmob.im.util.BmobUtils;
import cn.bmob.push.BmobPush;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.listener.GetServerTimeListener;

/**
 * Bmob IM sdk初始化
 * 
 * @ClassName: BmobChat
 * @Description: TODO
 * @author smile
 * @date 2014-5-29 上午11:01:53
 */
public class BmobChat {

	/**
	 * debug模式：若调试时，可将其设置为true
	 */
	public static boolean DEBUG_MODE = false;
	
	// 创建private static类实例
	private volatile static BmobChat INSTANCE;

	private static Object INSTANCE_LOCK = new Object();

	Context globalContext;
	
	/**采用双重锁定
	  * @Title: getInstance
	  * @Description: TODO
	  * @param  context
	  * @return BmobChat
	  * @throws
	  */
	public static BmobChat getInstance(Context context) {
		if (INSTANCE == null)
			synchronized (INSTANCE_LOCK) {
				if (INSTANCE == null) {
					INSTANCE = new BmobChat();
				}
				INSTANCE.start(context);
			}
		return INSTANCE;
	}

	public void start(Context context) {
		this.globalContext = context;
	}
	
	/**
	 * 初始化Bmob sdk、Bmob Push服务、新建Installation表
	 * @Title: init
	 * @Description:
	 * @param  context
	 * @param  applicationId
	 * @return 
	 * @throws
	 */
	public void init(String applicationId){
		Bmob.initialize(globalContext, applicationId);
		BmobPush.startWork(globalContext, applicationId);
		BmobChatInstallation.getCurrentInstallation(globalContext).save();
		//获取服务器的时间
		Bmob.getServerTime(globalContext, new GetServerTimeListener() {
			
			@Override
			public void onSuccess(long time) {
				// TODO Auto-generated method stub
				long localTime = System.currentTimeMillis()/1000;
				BmobUtils.interval = (int) (localTime - time);
				Log.i("life", "时间差 = "+BmobUtils.interval );
			}
			
			@Override
			public void onFailure(int code, String msg) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	/** 启动检测服务,用于解决聊天丢包的问题
	  * @Title: startPollingService
	  * @param  seconds：秒
	  * @return 
	  * @throws
	  */
	public void startPollService(int seconds) {
		AlarmManager manager = (AlarmManager) globalContext.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(globalContext, BmobPollService.class);
		intent.setAction(BmobPollService.ACTION);
		PendingIntent pendingIntent = PendingIntent.getService(globalContext, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		long triggerAtTime = SystemClock.elapsedRealtime();
		manager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime,
				seconds * 1000, pendingIntent);
	}

	/** 停止检测服务
	  * stopPollingService
	  * @param   
	  * @return void
	  * @throws
	  */
	public void stopPollService() {
		AlarmManager manager = (AlarmManager) globalContext.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(globalContext, BmobPollService.class);
		intent.setAction(BmobPollService.ACTION);
		PendingIntent pendingIntent = PendingIntent.getService(globalContext, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		manager.cancel(pendingIntent);
	}

}
