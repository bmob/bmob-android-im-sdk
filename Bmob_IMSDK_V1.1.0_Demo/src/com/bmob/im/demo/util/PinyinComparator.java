package com.bmob.im.demo.util;

import java.util.Comparator;

import com.bmob.im.demo.bean.User;

public class PinyinComparator implements Comparator<User> {

	public int compare(User o1, User o2) {
		if (o1.getSortLetters().equals("@")
				|| o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}
	}

}
