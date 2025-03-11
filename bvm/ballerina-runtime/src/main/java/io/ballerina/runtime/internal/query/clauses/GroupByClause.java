package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.values.ArrayValueImpl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroupByClause implements PipelineStage {
    private final BArray groupingKeys;
    private final BArray nonGroupingKeys;
    private final Environment env;

    /**
     * Constructor for the GroupByClause.
     *
     * @param groupingKeys The keys to group by.
     */
    public GroupByClause(Environment env, BArray groupingKeys, BArray nonGroupingKeys) {
        this.groupingKeys = groupingKeys;
        this.nonGroupingKeys = nonGroupingKeys;
        this.env = env;
    }

    public static GroupByClause initGroupByClause(Environment env, BArray groupingKeys, BArray nonGroupingKeys) {
        return new GroupByClause(env, groupingKeys, nonGroupingKeys);
    }

    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) {
        // Step 1: Group by extracted key
        LinkedHashMap<BMap<BString, Object>, List<Frame>> groupedData = new LinkedHashMap<>();

        inputStream.forEach(frame -> {
            BMap<BString, Object> key = extractGroupingKey(frame);
            groupedData.computeIfAbsent(key, k -> new ArrayList<>()).add(frame);
        });

        // Step 2: Convert grouped data into a stream
        return groupedData.entrySet().stream().map(entry -> buildGroupedFrame(entry.getKey(), entry.getValue()));
    }

    private BMap<BString, Object> extractGroupingKey(Frame frame) {
        BMap<BString, Object> keyMap = ValueCreator.createMapValue();
        BMap<BString, Object> record = frame.getRecord();

        for (int i = 0; i < groupingKeys.size(); i++) {
            BString key = (BString) groupingKeys.get(i);
            keyMap.put(key, record.get(key));
        }
        return keyMap;
    }

    private Frame buildGroupedFrame(BMap<BString, Object> key, List<Frame> frames) {
        Frame groupedFrame = new Frame();
        BMap<BString, Object> groupedRecord = groupedFrame.getRecord();

        // Set grouping keys
        key.entrySet().forEach(entry -> groupedRecord.put(entry.getKey(), entry.getValue()));

        // Set non-grouping keys as arrays
        for (int i = 0; i < nonGroupingKeys.size(); i++) {
            BString nonGroupingKey = (BString) nonGroupingKeys.get(i);
            Object[] values = frames.stream()
                    .map(f -> f.getRecord().get(nonGroupingKey))
                    .filter(Objects::nonNull)
                    .toArray();

            BArray valuesArray = new ArrayValueImpl(values, TypeCreator.createArrayType(PredefinedTypes.TYPE_ANY));
            groupedRecord.put(nonGroupingKey, valuesArray);
        }

        groupedFrame.updateRecord(groupedRecord);
        return groupedFrame;
    }
}
