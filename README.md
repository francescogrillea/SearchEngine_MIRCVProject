# Environment Setup
``` 
sudo apt install maven 
```

# Install SearchEngine
```
mvn package
```

# Run 

## Build Index
```
java -cp target/SearchEngine_Project-1.0-SNAPSHOT.jar org.offline_phase.MainClass -c -p
```

```
[-p] apply stemming and stopword removal 
[-c] index compression
```
## Command Line Interface

```
java -cp target/SearchEngine_Project-1.0-SNAPSHOT.jar org.online_phase.MainClass -c -p -s=bm25 -k=20
```

```
[-p] apply stemming and stopword removal 
[-c] index compression
[-s=bm25] use BM25 scoring function
[-k=20] retrieve the top 20 document
```
## Evaluation using TrecEval