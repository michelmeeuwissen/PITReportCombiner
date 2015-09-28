package org.pitest.report;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class DupHTMLFilter implements FileFilter {

    public boolean accept(File file) {

        Pattern p = Pattern.compile("(index.html)\\d+(.)(html)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

        return p.matcher(file.getName()).matches();
    }
}