package net.sf.juffrou.mq.util;

public class TextUtils {

	public static String escapeText(String text) {
        String content = text.replace("'", "\\'");
        content = content.replace("\"", "\\\"");
        content = content.replace(System.getProperty("line.separator"), "\\n");
        content = content.replace("\n", "\\n");
        content = content.replace("\r", "\\n");
        
        return content;
	}

}
