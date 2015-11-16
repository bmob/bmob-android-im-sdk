package cn.bmob.im.inteface;

import android.media.MediaPlayer;

/** 语音控制监听
  * @ClassName: VoiceListener
  * @Description: TODO
  * @author smile
  * @date 2014-6-30 下午7:54:44
  */
public interface PlayControlListener {
	
	/** 播放录音
	  * @Title: playGreeting
	  * @Description: TODO
	  * @param  View:点击播放的那个View
	  * @param  fileName
	  * @param  isUserSpeaker 是否使用扬声器 
	  * @return 
	  * @throws
	  */
	void playRecording(String fileName,boolean isUserSpeaker);

	/** 停止播放
	  * @Title: stopPlayback
	  * @Description: TODO
	  * @param  
	  * @return 
	  * @throws
	  */
	void stopPlayback();
	
	/** 是否处于播放状态
	  * @Title: pausePlayback
	  * @Description: TODO
	  * @param  
	  * @return 
	  * @throws
	  */
	boolean isPlaying();
	
	/** 获取播放时长
	  * @Title: getPlaybackDuration
	  * @Description: TODO
	  * @param  
	  * @return int
	  * @throws
	  */
	int getPlaybackDuration();
	
	/** 获取播放器
	  * @Title: getMediaPlayer
	  * @Description: TODO
	  * @param  
	  * @return MediaPlayer
	  * @throws
	  */
	MediaPlayer getMediaPlayer();

}
