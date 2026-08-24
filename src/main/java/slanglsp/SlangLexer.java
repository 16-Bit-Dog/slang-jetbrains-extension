package slanglsp;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class SlangLexer extends LexerBase {
    private CharSequence buffer;
    private int startOffset;
    private int endOffset;
    private int position;
    private int tokenStart;
    private int tokenEnd;
    private IElementType tokenType;

    public static final IElementType KEYWORD = new IElementType("KEYWORD", SlangLanguage.INSTANCE);
    public static final IElementType TYPE = new IElementType("TYPE", SlangLanguage.INSTANCE);
    public static final IElementType FUNCTION = new IElementType("FUNCTION", SlangLanguage.INSTANCE);
    public static final IElementType VARIABLE = new IElementType("VARIABLE", SlangLanguage.INSTANCE);
    public static final IElementType STRING = new IElementType("STRING", SlangLanguage.INSTANCE);
    public static final IElementType NUMBER = new IElementType("NUMBER", SlangLanguage.INSTANCE);
    public static final IElementType COMMENT = new IElementType("COMMENT", SlangLanguage.INSTANCE);
    public static final IElementType TEXT = new IElementType("TEXT", SlangLanguage.INSTANCE);

    private static final Set<String> KEYWORDS = Set.of(
            "struct", "interface", "enum", "extension", "typealias", "associatedtype", "let", "var", "void", "return",
            "if", "else", "for", "while", "do", "switch", "case", "default", "break", "continue", "static", "const",
            "true", "false", "in", "out", "inout", "__init", "operator", "public", "private", "protected", "internal",
            "import", "module", "implementing", "property", "get", "set", "yield", "defer", "as", "is", "reinterpret",
            "sizeof", "alignof", "typeof", "groupshared", "uniform", "inline", "extern", "export", "class",
            "namespace", "using", "typedef", "this", "discard", "null", "nullptr", "mutating", "nonmutating"
    );

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.position = startOffset;
        advance();
    }

    @Override
    public int getState() { return 0; }

    @Nullable
    @Override
    public IElementType getTokenType() { return tokenType; }

    @Override
    public int getTokenStart() { return tokenStart; }

    @Override
    public int getTokenEnd() { return tokenEnd; }

    @Override
    public void advance() {
        if (position >= endOffset) {
            tokenType = null;
            return;
        }

        tokenStart = position;
        char c = buffer.charAt(position);

        if (Character.isWhitespace(c)) {
            while (position < endOffset && Character.isWhitespace(buffer.charAt(position))) position++;
            tokenType = TEXT;
        } else if (c == '/' && position + 1 < endOffset && buffer.charAt(position + 1) == '/') {
            while (position < endOffset && buffer.charAt(position) != '\n') position++;
            tokenType = COMMENT;
        } else if (c == '"' || c == '\'') {
            char quote = c;
            position++;
            while (position < endOffset && buffer.charAt(position) != quote) {
                if (buffer.charAt(position) == '\\' && position + 1 < endOffset) position++;
                position++;
            }
            if (position < endOffset) position++;
            tokenType = STRING;
        } else if (Character.isDigit(c)) {
            while (position < endOffset && (Character.isDigit(buffer.charAt(position)) || buffer.charAt(position) == '.')) position++;
            tokenType = NUMBER;
        } else if (Character.isLetter(c) || c == '_') {
            while (position < endOffset && (Character.isLetterOrDigit(buffer.charAt(position)) || buffer.charAt(position) == '_')) position++;
            String word = buffer.subSequence(tokenStart, position).toString();

            if (KEYWORDS.contains(word) || word.matches("(int|float|uint|bool|half|double)[1-4]?(x[1-4])?")) {
                tokenType = KEYWORD;
            } else {
                int peek = position;
                while (peek < endOffset && Character.isWhitespace(buffer.charAt(peek))) peek++;
                boolean isFunction = peek < endOffset && buffer.charAt(peek) == '(';
                
                if (isFunction) {
                    tokenType = FUNCTION;
                } else if (Character.isUpperCase(word.charAt(0))) {
                    tokenType = TYPE;
                } else {
                    tokenType = VARIABLE;
                }
            }
        } else {
            position++;
            tokenType = TEXT;
        }
        tokenEnd = position;
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() { return buffer; }

    @Override
    public int getBufferEnd() { return endOffset; }
}
