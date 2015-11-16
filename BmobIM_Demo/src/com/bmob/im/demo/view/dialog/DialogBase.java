package com.bmob.im.demo.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bmob.im.demo.R;

/**
 *自定义对话框基类
 *支持：对话框全屏显示控制、title显示控制，一个button或两个
 */
public abstract class DialogBase extends Dialog {
	protected OnClickListener onSuccessListener;
	protected Context mainContext;
	protected OnClickListener onCancelListener;//提供给取消按钮
	protected OnDismissListener onDismissListener;
	
	protected View view;
	protected Button positiveButton, negativeButton;
	private boolean isFullScreen = false;
	
	private boolean hasTitle = true;//是否有title
	
	private int width = 0, height = 0, x = 0, y = 0;
	private int iconTitle = 0;
	private String message, title;
	private String namePositiveButton, nameNegativeButton;
	private final int MATCH_PARENT = android.view.ViewGroup.LayoutParams.MATCH_PARENT;

	private boolean isCancel = true;//默认是否可点击back按键/点击外部区域取消对话框
	
	
	public boolean isCancel() {
		return isCancel;
	}

	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}

	/**
	 * 构造函数
	 * @param context 对象应该是Activity
	 */
	public DialogBase(Context context) {
		super(context, R.style.alert);
		this.mainContext = context;
	}
	
	/** 
	 * 创建事件
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.v2_dialog_base);
		this.onBuilding();
		// 设置标题和消息
		LinearLayout dialog_top = (LinearLayout)findViewById(R.id.dialog_top);
		View title_red_line = (View)findViewById(R.id.title_red_line);
		
		//是否有title
		if(hasTitle){
			dialog_top.setVisibility(View.VISIBLE);
			title_red_line.setVisibility(View.VISIBLE);
		}else{
			dialog_top.setVisibility(View.GONE);
			title_red_line.setVisibility(View.GONE);
		}
		TextView titleTextView = (TextView)findViewById(R.id.dialog_title);
		titleTextView.setText(this.getTitle());
		TextView messageTextView = (TextView)findViewById(R.id.dialog_message);
		messageTextView.setText(this.getMessage());
		
		if (view != null) {
			FrameLayout custom = (FrameLayout) findViewById(R.id.dialog_custom);
			custom.addView(view, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
			findViewById(R.id.dialog_contentPanel).setVisibility(View.GONE);
		} else {
			findViewById(R.id.dialog_customPanel).setVisibility(View.GONE);
		}

		// 设置按钮事件监听
		positiveButton = (Button)findViewById(R.id.dialog_positivebutton);
		negativeButton = (Button)findViewById(R.id.dialog_negativebutton);
		if(namePositiveButton != null && namePositiveButton.length()>0){
			positiveButton.setText(namePositiveButton);
			positiveButton.setOnClickListener(GetPositiveButtonOnClickListener());
		} else {
			positiveButton.setVisibility(View.GONE);
			findViewById(R.id.dialog_leftspacer).setVisibility(View.VISIBLE);
			findViewById(R.id.dialog_rightspacer).setVisibility(View.VISIBLE);
		}
		if(nameNegativeButton != null && nameNegativeButton.length()>0){
			negativeButton.setText(nameNegativeButton);
			negativeButton.setOnClickListener(GetNegativeButtonOnClickListener());
		} else {
			negativeButton.setVisibility(View.GONE);
		}
		
		// 设置对话框的位置和大小
		LayoutParams params = this.getWindow().getAttributes();  
		if(this.getWidth()>0)
			params.width = this.getWidth();  
		if(this.getHeight()>0)
			params.height = this.getHeight();  
		if(this.getX()>0)
			params.width = this.getX();  
		if(this.getY()>0)
			params.height = this.getY();  
		
		// 如果设置为全屏
		if(isFullScreen) {
			params.width = WindowManager.LayoutParams.MATCH_PARENT;
			params.height = WindowManager.LayoutParams.MATCH_PARENT;
		}
		
		//设置点击dialog外部区域可取消
		if(isCancel){
			setCanceledOnTouchOutside(true);
			setCancelable(true);
		}else{
			setCanceledOnTouchOutside(false);
			setCancelable(false);
		}
	    getWindow().setAttributes(params);  
		this.setOnDismissListener(GetOnDismissListener());
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}

	/**
	 * 获取OnDismiss事件监听，释放资源
	 * @return OnDismiss事件监听
	 */
	protected OnDismissListener GetOnDismissListener() {
		return new OnDismissListener(){
			public void onDismiss(DialogInterface arg0) {
				DialogBase.this.onDismiss();
				DialogBase.this.setOnDismissListener(null);
				view = null;
				mainContext = null;
				positiveButton = null;
				negativeButton = null;
				if(onDismissListener != null){
					onDismissListener.onDismiss(null);
				}
			}			
		};
	}

	/**
	 * 获取确认按钮单击事件监听
	 * @return 确认按钮单击事件监听
	 */
	protected View.OnClickListener GetPositiveButtonOnClickListener() {
		return new View.OnClickListener() {
			public void onClick(View v) {
				if(OnClickPositiveButton())
					DialogBase.this.dismiss();
			}
		};
	}
	
	/**
	 * 获取取消按钮单击事件监听
	 * @return 取消按钮单击事件监听
	 */
	protected View.OnClickListener GetNegativeButtonOnClickListener() {
		return new View.OnClickListener() {
			public void onClick(View v) {
				OnClickNegativeButton();
				DialogBase.this.dismiss();
			}
		};
	}
	
	/**
	 * 获取焦点改变事件监听，设置EditText文本默认全选
	 * @return 焦点改变事件监听
	 */
	protected OnFocusChangeListener GetOnFocusChangeListener() {
		return new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && v instanceof EditText) {
					((EditText) v).setSelection(0, ((EditText) v).getText().length());
				}
			}
		};
	}
	
	/**
	 * 设置成功事件监听，用于提供给调用者的回调函数
	 * @param listener 成功事件监听
	 */
	public void SetOnSuccessListener(OnClickListener listener){
		onSuccessListener = listener;
	}
	
	/**
	 * 设置关闭事件监听，用于提供给调用者的回调函数
	 * @param listener 关闭事件监听
	 */
	public void SetOnDismissListener(OnDismissListener listener){
		onDismissListener = listener;
	}

	/**提供给取消按钮，用于实现类定制
	 * @param listener
	 */
	public void SetOnCancelListener(OnClickListener listener){
		onCancelListener = listener;
	}
	
	/**
	 * 创建方法，用于子类定制创建过程
	 */
	protected abstract void onBuilding();

	/**
	 * 确认按钮单击方法，用于子类定制
	 */
	protected abstract boolean OnClickPositiveButton();

	/**
	 * 取消按钮单击方法，用于子类定制
	 */
	protected abstract void OnClickNegativeButton();

	/**
	 * 关闭方法，用于子类定制
	 */
	protected abstract void onDismiss();

	/**
	 * @return 对话框标题
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title 对话框标题
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * @param iconTitle 标题图标的资源Id
	 */
	public void setIconTitle(int iconTitle) {
		this.iconTitle = iconTitle;
	}

	/**
	 * @return 标题图标的资源Id
	 */
	public int getIconTitle() {
		return iconTitle;
	}

	/**
	 * @return 对话框提示信息
	 */
	protected String getMessage() {
		return message;
	}

	/**
	 * @param message 对话框提示信息
	 */
	protected void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return 对话框View
	 */
	protected View getView() {
		return view;
	}

	/**
	 * @param view 对话框View
	 */
	protected void setView(View view) {
		this.view = view;
	}

	/**
	 * @return 是否全屏
	 */
	public boolean getIsFullScreen() {
		return isFullScreen;
	}

	/**
	 * @param isFullScreen 是否全屏
	 */
	public void setIsFullScreen(boolean isFullScreen) {
		this.isFullScreen = isFullScreen;
	}

	public boolean isHasTitle() {
		return hasTitle;
	}


	public void setHasTitle(boolean hasTitle) {
		this.hasTitle = hasTitle;
	}

	
	/**
	 * @return 对话框宽度
	 */
	protected int getWidth() {
		return width;
	}

	/**
	 * @param width 对话框宽度
	 */
	protected void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return 对话框高度
	 */
	protected int getHeight() {
		return height;
	}

	/**
	 * @param height 对话框高度
	 */
	protected void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return 对话框X坐标
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x 对话框X坐标
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return 对话框Y坐标
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y 对话框Y坐标
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @return 确认按钮名称
	 */
	protected String getNamePositiveButton() {
		return namePositiveButton;
	}

	/**
	 * @param namePositiveButton 确认按钮名称
	 */
	protected void setNamePositiveButton(String namePositiveButton) {
		this.namePositiveButton = namePositiveButton;
	}

	/**
	 * @return 取消按钮名称
	 */
	protected String getNameNegativeButton() {
		return nameNegativeButton;
	}

	/**
	 * @param nameNegativeButton 取消按钮名称
	 */
	protected void setNameNegativeButton(String nameNegativeButton) {
		this.nameNegativeButton = nameNegativeButton;
	}
}