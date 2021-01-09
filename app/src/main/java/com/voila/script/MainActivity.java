package com.voila.script;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.os.Environment;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.*;

import java.io.*;
import java.lang.*;
import java.util.*;

@SuppressWarnings("all")
public class MainActivity extends AppCompatActivity
{
	ListView list;
	public static String path = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static String fscp;
	public static String log;

	File dir = new File(path);
	File currp = new File(path);
	int[] pos = new int[50];
	int level = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		list = findViewById(R.id.list);
		fscp = getFilesDir().getAbsolutePath() + "/clean";
		log = getCacheDir().getAbsolutePath() + "/log.txt";
//		fscp="/data/local/tmp/clean";

		loadList();

		list.setOnItemClickListener((parent, view, position, id) ->
		{
			TextView it = view.findViewById(R.id.absname);
			String p = it.getText() + "\n";
			if(isItemExists(p.trim()))
				toast.makeText(MainActivity.this, "已存在！", 0).show();
			else
			{
				util.writeFile(fscp, p, true);
				toast.makeText(MainActivity.this, "添加成功", 0).show();
			}

		});

		list.setOnItemLongClickListener((parent, view, position, id) ->
		{
			TextView it = view.findViewById(R.id.name);
			TextView ait = view.findViewById(R.id.absname);
			if(new File((String)ait.getText()).isFile())
				return false;
			pos[level++] = list.getFirstVisiblePosition();
			String p = it.getText() + "";
			currp = Objects.requireNonNull(currp.listFiles(pathname ->
				pathname.getName().equals(p)))[0];
			loadList(currp);
			return false;
		});

		File script = new File(fscp);
		File logf = new File(log);
		if(!script.exists() || util.readFile(script).trim().isEmpty())
		{
			try
			{
				script.createNewFile();
				util.writeFile(script, "|", false);
			}catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		if(!logf.exists())
		{
			try
			{
				logf.createNewFile();
			}catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void loadList(File d)
	{
		File[] fs = d.listFiles();
		try
		{
			Arrays.sort(fs, Comparator.comparing(o -> o.getName().toLowerCase()));
			iAdapter adapter = new iAdapter(this, R.layout.entity, fs);
			list.setAdapter(adapter);
		}catch(Exception e)
		{
			e.printStackTrace();
			list.setAdapter(null);
		}
	}

	private void loadList()
	{
		loadList(path);
	}

	private void loadList(String p)
	{
		loadList(new File(p));
	}

	private boolean isItemExists(String item)
	{
		String[] its = util.readFile(fscp).split("\\|")[1].split("\n");
		for(String t : its)
		{
			if(t.trim().equals(item))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if((keyCode == KeyEvent.KEYCODE_BACK))
		{
			if(!currp.equals(dir))
			{
				currp = currp.getParentFile();
				loadList(currp);
				list.setSelection(pos[--level]);
			}
			return false;
		}else
		{
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inf = getMenuInflater();
		inf.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.edit:
				startActivity(new Intent(this, EditActivity.class));
				return true;
			case R.id.refresh:
				loadList(currp);
				return true;
			case R.id.run:
				run();
				return true;
			case R.id.log:
				startActivity(new Intent(this, LogActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void run()
	{
		currp = dir;
		loadList();
		ProgressDialog dlg = ProgressDialog.show(this, "", "正在清理");
		long befspace = dir.getUsableSpace();
		new Thread(() ->
		{
			StringBuilder sb = new StringBuilder("\n" + new Date().toString());
			sb.append("\n");
			try
			{
				String[] delList = util.readFile(fscp).split("\\|")[1].trim().split("\n");
				ArrayList<String> execList = new ArrayList<>();
				for(String t : delList)
				{
					if(t.isEmpty())
						continue;
					if(new File(t).exists())
						sb.append(t).append("\n");
					if(t.endsWith("/Android"))
					{
						util.shell("rm -rf " + t, true);
						continue;
					}
					execList.add("rm -rf " + t);
				}
				util.shell(execList.toArray(new String[0]), false);

				dlg.setMessage("正在查找删除");
				ArrayList<String> fList = new ArrayList<>(Arrays.asList(util.readFile(fscp)
					.split("\\|")[0].trim().split("\n")));
				fList.removeIf(String::isEmpty);
				if(fList.size()!=0)
				{
					StringBuilder fcmd=new StringBuilder("find "+path+"/ ");
					for(int i = 0; i < fList.size(); i++)
					{
						if(i!=0)
							fcmd.append(" -or ");
						fcmd.append("-name ").append('\"').append(fList.get(i)).append('\"');
					}
					String ret=util.shell(fcmd.toString(),false).ret.trim();
					sb.append(ret).append('\n');

					ArrayList<String> rmList=new ArrayList<>(Arrays.asList(ret.trim().split("\n")));
					rmList.removeIf(String::isEmpty);
					util.delete(rmList.toArray(new String[0]));
				}


				double space = dir.getUsableSpace() - befspace;
				String[] units = {"KB", "MB", "GB"};
				String unit = "B";
				for(int i = 0; Math.abs(space) > 1024; i++)
				{
					space /= 1024;
					unit = units[i];
				}
				double finalSpace = Double.parseDouble(String.format("%.2f", space));
				String finalUnit = unit;
				runOnUiThread(() ->
					toast.makeText(MainActivity.this, "清理完成\n释放了" + finalSpace + finalUnit, 1).show());

			}catch(Exception e)
			{
				e.printStackTrace();
				runOnUiThread(() ->
					toast.makeText(MainActivity.this, "失败", 1).show());

			}finally
			{
				util.writeFile(log, sb.toString(), true);
			}
			dlg.dismiss();
			runOnUiThread(this::loadList);
		}).start();
	}

	class iAdapter extends ArrayAdapter
	{
		private int view;

		public iAdapter(@NonNull Context context, int resource, @NonNull Object[] objects)
		{
			super(context, resource, objects);
			view = resource;
		}

		@NonNull
		@Override
		public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
		{
			LayoutInflater inf = LayoutInflater.from(getContext());
			View v = inf.inflate(view, parent, false);
			TextView name = v.findViewById(R.id.name);
			TextView absn = v.findViewById(R.id.absname);
			ImageView type = v.findViewById(R.id.img);
			File p = (File)getItem(position);
			name.setText(p.getName());
			absn.setText(p.getAbsolutePath());
			if(p.isDirectory())
				type.setImageResource(R.drawable.dir);
			else
				type.setImageResource(R.drawable.file);
			return v;
		}

	}
}
