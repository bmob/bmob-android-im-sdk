package cn.bmob.im.util;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.bmob.im.bean.BmobChatUser;

public class BmobUtils {

	public static boolean isNotNull(Collection<?> collection) {
		if (collection != null && collection.size() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * @Title: list2map
	 * @Description: TODO
	 * @param @param users
	 * @param @return
	 * @return Map<String,BmobChatUser>
	 * @throws
	 */
	public static Map<String, BmobChatUser> list2map(List<BmobChatUser> users) {
		Map<String, BmobChatUser> friends = new HashMap<String, BmobChatUser>();
		for (BmobChatUser user : users) {
			friends.put(user.getObjectId(), user);
		}
		return friends;
	}

	/**
	 * @Title: list2Array
	 * @Description: TODO
	 * @param @param users
	 * @param @return
	 * @return Map<String,BmobChatUser>
	 * @throws
	 */
	public static List<String> list2Array(List<BmobChatUser> users) {
		List<String> names = new ArrayList<String>();
		if(users!=null && users.size()>0){
			for (BmobChatUser user : users) {
				names.add(user.getUsername());
			}
		}
		return names;
	}

	/**
	 * @Title: map2list
	 * @Description: TODO
	 * @param @param maps
	 * @param @return
	 * @return List<BmobChatUser>
	 * @throws
	 */
	public static List<BmobChatUser> map2list(Map<String, BmobChatUser> maps) {
		List<BmobChatUser> users = new ArrayList<BmobChatUser>();
		Iterator<Entry<String, BmobChatUser>> iterator = maps.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			Entry<String, BmobChatUser> entry = iterator.next();
			users.add(entry.getValue());
		}
		return users;
	}

	/***
	 * MD5加码 生成32位md5码
	 */
	public static String string2MD5(String inStr) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
			return "";
		}
		char[] charArray = inStr.toCharArray();
		byte[] byteArray = new byte[charArray.length];

		for (int i = 0; i < charArray.length; i++)
			byteArray[i] = (byte) charArray[i];
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16)
				hexValue.append("0");
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();

	}

	/**
	 * 加密解密算法 执行一次加密，两次解密
	 */
	public static String convertMD5(String inStr) {

		char[] a = inStr.toCharArray();
		for (int i = 0; i < a.length; i++) {
			a[i] = (char) (a[i] ^ 't');
		}
		String s = new String(a);
		return s;
	}

	/**
	 * 转换文件大小
	 * 
	 * @Title: getFormatSize
	 * @Description: TODO
	 * @param @param size
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String getFormatSize(double size) {
		double kiloByte = size / 1024;
		if (kiloByte < 1) {
			return size + "Byte(s)";
		}

		double megaByte = kiloByte / 1024;
		if (megaByte < 1) {
			BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
			return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
					.toPlainString() + "KB";
		}

		double gigaByte = megaByte / 1024;
		if (gigaByte < 1) {
			BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
			return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
					.toPlainString() + "MB";
		}

		double teraBytes = gigaByte / 1024;
		if (teraBytes < 1) {
			BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
			return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
					.toPlainString() + "GB";
		}
		BigDecimal result4 = new BigDecimal(teraBytes);
		return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
				+ "TB";
	}

	/**
	 * 时间差 
	 */
	public static int interval=0;
	
	/** 获取本机的时间--与服务器时间进行同步
	 * @return
	 */
	public static long getTimeStamp(){
		long localTime = System.currentTimeMillis()/1000;
		return (localTime-interval);
	}
	
}
