/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.extension.undertow.deployment;

import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.modules.AbstractResourceLoader;
import org.jboss.modules.ClassSpec;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.filter.PathFilters;
import org.wildfly.extension.undertow.logging.UndertowLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

/**
 * Module dependencies processor.
 *
 * @author Emanuel Muckenhuber
 * @author Stan Silvert
 */
public class UndertowDependencyProcessor implements DeploymentUnitProcessor {

    private static final ModuleIdentifier JSTL = ModuleIdentifier.create("javax.servlet.jstl.api");

    private static final ModuleIdentifier UNDERTOW_CORE = ModuleIdentifier.create("io.undertow.core");
    private static final ModuleIdentifier UNDERTOW_SERVLET = ModuleIdentifier.create("io.undertow.servlet");
    private static final ModuleIdentifier UNDERTOW_JSP = ModuleIdentifier.create("io.undertow.jsp");
    private static final ModuleIdentifier UNDERTOW_WEBSOCKET = ModuleIdentifier.create("io.undertow.websocket");
    private static final ModuleIdentifier UNDERTOW_JS = ModuleIdentifier.create("io.undertow.js");
    private static final ModuleIdentifier CLUSTERING_API = ModuleIdentifier.create("org.wildfly.clustering.web.api");

    private static final ModuleIdentifier SERVLET_API = ModuleIdentifier.create("javax.servlet.api");
    private static final ModuleIdentifier JSP_API = ModuleIdentifier.create("javax.servlet.jsp.api");
    private static final ModuleIdentifier WEBSOCKET_API = ModuleIdentifier.create("javax.websocket.api");

    static {
        Module module = Module.forClass(UndertowDependencyProcessor.class);
        if (module != null) {
            //When testing the subsystems we are running in a non-modular environment
            //so module will be null. Having a null entry kills ModularURLStreamHandlerFactory
            Module.registerURLStreamHandlerFactoryModule(module);
        }
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

        final ModuleSpecification moduleSpecification = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
        final ModuleLoader moduleLoader = Module.getBootModuleLoader();

        //add the api classes for every deployment
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, SERVLET_API, false, false, true, false));
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, JSP_API, false, false, true, false));
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, WEBSOCKET_API, false, false, true, false));
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, JSTL, false, false, false, false));

        if (!DeploymentTypeMarker.isType(DeploymentType.WAR, deploymentUnit)) {
            return; // Skip non web deployments
        }

        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, UNDERTOW_CORE, false, false, true, false));
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, UNDERTOW_SERVLET, false, false, true, false));
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, UNDERTOW_JSP, false, false, true, false));
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, UNDERTOW_WEBSOCKET, false, false, true, false));
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, CLUSTERING_API, true, false, false, false));
        try {
            final Module undertowJs = moduleLoader.loadModule(UNDERTOW_JS);
            ModuleDependency dependency = new ModuleDependency(moduleLoader, UNDERTOW_JS, true, false, true, false);
            dependency.addImportFilter(PathFilters.match("io/undertow/js/templates/freemarker"), false);
            dependency.addImportFilter(PathFilters.match("io/undertow/js/templates/freemarker/**"), false);
            moduleSpecification.addResourceLoader(ResourceLoaderSpec.createResourceLoaderSpec(new AbstractResourceLoader() {
                @Override
                public ClassSpec getClassSpec(String fileName) throws IOException {

                    try (final InputStream is = undertowJs.getClassLoader().getResourceAsStream(fileName)) {
                        if (is == null) {
                            return null;
                        }
                        final ClassSpec spec = new ClassSpec();
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        final byte[] buf = new byte[16384];
                        int res;
                        while ((res = is.read(buf)) > 0) {
                            baos.write(buf, 0, res);
                        }
                        // done
                        baos.close();
                        is.close();
                        spec.setBytes(baos.toByteArray());
                        return spec;
                    }
                }

                @Override
                public Collection<String> getPaths() {
                    return Collections.singleton("io/undertow/js/templates/freemarker");
                }
            }, PathFilters.match("io/undertow/js/templates/freemarker")));
            moduleSpecification.addSystemDependency(dependency);
        } catch (ModuleLoadException e) {
            UndertowLogger.ROOT_LOGGER.debug("Undertow.js not available", e);
        }
    }

    @Override
    public void undeploy(final DeploymentUnit context) {
    }
}
