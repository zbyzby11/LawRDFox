package tech.oxfordsemantic.jrdfox;

import tech.oxfordsemantic.jrdfox.client.*;
import tech.oxfordsemantic.jrdfox.exceptions.JRDFoxException;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NewRDFox_dupin {

    public static void main(String[] args) throws Exception {
        long startTime=System.currentTimeMillis();   //获取开始时间
        try (ServerConnection serverConnection = ConnectionFactory.newServerConnection("rdfox:local", "", "")) {

            // We create a data store of type "par-complex-nn".
            serverConnection.createDataStore("example", "par-complex-nn", Collections.emptyMap());

            // We next specify how many threads the server should use during import of data and reasoning.
            System.out.println("Setting the number of threads...");
            serverConnection.setNumberOfThreads(12);

            // We connect to the data store.
            try (DataStoreConnection dataStoreConnection = serverConnection.newDataStoreConnection("example")) {

                // We next import the RDF data into the store. At present, only Turtle/N-triples files are supported.
                // At the moment, please convert RDF/XML files into Turtle format to load into JRDFox.
                System.out.println("Importing RDF data...");
                try (InputStream inputStream = new BufferedInputStream(JRDFoxDemo.class.getResourceAsStream("data/legal_dupin_1yi.nt"))) {
                    dataStoreConnection.importData(UpdateType.ADDITION, Prefixes.s_emptyPrefixes, inputStream);
                }

                System.out.println("Number of tuples after import: " + getTriplesCount(dataStoreConnection, "IDB"));

                dataStoreConnection.evaluateQuery(Prefixes.s_emptyPrefixes, "SELECT DISTINCT ?Y WHERE { ?X ?Y ?Z }", Collections.emptyMap(), System.out, "application/sparql-results+json");

                System.out.println("Importing rules from a file...");
                try (InputStream inputStream = new BufferedInputStream(JRDFoxDemo.class.getResourceAsStream("data/rules.txt"))) {
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

                System.out.println("Number of tuples after addition: " + getTriplesCount(dataStoreConnection, "IDB"));
                // One can export the facts from the current store into a file as follows
//                File finalFactsFile = File.createTempFile("final-facts", ".ttl", new File("./"));
//                System.out.print("Exporting facts to file '" + finalFactsFile + "' ... ");
//                try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(finalFactsFile))) {
//                    Map<String, String> parameters = new HashMap<String, String>();
//                    parameters.put("fact-domain", "IDB");
//                    dataStoreConnection.exportData(Prefixes.s_defaultPrefixes, outputStream , "application/n-triples", parameters);
//                }
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
