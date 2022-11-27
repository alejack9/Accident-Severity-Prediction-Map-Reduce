import subprocess

BUCKET_NAME='source'
REGION='EUROPE-WEST8' # milan
CLUSTER_NAME='Accidental-Severity-Prediction-Cluster'
MASTER_MACHINE_TYPE='N2'
WORKER_MACHINE_TYPE=MASTER_MACHINE_TYPE
NUM_WORKERS='2'
# TODO
SP_MODE=''
SR_MODE=''
PARTITIONS=''

INPUT_FILE_NAME="input_test_1024_binned.csv"
JOB_ID=4242

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
                f'data/${INPUT_FILE_NAME}input_test_1024_binned.csv',
                f'gs://${BUCKET_NAME}/${INPUT_FILE_NAME}'
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
                'yarn',
                f'gs://${BUCKET_NAME}/${INPUT_FILE_NAME}',
                f'gs://${BUCKET_NAME}',
                f'sp=${SP_MODE}',
                f'sr=${SR_MODE}',
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
                f'gs://${BUCKET_NAME}/stayPoints data/.'
])
subprocess.call(['gsutil',
                'cp',
                '-r',
                f'gs://${BUCKET_NAME}/stayRegions data/.'
])
