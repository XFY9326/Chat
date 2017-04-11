package tool.xfy9326.chat;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ChatSettings extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.chat_global_settings);
	}
	
}
