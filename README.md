# JAltImpact

## Author
Xiao XU(xiao.xu.cathiec@gmail.com)

## Before running it
1. Install [JavaSMT](https://github.com/sosy-lab/java-smt).
2. Install [corresponding solvers](https://github.com/sosy-lab/java-smt/blob/master/README.md#installation).
3. Download/Clone the project "[JAltImpact](https://github.com/cathiec/JAltImpact)" from [GitHub](https://github.com).
4. Open [Eclipse](http://www.eclipse.org/downloads/).
5. **Import** -> **Maven** -> **Existing Maven Projects**, choose "[pom.xml](https://github.com/cathiec/JAltImpact/blob/master/pom.xml)".

## How to run it (with [Eclipse](http://www.eclipse.org/downloads/))
The main function is in "[src/main/java/empty.java](https://github.com/cathiec/JAltImpact/blob/master/src/main/java/empty.java)".
It takes 3 arguments:
* **args[0]** (necessary) the input file
* **args[1]** (default: MATHSAT5) the solver
* **args[2]** (default: always back to the root) back-steps number when checking whether a node is accepting
> Using a smaller **back-steps number** can accelerate the program, but the correctness of result cannot be ensured in some cases.
>> * In the case of *Not Empty*, it is safe to use a smaller **back-steps number** to compute counter examples.
>> * In the case of *Empty*, it is safe to use a smaller **back-steps number** to prove the emptiness.

Example of arguments:
```
examples/array_simple.ada -SMTINTERPOL -23
```

* Click on **Run Configurations**
* Click on the tab **Arguments**
* In the inputbox **Program arguments**, put your arguments
* Click on **Run**

You can always modify the arguments by backing to **Run Configurations**.
