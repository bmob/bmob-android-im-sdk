package cn.bmob.im;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.inteface.DownloadListener;
import cn.bmob.im.util.BmobUtils;

/**
 * 文件下载
 * @ClassName: DownloadTask
 * @Description: TODO
 * @author smile
 * @date 2014-7-3 下午5:51:15
 */
public class BmobDownloadManager extends AsyncTask<String, Integer, String>{

	private Context context;
	private PowerManager.WakeLock mWakeLock;
	private final static int BUFFER = 1024; 
	BmobMsg msg;
	BmobUserManager userManager;
	DownloadListener downloadListener;

	public BmobDownloadManager(Context context, BmobMsg msg,DownloadListener downloadListener) {
		this.context = context;
		this.msg = msg;
		this.downloadListener = downloadListener;
		// 初始化用户管理
		userManager = BmobUserManager.getInstance(context);
	}

	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
		mWakeLock.acquire();
		//开始下载
		if(downloadListener!=null){
			downloadListener.onStart();
		}
	}
	
	@Override
	protected String doInBackground(String... sUrl) {
		String url = sUrl[0];
		InputStream input = null;
		OutputStream output = null;
		try {
			File dest_file = new File(getDownLoadFilePath(msg));
			URL u = new URL(url);
			DataInputStream stream = new DataInputStream(u.openStream());
			DataOutputStream fos = new DataOutputStream(new FileOutputStream(dest_file));
			 byte[] b = new byte[BUFFER];  
             int len = 0;  
             while((len=stream.read(b))!= -1){  
            	 fos.write(b,0,len); 
             }  
			stream.close();
			fos.close();
		} catch (Exception e) {
			return e.toString();
		} finally {
			try {
				if (output != null)
					output.close();
				if (input != null)
					input.close();
			} catch (IOException ignored) {
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
//		if(downloadListener!=null){
//			downloadListener.onProgress(values[0]);
//		}
	}
	
	@Override
	protected void onPostExecute(String result) {
		mWakeLock.release();
		if (result != null){
			if(downloadListener!=null){
				downloadListener.onError(result);
			}
		}else {
			//更新该消息的下载本地地址
			BmobDB.create(context).updateContentForTargetMsg(getDownLoadFilePath(msg), msg);
			if(downloadListener!=null){
				downloadListener.onSuccess();
			}
		}
	}
	/**
	 * 所有接收到的语音消息其存储的文件名的格式:该条语音消息的时间belongtime.amr
	 * @Title: getDownLoadFilePath
	 * @Description: TODO
	 * @param @param chatObjectId
	 * @param @return
	 * @return String
	 * @throws
	 */
	private String getDownLoadFilePath(BmobMsg msg) {
		String accountDir = BmobUtils.string2MD5(userManager
				.getCurrentUserObjectId());
		File dir = new File(BmobConfig.BMOB_VOICE_DIR + File.separator
				+ accountDir + File.separator + msg.getBelongId());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		// 在当前用户的目录下面存放录音文件
		File audioFile = new File(dir.getAbsolutePath() + File.separator
				+ msg.getMsgTime() + ".amr");
		try {
			if (!audioFile.exists()) {
				audioFile.createNewFile();
			}
		} catch (IOException e) {
		}
		return audioFile.getAbsolutePath();
	}
	
	/**
	 * 检测指定的文件是否存在
	 * @Title: checkTargetPathExist
	 * @Description: TODO
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	public static boolean checkTargetPathExist(String currentObjectId,BmobMsg msg) {
		String accountDir = BmobUtils.string2MD5(currentObjectId);
		File dir = new File(BmobConfig.BMOB_VOICE_DIR + File.separator
				+ accountDir + File.separator + msg.getBelongId());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		// 在当前用户的目录下面存放录音文件
		File audioFile = new File(dir.getAbsolutePath() + File.separator
				+ msg.getMsgTime() + ".amr");
		if (audioFile.exists() && audioFile.isFile()) {
			return true;
		} else {
			return false;
		}
	}
	
}