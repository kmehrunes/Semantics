# Semantics

A Java project which makes it easier to analyze text and extract features from it.

It mainly uses [Stanford's CoreNLP](https://stanfordnlp.github.io/CoreNLP/) and [DeepLearning4j](https://deeplearning4j.org). It also uses [Princton's WordNet](http://wordnet.princeton.edu) but that could be changed soon.

## Example
In order to create a documents processor you need to provide a properties object with the pre-processing steps and what you want to extract. This could be done as follows:

```Java
Properties operations = new ExtractionProcess()
                .lemmatize()
                .resolveCorefs()
                .removePunctuation()
                .gloVe()
                .relations()
                .ngrams(2)
                .entities()
                .get();
```

This tells the processor to convert words to their lemmas, resolve co-references, and remove punctuations as pre-processing steps. It also tells it return the following: [Global Vector (GloVe)](https://nlp.stanford.edu/projects/glove/) representation, relations (subjec-predicate-object tuples), n-grams of 2, and a list of all entities.

Then to instantiate a document processors we need to feed it the properties object as well as tell it the GloVe model to load.

```Java
DocumentProcessor processor = new DocumentProcessor(operations)
                .useDefaultGloVe(pathToGloVeFile);
```

Now we can use it to analyze some text. Note that the text will be split into sentences by default.

```Java
DocumentFeatures features = processor.processDocument(text);
```

You can get a JSON representation of the returned features object by using **features.toJson()**.

## Provided Features
1. **N-grams**: A list of n-grams in the text. This isn't cross-sentence so, for example, in ".. term3. term4 .." term3_term4 isn't a regonized n-gram. It also exclusively returns n-grams of 'n', without including all n-grams of < n.
2. **Parts of speech**: A list of parts of speech tags which appeared in the text in order.
3. **Sentiment**: The overall sentiment score of the text, you can provide your own sentiment scorer or use the default one which comes with CoreNLP. It currently gives a single value for the entire text but later it might return a score for each sentence.
4. **Entities**: All named entities in the text and their types.
5. **Relations**: A list of subject-predicate-object tuples in the text (this isn't very accurate so make sure that it works for your use case by manually examining the results first)
6. **Word2Vec**: A vector representation of the text as the mean vector of the vector representations of every word in the document. Later the mean could be changed for something more representative.
7. **GloVe**: Same as Word2Vec but uses GloVe models instead.
8. **TF-IDF**: Returns a TF-IDF vector of the text. *You need to specify your own IDF scores since they depend on the collection*
9. The rest explain themselves.
