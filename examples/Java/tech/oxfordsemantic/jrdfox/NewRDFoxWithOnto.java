// RDFox v1.0 Copyright 2013 Oxford University Innovation Limited and subsequent improvements Copyright 2017-2019 by Oxford Semantic Technologies Limited.

package tech.oxfordsemantic.jrdfox;

import tech.oxfordsemantic.jrdfox.client.*;
import tech.oxfordsemantic.jrdfox.exceptions.JRDFoxException;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NewRDFoxWithOnto {

    public static void main(String[] args) throws Exception {
        long startTime=System.currentTimeMillis();   //获取开始时间
        try (ServerConnection serverConnection = ConnectionFactory.newServerConnection("rdfox:local", "", "")) {

            // We create a data store of type "par-complex-nn".
            serverConnection.createDataStore("example", "par-complex-nn", Collections.emptyMap());

            // We next specify how many threads the server should use during import of data and reasoning.
            System.out.println("Setting the number of threads...");
            serverConnection.setNumberOfThreads(6);

            // We connect to the data store.
            try (DataStoreConnection dataStoreConnection = serverConnection.newDataStoreConnection("example")) {

                // We next import the RDF data into the store. At present, only Turtle/N-triples files are supported.
                // At the moment, please convert RDF/XML files into Turtle format to load into JRDFox.
                System.out.println("Importing RDF data...");
                try (InputStream inputStream = new BufferedInputStream(JRDFoxDemo.class.getResourceAsStream("data/go-triples-new.nt"))) {
                    dataStoreConnection.importData(UpdateType.ADDITION, Prefixes.s_emptyPrefixes, inputStream);
                }

                // RDFox manages data in several domains.
                //
                // - EDB are the explicitly stated facts.
                //
                // - IDB facts are the EDB facts plus all of their consequences. This is what normally should be
                //   queried -- that is, these are the "current" facts in the store.
                //
                // - IDBrep is different from IDB only if optimized equality reasoning is used. In that case, RDFox
                //   will select for each set of equal resources one representative, and IDBrep then consists of the
                //   IDB facts that contain just the representative resources.
                //
                // - IDBrepNoEDB is equal IDBrep minus EDB.
                //
                // The domain must be specified in various places where queries are evaluated. If a query domain is not
                // specified, the IDB domain is used.
                System.out.println("Number of tuples after import: " + getTriplesCount(dataStoreConnection, "IDB"));

                // SPARQL queries can be evaluated in several ways. One option is to have the query result be written to
                // an output stream in one of the supported formats.
                dataStoreConnection.evaluateQuery(Prefixes.s_emptyPrefixes, "SELECT DISTINCT ?Y WHERE { ?X ?Y ?Z }", Collections.emptyMap(), System.out, "application/sparql-results+json");

                // We now add the ontology and the custom rules to the data.

                // In this example, the rules are kept in a file separate from the ontology. JRDFox supports
                // SWRL rules; thus, it is possible to store the rules into the OWL ontology. However, JRDFox
                // does not (yet) support SWRL built-in predicates, so any rules involving built-in predicates
                // should be written in the native format of RDFox. The format of the rules should be obvious
                // from the example. Built-in functions are invoked using the BIND and FILTER syntax from
                // SPARQL, and most SPARQL built-in functions are supported.

                System.out.println("Adding the ontology to the store...");
                try (InputStream inputStream = new BufferedInputStream(JRDFoxDemo.class.getResourceAsStream("data/go.owl"))) {
                    dataStoreConnection.importData(UpdateType.ADDITION, Prefixes.s_defaultPrefixes, inputStream);
                }

                System.out.println("Importing rules from a file...");
                try (InputStream inputStream = new BufferedInputStream(JRDFoxDemo.class.getResourceAsStream("data/go-rules.txt"))) {
                    dataStoreConnection.importData(UpdateType.ADDITION, Prefixes.s_emptyPrefixes, inputStream);
                }
                System.out.println("Number of tuples after materialization: " + getTriplesCount(dataStoreConnection, "IDB"));

                // We now evaluate the same query as before, but we do so using a cursor, which provides us with
                // programmatic access to individual query results.
                try (Cursor cursor = dataStoreConnection.createCursor(Prefixes.s_emptyPrefixes, "SELECT DISTINCT ?Y WHERE { ?X ?Y ?Z }", Collections.emptyMap())) {
                    int numberOfRows = 0;
                    System.out.println();
                    System.out.println("=======================================================================================");
                    int arity = cursor.getArity();
                    // We iterate trough the result tuples
                    for (long multiplicity = cursor.open(); multiplicity != 0; multiplicity = cursor.advance()) {
                        // We iterate trough the terms of each tuple
                        for (int termIndex = 0; termIndex < arity; ++termIndex) {
                            if (termIndex != 0)
                                System.out.print("  ");
                            // For each term we get a Resource object that contains the lexical form and the data type of the term.
                            // One can also access terms as Resource objects from the tech.oxfordsemantic.jrdfox.logic package using
                            // the method Cursor.getResource(int termIndex). Using objects from the tech.oxfordsemantic.jrdfox.logic
                            // package has the benefit of ensuring that at any point each term is represented by at most one Java
                            // object. This benefit, however, comes at a price, since, unlike in the case of Resource objects, the
                            // creation of Resource objects involves a hash table lookup, which in some cases can lead to a significant
                            // overhead.
                            ResourceValue resource = cursor.getResourceValue(termIndex);
                            System.out.print(resource.toString(Prefixes.s_defaultPrefixes));
                        }
                        System.out.print(" * ");
                        System.out.print(multiplicity);
                        System.out.println();
                        ++numberOfRows;
                    }
                    // Since the iterator is exhausted, it does not need to be closed.
                    System.out.println("---------------------------------------------------------------------------------------");
                    System.out.println("  The number of rows returned: " + numberOfRows);
                    System.out.println("=======================================================================================");
                    System.out.println();
                }

                // RDFox supports incremental reasoning. One can import facts into the store incrementally by
                // calling DataStoreConnection.importDataFiles() with additional argument UpdateType.ADDITION.
//                System.out.println("Import triples for incremental reasoning");
//                try (InputStream inputStream = new BufferedInputStream(JRDFoxDemo.class.getResourceAsStream("data/lubm1-new.ttl"))) {
//                    dataStoreConnection.importData(UpdateType.ADDITION, Prefixes.s_emptyPrefixes, inputStream);
//                }
                // Adding the rules/facts changes the number of triples. Note that the store is updated incrementally.
                System.out.println("Number of tuples after addition: " + getTriplesCount(dataStoreConnection, "IDB"));
                // One can export the facts from the current store into a file as follows
                File finalFactsFile = File.createTempFile("final-facts", ".ttl", new File("./"));
                System.out.print("Exporting facts to file '" + finalFactsFile + "' ... ");
                try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(finalFactsFile))) {
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("fact-domain", "IDB");
                    dataStoreConnection.exportData(Prefixes.s_defaultPrefixes, outputStream , "application/n-triples", parameters);
                }
                System.out.println("done.");
            }
        }
        System.out.println("This is the end of the example!");
        long endTime=System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： "+(endTime-startTime)+"ms");

    }

    protected static Map<String, String> getParameters(String... keyValuePairs) {
        Map<String, String> parameters = new HashMap<String, String>();
        for (int index = 0; index < keyValuePairs.length; index += 2)
            parameters.put(keyValuePairs[index], keyValuePairs[index + 1]);
        return parameters;
    }

    protected static long getTriplesCount(DataStoreConnection dataStoreConnection, String queryDomain) throws JRDFoxException {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("fact-domain", queryDomain);
        try (Cursor cursor = dataStoreConnection.createCursor(Prefixes.s_emptyPrefixes, "SELECT ?X ?Y ?Z WHERE{ ?X ?Y ?Z }", parameters)) {
            dataStoreConnection.beginTransaction(TransactionType.READ_ONLY);
            try {
                long result = 0;
                for (long multiplicity = cursor.open(); multiplicity != 0; multiplicity = cursor.advance())
                    result += multiplicity;
                return result;
            }
            finally {
                dataStoreConnection.rollbackTransaction();
            }
        }
    }
}