# yang-compiler
Yang compiler is a tool based on [YangKit](https://github.com/yang-central/yangkit), it's designed to solve the problem of YANG files compilation dependencies.
For example, after a YANG module is written, if you want to validate it, you need to add all missing YANG module dependencies to your path. This could be troublesome because dependencies are chained, and the chain of dependencies for a YANG module may be very long. In addition,
where these dependented YANG files are archived and how to obtain these YANG files are also difficult problems. This brings great inconvenience to users and 
affects their enthusiasm for using YANG.

Yang compiler provide a mechanism to get the dependencies automatically when compiling YANG modules. Firstly, it will search the dependencies from the target directory to be complied, if not found, it will search these YANG files from local repository(it's defined in settings.json or default directory), and if it's not found also, it will search the dependencies from module information defined in settings.json(if available), and according to the information to download the YANG files. if it's still not found, it will search the dependencies for remote repository(defined in settings.json or 
[yangcatalog](https://yangcatalog.org/api/) by default). If the dependencies are fetched, it will be saved into local repository.

Using Yang compiler, you can compile any YANG file regardless where it's dependencies are. Yang compiler allow developer to develop 
plugin to extend customized functions.
## Specification
* search and download dependencies automatically.
* allow user to customize settings for compilation.
  * customize local repository,{user.home}/.yang is default.
  * customize remote repository, [yangcatalog](https://yangcatalog.org/api/) is default.
  * support proxy.
  * define module information directly if some modules are not in [yangcatalog](https://yangcatalog.org/api/).
* allow user to install yang files which are compiled OK.
* allow developer to develop customized plugin.


## Installation
### Prerequisites
* JDK or JRE 1.8 or above

### Obtain code
```
# git clone https://github.com/yang-central/yang-compiler.git
```
### build code
```
# cd yang-compiler
# mvn clean install
```
it will generate yang-compiler-1.0-SNAPSHOT.jar and libs directory under the directory target

copy yang-compiler-1.0-SNAPSHOT.jar and libs to anywhere in your computer.

## Usage:
```
# java -jar yang-compiler-1.0-SNAPSHOT.jar [yang=<_yang directory_>] [ settings=<_settings.json_> ] [install]
```
### **Parameters**
1. yang: optional, local directory for yang modules to be compiled, if not present, the 'yang' directory of build.json will be used.
2. settings: optional, the path of settings.json. {user.home}/.yang/settings.json is default. If no settings.json, the default settings will be used.
3. install: optional, if it's not present, the yang files to be complied will not be copied into local repo directory, if it's present, all yang files which is successfully compiled will be copied into local repository. 
### settings.json example:
```
 {
   
    "settings": {

      "local-repository": "/Users/llly/yang", 
      //local repo to find the missing yang module dependencies

      "remote-repository": "https://yangcatalog.org/api/", 
      //default remote repo to fetch the missing yang module dependencies unless specified by "module-info"
      
      "proxy: {
         
          "url":"http:proxy.mydomain.com:8080",
          
           "authentication": {
              
              "username":"foo",
              
              "password":"bar"
            
            }

       },

       "module-info": [
//remote address to fetch the missing yang module dependencies 
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
```
## Edit build.json
The build.json MUST be placed to work directory. And the example is listed below:
```
{

    "build": {

      "yang": "yang",// target path to compile YANG files
 
      "plugin": [
        {
   
          "name": "validator_plugin",

          "parameter": [
           
            {
               "name": "output",

               "value": "{yang}/validator.txt"
            
            }
           
           ]
          
          }
         
       ]
   
    }

 }
 ```
## develop plugin
1. specified a unique plugin name. e.g. yang-tree-generator
2. write a java class implements YangCompilerPlugin.
   @see [YangValidator](src/main/java/org/yangcentral/yangkit/plugin/validator/YangValidator.java)
3. add plugin information in plugins.json(in src/main/resource or in work directory)
 ```
    {

           "plugins": {
   
              "plugin": [
   
                 {
   
                    "name": "validator_plugin",

                    "class": "org.yangcentral.yangkit.plugin.validator.YangValidator",
   
                    "description": "a plugin for validating yang files",
   
                    "parameter": [
   
                       {
   
                          "name": "output",
   
                          "description": "the output directory."
   
                       }
   
                    ]
   
                 }
   
             ]
   
           }
   
    }
 ```
