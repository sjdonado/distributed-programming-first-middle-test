# Distributed Programming - First midterm
> File uploader/downloader using a multicast network and multiple web services managed by load balancing.

## Uploads folder location
The directory where files will be uploaded is `~/GlassFish_Server/glassfish/domains/domain1/uploads`

**Note**:
If the uploads folder doesn't exist, WebService will create it automatically.


## Web Manager flow
Please, follow these steps:
  - Build with dependencies the Web Manager and Run it.
  - Build with dependencies the Web Manager Client.
  
## Web Service flow
Please, follow these steps:
  - Make sure the web manager is running.
  - Create a setting file with the following variables: WEB_MANAGER_ADDRESS, WEB_SERVICE_PORT. Name it *web-service-setting.txt* and place it in the following path: `~/GlassFish_Server/glassfish/domains/domain1/config`.
  - Build with dependencies and Run it (It should be deployed in same host of the TCPReceptor).
  
**Note**:
  - You can use the *web-service-setting.template.txt* file.
  - If the uploads folder doesn't exist, WebService will create it automatically.
  
## Authors
  - [Brian Ramirez @brianr482](//github.com/brianr482)
  - [Juan Rodríguez @sjdonado](//github.com/sjdonado)
  - [John Fontalvo @RnJohn](//github.com/RnJohn)
  - [Oskhar Arrieta @oskhar1099](//github.com/oskhar1099)
