/*
 * The MIT License
 *
 * Copyright 2012 Antoine Gourlay <antoine@gourlay.fr>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
