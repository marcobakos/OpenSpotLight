/*
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
 * OpenSpotLight - Plataforma de Governan�a de TI de C�digo Aberto 
 *
 * Direitos Autorais Reservados (c) 2009, CARAVELATECH CONSULTORIA E TECNOLOGIA 
 * EM INFORMATICA LTDA ou como contribuidores terceiros indicados pela etiqueta 
 * @author ou por expressa atribui��o de direito autoral declarada e atribu�da pelo autor.
 * Todas as contribui��es de terceiros est�o distribu�das sob licen�a da
 * CARAVELATECH CONSULTORIA E TECNOLOGIA EM INFORMATICA LTDA. 
 * 
 * Este programa � software livre; voc� pode redistribu�-lo e/ou modific�-lo sob os 
 * termos da Licen�a P�blica Geral Menor do GNU conforme publicada pela Free Software 
 * Foundation. 
 * 
 * Este programa � distribu�do na expectativa de que seja �til, por�m, SEM NENHUMA 
 * GARANTIA; nem mesmo a garantia impl�cita de COMERCIABILIDADE OU ADEQUA��O A UMA
 * FINALIDADE ESPEC�FICA. Consulte a Licen�a P�blica Geral Menor do GNU para mais detalhes.  
 * 
 * Voc� deve ter recebido uma c�pia da Licen�a P�blica Geral Menor do GNU junto com este
 * programa; se n�o, escreva para: 
 * Free Software Foundation, Inc. 
 * 51 Franklin Street, Fifth Floor 
 * Boston, MA  02110-1301  USA
 */
package org.openspotlight.graph.query.parser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.openspotlight.common.util.ClassLoaderUtil;
import org.openspotlight.common.util.ClassPathResource;
import org.openspotlight.common.util.Sha1;
import org.openspotlight.graph.query.SLInvalidQuerySyntaxException;
import org.openspotlight.graph.query.SLQLVariable;
import org.openspotlight.graph.query.SLQueryTextInternal;

/**
 * The Class SLQueryTextInternalBuilder. This class genarates, based on slql external dsl, a new instance SLQueryTextInternal.
 * 
 * @author porcelli
 */
public class SLQueryTextInternalBuilder {

    private CtClass[] CONSTRUCTOR_ARGS;
    private CtClass[] CONSTRUCTOR_THROWS;
    private CtClass[] EXECUTE_ARGS;
    private CtClass[] EXECUTE_THROWS;
    private CtClass   EXECUTE_RETURN_TYPE;

    /**
     * Builds the SLQueryTextInternal based on input
     * 
     * @param slqlText the slql text
     * @return the sL query text internal
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     */
    public SLQueryTextInternal build( final String slqlText ) throws SLInvalidQuerySyntaxException {
        SLQueryTextInternalInfo queryInfo = buildQueryInfo(slqlText);

        SLQueryTextInternal target = null;
        if (queryInfo.hasTarget()) {
            target = buildTargetQuery(queryInfo.getTargetUniqueId(), queryInfo.getDefineTargetContent());
        }

        Set<SLQLVariable> variables = buildVariableCollection(queryInfo);

        return buildQuery(queryInfo.getId(), variables, queryInfo.getOutputModelName(), target, queryInfo.getContent());
    }

    /**
     * Builds the query.
     * 
     * @param id the id
     * @param variables the variables
     * @param outputModelName the output model name
     * @param target the target
     * @param executeContent the execute content
     * @return the sL query text internal
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     */
    private SLQueryTextInternal buildQuery( final String id,
                                            final Set<SLQLVariable> variables,
                                            final String outputModelName,
                                            final SLQueryTextInternal target,
                                            final String executeContent ) throws SLInvalidQuerySyntaxException {
        try {
            String className = getClassName(id);

            if (!ClassLoaderUtil.existsClass(className)) {
                createNewQueryClass(className, executeContent);
            }

            @SuppressWarnings( "unchecked" )
            Class<AbstractSLQueryTextInternal> queryResult = (Class<AbstractSLQueryTextInternal>)ClassLoaderUtil.getClass(className);

            Constructor<AbstractSLQueryTextInternal> constr;
            constr = queryResult.getConstructor(String.class, Set.class, String.class,
                                                SLQueryTextInternal.class);
            return constr.newInstance(id, variables, outputModelName, target);

        } catch (Exception e) {
            throw new SLInvalidQuerySyntaxException(e);
        }
    }

    /**
     * Builds the target query.
     * 
     * @param targetUniqueId the target unique id
     * @param defineTargetContent the define target content
     * @return the sL query text internal
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     */
    private SLQueryTextInternal buildTargetQuery( final String targetUniqueId,
                                                  final String defineTargetContent ) throws SLInvalidQuerySyntaxException {
        try {
            String className = getClassName(targetUniqueId);

            if (!ClassLoaderUtil.existsClass(className)) {
                createNewQueryClass(className, defineTargetContent);
            }

            @SuppressWarnings( "unchecked" )
            Class<AbstractSLQueryTextInternal> queryResult = (Class<AbstractSLQueryTextInternal>)ClassLoaderUtil.getClass(className);

            Constructor<AbstractSLQueryTextInternal> constr;
            constr = queryResult.getConstructor(String.class, Set.class, String.class,
                                                SLQueryTextInternal.class);
            return constr.newInstance(targetUniqueId, null, null, null);

        } catch (Exception e) {
            throw new SLInvalidQuerySyntaxException(e);
        }
    }

    /**
     * Gets the class name.
     * 
     * @param id the id
     * @return the class name
     */
    private String getClassName( String id ) {
        return "org.openspotlight.graph.query.SLQLQuery$A" + id;
    }

    /**
     * The Enum SLQLVariableDataType.
     * 
     * @author porcelli
     */
    private enum SLQLVariableDataType {

        /** The INTEGER data type. */
        INTEGER,

        /** The DECIMAL data type. */
        DECIMAL,

        /** The STRING data type. */
        STRING,

        /** The BOOLEAN data type. */
        BOOLEAN
    }

    /**
     * Builds the variable collection.
     * 
     * @param queryInfo the query info
     * @return the set< slql variable>
     */
    private Set<SLQLVariable> buildVariableCollection( SLQueryTextInternalInfo queryInfo ) {
        Set<SLQLVariable> result = new HashSet<SLQLVariable>();

        Collection<SLQLVariable> tempBoolVars = getVariablesByDataType(SLQLVariableDataType.BOOLEAN, queryInfo.getBoolVariables(), queryInfo.getMessageVariables(), queryInfo.getDomainVariables());
        Collection<SLQLVariable> tempIntVars = getVariablesByDataType(SLQLVariableDataType.INTEGER, queryInfo.getIntVariables(), queryInfo.getMessageVariables(), queryInfo.getDomainVariables());
        Collection<SLQLVariable> tempDecVars = getVariablesByDataType(SLQLVariableDataType.DECIMAL, queryInfo.getDecVariables(), queryInfo.getMessageVariables(), queryInfo.getDomainVariables());
        Collection<SLQLVariable> tempStringVars = getVariablesByDataType(SLQLVariableDataType.STRING, queryInfo.getStringVariables(), queryInfo.getMessageVariables(), queryInfo.getDomainVariables());

        result.addAll(tempBoolVars);
        result.addAll(tempIntVars);
        result.addAll(tempDecVars);
        result.addAll(tempStringVars);

        return result;
    }

    /**
     * Gets the variables by data type.
     * 
     * @param dataType the data type
     * @param variables the variables
     * @param messageVariables the message variables
     * @param domainVariables the domain variables
     * @return the variables by data type
     */
    private Collection<SLQLVariable> getVariablesByDataType( final SLQLVariableDataType dataType,
                                                             final Collection<String> variables,
                                                             final Map<String, String> messageVariables,
                                                             final Map<String, Set<Object>> domainVariables ) {
        Set<SLQLVariable> result = new HashSet<SLQLVariable>(variables.size());
        for (String activeVariableName : variables) {
            SLQLVariable variable = null;
            switch (dataType) {
                case INTEGER:
                    variable = new SLQLVariableInteger(activeVariableName);
                    break;
                case DECIMAL:
                    variable = new SLQLVariableFloat(activeVariableName);
                    break;
                case STRING:
                    variable = new SLQLVariableString(activeVariableName);
                    break;
                case BOOLEAN:
                    variable = new SLQLVariableBoolean(activeVariableName);
                    break;
            }

            if (messageVariables.containsKey(activeVariableName)) {
                variable.setDisplayMessage(messageVariables.get(activeVariableName));
            }
            if (dataType != SLQLVariableDataType.BOOLEAN && domainVariables.containsKey(activeVariableName)) {
                variable.addAllDomainValue(domainVariables.get(activeVariableName));
            }
            result.add(variable);
        }
        return result;
    }

    /**
     * Builds the query info.
     * 
     * @param slqlText the slql text
     * @return the sL query text internal info
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     */
    private SLQueryTextInternalInfo buildQueryInfo( final String slqlText ) throws SLInvalidQuerySyntaxException {
        try {
            InputStream stream = ClassPathResource.getResourceFromClassPath(getClass(), "SLQLTemplate.stg");
            Reader reader = new InputStreamReader(stream);
            StringTemplateGroup templates = new StringTemplateGroup(reader);
            reader.close();
            ANTLRStringStream inputStream = new ANTLRStringStream(slqlText);
            SLQLLexer lex = new SLQLLexer(inputStream);
            CommonTokenStream tokens = new CommonTokenStream(lex);

            SLQLParser parser = new SLQLParser(tokens);
            parser.setIsTesting(false);
            if (parser.hasErrors()) {
                throw parser.getErrors().get(0);
            }
            CommonTree result = (CommonTree)parser.compilationUnit().tree;

            String uniqueId = Sha1.getSha1SignatureEncodedAsHexa(result.toStringTree().toLowerCase());

            String targetUniqueId = null;
            if (parser.getDefineTargetTreeResult() != null) {
                targetUniqueId = Sha1.getSha1SignatureEncodedAsHexa(parser.getDefineTargetTreeResult());
            }

            CommonTreeNodeStream treeNodes = new CommonTreeNodeStream(result);

            SLQLWalker walker = new SLQLWalker(treeNodes);
            walker.setTemplateLib(templates);

            SLQueryTextInternalInfo queryInfo = walker.compilationUnit().queryInfoReturn;
            queryInfo.setId(uniqueId);
            queryInfo.setTargetUniqueId(targetUniqueId);

            return queryInfo;
        } catch (Exception e) {
            throw new SLInvalidQuerySyntaxException(e);
        }
    }

    /**
     * Creates the new query class.
     * 
     * @param className the class name
     * @param executeContent the execute content
     * @throws SLInvalidQuerySyntaxException the SL invalid query syntax exception
     */
    private void createNewQueryClass( String className,
                                      String executeContent ) throws SLInvalidQuerySyntaxException {
        try {

            ClassPool pool = ClassPool.getDefault();
            CtClass superClass = pool.get(AbstractSLQueryTextInternal.class.getName());
            CtClass clas = pool.makeClass(className, superClass);

            if (CONSTRUCTOR_ARGS == null) {
                for (Constructor<?> constructor : AbstractSLQueryTextInternal.class.getConstructors()) {
                    if (constructor.getParameterTypes().length > 0) {
                        CONSTRUCTOR_ARGS = new CtClass[constructor.getParameterTypes().length];
                        CONSTRUCTOR_THROWS = new CtClass[constructor.getExceptionTypes().length];
                        for (int i = 0; i < constructor.getParameterTypes().length; i++) {
                            CONSTRUCTOR_ARGS[i] = pool.get(constructor.getParameterTypes()[i].getName());
                        }
                        for (int i = 0; i < constructor.getExceptionTypes().length; i++) {
                            CONSTRUCTOR_THROWS[i] = pool.get(constructor.getExceptionTypes()[i].getName());
                        }
                        break;
                    }
                }

                for (Method method : AbstractSLQueryTextInternal.class.getMethods()) {
                    if (method.getName().equals("execute")) {
                        EXECUTE_ARGS = new CtClass[method.getParameterTypes().length];
                        EXECUTE_THROWS = new CtClass[method.getExceptionTypes().length];
                        for (int i = 0; i < method.getParameterTypes().length; i++) {
                            EXECUTE_ARGS[i] = pool.get(method.getParameterTypes()[i].getName());
                        }
                        for (int i = 0; i < method.getExceptionTypes().length; i++) {
                            EXECUTE_THROWS[i] = pool.get(method.getExceptionTypes()[i].getName());
                        }
                        EXECUTE_RETURN_TYPE = pool.get(method.getReturnType().getName());
                        break;
                    }
                }
            }

            CtConstructor newConstructor = CtNewConstructor.make(CONSTRUCTOR_ARGS, CONSTRUCTOR_THROWS, clas);
            clas.addConstructor(newConstructor);

            CtMethod newMethod = CtNewMethod.make(EXECUTE_RETURN_TYPE, "execute", EXECUTE_ARGS, EXECUTE_THROWS, executeContent, clas);
            clas.addMethod(newMethod);

            clas.toClass(SLQueryTextInternalBuilder.class.getClassLoader(), SLQueryTextInternalBuilder.class.getProtectionDomain());
        } catch (Exception e) {
            throw new SLInvalidQuerySyntaxException(e);
        }
    }
}
