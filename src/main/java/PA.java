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
import org.sosy_lab.java_smt.api.UFManager;

public class PA {
	
	// JavaSMT configuration and corresponding context
	// ====================
	SolverContext context;
	FormulaManager fmgr;
	BooleanFormulaManager bmgr;
	IntegerFormulaManager imgr;
	UFManager ufmgr;
	
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
	
	enum ExpressionType {equal, distinct};
	
/**
 * make a Boolean formula of 2 integer formulae according to the expression type
 * @param i1 : first integer formula
 * @param i2 : second integer formula
 * @param ET : expression type (relation between i1 and i2)
 */
	BooleanFormula make_bool(IntegerFormula i1, IntegerFormula i2, ExpressionType ET) {
		switch(ET) {
		case equal :
			return imgr.equal(i1, i2);
		case distinct :
			return not(imgr.equal(i1, i2));
		}
		System.out.println("# Error : expression type is not defined.");
		return null;
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
 * constructor (empty PA)
 * @param config : configuration
 * @param logger : log manager
 * @param shutdown : shutdown manager
 * @param solver : selected solver
 */
	public PA(Configuration config, LogManager logger, ShutdownManager shutdown, Solvers solver)
			throws
			InvalidConfigurationException
		{
			context = SolverContextFactory.createSolverContext(config, logger, shutdown.getNotifier(), solver);
			fmgr = context.getFormulaManager();
			bmgr = fmgr.getBooleanFormulaManager();
			imgr = fmgr.getIntegerFormulaManager();
			ufmgr = fmgr.getUFManager();
		}

	// initial state
	BooleanFormula i;

	// set of (the name-strings of) final states
	Set<String> F;
	
	// set of symbols
	Set<String> SIGMA;

	// transition rules
	Map<String, BooleanFormula> DELTA;
	Map<String, String> READ;

/**
 * parse a formula
 * @param f : string to be parsed
 */
	BooleanFormula parse(String f) {
		if(f.charAt(0) == '(') {
			int count = 1;
			int i = 1;
			while(count != 0) {
				if(f.charAt(i) == '(') {
					count++;
				}
				if(f.charAt(i) == ')') {
					count--;
				}
				i++;
			}
			if(i == f.length()) {
				return parse(f.substring(1, i - 1));
			}
			else if(f.charAt(i) == '/') {
				return and(parse(f.substring(1, i - 1)), parse(f.substring(i + 2, f.length())));
			}
			else if(f.charAt(i) == '\\') {
				return or(parse(f.substring(1, i - 1)), parse(f.substring(i + 2, f.length())));
			}
		}
		else {
			if(f.charAt(0) == '{') {
				int i = 1;
				while(f.charAt(i++) != '}') {}
				String name = f.substring(0, i);
				if(i == f.length()) {
					return make_bool(name);
				}
				else if(f.charAt(i) == '/') {
					return and(make_bool(name), parse(f.substring(i + 2, f.length())));
				}
				else if(f.charAt(i) == '\\') {
					return or(make_bool(name), parse(f.substring(i + 2, f.length())));
				}
				else if(f.charAt(i) == '(') {
					i++;
					String temp = "";
					while(f.charAt(i) != ')') {
						char c = f.charAt(i);
						temp = temp + c;
						i++;
					}
					BooleanFormula bf = null;
					if(temp.length() == 0) {
						bf = ufmgr.declareAndCallUF(name, FormulaType.BooleanType, make_int("*"));
					}
					else {
						bf = ufmgr.declareAndCallUF(name, FormulaType.BooleanType, make_int(temp));
					}
					if(i + 1 == f.length()) {
						return bf;
					}
					else if(f.charAt(i + 1) == '/') {
						return and(bf, parse(f.substring(i + 3, f.length())));
					}
					else if(f.charAt(i + 1) == '\\') {
						return or(bf, parse(f.substring(i + 3, f.length())));
					}
				}
			}
			else {
				if(f.length() >= 4 && f.substring(0, 4).equals("true")) {
					BooleanFormula bf = make_bool(true);
					if(f.length() == 4) {
						return bf;
					}
					else if(f.charAt(4) == '/') {
						return parse(f.substring(6, f.length()));
					}
					else if(f.charAt(4) == '\\') {
						return bf;
					}
				}
				else if(f.length() >= 5 && f.substring(0, 5).equals("false")) {
					BooleanFormula bf = make_bool(false);
					if(f.length() == 5) {
						return bf;
					}
					else if(f.charAt(5) == '/') {
						return bf;
					}
					else if(f.charAt(5) == '\\') {
						return parse(f.substring(7, f.length()));
					}
				}
				else {
					int i = 0;
					String left = "";
					String right = "";
					while(f.charAt(i) != '=' && f.charAt(i) != '!') {
						char c = f.charAt(i);
						left = left + c;
						i++;
					}
					IntegerFormula L = make_int(left);
					ExpressionType ET;
					if(f.charAt(i) == '!') {
						ET = ExpressionType.distinct;
						i = i + 2;
					}
					else if(f.charAt(i) == '=') {
						ET = ExpressionType.equal;
						i = i + 1;
					}
					else {
						System.out.println("# Error : string \"" + f + "\" is impossible to be parsed.");
						return null;
					}
					while(i < f.length()) {
						char c = f.charAt(i);
						if(c == '/') {
							IntegerFormula R = make_int(right);
							return and(make_bool(L, R, ET), parse(f.substring(i + 2, f.length())));
						}
						else if(c == '\\') {
							IntegerFormula R = make_int(right);
							return or(make_bool(L, R, ET), parse(f.substring(i + 2, f.length())));
						}
						else {
							right = right + c;
							i++;
						}
					}
					IntegerFormula R = make_int(right);
					return make_bool(L, R, ET);
				}
			}
		}
		System.out.println("# Error : string \"" + f + "\" is impossible to be parsed.");
		return null;
	}
	
/**
 * parse a file to assign the PA
 * @param filename : name of source file
 */
	void readFromFile(String filename)
			throws
			IOException
	{
		File file = new File(filename);
		BufferedReader reader = new BufferedReader(new FileReader(file));	
		char[] char_7 = new char [7];
		// read initial state
		reader.read(char_7,  0, 7);
		String temp = null;
		temp = new String(char_7);
		if(!temp.equals("start: ")) {
			System.out.println("# Error : Wrong format when reading initial state.");
			reader.close();
			return;
		}
		char c;
		temp = "";
		while((c = (char)reader.read()) != '.' ) {
			temp = temp + c;
		}
		temp = temp.replaceAll("\\s*", "");
		i = parse(temp);
		// read final states
		reader.readLine();
		reader.read(char_7,  0, 7);
		temp = new String(char_7);
		if(!temp.equals("final: ")) {
			System.out.println("# Error : Wrong format when reading final state(s).");
			reader.close();
			return;
		}
		temp = "";
		while((c = (char)reader.read()) != '.' ) {
			temp = temp + c;
		}
		temp = temp.replaceAll("\\s*", "");
		F = new HashSet<String>();
		String[] split = null;
		if(!temp.equals("none")) {
			split = temp.split(",");
			for(int i = 0; i < split.length; i++) {
				F.add(split[i]);
			}
		}
		// read transitions
		DELTA = new HashMap<String, BooleanFormula>();
		READ = new HashMap<String, String>();
		SIGMA = new HashSet<String>();
		temp = "";
		int EOF;
		do {
			EOF = reader.read();
			c = (char)EOF;
			temp = temp + c;
			if(c == '.') {
				temp = temp.replaceAll("\\s*", "");
				String[] part = temp.split("--");
				// left part of the transition (only name / without arguments)
				String leftWithArguments = part[0];
				int indexLastLeftPar;
				for(indexLastLeftPar = leftWithArguments.length() - 1; indexLastLeftPar >= 0; indexLastLeftPar--) {
					if(leftWithArguments.charAt(indexLastLeftPar) == '(') {
						break;
					}
				}
				String left = leftWithArguments.substring(0, indexLastLeftPar);
				// symbol
				temp = part[1].split("->")[0];
				int indexLastTwoPoints;
				for(indexLastTwoPoints = temp.length() - 1; indexLastTwoPoints >= 0; indexLastTwoPoints--) {
					if(temp.charAt(indexLastTwoPoints) == ':') {
						break;
					}
				}
				String symbol = temp.substring(1, indexLastTwoPoints);
				String read = temp.substring(indexLastTwoPoints + 1, indexLastTwoPoints + 2);
				SIGMA.add(symbol);
				READ.put(left + " " + symbol, read);
				// right part of the transition
				String right = part[1].split("->")[1].replace(".", "");
				DELTA.put(left + " " + symbol, parse(right));
				temp = "";
			}
		} while(EOF != -1);
		
		/*System.out.println("start : " + i);
		System.out.print("final : ");
		for(String str : F) {
			System.out.print(str + " ");
		}
		System.out.println();
		for(Map.Entry<String, BooleanFormula> entry : DELTA.entrySet()) {
			System.out.println(entry.getKey() + "   ->   #" + READ.get(entry.getKey()) + "#   " + entry.getValue());
		}
		System.out.println();*/

		reader.close();
	}
	
	class Edge {
		
		Node from;
		String symbol;
		Node to;
		List<BooleanFormula> thetaLeft;
		List<BooleanFormula> thetaRight;
		List<String> read;
		
		public Edge() {
			from = null;
			symbol = null;
			to = null;
			thetaLeft = new ArrayList<BooleanFormula>();
			thetaRight = new ArrayList<BooleanFormula>();
			read = new ArrayList<String>();
		}
		
		public Edge(Edge e) {
			from = e.from;
			symbol = e.symbol;
			to = e.to;
			thetaLeft = new ArrayList<BooleanFormula>(e.thetaLeft);
			thetaRight = new ArrayList<BooleanFormula>(e.thetaRight);
			read = new ArrayList<String>(e.read);
		}
		
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
		return result;
	}

	class Node {
		
		int num;
		int step;
		Edge fatherEdge;
		BooleanFormula label;
		List<BooleanFormula> R;
		
		public Node() {
			num = -1;
			step = -1;
			fatherEdge = null;
			label = null;
			R = new ArrayList<BooleanFormula>();
		}
		
		public Node(Node n) {
			num = n.num;
			step = n.step;
			fatherEdge = n.fatherEdge;
			label = n.label;
			R = new ArrayList<BooleanFormula>(n.R);
		}
		
		public String toString() {
			return label.toString();
		}
		
		public CheckResult isAccepting(int backStep) throws SolverException, InterruptedException {
			// collect all edges along the path
			ArrayList<Edge> edges = new ArrayList<Edge>();
			Node current = this;
			for(int i = 0; i < backStep; i++) {
				if(current.fatherEdge != null) {
					edges.add(0, current.fatherEdge);
					current = current.fatherEdge.from;
				}
				else {
					break;
				}
			}
			Node pivot = current;
			// add time-stamp to pivot state
			BooleanFormula pivotWithTimeStamp = addTimeStamp(pivot.label, 0);
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
					BooleanFormula implication = implies(thetaLeftWithTimeStamp, thetaRightWithTimeStamp);					
					thetaWithTimeStamp = and(thetaWithTimeStamp, implication);
				}
				//System.out.println(thetaWithTimeStamp);
				formulaGroup.add(thetaWithTimeStamp);
			}
			// add time-stamp to final implications
			BooleanFormula finalImplications = make_bool(true);
			BooleanFormula finalConjunctionOfThetaRight = pivot.label;
			if(edges.size() > 0) {
				int finalIndex = edges.size() - 1;
				finalConjunctionOfThetaRight = edges.get(finalIndex).thetaRight.get(0);
				if(implicationIsValid(finalConjunctionOfThetaRight, make_bool(false))) {
					finalConjunctionOfThetaRight = make_bool(true);
				}
				for(int j = 1; j < edges.get(finalIndex).thetaLeft.size(); j++) {
					BooleanFormula temp = edges.get(finalIndex).thetaRight.get(j);
					if(!implicationIsValid(temp, make_bool(false))) {
						finalConjunctionOfThetaRight = and(finalConjunctionOfThetaRight, temp);
					}
				}
			}
			Map<String, Formula> UFsWithNames = extractUFsWithNames(finalConjunctionOfThetaRight);
			for(Entry<String, Formula> entry : UFsWithNames.entrySet()) {
				if(!F.contains(entry.getKey())) {
					finalImplications = and(finalImplications, implies((BooleanFormula)entry.getValue(), make_bool(false)));
				}
			}
			BooleanFormula finalImplicationsWithTimeStamp = addTimeStamp(finalImplications, edges.size());
			formulaGroup.add(finalImplicationsWithTimeStamp);
			// check satisfiability of the conjunction
			//System.out.println(formulaGroup);
			CheckResult result = checkConjunctionSatisfiability(formulaGroup);
			// remove time-stamps from the interpolants
			for(int i = 0; i < result.interpolants.size(); i++) {
				result.interpolants.set(i, removeTimeStamp(result.interpolants.get(i)));
			}
			return result;
		}

		public boolean isSuccessorOf(Node n) {
			for(Node current = this; current != null; current = (current.fatherEdge == null ? null : current.fatherEdge.from)) {
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
			for(Node current = this; current != null; current = (current.fatherEdge == null ? null : current.fatherEdge.from)) {
				if(Covered.contains(current))
					return true;
			}
			return false;
		}
		
	}
	
	public Map<String, Formula> extractUFsWithNames(Formula f) {
		Map<String, Formula> result = new HashMap<String, Formula>();
		Map<String, Formula> UFs = fmgr.extractVariablesAndUFs(f);
		for(Entry<String, Formula> entry : UFs.entrySet()) {
			if(fmgr.getFormulaType(entry.getValue()).isBooleanType()) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}
	
	public ArrayList<BooleanFormula> extractUFs(Formula f) {
		ArrayList<BooleanFormula> result = new ArrayList<BooleanFormula>();
		Map<String, Formula> UFs = fmgr.extractVariablesAndUFs(f);
		for(Entry<String, Formula> entry : UFs.entrySet()) {
			if(fmgr.getFormulaType(entry.getValue()).isBooleanType()) {
				result.add((BooleanFormula)entry.getValue());
			}
		}
		return result;
	}
	
	public List<Formula> extractVariables(Formula f) {
		List<Formula> result = new ArrayList<Formula>();
		Map<String, Formula> UFs = fmgr.extractVariables(f);
		for(Entry<String, Formula> entry : UFs.entrySet()) {
			if(fmgr.getFormulaType(entry.getValue()).isIntegerType()) {
				result.add(entry.getValue());
			}
		}
		return result;
	}

/**
 * add time-stamp to a Boolean formula
 * @param original : original Boolean formula
 * @param stamp : time-stamp to be added
 */
	BooleanFormula addTimeStamp(BooleanFormula original, int stamp) {
		Map<Formula, Formula> fromToMapping = new HashMap<Formula, Formula>();
		Map<String, Formula> UFsWithNames = extractUFsWithNames(original);
		for(Entry<String, Formula> entry : UFsWithNames.entrySet()) {
			List<Formula> pArgs = extractVariables(entry.getValue());
			fromToMapping.put(entry.getValue(), ufmgr.declareAndCallUF(entry.getKey() + '_' + stamp, FormulaType.BooleanType, pArgs));
		}
		return fmgr.substitute(original, fromToMapping);
	}
	
/**
 * remove time-stamp from a Boolean formula
 * @param original : original Boolean formula
 */
	BooleanFormula removeTimeStamp(BooleanFormula original) {
		Map<Formula, Formula> fromToMapping = new HashMap<Formula, Formula>();
		Map<String, Formula> UFsWithNames = extractUFsWithNames(original);
		for(Entry<String, Formula> entry : UFsWithNames.entrySet()) {
			List<Formula> pArgs = extractVariables(entry.getValue());
			String name = entry.getKey();
			int i;
			for(i = name.length() - 1; i >= 0; i--) {
				if(name.charAt(i) == '_')
					break;
			}
			name = name.substring(0, i);
			fromToMapping.put(entry.getValue(), ufmgr.declareAndCallUF(name, FormulaType.BooleanType, pArgs));
		}
		return fmgr.substitute(original, fromToMapping);
	}

/** check if the PA is empty */
	public boolean is_empty(int backStep, Boolean printResult, int mode)
			throws
			IOException, SolverException, InterruptedException
	{
		//System.out.println(DELTA);
		//initialize
		ArrayList<Node> N = new ArrayList<Node>();
		ArrayList<Edge> E = new ArrayList<Edge>();
		ArrayList<Node> workList = new ArrayList<Node>();
		ArrayList<Node> covered = new ArrayList<Node>();
		ArrayList<Node> covering = new ArrayList<Node>();
		Node r = new Node();
		r.label = i;
		r.step = 0;
		r.R = extractUFs(i);
		workList.add(r);
		
		//start
		int nodeCounter = 0;
		while(!workList.isEmpty()) {
			//dequeue n from workList
			Node n = null;
			if(mode == 1) {
				n = workList.get(0);
				workList.remove(0);
			}
			else {
				n = workList.get(workList.size() - 1);
				workList.remove(workList.size() - 1);
			}
			//add n into N
			N.add(n);
			n.num = nodeCounter++;
			System.out.println("Current : [ Node " + n.num + " ] : " + n);
			//check whether n is accepting
			CheckResult result = n.isAccepting(backStep);
			//counter-example is feasible
			if(result.value) {
				if(printResult) {
					List<String> word = new ArrayList<String>();
					for(Node c = n; c.fatherEdge != null; c = c.fatherEdge.from) {
						word.add(0, c.fatherEdge.symbol);
					}
					System.out.print(word);
					System.out.println(result);
				}
				return false;
			}
			// counterexample is spurious
			else {
				// pick up all the nodes whose labels need to be strengthened
				ArrayList<Node> nodesToBeStrengthened = new ArrayList<Node>();
				Node current = n;
				//System.out.println(result.interpolants);
				for(int i = 0; i < result.interpolants.size(); i++) {
					nodesToBeStrengthened.add(0, current);
					current = (current.fatherEdge == null ? null : current.fatherEdge.from);
				}
				// strengthen the labels
				boolean b = false;
				for(int i = 0; i < result.interpolants.size(); i++) {
					Node ni = nodesToBeStrengthened.get(i);
					BooleanFormula label = ni.label;
					BooleanFormula interpolant = result.interpolants.get(i);
					if(!implicationIsValid(label, interpolant)) {
						// remove all the out-coverage (covers others)
						for(int j = 0; j < covering.size(); j++) {
							if(covering.get(j).num == ni.num) {
								covered.remove(j);
								covering.remove(j);
								j--;
							}
						}
						// make conjunction of label and interpolant
						ni.label = and(label, interpolant);
						System.out.println("# Label of [Node " + ni.num + "] strenghthened: " + label + " -> " + ni.label);
						// close if needed
						if(!b)
							b = ni.close(N, covered, covering);
					}
				}
			}
			// expand node
			if(!n.isCovered(covered)) {
				for(String a : SIGMA) {
					Node s = new Node();
					s.step = n.step + 1;
					Edge e = new Edge();
					e.from = n;
					e.symbol = a;
					e.to = s;
					e.thetaLeft = new ArrayList<BooleanFormula>(n.R);
					e.thetaRight = new ArrayList<BooleanFormula>();
					Set<BooleanFormula> tempR = new HashSet<BooleanFormula>();
					for(int j = 0; j < e.thetaLeft.size(); j++) {
						String name = null;
						for(String x : extractUFsWithNames(e.thetaLeft.get(j)).keySet()) {
							name = x;
						}
						String temp = name + " " + a;
						//System.out.println(temp);
						BooleanFormula right = DELTA.get(temp);
						if(right == null) {
							e.thetaRight.add(make_bool(false));
							e.read.add("i");
						}
						else {
							String read = READ.get(temp);
							if(read.equals("i")) {
								Map<Formula, Formula> fromToMapping = new HashMap<Formula, Formula>();
								fromToMapping.put(make_int("i"), make_int("x_" + s.step));
								right = fmgr.substitute(right, fromToMapping);
							}
							else {
								String variableName = extractVariables(e.thetaLeft.get(j)).get(0).toString();
								Map<Formula, Formula> fromToMapping = new HashMap<Formula, Formula>();
								fromToMapping.put(make_int("i"), make_int(variableName));
								fromToMapping.put(make_int("j"), make_int("x_" + s.step));
								right = fmgr.substitute(right, fromToMapping);
							}
							e.thetaRight.add(right);
							e.read.add(read);
							ArrayList<BooleanFormula> freeBooleanVariablesRight = extractUFs(right);
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
					//System.out.println("Add : " + e.from + " - " + e.symbol + " ->" + e.to + "   " + e.thetaLeft + "   " + e.thetaRight);
					workList.add(s);
				}
			}
			System.out.println();
		}
		return true;
	}

}