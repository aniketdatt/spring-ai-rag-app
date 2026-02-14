package com.ragapp.ragapp.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to convert Markdown text to HTML for rendering in Vaadin
 * components.
 * Supports basic markdown syntax including bold, italic, code blocks, lists,
 * and line breaks.
 */
public class MarkdownRenderer {

    /**
     * Converts markdown text to HTML.
     * 
     * @param markdown The markdown text to convert
     * @return HTML string ready for rendering
     */
    public static String toHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }

        String html = markdown;

        // Escape HTML special characters first to prevent XSS
        html = escapeHtml(html);

        // Convert code blocks (```code```) - must be done before inline code
        html = convertCodeBlocks(html);

        // Convert inline code (`code`)
        html = convertInlineCode(html);

        // Convert bold (**text** or __text__)
        html = html.replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>");
        html = html.replaceAll("__(.+?)__", "<strong>$1</strong>");

        // Convert italic (*text* or _text_)
        html = html.replaceAll("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)", "<em>$1</em>");
        html = html.replaceAll("(?<!_)_(?!_)(.+?)(?<!_)_(?!_)", "<em>$1</em>");

        // Convert headers
        html = convertHeaders(html);

        // Convert unordered lists
        html = convertUnorderedLists(html);

        // Convert ordered lists
        html = convertOrderedLists(html);

        // Convert line breaks (double newline to paragraph, single newline to <br>)
        html = html.replaceAll("\\n\\n", "</p><p>");
        html = html.replaceAll("\\n", "<br>");

        // Wrap in paragraph tags
        html = "<p>" + html + "</p>";

        // Clean up empty paragraphs
        html = html.replaceAll("<p>\\s*</p>", "");

        return html;
    }

    private static String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    private static String convertCodeBlocks(String text) {
        Pattern pattern = Pattern.compile("```([\\s\\S]*?)```");
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String code = matcher.group(1).trim();
            matcher.appendReplacement(sb, "<pre><code>" + code + "</code></pre>");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static String convertInlineCode(String text) {
        return text.replaceAll("`([^`]+)`", "<code>$1</code>");
    }

    private static String convertHeaders(String text) {
        // H1-H6
        text = text.replaceAll("(?m)^######\\s+(.+)$", "<h6>$1</h6>");
        text = text.replaceAll("(?m)^#####\\s+(.+)$", "<h5>$1</h5>");
        text = text.replaceAll("(?m)^####\\s+(.+)$", "<h4>$1</h4>");
        text = text.replaceAll("(?m)^###\\s+(.+)$", "<h3>$1</h3>");
        text = text.replaceAll("(?m)^##\\s+(.+)$", "<h2>$1</h2>");
        text = text.replaceAll("(?m)^#\\s+(.+)$", "<h1>$1</h1>");
        return text;
    }

    private static String convertUnorderedLists(String text) {
        Pattern pattern = Pattern.compile("(?m)^[*-]\\s+(.+)$");
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        boolean inList = false;

        while (matcher.find()) {
            if (!inList) {
                matcher.appendReplacement(sb, "<ul><li>$1</li>");
                inList = true;
            } else {
                matcher.appendReplacement(sb, "<li>$1</li>");
            }
        }
        matcher.appendTail(sb);

        if (inList) {
            sb.append("</ul>");
        }

        return sb.toString();
    }

    private static String convertOrderedLists(String text) {
        Pattern pattern = Pattern.compile("(?m)^\\d+\\.\\s+(.+)$");
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        boolean inList = false;

        while (matcher.find()) {
            if (!inList) {
                matcher.appendReplacement(sb, "<ol><li>$1</li>");
                inList = true;
            } else {
                matcher.appendReplacement(sb, "<li>$1</li>");
            }
        }
        matcher.appendTail(sb);

        if (inList) {
            sb.append("</ol>");
        }

        return sb.toString();
    }
}
