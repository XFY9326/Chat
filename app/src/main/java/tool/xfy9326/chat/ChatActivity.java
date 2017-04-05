package tool.xfy9326.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tool.xfy9326.chat.ChatActivity;
import tool.xfy9326.chat.Methods.Config;
import tool.xfy9326.chat.Methods.MessageMethod;
import tool.xfy9326.chat.Methods.NetWorkMethod;
import tool.xfy9326.chat.Methods.SystemMethod;
import tool.xfy9326.chat.Net.SocketClientHandler;
import tool.xfy9326.chat.Net.SocketServerHandler;
import tool.xfy9326.chat.Tools.FormatArrayList;

//主界面

public class ChatActivity extends Activity {
	 private String PassWord;
	 private String User;
	 private ArrayList<String> RemoteIP = new ArrayList<String>();
	 private int Port;
	 private EditText sendtext;
	 private TextView chatinfotext;
	 private TextView chattext;
	 private ScrollView chatscroll;
	 private SocketServerHandler NetWorkServer;
	 private SocketClientHandler NetWorkClient;
	 private Handler NetWorkServerHandler;
	 private SharedPreferences mSp;
	 private SharedPreferences.Editor mSpEditor;
	 private String CHATTEXT = "";
	 private String secretChatUser = "";
	 private boolean secretChatMode = false;
	 private Notification.Builder NotifyBuilder = null;
	 private int NoReadNum = 0;
	 private boolean ServerRegistered = false;

	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  setContentView(R.layout.activity_chat);
		  mSp = PreferenceManager.getDefaultSharedPreferences(this);
		  mSpEditor = mSp.edit();
		  ViewSet();
		  pushText(true, getString(R.string.msg_welcome));
		  pushText(true, getString(R.string.ip_local) + "> " + NetWorkMethod.getLocalIP(this));
		  Settings();
	 }

	 //初始数据设置
	 private void Settings() {
		  LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		  View layout = inflater.inflate(R.layout.dialog_settings, null);
		  final EditText usertext = (EditText) layout.findViewById(R.id.edittext_username);
		  usertext.setText(mSp.getString(Config.DATA_USERNAME, Config.DATA_DEFAULT_USERNAME));
		  final EditText porttext = (EditText) layout.findViewById(R.id.edittext_port);
		  porttext.setText(mSp.getString(Config.DATA_PORT, Config.DATA_DEFAULT_PORT));
		  final EditText iptext = (EditText) layout.findViewById(R.id.edittext_serverip);
		  iptext.setText(mSp.getString(Config.DATA_SERVERIP, NetWorkMethod.getLocalIP(this)));
		  final EditText pwtext = (EditText) layout.findViewById(R.id.edittext_password);
		  pwtext.setText(mSp.getString(Config.DATA_PASSWORD, Config.DATA_DEFAULT_PASSWORD));
		  AlertDialog.Builder set = new AlertDialog.Builder(this);
		  set.setTitle(R.string.settings);
		  set.setCancelable(false);
		  set.setView(layout);
		  set.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface d, int i) {
						 String ip = iptext.getText().toString();
						 String user = usertext.getText().toString();
						 String port = porttext.getText().toString();
						 String pw = pwtext.getText().toString();
						 if (NetWorkMethod.isIPCorrect(ip)) {
							  if (!port.isEmpty() && Integer.valueOf(port) >= 5000 && Integer.valueOf(port) <= 60000) {
								   if (!NetWorkMethod.isPortUsed(Integer.valueOf(port))) {
										if (!user.isEmpty()) {
											 if (!pw.isEmpty()) {
												  String localnet = NetWorkMethod.getLocalIP(ChatActivity.this);
												  User = user + " [" + localnet.substring(localnet.lastIndexOf(".") + 1) + "]";
												  if (!ip.isEmpty()) {
													   RemoteIP.add(ip);
												  }
												  Port = Integer.valueOf(port);
												  PassWord = pw;

												  mSpEditor.putString(Config.DATA_USERNAME, user);
												  mSpEditor.putString(Config.DATA_PORT, port);
												  mSpEditor.putString(Config.DATA_SERVERIP, ip);
												  mSpEditor.putString(Config.DATA_PASSWORD, pw);
												  mSpEditor.apply();

												  pushText(true, getString(R.string.port) + "> " + port);
												  pushText(true, getString(R.string.username) + "> " + user);
												  NetWorkSet();
											 } else {
												  ToastShow(R.string.err_password);
												  ChatActivity.this.Settings();
											 }
										} else {
											 ToastShow(R.string.err_username);
											 ChatActivity.this.Settings();
										}
								   } else {
										ToastShow(R.string.err_port_used);
										ChatActivity.this.Settings();
								   }
							  } else {
								   ToastShow(R.string.err_server_port);
								   ChatActivity.this.Settings();
							  }
						 } else {
							  ToastShow(R.string.err_server_ip);
							  ChatActivity.this.Settings();
						 }
					}
			   });
		  set.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface d, int i) {
						 CloseApp();
					}
			   });
		  set.show();
	 }

	 //布局设置
	 private void ViewSet() {
		  sendtext = (EditText) findViewById(R.id.edittext_send);
		  sendtext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView text, int id, KeyEvent event) {
						 if (id == EditorInfo.IME_ACTION_SEND) {
							  SendText();
							  return true;
						 }
						 return false;
					}
			   });
		  chatinfotext = (TextView) findViewById(R.id.textview_chatinfo);
		  chattext = (TextView) findViewById(R.id.textview_chat);
		  chatscroll = (ScrollView) findViewById(R.id.scroll_chat);
		  NotifyBuilder = MessageMethod.getNotifyBuilder(this);
		  Button send = (Button) findViewById(R.id.button_send);
		  send.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
                         SendText();
					}
			   });
	 }

	 //发送输入的文字
	 private void SendText() {
		  String str = sendtext.getText().toString().trim();
		  if (!str.isEmpty()) {
			   if (str.length() <= 500) {
					if (str.startsWith("/")) {
						 //命令
						 CommandHandler(str.substring(1));
					} else {
						 //信息
						 if (secretChatMode) {
							  //私聊
							  str = MessageMethod.buildColorText(MessageMethod.buildText(User, str), Config.COLOR_SECRETCHAT);
							  pushText(false, str);
							  NetWorkClient.Send(secretChatUser, str, Config.TYPE_SECRET_CHAT);
						 } else {
							  //正常发送
							  if (str.contains("@")) {
								   //提醒
								   str = alertUser(str);
							  }
							  str = MessageMethod.buildText(User, str);
							  pushText(false, str);
							  msgSend(str);
						 }
					}
					sendtext.setText("");
			   } else {
					ToastShow(R.string.err_msg_toolong);
			   }
		  } else {
			   ToastShow(R.string.err_msg_empty);
		  }
	 }

	 //网络设置
	 private void NetWorkSet() {
		  //网络数据处理
		  NetWorkServerHandler = new Handler(){
			   @Override
			   public void handleMessage(Message msg) {
					if (msg.what == 0 && msg.getData() == null) {
						 ToastShow(R.string.err_data_receive);
					} else {
						 Bundle bundle = msg.getData();
						 if (msg.what == Config.TYPE_MSG) {
							  //接收信息并显示
							  pushText(false, bundle.getString("RESULT").toString());
						 } else if (msg.what == Config.TYPE_USERLIST) {
							  //接收IP列表
							  String LocalIP = NetWorkMethod.getLocalIP(ChatActivity.this);
							  ArrayList<String> users = FormatArrayList.StringToStringArrayList(bundle.getString("RESULT"));
							  int size = users.size();
							  for (int i = 0; i < size; i++) {
								   if (RemoteIP.contains(users.get(i)) || users.get(i).equals(LocalIP) || users.get(i).equals("127.0.0.1")) {
										users.remove(i);
										i--;
										size--;
								   }
							  }
							  RemoteIP.addAll(users);
							  registerServer();
						 } else if (msg.what == Config.TYPE_ASK_USERLIST) {
							  //接收询问IP列表的回复
							  if (RemoteIP.size() >= 1) {
								   if (!RemoteIP.get(0).equals(NetWorkMethod.getLocalIP(ChatActivity.this)) && !RemoteIP.get(0).equalsIgnoreCase("127.0.0.1")) {
										if (!RemoteIP.contains(bundle.getString("RESULT"))) {
											 RemoteIP.add(bundle.getString("RESULT"));
										}
										ArrayList<String> users = new ArrayList<String>();
										users.addAll(RemoteIP);
										users.add(NetWorkMethod.getLocalIP(ChatActivity.this));
										NetWorkClient.Send(RemoteIP, users.toString(), Config.TYPE_USERLIST);
								   }
							  }
						 } else if (msg.what == Config.TYPE_RELOAD_USERLIST) {
							  //用户列表删减
							  String[] Result = bundle.getString("RESULT").split("-");
							  RemoteIP.remove(Result[1].trim().toString());
							  pushText(false, MessageMethod.buildSystemText(getString(R.string.msg_system), Result[0] + " " + getString(R.string.warn_offline) + " (" + Result[1] + ")"));
						 } else if (msg.what == Config.TYPE_ALERT_USER) {
							  //提醒功能
							  if (bundle.getString("RESULT").equalsIgnoreCase(NetWorkMethod.getLocalIP(ChatActivity.this))) {
								   SystemMethod.vibrateAlert(ChatActivity.this);
							  }
						 } else if (msg.what == Config.TYPE_SECRET_CHAT) {
							  //私聊功能
							  String secretmsg = bundle.getString("RESULT");
							  pushText(false, secretmsg);
						 }
						 //接收信息的IP不在列表时发起同步
						 if (msg.what != Config.TYPE_RELOAD_USERLIST) {
							  String ReloadIP = bundle.getString("IP").substring(1);
							  if (!ReloadIP.equalsIgnoreCase(NetWorkMethod.getLocalIP(ChatActivity.this)) && !ReloadIP.equalsIgnoreCase("127.0.0.1") && !RemoteIP.contains(ReloadIP)) {
								   RemoteIP.add(ReloadIP);
								   ArrayList<String> users = new ArrayList<String>();
								   users.addAll(RemoteIP);
								   users.add(NetWorkMethod.getLocalIP(ChatActivity.this));
								   if (NetWorkClient != null) {
										NetWorkClient.Send(RemoteIP, users.toString(), Config.TYPE_USERLIST);
								   }
							  }
						 }
					}
			   }
		  };
		  checkNetWorkServer();
		  //Socket服务器线程
		  NetWorkServer = new SocketServerHandler(Port, NetWorkServerHandler, PassWord);
		  NetWorkServer.start();
		  //Socket客户端实例
		  NetWorkClient = new SocketClientHandler(Port, PassWord);
		  //同步用户列表
		  NetWorkClient.Send(RemoteIP, NetWorkMethod.getLocalIP(this), Config.TYPE_ASK_USERLIST);
	 }

	 //命令处理
	 private void CommandHandler(String cmd) {
		  String originalCmd = cmd;
		  if (cmd.indexOf(" ") >= 0) {
			   cmd = cmd.substring(0, cmd.indexOf(" "));
		  }
		  switch (cmd) {
			   case "info":
					//信息反馈
					String info ="";
					if (NetWorkServer == null) {
						 info = MessageMethod.buildSystemText(getString(R.string.msg_system), getString(R.string.warn_server_notstart));
					} else {
						 info = "--- Server Info ---" + "\n"
							  + "User Name: " + User + "\n"
							  + "Remote IP: " + RemoteIP.toString() + "\n"
							  + "User Count: " + getUserCount() + "\n"
							  + "Local IP: " + NetWorkMethod.getLocalIP(this) + "\n"
							  + "Port: " + Port + "\n"
							  + "PassWord: " + PassWord + "\n"
							  + "--- End of List ---";
					}
					pushText(false, MessageMethod.buildColorText(info, Config.COLOR_INFO));
					break;
			   case "talk":
					//私聊
					String secretmode = originalCmd.substring(cmd.length() + 1);
					if (secretmode.equalsIgnoreCase("off")) {
						 pushText(false, MessageMethod.buildSystemText(getString(R.string.msg_system), getString(R.string.warn_secretmode_off)));
						 secretChatUser = "";
						 secretChatMode = false;
					} else {
						 secretmode = MessageMethod.fixIP(secretmode, this);
						 if (NetWorkMethod.isIPCorrect(secretmode)) {
							  pushText(false, MessageMethod.buildSystemText(getString(R.string.msg_system), getString(R.string.warn_secretmode_on) + " (" + secretmode + ")"));
							  secretChatUser = secretmode;
							  secretChatMode = true;
						 } else {
							  ToastShow(R.string.err_ip_format);
						 }
					}
					break;
			   case "clear":
					//清空聊天记录
					CHATTEXT = "";
					chattext.setText("");
					break;
			   default:
					//错误提示
					pushText(false, MessageMethod.buildSystemText(getString(R.string.msg_system), getString(R.string.warn_unknown_command) + " '" + originalCmd + "'"));
					break;
		  }
	 }

	 //网络检测与提示
	 private boolean checkNetWorkServer() {
		  if (NetWorkMethod.isWifiConnected(this)) {
			   if (NetWorkMethod.isVpnUsed()) {
					ToastShow(R.string.err_vpn_connect);
					return false;
			   } else {
					return true;
			   }
		  } else {
			   if (NetWorkMethod.isOnlyMobileNetWork(this)) {
					ToastShow(R.string.err_moblie_connect);
			   } else {
					ToastShow(R.string.err_wifi_connect);
			   }
			   return false;
		  }
	 }

	 //信息推送到UI显示
	 private void pushText(boolean isinfo, String str) {
		  if (isinfo) {
			   String old = chatinfotext.getText().toString();
			   if (old == "") {
					chatinfotext.setText(str);
			   } else {
					String result = chatinfotext.getText().toString() + "\n" + str ;
					chatinfotext.setText(result);
			   }
		  } else {
			   if (CHATTEXT == "") {
					CHATTEXT = str;
			   } else {
					CHATTEXT = CHATTEXT  + "\n" + str;
			   }
			   if (CHATTEXT.split("\n").length > 200) {
					CHATTEXT = CHATTEXT.substring(CHATTEXT.indexOf("\n") + 2);
			   }
			   String result = CHATTEXT.replace("\n", "<br>");
			   chattext.setText(Html.fromHtml(result));
			   //应用不在前台时通知栏提示
			   if (!SystemMethod.isTopActivity(ChatActivity.this, getPackageName())) {
					NoReadNum ++;
					MessageMethod.NotifyMsg(ChatActivity.this, Html.fromHtml(str), NotifyBuilder, NoReadNum);
			   }
		  }
		  //屏幕滚动
		  chatscroll.post(new Runnable() {
					@Override
					public void run() {
						 chatscroll.fullScroll(View.FOCUS_DOWN);
					}
			   });
	 }

	 //发送对话信息
	 private void msgSend(String str) {
		  if (NetWorkClient != null) {
			   NetWorkClient.Send(RemoteIP, str, Config.TYPE_MSG);
		  }
	 }

	 //显示Toast
	 private void ToastShow(int id) {
		  Toast.makeText(ChatActivity.this, getString(id), Toast.LENGTH_SHORT).show();
	 }

	 //获取用户数量
	 private int getUserCount() {
		  if (RemoteIP.size() == 1) {
			   if (RemoteIP.get(0).equals(Config.IP_LOCALHOST) || RemoteIP.get(0).equals(NetWorkMethod.getLocalIP(this))) {
					return 1;
			   } else {
					return 2;
			   }
		  } else {
			   return RemoteIP.size() + 1;
		  }
	 }
	 
	 private boolean hasUser(String ip){
		  return RemoteIP.contains(ip.trim());
	 }

	 //提醒用户
	 private String alertUser(String str) {
		  Pattern pat = Pattern.compile("(\\@)(\\S+)");
		  Matcher mat = pat.matcher(str);
		  ArrayList<String> alert = new ArrayList<String>();
		  while (mat.find()) {
			   String originget = mat.group(0).toString();
			   if (!originget.isEmpty() && originget != " ") {
					String get = originget.substring(1).trim();
					if (!get.contains("@")) {
						 get = MessageMethod.fixIP(get, ChatActivity.this);
						 if (NetWorkMethod.isIPCorrect(get) && hasUser(get)) {
							  alert.add(get);
							  str = str.replace(originget, MessageMethod.buildColorText(originget, Config.COLOR_ALERTUSER));
						 }
					}
			   }
		  }
		  NetWorkClient.Send(alert, NetWorkMethod.getLocalIP(ChatActivity.this), Config.TYPE_ALERT_USER);
		  return str;
	 }

	 //关闭Socket服务
	 private void CloseSocketServerConnect() {
		  if (NetWorkServer != null) {
			   NetWorkServer.StopServer();
			   NetWorkServer = null;
		  }
		  if (NetWorkServerHandler != null) {
			   NetWorkServerHandler.removeCallbacksAndMessages(null);
		  }
	 }

	 //关闭Socket客户端
	 private void CloseSocketClientConnect() {
		  if (NetWorkClient != null) {
			   NetWorkClient.Close();
			   NetWorkClient = null;
		  }
	 }

	 //上线消息发送
	 private void registerServer() {
		  if (!ServerRegistered) {
			   ServerRegistered = true;
			   msgSend(MessageMethod.buildSystemText(getString(R.string.msg_system), User + " " + getString(R.string.warn_online) + " (" + NetWorkMethod.getLocalIP(ChatActivity.this) + ")"));
		  }
	 }

	 //离线同步删除本机IP
	 private void unregisterServer() {
		  NetWorkClient.Send(RemoteIP, User + "-" + NetWorkMethod.getLocalIP(this), Config.TYPE_RELOAD_USERLIST);
	 }

	 //退出
	 private void CloseApp() {
		  ChatActivity.this.finish();
	 }

	 @Override
	 protected void onResume() {
		  NoReadNum = 0;
		  super.onResume();
	 }

	 @Override
	 public void onBackPressed() {
		  AlertDialog.Builder back = new AlertDialog.Builder(this);
		  back.setTitle(R.string.attention);
		  back.setMessage(R.string.msg_close_app);
		  back.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int i) {
						 unregisterServer();
						 CloseApp();
						 CloseSocketClientConnect();
						 new Handler().postDelayed(new Runnable(){
								   @Override
								   public void run() {
										CloseSocketServerConnect();
								   }
							  }, 800);
					}
			   });
		  back.setNegativeButton(R.string.cancel, null);
		  back.show();
	 }
}
