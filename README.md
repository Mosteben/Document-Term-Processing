# Document Retrieval System

## Overview

This project is a Document Retrieval System implemented in Java. It offers functionalities for text processing and information retrieval, including tokenization, stemming, term frequency calculation, and ranking documents based on TF-IDF scores. It also supports phrase queries and Boolean queries for advanced search capabilities.

## Features

- **Text Processing**: Reads and processes text files, applies tokenization, and performs stemming.
- **Term Frequency Calculation**: Computes the frequency of terms within each document.
- **TF-IDF Calculation**: Calculates Term Frequency-Inverse Document Frequency (TF-IDF) scores to rank documents based on their relevance to a query.
- **Cosine Similarity**: Computes similarity between query and documents using cosine similarity.
- **Query Processing**:
  - **Phrase Queries**: Finds documents containing specific sequences of terms.
  - **Boolean Queries**: Supports AND, OR, and NOT operations to refine search results.

## Requirements

- Java Development Kit (JDK) 8 or higher
- Apache Commons Math library (`commons-math3`)
- Apache OpenNLP library for tokenization and stemming (`opennlp-tools`)

## Getting Started

### Setup

1. **Clone the Repository**:

    ```bash
    git clone https://github.com/your-username/document-retrieval-system.git
    cd document-retrieval-system
    ```

2. **Add Dependencies**:

    Add the following dependencies to your project:
    - Apache Commons Math
    - Apache OpenNLP

    For Maven users, you can add these dependencies to your `pom.xml`:

    ```xml
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-math3</artifactId>
        <version>3.6.1</version>
    </dependency>
    <dependency>
        <groupId>opennlp</groupId>
        <artifactId>opennlp-tools</artifactId>
        <version>1.9.3</version>
    </dependency>
    ```

### Usage

1. **Prepare Documents**:

    Place your text documents (`1.txt`, `2.txt`, ..., `10.txt`) in the `resources` directory. Ensure that each file contains plain text for processing.

2. **Run the Application**:

    Compile and run the `Main` class:

    ```bash
    javac -cp "path/to/opennlp-tools.jar:path/to/commons-math3.jar" src/Main.java
    java -cp "path/to/opennlp-tools.jar:path/to/commons-math3.jar:." Main
    ```

3. **Interact with the System**:

    - The application will display term frequencies and the TF-IDF matrix for each document.
    - Enter a query when prompted to receive ranked results based on cosine similarity.

### Examples

- **Phrase Query**: Input a sequence of terms to find documents containing that exact phrase.
- **Boolean Query**: Use operators like AND, OR, and NOT to refine your search.

    Example query: `term1 AND term2 OR term3 NOT term4`

## Code Overview

- **Main Class**: Contains the main logic for processing documents, calculating term frequencies, and ranking documents.
- **Text Processing**:
  - **Tokenization**: Splits text into individual terms.
  - **Stemming**: Reduces terms to their base or root form.
- **TF-IDF Calculation**: Computes the relevance of terms in documents relative to the entire corpus.
- **Cosine Similarity**: Measures the similarity between query and document vectors.
- **Query Processing**:
  - **Phrase Query**: Checks if documents contain the exact phrase.
  - **Boolean Query**: Applies Boolean logic to filter documents.
