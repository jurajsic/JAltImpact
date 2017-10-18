import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.sosy_lab.java_smt.api.FormulaType;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.InterpolatingProverEnvironment;
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
 * make an AND Boolean formula
 * @param cb : collection of Boolean formulae to be put into AND
 */
	BooleanFormula and(Collection<BooleanFormula> cb) {
		return bmgr.and(cb);
	}

/**
 * make an OR Boolean formula
 * @param bs : (can be several) all the Boolean formulae to be put into OR
 */
	BooleanFormula or(BooleanFormula... bs) {
		return bmgr.or(bs);
	}

/**
 * make an OR Boolean formula
 * @param cb : collection of Boolean formulae to be put into OR
 */
	BooleanFormula or(Collection<BooleanFormula> cb) {
		return bmgr.or(cb);
	}
	
/**
 * parse a SMT format string to build a Boolean formula
 * @param f : SMT format string to be parsed
 */
	BooleanFormula parse(String f) {
		return fmgr.parse("(assert " + f + ")");
	}

/**
 * get all free Boolean variables from a given formula
 * @param f : formula from which all free Boolean variables will be got
 */
	ArrayList<BooleanFormula> getFreeBooleanVariables(Formula f) {
		ArrayList<BooleanFormula> result = new ArrayList<BooleanFormula>();
		Map<String, Formula> allFreeVariablesMap = fmgr.extractVariables(f);
		Iterator<Entry<String, Formula>> iter = allFreeVariablesMap.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Formula> entry = (Entry<String, Formula>) iter.next();
			Formula temp = entry.getValue();
			if(fmgr.getFormulaType(temp).toString().equals("Boolean")) {
				result.add((BooleanFormula) temp);
			}
		}
		return result;
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
	
	class Edge {
		
		Node left;
		String symbol;
		Node right;
		ArrayList<BooleanFormula> thetaLeft;
		ArrayList<BooleanFormula> thetaRight;
		
		public Edge() {
			left = null;
			symbol = null;
			right = null;
			thetaLeft = new ArrayList<BooleanFormula>();
			thetaRight = new ArrayList<BooleanFormula>();
		}
		
		public Edge(Edge e) {
			left = e.left;
			symbol = e.symbol;
			right = e.right;
			thetaLeft = new ArrayList<BooleanFormula>(e.thetaLeft);
			thetaRight = new ArrayList<BooleanFormula>(e.thetaRight);
		}
		
	}
		
	class Node {
		
		int num;
		Edge fatherEdge;
		BooleanFormula label;
		ArrayList<Edge> childrenEdge;
		
		public Node() {
			num = -1;
			fatherEdge = null;
			label = null;
			childrenEdge = new ArrayList<Edge>();
		}
		
		public Node(Node n) {
			num = n.num;
			fatherEdge = n.fatherEdge;
			label = n.label;
			childrenEdge = new ArrayList<Edge>(n.childrenEdge);
		}
		
		public String toString( ) {
			return label.toString();
		}
		
		public CheckResult isAccepting() throws SolverException, InterruptedException {
			CheckResult result = new CheckResult();
			ArrayList<Edge> temp = new ArrayList<Edge>();
			for(Node current = this; current.label != i; current = current.fatherEdge.left)
				temp.add(0, fatherEdge);
			
			BooleanFormula x = parse("(> x1 1)"), y = parse("(<= x1 1)");
			InterpolatingProverEnvironment<BooleanFormula> prover = (InterpolatingProverEnvironment<BooleanFormula>) context.newProverEnvironmentWithInterpolation();
			List<BooleanFormula> z = new ArrayList<BooleanFormula>();
			z.add(x);
			z.add(y);
			//prover.push(make_bool("true"));
			System.out.println(prover.getInterpolant(z));
			return result;
		}
		
	}

/** check if the ADA is empty */
	public boolean is_empty()
			throws
			IOException, SolverException, InterruptedException
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
			/* set of edges */
			ArrayList<Edge> E = new ArrayList<Edge>();
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
			System.out.println("\n" + result);
			if(result.value) {
				// counterexample is feasible
				
				return false;
			}
			else {
				// spurious counterexample
			}
		}
	    return true;
	}
}