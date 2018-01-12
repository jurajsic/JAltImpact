STATES
q0 q1 q2 q3 q4 q0_new q1_new q2_new q3_new 

INITIAL
(and q0 q0_new)

FINAL
q1 q2 q3 q4 q0_new 

SYMBOLS
x 

VARIABLES
a l N I i 

TRANSITIONS
x q0
(and q2
     (= I1 1)
     (= l1 (- N0 1))
     (= I0 0)
     (= l1 l0)
     (= l1 (- N1 1))
     (= i1 i0)
     (>= l1 (+ i1 4))
     (>= i1 3)
     (>= a1 (+ a0 3)))
#
x q2
(or (and q2
         (= l1 l0)
         (= N1 N0)
         (= i1 i0)
         (= I1 (+ I0 1))
         (<= I1 (- N1 1))
         (<= I1 (- i1 1))
         (>= a1 (+ a0 3)))
    (and q1 (= l1 l0) (= I1 N1) (= N1 N0) (= N1 (+ I0 1)) (= i1 i0))
    (and q3
         (= N1 N0)
         (= l1 l0)
         (= I1 i1)
         (= i1 i0)
         (= i1 (+ I0 1))
         (>= N1 (+ i1 1))
         (>= a1 (+ a0 3))))
#
x q3
(or (and q4
         (= I1 l1)
         (= l1 l0)
         (= N1 N0)
         (= l1 (+ I0 1))
         (= i1 i0)
         (= a1 (+ a0 5))
         (<= l1 (- N1 1)))
    (and q1 (= I1 N1) (= N1 N0) (= l1 l0) (= N1 (+ I0 1)) (= i1 i0))
    (and q3
         (= l1 l0)
         (= N1 N0)
         (= i1 i0)
         (= I1 (+ I0 1))
         (= a1 (+ a0 5))
         (<= I1 (- l1 1))
         (<= I1 (- N1 1))))
#
x q4
(or (and q1 (= l1 l0) (= I1 N1) (= N1 N0) (= N1 (+ I0 1)) (= i1 i0))
    (and q4 (= l1 l0) (= N1 N0) (= i1 i0) (= I1 (+ I0 1)) (<= I1 (- N1 1))))
#
x q0_new
(let ((a!1 (or q3_new
               (not (= I1 1))
               (not (= l0 1))
               (not (= I0 0))
               (not (= l1 1))
               (not (= N1 N0))
               (not (= i1 i0))
               (not (>= N1 2))
               (not (>= a1 (+ a0 3)))))
      (a!2 (or q2_new
               (not (= I1 1))
               (not (= I0 0))
               (not (= l1 l0))
               (not (= N1 N0))
               (not (= i1 i0))
               (not (>= l1 2))
               (not (>= N1 2))
               (not (>= a1 (+ a0 3))))))
  (and a!1
       (or q1_new
           (not (= I1 1))
           (not (= N0 1))
           (not (= I0 0))
           (not (= l1 l0))
           (not (= N1 1))
           (not (= i1 i0)))
       (or q3_new
           (not (= I1 1))
           (not (= I0 0))
           (not (= l1 l0))
           (not (= N1 N0))
           (not (= i1 i0))
           (not (>= N1 2))
           (not (<= l1 0)))
       a!2))
#
x q1_new
(not false)
#
x q2_new
(let ((a!1 (or q3_new
               (not (= I1 l1))
               (not (= l1 l0))
               (not (= N1 N0))
               (not (= l1 (+ I0 1)))
               (not (= i1 i0))
               (not (>= a1 (+ a0 3)))
               (not (<= l1 (- N1 1)))))
      (a!2 (or q2_new
               (not (= l1 l0))
               (not (= N1 N0))
               (not (= i1 i0))
               (not (= I1 (+ I0 1)))
               (not (<= I1 (- l1 1)))
               (not (<= I1 (- N1 1)))
               (not (>= a1 (+ a0 3)))))
      (a!3 (or q1_new
               (not (= l1 l0))
               (not (= I1 N1))
               (not (= N1 N0))
               (not (= N1 (+ I0 1)))
               (not (= i1 i0)))))
  (and a!1 a!2 a!3))
#
x q3_new
(let ((a!1 (or q1_new
               (not (= l1 l0))
               (not (= I1 N1))
               (not (= N1 N0))
               (not (= N1 (+ I0 1)))
               (not (= i1 i0))))
      (a!2 (or q3_new
               (not (= l1 l0))
               (not (= N1 N0))
               (not (= i1 i0))
               (not (= I1 (+ I0 1)))
               (not (<= I1 (- N1 1))))))
  (and a!1 a!2))
#

