/*********************************************************************************************
 *
 *
 * 'ISkill.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 *
 *
 **********************************************************************************************/
package msi.gaml.skills;

import msi.gama.common.interfaces.INamed;
import msi.gaml.compilation.ISkillConstructor;

/**
 * SkillInterface - convenience interface for any object that might be used as a "skill" for an
 * agent.
 *
 * @author drogoul 4 juil. 07
 */
public interface ISkill extends INamed {

	public abstract ISkill duplicate();

	public void setDuplicator(ISkillConstructor duplicator);

	public String getDefiningPlugin();

	public void setDefiningPlugin(final String plugin);
}
