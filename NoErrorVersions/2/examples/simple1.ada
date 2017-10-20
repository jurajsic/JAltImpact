STATES
q0 q1 q0_new q1_new 

INITIAL
(and q0 q0_new)

FINAL
q1 q0_new 

SYMBOLS
a b 

VARIABLES
x 

TRANSITIONS
a q0
(and q1 (<= 0 x1) (<= x1 1))
#
a q0_new
(or q1_new (not (<= 0 x1)) (not (<= x1 1)))
#
a q1_new
(not false)
#
b q1
(and q1 (= x0 x1))
#
b q0_new
(not false)
#
b q1_new
(or q1_new (not (= (- 1 x0) x1)))
#

