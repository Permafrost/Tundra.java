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
import com.wm.data.IDataFactory;
import com.wm.lang.ns.NSException;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSNode;
import com.wm.lang.ns.NSServiceType;
import com.wm.util.Values;
import com.wm.util.coder.IDataCodable;
import permafrost.tundra.data.IDataCursorHelper;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.lang.Startable;
import permafrost.tundra.server.NodeHelper;
import permafrost.tundra.server.PackageHelper;
import permafrost.tundra.flow.InputOutputSignature;

/**
 * Redirects the implementation of a source service to the target service.
 */
class RedirectService extends BaseService implements Startable, IDataCodable {
    /**
     * Whether the object is started or not.
     */
    protected volatile boolean started = false;
    /**
     * The node previously registered in the current namespace against the source service, if any.
     */
    protected NSNode originalNode;
    /**
     * The source service to be redirected.
     */
    protected NSName sourceService;
    /**
     * The target service to redirect to.
     */
    protected BaseService targetService;
    /**
     * The input pipeline parameter redirections.
     */
    protected IData[] inputSignature;
    /**
     * The output pipeline parameter redirections.
     */
    protected IData[] outputSignature;
    /**
     * The input pipeline merged into the current pipeline when invoking the target service.
     */
    protected IData inputPipeline;
    /**
     * The output pipeline merged into the current pipeline after the target service has returned.
     */
    protected IData outputPipeline;

    /**
     * Constructs a new RedirectService object.
     *
     * @param sourceService     The service to be redirected.
     * @param targetService     The service to redirect to.
     * @param inputSignature    The input signature redirect to use.
     * @param outputSignature   The output signature redirect to use.
     * @param inputPipeline     The input pipeline to use when invoking the target service.
     * @param outputPipeline    The output pipeline to merge with the results returned by the target service.
     */
    public RedirectService(NSName sourceService, BaseService targetService, IData[] inputSignature, IData[] outputSignature, IData inputPipeline, IData outputPipeline) {
        super(PackageHelper.getPackage("Tundra"), sourceService, NSServiceType.create("unknown", "unknown"));

        this.sourceService = sourceService;
        this.targetService = targetService;
        this.inputSignature = IDataHelper.duplicate(inputSignature, true);
        this.outputSignature = IDataHelper.duplicate(outputSignature, true);
        this.inputPipeline = IDataHelper.duplicate(inputPipeline, true);
        this.outputPipeline = IDataHelper.duplicate(outputPipeline, true);
    }

    /**
     * Returns the source service name.
     *
     * @return the source service name.
     */
    @Override
    public NSName getNSName() {
        return sourceService;
    }

    /**
     * Invokes the target service the source service was redirected to.
     *
     * @param pipeline      The invoke pipeline.
     * @return              The return pipeline.
     * @throws Exception    If an error occurs.
     */
    @Override
    public Values baseInvoke(Values pipeline) throws Exception {
        return Values.use(baseInvoke((IData)pipeline));
    }

    /**
     * Invokes the target service the source service was redirected to.
     *
     * @param pipeline      The invoke pipeline.
     * @return              The return pipeline.
     * @throws Exception    If an error occurs.
     */
    @Override
    public IData baseInvoke(IData pipeline) throws Exception {
        try {
            if (targetService != null) {
                applyInputRedirect(pipeline);
                pipeline = targetService.baseInvoke(pipeline);
            }
        } finally {
            if (targetService != null) {
                applyOutputRedirect(pipeline);
            }
        }
        return pipeline;
    }

    /**
     * Applies the input signature parameter redirection required for this redirect.
     *
     * @param inputPipeline     The input pipeline to be redirected.
     */
    private void applyInputRedirect(IData inputPipeline) {
        applyRedirect(inputPipeline, inputSignature, InputOutputSignature.INPUT);
        if (inputPipeline != null && this.inputPipeline != null) {
            IDataHelper.mergeInto(inputPipeline, this.inputPipeline);
        }
    }

    /**
     * Applies the output signature parameter redirection required for this redirect.
     *
     * @param outputPipeline    The output pipeline to be redirected.
     */
    private void applyOutputRedirect(IData outputPipeline) {
        applyRedirect(outputPipeline, outputSignature, InputOutputSignature.OUTPUT);
        if (outputPipeline != null) {
            if (this.inputPipeline != null) {
                IDataCursor inputCursor = this.inputPipeline.getCursor();
                IDataCursor outputCursor = outputPipeline.getCursor();
                try {
                    while (inputCursor.next()) {
                        IDataCursorHelper.remove(outputCursor, Object.class, inputCursor.getKey());
                    }
                } finally {
                    inputCursor.destroy();
                    outputCursor.destroy();
                }
            }
            if (this.outputPipeline != null) {
                IDataHelper.mergeInto(outputPipeline, this.outputPipeline);
            }
        }
    }

    /**
     * Updates the given pipeline, renaming element keys as required by the given parameter redirect rules.
     *
     * @param pipeline           The pipeline to be redirected.
     * @param parameterRedirects The parameter redirect rules specifying source to target key mappings.
     * @param direction          Whether to redirect input parameters or output parameters.
     */
    private static void applyRedirect(IData pipeline, IData[] parameterRedirects, InputOutputSignature direction) {
        if (pipeline != null && parameterRedirects != null && parameterRedirects.length > 0) {
            for (IData parameterRedirect : parameterRedirects) {
                if (parameterRedirect != null) {
                    IDataCursor cursor = parameterRedirect.getCursor();
                    try {
                        String sourceKey = IDataCursorHelper.get(cursor, String.class, "source");
                        String targetKey = IDataCursorHelper.get(cursor, String.class, "target");
                        if (direction == InputOutputSignature.INPUT && sourceKey != null) {
                            if (targetKey == null) {
                                IDataHelper.drop(pipeline, sourceKey);
                            } else if (IDataHelper.exists(pipeline, sourceKey)) {
                                IDataHelper.rename(pipeline, sourceKey, targetKey);
                            }
                        } else if (direction == InputOutputSignature.OUTPUT && targetKey != null) {
                            if (sourceKey == null) {
                                IDataHelper.drop(pipeline, targetKey);
                            } else if (IDataHelper.exists(pipeline, targetKey)) {
                                IDataHelper.rename(pipeline, targetKey, sourceKey);
                            }
                        }
                    } finally {
                        cursor.destroy();
                    }
                }
            }
        }
    }

    /**
     * Returns true if this object is started.
     *
     * @return True if this object is started.
     */
    @Override
    public boolean isStarted() {
        return started;
    }

    /**
     * Starts this object.
     */
    @Override
    public synchronized void start() {
        if (!started) {
            started = true;
            originalNode = Namespace.current().getNode(sourceService);
            Namespace.current().putNode(this, false, null);
            if (originalNode == null) {
                // hide virtual services
                NodeHelper.setPermission(sourceService.getFullName(), "list", "WmPrivate");
            }
        }
    }

    /**
     * Stops this object.
     */
    @Override
    public synchronized void stop() {
        if (started) {
            started = false;
            if (originalNode == null) {
                try {
                    Namespace.current().deleteNode(this.getNSName(), false, null);
                } catch(NSException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                Namespace.current().putNode(originalNode, false, null);
                originalNode = null;
            }
        }
    }

    /**
     * Restarts this object.
     */
    @Override
    public synchronized void restart() {
        stop();
        start();
    }

    /**
     * Not implemented.
     *
     * @param document Not used.
     */
    @Override
    public void setIData(IData document) {
        // does nothing, not implemented.
    }

    /**
     * Returns an IData representation of this object.
     *
     * @return an IData representation of this object.
     */
    @Override
    public IData getIData() {
        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        try {
            cursor.insertAfter("started?", started);

            IData redirect = IDataFactory.create();
            IDataCursor redirectCursor = redirect.getCursor();
            try {
                redirectCursor.insertAfter("source", sourceService.getFullName());
                redirectCursor.insertAfter("target", targetService == null ? null : targetService.getNSName().getFullName());

                if (inputSignature != null || outputSignature != null) {
                    IData signature = IDataFactory.create();
                    IDataCursor signatureCursor = signature.getCursor();
                    try {
                        if (inputSignature != null) {
                            signatureCursor.insertAfter("input", inputSignature);
                        }
                        if (outputSignature != null) {
                            signatureCursor.insertAfter("output", outputSignature);
                        }
                    } finally {
                        signatureCursor.destroy();
                    }
                    redirectCursor.insertAfter("signature", signature);
                }

                if (inputPipeline != null || outputPipeline != null) {
                    IData pipeline = IDataFactory.create();
                    IDataCursor pipelineCursor = pipeline.getCursor();
                    try {
                        if (inputPipeline != null) {
                            pipelineCursor.insertAfter("input", inputPipeline);
                        }
                        if (outputPipeline != null) {
                            pipelineCursor.insertAfter("output", outputPipeline);
                        }
                    } finally {
                        pipelineCursor.destroy();
                    }
                    redirectCursor.insertAfter("pipeline", pipeline);
                }
                cursor.insertAfter("redirect", redirect);
            } finally {
                redirectCursor.destroy();
            }
        } finally {
            cursor.destroy();
        }
        return document;
    }
}
