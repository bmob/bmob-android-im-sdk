package com.bmob.im.demo;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobNotifyManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.config.BmobConstant;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.inteface.EventListener;
import cn.bmob.im.util.BmobJsonUtil;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.listener.FindListener;

import com.bmob.im.demo.ui.MainActivity;
import com.bmob.im.demo.ui.NewFriendActivity;
import com.bmob.im.demo.util.CollectionUtils;
import com.bmob.im.demo.util.CommonUtils;

/**
 * 推送消息接收器
 * 
 * @ClassName: MyMessageReceiver
 * @Description: TODO
 * @author smile
 * @date 2014-5-30 下午4:01:13
 */
public class MyMessageReceiver extends BroadcastReceiver {

	// 事件监听
	public static ArrayList<EventListener> ehList = new ArrayList<EventListener>();
	
	public static final int NOTIFY_ID = 0x000;
	public static int mNewNum = 0;//
	BmobUserManager userManager;
	BmobChatUser currentUser;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String json = intent.getStringExtra("msg");
		BmobLog.i("收到的message = " + json);
		userManager = BmobUserManager.getInstance(context);
		currentUser = userManager.getCurrentUser();
		boolean isNetConnected = CommonUtils.isNetworkAvailable(context);
		if(isNetConnected){
			parseMessage(context, json);
		}else{
			for (int i = 0; i < ehList.size(); i++)
				((EventListener) ehList.get(i)).onNetChange(isNetConnected);
		}
	}

	/** 解析Json字符串
	  * @Title: parseMessage
	  * @Description: TODO
	  * @param @param context
	  * @param @param json 
	  * @return void
	  * @throws
	  */
	private void parseMessage(final Context context, String json) {
		JSONObject jo;
		try {
			jo = new JSONObject(json);
			String tag = BmobJsonUtil.getString(jo, BmobConstant.PUSH_KEY_TAG);
			if(tag.equals(BmobConfig.TAG_OFFLINE)){//下线通知
				if(currentUser!=null){
					if (ehList.size() > 0) {// 有监听的时候，传递下去
						for (EventListener handler : ehList)
							handler.onOffline();
					}else{
						//清空数据
						CustomApplcation.getInstance().logout();
					}
				}
			}else{
				String fromId = BmobJsonUtil.getString(jo, BmobConstant.PUSH_KEY_TARGETID);
			   //增加消息接收方的ObjectId--目的是解决多账户登陆同一设备时，无法接收到非当前登陆用户的消息。
				String toId = BmobJsonUtil.getString(jo, BmobConstant.PUSH_KEY_TOID);
				if(fromId!=null && !BmobDB.create(context,toId).isBlackUser(fromId)){//该消息发送方不为黑名单用户
					if(TextUtils.isEmpty(tag)){//不携带tag标签
						//组装BmobMessage对象
						BmobMsg msg =BmobMsg.createReceiveMsg(context,json);
						if(currentUser!=null){//当前设备没有账号在登陆
							if( toId.equals(currentUser.getObjectId())){//只有当toId和当前登陆用户相符合才继续向下传递和弹通知，防止别人恶意刷消息
								// 普通消息，
								if (ehList.size() > 0) {// 有监听的时候，传递下去
									for (int i = 0; i < ehList.size(); i++) {
										((EventListener) ehList.get(i)).onMessage(msg);
									}
								} else {
									//存储接收到的消息,并发送消息回执给对方
									BmobChatManager.getInstance(context).saveReceiveMessage(true,msg);
									boolean isAllow = CustomApplcation.getInstance().getSpUtil().isAllowPushNotify();
									if(isAllow && currentUser!=null && currentUser.getObjectId().equals(toId)){//当前登陆用户存在并且也等于接收方id
										mNewNum++;
										showNotification(context,msg);
									}
								}
							}else{//当前登陆账号不是toId的情况下，也应该存储收到的消息。
								BmobChatManager.getInstance(context).saveReceiveMessage(true,msg);
							}
						}else{//当之前的用户B注销登陆了，但又没有登陆其他设备，那么A再向B发的消息就应该存储到对应B账户的数据库中
							BmobChatManager.getInstance(context).saveReceiveMessage(true,msg);
						}
					}else{//带tag标签
						if(tag.equals(BmobConfig.TAG_ADD_CONTACT)){
							BmobInvitation message =BmobInvitation.createReceiverInvitation(json);
							//保存好友请求消息
							BmobDB.create(context,toId).saveInviteMessage(message);
							if(currentUser!=null){//有登陆用户
								if(toId.equals(currentUser.getObjectId())){
									if (ehList.size() > 0) {// 有监听的时候，传递下去
										for (EventListener handler : ehList)
											handler.onAddUser(message);
									}else{
										boolean isAllow = CustomApplcation.getInstance().getSpUtil().isAllowPushNotify();
										boolean isAllowVoice = CustomApplcation.getInstance().getSpUtil().isAllowVoice();
										boolean isAllowVibrate = CustomApplcation.getInstance().getSpUtil().isAllowVibrate();
										if(isAllow && currentUser!=null && currentUser.getObjectId().equals(toId)){
											//同时提醒通知
											String tickerText = message.getFromname()+"请求添加好友";
											BmobNotifyManager.getInstance(context).showNotify(isAllowVoice,isAllowVibrate,R.drawable.ic_launcher, tickerText, message.getFromname(), tickerText.toString(),NewFriendActivity.class);
										}
									}
								}
							}
						}else if(tag.equals(BmobConfig.TAG_ADD_AGREE)){
							String username = BmobJsonUtil.getString(jo, BmobConstant.PUSH_KEY_TARGETUSERNAME);
							//收到对方的同意请求之后，就得添加对方为好友--已默认添加同意方为好友，并保存到本地好友数据库
							BmobUserManager.getInstance(context).addContactAfterAgree(username, new FindListener<BmobChatUser>() {
								
								@Override
								public void onError(int arg0, final String arg1) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void onSuccess(List<BmobChatUser> arg0) {
									// TODO Auto-generated method stub
									//保存到内存中
									CustomApplcation.getInstance().setContactList(CollectionUtils.list2map(BmobDB.create(context).getContactList()));
								}
							});
							
							boolean isAllow = CustomApplcation.getInstance().getSpUtil().isAllowPushNotify();
							boolean isAllowVoice = CustomApplcation.getInstance().getSpUtil().isAllowVoice();
							boolean isAllowVibrate = CustomApplcation.getInstance().getSpUtil().isAllowVibrate();
							if(isAllow && currentUser!=null && currentUser.getObjectId().equals(toId)){
								String tickerText = username+"同意添加您为好友";
								BmobNotifyManager.getInstance(context).showNotify(isAllowVoice,isAllowVibrate,R.drawable.ic_launcher, tickerText, username, tickerText.toString(),MainActivity.class);
							}
							//创建一个临时验证会话--用于在会话界面形成初始会话
							BmobMsg.createAndSaveRecentAfterAgree(context, json);
							
						}else if(tag.equals(BmobConfig.TAG_READED)){//已读回执
							String conversionId = BmobJsonUtil.getString(jo,BmobConstant.PUSH_READED_CONVERSIONID);
							String msgTime = BmobJsonUtil.getString(jo,BmobConstant.PUSH_READED_MSGTIME);
							if(currentUser!=null){
								//更改某条消息的状态
								BmobChatManager.getInstance(context).updateMsgStatus(BmobConfig.STATUS_SEND_RECEIVERED, conversionId, msgTime);
								if(toId.equals(currentUser.getObjectId())){
									if (ehList.size() > 0) {// 有监听的时候，传递下去--便于修改界面
										for (EventListener handler : ehList)
											handler.onReaded(conversionId, msgTime);
									}
								}
							}
						}
					}
				}else{
					BmobLog.i("该消息发送方为黑名单用户");
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			BmobLog.i("parseMessage错误："+e.getMessage());
		}
	}
	
	/** 
	 *  显示通知
	  * @Title: showNotify
	  * @return void
	  * @throws
	  */
	public void showNotification(Context context,BmobMsg msg) {
		// 更新通知栏
		int icon = R.drawable.ic_launcher;
		String trueMsg = "";
		if(msg.getMsgType()==BmobConfig.TYPE_TEXT && msg.getContent().contains("\\ue")){
			trueMsg = "[表情]";
		}else if(msg.getMsgType()==BmobConfig.TYPE_IMAGE){
			trueMsg = "[图片]";
		}else if(msg.getMsgType()==BmobConfig.TYPE_VOICE){
			trueMsg = "[语音]";
		}else if(msg.getMsgType()==BmobConfig.TYPE_LOCATION){
			trueMsg = "[位置]";
		}else{
			trueMsg = msg.getContent();
		}
		CharSequence tickerText = msg.getBelongUsername() + ":" + trueMsg;
		String contentTitle = msg.getBelongUsername()+ " (" + mNewNum + "条新消息)";
		
		Intent intent = new Intent(context, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		boolean isAllowVoice = CustomApplcation.getInstance().getSpUtil().isAllowVoice();
		boolean isAllowVibrate = CustomApplcation.getInstance().getSpUtil().isAllowVibrate();
		
		BmobNotifyManager.getInstance(context).showNotifyWithExtras(isAllowVoice,isAllowVibrate,icon, tickerText.toString(), contentTitle, tickerText.toString(),intent);
		
	}
	
}
