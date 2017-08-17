package tool.xfy9326.chat.View;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tool.xfy9326.chat.Methods.Config;
import tool.xfy9326.chat.Methods.MessageMethod;
import tool.xfy9326.chat.R;

public class ListViewAdapter extends BaseAdapter {
    private final Context mContext;
    private ArrayList<SpannableString> Messages = null;
    private ArrayList<String> Times = null;
    private ArrayList<Integer> Types = null;
    private ArrayList<String> Users = null;
    private ArrayList<Integer> Colors = null;
    private ArrayList<String> IP = null;

    public ListViewAdapter(Context ctx, ArrayList<Integer> type, ArrayList<String> user, ArrayList<String> ip) {
        this.mContext = ctx;
        this.Messages = new ArrayList<>();
        this.Types = type;
        this.Users = user;
        this.IP = ip;
        this.Times = new ArrayList<>();
        this.Colors = new ArrayList<>();
    }

    public void addMainMessage(String msg, String time, String color) {
        Messages.add(MessageMethod.alertHighLight(mContext, msg, IP));
        Types.add(Config.TYPE_LISTVIEW_CHAT_MSG_MAIN);
        Users.add(Config.DATA_DEFAULT_USERNAME);
        Times.add(time);
        if (color == null) {
            Colors.add(-1);
        } else {
            Colors.add(Color.parseColor(color));
        }
        removeMessage();
        notifyDataSetChanged();
    }

    @SuppressWarnings("SameParameterValue")
    public void addSystemMessage(String msg, String time, String color) {
        Messages.add(new SpannableString(msg));
        Types.add(Config.TYPE_LISTVIEW_CHAT_SYSTEM);
        Users.add(Config.DATA_DEFAULT_USERNAME);
        Times.add(time);
        if (color == null) {
            Colors.add(-1);
        } else {
            Colors.add(Color.parseColor(color));
        }
        removeMessage();
        notifyDataSetChanged();
    }

    public void addOthersMessage(String user, String msg, String time, String color) {
        Messages.add(MessageMethod.alertHighLight(mContext, msg, IP));
        Types.add(Config.TYPE_LISTVIEW_CHAT_MSG_OTHERS);
        Users.add(user);
        Times.add(time);
        if (color == null) {
            Colors.add(-1);
        } else {
            Colors.add(Color.parseColor(color));
        }
        removeMessage();
        notifyDataSetChanged();
    }

    private void removeMessage() {
        if (Messages.size() > 200) {
            Messages.remove(0);
            Types.remove(0);
            Users.remove(0);
            Times.remove(0);
            Colors.remove(0);
        }
    }

    @Override
    public View getView(int p1, View p2, ViewGroup p3) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = null;
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        if (Types.get(p1) == Config.TYPE_LISTVIEW_CHAT_SYSTEM) {
            layout = inflater.inflate(R.layout.listview_chat_system, null);
            TextView text = layout.findViewById(R.id.textview_system_alert);
            TextView time = layout.findViewById(R.id.textview_system_time);
            text.setText(Messages.get(p1));
            if (Colors.get(p1) != -1) {
                text.setTextColor(Colors.get(p1));
            }
            time.setText(Times.get(p1));
        } else if (Types.get(p1) == Config.TYPE_LISTVIEW_CHAT_MSG_MAIN) {
            layout = inflater.inflate(R.layout.listview_chat_user_main, null);
            TextView text = layout.findViewById(R.id.textview_chat_main_text);
            text.setMaxWidth(w_screen / 2);
            TextView time = layout.findViewById(R.id.textview_chat_main_time);
            text.setText(Messages.get(p1));
            if (Colors.get(p1) != -1) {
                text.setTextColor(Colors.get(p1));
            }
            time.setText(Times.get(p1));
        } else if (Types.get(p1) == Config.TYPE_LISTVIEW_CHAT_MSG_OTHERS) {
            layout = inflater.inflate(R.layout.listview_chat_user_others, null);
            TextView text = layout.findViewById(R.id.textview_chat_others_text);
            text.setMaxWidth(w_screen / 2);
            TextView time = layout.findViewById(R.id.textview_chat_others_time);
            TextView user = layout.findViewById(R.id.textview_chat_others_name);
            text.setText(Messages.get(p1));
            if (Colors.get(p1) != -1) {
                text.setTextColor(Colors.get(p1));
            }
            time.setText(Times.get(p1));
            user.setText(Users.get(p1));
        }
        return layout;
    }

    @Override
    public int getCount() {
        return Messages.size();
    }

    @Override
    public Object getItem(int p1) {
        return Messages.get(p1);
    }

    @Override
    public long getItemId(int p1) {
        return p1;
    }

}
