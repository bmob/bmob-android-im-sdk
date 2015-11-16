package cn.bmob.im.inteface;


/** 播放变化监听 
 *  用于提供给开发者调用
  * @ClassName: VoiceListener
  * @Description: TODO
  * @author smile
  * @date 2014-6-30 下午7:54:44
  */
public interface OnPlayChangeListener {
	
	/** 播放录音开始
	  * @Title: playGreeting
	  * @Description: TODO
	  * @param  fileName
	  * @param  isUserSpeaker 是否使用扬声器 
	  * @return 
	  * @throws
	  */
	void onPlayStart();

	/** 播放录音结束
	  * @Title: stopPlayback
	  * @Description: TODO
	  * @param  
	  * @return 
	  * @throws
	  */
	void onPlayStop();
	

}
