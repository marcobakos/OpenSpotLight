/**
 * OpenSpotLight - Open Source IT Governance Platform
 *
 * Copyright (c) 2009, CARAVELATECH CONSULTORIA E TECNOLOGIA EM INFORMATICA LTDA
 * or third-party contributors as indicated by the @author tags or express
 * copyright attribution statements applied by the authors.  All third-party
 * contributions are distributed under license by CARAVELATECH CONSULTORIA E
 * TECNOLOGIA EM INFORMATICA LTDA.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 *
 ***********************************************************************
 * OpenSpotLight - Plataforma de Governança de TI de Código Aberto
 *
 * Direitos Autorais Reservados (c) 2009, CARAVELATECH CONSULTORIA E TECNOLOGIA
 * EM INFORMATICA LTDA ou como contribuidores terceiros indicados pela etiqueta
 * @author ou por expressa atribuição de direito autoral declarada e atribuída pelo autor.
 * Todas as contribuições de terceiros estão distribuídas sob licença da
 * CARAVELATECH CONSULTORIA E TECNOLOGIA EM INFORMATICA LTDA.
 *
 * Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo sob os
 * termos da Licença Pública Geral Menor do GNU conforme publicada pela Free Software
 * Foundation.
 *
 * Este programa é distribuído na expectativa de que seja útil, porém, SEM NENHUMA
 * GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU ADEQUAÇÃO A UMA
 * FINALIDADE ESPECÍFICA. Consulte a Licença Pública Geral Menor do GNU para mais detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral Menor do GNU junto com este
 * programa; se não, escreva para:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
/**
 * 
 */
package org.openspotlight.bundle.language.java.resolver;

import org.openspotlight.bundle.common.metamodel.link.AbstractTypeBind;
import org.openspotlight.bundle.language.java.metamodel.link.*;
import org.openspotlight.bundle.language.java.metamodel.node.*;
import org.openspotlight.common.util.InvocationCacheFactory;
import org.openspotlight.graph.SLContext;
import org.openspotlight.graph.GraphReaderorg.openspotlight.graph.SLLink;
import org.openspotlight.graph.Node;
import org.openspotlight.graph.query.SLQueryApi;
import org.openspotlight.graph.query.SLQueryResult;

import java.util.*;

import static java.lang.Class.forName;
import static java.text.MessageFormat.format;
import static java.util.Collections.reverse;
import static org.openspotlight.bundle.language.java.JavaConstants.ABSTRACT_CONTEXT;
import static org.openspotlight.common.util.Exceptions.*;

/**
 * The Class JavaTypeResolver is a {@link TypeResolver} used for Java Programming Language.
 * 
 * @author Luiz Fernando Teston - feu.teston@caravelatech.com
 */
public class JavaTypeResolver extends AbstractTypeResolver<JavaType> {

    /**
     * The Class {@link ByNameTypeFinder} executes a simple by name query
     */
    private class ByNameTypeFinder extends DolarReplacerQueryExecutor {

        /**
         * The Constructor.
         * 
         * @param s the s
         */
        public ByNameTypeFinder(
                                 final String s ) {
            super(s);
        }

        /**
         * Execute with this string.
         * 
         * @param s the s
         * @return the SL query result
         * @throws Exception the exception
         * @{inheritDoc
         */
        @Override
        protected SLQueryResult executeWithThisString( final String s ) throws Exception {
            final SLQueryApi query = getSession().createQueryApi();
            query.select().type(JavaType.class.getName()).subTypes().selectEnd().where().type(JavaType.class.getName()).subTypes().each().property(
                                                                                                                                                   "qualifiedName").equalsTo().value(
                                                                                                                                                                                     s).typeEnd().whereEnd();
            final SLQueryResult result = query.execute();
            return result;

        }

    }

    /**
     * This class executes a query replacing the dolar character '$' by dot to fill the way that Java Programming Language works.
     */
    private abstract class DolarReplacerQueryExecutor {

        /** The actual string. */
        private final String actualString;

        /**
         * Instantiates a new dolar replacer query executor.
         * 
         * @param s the s
         */
        public DolarReplacerQueryExecutor(
                                           final String s ) {
            actualString = s.replaceAll("[$]", ".");
        }

        /**
         * Execute with this string.
         * 
         * @param s the s
         * @return the sL query result
         * @throws Exception the exception
         */
        protected abstract SLQueryResult executeWithThisString( String s ) throws Exception;

        /**
         * Gets the new string.
         * 
         * @return the new string
         */
        protected String getNewString() {
            return actualString;
        }

        /**
         * Gets the preffered type.
         * 
         * @param result the result
         * @return the preffered type
         * @throws Exception the exception
         */
        private Node getPrefferedType( final SLQueryResult result ) throws Exception {
            final Map<String, List<Node>> resultMap = new HashMap<String, List<Node>>();
            for (final SLContext ctx : getOrderedActiveContexts()) {
                resultMap.put(ctx.getID(), new ArrayList<Node>());
            }
            for (final Node n : result.getNodes()) {
                final List<Node> resultList = resultMap.get(n.getContext().getID());
                if (resultList != null) {
                    resultList.add(n);
                    if (resultList.size() > 1) {
                        logAndThrow(new IllegalStateException(
                                                              format(
                                                                     "Two nodes of the same type and name on the same context: node {0} (parent {2}) inside context {1}",
                                                                     n.getName(), n.getContext().getID(), n.getParent().getName())));
                    }
                }
            }
            for (final SLContext ctx : getOrderedActiveContexts()) {
                final List<Node> resultList = resultMap.get(ctx.getID());
                if (resultList.size() > 0) {
                    return resultList.get(0);
                }
            }
            return null;
        }

        /**
         * Gets the type by all possible names.
         * 
         * @return the type by all possible names
         * @throws Exception the exception
         */
        public Node getTypeByAllPossibleNames() throws Exception {
            final Node result = getPrefferedType(executeWithThisString(actualString));
            return result;
        }

    }

    /**
     * The Class ComplexTypeFinderQueryExecutor .
     */
    private class TypesFromTheSamePackageQueryExecutor extends DolarReplacerQueryExecutor {

        /** The all types from same packages. */
        private final Collection<Node> allTypesFromSamePackages;

        /**
         * Instantiates a new complex type finder query executor.
         * 
         * @param typeToSolve the type to solve
         * @param allTypesFromSamePackages the all types from same packages
         */
        public TypesFromTheSamePackageQueryExecutor(
                                                     final String typeToSolve, final Collection<Node> allTypesFromSamePackages ) {
            super(typeToSolve);
            this.allTypesFromSamePackages = allTypesFromSamePackages;
        }

        /**
         * Execute with this string.
         * 
         * @param s the s
         * @return the SL query result
         * @throws Exception the exception
         * @{inheritDoc
         */
        @Override
        public SLQueryResult executeWithThisString( final String s ) throws Exception {
            final SLQueryApi justTheTargetTypeQuery = getSession().createQueryApi();
            justTheTargetTypeQuery.select().type(JavaType.class.getName()).subTypes().selectEnd().where().type(
                                                                                                               JavaType.class.getName()).subTypes().each().property(
                                                                                                                                                                    "simpleName").equalsTo().value(
                                                                                                                                                                                                   s).typeEnd().whereEnd();
            final SLQueryResult result = justTheTargetTypeQuery.execute(allTypesFromSamePackages);
            return result;
        }

    }

    /** The Constant implementationInheritanceLinks. */
    private static final Set<Class<? extends SLLink>> implementationInheritanceLinks     = new LinkedHashSet<Class<? extends SLLink>>();

    /** The Constant interfaceInheritanceLinks. */
    private static final Set<Class<? extends SLLink>> interfaceInheritanceLinks          = new LinkedHashSet<Class<? extends SLLink>>();

    /** The Constant primitiveHierarchyLinks. */
    private static final Set<Class<? extends SLLink>> primitiveHierarchyLinks            = new LinkedHashSet<Class<? extends SLLink>>();

    /** The all kinds of inheritance links. */
    private final Set<Class<? extends SLLink>>        allKindsOfInheritanceLinks         = new LinkedHashSet<Class<? extends SLLink>>();

    /** The all kinds of inheritance links reversed. */
    private final Set<Class<? extends SLLink>>        allKindsOfInheritanceLinksReversed = new LinkedHashSet<Class<? extends SLLink>>();

    /** The Constant primitiveTypes. */
    private static final Set<Class<?>>                primitiveTypes                     = new LinkedHashSet<Class<?>>();

    /** The Constant concreteTypes. */
    private static final Set<Class<?>>                concreteTypes                      = new LinkedHashSet<Class<?>>();

    /*
     * Here the link behavior is defined. This static block is so much important
     * because it can define the ordering behavior on the queries. It defines
     * also the link priority.
     */
    static {
        JavaTypeResolver.implementationInheritanceLinks.add(Extends.class);
        JavaTypeResolver.implementationInheritanceLinks.add(ImplicitExtends.class);
        JavaTypeResolver.implementationInheritanceLinks.add(AbstractTypeBind.class);
        JavaTypeResolver.interfaceInheritanceLinks.add(AbstractTypeBind.class);
        JavaTypeResolver.interfaceInheritanceLinks.add(Implements.class);
        JavaTypeResolver.interfaceInheritanceLinks.add(InterfaceExtends.class);

        JavaTypeResolver.primitiveTypes.add(JavaTypePrimitive.class);
        JavaTypeResolver.primitiveHierarchyLinks.add(Extends.class);// FIXME
        concreteTypes.add(JavaTypeClass.class);
        concreteTypes.add(JavaTypeEnum.class);

    }

    /**
     * Creates the new cached java type finder.
     * 
     * @param abstractContext the abstract context
     * @param orderedActiveContexts the ordered active contexts
     * @param enableBoxing the enable boxing
     * @param session the session
     * @return the java type resolver
     */
    @SuppressWarnings( {"cast", "boxing"} )
    public static JavaTypeResolver createNewCached( final SLContext abstractContext,
                                                    final List<SLContext> orderedActiveContexts,
                                                    final boolean enableBoxing,
                                                    final GraphReadGraphReader   final JavaTypeResolver cached = (JavaTypeResolver)InvocationCacheFactory.createIntoCached(JavaTypeResolver.class,
                                                                                                  new Class<?>[] {
                                                                                                      SLContext.class,
                                                                                                      List.class, boolean.class,
                                                                                                      GraphReader.class}GraphReader                                                                                new Object[] {abstractContext,
                                                                                                      orderedActiveContexts,
                                                                                                      enableBoxing, session});
        return cached;
    }

    /**
     * Creates the new uncached and slow java type finder. Normaly its better to use the method
     * {@link #createNewCached(SLContext, List, boolean, org.openspotlight.graph.GraphReader)}. This method GraphReadersed on tests.
     * 
     * @param abstractContext the abstract context
     * @param orderedActiveContexts the ordered active contexts
     * @param enableBoxing the enable boxing
     * @param session the session
     * @return the java type resolver
     */
    @Deprecated
    public static TypeResolver<JavaType> createNewUncachedAndSlow( final SLContext abstractContext,
                                                                   final List<SLContext> orderedActiveContexts,
                                                                   final boolean enableBoxing,
                                                                   final GraphReader session ) {
        retuGraphReaderver(abstractContext, orderedActiveContexts, enableBoxing, session);
    }

    /**
     * Instantiates a new java type resolver. Normaly should be better to use the static factory method
     * {@link #createNewCached(SLContext, List, boolean, org.openspotlight.graph.GraphReader)}.
     * 
     * @param abstractGraphReader context
     * @param orderedActiveContexts the ordered active contexts
     * @param enableBoxing the enable boxing
     * @param session the session
     */
    protected JavaTypeResolver(
                                final SLContext abstractContext, final List<SLContext> orderedActiveContexts,
                                final boolean enableBoxing, final GraphReader session ) {
        super(implementationInGraphReaderrfaceInheritanceLinks, primitiveHierarchyLinks, abstractContext,
              orderedActiveContexts, primitiveTypes, concreteTypes, enableBoxing, session);

        allKindsOfInheritanceLinks.add(AbstractTypeBind.class);
        allKindsOfInheritanceLinks.add(Extends.class);
        allKindsOfInheritanceLinks.add(ImplicitExtends.class);
        allKindsOfInheritanceLinks.add(InterfaceExtends.class);
        allKindsOfInheritanceLinks.add(Implements.class);

        allKindsOfInheritanceLinks.add(ImplicitPrimitiveCast.class);
        if (enableBoxing) {
            allKindsOfInheritanceLinks.add(AutoboxedBy.class);
            allKindsOfInheritanceLinks.add(Autoboxes.class);
        }
        final List<Class<? extends SLLink>> reversed = new ArrayList<Class<? extends SLLink>>(allKindsOfInheritanceLinks);
        reverse(reversed);
        allKindsOfInheritanceLinksReversed.addAll(reversed);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #bestMatch(org.openspotlight.graph.Node,
     * org.openspotlight.graph.Node, org.openspotlight.graph.Node)
     */
    @Override
    public <T extends JavaType> BestTypeMatch bestMatch( final T reference,
                                                         final T t1,
                                                         final T t2 ) throws InternalJavaFinderError {
        try {
            if (reference.equals(t1)) {
                if (t1.equals(t2)) {
                    return BestTypeMatch.SAME;
                }
                return BestTypeMatch.T1;
            }
            if (reference.equals(t2)) {
                return BestTypeMatch.T2;
            }
            if (this.isPrimitiveType(reference)) {
                final boolean t1Primitive = this.isPrimitiveType(t1);
                final boolean t2Primitive = this.isPrimitiveType(t2);
                if (t1Primitive && !t2Primitive) {
                    return BestTypeMatch.T1;
                }
                if (t2Primitive && !t1Primitive) {
                    return BestTypeMatch.T2;
                }
                final Collection<ImplicitPrimitiveCast> linksT1 = getSession().getLink(ImplicitPrimitiveCast.class, t1,
                                                                                        reference);
                final Collection<ImplicitPrimitiveCast> linksT2 = getSession().getLink(ImplicitPrimitiveCast.class, t2,
                                                                                        reference);
                if (linksT1.size() != linksT2.size()) {
                    if (linksT1.size() > linksT2.size()) {
                        return BestTypeMatch.T1;
                    }
                    return BestTypeMatch.T2;
                }
                if (linksT1.size() > 0) {
                    final ImplicitPrimitiveCast linkT1 = linksT1.iterator().next();
                    final ImplicitPrimitiveCast linkT2 = linksT2.iterator().next();
                    if (linkT1.getDistance() < linkT2.getDistance()) {
                        return BestTypeMatch.T1;
                    } else if (linkT1.getDistance() > linkT2.getDistance()) {
                        return BestTypeMatch.T2;
                    }
                }
                return BestTypeMatch.SAME;
            }

            final boolean referenceTypeOfT1 = this.isTypeOf(reference, t1);
            final boolean referenceTypeOfT2 = this.isTypeOf(reference, t2);
            if (!(referenceTypeOfT1 && referenceTypeOfT2)) {
                if (referenceTypeOfT1) {
                    return BestTypeMatch.T1;
                }
                if (referenceTypeOfT2) {
                    return BestTypeMatch.T2;
                }
                return BestTypeMatch.SAME;
            }

            final int referenceParentsCount = this.countConcreteParents(reference,
                                                                        IncludedResult.DO_NOT_INCLUDE_ACTUAL_TYPE_ON_RESULT);
            final int t1ParentsCount = this.countConcreteParents(t1, IncludedResult.DO_NOT_INCLUDE_ACTUAL_TYPE_ON_RESULT);
            final int t2ParentsCount = this.countConcreteParents(t2, IncludedResult.DO_NOT_INCLUDE_ACTUAL_TYPE_ON_RESULT);

            final int x = referenceParentsCount - t1ParentsCount;
            final int y = referenceParentsCount - t2ParentsCount;

            if (y == x) {
                return BestTypeMatch.SAME;
            } else if (y > x) {
                return BestTypeMatch.T1;
            }
            return BestTypeMatch.T2;
        } catch (final Exception e) {
            throw logAndReturnNew(e, InternalJavaFinderError.class);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #countAllChildren(org.openspotlight.graph.Node,
     * org.openspotlight.bundle
     * .dap.language.java.resolver.TypeResolver.IncludedResult)
     */
    @Override
    public <T extends JavaType> int countAllChildren( final T activeType,
                                                      final IncludedResult includedResult ) throws InternalJavaFinderError {
        return this.getAllChildren(activeType, ResultOrder.ASC, includedResult).size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #countAllParents(org.openspotlight.graph.Node,
     * org.openspotlight.bundle.
     * dap.language.java.resolver.TypeResolver.IncludedResult)
     */
    @Override
    public <T extends JavaType> int countAllParents( final T activeType,
                                                     final IncludedResult includedResult ) throws InternalJavaFinderError {
        return this.getAllParents(activeType, ResultOrder.ASC, includedResult).size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #countConcreteChildren(org.openspotlight.graph.Node,
     * org.openspotlight.bundle
     * .dap.language.java.resolver.TypeResolver.IncludedResult)
     */
    @Override
    public <T extends JavaType> int countConcreteChildren( final T activeType,
                                                           final IncludedResult includedResult ) throws InternalJavaFinderError {
        return this.getConcreteChildren(activeType, ResultOrder.ASC, includedResult).size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #countConcreteParents(org.openspotlight.graph.Node,
     * org.openspotlight.bundle
     * .dap.language.java.resolver.TypeResolver.IncludedResult)
     */
    @Override
    public <T extends JavaType> int countConcreteParents( final T activeType,
                                                          final IncludedResult includedResult ) throws InternalJavaFinderError {
        return this.getConcreteParents(activeType, ResultOrder.ASC, includedResult).size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #countInterfaceChildren(org.openspotlight.graph.Node,
     * org.openspotlight.
     * bundle.dap.language.java.resolver.TypeResolver.IncludedResult)
     */
    @Override
    public <T extends JavaType> int countInterfaceChildren( final T activeType,
                                                            final IncludedResult includedResult ) throws InternalJavaFinderError {
        return this.getInterfaceChildren(activeType, ResultOrder.ASC, includedResult).size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #countInterfaceParents(org.openspotlight.graph.Node,
     * org.openspotlight.bundle
     * .dap.language.java.resolver.TypeResolver.IncludedResult)
     */
    @Override
    public <T extends JavaType> int countInterfaceParents( final T activeType,
                                                           final IncludedResult includedResult ) throws InternalJavaFinderError {
        return this.getInterfaceParents(activeType, ResultOrder.ASC, includedResult).size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #getAllChildren(org.openspotlight.graph.Node,
     * org.openspotlight.bundle.dap
     * .language.java.resolver.TypeResolver.ResultOrder,
     * org.openspotlight.bundle
     * .dap.language.java.resolver.TypeResolver.IncludedResult)
     */
    @Override
    public <T extends JavaType, A extends T> List<T> getAllChildren( final A activeType,
                                                                     final ResultOrder order,
                                                                     final IncludedResult includedResult )
        throws InternalJavaFinderError {
        return this.getAllChildrenByAllLinkTypes(activeType, order, JavaType.class, includedResult, Recursive.FULLY_RECURSIVE);
    }

    /**
     * Gets the all children by all link types.
     * 
     * @param activeType the active type
     * @param order the order
     * @param typeToFilter the type to filter
     * @param includedResult the included result
     * @param recursive the recursive
     * @return the all children by all link types
     * @throws InternalJavaFinderError the internal java finder error
     */
    protected <T extends JavaType, A extends JavaType> List<T> getAllChildrenByAllLinkTypes( final A activeType,
                                                                                             final ResultOrder order,
                                                                                             final Class<? extends JavaType> typeToFilter,
                                                                                             final IncludedResult includedResult,
                                                                                             final Recursive recursive )
        throws InternalJavaFinderError {
        try {

            final Set<Node> resultSet = new LinkedHashSet<Node>();
            resultSet.add(activeType);

            int lastSize = resultSet.size();
            int actualSize = -1;
            biggerLoop: while (lastSize != actualSize) {
                lastSize = resultSet.size();
                final Map<Class<? extends SLLink>, List<Node>> deltasFromEachType = new HashMap<Class<? extends SLLink>, List<Node>>();
                for (final Class<? extends SLLink> linkType : allKindsOfInheritanceLinksReversed) {
                    deltasFromEachType.put(linkType, new ArrayList<Node>());
                }
                for (final Class<? extends SLLink> linkType : allKindsOfInheritanceLinksReversed) {
                    final SLQueryApi inheritanceTreeQuery = getSession().createQueryApi();
                    inheritanceTreeQuery.select().type(JavaType.class.getName()).subTypes().comma().byLink(linkType.getName()).a().selectEnd();
                    final Collection<Node> resultFromQuery = inheritanceTreeQuery.execute(new ArrayList<Node>(resultSet)).getNodes();
                    for (final Node possibleDelta : resultFromQuery) {
                        if (!resultSet.contains(possibleDelta)) {
                            deltasFromEachType.get(linkType).add(possibleDelta);
                        }
                    }
                    switch (recursive) {
                        case FULLY_RECURSIVE:
                            resultSet.addAll(resultFromQuery);
                            break;
                        case ONLY_DIRECT_PARENTS:
                            if (resultFromQuery.size() > 0) {
                                boolean changed = false;
                                for (final Node t : resultFromQuery) {
                                    if (typeToFilter.isAssignableFrom(forName(t.getTypeName()))) {
                                        changed = true;
                                        resultSet.add(t);
                                    }
                                }
                                if (changed) {
                                    break biggerLoop;
                                }
                            }
                            break;
                        default:
                            throw logAndReturn(new IllegalStateException());
                    }
                }
                for (final Class<? extends SLLink> linkType : allKindsOfInheritanceLinksReversed) {
                    final List<Node> deltas = deltasFromEachType.get(linkType);
                    // fixing the node order
                    resultSet.removeAll(deltas);
                    resultSet.addAll(deltas);
                }
                actualSize = resultSet.size();
            }
            final List<T> typedInheritedTypes = new LinkedList<T>();

            for (final Node n : resultSet) {
                if (n.getContext().getID().equals(ABSTRACT_CONTEXT)) {
                    if (!JavaTypePrimitive.class.isAssignableFrom(forName(n.getTypeName()))) {
                        continue;
                    }
                }
                if (!getOrderedActiveContexts().contains(n.getContext())) {
                    continue;
                }
                if (typeToFilter.isAssignableFrom(forName(n.getTypeName())) || n.equals(activeType)) {
                    @SuppressWarnings( "unchecked" )
                    final T typed = (T)this.getTypedNode(n);
                    typedInheritedTypes.add(typed);
                }
            }
            // typedInheritedTypes.remove(activeType);

            switch (includedResult) {
                case DO_NOT_INCLUDE_ACTUAL_TYPE_ON_RESULT:
                    typedInheritedTypes.remove(activeType);
                    break;
                case INCLUDE_ACTUAL_TYPE_ON_RESULT:
                    break;
                default:
                    throw logAndReturn(new IllegalStateException(
                                                                 "Unexpected return on case statement depending on enum constants declared on org.openspotlight.bundle.dap.language.java.support.TypeFinder.IncludedResult."
                                                                 + includedResult.name()));

            }
            return this.order(order, typedInheritedTypes);

        } catch (final Exception e) {
            throw logAndReturnNew(e, InternalJavaFinderError.class);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #getAllParents(org.openspotlight.graph.Node,
     * org.openspotlight.bundle.dap
     * .language.java.resolver.TypeResolver.ResultOrder,
     * org.openspotlight.bundle
     * .dap.language.java.resolver.TypeResolver.IncludedResult)
     */
    @Override
    public <T extends JavaType, A extends T> List<T> getAllParents( final A activeType,
                                                                    final ResultOrder order,
                                                                    final IncludedResult includedResult )
        throws InternalJavaFinderError {
        return this.getParentsByLinkTypes(activeType, order, allKindsOfInheritanceLinks, includedResult,
                                          Recursive.FULLY_RECURSIVE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #getConcreteChildren(org.openspotlight.graph.Node,
     * org.openspotlight.bundle
     * .dap.language.java.resolver.TypeResolver.ResultOrder,
     * org.openspotlight.bundle
     * .dap.language.java.resolver.TypeResolver.IncludedResult)
     */
    @Override
    public <T extends JavaType, A extends T> List<T> getConcreteChildren( final A activeType,
                                                                          final ResultOrder order,
                                                                          final IncludedResult includedResult )
        throws InternalJavaFinderError {
        return this.getAllChildrenByAllLinkTypes(activeType, order, JavaTypeClass.class, includedResult,
                                                 Recursive.FULLY_RECURSIVE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #getConcreteParents(org.openspotlight.graph.Node,
     * org.openspotlight.bundle
     * .dap.language.java.resolver.TypeResolver.ResultOrder,
     * org.openspotlight.bundle
     * .dap.language.java.resolver.TypeResolver.IncludedResult)
     */
    @Override
    public <T extends JavaType, A extends JavaType> List<T> getConcreteParents( final A activeType,
                                                                                final ResultOrder order,
                                                                                final IncludedResult includedResult )
        throws InternalJavaFinderError {
        return this.getParentsByLinkTypes(activeType, order, implementationInheritanceLinks, includedResult,
                                          Recursive.FULLY_RECURSIVE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #getDirectConcreteChildren(org.openspotlight.graph.Node)
     */
    @Override
    public <T extends JavaType, A extends T> List<T> getDirectConcreteChildren( final A activeType )
        throws InternalJavaFinderError {
        return this.getAllChildrenByAllLinkTypes(activeType, ResultOrder.DESC, JavaTypeClass.class,
                                                 IncludedResult.DO_NOT_INCLUDE_ACTUAL_TYPE_ON_RESULT,
                                                 Recursive.ONLY_DIRECT_PARENTS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #getDirectConcreteParents(org.openspotlight.graph.Node)
     */
    @Override
    public <T extends JavaType, A extends T> List<T> getDirectConcreteParents( final A activeType )
        throws InternalJavaFinderError {
        return this.getParentsByLinkTypes(activeType, ResultOrder.DESC, implementationInheritanceLinks,
                                          IncludedResult.DO_NOT_INCLUDE_ACTUAL_TYPE_ON_RESULT, Recursive.ONLY_DIRECT_PARENTS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #getDirectInterfaceChildren(org.openspotlight.graph.Node)
     */
    @Override
    public <T extends JavaType, A extends T> List<T> getDirectInterfaceChildren( final A activeType )
        throws InternalJavaFinderError {

        return this.getAllChildrenByAllLinkTypes(activeType, ResultOrder.DESC, JavaTypeInterface.class,
                                                 IncludedResult.DO_NOT_INCLUDE_ACTUAL_TYPE_ON_RESULT,
                                                 Recursive.ONLY_DIRECT_PARENTS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #getDirectInterfaceParents(org.openspotlight.graph.Node)
     */
    @Override
    public <T extends JavaType, A extends T> List<T> getDirectInterfaceParents( final A activeType )
        throws InternalJavaFinderError {

        final List<T> superTypes = this.getConcreteParents(activeType, ResultOrder.ASC,
                                                           IncludedResult.INCLUDE_ACTUAL_TYPE_ON_RESULT);
        final List<T> result = new ArrayList<T>();
        for (final T n : superTypes) {
            final List<T> currentResult = this.getParentsByLinkTypes(n, ResultOrder.DESC, interfaceInheritanceLinks,
                                                                     IncludedResult.DO_NOT_INCLUDE_ACTUAL_TYPE_ON_RESULT,
                                                                     Recursive.ONLY_DIRECT_PARENTS);
            if (currentResult.size() > 0) {
                result.add(currentResult.get(0));
                break;
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #getInterfaceChildren(org.openspotlight.graph.Node,
     * org.openspotlight.bundle
     * .dap.language.java.resolver.TypeResolver.ResultOrder,
     * org.openspotlight.bundle
     * .dap.language.java.resolver.TypeResolver.IncludedResult)
     */
    @Override
    public <T extends JavaType, A extends T> List<T> getInterfaceChildren( final A activeType,
                                                                           final ResultOrder order,
                                                                           final IncludedResult includedResult )
        throws InternalJavaFinderError {
        return this.getAllChildrenByAllLinkTypes(activeType, order, JavaTypeInterface.class, includedResult,
                                                 Recursive.FULLY_RECURSIVE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #getInterfaceParents(org.openspotlight.graph.Node,
     * org.openspotlight.bundle
     * .dap.language.java.resolver.TypeResolver.ResultOrder,
     * org.openspotlight.bundle
     * .dap.language.java.resolver.TypeResolver.IncludedResult)
     */
    @Override
    public <T extends JavaType, A extends T> List<T> getInterfaceParents( final A activeType,
                                                                          final ResultOrder order,
                                                                          final IncludedResult includedResult )
        throws InternalJavaFinderError {
        final List<T> superTypes = this.getConcreteParents(activeType, ResultOrder.ASC, includedResult);

        final Set<T> result = new LinkedHashSet<T>();

        for (final T n : superTypes) {
            final List<T> currentResult = this.getParentsByLinkTypes(n, ResultOrder.ASC, interfaceInheritanceLinks,
                                                                     IncludedResult.DO_NOT_INCLUDE_ACTUAL_TYPE_ON_RESULT,
                                                                     Recursive.FULLY_RECURSIVE);
            result.addAll(currentResult);
        }
        switch (includedResult) {
            case DO_NOT_INCLUDE_ACTUAL_TYPE_ON_RESULT:
                result.remove(activeType);
                break;
            case INCLUDE_ACTUAL_TYPE_ON_RESULT:
                result.add(activeType);
                break;
            default:
                throw logAndReturn(new IllegalStateException(
                                                             "Unexpected return on case statement depending on enum constants declared on org.openspotlight.bundle.dap.language.java.support.TypeFinder.IncludedResult."
                                                             + includedResult.name()));

        }
        ResultOrder newOrder;
        switch (order) {
            case ASC:
                newOrder = ResultOrder.DESC;
                break;
            case DESC:
                newOrder = ResultOrder.ASC;
                break;
            default:
                throw new IllegalStateException();
        }
        return this.order(newOrder, new ArrayList<T>(result));
    }

    /**
     * Gets the parents by link types.
     * 
     * @param activeType the active type
     * @param order the order
     * @param linkType the link type
     * @param includedResult the included result
     * @param recursive the recursive
     * @return the parents by link types
     * @throws InternalJavaFinderError the internal java finder error
     */
    protected <T extends JavaType, A extends JavaType> List<T> getParentsByLinkTypes( final A activeType,
                                                                                      final ResultOrder order,
                                                                                      final Class<? extends SLLink> linkType,
                                                                                      final IncludedResult includedResult,
                                                                                      final Recursive recursive )
        throws InternalJavaFinderError {
        final HashSet<Class<? extends SLLink>> links = new HashSet<Class<? extends SLLink>>();
        links.add(linkType);
        return this.getParentsByLinkTypes(activeType, order, links, includedResult, recursive);

    }

    /**
     * Gets the parents by link types.
     * 
     * @param activeType the active type
     * @param order the order
     * @param linkTypes the link types
     * @param includedResult the included result
     * @param recursive the recursive
     * @return the parents by link types
     * @throws InternalJavaFinderError the internal java finder error
     */
    protected <T extends JavaType, A extends JavaType> List<T> getParentsByLinkTypes( final A activeType,
                                                                                      final ResultOrder order,
                                                                                      final Set<Class<? extends SLLink>> linkTypes,
                                                                                      final IncludedResult includedResult,
                                                                                      final Recursive recursive )
        throws InternalJavaFinderError {
        try {

            final Set<Node> resultSet = new LinkedHashSet<Node>();
            resultSet.add(activeType);

            int lastSize = resultSet.size();
            int actualSize = -1;
            biggerLoop: while (lastSize != actualSize) {
                lastSize = resultSet.size();
                final Map<Class<? extends SLLink>, List<Node>> deltasFromEachType = new HashMap<Class<? extends SLLink>, List<Node>>();
                for (final Class<? extends SLLink> linkType : linkTypes) {
                    deltasFromEachType.put(linkType, new ArrayList<Node>());
                }
                for (final Class<? extends SLLink> linkType : linkTypes) {
                    final SLQueryApi inheritanceTreeQuery = getSession().createQueryApi();
                    inheritanceTreeQuery.select().type(JavaType.class.getName()).subTypes().comma().byLink(linkType.getName()).b().selectEnd();
                    final Collection<Node> resultFromQuery = inheritanceTreeQuery.execute(new ArrayList<Node>(resultSet)).getNodes();
                    for (final Node possibleDelta : resultFromQuery) {
                        if (!resultSet.contains(possibleDelta)) {
                            deltasFromEachType.get(linkType).add(possibleDelta);
                        }
                    }
                    switch (recursive) {
                        case FULLY_RECURSIVE:
                            resultSet.addAll(resultFromQuery);
                            break;
                        case ONLY_DIRECT_PARENTS:
                            if (resultFromQuery.size() > 0) {
                                resultSet.addAll(resultFromQuery);
                                break biggerLoop;
                            }
                            break;
                        default:
                            throw logAndReturn(new IllegalStateException());
                    }
                }
                for (final Class<? extends SLLink> linkType : linkTypes) {
                    final List<Node> deltas = deltasFromEachType.get(linkType);
                    // fixing the node order
                    resultSet.removeAll(deltas);
                    resultSet.addAll(deltas);
                }
                actualSize = resultSet.size();
            }
            final ArrayList<T> typedInheritedTypes = new ArrayList<T>(resultSet.size());

            for (final Node n : resultSet) {
                if (n.getContext().getID().equals(ABSTRACT_CONTEXT) || !getOrderedActiveContexts().contains(n.getContext())) {
                    continue;
                }
                @SuppressWarnings( "unchecked" )
                final T typed = (T)this.getTypedNode(n);
                typedInheritedTypes.add(typed);
            }
            switch (includedResult) {
                case DO_NOT_INCLUDE_ACTUAL_TYPE_ON_RESULT:
                    typedInheritedTypes.remove(activeType);
                    break;
                case INCLUDE_ACTUAL_TYPE_ON_RESULT:
                    break;
                default:
                    throw logAndReturn(new IllegalStateException(
                                                                 "Unexpected return on case statement depending on enum constants declared on org.openspotlight.bundle.dap.language.java.support.TypeFinder.IncludedResult."
                                                                 + includedResult.name()));

            }
            return this.order(order, typedInheritedTypes);

        } catch (final Exception e) {
            throw logAndReturnNew(e, InternalJavaFinderError.class);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #getPrimitiveFor(org.openspotlight.graph.Node)
     */
    @Override
    public <T extends JavaType, A extends JavaType> T getPrimitiveFor( final A wrappedType ) throws InternalJavaFinderError {
        return this.<T, A>getUniqueResult(wrappedType, Autoboxes.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #getType(java.lang.String)
     */
    @SuppressWarnings( "unchecked" )
    @Override
    public <T extends JavaType> T getType( final String typeToSolve ) throws InternalJavaFinderError {
        try {
            final Node Node = new ByNameTypeFinder(typeToSolve).getTypeByAllPossibleNames();
            if (Node == null) {
                throw logAndReturn(new InternalJavaFinderError());
            }
            return (T)this.getTypedNode(Node);
        } catch (final Exception e) {
            throw logAndReturnNew(e, InternalJavaFinderError.class);
        }
    }

    /**
     * Gets the type.
     * 
     * @param typeToSolve the type to solve
     * @param activeType the active type
     * @param parametrizedTypes the parametrized types
     * @return the type
     * @throws InternalJavaFinderError the internal java finder error
     * @{inheritDoc
     */
    @SuppressWarnings( "unchecked" )
    @Override
    public <T extends JavaType, A extends T> T getType( final String typeToSolve,
                                                        final A activeType,
                                                        final List<? extends JavaType> parametrizedTypes )
        throws InternalJavaFinderError {
        try {
            final List<Node> inheritedTypes = new LinkedList<Node>();
            inheritedTypes.add(activeType);
            for (final Class<? extends SLLink> linkType : JavaTypeResolver.implementationInheritanceLinks) {
                final SLQueryApi inheritanceTreeQuery = getSession().createQueryApi();
                inheritanceTreeQuery.select().type(JavaType.class.getName()).subTypes().comma().byLink(linkType.getName()).b().selectEnd().keepResult().executeXTimes();
                final Collection<Node> resultFromQuery = inheritanceTreeQuery.execute(inheritedTypes).getNodes();
                inheritedTypes.addAll(resultFromQuery);
            }
            for (final Class<? extends SLLink> linkType : JavaTypeResolver.interfaceInheritanceLinks) {
                final SLQueryApi inheritanceTreeQuery = getSession().createQueryApi();
                inheritanceTreeQuery.select().type(JavaType.class.getName()).subTypes().comma().byLink(linkType.getName()).b().selectEnd().keepResult().executeXTimes();
                final Collection<Node> resultFromQuery = inheritanceTreeQuery.execute(inheritedTypes).getNodes();
                inheritedTypes.addAll(resultFromQuery);
            }

            final SLQueryApi allTypesFromSamePackagesQuery = getSession().createQueryApi();
            allTypesFromSamePackagesQuery.select().type(JavaPackage.class.getName()).comma().byLink(PackageType.class.getName()).a().selectEnd();
            allTypesFromSamePackagesQuery.select().type(JavaType.class.getName()).subTypes().comma().byLink(
                                                                                                            PackageType.class.getName()).b().selectEnd();
            final Collection<Node> allTypesFromSamePackages = allTypesFromSamePackagesQuery.execute(inheritedTypes).getNodes();

            final Node Node = new TypesFromTheSamePackageQueryExecutor(typeToSolve, allTypesFromSamePackages).getTypeByAllPossibleNames();
            if (Node == null) {
                throw logAndReturn(new InternalJavaFinderError());
            }
            final T typed = (T)this.getTypedNode(Node);
            return typed;
        } catch (final Exception e) {
            throw logAndReturnNew(e, InternalJavaFinderError.class);
        }
    }

    /**
     * Gets the typed node.
     * 
     * @param Node the sl node
     * @return the typed node
     * @throws Exception the exception
     * @{inheritDoc
     */

    private <T extends JavaType> T getTypedNode( final Node Node ) throws Exception {
        @SuppressWarnings( "unchecked" )
        final T typedNode = (T)getSession().getNodeByID(Node.getID());
        return typedNode;
    }

    /**
     * Gets the unique result.
     * 
     * @param type the type
     * @param linkType the link type
     * @return the unique result
     * @throws InternalJavaFinderError the internal java finder error
     */
    @SuppressWarnings( "unchecked" )
    private <T extends JavaType, A extends JavaType> T getUniqueResult( final A type,
                                                                        final Class<? extends SLLink> linkType )
        throws InternalJavaFinderError {
        try {
            final ArrayList<Node> initial = new ArrayList<Node>();
            initial.add(type);
            final SLQueryApi inheritanceTreeQuery = getSession().createQueryApi();
            inheritanceTreeQuery.select().type(JavaType.class.getName()).subTypes().comma().byLink(linkType.getName()).b().selectEnd();
            final Collection<Node> resultFromQuery = inheritanceTreeQuery.execute(initial).getNodes();
            if (resultFromQuery.size() > 0) {
                return (T)this.getTypedNode(resultFromQuery.iterator().next());
            }
            throw new InternalJavaFinderError();
        } catch (final Exception e) {
            throw logAndReturnNew(e, InternalJavaFinderError.class);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #getWrapperFor(org.openspotlight.graph.Node)
     */
    @Override
    public <T extends JavaType, A extends JavaType> T getWrapperFor( final A primitiveType ) throws InternalJavaFinderError {
        return this.<T, A>getUniqueResult(primitiveType, AutoboxedBy.class);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #isConcreteType(org.openspotlight.graph.Node)
     */
    @Override
    public <T extends JavaType> boolean isConcreteType( final T type ) throws InternalJavaFinderError {
        try {
            final String typeName = type.getTypeName();
            return JavaTypeClass.class.isAssignableFrom(forName(typeName));
        } catch (final Exception e) {
            throw logAndReturnNew(e, InternalJavaFinderError.class);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #isPrimitiveType(org.openspotlight.graph.Node)
     */
    @Override
    public <T extends JavaType> boolean isPrimitiveType( final T type ) throws InternalJavaFinderError {
        try {
            return JavaTypePrimitive.class.isAssignableFrom(forName(type.getTypeName()));
        } catch (final Exception e) {
            throw logAndReturnNew(e, InternalJavaFinderError.class);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openspotlight.bundle.dap.language.java.resolver.AbstractTypeResolver
     * #isTypeOf(org.openspotlight.graph.Node, org.openspotlight.graph.Node)
     */
    @Override
    public <T extends JavaType, A extends JavaType> boolean isTypeOf( final T implementation,
                                                                      final A superType ) throws InternalJavaFinderError {
        try {
            final List<JavaType> children = this.<JavaType, A>getAllChildren(superType, ResultOrder.ASC,
                                                                             IncludedResult.INCLUDE_ACTUAL_TYPE_ON_RESULT);

            final boolean result = children.contains(implementation);

            if (result) {
                if (this.isPrimitiveType(implementation)) {
                    if (this.isConcreteType(superType)) {
                        final Collection<Autoboxes> links = getSession().getLink(Autoboxes.class, superType, implementation);
                        if (links.size() == 0) {
                            return false;
                        }
                    }
                }
            }
            return result;
        } catch (final Exception e) {
            throw logAndReturnNew(e, InternalJavaFinderError.class);
        }
    }

    /**
     * Order the result
     * 
     * @param order the order
     * @param typedInheritedTypes the typed inherited types
     * @return the list< t>
     */
    private <T extends JavaType> List<T> order( final ResultOrder order,
                                                final List<T> typedInheritedTypes ) {
        switch (order) {
            case ASC:
                Collections.reverse(typedInheritedTypes);
                return typedInheritedTypes;
            case DESC:
                return typedInheritedTypes;
            default:
                throw logAndReturn(new IllegalStateException(
                                                             "Unexpected return on case statement depending on enum constants declared on org.openspotlight.bundle.dap.language.java.support.TypeFinder.ResultOrder."
                                                             + order.name()));
        }

    }

}
