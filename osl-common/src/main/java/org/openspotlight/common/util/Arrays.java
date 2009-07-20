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
package org.openspotlight.common.util;

import static org.openspotlight.common.util.Assertions.checkCondition;
import static org.openspotlight.common.util.Assertions.checkEachParameterNotNull;
import static org.openspotlight.common.util.Exceptions.logAndThrow;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to deal with arrays
 * 
 * @author Luiz Fernando Teston - feu.teston@caravelatech.com
 * 
 */
public class Arrays {
    
    /**
     * Convenient method to group varargs on a array. This can be used on a
     * situation that is needed to group more than a group of Ts on a method.
     * 
     * <pre>
     * someMethod(of(someParam1,   someParam2,...,   someParamN),
     *         andOf(anotherParam1,anotherParam2,...,anotherParamN));
     * </pre>
     * 
     * @param <T>
     * @param array
     * @return the same parameters in a array
     */
    public static <T> T[] andOf(final T... array) {
        return array;
    }
    
    /**
     * Convenient method to group varargs on a array. This can be used on a
     * situation that is needed to group more than a group of Ts on a method.
     * 
     * <pre>
     * someMethod(ofNames(someParam1,   someParam2,...,   someParamN),
     *         andValues(anotherParam1,anotherParam2,...,anotherParamN));
     * </pre>
     * 
     * @param <T>
     * @param array
     * @return the same parameters in a array
     */
    public static <T> T[] andValues(final T... array) {
        return array;
    }
    
    /**
     * Returns a Map in the following way:
     * 
     * <pre>
     * Map&lt;String, Integer&gt; map = map(ofKeys(&quot;1&quot;, &quot;2&quot;, &quot;3&quot;), andValues(1, 2, 3));
     * </pre>
     * 
     * @param <K>
     * @param ofKeys
     * @param andValues
     * @return a new map with the parameters
     */
    public static <K> Map<K, Class<?>> map(final K[] ofKeys,
            final Class<?>[] andValues) {
        checkCondition("keysAndValuesWithSameSize", //$NON-NLS-1$
                ((ofKeys == null) || (andValues == null))
                        || (ofKeys.length == andValues.length));
        if (ofKeys == null) {
            return new HashMap<K, Class<?>>();
        }
        checkEachParameterNotNull("ofKeys", ofKeys); //$NON-NLS-1$
        final Map<K, Class<?>> map = new HashMap<K, Class<?>>();
        final int size = ofKeys.length;
        for (int i = 0; i < size; i++) {
            map.put(ofKeys[i], andValues[i]);
        }
        return map;
    }
    
    /**
     * Returns a Map in the following way:
     * 
     * <pre>
     * Map&lt;String, Integer&gt; map = map(ofKeys(&quot;1&quot;, &quot;2&quot;, &quot;3&quot;), andValues(1, 2, 3));
     * </pre>
     * 
     * @param <K>
     * @param <V>
     * @param ofKeys
     * @param andValues
     * @return a new map with the parameters
     */
    public static <K, V> Map<K, V> map(final K[] ofKeys, final V[] andValues) {
        checkCondition("keysAndValuesWithSameSize", //$NON-NLS-1$
                ((ofKeys == null) || (andValues == null))
                        || (ofKeys.length == andValues.length));
        if (ofKeys == null) {
            return new HashMap<K, V>();
        }
        checkEachParameterNotNull("ofKeys", ofKeys); //$NON-NLS-1$
        final Map<K, V> map = new HashMap<K, V>();
        final int size = ofKeys.length;
        for (int i = 0; i < size; i++) {
            map.put(ofKeys[i], andValues[i]);
        }
        return map;
    }
    
    /**
     * Convenient method to group varargs on a array. This can be used on a
     * situation that is needed to group more than a group of Ts on a method.
     * 
     * <pre>
     * someMethod(of(someParam1,   someParam2,...,   someParamN),
     *         andOf(anotherParam1,anotherParam2,...,anotherParamN));
     * </pre>
     * 
     * @param <T>
     * @param array
     * @return the same parameters in a array
     */
    public static <T> T[] of(final T... array) {
        return array;
    }
    
    /**
     * Convenient method to group varargs on a array. This can be used on a
     * situation that is needed to group more than a group of Ts on a method.
     * 
     * <pre>
     * someMethod(ofNames(someParam1,   someParam2,...,   someParamN),
     *         andValues(anotherParam1,anotherParam2,...,anotherParamN));
     * </pre>
     * 
     * @param <T>
     * @param array
     * @return the same parameters in a array
     */
    public static <T> T[] ofKeys(final T... array) {
        return array;
    }
    
    /**
     * Should not be instantiated
     */
    private Arrays() {
        logAndThrow(new IllegalStateException(Messages
                .getString("invalidConstructor"))); //$NON-NLS-1$
    }
    
}
