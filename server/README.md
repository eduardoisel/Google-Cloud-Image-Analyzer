# Serivce Account

Give the following permissions

* Cloud Datastore Owner
* Compute Admin
* Pub/Sub Admin
* Storage Admin

Go to the keys section and generate a json file for the next steps

# Setup

Create virtual machine, the next steps presume the used name to be server-base. Do not forget to associate an ssh key. Also take note of the value placed on your ssh ke comment

Command below does not seem to work, to use browser

Use bitvise to give jar and service account permission file

Run both command below on the virtual machine

```
sudo apt update
```

```
sudo apt install openjdk-25-jdk
```


## base image

```
gcloud compute images create image-server-base --source-disk=server-template --source-disk-zone=europe-southwest1-a --storage-location=europe-southwest1
```

## Instance template

Create on browser an instance template that allows all http trafic from the firewall. Also see the following startup file.

```
#!/bin/bash
echo Hello >> example.txt
export GOOGLE_APPLICATION_CREDENTIALS=/home/CN2526-T4-G08/cn2526-t4-g08-service_key.json && java -jar /home/CN2526-T4-G08/server-1.0-jar-with-dependencies.jar
```

The path CN2526-T4-G08 is to be replaced with whatever value was placed in the comment section on your created ssh key.

## MIG

Create the managed instance group. Assumes the instance template above was named server-template.

```
gcloud compute instance-groups managed create server-mig --template=projects/cn2526-t4-g08/regions/europe-southwest1/instanceTemplates/server-template --size=1 --zone=europe-southwest1-a
```
