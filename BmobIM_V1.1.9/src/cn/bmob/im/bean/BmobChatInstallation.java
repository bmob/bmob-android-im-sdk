package cn.bmob.im.bean;

import android.content.Context;
import cn.bmob.v3.BmobInstallation;

/** 自定义实现BmobInstallation，增加定制字段uid，将设备id和uid进行绑定
 * @ClassName: CustomBmobInstallation
 * @Description: TODO
 * @author smile
 * @date 2014-5-27 上午11:04:23
 */
public class BmobChatInstallation extends  BmobInstallation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BmobChatInstallation(Context arg0) {
		super(arg0);
	}
	
	/**
	 * 该设备对应的uid，有可能一个用户有多台设备，因此添加该uid字段--对应User表中的objectId字段
	 */
	private String uid;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	
}
