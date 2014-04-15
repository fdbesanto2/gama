/*********************************************************************************************
 * 
 *
 * 'PertinenceChainStatement.java', in plugin 'msi.gama.core', is part of the source code of the 
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gaml.statements;

import java.util.List;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.precompiler.*;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaList;
import msi.gaml.compilation.ISymbol;
import msi.gaml.descriptions.IDescription;

@symbol(name = IKeyword.CHAIN, kind = ISymbolKind.SEQUENCE_STATEMENT, with_sequence = true)
public class PertinenceChainStatement extends PertinenceStatement {

	private List<IStatement> pertinentsCommands;

	public PertinenceChainStatement(final IDescription desc) {
		super(desc);
	}

	@Override
	public Double computePertinence(final IScope scope) throws GamaRuntimeException {
		pertinenceValue = 0;
		pertinentsCommands = new GamaList<IStatement>();
		for ( ISymbol c : commands ) {
			if ( !(c instanceof IStatement) ) {
				continue;
			}
			IStatement command = (IStatement) c;
			Double p = 0.0;
			p = command.computePertinence(scope);
			if ( p > 0.0 ) {
				pertinentsCommands.add(command);
			}
			if ( p > pertinenceValue ) {
				pertinenceValue = p;
			}
		}
		return pertinenceValue;
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		computePertinence(scope);
		Object result = null;
		for ( IStatement c : pertinentsCommands ) {
			result = c.executeOn(scope);
		}
		return result;
	}
}