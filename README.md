# Accident Severity Prediction - Map Reduce
<p>
  <img src="https://img.shields.io/badge/Scala-%202.12-green" alt="alternatetext">
  <img src="https://img.shields.io/badge/Spark-3.1.2-red" alt="alternatetext">
</p>

Project for the "Scalable and Cloud Programming" course of the Alma Mater Studiorum University of Bologna.
The project aims to implement a decision tree algorithm to predict accident severity by exploiting the MapReduce paradigm.

# Abstract
In this project, we propose an implementation of C4.5 following the MapReduce framework for distributed computing. Our objective is to extend to decision trees the benefits given by this framework, such as the efficient processing of large datasets, fault tolerance, and parallelism that could greatly improve the overall performance of the algorithm.
We also implement a classic version of the algorithm and carried out tests in sequential and parallel modes.
Different size of datasets and clusters were being used to test the algorithms.\
We used Google Cloud Platform (GCP) which allowed us to test the distribuited version of the C4.5 decision tree.

## Project build

To build the project to execute in local environment you have to install sbt-assembly plugin and use the following sbt commands:
```
sbt clean
sbt assembly
```
While to run the project on cloud using Dataproc you have to use different command as the service already provides the necessary dependencies:
```
sbt clean
sbt package
```

## Instruction for running project on local environment

You can run the project .jar in the following way:
```
java -Xmx10G -jar $PATH $TRAIN_INPUT $TEST_INPUT $MODE
```
The `-Xmx10G` parameter specifies the maximum memory allocation pool for a Java Virtual Machine (JVM), while the `-jar` parameter allow you to run the .jar file.
The meaning of the variables above is the following:

- `$PATH` is the path where th .jar file is located.
- `$TRAIN_INPUT` is the training set path.
- `$TEST_INPUT` is the test set path.
- `$MODE` you can specify "seq", "par" or "spark" to execute the program in sequential, parallel or distribuited mode respectively.


# Cloud execution on GCP 

To test the algorithms on Google Cloud Platform, the first step is create a Google Cloud project.
To use correctly the project on Google CLoud Platform you have to enable the following services:

- Dataproc
- Cloud Storage

Moreover, it is necessary enable billing for the project.
We suggest installing the Google Cloud SDK for CLIs in your system for using GCP, instead using Google
Cloud Platform console. Do so following [this guide](https://cloud.google.com/sdk/docs/install).
Once you have completed all previous steps, you can manage buckets, clusters and jobs using google 
Cloud SDK for CLIs or open our Colab notebook available on this repository.

### Creating buckets and datasets uploading
All files for the project (JAR executables, csv datasets and the outputfile) need to be stored in a Cloud Storage bucket after it was created.
```
gsutil mb -l $REGION gs://$BUCKET_NAME
```
`$REGION` and `$BUCKET_NAME` can be environment variables, or you can just substitute them with the actual value.
Regions can be found [here](https://cloud.google.com/about/locations).
Beware the bucket name must be unique in the whole Google Cloud Platform, not only in your account.

### Provisioning cluster 
```
gcloud dataproc clusters create $CLUSTER_NAME --region $REGION --zone $ZONE --master-machine-type $MASTER_MACHINE_TYPE --num-workers $NUM_WORKERS --worker-machine-type $WORKER_MACHINE_TYPE
```

Also in this case, you can use environment variables or substitute them with values. The meaning of these variables is the following:

- `$CLUSTER_NAME` is the name of the cluster, you may choose one.
- `$REGION` and `$ZONE`, please follow the link in the section above.
- `$MASTER_MACHINE_TYPE` and `$WORKER_MACHINE_TYPE` can be specified by chosen one of machine types available in this list.
- `$NUM_WORKERS` is the number of total workers (the master is not included in this number) the master can utilize.

To provisioning a single node cluster, you have to specify `--single-node` parameter and remove the `--num-workers` parameter.
In oure case, we test the algorithm on three different type of cluster: a single node cluster, a 2 workers cluster and a 3 workers one.
Please considering limitations on resources available for cluster nodes based on machine types chosen (CPUs number, disk size, etc.).

### Compiling the project and uploading the files to the bucket in Cloud Storage
```
gsutil cp target/scala-2.12/finalproject_2.12-1.0.0.jar gs://$BUCKET_NAME/finalproject_2.12-1.0.0.jar
```
```
gsutil cp data/$INPUT_TRAIN gs://$BUCKET_NAME/$INPUT_TRAIN
```
```
gsutil cp data/$INPUT_TEST gs://$BUCKET_NAME/$INPUT_TEST
```
`$BUCKET_NAME` shall be the one used in the sections above for project jar file, while `$INPUT_TRAIN` and `$INPUT_TEST` are the variables that refered to training set and test set filename.

### Submit a job in Dataproc
```
gcloud dataproc jobs submit spark [--async] --cluster=$CLUSTER_NAME --region=$REGION \
--jar=gs://$BUCKET_NAME/finalproject_2.12-1.0.0.jar \
-- gs://$BUCKET_NAME/$INPUT_TRAIN gs://$BUCKET_NAME/$INPUT_TEST $MODE gs://$BUCKET_NAME/output_$DIM/
```
Use `--async` if you want to send the job and not wait for the result. The meaning of these variables is the following:
- `$CLUSTER_NAME`, `$REGION` are those defined in the above sections.
- `$BUCKET_NAME` stands for the bucket containing the jar file of the project
- `$INPUT_TRAIN` identifies the path to the bucket containing the chosen training set.
- `$INPUT_TEST` is the path to the bucket containing the chosen test set.
- `$MODE` specifies the mode chosen in which run the program.
- `$DIM` represent the dataset dimension.

### Delete cluster and buckets
Gcloud offers commands for delete clusters and buckets.
```
gcloud dataproc clusters delete $CLUSTER_NAME --region=$REGION
```

```
gcloud storage rm --recursive gs://$BUCKET_NAME
```
The meaning of these variables above is the following:
- `$BUCKET_NAME`, `$CLUSTER_NAME` and `$REGION` are those defined in the above sections.
