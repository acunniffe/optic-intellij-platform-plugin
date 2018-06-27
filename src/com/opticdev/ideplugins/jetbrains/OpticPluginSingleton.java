package com.opticdev.ideplugins.jetbrains;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.util.TextRange;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpticPluginSingleton {
    private static OpticPluginSingleton single_instance = null;

    SocketConnection socket;

    Document currentDocument = null;

    private OpticPluginSingleton()
    {

        Runtime rt = Runtime.getRuntime();
        try {
            Process pr = rt.exec("open /Applications/Optic.app");
        } catch (IOException e) {
            e.printStackTrace();
        }

        EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();

        eventMulticaster.addVisibleAreaListener(new VisibleAreaListener() {
            @Override
            public void visibleAreaChanged(VisibleAreaEvent visibleAreaEvent) {
                if (currentDocument != visibleAreaEvent.getEditor().getDocument()) {
                    update(visibleAreaEvent.getEditor());
                }
                currentDocument = visibleAreaEvent.getEditor().getDocument();
            }
        });

        eventMulticaster.addCaretListener(new CaretListener() {
            @Override
            public void caretPositionChanged(CaretEvent e) {
                update(e.getEditor());
            }
        });

        eventMulticaster.addSelectionListener(new SelectionListener() {
            @Override
            public void selectionChanged(SelectionEvent selectionEvent) {
                update(selectionEvent.getEditor());
            }
        });

        connect();


    }

    public static OpticPluginSingleton getInstance()
    {
        if (single_instance == null)
            single_instance = new OpticPluginSingleton();

        return single_instance;
    }


    private long lastReconnectAttempt = System.currentTimeMillis();

    private void connect() {
        lastReconnectAttempt = System.currentTimeMillis();
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
            socket = new SocketConnection(new URI("ws://localhost:30333/socket/editor/jetbrains"));
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    private Pattern searchRegex = Pattern.compile("^[\\s]*\\/\\/\\/[\\s]*(.+)");

    private String isSearch(String lineText, int start, int end) {
        Matcher matcher = searchRegex.matcher(lineText);
        if (end - start <= 1 && matcher.matches()) {
            Matcher matcher2 = searchRegex.matcher(lineText);
            while (matcher2.find()) {
                return matcher2.group(1);
            }
        }
        return null;
    }

    public void update(Editor editor) {

        String filePath = ToFilePath.fromEditor(editor);

        if (filePath.equals("")) {
            return;
        }

        int start = editor.getCaretModel().getCurrentCaret().getSelectionStart();
        int end = editor.getCaretModel().getCurrentCaret().getSelectionEnd();
        String content = editor.getDocument().getText();

        int line = editor.getCaretModel().getLogicalPosition().line;
        int lineStart = editor.getDocument().getLineStartOffset(line);
        int lineEnd = editor.getDocument().getLineEndOffset(line);
        String lineText = editor.getDocument().getText(new TextRange(lineStart, lineEnd));

        String searchQuery = isSearch(lineText, start, end);

        JSONObject json;
        if (searchQuery == null) {
            json = new JSONObject()
                    .put("event", "context")
                    .put("start", start)
                    .put("end", end)
                    .put("content", content)
                    .put("file", filePath);

        } else {
            json = new JSONObject()
                    .put("event", "search")
                    .put("query", searchQuery)
                    .put("start", start)
                    .put("end", end)
                    .put("content", content)
                    .put("file", filePath);
        }

//        System.out.println("SEND "+ json.toString());

        if (socket.isOpen()) {
            socket.send(json.toString());
        } else {
            //if it hasn't tried to reconnect in 10 seconds
            if (System.currentTimeMillis() - lastReconnectAttempt > 10 * 1000) {
                System.out.print("Reconnect Attempt");
                connect();
                if (socket.isOpen()) {
                    socket.send(json.toString());
                }
            }
        }
    }

}
