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
package org.openspotlight.graph;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openspotlight.SLRuntimeException;
import org.openspotlight.graph.annotation.SLProperty;

/**
 * The Class SLInvocationHandlerSupport.
 * 
 * @author Vitor Hugo Chagas
 */
public class SLInvocationHandlerSupport {
	
	/**
	 * Checks if is getter.
	 * 
	 * @param proxy the proxy
	 * @param method the method
	 * 
	 * @return true, if is getter
	 */
	static boolean isGetter(Object proxy, Method method) {
		try {
			boolean status = false;
			if (method.getName().startsWith("get") && !method.getReturnType().equals(void.class) && method.getParameterTypes().length == 0) {
				SLProperty propertyAnnotation = method.getAnnotation(SLProperty.class);
				if (propertyAnnotation == null) {
					try {
						String setterName = "set".concat(method.getName().substring(3));
						Class<?> iFace = proxy.getClass().getInterfaces()[0];
						Method setterMethod = iFace.getMethod(setterName, new Class<?>[] {method.getReturnType()});
						status = setterMethod.getAnnotation(SLProperty.class) != null && setterMethod.getReturnType().equals(void.class);
					}
					catch (NoSuchMethodException e) {}
				}
				else {
					status = true;
				}
			}
			return status;
		}
		catch (Exception e) {
			throw new SLRuntimeException("Error on attempt to verify if method is getter.", e);
		}
	}
	
	/**
	 * Checks if is setter.
	 * 
	 * @param proxy the proxy
	 * @param method the method
	 * 
	 * @return true, if is setter
	 */
	static boolean isSetter(Object proxy, Method method) {
		try {
			boolean status = false;
			if (method.getName().startsWith("set") && method.getReturnType().equals(void.class) && method.getParameterTypes().length == 1) {
				SLProperty propertyAnnotation = method.getAnnotation(SLProperty.class);
				if (propertyAnnotation == null) {
					try {
						String getterName = "get".concat(method.getName().substring(3));
						Class<?> iFace = proxy.getClass().getInterfaces()[0];
						Method getterMethod = iFace.getMethod(getterName, new Class<?>[] {});
						status = getterMethod.getAnnotation(SLProperty.class) != null 
							&& getterMethod.getReturnType().equals(method.getParameterTypes()[0]);
					}
					catch (NoSuchMethodException e) {}
				}
				else {
					status = true;
				}
			}
			return status;
		}
		catch (Exception e) {
			throw new SLRuntimeException("Error on attempt to verify if method is setter.", e);
		}
	}
	
	/**
	 * Invoke method.
	 * 
	 * @param object the object
	 * @param method the method
	 * @param args the args
	 * 
	 * @return the object
	 * 
	 * @throws Throwable the throwable
	 */
	static Object invokeMethod(Object object, Method method, Object[] args) throws Throwable {
		try {
			return method.invoke(object, args);
		}
		catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
		catch (Exception e) {
			throw new SLRuntimeException("Error on node proxy.", e);
		}
	}
	
	/**
	 * Gets the property name.
	 * 
	 * @param method the method
	 * 
	 * @return the property name
	 */
	static String getPropertyName(Method method) {
		return method.getName().substring(3, 4).toLowerCase().concat(method.getName().substring(4));
	}

}