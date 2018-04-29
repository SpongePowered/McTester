package pw.aaron1011.mctester.message;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.text.Text;

public class RPCTokens {

    public static final TypeToken<ListValue<String>> LIST_STRING_VALUE_TOKEN = new TypeToken<ListValue<String>>() {
        private static final long serialVersionUID = -1L;
    };

    public static final TypeToken<ListValue<Object>> LIST_OBJECT_VALUE_TOKEN = new TypeToken<ListValue<Object>>() {
        private static final long serialVersionUID = -1L;
    };

    public static final TypeToken<Value<Object>> OBJECT_VALUE_TOKEN = new TypeToken<Value<Object>>() {
        private static final long serialVersionUID = -1L;
    };


}
