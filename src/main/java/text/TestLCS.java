package text;

import org.apache.commons.text.diff.StringsComparator;

public class TestLCS {

    public static void main(String[] args) {
        StringsComparator sc = new StringsComparator("banco", "banco");
        int i = sc.getScript().getLCSLength();
        System.out.println(i);
    }
    
}
