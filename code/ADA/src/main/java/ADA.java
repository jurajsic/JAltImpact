import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;
import org.sosy_lab.java_smt.api.SolverException;

public class ADA {
	
	// JavaSMT configuration and corresponding context
	// ====================
	SolverContext context;
	FormulaManager fmgr;
	BooleanFormulaManager bmgr;
	IntegerFormulaManager imgr;
	
/**
 * make an integer variable
 * @param v : name of integer variable
 */
	IntegerFormula make_int(String v) {
		return imgr.makeVariable(v);
	}
	
/**
 * make a Boolean variable
 * @param v : name of Boolean variable
 */
	BooleanFormula make_bool(String v) {
		return bmgr.makeVariable(v);
	}

/**
 * make an IMPLIES Boolean formula
 * @param b1 : left side of implication
 * @param b2 : right side of implication 
 */
	BooleanFormula implies(BooleanFormula b1, BooleanFormula b2) {
		return bmgr.implication(b1, b2);
	}
	
/**
 * make an AND Boolean formula
 * @param bs : (can be several) all the Boolean formulae to be put into AND
 */
	BooleanFormula and(BooleanFormula... bs) {
		return bmgr.and(bs);
	}

/**
* make an OR Boolean formula
* @param bs : (can be several) all the Boolean formulae to be put into OR
*/
	BooleanFormula or(BooleanFormula... bs) {
		return bmgr.or(bs);
	}
	
/**
 * parse a SMT format string to build a Boolean formula
 * @param f : SMT format string to be parsed
 */
	BooleanFormula parse(String f) {
		return fmgr.parse("(assert " + f + ")");
	}

/**
 * check if a Boolean formula is satisfiable
 * @param constraint : Boolean formula to be checked
 */	
	boolean isSatisfiable(BooleanFormula constraint)
			throws
			SolverException,
			InterruptedException
	{
	// check if "constraint" is satisfiable:
	// if so, then return a model satisfying "constraint"
	// else, then return UNSAT
		ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_MODELS);
		prover.addConstraint(constraint);
		return !prover.isUnsat();
	}
	
	// ====================

/**
 * constructor (empty ADA)
 * @param config : configuration
 * @param logger : log manager
 * @param shutdown : shutdown manager
 * @param solver : selected solver
 */
	public ADA(Configuration config, LogManager logger, ShutdownManager shutdown, Solvers solver)
			throws
			InvalidConfigurationException
	{
		context = SolverContextFactory.createSolverContext(config, logger, shutdown.getNotifier(), solver);
		fmgr = context.getFormulaManager();
		bmgr = fmgr.getBooleanFormulaManager();
		imgr = fmgr.getIntegerFormulaManager();
	}

	// set of states
	ArrayList<BooleanFormula> Q;

	// initial state
	BooleanFormula i;

	// set of final states
	ArrayList<BooleanFormula> F;

	// set of symbols
	ArrayList<String> SIGMA;

	// set of variables
	ArrayList<IntegerFormula> X;
	
	// transitions
	Map<String, BooleanFormula> DELTA;
	
	// supports
		/* conjunction of final implications */
		BooleanFormula finalImplications;

/**
 * parse a file to assign the ADA
 * @param filename : name of source file
 */
	void readFromFile(String filename)
			throws
			IOException
	{
		File file = new File(filename);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String temp = null;
		// read states
		Q = new ArrayList<BooleanFormula>();
		reader.readLine();
		temp = reader.readLine();
		String [] temp2 = temp.split(" ");
		for(int i = 0; i < temp2.length; i++)
			Q.add(make_bool(temp2[i]));
		// read initial state
		reader.readLine();
		reader.readLine();
		temp = reader.readLine();
		i = parse(temp);
		// read final state
		F = new ArrayList<BooleanFormula>();
		reader.readLine();
		reader.readLine();
		temp = reader.readLine();
		temp2 = temp.split(" ");
		for(int i = 0; i < temp2.length; i++)
			F.add(make_bool(temp2[i]));
		// read symbols
		SIGMA = new ArrayList<String>();
		reader.readLine();
		reader.readLine();
		temp = reader.readLine();
		temp2 = temp.split(" ");
		for(int i = 0; i < temp2.length; i++)
			SIGMA.add(temp2[i]);
		// read variables
		X = new ArrayList<IntegerFormula>();
		reader.readLine();
		reader.readLine();
		temp = reader.readLine();
		temp2 = temp.split(" ");
		for(int i = 0; i < temp2.length; i++) {
			X.add(make_int(temp2[i]));
			make_int(temp2[i] + '0');
			make_int(temp2[i] + '1');
		}
		// read transitions
		DELTA = new HashMap<String, BooleanFormula>();
		reader.readLine();
		reader.readLine();
		String temp3 = null, temp4 = null;
		while((temp = reader.readLine()) != null && (!temp.equals(""))) {
			temp4 = "";
			while(!(temp3 = reader.readLine()).equals("#")) {
				temp4 = temp4 + temp3.trim() + ' ';
			}
			DELTA.put(temp, parse(temp4));
		}
		// close file
		reader.close();
	}

/**
 * save the ADA into a file 
 * @param filename : name of target file
 * */
	void saveIntoFile(String filename)
			throws
			IOException
	{
		FileWriter writer = new FileWriter(filename);
		// write states
		writer.write("STATES\n");
		for(int i = 0; i < Q.size(); i++) {
			writer.write(Q.get(i).toString());
			if(i != Q.size() - 1)
				writer.write(' ');
		}
		// write initial state
		writer.write('\n');
		writer.write('\n');
		writer.write("INITIAL\n");
		writer.write(i.toString());
		// write final state
		writer.write('\n');
		writer.write('\n');
		writer.write("FINAL\n");
		for(int i = 0; i < F.size(); i++) {
			writer.write(F.get(i).toString());
			if(i != F.size() - 1)
				writer.write(' ');
		}
		// write symbols
		writer.write('\n');
		writer.write('\n');
		writer.write("SYMBOLS\n");
		for(int i = 0; i < SIGMA.size(); i++) {
			writer.write(SIGMA.get(i));
			if(i != SIGMA.size() - 1)
				writer.write(' ');
		}
		// write variables
		writer.write('\n');
		writer.write('\n');
		writer.write("VARIABLES\n");
		for(int i = 0; i < X.size(); i++) {
			writer.write(X.get(i).toString());
			if(i != X.size() - 1)
				writer.write(' ');
		}
		// write transitions
		writer.write('\n');
		writer.write('\n');
		writer.write("TRANSITIONS\n");
		Iterator<Entry<String, BooleanFormula>> iter = DELTA.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, BooleanFormula> entry = (Entry<String, BooleanFormula>) iter.next();
			writer.write(entry.getKey());
			writer.write('\n');
			writer.write(entry.getValue().toString());
			writer.write('\n');
			writer.write('#');
			writer.write('\n');
		}
		// close file
		writer.close();
	}
	
	/**
	 * add time-stamp to a Boolean formula
	 * @param original : original Boolean formula
	 * @param stamp : time-stamp to be added
	 */
		BooleanFormula addTimeStamp(BooleanFormula original, int stamp) {
			Map<Formula, Formula> fromToMapping = new HashMap<Formula, Formula>();
			// add time-stamp for states
			for(int i = 0; i < Q.size(); i++)
				fromToMapping.put(Q.get(i), make_bool(Q.get(i).toString() + '_' + stamp));
			// add time-stamp for variables
			for(int i = 0; i < X.size(); i++) {
				fromToMapping.put(make_int(X.get(i).toString() + '0'), make_int(X.get(i).toString() + stamp));
				fromToMapping.put(make_int(X.get(i).toString() + '1'), make_int(X.get(i).toString() + (stamp + 1)));
			}
			BooleanFormula result = fmgr.substitute(original, fromToMapping);
			return result;
		}
	
	class Node {
		
		int num;
		Node father;
		ArrayList<String> arrivingSymbol;
		BooleanFormula label;
		Map<String, Node> children;
		
		public Node() {
			num = -1;
			father = null;
			arrivingSymbol = new ArrayList<String>();
			label = null;
			children = new HashMap<String, Node>();
		}
		
		public Node(Node n) {
			num = n.num;
			father = n.father;
			arrivingSymbol = new ArrayList<String>(n.arrivingSymbol);
			label = n.label;
			children = new HashMap<String, Node>(n.children);
		}
		
		public void addChild(String symbol, Node child) {
			children.put(symbol, child);
		}
		
		public String toString( ) {
			return label.toString();
		}
		
		public CheckResult isAccepting() {
			CheckResult result = new CheckResult();
			BooleanFormula toCheck = addTimeStamp(i, 0);
			ArrayList<String> freeVariables = new ArrayList<String>();
			freeVariables.add(i.toString());
			for(int i = 0; i < arrivingSymbol.size(); i++) {
				
			}
			System.out.println(result);
			return result;
		}
		
	}

/** check if the ADA is empty */
	public boolean is_empty()
			throws
			IOException
	{
		// setup final conjunction of implications
		for(int i = 0; i < Q.size(); i++) {
			if(!F.contains(Q.get(i))) {
				if(finalImplications == null)
					finalImplications = implies(Q.get(i), make_bool("false"));
				else
					finalImplications = and(finalImplications, implies(Q.get(i), make_bool("false")));
			}
		}
		// initialize
			/* set of nodes */
			ArrayList<Node> N = new ArrayList<Node>();
			/* work list */
			ArrayList<Node> WorkList = new ArrayList<Node>();
			/* coverage */
			ArrayList<Node> Covered = new ArrayList<Node>();
			ArrayList<Node> Covering = new ArrayList<Node>();
			/* root */
			Node r = new Node();
			r.label = i;
			r.num = 0;
			WorkList.add(r);
		
		// start
		while(!WorkList.isEmpty())
		{
			// dequeue n from WorkList
			Node n = new Node(WorkList.get(0));
			System.out.println("Current [Node " + n.num + "] : " + n);
			WorkList.remove(0);
			// add n into N
			N.add(n);
			// check if n is accepting
			CheckResult result = n.isAccepting();
			if(result.value) {
				// counterexample is feasible
				System.out.println("Model found:");
				System.out.println(result.model);
				return false;
			}
			else {
				// spurious counterexample
			}
		}
	    return true;
	}
}