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
 mvn -e exec:java -Dexec.mainClass="org.offline_phase.MainClass"  -Dexec.args="-p -c"
```

```
[-p] apply stemming and stopword removal 
[-c] index compression
```
## Command Line Interface

```
 mvn -e exec:java -Dexec.mainClass="org.online_phase.MainClass"  -Dexec.args="-p -c -k=20 -s=bm25 -mode=c"
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
