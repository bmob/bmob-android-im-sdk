package com.bmob.im.demo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;

import com.bmob.im.demo.bean.FaceText;

public class FaceTextUtils {

	public static List<FaceText> faceTexts = new ArrayList<FaceText>();
	static {
		faceTexts.add(new FaceText("\\ue056"));
		faceTexts.add(new FaceText("\\ue057"));
		faceTexts.add(new FaceText("\\ue058"));
		faceTexts.add(new FaceText("\\ue059"));
		faceTexts.add(new FaceText("\\ue105"));
		faceTexts.add(new FaceText("\\ue106"));
		faceTexts.add(new FaceText("\\ue107"));
		faceTexts.add(new FaceText("\\ue108"));
		faceTexts.add(new FaceText("\\ue401"));
		faceTexts.add(new FaceText("\\ue402"));
		faceTexts.add(new FaceText("\\ue403"));
		faceTexts.add(new FaceText("\\ue404"));
		faceTexts.add(new FaceText("\\ue405"));
		faceTexts.add(new FaceText("\\ue406"));
		faceTexts.add(new FaceText("\\ue407"));
		faceTexts.add(new FaceText("\\ue408"));
		faceTexts.add(new FaceText("\\ue409"));
		faceTexts.add(new FaceText("\\ue40a"));
		faceTexts.add(new FaceText("\\ue40b"));
		faceTexts.add(new FaceText("\\ue40d"));
		faceTexts.add(new FaceText("\\ue40e"));
		faceTexts.add(new FaceText("\\ue40f"));
		faceTexts.add(new FaceText("\\ue410"));
		faceTexts.add(new FaceText("\\ue411"));
		faceTexts.add(new FaceText("\\ue412"));
		faceTexts.add(new FaceText("\\ue413"));
		faceTexts.add(new FaceText("\\ue414"));
		faceTexts.add(new FaceText("\\ue415"));
		faceTexts.add(new FaceText("\\ue416"));
		faceTexts.add(new FaceText("\\ue417"));
		faceTexts.add(new FaceText("\\ue418"));
		faceTexts.add(new FaceText("\\ue41f"));
		faceTexts.add(new FaceText("\\ue00e"));
		faceTexts.add(new FaceText("\\ue421"));
	}

	public static String parse(String s) {
		for (FaceText faceText : faceTexts) {
			s = s.replace("\\" + faceText.text, faceText.text);
			s = s.replace(faceText.text, "\\" + faceText.text);
		}
		return s;
	}

	/** 
	  * toSpannableString
	  * @return SpannableString
	  * @throws
	  */
	public static SpannableString toSpannableString(Context context, String text) {
		if (!TextUtils.isEmpty(text)) {
			SpannableString spannableString = new SpannableString(text);
			int start = 0;
			Pattern pattern = Pattern.compile("\\\\ue[a-z0-9]{3}", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(text);
			while (matcher.find()) {
				String faceText = matcher.group();
				String key = faceText.substring(1);
				BitmapFactory.Options options = new BitmapFactory.Options();
				Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
						context.getResources().getIdentifier(key, "drawable", context.getPackageName()), options);
				ImageSpan imageSpan = new ImageSpan(context, bitmap);
				int startIndex = text.indexOf(faceText, start);
				int endIndex = startIndex + faceText.length();
				if (startIndex >= 0)
					spannableString.setSpan(imageSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				start = (endIndex - 1);
			}

			return spannableString;
		} else {
			return new SpannableString("");
		}
	}

	public static SpannableString toSpannableString(Context context, String text, SpannableString spannableString) {

		int start = 0;
		Pattern pattern = Pattern.compile("\\\\ue[a-z0-9]{3}", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			String faceText = matcher.group();
			String key = faceText.substring(1);
			BitmapFactory.Options options = new BitmapFactory.Options();
//			options.inSampleSize = 2;
			Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), context.getResources()
					.getIdentifier(key, "drawable", context.getPackageName()), options);
			ImageSpan imageSpan = new ImageSpan(context, bitmap);
			int startIndex = text.indexOf(faceText, start);
			int endIndex = startIndex + faceText.length();
			if (startIndex >= 0)
				spannableString.setSpan(imageSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			start = (endIndex - 1);
		}

		return spannableString;
	}

}
