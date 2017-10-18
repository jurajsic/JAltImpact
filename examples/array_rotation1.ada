STATES
q0 q1 q2 q3 q0_new q1_new q2_new q3_new 

INITIAL
(and q0 q0_new)

FINAL
q3 q0_new q1_new q2_new 

SYMBOLS
tau 

VARIABLES
a b c i j l x 

TRANSITIONS
tau q0
(and q1
     (= a0 x0)
     (= b0 a1)
     (= b0 c0)
     (<= 0 i0)
     (<= (+ 2 i0) l0)
     (= j0 0)
     (= j1 (+ j0 1))
     (= i1 i0)
     (= l0 l1)
     (= x0 x1))
#
tau q1
(or (and q1
         (= b0 a1)
         (= b0 c0)
         (<= j0 (- i0 1))
         (= j1 (+ j0 1))
         (= i1 i0)
         (= l0 l1)
         (= x0 x1))
    (and q2
         (= a1 b0)
         (= b0 c0)
         (= j0 i0)
         (= j1 (+ j0 1))
         (= i1 i0)
         (= l0 l1)
         (= x0 x1)))
#
tau q2
(or (and q2
         (= a0 b0)
         (= b0 c0)
         (<= j0 (- l0 1))
         (= j1 (+ j0 1))
         (= i1 i0)
         (= l0 l1)
         (= x0 x1))
    (and q3 (= j0 l0)))
#
tau q0_new
(or q1_new
    (not (= a0 x0))
    (not (= c0 a1))
    (not (<= 0 i0))
    (not (<= (+ 2 i0) l0))
    (not (= j0 0))
    (not (= j1 (+ j0 1)))
    (not (= i1 i0))
    (not (= l0 l1))
    (not (= x0 x1)))
#
tau q1_new
(let ((a!1 (or q1_new
               (not (= c0 a1))
               (not (<= j0 (- i0 1)))
               (not (= j1 (+ j0 1)))
               (not (= i1 i0))
               (not (= l0 l1))
               (not (= x0 x1))))
      (a!2 (or q2_new
               (not (= c0 a1))
               (not (= j0 (+ i0 0)))
               (not (= j1 (+ j0 1)))
               (not (= i1 i0))
               (not (= l0 l1))
               (not (= x0 x1)))))
  (and a!1 a!2))
#
tau q2_new
(let ((a!1 (or q2_new
               (not (= c0 a0))
               (not (<= j0 (- l0 1)))
               (not (= j1 (+ j0 1)))
               (not (= i1 i0))
               (not (= l0 l1))
               (not (= x0 x1)))))
  (and a!1 (or q3_new (not (= j0 l0)))))
#
tau q3_new
(not false)
#

