package cn.bmob.im;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.inteface.OnRecordChangeListener;
import cn.bmob.im.inteface.RecordControlListener;
import cn.bmob.im.util.BmobLog;
import cn.bmob.im.util.BmobUtils;

/**
 * 录音管理类 负责录音和存储
 * @ClassName: BmobVoiceManager
 * @Description: TODO
 * @author smile
 * @date 2014-6-30 上午11:28:34
 */
public class BmobRecordManager implements RecordControlListener {

	private static final int RECORDING_BITRATE = 12200;// 比特率
	private MediaRecorder mMediaRecorder;

	public static int MAX_RECORD_TIME = 60; // 最长录制时间，单位秒，0为无时间限制
	public static int MIN_RECORD_TIME = 1; // 最短录制时间，单位秒，0为无时间限制，建议设为1

	private File file;
	private long startTime;
	
	private String recordName;
	
	private String recordPath;
	
	Context context;
	BmobUserManager userManager;
	// 创建private static类实例
	private volatile static BmobRecordManager INSTANCE;
	// 同步锁
	private static Object INSTANCE_LOCK = new Object();

	/**
	 * 使用单例模式创建--双重锁定
	 */
	public static BmobRecordManager getInstance(Context context) {
		if (INSTANCE == null)
			synchronized (INSTANCE_LOCK) {
				if (INSTANCE == null) {
					INSTANCE = new BmobRecordManager();
				}
				INSTANCE.init(context);
			}
		return INSTANCE;
	}

	/**
	 * 初始化
	 * @Title: init
	 * @Description: TODO
	 * @param  c
	 * @return void
	 * @throws
	 */
	public void init(Context c) {
		this.context = c;
		// 初始化用户管理
		userManager = BmobUserManager.getInstance(c);
		// 初始化线程池
		mThreadPool = Executors.newCachedThreadPool();
	}

	OnRecordChangeListener mChangListener;

	public void setOnRecordChangeListener(OnRecordChangeListener listener) {
		mChangListener = listener;
	}

	private AtomicBoolean mIsRecording = new AtomicBoolean(false);

	// Thread pool
	private ExecutorService mThreadPool;

	private static final int UPDATE_VOICE_CHANGE = 10;///更新语音图片
	
    final Handler hander = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == UPDATE_VOICE_CHANGE) {
				int volumn = msg.arg1;
				int time = msg.arg2;
				if (mChangListener != null) {
					mChangListener.onVolumnChanged(volumn);
					if(time%10==0){
						mChangListener.onTimeChanged((time/10),recordPath);
					}
				}
				return true;
			} else {
				return false;
			}
			
		}
	});

	private final class RecordingChangeUpdater implements Runnable {

		@Override
		public void run() {
			int currentRecordCounter = 0;
			while (mIsRecording.get()) {
				//计算当前音量哦
				int volume = mMediaRecorder.getMaxAmplitude();
				int value = 5 * volume / 32768;// 将当前的音量值控制在0-6之间，方便切换动画图片
				if (value > 5)
					value = 5;
				//更新图片
				Message msg =new Message();
				msg.arg1 = value ;//当前音量大小
				msg.arg2 = currentRecordCounter;//当前录音时长
				msg.what = UPDATE_VOICE_CHANGE;
				hander.sendMessage(msg);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// Re-assert the thread's interrupted status
					Thread.currentThread().interrupt();
					return;
				}
				currentRecordCounter++;
			}
		}
	}

	@SuppressLint("InlinedApi")
	@Override
	public void startRecording(String chatObjectId) {
		// TODO Auto-generated method stub
		if (mMediaRecorder == null) {
			mMediaRecorder = new MediaRecorder();
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置输出格式
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);// 设置输出编码格式
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);// 设置音频编码器可用于录制
			mMediaRecorder.setAudioChannels(1);// 设置录制的音频通道数-单通道
			mMediaRecorder.setAudioEncodingBitRate(RECORDING_BITRATE);// 设置音频编码录音比特率
			mMediaRecorder.setOnErrorListener(new RecorderErrorListener());// 设置录音错误监听
		} else {
			mMediaRecorder.stop();
			mMediaRecorder.reset();
		}
		//获取录音文件名
		this.recordName = getRecordFileName();
		// 获取专属目录
	    this.recordPath = getRecordFilePath(chatObjectId);
//		BmobLog.i("voice", "此次录音文件的地址 = "+recordPath);
		this.file = new File(recordPath);
		// 设置输出源
		mMediaRecorder.setOutputFile(file.getAbsolutePath());
		try {
			mMediaRecorder.prepare();
			mMediaRecorder.start();
			// 设置为true--表明正在录音状态
			mIsRecording.set(true);
			//起始时间
			this.startTime = new Date().getTime();
			//计算录音长度和音量的变化
			mThreadPool.execute(new RecordingChangeUpdater());
			
		} catch (IllegalStateException e) {
			BmobLog.i("voice",
					"IllegalStateException thrown while trying to record a greeting");
			mIsRecording.set(false);
			mMediaRecorder.release();
			mMediaRecorder = null;
			
		} catch (IOException e) {
			BmobLog.i("voice",
					"IOException thrown while trying to record a greeting");
			mIsRecording.set(false);
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}

	@Override
	public void cancelRecording() {
		// TODO Auto-generated method stub
		if (mMediaRecorder == null)
			return;
		this.mMediaRecorder.stop();
		this.mMediaRecorder.release();
		this.mMediaRecorder = null;
		//放弃录音就删除指定待存储的文件
		if ((this.file != null) && (this.file.exists())
				&& (!this.file.isDirectory()))
			this.file.delete();
		// 设置为false--表明录音结束
		mIsRecording.set(false);
	}

	@Override
	public int stopRecording() {
		// TODO Auto-generated method stub
		// 设置为false--表明录音结束
		if (mMediaRecorder != null) {
//			BmobLog.i("voice", "Stopping recording");
			mIsRecording.set(false);
			mMediaRecorder.stop();
			mMediaRecorder.release();
			mMediaRecorder = null;
			// 获取当前的录音长度
			int i = (int)(new Date().getTime() - this.startTime) / 1000;
			return i;
		}
		return 0;
	}

	@Override
	public boolean isRecording() {
		// TODO Auto-generated method stub
		return mIsRecording.get();
	}
	
	@Override
	public MediaRecorder getMediaRecorder() {
		// TODO Auto-generated method stub
		return mMediaRecorder;
	}

	/**
	 * 监听录音错误
	 * 
	 * @ClassName: RecorderErrorListener
	 * @Description: TODO
	 * @author smile
	 * @date 2014-6-30 下午8:10:10
	 */
	public class RecorderErrorListener implements android.media.MediaRecorder.OnErrorListener {

		@Override
		public void onError(MediaRecorder mp, int what, int extra) {
			String whatDescription = "";
			switch (what) {
			case MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN:
				whatDescription = "MEDIA_RECORDER_ERROR_UNKNOWN";
				break;
			default:
				whatDescription = Integer.toString(what);
				break;
			}
			BmobLog.i("voice", String.format(
					"MediaRecorder error occured: %s,%d", whatDescription,
					extra));
		}

	}

	/**
	 * 获取录音文件的存储地址
	 * @Title: getOutputFileName
	 * @Description: TODO
	 * @param @param chatObjectId:聊天的对象Id
	 * @param @return
	 * @return String
	 * @throws
	 */
	@Override
	public String getRecordFilePath(String chatObjectId) {
		// 录音文件总目录中属于当前登陆用户的目录
		String accountDir = BmobUtils.string2MD5(userManager.getCurrentUserObjectId());
		File dir = new File(BmobConfig.BMOB_VOICE_DIR + File.separator
				+ accountDir+ File.separator+ chatObjectId );
		if (!dir.exists()) {
			dir.mkdirs();
		}
		// 在当前用户的目录下面存放录音文件
		File audioFile = new File(dir.getAbsolutePath() + File.separator + this.recordName);
		try {
			if (!audioFile.exists()) {
				audioFile.createNewFile();
			}
		} catch (IOException e) {
			
		}
		return audioFile.getAbsolutePath();
	}
	
	public String getRecordFileName() {
		return System.currentTimeMillis() + ".amr";
	}

}
