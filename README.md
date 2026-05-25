# Requirements

One must first have a google cloud account with some money available. This project's instructions also make use of
* Bitvise for file transfer to virtual machines
* google cloud for creation of some objects (fully replaceable by browser, but with gcloud all you need is to copy the command and maybe change names)

Most of the code can be run locally, helping avoid some costs of google cloud. Unfortunately, cloud functions such as [this](cloud-http-function) and [this](cloud-pub_sub-function) do not have an easy way to replicate locally. The first one especially will affect the [client application](client). Replace the initial main code
for a fixed address instead of the dynamic search the cloud function gives. Doing all of this locally will not remove all costs of google cloud (storage still entails a small cost), but doing so will ease development.

# Required Services

This project assumes some services are initialized, and they require specific settings, due to them being assumed on the code itself.

* Cloud storage bucket already created, chosen the name cn_g08_europe
* Pub/Sub already created, with topic named Image and subscription named Image-sub
* Firestore named public-spaces-standard, with collection named trab reserved for image info and [to specify] collection for cloud function logging. Also calling to attention changing the data class used for firestore may affect the way it is saved and read, which would be an issue if information is already saved there
* Managed Instance group namesof servers and labels are hardcoded on the server App.
* When creating Managed Instance groups according to the instructions on following readmes, assumes virtual machines have a specific name, and images/templates are given names assumed not used beforehand.

If one wnats to run this project themselves, check these names are free or change them yourselves. When following the next readmes, one may also want to change the location of
the cloud. Be warned of the difference between zones and regions according to google, otherwise you will have issues


# Setup

Follow the child folders README to prepare the code. The contract MUST be done before the client and the server, as it will generate a library they bot use.

* [contract](contract) required first for others
* [Server](server)
* [Client](client)
* [Cloud Http Function](cloud-http-function)
* [Cloud Pub/Sub Function](cloud-pub_sub_function)
