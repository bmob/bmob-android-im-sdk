package cn.bmob.im.util;

import android.util.Log;
import cn.bmob.im.BmobChat;
import cn.bmob.im.config.BmobConfig;

/**
 * Log工具类：若调试可设置BmobChat中的DEBUG_MODE为true
 * 
 * @ClassName: BmobLog
 * @Description: TODO
 * @author smile
 * @date 2014-6-12 上午11:54:01
 */
public class BmobLog {

	public static final String Tag = "BmobChat";

	public static void v(String tag, String msg) {
		if (BmobChat.DEBUG_MODE)
			Log.v(tag, BmobConfig.SDK_VERSION+"-->"+msg);
	}

	public static void d(String tag, String msg) {
		if (BmobChat.DEBUG_MODE)
			Log.d(tag, BmobConfig.SDK_VERSION+"-->"+msg);
	}

	public static void i(String type, String msg) {
		if (BmobChat.DEBUG_MODE)
			Log.i(Tag,BmobConfig.SDK_VERSION+"-->("+type+")"+msg);
	}

	
	public static void w(String tag, String msg) {
		if (BmobChat.DEBUG_MODE)
			Log.w(tag, BmobConfig.SDK_VERSION+"-->"+msg);
	}

	public static void e(String tag, String msg) {
		if (BmobChat.DEBUG_MODE)
			Log.e(tag, BmobConfig.SDK_VERSION+"-->"+msg);
	}
	
	public static void i(String msg) {
		if (BmobChat.DEBUG_MODE)
			Log.i(Tag, BmobConfig.SDK_VERSION+"-->"+msg);
	}
	
}
