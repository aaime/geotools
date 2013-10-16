package org.geotools.gce.imagemosaic.catalog;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.geotools.data.DataUtilities;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.gce.imagemosaic.Utils;
import org.geotools.util.logging.Logging;
import org.opengis.filter.Filter;

public class FootprintProviderFactory {

    private final static Logger LOGGER = Logging.getLogger(FootprintProviderFactory.class);
    
    // well known properties
    private static final String SOURCE_PROPERTY = "footprint_source";
    private static final String FILTER_PROPERTY = "footprint_filter";

    // store types
    private static final String TYPE_SIDECAR = "sidecar";

    private FootprintProviderFactory() {
    }

    /**
     * Builds a footprint provider from mosaic location
     * 
     * @param mosaicFolder The folder that contains the mosaic config files
     * @return
     * @throws Exception
     */
    public static FootprintProvider createFootprintProvider(File mosaicFolder) {
        File configFile = new File(mosaicFolder, "footprints.properties");
        final Properties properties;
        if(configFile.exists()) {
            properties = Utils.loadPropertiesFromURL(DataUtilities.fileToURL(configFile));
        } else {
            properties = new Properties();
        }
        
        // load the type of config file
        String source = (String) properties.get(SOURCE_PROPERTY);
        if(source == null) {
            // see if we have the default whole mosaic footprint
            File defaultShapefileFootprint = new File(mosaicFolder, "footprints.shp");
            if(defaultShapefileFootprint.exists()) {
                return buildShapefileSource(mosaicFolder, defaultShapefileFootprint.getName(), properties);
            } else {
                return new SidecarFootprintProvider(mosaicFolder);
            }
        } else if(TYPE_SIDECAR.equals(source)) {
            return new SidecarFootprintProvider(mosaicFolder);
        } else if(source.toLowerCase().endsWith(".shp")) {
            return buildShapefileSource(mosaicFolder, source, properties);
        } else {
            throw new IllegalArgumentException("Invalid source type, it should be a reference "
                    + "to a shapefile or 'sidecar', but was '" + source + "' instead");
        }
            
    }


    private static FootprintProvider buildShapefileSource(File mosaicFolder, String location,
            Properties properties) {
        File shapefile = new File(location);
        if(!shapefile.isAbsolute()) {
            shapefile = new File(mosaicFolder, location);
        }
        
        try {
            if(!shapefile.exists()) {
                throw new IllegalArgumentException("Tried to load the footprints from " 
                        + shapefile.getCanonicalPath() + " but the file was not found");
            } else {
                final Map<String, Serializable> params = new HashMap<String, Serializable>();
                params.put("url", DataUtilities.fileToURL(shapefile));
                String cql = (String) properties.get(FILTER_PROPERTY);
                Filter filter = null;
                if(cql != null) {
                    filter = ECQL.toFilter(cql);
                } else {
                    filter = ECQL.toFilter("location = granule.location");
                }
                return new GTDataStoreFootprintProvider(params, null, filter);
            }
        } catch(Exception e) {
            throw new IllegalArgumentException("Failed to create a shapefile based footprint provider", e);
        }
    }
    
}
