# Service account

Create a service account, give the following permissions:

* Cloud Datastore Owner
* Pub/Sub Admin

No need to get a json folder for this one use the related email instead

# Deployment instructions

Run in command line; --source needs to put path; leaving at target/deployment does not seem to work, absolute path does work

Replace on another project:
* value of project
* absolute path to [the jar](target/deployment)
* --service-acount name
* --triger-topic if chosen a different name

```
gcloud functions deploy funcPubSub --project=cn2526-t4-g08 --region=europe-southwest1 --entry-point=functionpubsub.Entrypoint --allow-unauthenticated --gen2 --runtime=java25 --trigger-topic Image --source=target/deployment --service-account=pub-sub-function@cn2526-t4-g08.iam.gserviceaccount.com
```
