/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.query.clauses;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.internal.query.pipeline.Frame;
import io.ballerina.runtime.internal.query.utils.QueryException;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * Represents a `limit` clause in the query pipeline that restricts the number of frames.
 *
 * @since 2201.13.0
 */
public class LimitClause implements PipelineStage {
    private final BFunctionPointer limitFunction;
    private final Environment env;

    /**
     * Constructor for the LimitClause.
     *
     * @param env          The runtime environment.
     * @param limitFunction The function to determine the limit dynamically.
     */
    private LimitClause(Environment env, BFunctionPointer limitFunction) {
        this.limitFunction = limitFunction;
        this.env = env;
    }

    /**
     * Static initializer for LimitClause.
     *
     * @param env          The runtime environment.
     * @param limitFunction The function to determine the limit dynamically.
     * @return A new instance of LimitClause.
     */
    public static LimitClause initLimitClause(Environment env, BFunctionPointer limitFunction) {
        return new LimitClause(env, limitFunction);
    }

    /**
     * Processes a stream of frames by applying the limit function to determine the maximum number of frames.
     *
     * @param inputStream The input stream of frames.
     * @return A stream of frames with at most `limit` frames.
     */
    @Override
    public Stream<Frame> process(Stream<Frame> inputStream) {
        Object limitResult;
        try {
            limitResult = limitFunction.call(env.getRuntime(), new Frame().getRecord());
        } catch (Exception e) {
            limitResult = ErrorCreator.createError(
                    StringUtils.fromString("variable declarations inside query cannot be used for limit"));
        }

        if (limitResult instanceof BError error) {
            // Build a stream that will throw the error during consumption
            return Stream.generate(() -> {
                throw new QueryException(error);
            });
        }

        if (!(limitResult instanceof Long limit)) {
            // Unexpected type
            BError typeError = ErrorCreator.createError(
                    StringUtils.fromString("limit function must return a long."));
            return Stream.generate(() -> {
                throw new QueryException(typeError);
            });
        }

        if (limit < 1) {
            // Invalid limit value
            BError invalidLimit = ErrorCreator.createError(
                    StringUtils.fromString("limit cannot be < 1."));
            return Stream.generate(() -> {
                throw new QueryException(invalidLimit);
            });
        }

        return inputStream.limit(limit);
    }
}
