package tool.xfy9326.chat.Methods;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Vibrator;
import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.regex.Pattern;

public class SystemMethod {
	 //前台应用
	 public static boolean isTopActivity(Context context, String packageName) {
		  ActivityManager am = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		  List<ActivityManager.RunningAppProcessInfo> list = am.getRunningAppProcesses();
		  if (list.size() == 0) return false;
		  for (ActivityManager.RunningAppProcessInfo process:list) {
			   if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && process.processName.equals(packageName)) {
					return true;
			   }
		  }
		  return false;
	 }

	 //用户提醒
	 public static void vibrateAlert(Context ctx) {
		  Vibrator vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
		  vibrator.vibrate(250);
	 }

	 //CPU核心数获取
	 public static int getCpuNumCores() {
		  class CpuFilter implements FileFilter {
			   @Override
			   public boolean accept(File pathname) {
					if (Pattern.matches("cpu[0-9]", pathname.getName())) {
						 return true;
					}
					return false;
			   }      
		  }
		  try {
			   File dir = new File("/sys/devices/system/cpu/");
			   File[] files = dir.listFiles(new CpuFilter());
			   return files.length;
		  } catch (Exception e) {
			   e.printStackTrace();
			   return 1;
		  }
	 }

}
