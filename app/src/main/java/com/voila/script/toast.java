package com.voila.script;

import android.annotation.*;
import android.content.*;
import android.widget.*;

@SuppressLint("all")
public class toast
{
	private static Toast t;

	public static Toast makeText(Context cxt, CharSequence msg, int duration)
	{
		if(t != null)
			t.cancel();
		t = Toast.makeText(cxt, msg, duration);
		return t;
	}
}
