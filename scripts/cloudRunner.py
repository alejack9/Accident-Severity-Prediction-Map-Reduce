import subprocess
import sys
import os

BUCKET_NAME='accidental-severity-prediction-source-b'
REGION='europe-west1' # belgium
ZONE='europe-west1-b' # belgium
CLUSTER_NAME='accidental-severity-prediction-cluster'
MASTER_MACHINE_TYPE='n1-standard-2'
WORKER_MACHINE_TYPE='n1-standard-4'
NUM_WORKERS='3'

SP_MODE='spark'
PARTITIONS=''

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

# subprocess.call(['sbt', 'clean', 'assembly'], shell=True)

subprocess.call(['gsutil', 
                'cp', 
                'target/scala-2.12/FinalProject-assembly-1.0.0.jar', 
                f'gs://{BUCKET_NAME}/FinalProject-assembly-1.0.0.jar'
], shell=True)

DIMS = [1024]

for dim in DIMS:
    print(f"-------------- DIM {dim} --------------")

    input_train_file_name=f"input_train_{dim}_binned.csv"
    input_test_file_name=f"input_test_{dim}_binned.csv"

    subprocess.call(['gsutil',
                    'cp',
                    f'data/{input_train_file_name}',
                    f'gs://{BUCKET_NAME}/{input_train_file_name}'
    ], shell=True)

    subprocess.call(['gsutil',
                    'cp',
                    f'data/{input_test_file_name}',
                    f'gs://{BUCKET_NAME}/{input_test_file_name}'
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
                    f'gs://{BUCKET_NAME}/{input_train_file_name}',
                    f'gs://{BUCKET_NAME}/{input_test_file_name}',
                    f'{SP_MODE}',
                    f'gs://{BUCKET_NAME}/output_{dim}/'
                    ]

    if (PARTITIONS != ""):
        submit_command_args.append(PARTITIONS)

    subprocess.call(submit_command_args, shell=True)

    os.makedirs(f"data/cloud_logs/output_{dim}")

    subprocess.call(['gsutil',
                    'cp',
                    '-r',
                    f'gs://{BUCKET_NAME}/output_{dim}', 
                    f'data/cloud_logs/output_{dim}'
    ], shell=True)
    
    subprocess.call(['gutil',
                    '-m',
                    'rm',
                    '-r'
                    '-x',
                    f'gs://{BUCKET_NAME}/FinalProject-assembly-1.0.0.jar',
                    f'gs://{BUCKET_NAME}/*',
                    
    ], shell=True)

subprocess.call(['gcloud',
                'dataproc',
                'clusters',
                'delete',
                f'{CLUSTER_NAME}',
                '--region',
                f'{REGION}'
], shell=True)

subprocess.call(['gcloud',
                'storage',
                'rm',
                '--recursive',
                f'gs://{BUCKET_NAME}'
], shell=True)

