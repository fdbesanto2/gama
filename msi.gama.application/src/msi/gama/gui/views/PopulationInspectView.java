/*
 * GAMA - V1.4 http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2012
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2012
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen (Batch, GeoTools & JTS), 2009-2012
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2012
 * - Phan Huy Cuong, DREAM team, Univ. Can Tho (XText-based GAML), 2012
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gama.gui.views;

import java.util.*;
import java.util.List;
import msi.gama.common.interfaces.*;
import msi.gama.common.util.GuiUtils;
import msi.gama.gui.parameters.*;
import msi.gama.gui.swt.SwtGui;
import msi.gama.gui.swt.commands.AgentsMenu;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.population.IPopulation;
import msi.gama.outputs.*;
import msi.gama.runtime.*;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaList;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.species.ISpecies;
import msi.gaml.types.*;
import msi.gaml.variables.IVariable;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * Written by drogoul Modified on 18 mai 2011
 * 
 * @todo Description
 * 
 */
// TODO Change this to a TabFolder
public class PopulationInspectView extends GamaViewPart {

	public static final String ID = GuiUtils.TABLE_VIEW_ID;
	public static final String CUSTOM = "custom";
	public static final List<String> DONT_INSPECT = Arrays.asList(IKeyword.PEERS, IKeyword.MEMBERS, IKeyword.AGENTS);
	final IScope scope = GAMA.obtainNewScope();
	boolean locked;
	TableViewer viewer;
	org.eclipse.swt.widgets.List /* speciesMenu, */attributesMenu;
	private AgentComparator comparator;
	private Label attributesLabel;
	private Composite expressionComposite;
	private ExpressionEditor customEditor;
	// private CLabel sizeLabel;
	private CTabItem currentTab;
	Map<String, List<String>> selectedColumns = new HashMap();

	@Override
	public void update(final IDisplayOutput output) {
		final IExpression expr = getOutput().getValue();
		if ( expr != null ) {
			viewer.setInput(getOutput().getLastValue());
		} else {
			viewer.setInput(null);
		}
		changePartName(currentTab.getText());
		viewer.refresh();
	}

	private int computeSize() {
		final IExpression expr = getOutput().getValue();
		if ( expr != null ) {
			final List list = getOutput().getLastValue();
			return list == null ? 0 : list.size();
		}
		return 0;
	}

	@Override
	public InspectDisplayOutput getOutput() {
		return (InspectDisplayOutput) super.getOutput();
	}

	@Override
	public void setOutput(final IDisplayOutput output) {
		super.setOutput(output);
		final IExpression expr = getOutput().getValue();
		if ( expr != null ) {
			final String name = expr.getContentType().getSpeciesName();
			if ( expr.literalValue().equals(name) ) {
				setSpeciesName(name, true);
			} else {
				setSpeciesName(CUSTOM, true);
			}
		}
	}

	private void setSpeciesName(final String name, final boolean fromMenu) {
		final String speciesName = name;
		if ( !CUSTOM.equals(speciesName) ) {
			if ( fromMenu ) {
				hideExpressionComposite();
				getOutput().setNewExpressionText(name);
			}
		} else {
			if ( fromMenu ) {
				showExpressionComposite();
				if ( customEditor != null ) {
					customEditor.setEditorTextNoPopup(getOutput().getExpressionText());
				}
			}
		}
		if ( !selectedColumns.containsKey(name) ) {
			selectedColumns.put(name, new ArrayList());
			final List<String> names = getOutput().getAttributes();
			if ( names != null ) {
				selectedColumns.get(name).addAll(names);
			} else if ( getOutput().getValue() != null ) {
				final IExpression expr = getOutput().getValue();
				final String realSpecies = expr.getContentType().getSpeciesName();
				final ISpecies species = GAMA.getModel().getSpecies(realSpecies);
				if ( species == null ) { return; }
				selectedColumns.get(name).addAll(species.getVarNames());
				selectedColumns.get(name).removeAll(DONT_INSPECT);
			}
			Collections.sort(selectedColumns.get(name));
			if ( selectedColumns.get(name).remove(IKeyword.NAME) ) {
				selectedColumns.get(name).add(0, IKeyword.NAME);
			}
		}
		changePartName(name);

	}

	private void changePartName(final String name) {
		this.setContentDescription(StringUtils.capitalize(name) + " population in macro-agent " +
			getOutput().getRootAgent().getName() + "; size: " + computeSize() + " agents");
		if ( name.equals(CUSTOM) ) {
			setPartName("Custom population browser");
		} else {
			setPartName("Population browser on " + name);
		}
	}

	private void createMenus(final Composite parent) {
		final Composite menuComposite = new Composite(parent, SWT.NONE);
		menuComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		final GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 1;
		menuComposite.setLayout(layout);

		// sizeLabel = new CLabel(menuComposite, SWT.LEFT);
		// sizeLabel.setFont(SwtGui.getLabelfont());
		// sizeLabel.setText("0 agents");
		// sizeLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		// final Label title = new Label(menuComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		// title.setText(""); // Spacer
		attributesLabel = new Label(menuComposite, SWT.NONE);
		attributesLabel.setText("Attributes");
		attributesLabel.setFont(SwtGui.getLabelfont());
		attributesMenu = new org.eclipse.swt.widgets.List(menuComposite, SWT.V_SCROLL | SWT.MULTI);
		attributesMenu.setBackground(parent.getBackground());
		attributesMenu.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fillAttributeMenu();
		menuComposite.pack(true);
	}

	private void createExpressionComposite(final Composite intermediate) {
		expressionComposite = new Composite(intermediate, SWT.BORDER_SOLID);
		expressionComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		final GridLayout layout = new GridLayout(3, false);
		layout.verticalSpacing = 5;
		expressionComposite.setLayout(layout);
		final Label lock = new Label(expressionComposite, SWT.NONE);
		lock.setImage(SwtGui.lock);
		lock.setToolTipText("Lock the current expression results (the list of agents will not be changed)");

		customEditor =
			EditorFactory.createExpression(expressionComposite, "Agents to inspect:", "",
				new EditorListener<IExpression>() {

					@Override
					public void valueModified(final IExpression newValue) {
						if ( output == null ) { return; }
						try {
							((InspectDisplayOutput) output).setNewExpression(newValue);
						} catch (final GamaRuntimeException e) {
							e.printStackTrace();
						}
						final ISpecies species = getOutput().getSpecies();
						setSpeciesName(species == null ? null : species.getName(), false);
						fillAttributeMenu();
						// TODO Make a test on the columns.
						recreateViewer();
						update(output);
					}
				}, Types.get(IType.LIST));

		customEditor.getEditor().setToolTipText("Enter a GAML expression returning one or several agents ");
		lock.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(final MouseEvent e) {
				locked = !locked;
				lock.setImage(locked ? SwtGui.unlock : SwtGui.lock);
				customEditor.getEditor().setEnabled(!locked);
			}

		});
		expressionComposite.pack();

	}

	private final SelectionAdapter attributeAdapter = new SelectionAdapter() {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			selectedColumns.put(currentTab.getText(), Arrays.asList(attributesMenu.getSelection()));
			recreateViewer();
			update(getOutput());
		}

	};

	private void fillAttributeMenu() {
		final String speciesName = currentTab.getText();
		attributesMenu.removeAll();
		attributesMenu.setVisible(false);
		attributesLabel.setVisible(false);
		String tooltipText;
		if ( speciesName.equals(CUSTOM) ) {
			tooltipText = "A list of the attributes common to the agents returned by the custom expression";
		} else {
			tooltipText =
				"A list of the attributes defined in species " + speciesName +
					". Select the ones you want to display in the table";
		}
		attributesMenu.setToolTipText(tooltipText);
		final IExpression expr = getOutput().getValue();
		if ( expr != null ) {
			final String realSpecies = expr.getContentType().getSpeciesName();
			final ISpecies species = GAMA.getModel().getSpecies(realSpecies);
			if ( species != null ) {
				final List<String> names = species.getVarNames();
				Collections.sort(names);
				attributesMenu.setItems(names.toArray(new String[0]));
				for ( int i = 0; i < names.size(); i++ ) {
					if ( selectedColumns.get(speciesName) != null &&
						selectedColumns.get(speciesName).contains(names.get(i)) ) {
						attributesMenu.select(i);
					}
				}
				attributesMenu.addSelectionListener(attributeAdapter);
				attributesLabel.setVisible(true);
				attributesMenu.setVisible(true);
			}
		}
	}

	@Override
	public void ownCreatePartControl(final Composite c) {
		final CTabFolder tabFolder = new CTabFolder(c, SWT.TOP);
		tabFolder.setBorderVisible(true);
		tabFolder.setBackgroundMode(SWT.INHERIT_DEFAULT);
		tabFolder.setSimple(true); // rounded tabs
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		final Iterable<IPopulation> populations = getOutput().getRootAgent().getMicroPopulations();
		final List<String> names = new ArrayList();
		for ( final IPopulation pop : populations ) {
			names.add(pop.getName());
		}
		names.add(CUSTOM);
		for ( final String s : names ) {
			final CTabItem item = new CTabItem(tabFolder, SWT.CLOSE);
			item.setText(s);
			item.setImage(SwtGui.speciesImage);
			item.setShowClose(true);
		}
		// Adds a composite to the tab

		final Composite view = new Composite(/* c */tabFolder, SWT.None);
		final String speciesName = getOutput().getExpressionText();
		int index = names.indexOf(speciesName);
		if ( index == -1 ) {
			index = names.indexOf(CUSTOM);
		}
		currentTab = tabFolder.getItem(index);
		currentTab.setControl(view);
		tabFolder.setSelection(currentTab);
		tabFolder.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				currentTab = (CTabItem) e.item;
				final String name = currentTab.getText();
				currentTab.setControl(view);
				setSpeciesName(name, true);
				fillAttributeMenu();
				recreateViewer();
				update(getOutput());
			}

		});
		final GridLayout viewLayout = new GridLayout(1, false);
		viewLayout.marginWidth = 0;
		viewLayout.marginHeight = 0;
		viewLayout.verticalSpacing = 0;
		view.setLayout(viewLayout);
		createExpressionComposite(view);
		final Composite intermediate = new Composite(view, SWT.NONE);
		intermediate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final GridLayout intermediateLayout = new GridLayout(2, false);
		intermediateLayout.marginWidth = 0;
		intermediateLayout.marginHeight = 0;
		intermediateLayout.verticalSpacing = 0;
		intermediate.setLayout(intermediateLayout);
		createMenus(intermediate);
		createViewer(intermediate);
		comparator = new AgentComparator();
		viewer.setComparator(comparator);
		intermediate.layout(true);
		parent = intermediate;
	}

	private void hideExpressionComposite() {
		if ( expressionComposite == null ) { return; }
		expressionComposite.setVisible(false);
		((GridData) expressionComposite.getLayoutData()).exclude = true;
		expressionComposite.getParent().layout();
	}

	private void showExpressionComposite() {
		if ( expressionComposite == null ) { return; }
		expressionComposite.setVisible(true);
		((GridData) expressionComposite.getLayoutData()).exclude = false;
		expressionComposite.getParent().layout();
	}

	private void createViewer(final Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns();
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setFont(SwtGui.getSmallFont());
		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(final DoubleClickEvent event) {
				final IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
				final Object o = s.getFirstElement();
				if ( o instanceof IAgent ) {
					GuiUtils.setHighlightedAgent((IAgent) o);
				}
			}
		});

		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(false);
		menuMgr.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(final IMenuManager manager) {
				IAgent agent = null;
				final IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
				final Object o = s.getFirstElement();
				if ( o instanceof IAgent ) {
					agent = (IAgent) o;
				}
				if ( agent != null ) {
					manager.removeAll();
					manager.update(true);
					AgentsMenu.createMenuForAgent(viewer.getControl().getMenu(), agent, false);
				}
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		// Layout the viewer
		final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 1;
		viewer.getControl().setLayoutData(gridData);
	}

	private void recreateViewer() {
		final Table table = viewer.getTable();
		table.dispose();
		createViewer(parent);
		// parent.pack(true);
		parent.layout(true);
	}

	private void createColumns() {
		final List<String> selection = new GamaList(attributesMenu.getSelection());
		selection.remove(IKeyword.NAME);
		selection.add(0, IKeyword.NAME);
		for ( final String title : selection ) {
			final TableViewerColumn col = createTableViewerColumn(title, 100, 0);
			col.setLabelProvider(new ColumnLabelProvider() {

				@Override
				public String getText(final Object element) {
					final IAgent agent = (IAgent) element;
					if ( agent.dead() && !title.equals(IKeyword.NAME) ) { return "N/A"; }
					return Cast.toGaml(scope.getAgentVarValue(agent, title));
				}
			});
		}

	}

	private SelectionAdapter getSelectionAdapter(final TableColumn column, final String name) {
		final SelectionAdapter columnSortAdapter = new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				comparator.setColumn(name);
				final int dir = comparator.getDirection();
				viewer.getTable().setSortDirection(dir);
				viewer.getTable().setSortColumn(column);
				viewer.refresh();
			}
		};
		return columnSortAdapter;
	}

	private TableViewerColumn createTableViewerColumn(final String title, final int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		column.addSelectionListener(getSelectionAdapter(column, title));
		return viewerColumn;
	}

	public TableViewer getViewer() {
		return viewer;
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	protected Integer[] getToolbarActionsId() {
		// TODO Need to be usable (not the case now)
		return new Integer[] { PAUSE, REFRESH };
	}

	public class AgentComparator extends ViewerComparator implements Comparator {

		private String attribute = null;
		private int direction = SWT.UP;
		private final NaturalOrderComparator stringComparator = new NaturalOrderComparator();

		public int getDirection() {
			return direction;
		}

		public void setColumn(final String column) {
			if ( column.equals(attribute) ) {
				// Same column as last sort; toggle the direction
				direction = direction == SWT.UP ? SWT.DOWN : SWT.UP;
			} else {
				// New column; do an ascending sort
				attribute = column;
				direction = SWT.UP;
			}
		}

		@Override
		public int compare(final Viewer viewer, final Object e1, final Object e2) {
			return compare(e1, e2);
		}

		@Override
		public int compare(final Object e1, final Object e2) {
			final IAgent p1 = (IAgent) e1;
			final IAgent p2 = (IAgent) e2;
			int rc = 0;
			if ( attribute == null ) {
				rc = p1.compareTo(p2);
			} else {
				try {
					final Object v1 = scope.getAgentVarValue(p1, attribute);
					if ( v1 == null ) {
						rc = -1;
					} else {
						final Object v2 = scope.getAgentVarValue(p2, attribute);
						if ( v2 == null ) {
							rc = 1;
						} else {
							final IVariable v = getOutput().getSpecies().getVar(attribute);
							final int id = v.getType().id();
							switch (id) {
								case IType.INT:
									rc = ((Integer) v1).compareTo((Integer) v2);
									break;
								case IType.FLOAT:
									rc = ((Double) v1).compareTo((Double) v2);
									break;
								case IType.STRING:
									rc = stringComparator.compare(v1, v2);
									break;
								default:
									rc = Cast.asFloat(scope, v1).compareTo(Cast.asFloat(scope, v2));
							}
						}
					}
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
			}

			// If descending order, flip the direction
			if ( direction == SWT.DOWN ) {
				rc = -rc;
			}
			return rc;
		}

	}

	public class NaturalOrderComparator implements Comparator {

		int compareRight(final String a, final String b) {
			int bias = 0;
			int ia = 0;
			int ib = 0;
			for ( ;; ia++, ib++ ) {
				final char ca = charAt(a, ia);
				final char cb = charAt(b, ib);

				if ( !Character.isDigit(ca) && !Character.isDigit(cb) ) {
					return bias;
				} else if ( !Character.isDigit(ca) ) {
					return -1;
				} else if ( !Character.isDigit(cb) ) {
					return +1;
				} else if ( ca < cb ) {
					if ( bias == 0 ) {
						bias = -1;
					}
				} else if ( ca > cb ) {
					if ( bias == 0 ) {
						bias = +1;
					}
				} else if ( ca == 0 && cb == 0 ) { return bias; }
			}
		}

		@Override
		public int compare(final Object o1, final Object o2) {
			final String a = o1.toString();
			final String b = o2.toString();

			int ia = 0, ib = 0;
			int nza = 0, nzb = 0;
			char ca, cb;
			int result;

			while (true) {
				// only count the number of zeroes leading the last number compared
				nza = nzb = 0;

				ca = charAt(a, ia);
				cb = charAt(b, ib);

				// skip over leading spaces or zeros
				while (Character.isSpaceChar(ca) || ca == '0') {
					if ( ca == '0' ) {
						nza++;
					} else {
						// only count consecutive zeroes
						nza = 0;
					}

					ca = charAt(a, ++ia);
				}

				while (Character.isSpaceChar(cb) || cb == '0') {
					if ( cb == '0' ) {
						nzb++;
					} else {
						// only count consecutive zeroes
						nzb = 0;
					}

					cb = charAt(b, ++ib);
				}

				// process run of digits
				if ( Character.isDigit(ca) && Character.isDigit(cb) ) {
					if ( (result = compareRight(a.substring(ia), b.substring(ib))) != 0 ) { return result; }
				}

				if ( ca == 0 && cb == 0 ) {
					// The strings compare the same. Perhaps the caller
					// will want to call strcmp to break the tie.
					return nza - nzb;
				}

				if ( ca < cb ) {
					return -1;
				} else if ( ca > cb ) { return +1; }

				++ia;
				++ib;
			}

		}

		char charAt(final String s, final int i) {
			if ( i >= s.length() ) { return 0; }
			return s.charAt(i);
		}
	}

	@Override
	public void dispose() {
		GAMA.releaseScope(scope);
		super.dispose();
	}

}
