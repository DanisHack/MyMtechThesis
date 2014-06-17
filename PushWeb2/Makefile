TOS = $(shell ncc -print-tosdir)
PACKAGE = net.tinyos.PushWeb2
APP = $(TOS)/../SenseToRadio
MIG = mig java

# List of message classes to build
MSGS = SenseToRadioMsg.java

INITIAL_TARGETS = $(MSGS)
OTHER_CLEAN = cleanmig

ROOT = ../../..
include $(ROOT)/Makefile.include

SenseToRadioMsg.java:
	$(MIG) -java-classname=$(PACKAGE).SenseToRadioMsg $(APP)/SenseToRadio.h SenseToRadioMsg-o $@
	$(JAVAC) $@

cleanmig:
	rm -f $(MSGS)
