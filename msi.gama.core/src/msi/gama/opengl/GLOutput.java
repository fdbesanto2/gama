package msi.gama.opengl;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.util.GuiUtils;
import msi.gama.outputs.AbstractDisplayOutput;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.precompiler.GamlAnnotations.with_sequence;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.compilation.ISymbolKind;
import msi.gaml.descriptions.IDescription;
import msi.gaml.types.IType;

/**
 * @author arno: This output is define in the core but it should be (or can be) defined in the
 *         plugin jogl
 * 
 */
@SuppressWarnings("unchecked")
@symbol(name = IKeyword.DISPLAY_GL, kind = ISymbolKind.OUTPUT)
@facets(value = {
// @facet(name = IKeyword.DISPLAY_GRAPH, type = IType.STRING_STR, optional = false),
@facet(name = IKeyword.NAME, type = IType.STRING_STR, optional = true) }, omissible = IKeyword.NAME)
@with_sequence
@inside(symbols = IKeyword.OUTPUT)
public class GLOutput extends AbstractDisplayOutput {

	public GLOutput(final IDescription desc) {
		super(desc);
		// System.err.println("output created");

	}

	@Override
	public String getViewId() {
		return GuiUtils.GL_VIEW_ID;

	}

	@Override
	public void compute(final IScope scope, final int cycle) throws GamaRuntimeException {
		// System.err.println("compute");
	}

}
