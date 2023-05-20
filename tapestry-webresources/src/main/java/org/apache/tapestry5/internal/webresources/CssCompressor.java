/*
 * YUI Compressor
 * http://developer.yahoo.com/yui/compressor/
 * Author: Julien Lecomte -  http://www.julienlecomte.net/
 * Author: Isaac Schlueter - http://foohack.com/
 * Author: Stoyan Stefanov - http://phpied.com/
 * Contributor: Dan Beam - http://danbeam.org/
 * Copyright (c) 2013 Yahoo! Inc.  All rights reserved.
 * The copyrights embodied in the content of this file are licensed
 * by Yahoo! Inc. under the BSD (revised) open source license.
 */
package org.apache.tapestry5.internal.webresources;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

public class CssCompressor {

    private static final Pattern PRESERVE_TOKEN_URL = Pattern.compile("(?i)url\\(\\s*([\"']?)data\\:");
    private static final Pattern PRESERVE_TOKEN_CALC = Pattern.compile("(?i)calc\\(\\s*([\"']?)");
    private static final Pattern PRESERVE_TOKEN_PROGID_DX_IMAGE_TRANSFORM_MICROSOFT_MATRIX = Pattern.compile("(?i)progid:DXImageTransform.Microsoft.Matrix\\s*([\"']?)");

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private static final Pattern PRESERVE_STRINGS = Pattern.compile("(\"([^\\\\\"]|\\\\.|\\\\)*\")|(\'([^\\\\\']|\\\\.|\\\\)*\')");
    private static final Pattern MINIFY_ALPHA_OPACITY_FILTER_STRINGS = Pattern.compile("(?i)progid:DXImageTransform.Microsoft.Alpha\\(Opacity=");
    private static final Pattern UNNECESSARY_SPACES1 = Pattern.compile("(^|\\})((^|([^\\{:])+):)+([^\\{]*\\{)");
    private static final Pattern UNNECESSARY_SPACES2 = Pattern.compile("\\s+([!{};:>+\\(\\)\\],])");
    private static final Pattern IMPORTANT = Pattern.compile("!important");
    private static final Pattern PSEUDO_CLASS_COLON = Pattern.compile("___YUICSSMIN_PSEUDOCLASSCOLON___");
    private static final Pattern IE6_SPACE = Pattern.compile("(?i):first\\-(line|letter)(\\{|,)");
    private static final Pattern CHARSET_DIRECTIVE = Pattern.compile("(?i)^(.*)(@charset)( \"[^\"]*\";)");
    private static final Pattern CHARSET_MULTIPLE = Pattern.compile("(?i)^((\\s*)(@charset)( [^;]+;\\s*))+");
    private static final Pattern LOWERCASE_DIRECTIVES = Pattern.compile("(?i)@(font-face|import|(?:-(?:atsc|khtml|moz|ms|o|wap|webkit)-)?keyframe|media|page|namespace)");
    private static final Pattern LOWERCAUSE_PSEUDO_CLASSES = Pattern.compile("(?i):(active|after|before|checked|disabled|empty|enabled|first-(?:child|of-type)|focus|hover|last-(?:child|of-type)|link|only-(?:child|of-type)|root|:selection|target|visited)");
    private static final Pattern LOWERCASE_FUNCTIONS1 = Pattern.compile("(?i):(lang|not|nth-child|nth-last-child|nth-last-of-type|nth-of-type|(?:-(?:moz|webkit)-)?any)\\(");
    private static final Pattern LOWERCASE_FUNCTIONS2 = Pattern.compile("(?i)([:,\\( ]\\s*)(attr|color-stop|from|rgba|to|url|(?:-(?:atsc|khtml|moz|ms|o|wap|webkit)-)?(?:calc|max|min|(?:repeating-)?(?:linear|radial)-gradient)|-webkit-gradient)");
    private static final Pattern RESTORE_AND_SPACE = Pattern.compile("(?i)\\band\\(");
    private static final Pattern TRAILING_SPACES = Pattern.compile("([!{}:;>+\\(\\[,])\\s+");
    private static final Pattern UNNECESSARY_SEMICOLON = Pattern.compile(";+}");
    private static final Pattern ZERO_UNITS = Pattern.compile("(?i)(^|: ?)((?:[0-9a-z-.]+ )*?)?(?:0?\\.)?0(?:px|em|in|cm|mm|pc|pt|ex|deg|g?rad|k?hz)");
    private static final Pattern ZERO_PERCENTAGE = Pattern.compile("(?i)(: ?)((?:[0-9a-z-.]+ )*?)?(?:0?\\.)?0(?:%)");
    private static final Pattern KEYFRAME_TO = Pattern.compile("(?i)(^|,|\\{) ?(?:100% ?\\{)");
    private static final Pattern ZERO_UNITS_GROUPS = Pattern.compile("(?i)\\( ?((?:[0-9a-z-.]+[ ,])*)?(?:0?\\.)?0(?:px|em|%|in|cm|mm|pc|pt|ex|deg|g?rad|m?s|k?hz)");
    private static final Pattern UNNECESSARY_DOT_ZERO1 = Pattern.compile("([0-9])\\.0(px|em|%|in|cm|mm|pc|pt|ex|deg|m?s|g?rad|k?hz| |;)");
    private static final Pattern UNNECESSARY_DOT_ZERO2 = Pattern.compile("([ |:])\\.0(px|em|%|in|cm|mm|pc|pt|ex|deg|m?s|g?rad|k?hz| |;)");
    private static final Pattern ZERO_VALUE_1 = Pattern.compile(":0 0 0 0(;|})");
    private static final Pattern ZERO_VALUE_2 = Pattern.compile(":0 0 0(;|})");
    private static final Pattern ZERO_VALUE_3 = Pattern.compile("(?<!flex):0 0(;|\\})");
    private static final Pattern BACKGROUND_POSITION_TRANSFORM_ORIGIN = Pattern.compile("(?i)(background-position|webkit-mask-position|transform-origin|webkit-transform-origin|moz-transform-origin|o-transform-origin|ms-transform-origin):0(;|})");
    private static final Pattern RESTORE_DOT_ZERO = Pattern.compile("(:|\\s)0+\\.(\\d+)");
    private static final Pattern RGB = Pattern.compile("rgb\\s*\\(\\s*([0-9,\\s]+)\\s*\\)");
    private static final Pattern HEX_COLORS = Pattern.compile("(\\=\\s*?[\"']?)?" + "#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])" + "(:?\\}|[^0-9a-fA-F{][^{]*?\\})");
    private static final Pattern COLOR_RED = Pattern.compile("(:|\\s)(#f00)(;|})");
    private static final Pattern COLOR_NAVY = Pattern.compile("(:|\\s)(#000080)(;|})");
    private static final Pattern COLOR_GRAY = Pattern.compile("(:|\\s)(#808080)(;|})");
    private static final Pattern COLOR_OLIVE = Pattern.compile("(:|\\s)(#808000)(;|})");
    private static final Pattern COLOR_PURPLE = Pattern.compile("(:|\\s)(#800080)(;|})");
    private static final Pattern COLOR_SILVER = Pattern.compile("(:|\\s)(#c0c0c0)(;|})");
    private static final Pattern COLOR_TEAL = Pattern.compile("(:|\\s)(#008080)(;|})");
    private static final Pattern COLOR_ORANGE = Pattern.compile("(:|\\s)(#ffa500)(;|})");
    private static final Pattern COLOR_MAROON = Pattern.compile("(:|\\s)(#800000)(;|})");
    private static final Pattern NONE = Pattern.compile("(?i)(border|border-top|border-right|border-bottom|border-left|outline|background):none(;|})");
    private static final Pattern OPERA_DEVICE_PIXEL_RATIO = Pattern.compile("\\(([\\-A-Za-z]+):([0-9]+)\\/([0-9]+)\\)");
    private static final Pattern EMPTY_RULE = Pattern.compile("[^\\}\\{/;]+\\{\\}");
    private static final Pattern MULTI_SEMICOLON = Pattern.compile(";;+");
    private static final Pattern CALC = Pattern.compile("calc\\([^\\)]*\\)");
    private static final Pattern CALC_PLUS = Pattern.compile("(?<=[-|%|px|em|rem|vw|\\d]+)\\+");
    private static final Pattern CALC_MINUS = Pattern.compile("(?<=[-|%|px|em|rem|vw|\\)|\\d]+)(?<!var\\([a-z-0-9]+)\\-");
    private static final Pattern CALC_MULTI = Pattern.compile("(?<=[-|%|px|em|rem|vw|\\d]+)\\*");
    private static final Pattern CALC_DIV = Pattern.compile("(?<=[-|%|px|em|rem|vw|\\d]+)\\/");

    /**
     * @param css - full css string
     * @param preservedToken - token to preserve
     * @param tokenRegex - regex to find token
     * @param removeWhiteSpace - remove any white space in the token
     * @param preservedTokens - array of token values
     * @return
     */
    private static String preserveToken(String css, String preservedToken,
            Pattern tokenRegex, boolean removeWhiteSpace, List<String> preservedTokens) {

        int maxIndex = css.length() - 1;
        int appendIndex = 0;

        StringBuilder sb = new StringBuilder();

        Pattern p = tokenRegex;
        Matcher m = p.matcher(css);

        while (m.find()) {
            int startIndex = m.start() + (preservedToken.length() + 1);
            String terminator = m.group(1);

            // skip this, if CSS was already copied to "sb" upto this position
            if (m.start() < appendIndex) {
                continue;
            }

            if (terminator.length() == 0) {
                terminator = ")";
            }

            boolean foundTerminator = false;

            int endIndex = m.end() - 1;
            while(foundTerminator == false && endIndex+1 <= maxIndex) {
                endIndex = css.indexOf(terminator, endIndex+1);

                if (endIndex <= 0) {
                    break;
                } else if ((endIndex > 0) && (css.charAt(endIndex-1) != '\\')) {
                    foundTerminator = true;
                    if (!")".equals(terminator)) {
                        endIndex = css.indexOf(")", endIndex);
                    }
                }
            }

            // Enough searching, start moving stuff over to the buffer
            sb.append(css.substring(appendIndex, m.start()));

            if (foundTerminator) {
                String token = css.substring(startIndex, endIndex);
                if(removeWhiteSpace)
                    token = WHITESPACE.matcher(token).replaceAll("");
                preservedTokens.add(token);

                String preserver = preservedToken + "(___YUICSSMIN_PRESERVED_TOKEN_" + (preservedTokens.size() - 1) + "___)";
                sb.append(preserver);

                appendIndex = endIndex + 1;
            } else {
                // No end terminator found, re-add the whole match. Should we throw/warn here?
                sb.append(css.substring(m.start(), m.end()));
                appendIndex = m.end();
            }
        }

        sb.append(css.substring(appendIndex));

        return sb.toString();
    }

    public static String compress(InputStream is)
            throws IOException {
        return compress(IOUtils.toString(is));
    }
    
    public static String compress(String uncompressedCss)
            throws IOException {

        Pattern p;
        Matcher m;
        String css = uncompressedCss;

        int startIndex = 0;
        int endIndex = 0;
        int i = 0;
        int max = 0;
        List<String> preservedTokens = new ArrayList<>();
        List<String> comments = new ArrayList<>();
        String token;
        int totallen = css.length();
        String placeholder;


        StringBuilder sb = new StringBuilder(css);

        // collect all comment blocks...
        while ((startIndex = sb.indexOf("/*", startIndex)) >= 0) {
            endIndex = sb.indexOf("*/", startIndex + 2);
            if (endIndex < 0) {
                endIndex = totallen;
            }

            token = sb.substring(startIndex + 2, endIndex);
            comments.add(token);
            sb.replace(startIndex + 2, endIndex, "___YUICSSMIN_PRESERVE_CANDIDATE_COMMENT_" + (comments.size() - 1) + "___");
            startIndex += 2;
        }
        css = sb.toString();


        css = preserveToken(css, "url", PRESERVE_TOKEN_URL, true, preservedTokens);
        css = preserveToken(css, "calc",  PRESERVE_TOKEN_CALC, false, preservedTokens);
        css = preserveToken(css, "progid:DXImageTransform.Microsoft.Matrix", PRESERVE_TOKEN_PROGID_DX_IMAGE_TRANSFORM_MICROSOFT_MATRIX , false, preservedTokens);


        // preserve strings so their content doesn't get accidentally minified
        sb = new StringBuilder();
        p = PRESERVE_STRINGS;
        m = p.matcher(css);
        while (m.find()) {
            token = m.group();
            char quote = token.charAt(0);
            token = token.substring(1, token.length() - 1);

            // maybe the string contains a comment-like substring?
            // one, maybe more? put'em back then
            if (token.indexOf("___YUICSSMIN_PRESERVE_CANDIDATE_COMMENT_") >= 0) {
                for (i = 0, max = comments.size(); i < max; i += 1) {
                    token = token.replace("___YUICSSMIN_PRESERVE_CANDIDATE_COMMENT_" + i + "___", comments.get(i).toString());
                }
            }

            // minify alpha opacity in filter strings
            token = MINIFY_ALPHA_OPACITY_FILTER_STRINGS.matcher(token).replaceAll("alpha(opacity=");

            preservedTokens.add(token);
            String preserver = quote + "___YUICSSMIN_PRESERVED_TOKEN_" + (preservedTokens.size() - 1) + "___" + quote;
            m.appendReplacement(sb, preserver);
        }
        m.appendTail(sb);
        css = sb.toString();


        // strings are safe, now wrestle the comments
        for (i = 0, max = comments.size(); i < max; i += 1) {

            token = comments.get(i).toString();
            placeholder = "___YUICSSMIN_PRESERVE_CANDIDATE_COMMENT_" + i + "___";

            // ! in the first position of the comment means preserve
            // so push to the preserved tokens while stripping the !
            if (token.startsWith("!")) {
                preservedTokens.add(token);
                css = css.replace(placeholder,  "___YUICSSMIN_PRESERVED_TOKEN_" + (preservedTokens.size() - 1) + "___");
                continue;
            }

            // \ in the last position looks like hack for Mac/IE5
            // shorten that to /*\*/ and the next one to /**/
            if (token.endsWith("\\")) {
                preservedTokens.add("\\");
                css = css.replace(placeholder,  "___YUICSSMIN_PRESERVED_TOKEN_" + (preservedTokens.size() - 1) + "___");
                i = i + 1; // attn: advancing the loop
                preservedTokens.add("");
                css = css.replace("___YUICSSMIN_PRESERVE_CANDIDATE_COMMENT_" + i + "___",  "___YUICSSMIN_PRESERVED_TOKEN_" + (preservedTokens.size() - 1) + "___");
                continue;
            }

            // keep empty comments after child selectors (IE7 hack)
            // e.g. html >/**/ body
            if (token.length() == 0) {
                startIndex = css.indexOf(placeholder);
                if (startIndex > 2) {
                    if (css.charAt(startIndex - 3) == '>') {
                        preservedTokens.add("");
                        css = css.replace(placeholder,  "___YUICSSMIN_PRESERVED_TOKEN_" + (preservedTokens.size() - 1) + "___");
                    }
                }
            }

            // in all other cases kill the comment
            css = css.replace("/*" + placeholder + "*/", "");
        }

        // preserve \9 IE hack
        final String backslash9 = "\\9"; 
        while (css.indexOf(backslash9) > -1) {
            preservedTokens.add(backslash9);
            css = css.replace(backslash9,  "___YUICSSMIN_PRESERVED_TOKEN_" + (preservedTokens.size() - 1) + "___");
        }

        // Normalize all whitespace strings to single spaces. Easier to work with that way.
        css = WHITESPACE.matcher(css).replaceAll(" ");

        // Remove the spaces before the things that should not have spaces before them.
        // But, be careful not to turn "p :link {...}" into "p:link{...}"
        // Swap out any pseudo-class colons with the token, and then swap back.
        sb = new StringBuilder();
        p = UNNECESSARY_SPACES1;
        m = p.matcher(css);
        while (m.find()) {
            String s = m.group();
            s = s.replace(":", PSEUDO_CLASS_COLON.pattern());
            s = s.replace( "\\", "\\\\" );
            s = s.replace( "$", "\\$" );
            m.appendReplacement(sb, s);
        }
        m.appendTail(sb);
        css = sb.toString();
        // Remove spaces before the things that should not have spaces before them.
        css = UNNECESSARY_SPACES2.matcher(css).replaceAll("$1");
        // Restore spaces for !important
        css = IMPORTANT.matcher(css).replaceAll(" !important");
        // bring back the colon
        css = PSEUDO_CLASS_COLON.matcher(css).replaceAll(":");

        // retain space for special IE6 cases
        sb = new StringBuilder();
        p = IE6_SPACE;
        m = p.matcher(css);
        while (m.find()) {
            m.appendReplacement(sb, ":first-" + m.group(1).toLowerCase() + " " + m.group(2));
        }
        m.appendTail(sb);
        css = sb.toString();

        // no space after the end of a preserved comment
        css = css.replace("*/ ", "*/");

        // TODO: Charset handling is broken if more than two charsets

        // If there are multiple @charset directives, push them to the top of the file.
        sb = new StringBuilder();
        p = CHARSET_DIRECTIVE;
        m = p.matcher(css);
        while (m.find()) {
            String s = m.group(1).replace("\\", "\\\\").replace("$", "\\$");
            m.appendReplacement(sb, m.group(2).toLowerCase() + m.group(3) + s);
        }
        m.appendTail(sb);
        css = sb.toString();

        // When all @charset are at the top, remove the second and after (as they are completely ignored).
        sb = new StringBuilder();
        p = CHARSET_MULTIPLE;
        m = p.matcher(css);
        while (m.find()) {
            String group2 = m.group(2);
            String group3 = m.group(3);
            String group4 = m.group(4);
            m.appendReplacement(sb, group2 + group3.toLowerCase() + group4);
        }
        m.appendTail(sb);
        css = sb.toString();

        // lowercase some popular @directives (@charset is done right above)
        sb = new StringBuilder();
        p = LOWERCASE_DIRECTIVES;
        m = p.matcher(css);
        while (m.find()) {
            m.appendReplacement(sb, '@' + m.group(1).toLowerCase());
        }
        m.appendTail(sb);
        css = sb.toString();

        // lowercase some more common pseudo-elements
        sb = new StringBuilder();
        p = LOWERCAUSE_PSEUDO_CLASSES;
        m = p.matcher(css);
        while (m.find()) {
            m.appendReplacement(sb, ':' + m.group(1).toLowerCase());
        }
        m.appendTail(sb);
        css = sb.toString();

        // lowercase some more common functions
        sb = new StringBuilder();
        p = LOWERCASE_FUNCTIONS1;
        m = p.matcher(css);
        while (m.find()) {
            m.appendReplacement(sb, ':' + m.group(1).toLowerCase() + '(');
        }
        m.appendTail(sb);
        css = sb.toString();

        // lower case some common function that can be values
        // NOTE: rgb() isn't useful as we replace with #hex later, as well as and() is already done for us right after this
        sb = new StringBuilder();
        p = LOWERCASE_FUNCTIONS2;
        m = p.matcher(css);
        while (m.find()) {
            m.appendReplacement(sb, m.group(1) + m.group(2).toLowerCase());
        }
        m.appendTail(sb);
        css = sb.toString();

        // Put the space back in some cases, to support stuff like
        // @media screen and (-webkit-min-device-pixel-ratio:0){
        css = RESTORE_AND_SPACE.matcher(css).replaceAll("and (");

        // Remove the spaces after the things that should not have spaces after them.
        css = TRAILING_SPACES.matcher(css).replaceAll("$1");

        // remove unnecessary semicolons
        css = UNNECESSARY_SEMICOLON.matcher(css).replaceAll("}");

        // Replace 0(px,em) with 0. (don't replace seconds are they are needed for transitions to be valid)
        String oldCss;
        p = ZERO_UNITS;
        do {
          oldCss = css;
          m = p.matcher(css);
          css = m.replaceAll("$1$20");
        } while (!(css.equals(oldCss)));
        
        // We do the same with % but don't replace the 0% in keyframes
        p = ZERO_PERCENTAGE;
        do {
          oldCss = css;
          m = p.matcher(css);
          css = m.replaceAll("$1$20");
        } while (!(css.equals(oldCss)));
        
        //Replace the keyframe 100% step with 'to' which is shorter
        p = KEYFRAME_TO;
        do {
          oldCss = css;
          m = p.matcher(css);
          css = m.replaceAll("$1to{");
        } while (!(css.equals(oldCss)));

        // Replace 0(px,em,%) with 0 inside groups (e.g. -MOZ-RADIAL-GRADIENT(CENTER 45DEG, CIRCLE CLOSEST-SIDE, ORANGE 0%, RED 100%))
        p = ZERO_UNITS_GROUPS;
        do {
          oldCss = css;
          m = p.matcher(css);
          css = m.replaceAll("($10");
        } while (!(css.equals(oldCss)));

        // Replace x.0(px,em,%) with x(px,em,%).
        css = UNNECESSARY_DOT_ZERO1.matcher(css).replaceAll("$1$2");

        // Replace .0(px,em,%) with 0(px,em,%).
        css = UNNECESSARY_DOT_ZERO2.matcher(css).replaceAll("$1\\0$2");

        // Replace 0 0 0 0; with 0.
        css = ZERO_VALUE_1.matcher(css).replaceAll(":0$1");
        css = ZERO_VALUE_2.matcher(css).replaceAll(":0$1");
        css = ZERO_VALUE_3.matcher(css).replaceAll(":0$1");


        // Replace background-position:0; with background-position:0 0;
        // same for transform-origin
        sb = new StringBuilder();
        p = BACKGROUND_POSITION_TRANSFORM_ORIGIN;
        m = p.matcher(css);
        while (m.find()) {
            m.appendReplacement(sb, m.group(1).toLowerCase() + ":0 0" + m.group(2));
        }
        m.appendTail(sb);
        css = sb.toString();

        // Replace 0.6 to .6, but only when preceded by : or a white-space
        css = RESTORE_DOT_ZERO.matcher(css).replaceAll("$1.$2");

        // Shorten colors from rgb(51,102,153) to #336699
        // This makes it more likely that it'll get further compressed in the next step.
        p = RGB;
        m = p.matcher(css);
        sb = new StringBuilder();
        while (m.find()) {
            String[] rgbcolors = m.group(1).split(",");
            StringBuffer hexcolor = new StringBuffer("#");
            for (i = 0; i < rgbcolors.length; i++) {
                int val = Integer.parseInt(rgbcolors[i]);
                if (val < 16) {
                    hexcolor.append("0");
                }

                // If someone passes an RGB value that's too big to express in two characters, round down.
                // Probably should throw out a warning here, but generating valid CSS is a bigger concern.
                if (val > 255) {
                    val = 255;
                }
                hexcolor.append(Integer.toHexString(val));
            }
            m.appendReplacement(sb, hexcolor.toString());
        }
        m.appendTail(sb);
        css = sb.toString();

        // Shorten colors from #AABBCC to #ABC. Note that we want to make sure
        // the color is not preceded by either ", " or =. Indeed, the property
        //     filter: chroma(color="#FFFFFF");
        // would become
        //     filter: chroma(color="#FFF");
        // which makes the filter break in IE.
        // We also want to make sure we're only compressing #AABBCC patterns inside { }, not id selectors ( #FAABAC {} )
        // We also want to avoid compressing invalid values (e.g. #AABBCCD to #ABCD)
        p = HEX_COLORS;

        m = p.matcher(css);
        sb = new StringBuilder();
        int index = 0;

        while (m.find(index)) {

            sb.append(css.substring(index, m.start()));

            boolean isFilter = (m.group(1) != null && !"".equals(m.group(1)));

            if (isFilter) {
                // Restore, as is. Compression will break filters
                sb.append(m.group(1).concat("#").concat(m.group(2)).concat(m.group(3)).concat(m.group(4)).concat(m.group(5)).concat(m.group(6)).concat(m.group(7)));
            } else {
                if( m.group(2).equalsIgnoreCase(m.group(3)) &&
                    m.group(4).equalsIgnoreCase(m.group(5)) &&
                    m.group(6).equalsIgnoreCase(m.group(7))) {

                    // #AABBCC pattern
                    sb.append("#".concat(m.group(3)).concat(m.group(5)).concat(m.group(7)).toLowerCase());

                } else {

                    // Non-compressible color, restore, but lower case.
                    sb.append("#".concat(m.group(2)).concat(m.group(3)).concat(m.group(4)).concat(m.group(5) + m.group(6) + m.group(7)).toLowerCase());
                }
            }

            index = m.end(7);
        }

        sb.append(css.substring(index));
        css = sb.toString();

        // Replace #f00 -> red
        css = COLOR_RED.matcher(css).replaceAll("$1red$3");
        // Replace other short color keywords
        css = COLOR_NAVY.matcher(css).replaceAll("$1navy$3");
        css = COLOR_GRAY.matcher(css).replaceAll("$1gray$3");
        css = COLOR_OLIVE.matcher(css).replaceAll("$1olive$3");
        css = COLOR_PURPLE.matcher(css).replaceAll("$1purple$3");
        css = COLOR_SILVER.matcher(css).replaceAll("$1silver$3");
        css = COLOR_TEAL.matcher(css).replaceAll("$1teal$3");
        css = COLOR_ORANGE.matcher(css).replaceAll("$1orange$3");
        css = COLOR_MAROON.matcher(css).replaceAll("$1maroon$3");

        // border: none -> border:0
        sb = new StringBuilder();
        p = NONE;
        m = p.matcher(css);
        while (m.find()) {
            m.appendReplacement(sb, m.group(1).toLowerCase().concat(":0").concat(m.group(2)));
        }
        m.appendTail(sb);
        css = sb.toString();

        // TODO: Why are we doing this again?
        // shorter opacity IE filter
        css = css.replaceAll("(?i)progid:DXImageTransform.Microsoft.Alpha\\(Opacity=", "alpha(opacity=");

        // Find a fraction that is used for Opera's -o-device-pixel-ratio query
        // Add token to add the "\" back in later
        css = OPERA_DEVICE_PIXEL_RATIO.matcher(css).replaceAll("($1:$2___YUI_QUERY_FRACTION___$3)");

        // Remove empty rules.
        css = EMPTY_RULE.matcher(css).replaceAll("");

        // Add "\" back to fix Opera -o-device-pixel-ratio query
        css = css.replaceAll("___YUI_QUERY_FRACTION___", "/");

        // Replace multiple semi-colons in a row by a single one
        // See SF bug #1980989
        css = MULTI_SEMICOLON.matcher(css).replaceAll(";");

        // restore preserved comments and strings
        for(i = 0, max = preservedTokens.size(); i < max; i++) {
            css = css.replace("___YUICSSMIN_PRESERVED_TOKEN_" + i + "___", preservedTokens.get(i).toString());
        }
        
        // Add spaces back in between operators for css calc function
        // https://developer.mozilla.org/en-US/docs/Web/CSS/calc
        // Added by Eric Arnol-Martin (earnolmartin@gmail.com)
        sb = new StringBuilder();
        p = CALC;
        m = p.matcher(css);
        while (m.find()) {
            String s = m.group();
            s = CALC_PLUS.matcher(s).replaceAll(" + ");
            s = CALC_MINUS.matcher(s).replaceAll(" - ");
            s = CALC_MULTI.matcher(s).replaceAll(" * ");
            s = CALC_DIV.matcher(s).replaceAll(" / ");
            
            m.appendReplacement(sb, s);
        }
        m.appendTail(sb);
        css = sb.toString(); 

        // Trim the final string (for any leading or trailing white spaces)
        return css.trim();
    }
}
