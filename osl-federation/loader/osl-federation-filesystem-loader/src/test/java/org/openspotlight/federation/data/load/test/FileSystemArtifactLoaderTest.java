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

package org.openspotlight.federation.data.load.test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.openspotlight.federation.data.load.FileSystemArtifactLoader;
import org.openspotlight.federation.domain.ArtifactMapping;
import org.openspotlight.federation.domain.ArtifactSource;
import org.openspotlight.federation.domain.StreamArtifact;

/**
 * Test for class {@link FileSystemArtifactLoader}
 * 
 * @author Luiz Fernando Teston - feu.teston@caravelatech.com
 */
@SuppressWarnings( "all" )
public class FileSystemArtifactLoaderTest extends AbstractArtifactLoaderTest {

    @Override
    @Before
    public void createArtifactLoader() {
        this.artifactLoader = new FileSystemArtifactLoader();
    }

    @Override
    @Before
    public void createConfiguration() throws Exception {
        this.configuration = new Configuration();
        final Repository repository = new Repository(this.configuration, this.REPOSITORY_NAME);
        this.configuration.setNumberOfParallelThreads(4);
        final Group project = new Group(repository, this.PROJECT_NAME);
        final ArtifactSource bundle = new ArtifactSource(project, this.BUNDLE_NAME);
        final String basePath = new File("../../../").getCanonicalPath() + "/";
        bundle.setInitialLookup(basePath);
        final ArtifactMapping artifactMapping = new ArtifactMapping(bundle,
                                                                    "osl-federation/loader/osl-federation-filesystem-loader/");
        new Included(artifactMapping, "src/main/java/**/*.java");
    }

    public ArtifactSource createConfigurationForChangeListen() throws Exception {
        this.configuration = new Configuration();
        final Repository repository = new Repository(this.configuration, "Local target folder");
        this.configuration.setNumberOfParallelThreads(4);
        final Group project = new Group(repository, "Osl Federation");
        final ArtifactSource bundle = new ArtifactSource(project, "Target folder");
        final String basePath = new File("../../../").getCanonicalPath() + "/";
        bundle.setInitialLookup(basePath);
        final ArtifactMapping artifactMapping = new ArtifactMapping(bundle,
                                                                    "osl-federation/loader/osl-federation-filesystem-loader/");
        new Included(artifactMapping, "target/test-data/FileSystemArtifactLoaderTest/*");
        return bundle;
    }

    @Test
    public void shouldListenChanges() throws Exception {
        new File("target/test-data/FileSystemArtifactLoaderTest/").mkdirs();
        final File textFile = new File("target/test-data/FileSystemArtifactLoaderTest/willBeChanged");
        FileOutputStream fos = new FileOutputStream(textFile);
        fos.write("new text content".getBytes());
        fos.flush();
        fos.close();

        final ArtifactSource bundle = this.createConfigurationForChangeListen();
        final SharedData sharedData = bundle.getInstanceMetadata().getSharedData();
        this.artifactLoader.loadArtifactsFromMappings(bundle);
        sharedData.markAsSaved();

        fos = new FileOutputStream(textFile);
        fos.write("changed text content".getBytes());
        fos.flush();
        fos.close();
        this.artifactLoader.loadArtifactsFromMappings(bundle);

        assertThat(sharedData.getDirtyNodes().size(), is(1));
        assertThat(sharedData.getNodeChangesSinceLastSave().size(), is(1));
        assertThat(sharedData.getNodeChangesSinceLastSave().get(0).getType(), is(ItemChangeType.CHANGED));
        textFile.delete();
    }

    @Test
    public void shouldListenExclusions() throws Exception {
        new File("target/test-data/FileSystemArtifactLoaderTest/").mkdirs();
        final File textFile = new File("target/test-data/FileSystemArtifactLoaderTest/willBeExcluded");
        final FileOutputStream fos = new FileOutputStream(textFile);
        fos.write("new text content".getBytes());
        fos.flush();
        fos.close();

        final ArtifactSource bundle = this.createConfigurationForChangeListen();
        final SharedData sharedData = bundle.getInstanceMetadata().getSharedData();
        this.artifactLoader.loadArtifactsFromMappings(bundle);
        sharedData.markAsSaved();

        assertThat(textFile.delete(), is(true));
        this.artifactLoader.loadArtifactsFromMappings(bundle);

        assertThat(sharedData.getDirtyNodes().size(), is(1));
        assertThat(sharedData.getNodeChangesSinceLastSave().size(), is(1));
        assertThat(sharedData.getNodeChangesSinceLastSave().get(0).getType(), is(ItemChangeType.CHANGED));
        final StreamArtifact changed = (StreamArtifact)sharedData.getDirtyNodes().iterator().next();
        assertThat(changed.getStatus(), is(Status.EXCLUDED));
    }

    @Test
    public void shouldListenInclusions() throws Exception {
        new File("target/test-data/FileSystemArtifactLoaderTest/").mkdirs();

        final ArtifactSource bundle = this.createConfigurationForChangeListen();
        final SharedData sharedData = bundle.getInstanceMetadata().getSharedData();
        new File("target/test-data/FileSystemArtifactLoaderTest/newTextFile").delete();

        this.artifactLoader.loadArtifactsFromMappings(bundle);
        sharedData.markAsSaved();
        final File textFile = new File("target/test-data/FileSystemArtifactLoaderTest/newTextFile");
        final FileOutputStream fos = new FileOutputStream(textFile);
        fos.write("new text content".getBytes());
        fos.flush();
        fos.close();
        this.artifactLoader.loadArtifactsFromMappings(bundle);

        final StreamArtifact sa = (StreamArtifact)sharedData.getDirtyNodes().iterator().next();

        for (final ItemChangeEvent<ConfigurationNode> change : sharedData.getNodeChangesSinceLastSave()) {
            System.out.println(change.getType() + " " + change.getNewItem());
        }
        assertThat(sharedData.getNodeChangesSinceLastSave().get(0).getType(), is(ItemChangeType.ADDED));
        assertThat(sharedData.getDirtyNodes().size(), is(1));
        assertThat(sharedData.getNodeChangesSinceLastSave().size(), is(1));

    }

}
