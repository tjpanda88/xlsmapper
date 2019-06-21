package com.gh.mygreen.xlsmapper.fieldaccessor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gh.mygreen.xlsmapper.util.ArgUtils;
import com.gh.mygreen.xlsmapper.util.Utils;

/**
 * {@link CommentGetter}のインスタンスを作成する。
 * 
 * @since 2.0
 * @author T.TSUCHIE
 *
 */
public class CommentGetterFactory {
    
    private static final Logger log = LoggerFactory.getLogger(CommentGetterFactory.class);
    
    /**
     * フィールドのコメント情報を取得するためのアクセッサを作成します。
     * @param beanClass フィールドが定義されているクラス情報
     * @param fieldName フィールドの名称
     * @return コメント情報のgetterが存在しない場合は空を返す。
     * @throws IllegalArgumentException {@literal beanClass == null or fieldName == null}
     * @throws IllegalArgumentException {@literal fieldName.isEmpty() = true}
     */
    public Optional<CommentGetter> create(final Class<?> beanClass, final String fieldName) {
        
        ArgUtils.notNull(beanClass, "beanClass");
        ArgUtils.notEmpty(fieldName, "fieldName");
        
        // フィールド Map commentsの場合
        Optional<CommentGetter> CommentGetter = createMapField(beanClass, fieldName);
        if(CommentGetter.isPresent()) {
            return CommentGetter;
        }
        
        // setter メソッドの場合
        CommentGetter = createMethod(beanClass, fieldName);
        if(CommentGetter.isPresent()) {
            return CommentGetter;
        }
        
        // フィールド + commentの場合
        CommentGetter = createField(beanClass, fieldName);
        if(CommentGetter.isPresent()) {
            return CommentGetter;
        }
        
        
        return Optional.empty();
        
        
    }
    
    private Optional<CommentGetter> createMapField(final Class<?> beanClass, final String fieldName) {
        
        final Field commentsField;
        try {
            commentsField = beanClass.getDeclaredField("comments");
            commentsField.setAccessible(true);
            
        } catch (NoSuchFieldException | SecurityException e) {
            // フィールドが見つからない場合は、何もしない。
            return Optional.empty();
        }
        
        if(!Map.class.isAssignableFrom(commentsField.getType())) {
            return Optional.empty();
        }
        
        final ParameterizedType type = (ParameterizedType) commentsField.getGenericType();
        final Class<?> keyType = (Class<?>) type.getActualTypeArguments()[0];
        final Class<?> valueType = (Class<?>) type.getActualTypeArguments()[1];
        
        if(keyType.equals(String.class) && valueType.equals(String.class)) {
            return Optional.of(new CommentGetter() {
                
                @SuppressWarnings("unchecked")
                @Override
                public Optional<String> get(final Object beanObj) {
                    ArgUtils.notNull(beanObj, "beanObj");
                    
                    try {
                        Map<String, String> commentsMapObj = (Map<String, String>) commentsField.get(beanObj);
                        if(commentsMapObj == null) {
                            return Optional.empty();
                        }
                        
                        return Optional.ofNullable(commentsMapObj.get(fieldName));
                        
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new RuntimeException("fail access comments field.", e);
                    }
                }
            });
            
        } else {
            // タイプが一致しない場合
            log.warn("not match generics type of comments. key type:{}, value type:{}.", keyType.getName(), valueType.getName());
            return Optional.empty();
        }
        
    }
    
    private Optional<CommentGetter> createMethod(final Class<?> beanClass, final String fieldName) {
        
        final String commentMethodName = "get" + Utils.capitalize(fieldName) + "Comment";
        
        final Method method;
        try {
            method = beanClass.getDeclaredMethod(commentMethodName);
            method.setAccessible(true);
            
        } catch (NoSuchMethodException | SecurityException e) {
            return Optional.empty();
        }
        
        if(method.getReturnType().equals(String.class)) {
            return Optional.of(new CommentGetter() {
                
                @Override
                public Optional<String> get(final Object beanObj) {
                    ArgUtils.notNull(beanObj, "beanObj");
                    
                    try {
                        final String address = (String)method.invoke(beanObj);
                        return Optional.ofNullable(address);
                        
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        throw new RuntimeException("fail access comments getter method.", e);
                    }
                    
                }
            });
            
        }
        
        return Optional.empty();
        
    }
    
    private Optional<CommentGetter> createField(final Class<?> beanClass, final String fieldName) {
        
        final String commentFieldName = fieldName + "Comment";
        
        final Field commentField;
        try {
            commentField = beanClass.getDeclaredField(commentFieldName);
            commentField.setAccessible(true);
            
        } catch (NoSuchFieldException | SecurityException e) {
            return Optional.empty();
        }
        
        if(commentField.getType().equals(String.class)) {
            
            return Optional.of(new CommentGetter() {
                
                @Override
                public Optional<String> get(final Object beanObj) {
                    ArgUtils.notNull(beanObj, "beanObj");
                    
                    try {
                        final String comment = (String) commentField.get(beanObj);
                        return Optional.ofNullable(comment);
                        
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new RuntimeException("fail access comment field.", e);
                    }
                }
            });
            
        }
        
        return Optional.empty();
    }
    
}
