package ca.fuwafuwa.kaku.Search;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

import java.util.List;

import ve.Parse;
import ve.Word;

public class VeParser {

    private final Tokenizer tokenizer;

    VeParser() {
        this.tokenizer = new Tokenizer();
    }

    public List<Word> parse(String japanese) {
        List<Token> tokenList = this.tokenizer.tokenize(japanese);
        Token[] tokenArray = tokenList.toArray(new Token[0]);
        return new Parse(tokenArray).words();
    }
}
