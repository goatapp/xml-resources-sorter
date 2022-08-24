package org.alias.ipp.xmlres.action.impl;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.alias.ipp.xmlres.action.AbstractXmlResourcesAction;
import org.alias.ipp.xmlres.dialog.XmlResourcesOptionDialog;
import org.jetbrains.annotations.NotNull;

import static org.alias.ipp.xmlres.util.VirtualFilesKt.isXmlResourcesFile;

public class XmlResourcesAction extends AbstractXmlResourcesAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        e.getPresentation().setEnabledAndVisible(isXmlResourcesFile(file));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = getEventProject(e);
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (project == null || editor == null) return;
        XmlResourcesOptionDialog dialog = new XmlResourcesOptionDialog(project);
        if (!dialog.showAndGet()) return;
        // options
        boolean insertSpaceEnabled = dialog.isInsertSpaceEnabled();
        boolean isSnakeCase = true;
        int prefix1stNwords = 0;
        if (insertSpaceEnabled) {
            isSnakeCase = dialog.isSnakeCase();
            prefix1stNwords = dialog.getPrefix1stNwords();
        }
        boolean insertXmlHeaderEnabled = dialog.isInsertXmlHeaderEnabled();
        boolean deleteCommentsEnabled = dialog.isDeleteCommentsEnabled();
        int indent = dialog.getIndent();
        boolean separateNonTranslatable = dialog.isSeparateNonTranslatableStringsEnabled();
        boolean isCaseSensitive = dialog.isCaseSensitive();
        execute(project,
                editor,
                isSnakeCase,
                prefix1stNwords,
                insertSpaceEnabled,
                insertXmlHeaderEnabled,
                deleteCommentsEnabled,
                indent,
                separateNonTranslatable,
                isCaseSensitive);
    }
}
