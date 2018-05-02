package org.spongepowered.mctester.main.framework.proxy;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.mctester.main.message.RPCKeys;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RemoteInvocationData extends InvocationData implements DataSerializable {

    public RemoteInvocationData(Object realObject, Method method, List<?> args) {
        super(realObject, method, args);
    }

    public RemoteInvocationData(InvocationData data) {
        super(data.realObject, data.method, data.args);
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(RPCKeys.CLASS_NAME, this.method.getDeclaringClass().getCanonicalName())
                .set(RPCKeys.METHOD_NAME, this.method.getName())
                .set(RPCKeys.PARAM_TYPES, Arrays.stream(this.method.getParameterTypes()).map(Class::getCanonicalName).collect(Collectors.toList()))
                .set(RPCKeys.PARAMS.getQuery(), this.args);
    }
}
