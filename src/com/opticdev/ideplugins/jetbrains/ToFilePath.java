package com.opticdev.ideplugins.jetbrains;

import com.intellij.openapi.editor.Editor;

public class ToFilePath {

    public static String fromEditor(Editor editor) {
        try {
            String file = editor.getDocument().toString();
            String[] split = file.split("file:\\/\\/");
            return split[1].substring(0, split[1].length() - 1);
        } catch (Exception e) {
            return "";
        }
    }

}
