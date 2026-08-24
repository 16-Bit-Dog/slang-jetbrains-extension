package slanglsp.highlighting;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;
import java.awt.Color;
import java.awt.Font;

public class SlangSyntaxHighlighterColors {

    // VS Code Teal
    public static final TextAttributesKey TYPE_NAME = TextAttributesKey.createTextAttributesKey(
            "SLANG_TYPE_NAME",
            new TextAttributes(new JBColor(new Color(0x267f99), new Color(0x4ec9b0)), null, null, null, Font.PLAIN)
    );

    // VS Code Yellow
    public static final TextAttributesKey FUNCTION_CALL = TextAttributesKey.createTextAttributesKey(
            "SLANG_FUNCTION_CALL",
            new TextAttributes(new JBColor(new Color(0x795e26), new Color(0xdcdcaa)), null, null, null, Font.PLAIN)
    );

    // VS Code Light Blue
    public static final TextAttributesKey VARIABLE = TextAttributesKey.createTextAttributesKey(
            "SLANG_VARIABLE",
            new TextAttributes(new JBColor(new Color(0x001080), new Color(0x9cdcfe)), null, null, null, Font.PLAIN)
    );

    // VS Code Bright Blue for Constants
    public static final TextAttributesKey CONSTANT = TextAttributesKey.createTextAttributesKey(
            "SLANG_CONSTANT",
            new TextAttributes(new JBColor(new Color(0x0070c1), new Color(0x4fc1ff)), null, null, null, Font.PLAIN)
    );

    // VS Code Blue for Keywords
    public static final TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey(
            "SLANG_KEYWORD",
            new TextAttributes(new JBColor(new Color(0x0000ff), new Color(0x569cd6)), null, null, null, Font.PLAIN)
    );

    // VS Code Purple for Control flow / Macros
    public static final TextAttributesKey MACRO_KEYWORD = TextAttributesKey.createTextAttributesKey(
            "SLANG_MACRO_KEYWORD",
            new TextAttributes(new JBColor(new Color(0xaf00db), new Color(0xc586c0)), null, null, null, Font.PLAIN)
    );

    // VS Code Orange/Red for Strings
    public static final TextAttributesKey STRING = TextAttributesKey.createTextAttributesKey(
            "SLANG_STRING",
            new TextAttributes(new JBColor(new Color(0xa31515), new Color(0xce9178)), null, null, null, Font.PLAIN)
    );

    // VS Code Green for Numbers
    public static final TextAttributesKey NUMBER = TextAttributesKey.createTextAttributesKey(
            "SLANG_NUMBER",
            new TextAttributes(new JBColor(new Color(0x098658), new Color(0xb5cea8)), null, null, null, Font.PLAIN)
    );

    // VS Code Dark Green for Comments
    public static final TextAttributesKey LINE_COMMENT = TextAttributesKey.createTextAttributesKey(
            "SLANG_LINE_COMMENT",
            new TextAttributes(new JBColor(new Color(0x008000), new Color(0x6a9955)), null, null, null, Font.PLAIN)
    );
}
