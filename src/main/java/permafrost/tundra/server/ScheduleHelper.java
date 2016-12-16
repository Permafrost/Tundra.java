/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Lachlan Dowding
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

import com.wm.app.b2b.server.ServiceException;
import com.wm.app.b2b.server.scheduler.ScheduledTask;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.flow.ConditionEvaluator;
import permafrost.tundra.lang.BooleanHelper;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.math.LongHelper;
import permafrost.tundra.time.DateTimeHelper;
import permafrost.tundra.time.DurationHelper;
import permafrost.tundra.time.DurationPattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A collection of convenience methods for working with Integration Server scheduled tasks.
 */
public class ScheduleHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ScheduleHelper() {}

    /**
     * Returns the scheduled task identified by the given ID, or null if no task for that ID exists.
     *
     * @param identity          The ID of the scheduled task to be returned.
     * @return                  The scheduled task with the given ID, or null if no task with that ID exists.
     * @throws ServiceException If an error occurs when retrieving scheduled tasks.
     */
    public static IData get(String identity) throws ServiceException {
        if (identity == null || !exists(identity)) return null;

        IData pipeline = IDataFactory.create();
        IDataCursor cursor = pipeline.getCursor();
        IDataUtil.put(cursor, "taskID", identity);
        cursor.destroy();

        pipeline = ServiceHelper.invoke("pub.scheduler:getTaskInfo", pipeline);

        cursor = pipeline.getCursor();
        String type = IDataUtil.getString(cursor, "type");
        String user = IDataUtil.getString(cursor, "runAsUser");
        String target = IDataUtil.getString(cursor, "target");
        String description = IDataUtil.getString(cursor, "description");
        String lateness = IDataUtil.getString(cursor, "lateness");
        String latenessAction = IDataUtil.getString(cursor, "latenessAction");
        String service = IDataUtil.getString(cursor, "service");
        long next = Long.parseLong(IDataUtil.getString(cursor, "nextRun"));
        int state = Integer.parseInt(IDataUtil.getString(cursor, "execState"));
        IData inputs = IDataUtil.getIData(cursor, "inputs");

        boolean overlap = true;

        String startDate = null, startTime = null, startDateTime = null, endDate = null, endTime = null, endDateTime = null;
        IData info = null;

        if (type.equals("repeat")) {
            info = IDataUtil.getIData(cursor, "repeatingTaskInfo");
            IDataCursor ic = info.getCursor();

            String intervalSeconds = IDataUtil.getString(ic, "interval");
            overlap = !Boolean.valueOf(IDataUtil.getString(ic, "doNotOverlap"));

            startDate = IDataUtil.getString(ic, "startDate");
            startTime = IDataUtil.getString(ic, "startTime");

            endDate = IDataUtil.getString(ic, "endDate");
            endTime = IDataUtil.getString(ic, "endTime");

            ic.destroy();

            info = IDataFactory.create();
            ic = info.getCursor();

            IDataUtil.put(ic, "interval", DurationHelper.format(intervalSeconds, DurationPattern.SECONDS, DurationPattern.XML));

            ic.destroy();
        } else if (type.equals("complex")) {
            info = IDataUtil.getIData(cursor, "complexTaskInfo");
            IDataCursor ic = info.getCursor();

            overlap = !Boolean.valueOf(IDataUtil.getString(ic, "doNotOverlap"));

            startDate = IDataUtil.getString(ic, "startDate");
            startTime = IDataUtil.getString(ic, "startTime");

            endDate = IDataUtil.getString(ic, "endDate");
            endTime = IDataUtil.getString(ic, "endTime");

            String[] minutes = IDataUtil.getStringArray(ic, "minutes");
            String[] hours = IDataUtil.getStringArray(ic, "hours");
            String[] months = IDataUtil.getStringArray(ic, "months");
            String[] daysOfMonth = IDataUtil.getStringArray(ic, "daysOfMonth");
            String[] daysOfWeek = IDataUtil.getStringArray(ic, "daysOfWeek");

            ic.destroy();

            info = IDataFactory.create();
            ic = info.getCursor();

            if (months != null) IDataUtil.put(ic, "months", months);
            if (daysOfMonth != null) IDataUtil.put(ic, "days", daysOfMonth);
            if (daysOfWeek != null) IDataUtil.put(ic, "weekdays", daysOfWeek);
            if (hours != null) IDataUtil.put(ic, "hours", hours);
            if (minutes != null) IDataUtil.put(ic, "minutes", minutes);

            ic.destroy();
        } else {
            info = IDataUtil.getIData(cursor, "oneTimeTaskInfo");
            IDataCursor ic = info.getCursor();
            startDate = endDate = IDataUtil.getString(ic, "date");
            startTime = endTime = IDataUtil.getString(ic, "time");
            ic.destroy();
            info = null;
        }
        cursor.destroy();

        IData output = IDataFactory.create();
        cursor = output.getCursor();

        IDataUtil.put(cursor, "id", identity);
        if (inputs != null) {
            IDataCursor inputCursor = inputs.getCursor();
            String name = IDataUtil.getString(inputCursor, "$schedule.name");
            inputCursor.destroy();

            if (name != null) IDataUtil.put(cursor, "name", name);
        }

        if (description != null) IDataUtil.put(cursor, "description", description);
        IDataUtil.put(cursor, "type", type);
        IDataUtil.put(cursor, "service", service);
        String packageName = ServiceHelper.getPackageName(service);
        if (packageName != null) IDataUtil.put(cursor, "package", packageName);
        IDataUtil.put(cursor, "target", target);

        if (SchedulerHelper.self().equalsIgnoreCase(target)) {
            IDataUtil.put(cursor, "target.logical", "$self");
        } else {
            IDataUtil.put(cursor, "target.logical", target);
        }

        IDataUtil.put(cursor, "user", user);

        String pattern = "yyyy/MM/dd HH:mm:ss";

        if (startDate != null && startTime != null) {
            startDateTime = DateTimeHelper.format(startDate + " " + startTime, pattern, "datetime");
            IDataUtil.put(cursor, "start", startDateTime);
        }

        if (endDate != null && endTime != null) {
            endDateTime = DateTimeHelper.format(endDate + " " + endTime, pattern, "datetime");
            IDataUtil.put(cursor, "end", endDateTime);
        }

        IDataUtil.put(cursor, "overlap?", BooleanHelper.emit(overlap));

        if (lateness != null) {
            IData late = IDataFactory.create();
            IDataCursor lc = late.getCursor();

            IDataUtil.put(lc, "duration", DurationHelper.format(lateness, DurationPattern.MINUTES, DurationPattern.XML));

            if (latenessAction.equals("0") || latenessAction.equalsIgnoreCase("run immediately")) {
                latenessAction = "run immediately";
            } else if (latenessAction.equals("1") || latenessAction.equalsIgnoreCase("skip and run at next scheduled interval")) {
                latenessAction = "skip and run at next scheduled interval";
            } else if (latenessAction.equals("2") || latenessAction.equalsIgnoreCase("suspend")) {
                latenessAction = "suspend";
            }

            IDataUtil.put(lc, "action", latenessAction);
            lc.destroy();

            IDataUtil.put(cursor, "lateness", late);
        }

        if (info != null) IDataUtil.put(cursor, type, info);
        if (inputs != null && IDataHelper.size(inputs) > 0) IDataUtil.put(cursor, "pipeline", inputs);

        String stateString = null;
        switch(state) {
            case ScheduledTask.STATE_READY:
                stateString = "waiting";
                break;
            case ScheduledTask.STATE_RUNNING:
                stateString = "running";
                break;
            case ScheduledTask.STATE_SUSPENDED:
                stateString = "suspended";
                break;
            case ScheduledTask.STATE_DELETED:
                stateString = "cancelled";
                break;
            default:
                ExceptionHelper.raise("Scheduled task '" + identity + "' has unsupported status: " + state);
                break;
        }
        IDataUtil.put(cursor, "status", stateString);

        if (next > 0) IDataUtil.put(cursor, "next", DateTimeHelper.format(LongHelper.emit(next), "milliseconds", "datetime"));

        cursor.destroy();

        return output;
    }

    /**
     * Returns the scheduled task identified by the given task name, or null if no task for that name exists.
     *
     * @param name              The name of the task to be returned.
     * @return                  The task with the given name, or null if no task with the given name exists.
     * @throws ServiceException If an error occurs retrieving scheduled tasks.
     */
    public static IData getByName(String name) throws ServiceException {
        if (name == null) return null;

        IData[] results = list(name, null, null, null);
        return results.length > 0 ? results[0] : null;
    }

    /**
     * Returns true if a scheduled task with the given ID exists.
     *
     * @param identity          The ID of the scheduled task to check existence of.
     * @return                  Whether a scheduled task with the given ID exists.
     * @throws ServiceException If an error occurs when retrieving the scheduled tasks.
     */
    public static boolean exists(String identity) throws ServiceException {
        return identity != null && Arrays.binarySearch(listAll(), identity) >= 0;
    }

    /**
     * Returns true if a scheduled task exists with the given name.
     *
     * @param name              The name of the scheduled task to check existence of.
     * @return                  Whether a scheduled task with the given name exists.
     * @throws ServiceException If an error occurs when retrieving the scheduled tasks.
     */
    public static boolean existsByName(String name) throws ServiceException {
        return getByName(name) != null;
    }

    /**
     * Returns all scheduled tasks matching the given filter condition.
     *
     * @param filter            An optional filter condition which when true will include the matching task in the
     *                          returned results.
     * @param pipeline          The pipeline used when evaluating the given filter condition.
     * @return                  The scheduled tasks that match the given name, service, and filter condition.
     * @throws ServiceException If an error occurs when retrieving scheduled tasks or evaluating filter conditions.
     */
    public static IData[] list(String filter, IData pipeline) throws ServiceException {
        return list(null, filter, pipeline);
    }


    /**
     * Returns all scheduled tasks matching the given service, and filter condition.
     *
     * @param service           An optional service name which filters the returned results to only tasks that execute
     *                          the given service.
     * @param filter            An optional filter condition which when true will include the matching task in the
     *                          returned results.
     * @param pipeline          The pipeline used when evaluating the given filter condition.
     * @return                  The scheduled tasks that match the given name, service, and filter condition.
     * @throws ServiceException If an error occurs when retrieving scheduled tasks or evaluating filter conditions.
     */
    public static IData[] list(String service, String filter, IData pipeline) throws ServiceException {
        return list(null, service, filter, pipeline);
    }

    /**
     * Returns all scheduled tasks matching the given name, service, and filter condition.
     *
     * @param name              An optional task name which filters the returned results to only tasks that have the
     *                          given task name.
     * @param service           An optional service name which filters the returned results to only tasks that execute
     *                          the given service.
     * @param filter            An optional filter condition which when true will include the matching task in the
     *                          returned results.
     * @param pipeline          The pipeline used when evaluating the given filter condition.
     * @return                  The scheduled tasks that match the given name, service, and filter condition.
     * @throws ServiceException If an error occurs when retrieving scheduled tasks or evaluating filter conditions.
     */
    public static IData[] list(String name, String service, String filter, IData pipeline) throws ServiceException {
        if (pipeline == null) pipeline = IDataFactory.create();

        String[] identities = listAll();
        List<IData> tasks = new ArrayList<IData>(identities.length);

        for (String identity : identities) {
            IData task = get(identity);

            boolean matched = true;

            if (name != null) {
                IDataCursor cursor = task.getCursor();
                String taskName = IDataUtil.getString(cursor, "name");
                cursor.destroy();

                matched = name.equals(taskName);
            }

            if (matched && service != null) {
                IDataCursor cursor = task.getCursor();
                String taskService = IDataUtil.getString(cursor, "service");
                cursor.destroy();

                matched = service.equals(taskService);
            }

            if (matched && filter != null) {
                IData scope = IDataUtil.clone(pipeline);
                IDataCursor cursor = scope.getCursor();
                IDataUtil.put(cursor, "$schedule", task);
                cursor.destroy();

                matched = ConditionEvaluator.evaluate(filter, scope);
            }

            if (matched) tasks.add(task);
        }

        return tasks.toArray(new IData[tasks.size()]);
    }

    /**
     * Returns all scheduled task IDs.
     *
     * @return All scheduled task IDs.
     * @throws ServiceException If an error is encountered while retrieving the schedule IDs.
     */
    private static String[] listAll() throws ServiceException {
        return list(false);
    }

    /**
     * Returns currently running scheduled task IDs.
     *
     * @return Currently running scheduled task IDs.
     * @throws ServiceException If an error is encountered while retrieving the schedule IDs.
     */
    private static String[] listRunning() throws ServiceException {
        return list(true);
    }

    /**
     * Returns all scheduled task IDs, or only those that are currently running if the given flag is true.
     *
     * @param running           If true, only schedules currently running are returned, otherwise all are returned.
     * @return                  The resulting list of scheduled task IDs.
     * @throws ServiceException If an error is encountered while retrieving the schedule IDs.
     */
    private static String[] list(boolean running) throws ServiceException {
        IData pipeline = IDataFactory.create();

        IDataCursor cursor = pipeline.getCursor();
        IDataUtil.put(cursor, "running", BooleanHelper.emit(running));
        cursor.destroy();

        pipeline = ServiceHelper.invoke("pub.scheduler:getTaskIDs", pipeline);

        cursor = pipeline.getCursor();
        String[] list = IDataUtil.getStringArray(cursor, "taskIDs");
        cursor.destroy();

        if (list == null) {
            list = new String[0];
        } else if (list.length > 1) {
            Arrays.sort(list);
        }

        return list;
    }
}
