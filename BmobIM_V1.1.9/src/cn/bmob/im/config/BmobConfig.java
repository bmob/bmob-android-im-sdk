package cn.bmob.im.config;

import java.io.File;

import android.os.Environment;

/** 常量
  * @ClassName: BmobConfig
  * @Description: TODO
  * @author smile
  * @date 2014-5-29 下午5:47:51
  */
public class BmobConfig {
	
	/**
	 * 当前SDK版本号
	 */
	public static final String SDK_VERSION="1.1.9";
	
	/**
	 * 每个人的好友查询个数上限
	 */
	public static int LIMIT_CONTACTS = 100;
	
	/**
	 * 录音文件存储目录
	 */
	public static final String BMOB_VOICE_DIR =Environment.getExternalStorageDirectory().getAbsolutePath() +
			 File.separator+"BmobChat"+File.separator+"voice";
	
	/**
	 * 消息类型:目前暂支持文本（包含表情）的发送
	 */
	public static final int TYPE_TEXT=1;
	/**
	 * 消息类型:图片
	 */
	public static final int TYPE_IMAGE=2;
	
	/**
	 * 消息类型：位置
	 */
	public static final int TYPE_LOCATION=3;
	
	/**
	 * 消息类型:声音
	 */
	public static final int TYPE_VOICE=4;
	
	/**
	 * 消息是否未读：未读
	 */
	public static final int STATE_UNREAD=0;
	/**
	 * 消息是否未读：已读
	 */
	public static final int STATE_READED=1;
	
	/**
	 * 消息是否未读：未读但已收到
	 */
	public static final int STATE_UNREAD_RECEIVED=2;
	
	/**
	 * 消息发送状态：消息开始发送-
	 */
	public static final int STATUS_SEND_START=0;
	/**
	 * 消息发送状态：成功
	 */
	public static final int STATUS_SEND_SUCCESS=1;
	/**
	 * 消息发送状态：失败
	 */
	public static final int STATUS_SEND_FAIL=2;
	/**
	 * 消息发送状态：发送接收到
	 */
	public static final int STATUS_SEND_RECEIVERED=3;
	
	/**
	 * 好友邀请状态：发送添加好友请求时候的状态是未验证--等同于
	 */
	public static final int INVITE_ADD_NO_VALIDATION=0;//
	
	/**
	 * 好友邀请状态：对方同意添加好友
	 */
	public static final int INVITE_ADD_AGREE=1;//
	
	/**
	 * 好友邀请状态：收到对方的好友请求，但未处理
	 */
	public static final int INVITE_ADD_NO_VALI_RECEIVED = 2;
	
	/**
	 * 标签消息种类:添加好友
	 */
	public static final String TAG_ADD_CONTACT="add";//添加好友
	/**
	 * 标签消息种类:同意添加好友
	 */
	public static final String TAG_ADD_AGREE="agree";//
	/**
	 * 标签消息种类:已读
	 */
	public static final String TAG_READED="readed";//
	
	/**
	 * 标签消息种类:下线
	 */
	public static final String TAG_OFFLINE="offline";//
	
	/**
	 * 本地已存在
	 */
	public static final int CODE_COMMON_EXISTED=1002;//
	/**
	 * 是黑名单用户
	 */
	public static final String BLACK_YES = "y";
	
	/**
	 * 不是黑名单用户
	 */
	public static final String BLACK_NO = "n";
	
	
	/**
	 * Tag消息
	 */
	public static final String BROADCAST_NEW_MESSAGE="cn.bmob.new_msg";
	
	/**
	 * 标签消息
	 */
	public static final String BROADCAST_ADD_USER_MESSAGE="cn.bmob.add_user_msg";
	
	/**
	 * 查询成功，无数据返回
	 */
	public static final int CODE_COMMON_NONE=1000;//响应成功，无返回值
	
	/**
	 * 查询失败
	 */
	public static final int CODE_COMMON_FAILURE=1001;//响应出错
	
	public static final int CODE_USER_NULL=1003;//用户为空
	public static final int CODE_USERNAME_NULL=1004;//用户名为空
	public static final int CODE_PASSWORD_NULL=1005;//密码为空
	public static final int CODE_NETWORK_ERROR=1006;//无网络
	
}
