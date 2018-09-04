BUILD THE APPOINTMENT PATHWAYSDOS PROXY SERVER
===============

1. cd appoint-proxy # change into current directory from project root
2. docker build -t appointimg .
3. docker run -it -d -p 9999:80 --name=appointcon appointimg sh
4. Access http://localhost:9999/app/api/webservices/index.xml in your Postman/SoapUI (not in your browser)
