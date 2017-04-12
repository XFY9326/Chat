package tool.xfy9326.chat.Methods;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	private Context mContext;
	private Map<String, String> infos = new HashMap<String, String>();
	private String Log = "";
	private String Info = "";
	private String outputpath;

	public static CrashHandler get() {
		return new CrashHandler();
	}

	public void Catch(Context context, String path) {
		this.outputpath = path;
		this.mContext = context;
		this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				new Thread(new Runnable() {
						@Override
						public void run() {
							Looper.prepare();
							Toast.makeText(mContext, "Creating Log ...", Toast.LENGTH_SHORT).show();
							Looper.loop();
						}
					}).start();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			String FileName = "CrashLog_" + stampToDate(new Date().getTime() + "");
			writefile(outputpath + FileName + ".txt", Info + "\n\n" + Log);
			System.exit(0);
		}
	}

	private boolean writefile(String path, String data) {
		try {
			File file = new File(path);
			pathset(path);
			byte[] Bytes = new String(data).getBytes();
			if (file.exists()) {
				if (file.isFile()) {
					OutputStream writer = new FileOutputStream(file);
					writer.write(Bytes);
					writer.close();
					return true;
				} else {
					return false;
				}
			} else {
				file.createNewFile();
				OutputStream writer = new FileOutputStream(file);
				writer.write(Bytes);
				writer.close();
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void pathset(String path) {
		String[] dirs = path.split("/");
		String pth = "";
		for (int i = 0;i < dirs.length;i++) {
			if (i != dirs.length - 1) {
				pth += "/" + dirs[i];
			}
		}
		File dir = new File(pth);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		collectDeviceInfo(mContext);
		saveCrashInfo(mContext, ex);
		return true;
	}

	private String getAppInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null" : pi.versionName;
				String versionCode = pi.versionCode + "";
				String installTime = stampToDate(pi.firstInstallTime + "");
				String lastupdatetime = stampToDate(pi.lastUpdateTime + "");
				String errortime = stampToDate(new Date().getTime() + "");
				String result = "VersionName = " + versionName + "\n" + "VersionCode = " + versionCode + "\n" + "ErrorTime = " + errortime + "\n" + "InstallTime = " + installTime + "\n" + "LastUpdateTime = " + lastupdatetime;
				result += "\n" + "SDK = " + Build.VERSION.SDK + "(" + Build.VERSION.SDK_INT + ")";
				return result;
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return "Unknown Application Version";
	}

	private void collectDeviceInfo(Context ctx) {
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName().toString(), field.get(null).toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String stampToDate(String s) {
		String res;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long lt = new Long(s);
		Date date = new Date(lt);
		res = simpleDateFormat.format(date);
		return res;
	}

	private void saveCrashInfo(Context ctx, Throwable ex) {
		String str = "";
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey().toString();
			String value = entry.getValue().toString();
			if (key.contains("java.lang.String") || value.contains("java.lang.String") || key.equalsIgnoreCase("UNKNOWN") || value.equalsIgnoreCase("unknown")) {
				continue;
			} else {

				str += key + " = " + value + " \n";
			}
		}
		str = getAppInfo(ctx) + "\n\n" + str;
		String result = ExToString(ex);
		Info = str;
		Log = result;
	}

	private String ExToString(Throwable ex) {
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		return writer.toString();
	}

}
