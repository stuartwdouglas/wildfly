/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.as.web.deployment;

import org.jboss.as.server.suspend.SuspendManager;
import org.jboss.as.server.suspend.SuspendPermitManager;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * Class that is responsible for gracefully suspending web deployments.
 *
 * @author Stuart Douglas
 */
public class WebSuspendManagerService implements Service<WebSuspendManagerService> {

    public static final ServiceName SERVICE_NAME = ServiceName.of("webSuspendManager");

    private final String deploymentName;

    private final InjectedValue<SuspendManager> suspendManager = new InjectedValue<SuspendManager>();

    private volatile SuspendPermitManager suspendPermitManager;

    public WebSuspendManagerService(final String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public InjectedValue<SuspendManager> getSuspendManager() {
        return suspendManager;
    }

    @Override
    public synchronized void start(final StartContext context) throws StartException {
        suspendPermitManager = new SuspendPermitManager(deploymentName + "-web");
        suspendManager.getValue().addPermitManager(suspendPermitManager);
    }

    @Override
    public synchronized void stop(final StopContext context) {
        suspendManager.getValue().removePermitManager(suspendPermitManager);
    }

    @Override
    public synchronized  WebSuspendManagerService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public SuspendPermitManager getSuspendPermitManager() {
        return suspendPermitManager;
    }
}
