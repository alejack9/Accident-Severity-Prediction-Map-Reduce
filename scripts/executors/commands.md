## create bucket
```
gsutil mb -l europe-west1 gs://accidental-severity-prediction-source-j
```

## move jar in bucket
```
gsutil cp .\target\scala-2.12\accidentSeverityPrediction_2.12-1.0.0.jar gs://accidental-severity-prediction-source-j/AccidentSeverityPrediction-assembly-1.0.0.jar
```

## cp input
```
gsutil cp data/input_train_*_binned.csv gs://accidental-severity-prediction-source-j/
gsutil cp data/input_test_*_binned.csv gs://accidental-severity-prediction-source-j/
```

## create cluster with 3 workers
```
gcloud dataproc clusters create accidental-severity-prediction-cluster --enable-component-gateway --region europe-west1 --zone europe-west1-b --master-machine-type n1-standard-2 --num-workers 3 --worker-machine-type n1-standard-4
```

## run job:
Example with dataset of size 262144:

```
gcloud "dataproc" "jobs" "submit" "spark" "--async" "--cluster=accidental-severity-prediction-cluster" "--region=europe-west1" 
"--jar=gs://accidental-severity-prediction-source-j/AccidentSeverityPrediction-assembly-1.0.0.jar" "--" "gs://accidental-severity-prediction-source-j/input_train_262144_binned.csv" "gs://accidental-severity-prediction-source-j/input_test_262144_binned.csv" "spark" "gs://accidental-severity-prediction-source-j/output_3_262144/"
```

## copy results:
```
mkdir -p data/cloud_logs/
gsutil cp -r gs://accidental-severity-prediction-source-j/output_* data/cloud_logs/
```

## delete old data:
```
gsutil -m rm -r gs://accidental-severity-prediction-source-j/input_train_4096_binned.csv
gsutil -m rm -r gs://accidental-severity-prediction-source-j/input_test_4096_binned.csv
gsutil -m rm -r gs://accidental-severity-prediction-source-j/output_4096
```
