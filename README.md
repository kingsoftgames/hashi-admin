# hashi-admin

A command-line tool to make it easier to manage HashiStack clusters.

## Run without GraalVM

You do not need to compile to GraalVM native-image to run this tool locally.

Just run it with [exec:java](https://www.mojohaus.org/exec-maven-plugin/usage.html):

```
mvn exec:java -Dexec.args="<command-line arguments separated by space>"
```
