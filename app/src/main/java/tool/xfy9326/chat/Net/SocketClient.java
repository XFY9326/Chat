package tool.xfy9326.chat.Net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import tool.xfy9326.chat.Tools.AES;

//Socket信息发送线程
class SocketClient extends Thread {
    private final String IP;
    private final int Port;
    private final String PassWord;
    private final String Text;

    public SocketClient(String text, String ip, int port, String pw) {
        this.Port = port;
        this.PassWord = pw;
        this.IP = ip;
        this.Text = text;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        OutputStream socketOut = null;
        try {
            socket.connect(new InetSocketAddress(IP, Port), 2000);
            socketOut = socket.getOutputStream();
            socketOut.write(AES.encrypt(Text, PassWord).getBytes("UTF-8"));
            socketOut.flush();
            socketOut.close();
            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();
        } catch (Exception e) {
            if (!socket.isClosed()) {
                try {
                    if (socketOut != null) {
                        socketOut.close();
                        socket.close();
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

}
