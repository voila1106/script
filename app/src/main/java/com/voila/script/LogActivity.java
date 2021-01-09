package com.voila.script;

import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.util.*;

public class LogActivity extends AppCompatActivity
{
	TextView tx;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);

		tx = findViewById(R.id.logtx);
		tx.setText(util.readFile(MainActivity.log));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inf = getMenuInflater();
		inf.inflate(R.menu.log_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item)
	{
		if(item.getItemId() == R.id.clear)
		{
			util.writeFile(MainActivity.log, new Date() + "\n(clear)\n", false);
			tx.setText(util.readFile(MainActivity.log));
		}
		return true;
	}
}