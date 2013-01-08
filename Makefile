# Makefile - A simple Makefile for SAGE
#
# Project: GeoApi / SAGE
# Author: Ken Zalewski
# Organization: New York State Senate
# Date: 2013-01-08

# These variables should be overridden on the command line.
# For example:  $ make TARGET_DIR=/tmp/build_war/WEB-INF/classes

# The location of the compiled class files
TARGET_DIR= $(HOME)/sage_classes
# The location of all the non-Tomcat JAR file dependencies
LOCAL_LIBDIR= $(HOME)/lib
# The location of all the Tomcat JAR file dependencies
TOMCAT_LIBDIR= /opt/tomcat/lib

.SUFFIXES: .java .class

JAVAC= javac
SRC_DIR= src/main/java

CLASSPATH=\
	$(LOCAL_LIBDIR)/commons-dbutils-1.5.jar:\
	$(LOCAL_LIBDIR)/commons-io-2.4.jar:\
	$(LOCAL_LIBDIR)/commons-lang3-3.1.jar:\
	$(LOCAL_LIBDIR)/fluent-hc-4.2.2.jar:\
	$(LOCAL_LIBDIR)/gson-2.2.2.jar:\
	$(LOCAL_LIBDIR)/httpclient-4.2.2.jar:\
	$(LOCAL_LIBDIR)/jasypt-1.9.0.jar:\
	$(LOCAL_LIBDIR)/javamail-1.4.5.jar:\
	$(LOCAL_LIBDIR)/json-20121202.jar:\
	$(LOCAL_LIBDIR)/log4j-1.2.17.jar:\
	$(LOCAL_LIBDIR)/signpost-core-1.2.1.2.jar:\
	$(LOCAL_LIBDIR)/xmlrpc-client-3.1.3.jar:\
	$(LOCAL_LIBDIR)/xmlrpc-common-3.1.3.jar:\
	$(LOCAL_LIBDIR)/xstream-1.4.3.jar:\
	$(TOMCAT_LIBDIR)/servlet-api.jar:\
	$(TOMCAT_LIBDIR)/tomcat-jdbc.jar:\
	$(SRC_DIR)
CLASSPATH:= $(subst : ,:,$(CLASSPATH))

JAVA_SRC= $(shell find $(SRC_DIR) -name "*.java")
JAVA_OBJ= $(subst $(SRC_DIR),$(TARGET_DIR),$(JAVA_SRC:.java=.class))

JFLAGS= -cp "$(CLASSPATH)" -d "$(TARGET_DIR)" -Xlint:deprecation

default: build

build: $(TARGET_DIR) $(JAVA_OBJ)

$(TARGET_DIR):
	mkdir -p $(TARGET_DIR)

$(TARGET_DIR)/%.class: $(SRC_DIR)/%.java
	$(JAVAC) $(JFLAGS) $<

clean:
	$(RM) -vf $(JAVA_OBJ)

