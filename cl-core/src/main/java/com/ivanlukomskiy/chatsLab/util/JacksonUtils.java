package com.ivanlukomskiy.chatsLab.util;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Provides access to singleton object mapper instance
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 04.10.2017.
 */
public class JacksonUtils {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
}
