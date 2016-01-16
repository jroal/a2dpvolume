package a2dp.Vol;

import java.util.Arrays;

public class FileNameCleaner {
final static int[] illegalChars = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};
static {
    Arrays.sort(illegalChars);
}
public static String cleanFileName(String badFileName) {
    StringBuilder cleanName = new StringBuilder();
    if(badFileName == null)return "bad_name";
    for (int i = 0; i < badFileName.length(); i++) {
        int c = (int)badFileName.charAt(i);
        if (Arrays.binarySearch(illegalChars, c) < 0) {
            cleanName.append((char)c);
        }
    }
    return cleanName.toString();
}
}

