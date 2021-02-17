
all:
	javac Rtuples.java

tests:
	java Rtuples  < two_jumps.in | diff - two_jumps.out
	java Rtuples  < basic.in | diff - basic.out
	java Rtuples  < find_better_journey.in | diff - find_better_journey.out
	java Rtuples  < delete_multiple_labels.in  | diff - delete_multiple_labels.out
	java Rtuples  < baseline_error.in  | diff - baseline_error.out
	java Rtuples  < tc_error.in  | diff - tc_error.out
	java Rtuples  < tc_error2.in  | diff - tc_error2.out
	java Rtuples  < tc_error3.in  | diff - tc_error3.out
	java Rtuples  < tc_error4.in  | diff - tc_error4.out

