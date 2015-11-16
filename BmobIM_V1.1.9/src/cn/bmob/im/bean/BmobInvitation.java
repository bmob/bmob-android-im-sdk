package cn.bmob.im.bean;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.config.BmobConstant;
import cn.bmob.im.util.BmobJsonUtil;
import cn.bmob.im.util.BmobLog;


/** 好友邀请
  * @ClassName: BmobInvitation
  * @Description: TODO
  * @author smile
  * @date 2014-6-9 下午3:23:02
  */
public class BmobInvitation implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String fromid;
	private String fromname;//来源
	private String avatar;//头像
	private String nick;
	private long time;//时间
	private int status;
	
	public BmobInvitation(String fromid,String fromname,String avatar,String nick,long time,int status){
		this.fromid =fromid;
		this.fromname =fromname;
		this.nick =nick;
		this.avatar =avatar;
		this.time =time;
		this.status =status;
	}

	/** 解析json-创建一个BmobMsg对象
	  * @Title: createReceiveMsg
	  * @Description: TODO
	  * @param @param json
	  * @param @return 
	  * @return BmobMsg
	  * @throws
	  */
	public static BmobInvitation createReceiverInvitation(String json){
		JSONObject jo;
		BmobInvitation message=null;
		try {
			jo = new JSONObject(json);
			//消息发送方的信息
			String targetId = BmobJsonUtil.getString(jo, BmobConstant.PUSH_KEY_TARGETID);
			String username = BmobJsonUtil.getString(jo, BmobConstant.PUSH_KEY_TARGETUSERNAME);
			String avatar = BmobJsonUtil.getString(jo,	BmobConstant.PUSH_KEY_TARGETAVATAR);
			String nick = BmobJsonUtil.getString(jo,	BmobConstant.PUSH_KEY_TARGETNICK);
			long time = BmobJsonUtil.getLong(jo,BmobConstant.PUSH_ADD_FROM_TIME);
			message = new BmobInvitation(targetId,username==null?"":username, avatar==null?"":avatar, nick==null?"":nick,time, BmobConfig.INVITE_ADD_NO_VALI_RECEIVED);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			BmobLog.i("parseMessage错误："+e.getMessage());
		}
		return message;
	}
	
	/** 从BmobMsg表中解析出Tag消息,本地标示为已收到，但未处理
	  * @Title: createReceiverInvitation
	  * @Description: TODO
	  * @param @param msg
	  * @param @return 
	  * @return BmobInvitation
	  * @throws
	  */
	public static BmobInvitation createReceiverInvitation(BmobMsg msg){
		BmobInvitation message = null;
		if(msg.getTag().equals(BmobConfig.TAG_ADD_CONTACT)){//添加好友请求
			String username = msg.getBelongUsername();
			String avatar = msg.getBelongAvatar();
			String nick = msg.getBelongNick();
			message = new BmobInvitation(msg.getBelongId(),username==null?"":username, avatar==null?"":avatar, nick==null?"":nick,Long.parseLong(msg.getMsgTime()), BmobConfig.INVITE_ADD_NO_VALI_RECEIVED);
		}
		return message;
	}
	
	public String getNick() {
		return nick;
	}


	public void setNick(String nick) {
		this.nick = nick;
	}


	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getFromid() {
		return fromid;
	}

	public void setFromid(String fromid) {
		this.fromid = fromid;
	}

	public String getFromname() {
		return fromname;
	}

	public void setFromname(String fromname) {
		this.fromname = fromname;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
}



