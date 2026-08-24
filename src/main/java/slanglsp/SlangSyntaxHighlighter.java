package slanglsp;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import slanglsp.highlighting.SlangSyntaxHighlighterColors;

public class SlangSyntaxHighlighter extends SyntaxHighlighterBase {
    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new SlangLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (tokenType.equals(SlangLexer.KEYWORD)) return new TextAttributesKey[]{SlangSyntaxHighlighterColors.KEYWORD};
        if (tokenType.equals(SlangLexer.TYPE)) return new TextAttributesKey[]{SlangSyntaxHighlighterColors.TYPE_NAME};
        if (tokenType.equals(SlangLexer.FUNCTION)) return new TextAttributesKey[]{SlangSyntaxHighlighterColors.FUNCTION_CALL};
        if (tokenType.equals(SlangLexer.VARIABLE)) return new TextAttributesKey[]{SlangSyntaxHighlighterColors.VARIABLE};
        if (tokenType.equals(SlangLexer.STRING)) return new TextAttributesKey[]{SlangSyntaxHighlighterColors.STRING};
        if (tokenType.equals(SlangLexer.NUMBER)) return new TextAttributesKey[]{SlangSyntaxHighlighterColors.NUMBER};
        if (tokenType.equals(SlangLexer.COMMENT)) return new TextAttributesKey[]{SlangSyntaxHighlighterColors.LINE_COMMENT};
        return new TextAttributesKey[0];
    }
}
