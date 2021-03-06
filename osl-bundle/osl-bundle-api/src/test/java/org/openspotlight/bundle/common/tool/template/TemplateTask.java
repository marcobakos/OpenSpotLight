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
package org.openspotlight.bundle.common.tool.template;

import static java.text.MessageFormat.format;
import static org.openspotlight.common.util.Assertions.checkCondition;
import static org.openspotlight.common.util.Exceptions.logAndReturnNew;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dynamo.file.util.FileDescriptorManager.FileSavingListener;
import dynamo.file.vo.FileDescriptor;
import dynamo.runner.FileHelper;
import dynamo.runner.RunningParameters;

/**
 * This class is an {@link Task ant task} to read the XML File generated by and based on that file, newPair a result file. This
 * task should execute this script depending on the configuration.
 * 
 * @author Luiz Fernando Teston - feu.teston@caravelatech.com
 */
public class TemplateTask extends Task {

    /**
     * The log.
     */
    private final Logger      log           = LoggerFactory.getLogger(this.getClass());

    /**
     * The output directory.
     */
    private String            outputDirectory;

    /**
     * The script file location.
     */
    @SuppressWarnings("unused")
    private String            scriptFileLocation;

    /**
     * The template files.
     */
    private final Set<String> templateFiles = new HashSet<String>();

    /**
     * The template path.
     */
    private String            templatePath;

    /**
     * The xml files.
     */
    private final Set<String> xmlFiles      = new HashSet<String>();

    /**
     * Adds the template files.
     * 
     * @param templates the templates
     */
    public void addTemplateFiles(final String... templates) {
        for (final String template: templates) {
            templateFiles.add(template);
        }
    }

    /**
     * Adds the xml files.
     * 
     * @param artifactSet the artifact set
     */
    public void addXmlFiles(final FileSet artifactSet) {
        final DirectoryScanner scanner = artifactSet.getDirectoryScanner(getProject());
        for (final String activeFileName: scanner.getIncludedFiles()) {
            final File file = new File(artifactSet.getDir(getProject()), activeFileName);
            xmlFiles.add(file.getAbsolutePath());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute()
        throws BuildException {
        try {
            new File(outputDirectory).mkdirs();
            checkCondition("outputIsDir", new File(outputDirectory).isDirectory());
            checkCondition("templatePathIsDir", new File(templatePath).isDirectory());
            for (final String xmlFile: xmlFiles) {
                checkCondition("xmlFileExists", new File(xmlFile).isFile());
            }
            for (final String templateFile: templateFiles) {
                checkCondition("templateFileExists", new File(templatePath + templateFile).isFile());
            }

            final RunningParameters parameters = new RunningParameters();
            parameters.setInputTemplates(templateFiles.toArray(new String[] {}));
            parameters.setInputXmls(xmlFiles.toArray(new String[] {}));
            parameters.setOutputDir(outputDirectory);
            parameters.setTemplatePath(templatePath);
            final List<String> filesBeenSaved = new ArrayList<String>();
            final FileSavingListener listener = new FileSavingListener() {

                @Override
                public void fileSaved(final FileDescriptor descriptor) {
                    final String file = descriptor.getLocation() + "/" + descriptor.getName();
                    filesBeenSaved.add(file);
                }
            };
            parameters.addListener(listener);
            log.info(format("Executing TemplateTask with {0} as a templates and {1} as a xml file for filling the data",
                    templateFiles, xmlFiles));
            new FileHelper().generate(parameters);
            log.info("done!");
            if (filesBeenSaved.size() == 0) { throw new IllegalStateException("There's any file generated by this configuration"); }
            log.info("done all job!");
        } catch (final Exception e) {
            throw logAndReturnNew(e, BuildException.class);
        }

    }

    /**
     * Sets the output directory.
     * 
     * @param outputDirectory the new output directory
     */
    public void setOutputDirectory(final String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Sets the script file location.
     * 
     * @param scriptFileLocation the new script file location
     */
    public void setScriptFileLocation(final String scriptFileLocation) {
        this.scriptFileLocation = scriptFileLocation;
    }

    /**
     * Sets the template path.
     * 
     * @param templatePath the new template path
     */
    public void setTemplatePath(final String templatePath) {
        this.templatePath = templatePath;
    }

}
