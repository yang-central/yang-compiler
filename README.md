# yang-compiler
Yang compiler is a tool based on [YangKit](https://github.com/yang-central/yangkit), it's designed to solve the problem of YANG files compilation dependencies.
For example, after a YANG module is written, if you want to validate it, you will find you have to search all other YANG files that the module depends on. It's very
difficult because dependencies are chained, and the chain of dependencies for a YANG module may be very long. In addition,
where these dependent YANG files are archived and how to obtain these YANG files are also difficult problems. This brings great inconvenience to users and 
affects their enthusiasm for using YANG.

Yang compiler provide a mechanism to get the dependencies automatically.Firstly, it will search the dependencies from the directory to be complied, if not found, 
it will search these YANG files from local repository(it's defined in settings.json or default directory), and if it's not found also,
it will search the dependencies from module information defined in settings.json(if available), and according to the
information to download the YANG files. if it's still not found, it will search the dependencies for remote repository(defined in settings.json or 
[yangcatalog](https://yangcatalog.org/api/) default). If the dependencies are downloaded, it will save to local repository.

Using Yang compiler, you can compile any YANG file regardless where it's dependencies are.
## Specification
* search and download dependencies automatically.
* allow user to customize settings for compilation.
  * customize local repository,{user.home}/.yang is default.
  * customize remote repository, [yangcatalog](https://yangcatalog.org/api/) is default.
  * support proxy.
  * define module information directly if some modules are not in [yangcatalog](https://yangcatalog.org/api/).
* allow user to install yang files which are compiled OK.


## Installation
### Prerequisites
* JDK or JRE 1.8 or above

### Obtain code
git clone https://github.com/yang-central/yang-compiler.git

### build code
mvn clean install

it will generate yang-compiler-1.0-SNAPSHOT.jar and libs directory under the directory target

copy yang-compiler-1.0-SNAPSHOT.jar and libs to anywhere in your computer.

## Usage:
java -jar yang-compiler-1.0-SNAPSHOT.jar yang=<_yang directory_> [ settings=<_settings.json_> ] [install]

### **Parameters**
1. yang: mandatory, source directory for yang files
2. settings: optional, the path of settings.json. {user.home}/.yang/settings.json is default. If no settings.json, the default settings will be used.
3. install: optional, if it's not present, the yang files to be complied will not be installed, if it's present, all yang files which is correct will be 
    installed to local repository. 
### settings.json example:
`
 {
   
    "settings": {

      "local-repository": "/Users/llly/yang",

      "remote-repository": "https://yangcatalog.org/api/",
      
      "proxy: {
         
          "url":"http:proxy.mydomain.com:8080",
          
           "authentication": {
              
              "username":"foo",
              
              "password":"bar"
            
            }

       },

       "module-info": [

         {
           
            "name": "openconfig-acl",

            "revision": "2022-01-14",

            "schema": "https://raw.githubusercontent.com/openconfig/public/master/release/models/acl/openconfig-acl.yang"

          },

          {
             "name": "openconfig-packet-match-types",

              "revision": "2021-07-14",

              "schema": "https://raw.githubusercontent.com/openconfig/public/master/release/models/acl/openconfig-packet-match-types.yang"

           }

        ]

     }

}
`

