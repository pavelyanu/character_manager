# Character manager in java

## User Documentation:

### Launching the project:

1) build the project with

```
mvn clean compile assembly:single
```
2) run the project with
```
java -jar target/mvn-example-1.0-SNAPSHOT-jar-with-dependencies.jar
```
adding
```
server
```
to run as a server and 
```
client
```
to run as a client

### Using the project

If the project is run as a server, no further usage apart from hosting the server is possible.

If the project is run as a client, the further commands will trigger trigger following actions

- auth - will try to reauthenticate a client
- pick - will launch the procedure of picking a character
- view - will print a character sheet if the character is picked
- set - will launch the procedure of setting a field on a character sheet
- help - will print a help message


## Developer Documentation:

[Developer Documentation](./apidocs/index.html)
