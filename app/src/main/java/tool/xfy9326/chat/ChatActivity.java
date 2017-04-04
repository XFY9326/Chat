package tool.xfy9326.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
import android.app.Notification;

//主界面

public class ChatActivity extends Activity {
	 private String PassWord;
	 private String User;
	 private String DefaultString = "Welcome to ChatRoom (Made By XFY9326)";
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

	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  setContentView(R.layout.activity_chat);
		  mSp = PreferenceManager.getDefaultSharedPreferences(this);
		  mSpEditor = mSp.edit();
		  ViewSet();
		  pushText(true, DefaultString);
		  pushText(true, "Local IP> " + NetWorkMethod.getLocalIP(this));
		  Settings();
	 }

	 //初始数据设置
	 private void Settings() {
		  LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		  View layout = inflater.inflate(R.layout.dialog_settings, null);
		  final EditText usertext = (EditText) layout.findViewById(R.id.edittext_username);
		  usertext.setText(mSp.getString("UserName", "User"));
		  final EditText porttext = (EditText) layout.findViewById(R.id.edittext_port);
		  porttext.setText(mSp.getString("Port", "51030"));
		  final EditText iptext = (EditText) layout.findViewById(R.id.edittext_serverip);
		  iptext.setText(mSp.getString("RemoteIP", NetWorkMethod.getLocalIP(this)));
		  final EditText pwtext = (EditText) layout.findViewById(R.id.edittext_password);
		  pwtext.setText(mSp.getString("PassWord", "PassWord"));
		  AlertDialog.Builder set = new AlertDialog.Builder(this);
		  set.setTitle("Settings");
		  set.setCancelable(false);
		  set.setView(layout);
		  set.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface d, int i) {
						 String ip = iptext.getText().toString();
						 String user = usertext.getText().toString();
						 String port = porttext.getText().toString();
						 String pw = pwtext.getText().toString();
						 if (!ip.isEmpty() && NetWorkMethod.isIPCorrect(ip)) {
							  if (!port.isEmpty() && Integer.valueOf(port) >= 5000 && Integer.valueOf(port) <= 60000) {
								   if (!NetWorkMethod.isPortUsed(Integer.valueOf(port))) {
										if (!user.isEmpty()) {
											 if (!pw.isEmpty()) {
												  String localnet = NetWorkMethod.getLocalIP(ChatActivity.this);
												  User = user + " [" + localnet.substring(localnet.lastIndexOf(".") + 1) + "]";
												  RemoteIP.add(ip);
												  Port = Integer.valueOf(port);
												  PassWord = pw;

												  mSpEditor.putString("UserName", user);
												  mSpEditor.putString("Port", port);
												  mSpEditor.putString("RemoteIP", ip);
												  mSpEditor.putString("PassWord", pw);
												  mSpEditor.apply();

												  pushText(true, "Port> " + port);
												  pushText(true, "User Name> " + user);
												  NetSet();
												  msgSend(MessageMethod.buildSystemText(User + " is online now (" + NetWorkMethod.getLocalIP(ChatActivity.this) + ")"));
											 } else {
												  ToastShow("PassWord Error!");
												  ChatActivity.this.Settings();
											 }
										} else {
											 ToastShow("User Name Error!");
											 ChatActivity.this.Settings();
										}
								   } else {
										ToastShow("This Port has been used!\nClose this app completely may solve it.");
										ChatActivity.this.Settings();
								   }
							  } else {
								   ToastShow("Server Port Error!");
								   ChatActivity.this.Settings();
							  }
						 } else {
							  ToastShow("NetWorkServer Setting Error!");
							  ChatActivity.this.Settings();
						 }
					}
			   });
		  set.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
		  chatinfotext = (TextView) findViewById(R.id.textview_chatinfo);
		  chattext = (TextView) findViewById(R.id.textview_chat);
		  chatscroll = (ScrollView) findViewById(R.id.scroll_chat);
		  NotifyBuilder = MessageMethod.getNotifyBuilder(this);
		  Button send = (Button) findViewById(R.id.button_send);
		  send.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
                         String str = sendtext.getText().toString().trim();
						 if (!str.isEmpty()) {
							  if (str.startsWith("/")) {
								   //命令
								   CommandHandler(str.substring(1));
							  } else {
								   //信息
								   if (secretChatMode) {
										//私聊
                                        str = MessageMethod.buildText(User, str);
										pushText(false, "<font color='#87CEFA'>" + str + "</font>");
										NetWorkClient.Send(secretChatUser, str, Config.TYPE_SECRET_CHAT);
								   } else {
										//正常发送
										if (str.contains("@")) {
											 //提醒
											 Pattern pat = Pattern.compile("(\\@)(\\S+)");
											 Matcher mat = pat.matcher(str);
											 ArrayList<String> alert = new ArrayList<String>();
											 while (mat.find()) {
												  String originget = mat.group(0).toString();
												  if (!originget.isEmpty() && originget != " ") {
													   String get = originget.substring(1).trim();
													   if (!get.contains("@")) {
															get = MessageMethod.fixIP(get, ChatActivity.this);
															if (NetWorkMethod.isIPCorrect(get)) {
																 alert.add(get);
																 str = str.replace(originget, "<font color='#FF8C00'>" + originget + "</font>");
															} else {
																 ToastShow("Alert IP Format '" + originget + "' Error!");
															}
													   } else {
															ToastShow("Alert Format '" + originget + "' Error!");
													   }
												  }
											 }
											 NetWorkClient.Send(alert, "NULL", Config.TYPE_ALERT_USER);
										}
										str = MessageMethod.buildText(User, str);
										pushText(false, str);
										msgSend(str);
								   }
							  }
							  sendtext.setText("");
						 } else {
							  ToastShow("Msg Error!");
						 }
					}
			   });
	 }

	 //网络设置
	 private void NetSet() {
		  checkNetWorkServer();
		  //网络数据处理
		  NetWorkServerHandler = new Handler(){
			   @Override
			   public void handleMessage(Message msg) {
					if (msg.what == 0 && msg.getData() == null) {
						 ToastShow("Receive Data Error!");
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
							  String ReloadIP = bundle.getString("RESULT");
							  RemoteIP.remove(ReloadIP);
						 } else if (msg.what == Config.TYPE_ALERT_USER) {
							  //提醒功能
							  SystemMethod.vibrateAlert(ChatActivity.this);
						 } else if (msg.what == Config.TYPE_SECRET_CHAT) {
							  //私聊功能
							  String secretmsg = bundle.getString("RESULT");
							  pushText(false, "<font color='#87CEFA'>" + secretmsg + "</font>");
						 }
						 //接收信息的IP不在列表时发起同步
						 String ReloadIP = bundle.getString("IP").substring(1);
						 if (!ReloadIP.equalsIgnoreCase(NetWorkMethod.getLocalIP(ChatActivity.this)) && !ReloadIP.equalsIgnoreCase("127.0.0.1") && !RemoteIP.contains(ReloadIP)) {
							  RemoteIP.add(ReloadIP);
							  ArrayList<String> users = new ArrayList<String>();
							  users.addAll(RemoteIP);
							  users.add(NetWorkMethod.getLocalIP(ChatActivity.this));
							  NetWorkClient.Send(RemoteIP, users.toString(), Config.TYPE_USERLIST);
						 }
					}
			   }
		  };
		  //Socket服务器线程
		  NetWorkServer = new SocketServerHandler(Port, NetWorkServerHandler, PassWord);
		  NetWorkServer.start();
		  //Socket客户端实例
		  NetWorkClient = new SocketClientHandler(Port, PassWord);
		  //询问IP列表
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
						 info = MessageMethod.buildSystemText("Server has not start yet");
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
					pushText(false, "<font color='green'>" + info + "</font>");
					break;
			   case "talk":
					//私聊
					String secretmode = originalCmd.substring(cmd.length() + 1);
					if (secretmode.equalsIgnoreCase("off")) {
						 pushText(false, MessageMethod.buildSystemText("Secret Mode is Off"));
						 secretChatUser = "";
						 secretChatMode = false;
					} else {
						 secretmode = MessageMethod.fixIP(secretmode, this);
						 if (NetWorkMethod.isIPCorrect(secretmode)) {
							  pushText(false, MessageMethod.buildSystemText("Secret Mode is On (" + secretmode + ")"));
							  secretChatUser = secretmode;
							  secretChatMode = true;
						 } else {
							  ToastShow("IP Format Error!");
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
					pushText(false, MessageMethod.buildSystemText("Unknown Command '" + originalCmd + "'"));
					break;
		  }
	 }

	 //网络检测与提示
	 private boolean checkNetWorkServer() {
		  if (NetWorkMethod.isWifiConnected(this)) {
			   if (NetWorkMethod.isVpnUsed()) {
					ToastShow("You'd better close VPN!");
					return false;
			   } else {
					if (NetWorkMethod.isWifiApEnabled(this)) {
						 ToastShow("You'd better use Wifi Environment but not AP Environment!");
						 return false;
					} else {
						 return true;
					}
			   }
		  } else {
			   ToastShow("Wifi Connect Error!");
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
			   if (!SystemMethod.isTopActivity(ChatActivity.this, getPackageName())) {
					NoReadNum ++;
					MessageMethod.NotifyMsg(ChatActivity.this, Html.fromHtml(str), NotifyBuilder, NoReadNum);
			   }
		  }
		  chatscroll.post(new Runnable() {
					@Override
					public void run() {
						 chatscroll.fullScroll(View.FOCUS_DOWN);
					}
			   });
	 }

	 //发送对话信息
	 private void msgSend(String str) {
		  NetWorkClient.Send(RemoteIP, str, Config.TYPE_MSG);
	 }

	 //显示Toast
	 private void ToastShow(String str) {
		  Toast.makeText(ChatActivity.this, str, Toast.LENGTH_SHORT).show();
	 }

	 //获取用户数量
	 private int getUserCount() {
		  if (RemoteIP.size() == 1) {
			   if (RemoteIP.get(0).equals("127.0.0.1") || RemoteIP.get(0).equals(NetWorkMethod.getLocalIP(this))) {
					return 1;
			   } else {
					return 2;
			   }
		  } else {
			   return RemoteIP.size() + 1;
		  }
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

	 private void CloseSocketClientConnect() {
		  if (NetWorkClient != null) {
			   NetWorkClient.Close();
			   NetWorkClient = null;
		  }
	 }

	 //发送离线信息并同步删除本机IP
	 private void unregisterServer() {
		  msgSend(MessageMethod.buildSystemText(User + " is offline now (" + NetWorkMethod.getLocalIP(this) + ")"));
		  NetWorkClient.Send(RemoteIP, NetWorkMethod.getLocalIP(this), Config.TYPE_RELOAD_USERLIST);
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
		  back.setTitle("Attention");
		  back.setMessage("Do you really want to close it?");
		  back.setPositiveButton("Done", new DialogInterface.OnClickListener() {
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
		  back.setNegativeButton("Cancel", null);
		  back.show();
	 }
}
