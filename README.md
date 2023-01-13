# Accident Severity Prediction - Map Reduce
<p>
  <img src="https://img.shields.io/badge/Scala-%202.12-green" alt="alternatetext">
  <img src="https://img.shields.io/badge/Spark-3.1.2-red" alt="alternatetext">
</p>

Project for the "Scalable and Cloud Programming" course of the Alma Mater Studiorum University of Bologna.
The project aims to implement a decision tree algorithm to predict accident severity by exploiting the MapReduce paradigm.

## Abstract

In this project, we propose an implementation of C4.5 following the MapReduce framework for distributed computing. Our objective is to extend to decision trees the benefits given by this framework, such as the efficient processing of large datasets, fault tolerance, and parallelism that could greatly improve the overall performance of the algorithm.

We also implement a classic version of the algorithm and carried out tests in sequential and parallel modes.
Different datasets and cluster sizes were used to test the algorithms.

We used Google Cloud Platform (GCP) which allowed us to test the distributed version of the C4.5 decision tree.

Note that we used Windows Powershell to perform each command.

### Project build

To build the project to be executable in local environment, we added the sbt-assembly plugin. IntelliJ automatically install the plugin while loading the project, so use the following command to create a fat-package:
```
sbt assembly
```
To run the project on the cloud using Dataproc, simply use the built-in package command since the service already provides the required dependencies:
```
sbt package
```

### Local Environment

Use the command below to run the jar file:
```
java -Xmx10G -Xss8G -jar $PATH $TRAIN_INPUT $TEST_INPUT $MODE
```
Where:
 - `-Xss8G` specifies the maximum stack memory allocation (set to `8GB`)
 - `-Xmx10G` specifies the maximum heap memory allocation (set to `10GB`)
 - `$PATH` is the path of the jar file
 - `$TRAIN_INPUT` is the training set path
 - `$TEST_INPUT` is the test set path
 - `$MODE` can be `seq`, `par` or `spark`, specifying the program execution mode

## Cloud execution on GCP 

The first step to run the algorithm on Google Cloud Platform is to setup a project and enable the following services:
- Dataproc
- Cloud Storage

It is also necessary to enable the Billing for the project.

We recommend installing the Google Cloud SDK for CLIs in the host system for using GCP, instead of using Google
Cloud Platform console. To do so, follow [this guide](https://cloud.google.com/sdk/docs/install).

### Creating buckets and datasets uploading

First of all create the Cloud Storage bucket using Google Cloud SDK:
```
gsutil mb -l $REGION gs://$BUCKET_NAME
```
`$REGION` and `$BUCKET_NAME` can be found [here](https://cloud.google.com/about/locations).
Beware the bucket name must be unique in the whole Google Cloud Platform, not only in your account.

Then upload the datasets and the package using:
```
gsutil cp $INPUT_TRAIN gs://$BUCKET_NAME/$INPUT_TRAIN
gsutil cp $INPUT_TEST gs://$BUCKET_NAME/$INPUT_TEST
gsutil cp $JAR_PATH gs://$BUCKET_NAME/$JAR_PATH
```
Where `$INPUT_TRAIN` and `$INPUT_TEST` are the paths of the training and test sets, respectively, and the `$JAR_PATH` corresponds to package path.

Note that you shall use different names or paths for the datasets or jar in the destination.

### Provisioning cluster 

To create the cluster, use:

```
gcloud dataproc clusters create $CLUSTER_NAME --region $REGION --zone $ZONE --master-machine-type $MASTER_MACHINE_TYPE --num-workers $NUM_WORKERS --worker-machine-type $WORKER_MACHINE_TYPE
```
Where:
 - `$CLUSTER_NAME` is the name of the cluster
 - `$REGION` and `$ZONE` represent the cluster location. Follow the previous section link.
 - `$MASTER_MACHINE_TYPE` and `$WORKER_MACHINE_TYPE` are the types of the nodes. Different types have different features and performance, refer to [this list](https://cloud.google.com/compute/docs/machine-resource)
 - `$NUM_WORKERS` is the number of total workers (the master is not included in the count)

To provisioning a single node cluster, you have to specify `--single-node` parameter and remove the `--num-workers` parameter.
In oure case, we test the algorithm on three different type of cluster: a single node cluster, a 2 workers cluster and a 3 workers one.
Please considering limitations on resources available for cluster nodes based on machine types chosen (CPUs number, disk size, etc.).

### Submit a job in Dataproc

To submit a job, use:
```
gcloud dataproc jobs submit spark [--async] --cluster=$CLUSTER_NAME --region=$REGION \
--jar=gs://$BUCKET_NAME/$JAR_NAME \
-- gs://$BUCKET_NAME/$INPUT_TRAIN \
gs://$BUCKET_NAME/$INPUT_TEST \
$MODE \
gs://$BUCKET_NAME/output_$WORKERS_$DIM/
```

Use `--async` if you want to send the job and not wait for the result.

Note that the last row defines the output file path which is a folder named according to the number of workers `$WORKERS` and the dataset dimension `$DIM`

### Delete cluster and buckets

If you want to clear the environment, GCloud offers commands for delete clusters and buckets through its SDK:

```
gcloud dataproc clusters delete $CLUSTER_NAME --region=$REGION
gcloud storage rm --recursive gs://$BUCKET_NAME
```
