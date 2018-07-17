/**
 * This package is special - it has a classlaoder exclusion applied.
 * JUNit is responsbile for loading MinecraftRunner from the @Runner annotation,
 * which means that it will be loaded on the AppClassLoader. This means that
 * MinecraftRunner, and any classes that it
 */
package org.spongepowered.mctester.api.junit;
