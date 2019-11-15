/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2019, Open Source Geospatial Foundation (OSGeo)
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
 *
 */

package org.geotools.mbtiles;

import static org.geotools.jdbc.JDBCDataStoreFactory.DATASOURCE;
import static org.geotools.jdbc.JDBCDataStoreFactory.FETCHSIZE;
import static org.geotools.jdbc.JDBCDataStoreFactory.NAMESPACE;
import static org.geotools.jdbc.JDBCDataStoreFactory.PASSWD;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.Parameter;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.javax.SQLiteConnectionPoolDataSource;

public class MBTilesDataStoreFactory implements DataStoreFactorySpi {

    /** parameter for database type */
    public static final Param DBTYPE =
            new Param(
                    "dbtype",
                    String.class,
                    "Type",
                    true,
                    "mbtiles",
                    Collections.singletonMap(Parameter.LEVEL, "program"));

    /** optional user parameter */
    public static final Param USER =
            new Param(
                    JDBCDataStoreFactory.USER.key,
                    JDBCDataStoreFactory.USER.type,
                    JDBCDataStoreFactory.USER.description,
                    false,
                    JDBCDataStoreFactory.USER.sample);

    public static final Param DATABASE =
            new Param(
                    "database",
                    File.class,
                    "Database",
                    true,
                    null,
                    Collections.singletonMap(Param.EXT, "mbtiles"));

    @Override
    public String getDisplayName() {
        return "MBTiles";
    }

    @Override
    public String getDescription() {
        return getDisplayName();
    }

    @Override
    public Param[] getParametersInfo() {
        LinkedHashMap map = new LinkedHashMap();
        setupParameters(map);

        return (Param[]) map.values().toArray(new Param[map.size()]);
    }

    protected void setupParameters(Map parameters) {
        parameters.put(
                DBTYPE.key,
                new Param(
                        DBTYPE.key,
                        DBTYPE.type,
                        DBTYPE.description,
                        DBTYPE.required,
                        DBTYPE.getDefaultValue()));
        parameters.put(USER.key, USER);
        parameters.put(PASSWD.key, PASSWD);
        parameters.put(DATABASE.key, DATABASE);
        parameters.put(NAMESPACE.key, NAMESPACE);
        parameters.put(FETCHSIZE.key, FETCHSIZE);
    }

    @Override
    public boolean isAvailable() {
        try {
            // check if the sqlite-jdbc driver is in the classpath
            Class.forName("org.sqlite.JDBC");

            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
        return null;
    }

    @Override
    public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
        // datasource
        // check if the DATASOURCE parameter was supplied, it takes precendence
        DataSource ds = (DataSource) DATASOURCE.lookUp(params);
        if (ds == null) {
            ds = createDataSource(params);
        }
        MBTilesFile mbtiles = new MBTilesFile(ds);

        return new MBTilesDataStore(mbtiles);
    }

    /**
     * Same as the GeoPackage data store, if you modify this, probably want to check if
     * modifications make sense there too
     *
     * @param params
     */
    protected DataSource createDataSource(Map<String, Serializable> params) throws IOException {
        SQLiteConfig config = new SQLiteConfig();
        config.setSharedCache(true);
        config.enableLoadExtension(true);
        // support for encrypted databases has been ddded after 3.20.1, we'll have to
        // wait for a future release of sqlite-jdbc
        // if(password != null) {
        //     config.setPragma(SQLiteConfig.Pragma.PASSWORD, password);
        // }
        // TODO: add this and make configurable once we upgrade to a sqlitejdbc exposing mmap_size
        // config.setPragma(SQLiteConfig.Pragma.MMAP_SIZE, String.valueOf(1024 * 1024 * 1000));

        // use native "pool", which is actually not pooling anything (that's fast and
        // has less scalability overhead)
        SQLiteConnectionPoolDataSource ds = new SQLiteConnectionPoolDataSource(config);
        ds.setUrl(getJDBCUrl(params));

        return ds;
    }

    private String getJDBCUrl(Map params) throws IOException {
        File db = (File) DATABASE.lookUp(params);
        if (db.getPath().startsWith("file:")) {
            db = new File(db.getPath().substring(5));
        }
        return "jdbc:sqlite:" + db;
    }
}
