package com.jackywong.generator.processor.factory;

import javax.lang.model.element.Element;

/**
 * Created by huangziqi on 2019/4/2
 */

/* 解析注解时报的的错 */
public class ProcessingException extends Exception {

    Element element;

    public ProcessingException(Element element, String msg, Object... args) {
        super(String.format(msg, args));
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

}