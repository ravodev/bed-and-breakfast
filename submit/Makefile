JAVAC=/usr/lib/jvm/java-7-openjdk-amd64/bin/javac
JAVA=/usr/lib/jvm/java-7-openjdk-amd64/bin/java

all:
	$(JAVAC) -cp ./forms-1.3.0.jar:./ojdbc14.jar:. \
		     -sourcepath ./forms-1.3.0-src.zip \
			 InnReservations.java ReservationCompletionForm.java \
			 TableDialog.java VerticalFlowLayout.java TableColumnAdjuster.java

test: all
	$(JAVA) -cp ./forms-1.3.0.jar:./ojdbc14.jar:. InnReservations

clean:
	rm -f *.class
