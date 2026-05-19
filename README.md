# Notes

create virtual machine

gcloud compute instances create server-base --machine-type=e2-small --zone=europe-southwest1-a --metadata=ssh-keys=CN2526-T4-G08:ssh-rsa\ AAAAB3NzaC1yc2EAAAADAQABAAABgQDLHl5C6evKcgvwfpvRGUfG\+K/FsQ6lfsgmpPn6Z3fz4Cbjk26/vWPRsSVBut6feUiES9LidTjicZTm6zV4WDjjyyhkaJdsn/cWLXxyuxyLbfJGIJGkt55hVY1I1gH/kCa95ONxez2s9\+ZGc5KElNfnC/pEjrrYKIo/Fhx\+hS578V3VvdkatOxqj7aaTicLTtxWA2O2pfcAZZnbi78O5tm9AjHiB0s4VgZ2xXvDqlhfGBVTgq3N3mQrTzjDbojGkJChYp1PcoenpuQ5PsfscOfyCZWo7lhK4KxarT8CS7X\+pCjXDUXFOFwEyxHMCBeoQwJ/M2jE5Ynx6uyEa8JmO1/pHF6rmk0MvlfboUyKBIMFX4yQz91d\+b5cT5E59yCEi27928QOWlOu61oFfXi6HSLbBgqJOY8xbcX2YX5wd\+pLIVCpLrmn4KoC/d7GFuMtWpNXOSytHOa1n3heWNFcA3CCTs6SsV8VsmtGNTXtZ8CDQvP9zSxWmsf3VGcACjU080E=\ CN2526-T4-G08 --image=projects/ubuntu-os-cloud/global/images/ubuntu-minimal-2404-noble-amd64-v20260429 --provisioning-model=STANDARD


gcloud compute instances create server-template \
    --project=cn2526-t4-g08 \
    --zone=europe-southwest1-a \
    --machine-type=e2-small \
    --network-interface=network-tier=PREMIUM,stack-type=IPV4_ONLY,subnet=default \
    --metadata=enable-osconfig=TRUE,ssh-keys=CN2526-T4-G08:ssh-rsa\ \
AAAAB3NzaC1yc2EAAAADAQABAAABgQDLHl5C6evKcgvwfpvRGUfG\+K/FsQ6lfsgmpPn6Z3fz4Cbjk26/vWPRsSVBut6feUiES9LidTjicZTm6zV4WDjjyyhkaJdsn/cWLXxyuxyLbfJGIJGkt55hVY1I1gH/kCa95ONxez2s9\+ZGc5KElNfnC/pEjrrYKIo/Fhx\+hS578V3VvdkatOxqj7aaTicLTtxWA2O2pfcAZZnbi78O5tm9AjHiB0s4VgZ2xXvDqlhfGBVTgq3N3mQrTzjDbojGkJChYp1PcoenpuQ5PsfscOfyCZWo7lhK4KxarT8CS7X\+pCjXDUXFOFwEyxHMCBeoQwJ/M2jE5Ynx6uyEa8JmO1/pHF6rmk0MvlfboUyKBIMFX4yQz91d\+b5cT5E59yCEi27928QOWlOu61oFfXi6HSLbBgqJOY8xbcX2YX5wd\+pLIVCpLrmn4KoC/d7GFuMtWpNXOSytHOa1n3heWNFcA3CCTs6SsV8VsmtGNTXtZ8CDQvP9zSxWmsf3VGcACjU080E=\ CN2526-T4-G08 \
    --maintenance-policy=MIGRATE \
    --provisioning-model=STANDARD \
    --service-account=140994775738-compute@developer.gserviceaccount.com \
    --scopes=https://www.googleapis.com/auth/devstorage.read_only,https://www.googleapis.com/auth/logging.write,https://www.googleapis.com/auth/monitoring.write,https://www.googleapis.com/auth/service.management.readonly,https://www.googleapis.com/auth/servicecontrol,https://www.googleapis.com/auth/trace.append \
    --create-disk=auto-delete=yes,boot=yes,device-name=server-template,image=projects/debian-cloud/global/images/debian-12-bookworm-v20260513,mode=rw,size=10,type=pd-balanced \
    --no-shielded-secure-boot \
    --shielded-vtpm \
    --shielded-integrity-monitoring \
    --labels=goog-ops-agent-policy=v2-template-1-7-0,goog-ec-src=vm_add-gcloud \
    --reservation-affinity=any \
&& \
printf 'agentsRule:\n  packageState: installed\n  version: latest\ninstanceFilter:\n  inclusionLabels:\n  - labels:\n      goog-ops-agent-policy: v2-template-1-7-0\n' > config.yaml \
&& \
gcloud compute instances ops-agents policies create goog-ops-agent-v2-template-1-7-0-europe-southwest1-a \
    --project=cn2526-t4-g08 \
    --zone=europe-southwest1-a \
    --file=config.yaml \
&& \
gcloud compute resource-policies create snapshot-schedule default-schedule-1 \
    --project=cn2526-t4-g08 \
    --region=europe-southwest1 \
    --max-retention-days=14 \
    --on-source-disk-delete=keep-auto-snapshots \
    --daily-schedule \
    --start-time=07:00 \
&& \
gcloud compute disks add-resource-policies server-template \
    --project=cn2526-t4-g08 \
    --zone=europe-southwest1-a \
    --resource-policies=projects/cn2526-t4-g08/regions/europe-southwest1/resourcePolicies/default-schedule-1