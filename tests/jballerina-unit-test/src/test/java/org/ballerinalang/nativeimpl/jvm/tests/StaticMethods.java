/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.nativeimpl.jvm.tests;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BFuture;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.BmpStringValue;
import io.ballerina.runtime.internal.values.DecimalValue;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.FPValue;
import io.ballerina.runtime.internal.values.HandleValue;
import io.ballerina.runtime.internal.values.ListInitialValueEntry;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.ObjectValue;
import io.ballerina.runtime.internal.values.StringValue;
import io.ballerina.runtime.internal.values.TableValue;
import io.ballerina.runtime.internal.values.TupleValueImpl;
import io.ballerina.runtime.internal.values.TypedescValue;
import org.testng.Assert;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class contains a set of utility static methods required for interoperability testing.
 *
 * @since 1.0.0
 */
@SuppressWarnings({"all"})
public final class StaticMethods {

    private static final BArrayType INT_ARRAY_TYPE = new BArrayType(PredefinedTypes.TYPE_INT);
    private static final BArrayType JSON_ARRAY_TYPE = new BArrayType(PredefinedTypes.TYPE_JSON);
    private static final BTupleType TUPLE_TYPE = new BTupleType(
            Arrays.asList(PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_FLOAT, PredefinedTypes.TYPE_STRING,
                          PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_STRING));
    private static final Module ERROR_MODULE = new Module("testorg", "distinct_error.errors", "1");

    private StaticMethods() {
    }

    public static void throwNPE() {
        throw new NullPointerException();
    }

    public static void acceptNothingAndReturnNothing() {
    }

    public static Date acceptNothingButReturnDate() {
        return new Date();
    }

    public static StringValue acceptNothingButReturnString() {
        return new BmpStringValue("hello world");
    }

    public static BString stringParamAndReturn(BString a1) {
        return a1.concat(new BmpStringValue(" and Hadrian"));
    }

    public static Date acceptSomethingAndReturnSomething(Date date) {
        return date;
    }

    public static String acceptTwoParamsAndReturnSomething(String s1, String s2) {
        return s1 + s2;
    }

    public static Integer acceptThreeParamsAndReturnSomething(Integer s1, Integer s2, Integer s3) {
        return s1 + s2 + s3;
    }

    public static BDecimal getBDecimalValue() {
        return ValueCreator.createDecimalValue("5.0");
    }

    public static BDecimal getNullInsteadOfBDecimal() {
        return null;
    }

    // This scenario is for map value to be passed to interop and return array value.
    public static ArrayValue getArrayValueFromMap(BString key, MapValue<?, ?> mapValue) {
        ArrayValue arrayValue = (ArrayValue) ValueCreator.createArrayValue(INT_ARRAY_TYPE);
        arrayValue.add(0, 1);
        long fromMap = (long) mapValue.get(key);
        arrayValue.add(1, fromMap);
        return arrayValue;
    }

    public static BMap<BString, Object> acceptRefTypesAndReturnMap(ObjectValue a, ArrayValue b, Object c,
                                                                   ErrorValue d, Object e, Object f, MapValue<?, ?> g) {
        BMap<BString, Object> mapValue = ValueCreator.createMapValue();
        mapValue.put(StringUtils.fromString("a"), a);
        mapValue.put(StringUtils.fromString("b"), b);
        mapValue.put(StringUtils.fromString("c"), c);
//        mapValue.put("d", d);
        mapValue.put(StringUtils.fromString("e"), e);
        mapValue.put(StringUtils.fromString("f"), f);
        mapValue.put(StringUtils.fromString("g"), g);
        return mapValue;
    }

    public static boolean acceptServiceObjectAndReturnBoolean(ObjectValue serviceObject) {
        return TypeTags.SERVICE_TAG == serviceObject.getType().getTag();
    }

    public static BError acceptStringErrorReturn(BString msg) {
        return ErrorCreator.createError(msg, new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
    }

    public static Object acceptIntUnionReturn(int flag) {
        return switch (flag) {
            case 1 -> 25;
            case 2 -> StringUtils.fromString("sample value return");
            case 3 -> 54.88;
            case 4 -> null;
            case 5 -> ValueCreator.createMapValue(TypeCreator.createMapType(PredefinedTypes.TYPE_ANYDATA));
            default -> true;
        };
    }

    public static Object acceptIntAnyReturn(int flag) {
        return switch (flag) {
            case 1 -> 25;
            case 2 -> "sample value return";
            case 3 -> 54.88;
            default -> true;
        };
    }

    public static Object acceptNothingInvalidAnydataReturn() {
        return "invalid java string";
    }



    public static ObjectValue acceptObjectAndObjectReturn(ObjectValue p, int newVal) {
        p.set(StringUtils.fromString("age"), newVal);
        return p;
    }

    public static int acceptObjectAndReturnField(ObjectValue p) {
        return ((Long) p.get(StringUtils.fromString("age"))).intValue();
    }

    public static MapValue<BString, Object> acceptRecordAndRecordReturn(MapValue<BString, Object> e, BString newVal) {
        e.put(StringUtils.fromString("name"), newVal);
        return e;
    }

    public static String getString(UUID uuid) {
        return uuid.toString() + ": Sameera";
    }

    public static void acceptNothingReturnNothingAndThrowsCheckedException() throws JavaInteropTestCheckedException {

    }

    public static void acceptNothingReturnNothingAndThrowsMultipleCheckedException()
            throws JavaInteropTestCheckedException, IOException, ClassNotFoundException {

    }

    public static void acceptNothingReturnNothingAndThrowsUncheckedException() throws UnsupportedOperationException {

    }

    public static void acceptNothingReturnNothingAndThrowsCheckedAndUncheckedException()
            throws JavaInteropTestCheckedException, UnsupportedOperationException, ClassNotFoundException {

    }


    public static Date acceptNothingReturnSomethingAndThrowsCheckedException() throws JavaInteropTestCheckedException {
        return new Date();
    }

    public static Date acceptNothingReturnSomethingAndThrowsMultipleCheckedException()
            throws JavaInteropTestCheckedException, IOException, ClassNotFoundException {
        return new Date();
    }

    public static Date acceptNothingReturnSomethingAndThrowsUncheckedException() throws UnsupportedOperationException {
        return new Date();
    }

    public static Date acceptNothingReturnSomethingAndThrowsCheckedAndUncheckedException()
            throws JavaInteropTestCheckedException, UnsupportedOperationException, ClassNotFoundException {
        return new Date();
    }

    public static Date acceptSomethingReturnSomethingAndThrowsCheckedAndUncheckedException(Date date)
            throws JavaInteropTestCheckedException, UnsupportedOperationException, ClassNotFoundException {
        return date;
    }

    public static Date acceptSomethingReturnSomethingAndThrowsCheckedException(Date date)
            throws JavaInteropTestCheckedException {
        return date;
    }

    public static Date acceptSomethingReturnSomethingAndThrowsMultipleCheckedException(Date date)
            throws JavaInteropTestCheckedException, IOException, ClassNotFoundException {
        return date;
    }

    public static Date acceptSomethingReturnSomethingAndThrowsUncheckedException(Date date)
            throws UnsupportedOperationException {
        return date;
    }

    public static int acceptIntReturnIntThrowsCheckedException(long a) throws JavaInteropTestCheckedException {
        return (int) (a + 5);
    }

    public static int[] acceptIntReturnIntArrayThrowsCheckedException(long a) throws JavaInteropTestCheckedException {
        int arr[] = {1, 2, 3};
        if (a == 1) {
            return arr;
        } else {
            throw new JavaInteropTestCheckedException("Invalid state");
        }
    }

    public static ArrayValue getArrayValueFromMapWhichThrowsCheckedException(BString key, MapValue<?, ?> mapValue)
            throws JavaInteropTestCheckedException {
        ArrayValue arrayValue = (ArrayValue) ValueCreator.createArrayValue(INT_ARRAY_TYPE);
        arrayValue.add(0, 1);
        long fromMap = mapValue.getIntValue(key);
        arrayValue.add(1, fromMap);
        return arrayValue;
    }

    public static BMap<BString, Object> acceptRefTypesAndReturnMapWhichThrowsCheckedException(ObjectValue a,
                                                                                              ArrayValue b, Object c,
                                                                                              ErrorValue d, Object e,
                                                                                              Object f,
                                                                                              MapValue<?, ?> g)
            throws JavaInteropTestCheckedException {
        BMap<BString, Object> mapValue = ValueCreator.createMapValue();
        mapValue.put(StringUtils.fromString("a"), a);
        mapValue.put(StringUtils.fromString("b"), b);
        mapValue.put(StringUtils.fromString("c"), c);
        mapValue.put(StringUtils.fromString("e"), e);
        mapValue.put(StringUtils.fromString("f"), f);
        mapValue.put(StringUtils.fromString("g"), g);
        return mapValue;
    }

    public static BError acceptStringErrorReturnWhichThrowsCheckedException(BString msg)
            throws JavaInteropTestCheckedException {
        return ErrorCreator.createError(msg, new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
    }

    public static Object acceptIntErrorUnionReturnWhichThrowsCheckedException(int flag)
            throws JavaInteropTestCheckedException {
        if (flag == 0) {
            return 5;
        } else {
            return new ErrorValue(StringUtils.fromString("error message"));
        }
    }

    public static Object returnDistinctErrorUnionWhichThrowsCheckedException(int flag, BString errorName)
            throws JavaInteropTestCheckedException {
        if (flag == 0) {
            return 5;
        } else if (flag == 1) {
            BMap<BString, Object> errorDetails = ValueCreator.createMapValue();
            errorDetails.put(StringUtils.fromString("detail"), "detail error message");
            return ErrorCreator.createError(ERROR_MODULE, errorName.getValue(), StringUtils.fromString("error msg"),
                    null, errorDetails);
        } else {
            return ErrorCreator.createError(StringUtils.fromString("Invalid data given"));
        }
    }

    public static long acceptIntErrorUnionReturnWhichThrowsUncheckedException() throws RuntimeException {
        return 5;
    }

    public static Object acceptIntUnionReturnWhichThrowsCheckedException(int flag)
            throws JavaInteropTestCheckedException {
        return switch (flag) {
            case 1 -> 25;
            case 2 -> "sample value return";
            case 3 -> 54.88;
            default -> true;
        };
    }

    public static ObjectValue acceptObjectAndObjectReturnWhichThrowsCheckedException(ObjectValue p, int newVal)
            throws JavaInteropTestCheckedException {
        p.set(StringUtils.fromString("age"), newVal);
        return p;
    }

    public static MapValue<BString, Object> acceptRecordAndRecordReturnWhichThrowsCheckedException(
            MapValue<BString, Object> e, BString newVal) throws JavaInteropTestCheckedException {
        e.put(StringUtils.fromString("name"), newVal);
        return e;
    }

    public static BMap<BString, Object> getMapOrError(BString swaggerFilePath, MapValue<?, ?> apiDef)
            throws JavaInteropTestCheckedException {
        BString finalBasePath = StringUtils.fromString("basePath");
        AtomicLong runCount = new AtomicLong(0L);
        ArrayValue arrayValue = new ArrayValueImpl(new BArrayType(ValueCreator.createRecordValue(new Module(
                "", "."), "ResourceDefinition").getType()));
        BMap<BString, Object> apiDefinitions = ValueCreator.createRecordValue(new Module("",
                                                                        "."), "ApiDefinition");
        BMap<BString, Object> resource = ValueCreator.createRecordValue(new Module("",
                                                                  "."), "ResourceDefinition");
        resource.put(StringUtils.fromString("path"), finalBasePath);
        resource.put(StringUtils.fromString("method"), StringUtils.fromString("Method string"));
        arrayValue.add(runCount.getAndIncrement(), resource);
        apiDefinitions.put(StringUtils.fromString("resources"), arrayValue);
        return apiDefinitions;
    }

    public static Object returnObjectOrError() {
        return ErrorCreator.createError(StringUtils.fromString("some reason"),
                                        new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL));
    }

    public static TupleValueImpl getArrayValue() throws BError {
        String name = null;
        String type = null;
        try {
            return new TupleValueImpl(new String[]{name, type}, new BTupleType(new ArrayList<Type>() {
                {
                    add(PredefinedTypes.TYPE_STRING);
                    add(PredefinedTypes.TYPE_STRING);
                }
            }));
        } catch (BError e) {
            throw ErrorCreator.createError(StringUtils.fromString(
                    "Error occurred while creating ArrayValue."), e);
        }
    }

    public static long funcWithAsyncDefaultParamExpression(long a, long b) {
        return a + (b * 2);
    }

    public static long usingParamValues(long a, long b) {
        return a + (b * 3);
    }

    public static BDecimal decimalParamAndReturn(BDecimal a) {
        return new DecimalValue(new BigDecimal("99.7")).add(a);
    }

    public static Object decimalParamAndReturnAsObject(BDecimal a) {
        return new DecimalValue(new BigDecimal("99.6")).add(a);
    }

    public static BDecimal decimalParamAndWithBigDecimal(BigDecimal a) {
        return new DecimalValue(new BigDecimal("99.6")).add(new DecimalValue(a));
    }

    public static BDecimal decimalParamAsObjectAndReturn(Object a) {
        return new DecimalValue(new BigDecimal("99.4").add((BigDecimal) a));
    }

    public static String returnStringForBUnionFromJava() {
        return "99999";
    }
    /////////////


    public static TupleValueImpl mockedNativeFuncWithOptionalParams(long a, double b, String c,
                                                                    long d, String e) {
        TupleValueImpl tuple = (TupleValueImpl) ValueCreator.createTupleValue(TUPLE_TYPE);
        tuple.add(0, Long.valueOf(a));
        tuple.add(1, Double.valueOf(b));
        tuple.add(2, (Object) c);
        tuple.add(3, Long.valueOf(d));
        tuple.add(4, (Object) e);
        return tuple;
    }

    public static UUID getUUId() {
        UUID uuid = UUID.randomUUID();
        return uuid;
    }

    public static Object getJson() {
        MapValueImpl<BString, Object> map = new MapValueImpl<>(PredefinedTypes.TYPE_JSON);
        map.put(StringUtils.fromString("name"), StringUtils.fromString("John"));
        return map;
    }

    public static MapValueImpl<BString, Object> getJsonObject() {
        MapValueImpl<BString, Object> map = new MapValueImpl<>(PredefinedTypes.TYPE_JSON);
        map.put(StringUtils.fromString("name"), StringUtils.fromString("Doe"));
        return map;
    }

    public static ArrayValue getJsonArray() {
        ArrayValue array = (ArrayValue) ValueCreator.createArrayValue(JSON_ARRAY_TYPE);
        array.add(0, (Object) StringUtils.fromString("John"));
        return array;
    }

    public static Object getNullJson() {
        return null;
    }

    public static int getInt() {
        return 4;
    }

    public static int getIntFromJson(Object json) {
        return ((Number) json).intValue();
    }

    public static int getIntFromJsonInt(int json) {
        return json;
    }

    public static BFuture getFuture(BTypedesc typeDesc, BFuture future) {
        return future;
    }

    public static BTypedesc getTypeDesc(BTypedesc typeDesc, BFuture future) {
        return typeDesc;
    }

    public static BFuture getFutureOnly(BFuture future) {
        return future;
    }

    public static BTypedesc getTypeDescOnly(BTypedesc typeDesc) {
        return typeDesc;
    }

    public static ArrayValue getValues(MapValue<BString, Long> intMap, MapValue<BString, BString> stringMap) {
        int length = intMap.size() + stringMap.size();
        ListInitialValueEntry[] entries = new ListInitialValueEntry[length];

        int index = 0;

        for (Map.Entry<BString, Long> intEntry : intMap.entrySet()) {
            entries[index++] = new ListInitialValueEntry.ExpressionEntry(intEntry.getValue());
        }

        for (Map.Entry<BString, BString> stringEntry : stringMap.entrySet()) {
            entries[index++] = new ListInitialValueEntry.ExpressionEntry(stringEntry.getValue());
        }

        return new ArrayValueImpl(new BArrayType(new BUnionType(new ArrayList<>(2) {{
            add(PredefinedTypes.TYPE_INT);
            add(PredefinedTypes.TYPE_STRING);
        }}), length, true), entries);
    }

    public static Object echoAnydataAsAny(Object value) {
        return value;
    }

    public static ObjectValue echoObject(ObjectValue obj) {
        return obj;
    }

    public static boolean echoImmutableRecordField(MapValue<?, ?> value, BString key) {
        return value.getBooleanValue(key);
    }

    public static long addTwoNumbersSlowAsyncVoidSig(Environment env, long a, long b) {
        CompletableFuture<Long> cf = new CompletableFuture<>();
        Thread.startVirtualThread(() -> {
            sleep();
            cf.complete(a + b);
        });
        try {
            return cf.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static long addTwoNumbersFastAsyncVoidSig(Environment env, long a, long b) {
        CompletableFuture<Long> cf = new CompletableFuture<>();
        cf.complete(a + b);
        try {
            return cf.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    public static long addTwoNumbersSlowAsync(Environment env, long a, long b) {
        CompletableFuture<Long> cf = new CompletableFuture<>();
        Thread.startVirtualThread(() -> {
            sleep();
            cf.complete(a + b);
        });
        try {
            return cf.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static long addTwoNumbersFastAsync(Environment env, long a, long b) {
        CompletableFuture<Long> cf = new CompletableFuture<>();
        cf.complete(a + b);
        try {
            return cf.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object returnNullString(boolean nullVal) {
        return nullVal ? null : StringUtils.fromString("NotNull");
    }

    public static void addTwoNumbersBuggy(Environment env, long a, long b) {
        // Buggy because env.markAsync() is not called
        // TODO: see if we can verify this
    }

    public static BString getCurrentModule(Environment env, long b) {
        Module callerModule = env.getCurrentModule();
        return StringUtils.fromString(callerModule.getOrg() + "#" + callerModule.getName() + "#" +
                                              callerModule.getMajorVersion() + "#" + b);
    }

    public static BString getCurrentModuleForObject(Environment env, ObjectValue a, long b) {
        Module callerModule = env.getCurrentModule();
        return StringUtils.fromString(callerModule.getOrg() + "#" + callerModule.getName() + "#" +
                                              callerModule.getMajorVersion() + "#" +
                                              a.get(StringUtils.fromString("age")) + "#" + b);
    }

    public static long getDefaultValueWithBEnv(Environment env, long b) {
        return b;
    }

    public static long getDefaultValueWithBEnvForObject(Environment env, ObjectValue a, long b) {
        return b;
    }

    private static void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }

    public static Object acceptAndReturnReadOnly(Object value) {
        Type type = TypeUtils.getImpliedType(TypeChecker.getType(value));

        return switch (type.getTag()) {
            case TypeTags.INT_TAG -> 100L;
            case TypeTags.ARRAY_TAG,
                 TypeTags.OBJECT_TYPE_TAG -> value;
            case TypeTags.RECORD_TYPE_TAG,
                 TypeTags.MAP_TAG -> ((MapValue<?, ?>) value).get(StringUtils.fromString("first"));
            default -> StringUtils.fromString("other");
        };
    }

    public static void getNilAsReadOnly() {
    }

    public static boolean getBooleanAsReadOnly() {
        return true;
    }

    public static Long getIntAsReadOnly() {
        return 100L;
    }

    public static double getFloatAsReadOnly(double f) {
        return f;
    }

    public static BDecimal getDecimalAsReadOnly(BDecimal d) {
        return d;
    }

    public static BString getStringAsReadOnly(BString s1, BString s2) {
        return s1.concat(s2);
    }

    public static BError getErrorAsReadOnly(BError e) {
        return e;
    }

    public static FPValue getFunctionPointerAsReadOnly(FPValue func) {
        return func;
    }

    public static ObjectValue getObjectOrServiceAsReadOnly(ObjectValue ob) {
        return ob;
    }

    public static TypedescValue getTypedescAsReadOnly(TypedescValue t) {
        return t;
    }

    public static HandleValue getHandleAsReadOnly(HandleValue h) {
        return h;
    }

    public static BXml getXmlAsReadOnly(BXml x) {
        return x;
    }

    public static ArrayValue getListAsReadOnly(ArrayValue list) {
        return list;
    }

    public static MapValue<?, ?> getMappingAsReadOnly(MapValue<?, ?> mp) {
        return mp;
    }

    public static TableValue<?, ?> getTableAsReadOnly(TableValue<?, ?> tb) {
        return tb;
    }

    public static Object getValue() {
        return StringUtils.fromString("Ballerina");
    }


    public static BMap<BString, Object> createStudentUsingType() {
        Module module = new Module("$anon", ".", "0.0.0");
        BMap<BString, Object> bmap = ValueCreator.createRecordValue(module, "(Student & readonly)");
        BMapInitialValueEntry[] mapInitialValueEntries = {ValueCreator.createKeyFieldEntry(
                StringUtils.fromString("name"), StringUtils.fromString("Riyafa")), ValueCreator.createKeyFieldEntry(
                StringUtils.fromString("birth"), StringUtils.fromString("Sri Lanka"))};
        return ValueCreator.createRecordValue((RecordType) bmap.getType(), mapInitialValueEntries);
    }

    public static BMap<BString, Object> createStudent() {
        Module module = new Module("$anon", ".", "0.0.0");
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("name", StringUtils.fromString("Riyafa"));
        mapInitialValueEntries.put("birth", StringUtils.fromString("Sri Lanka"));
        return ValueCreator.createReadonlyRecordValue(module, "Student", mapInitialValueEntries);
    }

    public static BMap<BString, Object> createDetails() {
        Module module = new Module("$anon", ".", "0.0.0");
        Map<String, Object> mapInitialValueEntries = new HashMap<>();
        mapInitialValueEntries.put("name", StringUtils.fromString("Riyafa"));
        mapInitialValueEntries.put("id", 123);
        return ValueCreator.createReadonlyRecordValue(module, "Details", mapInitialValueEntries);
    }

    public static BMap<BString, Object> createRawDetails() {
        Module module = new Module("$anon", ".", "0.0.0");
        Map<String, Field> fieldMap = new HashMap<>();
        fieldMap.put("name", TypeCreator
                .createField(PredefinedTypes.TYPE_STRING, "name", SymbolFlags.REQUIRED + SymbolFlags.PUBLIC));
        fieldMap.put("id", TypeCreator
                .createField(PredefinedTypes.TYPE_INT, "id", SymbolFlags.REQUIRED + SymbolFlags.PUBLIC));
        RecordType recordType = TypeCreator.createRecordType("Details", module, SymbolFlags.READONLY
                , fieldMap, null, true, 0);
        BMapInitialValueEntry[] mapInitialValueEntries = {ValueCreator.createKeyFieldEntry(
                StringUtils.fromString("name"), StringUtils.fromString("aee")), ValueCreator.createKeyFieldEntry(
                StringUtils.fromString("id"), 123L)};
        return ValueCreator.createRecordValue(recordType, mapInitialValueEntries);
    }

    public static BDecimal defaultDecimalArgsAddition(BDecimal a, BDecimal b) {
        BDecimal c = a.add(b);
        return c;
    }

    public static Object defaultDecimalArgs(String s, BDecimal d) {
        if (!d.booleanValue()) {
            BDecimal a = new DecimalValue(s);
            return a.multiply(d);
        } else {
            return null;
        }
    }

    public static void errorStacktraceTest() {
        foo();
    }

    static void foo() {
        bar();
    }

    static void bar() {
        List<Integer> integers = Arrays.asList(0);
        integers.forEach(i -> {
            throw ErrorCreator.createError(StringUtils.fromString("error!!!"));
        });
    }

    public static int getResource() {
        return 1;
    }

    public static int getResource(BArray paths) {
        return paths.size();
    }

    public static int getResource(BObject client, BArray path) {
        return path.size();
    }

    public static int getResource(BObject client, BArray paths, double value, BString str) {
        return paths.size();
    }

    public static int getResource(BObject client, BArray path, BString p2, double value, BString str) {
        return path.size();
    }

    public static int getResource(BObject client, long p1, BString p2, double value, BString str) {
        return 1;
    }

    public static BString getResource(Environment env, BObject client, BArray path, BString str) {
        return str;
    }

    public static BString getResource(Environment env, BArray path, BObject client, BString str, BArray arr) {
        return str;
    }

    public static BString getResource(Environment env, BObject client, BArray path, BString str, BArray arr) {
        return str;
    }

    public static BString getResourceOne(Environment env, BObject client, BArray path, BTypedesc recordType) {
        return StringUtils.fromString("getResourceOne");
    }

    public static BString getResourceTwo(Environment env, BObject client, BTypedesc recordType) {
        return StringUtils.fromString("getResourceTwo");
    }

    public static BString getStringWithBalEnv(Environment env) {
        return StringUtils.fromString("Hello World!");
    }

    public static Object getIntWithBalEnv(Environment env) {
        return 7;
    }

    public static BMap<BString, Object> getMapValueWithBalEnv(Environment env, BString name, long age,
                                             MapValue<BString, Object> results) {
        BMap<BString, Object> output = ValueCreator.createMapValue();
        output.put(StringUtils.fromString("name"), name);
        output.put(StringUtils.fromString("age"), age);
        output.put(StringUtils.fromString("results"), results);
        return output;
    }

    public static BString testOverloadedMethods(Environment env, BArray arr, BString str) {
        return str;
    }

    public static BString testOverloadedMethods(ArrayValue obj, BString str) {
        return str;
    }

    public static Object getResource(Environment env, BObject client, BArray path, BArray args) {
        return 5;
    }

    public static Object getResourceWithBundledParams(BObject client, BArray path, BArray args) {
        return 1;
    }

    public static Object getResource(Environment env, BObject client, BArray args) {
        return 10;
    }

    public static Object getResourceMethod(BObject service, BArray path) {
        return 1000;
    }
}
