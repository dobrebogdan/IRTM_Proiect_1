package irtm1;

import java.io.File;
import java.io.FileFilter;

public class CustomFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        String stringPathname = pathname.getName().toLowerCase();
        return stringPathname.endsWith(".txt") || stringPathname.endsWith(".doc") || stringPathname.endsWith(".docx")
                || stringPathname.endsWith(".pdf");
    }
}
