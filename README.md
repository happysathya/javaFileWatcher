# Sample program to watch file events using java nio.

### Build:
##### ./gradlew clean build jar

### Usage:
##### java -cp build/libs/javaFileWatcher-0.1.jar com.happysathya.sample.FileWatcherRunner file:/Users/sathya/filewatch

### Sample output:

```
press enter to exit!!!
**************************************
Eventname: ENTRY_CREATE
File changed: .hello1.txt.swp
**************************************
**************************************
Eventname: ENTRY_MODIFY
File changed: hello1.txt
**************************************
**************************************
Eventname: ENTRY_DELETE
File changed: .hello1.txt.swp
**************************************
**************************************
Eventname: ENTRY_MODIFY
File changed: hello1.txt
**************************************
```
