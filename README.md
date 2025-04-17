# Spark RDD Pipe Command Injection Vulnerability

This project demonstrates a command injection vulnerability in Apache Spark's RDD pipe functionality.

## Vulnerability Details

### Root Cause
- **Package**: `org.apache.spark:spark-core_2.12:3.5.5`
- **Component**: Spark RDD Pipe Functionality
- **Vulnerability Type**: Command Injection

### Description
The vulnerability exists in Spark's RDD pipe functionality which allows executing external commands. When combined with improper input validation, this can lead to command injection attacks.

### Technical Details
- **Affected Versions**: Apache Spark 3.5.5
- **Dependencies**:
  - Spark Core: 3.5.5
  - Scala: 2.12.18
  - SLF4J: 2.0.9
  - Log4j12: 2.0.9

### Proof of Concept
The PoC demonstrates how the RDD pipe functionality can be exploited to execute arbitrary commands:

```scala
val command: Seq[String] = Seq("sh", "-c", s"whoami >> ${outputFile.getAbsolutePath}")
val process: ProcessBuilder = Process(command)
val exitCode: Int = process.!
```

## Setup and Execution

### Prerequisites
- Java 8 or higher
- Maven 3.x
- Apache Spark 3.5.5

### Building
```bash
mvn clean package
```

### Running
```bash
java --add-opens java.base/java.nio=ALL-UNNAMED \
     --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     --add-opens java.base/java.lang.invoke=ALL-UNNAMED \
     -jar target/spark-command-injection-poc-1.0-SNAPSHOT.jar
```

## Mitigation
To prevent command injection:
1. Validate and sanitize all user inputs
2. Use parameterized commands instead of string concatenation
3. Implement proper access controls
4. Consider using Spark's built-in functions instead of pipe operations when possible

## Security Impact
- **Severity**: High
- **Impact**: Remote Code Execution
- **Attack Vector**: Command Injection through RDD pipe operations

