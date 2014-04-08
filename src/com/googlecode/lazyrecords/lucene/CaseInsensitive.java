package com.googlecode.lazyrecords.lucene;

import com.googlecode.totallylazy.Function2;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import java.io.Reader;

public class CaseInsensitive {

    public static LuceneStorage storage(Directory directory, SearcherPool searcherPool) {
        return new OptimisedStorage(directory, Version.LUCENE_45, new StringPhraseAnalyzer(), IndexWriterConfig.OpenMode.CREATE_OR_APPEND, searcherPool);
    }

    public static Function2<Directory, SearcherPool, LuceneStorage> storage() {
        return new Function2<Directory, SearcherPool, LuceneStorage>() {
            @Override
            public LuceneStorage call(Directory directory, SearcherPool searcherPool) throws Exception {
                return storage(directory, searcherPool);
            }
        };
    }

    public static QueryVisitor queryVisitor() {
        return new LowerCasingQueryVisitor();
    }


    public static class StringPhraseAnalyzer extends Analyzer {
        protected TokenStreamComponents createComponents (String fieldName, Reader reader) {
            Tokenizer tok = new KeywordTokenizer(reader);
            TokenFilter filter = new LowerCaseFilter(Version.LUCENE_45, tok);
            filter = new TrimFilter(Version.LUCENE_45, filter);
            return new TokenStreamComponents(tok, filter);
        }
    }
}
