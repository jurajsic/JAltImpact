STATES
q0 q1 q0_new 

INITIAL
(and q0 q0_new)

FINAL
q1 

SYMBOLS
a 

VARIABLES
x y 

TRANSITIONS
a q0
(and q1 (= 0 x1) (= y1 0))
#
a q1
(and q1 (= x1 (+ x0 1)) (= y1 (+ y0 1)))
#
a q0_new
(or q0_new (not (= x1 y1)))
#

