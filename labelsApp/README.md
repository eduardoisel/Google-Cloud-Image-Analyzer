# Setup

Create virtual machine, the next steps presume the used name to be labels-app-template. Do not forget to associate an ssh key. Also take note of the value placed on your ssh ke comment.


Do on the vm terminal the commands below

```
sudo apt update
```

```
sudo apt install openjdk-25-jdk
```

Use bitvise to give jar and service account permission

## base image

Create the base image. Assumes the name of the above vm was labels-app-template

```
gcloud compute images create image-labels-app --source-disk=labels-app-template --source-disk-zone=europe-southwest1-b --storage-location=europe-southwest1
```


## Instance template

Create on browser an instance template that allows all http trafic from the firewall. Also see the following startup file.

```
#!/bin/bash
echo Hello >> example.txt
export GOOGLE_APPLICATION_CREDENTIALS=/home/CN2526-T4-G08/labelsAppKey.json && java -jar /home/CN2526-T4-G08/labelsApp-1.0-jar-with-dependencies.jar
```

The path CN2526-T4-G08 is to be replaced with whatever value was placed in the comment section on your created ssh key.

## MIG

Create the managed instance group. Assumes the instance template was named label-app-template.

```
gcloud compute instance-groups managed create label-app-mig --template=projects/cn2526-t4-g08/regions/europe-southwest1/instanceTemplates/label-app-template --size=1 --zone=europe-southwest1-b
```
