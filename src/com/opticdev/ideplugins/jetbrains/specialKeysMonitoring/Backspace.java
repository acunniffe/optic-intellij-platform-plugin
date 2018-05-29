package com.opticdev.ideplugins.jetbrains.specialKeysMonitoring;

import com.intellij.codeInsight.editorActions.BackspaceHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.opticdev.ideplugins.jetbrains.OpticPluginSingleton;

public class Backspace extends BackspaceHandlerDelegate {
    @Override
    public void beforeCharDeleted(char c, PsiFile psiFile, Editor editor) {
        return;
    }

    @Override
    public boolean charDeleted(char c, PsiFile psiFile, Editor editor) {
        OpticPluginSingleton.getInstance().update(editor);
        return false;
    }
}
