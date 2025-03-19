 # Log System with Proof-of-Work

This project implements a secure log system using a proof-of-work mechanism to validate log entries. It consists of three main components:
1. **LogServer**: A server that accepts log entries with valid proof-of-work and appends them to a log file.
2. **Log**: A client that generates proof-of-work for a message and sends it to the server.
3. **CheckLog**: A utility to validate the integrity of the log file.

## Features
- **Proof-of-Work**: Ensures that each log entry requires computational effort to prevent spam.
- **Log Integrity**: Each log entry is linked to the previous one using cryptographic hashes, ensuring tamper resistance.
- **Simple Client-Server Architecture**: Easy to set up and use.

## Prerequisites
- Java Development Kit (JDK) 8 or later.
- Basic knowledge of running Java applications from the command line.

## Setup and Usage

### 1. Clone the Repository
git clone https://github.com/your-username/log-system.git
cd log-system

### 2. Compile the Java Files
You can compile the Java files using the provided Makefile:
make

### 3. Start the Log Server
Run the LogServer to start the server: **./logserver**

### 4. Send Log Entries Using the Client

Use the Log client to send log entries to the server. Replace <port> with the port number printed by the server and <message> with your log message: **./log port "message"**

### 5. Validate the Log File

Use the CheckLog utility to validate the integrity of the log file: **./checklog**

## Log Format

Each log entry is stored in the following format: timestamp - previous_hash message

<timestamp>: The time when the log entry was created.
<previous_hash>: The hash of the previous log entry (or start for the first entry).
<message>: The log message.

## Makefile command to remove all the compiled class files
 make clean



