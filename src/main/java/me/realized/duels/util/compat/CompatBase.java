package me.realized.duels.util.compat;

import java.lang.reflect.Method;
import me.realized.duels.util.ReflectionUtil;
import org.bukkit.inventory.ItemStack;

class CompatBase {

    static final Method AS_NMS_COPY;
    static final Method AS_BUKKIT_COPY;
    
    static final Class<?> TAG_COMPOUND;
    static final Class<?> TAG_LIST;
    static final Class<?> TAG_BASE;
    
    static final Method GET_TAG;
    static final Method SET_TAG;
    static final Method GET;
    static final Method SET;
    static final Method ADD;
    static final Method SIZE;
    static final Method GET_LIST;
    static final Method GET_STRING;
    static final Method SET_STRING;
    static final Method GET_INT;
    static final Method SET_INT;
    static final Method GET_DOUBLE;
    static final Method SET_DOUBLE;
    static final Method SET_LONG;
    static final Method GET_COMPOUND;
    
    static {
        final Class<?> CB_ITEMSTACK = ReflectionUtil.getCBClass("inventory.CraftItemStack");
        final Class<?> NMS_ITEMSTACK = ReflectionUtil.getNMSClass("ItemStack");
        AS_NMS_COPY = ReflectionUtil.getMethod(CB_ITEMSTACK, "asNMSCopy", ItemStack.class);
        AS_BUKKIT_COPY = ReflectionUtil.getMethod(CB_ITEMSTACK, "asBukkitCopy", NMS_ITEMSTACK);
        
        TAG_COMPOUND = ReflectionUtil.getNMSClass("NBTTagCompound");
        TAG_LIST = ReflectionUtil.getNMSClass("NBTTagList");
        TAG_BASE = ReflectionUtil.getNMSClass("NBTBase");
        
        GET_TAG = ReflectionUtil.getMethod(NMS_ITEMSTACK, "getTag");
        SET_TAG = ReflectionUtil.getMethod(NMS_ITEMSTACK, "setTag", TAG_COMPOUND);
        SET = ReflectionUtil.getMethod(TAG_COMPOUND, "set", String.class, TAG_BASE);
        GET_LIST = ReflectionUtil.getMethod(TAG_COMPOUND, "getList", String.class, int.class);
        GET = ReflectionUtil.getMethod(TAG_LIST, "get", int.class);
        ADD = ReflectionUtil.getMethod(TAG_LIST, "add", TAG_BASE);
        SIZE = ReflectionUtil.getMethod(TAG_LIST, "size");
        GET_STRING = ReflectionUtil.getMethod(TAG_COMPOUND, "getString", String.class);
        SET_STRING = ReflectionUtil.getMethod(TAG_COMPOUND, "setString", String.class, String.class);
        GET_INT = ReflectionUtil.getMethod(TAG_COMPOUND, "getInt", String.class);
        SET_INT = ReflectionUtil.getMethod(TAG_COMPOUND, "setInt", String.class, int.class);
        GET_DOUBLE = ReflectionUtil.getMethod(TAG_COMPOUND, "getDouble", String.class);
        SET_DOUBLE = ReflectionUtil.getMethod(TAG_COMPOUND, "setDouble", String.class, double.class);
        SET_LONG = ReflectionUtil.getMethod(TAG_COMPOUND, "setLong", String.class, long.class);
        GET_COMPOUND = ReflectionUtil.getMethod(TAG_COMPOUND, "getCompound", String.class);
    }
}
