STATES
q0 q1 q2 q0_new q1_new q2_new 

INITIAL
(and q0 q0_new)

FINAL
q0 q1_new q2_new 

SYMBOLS
a b 

VARIABLES
x y 

TRANSITIONS
a q0
(and q1 (= x1 (+ x0 1)) (= y1 y0))
#
a q0_new
(let ((a!1 (or q2_new (not (>= (- x1 y1) (- x0 y0))))))
  (and (or q1_new (not (< x0 x1)) (not (<= y1 y0))) a!1))
#
a q1_new
(not false)
#
a q2_new
(not false)
#
b q1
(and q0 (= x0 x1) (= y1 (+ y0 1)))
#
b q0_new
(not false)
#
b q1_new
(or q0_new (not (>= x0 x1)) (not (> y1 y0)))
#
b q2_new
(or q0_new (not (<= (- x1 y1) (- x0 y0))))
#

