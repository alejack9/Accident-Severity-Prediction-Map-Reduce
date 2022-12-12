import subprocess
import sys

BUCKET_NAME='accidental-severity-prediction-source-b'
REGION='europe-west6' # zurigo
ZONE='europe-west6-b' # zurigo
CLUSTER_NAME='accidental-severity-prediction-cluster'
MASTER_MACHINE_TYPE='n1-standard-2'
WORKER_MACHINE_TYPE='n1-standard-4'
NUM_WORKERS='3'

SP_MODE='spark'
PARTITIONS=''

DIM = 1024
INPUT_TRAIN_FILE_NAME=f"input_train_{DIM}_binned.csv"
INPUT_TEST_FILE_NAME=f"input_test_{DIM}_binned.csv"
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

# subprocess.call(['sbt', 'clean', 'assembly'], shell=True)

# Create the bucket for jar, input and output
subprocess.call(['gsutil', 'mb', '-l', f'{REGION}', f'gs://{BUCKET_NAME}'], shell=True)

# Create the cluster
subprocess.call(['gcloud',
                'dataproc',
                'clusters',
                'create',
                f'{CLUSTER_NAME}',
                '--enable-component-gateway',
                '--region',
                f'{REGION}',
                '--zone',
                f'{ZONE}',
                '--master-machine-type',
                f'{MASTER_MACHINE_TYPE}',
                '--num-workers',
                f'{NUM_WORKERS}',
                '--worker-machine-type',
                f'{WORKER_MACHINE_TYPE}'
], shell=True)

subprocess.call(['gsutil', 
                'cp', 
                'target/scala-2.12/FinalProject-assembly-1.0.0.jar', 
                f'gs://{BUCKET_NAME}/FinalProject-assembly-1.0.0.jar'
], shell=True)

subprocess.call(['gsutil',
                'cp',
                f'data/{INPUT_TRAIN_FILE_NAME}',
                f'gs://{BUCKET_NAME}/{INPUT_TRAIN_FILE_NAME}'
], shell=True)

subprocess.call(['gsutil',
                'cp',
                f'data/{INPUT_TEST_FILE_NAME}',
                f'gs://{BUCKET_NAME}/{INPUT_TEST_FILE_NAME}'
], shell=True)

    
# arg 3: out path
# arg 4: partitions

submit_command_args = ['gcloud',
                'dataproc',
                'jobs',
                'submit',
                'spark',
                f'--cluster={CLUSTER_NAME}',
                f'--region={REGION}',
                f'--jar=gs://{BUCKET_NAME}/FinalProject-assembly-1.0.0.jar',
                '--',
                f'gs://{BUCKET_NAME}/{INPUT_TRAIN_FILE_NAME}',
                f'gs://{BUCKET_NAME}/{INPUT_TEST_FILE_NAME}',
                f'{SP_MODE}',
                f'gs://{BUCKET_NAME}/output_{DIM}/'
                ]

if (PARTITIONS != ""):
    submit_command_args.append(PARTITIONS)

subprocess.call(submit_command_args, shell=True)

subprocess.call(['gcloud',
                'dataproc',
                'clusters',
                'delete',
                f'{CLUSTER_NAME}',
                '--region',
                f'{REGION}'
], shell=True)

subprocess.call(['gsutil',
                'cp',
                '-r',
                f'gs://{BUCKET_NAME}/output_{DIM}.txt', 
                'data/.'
], shell=True)

subprocess.call(['gcloud',
                'storage',
                'rm',
                '--recursive',
                f'gs://{BUCKET_NAME}'
], shell=True)