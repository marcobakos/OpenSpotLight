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
package org.openspotlight.tool.dap.language.java.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.objectweb.asm.ClassReader;
import org.openspotlight.tool.dap.language.java.asm.model.JavaType;
import org.openspotlight.tool.dap.language.java.asm.model.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;

public class ASMExportTask extends Task {

    private Logger         LOG               = LoggerFactory.getLogger(ASMExportTask.class);
    private Set<File>      compiledArtifacts = new HashSet<File>();
    private ASMVisitor     asmVisitor        = new ASMVisitor();
    private List<JavaType> scannedTypes      = new LinkedList<JavaType>();
    private boolean        serializeXML      = false;
    private String         xmlOutputFileName = "";

    public void setSerializeXML( boolean serializeXML ) {
        this.serializeXML = serializeXML;
    }

    public void setXmlOutputFileName( String xmlOutputFileName ) {
        this.xmlOutputFileName = xmlOutputFileName;
    }

    public void addCompiledArtifacts( FileSet artifactSet ) {
        DirectoryScanner scanner = artifactSet.getDirectoryScanner(getProject());
        for (String activeFileName : scanner.getIncludedFiles()) {
            File file = new File(artifactSet.getDir(getProject()), activeFileName);
            compiledArtifacts.add(file);
        }
    }

    public void execute() {
        if (isValid()) {
            int count = 0;
            try {
                for (File activeArtifact : compiledArtifacts) {
                    if (activeArtifact.getName().endsWith(".jar")) {
                        LOG.info("Parsing Jar Artifact \"" + activeArtifact.getCanonicalPath() + "\"");
                        // Open Zip file for reading
                        ZipFile zipFile = new ZipFile(activeArtifact, ZipFile.OPEN_READ);

                        // Create an enumeration of the entries in the zip file
                        Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();

                        // Process each entry
                        while (zipFileEntries.hasMoreElements()) {
                            // grab a zip file entry
                            ZipEntry entry = zipFileEntries.nextElement();
                            // extract file if not a directory
                            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                                ClassReader reader = new ClassReader(zipFile.getInputStream(entry));
                                reader.accept(asmVisitor, 0);
                                count++;
                                scannedTypes.add(asmVisitor.getType());
                            }
                        }
                        zipFile.close();
                    } else if (activeArtifact.getName().endsWith(".class")) {
                        LOG.info("Parsing Class Artifact \"" + activeArtifact.getCanonicalPath() + "\"");
                        ClassReader reader = new ClassReader(new FileInputStream(activeArtifact));
                        reader.accept(asmVisitor, 0);
                        count++;
                        scannedTypes.add(asmVisitor.getType());
                    }
                }

                System.out.println("ASMExporter: Finishing processing " + count + " types.");
                LOG.info("ASMExporter: Finishing processing " + count + " types.");

                if (serializeXML) {
                    System.out.println("ASMExporter: Starting generation XML output file.");
                    LOG.info("ASMExporter: Starting generation XML output file.");
                    XStream xstream = new XStream();
                    xstream.aliasPackage("", "org.openspotlight.tool.dap.language.java.asm.model");
                    xstream.registerConverter(new JavaBeanConverter(xstream.getMapper()) {
                        @SuppressWarnings( "unchecked" )
                        public boolean canConvert( Class type ) {
                            return type.getName() == MethodDeclaration.class.getName();
                        }
                    });

                    if (this.xmlOutputFileName != null && this.xmlOutputFileName.indexOf("/") != -1) {
                        final String dir = this.xmlOutputFileName.substring(0, this.xmlOutputFileName.lastIndexOf("/"));
                        new File(dir).mkdirs();
                    }

                    xstream.toXML(scannedTypes, new FileOutputStream(xmlOutputFileName));
                    System.out.println("ASMExporter: Finished XML output file.");
                    LOG.info("ASMExporter: Finished XML output file.");
                }
            } catch (Exception ex) {
                StringWriter sWriter = new StringWriter();
                ex.printStackTrace(new PrintWriter(sWriter));
                LOG.error("Problems during parser - Stack trace:\n" + sWriter.getBuffer().toString());
            }
        } else {
            System.out.println("ASMExporter ERROR - Invalid State: Missing XmlOutputFileName.");
            LOG.error("ASMExporter ERROR - Invalid State: Missing XmlOutputFileName.");
        }
    }

    public List<JavaType> getScannedTypes() {
        return scannedTypes;
    }

    private boolean isValid() {
        if (serializeXML == true && (xmlOutputFileName == null || xmlOutputFileName.trim().length() == 0)) {
            return false;
        }
        return true;
    }
}