package org.alias.ipp.xmlres.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.alias.ipp.xmlres.util.*
import org.w3c.dom.Document
import org.alias.ipp.xmlres.CommentedNode
import java.io.IOException

abstract class AbstractXmlResourcesAction : AnAction() {
    /**
     * \n: 换行
     * \\s+: 多个空白字符
     * $n: 表示regex中第n个小括号内的内容
     */
    protected fun execute(
        project: Project,
        editor: Editor,
        isSnakeCase: Boolean,
        prefix1stNwords: Int,
        insertSpaceBetweenDiffPrefix: Boolean,
        insertXmlEncoding: Boolean,
        deleteComment: Boolean,
        indent: Int,
        separateNonTranslatable: Boolean,
        isCaseSensitive: Boolean
    ) {
        //content, remove \n & whitespace(s) between resources items
        var escapedContent = editor.document.text.replace(">\n*\\s+?<".toRegex(), "><")

        for (tag in HTML_TAGS) {
            escapedContent = escapedContent.replace("<$tag>".toRegex(), "&lt;$tag&gt;")
                .replace("</$tag>".toRegex(), "&lt;/$tag&gt;")
        }
        val document: Document
        try {
            document = escapedContent.toDocument()
        } catch (e: Exception) {
            notifyError(e.localizedMessage)
            return
        }
        //get node list from document object
        var commentedNodes = document.toNodeList()
        //sort
        commentedNodes =
            commentedNodes.sortedWith(CommentedNode.AttrNameComparator(separateNonTranslatable, isCaseSensitive))
        document.deleteChildNodes()

        //insert space if enabled
        if (insertSpaceBetweenDiffPrefix) {
            commentedNodes = document.insertSpaceBetweenDiffPrefix(commentedNodes, prefix1stNwords, isSnakeCase)
        }

        //append node into document with comments if enabled
        for (commentedNode in commentedNodes) {
            // don't write comment if `enableDeleteComment` is true
            if (!deleteComment) {
                val comments = commentedNode.comments
                comments?.let {
                    for (comment in it) {
                        document.documentElement.appendChild(comment)
                    }
                }
            }
            document.documentElement.appendChild(commentedNode.node)
        }

        //get pretty string from document object
        var prettyString: String
        try {
            prettyString = document.toPrettyString(indent, insertXmlEncoding)
            // IDEA uses '\n' for all their text editors internally, so we just use '\n' as our line separator
            // See: http://www.jetbrains.org/intellij/sdk/docs/basics/architectural_overview/documents.html
            val lineSeparator = System.getProperty("line.separator")
            if (lineSeparator != "\n") {
                prettyString = prettyString.replace(lineSeparator, "\n")
            }
        } catch (e: IOException) {
            notifyError(e.localizedMessage)
            return
        }

        // write option
        if (insertSpaceBetweenDiffPrefix) {
            prettyString = prettyString.replace("\n\\s+<space/>".toRegex(), "\n")
        }
        // eliminate line breaks before/after xliff declaration
        prettyString = prettyString.replace("\n\\s+<xliff:".toRegex(), "<xliff:")
        prettyString = prettyString.replace("(</xliff:\\w+>)\n\\s+".toRegex(), "$1")
        // eliminate line breaks before <![CDATA[
        prettyString = prettyString.replace("\n\\s+<\\!\\[CDATA\\[".toRegex(), "<\\!\\[CDATA\\[")
        // eliminate line breaks after ]]>
        prettyString = prettyString.replace("\\]\\]>\n\\s+".toRegex(), "\\]\\]>")
        // insert space before /> if there is none
        prettyString = prettyString.replace("\"\\s*\\/>".toRegex(), "\" \\/>")
        //Write action
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.setText(prettyString)
        }
    }

    companion object {
        internal val HTML_TAGS = arrayOf(
            "b",
            "em",
            "i",
            "cite",
            "dfn",
            "big",
            "small",
            "tt",
            "strike",
            "del",
            "u",
            "sup",
            "sub",
            "ul",
            "li",
            "br",
            "div",
        )
    }
}