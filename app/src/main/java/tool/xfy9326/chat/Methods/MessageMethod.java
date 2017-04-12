package tool.xfy9326.chat.Methods;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tool.xfy9326.chat.ChatActivity;
import tool.xfy9326.chat.Methods.NetWorkMethod;
import tool.xfy9326.chat.R;

//消息处理

public class MessageMethod {

	//后台运行时通知栏提示
	public static void NotifyMsg(Context ctx, String text, Notification.Builder notification, int num) {
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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			notification.setFullScreenIntent(pendingIntent, true);
		}
		return notification;
	}

	//提醒高亮处理
	public static SpannableString alertHighLight(Context ctx, String text, ArrayList<String> IP) {
		SpannableString sp = new SpannableString(text);
		Pattern pat = Pattern.compile("(\\@)(\\S+)");
		Matcher mat = pat.matcher(text);
		while (mat.find()) {
			String originget = mat.group(0).toString();
			if (!originget.isEmpty() && originget != " ") {
				String get = originget.substring(1).trim();
				if (!get.contains("@")) {
					get = MessageMethod.fixIP(get, ctx);
					if (NetWorkMethod.isIPCorrect(get) && IP.toString().contains(get) || get.equalsIgnoreCase(NetWorkMethod.getLocalIP(ctx))) {
						sp.setSpan(new ForegroundColorSpan(Color.parseColor(Config.COLOR_ALERTUSER)), mat.start(), mat.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				}
			}
		}
		return sp;
	}

	//接收信息处理
	public static String[] msgScanner(String text) {
		String time = text.substring(0, text.indexOf(">"));
		String ip = text.substring(text.indexOf(">") + 1, text.indexOf("-"));
		String user = text.substring(text.indexOf("-") + 1, text.indexOf("_"));
		String msg = text.substring(text.indexOf("_") + 1);
		return new String[]{time, ip, user, msg};
	}

	//发送格式信息
	public static String msgBuilder(String user, String ip, String text) {
		return getMsgTime() + ">" + ip + "-" + user + "_" + text;
	}

	//消息发送时间
	public static String getMsgTime() {
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
