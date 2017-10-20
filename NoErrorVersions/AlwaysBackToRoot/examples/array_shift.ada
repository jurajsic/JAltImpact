STATES
q0 q1 q2 q0_new q1_new q2_new 

INITIAL
(and q0 q0_new)

FINAL
q2 q0_new q1_new 

SYMBOLS
a 

VARIABLES
i n j a b 

TRANSITIONS
a q0
(and q1 (< 0 n1) (= 0 j1) (= i1 (- n1 1)))
#
a q1
(or (and q1 (< j0 i0) (= b1 a0) (= j1 (+ j0 1)) (= n0 n1) (= i0 i1))
    (and q2 (= j0 i0) (= j1 (+ j0 1)) (= n0 n1) (= i0 i1)))
#
a q0_new
(or q1_new (not (< 0 n1)) (not (= 0 j1)) (not (< i1 n1)) (not (<= 0 i1)))
#
a q1_new
(let ((a!1 (or q1_new
               (not (< j0 i0))
               (not (= b1 a0))
               (not (= j1 (+ j0 1)))
               (not (= n0 n1))
               (not (= i0 i1))))
      (a!2 (or q2_new
               (not (= j0 i0))
               (not (= j1 (+ j0 1)))
               (not (= n0 n1))
               (not (= i0 i1)))))
  (and a!1 a!2))
#
a q2_new
(or q2_new
    (not (< j0 n0))
    (not (= j1 (+ j0 1)))
    (not (= n0 n1))
    (not (= i0 i1)))
#

