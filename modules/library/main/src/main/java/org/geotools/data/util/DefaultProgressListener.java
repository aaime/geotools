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
 */
package org.geotools.data.util;

import java.util.LinkedList;
import java.util.Queue;
import org.geotools.api.util.InternationalString;

/**
 * Default Implementation of {@link org.geotools.api.util.ProgressListener} that does retain exceptions.
 *
 * <p>We do not put particular attention on the management of canceled, started, completed, this is a default
 * implementation.
 *
 * @author Simone Giannecchini, GeoSolutions SAS
 * @since 2.8
 */
public class DefaultProgressListener extends NullProgressListener implements org.geotools.api.util.ProgressListener {

    @Override
    public String toString() {
        return "DefaultProgressListener [completed="
                + completed
                + ", progress="
                + progress
                + ", started="
                + started
                + ", task="
                + task
                + "]";
    }

    /**
     * Collector class for warnings.
     *
     * @author Simone Giannecchini, GeoSolutions SAS
     * @since 2.8
     */
    public static class Warning {
        @Override
        public String toString() {
            return "Warning [margin=" + margin + ", source=" + source + ", warning=" + warning + "]";
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getMargin() {
            return margin;
        }

        public void setMargin(String margin) {
            this.margin = margin;
        }

        public String getWarning() {
            return warning;
        }

        public void setWarning(String warning) {
            this.warning = warning;
        }

        private String source;
        private String margin;
        private String warning;
    }

    /** List of warnings occurred during the execution.* */
    private final Queue<Warning> warnings = new LinkedList<>();

    /** List of exceptions that were caught during executiong.* */
    private final Queue<Throwable> exceptionQueue = new LinkedList<>();

    /** IS the task we are listening for completed?.* */
    private boolean completed;

    /** What is the progress of the task we are listening for?.* */
    private float progress;

    /** Identifier of the task we are listening for.* */
    private InternationalString task;

    /** Has the task we are listening for started?* */
    private boolean started;

    /* (non-Javadoc)
     * @see org.geotools.util.ProgressListener#complete()
     */
    @Override
    public void complete() {
        this.completed = true;
    }

    /* (non-Javadoc)
     * @see org.geotools.util.ProgressListener#dispose()
     */
    @Override
    public void dispose() {
        exceptionQueue.clear();
        warnings.clear();
    }

    /* (non-Javadoc)
     * @see org.geotools.util.ProgressListener#exceptionOccurred(java.lang.Throwable)
     */
    @Override
    public void exceptionOccurred(Throwable exception) {
        this.exceptionQueue.add(exception);
    }

    /* (non-Javadoc)
     * @see org.geotools.util.ProgressListener#progress(float)
     */
    @Override
    public void progress(float percent) {
        this.progress = percent;
    }

    /* (non-Javadoc)
     * @see org.geotools.util.ProgressListener#started()
     */
    @Override
    public void started() {
        this.started = true;
    }

    /* (non-Javadoc)
     * @see org.geotools.util.ProgressListener#warningOccurred(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void warningOccurred(String source, String margin, String warning) {
        final Warning w = new Warning();
        w.setMargin(margin);
        w.setSource(source);
        w.setWarning(warning);
        warnings.add(w);
    }

    /* (non-Javadoc)
     * @see org.geotools.api.util.ProgressListener#getProgress()
     */
    @Override
    public float getProgress() {
        return progress;
    }

    /* (non-Javadoc)
     * @see org.geotools.api.util.ProgressListener#getTask()
     */
    @Override
    public InternationalString getTask() {
        return task;
    }

    /* (non-Javadoc)
     * @see org.geotools.api.util.ProgressListener#setTask(org.geotools.api.util.InternationalString)
     */
    @Override
    public void setTask(InternationalString task) {
        this.task = task;
    }

    /**
     * Is the task we are listening is completed.
     *
     * @return <code>true</code> if the task is completed, <code>false</code> if it is not.
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Return a copy of the {@link Queue} of exceptions that had happened.
     *
     * @return a copy of the {@link Queue} of exceptions that had happened.
     */
    public Queue<Throwable> getExceptions() {
        return new LinkedList<>(exceptionQueue);
    }

    /**
     * It tells us if we have exceptions or not.
     *
     * @return <code>true</code> if there are exceptions, <code>false</code> otherwise.
     */
    public boolean hasExceptions() {
        return !exceptionQueue.isEmpty();
    }

    /**
     * Is the task we are listening for started.
     *
     * @return <code>true</code> if the task is started, <code>false</code> if it is not.
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Retrieves a copy of the warnings occurred.
     *
     * @return a copy of the warnings occurred.
     */
    public Queue<Warning> getWarnings() {
        return new LinkedList<>(warnings);
    }
}
