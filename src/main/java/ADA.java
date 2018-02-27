/*
	JAltImpact
    Copyright (C) 2017  Xiao XU & Radu IOSIF

	This file is part of JAltImpact.

    JAltImpact is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
    
    If you have any questions, please contact Xiao XU <xiao.xu.cathiec@gmail.com>.
*/

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
 * make a Boolean variable
 * @param b : Boolean value
 */
	BooleanFormula make_bool(boolean b) {
		return bmgr.makeBoolean(b);
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
 * make a NOT Boolean formula
 * @param b : Boolean formula to be negated
 */
	BooleanFormula not(BooleanFormula b) {
		return bmgr.not(b);
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
 * 	given 2 Boolean formulae, check whether the implication is valid
 * @param b1 : left side of implication
 * @param b2 : right side of implication
 * @throws InterruptedException 
 * @throws SolverException 
 */
	boolean implicationIsValid(BooleanFormula b1, BooleanFormula b2)
			throws
			SolverException,
			InterruptedException
	{
		BooleanFormula implication = implies(b1, b2);
		ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_MODELS);
		prover.addConstraint(not(implication));
		boolean isUnsat = prover.isUnsat();
		prover.close();
		if(isUnsat)
			return true;
		else
			return false;	
	}
	
/**
 * parse a SMT format string to build a Boolean formula
 * @param f : SMT format string to be parsed
 */
	BooleanFormula parse(String f) {
		return fmgr.parse("(assert " + f + ")");
	}

/**
 * given n Boolean formulae, check the satisfiability of the conjunction of these Boolean formulae
 * if not satisfiable, then compute the interpolants (there will be n - 1 interpolants)
 * @param cb : collection of n Boolean formulae
 * @return [.value] whether the conjunction is satisfiable [.model] model proving satisfiability if satisfiable [.interpolants] collection of interpolants if not satisfiable
 * @throws InterruptedException 
 * @throws SolverException 
 */
	CheckResult checkConjunctionSatisfiability(ArrayList<BooleanFormula> cb)
			throws
			SolverException,
			InterruptedException
	{
		CheckResult result = new CheckResult();
		InterpolatingProverEnvironment prover = context.newProverEnvironmentWithInterpolation();
		/*List<Set> temp = new ArrayList<Set>();
		for(int i = 0; i < cb.size(); i++) {
			Set<Object> temp2 = new HashSet();
			Object temp3 = prover.push(cb.get(i));
			temp2.add(temp3);
			temp.add(temp2);
		}
		if(prover.isUnsat()) {
			result.value = false;
			result.interpolants = (ArrayList<BooleanFormula>) prover.getSeqInterpolants(temp);
		}*/
		List temp = new ArrayList();
		for(int i = 0; i < cb.size(); i++) {
			temp.add(prover.push(cb.get(i)));
		}
		if(prover.isUnsat()) {
			for(int i = 0; i < temp.size() - 1; i++) {
				List temp2 = new ArrayList();
				for(int j = 0; j <= i; j++) {
					temp2.add(temp.get(j));
				}
				result.interpolants.add(prover.getInterpolant(temp2));
			}
			result.value = false;
		}
		else {
			result.value = true;
			result.model = prover.getModel();
		}
		
		//prover.close();
		return result;
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
		for(int i = 0; i < temp2.length; i++) {
			Q.add(make_bool(temp2[i]));
		}
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
		// add time-stamp to states
		for(int i = 0; i < Q.size(); i++)
			fromToMapping.put(Q.get(i), make_bool(Q.get(i).toString() + '_' + stamp));
		// add time-stamp to variables
		for(int i = 0; i < X.size(); i++) {
			fromToMapping.put(make_int(X.get(i).toString() + '0'), make_int(X.get(i).toString() + (stamp - 1)));
			fromToMapping.put(make_int(X.get(i).toString() + '1'), make_int(X.get(i).toString() + stamp));
		}
		BooleanFormula result = fmgr.substitute(original, fromToMapping);
		return result;
	}
	
/**
 * remove time-stamp from a Boolean formula
 * @param original : original Boolean formula
 * @param stamp : time-stamp to be removed
 */
	BooleanFormula removeTimeStamp(BooleanFormula original, int stamp) {
		Map<Formula, Formula> fromToMapping = new HashMap<Formula, Formula>();
		// remove time-stamp from states
		for(int i = 0; i < Q.size(); i++)
			fromToMapping.put(make_bool(Q.get(i).toString() + '_' + stamp), Q.get(i));
		// remove time-stamp from variables
		for(int i = 0; i < X.size(); i++) {
			fromToMapping.put(make_int(X.get(i).toString() + stamp), X.get(i));
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
		ArrayList<BooleanFormula> R;
		
		public Node() {
			num = -1;
			fatherEdge = null;
			label = null;
			R = new ArrayList<BooleanFormula>();
		}
		
		public Node(Node n) {
			num = n.num;
			fatherEdge = n.fatherEdge;
			label = n.label;
			R = new ArrayList<BooleanFormula>(n.R);
		}
		
		public String toString( ) {
			return label.toString();
		}
		
		public CheckResult isAccepting(int backStep) throws SolverException, InterruptedException {
			// collect all edges along the path
			ArrayList<Edge> edges = new ArrayList<Edge>();
			Node current = this;
			for(int i = 0; i < backStep; i++) {
				if(current.fatherEdge != null) {
					edges.add(0, current.fatherEdge);
					current = current.fatherEdge.left;
				}
				else {
					break;
				}
			}
			Node pivot = current;
			// add time-stamp to pivot state
			BooleanFormula pivotWithTimeStamp = addTimeStamp(pivot.label, 0);
			// add time-stamp to final implications
			BooleanFormula finalImplicationsWithTimeStamp = addTimeStamp(finalImplications, edges.size());
			// add time-stamp to edges and collect all the time-stamp-added formulae
			ArrayList<BooleanFormula> formulaGroup = new ArrayList<BooleanFormula>();
			formulaGroup.add(pivotWithTimeStamp);
			for(int i = 0; i < edges.size(); i++) {
				BooleanFormula thetaLeftWithTimeStamp = addTimeStamp(edges.get(i).thetaLeft.get(0), i);
				BooleanFormula thetaRightWithTimeStamp = addTimeStamp(edges.get(i).thetaRight.get(0), i + 1);
				BooleanFormula thetaWithTimeStamp = implies(thetaLeftWithTimeStamp, thetaRightWithTimeStamp);
				for(int j = 1; j < edges.get(i).thetaLeft.size(); j++) {
					thetaLeftWithTimeStamp = addTimeStamp(edges.get(i).thetaLeft.get(j), i);
					thetaRightWithTimeStamp = addTimeStamp(edges.get(i).thetaRight.get(j), i + 1);
					thetaWithTimeStamp = and(thetaWithTimeStamp, implies(thetaLeftWithTimeStamp, thetaRightWithTimeStamp));
				}
				formulaGroup.add(thetaWithTimeStamp);
			}
			formulaGroup.add(finalImplicationsWithTimeStamp);
			// check satisfiability of the conjunction
			//System.out.println(formulaGroup);
			CheckResult result = checkConjunctionSatisfiability(formulaGroup);
			// remove time-stamps from the interpolants
			for(int i = 0; i < result.interpolants.size(); i++) {
				result.interpolants.set(i, removeTimeStamp(result.interpolants.get(i), i));
			}
			return result;
		}
		
		public boolean isSuccessorOf(Node n) {
			for(Node current = this; current != null; current = (current.fatherEdge == null ? null : current.fatherEdge.left)) {
				if(current.num == n.num)
					return true;
			}
			return false;
		}
		
		public boolean close(ArrayList<Node> N, ArrayList<Node> Covered, ArrayList<Node> Covering)
				throws
				SolverException,
				InterruptedException
		{
			for(int i = 0; i < num; i++) {
				Node y = N.get(i);
				if(implicationIsValid(label, y.label)) {
					for(int j = 0; j < Covering.size(); j++) {
						if(Covering.get(j).isSuccessorOf(this)) {
							Covered.remove(j);
							Covering.remove(j);
							j--;
						}
					}
					Covered.add(this);
					Covering.add(y);
					return true;
				}
			}
			return false;
		}
		
		public boolean isCovered(ArrayList<Node> Covered) {
			for(Node current = this; current != null; current = (current.fatherEdge == null ? null : current.fatherEdge.left)) {
				if(Covered.contains(current))
					return true;
			}
			return false;
		}
		
	}

/** check if the ADA is empty */
	public boolean is_empty(int backStep, Boolean printResult, int mode)
			throws
			IOException, SolverException, InterruptedException
	{
		// setup final conjunction of implications
		for(int i = 0; i < Q.size(); i++) {
			if(!F.contains(Q.get(i))) {
				if(finalImplications == null)
					finalImplications = implies(Q.get(i), make_bool(false));
				else
					finalImplications = and(finalImplications, implies(Q.get(i), make_bool(false)));
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
			r.R = getFreeBooleanVariables(i);
			WorkList.add(r);
		
		// start
		int nodeCounter = 0;
		while(!WorkList.isEmpty()) {
			// dequeue n from WorkList
			Node n = null;
			if(mode == 1) {
				n = WorkList.get(0);
				WorkList.remove(0);
			}
			else if(mode == 2) {
				n = WorkList.get(WorkList.size() - 1);
				WorkList.remove(WorkList.size() - 1);
			}
			// add n into N
			N.add(n);
			n.num = nodeCounter++;
			//System.out.println("Current [Node " + n.num + "] : " + n);
			// check whether n is accepting
			CheckResult result = n.isAccepting(backStep);
			// counterexample is feasible
			if(result.value) {
				if(printResult) {
					System.out.println(result);
				}
				return false;
			}
			// counterexample is spurious
			else {
				// pick up all the nodes whose labels need to be strengthened
				ArrayList<Node> nodesToBeStrengthened = new ArrayList<Node>();
				Node current = n;
				for(int i = 0; i < result.interpolants.size(); i++) {
					nodesToBeStrengthened.add(0, current);
					current = (current.fatherEdge == null ? null : current.fatherEdge.left);
				}
				// strengthen the labels
				boolean b = false;
				for(int i = 0; i < result.interpolants.size(); i++) {
					Node ni = nodesToBeStrengthened.get(i);
					BooleanFormula label = ni.label;
					BooleanFormula interpolant = result.interpolants.get(i);
					if(!implicationIsValid(label, interpolant)) {
						// remove all the out-coverage (covers others)
						for(int j = 0; j < Covering.size(); j++) {
							if(Covering.get(j).num == ni.num) {
								Covered.remove(j);
								Covering.remove(j);
								j--;
							}
						}
						// make conjunction of label and interpolant
						ni.label = and(label, interpolant);
						//System.out.println("# Label of [Node " + ni.num + "] strenghthened: " + label + " -> " + ni.label);
						// close if needed
						if(!b)
							b = ni.close(N, Covered, Covering);
					}
				}
			}
			// expand node
			if(!n.isCovered(Covered)) {
				for(int i = 0; i < SIGMA.size(); i++) {
					String a = SIGMA.get(i);
					Node s = new Node();
					Edge e = new Edge();
					e.left = n;
					e.symbol = a;
					e.right = s;
					e.thetaLeft = new ArrayList<BooleanFormula>(n.R);
					e.thetaRight = new ArrayList<BooleanFormula>();
					Set<BooleanFormula> tempR = new HashSet<BooleanFormula>();
					for(int j = 0; j < e.thetaLeft.size(); j++) {
						String temp = a + " " + e.thetaLeft.get(j);
						BooleanFormula right = DELTA.get(temp);
						if(right == null) {
							e.thetaRight.add(make_bool(false));
						}
						else {
							e.thetaRight.add(right);
							ArrayList<BooleanFormula> freeBooleanVariablesRight = getFreeBooleanVariables(right);
							for(int k = 0; k < freeBooleanVariablesRight.size(); k++)
								tempR.add(freeBooleanVariablesRight.get(k));
						}
					}
					Iterator<BooleanFormula> iterator = tempR.iterator();
					while(iterator.hasNext()) {
						s.R.add(iterator.next());
					}
					E.add(e);
					s.label = make_bool(true);
					s.fatherEdge = e;
					WorkList.add(s);
				}
			}
			//System.out.println();
		}
	    return true;
	}

}