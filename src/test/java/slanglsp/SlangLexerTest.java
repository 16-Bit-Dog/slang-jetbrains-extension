package slanglsp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SlangLexerTest {
    @Test
    public void testLexerHeuristics() {
        SlangLexer lexer = new SlangLexer();
        String code = "struct Tensor { int a; void main() { } }";
        lexer.start(code);
        assertNotNull(lexer.getTokenType(), "Lexer should start with a valid token");
    }
}
