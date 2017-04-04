package tool.xfy9326.chat.Net;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tool.xfy9326.chat.Methods.Config;
import tool.xfy9326.chat.Methods.SystemMethod;
import java.util.List;

//多线程发送Socket信息处理
public class SocketClientHandler {
	 private int Port;
	 private String PassWord = null;
	 private ExecutorService executor = null;

	 public SocketClientHandler(int port, String pw) {
		  this.Port = port;
		  this.PassWord = pw;
		  executor = Executors.newFixedThreadPool(SystemMethod.getCpuNumCores());
	 }

	 //单IP发送
	 public void Send(String IP, String text, int type) {
		  executor.execute(new SocketClient(TagFix(text, type), IP, Port, PassWord));
	 }

	 //多IP发送
	 public void Send(ArrayList<String> IP, String text, int type) {
		  if (IP.size() > 0) {
			   for (String ip : IP) {
					executor.execute(new SocketClient(TagFix(text, type), ip, Port, PassWord));
			   }
		  }
	 }

	 public void Close() {
		  executor.shutdownNow();
	 }

	 private String TagFix(String text, int type) {
		  switch (type) {
			   case 1:
					text = Config.MSG_TAG + text;
					break;
			   case 2:
					text = Config.USERLIST_TAG + text;
					break;
			   case 3:
					text = Config.ASK_USERLIST_TAG + text;
					break;
			   case 4:
					text = Config.RELOAD_USERLIST_TAG + text;
					break;
			   case 5:
					text = Config.ALERT_TAG + text;
					break;
			   case 6:
					text = Config.SECRET_TAG + text;
					break;
		  }
		  return text;
	 }
}
