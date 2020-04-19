/*******************************************************************************************************
 *
 * gaml.compilation.AbstractGamlAdditions.java, in plugin gama.core, is part of the source code of the GAMA modeling and
 * simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compilation;

import static gama.common.interfaces.IKeyword.OF;
import static gama.common.interfaces.IKeyword.SPECIES;
import static gama.common.interfaces.IKeyword._DOT;
import static gaml.expressions.IExpressionCompiler.OPERATORS;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import gama.common.interfaces.IKeyword;
import gama.common.interfaces.ISkill;
import gama.common.interfaces.experiment.IExperimentAgentCreator;
import gama.common.interfaces.experiment.IExperimentAgentCreator.ExperimentAgentDescription;
import gama.common.util.JavaUtils;
import gama.processor.annotations.GamlAnnotations.doc;
import gama.processor.annotations.GamlAnnotations.vars;
import gama.processor.annotations.ISymbolKind;
import gama.processor.annotations.ITypeProvider;
import gama.runtime.scope.IScope;
import gama.util.file.IGamaFile;
import gama.util.map.GamaMapFactory;
import gama.util.map.IMap;
import gaml.compilation.annotations.serializer;
import gaml.compilation.annotations.validator;
import gaml.compilation.factories.DescriptionFactory;
import gaml.compilation.factories.SymbolFactory;
import gaml.compilation.interfaces.GamaGetter;
import gaml.compilation.interfaces.IAgentConstructor;
import gaml.compilation.interfaces.IGamaHelper;
import gaml.compilation.interfaces.ISymbolConstructor;
import gaml.compilation.interfaces.IValidator;
import gaml.compilation.kernel.GamaMetaModel;
import gaml.compilation.kernel.GamaSkillRegistry;
import gaml.descriptions.IDescription;
import gaml.descriptions.IDescription.DescriptionVisitor;
import gaml.descriptions.PrimitiveDescription;
import gaml.descriptions.SkillDescription;
import gaml.descriptions.StatementDescription;
import gaml.descriptions.SymbolSerializer;
import gaml.descriptions.TypeDescription;
import gaml.descriptions.VariableDescription;
import gaml.expressions.IExpression;
import gaml.expressions.IExpressionCompiler;
import gaml.prototypes.FacetProto;
import gaml.prototypes.OperatorProto;
import gaml.prototypes.SymbolProto;
import gaml.types.GamaFileType;
import gaml.types.IType;
import gaml.types.ParametricFileType;
import gaml.types.Signature;
import gaml.types.Types;

/**
 *
 * The class AbstractGamlAdditions. Default base implementation for plugins' gaml additions.
 *
 * @author drogoul
 * @since 17 mai 2012
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public abstract class AbstractGamlAdditions {

	// public static class Children {
	//
	// private final Iterable<IDescription> children;
	//
	// public Children(final IDescription... descs) {
	// if (descs == null || descs.length == 0) {
	// children = Collections.emptyList();
	// } else {
	// children = Arrays.asList(descs);
	// }
	// }
	//
	// public Iterable<IDescription> getChildren() {
	// return children;
	// }
	// }

	protected int[] AI = new int[0];
	protected String[] AS = new String[0];
	protected boolean F = false;
	protected boolean T = true;
	protected String PRIM = IKeyword.PRIMITIVE;
	protected Class<?> O = Object.class;
	protected Class<?> B = Boolean.class;
	protected Class<?> I = Integer.class;
	protected Class<?> D = Double.class;
	protected Class<?> S = String.class;
	protected Class<?> GF = IGamaFile.class;
	protected Class<?> SC = IScope.class;
	protected Class<?> i = int.class;
	protected Class<?> d = double.class;
	protected Class<?> b = boolean.class;

	public static IDescription[] D(final IDescription... input) {
		return input;
	}

	public static final Set<String> CONSTANTS = new HashSet();
	final static Multimap<Class, IDescription> ADDITIONS = HashMultimap.create();
	private static Function<Class, Collection<IDescription>> INTO_DESCRIPTIONS = input -> ADDITIONS.get(input);
	private final static Multimap<Class, OperatorProto> FIELDS = HashMultimap.create();
	public final static Multimap<Integer, String> VARTYPE2KEYWORDS = HashMultimap.create();
	public static String CURRENT_PLUGIN_NAME = "gama.core.base";
	// public final static Map<String, IGamaPopulationsLinker> POPULATIONS_LINKERS =
	// new THashMap<>();
	public final static Map<String, String> TEMPORARY_BUILT_IN_VARS_DOCUMENTATION = new HashMap<>();

	public abstract void initialize() throws SecurityException, NoSuchMethodException;

	protected static String[] S(final String... strings) {
		return strings;
	}

	protected static int[] I(final int... integers) {
		return integers;
	}

	protected static FacetProto[] P(final FacetProto... protos) {
		return protos;
	}

	protected static Class[] C(final Class... classes) {
		return classes;
	}

	protected static IType<?> T(final Class<?> c) {
		return Types.get(c);
	}

	protected static String Ti(final Class c) {
		return String.valueOf(Types.get(c).id());
	}

	protected static String Ts(final Class c) {
		return Types.get(c).toString();
	}

	protected static IType T(final String c) {
		return Types.get(c);
	}

	protected static IType T(final int c) {
		return Types.get(c);
	}

	public void _experiment(final String string, final IExperimentAgentCreator d) {
		CONSTANTS.add(string);
		final ExperimentAgentDescription ed = new ExperimentAgentDescription(d, string, CURRENT_PLUGIN_NAME);
		GamaMetaModel.INSTANCE.addExperimentAgentCreator(string, ed);
	}

	public void _species(final String name, final Class clazz, final IAgentConstructor helper, final String... skills) {
		GamaMetaModel.INSTANCE.addSpecies(name, clazz, helper, skills);
		// DescriptionFactory.addSpeciesNameAsType(name);
	}

	protected void _type(final String keyword, final IType typeInstance, final int id, final int varKind,
			final Class... wraps) {
		initType(keyword, typeInstance, id, varKind, wraps);
	}

	protected void _file(final String string, final Class clazz, final GamaGetter.Unary<IGamaFile<?, ?>> helper,
			final int innerType, final int keyType, final int contentType, final String[] s) {
		// helper.setSkillClass(clazz);
		GamaFileType.addFileTypeDefinition(string, Types.get(innerType), Types.get(keyType), Types.get(contentType),
				clazz, helper, s, CURRENT_PLUGIN_NAME);
		VARTYPE2KEYWORDS.put(ISymbolKind.Variable.CONTAINER, string + "_file");
	}

	protected void _skill(final String name, final Class clazz, final String... species) {
		GamaSkillRegistry.INSTANCE.register(name, clazz, CURRENT_PLUGIN_NAME, ADDITIONS.get(clazz), species);
	}

	protected void _factories(final SymbolFactory... factories) {
		for (final SymbolFactory f : factories) {
			DescriptionFactory.addFactory(f);
		}
	}

	protected void _symbol(final String[] names, final Class c, final int sKind, final boolean remote,
			final boolean args, final boolean scope, final boolean sequence, final boolean unique,
			final boolean name_unique, final String[] contextKeywords, final int[] contextKinds, final FacetProto[] fmd,
			final String omissible, final ISymbolConstructor sc) {

		IValidator validator2 = null;
		SymbolSerializer serializer2 = null;
		final validator v = (validator) c.getAnnotation(validator.class);
		final serializer s = (serializer) c.getAnnotation(serializer.class);
		try {
			if (v != null) {
				validator2 = v.value().getConstructor().newInstance();
			}
			if (s != null) {
				serializer2 = s.value().getConstructor().newInstance();
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {}

		final Collection<String> keywords;
		if (ISymbolKind.Variable.KINDS.contains(sKind)) {
			keywords = VARTYPE2KEYWORDS.get(sKind);
			keywords.remove(SPECIES);
		} else {
			keywords = Arrays.asList(names);
		}
		if (fmd != null) {
			for (final FacetProto f : fmd) {
				f.buildDoc(c);
			}
		}

		final SymbolProto md = new SymbolProto(c, sequence, args, sKind, !scope, fmd, omissible, contextKeywords,
				contextKinds, remote, unique, name_unique, sc, validator2, serializer2,
				names == null || names.length == 0 ? "variable declaration" : names[0], CURRENT_PLUGIN_NAME);
		DescriptionFactory.addProto(md, keywords);
	}

	public void _iterator(final String[] keywords, final Method method, final Class[] classes,
			final int[] expectedContentTypes, final Class ret, final boolean c, final int t, final int content,
			final int index, final int contentContentType, final GamaGetter.Binary helper) {
		IExpressionCompiler.ITERATORS.addAll(Arrays.asList(keywords));
		_binary(keywords, method, classes, expectedContentTypes, ret, c, t, content, index, contentContentType, helper);
	}

	public void _binary(final String[] keywords, final AccessibleObject method, final Class[] classes,
			final int[] expectedContentTypes, final Object returnClassOrType, final boolean c, final int t,
			final int content, final int index, final int contentContentType, final GamaGetter.Binary helper) {
		final Signature signature = new Signature(classes);
		final String plugin = CURRENT_PLUGIN_NAME;
		for (final String keyword : keywords) {
			final String kw = keyword;
			if (!OPERATORS.containsKey(kw)) {
				OPERATORS.put(kw, GamaMapFactory.createUnordered(4));
			}
			final Map<Signature, OperatorProto> map = OPERATORS.get(kw);
			if (!map.containsKey(signature)) {
				OperatorProto proto;
				IType rt;
				if (returnClassOrType instanceof Class) {
					rt = Types.get((Class) returnClassOrType);
				} else {
					rt = (IType) returnClassOrType;
				}
				// binary
				if ((kw.equals(OF) || kw.equals(_DOT)) && signature.get(0).isAgentType()) {
					proto = new OperatorProto(kw, method, helper, c, true, rt, signature,
							IExpression.class.equals(classes[1]), t, content, index, contentContentType,
							expectedContentTypes, plugin);
				} else {
					proto = new OperatorProto(kw, method, helper, c, false, rt, signature,
							IExpression.class.equals(classes[1]), t, content, index, contentContentType,
							expectedContentTypes, plugin);
				}

				map.put(signature, proto);
			}
		}

	}

	public void _operator(final String[] keywords, final AccessibleObject method, final Class[] classes,
			final int[] expectedContentTypes, final Object returnClassOrType, final boolean c, final int t,
			final int content, final int index, final int contentContentType, final GamaGetter.NAry helper) {
		final Signature signature = new Signature(classes);
		final String plugin = CURRENT_PLUGIN_NAME;
		for (final String keyword : keywords) {
			final String kw = keyword;
			if (!OPERATORS.containsKey(kw)) {
				OPERATORS.put(kw, GamaMapFactory.createUnordered(4));
			}
			final Map<Signature, OperatorProto> map = OPERATORS.get(kw);
			if (!map.containsKey(signature)) {
				OperatorProto proto;
				IType rt;
				if (returnClassOrType instanceof Class) {
					rt = Types.get((Class) returnClassOrType);
				} else {
					rt = (IType) returnClassOrType;
				}
				if (classes.length == 1) { // unary
					proto = new OperatorProto(kw, method, helper, c, false, rt, signature,
							IExpression.class.equals(classes[0]), t, content, index, contentContentType,
							expectedContentTypes, plugin);
				} else if (classes.length == 2) { // binary
					if ((kw.equals(OF) || kw.equals(_DOT)) && signature.get(0).isAgentType()) {
						proto = new OperatorProto(kw, method, helper, c, true, rt, signature,
								IExpression.class.equals(classes[1]), t, content, index, contentContentType,
								expectedContentTypes, plugin);
					} else {
						proto = new OperatorProto(kw, method, helper, c, false, rt, signature,
								IExpression.class.equals(classes[1]), t, content, index, contentContentType,
								expectedContentTypes, plugin);
					}
				} else {
					proto = new OperatorProto(kw, method, helper, c, false, rt, signature,
							IExpression.class.equals(classes[classes.length - 1]), t, content, index,
							contentContentType, expectedContentTypes, plugin);
				}
				map.put(signature, proto);
			}
		}

	}

	public void _unary(final String[] keywords, final AccessibleObject method, final Class[] classes,
			final int[] expectedContentTypes, final Object returnClassOrType, final boolean c, final int t,
			final int content, final int index, final int contentContentType, final GamaGetter.Unary helper) {
		final Signature signature = new Signature(classes);
		final String plugin = CURRENT_PLUGIN_NAME;
		for (final String keyword : keywords) {
			final String kw = keyword;
			if (!OPERATORS.containsKey(kw)) {
				OPERATORS.put(kw, GamaMapFactory.createUnordered(4));
			}
			final Map<Signature, OperatorProto> map = OPERATORS.get(kw);
			if (!map.containsKey(signature)) {
				OperatorProto proto;
				IType rt;
				if (returnClassOrType instanceof Class) {
					rt = Types.get((Class) returnClassOrType);
				} else {
					rt = (IType) returnClassOrType;
				}
				proto = new OperatorProto(kw, method, helper, c, false, rt, signature,
						IExpression.class.equals(classes[0]), t, content, index, contentContentType,
						expectedContentTypes, plugin);
				map.put(signature, proto);
			}
		}

	}

	// For files
	public void _operator(final String[] keywords, final AccessibleObject method, final Class[] classes,
			final int[] expectedContentTypes, final Class ret, final boolean c, final String typeAlias,
			final GamaGetter.NAry helper) {
		final ParametricFileType fileType = GamaFileType.getTypeFromAlias(typeAlias);
		int indexOfIType = -1;
		for (int i = 0; i < classes.length; i++) {
			final Class cl = classes[i];
			if (IType.class.isAssignableFrom(cl)) {
				indexOfIType = i;
			}
		}
		final int content =
				indexOfIType == -1 ? ITypeProvider.NONE : ITypeProvider.DENOTED_TYPE_AT_INDEX + indexOfIType + 1;
		this._operator(keywords, method, classes, expectedContentTypes, fileType, c, ITypeProvider.NONE, content,
				ITypeProvider.NONE, ITypeProvider.NONE, helper);
	}

	public void _binary(final String[] keywords, final AccessibleObject method, final Class[] classes,
			final int[] expectedContentTypes, final Class ret, final boolean c, final String typeAlias,
			final GamaGetter.Binary helper) {
		final ParametricFileType fileType = GamaFileType.getTypeFromAlias(typeAlias);
		int indexOfIType = -1;
		for (int i = 0; i < classes.length; i++) {
			final Class cl = classes[i];
			if (IType.class.isAssignableFrom(cl)) {
				indexOfIType = i;
			}
		}
		final int content =
				indexOfIType == -1 ? ITypeProvider.NONE : ITypeProvider.DENOTED_TYPE_AT_INDEX + indexOfIType + 1;
		this._binary(keywords, method, classes, expectedContentTypes, fileType, c, ITypeProvider.NONE, content,
				ITypeProvider.NONE, ITypeProvider.NONE, helper);
	}

	private void add(final Class clazz, final IDescription desc) {
		ADDITIONS.put(clazz, desc);
	}

	protected void _var(final Class clazz, final IDescription desc, final IGamaHelper get, final IGamaHelper init,
			final IGamaHelper set) {
		add(clazz, desc);
		((VariableDescription) desc).addHelpers(clazz, get, init, set);
		TEMPORARY_BUILT_IN_VARS_DOCUMENTATION.putIfAbsent(desc.getName(), getVarDoc(desc.getName(), clazz));
		((VariableDescription) desc).setDefiningPlugin(CURRENT_PLUGIN_NAME);
	}

	private String getVarDoc(final String name, final Class<?> clazz) {
		final vars vars = clazz.getAnnotationsByType(vars.class)[0];
		for (final gama.processor.annotations.GamlAnnotations.variable v : vars.value()) {
			if (v.name().equals(name)) {
				final doc[] docs = v.doc();
				// final String d = "";
				if (docs.length > 0)
					// documentation of fields is not used
					return docs[0].value();
			}
		}
		return "";
	}

	protected FacetProto _facet(final String name, final int[] types, final int ct, final int kt, final String[] values,
			final boolean optional, final boolean internal, final boolean isRemote) {
		return new FacetProto(name, types, ct, kt, values, optional, internal, isRemote);
	}

	protected OperatorProto _proto(final String name, final GamaGetter.Unary helper, final int returnType,
			final Class signature, final int typeProvider, final int contentTypeProvider, final int keyTypeProvider) {
		return new OperatorProto(name, null, helper, false, true, returnType, signature, false, typeProvider,
				contentTypeProvider, keyTypeProvider, AI);
	}

	protected void _field(final Class clazz, final OperatorProto getter) {
		FIELDS.put(clazz, getter);
	}

	protected IDescription desc(final String keyword, final IDescription[] children, final String... facets) {
		return DescriptionFactory.create(keyword, null, Arrays.asList(children), facets);
	}

	protected IDescription desc(final String keyword, final String... facets) {
		return DescriptionFactory.create(keyword, facets);
	}

	/**
	 * Creates a VariableDescription
	 *
	 * @param keyword
	 * @param facets
	 * @return
	 */
	protected IDescription desc(final int keyword, final String... facets) {
		final IType t = Types.get(keyword);
		if (t == null)
			throw new RuntimeException("Types not defined");
		return desc(t.toString(), facets);
	}

	protected void _action(final IGamaHelper e, final IDescription desc, final Method method) {
		final Class clazz = method.getDeclaringClass();
		((PrimitiveDescription) desc).setHelper(new GamaHelper(clazz, e), method);
		((PrimitiveDescription) desc).setDefiningPlugin(CURRENT_PLUGIN_NAME);
		add(clazz, desc);
	}

	public static void initType(final String keyword, final IType<?> typeInstance, final int id, final int varKind,
			final Class... wraps) {
		final IType<?> type = Types.builtInTypes.initType(keyword, typeInstance, id, varKind, wraps[0]);
		for (final Class cc : wraps) {
			Types.CLASSES_TYPES_CORRESPONDANCE.put(cc, type.getName());
		}
		type.setDefiningPlugin(CURRENT_PLUGIN_NAME);
		Types.cache(id, typeInstance);
		VARTYPE2KEYWORDS.put(varKind, keyword);
	}

	public static Collection<IDescription> getAdditions(final Class clazz) {
		return ADDITIONS.get(clazz);
	}

	public static Map<String, OperatorProto> getAllFields(final Class clazz) {
		final List<Class> classes =
				JavaUtils.collectImplementationClasses(clazz, Collections.EMPTY_SET, FIELDS.keySet());
		final Map<String, OperatorProto> fieldsMap = GamaMapFactory.create();
		for (final Class c : classes) {
			for (final OperatorProto desc : FIELDS.get(c)) {
				fieldsMap.put(desc.getName(), desc);
			}
		}
		return fieldsMap;
	}

	public static Iterable<IDescription> getAllChildrenOf(final Class base,
			final Iterable<Class<? extends ISkill>> skills) {
		final List<Class> classes = JavaUtils.collectImplementationClasses(base, skills, ADDITIONS.keySet());
		return Iterables.concat(Iterables.transform(classes, INTO_DESCRIPTIONS));
	}

	public static Collection<OperatorProto> getAllFields() {
		return FIELDS.values();
	}

	public static Collection<IDescription> getAllVars() {
		final HashSet<IDescription> result = new HashSet<>();

		final DescriptionVisitor<IDescription> varVisitor = desc -> {
			result.add(desc);
			return true;
		};

		final DescriptionVisitor<IDescription> actionVisitor = desc -> {
			Iterables.addAll(result, ((StatementDescription) desc).getFormalArgs());
			return true;
		};

		for (final TypeDescription desc : Types.getBuiltInSpecies()) {
			desc.visitOwnAttributes(varVisitor);
			desc.visitOwnActions(actionVisitor);

		}
		GamaSkillRegistry.INSTANCE.visitSkills(desc -> {
			((TypeDescription) desc).visitOwnAttributes(varVisitor);
			((TypeDescription) desc).visitOwnActions(actionVisitor);
			return true;
		});

		return result;
	}

	public static Collection<SymbolProto> getStatementsForSkill(final String s) {
		final Set<SymbolProto> result = new LinkedHashSet();
		for (final String p : DescriptionFactory.getStatementProtoNames()) {
			final SymbolProto proto = DescriptionFactory.getStatementProto(p, s);
			if (proto != null && proto.shouldBeDefinedIn(s)) {
				result.add(proto);
			}
		}
		return result;
	}

	public static Collection<IDescription> getAllActions() {
		final IMap<String, IDescription> result = GamaMapFactory.createUnordered();

		final DescriptionVisitor<IDescription> visitor = desc -> {
			result.putIfAbsent(desc.getName(), desc);
			return true;
		};

		for (final TypeDescription s : Types.getBuiltInSpecies()) {
			s.visitOwnActions(visitor);
		}
		GamaSkillRegistry.INSTANCE.visitSkills(desc -> {
			((SkillDescription) desc).visitOwnActions(visitor);
			return true;
		});
		return result.values();
	}

	public static void _constants(final String[]... strings) {
		for (final String[] s : strings) {
			for (final String s2 : s) {
				CONSTANTS.add(s2);
			}
		}
	}

	/**
	 * @param name
	 * @return
	 */
	public static boolean isUnaryOperator(final String name) {
		if (!OPERATORS.containsKey(name))
			return false;
		final Map<Signature, OperatorProto> map = OPERATORS.get(name);
		for (final Signature s : map.keySet()) {
			if (s.isUnary())
				return true;
		}
		return false;
	}

}
