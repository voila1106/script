package com.voila.script;

import java.io.*;
import java.util.*;

public class util
{
	@Deprecated
	public static result shell(String cmd)
	{
		return shell(cmd,false);
	}

	public static result shell(String cmd,boolean su)
	{
		return shell(new String[]{cmd},su);
	}

	@Deprecated
	public static result shell(String[] cmd)
	{
		return shell(cmd,false);
	}

	public static result shell(String[] cmd,boolean su)
	{
		try{
			Process p = Runtime.getRuntime().exec(su?"su":"sh");
			DataOutputStream os=new DataOutputStream(p.getOutputStream());
			for(String t:cmd)
			{
				os.writeBytes(t+"\n");
			}
			os.writeBytes("exit\n");
			os.flush();

			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader ebr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line;
			StringBuilder sb = new StringBuilder();
			StringBuilder esb = new StringBuilder();
			while((line = br.readLine()) != null)
				sb.append(line).append("\n");
			while((line = ebr.readLine()) != null)
				esb.append(line).append("\n");
			br.close();
			os.close();
			ebr.close();
			p.waitFor();
			p.destroy();
			System.err.println(esb.toString());
			if(esb.toString().isEmpty())
				return new result(true,sb.toString());
			else if(sb.toString().isEmpty())
				return new result(false,"",esb.toString());
			else
				return new result(true,sb.toString(),esb.toString());
		}catch(Exception e)
		{
			e.printStackTrace();
			return new result(false,"",e.toString());
		}
	}

	public static boolean writeFile(File file, String content, boolean append)
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(file, append);
			fos.write(content.getBytes());
			fos.close();
			return true;
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static boolean writeFile(String path, String content, boolean append)
	{
		return writeFile(new File(path), content, append);
	}

	public static String readFile(File file)
	{
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine()) != null)
			{
				sb.append(line).append("\n");
			}
			br.close();
			return sb.toString();
		}catch(Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}

	public static String readFile(String path)
	{
		return readFile(new File(path));
	}


	public static result delete(String[] files)
	{
		ArrayList<String> execList = new ArrayList<>();
		for(String t : files)
		{
			if(t.isEmpty())
				continue;
			execList.add("rm -rf " + t);
		}
		return util.shell(execList.toArray(new String[0]), false);
	}
}
class result
{
	public final boolean success;
	public final String ret;
	public final String err;

	public result(boolean success, String ret, String err)
	{
		this.success = success;
		this.ret = ret;
		this.err = err;
	}

	public result(boolean success, String ret)
	{
		this.success = success;
		this.ret = ret;
		this.err="";
	}

	@Override
	public String toString()
	{
		return "result{\n" +
			"success=" + success +
			"\nret=\n" + ret +
			" \nerr=\n" + err +
			"\n}";
	}
}