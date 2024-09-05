import org.apache.commons.math3.util.FastMath;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.SimpleTokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Main {
    private static final Map<String, List<String>> DTerms = new HashMap<>();
    private static final Map<String, Integer> termDocumentFrequency = new HashMap<>();
    private static final Map<String, Map<Integer, List<Integer>>> positionalIndex = new HashMap<>();
    private static final int TOTAL_DOCS = 10;

    public static void main(String[] args) throws IOException {
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        PorterStemmer stemmer = new PorterStemmer();

        for (int i = 1; i <= TOTAL_DOCS; i++) {
            String fileName = i + ".txt";
            InputStream fileStream = Main.class.getClassLoader().getResourceAsStream(fileName);
            if (fileStream != null) {
                String content = readFromInputStream(fileStream);
                String[] tokens = tokenizer.tokenize(content);
                List<String> stemmedTokens = new ArrayList<>();
                for (String token : tokens) {
                    String stemmedToken = stemmer.stem(token).toLowerCase();
                    stemmedTokens.add(stemmedToken);
                }
                DTerms.put("D" + i, stemmedTokens);
                updatePositionalIndex(stemmedTokens, i);
            } else {
                System.out.println("File not found: " + fileName);
            }
        }

        Map<String, Map<String, Integer>> termFrequencies = calculateTermFrequencies();
        Map<String, Map<String, Double>> tfIdfMatrix = calculateTfIdfMatrix(termFrequencies);

        System.out.println("Term Frequencies:");
        for (Map.Entry<String, Map<String, Integer>> entry : termFrequencies.entrySet()) {
            System.out.println("Document: " + entry.getKey());
            for (Map.Entry<String, Integer> termEntry : entry.getValue().entrySet()) {
                System.out.println("Term: " + termEntry.getKey() + ", Frequency: " + termEntry.getValue());
            }
        }

        System.out.println("\nTF-IDF Matrix:");
        for (Map.Entry<String, Map<String, Double>> docEntry : tfIdfMatrix.entrySet()) {
            System.out.println("Document: " + docEntry.getKey());
            for (Map.Entry<String, Double> termEntry : docEntry.getValue().entrySet()) {
                System.out.println("Term: " + termEntry.getKey() + ", TF-IDF: " + termEntry.getValue());
            }
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter a query: ");
        String query = scanner.nextLine();

        rankDocuments(tfIdfMatrix, query, stemmer);
    }

    private static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static void updatePositionalIndex(List<String> tokens, int docId) {
        for (int pos = 0; pos < tokens.size(); pos++) {
            String term = tokens.get(pos);
            positionalIndex.computeIfAbsent(term, k -> new HashMap<>());
            positionalIndex.get(term).computeIfAbsent(docId, k -> new ArrayList<>()).add(pos + 1);
        }
    }

    private static Map<String, Map<String, Integer>> calculateTermFrequencies() {
        Map<String, Map<String, Integer>> termFrequencies = new HashMap<>();
        for (Map.Entry<String, List<String>> doc : DTerms.entrySet()) {
            String docId = doc.getKey();
            List<String> terms = doc.getValue();
            Map<String, Integer> docTermFreq = new HashMap<>();
            for (String term : terms) {
                docTermFreq.put(term, docTermFreq.getOrDefault(term, 0) + 1);
                termDocumentFrequency.put(term, termDocumentFrequency.getOrDefault(term, 0) + 1);
            }
            termFrequencies.put(docId, docTermFreq);
        }
        return termFrequencies;
    }

    private static Map<String, Map<String, Double>> calculateTfIdfMatrix(Map<String, Map<String, Integer>> termFrequencies) {
        Map<String, Map<String, Double>> tfIdfMatrix = new HashMap<>();
        for (Map.Entry<String, Map<String, Integer>> docEntry : termFrequencies.entrySet()) {
            String docId = docEntry.getKey();
            Map<String, Double> tfIdfValues = new HashMap<>();
            for (Map.Entry<String, Integer> termEntry : docEntry.getValue().entrySet()) {
                String term = termEntry.getKey();
                int tf = termEntry.getValue();
                int df = termDocumentFrequency.getOrDefault(term, 1);
                double idf = FastMath.log10((double) TOTAL_DOCS / df);
                double tfIdf = tf * idf;
                tfIdfValues.put(term, tfIdf);
            }
            tfIdfMatrix.put(docId, tfIdfValues);
        }
        return tfIdfMatrix;
    }

    private static void rankDocuments(Map<String, Map<String, Double>> tfIdfMatrix, String query, PorterStemmer stemmer) {
        Map<String, Double> queryTfIdf = calculateQueryTfIdf(query, stemmer);
        Map<Integer, Double> docScores = new HashMap<>();

        for (int docId = 1; docId <= TOTAL_DOCS; docId++) {
            String docKey = "D" + docId;
            Map<String, Double> docTfIdf = tfIdfMatrix.get(docKey);
            double similarity = calculateCosineSimilarity(queryTfIdf, docTfIdf);
            docScores.put(docId, similarity);
        }

        System.out.println("\nRanking Results:");
        docScores.entrySet()
                .stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(entry -> {
                    System.out.println("Document D" + entry.getKey() + " Similarity: " + entry.getValue());
                });
    }

    private static double calculateCosineSimilarity(Map<String, Double> queryVector, Map<String, Double> docVector) {
        double dotProduct = 0.0;
        double queryMagnitude = 0.0;
        double docMagnitude = 0.0;

        for (String term : queryVector.keySet()) {
            double queryTfIdf = queryVector.getOrDefault(term, 0.0);
            double docTfIdf = docVector.getOrDefault(term, 0.0);

            dotProduct += queryTfIdf * docTfIdf;
            queryMagnitude += queryTfIdf * queryTfIdf;
        }

        for (double value : docVector.values()) {
            docMagnitude += value * value;
        }

        if (queryMagnitude == 0 || docMagnitude == 0) {
            return 0.0;
        }

        return dotProduct / (FastMath.sqrt(queryMagnitude) * FastMath.sqrt(docMagnitude));
    }

    private static Map<String, Double> calculateQueryTfIdf(String query, PorterStemmer stemmer) {
        String[] queryTerms = query.split(" ");
        Map<String, Integer> queryTermFrequency = new HashMap<>();

        for (String term : queryTerms) {
            String stemmedTerm = stemmer.stem(term).toLowerCase();
            queryTermFrequency.put(stemmedTerm, queryTermFrequency.getOrDefault(stemmedTerm, 0) + 1);
        }

        Map<String, Double> queryTfIdf = new HashMap<>();
        for (String term : queryTermFrequency.keySet()) {
            int tf = queryTermFrequency.get(term);
            int df = termDocumentFrequency.getOrDefault(term, 1);
            double idf = FastMath.log10((double) TOTAL_DOCS / df);
            queryTfIdf.put(term, tf * idf);
        }

        return queryTfIdf;
    }

    private static List<Integer> processPhraseQuery(String query, PorterStemmer stemmer) {
        String[] queryTerms = query.split(" ");
        List<Integer> resultDocs = new ArrayList<>();
        for (int docId = 1; docId <= TOTAL_DOCS; docId++) {
            boolean found = true;
            for (String term : queryTerms) {
                String stemmedTerm = stemmer.stem(term).toLowerCase();
                if (!positionalIndex.containsKey(stemmedTerm) || !positionalIndex.get(stemmedTerm).containsKey(docId)) {
                    found = false;
                    break;
                }
            }
            if (found) {
                resultDocs.add(docId);
            }
        }
        return resultDocs;
    }

    private static List<Integer> processBooleanQuery(String query, PorterStemmer stemmer) {
        String[] queryTerms = query.split(" ");
        Set<Integer> resultDocs = new HashSet<>();
        String currentOperator = "AND";

        for (String token : queryTerms) {
            String stemmedToken = stemmer.stem(token).toLowerCase();

            if (token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR") || token.equalsIgnoreCase("NOT")) {
                currentOperator = token.toUpperCase();
            } else {
                Set<Integer> termDocs = new HashSet<>();
                if (positionalIndex.containsKey(stemmedToken)) {
                    termDocs.addAll(positionalIndex.get(stemmedToken).keySet());
                }

                if (currentOperator.equals("AND")) {
                    if (resultDocs.isEmpty()) {
                        resultDocs.addAll(termDocs);
                    } else {
                        resultDocs.retainAll(termDocs);
                    }
                } else if (currentOperator.equals("OR")) {
                    resultDocs.addAll(termDocs);
                } else if (currentOperator.equals("NOT")) {
                    resultDocs.removeAll(termDocs);
                }
            }
        }

        return new ArrayList<>(resultDocs);
    }
}
