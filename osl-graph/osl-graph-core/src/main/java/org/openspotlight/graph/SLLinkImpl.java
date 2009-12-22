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
package org.openspotlight.graph;

import java.io.Serializable;
import java.text.Collator;
import java.util.HashSet;
import java.util.Set;

import org.openspotlight.common.concurrent.Lock;
import org.openspotlight.common.exception.SLException;
import org.openspotlight.common.exception.SLRuntimeException;
import org.openspotlight.common.util.Exceptions;
import org.openspotlight.graph.persistence.SLInvalidPersistentPropertyTypeException;
import org.openspotlight.graph.persistence.SLPersistentNode;
import org.openspotlight.graph.persistence.SLPersistentProperty;
import org.openspotlight.graph.persistence.SLPersistentTreeSessionException;
import org.openspotlight.graph.util.ProxyUtil;

/**
 * The Class SLLinkImpl.
 * 
 * @author Vitor Hugo Chagas
 */
public class SLLinkImpl implements SLLink {

	/** The session. */
	private final SLGraphSession session;

	private final Lock lock;

	/** The link node. */
	private final SLPersistentNode linkNode;

	/** The event poster. */
	private final SLGraphSessionEventPoster eventPoster;

	/**
	 * Instantiates a new sL link impl.
	 * 
	 * @param session
	 *            the session
	 * @param linkNode
	 *            the link node
	 * @param eventPoster
	 *            the event poster
	 */
	public SLLinkImpl(final SLGraphSession session,
			final SLPersistentNode linkNode,
			final SLGraphSessionEventPoster eventPoster) {
		this.session = session;
		lock = session.getLockObject();
		this.linkNode = linkNode;
		this.eventPoster = eventPoster;
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final SLLink l) {
		synchronized (lock) {
			try {
				final SLLinkImpl link = (SLLinkImpl) ProxyUtil
						.getLinkFromProxy(l);
				final String linkClassName1 = getLinkClassNode().getName();
				final String linkClassName2 = link.getLinkClassNode().getName();
				if (linkClassName1.equals(linkClassName2)) {
					final String pairName1 = getPairKeyNode().getName();
					final String pairName2 = link.getPairKeyNode().getName();
					if (pairName1.equals(pairName2)) {
						final Long linkCount1 = linkNode.getProperty(
								Long.class, SLConsts.PROPERTY_NAME_LINK_COUNT)
								.getValue();
						final Long linkCount2 = link.linkNode.getProperty(
								Long.class, SLConsts.PROPERTY_NAME_LINK_COUNT)
								.getValue();
						return linkCount1.compareTo(linkCount2);
					} else {
						return pairName1.compareTo(pairName2);
					}
				} else {
					return linkClassName1.compareTo(linkClassName2);
				}
			} catch (final SLPersistentTreeSessionException e) {
				throw new SLRuntimeException(
						"Error on attempt to execute SLLinkImpl.compareTo().",
						e);
			}
		}
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		synchronized (lock) {
			try {
				if (obj == null || !(obj instanceof SLLink)) {
					return false;
				}
				final SLPersistentNode classNode1 = getLinkClassNode();
				final SLPersistentNode pairNode1 = getPairKeyNode();
				final String name1 = classNode1.getName().concat(
						pairNode1.getName());
				final SLLinkImpl link = (SLLinkImpl) ProxyUtil
						.getLinkFromProxy(obj);
				final SLPersistentNode classNode2 = link.getLinkClassNode();
				final SLPersistentNode pairNode2 = link.getPairKeyNode();
				final String name2 = classNode2.getName().concat(
						pairNode2.getName());
				return name1.equals(name2);
			} catch (final SLException e) {
				throw new SLRuntimeException(
						"Error on attempt to execute SLLinkImpl.equals().", e);
			}
		}
	}

	/**
	 * Gets the a node.
	 * 
	 * @return the a node
	 * 
	 * @throws SLException
	 *             the SL exception
	 */
	private SLNode getANode() throws SLException {
		synchronized (lock) {

			final SLPersistentNode pairKeyNode = getPairKeyNode();
			final SLPersistentProperty<String> nodeIDProp = pairKeyNode
					.getProperty(String.class, SLConsts.PROPERTY_NAME_A_NODE_ID);
			return session.getNodeByID(nodeIDProp.getValue());
		}
	}

	/**
	 * Gets the b node.
	 * 
	 * @return the b node
	 * 
	 * @throws SLException
	 *             the SL exception
	 */
	private SLNode getBNode() throws SLException {
		synchronized (lock) {

			final SLPersistentNode pairKeyNode = getPairKeyNode();
			final SLPersistentProperty<String> nodeIDProp = pairKeyNode
					.getProperty(String.class, SLConsts.PROPERTY_NAME_B_NODE_ID);
			return session.getNodeByID(nodeIDProp.getValue());
		}
	}

	/**
	 * Gets the direction.
	 * 
	 * @return the direction
	 * 
	 * @throws SLPersistentTreeSessionException
	 *             the SL persistent tree session exception
	 */
	private int getDirection() throws SLPersistentTreeSessionException {
		synchronized (lock) {
			final SLPersistentProperty<Integer> directionProp = linkNode
					.getProperty(Integer.class,
							SLConsts.PROPERTY_NAME_DIRECTION);
			return directionProp.getValue();
		}
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openspotlight.graph.SLLink#getID()
	 */
	public String getID() throws SLGraphSessionException {
		synchronized (lock) {
			try {
				return linkNode.getID();
			} catch (final SLPersistentTreeSessionException e) {
				throw new SLGraphSessionException(
						"Error on attempt to retrieve link ID.", e);
			}
		}
	}

	/**
	 * Gets the link class node.
	 * 
	 * @return the link class node
	 * 
	 * @throws SLPersistentTreeSessionException
	 *             the SL persistent tree session exception
	 */
	private SLPersistentNode getLinkClassNode()
			throws SLPersistentTreeSessionException {
		synchronized (lock) {
			return getPairKeyNode().getParent();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openspotlight.graph.SLLink#getLinkType()
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends SLLink> getLinkType() throws SLGraphSessionException {
		synchronized (lock) {
			try {
				return (Class<? extends SLLink>) Class
						.forName(getLinkClassNode().getName());
			} catch (final Exception e) {
				throw new SLGraphSessionException(
						"Error on attempt to retrieve link type.", e);
			}
		}
	}

	public Lock getLockObject() {
		return lock;
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openspotlight.graph.SLLink#getOtherSide(org.openspotlight.graph.SLNode
	 * )
	 */
	public SLNode getOtherSide(final SLNode side)
			throws SLInvalidLinkSideException, SLGraphSessionException {
		synchronized (lock) {
			SLNode otherSide = null;
			try {
				final SLNode aNode = getANode();
				final SLNode bNode = getBNode();
				if (aNode.equals(bNode) && aNode.equals(side)) {
					otherSide = side;
				} else {
					if (side.equals(aNode)) {
						otherSide = bNode;
					} else if (side.equals(bNode)) {
						otherSide = aNode;
					}
				}
			} catch (final SLException e) {
				throw new SLGraphSessionException(
						"Error on attempt to retrieve link other side.", e);
			}
			if (otherSide == null) {
				throw new SLInvalidLinkSideException();
			}
			return otherSide;
		}
	}

	/**
	 * Gets the pair key node.
	 * 
	 * @return the pair key node
	 * 
	 * @throws SLPersistentTreeSessionException
	 *             the SL persistent tree session exception
	 */
	private SLPersistentNode getPairKeyNode()
			throws SLPersistentTreeSessionException {
		synchronized (lock) {
			return linkNode.getParent();
		}
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openspotlight.graph.SLLink#getProperties()
	 */
	public Set<SLLinkProperty<Serializable>> getProperties()
			throws SLGraphSessionException {
		synchronized (lock) {
			try {
				final Set<SLLinkProperty<Serializable>> properties = new HashSet<SLLinkProperty<Serializable>>();
				final Set<SLPersistentProperty<Serializable>> persistentProperties = linkNode
						.getProperties(SLConsts.PROPERTY_PREFIX_USER + ".*");
				for (final SLPersistentProperty<Serializable> persistentProperty : persistentProperties) {
					final SLLink linkProxy = ProxyUtil.createLinkProxy(
							getLinkType(), this);
					final SLLinkProperty<Serializable> property = new SLLinkPropertyImpl<Serializable>(
							linkProxy, persistentProperty);
					properties.add(property);
				}
				return properties;
			} catch (final Exception e) {
				throw new SLGraphSessionException(
						"Error on attempt to retrieve node properties.", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openspotlight.graph.SLLink#getProperty(java.lang.Class,
	 * java.lang.String)
	 */
	public <V extends Serializable> SLLinkProperty<V> getProperty(
			final Class<V> clazz, final String name)
			throws SLLinkPropertyNotFoundException,
			SLInvalidLinkPropertyTypeException, SLGraphSessionException {
		synchronized (lock) {
			return this.getProperty(clazz, name, null);
		}
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openspotlight.graph.SLLink#getProperty(java.lang.Class,
	 * java.lang.String, java.text.Collator)
	 */
	public <V extends Serializable> SLLinkProperty<V> getProperty(
			final Class<V> clazz, final String name, final Collator collator)
			throws SLLinkPropertyNotFoundException,
			SLInvalidLinkPropertyTypeException, SLGraphSessionException {
		synchronized (lock) {

			SLLinkProperty<V> property = null;

			try {

				final String propName = SLCommonSupport
						.toUserPropertyName(name);
				SLPersistentProperty<V> pProperty = SLCommonSupport
						.getProperty(linkNode, clazz, propName);

				// if property not found find collator if its strength is not
				// identical ...
				if (pProperty == null) {
					final Class<? extends SLLink> nodeType = getLinkType();
					if (nodeType != null) {
						final Set<SLPersistentProperty<Serializable>> pProperties = linkNode
								.getProperties(SLConsts.PROPERTY_PREFIX_USER
										+ ".*");
						for (final SLPersistentProperty<Serializable> current : pProperties) {
							final String currentName = SLCommonSupport
									.toSimplePropertyName(current.getName());
							final Collator currentCollator = collator == null ? SLCollatorSupport
									.getPropertyCollator(nodeType, currentName)
									: collator;
							if (currentCollator.compare(name, currentName) == 0) {
								pProperty = linkNode.getProperty(clazz, current
										.getName());
								break;
							}
						}
					}
				}

				if (pProperty != null) {
					final SLLink linkProxy = ProxyUtil.createLinkProxy(
							getLinkType(), this);
					property = new SLLinkPropertyImpl<V>(linkProxy, pProperty);
				}
			} catch (final SLInvalidPersistentPropertyTypeException e) {
				throw new SLInvalidNodePropertyTypeException(e);
			} catch (final Exception e) {
				throw new SLGraphSessionException(
						"Error on attempt to retrieve link property.", e);
			}

			if (property == null) {
				throw new SLNodePropertyNotFoundException(name);
			}
			return property;
		}
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openspotlight.graph.SLLink#getPropertyValueAsString(java.lang.String)
	 */
	public String getPropertyValueAsString(final String name)
			throws SLLinkPropertyNotFoundException, SLGraphSessionException {
		synchronized (lock) {
			return this.getProperty(Serializable.class, name).getValue()
					.toString();
		}
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openspotlight.graph.SLLink#getSession()
	 */
	public SLGraphSession getSession() {
		return session;
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openspotlight.graph.SLLink#getSides()
	 */
	public SLNode[] getSides() throws SLGraphSessionException {
		synchronized (lock) {
			try {
				final SLNode a = getANode();
				final SLNode b = getBNode();
				return new SLNode[] { a, b };
			} catch (final SLException e) {
				throw new SLGraphSessionException(
						"Error on attempt to retrieve link sides.", e);
			}
		}
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openspotlight.graph.SLLink#getSource()
	 */
	public SLNode getSource() throws SLGraphSessionException {
		synchronized (lock) {
			if (isBidirectional()) {
				// this method cannot be used on bidirecional links, because
				// source and targets are relatives.
				// on unidirecional links, source and target are well defined.
				throw new UnsupportedOperationException(
						"SLLink.getSource() cannot be used on bidirecional links.");
			}
			try {
				return getDirection() == SLConsts.DIRECTION_AB ? getANode()
						: getBNode();
			} catch (final SLException e) {
				throw new SLGraphSessionException(
						"Error on attempt to retrieve link source.", e);
			}
		}
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openspotlight.graph.SLLink#getTarget()
	 */
	public SLNode getTarget() throws SLGraphSessionException {
		synchronized (lock) {
			if (isBidirectional()) {
				// this method cannot be used on bidirecional links, because
				// source and targets are relatives.
				// on unidirecional links, source and target are well defined.
				throw new UnsupportedOperationException(
						"SLLink.getTarget() cannot be used on bidirecional links.");
			}
			try {
				return getDirection() == SLConsts.DIRECTION_AB ? getBNode()
						: getANode();
			} catch (final SLException e) {
				throw new SLGraphSessionException(
						"Error on attempt to retrieve link source.", e);
			}
		}
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		synchronized (lock) {
			try {
				return getID().hashCode();
			} catch (final SLGraphSessionException e) {
				throw new SLRuntimeException(
						"Error on attempt to execute SLLinkImpl.hasCode().", e);
			}
		}
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openspotlight.graph.SLLink#isBidirectional()
	 */
	public boolean isBidirectional() throws SLGraphSessionException {
		synchronized (lock) {
			try {
				return getDirection() == SLConsts.DIRECTION_BOTH;
			} catch (final SLPersistentTreeSessionException e) {
				Exceptions.catchAndLog(e);
				throw new SLGraphSessionException(
						"Error on attempt to verify if link is bidirectional.",
						e);
			}
		}
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openspotlight.graph.SLLink#remove()
	 */
	public void remove() throws SLGraphSessionException {
		synchronized (lock) {
			try {
				final SLLinkEvent event = new SLLinkEvent(
						SLLinkEvent.TYPE_LINK_REMOVED, this);
				event.setBidirectional(isBidirectional());
				if (event.isBidirectional()) {
					event.setSides(getSides());
				} else {
					event.setSource(getSource());
					event.setTarget(getTarget());
				}
				linkNode.remove();
				eventPoster.post(event);
			} catch (final SLException e) {
				throw new SLGraphSessionException(
						"Error on attempt to remove link.", e);
			}
		}
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openspotlight.graph.SLLink#setProperty(java.lang.Class,
	 * java.lang.String, java.io.Serializable)
	 */
	public <V extends Serializable> SLLinkProperty<V> setProperty(
			final Class<V> clazz, final String name, final V value)
			throws SLGraphSessionException {
		synchronized (lock) {
			try {
				final String propName = SLCommonSupport
						.toUserPropertyName(name);
				final SLPersistentProperty<V> pProperty = linkNode.setProperty(
						clazz, propName, value);
				final SLLink linkProxy = ProxyUtil.createLinkProxy(
						getLinkType(), this);
				final SLLinkProperty<V> property = new SLLinkPropertyImpl<V>(
						linkProxy, pProperty);
				final SLLinkPropertyEvent event = new SLLinkPropertyEvent(
						SLLinkPropertyEvent.TYPE_LINK_PROPERTY_SET, property,
						pProperty);
				eventPoster.post(event);
				return property;
			} catch (final Exception e) {
				throw new SLGraphSessionException(
						"Error on attempt to set link property.", e);
			}
		}
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		synchronized (lock) {
			return linkNode.toString();
		}
	}

}
