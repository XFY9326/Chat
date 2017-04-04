package tool.xfy9326.chat.Methods;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.text.SimpleDateFormat;
import tool.xfy9326.chat.ChatActivity;
import tool.xfy9326.chat.Methods.NetWorkMethod;
import tool.xfy9326.chat.R;
import android.text.Spanned;

//消息处理

public class MessageMethod extends Thread {

	 //后台运行时通知栏提示
	 public static void NotifyMsg(Context ctx, Spanned text, Notification.Builder notification, int num) {
		  NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		  notification.setContentText(text);
		  notification.setTicker(text);
		  notification.setNumber(num);
		  nm.cancel(Config.NOTIFICATION_ID);
		  nm.notify(Config.NOTIFICATION_ID, notification.getNotification());
	 }

	 public static Notification.Builder getNotifyBuilder(Context ctx) {
		  PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0,  new Intent(ctx, ChatActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
		  Notification.Builder notification = new Notification.Builder(ctx);
		  notification.setAutoCancel(true);
		  notification.setContentIntent(pendingIntent);
		  notification.setSmallIcon(android.R.mipmap.sym_def_app_icon);
		  notification.setWhen(System.currentTimeMillis());
		  notification.setContentTitle(ctx.getString(R.string.app_name));
		  notification.setPriority(Notification.PRIORITY_DEFAULT);
		  notification.setCategory(Notification.CATEGORY_MESSAGE);
		  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			   notification.setFullScreenIntent(pendingIntent, true);
		  } 
		  return notification;
	 }

	 //用户对话信息构建
	 public static String buildText(String user, String text) {
		  String result = getMsgTime() + "> " + user + " : " + text;
		  return result;
	 }

	 //彩色文字构建
	 public static String buildColorText(String text, String color) {
		  String result = "<font color='" + color + "'>" + text + "</font>";
		  return result;
	 }

     //系统提示构建
	 public static String buildSystemText(String text) {
		  String result = "#Server Message# [" + getMsgTime() + "]" + "\n" + text;
		  result = buildColorText(result, Config.COLOR_SYSTEM);
		  return result;
	 }

	 //消息发送时间
	 private static String getMsgTime() {
		  SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		  String time = sdf.format(System.currentTimeMillis());
		  return time;
	 }

	 //IP地址补全
	 public static String fixIP(String ip, Context ctx) {
		  if (ip.indexOf(".") < 0) {
			   String localnet = NetWorkMethod.getLocalIP(ctx);
			   localnet = localnet.substring(0, localnet.lastIndexOf(".") + 1);
			   ip = localnet + ip;
		  }
		  return ip;
	 }

}
