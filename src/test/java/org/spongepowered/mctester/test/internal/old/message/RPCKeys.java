package org.spongepowered.mctester.test.internal.old.message;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.TypeTokens;

import java.util.List;

public class RPCKeys {

    public static final Key<Value<String>> CLASS_NAME = Key.builder().type(TypeTokens.STRING_VALUE_TOKEN).id("mctester:class_name").name("ClassName").query(DataQuery.of("ClassName")).build();
    public static final Key<Value<String>> METHOD_NAME = Key.builder().type(TypeTokens.STRING_VALUE_TOKEN).id("mctester:method_name").name("MethodName").query(DataQuery.of("MethodName")).build();
    public static final Key<ListValue<String>> PARAM_TYPES = Key.builder().type(RPCTokens.LIST_STRING_VALUE_TOKEN).id("mctester:param_types").name("ParamTypes").query(DataQuery.of("ParamTypes")).build();
    public static final Key<ListValue<Object>> PARAMS = Key.builder().type(RPCTokens.LIST_OBJECT_VALUE_TOKEN).id("mctester:params").name("Params").query(DataQuery.of("Params")).build();
    public static final Key<Value<Object>> RESPONSE = Key.builder().type(RPCTokens.OBJECT_VALUE_TOKEN).id("mctester:response").name("Response").query(DataQuery.of("Response")).build();

    public static final List<Key<?>> ALL_KEYS = ImmutableList.of(CLASS_NAME, METHOD_NAME, PARAM_TYPES, PARAMS);

}
