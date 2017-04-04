package tool.xfy9326.chat.Net;

import android.os.Handler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tool.xfy9326.chat.Methods.SystemMethod;

//Socket服务器多线程处理
public class SocketServerHandler extends Thread {
	 private String PassWord = null;
	 private int Port;
	 private Handler mHandler = null;
	 private ServerSocket Server = null;
	 private Socket socket = null;
	 private ExecutorService executor = null;
	 private boolean flag = true;

	 public SocketServerHandler(int port, Handler handler, String password) {
		  this.Port = port;
		  this.mHandler = handler;
		  this.PassWord = password;
	 }

	 @Override
	 public void run() {
		  super.run();
		  if (Server != null && executor != null) {
			   try {
					while (flag) {
						 socket = Server.accept();
						 executor.execute(new SocketServer(socket, Port, mHandler, PassWord));
					}
			   } catch (IOException ioe) {
					if (!Server.isClosed() && Server != null) {
						 try {
							  Server.close();
						 } catch (IOException e) {
							  e.printStackTrace();
						 }
					}
			   }
		  } else {
			   interrupt();
		  }
	 }

	 //启动
	 @Override
	 public void start() {
		  try {
			   Server = new ServerSocket(Port);
			   executor = Executors.newFixedThreadPool(SystemMethod.getCpuNumCores());
			   flag = true;
		  } catch (IOException e) {
			   e.printStackTrace();
		  }
		  super.start();
	 }

	 //关闭所有线程服务
	 public void StopServer() {
		  try {
			   if (Server != null && executor != null) {
					if (!Server.isClosed()) {
						 flag = false;
						 executor.shutdownNow();
						 Server.close();
					}
					if (!interrupted() || isAlive()) {
						 Thread.currentThread().interrupt();
					}

			   }
		  } catch (IOException e) {
			   e.printStackTrace();
		  }
	 }

}
