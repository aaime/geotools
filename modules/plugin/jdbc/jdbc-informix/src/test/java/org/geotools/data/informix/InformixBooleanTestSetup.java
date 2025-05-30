/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2022, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.informix;

import org.geotools.jdbc.JDBCBooleanTestSetup;
import org.geotools.jdbc.JDBCDataStore;

public class InformixBooleanTestSetup extends JDBCBooleanTestSetup {

    protected InformixBooleanTestSetup() {
        super(new InformixTestSetup());
    }

    @Override
    protected void setUpDataStore(JDBCDataStore dataStore) {
        super.setUpDataStore(dataStore);
        dataStore.setDatabaseSchema(null);
    }

    @Override
    protected void createBooleanTable() throws Exception {
        run("CREATE TABLE b (id serial PRIMARY KEY, boolProperty BOOLEAN)");
        run("INSERT INTO b (boolProperty) VALUES ('F')");
        run("INSERT INTO b (boolProperty) VALUES ('T')");
    }

    @Override
    protected void dropBooleanTable() throws Exception {
        run("DROP TABLE b");
    }
}
