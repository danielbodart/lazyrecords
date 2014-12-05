package com.googlecode.lazyrecords.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;

import java.io.Reader;

public class CaseInsensitive {

    public static Analyzer queryAnalyzer() {
        return new StringPhraseAnalyzer();
    }

    public static LuceneQueryPreprocessor luceneQueryPreprocessor() {
        return new LowerCasingLuceneQueryPreprocessor();
    }


    public static class StringPhraseAnalyzer extends Analyzer {
        protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
            Tokenizer tok = new KeywordTokenizer(reader);
            TokenFilter filter = new LowerCaseFilter(tok);
            filter = new TrimFilter(filter);
            return new TokenStreamComponents(tok, filter);
        }
    }
}