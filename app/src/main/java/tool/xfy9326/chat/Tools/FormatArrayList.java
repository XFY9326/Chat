package tool.xfy9326.chat.Tools;

import java.util.ArrayList;

//ArrayList与String转换

public class FormatArrayList {
    public static ArrayList<String> StringToStringArrayList(String str) {
        ArrayList<String> arr = new ArrayList<>();
        if (str.contains("[") && str.length() >= 3) {
            str = str.substring(1, str.length() - 1);
            if (str.contains(",")) {
                String[] temp = str.split(",");
                for (int i = 0; i < temp.length; i++) {
                    if (i != 0) {
                        temp[i] = temp[i].substring(1, temp[i].length());
                    }
                    arr.add(temp[i]);
                }
            } else {
                arr.add(str);
            }
        }
        return arr;
    }
}
