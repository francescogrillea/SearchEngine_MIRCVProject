# Environment Setup
``` 
sudo apt install maven 
```

# Install SearchEngine
```
mvn package
```

## Setup Environment
```
mkdir data/intermediate_postings/
mkdir data/intermediate_postings/index/
mkdir data/intermediate_postings/lexicon/
mkdir data/intermediate_postings/doc_index/
```

## Cleanup Environment
```
cd data
chmod +x cleanup.sh
./cleanup.sh
cd ..
```

# Run 

## Build Index
```
java -cp target/SearchEngine_Project-1.0-SNAPSHOT.jar org.offline_phase.MainClass -c -p
```

oppure 

```
 mvn -e exec:java -Dexec.mainClass="org.offline_phase.MainClass"  -Dexec.args="-p -c"
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
[-k=20] retrieve the top 20 document
[-s=bm25] use BM25 scoring function (otherwise TFIDF will be applied)
[-mode=c] use DAAT in conjunctive mode
[-mode=d] use DAAT in disjunctive mode (if no mode is specified, MaxScore si used
```
## Evaluation using TrecEval
