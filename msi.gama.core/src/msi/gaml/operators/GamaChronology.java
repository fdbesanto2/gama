/*********************************************************************************************
 * 
 *
 * 'GamaChronology.java', in plugin 'msi.gama.core', is part of the source code of the 
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gaml.operators;

import org.joda.time.*;
import org.joda.time.chrono.*;
import org.joda.time.field.PreciseDurationField;

/**
 * The class GamaChronology.
 * 
 * @author drogoul
 * @since 20 janv. 2012
 * 
 */

public final class GamaChronology extends AssembledChronology {

	private static final GamaChronology INSTANCE_UTC;

	static {
		INSTANCE_UTC = new GamaChronology(GregorianChronology.getInstanceUTC());
	}

	public static GamaChronology getInstance() {
		return INSTANCE_UTC;
	}

	private GamaChronology(final Chronology base) {
		super(base, null);
	}

	@Override
	protected void assemble(final org.joda.time.chrono.AssembledChronology.Fields fields) {
		fields.months =
			new PreciseDurationField(DurationFieldType.months(), (long) IUnits.month * 1000);
		fields.years =
			new PreciseDurationField(DurationFieldType.months(), (long) IUnits.year * 1000);
	}

	@Override
	public Chronology withUTC() {
		return INSTANCE_UTC;
	}

	@Override
	public Chronology withZone(final DateTimeZone zone) {
		throw new UnsupportedOperationException("Method was not implemented");
	}

	@Override
	public String toString() {
		return "EightHoursDayChronology";
	}

}
