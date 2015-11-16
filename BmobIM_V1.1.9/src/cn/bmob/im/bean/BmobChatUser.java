package cn.bmob.im.bean;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobRelation;

/** 自定义BmobUser，添加关联字段：nick（昵称）、avatar（头像）、installId（设备id）、deviceType(设备类型)、blacklist（黑名单）、contacts（好友列表）
 * @ClassName: ChatBmobUser
 * @Description: TODO
 * @author smile
 * @date 2014-5-29 下午3:13:35
 */
public class BmobChatUser extends BmobUser{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String nick;//昵称
	private String avatar;//头像信息
	private BmobRelation contacts;//好友联系人
	private String installId;//设备Id
	private String deviceType;//设备类型
	private BmobRelation blacklist;//黑名单
	
//	private List<String> groups = new ArrayList<String>();//加入的群组名-这个群组对应的是Installation表里的channels字段
//	
//	
//	public List<String> getGroups() {
//		return groups;
//	}
//
//	public void setGroups(List<String> groups) {
//		this.groups = groups;
//	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public BmobRelation getBlacklist() {
		return blacklist;
	}

	public void setBlacklist(BmobRelation blacklist) {
		this.blacklist = blacklist;
	}

	public String getInstallId() {
		return installId;
	}

	public void setInstallId(String installId) {
		this.installId = installId;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}


	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public BmobRelation getContacts() {
		return contacts;
	}

	public void setContacts(BmobRelation contacts) {
		this.contacts = contacts;
	}
	
}
