COMPONENT=SenseToRadioAppC
CFLAGS += -I$(TOSDIR)/lib/printf

BUILD_EXTRA_DEPS = SenseToRadioMsg.class
CLEAN_EXTRA = *.class SenseToRadioMsg.java

SenseToRadioMsg.class: SenseToRadioMsg.java
	javac SenseToRadioMsg.java

SenseToRadioMsg.java:
	mig java -target=null $(CFLAGS) -java-classname=SenseToRadioMsg SenseToRadio.h SenseToRadioMsg -o $@


include $(MAKERULES)
