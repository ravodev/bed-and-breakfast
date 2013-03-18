all:
	javac -cp ./forms-1.3.0.jar:./ojdbc14.jar:. -sourcepath ./forms-1.3.0-src.zip InnReservations.java ReservationCompletionForm.java TableDialog.java VerticalFlowLayout.java TableColumnAdjuster.java

test: all
	java -cp "forms-1.3.0.jar;ojdbc14.jar;." InnReservations

clean:
	rm -f *.class
