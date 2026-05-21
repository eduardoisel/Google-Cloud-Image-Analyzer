# Notes

create virtual machine

gcloud compute instances create server-base --machine-type=e2-small --zone=europe-southwest1-a --metadata=ssh-keys=CN2526-T4-G08:ssh-rsa\ AAAAB3NzaC1yc2EAAAADAQABAAABgQDLHl5C6evKcgvwfpvRGUfG\+K/FsQ6lfsgmpPn6Z3fz4Cbjk26/vWPRsSVBut6feUiES9LidTjicZTm6zV4WDjjyyhkaJdsn/cWLXxyuxyLbfJGIJGkt55hVY1I1gH/kCa95ONxez2s9\+ZGc5KElNfnC/pEjrrYKIo/Fhx\+hS578V3VvdkatOxqj7aaTicLTtxWA2O2pfcAZZnbi78O5tm9AjHiB0s4VgZ2xXvDqlhfGBVTgq3N3mQrTzjDbojGkJChYp1PcoenpuQ5PsfscOfyCZWo7lhK4KxarT8CS7X\+pCjXDUXFOFwEyxHMCBeoQwJ/M2jE5Ynx6uyEa8JmO1/pHF6rmk0MvlfboUyKBIMFX4yQz91d\+b5cT5E59yCEi27928QOWlOu61oFfXi6HSLbBgqJOY8xbcX2YX5wd\+pLIVCpLrmn4KoC/d7GFuMtWpNXOSytHOa1n3heWNFcA3CCTs6SsV8VsmtGNTXtZ8CDQvP9zSxWmsf3VGcACjU080E=\ CN2526-T4-G08 --image=projects/ubuntu-os-cloud/global/images/ubuntu-minimal-2404-noble-amd64-v20260429 --provisioning-model=STANDARD


Do

sudo apt update
sudo apt install openjdk-25-jdk


Use bitvise to give jar and service account permission

## base image

gcloud compute images create image-server-base --source-disk=server-template --source-disk-zone=europe-southwest1-a --storage-location=europe-southwest1

## Instance template

Startup file:

#!/bin/bash
echo Hello >> example.txt
export GOOGLE_APPLICATION_CREDENTIALS=/home/CN2526-T4-G08/cn2526-t4-g08-service_key.json && java -jar /home/CN2526-T4-G08/server-1.0-jar-with-dependencies.jar

## MIG

gcloud compute instance-groups managed create server-mig --template=projects/cn2526-t4-g08/regions/europe-southwest1/instanceTemplates/server-template --size=1 --zone=europe-southwest1-a

gcloud compute instance-groups managed set-autoscaling server-mig  --zone=europe-southwest1-a  --min-num-replicas=1  --max-num-replicas=4  --target-cpu-utilization=0.50  --cool-down-period=60