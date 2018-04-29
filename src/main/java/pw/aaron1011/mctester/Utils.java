package pw.aaron1011.mctester;

import org.apache.commons.lang3.ClassUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

public class Utils {

    public static DataContainer arbitraryToData(Object object) {
        if (object instanceof DataSerializable) {
            return ((DataSerializable) object).toContainer();
        }
        DataTranslator<Object> translator = (DataTranslator<Object>) Sponge.getDataManager().getTranslator(((Object) object).getClass()).get();
        return translator.translate(object);
    }

    public static Optional<Object> dataToArbitrary(DataView view, Class<?> type) {
        Optional<DataBuilder<?>> builder = (Optional) Sponge.getDataManager().getBuilder((Class<DataSerializable>) type);
        Optional<DataTranslator<?>> translator = (Optional) Sponge.getDataManager().getTranslator(type);
        if (builder.isPresent()) {
            return Optional.of(builder.get().build(view).get());
        } else if (translator.isPresent()) {
            return Optional.of(translator.get().translate(view));
        }
        return Optional.empty();
    }

    public static Class<?> classForName(String name) throws InvalidDataException {
        try {
            return ClassUtils.getClass(name); // Handles primitives
        } catch (ClassNotFoundException e) {
            throw new InvalidDataException(e);
        }
    }
}
