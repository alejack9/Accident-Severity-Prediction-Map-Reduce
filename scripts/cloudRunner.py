import subprocess

BUCKET_NAME='test'
subprocess.call(['gsutil', 'mb', '-l', f'${REGION}', f'gs://${BUCKET_NAME}'])

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

subprocess.call(['sbt', 'clean', 'assembly'])
subprocess.call(['gsutil', 
                'cp', 
                'target/scala-2.12/FinalProject-assembly-1.0.0.jar', 
                f'gs://${BUCKET_NAME}/FinalProject-assembly-1.0.0.jar'
])