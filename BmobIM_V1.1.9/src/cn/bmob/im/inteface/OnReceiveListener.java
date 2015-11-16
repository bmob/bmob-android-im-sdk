package cn.bmob.im.inteface;

import cn.bmob.im.bean.BmobMsg;

/**
 * BmobMsg表的回调监听
 * @ClassName: DownloadListener
 * @Description: TODO
 * @author smile
 * @date 2014-7-3 上午11:27:32
 */
public abstract interface OnReceiveListener {

	public abstract void onSuccess(BmobMsg msg);

	public abstract void onFailure(int code, String arg1);

}
