# Deployment instructions

Use maven lifecycle package

run in command line

see path on --source. absolute path works, to relative maybe not
gcloud functions deploy serverLookup --project=cn2526-t4-g08 --allow-unauthenticated --entry-point=functionhttp.Entrypoint --gen2 --runtime=java25 --trigger-http --region=europe-southwest1 --source=C:\Users\Edu\Desktop\CN\trab\cloud-Image-Analyzer\cloud-http-function\target\deployment