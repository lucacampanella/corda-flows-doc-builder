package com.github.lucacampanella.callgraphflows.utils;

import com.github.lucacampanella.callgraphflows.staticanalyzer.matchers.MatcherHelper;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.visitor.chain.CtQueryable;
import spoon.template.TemplateMatcher;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static final boolean REMOVE_PKG_INFO = true;
    public static final boolean REMOVE_UNWRAP = true;

    private Utils() {
        //private constructor to hide public one
    }

    public static String removePackageDescription(String code) {
        Pattern pattern = Pattern.compile("([a-z0-9]|\\.)+\\.([A-Z])");
        Matcher matcher = pattern.matcher(code);
        StringBuilder sb = new StringBuilder(code);
        int removedCharacters = 0;
        while (matcher.find()) {
            int startPoint = matcher.start();
            int endPoint = matcher.end()-1;
            if(matcher.start()-1 < 0 || !(code.charAt(matcher.start()-1) >= 'A'
                    &&  code.charAt(matcher.start()-1) <= 'Z')) {
                sb.delete(startPoint - removedCharacters, endPoint - removedCharacters);
                removedCharacters += endPoint - startPoint;
            }
        }

        return sb.toString().replaceAll("this\\.", "");
    }

    public static String removeUnwrapIfWanted(CtQueryable queryable, String code) {
        if(!REMOVE_UNWRAP) {
            return code;
        }

        final TemplateMatcher unwrapTemplate = MatcherHelper.getMatcher("unwrapTemplate");
        final List<CtExpression> matches = queryable.filterChildren(unwrapTemplate).list();
        if(matches.isEmpty()) {
            return code;
        }
        CtInvocation invocation = (CtInvocation) matches.get(0);
        StringBuilder unwrapText = new StringBuilder(
                invocation.toString().replace(invocation.getTarget().toString(), ""));
        while (unwrapText.charAt(0) == '(') {
            unwrapText.deleteCharAt(0);
            unwrapText.deleteCharAt(unwrapText.length()-1);
        }
        code = code.replace(unwrapText, "");
        return code;
    }

    public static String removePackageDescriptionIfWanted(String code) {
        return REMOVE_PKG_INFO ? removePackageDescription(code) : code;
    }

    public static String fromStatementToString(CtStatement stmt) {
        return Utils.REMOVE_PKG_INFO ? Utils.removePackageDescription(stmt.toString()) : stmt.toString();
    }
}
