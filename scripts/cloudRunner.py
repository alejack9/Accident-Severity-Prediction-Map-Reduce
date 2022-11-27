import subprocess
import sys

BUCKET_NAME='source'
REGION='europe-west8' # milan
ZONE='europe-west8-b' # milan
CLUSTER_NAME='Accidental-Severity-Prediction-Cluster'
MASTER_MACHINE_TYPE='n2-standard-2'
WORKER_MACHINE_TYPE='n2-standard-4'
NUM_WORKERS='2'

SP_MODE='spark'
PARTITIONS=''

INPUT_TRAIN_FILE_NAME="input_train_1024_binned.csv"
INPUT_TEST_FILE_NAME="input_test_1024_binned.csv"
JOB_ID=4242

while sys.argv[-1] != '-y':
    print("Did you run 'gcloud init'? (run with '-y' to avoid this check)")
    print("y/n: ")
    answer = input()
    if answer == "n":
        print("Do it.")
        exit()
    if answer == "y":
        break
    print("Answer 'y' or 'n'")

subprocess.call(['sbt', 'clean', 'assembly'])

# Create the bucket for jar, input and output
subprocess.call(['gsutil', 'mb', '-l', f'${REGION}', f'gs://${BUCKET_NAME}'])

# Create the cluster
subprocess.call(['gcloud',
                'dataproc',
                'clusters',
                'create',
                f'${CLUSTER_NAME}',
                '--region',
                f'${REGION}',
                '--zone',
                f'${ZONE}',
                '--master-machine-type',
                f'${MASTER_MACHINE_TYPE}',
                '--num-workers',
                f'${NUM_WORKERS}',
                '--worker-machine-type',
                f'${WORKER_MACHINE_TYPE}'
])

subprocess.call(['gsutil', 
                'cp', 
                'target/scala-2.12/FinalProject-assembly-1.0.0.jar', 
                f'gs://${BUCKET_NAME}/FinalProject-assembly-1.0.0.jar'
])

subprocess.call(['gsutil',
                'cp',
                f'data/${INPUT_TRAIN_FILE_NAME}',
                f'gs://${BUCKET_NAME}/${INPUT_TRAIN_FILE_NAME}'
])

subprocess.call(['gsutil',
                'cp',
                f'data/${INPUT_TEST_FILE_NAME}',
                f'gs://${BUCKET_NAME}/${INPUT_TEST_FILE_NAME}'
])
subprocess.call(['gcloud',
                'dataproc',
                'jobs',
                'submit',
                'spark',
                f'--id ${JOB_ID}',
                '--async',
                f'--cluster=${CLUSTER_NAME}',
                f'--region=${REGION}',
                f'--jar=gs://${BUCKET_NAME}/FinalProject-assembly-1.0.0.jar',
                '--',
                f'gs://${BUCKET_NAME}/${INPUT_TRAIN_FILE_NAME}',
                f'gs://${BUCKET_NAME}/${INPUT_TEST_FILE_NAME}',
                f'${SP_MODE}',
                f'${PARTITIONS}'
])

subprocess.call(['gcloud',
                'dataproc',
                'clusters',
                'delete',
                f'${CLUSTER_NAME}',
                '--region',
                f'${REGION}',])

subprocess.call(['gsutil',
                'cp',
                '-r',
                f'gs://${BUCKET_NAME}/output.txt data/.'
])
