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

import java.util.ArrayList;

import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.Model;

public class CheckResult {

	Boolean value;
	Model model;
	ArrayList<BooleanFormula> interpolants;
	
	public CheckResult() {
		value = null;
		model = null;
		interpolants = new ArrayList<BooleanFormula>();
	}
	
	public String toString() {
		if(!value) {
			return "$ Non-Accepting @ Interpolants :\n$ " + interpolants.toString();
		}
		else {
			return "$ Accepting @ Model :\n" + ( model == null ? "[Empty Model]" : model.toString());
		}
	}
	
}
