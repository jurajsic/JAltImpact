STATES
q0 q1 q0_new 

INITIAL
(and q0 q0_new)

FINAL
q1 

SYMBOLS
a b 

VARIABLES
a m 

TRANSITIONS
a q0
(and q1 (> m0 1) (= m1 m0) (= a1 0))
#
a q1
(and q1 (< a0 (- m0 1)) (= a1 (+ a0 1)) (= m0 m1))
#
a q0_new
(or q0_new (not (< a1 m1)))
#
b q1
(and q1 (= a0 (- m0 1)) (= a1 0) (= m0 m1))
#
b q0_new
(or q0_new (not (< a1 m1)))
#

