package tool.xfy9326.chat;

import android.app.Application;
import tool.xfy9326.chat.Methods.CrashHandler;

public class ChatApplication extends Application {
	@Override
	public void onCreate() {
		CrashHandler.get().Catch(this, getExternalFilesDir(null).getAbsolutePath().toString());
		super.onCreate();
	}
}
