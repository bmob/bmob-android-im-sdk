package cn.bmob.im.inteface;

/**
 * 下载回调监听
 * @ClassName: DownloadListener
 * @Description: TODO
 * @author smile
 * @date 2014-7-3 上午11:27:32
 */
public abstract interface DownloadListener {

	public abstract void onStart();
	
	public abstract void onSuccess();

	public abstract void onError(String error);

//	public abstract void onProgress(int progress);
}
