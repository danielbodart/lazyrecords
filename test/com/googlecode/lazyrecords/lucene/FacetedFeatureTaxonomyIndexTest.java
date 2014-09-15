package com.googlecode.lazyrecords.lucene;

import com.googlecode.lazyrecords.Keyword;
import com.googlecode.lazyrecords.Record;
import com.googlecode.lazyrecords.mappings.StringMappings;
import com.googlecode.lazyrecords.parser.StandardParser;
import com.googlecode.totallylazy.Block;
import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Predicate;
import com.googlecode.totallylazy.Predicates;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import com.googlecode.totallylazy.numbers.Numbers;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.lazyrecords.lucene.PartitionedIndex.functions.mmapDirectory;
import static com.googlecode.totallylazy.Sequences.empty;
import static com.googlecode.totallylazy.Sequences.sequence;
import static org.apache.lucene.index.DirectoryReader.open;
import static org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE;
import static org.apache.lucene.util.Version.LUCENE_4_10_0;

public class FacetedFeatureTaxonomyIndexTest {

    @Test
    public void testFacetSearch() throws Exception {
        StandardParser parser = new StandardParser();
        final Predicate<Record> queryPredicate = parser.parse("-Product:DSL -(Product:DSL AND  \"Order Type\":UnsolicitedCease) AND \"Order Status\":Created AND \"Last Created Date\" < \"2014/09/06 16:25:00\"", Sequences.<Keyword<?>>empty());

        final Lucene lucene = new Lucene(new StringMappings());
        final Query query = lucene.query(queryPredicate);
        final LuceneQueryVisitor visitor = new LuceneQueryVisitor(CaseInsensitive.luceneQueryPreprocessor());
        final Query actualQuery = visitor.visit(query);
        final Directory resultIndex = mmapDirectory(new File("/tmp/result-index")).apply("orders");
        final Directory taxonomyDirectory = mmapDirectory(new File("/tmp/taxonomy-index")).apply("orders");

        final DirectoryReader directoryReader = open(resultIndex);
        final IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
        final DirectoryTaxonomyReader directoryTaxonomyReader = new DirectoryTaxonomyReader(taxonomyDirectory);
        FacetsCollector collector = new FacetsCollector();
        final TotalHitCountCollector totalHitCountCollector = new TotalHitCountCollector();
        final Collector multiCollector = MultiCollector.wrap(totalHitCountCollector, collector);

        final long start = System.currentTimeMillis();
        Numbers.range(0, 10).each(new Block<Number>() {
            @Override
            protected void execute(Number number) throws Exception {
                indexSearcher.search(actualQuery, multiCollector);
            }
        });
        indexSearcher.search(actualQuery, multiCollector);
        final long end = System.currentTimeMillis();
        System.out.println("collector.getMatchingDocs().size() = " + totalHitCountCollector.getTotalHits());
        System.out.println("end - start = " + (end - start));


        Facets facets = new FastTaxonomyFacetCounts(directoryTaxonomyReader, new FacetsConfig(), collector);
        List<FacetResult> results = new ArrayList<FacetResult>();
        results.add(facets.getTopChildren(Integer.MAX_VALUE, "Product"));
        results.add(facets.getTopChildren(Integer.MAX_VALUE, "Last BT Code"));
        results.add(facets.getTopChildren(Integer.MAX_VALUE, "Last BT Message"));
        results.add(facets.getTopChildren(Integer.MAX_VALUE, "Code"));

        sequence(results).filter(Predicates.notNullValue()).each(new Block<FacetResult>() {
            @Override
            protected void execute(FacetResult facetResult) throws Exception {
                final LabelAndValue[] labelValues = facetResult.labelValues;
                System.out.printf("%s = %s\n", facetResult.dim, facetResult.value);
                for (LabelAndValue labelValue : labelValues) {
                    System.out.printf("%s = %s\n", labelValue.label, labelValue.value);
                }

            }
        });
    }

    @Test
    public void testFacetSearchforAllBuckets() throws Exception {
        final Directory resultIndex = mmapDirectory(new File("/tmp/result-index")).apply("orders");
        final DirectoryReader directoryReader = open(resultIndex);
        final IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
        FacetsCollector collector = new FacetsCollector();
        final TotalHitCountCollector totalHitCountCollector = new TotalHitCountCollector();
        final Collector multiCollector = MultiCollector.wrap(totalHitCountCollector);

        final Sequence<Query> bucketQueries = empty(String.class)
                .append("")
                .append("Product:DSL -(Product:DSL AND  \"Order Type\":UnsolicitedCease) AND \"Order Status\":Created AND \"Last Created Date\" < \"2014/09/06 16:25:00\"")
                .append("-Product:DSL -(Product:DSL AND  \"Order Type\":UnsolicitedCease) AND \"Order Status\":Created AND \"Last Created Date\" < \"2014/09/06 16:25:00\"")
                .append("\"Order Status\":InException AND -(Product:DSL AND  \"Order Type\":UnsolicitedCease) AND ((-Product:\"Connect DSL\" AND (\"Code\":\"system.technicalFailure\",\"system.technicalFailure.thirdParty\" OR \"Static Ip Added\":\"Error allocating Static IP\")) OR (Product:FTTC,\"Connect DSL\" AND \"Order Type\":Restrict,Reinstate) OR (Product:DSL))")
                .append("Product:\"Connect DSL\" AND \"Order Status\":InException AND \"Code\":\"duplicate.order\",\"order.inProgress\"").map(new Mapper<String, Query>() {
                    @Override
                    public Query call(String queryStr) throws Exception {
                        return getBucketQuery(queryStr);
                    }
                });


        final long start = System.currentTimeMillis();
        bucketQueries.each(new Block<Query>() {
            @Override
            protected void execute(Query query) throws Exception {
                indexSearcher.search(query, totalHitCountCollector);
            }
        });
        final long end = System.currentTimeMillis();
        System.out.println("collector.getMatchingDocs().size() = " + totalHitCountCollector.getTotalHits());
        System.out.println("end - start = " + (end - start));
    }

    private Query getBucketQuery(String queryStr) {
        StandardParser parser = new StandardParser();

        final Predicate<Record> queryPredicate = parser.parse(queryStr, Sequences.<Keyword<?>>empty());
        final Lucene lucene = new Lucene(new StringMappings());
        final LuceneQueryVisitor visitor = new LuceneQueryVisitor(CaseInsensitive.luceneQueryPreprocessor());
        final Query query = lucene.query(queryPredicate);
        return visitor.visit(query);
    }


    @Test
    public void itShouldGenerateTheTaxonomyIndex() throws Exception {
        final Directory originalIndexDirectory = mmapDirectory(new File("/tmp/original-index")).apply("orders");
        final Directory taxonomyDirectory = mmapDirectory(new File("/tmp/taxonomy-index")).apply("orders");
        final Directory resultIndex = mmapDirectory(new File("/tmp/result-index")).apply("orders");
        final IndexWriter resultIndexWriter = new IndexWriter(resultIndex, new IndexWriterConfig(LUCENE_4_10_0, new CaseInsensitive.StringPhraseAnalyzer()).setOpenMode(CREATE));
        final IndexSearcher indexSearcher = new IndexSearcher(open(originalIndexDirectory));
        final TaxonomyWriter taxonomyWriter = new DirectoryTaxonomyWriter(taxonomyDirectory, CREATE);
        final FacetsConfig facetsConfig = new FacetsConfig();
//        final FacetFields facetFields = new FacetFields(taxonomyWriter);

        final TotalHitCountCollector collector = new TotalHitCountCollector();
        indexSearcher.search(new MatchAllDocsQuery(), collector);
        final TopDocs documentsResult = indexSearcher.search(new MatchAllDocsQuery(), collector.getTotalHits());

        for (ScoreDoc scoreDoc : documentsResult.scoreDocs) {
            final Document document = indexSearcher.doc(scoreDoc.doc);
            final Document facetedDocument = new Document();
            sequence(document.getFields()).filter(new Predicate<IndexableField>() {
                @Override
                public boolean matches(IndexableField field) {
                    return !field.stringValue().isEmpty();
                }
            }).each(new Block<IndexableField>() {
                @Override
                public void execute(IndexableField indexableField) throws Exception {
                    facetedDocument.add(new FacetField(indexableField.name(), indexableField.stringValue()));
                }
            });
            sequence(document.getFields()).forEach(new Block<IndexableField>() {
                @Override
                protected void execute(IndexableField indexableField) throws Exception {
                    facetedDocument.add(indexableField);
                }
            });
            resultIndexWriter.addDocument(facetsConfig.build(taxonomyWriter, facetedDocument));
        }
        taxonomyWriter.commit();
        resultIndexWriter.commit();

        originalIndexDirectory.close();
        resultIndexWriter.close();
        resultIndex.close();
        taxonomyWriter.close();
        taxonomyDirectory.close();
    }
}
