#!/bin/sh

# Character substitution on config values
# sed -i "s/{WARDWARE_DATABASE_NAME}/$MYSQL_WARDWARE_DBASE/g" /var/www/html/WW-NEWS/config/local_config.php

# Change into dir with hard-coded SOAP
cd /var/www/html/app/api/webservices/

# Start web server
service apache2 start

# WARNING: Lastly call a process that NEVER EXITS, else the container will STOP running!
tail -f /dev/null
