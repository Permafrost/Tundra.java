/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Lachlan Dowding
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package permafrost.tundra.server.service;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.ns.Namespace;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.lang.ns.NSName;
import permafrost.tundra.data.IDataCursorHelper;
import permafrost.tundra.lang.StartableManager;

/**
 * Manages redirecting services from source to target implementation.
 */
public class RedirectServiceManager extends StartableManager<NSName, RedirectService> {
    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final RedirectServiceManager INSTANCE = new RedirectServiceManager();
    }

    /**
     * Returns the singleton instances of this class.
     *
     * @return the singleton instances of this class.
     */
    public static RedirectServiceManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Disallow instantiation of this class.
     */
    private RedirectServiceManager() {
        super(true);
    }

    /**
     * Redirects the implementation of the given source service to use the given target service.
     *
     * @param serviceRedirect   The service redirect to be registered.
     */
    public synchronized void register(IData serviceRedirect) {
        if (serviceRedirect != null) {
            IDataCursor cursor = serviceRedirect.getCursor();
            try {
                String sourceService = IDataCursorHelper.get(cursor, String.class, "source");
                String targetService = IDataCursorHelper.get(cursor, String.class, "target");

                IData signature = IDataCursorHelper.get(cursor, IData.class, "signature");
                IData[] inputSignature = null, outputSignature = null;
                if (signature != null) {
                    IDataCursor signatureCursor = signature.getCursor();
                    try {
                        inputSignature = IDataCursorHelper.get(signatureCursor, IData[].class, "input");
                        outputSignature = IDataCursorHelper.get(signatureCursor, IData[].class, "output");
                    } finally {
                        signatureCursor.destroy();
                    }
                }

                IData pipeline = IDataCursorHelper.get(cursor, IData.class, "pipeline");
                IData inputPipeline = null, outputPipeline = null;
                if (pipeline != null) {
                    IDataCursor pipelineCursor = pipeline.getCursor();
                    try {
                        inputPipeline = IDataCursorHelper.get(pipelineCursor, IData.class, "input");
                        outputPipeline = IDataCursorHelper.get(pipelineCursor, IData.class, "output");
                    } finally {
                        pipelineCursor.destroy();
                    }
                }

                register(sourceService, targetService, inputSignature, outputSignature, inputPipeline, outputPipeline);
            } finally {
                cursor.destroy();
            }
        }
    }

    /**
     * Redirects the implementation of the given source service to use the given target service.
     *
     * @param sourceService     The service to be redirected.
     * @param targetService     The service to redirect to.
     * @param inputSignature    The input signature redirect to use.
     * @param outputSignature   The output signature redirect to use.
     * @param inputPipeline     The input pipeline to use when invoking the target service.
     * @param outputPipeline    The output pipeline to merge with the results returned by the target service.
     */
    protected synchronized void register(String sourceService, String targetService, IData[] inputSignature, IData[] outputSignature, IData inputPipeline, IData outputPipeline) {
        if (sourceService == null) throw new NullPointerException("source service must not be null");
        register(NSName.create(sourceService), NSName.create(targetService), inputSignature, outputSignature, inputPipeline, outputPipeline);
    }

    /**
     * Redirects the implementation of the given source service to use the given target service.
     *
     * @param sourceService     The service to be redirected.
     * @param targetService     The service to redirect to.
     * @param inputSignature    The input signature redirect to use.
     * @param outputSignature   The output signature redirect to use.
     * @param inputPipeline     The input pipeline to use when invoking the target service.
     * @param outputPipeline    The output pipeline to merge with the results returned by the target service.
     */
    protected synchronized void register(NSName sourceService, NSName targetService, IData[] inputSignature, IData[] outputSignature, IData inputPipeline, IData outputPipeline) {
        BaseService baseTargetService;
        if (targetService == null) {
            baseTargetService = null;
        } else {
            baseTargetService = Namespace.getService(targetService);
            if (baseTargetService == null) {
                throw new NullPointerException("service does not exist: " + targetService.getFullName());
            }
        }
        register(sourceService, baseTargetService, inputSignature, outputSignature, inputPipeline, outputPipeline);
    }

    /**
     * Redirects the implementation of the given source service to use the given target service.
     *
     * @param sourceService     The service to be redirected.
     * @param targetService     The service to redirect to.
     * @param inputSignature    The input signature redirect to use.
     * @param outputSignature   The output signature redirect to use.
     * @param inputPipeline     The input pipeline to use when invoking the target service.
     * @param outputPipeline    The output pipeline to merge with the results returned by the target service.
     */
    protected synchronized void register(NSName sourceService, BaseService targetService, IData[] inputSignature, IData[] outputSignature, IData inputPipeline, IData outputPipeline) {
        RedirectService redirect = new RedirectService(sourceService, targetService, inputSignature, outputSignature, inputPipeline, outputPipeline);
        try {
            RedirectService existingRedirect = REGISTRY.remove(sourceService);
            if (existingRedirect != null) {
                existingRedirect.stop();
            }
        } finally {
            register(sourceService, redirect);
        }
    }
}
