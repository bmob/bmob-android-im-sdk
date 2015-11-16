package cn.bmob.im.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.bean.BmobRecent;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.DBConfig.DbUpdateListener;
import cn.bmob.im.util.BmobUtils;

/**
 * Bmob数据库管理：目前本地数据库中的表：消息表、会话表、好友表、好友请求表
 * @ClassName: BmobDB
 * @Description: TODO
 * @author smile
 * @date 2014-5-29 下午9:14:19
 */
public class BmobDB {

	private static HashMap<String, BmobDB> daoMap = new HashMap<String, BmobDB>();

	private SQLiteDatabase db;

	Context mContext;
	
	/** 创建并打开BmobDB：默认打开以当前登录用户名为dbName的数据库，建议使用此方法打开数据库
	  * @Title: create
	  * @Description: TODO
	  * @param  context
	  * @param  
	  * @return BmobDB
	  * @throws
	  */
	public static BmobDB create(Context context) {
		DBConfig config = new DBConfig();
		config.setContext(context);
		String dbName = BmobUserManager.getInstance(context).getCurrentUserObjectId();
		if(dbName!=null && !dbName.equals("")){
			config.setDbName(dbName);
		}
		return getInstance(config);
	}

	/** 创建指定dbname的数据库 --用于保存登陆过同一设备的多账户的消息
	  * @Title: create
	  * @Description: TODO
	  * @param  context
	  * @param  dbName
	  * @param  
	  * @return BmobDB
	  * @throws
	  */
	public static BmobDB create(Context context, String dbName) {
		DBConfig config = new DBConfig();
		config.setContext(context);
		config.setDbName(dbName);
		return getInstance(config);
	}
	
//	/**  自定义数据库配置创建数据库,可用于数据库升级
//	  * @Title: create
//	  * @Description: TODO
//	  * @param @param context 上下文对象
//	  * @param @param dbName 数据库名
//	  * @param @param dbVersion 数据库版本
//	  * @param @param dbUpdateListener 数据库的升级操作
//	  * @param @return 
//	  * @return BmobDB
//	  * @throws
//	  */
//	public static BmobDB create(Context context, String dbName, int dbVersion,
//			DbUpdateListener dbUpdateListener) {
//		DBConfig config = new DBConfig();
//		config.setContext(context);
//		config.setDbName(dbName);
//		config.setDbVersion(dbVersion);
//		config.setDbUpdateListener(dbUpdateListener);
//		return getInstance(config);
//	}

	/**
	 * 获取实例BmobDB
	 * @param daoConfig
	 * @return
	 */
	public static BmobDB getInstance(DBConfig daoConfig) {
		return init(daoConfig);
	}

	private synchronized static BmobDB init(DBConfig daoConfig) {
		BmobDB dao = daoMap.get(daoConfig.getDbName());
		if (dao == null) {
			dao = new BmobDB(daoConfig);
			daoMap.put(daoConfig.getDbName(), dao);
		}
		return dao;
	}

	private BmobDB(DBConfig config) {
		if (config == null)
			throw new RuntimeException("dbConfig is null");
		if (config.getContext() == null)
			throw new RuntimeException("android context is null");
		//获取指定dbname的数据库
		this.db = new SqliteDbHelper(config.getContext()
				.getApplicationContext(), config.getDbName(),
				config.getDbVersion(), config.getDbUpdateListener())
				.getWritableDatabase();
	}
//==============================================================================================
	// 消息表
	private static final String CHAT_TABLE_NAME = "chat";// 所有的聊天记录均存储在此数据库中：单聊\群聊
	private static final String COLUMN_NAME_ID = "_id";// 自增id
	private static final String COLUMN_NAME_UID = "conversationid";// 会话id：单聊：fromObjectId+toObjectId
															       // 群聊：groudId
	private static final String COLUMN_NAME_TOID = "toId";// 该消息接收方id
	
	private static final String COLUMN_NAME_BELONGID = "belongid";// 该消息是谁发送的：用于和当前登录用户做比较来区分发送/接收
	private static final String COLUMN_NAME_ACCOUNT = "belongaccount";// 消息发送者的账号
	private static final String COLUMN_NAME_BELONGNICK = "belongnick";// 消息发送者的昵称
	private static final String COLUMN_NAME_BELONGAVATAR = "belongavatar";// 消息发送者的头像
	private static final String COLUMN_NAME_TYPE = "msgtype";// 该消息类型---暂时只有Text类型
	private static final String COLUMN_NAME_TIME = "msgtime";// 时间
	private static final String COLUMN_NAME_CONTENT = "content";// 消息内容
	private static final String COLUMN_NAME_ISREADED = "isreaded";// 读取状态：未读
																	// -0、已读状态-1
	private static final String COLUMN_NAME_STATUS = "status";// 发送状态

	/**
	 * 创建或检测聊天消息表是否已经创建成功 createOrCheckTable
	 * 
	 * @return String
	 * @throws
	 */
	private void createOrCheckChatTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + CHAT_TABLE_NAME + " ("
				+ COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "// _id
				+ COLUMN_NAME_UID + " INTEGER, "// 会话id
				+ COLUMN_NAME_ACCOUNT + " TEXT, "// 账号
				+ COLUMN_NAME_BELONGNICK + " TEXT, "// 昵称
				+ COLUMN_NAME_BELONGAVATAR + " TEXT, "// 头像
				+ COLUMN_NAME_CONTENT + " TEXT NOT NULL, "// 消息体
				+ COLUMN_NAME_TOID + " TEXT NOT NULL, "// 消息体
				+ COLUMN_NAME_BELONGID + " TEXT NOT NULL, "// 消息发送者id
				+ COLUMN_NAME_ISREADED + " INTEGER, "// 是否未读：0-未读/1-已读
				+ COLUMN_NAME_STATUS + " INTEGER, "// 状态
				+ COLUMN_NAME_TYPE + " INTEGER, "// 类型--目前只有文本类型
				+ COLUMN_NAME_TIME + " TEXT); ");
	}
	
	/** 针对单聊 获取指定会话id的所有消息 ，支持分页操作
	  * @Title: queryMessages
	  * @Description: TODO
	  * @param  toId 当前聊天用户的Objectid
	  * @param  page 当前页
	  * @return List<ZBmobMessage>
	  * @throws
	  */
	public List<BmobMsg> queryMessages(String toId, int page) {
//		BmobLog.i("queryMessages-->toId:"+toId);
		String fromId = BmobUserManager.getInstance(mContext).getCurrentUserObjectId();
		List<BmobMsg> list = new LinkedList<BmobMsg>();
		int num = 10 * (page + 1);
		if (db != null) {
			String fromto = fromId + "&" + toId;
			String tofrom = toId + "&" + fromId;

			String sql = "SELECT * from " + CHAT_TABLE_NAME + " WHERE "
					+ COLUMN_NAME_UID + " IN ( '" + fromto + "' , '" + tofrom
					+ "' ) " + " ORDER BY " + COLUMN_NAME_ID + " DESC LIMIT "
					+ num;

			Cursor c = db.rawQuery(sql, null);
			while (c.moveToNext()) {
				String convertsationId = c.getString(c
						.getColumnIndex(COLUMN_NAME_UID));
				String content = c.getString(c
						.getColumnIndex(COLUMN_NAME_CONTENT));
				String belongId = c.getString(c
						.getColumnIndex(COLUMN_NAME_BELONGID));
				String belongavatar = c.getString(c
						.getColumnIndex(COLUMN_NAME_BELONGAVATAR));
				String nick = c.getString(c
						.getColumnIndex(COLUMN_NAME_BELONGNICK));
				String account = c.getString(c.getColumnIndex(COLUMN_NAME_ACCOUNT));
				int isReaded = c.getInt(c.getColumnIndex(COLUMN_NAME_ISREADED));
				int status = c.getInt(c.getColumnIndex(COLUMN_NAME_STATUS));
				int msgType = c.getInt(c.getColumnIndex(COLUMN_NAME_TYPE));
				String toid = c.getString(c.getColumnIndex(COLUMN_NAME_TOID));//这个toid和toId还是不一样的，toId是对方的，而toid有时候却是自己的
				
				String msgtime = c.getString(c.getColumnIndex(COLUMN_NAME_TIME));
				BmobMsg msg = new BmobMsg("",convertsationId, 
						content,
						toid,
						belongId, 
						account, 
						belongavatar, 
						nick,
						msgtime,
						msgType, 
						isReaded, 
						status);
				list.add(msg);
			}
			
			if (c != null && !c.isClosed()) {
				c.close();
				c = null;
			}
			Collections.reverse(list);// 前后反转一下消息记录
		}
		return list;
	}
	
	/** 获取指定消息
	  * @Title: getMessage
	  * @Description: TODO
	  * @param  conversionId
	  * @param  msgTime
	  * @return BmobMsg
	  * @throws
	  */
	public BmobMsg getMessage(final String conversionId, final String msgTime){
		BmobMsg msg =new BmobMsg();
		if (db != null) {
			String[] args = new String[] { conversionId,msgTime};
			Cursor c = db.query(CHAT_TABLE_NAME, null, COLUMN_NAME_UID + " = ?  AND " + COLUMN_NAME_TIME + " = ?",
					args, null, null, null);
			if (c.moveToFirst()) {
				String convertsationId = c.getString(c.getColumnIndex(COLUMN_NAME_UID));
				String content = c.getString(c
						.getColumnIndex(COLUMN_NAME_CONTENT));
				String belongId = c.getString(c
						.getColumnIndex(COLUMN_NAME_BELONGID));
				String belongavatar = c.getString(c
						.getColumnIndex(COLUMN_NAME_BELONGAVATAR));
				String nick = c.getString(c
						.getColumnIndex(COLUMN_NAME_BELONGNICK));
				String account = c.getString(c.getColumnIndex(COLUMN_NAME_ACCOUNT));
				int isReaded = c.getInt(c.getColumnIndex(COLUMN_NAME_ISREADED));
				int status = c.getInt(c.getColumnIndex(COLUMN_NAME_STATUS));
				int msgType = c.getInt(c.getColumnIndex(COLUMN_NAME_TYPE));
				String toid = c.getString(c.getColumnIndex(COLUMN_NAME_TOID));//这个toid和toId还是不一样的，toId是对方的，而toid有时候却是自己的
				String msgtime = c.getString(c.getColumnIndex(COLUMN_NAME_TIME));
				msg = new BmobMsg("",convertsationId, 
						content,
						toid,
						belongId, 
						account, 
						belongavatar, 
						nick,
						msgtime,
						msgType, 
						isReaded, 
						status);
			}
			
			if (c != null && !c.isClosed()) {
				c.close();
				c = null;
			}
		}
		return msg;
	}

	/** 查询该会话对象的聊天消息记录总数
	  * @Title: queryTotalCount
	  * @Description: TODO
	  * @param  toId 当前聊天对象的objectid
	  * @return 
	  * @throws
	  */
	public int queryChatTotalCount(String toId){
		String fromId = BmobUserManager.getInstance(mContext).getCurrentUserObjectId();
		String fromto = fromId + "&" + toId;
		String tofrom = toId + "&" + fromId;
		String sql = "SELECT * from " + CHAT_TABLE_NAME + " WHERE "
				+ COLUMN_NAME_UID + " IN ( '" + fromto + "' , '" + tofrom
				+ "' )";
		Cursor c = db.rawQuery(sql, null);
		int count = c.getCount();
		if (c != null && !c.isClosed()) {
			c.close();
			c = null;
		}
		return count;
	}
	
	/**删除与指定对象的所有消息 
	  * @Title: deleteMessages
	  * @Description: TODO
	  * @param  toid 当前聊天的objectid
	  * @return 
	  * @throws
	  */
	public void deleteMessages(String toid) {
		String fromId = BmobUserManager.getInstance(mContext).getCurrentUserObjectId();
		String[] args = new String[] { fromId + "&" + toid, toid + "&" + fromId };
		db.delete(CHAT_TABLE_NAME, COLUMN_NAME_UID + " in(?,?)", args);
	}

	/** 删除会话表中指定消息
	  * @Description: TODO
	  * @param  
	  * @return 
	  * @throws
	  */
	public void deleteTargetMsg(BmobMsg msg){
		if(db.isOpen()){
			db.delete(CHAT_TABLE_NAME, COLUMN_NAME_UID + " = ? AND " + COLUMN_NAME_TIME + " = ? ", new String[]{msg.getConversationId(),msg.getMsgTime()});
		}
	}
	
	/** 保存聊天消息 saveMessage
	  * @Title: saveMessage
	  * @Description: TODO
	  * @param  message
	  * @return int
	  * @throws
	  */
	public int saveMessage(BmobMsg message) {
		int id = -1;
		if (db.isOpen()) {
			ContentValues values = new ContentValues();
			values.put(COLUMN_NAME_CONTENT, message.getContent());
			values.put(COLUMN_NAME_STATUS, message.getStatus());
			values.put(COLUMN_NAME_BELONGAVATAR, message.getBelongAvatar());//也同样更新头像
			if(!checkTargetMsgExist(message.getConversationId(), message.getMsgTime())){//不存在则插入
				values.put(COLUMN_NAME_UID, message.getConversationId());
				values.put(COLUMN_NAME_TIME, message.getMsgTime());
				values.put(COLUMN_NAME_ISREADED, message.getIsReaded());
				values.put(COLUMN_NAME_TYPE, message.getMsgType());
				values.put(COLUMN_NAME_BELONGID, message.getBelongId());
				values.put(COLUMN_NAME_BELONGNICK, message.getBelongNick());
				values.put(COLUMN_NAME_ACCOUNT, message.getBelongUsername());
				values.put(COLUMN_NAME_TOID, message.getToId());//增加toId
				db.insert(CHAT_TABLE_NAME, null, values);
			}else{//更新指定消息的内容、状态、头像
				String[] args = new String[] { message.getConversationId(),message.getMsgTime()};
				db.update(CHAT_TABLE_NAME, values, COLUMN_NAME_UID + " = ?  AND "+ COLUMN_NAME_TIME + " = ? ", args);
			}
			// 插入成功
			Cursor c = db.rawQuery("select last_insert_rowid() from "+ CHAT_TABLE_NAME, null);
			if (c.moveToFirst()) {
				id = c.getInt(0);
			}
			if (c != null && !c.isClosed()) {
				c.close();
				c = null;			}
		}
		return id;
	}
//
//	/** 当用户的头像信息发生变化的时候，以往数据库中的头像信息也应该随即改变
//	  * @Title: updateUserAvatar
//	  * @Description: TODO
//	  * @param  
//	  * @return void
//	  * @throws
//	  */
//	public void updateUserAvatar(BmobMsg message){
//		ContentValues values = new ContentValues();
//		values.put(COLUMN_NAME_BELONGAVATAR, BmobConfig.STATE_READED);
//		String conversionId = message.getConversationId();
//		if(checkAvatarUpdate(conversionId, message.getBelongAvatar())){
//			String[] args = new String[] { conversionId};
//			db.update(CHAT_TABLE_NAME, values, COLUMN_NAME_UID + " = ? ", args);
//		}
//	}
//	
//	/** 检查头像是否发送变化
//	  * @Title: checkTargetMsgExist
//	  * @Description: TODO
//	  * @param  
//	  * @return void
//	  * @throws
//	  */
//	private boolean checkAvatarUpdate(String conversionId,String newAvatar){
//		String[] args = new String[] { conversionId};
//		String[] colums = new String[] { COLUMN_NAME_BELONGAVATAR};
//		Cursor c = db.query(CHAT_TABLE_NAME, colums, COLUMN_NAME_UID + " = ? ",args, null, null, null);
//		if(c!=null && c.moveToFirst()){
//			String oldAvatar  = "";
//			try {
//				oldAvatar =c.getString(c.getColumnIndex(COLUMN_NAME_BELONGAVATAR));
//			}finally{
//				if (c != null) {
//					c.close();
//					c = null;
//				}
//			}
//			if(oldAvatar.equals(newAvatar)){
//				return false;
//			}else{
//				return true;
//			}
//		}else{
//			return false;
//		}
//	}
	
	
	/** 获取指定聊天对象之间的未读消息数 getUnreadCount
	  * @Title: getUnreadCount
	  * @Description: TODO
	  * @param  toId
	  * @return int
	  * @throws
	  */
	public int getUnreadCount(String toId) {
		String fromId = BmobUserManager.getInstance(mContext).getCurrentUserObjectId();
		String fromto = fromId + "&" + toId;
		String tofrom = toId + "&" + fromId;
//		String sql = "SELECT * from " + CHAT_TABLE_NAME + " WHERE "	+ COLUMN_NAME_UID + " IN ( '" + fromto + "' , '" + tofrom+ "' ) AND " 
//				+ COLUMN_NAME_ISREADED + " IN ( '" + BmobConfig.STATE_UNREAD_RECEIVED + "' , '" + BmobConfig.STATE_UNREAD
//				+ "' ) ";
		String sql = "SELECT * from " + CHAT_TABLE_NAME + " WHERE "	+ COLUMN_NAME_UID + " IN ( '" + fromto + "' , '" + tofrom+ "' ) AND " 
		+ COLUMN_NAME_ISREADED + " = " + BmobConfig.STATE_UNREAD_RECEIVED ;
		Cursor c = db.rawQuery(sql, null);
		int count = c.getCount();
		if (c != null && !c.isClosed()) {
			c.close();
			c = null;
		}
		return count;
	}
	
	/** 获取所有的未读消息数
	  * @Title: getAllUnReadCount
	  * @Description: TODO
	  * @return int
	  * @throws
	  */
	public int getAllUnReadCount(){
		String sql = "SELECT * from " + CHAT_TABLE_NAME + " WHERE "	+ COLUMN_NAME_ISREADED + " = "+ BmobConfig.STATE_UNREAD_RECEIVED;
		Cursor c = db.rawQuery(sql, null);
		int count = 0;
		try {
			count = c.getCount();
		}finally{
			if (c != null && !c.isClosed()) {
				c.close();
				c = null;
			}
		}
		return count;
	}
	

	/** 是否有未读的消息--针对所有用户
	  * @param  
	  * @return 
	  * @throws
	  */
	public boolean hasUnReadMsg(){
		String sql = "SELECT * from " + CHAT_TABLE_NAME + " WHERE "	+ COLUMN_NAME_ISREADED + " = "+ BmobConfig.STATE_UNREAD_RECEIVED;
//		String sql = "SELECT * from " + CHAT_TABLE_NAME + " WHERE "	+ COLUMN_NAME_ISREADED + " IN ( '" + BmobConfig.STATE_UNREAD_RECEIVED + "' , '" + BmobConfig.STATE_UNREAD
//				+ "' ) ";
		Cursor c = db.rawQuery(sql, null);
		int count = 0;
		try {
			count = c.getCount();
		}finally{
			if (c != null && !c.isClosed()) {
				c.close();
				c = null;
			}
		}
		if(count>0){
			return true;
		}else{
			return false;
		}
	}
	
	/** 重置与指定用户之间的所有未读消息
	  * @Title: resetUnread
	  * @Description: TODO
	  * @param  fromId
	  * @param  toId 
	  * @throws
	  */
	public void resetUnread(String toId) {
		String fromId = BmobUserManager.getInstance(mContext).getCurrentUserObjectId();
		String fromto = fromId + "&" + toId;
		String tofrom = toId + "&" + fromId;
		String[] args = new String[] { fromto, tofrom };
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_ISREADED, BmobConfig.STATE_READED);
		db.update(CHAT_TABLE_NAME, values, COLUMN_NAME_UID + " in( ?, ? )", args);
	}
	
	/**更新指定消息的消息状态
	  * @Title: updateTargetMsgStatus
	  * @Description: TODO
	  * @param  status:消息状态：发送成功/发送对方已阅读
	  * @param  msg 该消息
	  * @return 
	  * @throws
	  */
	public void updateTargetMsgStatus(int status,String conversionId,String msgTime) {
		String[] args = new String[] { conversionId,msgTime};
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_STATUS, status);
		db.update(CHAT_TABLE_NAME, values, COLUMN_NAME_UID + " = ?  AND "+ COLUMN_NAME_TIME + " = ? ", args);
	}
	
	/** 更新指定消息的content字段--主要用于更新收到的语音类型的消息
	  * @Title: updateContentForTargetMsg
	  * @Description: TODO
	  * @param  
	  * @return 
	  * @throws
	  */
	public void updateContentForTargetMsg(String localPath,BmobMsg msg){
		String[] args = new String[] { msg.getConversationId(),msg.getMsgTime()};
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_CONTENT, localPath+"&"+msg.getContent());//
		db.update(CHAT_TABLE_NAME, values, COLUMN_NAME_UID + " = ?  AND "+ COLUMN_NAME_TIME + " = ? ", args);
	
	}
	
	/** 检查指定消息是否存在
	  * @Title: checkTargetMsgExist
	  * @Description: TODO
	  * @param  
	  * @return 
	  * @throws
	  */
	public boolean checkTargetMsgExist(String conversionId,String msgTime){
		String[] args = new String[] { conversionId,msgTime};
		Cursor c = db.query(CHAT_TABLE_NAME, null, COLUMN_NAME_UID + " = ?  AND "+ COLUMN_NAME_TIME + " = ? ",args, null, null, null);
		boolean isTrue =false;
		try {
			isTrue = c.moveToFirst();
		}finally{
			if (c != null) {
				c.close();
				c = null;
			}
		}
		return isTrue;
	}
	
	//=======================================================================
	// 最近会话表
	private static final String RECENT_TABLE_NAME = "recent";
	
	private static final String COLUMN_RECENT_ID = "tuid";//targetUid
	private static final String COLUMN_RECENT_USERNAME = "tusername";
	private static final String COLUMN_RECENT_NICK = "tnick";
	private static final String COLUMN_RECENT_AVATAR = "tavatar";
	private static final String COLUMN_RECENT_LASTMESSAGE = "lastmessage";

	/**
	 * 创建或检测最近会话表是否已经创建成功
	 * 
	 * @return String
	 * @throws
	 */
	private void createOrCheckRecentTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + RECENT_TABLE_NAME + " ("
				+ COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "// 
				+ COLUMN_RECENT_ID + " TEXT, "// 对方id
				+ COLUMN_RECENT_USERNAME + " TEXT, "// 对方的昵称
				+ COLUMN_RECENT_NICK + " TEXT, "// 对方的昵称
				+ COLUMN_RECENT_AVATAR + " TEXT, "// 对方的头像
				+ COLUMN_RECENT_LASTMESSAGE + " TEXT NOT NULL, "// 消息体
				+ COLUMN_NAME_TYPE + " INTEGER, "// 类型--目前只有文本类型
				+ COLUMN_NAME_TIME + " TEXT); ");
	}

	/** 查询登陆用户所有会话列表 
	  * @Title: queryRecents
	  * @Description: TODO
	  * @return List<BmobRecent>
	  * @throws
	  */
	public List<BmobRecent> queryRecents() {
		List<BmobRecent> recents = new ArrayList<BmobRecent>();
		Cursor c = db.rawQuery("SELECT * from " + RECENT_TABLE_NAME + " ORDER BY " + COLUMN_NAME_TIME, null);
		while (c.moveToNext()) {
			String uid = c.getString(c.getColumnIndex(COLUMN_RECENT_ID));// 一直是对方的uid
			String nick = c.getString(c.getColumnIndex(COLUMN_RECENT_NICK));
			String userName = c.getString(c
					.getColumnIndex(COLUMN_RECENT_USERNAME));
			String avatar = c.getString(c.getColumnIndex(COLUMN_RECENT_AVATAR));
			long time = c.getLong(c.getColumnIndex(COLUMN_NAME_TIME));
			String message = c.getString(c
					.getColumnIndex(COLUMN_RECENT_LASTMESSAGE));
			int type = c.getInt(c.getColumnIndex(COLUMN_NAME_TYPE));
			BmobRecent item = new BmobRecent(uid, userName, nick, avatar,
					message, time, type);
			recents.add(item);
		}
		
		if (c != null && !c.isClosed()) {
			c.close();
			c = null;	
		}
		Collections.sort(recents);// 按时间降序
		return recents;
	}

	/** 保存本地会话
	  * @Title: saveRecent
	  * @Description: TODO
	  * @param  recent 
	  * @return 
	  * @throws
	  */
	public void saveRecent(BmobRecent recent) {
		if (db.isOpen()) {// 数据库已打开并且目标uid之前有存储过消息，更新最后一条消息就行
			ContentValues values = new ContentValues();
			values.put(COLUMN_RECENT_AVATAR, recent.getAvatar());
			values.put(COLUMN_RECENT_NICK, recent.getNick());
			if (!isRecentExist(recent.getTargetid())) {// 不存在
				values.put(COLUMN_RECENT_ID, recent.getTargetid());
				values.put(COLUMN_RECENT_USERNAME, recent.getUserName());
				values.put(COLUMN_NAME_TIME, recent.getTime());
				values.put(COLUMN_RECENT_LASTMESSAGE, recent.getMessage());
				values.put(COLUMN_NAME_TYPE, recent.getType());
				db.insert(RECENT_TABLE_NAME, null, values);
			} else {//存在的话
				values.put(COLUMN_RECENT_LASTMESSAGE, recent.getMessage());
				if(isRecentExist(recent.getTargetid(), String.valueOf(recent.getTime()))){//更新某一个特定时间的消息
					db.update(RECENT_TABLE_NAME, values, COLUMN_RECENT_ID + " = ?  AND  " + COLUMN_NAME_TIME + " = ?",
							new String[] { recent.getTargetid(),String.valueOf(recent.getTime())});
				}else{//更新最后一条消息
					values.put(COLUMN_NAME_TIME, recent.getTime());
					values.put(COLUMN_NAME_TYPE, recent.getType());
					db.update(RECENT_TABLE_NAME, values, COLUMN_RECENT_ID + " = ?",
							new String[] { recent.getTargetid() });
				}
			}
		}
	}
	
	/** 检查目标用户id(聊天对象)是否在（recent）数据库中 
	  * @Title: isRecentExist
	  * @Description: TODO
	  * @param  targetUid
	  * @return boolean
	  * @throws
	  */
	private boolean isRecentExist(String targetUid) {
		String[] args = new String[] { targetUid};
		Cursor c = db.query(RECENT_TABLE_NAME, null, COLUMN_RECENT_ID + "=?",
				args, null, null, null);
		boolean isTrue =false;
		try {
			isTrue = c.moveToFirst();
		}finally{
			if (c != null) {
				c.close();
				c = null;
			}
		}
		return isTrue;
	}
	
	/** 检查目标用户id指定时间的消息是否在（recent）数据库中 
	 * @Title: isRecentExist
	 * @Description: TODO
	 * @param  targetUid
	 * @return boolean
	 * @throws
	 */
	private boolean isRecentExist(String targetUid,String msgTime) {
		String[] args = new String[] { targetUid,msgTime};
		Cursor c = db.query(RECENT_TABLE_NAME, null, COLUMN_RECENT_ID + " = ?  AND " + COLUMN_NAME_TIME + " = ?",
				args, null, null, null);
		boolean isTrue =false;
		try {
			isTrue = c.moveToFirst();
		}finally{
			if (c != null) {
				c.close();
				c = null;
			}
		}
		return isTrue;
	}
	
	
	/** 删除与指定用户之间的会话记录
	  * @Title: deleteRecent
	  * @Description: TODO
	  * @param  targetUid 
	  * @return 
	  * @throws
	  */
	public void deleteRecent(String targetUid){
		db.delete(RECENT_TABLE_NAME, COLUMN_RECENT_ID +" = ?", new String[] { targetUid });
	}
	
	
	/** 删除所有的聊天记录-用于清除缓存操作
	  * @Title: deleteAllRecent
	  * @return void
	  * @throws
	  */
	public void deleteAllRecent(){
		db.delete(RECENT_TABLE_NAME, null, null);
	}
	
	//==============================================================
	
	//好友请求数据库
	private static final String INVITE_TABLE_NAME = "tab_new_contacts";
	private static final String COLUMN_FROM_ID = "fromid";
	private static final String COLUMN_FROM_NAME = "fromname";
	private static final String COLUMN_FROM_AVATAR = "avatar";
	private static final String COLUMN_FROM_NICK = "fromnick";
	private static final String COLUMN_FROM_TIME = "fromtime";
	private static final String COLUMN_STATUS = "status";

	//新增字段--用于指定该请求的接收方
//	private static final String COLUMN_TO_ID = "toid";
	
	private static final String INIVTE_MESSAGE_TABLE_CREATE = "CREATE TABLE "
			+ INVITE_TABLE_NAME + " ("
			+ COLUMN_FROM_ID + " TEXT, "
			+ COLUMN_FROM_NAME + " TEXT, "
			+ COLUMN_FROM_NICK + " TEXT, "
			+ COLUMN_FROM_AVATAR + " TEXT, "
			+ COLUMN_FROM_TIME + " TEXT, "
			+ COLUMN_STATUS + " INTEGER); ";
	
	/**
	 * 创建或检测好友请求表是否已经创建成功
	 * 
	 * @return String
	 * @throws
	 */
	private void createOrCheckInviteTable(SQLiteDatabase db) {
		db.execSQL(INIVTE_MESSAGE_TABLE_CREATE);
	}
	
	
	/** 保存好友请求
	  * @Title: saveInviteMessage
	  * @Description: TODO
	  * @param  message
	  * @return Integer
	  * @throws
	  */
	public synchronized Integer saveInviteMessage(BmobInvitation message){
		int id = -1;
		if(db.isOpen()){
			ContentValues values = new ContentValues();
			values.put(COLUMN_FROM_ID, message.getFromid());
			values.put(COLUMN_FROM_NAME, message.getFromname());
			values.put(COLUMN_FROM_AVATAR, message.getAvatar());
			values.put(COLUMN_FROM_NICK, message.getNick());
			values.put(COLUMN_FROM_TIME, message.getTime());
			values.put(COLUMN_STATUS, message.getStatus());
			db.insert(INVITE_TABLE_NAME, null, values);
			
			Cursor cursor = db.rawQuery("select last_insert_rowid() from " + INVITE_TABLE_NAME,null); 
            if(cursor.moveToFirst()){
                id = cursor.getInt(0);
            }
            if (cursor != null && !cursor.isClosed()) {
            	cursor.close();
            	cursor = null;	
    		}
		}
		return id;
	}
	
	/** 检查数据库中是否存在相同的好友请求 
	 * @Title: isRecentExist
	 * @Description: TODO
	 * @param  targetUid
	 * @return boolean
	 * @throws
	 */
	public boolean checkInviteExist(String fromId,String msgTime) {
		String[] args = new String[] { fromId,msgTime};
		Cursor c = db.query(INVITE_TABLE_NAME, null, COLUMN_FROM_ID + " = ?  AND " + COLUMN_FROM_TIME + " = ?",
				args, null, null, null);
		boolean isTrue =false;
		try {
			isTrue = c.moveToFirst();
		}finally{
			if (c != null) {
				c.close();
				c = null;
			}
		}
		return isTrue;
	}
	
	
	/** 删除指定好友和指定时间的好友请求
	  * @Title: deleteInviteMsg
	  * @Description: TODO
	  * @param  
	  * @return 
	  * @throws
	  */
	public void deleteInviteMsg(String uid,String time){
		if(db.isOpen()){
			db.delete(INVITE_TABLE_NAME, COLUMN_FROM_ID + " = ? AND " + COLUMN_FROM_TIME + " = ? ", new String[]{uid,time});
		}
	}
	
	/** 更新来自指定用户的好友请求的状态
	  * @Title: updateAgreeMessage
	  * @Description: TODO
	  * @param  fromname 
	  * @return 
	  * @throws
	  */
	public void updateAgreeMessage(String fromname){
		if(db.isOpen()){
			ContentValues values = new ContentValues();
			values.put(COLUMN_STATUS, BmobConfig.INVITE_ADD_AGREE);
			db.update(INVITE_TABLE_NAME, values, COLUMN_FROM_NAME + " = ?", new String[]{fromname});
		}
	}
	
	/** 是否有新添加好友的请求
	  * @Title: hasNewInvite
	  * @Description: TODO
	  * @return boolean
	  * @throws
	  */
	public boolean hasNewInvite(){
		if(db.isOpen()){
			String sql = "SELECT * from " + INVITE_TABLE_NAME + " WHERE "+ COLUMN_STATUS + " = "+ BmobConfig.INVITE_ADD_NO_VALI_RECEIVED;
			Cursor c = db.rawQuery(sql, null);
			int count = 0;
			try {
				count = c.getCount();
			}finally{
				if (c != null) {
					c.close();
					c = null;
				}
			}
			if(count>0){
				return true;
			}else{
				return false;
			}
		}
		return false;
	}
	
	/**获取所有的好友请求数据-默认按照时间的先后顺序排列
	  * @Title: queryBmobInviteList
	  * @Description: TODO
	  * @return List<BmobInvitation>
	  * @throws
	  */
	public List<BmobInvitation> queryBmobInviteList(){
		List<BmobInvitation> msgs = new ArrayList<BmobInvitation>();
		if(db.isOpen()){
			Cursor cursor = db.rawQuery("select * from " + INVITE_TABLE_NAME + " order by "+COLUMN_FROM_TIME+" desc",null);
			while(cursor.moveToNext()){
				String fromid = cursor.getString(cursor.getColumnIndex(COLUMN_FROM_ID));
				String fromname = cursor.getString(cursor.getColumnIndex(COLUMN_FROM_NAME));
				String nick= cursor.getString(cursor.getColumnIndex(COLUMN_FROM_NICK));
				String fromavatar = cursor.getString(cursor.getColumnIndex(COLUMN_FROM_AVATAR));
				long time = cursor.getLong(cursor.getColumnIndex(COLUMN_FROM_TIME));
				int status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
				
				BmobInvitation msg = new BmobInvitation(fromid,fromname,fromavatar,nick,time,status);
				msgs.add(msg);
			}
			if (cursor != null && !cursor.isClosed()) {
            	cursor.close();
            	cursor = null;	
    		}
		}
		return msgs;
	}
	
	//======================================================
    //	好友表
	private static final String TABLE_NAME = "friends";
	private static final String COLUMN_NAME_FID = "uid";
	private static final String COLUMN_NAME_NAME = "username";
	private static final String COLUMN_NAME_AVATAR = "avatar";
	private static final String COLUMN_NAME_NICK = "nick";
	private static final String COLUMN_NAME_ISBLACK = "isblack";//是否是你的黑名单用户
	
	private static final String CONTACTS_TABLE_CREATE = "CREATE TABLE "
	+ TABLE_NAME + " ("
	+ COLUMN_NAME_NAME +" TEXT, "
	+ COLUMN_NAME_NICK +" TEXT, "
	+ COLUMN_NAME_AVATAR +" TEXT, "
	+ COLUMN_NAME_ISBLACK +" TEXT, "
	+ COLUMN_NAME_FID + " TEXT);";
	
	
	/**
	 * 创建或检测好友请求表是否已经创建成功
	 * 
	 * @return String
	 * @throws
	 */
	private void createOrCheckFriendsTable(SQLiteDatabase db) {
		db.execSQL(CONTACTS_TABLE_CREATE);
	}
	
	/**保存或更新用户好友列表到本地数据
	  * @Title: saveOrCheckContactList
	  * @Description: TODO
	  * @param  contactList 
	  * @return 
	  * @throws
	  */
	public void saveOrCheckContactList(List<BmobChatUser> contactList) {
		if (db.isOpen()) {
			for (BmobChatUser user : contactList) {
				ContentValues values = new ContentValues();
				values.put(COLUMN_NAME_NICK, user.getNick());
				values.put(COLUMN_NAME_NAME, user.getUsername());
				values.put(COLUMN_NAME_AVATAR, user.getAvatar());
				values.put(COLUMN_NAME_ISBLACK, BmobConfig.BLACK_NO);//不是黑名单用户
				if(checkIsExists(user.getObjectId())){//存在就更新其资料
					String[] args = new String[] {user.getObjectId()};
					db.update(TABLE_NAME, values, COLUMN_NAME_FID + " = ? ", args);
				}else{//插入
					values.put(COLUMN_NAME_FID, user.getObjectId());
					db.insert(TABLE_NAME, null, values);
				}
			}
		}
	}

	/** 批量添加黑名单用户：根据黑名单列表来批量更新对应好友列表的isblack字段
	  * @Title: batchUpdate
	  * @Description: TODO
	  * @param  
	  * @return 
	  * @throws
	  */
	public void batchAddBlack(List<BmobChatUser> blackList){
		int size = blackList.size();
		for(int i=0;i<size;i++){
			BmobChatUser user  = blackList.get(i);
			if(checkIsExists(user.getObjectId())){//存在就更新其为黑名单用户
				addBlack(user.getUsername());
			}
		}
	}
	
	/** 指定用户是否为黑名单用户
	  * @Title: isBlackUser
	  * @Description: TODO
	  * @param @param fromId
	  * @return boolean
	  * @throws
	  */
	public boolean isBlackUser(String fromId){
		String[] columns = new String[]{ COLUMN_NAME_ISBLACK };
		Cursor c = db.query(TABLE_NAME, columns, COLUMN_NAME_FID + " = ? ",
				new String[] { fromId }, null, null, null);
		if(c!=null && c.moveToFirst()){
			String isBlack  = "";
			try {
				isBlack =c.getString(c.getColumnIndex(COLUMN_NAME_ISBLACK));
			}finally{
				if (c != null) {
					c.close();
					c = null;
				}
			}
			if(isBlack.equals(BmobConfig.BLACK_NO)){
				return false;
			}else{
				return true;
			}
		}else{
			return false;
		}
	}
	
	/** 添加指定用户进黑名单
	  * @Title: updateTargetBlackStatus
	  * @Description: TODO
	  * @param  username 
	  * @return 
	  * @throws
	  */
	public void addBlack(String username){
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_ISBLACK, BmobConfig.BLACK_YES);//将指定用户标示为黑名单用户
		String[] args = new String[] {username};
		db.update(TABLE_NAME, values, COLUMN_NAME_NAME + " = ? ", args);
	}
	
	
	/** 将指定用户从黑名单中移除：更改好友列表中对应用户的黑名单状态
	  * @Title: removeBlack
	  * @Description: TODO
	  * @param  username 
	  * @return 
	  * @throws
	  */
	public void removeBlack(String username){
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_ISBLACK, BmobConfig.BLACK_NO);//将指定用户的黑名单状态标示为不是黑名单用户
		String[] args = new String[] {username};
		db.update(TABLE_NAME, values, COLUMN_NAME_NAME + " = ? ", args);
	}
	
	/** 比对好友列表，剔除掉黑名单用户
	  * @Description: TODO
	  * @param  contactList
	  * @param  blacklist
	  * @return List<BmobChatUser>
	  * @throws
	  */
	public List<BmobChatUser> getContactsWithoutBlack(List<BmobChatUser> contactList,List<BmobChatUser> blacklist){
		List<BmobChatUser> newUsers = new ArrayList<BmobChatUser>();
		Map<String,BmobChatUser> blackMap = BmobUtils.list2map(blacklist);//好友map
		int size = contactList.size();
		for(int i=0;i<size;i++){
			BmobChatUser friend = contactList.get(i);
			if(!blackMap.containsKey(friend.getObjectId())){
				newUsers.add(friend);
			}
		}
		return newUsers;
	}
	
	/** 检测目标用户是否已在好友表中
	  * @Title: checkIsExists
	  * @Description: TODO
	  * @param  uid
	  * @return boolean
	  * @throws
	  */
	private boolean checkIsExists(String uid){
		Cursor c = db.query(TABLE_NAME, null, COLUMN_NAME_FID + "=?",
				new String[] { uid }, null, null, null);
		boolean isTrue  = false;
		try {
			isTrue =c.moveToFirst();
		}finally{
			if (c != null) {
				c.close();
				c = null;
			}
		}
		return isTrue;
	}
	
	/** 保存好友
	  * @Title: saveContact
	  * @Description: TODO
	  * @param  msg 好友请求消息
	  * @return 
	  * @throws
	  */
	public void saveContact(BmobInvitation msg){
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_FID, msg.getFromid());
		values.put(COLUMN_NAME_NAME, msg.getFromname());
		values.put(COLUMN_NAME_AVATAR, msg.getAvatar());
		values.put(COLUMN_NAME_NICK, msg.getNick());
		values.put(COLUMN_NAME_ISBLACK, BmobConfig.BLACK_NO);
		if(db.isOpen()){
			db.insert(TABLE_NAME, null, values);
		}
	}
	
	/**保存好友
	  * @Title: saveContact
	  * @Description: TODO
	  * @param  user BmobUser
	  * @return 
	  * @throws
	  */
	public void saveContact(BmobChatUser user){
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_FID, user.getObjectId());
		values.put(COLUMN_NAME_NAME, user.getUsername());
		values.put(COLUMN_NAME_AVATAR, user.getAvatar());
		values.put(COLUMN_NAME_NICK, user.getNick());
		values.put(COLUMN_NAME_ISBLACK, BmobConfig.BLACK_NO);
		if(db.isOpen()){
			db.insert(TABLE_NAME, null, values);
		}
	}

	/** 获取所有的好友：包括黑名单用户
	  * @Title: getAllFriends
	  * @Description: TODO
	  * @return List<BmobChatUser>
	  * @throws
	  */
	public List<BmobChatUser> getAllContactList(){
		List<BmobChatUser> users = new ArrayList<BmobChatUser>();
		if (db.isOpen()) {//查询黑名单状态为no的用户
			Cursor cursor = db.rawQuery("select * from " + TABLE_NAME, null);
			while (cursor.moveToNext()) {
				String uid = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_FID));
				String username = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NAME));
				String avatar = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_AVATAR));
				String nick = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NICK));
				BmobChatUser user = new BmobChatUser();
				user.setUsername(username);
				user.setNick(nick);
				user.setObjectId(uid);
				user.setAvatar(avatar);
				users.add(user);
			}
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
				cursor = null;
			}
		}
		return users;
	}
	
	/**获取本地数据库中的好友列表：不包含黑名单用户
	  * @Title: getContactList
	  * @Description: TODO
	  * @return List<BmobChatUser>
	  * @throws
	  */
	public List<BmobChatUser> getContactList() {
		List<BmobChatUser> users = new ArrayList<BmobChatUser>();
		if (db.isOpen()) {//查询黑名单状态为no的用户
			Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " WHERE "+ COLUMN_NAME_ISBLACK + " = ?" , new String[]{BmobConfig.BLACK_NO});
			while (cursor.moveToNext()) {
				String uid = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_FID));
				String username = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NAME));
				String avatar = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_AVATAR));
				String nick = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NICK));
				BmobChatUser user = new BmobChatUser();
				user.setUsername(username);
				user.setNick(nick);
				user.setObjectId(uid);
				user.setAvatar(avatar);
				users.add(user);
			}
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
				cursor = null;
			}
		}
		return users;
	}
	
	/** 获取本地数据库中的黑名单列表
	  * @Title: getBlackList
	  * @Description: TODO
	  * @return List<BmobChatUser>
	  * @throws
	  */
	public List<BmobChatUser> getBlackList(){
		List<BmobChatUser> users = new ArrayList<BmobChatUser>();
		if (db.isOpen()) {//查询黑名单状态为no的用户
			Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " WHERE "+ COLUMN_NAME_ISBLACK + " = ?", new String[]{BmobConfig.BLACK_YES});
			while (cursor.moveToNext()) {
				String uid = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_FID));
				String username = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NAME));
				String avatar = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_AVATAR));
				String nick = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NICK));
				BmobChatUser user = new BmobChatUser();
				user.setUsername(username);
				user.setNick(nick);
				user.setObjectId(uid);
				user.setAvatar(avatar);
				users.add(user);
			}
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
				cursor = null;
			}
		}
		return users;
	}
	
	/** 清空好友列表
	 * @method deleteAllContact 
	 * @param     
	 * @return void  
	 * @exception   
	 */
	public void deleteAllContact(){
		if(db.isOpen()){
			db.delete(TABLE_NAME, null, null);
		}
	}
	
	/**删除指定uid的好友
	  * @Title: deleteContact
	  * @Description: TODO
	  * @param  uid 
	  * @return 
	  * @throws
	  */
	public void deleteContact(String uid){
		if(db.isOpen()){
			db.delete(TABLE_NAME, COLUMN_NAME_FID + " = ?", new String[]{uid});
		}
	}
	
	
	// 初始化时创建数据库
	class SqliteDbHelper extends SQLiteOpenHelper {

		private DbUpdateListener mDbUpdateListener;
		Context context;
		public SqliteDbHelper(Context context, String name, int version,
				DbUpdateListener dbUpdateListener) {
			super(context, name, null, version);
			this.mDbUpdateListener = dbUpdateListener;
			this.context =context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// 创建聊天表
			createOrCheckChatTable(db);
			// 创建最近会话表
			createOrCheckRecentTable(db);
			//创建好友请求表
			createOrCheckInviteTable(db);
			//创建好友数据库
			createOrCheckFriendsTable(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (mDbUpdateListener != null) {
				mDbUpdateListener.onUpgrade(db, oldVersion, newVersion);
			} else { // 清空所有的数据信息
				BmobDB.create(context).clearAllDbCache();
				// 重新创建
//				db.execSQL(ADD_INVITE_COLUMN);
			}
		}
	}
	//增加好友请求表一个列
//	public static final String ADD_INVITE_COLUMN = "alter table "
//			+ INVITE_TABLE_NAME + " ADD COLUMN " + COLUMN_TO_ID +" TEXT";
	

	/**数据库升级时,删除所有数据表
	  * 用于清空缓存操作--谨慎使用
	  * @Title: clearAllDbCache
	  * @Description: TODO
	  * @return 
	  * @throws
	  */
	public void clearAllDbCache() {
		Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type ='table' AND name != 'sqlite_sequence'",	null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				db.execSQL("DROP TABLE " + cursor.getString(0));
			}
		}
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
	}

}
