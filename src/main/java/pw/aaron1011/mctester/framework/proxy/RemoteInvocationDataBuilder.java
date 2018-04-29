package pw.aaron1011.mctester.framework.proxy;

import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import pw.aaron1011.mctester.Utils;
import pw.aaron1011.mctester.message.RPCKeys;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RemoteInvocationDataBuilder implements DataBuilder<RemoteInvocationData> {

    private final Object realObject;

    public RemoteInvocationDataBuilder(Object realObject) {
        this.realObject = realObject;
    }

    @Override
    public Optional<RemoteInvocationData> build(DataView container) throws InvalidDataException {
        if (!(RPCKeys.ALL_KEYS.stream().allMatch(container::contains))) {
            return Optional.empty();
        }

        Class<?> clazz = Utils.classForName(container.getString(RPCKeys.CLASS_NAME.getQuery()).get());

        String methodName = container.getString(RPCKeys.METHOD_NAME.getQuery()).get();
        Class<?>[] paramTypes =
                    container.getStringList(RPCKeys.PARAM_TYPES.getQuery()).get().stream().map(Utils::classForName).toArray(Class<?>[]::new);

        Method method;
        try {
            method = clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            throw new InvalidDataException(e);
        }

        List<Object> rawParams = (List) container.getList(RPCKeys.PARAMS.getQuery()).get();
        List<Object> finalParams = new ArrayList<>(rawParams.size());
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            Object rawParam = rawParams.get(i);
            if (rawParam instanceof DataView) {
                finalParams.add(Utils.dataToArbitrary((DataView) rawParam, paramType));
            } else {
                finalParams.add(rawParam);
            }

            /*Utils.dataToArbitrary(rawParam).
            finalParams.add(Utils.dataToArbitrary(rawParam, paramType));*/
        }
        return Optional.of(new RemoteInvocationData(this.realObject, method, finalParams));
    }

}
