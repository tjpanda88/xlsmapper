package com.gh.mygreen.xlsmapper.textformatter;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.gh.mygreen.xlsmapper.util.ArgUtils;

/**
 * 列挙型のフォーマッタ。
 *
 * @since 2.0
 * @author T.TSUCHIE
 *
 * @param <T> 列挙型
 */
public class EnumFormatter<T extends Enum<T>> implements TextFormatter<T> {
    
    private final Class<? extends Enum<?>> type;
    
    private final boolean ignoreCase;
    
    private final Optional<Method> selectorMethod;
    
    /**
     * キーが列挙型、値が文字列のマップ。
     */
    private final Map<Enum<?>, String> toStringMap;
    
    /**
     * キーが文字列、値が列挙型のマップ
     */
    private final Map<String, Enum<?>> toObjectMap;
    
    public EnumFormatter(final Class<T> type, final boolean ignoreCase) {
        ArgUtils.notNull(type, "type");
        
        this.type = type;
        this.ignoreCase = ignoreCase;
        this.selectorMethod = Optional.empty();
        
        this.toStringMap = createToStringMap(type);
        this.toObjectMap = createToObjectMap(type, ignoreCase);
    }
    
    public EnumFormatter(final Class<T> type, final boolean ignoreCase, final String selector) {
        ArgUtils.notNull(type, "type");
        ArgUtils.notEmpty(selector, "selector");
        
        this.type = type;
        this.ignoreCase = ignoreCase;
        this.selectorMethod = Optional.of(getEnumSelectorMethod(type, selector));
        
        this.toStringMap = createToStringMap(type, selector);
        this.toObjectMap = createToObjectMap(type, ignoreCase, selector);
        
    }
    
    public EnumFormatter(final Class<T> type) {
        this(type, false);
    }
    
    public EnumFormatter(final Class<T> type, final String selector) {
        this(type, false, selector);
    }
    
    private static <T extends Enum<T>> Method getEnumSelectorMethod(final Class<T> enumClass, final String selector) {
        
        try {
            final Method method = enumClass.getMethod(selector);
            method.setAccessible(true);
            return method;
            
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(String.format("not found method '%s'", selector), e);
        }
        
    }
    
    private static <T extends Enum<T>> Map<Enum<?>, String> createToStringMap(final Class<T> enumClass) {
        
        final EnumSet<T> set = EnumSet.allOf(enumClass);
        
        final Map<Enum<?>, String> map = new LinkedHashMap<>();
        for(T e : set) {
            map.put(e, e.name());
            
        }
        
        return Collections.unmodifiableMap(map);
    }
    
    private static <T extends Enum<T>> Map<Enum<?>, String> createToStringMap(final Class<T> enumClass, final String selector) {
        
        final Method method = getEnumSelectorMethod(enumClass, selector);
        
        final Map<Enum<?>, String> map = new LinkedHashMap<>();
        try {
            final EnumSet<T> set = EnumSet.allOf(enumClass);
            for(T e : set) {
                Object returnValue = method.invoke(e);
                map.put(e, returnValue.toString());
                
            }
            
        } catch(ReflectiveOperationException e) {
            throw new RuntimeException("fail get enum value.", e);
        }
        
        return Collections.unmodifiableMap(map);
    }
    
    private static <T extends Enum<T>> Map<String, Enum<?>> createToObjectMap(final Class<T> enumClass, final boolean ignoreCase) {
        
        final EnumSet<T> set = EnumSet.allOf(enumClass);
        
        final Map<String, Enum<?>> map = new LinkedHashMap<>();
        for(T e : set) {
            final String key = (ignoreCase ? e.name().toLowerCase() : e.name());
            map.put(key, e);
            
        }
        
        return Collections.unmodifiableMap(map);
    }
    
    private static <T extends Enum<T>> Map<String, Enum<?>> createToObjectMap(final Class<T> enumClass, final boolean ignoreCase,
            final String selector) {
        
        final Method method = getEnumSelectorMethod(enumClass, selector);
        
        final Map<String, Enum<?>> map = new LinkedHashMap<>();
        try {
            
            EnumSet<T> set = EnumSet.allOf(enumClass);
            for(T e : set) {
                Object returnValue = method.invoke(e);
                final String key = (ignoreCase ? returnValue.toString().toLowerCase() : returnValue.toString());
                
                map.put(key, e);
            }
            
        } catch(ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        
        return Collections.unmodifiableMap(map);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public T parse(final String text) throws TextParseException {
        final String keyText = ignoreCase ? text.toLowerCase() : text;
        final Optional<T> obj = Optional.ofNullable((T)toObjectMap.get(keyText));
        
        return obj.orElseThrow(() -> {
            
            final Map<String, Object> vars = new HashMap<>();
            vars.put("type", getType().getName());
            vars.put("ignoreCase", isIgnoreCase());
            
            getSelectorMethod().ifPresent(method -> vars.put("selector", method.getName()));
            vars.put("enums", getToStringMap().values());
            
            return new TextParseException(text, type, vars);
        });
    }
    
    @Override
    public String format(final T value) {
        final Optional<String> text = Optional.ofNullable(toStringMap.get(value));
        return text.orElseGet(() -> value.toString());
    }
    
    public Class<? extends Enum<?>> getType() {
        return type;
    }
    
    public boolean isIgnoreCase() {
        return ignoreCase;
    }
    
    public Optional<Method> getSelectorMethod() {
        return selectorMethod;
    }
    
    public Map<Enum<?>, String> getToStringMap() {
        return toStringMap;
    }
    
    public Map<String, Enum<?>> getToObjectMap() {
        return toObjectMap;
    }
    
}
