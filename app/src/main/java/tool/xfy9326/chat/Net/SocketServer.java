package tool.xfy9326.chat.Net;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;

import tool.xfy9326.chat.Methods.Config;
import tool.xfy9326.chat.Methods.MessageMethod;
import tool.xfy9326.chat.Tools.AES;

//Socket服务处理线程
class SocketServer extends Thread {
	private Socket socket = null;
	private Handler NetHandler = null;
	private String PassWord = null;

	SocketServer(Socket socket, Handler handler, String pw) {
		this.socket = socket;
		this.NetHandler = handler;
		this.PassWord = pw;
	}

	@Override
	public void run() {
		super.run();
		try {
			InputStream socketIn = socket.getInputStream();
			Scanner scanner = new Scanner(socketIn, "UTF-8");
			String text = AES.decrypt(scanner.useDelimiter("\\A").next(), PassWord);
			if (text.contains(Config.TAG_TITLE)) {
				String tag = text.substring(0, text.indexOf("_") + 1);
				Message message = new Message();
				Bundle bundle = new Bundle();
				String result = text.substring(text.indexOf("_") + 1);
				bundle.putString(Config.DATA_RECEIVE_IP, socket.getInetAddress().toString());
				bundle.putString(Config.DATA_RECEIVE, result);
				//分别处理信息
				switch (tag) {
					case Config.MSG_TAG: {
						String[] info = MessageMethod.msgScanner(result);
						bundle.putStringArray(Config.DATA_RECEIVE_INFO, info);
						message.what = Config.TYPE_MSG;
						break;
					}
					case Config.USERLIST_TAG:
						message.what = Config.TYPE_USERLIST;
						break;
					case Config.ASK_USERLIST_TAG:
						message.what = Config.TYPE_ASK_USERLIST;
						break;
					case Config.RELOAD_USERLIST_TAG:
						message.what = Config.TYPE_RELOAD_USERLIST;
						break;
					case Config.ALERT_TAG:
						message.what = Config.TYPE_ALERT_USER;
						break;
					case Config.SECRET_TAG: {
						String[] info = MessageMethod.msgScanner(result);
						bundle.putStringArray(Config.DATA_RECEIVE_INFO, info);
						message.what = Config.TYPE_SECRET_CHAT;
						break;
					}
					case Config.SYSTEM_TAG: {
						String[] info = MessageMethod.msgScanner(result);
						bundle.putStringArray(Config.DATA_RECEIVE_INFO, info);
						message.what = Config.TYPE_SYSTEM;
						break;
					}
				}
				message.setData(bundle);
				NetHandler.sendMessage(message);
				socketIn.close();
				CloseConnect();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//确认客户端关闭后再关闭
	private void CloseConnect() {
		if (isClientClosed()) {
			try {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Thread.currentThread().interrupt();
		} else {
			try {
				sleep(500);
				CloseConnect();
			} catch (InterruptedException e) {
				e.printStackTrace();
				CloseConnect();
			}
		}
	}

	private boolean isClientClosed() {
		try {
			socket.sendUrgentData(0xFF);
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}
	}
}
