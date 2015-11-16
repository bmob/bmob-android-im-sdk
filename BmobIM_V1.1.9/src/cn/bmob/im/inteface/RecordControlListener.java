package cn.bmob.im.inteface;

import android.media.MediaRecorder;

/** 录音控制监听
  * @ClassName: VoiceListener
  * @Description: TODO
  * @author smile
  * @date 2014-6-30 下午7:54:44
  */
public interface RecordControlListener {
	
	/** 开始录音
	  * @Title: startRecord
	  * @Description: TODO
	  * @param  fileName 
	  * @return void
	  * @throws
	  */
	public void startRecording(String chatObjectId);
	
	/** 取消录音
	  * @Title: giveUpRecordIng
	  * @Description: TODO
	  * @param  
	  * @return void
	  * @throws
	  */
	public void cancelRecording();
	
	/** 停止录音
	  * @Title: stopRecording
	  * @Description: TODO
	  * @param  
	  * @return int
	  * @throws
	  */
	public int stopRecording();
	
	/** 当前是否是处于录音状态
 	  * @Title: isRecording
	  * @Description: TODO
	  * @param  
	  * @return boolean
	  * @throws
	  */
	public boolean isRecording();
	
	/** 获取录音器
	  * @Title: getMediaRecorder
	  * @Description: TODO
	  * @param  
	  * @return MediaRecorder
	  * @throws
	  */
	public MediaRecorder getMediaRecorder();

	/** 获取此次录音文件的完整地址
	  * @Title: getVoicePath
	  * @Description: TODO
	  * @param  
	  * @return String
	  * @throws
	  */
	public String getRecordFilePath(String chatObjectId);
	
}
