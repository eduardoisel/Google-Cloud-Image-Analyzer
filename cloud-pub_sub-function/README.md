# Deployment instructions

Run in command line; --source needs to put path; leaving at target/deployment does not seem to work, absolute path does work

gcloud functions deploy funcPubSub --project=cn2526-t4-g08 --region=europe-southwest1 --entry-point=functionpubsub.Entrypoint --allow-unauthenticated --gen2 --runtime=java25 --trigger-topic Image --source=target/deployment --service-account=pub-sub-function@cn2526-t4-g08.iam.gserviceaccount.com
