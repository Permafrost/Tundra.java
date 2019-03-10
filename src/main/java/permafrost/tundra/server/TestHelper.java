/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Lachlan Dowding
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

package permafrost.tundra.server;

import com.wm.app.b2b.server.Service;
import com.wm.data.IData;
import com.wm.app.b2b.server.Package;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.lang.ns.NSName;
import com.wm.util.coder.IDataCodable;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.lang.ArrayHelper;
import permafrost.tundra.lang.EnumerationHelper;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.util.concurrent.DirectExecutorService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

/**
 * Services for executing flow service unit tests.
 */
public final class TestHelper {
    /**
     * Regular expression pattern which is used to identify test case services.
     */
    private static final Pattern TEST_SERVICE_PATTERN = Pattern.compile("((.*\\.)|^)test.*:should.+");

    /**
     * Disallow instantiation of this class.
     */
    private TestHelper() {}

    /**
     * Executes all the tests in the given package, returning the results as an IData document.
     *
     * @param testPackage   The package that contains tests to be executed.
     * @return              The results of executing the tests.
     */
    @SuppressWarnings("unchecked")
    public static IData execute(Package testPackage) {
        return execute(testPackage, 1);
    }

    /**
     * Executes all the tests in the given package, returning the results as an IData document.
     *
     * @param testPackage   The package that contains tests to be executed.
     * @param concurrency   The number of threads to use to execute the tests.
     * @return              The results of executing the tests.
     */
    @SuppressWarnings("unchecked")
    public static IData execute(Package testPackage, int concurrency) {
        if (testPackage == null) return null;

        TestSuite suite = new TestSuite(testPackage, concurrency);
        return suite.call();
    }

    /**
     * Represents a test suite.
     */
    private static class TestSuite implements Callable<IData> {
        /**
         * The name of this test suite.
         */
        private String name;
        /**
         * The number of threads to use when executing tests.
         */
        private int concurrency;
        /**
         * The test cases that comprise this test suite.
         */
        private List<TestCase> testCases;
        /**
         * The executor used to execute test case, with optional concurrency via a thread pool
         */
        private ExecutorService executor;

        /**
         * Constructs a new TestSuite.
         *
         * @param testPackage   The package that this test suite is implemented in.
         */
        public TestSuite(Package testPackage) {
            this(testPackage, 1);
        }

        /**
         * Constructs a new TestSuite.
         *
         * @param testPackage   The package that this test suite is implemented in.
         */
        public TestSuite(Package testPackage, int concurrency) {
            if (testPackage == null) throw new NullPointerException("testPackage must not be null");
            this.name = testPackage.getName();
            this.concurrency = concurrency;

            String[] services = ArrayHelper.sort(EnumerationHelper.stringify(testPackage.getLoaded()));
            testCases = new ArrayList<TestCase>(services.length);

            for (String service : services) {
                if (TEST_SERVICE_PATTERN.matcher(service).matches()) {
                    testCases.add(new TestCase(NSName.create(service)));
                }
            }
        }

        /**
         * Executes all test cases in this test suite, returning an IData document describing the test results.
         *
         * @return An IData document describing the test results.
         */
        @Override
        public IData call() {
            if (concurrency <= 1) {
                executor = new DirectExecutorService();
            } else {
                executor = new BlockingServerThreadPoolExecutor(concurrency, "Tundra/Test " + name, Thread.MIN_PRIORITY);
            }

            IData results = IDataFactory.create();
            IDataCursor cursor = results.getCursor();

            boolean testSuitePassed = true;
            int totalCount = 0, passedCount = 0, failedCount = 0;

            try {
                List<Future<TestResult>> futures = new ArrayList<Future<TestResult>>(testCases.size());
                List<IData> cases = new ArrayList<IData>(testCases.size());

                for (TestCase testCase : testCases) {
                    futures.add(executor.submit(testCase));
                }

                for (Future<TestResult> future : futures) {
                    try {
                        TestResult result = future.get();
                        testSuitePassed = testSuitePassed && result.isPassed();

                        if (result.isPassed()) {
                            passedCount++;
                        } else {
                            failedCount++;
                        }

                        totalCount++;

                        cases.add(result.getIData());
                    } catch(CancellationException ex) {
                        break;
                    } catch(ExecutionException ex) {
                        break;
                    } catch(InterruptedException ex) {
                        break;
                    }
                }

                IDataHelper.put(cursor, "package", name);
                IDataHelper.put(cursor, "passed?", testSuitePassed, String.class);

                IData counts = IDataFactory.create();
                IDataCursor countsCursor = counts.getCursor();

                try {
                    IDataHelper.put(countsCursor, "total", totalCount, String.class);
                    IDataHelper.put(countsCursor, "passed", passedCount, String.class);
                    IDataHelper.put(countsCursor, "failed", failedCount, String.class);
                } finally {
                    countsCursor.destroy();
                }

                IDataHelper.put(cursor, "counts", counts);
                IDataHelper.put(cursor, "cases", cases.toArray(new IData[cases.size()]));
            } finally {
                cursor.destroy();
                executor.shutdownNow();
                executor = null;
            }

            return results;
        }
    }

    /**
     * Represents a single test case.
     */
    private static class TestCase implements Callable<TestResult> {
        /**
         * The service that is executed for this test case.
         */
        NSName service;

        /**
         * Constructs a new test case.
         *
         * @param service           The test case service to be executed.
         */
        public TestCase(NSName service) {
            if (service == null) throw new NullPointerException("service must not be null");
            if (service.isInterface()) throw new IllegalArgumentException("service must not be an interface");
            if (!ServiceHelper.exists(service.getFullName())) throw new IllegalArgumentException("service must exist");

            this.service = service;
        }

        /**
         * Returns the setup service for this test case, if one exists.
         *
         * @return the setup service for this test case, if one exists.
         */
        private NSName getSetupService() {
            return getSibling("setup");
        }

        /**
         * Returns the teardown service for this test case, if one exists.
         *
         * @return the teardown service for this test case, if one exists.
         */
        private NSName getTeardownService() {
            return getSibling("teardown");
        }

        /**
         * Returns the sibling service with the given name for this test case, if one exists.
         *
         * @param name  The name of the sibling service.
         * @return      The the sibling service with the given name for this test case, if one exists.
         */
        private NSName getSibling(String name) {
            NSName sibling = NSName.create(service.getInterfaceNSName().getFullName(), name);
            String siblingName = sibling.getFullName();

            if (ServiceHelper.exists(siblingName)) {
                if (ServiceHelper.getPackageName(service.getFullName()).equals(ServiceHelper.getPackageName(siblingName))) {
                    return sibling;
                }
            }

            return null;
        }

        /**
         * Executes this test case, returning true if the test case passed, or false if it failed.
         *
         * @return True if the test case passed, or false if it failed.
         */
        @Override
        public TestResult call() {
            String description = getDescription(), message = null;
            boolean passed = true;

            try {
                IData pipeline = IDataFactory.create();

                NSName setupService = getSetupService();
                if (setupService != null) {
                    pipeline = Service.doInvoke(setupService, pipeline);
                }

                pipeline = Service.doInvoke(service, pipeline);

                NSName teardownService = getTeardownService();
                if (teardownService != null) {
                    pipeline = Service.doInvoke(teardownService, pipeline);
                }
            } catch(Throwable ex) {
                message = ExceptionHelper.getMessage(ex);
                passed = false;
            }

            return new TestResult(description, message, passed);
        }

        /**
         * Returns the description of this test case.
         *
         * @return The description of this test case.
         */
        public String getDescription() {
            return service.getFullName();
        }
    }

    /**
     * Represents the result from executing a TestCase.
     */
    private static class TestResult implements IDataCodable {
        /**
         * The description of the test case that produced this result.
         */
        String description;
        /**
         * An optional message describing why the test case passed or failed.
         */
        String message;
        /**
         * Whether the test case passed.
         */
        boolean passed;

        /**
         * Constructs a new TestResult.
         *
         * @param description   The description of the test case that produced this result.
         * @param message       An optional message describing why the test case passed or failed.
         * @param passed        Whether the test case passed.
         */
        public TestResult(String description, String message, boolean passed) {
            if (description == null) throw new NullPointerException("description must not be null");
            this.description = description;
            this.message = message;
            this.passed = passed;
        }

        /**
         * Returns the description of the test case that produced this result.
         *
         * @return the description of the test case that produced this result.
         */
        public String getDescription() {
            return description;
        }

        /**
         * Returns an optional message describing why the test case passed or failed.
         *
         * @return an optional message describing why the test case passed or failed.
         */
        public String getMessage() {
            return message;
        }

        /**
         * Returns whether the test case passed.
         *
         * @return whether the test case passed.
         */
        public boolean isPassed() {
            return passed;
        }

        /**
         * Returns an IData representation of this TestResult.
         *
         * @return an IData representation of this TestResult.
         */
        public IData getIData() {
            IData output = IDataFactory.create();
            IDataCursor cursor = output.getCursor();

            try {
                IDataHelper.put(cursor, "description", description);
                IDataHelper.put(cursor, "message", message, false);
                IDataHelper.put(cursor, "passed?", passed, String.class);
            } finally {
                cursor.destroy();
            }

            return output;
        }

        /**
         * This method is not implemented.
         *
         * @param document                          Not applicable.
         * @throws UnsupportedOperationException    This method is not implemented.
         */
        public void setIData(IData document) {
            throw new UnsupportedOperationException("setIData(IData) is not implemented by this class");
        }
    }
}
