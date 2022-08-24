package org.alias.ipp.xmlres.action.impl;

import com.intellij.ide.util.PropertiesComponent;
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

public class XmlResourcesInstantAction extends AbstractXmlResourcesAction {

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
        if (project == null || editor == null) {
            return;
        }
        PropertiesComponent pc = PropertiesComponent.getInstance();
        execute(project,
                editor,
                pc.getInt(XmlResourcesOptionDialog.PC_KEY_NAMING_STYLE, 0) == 0,
                XmlResourcesOptionDialog.getPrefix1stNwordsValueAt(pc.getInt(XmlResourcesOptionDialog.PC_KEY_PREFIX_1ST_N_WORDS, 0)),
                pc.getBoolean(XmlResourcesOptionDialog.PC_KEY_SPACE_BETWEEN_PREFIX, true),
                pc.getBoolean(XmlResourcesOptionDialog.PC_KEY_INSERT_XML_HEADER, true),
                pc.getBoolean(XmlResourcesOptionDialog.PC_KEY_DELETE_COMMENT, false),
                XmlResourcesOptionDialog.getIndentValueAt(pc.getInt(XmlResourcesOptionDialog.PC_KEY_INDENT, 1)),
                pc.getBoolean(XmlResourcesOptionDialog.PC_KEY_SEPARATE_NON_TRANSLATABLE, false),
                pc.getBoolean(XmlResourcesOptionDialog.PC_KEY_CASE_SENSITIVE, true)
        );
    }
}
