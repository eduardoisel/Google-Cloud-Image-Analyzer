# Setup

Create virtual machine, the next steps presume the used name to be server-base. Do not forget to associate an ssh key. Also take note of the value placed on your ssh ke comment

Command below does not seem to work, to use browser

```
gcloud compute instances create server-base --machine-type=e2-small --zone=europe-southwest1-a --metadata=ssh-keys=CN2526-T4-G08:ssh-rsa\ AAAAB3NzaC1yc2EAAAADAQABAAABgQDLHl5C6evKcgvwfpvRGUfG\+K/FsQ6lfsgmpPn6Z3fz4Cbjk26/vWPRsSVBut6feUiES9LidTjicZTm6zV4WDjjyyhkaJdsn/cWLXxyuxyLbfJGIJGkt55hVY1I1gH/kCa95ONxez2s9\+ZGc5KElNfnC/pEjrrYKIo/Fhx\+hS578V3VvdkatOxqj7aaTicLTtxWA2O2pfcAZZnbi78O5tm9AjHiB0s4VgZ2xXvDqlhfGBVTgq3N3mQrTzjDbojGkJChYp1PcoenpuQ5PsfscOfyCZWo7lhK4KxarT8CS7X\+pCjXDUXFOFwEyxHMCBeoQwJ/M2jE5Ynx6uyEa8JmO1/pHF6rmk0MvlfboUyKBIMFX4yQz91d\+b5cT5E59yCEi27928QOWlOu61oFfXi6HSLbBgqJOY8xbcX2YX5wd\+pLIVCpLrmn4KoC/d7GFuMtWpNXOSytHOa1n3heWNFcA3CCTs6SsV8VsmtGNTXtZ8CDQvP9zSxWmsf3VGcACjU080E=\ CN2526-T4-G08 --image=projects/ubuntu-os-cloud/global/images/ubuntu-minimal-2404-noble-amd64-v20260429 --provisioning-model=STANDARD
```

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
