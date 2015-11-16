package cn.bmob.im.poll;

import java.util.Arrays;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

/**
 * 定时检测操作，查询BmobMsg表中是否有未读消息
 * @ClassName: BmobPollService
 * @Description: TODO
 * @author smile
 * @date 2014-9-13 下午4:22:26
 */
public class BmobPollService extends Service {
	
	public static final String ACTION = "cn.bmob.im.service.BmobPollService";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {

	}

	@Override
	public void onStart(Intent intent, int startId) {
		new PollingThread().start();
	}

	class PollingThread extends Thread {
		@Override
		public void run() {
//			BmobLog.i("***定时检测开始****");
			queryUnReadMsgs();
		}
	}

	
	/** 查询当前登录用户的所有未读消息（未读/未读已收到）
	  * @Title: queryNoReadedMsg
	  * @Description: TODO
	  * @param  
	  * @return void
	  * @throws
	  */
	private void queryUnReadMsgs(){
		//查询BmobMsg表中接收方是当前用户且未读/未读已收到的聊天消息
		String toId = BmobUserManager.getInstance(this).getCurrentUserObjectId();
		BmobQuery<BmobMsg> query = new BmobQuery<BmobMsg>();
		query.addWhereEqualTo("toId", toId);
		Integer[] names = {BmobConfig.STATE_UNREAD, BmobConfig.STATE_UNREAD_RECEIVED};
		query.addWhereContainedIn("isReaded", Arrays.asList(names));
		query.order("createdAt");//按升序排列
		query.findObjects(this, new FindListener<BmobMsg>() {
			
			@Override
			public void onSuccess(List<BmobMsg> arg0) {
				// TODO Auto-generated method stub
				if(arg0!=null && arg0.size()>0){//当有未读消息的时候就更新本地数据
					int total = arg0.size();
//					BmobLog.i("查询到的未读消息个数 =  "+total);
					for(int i =0;i<total;i++){
						BmobMsg msg = arg0.get(i);
						checkBlack(msg);
					}
				}else{
//					BmobLog.i("没有未读的消息");
				}
			}
			
			@Override
			public void onError(int arg0, String arg1) {
				// TODO Auto-generated method stub
				BmobLog.i("查询未读消息失败：arg0="+arg0+",msg = "+arg1);
			}
		});
	}
	
	/** 检查收到的消息中是否有黑名单用户发送的：这里的消息包含聊天消息、Tag消息（发送好友验证消息、同意添加好友的验证消息），不排除陌生人的消息
	  * @Title: checkBlack
	  * @Description: TODO
	  * @return void
	  * @throws
	  */
	private void checkBlack(BmobMsg msg){
		if(!BmobDB.create(getApplicationContext(),msg.getToId()).isBlackUser(msg.getBelongId())){//如果不为黑名单
			String tag = msg.getTag();
			if(TextUtils.isEmpty(tag)){//聊天消息
				boolean isExist = BmobDB.create(getApplicationContext()).checkTargetMsgExist(msg.getConversationId(), msg.getMsgTime());
				if(!isExist){
					BmobChatManager.getInstance(getApplicationContext()).saveReceiveMessage(true, msg);
					//发送广播
					sendNewBroadCast(msg);
				}else{
					BmobLog.i("已经存储过该聊天消息...");
				}
			}else if(tag.equals(BmobConfig.TAG_ADD_CONTACT)){//添加好友的请求，之前是否已经存储过
				if(!BmobDB.create(getApplicationContext()).checkInviteExist(msg.getBelongId(), msg.getMsgTime())){//如果不存在
					//保存好友请求，并更新BmobMsg表中的未读标示
					BmobInvitation message = BmobChatManager.getInstance(getApplicationContext()).saveReceiveInvite(msg);
					//发送广播
					sendAddUserBroadCast(message);
				}else{
					BmobLog.i("已经存储过该好友请求...");
				}
			}else if(tag.equals(BmobConfig.TAG_ADD_AGREE)){//同意添加好友的请求
				//这里有个缺陷就是如果对方没有收到同意的消息，那么好友已经添加了，但是好友界面却显示不出来.故在客户端界面在做一次本地好友数据库的检测
				BmobUserManager.getInstance(this).addContactAfterAgree(msg.getBelongUsername());
				//创建一个临时验证会话--用于在会话界面形成初始会话
				BmobMsg.createAndSaveRecentAfterAgree(this, msg);
			}else{//其他由开发者自定义格式的消息，也同样置为已读，不然会出现频繁的检测
				BmobChatManager.getInstance(getApplicationContext()).updateMsgReaded(true, msg.getBelongId(), msg.getMsgTime());
			}
		}else{//在黑名单期间所有的消息都应该置为已读，不然等取消黑名单之后又可以看得见
			BmobChatManager.getInstance(getApplicationContext()).updateMsgReaded(true, msg.getBelongId(), msg.getMsgTime());
			BmobLog.i("发送方("+msg.getBelongId()+")为黑名单用户");
		}
	}
	
	/**只要是未读的消息均发送广播
	  * @Title: sendBroadCast
	  * @Description: TODO
	  * @param @param msg 
	  * @return void
	  * @throws
	  */
	private void sendNewBroadCast(BmobMsg msg){
		Intent intent = new Intent(BmobConfig.BROADCAST_NEW_MESSAGE);
		intent.putExtra("fromId", msg.getBelongId());
		intent.putExtra("msgId", msg.getConversationId());
		intent.putExtra("msgTime", msg.getMsgTime());
//		sendBroadcast(intent);
		//需要发送的是有序广播，否则会提示“BroadcastReceiver trying to return result during a non-ordered broadcast”
		sendOrderedBroadcast(intent, null);
	}
	
	/**发送好友请求的广播
	 * @Title: sendBroadCast
	 * @Description: TODO
	 * @param @param msg 
	 * @return void
	 * @throws
	 */
	private void sendAddUserBroadCast(BmobInvitation msg){
		Intent intent = new Intent(BmobConfig.BROADCAST_ADD_USER_MESSAGE);
		intent.putExtra("invite", msg);
//		sendBroadcast(intent);
		sendOrderedBroadcast(intent, null);
	}

}
