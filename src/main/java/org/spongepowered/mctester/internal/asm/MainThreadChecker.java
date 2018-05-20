package org.spongepowered.mctester.internal.asm;

import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.lib.AnnotationVisitor;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.lib.ClassVisitor;
import org.spongepowered.asm.lib.MethodVisitor;
import org.spongepowered.asm.lib.Opcodes;

import java.io.IOException;

public class MainThreadChecker extends ClassVisitor {

    public static void checkClass(Class<?> clazz) {
        byte bytecode[];
        try {
            bytecode = Launch.classLoader.getClassBytes(clazz.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ClassReader reader = new ClassReader(bytecode);
        MainThreadChecker checker = new MainThreadChecker();
        reader.accept(checker, 0);
    }

    public MainThreadChecker() {
        super(Opcodes.ASM5);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new MethodChecker(name);
    }
}

class MethodChecker extends MethodVisitor {

    private String name;

    public MethodChecker(String name) {
        super(Opcodes.ASM5);
        this.name = name;
    }

    private boolean isTestMethod;

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals("Lorg/junit/Test;")) {
            this.isTestMethod = true;
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (this.isTestMethod) {
            // All calls to SpongeAPI methods should happen within a call to TestUtils.runOnMainThread - and therefore,
            // in an anonymous class
            if (owner.startsWith("org/spongepowered/api")) {
                throw new IllegalStateException(String.format("Test method %s tried to call method: %s.%s(%s) from off the main thread!", this.name, owner, name, desc));
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
