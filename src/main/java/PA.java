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

	// array of (strings of) final states
	String[] F;

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
					List<Formula> arguments = new ArrayList<Formula>();
					String temp = "";
					while(f.charAt(i) != ')') {
						char c = f.charAt(i);
						if(c != ',') {
							temp = temp + c;
						}
						else {
							arguments.add(make_int(temp));
							temp = "";
						}
						i++;
					}
					if(temp.length() > 0) {
						arguments.add(make_int(temp));
					}
					if(arguments.size() == 0) {
						BooleanFormula bf = make_bool(name);
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
					else {
						BooleanFormula bf = ufmgr.declareAndCallUF(name, FormulaType.BooleanType, arguments);
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
		//System.out.println("start: " + i);
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
		if(!temp.equals("none")) {
			F = temp.split(",");
			/*System.out.print("final: ");
			for(int i = 0; i < F.length - 1; i++)
				System.out.print(F[i] + ", ");
			System.out.println(F[F.length - 1]);*/
		}
		else {
			F = new String[0];
			//System.out.println("final: none");
		}
		// read transitions
		temp = "";
		int EOF;
		do {
			EOF = reader.read();
			c = (char)EOF;
			temp = temp + c;
			if(c == '.') {
				temp = temp.replaceAll("\\s*", "");
				String[] part = temp.split("--");
				// left part of the transition
				String left = part[0];
				String[] tempSplit = part[1].split(":");
				// data read during the transition
				char data = tempSplit[tempSplit.length - 1].split("->")[0].charAt(0);
				temp = part[1].split("->")[0];
				int indexLastTwoPoints;
				for(indexLastTwoPoints = temp.length() - 1; indexLastTwoPoints >= 0; indexLastTwoPoints--) {
					if(temp.charAt(indexLastTwoPoints) == ':') {
						break;
					}
				}
				// symbol
				String symbol = temp.substring(1, indexLastTwoPoints);
				// right part of the transition
				String right = part[1].split("->")[1].replace(".", "");
				BooleanFormula implication = implies(parse(left), parse(right));
				Map<IntegerFormula, IntegerFormula> fromToMapping = new HashMap<IntegerFormula, IntegerFormula>();
				if(data == 'i') {
					fromToMapping.put(make_int("i"), make_int("newValue"));
				}
				else if(data == 'j') {
					fromToMapping.put(make_int("i"), make_int("oldValue"));
					fromToMapping.put(make_int("j"), make_int("newValue"));
				}
				else {
					System.out.println("# Error : Wrong format when reading transitions.");
					reader.close();
					return;
				}
				implication = fmgr.substitute(implication, fromToMapping);
				temp = "";
			}
		} while(EOF != -1);

		reader.close();
	}

}