# Notes

create virtual machine

Do on terminal

sudo apt update
sudo apt install openjdk-25-jdk


Use bitvise to give jar and service account permission

## base image

gcloud compute images create image-labels-app --source-disk=labels-app-template --source-disk-zone=europe-southwest1-b --storage-location=europe-southwest1

## Instance template

Startup file:

#!/bin/bash
echo Hello >> example.txt
export GOOGLE_APPLICATION_CREDENTIALS=/home/CN2526-T4-G08/labelsAppKey.json && java -jar /home/CN2526-T4-G08/labelsApp-1.0-jar-with-dependencies.jar

## MIG

gcloud compute instance-groups managed create label-app-mig --template=projects/cn2526-t4-g08/regions/europe-southwest1/instanceTemplates/label-app-template --size=1 --zone=europe-southwest1-b

gcloud compute instance-groups managed set-autoscaling label-app-mig  --zone=europe-southwest1-b  --min-num-replicas=1  --max-num-replicas=4  --target-cpu-utilization=0.50  --cool-down-period=60




