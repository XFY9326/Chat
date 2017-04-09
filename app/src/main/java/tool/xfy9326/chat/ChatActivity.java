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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import tool.xfy9326.chat.View.ListViewAdapter;

//主界面

public class ChatActivity extends Activity {
	 private String PassWord;
	 private String User;
	 private ArrayList<String> RemoteIP = new ArrayList<String>();
	 private ArrayList<String> BlockIP = new ArrayList<String>();
	 private int Port;
	 private EditText sendtext;
	 private TextView chatinfotext;
	 private ListView chattext;
	 private ListViewAdapter chatadapter;
	 private SocketServerHandler NetWorkServer;
	 private SocketClientHandler NetWorkClient;
	 private Handler NetWorkServerHandler;
	 private SharedPreferences mSp;
	 private SharedPreferences.Editor mSpEditor;
	 private String secretChatUser = "";
	 private boolean secretChatMode = false;
	 private Notification.Builder NotifyBuilder = null;
	 private int NoReadNum = 0;
	 private boolean ServerRegistered = false;

	 private ArrayList<Integer> Types = new ArrayList<Integer>();
	 private ArrayList<String> Users = new ArrayList<String>();

	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  setContentView(R.layout.activity_chat);
		  mSp = PreferenceManager.getDefaultSharedPreferences(this);
		  mSpEditor = mSp.edit();
		  ViewSet();
		  pushInfoText(getString(R.string.msg_welcome));
		  pushInfoText(getString(R.string.ip_local) + "> " + NetWorkMethod.getLocalIP(this));
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
										if (!user.isEmpty() && !user.contains(">") && !user.contains("-") && !user.contains("_") && !user.contains("[") && !user.contains("]")) {
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

												  pushInfoText(getString(R.string.port) + "> " + port);
												  pushInfoText(getString(R.string.username) + "> " + user);
												  NetWorkSet();
												  pushText(null, getString(R.string.msg_join), MessageMethod.getMsgTime(), Config.MSGTYPE_SYSTEM);
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
						 CloseApp(false);
					}
			   });
		  set.show();
	 }

	 //布局设置
	 private void ViewSet() {
		  sendtext = (EditText) findViewById(R.id.edittext_send);
		  chatinfotext = (TextView) findViewById(R.id.textview_chatinfo);
		  chattext = (ListView) findViewById(R.id.listview_chat);
		  chatadapter = new ListViewAdapter(this, Types, Users, RemoteIP);
		  chattext.setAdapter(chatadapter);
		  chattext.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView adapter, View view, int id, long l) {
						 if (Types.get(id) == Config.TYPE_LISTVIEW_CHAT_MSG_OTHERS) {
							  String name = Users.get(id);
							  String ip = name.substring(name.indexOf("[") + 1, name.lastIndexOf("]")).toString().trim();
							  sendtext.append("@" + ip + " ");
							  return true;
						 }
						 return false;
					}
			   });
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
							  pushText(User, str, MessageMethod.getMsgTime(), Config.MSGTYPE_MAIN_SECRET);
							  NetWorkClient.Send(secretChatUser, MessageMethod.msgBuilder(User, NetWorkMethod.getLocalIP(ChatActivity.this), str), Config.TYPE_SECRET_CHAT);
						 } else {
							  //正常发送
							  if (str.contains("@")) {
								   //提醒
								   str = alertUser(str);
							  }
							  pushText(User, str, MessageMethod.getMsgTime(), Config.MSGTYPE_MAIN);
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
						 //错误消息提示
						 ToastShow(R.string.err_data_receive);
					} else {
						 Bundle bundle = msg.getData();
						 if (!BlockIP.contains(bundle.getString(Config.DATA_RECEIVE_IP).substring(1))) {
							  if (msg.what == Config.TYPE_MSG) {
								   //接收信息并显示
								   String[] str = bundle.getStringArray(Config.DATA_RECEIVE_INFO);
								   pushText(str[2], str[3], str[0], Config.MSGTYPE_OTHERS);
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
										if (!RemoteIP.get(0).equals(NetWorkMethod.getLocalIP(ChatActivity.this)) && !RemoteIP.get(0).equalsIgnoreCase(Config.IP_LOCALHOST)) {
											 if (!RemoteIP.contains(bundle.getString(Config.DATA_RECEIVE))) {
												  RemoteIP.add(bundle.getString(Config.DATA_RECEIVE));
											 }
											 ArrayList<String> users = new ArrayList<String>();
											 users.addAll(RemoteIP);
											 users.add(NetWorkMethod.getLocalIP(ChatActivity.this));
											 NetWorkClient.Send(RemoteIP, users.toString(), Config.TYPE_USERLIST);
										}
								   }
							  } else if (msg.what == Config.TYPE_RELOAD_USERLIST) {
								   //用户列表删减
								   String[] Result = bundle.getString(Config.DATA_RECEIVE).split("-");
								   RemoteIP.remove(Result[1].trim().toString());
								   pushText(User, Result[0] + " " + getString(R.string.warn_offline) + " (" + Result[1] + ")", MessageMethod.getMsgTime(), Config.MSGTYPE_SYSTEM);
							  } else if (msg.what == Config.TYPE_ALERT_USER) {
								   //提醒功能
								   if (bundle.getString(Config.DATA_RECEIVE).equalsIgnoreCase(NetWorkMethod.getLocalIP(ChatActivity.this))) {
										SystemMethod.vibrateAlert(ChatActivity.this);
								   }
							  } else if (msg.what == Config.TYPE_SECRET_CHAT) {
								   //私聊功能
								   String[] str = bundle.getStringArray(Config.DATA_RECEIVE_INFO);
								   pushText(str[2], str[3], str[0], Config.MSGTYPE_OTHERS_SECRET);
							  } else if (msg.what == Config.TYPE_SYSTEM) {
								   //上线提示
								   String[] str = bundle.getStringArray(Config.DATA_RECEIVE_INFO);
								   if (str[3].equalsIgnoreCase(Config.MSGTYPE_SYSTEM_ONLINE)) {
										pushText(str[2], str[2] + " " + getString(R.string.warn_online) + " (" + str[1] + ")", str[0] , Config.MSGTYPE_SYSTEM);
								   }
							  }
							  //接收信息的IP不在列表时发起同步
							  if (msg.what != Config.TYPE_RELOAD_USERLIST) {
								   String ReloadIP = bundle.getString(Config.DATA_RECEIVE_IP).substring(1);
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
						 info = getString(R.string.warn_server_notstart);
					} else {
						 info = "--- Server Info ---" + "\n"
							  + "User Name: " + User + "\n"
							  + "Remote IP: " + RemoteIP.toString() + "\n"
							  + "User Count: " + getUserCount() + "\n"
							  + "Local IP: " + NetWorkMethod.getLocalIP(this) + "\n"
							  + "Port: " + Port + "\n"
							  + "PassWord: " + PassWord + "\n"
							  + "Block IP: " + BlockIP.toString() + "\n"
							  + "--- End of List ---";
					}
					pushText(null, info, MessageMethod.getMsgTime(), Config.MSGTYPE_SYSTEM_INFO);
					break;
			   case "talk":
					//私聊
					if (originalCmd.length() > 5) {
						 String secretmode = originalCmd.substring(cmd.length() + 1);
						 if (secretmode.equalsIgnoreCase("off")) {
							  pushText(User, getString(R.string.warn_secretmode_off), MessageMethod.getMsgTime(), Config.MSGTYPE_SYSTEM);
							  secretChatUser = "";
							  secretChatMode = false;
						 } else {
							  secretmode = MessageMethod.fixIP(secretmode, this);
							  if (NetWorkMethod.isIPCorrect(secretmode)) {
								   pushText(User, getString(R.string.warn_secretmode_on) + " (" + secretmode + ")", MessageMethod.getMsgTime(), Config.MSGTYPE_SYSTEM);
								   secretChatUser = secretmode;
								   secretChatMode = true;
							  } else {
								   ToastShow(R.string.err_ip_format);
							  }
						 }
					}
					break;
			   case "connect":
					//重连服务
					if (originalCmd.length() > 8) {
						 String ip = originalCmd.substring(cmd.length() + 1);
						 ip = MessageMethod.fixIP(ip, this);
						 if (NetWorkMethod.isIPCorrect(ip)) {
							  RemoteIP.clear();
							  RemoteIP.add(ip);
							  NetWorkClient.Send(ip, NetWorkMethod.getLocalIP(this), Config.TYPE_ASK_USERLIST);
						 } else {
							  ToastShow(R.string.err_ip_format);
						 }
					}
					break;
			   case "block":
					//屏蔽
					if (originalCmd.length() > 9) {
						 String blockmode = originalCmd.substring(cmd.length() + 1);
						 if (blockmode.contains(" ")) {
							  blockmode = blockmode.substring(0, blockmode.indexOf(" "));
							  if (blockmode.equalsIgnoreCase("add")) {
								   String blockip = originalCmd.substring(cmd.length() + blockmode.length() + 2);
								   blockip = MessageMethod.fixIP(blockip, this);
								   if (NetWorkMethod.isIPCorrect(blockip)) {
										if (!BlockIP.toString().contains(blockip)) {
											 BlockIP.add(blockip.trim());
											 pushText(null,blockip + getString(R.string.msg_block), MessageMethod.getMsgTime(), Config.MSGTYPE_SYSTEM);
										}
								   } else {
										ToastShow(R.string.err_ip_format);
								   }
							  } else if (blockmode.equalsIgnoreCase("del")) {
								   String blockip = originalCmd.substring(cmd.length() + blockmode.length() + 2);
								   blockip = MessageMethod.fixIP(blockip, this);
								   if (NetWorkMethod.isIPCorrect(blockip)) {
										if (BlockIP.toString().contains(blockip)) {
											 BlockIP.remove(blockip.trim());
											 pushText(null,blockip + getString(R.string.msg_unblock), MessageMethod.getMsgTime(), Config.MSGTYPE_SYSTEM);
										}
								   } else {
										ToastShow(R.string.err_ip_format);
								   }
							  }
						 }
					}
					break;
			   default:
					//错误提示
					pushText(null, getString(R.string.warn_unknown_command) + " '" + originalCmd + "'", MessageMethod.getMsgTime(), Config.MSGTYPE_SYSTEM);
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

	 //配置信息推送到UI显示
	 private void pushInfoText(String str) {
		  String old = chatinfotext.getText().toString();
		  if (old == "") {
			   chatinfotext.append(str);
		  } else {
			   String result = old + "\n" + str ;
			   chatinfotext.setText(result);
		  }
	 }

	 //聊天信息推送到UI显示
	 private void pushText(String user, String str, String time, int type) {
		  if (user == null) {
			   user = Config.DATA_DEFAULT_USERNAME;
		  }
		  if (type == Config.MSGTYPE_SYSTEM) {
			   chatadapter.addSystemMessage(str, time, Config.COLOR_SYSTEM);
		  } else if (type == Config.MSGTYPE_SYSTEM_INFO) {
			   chatadapter.addSystemMessage(str, time, Config.COLOR_INFO);
		  } else if (type == Config.MSGTYPE_MAIN) {
			   chatadapter.addMainMessage(str, time, null);
		  } else if (type == Config.MSGTYPE_MAIN_SECRET) {
			   chatadapter.addMainMessage(str, time, Config.COLOR_SECRETCHAT);
		  } else if (type == Config.MSGTYPE_OTHERS) {
			   chatadapter.addOthersMessage(user, str, time, null);
		  } else if (type == Config.MSGTYPE_OTHERS_SECRET) {
			   chatadapter.addOthersMessage(user, str, time, Config.COLOR_SECRETCHAT);
		  }
		  //应用不在前台时通知栏提示
		  if (!SystemMethod.isTopActivity(ChatActivity.this, getPackageName())) {
			   NoReadNum ++;
			   MessageMethod.NotifyMsg(ChatActivity.this, str, NotifyBuilder, NoReadNum);
		  }
	 }

	 //发送对话信息
	 private void msgSend(String str) {
		  if (NetWorkClient != null) {
			   NetWorkClient.Send(RemoteIP, MessageMethod.msgBuilder(User, NetWorkMethod.getLocalIP(ChatActivity.this), str), Config.TYPE_MSG);
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

	 //是否有用户
	 private boolean hasUser(String ip) {
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
						 }
					}
			   }
		  }
		  NetWorkClient.Send(alert, alert, Config.TYPE_ALERT_USER);
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
			   NetWorkClient.Send(RemoteIP, MessageMethod.msgBuilder(User, NetWorkMethod.getLocalIP(ChatActivity.this), Config.MSGTYPE_SYSTEM_ONLINE), Config.TYPE_SYSTEM);
		  }
	 }

	 //离线同步删除本机IP
	 private void unregisterServer() {
		  NetWorkClient.Send(RemoteIP, User + "-" + NetWorkMethod.getLocalIP(this), Config.TYPE_RELOAD_USERLIST);
	 }

	 //退出
	 private void CloseApp(boolean launcher) {
		  if (launcher) {
			   moveTaskToBack(true);
		  } else {
			   ChatActivity.this.finish();
		  }
	 }

	 @Override
	 protected void onResume() {
		  //未读计数清零
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
						 CloseApp(false);
						 CloseSocketClientConnect();
						 new Handler().postDelayed(new Runnable(){
								   @Override
								   public void run() {
										CloseSocketServerConnect();
								   }
							  }, 800);
						 d.dismiss();
					}
			   });
		  back.setNeutralButton(R.string.back_launcher, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface d, int i) {
						 CloseApp(true);
					}
			   });
		  back.setNegativeButton(R.string.cancel, null);
		  back.show();
	 }
}
