package tool.xfy9326.chat.Net;

import java.util.ArrayList;

import tool.xfy9326.chat.Methods.Config;

//发送Socket信息处理
public class SocketClientHandler {
    private final int Port;
    private String PassWord = null;

    public SocketClientHandler(int port, String pw) {
        this.Port = port;
        this.PassWord = pw;
    }

    //单IP发送
    public void Send(String IP, String text, int type) {
        new SocketClient(TagFix(text, type), IP, Port, PassWord).start();
    }

    //多IP发送
    public void Send(ArrayList<String> IP, String text, int type) {
        if (IP.size() > 0) {
            for (String ip : IP) {
                new SocketClient(TagFix(text, type), ip, Port, PassWord).start();
            }
        }
    }

    //多IP发送不同内容
    @SuppressWarnings("SameParameterValue")
    public void Send(ArrayList<String> IP, ArrayList<String> text, int type) {
        if (IP.size() > 0) {
            for (int i = 0; i < IP.size(); i++) {
                new SocketClient(TagFix(text.get(i), type), IP.get(i), Port, PassWord).start();
            }
        }
    }

    public void Close() {
        Thread.currentThread().interrupt();
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
            case 7:
                text = Config.SYSTEM_TAG + text;
                break;
        }
        return text;
    }
}
