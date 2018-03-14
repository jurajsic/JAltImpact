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
import org.sosy_lab.java_smt.api.FunctionDeclaration;
import org.sosy_lab.java_smt.api.FunctionDeclarationKind;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.InterpolatingProverEnvironment;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;
import org.sosy_lab.java_smt.api.SolverException;
import org.sosy_lab.java_smt.api.UFManager;
import org.sosy_lab.java_smt.basicimpl.FunctionDeclarationImpl;

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
	
	// transition rules
	Map<String, BooleanFormula> DELTA;

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
				// right part of the transition
				String right = part[1].split("->")[1].replace(".", "");
				BooleanFormula implication = implies(parse(leftWithArguments), parse(right));
				DELTA.put(left + " " + symbol, implication);
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
			System.out.println(entry.getKey() + "   ->   " + entry.getValue());
		}
		System.out.println();*/

		reader.close();
	}
	
	public boolean is_empty(int backStep, Boolean printResult, int mode)
			throws
			IOException, SolverException, InterruptedException
	{
		
		return true;
	}

}