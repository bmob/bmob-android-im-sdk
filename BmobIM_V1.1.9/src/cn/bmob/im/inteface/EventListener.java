package cn.bmob.im.inteface;

import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;

/** 事件监听
  * @ClassName: EventListener
  * @Description: TODO
  * @author smile
  * @date 2014-6-6 上午10:02:41
  */
public abstract interface EventListener {
	
	/** 接收到消息
	  * @Title: onMessage
	  * @Description: TODO
	  * @param  message 
	  * @return 
	  * @throws
	  */
	public abstract void onMessage(BmobMsg message);

	/** 已读回执
	  * @Title: onReaded
	  * @Description: TODO
	  * @param  conversionId
	  * @param  msgTime 
	  * @return 
	  * @throws
	  */
	public abstract void onReaded(String conversionId,String msgTime);
	
	/**网络改变
	  * @Title: onNetChange
	  * @Description: TODO
	  * @param  isNetConnected 
	  * @return 
	  * @throws
	  */
	public abstract void onNetChange(boolean isNetConnected);
	
	/** 好友请求
	  * @Title: onAddUser
	  * @Description: TODO
	  * @param  message 
	  * @return 
	  * @throws
	  */
	public abstract void onAddUser(BmobInvitation message);
	
	/** 下线通知
	  * @Title: onOffline
	  * @Description: TODO
	  * @param  
	  * @return 
	  * @throws
	  */
	public abstract void onOffline();
}
