package com.voila.script;

import android.content.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.*;
import android.os.Bundle;

import java.util.*;

@SuppressWarnings("all")
public class EditActivity extends AppCompatActivity
{
	ListView fn;
	ListView sh;
	LinearLayout.LayoutParams fnpara;
	LinearLayout.LayoutParams shpara;
	TextView addb;

	ArrayList<String> fnList;
	ArrayList<String> shList;
	iAdapter fnap;
	iAdapter shap;

	double scale;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
		fn = findViewById(R.id.forname);
		sh = findViewById(R.id.search);
		addb = findViewById(R.id.addb);
		fnpara = (LinearLayout.LayoutParams)fn.getLayoutParams();
		shpara = (LinearLayout.LayoutParams)sh.getLayoutParams();
		scale = getResources().getDisplayMetrics().density;

		fnList = new ArrayList<>(Arrays.asList(util.readFile(MainActivity.fscp)
			.split("\\|")[1].trim().split("\n")));
		shList = new ArrayList<>(Arrays.asList(util.readFile(MainActivity.fscp)
			.split("\\|")[0].trim().split("\n")));
		fnList.removeIf(String::isEmpty);
		shList.removeIf(String::isEmpty);
		fnap = new iAdapter(this, R.layout.edit_entity, fnList);
		shap = new iAdapter(this, R.layout.edit_entity, shList);
		fn.setAdapter(fnap);
		sh.setAdapter(shap);

		fnpara.height = (int)(31 * scale) * (fnList.size() + 1);
		fn.setLayoutParams(fnpara);
		shpara.height = (int)(31 * scale) * (shList.size() + 1) + 270;
		sh.setLayoutParams(shpara);

		fn.setOnItemClickListener((parent, view, position, id) ->
		{
			TextView tv = view.findViewById(R.id.editx);
			shList.remove(tv.getText());
			fnList.remove(position);
			fnap.notifyDataSetChanged();
			fnpara.height -= (31 * scale);
		});

		sh.setOnItemClickListener((parent, view, position, id) ->
		{
			TextView tv = view.findViewById(R.id.editx);
			shList.remove(tv.getText());
			shap.notifyDataSetChanged();
			shpara.height -= (31 * scale);
		});

		addb.setOnClickListener(v ->
		{
			EditText findcont = new EditText(EditActivity.this);
			new AlertDialog.Builder(EditActivity.this)
				.setTitle("输入查找表达式")
				.setView(findcont)
				.setPositiveButton("确定", (dialog, which) ->
				{
					if(!isItemExists((findcont.getText() + "").trim()))
					{
						shList.add(findcont.getText() + "");
						shap.notifyDataSetChanged();
						shpara.height += (31 * scale);
					}else
					{
						toast.makeText(EditActivity.this, "已存在或为空！", 0).show();
					}
				}).show();
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inf = getMenuInflater();
		inf.inflate(R.menu.edit_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item)
	{
		if(item.getItemId() == R.id.save)
		{
			StringBuilder sb = new StringBuilder();
			for(String t : shList)
			{
				sb.append(t).append("\n");
			}
			sb.append("|");
			for(String t : fnList)
			{
				sb.append(t).append("\n");
			}
			if(util.writeFile(MainActivity.fscp, sb.toString(), false))
				toast.makeText(this, "保存成功", 0).show();
			else
				toast.makeText(this, "保存失败！", 0).show();

//			fnList = new ArrayList<>(Arrays.asList(util.readFile(MainActivity.fscp)
//				.split("\\|")[1].trim().split("\n")));
//			shList = new ArrayList<>(Arrays.asList(util.readFile(MainActivity.fscp)
//				.split("\\|")[0].trim().split("\n")));
//			fnList.removeIf(String::isEmpty);
//			shList.removeIf(String::isEmpty);
//			fnap.notifyDataSetChanged();
//			shap.notifyDataSetChanged();
		}
		return true;
	}

	private boolean isItemExists(String item)
	{
		if(item.isEmpty())
			return true;
		//String[] its = util.readFile(MainActivity.fscp).split("\\|")[0].split("\n");
		for(String t : shList)
		{
			if(t.trim().equals(item))
			{
				return true;
			}
		}
		return false;
	}

	class iAdapter extends ArrayAdapter
	{
		private int view;

		public iAdapter(@NonNull Context context, int resource, @NonNull Object[] objects)
		{
			super(context, resource, objects);
			view = resource;
		}

		public iAdapter(Context context, int resource, List list)
		{
			super(context, resource, list);
			view = resource;
		}

		@NonNull
		@Override
		public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
		{
			LayoutInflater inf = LayoutInflater.from(getContext());
			View v = inf.inflate(view, parent, false);
			TextView name = v.findViewById(R.id.editx);
			String p = (String)getItem(position);
			name.setText(p);
			return v;
		}

	}
}
