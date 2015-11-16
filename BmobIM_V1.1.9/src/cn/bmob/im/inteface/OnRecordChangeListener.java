package cn.bmob.im.inteface;

/** 录音变化监听
  * @ClassName: UploadListener
  * @Description: TODO
  * @author smile
  * @date 2014-6-19 下午3:07:25
  */
public interface OnRecordChangeListener {
	
	/** 音量的变化
	  * @Title: onChange
	  * @Description: TODO
	  * @param  value：当前音量值 
	  * @return 
	  * @throws
	  */
	public abstract void onVolumnChanged(int value);
		
	/** 录音时间监听
	  * @Title: onTimeChanged
	  * @Description: TODO
	  * @param  value 当前录音时长
	  * @param  localPath 录音文件地址--用于当录到1分钟的时候默认就发送消息
	  * @return 
	  * @throws
	  */
	public abstract void onTimeChanged(int value,String localPath);
}
