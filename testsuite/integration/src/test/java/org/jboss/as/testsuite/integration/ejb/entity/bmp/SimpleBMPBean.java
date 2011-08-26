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
package org.jboss.as.testsuite.integration.ejb.entity.bmp;

import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Stuart Douglas
 */
public class SimpleBMPBean implements EntityBean {

    private static final AtomicInteger ID = new AtomicInteger();
    
    private String myField;
    private EntityContext entityContext;
    private Integer primaryKey;
    private boolean ejbPostCreateCalled;


    public Integer ejbCreateEmpty() {
        primaryKey = ID.incrementAndGet();
        return primaryKey;
    }

    public Integer ejbCreateWithValue(String value) {
        primaryKey = ID.incrementAndGet();
        myField = value;
        return primaryKey;
    }

    public void ejbPostCreateEmpty() {
        ejbPostCreateCalled = true;
    }

    public void ejbPostCreateWithValue(String value) {
        ejbPostCreateCalled = true;
    }

    public int exampleHomeMethod() {
        return 100;
    }


    @Override
    public void setEntityContext(final EntityContext ctx) throws EJBException, RemoteException {
        this.entityContext = ctx;
    }

    @Override
    public void unsetEntityContext() throws EJBException, RemoteException {
        this.entityContext = null;
    }

    @Override
    public void ejbRemove() throws RemoveException, EJBException, RemoteException {
        DataStore.DATA.remove(primaryKey);
    }

    @Override
    public void ejbActivate() throws EJBException, RemoteException {

    }

    @Override
    public void ejbPassivate() throws EJBException, RemoteException {

    }

    @Override
    public void ejbLoad() throws EJBException, RemoteException {
        this.myField = DataStore.DATA.get(primaryKey);
    }

    @Override
    public void ejbStore() throws EJBException, RemoteException {
        DataStore.DATA.put(primaryKey, myField);
    }

    public String getMyField() {
        return myField;
    }

    public void setMyField(final String myField) {
        this.myField = myField;
    }

    public boolean isEjbPostCreateCalled() {
        return ejbPostCreateCalled;
    }
}
