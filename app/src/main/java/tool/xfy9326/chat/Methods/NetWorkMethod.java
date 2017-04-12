package tool.xfy9326.chat.Methods;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Scanner;

//其他方法

public class NetWorkMethod {

	//热点网络检测
	public static boolean isWifiApEnabled(Context context) {
		try {
			WifiManager mWifiManager=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
			Method method= mWifiManager.getClass().getMethod("isWifiApEnabled");
			method.setAccessible(true);
			return (boolean) method.invoke(mWifiManager);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	//检测端口是否被占用
	public static boolean isPortUsed(int port) {
		Process process = null;
		try {
			String command = "netstat -tln";
			process = Runtime.getRuntime().exec(command);
			InputStream in = process.getInputStream();
			Scanner scanner = new Scanner(in, "UTF-8");
			String[] result = scanner.useDelimiter("\\A").next().toString().split("\n");
			boolean isUsed = false;
			for (int i = 1; i < result.length; i++) {
				String[] detail = result[i].split("\\s+");
				if (detail[3].contains(port + "") || detail[4].contains(port + "")) {
					isUsed = true;
					break;
				}
			}
			return isUsed;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		finally {
			if (process != null) {
				process.destroy();
			}
		}
	}

	//检测IP是否正确
	public static boolean isIPCorrect(String ip) {
		if (ip.equals("")) {
			return true;
		} else {
			String reg = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\." + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
			if (ip.matches(reg)) {
				return true;
			} else {
				return false;
			}
		}
	}

	//检测WIFI网络连接
	public static boolean isWifiConnected(Context context) {  
		ConnectivityManager con= (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean wifi = con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
		if (isWifiApEnabled(context)) {
			wifi = true;
		}
		return wifi;
	}

	//检测是否只使用移动网络
	public static boolean isOnlyMobileNetWork(Context context) {
		ConnectivityManager con= (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean internet=con.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
		boolean wifi = con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
		return !wifi && internet;
	}

	//检测VPN使用状态
	public static boolean isVpnUsed() {
		try {
			Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
			if (niList != null) {
				for (NetworkInterface intf : Collections.list(niList)) {
					if (!intf.isUp() || intf.getInterfaceAddresses().size() == 0) {
						continue;
					}
					if ("tun0".equals(intf.getName()) || "ppp0".equals(intf.getName())) {                        
						return true;
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	//获取本地IP
	public static String getLocalIP(Context ctx) {
		WifiManager wifiService = (WifiManager) ctx.getSystemService(ctx.WIFI_SERVICE);
		String ip = Config.IP_LOCALHOST;
		if (isWifiApEnabled(ctx)) {
			ip = "192.168.43.1";
		} else {
			WifiInfo wifiinfo = wifiService.getConnectionInfo();
			String wifiip = intToIp(wifiinfo.getIpAddress());
			if (wifiip.equalsIgnoreCase("0.0.0.0")) {
				ip = Config.IP_LOCALHOST;
			} else {
				ip = wifiip;
			}
		}
		return ip;
	}

	//IP地址格式化
	private static String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
	}
}
