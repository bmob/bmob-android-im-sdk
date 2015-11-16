package cn.bmob.im;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/** Bmob通知管理
  * @ClassName: BmobNotifyManager
  * @Description: TODO
  * @author smile
  * @date 2014-6-6 上午10:12:57
  */
public class BmobNotifyManager {

	public static NotificationManager mNotificationManager;
	
	//通知ID
	public static final int NOTIFY_ID = 0x000;
	
	Context globalContext;
	// 创建private static类实例
	private volatile static BmobNotifyManager INSTANCE;
   //同步锁
	private static Object INSTANCE_LOCK = new Object();

	/**
	 * 使用单例模式创建--双重锁定
	 */
	public static BmobNotifyManager getInstance(Context context) {
		if (INSTANCE == null)
			synchronized (INSTANCE_LOCK) {
				if (INSTANCE == null) {
					INSTANCE = new BmobNotifyManager();
				}
				INSTANCE.init(context);
			}
		return INSTANCE;
	}
	
	/**
	 * 只初始化创建一次上下文对象 init
	 */
	public void init(Context context) {
		this.globalContext = context;
		 mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
//	private static final Class[] mStartForegroundSignature = new Class[] {
//		int.class, Notification.class };
//	private static final Class[] mStopForegroundSignature = new Class[] { boolean.class };
//	private Method mStartForeground;
//	private Method mStopForeground;
//	private Object[] mStartForegroundArgs = new Object[2];
//	private Object[] mStopForegroundArgs = new Object[1];
//		
//	/** 创建前台服务
//	  * @Title: createForgroundService
//	  * @Description: TODO
//	  * @param @param mContext
//	  * @param @param notifyIconId
//	  * @param @param targetClass 
//	  * @return void
//	  * @throws
//	  */
//	public void createForgroundService(Context mContext,int notifyIconId,Class<?> targetClass){
//		try {
//			mStartForeground = PushService.class.getMethod("startForeground", mStartForegroundSignature);
//			mStopForeground = PushService.class.getMethod("stopForeground", mStopForegroundSignature);
//		} catch (NoSuchMethodException e) {
//			mStartForeground = mStopForeground = null;
//		}
//		/*
//		 * 我们并不需要为 notification.flags 设置 FLAG_ONGOING_EVENT，因为
//		 * 前台服务的 notification.flags 总是默认包含了那个标志位
//		 */
//        Notification notification = new Notification(notifyIconId, "officechat后台运行.",
//                System.currentTimeMillis());
//        Intent intent=new Intent(mContext, targetClass);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
//                intent, 0);
//        notification.setLatestEventInfo(mContext, "officechat","正在运行", contentIntent);
//        
//        /*
//         * 注意使用  startForeground ，id 为 0 将不会显示 notification
//         */
//        startForegroundCompat(1, notification);
//	}
//	
//	/**
//	 * 以兼容性方式开始前台服务
//	 */
//	private void startForegroundCompat(int id, Notification n){
//		if(mStartForeground != null){
//			mStartForegroundArgs[0] = id;
//			mStartForegroundArgs[1] = n;
//			
//			try {
//				mStartForeground.invoke(this, mStartForegroundArgs);
//			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				e.printStackTrace();
//			}
//			
//			return;
//		}
////		setForeground(true);
//		mNotificationManager.notify(id, n);
//	}
//	
//	/**
//	 * 以兼容性方式停止前台服务
//	 */
//	public void stopForegroundCompat(int id){
//		if(mStopForeground != null){
//			mStopForegroundArgs[0] = Boolean.TRUE;
//			
//			try {
//				mStopForeground.invoke(this, mStopForegroundArgs);
//			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				e.printStackTrace();
//			}
//			return;
//		}		
//		/*
//		 *  在 setForeground 之前调用 cancel，因为我们有可能在取消前台服务之后
//		 *  的那一瞬间被kill掉。这个时候 notification 便永远不会从通知一栏移除
//		 */
//		mNotificationManager.cancel(id);
////		setForeground(false);
//	}
		
//	/** 显示通知--不带参数
//	  * @param @param smallIcon :状态栏小图标
//	  * @param @param tickerText：状态栏提示语
//	  * @param @param largetIcon：大图标
//	  * @param @param contentTitle：通知标题
//	  * @param @param contentText ：通知内容
//	  * @param @param contentInfo ：补充信息
//	  * @param @param targetClass ：点击通知启动的activity
//	  * @return void
//	  * @throws
//	  */
//	public void showNotify(int smallIcon,String tickerText,int largetIcon,String contentTitle,String contentText,String contentInfo,Class<?> targetClass){
//		 Bitmap  remote_picture = BitmapFactory.decodeResource(globalContext.getResources(), largetIcon);
//
//	     // Creates an explicit intent for an ResultActivity to receive.
//	     Intent resultIntent = new Intent(globalContext, targetClass);
//	     resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//
//	     // This ensures that the back button follows the recommended convention for the back key.
//	     TaskStackBuilder stackBuilder = TaskStackBuilder.create(globalContext);
//
//	     // Adds the back stack for the Intent (but not the Intent itself).
//	     stackBuilder.addParentStack(targetClass);
//
//	     // Adds the Intent that starts the Activity to the top of the stack.
//	     stackBuilder.addNextIntent(resultIntent);
//	     PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//	     Notification notify =  new NotificationCompat.Builder(globalContext)
//	             .setSmallIcon(smallIcon)
//	             .setWhen(System.currentTimeMillis())
//	             .setTicker(tickerText)
//	             .setAutoCancel(true)
//	             .setLargeIcon(remote_picture)
//	             .setContentIntent(resultPendingIntent)
//	             .setDefaults(Notification.DEFAULT_ALL)//设置使用所有默认值（声音、震动、闪屏等）
////	             .addAction(smallIcon, "One", resultPendingIntent)
//	             .setContentTitle(contentTitle)
//	             .setContentText(contentText)
//	             .setContentInfo(contentInfo)
//	             .build();
//	     
//	     //可选参数
//	     notify.defaults |= Notification.DEFAULT_LIGHTS;//闪光
//	     notify.defaults |= Notification.DEFAULT_VIBRATE;//震动
//	     notify.defaults |= Notification.DEFAULT_SOUND;//声音
//	     notify.flags |= Notification.FLAG_ONLY_ALERT_ONCE;//只警示一次
//         
//	     mNotificationManager.notify(NOTIFY_ID, notify);
//	}
//	
//	/** 显示通知-可携带参数
//	  * showNotifyWithExtras
//	  * @return void
//	  * @throws
//	  */
//	public void showNotifyWithExtras(int smallIcon,String tickerText,int largetIcon,String contentTitle,String contentText,String contentInfo
//			,Intent resultIntent){
//		 Bitmap  remote_picture = BitmapFactory.decodeResource(globalContext.getResources(), largetIcon);
//
//	     // This ensures that the back button follows the recommended convention for the back key.
//	     TaskStackBuilder stackBuilder = TaskStackBuilder.create(globalContext);
//
////	      Adds the back stack for the Intent (but not the Intent itself).
////	     stackBuilder.addParentStack(backClass);
//
//	     // Adds the Intent that starts the Activity to the top of the stack.
//	     stackBuilder.addNextIntent(resultIntent);
//	     PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//
//	     Notification notify =  new NotificationCompat.Builder(globalContext)
//	             .setSmallIcon(smallIcon)
//	             .setWhen(System.currentTimeMillis())
//	             .setTicker(tickerText)
//	             .setAutoCancel(true)
//	             .setLargeIcon(remote_picture)
//	             .setContentIntent(resultPendingIntent)
//	             .setDefaults(Notification.DEFAULT_ALL)//设置使用所有默认值（声音、震动、闪屏等）
////	             .addAction(smallIcon, "One", resultPendingIntent)
//	             .setContentTitle(contentTitle)
//	             .setContentText(contentText)
//	             .setContentInfo(contentInfo)
//	             .build();
//	     
//	     //可选参数
//	     notify.defaults |= Notification.DEFAULT_LIGHTS;//闪光
//	     notify.defaults |= Notification.DEFAULT_VIBRATE;//震动
//	     notify.defaults |= Notification.DEFAULT_SOUND;//声音
//	     notify.flags |= Notification.FLAG_ONLY_ALERT_ONCE;//只警示一次
//	     mNotificationManager.notify(NOTIFY_ID, notify);
//	}
	
	/** 创建显示通知栏
	  * @param  icon:通知栏的图标
	  * @param  tickerText：状态栏提示语
	  * @param  contentTitle：通知标题
	  * @param  contentText：通知内容
	  * @param  targetClass ：点击之后进入的Class
	  * @return 
	  * @throws
	  */
	public  void showNotify(boolean isAllowVoice,boolean isAllowVirbate,int icon,String tickerText,String contentTitle,String contentText,Class<?> targetClass) {
		// TODO Auto-generated method stub
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		if(isAllowVoice){
			// 设置默认声音
			notification.defaults |= Notification.DEFAULT_SOUND;
		}
		if(isAllowVirbate){
			// 设定震动(需加VIBRATE权限)
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}
		notification.contentView = null;
		Intent intent = new Intent(globalContext, targetClass);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(globalContext, 0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(globalContext,contentTitle,	contentText, contentIntent);
		mNotificationManager.notify(NOTIFY_ID, notification);// 通知一下才会生效哦
	}
	
	/**创建显示通知栏
	 * @param  icon:通知栏的图标
	 * @param  tickerText：状态栏提示语
	 * @param  contentTitle：通知标题
	 * @param  contentText：通知内容
	 * @param  targetClass ：点击之后进入的Class
	 * @return 
	 * @throws
	 */
	public  void showNotifyWithExtras(boolean isAllowVoice,boolean isAllowVirbate,int icon,String tickerText,String contentTitle,String contentText,Intent targetIntent) {
		// TODO Auto-generated method stub
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		if(isAllowVoice){
			// 设置默认声音
			notification.defaults |= Notification.DEFAULT_SOUND;
		}
		if(isAllowVirbate){
			// 设定震动(需加VIBRATE权限)
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}
		notification.contentView = null;
		PendingIntent contentIntent = PendingIntent.getActivity(globalContext, 0,targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(globalContext,contentTitle,	contentText, contentIntent);
		mNotificationManager.notify(NOTIFY_ID, notification);// 通知一下才会生效哦
	}

	/** 取消指定通知栏
	  * @Title: cancelNotify
	  * @Description: TODO
	  * @param  
	  * @return 
	  * @throws
	  */
	public void cancelNotify(){
		if(mNotificationManager!=null){
			mNotificationManager.cancel(NOTIFY_ID);
		}
	}
	
	/** 取消所有通知栏
	  * @Title: cancelAll
	  * @Description: TODO
	  * @param  
	  * @return 
	  * @throws
	  */
	public void cancelAll(){
		if(mNotificationManager!=null){
			mNotificationManager.cancelAll();
		}
	}
}
