package com.opticdev.ideplugins.jetbrains;

import com.intellij.ide.SaveAndSyncHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import javax.swing.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

public class SocketConnection extends WebSocketClient {

    public SocketConnection(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Connected to Optic");
    }

    @Override
    public void onMessage(String s) {
        try {
            JSONObject jsonObj = new JSONObject(s);

            if (jsonObj.getString("event").equals("files-updated")) {
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        SaveAndSyncHandler.getInstance().refreshOpenFiles();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {

    }

    @Override
    public void onError(Exception e) {
        System.out.print(e);
    }
}
