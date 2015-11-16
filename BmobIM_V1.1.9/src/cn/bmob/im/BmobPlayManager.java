package cn.bmob.im;

import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import cn.bmob.im.inteface.OnPlayChangeListener;
import cn.bmob.im.inteface.PlayControlListener;
import cn.bmob.im.util.BmobLog;

/**
 * 语音播放类 负责播放控制--暂时未使用
 * ps:会出现播放错位的问题
 * @ClassName: BmobVoiceManager
 * @Description: TODO
 * @author smile
 * @date 2014-6-30 上午11:28:34
 */
public class BmobPlayManager implements PlayControlListener,OnCompletionListener{

	private MediaPlayer mMediaPlayer;
	
	Context context;
	BmobUserManager userManager;
	// 创建private static类实例
	private volatile static BmobPlayManager INSTANCE;
	// 同步锁
	private static Object INSTANCE_LOCK = new Object();

	private boolean isPlaying = false;
	
	/**
	 * 使用单例模式创建--双重锁定
	 */
	public static BmobPlayManager getInstance(Context context) {
		if (INSTANCE == null)
			synchronized (INSTANCE_LOCK) {
				if (INSTANCE == null) {
					INSTANCE = new BmobPlayManager();
				}
				INSTANCE.init(context);
			}
		return INSTANCE;
	}

    OnPlayChangeListener	mPlayListener;

	/** 设置播放监听
	  * @Title: setOnPlayControlListener
	  * @Description: TODO
	  * @param  listener 
	  * @return 
	  * @throws
	  */
	public void setOnPlayChangeListener(OnPlayChangeListener listener) {
		mPlayListener = listener;
	}
	
	/**
	 * 初始化
	 * @Title: init
	 * @Description: TODO
	 * @param  c
	 * @return 
	 * @throws
	 */
	public void init(Context c) {
		this.context = c;
		// 初始化用户管理
		userManager = BmobUserManager.getInstance(c);
	}

	@Override
	public void playRecording(String fileName,boolean isUserSpeaker) {
		// TODO Auto-generated method stub
		File file = new File(fileName);
		if(file==null || !file.exists()){
			return;
		}
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnErrorListener(new PlayerErrorListener());
		} else {
			mMediaPlayer.stop();
			mMediaPlayer.reset();
		}
		//开启外放模式
		AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		if (isUserSpeaker){//设置是否使用扬声器,
			audioManager.setMode(AudioManager.MODE_NORMAL);
			audioManager.setSpeakerphoneOn(true);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
		}else{//关闭扬声器--启用耳机听筒模式
			audioManager.setSpeakerphoneOn(false);
			 audioManager.setMode(AudioManager.MODE_IN_CALL);
			 mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
		}
		
		try {
			mMediaPlayer.setDataSource(fileName);
			mMediaPlayer.prepare();
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.seekTo(0);
			mMediaPlayer.start();
			isPlaying = true;
			//用于提供给开发者在此时做一些界面更新操作：如播放动画
			if(mPlayListener!=null){
				mPlayListener.onPlayStart();
			}
		} catch (IOException e) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	@Override
	public void stopPlayback() {
		// TODO Auto-generated method stub
		if (mMediaPlayer != null) {
			onStop();
		}
	}

	@Override
	public boolean isPlaying() {
		// TODO Auto-generated method stub
		return isPlaying;
	}

	@Override
	public int getPlaybackDuration() {
		// TODO Auto-generated method stub
		int duration = 0;
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			duration = mMediaPlayer.getDuration();
		}
		return duration;
	}

	@Override
	public MediaPlayer getMediaPlayer() {
		// TODO Auto-generated method stub
		return mMediaPlayer;
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		onStop();
	}
	
	/** 一种是播放完成停止，一种是人为停止，不管哪种，都需要重置
	  * @Title: onStop
	  * @Description: TODO
	  * @param  
	  * @return void
	  * @throws
	  */
	private void onStop(){
		//用于语音播放完成之后的界面更新
		if(mPlayListener!=null){
			mPlayListener.onPlayStop();
		}
		mMediaPlayer.stop();
		mMediaPlayer.release();
		mMediaPlayer = null;
		isPlaying = false;
	}
	
	/**
	 * 监听播放错误
	 * @ClassName: PlayerErrorListener
	 * @Description: TODO
	 * @author smile
	 * @date 2014-6-30 下午8:09:56
	 */
	@SuppressLint("DefaultLocale")
	public class PlayerErrorListener implements	android.media.MediaPlayer.OnErrorListener {

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			String whatDescription = "";
			switch (what) {
			case MediaPlayer.MEDIA_ERROR_UNKNOWN:
				whatDescription = "MEDIA_ERROR_UNKNOWN";
				break;
			case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
				whatDescription = "MEDIA_ERROR_SERVER_DIED";
				break;
			default:
				whatDescription = Integer.toString(what);
				break;
			}
			BmobLog.i("voice", String.format(
					"MediaPlayer error occured: %s:%d", whatDescription, extra));
			return false;
		}

	}

}
