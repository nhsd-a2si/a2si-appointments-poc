In this directory

docker build . -t ccri-document

docker tag ccri-document thorlogic/ccri-document

docker push thorlogic/ccri-document


docker run -d -p 8181:8181 ccri-document 


FileCopy command
pscp xxxx@purple.testlab.nhs.uk:/careconnect/ccri-server/docker-compose.yml docker-compose.yml
