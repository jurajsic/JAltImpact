STATES
q0 q1 q2 q3 q4 q5 q6 q0_new q1_new q2_new 

INITIAL
(and q0 q0_new)

FINAL
q1 q0_new q2_new 

SYMBOLS
init app down up in out 

VARIABLES
a b c t 

TRANSITIONS
init q0
(and q1 (= 0 a1) (= 0 b1) (= 0 c1))
#
init q0_new
(or q1_new (not (= 0 c1)))
#
init q1_new
(not false)
#
init q2_new
(not false)
#
app q1
(and q2
     (<= t0 t1)
     (= a1 t0)
     (= b1 t0)
     (= c1 t0)
     (<= (- t1 a1) 5)
     (< (- t1 b1) 2))
#
app q0_new
(not false)
#
app q1_new
(or q1_new (not (<= t0 t1)) (not (= c1 t0)))
#
app q2_new
(not false)
#
down q2
(and q4
     (<= t0 t1)
     (= a1 a0)
     (= b1 b0)
     (= c1 t0)
     (<= (- t1 a1) 5)
     (<= 1 (- t0 b0))
     (< (- t0 b0) 2))
#
down q3
(and q5
     (<= t0 t1)
     (= a1 a0)
     (= b1 b0)
     (= c1 t0)
     (<= (- t1 a1) 5)
     (<= 1 (- t0 b0))
     (< (- t0 b0) 2))
#
down q0_new
(not false)
#
down q1_new
(or q2_new (not (<= t0 t1)) (not (= c1 t0)) (not (<= (- t1 c1) 5)))
#
down q2_new
(not false)
#
up q6
(and q1 (<= t0 t1) (= a1 a0) (= b1 b0) (= c1 c0) (<= (- t0 b0) 1))
#
up q0_new
(not false)
#
up q1_new
(or q1_new (not (<= t0 t1)) (not (= c1 c0)))
#
up q2_new
(or q1_new (not (<= t0 t1)) (not (= c1 c0)))
#
in q2
(and q3
     (<= t0 t1)
     (= a1 a0)
     (= b1 b0)
     (= c1 c0)
     (<= (- t1 a1) 5)
     (<= 2 (- t0 a0))
     (< (- t0 a0) 5))
#
in q4
(and q5
     (<= t0 t1)
     (= a1 a0)
     (= b1 b0)
     (= c1 c0)
     (<= (- t1 a1) 5)
     (<= 2 (- t0 a0))
     (< (- t0 a0) 5))
#
in q0_new
(not false)
#
in q1_new
(or q1_new (not (<= t0 t1)) (not (= c1 c0)))
#
in q2_new
(or q2_new (not (<= t0 t1)) (not (= c1 c0)) (not (<= (- t1 c1) 5)))
#
out q5
(and q6
     (<= t0 t1)
     (= a1 a0)
     (= b1 t0)
     (= c1 c0)
     (<= (- t0 a0) 5)
     (<= (- t1 b1) 1))
#
out q0_new
(not false)
#
out q1_new
(not false)
#
out q2_new
(or q2_new (not (<= t0 t1)) (not (= c1 c0)) (not (<= (- t1 c1) 5)))
#

