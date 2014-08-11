package com.bmob.im.demo.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.bmob.im.demo.R;
import com.bmob.im.demo.bean.User;
import com.bmob.im.demo.util.ImageLoadOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/** 好友列表
  * @ClassName: UserFriendAdapter
  * @Description: TODO
  * @author smile
  * @date 2014-6-12 下午3:03:40
  */
@SuppressLint("DefaultLocale")
public class UserFriendAdapter extends BaseAdapter implements SectionIndexer {
	private Context ct;
	private List<User> data;

	public UserFriendAdapter(Context ct, List<User> datas) {
		this.ct = ct;
		this.data = datas;
	}

	/** 当ListView数据发生变化时,调用此方法来更新ListView
	  * @Title: updateListView
	  * @Description: TODO
	  * @param @param list 
	  * @return void
	  * @throws
	  */
	public void updateListView(List<User> list) {
		this.data = list;
		notifyDataSetChanged();
	}

	public void remove(User user){
		this.data.remove(user);
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(ct).inflate(
					R.layout.item_user_friend, null);
			viewHolder = new ViewHolder();
			viewHolder.alpha = (TextView) convertView.findViewById(R.id.alpha);
			viewHolder.name = (TextView) convertView
					.findViewById(R.id.tv_friend_name);
			viewHolder.avatar = (ImageView) convertView
					.findViewById(R.id.img_friend_avatar);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		User friend = data.get(position);
		final String name = friend.getUsername();
		final String avatar = friend.getAvatar();

		if (!TextUtils.isEmpty(avatar)) {
			ImageLoader.getInstance().displayImage(avatar, viewHolder.avatar, ImageLoadOptions.getOptions());
		} else {
			viewHolder.avatar.setImageDrawable(ct.getResources().getDrawable(R.drawable.head));
		}
		viewHolder.name.setText(name);

		// 根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(position);
		// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		if (position == getPositionForSection(section)) {
			viewHolder.alpha.setVisibility(View.VISIBLE);
			viewHolder.alpha.setText(friend.getSortLetters());
		} else {
			viewHolder.alpha.setVisibility(View.GONE);
		}

		return convertView;
	}

	static class ViewHolder {
		TextView alpha;// 首字母提示
		ImageView avatar;
		TextView name;
	}

	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		return data.get(position).getSortLetters().charAt(0);
	}

	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 */
	@SuppressLint("DefaultLocale")
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = data.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section){
				return i;
			}
		}

		return -1;
	}

	@Override
	public Object[] getSections() {
		return null;
	}

}