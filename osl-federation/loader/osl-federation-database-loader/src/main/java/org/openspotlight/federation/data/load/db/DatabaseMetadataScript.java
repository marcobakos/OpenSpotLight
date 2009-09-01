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

package org.openspotlight.federation.data.load.db;

import org.openspotlight.federation.data.impl.DatabaseType;

/**
 * Pojo class to store the script to get database metadata for a database type.
 * This class should be getter by {@link DatabaseMetadataScriptManager}.
 * 
 * @author Luiz Fernando Teston - feu.teston@caravelatech.com
 * 
 */
public final class DatabaseMetadataScript {

	public static enum PreferedType {
		TEMPLATE, SQL
	}

	private PreferedType preferedType;
	private boolean immutable = false;
	private ScriptType scriptType;
	private DatabaseType database;
	private String contentSelect;
	private String dataSelect;
	private String template;
	private String templatesSelect;

	public ScriptType getScriptType() {
		return this.scriptType;
	}

	public void setScriptType(ScriptType scriptType) {
		if (this.immutable) {
			throw new UnsupportedOperationException();
		}
		this.scriptType = scriptType;
	}

	public DatabaseType getDatabase() {
		return this.database;
	}

	public void setDatabase(DatabaseType database) {
		if (this.immutable) {
			throw new UnsupportedOperationException();
		}
		this.database = database;
	}

	public String getContentSelect() {
		return this.contentSelect;
	}

	public void setContentSelect(String contentSelect) {
		if (this.immutable) {
			throw new UnsupportedOperationException();
		}
		this.contentSelect = contentSelect;
	}

	public String getDataSelect() {
		return this.dataSelect;
	}

	public void setDataSelect(String dataSelect) {
		if (this.immutable) {
			throw new UnsupportedOperationException();
		}
		this.dataSelect = dataSelect;
	}

	void setImmutable() {
		if (!this.immutable) {
			this.immutable = true;
		}
	}

	public PreferedType getPreferedType() {
		return this.preferedType;
	}

	public void setPreferedType(PreferedType preferedType) {
		this.preferedType = preferedType;
	}

	public String getTemplate() {
		return this.template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getTemplatesSelect() {
		return this.templatesSelect;
	}

	public void setTemplatesSelect(String templatesSelect) {
		this.templatesSelect = templatesSelect;
	}

}
