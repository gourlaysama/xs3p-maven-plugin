/*
 * The DSTC Public License (DPL)
 *
 * Copyright (c) 2012 Antoine Gourlay <antoine@gourlay.fr>
 *
 * The contents of this file are subject to the DSTC Public License Version 
 * 1.1 (the 'License'); you may not use this file except in compliance with 
 * the License.
 * 
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the 
 * License.
 */
package net.xs3p;

import java.io.File;
import java.util.Collection;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which touches a timestamp file.
 *
 * @goal xs3p
 *
 * @author Antoine Gourlay <antoine@gourlay.fr>
 */
public class Xs3jMojo
        extends AbstractMojo {

        /**
         * Location of the output directory.
         * @parameter expression="${project.build.directory}"
         * @required
         */
        private File outputDirectory;
        /**
         * Location of the xsd sources.
         * @parameter default-value="src/main/resources/"
         */
        private File xsdSources;

        public void execute() throws MojoExecutionException, MojoFailureException {
                final String xsdSrcPath = xsdSources.getAbsolutePath();

                getLog().info(String.format("Processing folder '%s'", xsdSrcPath));

                File targetDirectory = new File(outputDirectory, "schema-doc");

                if (targetDirectory.exists()) {
                        FileUtils.deleteQuietly(targetDirectory);
                        targetDirectory.mkdir();
                } else {
                        targetDirectory.mkdirs();
                }


                Collection<File> fil = FileUtils.listFiles(xsdSources, new String[]{"xsd"}, true);
                getLog().info(String.format("Found %d xsd files.", fil.size()));

                TransformerFactory fac = TransformerFactory.newInstance();
                Transformer tr = null;
                try {
                        tr = fac.newTransformer(new StreamSource(Xs3jMojo.class.getResourceAsStream("xs3p.xsl")));
                } catch (TransformerConfigurationException ex) {
                        throw new MojoExecutionException("Failed to initialize the XSLT Processor.", ex);
                }

                for (File ff : fil) {
                        getLog().info(String.format("Processing schema '%s'", ff.getAbsolutePath()));

                        String oldpath = ff.getParentFile().getAbsolutePath();
                        String localpath = oldpath.substring(xsdSrcPath.length());

                        File targetDir2 = new File(targetDirectory, localpath);
                        targetDir2.mkdirs();

                        File targetFile = new File(targetDir2, ff.getName() + ".html");
                        getLog().info(String.format("Generating '%s'", targetFile.getAbsolutePath()));

                        try {
                                tr.transform(new StreamSource(ff), new StreamResult(targetFile));
                        } catch (TransformerException transformerException) {
                                throw new MojoFailureException("Failed to generate documentation for '"
                                        + ff.getName() + "'.", transformerException);
                        }
                }

                getLog().info(String.format("Generated %d files in folder '%s'.", fil.size(), targetDirectory.getAbsolutePath()));
        }
}
