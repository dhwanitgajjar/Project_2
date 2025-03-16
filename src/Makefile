# Commands and settings
JAVAC = javac
JAVA  = java
SHELL = /bin/bash

# Java source files
SOURCES = Log.java LogServer.java CheckLog.java
CLASSES = $(SOURCES:.java=.class)

.PHONY: all clean scripts

# Default target: compile classes and create executable scripts.
all: $(CLASSES) scripts

# Compile each Java file into a class file.
%.class: %.java
	$(JAVAC) $<

# Create the shell scripts that run the respective Java classes.
scripts: log logserver checklog

log:
	@echo "#!/bin/bash" > log
	@echo "$(JAVA) Log \"\$$@\"" >> log
	@chmod u+x log

logserver:
	@echo "#!/bin/bash" > logserver
	@echo "$(JAVA) LogServer \"\$$@\"" >> logserver
	@chmod u+x logserver

checklog:
	@echo "#!/bin/bash" > checklog
	@echo "$(JAVA) CheckLog \"\$$@\"" >> checklog
	@chmod u+x checklog

# Clean up generated class files and scripts.
clean:
	rm -f $(CLASSES) log logserver checklog