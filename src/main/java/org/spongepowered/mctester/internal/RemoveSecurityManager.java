package org.spongepowered.mctester.internal;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

// Inspired by http://weblog.ikvm.net/2011/08/01/HowToDisableTheJavaSecurityManager.aspx
public class RemoveSecurityManager {


    // Horrendously awful hack, needed until
    // https://github.com/gradle/gradle/issues/5305 is resolved
    @SuppressWarnings("deprecation")
    public static void clearSecurityManager() {
        if (System.getSecurityManager() == null) {
            return;
        }

        Unsafe unsafe = getUnsafe();

        Object systemBase = null;

        // Copied from Unsafe#staticFieldBase, since OracleJDK
        // seems to lack the staticFieldBase(Class) overload
        Field[] fields = System.class.getDeclaredFields();
        for(int i = 0; i < fields.length; i++) {
            if (Modifier.isStatic(fields[i].getModifiers())) {
                systemBase = unsafe.staticFieldBase(fields[i]);
                break;
            }
        }
        if (systemBase == null) {
            // lol what
            throw new IllegalStateException("Failed to find static field for System!?!?!?");
        }
        long securityOffset = calcSecurityOffset(unsafe);

        Object manager = unsafe.getObject(systemBase, securityOffset);
        if (manager instanceof SecurityManager) {
            System.err.println("Found SecurityManager, clearing via Unsafe");
            unsafe.putObject(systemBase, securityOffset, null);
        }
        if (System.getSecurityManager() != null) {
            throw new RuntimeException("Failed to replace SecurityManager!");
        }
    }

    private static Unsafe getUnsafe() {
        try {
            Field theUnasfe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnasfe.setAccessible(true);
            return (Unsafe) theUnasfe.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static long calcSecurityOffset(Unsafe unsafe) {
        try {
            Field out = System.class.getDeclaredField("out");
            Field err = System.class.getDeclaredField("err");

            long outOffset = unsafe.staticFieldOffset(out);
            long errOffset = unsafe.staticFieldOffset(err);

            return errOffset + (errOffset - outOffset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
