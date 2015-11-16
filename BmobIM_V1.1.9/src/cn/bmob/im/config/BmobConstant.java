package cn.bmob.im.config;

/**
  * @ClassName: BmobConstant
  * @Description: TODO
  * @author smile
  * @date 2014-6-19 上午10:08:15
  */
public class BmobConstant {

	
	/**
	 * IOS通讯的基础json字段
	 */
	public static final String PUSH_BASE_APS="aps";
	public static final String PUSH_BASE_SOUND="sound";
	public static final String PUSH_BASE_ALERT="alert";
	public static final String PUSH_BASE_BADGE="badge";
	
	/**
	 * 聊天消息的相关json字段:标示该消息是否属于标签消息
	 */
	public static final String PUSH_KEY_TAG="tag";
	/**
	 * 聊天消息的相关json字段:标示该消息来源用户objectid
	 */
	public static final String PUSH_KEY_TARGETID="fId";//fromId
	
	/**
	 * 聊天消息的相关json字段:标示该消息接收方
	 */
	public static final String PUSH_KEY_TOID="tId";//toId
	
	/**
	 * 聊天消息的相关json字段:标示该消息的发送时间--解决出现两个用户之间相同消息的时间不对等导致无法更新消息状态的问题
	 */
	public static final String PUSH_KEY_MSGTIME="ft";//fromtime
	
	/**
	 * 预留的额外字段
	 */
	public static final String PUSH_KEY_EXTRA="ex";//
	
	/**
	 * 聊天消息的相关json字段:标示该消息类型
	 */
	public static final String PUSH_KEY_MSGTYPE="mt";//msgtype
	/**
	 * 聊天消息的相关json字段:标示该消息内容
	 */
	public static final String PUSH_KEY_CONTENT="mc";//content
	
	/**
	 * 聊天消息的相关json字段:标示该消息来源用户头像
	 */
	public static final String PUSH_KEY_TARGETAVATAR="fa";//fromavatar
	/**
	 * 聊天消息的相关json字段:标示该消息来源用户名
	 */
	public static final String PUSH_KEY_TARGETUSERNAME="fu";//fromusername
	/**
	 * 聊天消息的相关json字段:标示该消息来源用户昵称
	 */
	public static final String PUSH_KEY_TARGETNICK="fn";//fromnick
	
	//添加好友的相关组装字段-发送给对方，其携带的内容为请求方的内容
	/**
	 * 好友请求的相关json字段:标识请求方Objectid
	 */
	public static final String PUSH_ADD_ID="fId";//fromId
	/**
	 * 好友请求的相关json字段:标示请求方用户名
	 */
	public static final String PUSH_ADD_FROM_NAME="fu";//fromusername
	/**
	 * 好友请求的相关json字段:标示请求方头像
	 */
	public static final String PUSH_ADD_FROM_AVATAR="fa";
	/**
	 * 好友请求的相关json字段:标示好友请求方昵称
	 */
	public static final String PUSH_ADD_FROM_NICK="fn";
	/**
	 * 好友请求的相关json字段:标示好友请求接收方objectId
	 */
	public static final String PUSH_ADD_FROM_TOID="tId";
	/**
	 * 好友请求的相关json字段:标示该请求发送时间
	 */
	public static final String PUSH_ADD_FROM_TIME="ft";//请求发送时间
	
	/**
	 * 已读回执的json字段：此条消息的接收方:toId、会话id、消息时间、回执消息的发送方name:fromusername
	 */
	public static final String PUSH_READED_CONVERSIONID="mId";//消息id--msgId
	public static final String PUSH_READED_FROM_ID="fId";//回执消息的发送方id
	public static final String PUSH_READED_MSGTIME="ft";//请求发送时间
	
}
