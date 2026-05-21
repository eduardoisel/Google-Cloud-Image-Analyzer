# Replication instructions

One must first have a google cloud account with some money available. This project's instructions also make use of
* Bitvise for file transfer to virtual machines
* google cloud for creation of some objects (replaceable by browser)

Most of the code can be run locally, even avoiding some costs of google cloud. Unfortunately, cloud functions such as [this](cloud-http-function) and [this](cloud-pub_sub-function) do not have an easy way to replicate locally. The first one especially will affect the [client application](client). Replace the initial main code
for a fixed address instead of the dynamic search the cloud function gives. Doing all of this locally will not remove all costs of google cloud (storage still entails a small cost), but doing so will ease development.

When the time to run code on the cloud arrives, follow the readmes on every child folder.