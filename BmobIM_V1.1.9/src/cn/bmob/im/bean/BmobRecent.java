package cn.bmob.im.bean;

import java.io.Serializable;

/**
 * 本地维护的最近会话列表
 * @ClassName: BmobRecent
 * @Description: TODO
 * @author smile
 * @date 2014-5-30 上午11:53:08
 */
public class BmobRecent implements Comparable<BmobRecent>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 目标用户id
	 */
	private String targetid;//
	/**
	 * 目标用户名
	 */
	private String userName;//
	/**
	 * 目标昵称
	 */
	private String nick;//
	/**
	 * 目标用户头像
	 */
	private String avatar;//
	/**
	 * 消息内容
	 */
	private String message;//
	/**
	 * 消息日期
	 */
	private long time;//
	/**
	 * 消息类型
	 */
	private int type;//

	public BmobRecent() {

	}

	public BmobRecent(String targetid, String userName, String nick,
			String avatar, String message, long time, int type) {
		super();
		this.targetid = targetid;
		this.avatar = avatar;
		this.nick = nick;
		this.userName = userName;
		this.message = message;
		this.time = time;
		this.type = type;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTargetid() {
		return targetid;
	}

	public void setTargetid(String targetid) {
		this.targetid = targetid;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	@Override
	public int hashCode() {
		return 10 * getUserName().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof BmobRecent)) {
			return false;
		}
		return getUserName().equals(((BmobRecent) o).getUserName());
	}

	@Override
	public String toString() {
		return nick == null ? userName : nick;
	}

	@Override
	public int compareTo(BmobRecent another) {
		// TODO Auto-generated method stub
		return (int) (another.time - this.time);
	}

}
