package cn.bmob.im;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import cn.bmob.im.bean.BmobChatInstallation;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.config.BmobConstant;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.task.BCountTask;
import cn.bmob.im.task.BFindTask;
import cn.bmob.im.task.BQuery;
import cn.bmob.im.task.BRequest;
import cn.bmob.im.task.BTable;
import cn.bmob.im.util.BmobLog;
import cn.bmob.im.util.BmobUtils;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobPushManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.PushListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 用户管理类--所有和用户有关的操作均使用此类：登陆、退出、获取好友列表、获取当前登陆用户、删除好友、添加好友等
 * 
 * @ClassName: UserManager
 * @Description: TODO
 * @author smile
 * @param <T>
 * @date 2014-5-29 下午2:39:11
 */
public class BmobUserManager{

	BmobPushManager<BmobInstallation> bmobPush;
	
	/**
	 * 用户表中所关联的好友字段
	 */
	public static final String COLUMN_NAME_CONTACTS = "contacts";
	
	/**
	 * 用户表中所关联的黑名单列表
	 */
	public static final String COLUMN_NAME_BLACKLIST = "blacklist";

	Context context;
	// 创建private static类实例
	private volatile static BmobUserManager INSTANCE;
   //同步锁
	private static Object INSTANCE_LOCK = new Object();

	/**
	 * 使用单例模式创建
	 */
	public static BmobUserManager getInstance(Context context) {
		if (INSTANCE == null)
			synchronized (INSTANCE_LOCK) {
				if (INSTANCE == null) {
					INSTANCE = new BmobUserManager();
				}
				INSTANCE.init(context);
			}
		return INSTANCE;
	}

	/**
	  *  初始化
	  * @Title: init
	  * @Description: TODO
	  * @param  c 
	  * @return void
	  * @throws
	  */
	public void init(Context c) {
		this.context = c;
		bmobPush = new BmobPushManager<BmobInstallation>(context);
	}
	
	/** 若重载BombChatUser，请使用此获取当前用户
	  * @Title: getCurrentUser
	  * @Description: TODO
	  * @param  arg0
	  * @return T
	  * @throws
	  */
	public <T> T getCurrentUser(Class<T> arg0) {
		return BmobUser.getCurrentUser(context, arg0);
	}
	
	/**获取当前登陆用户对象 getCurrentUser
	  * @Title: getCurrentUser
	  * @Description: TODO
	  * @return BmobChatUser
	  * @throws
	  */
	public BmobChatUser getCurrentUser() {
		return BmobUser.getCurrentUser(context, BmobChatUser.class);
	}
	
	/** 获取当前登录用户的用户名
	 * @Description: TODO
	 * @return String
	 * @throws
	 */
	public String getCurrentUserName(){
		return getCurrentUser()!=null ? getCurrentUser().getUsername():"";
	}
	
	/** 获取当前登录用户的ObjectId
	  * @Description: TODO
	  * @return String
	  * @throws
	  */
	public String getCurrentUserObjectId(){
		return getCurrentUser()!=null ? getCurrentUser().getObjectId():"";
	}
	
	/** 退出登陆
	  * @Title: logout
	  * @Description: TODO
	  * @return 
	  * @throws
	  */
	public void logout() {
		BmobUser.logOut(context);
	}
	
	/**登陆-默认登陆成功之后会检测当前账号是否绑定过设备
	  * @Title: login
	  * @Description: TODO
	  * @param  username Bmob username
	  * @param  password 密码
	  * @param  callback 成功与否回调
	  * @throws
	  */
	@Deprecated
	public void login(String username, String password, final SaveListener callback) {
		final BmobChatUser user = new BmobChatUser();
		user.setUsername(username);
		user.setPassword(password);
		user.login(context, new SaveListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				checkAndBindInstallation(getCurrentUserName());
				//调换一个位置就行了，不能提交当前用户
				updateInstallIdForUser(getCurrentUser(),new UpdateListener() {
					
					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						callback.onSuccess();
						
					}
					
					@Override
					public void onFailure(int code, String msg) {
						// TODO Auto-generated method stub
						callback.onFailure(code,msg);
					}
				});
			}
			
			@Override
			public void onFailure(int arg0, String arg1) {
				// TODO Auto-generated method stub
				callback.onFailure(arg0,arg1);
			}
		});
	}
	
	/** 新的登陆方法，解决登陆成功后无法获取本地存储的派生属性的值
	  * @Title: login
	  * @Description: TODO
	  * @param  user
	  * @throws
	  */
	public void login(final BmobChatUser user, final SaveListener callback) {
		if(user==null){
			callback.onFailure(BmobConfig.CODE_USER_NULL, "BmobChatUser is null。");
			return;
		}
		if(user.getUsername()==null || user.getUsername().equals("")){
			callback.onFailure(BmobConfig.CODE_USERNAME_NULL, "please input your username。");
			return;
		}
		user.login(context, new SaveListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				checkAndBindInstallation(getCurrentUserName());
				//调换一个位置就行了，不能提交当前用户
				updateInstallIdForUser(getCurrentUser(),new UpdateListener() {
					
					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						callback.onSuccess();
					}
					
					@Override
					public void onFailure(int code, String msg) {
						// TODO Auto-generated method stub
						BmobLog.i("updateInstallIdForUser-->更新User表中设备id字段onFailure："+msg);
						callback.onSuccess();
					}
				});
			}
			
			@Override
			public void onFailure(int arg0, String arg1) {
				// TODO Auto-generated method stub
				callback.onFailure(arg0,arg1);
			}
		});
	}
	
	/**提供新版登陆方法  
	 * @param context
	 * @param account
	 * @param password
	 * @param callback   
	 * @return void  
	 * @exception   
	 */
	public void loginByAccount(String account,String password,final SaveListener callback){
		BmobUser.loginByAccount(context, account, password, new LogInListener<BmobChatUser>() {

			@Override
			public void done(BmobChatUser arg0, BmobException e) {
				// TODO Auto-generated method stub
				if(e==null){
					checkAndBindInstallation(getCurrentUserObjectId());
					//调换一个位置就行了，不能提交当前用户
					updateInstallIdForUser(arg0,new UpdateListener() {
						
						@Override
						public void onSuccess() {
							// TODO Auto-generated method stub
							callback.onSuccess();
						}
						
						@Override
						public void onFailure(int code, String msg) {
							// TODO Auto-generated method stub
							BmobLog.i("updateInstallIdForUser-->更新User表中设备id字段onFailure："+msg);
							callback.onSuccess();
						}
					});
				}else{
					callback.onFailure(e.getErrorCode(), e.getLocalizedMessage());
				}
			}
		});
	}
	
	/** 更新用户表中的installId和deviceType字段
	  * @Title: updateInstallIdForUser
	  * @Description: TODO
	  * @param  
	  * @return 
	  * @throws
	  */
	public void updateInstallIdForUser(final BmobChatUser user,UpdateListener listener){
//		BmobLog.i("当前设备id："+BmobInstallation.getInstallationId(context)+",ObjectId  = "+user.getObjectId());
		BmobChatUser newUser = new BmobChatUser();
		newUser.setInstallId(BmobInstallation.getInstallationId(context));
		newUser.setDeviceType("android");
		newUser.setObjectId(user.getObjectId());
		newUser.update(context,listener);
	}
	
	/** 检测设备表中该用户是否绑定过该设备-用于每次登陆时候的检测操作：1、如果绑定过，则通知其他设备下线，2、若未绑定，则更新设备表中的uid字段，便于检测
	  * @Title: checkAndBindInstallation
	  * @Description: 
	  * @param  username  便于查看该设备对应哪个用户
	  * @return 
	  * @throws
	  */
	private void checkAndBindInstallation(final String username){
		checkUidExistByInstallation(username, new FindListener<BmobChatInstallation>() {

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				BmobLog.i("checkBindSuccess-->onFailure ：errorCode = "+arg1+",errorMsg = "+arg1);
			}

			@Override
			public void onSuccess(List<BmobChatInstallation> arg0) {
				// TODO Auto-generated method stub
				if(arg0!=null && arg0.size()>0){
//					BmobLog.i("checkBindSuccess-->onSuccess 该 "+username+" 已经绑定了："+arg0.get(0).getInstallationId());
					//通知其他设备下线
					if(arg0.size()>1){
						notifyOtherOffline(arg0);
					}
					//检测并绑定-用于更新登陆时间
					bindInstallationForRegister(username);
				}else{
					BmobLog.i("checkBindSuccess-->onSuccess 该 "+username+" 未绑定过任何设备,进行绑定中...");
					bindInstallationForRegister(username);
				}
			}
		} );
	}
	
	/** 从设备表中检测指定uid是否存在
	  * @Title: getUidForInstallation
	  * @Description: TODO
	  * @param  bindId
	  * @param  findcallback 
	  * @return 
	  * @throws
	  */
	private void checkUidExistByInstallation(String bindusername,FindListener<BmobChatInstallation> findcallback){
		BmobQuery<BmobChatInstallation> query = new BmobQuery<BmobChatInstallation>();
		query.addWhereEqualTo("deviceType", "android");
		query.addWhereEqualTo("uid", bindusername);
		query.order("-updatedAt");//按照更新时间降序排列--默认取第一个
		query.findObjects(context, findcallback);
	}
	
	/** 绑定设备--用于首次注册时候的绑定设备操作,将用户与设备关联起来
	  * @Title: bindInstallation
	  * @Description: TODO
	  * @param  bindUid 
	  * @return 
	  * @throws
	  */
	public void bindInstallationForRegister(String username){
		updateInstallation(username, new UpdateListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
//				BmobLog.i("bindInstallation-->onSuccess设备更新成功 ");
			}
			
			@Override
			public void onFailure(int arg0, String arg1) {
				// TODO Auto-generated method stub
				BmobLog.i("bindInstallation-->onFailure ：errorCode = "+arg1+",errorMsg = "+arg1);
			}
		});
	}
	
	/** 更新指定设备id对象的uid字段
	  * @Title: updateInstallation
	  * @Description: TODO
	  * @param  bindId
	  * @param  updatecallback 
	  * @return 
	  * @throws
	  */
	private void updateInstallation(final String username,final UpdateListener updatecallback){
		BmobQuery<BmobChatInstallation> query = new BmobQuery<BmobChatInstallation>();
		query.addWhereEqualTo("installationId", BmobInstallation.getInstallationId(context));
		query.findObjects(context, new FindListener<BmobChatInstallation>() {
			
			@Override
			public void onSuccess(List<BmobChatInstallation> object) {
				// TODO Auto-generated method stub
				if(object.size() > 0){
//					BmobLog.i("updateInstallation-->onSuccess："+object.get(0).getInstallationId());
					BmobChatInstallation mbi = object.get(0);
					mbi.setUid(username);
					mbi.update(context,updatecallback);
				}else{
					BmobLog.i("updateInstallation-->未查询到指定设备id：");
				}
			}
			
			@Override
			public void onError(int code, String msg) {
				// TODO Auto-generated method stub
				updatecallback.onFailure(code, msg);
			}
		});
	}
	
	/** 通知该用户在其他设备上的账号下线
	  * @Title: notifyTargetOffline
	  * @Description: TODO
	  * @param  
	  * @return 
	  * @throws
	  */
	private void notifyOtherOffline(List<BmobChatInstallation> arg0){
		int size = arg0.size();
		String currentInstallationId = BmobInstallation.getInstallationId(context);
		for(int i=0;i<size;i++){
			BmobChatInstallation install = arg0.get(i);
			String otherId = install.getInstallationId();
			if(!otherId.equals(currentInstallationId)){
				notifyTargetOffline(install);
			}
		}
	}
	
	/** 通知单个设备下线
	  * notifyTargetOffline
	  * @Title: notifyTargetOffline
	  * @Description: TODO
	  * @param  installationId 
	  * @return 
	  * @throws
	  */
	private void notifyTargetOffline(BmobChatInstallation install){
		BmobQuery<BmobInstallation> query = BmobInstallation.getQuery();
		String deviceType = install.getDeviceType();
		if(deviceType.equals("ios")){
			query.addWhereEqualTo("deviceToken", install.getDeviceToken());
		}else{
			query.addWhereEqualTo("installationId", install.getInstallationId());
		}
		bmobPush.setQuery(query);
		JSONObject jsontag = createOfflineTagMessage();
		bmobPush.pushMessage(jsontag, new PushListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
//				BmobLog.i("notifyTargetOffline-->onSuccess");
			}
			
			@Override
			public void onFailure(int arg0, String arg1) {
				// TODO Auto-generated method stub
				BmobLog.i("notifyTargetOffline-->onFailure:"+arg1);
			}
		});
		
	}
	
	/** 向指定用户发送下线的tag请求
	  * @return 
	  * @throws
	  */
	private JSONObject createOfflineTagMessage(){
		try {
			JSONObject json=new JSONObject();
			json.put(BmobConstant.PUSH_KEY_TAG, BmobConfig.TAG_OFFLINE);
			return json;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	

	/** 同意对方的添加好友请求:此方法默认在添加成功之后：1、更新本地好友数据库，2、向请求方发生同意tag请求，3、保存该好友到本地好友库
	  * @Title: agreeAddContact
	  * @Description: TODO
	  * @param  msg 好友请求
	  * @param  updateCallback  
	  * @return 
	  * @throws
	  */
	public void agreeAddContact(final BmobInvitation msg,final UpdateListener updateCallback){
		//添加好友
		addContact(msg.getFromname(), new UpdateListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				//更新本地请求好友
				BmobDB.create(context).updateAgreeMessage(msg.getFromname());
				//发送tag请求
				BmobChatManager.getInstance(context).sendTagMessage(BmobConfig.TAG_ADD_AGREE, msg.getFromid());
				//保存此好友到本地数据库中	
				BmobDB.create(context).saveContact(msg);
				updateCallback.onSuccess();
			}
			
			@Override
			public void onFailure(int arg0, final String arg1) {
				// TODO Auto-generated method stub
				updateCallback.onFailure(arg0, arg1);
			}
		});
	}
	
	/** 发送添加好友请求之后的对方回应：默认添加成功之后会存储到本地好友数据库中
	  * @Description: TODO
	  * @param  targetName 目标用户名
	  * @param  findCallback 
	  * @return 
	  * @throws
	  */
	public void addContactAfterAgree(String targetName,final FindListener<BmobChatUser> findCallback){
		queryUserByName(targetName, new FindListener<BmobChatUser>() {

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				findCallback.onError(arg0, arg1);
			}

			@Override
			public void onSuccess(List<BmobChatUser> arg0) {
				// TODO Auto-generated method stub
				//添加关联关系
				addRelation(arg0.get(0));
				//保存本地联系人
				BmobDB.create(context).saveContact(arg0.get(0));
				findCallback.onSuccess(arg0);
			}
		});
	}
	
	/**发送添加好友请求之后的对方回应：默认添加成功之后会存储到本地好友数据库中
	  * @Title: addContactAfterAgree
	  * @Description: TODO
	  * @param  targetName 
	  * @return 
	  * @throws
	  */
	public void addContactAfterAgree(String targetName){
		addContactAfterAgree(targetName, new FindListener<BmobChatUser>() {

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				BmobLog.i("添加好友失败："+arg1);
			}

			@Override
			public void onSuccess(List<BmobChatUser> arg0) {
				// TODO Auto-generated method stub
//				BmobLog.i("添加好友成功");
			}
		});
	}
	
	/** 添加指定用户名的好友--用于同意对方的好友请求
	  * @Title: addContact
	  * @Description: TODO
	  * @param  username
	  * @param  updateCallback 
	  * @return 
	  * @throws
	  */
	private void addContact(String username, final UpdateListener updateCallback) {
		BmobQuery<BmobChatUser> query = new BmobQuery<BmobChatUser>();
		query.addWhereEqualTo("username", username);
		query.findObjects(context, new FindListener<BmobChatUser>() {

			@Override
			public void onSuccess(List<BmobChatUser> arg0) {
				// TODO Auto-generated method stub
				if(arg0!=null && arg0.size()>0){
					BmobChatUser contact = arg0.get(0);
					addRelation(contact, updateCallback);
				}else{
					updateCallback.onFailure(BmobConfig.CODE_COMMON_NONE, "暂无此用户");
				}
			}

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				updateCallback.onFailure(arg0, arg1);
			}
		});
	}
	
	/** 根据名称查找指定的用户：-用于查找目标用户的个人详细资料
	  * @Title: queryTargetUserByName
	  * @Description: TODO
	  * @param  username
	  * @param  findCallback 
	  * @return 
	  * @throws
	  */
	public void queryUserByName(String username, final FindListener<BmobChatUser> findCallback) {
		BmobQuery<BmobChatUser> query = new BmobQuery<BmobChatUser>();
		query.addWhereEqualTo("username", username);
		query.findObjects(context, new FindListener<BmobChatUser>() {
			
			@Override
			public void onSuccess(List<BmobChatUser> arg0) {
				// TODO Auto-generated method stub
				findCallback.onSuccess(arg0);
			}
			
			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				findCallback.onError(arg0, arg1);
			}
		});
	}
	/** 根据id查找指定的用户：
	 * @Title: queryTargetUserByName
	 * @Description: TODO
	 * @param  username
	 * @param  findCallback 
	 * @return 
	 * @throws
	 */
	public void queryUserById(String objectId, final FindListener<BmobChatUser> findCallback) {
		BmobQuery<BmobChatUser> query = new BmobQuery<BmobChatUser>();
		query.addWhereEqualTo("objectId", objectId);
		query.findObjects(context, new FindListener<BmobChatUser>() {
			
			@Override
			public void onSuccess(List<BmobChatUser> arg0) {
				// TODO Auto-generated method stub
				findCallback.onSuccess(arg0);
			}
			
			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				findCallback.onError(arg0, arg1);
			}
		});
	}
	
	/** 查询用户信息-用于查找继承自BmobChatUser的用户类信息
	 * @Title: queryTargetUserByName
	 * @Description: TODO
	 * @param  username
	 * @param  findCallback 
	 * @return 
	 * @throws
	 */
	public <T> void queryUser(String username, final FindListener<T> findCallback) {
		BmobQuery<T> query = new BmobQuery<T>();
		query.addWhereEqualTo("username", username);
		query.findObjects(context, findCallback);
	}

	/** 添加关联关系：
	  * @Title: addRelation
	  * @Description: TODO
	  * @param  user
	  * @param  callback 
	  * @return 
	  * @throws
	  */
	private void addRelation(BmobUser user, UpdateListener callback) {
		// 获取当前登陆用户
		BmobChatUser newUser = new BmobChatUser();
		// 添加关联关系
		BmobRelation relation = new BmobRelation();
		relation.add(user);
		newUser.setContacts(relation);
		newUser.setObjectId(getCurrentUserObjectId());
		newUser.update(context,callback);
	}
	
	/** 添加关联关系-不携带回调
	  * @Title: addRelation
	  * @Description: TODO
	  * @param  user 
	  * @return 
	  * @throws
	  */
	private void addRelation(BmobUser user) {
		addRelation(user, new UpdateListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
//				BmobLog.i("addRelation success");
			}
			
			@Override
			public void onFailure(int arg0, String arg1) {
				// TODO Auto-generated method stub
				BmobLog.i("addRelation onFailure："+arg0+",errorMsg = "+arg1);
			}
		});
	}
	
	/** 删除关联关系：
	  * @Title: deleteRelation
	  * @Description: TODO
	  * @param  user
	  * @param  updateCallback 
	  * @return 
	  * @throws
	  */
	private void deleteRelation(BmobUser user, UpdateListener updateCallback) {
		// 获取当前登陆用户
		BmobChatUser current = new BmobChatUser();
		// 添加关联关系
		BmobRelation relation = new BmobRelation();
		relation.remove(user);
		current.setContacts(relation);
		current.setObjectId(getCurrentUserObjectId());
		current.update(context,updateCallback);
	}
	
	/** 删除指定联系人--取消关联：默认成功之后删除本地好友、会话表、消息表中与目标用户有关的数据
	  * @Title: deleteContact
	  * @Description: TODO
	  * @param  userObjectId 用户id
	  * @param  updateCallback 
	  * @return 
	  * @throws
	  */
	public void deleteContact(final String targetObjectId,final UpdateListener updateCallback){
		BmobQuery<BmobChatUser> query = new BmobQuery<BmobChatUser>();
		query.addWhereEqualTo("objectId", targetObjectId);
		query.findObjects(context, new FindListener<BmobChatUser>() {

			@Override
			public void onSuccess(List<BmobChatUser> arg0) {
				// TODO Auto-generated method stub
				BmobChatUser contact = arg0.get(0);
				deleteRelation(contact, new UpdateListener() {
					
					@Override
					public void onSuccess() {
						//删除好友db	
						BmobDB.create(context).deleteContact(targetObjectId);
						//删除会话和聊天消息
						BmobDB.create(context).deleteRecent(targetObjectId);
						BmobDB.create(context).deleteMessages(targetObjectId);
						updateCallback.onSuccess();
					}
					
					@Override
					public void onFailure(int arg0, String arg1) {
						// TODO Auto-generated method stub
						updateCallback.onFailure(arg0, arg1);
					}
				});
			}

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				updateCallback.onFailure(arg0, arg1);
			}
		});
	}
	
	/** 删除指定联系人--不携带回调
	  * @Title: deleteContact
	  * @Description: TODO
	  * @param  targetObjectId 
	  * @return 
	  * @throws
	  */
	public void deleteContact(final String targetObjectId){
		deleteContact(targetObjectId, new UpdateListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
//				BmobLog.i("deleteContact onSuccess");
			}
			
			@Override
			public void onFailure(int arg0, String arg1) {
				// TODO Auto-generated method stub
				BmobLog.i("deleteContact onFailure："+arg1);
			}
		});
	}
	
	/** 创建查询好友的请求条件
	  * @Title: createContactQuery
	  * @Description: TODO
	  * @return 
	  * @throws
	  */
	private List<BQuery> createContactQuery(){
		//包含指定用户的好友个数
		List<BTable> b = new ArrayList<BTable>();
		Object[] obj = new Object[1];
		obj[0] =  new BmobPointer(getCurrentUser());
		BTable t = new BTable(COLUMN_NAME_CONTACTS, obj);
		b.add(t);
		BQuery queryIn = new BQuery(BQuery.QueryType.RELATEDTO, b);
		//添加到request中
		List<BQuery> querys = new ArrayList<BQuery>();
		querys.add(queryIn);
				
		return querys;
	}
	
	/** 查询用户的好友总数
	  * @Title: queryContactsTotalCount
	  * @Description: TODO
	  * @param  
	  * @return 
	  * @throws
	  */
	public void queryContactsTotalCount(CountListener countCallback){
		BRequest request = new BRequest(createContactQuery()){};
		new BCountTask<BmobChatUser>(context, request, BmobChatUser.class, countCallback);
	}
	
	/** 获取当前用户的好友列表 ,默认按照更新时间降序排列,同时剔除掉黑名单用户
	  * @Title: queryCurrentContactList
	  * @Description: TODO
	  * @param  callback：回调中的onSuccess中的arg0:是当前用户的好友列表(已经剔除掉了黑名单用户)
	  * @return void
	  * @throws
	  */
	public void queryCurrentContactList(final FindListener<BmobChatUser> callback) {
		BmobQuery<BmobChatUser> query = new BmobQuery<BmobChatUser>();
		query.order("-updatedAt");
		query.setLimit(BmobConfig.LIMIT_CONTACTS);
		query.addWhereRelatedTo(COLUMN_NAME_CONTACTS, new BmobPointer(getCurrentUser()));
		query.findObjects(context, new FindListener<BmobChatUser>() {

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				callback.onError(arg0, arg1);
			}

			@Override
			public void onSuccess(final List<BmobChatUser> friends) {
				// TODO Auto-generated method stub
				if (friends != null && friends.size() > 0) {
					BmobDB.create(context).saveOrCheckContactList(friends);
					//查询该用户的黑名单列表
					queryBlackList(new FindListener<BmobChatUser>() {

						@Override
						public void onError(int arg0, String arg1) {
							// TODO Auto-generated method stub
							if(arg0==BmobConfig.CODE_COMMON_NONE){
//								BmobLog.i(arg1);
							}else{
								BmobLog.i("查询用户的黑名单列表失败:"+arg1);
							}
							callback.onSuccess(friends);
						}

						@Override
						public void onSuccess(List<BmobChatUser> blacklist) {
							// TODO Auto-generated method stub
							BmobDB.create(context).batchAddBlack(blacklist);
//							BmobLog.i("查询用户的黑名单列表onSuccess:"+blacklist.size());
							callback.onSuccess(BmobDB.create(context).getContactsWithoutBlack(friends, blacklist));
						}
					});
				}else{
					//当查询无好友时，需要清空本地的好友列表
					BmobDB.create(context).deleteAllContact();
					callback.onError(BmobConfig.CODE_COMMON_NONE, "暂无好友");
				}
			}
		});
	}
	
	/** 分页查询指定名称的好友列表
	  * @Title: queryUserByName
	  * @Description: TODO
	  * @param  isRefreshAction：是否是下拉或者上拉刷新动作
	  * @param  page：当前页码
	  * @param  username：查询的用户名
	  * @param  findCallback 
	  * @return 
	  * @throws
	  */
	public <T> void queryUserByPage(boolean isRefreshAction,int page,final String searchName,final FindListener<T> findCallback){
		BRequest request = new BRequest(isRefreshAction,true,createSearchQuery(searchName),page) {};
		new BFindTask<T>(context, request, findCallback);
	}
	
	/** 创建搜索的请求条件
	  * @Title: createSearchRequest
	  * @Description: TODO
	  * @param  
	  * @return 
	  * @throws
	  */
	private List<BQuery>  createSearchQuery(String searchName){
		//包含指定username
		List<BTable> b = new ArrayList<BTable>();
		Object[] obj = new Object[1];
		obj[0] =  searchName;
		BTable t = new BTable("username", obj);
		b.add(t);
		BQuery queryIn = new BQuery(BQuery.QueryType.CONTAINS, b);
		
		//不包含好友列表和自己
		List<BTable> btable = new ArrayList<BTable>();
		List<BmobChatUser>  friends = BmobDB.create(context).getAllContactList();
		//添加自己到这个list中
		friends.add(getCurrentUser());
		Object[] obj1= new Object[1];
		obj1[0] =   BmobUtils.list2Array(friends);
		BTable tname = new BTable("username",obj1);
		btable.add(tname);
		BQuery queryNot = new BQuery(BQuery.QueryType.NOTCONTAINEDIN, btable);
		//添加到request中
		List<BQuery> querys = new ArrayList<BQuery>();
		querys.add(queryIn);
		querys.add(queryNot);
				
		return querys;
	}
	
	/** 查询待搜索用户的总数
	  * @Title: queryNearTotalCount
	  * @Description: TODO
	  * @param  clas：JavaBean对象
	  * @param  property：查询条件
	  * @param  longtitude：经度
	  * @param  latitude：纬度
	  * @param  countCallback ：个数回调
	  * @return 
	  * @throws
	  */
	public void querySearchTotalCount(String searchName,CountListener countCallback){
		BRequest request = new BRequest(createSearchQuery(searchName)){};
		new BCountTask<BmobChatUser>(context, request, BmobChatUser.class, countCallback);
	}
	
	/** 查询当前用户的黑名单列表
	  * @Title: queryBlackList
	  * @Description: TODO
	  * @param  
	  * @return 
	  * @throws
	  */
	public void queryBlackList(final FindListener<BmobChatUser> callback){
		BmobQuery<BmobChatUser> query = new BmobQuery<BmobChatUser>();
		query.addWhereRelatedTo(COLUMN_NAME_BLACKLIST, new BmobPointer(getCurrentUser()));
		query.findObjects(context, new FindListener<BmobChatUser>() {

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				callback.onError(arg0, arg1);
			}

			@Override
			public void onSuccess(List<BmobChatUser> arg0) {
				// TODO Auto-generated method stub
				if (arg0 != null && arg0.size() > 0) {
					callback.onSuccess(arg0);
				}else{
					callback.onError(BmobConfig.CODE_COMMON_NONE, "暂无黑名单用户");
				}
			}
		});
	}
	
	/** 将指定用户移出黑名单列表
	  * @Title: removeBlack
	  * @Description: TODO
	  * @param  username 
	  * @return 
	  * @throws
	  */
	public void removeBlack(final String username,final UpdateListener updateCallback){
		BmobQuery<BmobChatUser> query = new BmobQuery<BmobChatUser>();
		query.addWhereEqualTo("username", username);
		query.findObjects(context, new FindListener<BmobChatUser>() {

			@Override
			public void onSuccess(List<BmobChatUser> arg0) {
				// TODO Auto-generated method stub
				BmobChatUser contact = arg0.get(0);
				deleteBlackRelation(contact, new UpdateListener() {
					
					@Override
					public void onSuccess() {
						//移除黑名单
						BmobDB.create(context).removeBlack(username);
						updateCallback.onSuccess();
//						BmobLog.i("deleteBlackRelation onSuccess");
					}
					
					@Override
					public void onFailure(int arg0, String arg1) {
						// TODO Auto-generated method stub
						BmobLog.i("deleteBlackRelation onFailure："+arg1);
						updateCallback.onFailure(arg0, arg1);
					}
				});
			}

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				BmobLog.i("deleteBlackRelation onFailure："+arg1);
				updateCallback.onFailure(arg0, arg1);
			}
		});
	}
	/** 添加到黑名单列表-默认关联Bmob的黑名单之后，就更改好友表中的该用户的黑名单状态和删除本地会话表的记录-并不删除聊天数据
	  * @Title: addBlack
	  * @Description: TODO
	  * @param  username 
	  * @return 
	  * @throws
	  */
	public void addBlack(final String username,final UpdateListener updateCallback){
		BmobQuery<BmobChatUser> query = new BmobQuery<BmobChatUser>();
		query.addWhereEqualTo("username", username);
		query.findObjects(context, new FindListener<BmobChatUser>() {

			@Override
			public void onSuccess(List<BmobChatUser> arg0) {
				// TODO Auto-generated method stub
				if(arg0!=null && arg0.size()>0){
					final BmobChatUser contact = arg0.get(0);
					addBlackRelation(contact, new UpdateListener() {
						
						@Override
						public void onSuccess() {
							// TODO Auto-generated method stub
							//添加黑名单
							BmobDB.create(context).addBlack(username);
							//删除会话页面中的此会话列表
							BmobDB.create(context).deleteRecent(contact.getObjectId());
							updateCallback.onSuccess();
						}
						
						@Override
						public void onFailure(int arg0, String arg1) {
							// TODO Auto-generated method stub
							updateCallback.onFailure(arg0, arg1);
						}
					});
				}else{
					updateCallback.onFailure(BmobConfig.CODE_COMMON_NONE, "暂无此用户");
				}
			}

			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				updateCallback.onFailure(arg0, arg1);
			}
		});
		
	}
	
	/** 添加黑名单关联关系：
	  * @Title: addBlackRelation
	  * @Description: TODO
	  * @param  user
	  * @param  callback 
	  * @return 
	  * @throws
	  */
	private void addBlackRelation(BmobChatUser user, UpdateListener callback) {
		// 获取当前登陆用户
		BmobChatUser newUser = new BmobChatUser();
		// 添加关联关系
		BmobRelation relation = new BmobRelation();
		relation.add(user);
		newUser.setBlacklist(relation);
		newUser.setObjectId(getCurrentUserObjectId());
		newUser.update(context,callback);
	}
	
	/** 删除黑名单关联关系：
	  * @Title: deleteRelation
	  * @Description: TODO
	  * @param  user
	  * @param  updateCallback 
	  * @return 
	  * @throws
	  */
	private void deleteBlackRelation(BmobUser user, UpdateListener updateCallback) {
		// 获取当前登陆用户
		BmobChatUser current = new BmobChatUser();
		// 添加关联关系
		BmobRelation relation = new BmobRelation();
		relation.remove(user);
		current.setBlacklist(relation);
		current.setObjectId(getCurrentUserObjectId());
		current.update(context, updateCallback);
	}
	
	/** 分页加载指定公里范围内用户：排除自己，是否排除好友由开发者决定，可以添加额外查询条件
	  * @Title: queryNearByListByPage
	  * @Description: TODO
	  * @param  isPull：是否是属于刷新动作：如果是上拉或者下拉动作则设在为true,其他设在为false
	  * @param  page：当前查询页码
	  * @param  property：查询条件，自己定义的位置属性
	  * @param  longtitude：经度
	  * @param  latitude：纬度
	  * @param  isShowFriends：是否显示附近的好友
	  * @param  kilometers ：公里数
	  * @param  equalProperty：自己定义的其他属性：使用方法AddWhereEqualTo对应的属性名称
	  * @param  equalObj：查询equalProperty属性对应的属性值
	  * @param  findCallback ：回调
	  * @return 
	  * @throws
	  */
	public <T>  void queryKiloMetersListByPage(boolean isRefreshAction,int page,String locationProperty,double longtitude,double latitude,boolean isShowFriends,double kilometers,String equalProperty,Object equalObj,FindListener<T> findCallback){
		BRequest request = new BRequest(isRefreshAction,true,createNearQuery(false,kilometers,locationProperty, longtitude, latitude, isShowFriends,equalProperty,equalObj),page) {};
		new BFindTask<T>(context, request, findCallback);
	}
	
	/** 查询指定范围内的用户人数,可以添加额外查询条件
	  * @Title: queryNearTotalCount
	  * @Description: TODO
	  * @param  clas：JavaBean对象
	  * @param  property：查询条件
	  * @param  longtitude：经度
	  * @param  latitude：纬度
	  * @param  isShowFriends：是否显示附近的好友
	  * @param  equalProperty：自己定义的其他属性：使用方法AddWhereEqualTo对应的属性名称
	  * @param  equalObj：查询equalProperty属性对应的属性值
	  * @param  countCallback ：个数回调
	  * @return 
	  * @throws
	  */
	public <T>  void queryKiloMetersTotalCount(Class<T> clas,String locationProperty,double longtitude,double latitude,boolean isShowFriends,double kilometers,String equalProperty,Object equalObj,CountListener countCallback){
		BRequest request = new BRequest(createNearQuery(false,kilometers,locationProperty, longtitude, latitude, isShowFriends,equalProperty,equalObj)){};
		new BCountTask<T>(context, request, clas, countCallback);
	}
	
	
	/** 分页加载全部的用户列表：排除自己，是否排除好友由开发者决定，可以添加额外查询条件
	  * @Title: queryNearByListByPage
	  * @Description: TODO
	  * @param  isPull：是否是属于刷新动作：如果是上拉或者下拉动作则设在为true,其他设在为false
	  * @param  page：当前查询页码
	  * @param  locationProperty：自己定义的位置属性
	  * @param  longtitude：经度
	  * @param  latitude：纬度
	  * @param  isShowFriends：是否显示附近的好友
	  * @param  equalProperty：自己定义的其他属性：使用方法AddWhereEqualTo对应的属性名称
	  * @param  equalObj：查询equalProperty属性对应的属性值
	  * @param  findCallback ：回调
	  * @return 
	  * @throws
	  */
	public <T>  void queryNearByListByPage(boolean isRefreshAction,int page,String locationProperty,double longtitude,double latitude,boolean isShowFriends,String equalProperty,Object equalObj,FindListener<T> findCallback){
		BRequest request = new BRequest(isRefreshAction,true,createNearQuery(true,0,locationProperty, longtitude, latitude, isShowFriends,equalProperty,equalObj),page) {};
		new BFindTask<T>(context, request, findCallback);
	}
	
	/** 创建附近的人的请求条件:是否是查询附近的人/查询指定范围内的人
	  * @Title: createNearQuery
	  * @Description: TODO
	  * @param  property
	  * @param  longtitude
	  * @param  latitude
	  * @param  isShowFriends
	  * @return List<BQuery>
	  * @throws
	  */
	private List<BQuery> createNearQuery(boolean isNear,double kilometers,String locationProperty,double longtitude,double latitude,boolean isShowFriends,String equalProperty,Object equalObj){
		//查询指定地理位置附近的人
		List<BTable> params = new ArrayList<BTable>();
		Object[] obj = new Object[2];
		obj[0] =  new BmobGeoPoint(longtitude, latitude);
		if(!isNear){
			obj[1] =  kilometers;
		}
		BTable table = new BTable(locationProperty, obj);
		params.add(table);
		
		BQuery queryNear ;
		if(isNear){
			queryNear =  new BQuery(BQuery.QueryType.NEAR, params);
		}else{
			queryNear =  new BQuery(BQuery.QueryType.WITHINKILOMETERS, params);
		}
		//是否排除掉好友--附近的人一定排除自己
		List<BTable> btable = new ArrayList<BTable>();
		List<BmobChatUser>  friends = new ArrayList<BmobChatUser>();
		if(!isShowFriends){
			//不包含好友
			friends.addAll(BmobDB.create(context).getAllContactList());
			friends.add(getCurrentUser());
		}
		//不包含自己
		friends.add(getCurrentUser());
		Object[] obj2 = new Object[1];
		obj2[0] = BmobUtils.list2Array(friends);
		BTable tname = new BTable("username", obj2);
		btable.add(tname);
		BQuery queryNot = new BQuery(BQuery.QueryType.NOTCONTAINEDIN, btable);
		
		//添加到request中
		List<BQuery> querys = new ArrayList<BQuery>();
		querys.add(queryNear);
		querys.add(queryNot);
		
		//不能为空
       if(equalProperty !=null && equalObj!=null){
    	   //查询指定属性的值
    	   BQuery queryOther;
    	   List<BTable> oTable = new ArrayList<BTable>();
    	   Object[] obj1 = new Object[1];
    	    int type = 0 ;
//    	   if(equalObj instanceof Boolean){//如果是布尔类型的
//    		   boolean istrue = (Boolean) equalObj;//取他的相反值
//    		   obj1[0] = !istrue;
//    		   type = BQuery.QueryType.NOTEQUALTO;
//    	   }else{
    		   type = BQuery.QueryType.EQUALTO;
    		   obj1[0] = equalObj;
//    	   }
    	   BTable otherTable = new BTable(equalProperty, obj1);
    	   oTable.add(otherTable);
    	   queryOther  =  new BQuery(type, oTable);
    	   querys.add(queryOther);
		}
		return querys;
	}
	
	/** 查询全部的总人数（所有显示地理信息的人），可以添加额外查询条件
	  * @Title: queryNearTotalCount
	  * @Description: TODO
	  * @param  clas：JavaBean对象
	  * @param  property：查询条件
	  * @param  longtitude：经度
	  * @param  latitude：纬度
	  * @param  isShowFriends：是否显示附近的好友
	  * @param  equalProperty：自己定义的其他属性：使用方法AddWhereEqualTo对应的属性名称
	  * @param  equalObj：查询equalProperty属性对应的属性值
	  * @param  countCallback ：个数回调
	  * @return 
	  * @throws
	  */
	public <T>  void queryNearTotalCount(Class<T> clas,String property,double longtitude,double latitude,boolean isShowFriends,String equalProperty,Object equalObj,CountListener countCallback){
		BRequest request = new BRequest(createNearQuery(true,0,property, longtitude, latitude, isShowFriends,equalProperty,equalObj)){};
		new BCountTask<T>(context, request, clas, countCallback);
	}
	
}

