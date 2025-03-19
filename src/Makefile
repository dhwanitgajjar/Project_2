#!/bin/bash
JAVAC = javac
JAVA = java
CLASSPATH = .

all: Log.class LogServer.class CheckLog.class log logserver checklog

Log.class: Log.java
	$(JAVAC) Log.java

LogServer.class: LogServer.java
	$(JAVAC) LogServer.java

CheckLog.class: CheckLog.java
	$(JAVAC) CheckLog.java

log: Log.class
	echo '#!/bin/bash' > log
	echo 'CLASSPATH=$(CLASSPATH) $(JAVA) Log "$$@"' >> log
	chmod u+x log

logserver: LogServer.class
	echo '#!/bin/bash' > logserver
	echo 'CLASSPATH=$(CLASSPATH) $(JAVA) LogServer "$$@"' >> logserver
	chmod u+x logserver

checklog: CheckLog.class
	echo '#!/bin/bash' > checklog
	echo 'CLASSPATH=$(CLASSPATH) $(JAVA) CheckLog "$$@"' >> checklog
	chmod u+x checklog

clean:
	rm -f *.class log logserver checklog log.txt loghead.txt