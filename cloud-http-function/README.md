# Deployment instructions

Use maven lifecycle package

Replace source for absolute path to [here](target/deployment) run in command line

```
gcloud functions deploy serverLookup --project=cn2526-t4-g08 --allow-unauthenticated --entry-point=functionhttp.Entrypoint --gen2 --runtime=java25 --trigger-http --region=europe-southwest1 --source=\target\deployment
```
