JFLAGS = -g
JC = javac

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        ACN.java \
        extras.java \
        gateway.java \
        internalMsg.java \
        PEER.java \
        MetadataClass.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class